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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;
import controllers.BorrowingHistory;
import controllers.BorrowedBook;

public class BorrowingHistoryController implements Initializable {
    
    private int adminId;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TableView<BorrowingHistory> historyTable;
    
    @FXML
    private TableColumn<BorrowingHistory, String> studentIdColumn;
    
    @FXML
    private TableColumn<BorrowingHistory, String> studentNameColumn;
    
    @FXML
    private TableColumn<BorrowingHistory, Integer> totalBorrowedColumn;
    
    @FXML
    private TableColumn<BorrowingHistory, Integer> totalReservedColumn;
    
    @FXML
    private TableView<BorrowedBook> borrowedBooksTable;
    
    @FXML
    private TableColumn<BorrowedBook, Integer> borrowIdColumn;
    
    @FXML
    private TableColumn<BorrowedBook, String> bookTitleColumn;
    
    @FXML
    private TableColumn<BorrowedBook, String> borrowDateColumn;
    
    @FXML
    private TableColumn<BorrowedBook, String> dueDateColumn;
    
    @FXML
    private TableColumn<BorrowedBook, String> returnDateColumn;
    
    @FXML
    private TableColumn<BorrowedBook, String> statusColumn;
    
    @FXML
    private TableColumn<BorrowedBook, Double> penaltyColumn;
    
    private ObservableList<BorrowingHistory> historyList = FXCollections.observableArrayList();
    private ObservableList<BorrowedBook> borrowedBooksList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        initializeHistoryTable();
        initializeBorrowedBooksTable();
        
        // Load all borrowing history data
        loadBorrowingHistoryData();
        
        // Add listener to historyTable selection to show borrowed books for selected student
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadBorrowedBooksForStudent(newSelection.getStudentId());
            }
        });
        
        // Set up search functionality
        searchButton.setOnAction(e -> searchStudent());
    }
    
    private void initializeHistoryTable() {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        totalBorrowedColumn.setCellValueFactory(new PropertyValueFactory<>("totalBorrowedBooks"));
        totalReservedColumn.setCellValueFactory(new PropertyValueFactory<>("totalReservedBooks"));
        
        historyTable.setItems(historyList);
    }
    
    private void initializeBorrowedBooksTable() {
        borrowIdColumn.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        penaltyColumn.setCellValueFactory(new PropertyValueFactory<>("penaltyFee"));
        
        borrowedBooksTable.setItems(borrowedBooksList);
    }
    
    private void loadBorrowingHistoryData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Borrowing_History";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            historyList.clear();
            
            while (rs.next()) {
                BorrowingHistory history = new BorrowingHistory(
                    rs.getInt("Borrow_history_id"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getInt("Total_Borrowed_Books"),
                    rs.getInt("Total_Reserved_Books")
                );
                historyList.add(history);
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading borrowing history: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void loadBorrowedBooksForStudent(String studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Borrowed_Books WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();
            
            borrowedBooksList.clear();
            
            while (rs.next()) {
                BorrowedBook book = new BorrowedBook(
                    rs.getInt("Borrow_ID"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getInt("Book_ID"),
                    rs.getString("Book_Title"),
                    rs.getDate("Borrow_Date"),
                    rs.getDate("Due_Date"),
                    rs.getDate("Return_Date"),
                    rs.getString("Status"),
                    rs.getDouble("Penalty_Fee")
                );
                borrowedBooksList.add(book);
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading borrowed books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void searchStudent() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadBorrowingHistoryData();
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Borrowing_History WHERE Student_ID LIKE ? OR Student_Name LIKE ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            rs = stmt.executeQuery();
            
            historyList.clear();
            
            while (rs.next()) {
                BorrowingHistory history = new BorrowingHistory(
                    rs.getInt("Borrow_history_id"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getInt("Total_Borrowed_Books"),
                    rs.getInt("Total_Reserved_Books")
                );
                historyList.add(history);
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error searching students: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Log system action
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "Admin");
            stmt.setInt(2, adminId);
            stmt.setString(3, action);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
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
        // Already on this page
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
}