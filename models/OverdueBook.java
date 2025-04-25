package models;

import java.util.Date;

public class OverdueBook {
    private String title;
    private String studentName;
    private Date dueDate;
    private int daysOverdue;

    public OverdueBook(String title, String studentName, Date dueDate, int daysOverdue) {
        this.title = title;
        this.studentName = studentName;
        this.dueDate = dueDate;
        this.daysOverdue = daysOverdue;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    @Override
    public String toString() {
        return "OverdueBook{" +
                "title='" + title + '\'' +
                ", studentName='" + studentName + '\'' +
                ", dueDate=" + dueDate +
                ", daysOverdue=" + daysOverdue +
                '}';
    }
}