package controllers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BookCategory {
    private final IntegerProperty categoryId;
    private final StringProperty categoryName;
    private final IntegerProperty totalBooks;
    
    public BookCategory(int categoryId, String categoryName, int totalBooks) {
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.categoryName = new SimpleStringProperty(categoryName);
        this.totalBooks = new SimpleIntegerProperty(totalBooks);
    }
    
    // Category ID getters
    public int getCategoryId() {
        return categoryId.get();
    }
    
    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }
    
    // Category Name getters and setters
    public String getCategoryName() {
        return categoryName.get();
    }
    
    public void setCategoryName(String name) {
        this.categoryName.set(name);
    }
    
    public StringProperty categoryNameProperty() {
        return categoryName;
    }
    
    // Total Books getters and setters
    public int getTotalBooks() {
        return totalBooks.get();
    }
    
    public void setTotalBooks(int count) {
        this.totalBooks.set(count);
    }
    
    public IntegerProperty totalBooksProperty() {
        return totalBooks;
    }
    
    @Override
    public String toString() {
        return categoryName.get();
    }
}