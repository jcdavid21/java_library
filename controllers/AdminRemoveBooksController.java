package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import controllers.Book;
import util.DatabaseConnector;

public class AdminRemoveBooksController implements Initializable {

    @FXML
    private TextField bookIdField;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button removeButton;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TableView<Book> booksTableView;
    
    @FXML
    private TableColumn<Book, Integer> bookIdColumn;
    
    @FXML
    private TableColumn<Book, String> isbnColumn;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> categoryColumn;
    
    @FXML
    private TableColumn<Book, Integer> totalCopiesColumn;
    
    @FXML
    private TableColumn<Book, Integer> availableCopiesColumn;
    
    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    
    private Connection connection;
    private int adminId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = DatabaseConnector.getConnection();
        
        // Initialize table columns
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        totalCopiesColumn.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        availableCopiesColumn.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        
        // Load all books initially
        loadBooks();
        
        // Add listener for table selection
        booksTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                bookIdField.setText(String.valueOf(newSelection.getBookId()));
            }
        });
    }
    
    private void loadBooks() {
        booksList.clear();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT * FROM Books";
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("Book_ID"),
                    rs.getString("ISBN"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Category"),
                    rs.getInt("Total_Copies"),
                    rs.getInt("Available_Copies"),
                    rs.getInt("Reserved_Copies"),
                    rs.getInt("Lost_Damaged_Copies"),
                    rs.getDate("Added_Date"),
                    rs.getDate("Last_Borrowed_Date"),
                    rs.getInt("Times_Borrowed")
                );
                booksList.add(book);
            }
            
            booksTableView.setItems(booksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    @FXML
    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadBooks();
            return;
        }
        
        booksList.clear();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT * FROM Books WHERE Book_ID LIKE ? OR ISBN LIKE ? OR Title LIKE ? OR Author LIKE ? OR Category LIKE ?";
            stmt = connection.prepareStatement(query);
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, searchPattern);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("Book_ID"),
                    rs.getString("ISBN"),
                    rs.getString("Title"),
                    rs.getString("Author"),
                    rs.getString("Category"),
                    rs.getInt("Total_Copies"),
                    rs.getInt("Available_Copies"),
                    rs.getInt("Reserved_Copies"),
                    rs.getInt("Lost_Damaged_Copies"),
                    rs.getDate("Added_Date"),
                    rs.getDate("Last_Borrowed_Date"),
                    rs.getInt("Times_Borrowed")
                );
                booksList.add(book);
            }
            
            booksTableView.setItems(booksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", "Error searching books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    @FXML
    private void removeBook() {
        String bookIdText = bookIdField.getText().trim();
        
        if (bookIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a Book ID or select a book from the table.");
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdText);
            
            // Check if book exists and has no outstanding borrows
            if (!canRemoveBook(bookId)) {
                showAlert(Alert.AlertType.WARNING, "Cannot Remove Book", 
                         "This book cannot be removed because it has active borrows or reservations.");
                return;
            }
            
            // Confirm removal
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Book Removal");
            confirmAlert.setHeaderText("Are you sure you want to remove this book?");
            confirmAlert.setContentText("This action cannot be undone.");
            
            if (confirmAlert.showAndWait().get().getButtonData().isDefaultButton()) {
                // User confirmed, remove the book
                removeBookFromDatabase(bookId);
                
                // Update the books table and clear the input field
                loadBooks();
                bookIdField.clear();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book has been successfully removed.");
                
                // Log the action
                logAction("Admin removed book ID: " + bookId);
            }
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Book ID must be a number.");
        }
    }
    
    private boolean canRemoveBook(int bookId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Check if book exists
            String checkBookQuery = "SELECT * FROM Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(checkBookQuery);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Book Not Found", "No book found with ID: " + bookId);
                return false;
            }
            
            // Close the previous resources
            DatabaseConnector.closeResources(null, stmt, rs);
            
            // Check if book has active borrows
            String checkBorrowsQuery = "SELECT COUNT(*) as count FROM Borrowed_Books WHERE Book_ID = ? AND Status = 'Borrowed'";
            stmt = connection.prepareStatement(checkBorrowsQuery);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                return false;  // Book has active borrows
            }
            
            // Close the previous resources
            DatabaseConnector.closeResources(null, stmt, rs);
            
            // Check if book has active reservations
            String checkReservationsQuery = "SELECT COUNT(*) as count FROM Reserved_Books WHERE Book_ID = ? AND (Status = 'Pending' OR Status = 'Reserved')";
            stmt = connection.prepareStatement(checkReservationsQuery);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                return false;  // Book has active reservations
            }
            
            return true;  // Book can be removed
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking book status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void removeBookFromDatabase(int bookId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection.setAutoCommit(false);
            
            // First, get book details needed for category update
            String getBookDetailsQuery = "SELECT Category, Title FROM Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(getBookDetailsQuery);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            String category = null;
            String bookTitle = null;
            
            if (rs.next()) {
                category = rs.getString("Category");
                bookTitle = rs.getString("Title");
            }
            
            // Close the resources for this query
            DatabaseConnector.closeResources(null, stmt, rs);
            
            // First, handle Reservation_Approvals
            // Since it has FK to Reserved_Books, find any reservation IDs for this book
            String findReservationIdsQuery = "SELECT Reservation_ID FROM Reserved_Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(findReservationIdsQuery);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            // Store the reservation IDs to delete from Reservation_Approvals
            java.util.List<Integer> reservationIds = new java.util.ArrayList<>();
            while (rs.next()) {
                reservationIds.add(rs.getInt("Reservation_ID"));
            }
            
            // Close resources
            DatabaseConnector.closeResources(null, stmt, rs);
            
            // Delete from Reservation_Approvals using the reservation IDs
            if (!reservationIds.isEmpty()) {
                for (Integer reservationId : reservationIds) {
                    String deleteApprovalQuery = "DELETE FROM Reservation_Approvals WHERE Reservation_ID = ?";
                    stmt = connection.prepareStatement(deleteApprovalQuery);
                    stmt.setInt(1, reservationId);
                    stmt.executeUpdate();
                    DatabaseConnector.closeResources(null, stmt, null);
                }
            }
            
            // Delete from Due_Books_Notifications
            String deleteDueNotificationsQuery = "DELETE FROM Due_Books_Notifications WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteDueNotificationsQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            DatabaseConnector.closeResources(null, stmt, null);
            
            // Delete from Lost_Damaged_Reports
            String deleteLostDamagedQuery = "DELETE FROM Lost_Damaged_Reports WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteLostDamagedQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            DatabaseConnector.closeResources(null, stmt, null);
            
            // Delete from Borrowed_Books
            String deleteBorrowedQuery = "DELETE FROM Borrowed_Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteBorrowedQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            DatabaseConnector.closeResources(null, stmt, null);
            
            // Delete from Reserved_Books
            String deleteReservationsQuery = "DELETE FROM Reserved_Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteReservationsQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            DatabaseConnector.closeResources(null, stmt, null);
            
            // Delete from Most_Borrowed_Books
            String deleteMostBorrowedQuery = "DELETE FROM Most_Borrowed_Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteMostBorrowedQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            DatabaseConnector.closeResources(null, stmt, null);
            
            // Clean up any Penalties_Fines related to this book (by title)
            if (bookTitle != null) {
                String deletePenaltiesQuery = "DELETE FROM Penalties_Fines WHERE Book_Title = ?";
                stmt = connection.prepareStatement(deletePenaltiesQuery);
                stmt.setString(1, bookTitle);
                stmt.executeUpdate();
                DatabaseConnector.closeResources(null, stmt, null);
            }
            
            // Update category count if category is not null
            if (category != null) {
                String updateCategoryQuery = "UPDATE Book_Categories SET Total_Books = Total_Books - 1 WHERE Category_Name = ?";
                stmt = connection.prepareStatement(updateCategoryQuery);
                stmt.setString(1, category);
                stmt.executeUpdate();
                DatabaseConnector.closeResources(null, stmt, null);
            }
            
            // Finally, delete the book from the Books table
            String deleteBookQuery = "DELETE FROM Books WHERE Book_ID = ?";
            stmt = connection.prepareStatement(deleteBookQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error removing book: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void logAction(String action) {
        PreparedStatement stmt = null;
        
        try {
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), 'Admin', ?, ?)";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, String.valueOf(adminId));
            stmt.setString(2, action);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
}