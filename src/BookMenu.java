import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BookMenu extends BaseMenu {

    public BookMenu(LibraryService service, ConsoleHelper input, Member user) {
        super(service, input, user);
    }

    public void run() {
        runMenu("--- Books ---", List.of(
                option(1, "View all books", EVERYONE, this::viewBooks),
                option(2, "Search books", EVERYONE, this::searchBooks),
                option(3, "Add a book", PUBLISHER_OR_LIBRARIAN, this::addOneBook),
                option(4, "Add multiple books for a Publisher ID", PUBLISHER_OR_LIBRARIAN, this::addManyBooks),
                option(5, "View books by Publisher ID", EVERYONE, this::viewByPublisher),
                back()));
    }


    private void viewByPublisher() {
        int publisherId = Helpers.either(isPublisher(),
                user::getId,
                () -> input.readInt("Publisher ID: ", 1, Integer.MAX_VALUE));
        String publisherName = service.getMember(publisherId).getName();

        List<Book> publications = new ArrayList<>();
        Helpers.copyBooks(service.getBooksByPublisher(publisherId), publications);

        System.out.println("\nBooks by " + publisherName + " (" + publications.size() + "):");
        Helpers.printAll(publications);
        System.out.println("  Book IDs: " + Helpers.idsOf(publications));
    }

    private void viewBooks() {
        List<Book> books = service.getAllBooks();
        System.out.println("\nAll books (" + books.size() + "):");
        Helpers.printAll(books);
        long available = Helpers.countMatching(books, Book::isAvailable);
        System.out.println("  " + available + " of " + books.size() + " available right now.");
    }

    private void searchBooks() {
        String keyword = input.readRequired("Search by title, author or category: ");
        List<Book> found = service.searchBooks(keyword);
        System.out.println("\nFound " + found.size() + " book(s):");
        Helpers.printAll(found);
    }

    private void addOneBook() {
        System.out.println("\nAdd a new book");

        int publisherId = Helpers.either(isPublisher(),
                user::getId,
                () -> input.readInt("Publisher ID (0 if none): ", 0, Integer.MAX_VALUE));

        String title = input.readRequired("Title: ");
        String author = input.readRequired("Author: ");
        String isbn = input.readLine("ISBN (optional): ");
        String category = input.readLine("Category (optional): ");
        LocalDate date = input.readPastDate("Publication date (yyyy-MM-dd): ");

        service.addBook(title, author, isbn, category, date, publisherId,
                book -> System.out.println("\n  Saved: " + book));
    }

    private void addManyBooks() {
        System.out.println("\nAdd multiple books for one publisher");

        int publisherId = Helpers.either(isPublisher(),
                () -> {
                    System.out.println("Using your Publisher ID: " + user.getId());
                    return user.getId();
                },
                () -> input.readInt("Publisher ID: ", 1, Integer.MAX_VALUE));

        int howMany = input.readInt("How many books do you want to add (1-20)? ", 1, 20);
        List<Book> newBooks = IntStream.rangeClosed(1, howMany)
                .mapToObj(number -> readBook(number, howMany, publisherId))
                .collect(Collectors.toList());

        service.addMultipleBooks(publisherId, newBooks,
                book -> System.out.println("  Saved: " + book));
        System.out.println("\n  Done. " + newBooks.size() + " book(s) added for publisher " + publisherId + ".");
    }

    private Book readBook(int number, int total, int publisherId) {
        System.out.println("\nBook " + number + " of " + total);
        String title = input.readRequired("Title: ");
        String author = input.readRequired("Author: ");
        String isbn = input.readLine("ISBN (optional): ");
        String category = input.readLine("Category (optional): ");
        LocalDate date = input.readPastDate("Publication date (yyyy-MM-dd): ");
        return new Book(0, title, author, isbn, category, date, publisherId);
    }
}
