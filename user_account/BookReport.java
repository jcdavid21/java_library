package user_account;

import java.time.LocalDate;

public class BookReport {
    private int reportId;
    private int bookId;
    private String bookTitle;
    private String studentId;
    private String studentName;
    private LocalDate reportDate;
    private String issueType;
    private double penaltyFee;
    private String status;
    
    public BookReport(int reportId, int bookId, String bookTitle, String studentId, String studentName, 
                     LocalDate reportDate, String issueType, double penaltyFee, String status) {
        this.reportId = reportId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.reportDate = reportDate;
        this.issueType = issueType;
        this.penaltyFee = penaltyFee;
        this.status = status;
    }
    
    // Getters and setters
    public int getReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public LocalDate getReportDate() {
        return reportDate;
    }
    
    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }
    
    public String getIssueType() {
        return issueType;
    }
    
    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }
    
    public double getPenaltyFee() {
        return penaltyFee;
    }
    
    public void setPenaltyFee(double penaltyFee) {
        this.penaltyFee = penaltyFee;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return bookTitle;
    }
}