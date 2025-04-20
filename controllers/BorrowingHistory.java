package controllers;

public class BorrowingHistory {
    private int borrowHistoryId;
    private String studentId;
    private String studentName;
    private int totalBorrowedBooks;
    private int totalReservedBooks;
    
    public BorrowingHistory(int borrowHistoryId, String studentId, String studentName, 
                           int totalBorrowedBooks, int totalReservedBooks) {
        this.borrowHistoryId = borrowHistoryId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalBorrowedBooks = totalBorrowedBooks;
        this.totalReservedBooks = totalReservedBooks;
    }
    
    // Getters
    public int getBorrowHistoryId() {
        return borrowHistoryId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public int getTotalBorrowedBooks() {
        return totalBorrowedBooks;
    }
    
    public int getTotalReservedBooks() {
        return totalReservedBooks;
    }
    
    // Setters
    public void setBorrowHistoryId(int borrowHistoryId) {
        this.borrowHistoryId = borrowHistoryId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public void setTotalBorrowedBooks(int totalBorrowedBooks) {
        this.totalBorrowedBooks = totalBorrowedBooks;
    }
    
    public void setTotalReservedBooks(int totalReservedBooks) {
        this.totalReservedBooks = totalReservedBooks;
    }
}