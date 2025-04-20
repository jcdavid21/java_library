package controllers;

public class AdminBaseController {
    protected int adminId;
    
    public void setAdminData(int adminId) {
        this.adminId = adminId;
        // Additional initialization if needed
    }
}