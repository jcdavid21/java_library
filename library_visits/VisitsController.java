package library_visits;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import util.DatabaseConnector;

public class VisitsController implements Initializable {

    @FXML
    private TextField studentIDField;
    
    @FXML
    private TextField employeeIDField;
    
    @FXML
    private ComboBox<String> studentPurposeComboBox;
    
    @FXML
    private ComboBox<String> employeePurposeComboBox;
    
    @FXML
    private Button studentSubmitButton;
    
    @FXML
    private Button employeeSubmitButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize purpose options for students - don't recreate the ComboBox
        studentPurposeComboBox.getItems().addAll(
            "Study/Research",
            "Borrow Books",
            "Return Books",
            "Group Discussion",
            "Use Computers",
            "Use WiFi",
            "Read Newspapers/Magazines",
            "Other"
        );
        
        // Initialize purpose options for employees
        employeePurposeComboBox.getItems().addAll(
            "Research",
            "Preparation",
            "Meeting",
            "Supervision",
            "Borrow Materials",
            "Return Materials",
            "Other"
        );
        
        // Set default values
        studentPurposeComboBox.setValue("Study/Research");
        employeePurposeComboBox.setValue("Research");
        
        // Add event handlers for submit buttons
        studentSubmitButton.setOnAction(this::handleStudentSubmit);
        employeeSubmitButton.setOnAction(this::handleEmployeeSubmit);
    }
    
    /**
     * Handles the student visit submission
     * @param event The action event triggered by button click
     */
    @FXML
    private void handleStudentSubmit(ActionEvent event) {
        String studentID = studentIDField.getText().trim();
        String purpose = studentPurposeComboBox.getValue();
        
        if (studentID.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Student ID is required");
            return;
        }
        
        // Validate student ID exists in database
        if (!validateStudentID(studentID)) {
            showAlert(AlertType.ERROR, "Error", "Invalid Student ID");
            return;
        }
        
        // Log the student visit
        if (logStudentVisit(studentID, purpose)) {
            showAlert(AlertType.INFORMATION, "Success", "Visit logged successfully");
            studentIDField.clear();
        } else {
            showAlert(AlertType.ERROR, "Error", "Failed to log visit");
        }
    }
    
    /**
     * Handles the employee visit submission
     * @param event The action event triggered by button click
     */
    @FXML
    private void handleEmployeeSubmit(ActionEvent event) {
        String employeeID = employeeIDField.getText().trim();
        String purpose = employeePurposeComboBox.getValue();
        
        if (employeeID.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Employee ID is required");
            return;
        }
        
        // Validate employee ID exists in database
        if (!validateEmployeeID(employeeID)) {
            showAlert(AlertType.ERROR, "Error", "Invalid Employee ID");
            return;
        }
        
        // Log the employee visit
        if (logEmployeeVisit(employeeID, purpose)) {
            showAlert(AlertType.INFORMATION, "Success", "Visit logged successfully");
            employeeIDField.clear();
        } else {
            showAlert(AlertType.ERROR, "Error", "Failed to log visit");
        }
    }
    
    /**
     * Validates if the student ID exists in the database
     * @param studentID The student ID to validate
     * @return true if valid, false otherwise
     */
    private boolean validateStudentID(String studentID) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareStatement("SELECT Student_Name FROM Students WHERE Student_ID = ?");
            stmt.setString(1, studentID);
            rs = stmt.executeQuery();
            return rs.next(); // Returns true if student exists
        } catch (SQLException e) {
            System.err.println("Error validating student ID: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Validates if the employee ID exists in the database
     * @param employeeID The employee ID to validate
     * @return true if valid, false otherwise
     */
    private boolean validateEmployeeID(String employeeID) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareStatement("SELECT Last_Name, First_Name FROM Admin_Accounts WHERE Admin_ID = ?");
            stmt.setString(1, employeeID);
            rs = stmt.executeQuery();
            return rs.next(); // Returns true if employee exists
        } catch (SQLException e) {
            System.err.println("Error validating employee ID: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Logs a student visit in the Visit_Logs table
     * @param studentID The student ID
     * @param purpose The purpose of the visit
     * @return true if successful, false otherwise
     */
    private boolean logStudentVisit(String studentID, String purpose) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // First get the student name
            String studentName = "";
            stmt = conn.prepareStatement("SELECT Student_Name FROM Students WHERE Student_ID = ?");
            stmt.setString(1, studentID);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                studentName = rs.getString("Student_Name");
            }
            
            // Log the visit
            DatabaseConnector.closeResources(null, stmt, rs);
            stmt = conn.prepareStatement(
                "INSERT INTO Visit_Logs (Student_ID, Student_Name, Purpose, Visitor_Type) VALUES (?, ?, ?, 'Student')"
            );
            stmt.setString(1, studentID);
            stmt.setString(2, studentName);
            stmt.setString(3, purpose);
            
            int result = stmt.executeUpdate();
            
            // Log in system logs as well
            if (result > 0) {
                logSystemAction(studentID, "Student", "Library Visit: " + purpose);
            }
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error logging student visit: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Logs an employee visit in the Visit_Logs table
     * @param employeeID The employee ID
     * @param purpose The purpose of the visit
     * @return true if successful, false otherwise
     */
    private boolean logEmployeeVisit(String employeeID, String purpose) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // First get the employee name
            String employeeName = "";
            stmt = conn.prepareStatement("SELECT CONCAT(First_Name, ' ', Last_Name) AS Employee_Name FROM Admin_Accounts WHERE Admin_ID = ?");
            stmt.setString(1, employeeID);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                employeeName = rs.getString("Employee_Name");
            }
            
            // Log the visit
            DatabaseConnector.closeResources(null, stmt, rs);
            stmt = conn.prepareStatement(
                "INSERT INTO Visit_Logs (Employee_ID, Employee_Name, Purpose, Visitor_Type) VALUES (?, ?, ?, 'Employee')"
            );
            stmt.setString(1, employeeID);
            stmt.setString(2, employeeName);
            stmt.setString(3, purpose);
            
            int result = stmt.executeUpdate();
            
            // Log in system logs as well
            if (result > 0) {
                logSystemAction(employeeID, "Employee", "Library Visit: " + purpose);
            }
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error logging employee visit: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Logs an action in the System_Logs table
     * @param userID The user ID (student or employee)
     * @param userType The type of user (Student or Employee)
     * @param action The action performed
     */
    private void logSystemAction(String userID, String userType, String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.prepareStatement(
                "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), ?, ?, ?)"
            );
            stmt.setString(1, userType);
            stmt.setString(2, userID);
            stmt.setString(3, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging system action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    /**
     * Shows an alert dialog
     * @param type The type of alert
     * @param title The title of the alert
     * @param message The message to display
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}