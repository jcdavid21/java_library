package user_account;

import java.time.LocalDate;

public class PenaltyFine {
    private String studentId;
    private String bookTitle;
    private String issueType;
    private double fineAmount;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;

    public PenaltyFine(String studentId, String bookTitle, String issueType, double fineAmount, 
                       LocalDate dueDate, LocalDate returnDate, String status) {
        this.studentId = studentId;
        this.bookTitle = bookTitle;
        this.issueType = issueType;
        this.fineAmount = fineAmount;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getIssueType() {
        return issueType;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "PenaltyFine{" +
                "studentId='" + studentId + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", issueType='" + issueType + '\'' +
                ", fineAmount=" + fineAmount +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                '}';
    }
}