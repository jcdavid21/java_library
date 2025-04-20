package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import controllers.SystemLog;
import util.DatabaseConnector;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SystemLogsController implements Initializable {

    @FXML
    private TableView<SystemLog> logsTable;
    
    @FXML
    private TableColumn<SystemLog, Integer> logIdColumn;
    
    @FXML
    private TableColumn<SystemLog, LocalDateTime> timestampColumn;
    
    @FXML
    private TableColumn<SystemLog, String> userTypeColumn;
    
    @FXML
    private TableColumn<SystemLog, String> userIdColumn;
    
    @FXML
    private TableColumn<SystemLog, String> actionPerformedColumn;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private ComboBox<String> userTypeFilter;
    
    @FXML
    private TextField userIdFilter;
    
    @FXML
    private TextField actionFilter;
    
    @FXML
    private Button filterButton;
    
    @FXML
    private Button resetButton;
    
    private int adminId;
    
    private ObservableList<SystemLog> logsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        actionPerformedColumn.setCellValueFactory(new PropertyValueFactory<>("actionPerformed"));
        
        // Format the timestamp column to display date and time
        timestampColumn.setCellFactory(column -> {
            return new TableCell<SystemLog, LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
        
        // Initialize user type filter combobox
        userTypeFilter.getItems().addAll("All", "Student", "Admin");
        userTypeFilter.setValue("All");
        
        // Set up date pickers with default values (last 7 days)
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        
        // Load logs
        loadSystemLogs();
        
        // Set up filter button action
        filterButton.setOnAction(event -> applyFilters());
        
        // Set up reset button action
        resetButton.setOnAction(event -> resetFilters());
    }
    
    private void loadSystemLogs() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            String query = "SELECT * FROM System_Logs ORDER BY Timestamp DESC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            logsList.clear();
            
            while (rs.next()) {
                int logId = rs.getInt("Log_ID");
                LocalDateTime timestamp = rs.getTimestamp("Timestamp").toLocalDateTime();
                String userType = rs.getString("User_Type");
                String userId = rs.getString("User_ID");
                String actionPerformed = rs.getString("Action_Performed");
                
                SystemLog log = new SystemLog(logId, timestamp, userType, userId, actionPerformed);
                logsList.add(log);
            }
            
            logsTable.setItems(logsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading system logs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    public void applyFilters() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM System_Logs WHERE 1=1");
            
            // Add date range filter
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                queryBuilder.append(" AND DATE(Timestamp) BETWEEN ? AND ?");
            }
            
            // Add user type filter
            if (userTypeFilter.getValue() != null && !userTypeFilter.getValue().equals("All")) {
                queryBuilder.append(" AND User_Type = ?");
            }
            
            // Add user ID filter
            if (userIdFilter.getText() != null && !userIdFilter.getText().trim().isEmpty()) {
                queryBuilder.append(" AND User_ID LIKE ?");
            }
            
            // Add action filter
            if (actionFilter.getText() != null && !actionFilter.getText().trim().isEmpty()) {
                queryBuilder.append(" AND Action_Performed LIKE ?");
            }
            
            queryBuilder.append(" ORDER BY Timestamp DESC");
            
            stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Set parameters
            int paramIndex = 1;
            
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                stmt.setString(paramIndex++, fromDatePicker.getValue().toString());
                stmt.setString(paramIndex++, toDatePicker.getValue().toString());
            }
            
            if (userTypeFilter.getValue() != null && !userTypeFilter.getValue().equals("All")) {
                stmt.setString(paramIndex++, userTypeFilter.getValue());
            }
            
            if (userIdFilter.getText() != null && !userIdFilter.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + userIdFilter.getText().trim() + "%");
            }
            
            if (actionFilter.getText() != null && !actionFilter.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + actionFilter.getText().trim() + "%");
            }
            
            rs = stmt.executeQuery();
            
            logsList.clear();
            
            while (rs.next()) {
                int logId = rs.getInt("Log_ID");
                LocalDateTime timestamp = rs.getTimestamp("Timestamp").toLocalDateTime();
                String userType = rs.getString("User_Type");
                String userId = rs.getString("User_ID");
                String actionPerformed = rs.getString("Action_Performed");
                
                SystemLog log = new SystemLog(logId, timestamp, userType, userId, actionPerformed);
                logsList.add(log);
            }
            
            logsTable.setItems(logsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error applying filters: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    public void resetFilters() {
        userTypeFilter.setValue("All");
        userIdFilter.clear();
        actionFilter.clear();
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        
        loadSystemLogs();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "Admin");
            stmt.setString(2, String.valueOf(adminId));
            stmt.setString(3, action);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    // Navigation Methods for Sidebar
    @FXML
    public void navigateToDashboard() {
        navigateToPage("admin_dashboard.fxml", "Dashboard");
    }

    @FXML
    public void navigateToTotalBooks() {
        navigateToPage("admin_total_books_available.fxml", "Total Books Available");
    }
    
    @FXML
    public void navigateToBookCategories() {
        navigateToPage("admin_book_categories.fxml", "Book Categories");
    }
    
    @FXML
    public void navigateToAddNewBooks() {
        navigateToPage("admin_add_new_books.fxml", "Add New Books");
    }
    
    @FXML
    public void navigateToUpdateBooks() {
        navigateToPage("admin_update_book_details.fxml", "Update Book Details");
    }
    
    @FXML
    public void navigateToRemoveBooks() {
        navigateToPage("admin_remove_books.fxml", "Remove Books");
    }
    
    @FXML
    public void navigateToBorrowedBooks() {
        navigateToPage("admin_borrowed_books.fxml", "Borrowed Books");
    }
    
    @FXML
    public void navigateToReservedBooks() {
        navigateToPage("admin_reserved_books.fxml", "Reserved Books");
    }
    
    @FXML
    public void navigateToMostBorrowedBooks() {
        navigateToPage("admin_most_borrowed_books.fxml", "Most Borrowed Books");
    }
    
    @FXML
    public void navigateToViewStudents() {
        navigateToPage("admin_view_students.fxml", "View Students");
    }
    
    @FXML
    public void navigateToBorrowingHistory() {
        navigateToPage("admin_borrowing_history.fxml", "Borrowing History");
    }
    
    @FXML
    public void navigateToPenaltiesAndFines() {
        navigateToPage("admin_penalties_fines.fxml", "Penalties and Fines");
    }
    
    @FXML
    public void navigateToLibraryVisitLogs() {
        navigateToPage("admin_library_logs.fxml", "Library Visit Logs");
    }
    
    @FXML
    public void navigateToManageAdminAccounts() {
        navigateToPage("admin_manage_admin_accounts.fxml", "Manage Admin Accounts");
    }
    
    @FXML
    public void navigateToSystemLogs() {
        // Just refresh the data since we're already on this page
        loadSystemLogs();
    }
    
    @FXML
    public void logout() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) logsTable.getScene().getWindow();
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();
            
            // Create and set new scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - Login");
            currentStage.show();
            
            // Log the logout action
            logAction("Admin logged out");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) logsTable.getScene().getWindow();
            
            // Load the selected view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/" + fxmlFile));
            Parent root = loader.load();
            
            // Pass admin ID to the new controller
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setAdminData", int.class).invoke(controller, adminId);
                } catch (NoSuchMethodException e) {
                    try {
                        controller.getClass().getMethod("setAdminId", int.class).invoke(controller, adminId);
                    } catch (NoSuchMethodException ex) {
                        System.out.println("Controller doesn't have setAdminData or setAdminId method");
                    }
                }
            }
            
            // Create and set new scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - " + title);
            currentStage.show();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
        // You may want to log this action
        logAction("Viewed system logs");
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
        // You may want to log this action
        logAction("Viewed system logs");
    }
    
    // Method to export logs to CSV
    @FXML
    public void exportToCSV() {
        // Implementation can be added here for exporting log data to CSV
        showAlert(Alert.AlertType.INFORMATION, "Export Logs", 
                "Log export functionality will be implemented here.");
    }
    
    // Method to generate a report
    @FXML
    public void generateReport() {
        // Implementation can be added here for generating reports based on the log data
        showAlert(Alert.AlertType.INFORMATION, "Generate Report", 
                "Report generation functionality will be implemented here.");
    }
}