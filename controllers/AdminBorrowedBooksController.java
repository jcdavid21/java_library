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
import util.DatabaseConnector;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminBorrowedBooksController implements Initializable {

    // FXML UI Components
    @FXML private TableView<BorrowedBook> borrowedBooksTable;
    @FXML private TableColumn<BorrowedBook, Integer> borrowIdColumn;
    @FXML private TableColumn<BorrowedBook, String> studentIdColumn;
    @FXML private TableColumn<BorrowedBook, String> studentNameColumn;
    @FXML private TableColumn<BorrowedBook, Integer> bookIdColumn;
    @FXML private TableColumn<BorrowedBook, String> bookTitleColumn;
    @FXML private TableColumn<BorrowedBook, LocalDate> borrowDateColumn;
    @FXML private TableColumn<BorrowedBook, LocalDate> dueDateColumn;
    @FXML private TableColumn<BorrowedBook, LocalDate> returnDateColumn;
    @FXML private TableColumn<BorrowedBook, String> statusColumn;
    @FXML private TableColumn<BorrowedBook, Double> penaltyFeeColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button returnBookButton;
    @FXML private Button refreshButton;
    
    // Admin data
    private int adminId;
    
    // Observable list to store the borrowed books data
    private ObservableList<BorrowedBook> borrowedBooksList = FXCollections.observableArrayList();

    /**
     * Initialize the controller.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup table columns
        setupTableColumns();
        
        // Setup filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Borrowed", "Returned", "Overdue"));
        filterComboBox.setValue("All");
        
        // Add filter listener
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadBorrowedBooks();
        });
        
        // Add search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadBorrowedBooks();
        });
        
        // Add selection listener for the return book button
        borrowedBooksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        returnBookButton.setDisable(!(newSelection.getStatus().equals("Borrowed") || 
                                                     newSelection.getStatus().equals("Overdue")));
                    } else {
                        returnBookButton.setDisable(true);
                    }
                });
        
        // Load books
        loadBorrowedBooks();
    }
    
    /**
     * Setup table columns with property value factories.
     */
    private void setupTableColumns() {
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        penaltyFeeColumn.setCellValueFactory(new PropertyValueFactory<>("penaltyFee"));
    }
    
    /**
     * Load borrowed books from the database.
     */
    private void loadBorrowedBooks() {
        borrowedBooksList.clear();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Build the SQL query based on filter and search
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT * FROM Borrowed_Books WHERE 1=1");
            
            // Apply filter
            String filter = filterComboBox.getValue();
            if (!"All".equals(filter)) {
                sqlBuilder.append(" AND Status = ?");
            }
            
            // Apply search
            String search = searchField.getText().trim();
            if (!search.isEmpty()) {
                sqlBuilder.append(" AND (Student_ID LIKE ? OR Student_Name LIKE ? OR Book_Title LIKE ?)");
            }
            
            // Prepare statement
            stmt = conn.prepareStatement(sqlBuilder.toString());
            
            int paramIndex = 1;
            
            // Set filter parameter
            if (!"All".equals(filter)) {
                stmt.setString(paramIndex++, filter);
            }
            
            // Set search parameters
            if (!search.isEmpty()) {
                String searchPattern = "%" + search + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            
            // Execute query
            rs = stmt.executeQuery();
            
            // Process results
            while (rs.next()) {
                BorrowedBook book = new BorrowedBook(
                    rs.getInt("Borrow_ID"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getInt("Book_ID"),
                    rs.getString("Book_Title"),
                    rs.getDate("Borrow_Date") != null ? rs.getDate("Borrow_Date").toLocalDate() : null,
                    rs.getDate("Due_Date") != null ? rs.getDate("Due_Date").toLocalDate() : null,
                    rs.getDate("Return_Date") != null ? rs.getDate("Return_Date").toLocalDate() : null,
                    rs.getString("Status"),
                    rs.getObject("Penalty_Fee") != null ? rs.getDouble("Penalty_Fee") : 0.0
                );
                borrowedBooksList.add(book);
            }
            
            // Update the table
            borrowedBooksTable.setItems(borrowedBooksList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading borrowed books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    /**
     * Handle returning a book.
     */
    @FXML
    private void handleReturnBook() {
        BorrowedBook selectedBook = borrowedBooksTable.getSelectionModel().getSelectedItem();
        
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "Please select a book to return.");
            return;
        }
        
        // Confirm return
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Return");
        confirmAlert.setHeaderText("Return Book");
        confirmAlert.setContentText("Are you sure you want to mark \"" + 
                selectedBook.getBookTitle() + "\" as returned?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            returnBook(selectedBook);
        }
    }
    
    /**
     * Process returning a book.
     */
    private void returnBook(BorrowedBook book) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateBookStmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Update the borrowed book status
            String borrowedBookSql = "UPDATE Borrowed_Books SET Return_Date = ?, Status = 'Returned' WHERE Borrow_ID = ?";
            stmt = conn.prepareStatement(borrowedBookSql);
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, book.getBorrowId());
            stmt.executeUpdate();
            
            // 2. Update the book's available copies
            String updateBookSql = "UPDATE Books SET Available_Copies = Available_Copies + 1 WHERE Book_ID = ?";
            updateBookStmt = conn.prepareStatement(updateBookSql);
            updateBookStmt.setInt(1, book.getBookId());
            updateBookStmt.executeUpdate();
            
            // 3. Log the action
            logAction(book.getStudentId(), book.getBookId());
            
            // Commit transaction
            conn.commit();
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Book Returned", 
                    "The book \"" + book.getBookTitle() + "\" has been marked as returned.");
            
            // Refresh the table
            loadBorrowedBooks();
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            showAlert(Alert.AlertType.ERROR, "Return Error", 
                    "Error returning book: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Reset auto-commit
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            DatabaseConnector.closeResources(null, stmt, null);
            DatabaseConnector.closeResources(null, updateBookStmt, null);
        }
    }
    
    /**
     * Log the book return action to the system logs.
     */
    private void logAction(String studentId, int bookId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            String sql = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, "Admin");
            stmt.setInt(3, adminId);
            stmt.setString(4, "Processed book return: Book ID " + bookId + " for student " + studentId);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, null);
        }
    }
    
    /**
     * Refresh the borrowed books table.
     */
    @FXML
    private void handleRefresh() {
        loadBorrowedBooks();
    }
    
    /**
     * Calculate penalty fee for overdue books.
     */
    @FXML
    private void calculatePenalties() {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Get all borrowed books that are overdue but don't have a penalty set
            String selectSql = "SELECT Borrow_ID, Due_Date FROM Borrowed_Books " +
                               "WHERE Status = 'Borrowed' AND Due_Date < ? AND (Penalty_Fee IS NULL OR Penalty_Fee = 0)";
            selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setDate(1, Date.valueOf(LocalDate.now()));
            rs = selectStmt.executeQuery();
            
            int updatedCount = 0;
            
            while (rs.next()) {
                int borrowId = rs.getInt("Borrow_ID");
                LocalDate dueDate = rs.getDate("Due_Date").toLocalDate();
                LocalDate today = LocalDate.now();
                
                // Calculate days overdue
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                
                // Calculate penalty (0.50 per day)
                double penalty = daysOverdue * 0.5;
                
                // Update the borrowed book
                String updateSql = "UPDATE Borrowed_Books SET Status = 'Overdue', Penalty_Fee = ? WHERE Borrow_ID = ?";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setDouble(1, penalty);
                updateStmt.setInt(2, borrowId);
                updateStmt.executeUpdate();
                
                updatedCount++;
                DatabaseConnector.closeResources(null, updateStmt, null);
                updateStmt = null;
            }
            
            conn.commit();
            
            if (updatedCount > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Penalties Calculated", 
                        "Updated " + updatedCount + " overdue books with penalties.");
                loadBorrowedBooks();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Updates", 
                        "No new overdue books to update.");
            }
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            showAlert(Alert.AlertType.ERROR, "Penalty Calculation Error", 
                    "Error calculating penalties: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Reset auto-commit
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            DatabaseConnector.closeResources(null, selectStmt, rs);
            DatabaseConnector.closeResources(null, updateStmt, null);
        }
    }
    
    /**
     * Show an alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Set the admin ID.
     */
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
    
    /**
     * Set admin data (for compatibility with other controllers).
     */
    public void setAdminData(int adminId) {
        this.adminId = adminId;
    }
    
    // Navigation Methods
    
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
        // We're already on this page, so no need to navigate
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
            Stage currentStage = (Stage) borrowedBooksTable.getScene().getWindow();
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();
            
            // Create and set new scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - Login");
            currentStage.show();
            
            // Log the logout action
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = DatabaseConnector.getConnection();
                
                String sql = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                stmt.setString(2, "Admin");
                stmt.setInt(3, adminId);
                stmt.setString(4, "Admin logged out");
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                System.err.println("Error logging action: " + e.getMessage());
                e.printStackTrace();
            } finally {
                DatabaseConnector.closeResources(null, stmt, null);
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) borrowedBooksTable.getScene().getWindow();
            
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
    
    /**
     * BorrowedBook data model class.
     */
    public static class BorrowedBook {
        private final int borrowId;
        private final String studentId;
        private final String studentName;
        private final int bookId;
        private final String bookTitle;
        private final LocalDate borrowDate;
        private final LocalDate dueDate;
        private LocalDate returnDate;
        private String status;
        private double penaltyFee;
        
        public BorrowedBook(int borrowId, String studentId, String studentName, int bookId, 
                String bookTitle, LocalDate borrowDate, LocalDate dueDate, 
                LocalDate returnDate, String status, double penaltyFee) {
            this.borrowId = borrowId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
            this.penaltyFee = penaltyFee;
        }
        
        // Getters
        public int getBorrowId() { return borrowId; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public int getBookId() { return bookId; }
        public String getBookTitle() { return bookTitle; }
        public LocalDate getBorrowDate() { return borrowDate; }
        public LocalDate getDueDate() { return dueDate; }
        public LocalDate getReturnDate() { return returnDate; }
        public String getStatus() { return status; }
        public double getPenaltyFee() { return penaltyFee; }
        
        // Setters for mutable properties
        public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
        public void setStatus(String status) { this.status = status; }
        public void setPenaltyFee(double penaltyFee) { this.penaltyFee = penaltyFee; }
    }
}