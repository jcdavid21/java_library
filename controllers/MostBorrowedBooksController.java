package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;

public class MostBorrowedBooksController implements Initializable {
    
    private int adminId;
    
    @FXML
    private TableView<MostBorrowedBook> mostBorrowedBooksTable;
    
    @FXML
    private TableColumn<MostBorrowedBook, Integer> bookIdColumn;
    
    @FXML
    private TableColumn<MostBorrowedBook, String> isbnColumn;
    
    @FXML
    private TableColumn<MostBorrowedBook, String> titleColumn;
    
    @FXML
    private TableColumn<MostBorrowedBook, Integer> borrowCountColumn;
    
    @FXML
    private Button dashboardButton; // Assuming you have this in your FXML
    
    private ObservableList<MostBorrowedBook> mostBorrowedBooksList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        borrowCountColumn.setCellValueFactory(new PropertyValueFactory<>("borrowCount"));
        
        // Load data from database
        loadMostBorrowedBooks();
    }
    
    private void loadMostBorrowedBooks() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Query to get most borrowed books data
            // Join with Books table to get more details
            String query = "SELECT mb.Book_ID, b.ISBN, mb.Book_Title, mb.Total_Borrowed_Times " +
                          "FROM Most_Borrowed_Books mb " +
                          "JOIN Books b ON mb.Book_ID = b.Book_ID " +
                          "ORDER BY mb.Total_Borrowed_Times DESC";
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            // Clear existing items
            mostBorrowedBooksList.clear();
            
            // Process results
            while (rs.next()) {
                int bookId = rs.getInt("Book_ID");
                String isbn = rs.getString("ISBN");
                String title = rs.getString("Book_Title");
                int borrowCount = rs.getInt("Total_Borrowed_Times");
                
                // Add to list
                mostBorrowedBooksList.add(new MostBorrowedBook(bookId, isbn, title, borrowCount));
            }
            
            // Set items to table
            mostBorrowedBooksTable.setItems(mostBorrowedBooksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading most borrowed books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    // Alternative method to load data directly from the Books table if Most_Borrowed_Books is not updated
    private void loadMostBorrowedBooksFromBooksTable() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Query to get most borrowed books directly from Books table
            String query = "SELECT Book_ID, ISBN, Title, Times_Borrowed " +
                          "FROM Books " +
                          "WHERE Times_Borrowed > 0 " +
                          "ORDER BY Times_Borrowed DESC " +
                          "LIMIT 10"; // Limiting to top 10
            
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            // Clear existing items
            mostBorrowedBooksList.clear();
            
            // Process results
            while (rs.next()) {
                int bookId = rs.getInt("Book_ID");
                String isbn = rs.getString("ISBN");
                String title = rs.getString("Title");
                int borrowCount = rs.getInt("Times_Borrowed");
                
                // Add to list
                mostBorrowedBooksList.add(new MostBorrowedBook(bookId, isbn, title, borrowCount));
            }
            
            // Set items to table
            mostBorrowedBooksTable.setItems(mostBorrowedBooksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading most borrowed books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    // Method to refresh the data
    @FXML
    private void refreshData() {
        loadMostBorrowedBooks();
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
            Stage currentStage = (Stage) mostBorrowedBooksTable.getScene().getWindow();
            
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
            Stage currentStage = (Stage) mostBorrowedBooksTable.getScene().getWindow();
            
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
    
    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Method to log actions in the System_Logs table
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

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
        // You could perform additional initialization here if needed
    }
    
    // Model class for Most Borrowed Books
    public static class MostBorrowedBook {
        private final int bookId;
        private final String isbn;
        private final String title;
        private final int borrowCount;
        
        public MostBorrowedBook(int bookId, String isbn, String title, int borrowCount) {
            this.bookId = bookId;
            this.isbn = isbn;
            this.title = title;
            this.borrowCount = borrowCount;
        }
        
        // Getters
        public int getBookId() {
            return bookId;
        }
        
        public String getIsbn() {
            return isbn;
        }
        
        public String getTitle() {
            return title;
        }
        
        public int getBorrowCount() {
            return borrowCount;
        }
    }
}