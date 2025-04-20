package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import controllers.BookCategory;
import util.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminBookCategoriesController {
    
    @FXML
    private TableView<BookCategory> categoryTable;
    
    @FXML
    private TableColumn<BookCategory, Integer> categoryIdColumn;
    
    @FXML
    private TableColumn<BookCategory, String> categoryNameColumn;
    
    @FXML
    private TableColumn<BookCategory, Integer> totalBooksColumn;
    
    @FXML
    private TextField categoryNameField;
    
    @FXML
    private Button addCategoryButton;
    
    @FXML
    private Button editCategoryButton;
    
    @FXML
    private Button deleteCategoryButton;
    
    private int adminId;
    private ObservableList<BookCategory> categoryList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Set up table columns
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        totalBooksColumn.setCellValueFactory(new PropertyValueFactory<>("totalBooks"));
        
        // Load data
        loadCategories();
        
        // Set up selection listener for table
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categoryNameField.setText(newSelection.getCategoryName());
                editCategoryButton.setDisable(false);
                deleteCategoryButton.setDisable(false);
            } else {
                categoryNameField.clear();
                editCategoryButton.setDisable(true);
                deleteCategoryButton.setDisable(true);
            }
        });
    }
    
    private void loadCategories() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Book_Categories ORDER BY Category_Name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            categoryList.clear();
            while (rs.next()) {
                BookCategory category = new BookCategory(
                    rs.getInt("Category_ID"),
                    rs.getString("Category_Name"),
                    rs.getInt("Total_Books")
                );
                categoryList.add(category);
            }
            
            categoryTable.setItems(categoryList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading book categories: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    @FXML
    private void addCategory() {
        String categoryName = categoryNameField.getText().trim();
        
        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", 
                    "Please enter a category name.");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO Book_Categories (Category_Name, Total_Books) VALUES (?, 0)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, categoryName);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Category added successfully.");
                categoryNameField.clear();
                loadCategories();
                logAction("Added new book category: " + categoryName);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to add category.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error adding category: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    @FXML
    private void editCategory() {
        BookCategory selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "Please select a category to edit.");
            return;
        }
        
        String newCategoryName = categoryNameField.getText().trim();
        if (newCategoryName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", 
                    "Please enter a category name.");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "UPDATE Book_Categories SET Category_Name = ? WHERE Category_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, newCategoryName);
            stmt.setInt(2, selectedCategory.getCategoryId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Category updated successfully.");
                categoryNameField.clear();
                loadCategories();
                logAction("Updated book category ID " + selectedCategory.getCategoryId() + 
                        " from '" + selectedCategory.getCategoryName() + "' to '" + newCategoryName + "'");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to update category.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error updating category: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    @FXML
    private void deleteCategory() {
        BookCategory selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "Please select a category to delete.");
            return;
        }
        
        // Check if the category has any books
        if (selectedCategory.getTotalBooks() > 0) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete", 
                    "Cannot delete category with books. Move or delete the books first.");
            return;
        }
        
        // Ask for confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Category");
        confirmAlert.setContentText("Are you sure you want to delete the category: " + 
                selectedCategory.getCategoryName() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = DatabaseConnector.getConnection();
                String query = "DELETE FROM Book_Categories WHERE Category_ID = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedCategory.getCategoryId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Category deleted successfully.");
                    categoryNameField.clear();
                    loadCategories();
                    logAction("Deleted book category: " + selectedCategory.getCategoryName());
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to delete category.");
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                        "Error deleting category: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DatabaseConnector.closeResources(conn, stmt, null);
            }
        }
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) " +
                          "VALUES (NOW(), 'Admin', ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, String.valueOf(adminId));
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
            Stage currentStage = (Stage) categoryTable.getScene().getWindow();
            
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
            Stage currentStage = (Stage) categoryTable.getScene().getWindow();
            
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