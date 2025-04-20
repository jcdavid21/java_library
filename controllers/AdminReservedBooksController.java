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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;

public class AdminReservedBooksController implements Initializable {

    @FXML
    private TableView<ReservedBook> reservedBooksTable;
    
    @FXML
    private TableColumn<ReservedBook, Integer> reservationIdCol;
    
    @FXML
    private TableColumn<ReservedBook, String> studentIdCol;
    
    @FXML
    private TableColumn<ReservedBook, String> studentNameCol;
    
    @FXML
    private TableColumn<ReservedBook, Integer> bookIdCol;
    
    @FXML
    private TableColumn<ReservedBook, String> bookTitleCol;
    
    @FXML
    private TableColumn<ReservedBook, LocalDate> reservationDateCol;
    
    @FXML
    private TableColumn<ReservedBook, LocalDate> expirationDateCol;
    
    @FXML
    private TableColumn<ReservedBook, String> statusCol;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private TextField reservationIdField;
    
    @FXML
    private Button updateStatusBtn;
    
    private int adminId;
    private ObservableList<ReservedBook> reservedBooksList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        reservationIdCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        bookTitleCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        reservationDateCol.setCellValueFactory(new PropertyValueFactory<>("reservationDate"));
        expirationDateCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Pending", "Reserved", "Cancelled", "Expired", "Completed"));
        
        // Load reserved books data
        loadReservedBooks();
        
        // Add listener for table row selection
        reservedBooksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                reservationIdField.setText(String.valueOf(newSelection.getReservationId()));
                statusComboBox.setValue(newSelection.getStatus());
            }
        });
        
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReservedBooks(newValue);
        });
    }
    
    private void loadReservedBooks() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Reserved_Books ORDER BY Reservation_Date DESC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            reservedBooksList.clear();
            
            while (rs.next()) {
                ReservedBook book = new ReservedBook(
                    rs.getInt("Reservation_ID"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getInt("Book_ID"),
                    rs.getString("Book_Title"),
                    rs.getDate("Reservation_Date").toLocalDate(),
                    rs.getDate("Expiration_Date").toLocalDate(),
                    rs.getString("Status")
                );
                reservedBooksList.add(book);
            }
            
            reservedBooksTable.setItems(reservedBooksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading reserved books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void filterReservedBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            reservedBooksTable.setItems(reservedBooksList);
            return;
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        ObservableList<ReservedBook> filteredList = FXCollections.observableArrayList();
        
        for (ReservedBook book : reservedBooksList) {
            if (String.valueOf(book.getReservationId()).contains(lowercaseKeyword) ||
                book.getStudentId().toLowerCase().contains(lowercaseKeyword) ||
                book.getStudentName().toLowerCase().contains(lowercaseKeyword) ||
                String.valueOf(book.getBookId()).contains(lowercaseKeyword) ||
                book.getBookTitle().toLowerCase().contains(lowercaseKeyword) ||
                book.getStatus().toLowerCase().contains(lowercaseKeyword)) {
                filteredList.add(book);
            }
        }
        
        reservedBooksTable.setItems(filteredList);
    }
    
    @FXML
    private void updateReservationStatus() {
        String reservationIdText = reservationIdField.getText();
        String newStatus = statusComboBox.getValue();
        
        if (reservationIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                    "Please select a reservation to update.");
            return;
        }
        
        if (newStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", 
                    "Please select a status.");
            return;
        }
        
        int reservationId = Integer.parseInt(reservationIdText);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement approvalStmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Begin transaction
            conn.setAutoCommit(false);
            
            // Update in Reserved_Books table
            String updateQuery = "UPDATE Reserved_Books SET Status = ? WHERE Reservation_ID = ?";
            stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newStatus);
            stmt.setInt(2, reservationId);
            
            int rowsAffected = stmt.executeUpdate();
            
            // If the status is changing to Reserved or Rejected, also update Reservation_Approvals
            if (rowsAffected > 0 && (newStatus.equals("Reserved") || newStatus.equals("Cancelled"))) {
                String approvalStatus = newStatus.equals("Reserved") ? "Approved" : "Rejected";
                String approvalQuery = "UPDATE Reservation_Approvals SET Status = ? WHERE Reservation_ID = ?";
                approvalStmt = conn.prepareStatement(approvalQuery);
                approvalStmt.setString(1, approvalStatus);
                approvalStmt.setInt(2, reservationId);
                approvalStmt.executeUpdate();
            }
            
            // If status is changing to Reserved, we need to update book availability
            if (newStatus.equals("Reserved")) {
                updateBookAvailability(conn, reservationId);
            }
            
            // Commit transaction
            conn.commit();
            
            // Log the action
            logAction("Updated reservation status for ID: " + reservationId + " to " + newStatus);
            
            // Refresh the table
            loadReservedBooks();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Reservation status updated successfully.");
            
        } catch (SQLException e) {
            // Rollback transaction on failure
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error updating reservation status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseConnector.closeResources(null, stmt, null);
            DatabaseConnector.closeResources(null, approvalStmt, null);
        }
    }
    
    private void updateBookAvailability(Connection conn, int reservationId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // First, get the book ID from reservation
            String getBookQuery = "SELECT Book_ID FROM Reserved_Books WHERE Reservation_ID = ?";
            stmt = conn.prepareStatement(getBookQuery);
            stmt.setInt(1, reservationId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int bookId = rs.getInt("Book_ID");
                
                // Close previous resources
                DatabaseConnector.closeResources(null, stmt, rs);
                
                // Update book availability and reserved copies
                String updateBookQuery = "UPDATE Books SET " +
                                        "Available_Copies = Available_Copies - 1, " +
                                        "Reserved_Copies = Reserved_Copies + 1 " +
                                        "WHERE Book_ID = ? AND Available_Copies > 0";
                stmt = conn.prepareStatement(updateBookQuery);
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) " +
                          "VALUES (NOW(), ?, ?, ?)";
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
    
    @FXML
    private void refreshTable() {
        loadReservedBooks();
        clearFields();
    }
    
    @FXML
    private void clearFields() {
        reservationIdField.clear();
        statusComboBox.setValue(null);
        searchField.clear();
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
            Stage currentStage = (Stage) reservationIdField.getScene().getWindow();
            
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
            Stage currentStage = (Stage) reservationIdField.getScene().getWindow();
            
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
    
    // Model class for Reserved Book
    public static class ReservedBook {
        private final int reservationId;
        private final String studentId;
        private final String studentName;
        private final int bookId;
        private final String bookTitle;
        private final LocalDate reservationDate;
        private final LocalDate expirationDate;
        private final String status;
        
        public ReservedBook(int reservationId, String studentId, String studentName, int bookId, 
                String bookTitle, LocalDate reservationDate, LocalDate expirationDate, String status) {
            this.reservationId = reservationId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.reservationDate = reservationDate;
            this.expirationDate = expirationDate;
            this.status = status;
        }
        
        public int getReservationId() {
            return reservationId;
        }
        
        public String getStudentId() {
            return studentId;
        }
        
        public String getStudentName() {
            return studentName;
        }
        
        public int getBookId() {
            return bookId;
        }
        
        public String getBookTitle() {
            return bookTitle;
        }
        
        public LocalDate getReservationDate() {
            return reservationDate;
        }
        
        public LocalDate getExpirationDate() {
            return expirationDate;
        }
        
        public String getStatus() {
            return status;
        }
    }
}