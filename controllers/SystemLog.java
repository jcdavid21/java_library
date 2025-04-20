package controllers;

import java.time.LocalDateTime;

public class SystemLog {
    private int logId;
    private LocalDateTime timestamp;
    private String userType;
    private String userId;
    private String actionPerformed;
    
    public SystemLog(int logId, LocalDateTime timestamp, String userType, String userId, String actionPerformed) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.userType = userType;
        this.userId = userId;
        this.actionPerformed = actionPerformed;
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
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
                ", actionPerformed='" + actionPerformed + '\'' +
                '}';
    }
}