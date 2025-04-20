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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import util.DatabaseConnector;
import controllers.AdminTotalBooksController.Book;

public class AdminUpdateBookDetailsController implements Initializable {

    @FXML
    private TextField bookIdField;
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
    private TextField availableCopiesField;
    @FXML
    private TextField reservedCopiesField;
    @FXML
    private TextField lostDamagedCopiesField;
    @FXML
    private DatePicker addedDatePicker;
    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button dashboardButton;

    private int adminId;
    private Book selectedBook;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize connection outside of try-with-resources so it doesn't auto-close
        Connection conn = null;

        try {
            conn = DatabaseConnector.getConnection();
            if (conn != null && !conn.isClosed()) {
                loadCategories(conn);
                setupNumericValidation();
                // Removed checkForSelectedBook() call
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to establish database connection");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error initializing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Do NOT close the connection here
            // DatabaseConnector will manage connection closing when needed
        }
    }

    public void setBookData(Book book) {
        this.selectedBook = book;
        
        // Check if the book is null after it's been set
        if (this.selectedBook == null) {
            javafx.application.Platform.runLater(() -> {
                showAlert(Alert.AlertType.WARNING, "No Book Selected", 
                      "Please select a book from Total Books Available to update.");
                navigateToTotalBooks();
            });
            return;
        }
        
        // Check if UI is ready for population
        if (bookIdField != null) {
            populateFields();
        } else {
            // If UI components aren't ready yet, schedule population after initialization
            javafx.application.Platform.runLater(this::populateFields);
        }
    }

    private void populateFields() {
        if (selectedBook != null) {
            bookIdField.setText(String.valueOf(selectedBook.getBookId()));
            isbnField.setText(selectedBook.getIsbn());
            titleField.setText(selectedBook.getTitle());
            authorField.setText(selectedBook.getAuthor());

            if (selectedBook.getCategory() != null) {
                categoryComboBox.getSelectionModel().select(selectedBook.getCategory());
            }

            totalCopiesField.setText(String.valueOf(selectedBook.getTotalCopies()));
            availableCopiesField.setText(String.valueOf(selectedBook.getAvailableCopies()));
            reservedCopiesField.setText(String.valueOf(selectedBook.getReservedCopies()));
            lostDamagedCopiesField.setText(String.valueOf(selectedBook.getLostDamagedCopies()));

            if (selectedBook.getAddedDate() != null) {
                addedDatePicker.setValue(selectedBook.getAddedDate());
            }

            bookIdField.setEditable(false);
        }
    }

    private void loadCategories(Connection conn) {
        String query = "SELECT DISTINCT Category FROM Books ORDER BY Category";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ObservableList<String> categories = FXCollections.observableArrayList();

        try {
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("Category");
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                }
            }

            categoryComboBox.setItems(categories);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading categories: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close statement and result set but NOT the connection
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void setupNumericValidation() {
        totalCopiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                totalCopiesField.setText(oldValue);
            }
        });

        availableCopiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                availableCopiesField.setText(oldValue);
            }
        });

        reservedCopiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                reservedCopiesField.setText(oldValue);
            }
        });

        lostDamagedCopiesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                lostDamagedCopiesField.setText(oldValue);
            }
        });
    }

    @FXML
    private void updateBook() {
        if (!validateInputs()) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnector.getConnection();

            if (conn == null || conn.isClosed()) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
                return;
            }

            String updateQuery = "UPDATE Books SET ISBN=?, Title=?, Author=?, Category=?, " +
                    "Total_Copies=?, Available_Copies=?, Reserved_Copies=?, " +
                    "Lost_Damaged_Copies=?, Added_Date=? " +
                    "WHERE Book_ID=?";

            stmt = conn.prepareStatement(updateQuery);

            stmt.setString(1, isbnField.getText().trim());
            stmt.setString(2, titleField.getText().trim());
            stmt.setString(3, authorField.getText().trim());
            stmt.setString(4, categoryComboBox.getValue());
            stmt.setInt(5, Integer.parseInt(totalCopiesField.getText().trim()));
            stmt.setInt(6, Integer.parseInt(availableCopiesField.getText().trim()));
            stmt.setInt(7, Integer.parseInt(reservedCopiesField.getText().trim()));
            stmt.setInt(8, Integer.parseInt(lostDamagedCopiesField.getText().trim()));

            if (addedDatePicker.getValue() != null) {
                stmt.setObject(9, addedDatePicker.getValue());
            } else {
                stmt.setObject(9, LocalDate.now());
            }

            stmt.setInt(10, Integer.parseInt(bookIdField.getText().trim()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book details updated successfully!");
                logAction("Updated book ID: " + bookIdField.getText() + ", Title: " + titleField.getText());
                navigateToTotalBooks();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "No records were updated. Please check the Book ID.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating book: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please ensure all numeric fields contain valid numbers.");
        } finally {
            // Close statement but not connection
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }

    private boolean validateInputs() {
        if (isbnField.getText().trim().isEmpty() ||
                titleField.getText().trim().isEmpty() ||
                authorField.getText().trim().isEmpty() ||
                categoryComboBox.getValue() == null ||
                totalCopiesField.getText().trim().isEmpty() ||
                availableCopiesField.getText().trim().isEmpty() ||
                reservedCopiesField.getText().trim().isEmpty() ||
                lostDamagedCopiesField.getText().trim().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields are required.");
            return false;
        }

        if (!isbnField.getText().trim().matches("\\d{10}|\\d{13}")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "ISBN should be 10 or 13 digits.");
            return false;
        }

        try {
            int totalCopies = Integer.parseInt(totalCopiesField.getText().trim());
            int availableCopies = Integer.parseInt(availableCopiesField.getText().trim());
            int reservedCopies = Integer.parseInt(reservedCopiesField.getText().trim());
            int lostDamaged = Integer.parseInt(lostDamagedCopiesField.getText().trim());

            if (totalCopies < 0 || availableCopies < 0 || reservedCopies < 0 || lostDamaged < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity fields cannot be negative.");
                return false;
            }

            if (availableCopies + reservedCopies + lostDamaged != totalCopies) {
                showAlert(Alert.AlertType.WARNING, "Validation Error",
                        "The sum of available, reserved, and lost/damaged copies must equal total copies.");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Quantity fields must contain valid numbers.");
            return false;
        }

        return true;
    }

    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            String logQuery = "INSERT INTO AdminLogs (Admin_ID, Action, Action_Date) VALUES (?, ?, NOW())";
            stmt = conn.prepareStatement(logQuery);
            stmt.setInt(1, adminId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging admin action: " + e.getMessage());
        } finally {
            // Close statement but not connection
            DatabaseConnector.closeResources(null, stmt, null);
        }
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
            Stage currentStage = (Stage) bookIdField.getScene().getWindow();
            
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
            Stage currentStage = (Stage) bookIdField.getScene().getWindow();
            
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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}