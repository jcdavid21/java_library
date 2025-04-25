package models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SystemLog {
    private int logId;
    private LocalDateTime timestamp;
    private String userType;
    private String userId;
    private String action; // Field name should match or have appropriate getter

    // Constructor
    public SystemLog(int logId, LocalDateTime timestamp, String userType, String userId, String actionPerformed) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.userType = userType;
        this.userId = userId;
        this.actionPerformed = actionPerformed;
    }
    
    // Overloaded constructor to handle Timestamp type without logId (for compatibility with the controller)
    public SystemLog(Timestamp timestamp, String userType, String userId, String action) {
        this.logId = 0; // Default value or you might want to generate a unique ID
        this.timestamp = timestamp.toLocalDateTime();
        this.userType = userType;
        this.userId = userId;
        this.action = action;
    }

    // Getters and setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // Add this method to support JavaFX's PropertyValueFactory
    public Timestamp getTimestampAsTimestamp() {
        return timestamp == null ? null : Timestamp.valueOf(timestamp);
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp.toLocalDateTime();
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    @Override
    public String toString() {
        return "SystemLog{" +
                "logId=" + logId +
                ", timestamp=" + timestamp +
                ", userType='" + userType + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}