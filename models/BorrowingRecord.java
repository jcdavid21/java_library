package models;

import java.util.Date;

public class BorrowingRecord {
    private String title;
    private String studentName;
    private Date borrowDate;

    public BorrowingRecord(String title, String studentName, Date borrowDate) {
        this.title = title;
        this.studentName = studentName;
        this.borrowDate = borrowDate;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    @Override
    public String toString() {
        return "BorrowingRecord{" +
                "title='" + title + '\'' +
                ", studentName='" + studentName + '\'' +
                ", borrowDate=" + borrowDate +
                '}';
    }
}