import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class Member implements Identifiable {

    private int id;
    private String name;
    private String email;
    private String phone;
    private LocalDate joinDate;

    private final Set<Integer> borrowedBookIds = new HashSet<>();

    public Member(int id, String name, String email, String phone, LocalDate joinDate) {
        this.id = id;
        setName(name);
        setEmail(email);
        setPhone(phone);
        this.joinDate = joinDate;
    }

    public abstract double calculateLateFees(int daysLate);

    public abstract int getMaxBooks();

    public abstract int getLoanDays();

    public abstract String getRole();

    public abstract String getExtraInfo();

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Helpers.required(name, "Name cannot be empty.");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Helpers.require(Helpers.isValidEmail(email),
                () -> new IllegalArgumentException("Email address is not valid: " + email));
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Helpers.orDefault(phone, "N/A");
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Set<Integer> getBorrowedBookIds() {
        return new HashSet<>(borrowedBookIds);   // copy so nobody changes it by accident
    }

    public void addBorrowedBook(int bookId) {
        borrowedBookIds.add(bookId);
    }

    public void removeBorrowedBook(int bookId) {
        borrowedBookIds.remove(bookId);
    }

    public boolean canBorrowMore() {
        return borrowedBookIds.size() < getMaxBooks();
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) | %s | %s | Joined: %s | Books out: %d/%d",
                id, name, getRole(), email, phone, joinDate,
                borrowedBookIds.size(), getMaxBooks());
    }

    @FunctionalInterface
    private interface MemberBuilder {
        Member build(int id, String name, String email, String phone, LocalDate joinDate, String extra);
    }


    private static final Map<String, MemberBuilder> BUILDERS = Map.of(
            "student", StudentMember::new,
            "publisher", PublisherMember::new,
            "librarian", LibrarianMember::new);

    public static Member create(int id, String role, String name, String email,
                                String phone, LocalDate joinDate, String extra) {
        MemberBuilder builder = Optional.ofNullable(role)
                .map(text -> text.trim().toLowerCase())
                .map(BUILDERS::get)
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role));
        return builder.build(id, name, email, phone, joinDate, extra);
    }
}
