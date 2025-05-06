package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;

public class AdminViewStudentsController implements Initializable {

    // Admin ID
    private int adminId;

    // FXML UI Components
    @FXML private TableView<StudentModel> studentsTable;
    @FXML private TableColumn<StudentModel, String> colStudentId;
    @FXML private TableColumn<StudentModel, String> colStudentName;
    
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchField;

    // Observable list for table data
    private ObservableList<StudentModel> studentsList = FXCollections.observableArrayList();
    
    // Flag to track if we're updating an existing student
    private boolean isUpdating = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configure table columns
            if (colStudentId != null) {
                colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
            } else {
                System.err.println("Warning: colStudentId is null");
            }
            
            if (colStudentName != null) {
                colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
            } else {
                System.err.println("Warning: colStudentName is null");
            }
            
            // Set up table selection listener
            if (studentsTable != null && studentsTable.getSelectionModel() != null) {
                studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        studentIdField.setText(newSelection.getStudentId());
                        studentNameField.setText(newSelection.getStudentName());
                        isUpdating = true;
                        // Student ID shouldn't be editable when updating
                        studentIdField.setEditable(false);
                        deleteButton.setDisable(false);
                    }
                });
            } else {
                System.err.println("Warning: studentsTable or its selection model is null");
            }
            
            // Set up search functionality
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()) {
                        loadStudentsData();
                    } else {
                        searchStudents(newValue);
                    }
                });
            } else {
                System.err.println("Warning: searchField is null");
            }
            
            // Disable delete button initially
            if (deleteButton != null) {
                deleteButton.setDisable(true);
            } else {
                System.err.println("Warning: deleteButton is null");
            }
            
            // Schedule loading data in a separate thread
            Platform.runLater(this::loadStudentsData);
            
        } catch (Exception e) {
            System.err.println("Error in initialize: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Error", 
                "Error initializing the view: " + e.getMessage());
        }
    }

    // Load all students from database
    private void loadStudentsData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String query = "SELECT Student_ID, Student_Name FROM Students";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            ObservableList<StudentModel> tempList = FXCollections.observableArrayList();
            
            while (rs.next()) {
                tempList.add(new StudentModel(
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name")
                ));
            }
            
            // Update UI on JavaFX application thread
            Platform.runLater(() -> {
                studentsList.clear();
                studentsList.addAll(tempList);
                
                if (studentsTable != null) {
                    studentsTable.setItems(studentsList);
                }
            });
            
        } catch (SQLException e) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading students data: " + e.getMessage());
            });
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    // Search students by ID or name
    private void searchStudents(String searchText) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String query = "SELECT Student_ID, Student_Name FROM Students " +
                          "WHERE Student_ID LIKE ? OR Student_Name LIKE ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            rs = stmt.executeQuery();
            
            ObservableList<StudentModel> tempList = FXCollections.observableArrayList();
            
            while (rs.next()) {
                tempList.add(new StudentModel(
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name")
                ));
            }
            
            // Update UI on JavaFX application thread
            Platform.runLater(() -> {
                studentsList.clear();
                studentsList.addAll(tempList);
                
                if (studentsTable != null) {
                    studentsTable.setItems(studentsList);
                }
            });
            
        } catch (SQLException e) {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error searching students: " + e.getMessage());
            });
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            if (!validateInputs()) {
                return;
            }
            
            String studentId = studentIdField.getText().trim();
            String studentName = studentNameField.getText().trim();
            String password = passwordField.getText();
            
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = DatabaseConnector.getConnection();
                if (conn == null) {
                    throw new SQLException("Failed to establish database connection");
                }
                
                if (isUpdating) {
                    // Update existing student
                    if (password.isEmpty()) {
                        // Update name only
                        String updateQuery = "UPDATE Students SET Student_Name = ? WHERE Student_ID = ?";
                        stmt = conn.prepareStatement(updateQuery);
                        stmt.setString(1, studentName);
                        stmt.setString(2, studentId);
                    } else {
                        // Update name and password
                        String updateQuery = "UPDATE Students SET Student_Name = ?, password = ? WHERE Student_ID = ?";
                        stmt = conn.prepareStatement(updateQuery);
                        stmt.setString(1, studentName);
                        stmt.setString(2, password);
                        stmt.setString(3, studentId);
                    }
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully!");
                        logAction("Updated student details for ID: " + studentId);
                        clearFields();
                        loadStudentsData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student!");
                    }
                    
                } else {
                    // Check if student ID already exists
                    PreparedStatement checkStmt = conn.prepareStatement("SELECT Student_ID FROM Students WHERE Student_ID = ?");
                    checkStmt.setString(1, studentId);
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Student ID already exists!");
                        DatabaseConnector.closeResources(null, checkStmt, rs);
                        return;
                    }
                    DatabaseConnector.closeResources(null, checkStmt, rs);
                    
                    // Insert new student
                    String insertQuery = "INSERT INTO Students (Student_ID, Student_Name, password) VALUES (?, ?, ?)";
                    stmt = conn.prepareStatement(insertQuery);
                    stmt.setString(1, studentId);
                    stmt.setString(2, studentName);
                    stmt.setString(3, password);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "New student added successfully!");
                        logAction("Added new student with ID: " + studentId);
                        clearFields();
                        loadStudentsData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student!");
                    }
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving student: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DatabaseConnector.closeResources(null, stmt, null);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "Error during save operation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDelete() {
        try {
            if (studentsTable == null || studentsTable.getSelectionModel() == null || 
                studentsTable.getSelectionModel().getSelectedItem() == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete!");
                return;
            }
            
            String studentId = studentIdField.getText().trim();
            
            // Confirm deletion
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this student? This will remove all related records as well.",
                    ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Deletion");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                Connection conn = null;
                PreparedStatement stmt = null;
                
                try {
                    conn = DatabaseConnector.getConnection();
                    if (conn == null) {
                        throw new SQLException("Failed to establish database connection");
                    }
                    
                    // Start transaction
                    conn.setAutoCommit(false);
                    
                    // First delete related records from child tables
                    String[] tables = {
                        "Borrowing_History", "Reserved_Books", "Borrowed_Books", 
                        "Penalties_Fines", "Lost_Damaged_Reports", "Reservation_Approvals", 
                        "Due_Books_Notifications"
                    };
                    
                    for (String table : tables) {
                        deleteFromTable(conn, table, studentId);
                    }
                    
                    // Finally delete the student
                    String deleteQuery = "DELETE FROM Students WHERE Student_ID = ?";
                    stmt = conn.prepareStatement(deleteQuery);
                    stmt.setString(1, studentId);
                    
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Commit transaction
                        conn.commit();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");
                        logAction("Deleted student with ID: " + studentId);
                        clearFields();
                        loadStudentsData();
                    } else {
                        // Rollback transaction
                        conn.rollback();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student!");
                    }
                    
                    // Reset auto-commit
                    conn.setAutoCommit(true);
                    
                } catch (SQLException e) {
                    try {
                        if (conn != null) {
                            conn.rollback();
                            conn.setAutoCommit(true);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting student: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    DatabaseConnector.closeResources(null, stmt, null);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "Error during delete operation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteFromTable(Connection conn, String tableName, String studentId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            String deleteQuery = "DELETE FROM " + tableName + " WHERE Student_ID = ?";
            stmt = conn.prepareStatement(deleteQuery);
            stmt.setString(1, studentId);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    @FXML
    private void handleClear() {
        clearFields();
    }
    
    private void clearFields() {
        try {
            if (studentIdField != null) studentIdField.clear();
            if (studentNameField != null) studentNameField.clear();
            if (passwordField != null) passwordField.clear();
            if (confirmPasswordField != null) confirmPasswordField.clear();
            
            if (studentIdField != null) studentIdField.setEditable(true);
            isUpdating = false;
            
            if (deleteButton != null) deleteButton.setDisable(true);
            
            if (studentsTable != null && studentsTable.getSelectionModel() != null) {
                studentsTable.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            System.err.println("Error in clearFields: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInputs() {
        try {
            if (studentIdField == null || studentNameField == null || 
                passwordField == null || confirmPasswordField == null) {
                showAlert(Alert.AlertType.ERROR, "UI Error", "Form fields not properly initialized");
                return false;
            }
            
            String studentId = studentIdField.getText().trim();
            String studentName = studentNameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (studentId.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Student ID cannot be empty!");
                return false;
            }
            
            if (studentName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Student Name cannot be empty!");
                return false;
            }
            
            // If updating and password field is empty, we're not changing the password
            if (!isUpdating || !password.isEmpty()) {
                // For new students, password is required
                if (password.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Password cannot be empty!");
                    return false;
                }
                
                if (!password.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords don't match!");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Error during validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            } catch (Exception e) {
                System.err.println("Error showing alert: " + e.getMessage());
                System.err.println("Alert details - Type: " + alertType + ", Title: " + title + ", Message: " + message);
                e.printStackTrace();
            }
        });
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            if (conn == null) {
                System.err.println("Error logging action: Cannot establish database connection");
                return;
            }
            
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "Admin");
            stmt.setString(2, String.valueOf(adminId));
            stmt.setString(3, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    // Model class for Student table
    public static class StudentModel {
        private String studentId;
        private String studentName;
        
        public StudentModel(String studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
        }
        
        public String getStudentId() {
            return studentId;
        }
        
        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }
        
        public String getStudentName() {
            return studentName;
        }
        
        public void setStudentName(String studentName) {
            this.studentName = studentName;
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
    private void navigateToUpdateBooks() {
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
            Stage currentStage = (Stage) studentIdField.getScene().getWindow();
            if (currentStage == null) {
                throw new RuntimeException("Cannot get current stage");
            }
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();
            if (root == null) {
                throw new RuntimeException("Failed to load login page");
            }
            
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
            if (studentIdField == null || studentIdField.getScene() == null || 
                studentIdField.getScene().getWindow() == null) {
                throw new RuntimeException("Cannot get current stage");
            }
            
            Stage currentStage = (Stage) studentIdField.getScene().getWindow();
            
            // Load the selected view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/" + fxmlFile));
            Parent root = loader.load();
            if (root == null) {
                throw new RuntimeException("Failed to load " + fxmlFile);
            }
            
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
        // Any additional initialization with admin data can be done here
    }
}