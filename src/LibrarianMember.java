import java.time.LocalDate;

public class LibrarianMember extends Member {

    private String staffNumber;

    public LibrarianMember(int id, String name, String email, String phone,
                           LocalDate joinDate, String staffNumber) {
        super(id, name, email, phone, joinDate);
        setStaffNumber(staffNumber);
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = Helpers.orDefault(staffNumber, "Not given");
    }

    @Override
    public double calculateLateFees(int daysLate) {
        return 0;
    }

    @Override
    public int getMaxBooks() {
        return 20;
    }

    @Override
    public int getLoanDays() {
        return 30;
    }

    @Override
    public String getRole() {
        return "Librarian";
    }

    @Override
    public String getExtraInfo() {
        return staffNumber;
    }

    @Override
    public String toString() {
        return super.toString() + " | Staff No: " + staffNumber;
    }
}
