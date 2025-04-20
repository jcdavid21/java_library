package user_account;

import java.time.LocalDate;
import javafx.scene.control.Button;

public class BorrowedBook {
    private int borrowId;
    private int bookId;
    private String bookTitle;
    private String author;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String status;
    private double penaltyFee;
    private Button returnButton;

    public BorrowedBook(int borrowId, int bookId, String bookTitle, String author, 
                        LocalDate borrowDate, LocalDate dueDate, String status, double penaltyFee) {
        this.borrowId = borrowId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.author = author;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.penaltyFee = penaltyFee;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
    
    public double getPenaltyFee() {
        return penaltyFee;
    }
    
    public void setPenaltyFee(double penaltyFee) {
        this.penaltyFee = penaltyFee;
    }
    
    // Getter and setter for returnButton (needed for TableView)
    public Button getReturnButton() {
        return returnButton;
    }
    
    public void setReturnButton(Button returnButton) {
        this.returnButton = returnButton;
    }
    
    // Optional: Add a toString method for debugging
    @Override
    public String toString() {
        return "BorrowedBook{" +
               "borrowId=" + borrowId +
               ", bookId=" + bookId +
               ", bookTitle='" + bookTitle + '\'' +
               ", author='" + author + '\'' +
               ", borrowDate=" + borrowDate +
               ", dueDate=" + dueDate +
               ", status='" + status + '\'' +
               ", penaltyFee=" + penaltyFee +
               '}';
    }
}