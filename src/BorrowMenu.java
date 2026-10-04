import java.util.List;
import java.util.Optional;

public class BorrowMenu extends BaseMenu {

    public BorrowMenu(LibraryService service, ConsoleHelper input, Member user) {
        super(service, input, user);
    }

    public void run() {
        runMenu("--- Borrowings ---", List.of(
                option(1, "Borrow a book", EVERYONE, this::borrowBook),
                option(2, "Return a book", EVERYONE, this::returnBook),
                option(3, "View active loans", EVERYONE, this::viewLoans),
                option(4, "Register a new member", LIBRARIAN_ONLY, this::registerMember),
                option(5, "View all members", LIBRARIAN_ONLY, this::viewMembers),
                back()));
    }

    private void borrowBook() {
        int memberId = Helpers.either(isLibrarian(),
                () -> input.readInt("Member ID (your own ID is " + user.getId() + "): ", 1, Integer.MAX_VALUE),
                user::getId);
        int bookId = input.readInt("ID of the book to borrow: ", 1, Integer.MAX_VALUE);

        BorrowRecord record = service.borrowBook(memberId, bookId);
        System.out.println("\n  Done! \"" + service.getBook(bookId).getTitle()
                + "\" is due on " + record.getDueDate() + ".");
    }

    private void returnBook() {
        int bookId = input.readInt("ID of the book being returned: ", 1, Integer.MAX_VALUE);

        // normal members can only return their own books
        boolean isMine = service.getActiveLoansForMember(user.getId()).stream()
                .anyMatch(loan -> loan.getBookId() == bookId);
        Helpers.require(isLibrarian() || isMine,
                () -> new LibraryException("You do not have that book on loan."));

        BorrowRecord record = service.returnBook(bookId);
        System.out.println("\n  Book returned on " + record.getReturnDate() + ".");
        Optional.of(record)
                .filter(returned -> returned.getLateFee() > 0)
                .ifPresentOrElse(
                        returned -> System.out.printf("  It was late. Late fee: %.2f%n", returned.getLateFee()),
                        () -> System.out.println("  No late fee."));
    }

    private void viewLoans() {
        List<BorrowRecord> loans = Helpers.either(isLibrarian(),
                service::getActiveLoans,
                () -> service.getActiveLoansForMember(user.getId()));
        String owner = Helpers.either(isLibrarian(), () -> "All active loans", () -> "Your active loans");

        System.out.println("\n" + owner + " (" + loans.size() + "):");
        printList(loans, "(no active loans)", service::describeLoan);
    }

    private void registerMember() {
        System.out.println("\nRegister a new member");
        Member member = Main.readAndRegister(service, input);
        System.out.println("\n  Member registered. Their ID is " + member.getId() + ".");
        System.out.println("  " + member);
    }

    private void viewMembers() {
        List<Member> members = service.getAllMembers();
        System.out.println("\nAll members (" + members.size() + "):");
        printList(members, "(no members)", Member::toString);
    }
}
