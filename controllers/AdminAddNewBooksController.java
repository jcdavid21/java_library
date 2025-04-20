package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.DatabaseConnector;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminAddNewBooksController implements Initializable {

    @FXML
    private TextField isbnField;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextField authorField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private TextField totalCopiesField;
    
    @FXML
    private DatePicker addedDatePicker;
    
    @FXML
    private Button addBookButton;
    
    @FXML
    private Button cancelButton;
    
    private int adminId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set default date to today
        addedDatePicker.setValue(LocalDate.now());
        
        // Load categories into the combo box
        loadCategories();
    }
    
    private void loadCategories() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT Category_Name FROM Book_Categories ORDER BY Category_Name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                categoryComboBox.getItems().add(rs.getString("Category_Name"));
            }
            
            // Select first category by default if available
            if (!categoryComboBox.getItems().isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading book categories: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    @FXML
    private void addBook() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }
        
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryComboBox.getValue();
        int totalCopies = Integer.parseInt(totalCopiesField.getText().trim());
        LocalDate addedDate = addedDatePicker.getValue();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Check if book with ISBN already exists
            if (isbnExists(isbn, conn)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate ISBN", 
                        "A book with ISBN " + isbn + " already exists in the database.");
                return;
            }

            //get the maximum book id
            String maxBookIdQuery = "SELECT MAX(Book_ID) FROM Books";
            stmt = conn.prepareStatement(maxBookIdQuery);
            ResultSet rs = stmt.executeQuery();
            int bookId = 0;
            if (rs.next()) {
                bookId = rs.getInt(1) + 1; // Increment the max book ID by 1
            }

            
            // Insert new book record
            String query = "INSERT INTO Books (Book_ID, ISBN, Title, Author, Category, Total_Copies, " +
                    "Available_Copies, Reserved_Copies, Lost_Damaged_Copies, Added_Date, Times_Borrowed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, bookId);
            stmt.setString(2, isbn);
            stmt.setString(3, title);
            stmt.setString(4, author);
            stmt.setString(5, category);
            stmt.setInt(6, totalCopies);
            stmt.setInt(7, totalCopies); // Available copies
            stmt.setInt(8, 0); // Reserved copies
            stmt.setInt(9, 0); // Lost/Damaged copies
            stmt.setDate(10, java.sql.Date.valueOf(addedDate));
            stmt.setInt(11, 0); // Times borrowed
            // Execute the insert statement
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Update the book category count
                updateCategoryCount(category, conn);
                
                // Log the action
                logAction("Added new book: " + title);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Book '" + title + "' has been added successfully!");
                
                // Clear inputs for the next entry
                clearInputs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to add the book. No rows affected.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error adding book: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    private boolean isbnExists(String isbn, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, isbn);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void updateCategoryCount(String category, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        
        try {
            String query = "UPDATE Book_Categories SET Total_Books = Total_Books + 1 WHERE Category_Name = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            stmt.executeUpdate();
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    private boolean validateInputs() {
        // Validate ISBN
        if (isbnField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "ISBN cannot be empty");
            isbnField.requestFocus();
            return false;
        }
        
        // Validate Title
        if (titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Title cannot be empty");
            titleField.requestFocus();
            return false;
        }
        
        // Validate Author
        if (authorField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Author cannot be empty");
            authorField.requestFocus();
            return false;
        }
        
        // Validate Category selection
        if (categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Category must be selected");
            categoryComboBox.requestFocus();
            return false;
        }
        
        // Validate Total Copies
        try {
            int copies = Integer.parseInt(totalCopiesField.getText().trim());
            if (copies <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", 
                        "Total copies must be a positive number");
                totalCopiesField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                    "Total copies must be a valid number");
            totalCopiesField.requestFocus();
            return false;
        }
        
        // Validate Added Date
        if (addedDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Added date cannot be empty");
            addedDatePicker.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearInputs() {
        isbnField.clear();
        titleField.clear();
        authorField.clear();
        totalCopiesField.clear();
        addedDatePicker.setValue(LocalDate.now());
        
        // Refocus to ISBN field for next entry
        isbnField.requestFocus();
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) " +
                    "VALUES (NOW(), 'Admin', ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, adminId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void cancel() {
        navigateToTotalBooks();
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
            Stage currentStage = (Stage) isbnField.getScene().getWindow();
            
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
            Stage currentStage = (Stage) isbnField.getScene().getWindow();
            
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
}