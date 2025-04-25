package models;

public class PopularBook {
    private String title;
    private int borrowCount;

    public PopularBook(String title, int borrowCount) {
        this.title = title;
        this.borrowCount = borrowCount;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    @Override
    public String toString() {
        return "PopularBook{" +
                "title='" + title + '\'' +
                ", borrowCount=" + borrowCount +
                '}';
    }
}