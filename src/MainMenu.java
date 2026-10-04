import java.util.List;
import java.util.Map;

public class MainMenu extends BaseMenu {

    private static final Map<String, String> BOOK_LABELS = Map.of(
            "Librarian", "Book & Publisher Management",
            "Publisher", "Books & My Publications",
            "Student", "Browse Books");

    private static final Map<String, String> BORROW_LABELS = Map.of(
            "Librarian", "Members & Borrowings",
            "Publisher", "Borrow / Return Books",
            "Student", "Borrow / Return Books");

    private final ReportService reports;

    public MainMenu(LibraryService service, ReportService reports, ConsoleHelper input, Member user) {
        super(service, input, user);
        this.reports = reports;
    }

    public void run() {
        String title = "==============================================\n"
                + " Main Menu - " + user.getName() + " (" + user.getRole() + ")\n"
                + "==============================================";

        runMenu(title, List.of(
                option(1, BOOK_LABELS.get(user.getRole()), EVERYONE,
                        () -> new BookMenu(service, input, user).run()),
                option(2, BORROW_LABELS.get(user.getRole()), EVERYONE,
                        () -> new BorrowMenu(service, input, user).run()),
                option(3, "Reports", EVERYONE,
                        () -> new ReportMenu(service, reports, input, user).run()),
                option(4, "My Profile", EVERYONE, this::showProfile),
                option(0, "Exit", EVERYONE,
                        () -> System.out.println("\nGoodbye, " + user.getName() + "! Your data has been saved."))));
    }

    private void showProfile() {
        System.out.println("\nYour profile");
        System.out.println("  " + user);
        System.out.println("  You can borrow up to " + user.getMaxBooks() + " books for "
                + user.getLoanDays() + " days each.");
        System.out.printf("  Late fee for a book that is 1 day late: %.2f%n", user.calculateLateFees(1));
    }
}
