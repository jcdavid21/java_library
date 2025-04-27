package models;

import java.time.LocalDateTime;

/**
 * Model class representing a library visit log entry
 */
public class VisitLog {
    private int visitId;
    private String visitorType;  // "Student" or "Employee"
    private String visitorId;    // Student ID or Employee ID
    private String visitorName;  // Student Name or Employee Name
    private LocalDateTime visitDate;
    private String purpose;
    
    /**
     * Constructor for creating a new visit log
     * 
     * @param visitId The unique ID of the visit
     * @param visitorType The type of visitor (Student or Employee)
     * @param visitorId The ID of the visitor
     * @param visitorName The name of the visitor
     * @param visitDate The date and time of the visit
     * @param purpose The purpose of the visit
     */
    public VisitLog(int visitId, String visitorType, String visitorId, String visitorName, LocalDateTime visitDate, String purpose) {
        this.visitId = visitId;
        this.visitorType = visitorType;
        this.visitorId = visitorId;
        this.visitorName = visitorName;
        this.visitDate = visitDate;
        this.purpose = purpose;
    }
    
    /**
     * No-arg constructor for JavaFX and other frameworks
     */
    public VisitLog() {
    }
    
    /**
     * Gets the visit ID
     * 
     * @return The unique ID of the visit
     */
    public int getVisitId() {
        return visitId;
    }
    
    /**
     * Sets the visit ID
     * 
     * @param visitId The unique ID of the visit
     */
    public void setVisitId(int visitId) {
        this.visitId = visitId;
    }
    
    /**
     * Gets the visitor type
     * 
     * @return The type of visitor (Student or Employee)
     */
    public String getVisitorType() {
        return visitorType;
    }
    
    /**
     * Sets the visitor type
     * 
     * @param visitorType The type of visitor (Student or Employee)
     */
    public void setVisitorType(String visitorType) {
        this.visitorType = visitorType;
    }
    
    /**
     * Gets the visitor ID
     * 
     * @return The ID of the visitor
     */
    public String getVisitorId() {
        return visitorId;
    }
    
    /**
     * Sets the visitor ID
     * 
     * @param visitorId The ID of the visitor
     */
    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }
    
    /**
     * Gets the visitor name
     * 
     * @return The name of the visitor
     */
    public String getVisitorName() {
        return visitorName;
    }
    
    /**
     * Sets the visitor name
     * 
     * @param visitorName The name of the visitor
     */
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }
    
    /**
     * Gets the visit date and time
     * 
     * @return The date and time of the visit
     */
    public LocalDateTime getVisitDate() {
        return visitDate;
    }
    
    /**
     * Sets the visit date and time
     * 
     * @param visitDate The date and time of the visit
     */
    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }
    
    /**
     * Gets the purpose of the visit
     * 
     * @return The purpose of the visit
     */
    public String getPurpose() {
        return purpose;
    }
    
    /**
     * Sets the purpose of the visit
     * 
     * @param purpose The purpose of the visit
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    /**
     * Returns a string representation of the visit log
     * 
     * @return A string representation of the visit log
     */
    @Override
    public String toString() {
        return "VisitLog{" +
                "visitId=" + visitId +
                ", visitorType='" + visitorType + '\'' +
                ", visitorId='" + visitorId + '\'' +
                ", visitorName='" + visitorName + '\'' +
                ", visitDate=" + visitDate +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}