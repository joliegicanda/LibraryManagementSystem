import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
public class LibraryService {

    private final Repository<Book> books = new InMemoryRepository<>();
    private final Repository<Member> members = new InMemoryRepository<>();
    private final Repository<BorrowRecord> borrowings = new InMemoryRepository<>();
    private final FileStorage storage;

    public LibraryService(FileStorage storage) {
        this.storage = storage;
    }

    public void loadAll() {
        loadInto(books, storage.loadBooks(), "book");
        loadInto(members, storage.loadMembers(), "member");
        loadInto(borrowings, storage.loadBorrowings(), "borrowing");

        getActiveLoans().forEach(record -> {
            books.findById(record.getBookId()).ifPresent(book -> book.setAvailable(false));
            members.findById(record.getMemberId()).ifPresent(m -> m.addBorrowedBook(record.getBookId()));
        });
    }

    private <T extends Identifiable> void loadInto(Repository<T> repo, List<? extends T> loaded, String label) {
        loaded.forEach(item -> {
            try {
                repo.add(item);
            } catch (LibraryException e) {
                System.out.println("Warning: skipped duplicate " + label + " with ID " + item.getId());
            }
        });
    }

    public Book addBook(String title, String author, String isbn, String category,
                        LocalDate publicationDate, int publisherId, Callback<Book> onAdded) {
        checkPublisher(publisherId);
        Book book = new Book(books.nextId(), title, author, isbn, category, publicationDate, publisherId);
        books.add(book);
        storage.saveBooks(books.findAll());
        Optional.ofNullable(onAdded).ifPresent(callback -> callback.onDone(book));
        return book;
    }

    public void addMultipleBooks(int publisherId, List<Book> newBooks, Callback<Book> onEachAdded) {
        Helpers.require(publisherId != 0,
                () -> new LibraryException("A publisher ID is needed to add books for a publisher."));
        checkPublisher(publisherId);
        newBooks.forEach(book -> {
            book.setId(books.nextId());
            book.setPublisherId(publisherId);
            books.add(book);
            Optional.ofNullable(onEachAdded).ifPresent(callback -> callback.onDone(book));
        });
        storage.saveBooks(books.findAll());
    }

    private void checkPublisher(int publisherId) {
        Optional.of(publisherId)
                .filter(id -> id != 0)
                .ifPresent(id -> {
                    Member member = getMember(id);
                    Helpers.require(member instanceof PublisherMember,
                            () -> new LibraryException("Member " + id + " (" + member.getName() + ") is not a Publisher."));
                });
    }

    public Book getBook(int bookId) {
        return books.findById(bookId)
                .orElseThrow(() -> new LibraryException("No book found with ID " + bookId + "."));
    }

    public List<Book> getAllBooks() {
        return books.findAll().stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooks(String keyword) {
        String key = keyword.trim().toLowerCase();
        return getAllBooks().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(key)
                        || b.getAuthor().toLowerCase().contains(key)
                        || b.getCategory().toLowerCase().contains(key))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByPublisher(int publisherId) {
        return books.findAll().stream()
                .filter(book -> book.getPublisherId() == publisherId)
                .collect(Collectors.toList());
    }


    public Member registerMember(String role, String name, String email, String phone, String extra) {
        Helpers.require(members.findAll().stream().noneMatch(m -> m.getEmail().equalsIgnoreCase(email.trim())),
                () -> new LibraryException("A member with the email " + email + " already exists."));
        Member member = Member.create(members.nextId(), role, name, email, phone, LocalDate.now(), extra);
        members.add(member);
        storage.saveMembers(members.findAll());
        return member;
    }

    public Member getMember(int memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new LibraryException("No member found with ID " + memberId + "."));
    }

    public boolean hasMembers() {
        return members.count() > 0;
    }

    public List<Member> getAllMembers() {
        return members.findAll().stream()
                .sorted(Comparator.comparingInt(Member::getId))
                .collect(Collectors.toList());
    }


    public BorrowRecord borrowBook(int memberId, int bookId) {
        Member member = getMember(memberId);
        Book book = getBook(bookId);

        Helpers.require(book.isAvailable(),
                () -> new LibraryException("\"" + book.getTitle() + "\" is already borrowed."));
        Helpers.require(member.canBorrowMore(),
                () -> new LibraryException(member.getName() + " has reached the limit of "
                        + member.getMaxBooks() + " books."));

        LocalDate today = LocalDate.now();
        BorrowRecord record = new BorrowRecord(borrowings.nextId(), bookId, memberId,
                today, today.plusDays(member.getLoanDays()));
        borrowings.add(record);
        book.setAvailable(false);
        member.addBorrowedBook(bookId);
        storage.saveBorrowings(borrowings.findAll());
        return record;
    }

    public BorrowRecord returnBook(int bookId) {
        return returnBook(bookId, LocalDate.now());
    }

    public BorrowRecord returnBook(int bookId, LocalDate returnDate) {
        BorrowRecord record = getActiveLoans().stream()
                .filter(loan -> loan.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new LibraryException("Book " + bookId + " is not currently borrowed."));

        int daysLate = (int) Math.max(0, ChronoUnit.DAYS.between(record.getDueDate(), returnDate));

        double fee = members.findById(record.getMemberId())
                .map(member -> {
                    member.removeBorrowedBook(bookId);
                    return member.calculateLateFees(daysLate);
                })
                .orElse(0.0);

        record.setReturnDate(returnDate);
        record.setLateFee(fee);
        books.findById(bookId).ifPresent(book -> book.setAvailable(true));
        storage.saveBorrowings(borrowings.findAll());
        return record;
    }

    public List<BorrowRecord> getActiveLoans() {
        return borrowings.findAll().stream()
                .filter(record -> !record.isReturned())
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getActiveLoansForMember(int memberId) {
        return getActiveLoans().stream()
                .filter(record -> record.getMemberId() == memberId)
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getAllBorrowings() {
        return borrowings.findAll();
    }

    public String describeLoan(BorrowRecord record) {
        String title = books.findById(record.getBookId()).map(Book::getTitle).orElse("(deleted book)");
        String who = members.findById(record.getMemberId()).map(Member::getName).orElse("(unknown member)");

        long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
        String status = Helpers.either(daysLate > 0,
                () -> {
                    double fee = members.findById(record.getMemberId())
                            .map(m -> m.calculateLateFees((int) daysLate))
                            .orElse(0.0);
                    return "OVERDUE by " + daysLate + " day(s), fee so far: " + String.format("%.2f", fee);
                },
                () -> "due " + record.getDueDate());

        return String.format("Loan #%d | Book %d: %s | Member %d: %s | Borrowed %s | %s",
                record.getId(), record.getBookId(), title, record.getMemberId(), who,
                record.getBorrowDate(), status);
    }
}
