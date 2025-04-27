package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import util.DatabaseConnector;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class LibraryVisitLogsController implements Initializable {

    @FXML private TableView<VisitLog> visitLogsTable;
    @FXML private TableColumn<VisitLog, Integer> logIdColumn;
    @FXML private TableColumn<VisitLog, String> timestampColumn;
    @FXML private TableColumn<VisitLog, String> userTypeColumn;
    @FXML private TableColumn<VisitLog, String> userIdColumn;
    @FXML private TableColumn<VisitLog, String> actionColumn;
    
    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> userTypeComboBox;
    
    private int adminId;
    private ObservableList<VisitLog> visitLogsList = FXCollections.observableArrayList();
    private FilteredList<VisitLog> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up table columns
        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("actionPerformed"));
        
        // Initialize user type filter dropdown
        userTypeComboBox.getItems().addAll("All", "Student", "Admin");
        userTypeComboBox.setValue("All");
        
        // Load data
        loadVisitLogs();
        
        // Set up search and filtering
        setupSearch();
        
        // Add filter change listeners
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        userTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void loadVisitLogs() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // We'll get all system logs and filter them to include only library visit-related activities
            String query = "SELECT * FROM System_Logs ORDER BY Timestamp DESC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            visitLogsList.clear();
            while (rs.next()) {
                int logId = rs.getInt("Log_ID");
                Timestamp timestamp = rs.getTimestamp("Timestamp");
                String userType = rs.getString("User_Type");
                String userId = rs.getString("User_ID");
                String action = rs.getString("Action_Performed");
                
                // Only add entries related to library visits
                // Assuming library visits could include borrowing, returning, or reserving books
                if (action != null && (
                    action.contains("Borrowed") || 
                    action.contains("Returned") || 
                    action.contains("Reserved") ||
                    action.contains("visited") ||
                    action.contains("login") ||
                    action.contains("logout"))) {
                    
                    visitLogsList.add(new VisitLog(logId, timestamp.toString(), userType, userId, action));
                }
            }
            
            // Set filtered data
            filteredData = new FilteredList<>(visitLogsList, p -> true);
            SortedList<VisitLog> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(visitLogsTable.comparatorProperty());
            visitLogsTable.setItems(sortedData);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading library visit logs: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }
    
    private void applyFilters() {
        filteredData.setPredicate(log -> {
            // If filter text is empty, display all logs
            String searchText = searchField.getText().toLowerCase();
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            String userType = userTypeComboBox.getValue();
            
            boolean matchesSearch = searchText == null || searchText.isEmpty() || 
                    log.getUserId().toLowerCase().contains(searchText) ||
                    log.getActionPerformed().toLowerCase().contains(searchText);
            
            boolean matchesDateRange = true;
            if (fromDate != null) {
                LocalDate logDate = LocalDate.parse(log.getTimestamp().split(" ")[0]);
                matchesDateRange = !logDate.isBefore(fromDate);
            }
            if (toDate != null) {
                LocalDate logDate = LocalDate.parse(log.getTimestamp().split(" ")[0]);
                matchesDateRange = matchesDateRange && !logDate.isAfter(toDate);
            }
            
            boolean matchesUserType = "All".equals(userType) || 
                    userType.equals(log.getUserType());
            
            return matchesSearch && matchesDateRange && matchesUserType;
        });
    }
    
    @FXML
    private void exportToCSV() {
        // Implement CSV export functionality
        showAlert(Alert.AlertType.INFORMATION, "Export", 
                "Export to CSV feature would be implemented here.");
    }
    
    @FXML
    private void clearFilters() {
        searchField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        userTypeComboBox.setValue("All");
    }
    
    @FXML
    private void refreshData() {
        loadVisitLogs();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void logAction(String action) {
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
    private void navigateToDashboard() {
        navigateToPage("admin_dashboard.fxml", "Dashboard");
    }

    @FXML
    private void navigateToTotalBooks() {
        navigateToPage("admin_total_books_available.fxml", "Total Books Available");
    }
    
    @FXML
    private void navigateToBookCategories() {
        navigateToPage("admin_book_categories.fxml", "Book Categories");
    }
    
    @FXML
    private void navigateToAddNewBooks() {
        navigateToPage("admin_add_new_books.fxml", "Add New Books");
    }
    
    @FXML
    private void navigateToUpdateBookDetails() {
        navigateToPage("admin_update_book_details.fxml", "Update Book Details");
    }
    
    @FXML
    private void navigateToRemoveBooks() {
        navigateToPage("admin_remove_books.fxml", "Remove Books");
    }
    
    @FXML
    private void navigateToBorrowedBooks() {
        navigateToPage("admin_borrowed_books.fxml", "Borrowed Books");
    }
    
    @FXML
    private void navigateToReservedBooks() {
        navigateToPage("admin_reserved_books.fxml", "Reserved Books");
    }
    
    @FXML
    private void navigateToMostBorrowedBooks() {
        navigateToPage("admin_most_borrowed_books.fxml", "Most Borrowed Books");
    }
    
    @FXML
    private void navigateToViewStudents() {
        navigateToPage("admin_view_students.fxml", "View Students");
    }
    
    @FXML
    private void navigateToBorrowingHistory() {
        navigateToPage("admin_borrowing_history.fxml", "Borrowing History");
    }
    
    @FXML
    private void navigateToPenaltiesAndFines() {
        navigateToPage("admin_penalties_and_fines.fxml", "Penalties and Fines");
    }
    
    @FXML
    private void navigateToLibraryVisitLogs() {
        // We're already here, but including for completeness
        navigateToPage("admin_library_logs.fxml", "Library Visit Logs");
    }
    
    @FXML
    private void navigateToManageAdminAccounts() {
        navigateToPage("admin_manage_admin_accounts.fxml", "Manage Admin Accounts");
    }
    
    @FXML
    private void navigateToSystemLogs() {
        navigateToPage("admin_system_logs.fxml", "System Logs");
    }
    
    @FXML
    private void logout() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) visitLogsTable.getScene().getWindow();
            
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
            Stage currentStage = (Stage) visitLogsTable.getScene().getWindow();
            
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
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
    }
    
    // Model class for Visit Logs
    public static class VisitLog {
        private final int logId;
        private final String timestamp;
        private final String userType;
        private final String userId;
        private final String actionPerformed;
        
        public VisitLog(int logId, String timestamp, String userType, String userId, String actionPerformed) {
            this.logId = logId;
            this.timestamp = timestamp;
            this.userType = userType;
            this.userId = userId;
            this.actionPerformed = actionPerformed;
        }
        
        public int getLogId() {
            return logId;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public String getUserType() {
            return userType;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getActionPerformed() {
            return actionPerformed;
        }
    }
}