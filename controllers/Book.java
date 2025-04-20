package controllers;

import java.sql.Date;

public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;
    private int reservedCopies;
    private int lostDamagedCopies;
    private Date addedDate;
    private Date lastBorrowedDate;
    private int timesBorrowed;

    public Book(int bookId, String isbn, String title, String author, String category, 
                int totalCopies, int availableCopies, int reservedCopies, int lostDamagedCopies, 
                Date addedDate, Date lastBorrowedDate, int timesBorrowed) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.reservedCopies = reservedCopies;
        this.lostDamagedCopies = lostDamagedCopies;
        this.addedDate = addedDate;
        this.lastBorrowedDate = lastBorrowedDate;
        this.timesBorrowed = timesBorrowed;
    }

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getReservedCopies() {
        return reservedCopies;
    }

    public void setReservedCopies(int reservedCopies) {
        this.reservedCopies = reservedCopies;
    }

    public int getLostDamagedCopies() {
        return lostDamagedCopies;
    }

    public void setLostDamagedCopies(int lostDamagedCopies) {
        this.lostDamagedCopies = lostDamagedCopies;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getLastBorrowedDate() {
        return lastBorrowedDate;
    }

    public void setLastBorrowedDate(Date lastBorrowedDate) {
        this.lastBorrowedDate = lastBorrowedDate;
    }

    public int getTimesBorrowed() {
        return timesBorrowed;
    }

    public void setTimesBorrowed(int timesBorrowed) {
        this.timesBorrowed = timesBorrowed;
    }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}