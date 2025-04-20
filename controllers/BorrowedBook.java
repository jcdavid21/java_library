package controllers;

import java.sql.Date;

public class BorrowedBook {
    private int borrowId;
    private String studentId;
    private String studentName;
    private int bookId;
    private String bookTitle;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status;
    private double penaltyFee;
    
    public BorrowedBook(int borrowId, String studentId, String studentName, int bookId, 
                        String bookTitle, Date borrowDate, Date dueDate, Date returnDate, 
                        String status, double penaltyFee) {
        this.borrowId = borrowId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.penaltyFee = penaltyFee;
    }
    
    // Getters
    public int getBorrowId() {
        return borrowId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public Date getBorrowDate() {
        return borrowDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public Date getReturnDate() {
        return returnDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public double getPenaltyFee() {
        return penaltyFee;
    }
    
    // Setters
    public void setBorrowId(int borrowId) {
        this.borrowId = borrowId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setPenaltyFee(double penaltyFee) {
        this.penaltyFee = penaltyFee;
    }
}