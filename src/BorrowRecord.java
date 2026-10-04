import java.time.LocalDate;

public class BorrowRecord implements Identifiable {

    private int id;
    private int bookId;
    private int memberId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double lateFee;

    public BorrowRecord(int id, int bookId, int memberId, LocalDate borrowDate, LocalDate dueDate) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", lateFee=" + lateFee +
                '}';
    }
}
