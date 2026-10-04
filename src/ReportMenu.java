import java.util.List;


public class ReportMenu extends BaseMenu {

    private final ReportService reports;

    public ReportMenu(LibraryService service, ReportService reports, ConsoleHelper input, Member user) {
        super(service, input, user);
        this.reports = reports;
    }

    public void run() {
        runMenu("--- Reports ---", List.of(
                option(1, "Total borrowed books", EVERYONE, this::showBorrowedCount),
                option(2, "Books grouped by starting letter of the title", EVERYONE, this::showByLetter),
                option(3, "Books grouped by publication date range", EVERYONE, this::showByDateRange),
                option(4, "Books per publisher", EVERYONE, this::showPerPublisher),
                back()));
    }

    private void showBorrowedCount() {
        System.out.println("\nBorrowing report");
        System.out.println("  Total borrowings ever made : " + reports.totalBorrowedCount());
        System.out.println("  Books out right now        : " + reports.currentlyBorrowedCount());
    }

    private void showByLetter() {
        System.out.println("\nBooks by starting letter");
        printMap(reports.booksByStartingLetter(), "(no books yet)",
                (letter, count) -> System.out.println("  " + letter + " : " + count));
    }

    private void showByDateRange() {
        System.out.println("\nBooks by publication date range");
        printMap(reports.booksByDateRange(), "(no books yet)",
                (range, count) -> System.out.printf("  %-15s : %d%n", range, count));
    }

    private void showPerPublisher() {
        System.out.println("\nBooks per publisher");
        printMap(reports.booksPerPublisher(), "(no books linked to a publisher yet)",
                (publisherId, count) -> System.out.println("  Publisher " + publisherId + " ("
                        + service.getMember(publisherId).getName() + ") : " + count));
    }
}
