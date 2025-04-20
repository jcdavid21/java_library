package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;

public class PenaltiesAndFinesController implements Initializable {
    
    private int adminId;
    
    @FXML
    private TableView<PenaltyRecord> penaltiesTable;
    
    @FXML
    private TableColumn<PenaltyRecord, Integer> penaltyIdColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, String> studentIdColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, String> studentNameColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, String> bookTitleColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, String> issueTypeColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, Double> fineAmountColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, LocalDate> dueDateColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, LocalDate> returnDateColumn;
    
    @FXML
    private TableColumn<PenaltyRecord, String> statusColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private TextField studentIdField;
    
    @FXML
    private TextField bookTitleField;
    
    @FXML
    private TextField issueTypeField;
    
    @FXML
    private TextField fineAmountField;
    
    @FXML
    private Button markAsPaidButton;
    
    @FXML
    private Button addPenaltyButton;
    
    @FXML
    private Button updatePenaltyButton;
    
    @FXML
    private Button clearFieldsButton;
    
    private ObservableList<PenaltyRecord> penaltyList = FXCollections.observableArrayList();
    private PenaltyRecord selectedPenalty;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up columns
        penaltyIdColumn.setCellValueFactory(new PropertyValueFactory<>("penaltyId"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        issueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("issueType"));
        fineAmountColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set up status filter
        statusFilter.setItems(FXCollections.observableArrayList("All", "Paid", "Unpaid"));
        statusFilter.setValue("All");
        statusFilter.setOnAction(event -> filterPenalties());
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPenalties());
        
        // Setup table selection event
        penaltiesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedPenalty = newSelection;
                        populateFields(newSelection);
                        updateButtonsState(true);
                    }
                });
        
        // Load penalties data
        loadPenalties();
    }
    
    private void loadPenalties() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Penalties_Fines";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            penaltyList.clear();
            
            while (rs.next()) {
                PenaltyRecord penalty = new PenaltyRecord(
                        rs.getInt("Penalty_ID"),
                        rs.getString("Student_ID"),
                        rs.getString("Student_Name"),
                        rs.getString("Book_Title"),
                        rs.getString("Issue_Type"),
                        rs.getDouble("Fine_Amount"),
                        rs.getDate("Due_Date") != null ? rs.getDate("Due_Date").toLocalDate() : null,
                        rs.getDate("Return_Date") != null ? rs.getDate("Return_Date").toLocalDate() : null,
                        rs.getString("Status")
                );
                
                penaltyList.add(penalty);
            }
            
            penaltiesTable.setItems(penaltyList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading penalties data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void filterPenalties() {
        String searchText = searchField.getText().toLowerCase();
        String statusValue = statusFilter.getValue();
        
        ObservableList<PenaltyRecord> filteredList = FXCollections.observableArrayList();
        
        for (PenaltyRecord penalty : penaltyList) {
            boolean statusMatch = statusValue.equals("All") || 
                    penalty.getStatus().equals(statusValue);
            
            boolean searchMatch = searchText.isEmpty() ||
                    penalty.getStudentId().toLowerCase().contains(searchText) ||
                    penalty.getStudentName().toLowerCase().contains(searchText) ||
                    penalty.getBookTitle().toLowerCase().contains(searchText);
            
            if (statusMatch && searchMatch) {
                filteredList.add(penalty);
            }
        }
        
        penaltiesTable.setItems(filteredList);
    }
    
    private void populateFields(PenaltyRecord penalty) {
        studentIdField.setText(penalty.getStudentId());
        bookTitleField.setText(penalty.getBookTitle());
        issueTypeField.setText(penalty.getIssueType());
        fineAmountField.setText(String.valueOf(penalty.getFineAmount()));
    }
    
    private void clearFields() {
        studentIdField.clear();
        bookTitleField.clear();
        issueTypeField.clear();
        fineAmountField.clear();
        selectedPenalty = null;
        updateButtonsState(false);
    }
    
    private void updateButtonsState(boolean recordSelected) {
        updatePenaltyButton.setDisable(!recordSelected);
        markAsPaidButton.setDisable(!recordSelected || 
                (selectedPenalty != null && selectedPenalty.getStatus().equals("Paid")));
    }
    
    @FXML
    private void handleMarkAsPaid() {
        if (selectedPenalty == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a penalty record to mark as paid.");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "UPDATE Penalties_Fines SET Status = 'Paid' WHERE Penalty_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedPenalty.getPenaltyId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Penalty marked as paid successfully.");
                
                // Log the action
                logAction("Admin marked penalty ID " + selectedPenalty.getPenaltyId() + " as paid for student " + selectedPenalty.getStudentId());
                
                // Reload the table
                loadPenalties();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update penalty status.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating penalty status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    @FXML
    private void handleAddPenalty() {
        if (validateFields()) {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                // First, check if student exists
                if (!checkStudentExists(studentIdField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Student", "Student ID does not exist in the system.");
                    return;
                }
                
                conn = DatabaseConnector.getConnection();
                String query = "INSERT INTO Penalties_Fines (Student_ID, Student_Name, Book_Title, Issue_Type, Fine_Amount, Due_Date, Status) " +
                        "VALUES (?, (SELECT Student_Name FROM Students WHERE Student_ID = ?), ?, ?, ?, CURRENT_DATE(), 'Unpaid')";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, studentIdField.getText());
                stmt.setString(2, studentIdField.getText());
                stmt.setString(3, bookTitleField.getText());
                stmt.setString(4, issueTypeField.getText());
                stmt.setDouble(5, Double.parseDouble(fineAmountField.getText()));
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "New penalty added successfully.");
                    
                    // Log action
                    logAction("Admin added new penalty for student " + studentIdField.getText() + " for " + issueTypeField.getText());
                    
                    // Reload the table and clear fields
                    loadPenalties();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add new penalty.");
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error adding penalty: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DatabaseConnector.closeResources(null, stmt, null);
            }
        }
    }
    
    @FXML
    private void handleUpdatePenalty() {
        if (selectedPenalty == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a penalty record to update.");
            return;
        }
        
        if (validateFields()) {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = DatabaseConnector.getConnection();
                String query = "UPDATE Penalties_Fines SET Issue_Type = ?, Fine_Amount = ? WHERE Penalty_ID = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, issueTypeField.getText());
                stmt.setDouble(2, Double.parseDouble(fineAmountField.getText()));
                stmt.setInt(3, selectedPenalty.getPenaltyId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Penalty updated successfully.");
                    
                    // Log action
                    logAction("Admin updated penalty ID " + selectedPenalty.getPenaltyId() + " for student " + selectedPenalty.getStudentId());
                    
                    // Reload the table
                    loadPenalties();
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update penalty.");
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating penalty: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DatabaseConnector.closeResources(null, stmt, null);
            }
        }
    }
    
    @FXML
    private void handleClearFields() {
        clearFields();
    }
    
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (studentIdField.getText().trim().isEmpty()) {
            errorMessage.append("Student ID is required.\n");
        }
        
        if (bookTitleField.getText().trim().isEmpty()) {
            errorMessage.append("Book Title is required.\n");
        }
        
        if (issueTypeField.getText().trim().isEmpty()) {
            errorMessage.append("Issue Type is required.\n");
        }
        
        if (fineAmountField.getText().trim().isEmpty()) {
            errorMessage.append("Fine Amount is required.\n");
        } else {
            try {
                double fineAmount = Double.parseDouble(fineAmountField.getText());
                if (fineAmount <= 0) {
                    errorMessage.append("Fine Amount must be greater than zero.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Fine Amount must be a valid number.\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private boolean checkStudentExists(String studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT COUNT(*) FROM Students WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
        
        return exists;
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), 'Admin', ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, String.valueOf(adminId));
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            Stage currentStage = (Stage) searchField.getScene().getWindow();
            
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
            Stage currentStage = (Stage) searchField.getScene().getWindow();
            
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
    
    // Model class for Penalty Record
    public static class PenaltyRecord {
        private final int penaltyId;
        private final String studentId;
        private final String studentName;
        private final String bookTitle;
        private final String issueType;
        private final double fineAmount;
        private final LocalDate dueDate;
        private final LocalDate returnDate;
        private final String status;
        
        public PenaltyRecord(int penaltyId, String studentId, String studentName, String bookTitle, 
                String issueType, double fineAmount, LocalDate dueDate, LocalDate returnDate, String status) {
            this.penaltyId = penaltyId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.bookTitle = bookTitle;
            this.issueType = issueType;
            this.fineAmount = fineAmount;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
        }
        
        public int getPenaltyId() {
            return penaltyId;
        }
        
        public String getStudentId() {
            return studentId;
        }
        
        public String getStudentName() {
            return studentName;
        }
        
        public String getBookTitle() {
            return bookTitle;
        }
        
        public String getIssueType() {
            return issueType;
        }
        
        public double getFineAmount() {
            return fineAmount;
        }
        
        public LocalDate getDueDate() {
            return dueDate;
        }
        
        public LocalDate getReturnDate() {
            return returnDate;
        }
        
        public String getStatus() {
            return status;
        }
    }
}