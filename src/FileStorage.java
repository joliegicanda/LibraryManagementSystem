import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileStorage {

    String booksHeader = "# id|title|author|isbn|category|publicationDate|publisherId";
    String membersHeader = "# id|role|name|email|phone|joinDate|extraInfo";
    String borrowingsHeader = "# id|bookId|memberId|borrowDate|dueDate|returnDate|lateFee";

    private static final List<String> SAMPLE_MEMBERS = List.of(
            "1|Librarian|Grace Uwase|grace.uwase@library.org|0788000001|2026-01-10|STAFF-001",
            "2|Publisher|Kigali Tech Press|info@kigalitechpress.com|0788000002|2026-02-01|Kigali Tech Press Ltd",
            "3|Student|Eric Mugisha|eric.mugisha@student.edu|0788000003|2026-03-15|Kigali Secondary School");

    private final Path booksFile = Paths.get("books.txt");
    private final Path membersFile = Paths.get("members.txt");
    private final Path borrowingsFile = Paths.get("borrowings.txt");


    private List<String> makeSampleBooks() {
        List<String> books = new ArrayList<>();
        books.add("1|Clean Code|Robert C. Martin|9780132350884|Programming|2008-08-01|2");
        books.add("2|Effective Java|Joshua Bloch|9780134685991|Programming|2018-01-06|2");
        books.add("3|Sapiens|Yuval Noah Harari|9780062316110|History|2015-02-10|0");
        books.add("4|Atomic Habits|James Clear|9780735211292|Self-Help|2018-10-16|0");
        books.add("5|Born a Crime|Trevor Noah|9780399588174|Biography|2016-11-15|0");
        return books;
    }


    public void init() {
        createIfMissing(booksFile, booksHeader, makeSampleBooks());
        createIfMissing(membersFile, membersHeader, SAMPLE_MEMBERS);
        createIfMissing(borrowingsFile, borrowingsHeader, List.of());
    }

    private void createIfMissing(Path file, String header, List<String> startingLines) {
        Optional.of(file)
                .filter(path -> Files.notExists(path))
                .ifPresent(path -> save(path, header, startingLines, line -> line));
    }


    public List<Book> loadBooks() {
        return load(booksFile, this::toBook);
    }

    public void saveBooks(Collection<Book> books) {
        save(booksFile, booksHeader, books, this::bookLine);
    }

    private Book toBook(String[] p) {
        return new Book(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4],
                LocalDate.parse(p[5]), Integer.parseInt(p[6]));
    }

    private String bookLine(Book b) {
        return b.getId() + "|" + clean(b.getTitle()) + "|" + clean(b.getAuthor()) + "|"
                + clean(b.getIsbn()) + "|" + clean(b.getCategory()) + "|"
                + b.getPublicationDate() + "|" + b.getPublisherId();
    }

    public List<Member> loadMembers() {
        return load(membersFile, this::toMember);
    }

    public void saveMembers(Collection<Member> members) {
        save(membersFile, membersHeader, members, this::memberLine);
    }

    private Member toMember(String[] p) {
        return Member.create(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4],
                LocalDate.parse(p[5]), p[6]);
    }

    private String memberLine(Member m) {
        return m.getId() + "|" + m.getRole() + "|" + clean(m.getName()) + "|"
                + clean(m.getEmail()) + "|" + clean(m.getPhone()) + "|"
                + m.getJoinDate() + "|" + clean(m.getExtraInfo());
    }

    public List<BorrowRecord> loadBorrowings() {
        return load(borrowingsFile, this::toBorrowRecord);
    }

    public void saveBorrowings(Collection<BorrowRecord> records) {
        save(borrowingsFile, borrowingsHeader, records, this::borrowingLine);
    }

    private BorrowRecord toBorrowRecord(String[] p) {
        BorrowRecord record = new BorrowRecord(Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                Integer.parseInt(p[2]), LocalDate.parse(p[3]), LocalDate.parse(p[4]));
        Optional.of(p[5])
                .filter(text -> !text.isBlank())
                .map(LocalDate::parse)
                .ifPresent(record::setReturnDate);
        record.setLateFee(Double.parseDouble(p[6]));
        return record;
    }

    private String borrowingLine(BorrowRecord r) {
        String returned = Optional.ofNullable(r.getReturnDate()).map(LocalDate::toString).orElse("");
        return r.getId() + "|" + r.getBookId() + "|" + r.getMemberId() + "|"
                + r.getBorrowDate() + "|" + r.getDueDate() + "|" + returned + "|" + r.getLateFee();
    }

    private <T> List<T> load(Path file, Function<String[], T> builder) {
        String fileName = file.getFileName().toString();
        return readLines(file).stream()
                .map(line -> parseLine(line, builder, fileName))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private <T> Optional<T> parseLine(String line, Function<String[], T> builder, String fileName) {
        try {
            return Optional.of(builder.apply(line.split("\\|", -1)));
        } catch (RuntimeException e) {
            System.out.println("Warning: skipped a bad line in " + fileName + " -> " + line);
            return Optional.empty();
        }
    }

    private <T> void save(Path file, String header, Collection<T> items, Function<T, String> toLine) {
        List<String> lines = Stream.concat(Stream.of(header), items.stream().map(toLine))
                .collect(Collectors.toList());
        Helpers.ioRun(() -> Files.write(file, lines), "Could not save " + file.getFileName());
    }

    private List<String> readLines(Path file) {
        return Helpers.ioCall(() -> Files.readAllLines(file), "Could not read " + file.getFileName())
                .stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .collect(Collectors.toList());
    }
    private String clean(String text) {
        return text.replace("|", "/").replace("\n", " ").replace("\r", " ");
    }
}