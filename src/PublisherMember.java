import java.time.LocalDate;

public class PublisherMember extends Member {

    private static final double FEE_PER_DAY = 1.00;

    private String companyName;

    public PublisherMember(int id, String name, String email, String phone,
                           LocalDate joinDate, String companyName) {
        super(id, name, email, phone, joinDate);
        setCompanyName(companyName);
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = Helpers.orDefault(companyName, "Independent");
    }

    @Override
    public double calculateLateFees(int daysLate) {
        return Math.max(0, daysLate) * FEE_PER_DAY;
    }

    @Override
    public int getMaxBooks() {
        return 10;
    }

    @Override
    public int getLoanDays() {
        return 21;
    }

    @Override
    public String getRole() {
        return "Publisher";
    }

    @Override
    public String getExtraInfo() {
        return companyName;
    }

    @Override
    public String toString() {
        return super.toString() + " | Company: " + companyName;
    }
}
