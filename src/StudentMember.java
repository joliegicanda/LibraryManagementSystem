import java.time.LocalDate;

public class StudentMember extends Member {

    private static final double FEE_PER_DAY = 0.50;

    private String school;

    public StudentMember(int id, String name, String email, String phone,
                         LocalDate joinDate, String school) {
        super(id, name, email, phone, joinDate);
        setSchool(school);
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = Helpers.orDefault(school, "Not given");
    }

    @Override
    public double calculateLateFees(int daysLate) {
        return Math.max(0, daysLate) * FEE_PER_DAY;
    }

    @Override
    public int getMaxBooks() {
        return 3;
    }

    @Override
    public int getLoanDays() {
        return 14;
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String getExtraInfo() {
        return school;
    }

    @Override
    public String toString() {
        return super.toString() + " | School: " + school;
    }
}
