package user_account;

import java.time.LocalDate;

public class BorrowedBookReport {
    private int borrowId;
    private int bookId;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String status;
    
    public BorrowedBookReport(int borrowId, int bookId, String bookTitle, LocalDate borrowDate, LocalDate dueDate, String status) {
        this.borrowId = borrowId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }
    
    // Getters and setters
    public int getBorrowId() {
        return borrowId;
    }
    
    public void setBorrowId(int borrowId) {
        this.borrowId = borrowId;
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