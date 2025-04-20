package user_account;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import user_account.BookReport;
import user_account.BorrowedBookReport;
import util.DatabaseConnector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportAnIssueController implements Initializable {

    @FXML
    private Text studentNameLabel;
    @FXML
    private Text studentIdLabel;
    @FXML
    private TableView<BookReport> reportTableView;
    @FXML
    private TableColumn<BookReport, Integer> reportIdColumn;
    @FXML
    private TableColumn<BookReport, String> bookTitleColumn;
    @FXML
    private TableColumn<BookReport, String> issueTypeColumn;
    @FXML
    private TableColumn<BookReport, LocalDate> reportDateColumn;
    @FXML
    private TableColumn<BookReport, String> statusColumn;
    @FXML
    private TableColumn<BookReport, Double> penaltyFeeColumn;
    
    @FXML
    private ComboBox<BorrowedBookReport> borrowedBooksComboBox;
    @FXML
    private ComboBox<String> issueTypeComboBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button submitReportButton;

    private String currentStudentId;
    private String currentStudentName;
    private ObservableList<BookReport> reportsList = FXCollections.observableArrayList();
    private ObservableList<BorrowedBookReport> borrowedBooksList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize issue type options
        issueTypeComboBox.setItems(FXCollections.observableArrayList(
                "Damaged cover", "Water damage", "Missing pages", "Lost book", "Other damage"
        ));
        
        // Set up table columns
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        issueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("issueType"));
        reportDateColumn.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        penaltyFeeColumn.setCellValueFactory(new PropertyValueFactory<>("penaltyFee"));
        
        // Configure combo box display
        borrowedBooksComboBox.setCellFactory(lv -> new ListCell<BorrowedBookReport>() {
            @Override
            protected void updateItem(BorrowedBookReport item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getBookTitle());
            }
        });
        
        borrowedBooksComboBox.setButtonCell(new ListCell<BorrowedBookReport>() {
            @Override
            protected void updateItem(BorrowedBookReport item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Select a book" : item.getBookTitle());
            }
        });
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadStudentDetails();
        loadReportedIssues();
        loadBorrowedBooks();
    }
    
    private void loadStudentDetails() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT Student_Name FROM Students WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                currentStudentName = rs.getString("Student_Name");
                studentNameLabel.setText(currentStudentName);
                studentIdLabel.setText(currentStudentId);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading student details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void loadReportedIssues() {
        reportsList.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Lost_Damaged_Reports WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                BookReport report = new BookReport(
                    rs.getInt("Report_ID"),
                    rs.getInt("Book_ID"),
                    rs.getString("Book_Title"),
                    rs.getString("Student_ID"),
                    rs.getString("Student_Name"),
                    rs.getDate("Report_Date").toLocalDate(),
                    rs.getString("Issue_Type"),
                    rs.getDouble("Penalty_Fee"),
                    rs.getString("Status")
                );
                reportsList.add(report);
            }
            
            reportTableView.setItems(reportsList);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading reported issues: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void loadBorrowedBooks() {
        borrowedBooksList.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Borrowed_Books WHERE Student_ID = ? AND (Status = 'Borrowed' OR Status = 'Overdue')";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                BorrowedBookReport book = new BorrowedBookReport(
                    rs.getInt("Borrow_ID"),
                    rs.getInt("Book_ID"),
                    rs.getString("Book_Title"),
                    rs.getDate("Borrow_Date").toLocalDate(),
                    rs.getDate("Due_Date").toLocalDate(),
                    rs.getString("Status")
                );
                borrowedBooksList.add(book);
            }
            
            borrowedBooksComboBox.setItems(borrowedBooksList);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading borrowed books: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    @FXML
    private void submitReport() {
        BorrowedBookReport selectedBook = borrowedBooksComboBox.getValue();
        String issueType = issueTypeComboBox.getValue();
        String description = descriptionTextArea.getText().trim();
        
        // Validate fields
        if (selectedBook == null) {
            showAlert("Validation Error", "Please select a book", Alert.AlertType.WARNING);
            return;
        }
        
        if (issueType == null || issueType.isEmpty()) {
            showAlert("Validation Error", "Please select an issue type", Alert.AlertType.WARNING);
            return;
        }
        
        if (description.isEmpty()) {
            showAlert("Validation Error", "Please provide a description of the issue", Alert.AlertType.WARNING);
            return;
        }
        
        // Calculate default penalty fee based on issue type
        double penaltyFee = calculatePenaltyFee(issueType);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO Lost_Damaged_Reports (Book_ID, Book_Title, Student_ID, Student_Name, " +
                          "Report_Date, Issue_Type, Penalty_Fee, Status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, selectedBook.getBookId());
            stmt.setString(2, selectedBook.getBookTitle());
            stmt.setString(3, currentStudentId);
            stmt.setString(4, currentStudentName);
            stmt.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(6, issueType + (description.isEmpty() ? "" : ": " + description));
            stmt.setDouble(7, penaltyFee);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Report submitted successfully", Alert.AlertType.INFORMATION);
                // Clear form fields
                borrowedBooksComboBox.setValue(null);
                issueTypeComboBox.setValue(null);
                descriptionTextArea.clear();
                // Refresh the table
                loadReportedIssues();
                
                // Also insert into Penalties_Fines table
                insertPenaltyRecord(selectedBook, issueType, penaltyFee);
            } else {
                showAlert("Error", "Failed to submit report", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error submitting report: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    private void insertPenaltyRecord(BorrowedBookReport book, String issueType, double penaltyFee) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO Penalties_Fines (Student_ID, Student_Name, Book_Title, Issue_Type, " +
                          "Fine_Amount, Due_Date, Status) VALUES (?, ?, ?, ?, ?, ?, 'Unpaid')";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            stmt.setString(2, currentStudentName);
            stmt.setString(3, book.getBookTitle());
            stmt.setString(4, issueType);
            stmt.setDouble(5, penaltyFee);
            stmt.setDate(6, java.sql.Date.valueOf(LocalDate.now().plusDays(14))); // Due in 14 days
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting penalty record: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    private double calculatePenaltyFee(String issueType) {
        // Default penalty fees based on issue type
        switch (issueType) {
            case "Damaged cover":
                return 15.00;
            case "Water damage":
                return 20.00;
            case "Missing pages":
                return 10.00;
            case "Lost book":
                return 25.00;
            case "Other damage":
                return 15.00;
            default:
                return 10.00;
        }
    }
    
    // Navigation methods
    @FXML
    private void navigateToHome(ActionEvent event) {
        navigateToPage("../student_teacher_account/student_teacher_home_page.fxml", event);
    }
    
    @FXML
    private void navigateToProfile(MouseEvent event) {
        navigateToPage("account_dashboard.fxml", event);
    }
    
    @FXML
    private void navigateToDashboard(MouseEvent event) {
        navigateToPage("account_dashboard.fxml", event);
    }
    
    @FXML
    private void navigateToMyBorrowedBooks(MouseEvent event) {
        navigateToPage("account_my_borrowed_books.fxml", event);
    }
    
    @FXML
    private void navigateToBorrowingHistory(MouseEvent event) {
        navigateToPage("account_borrowing_history.fxml", event);
    }
    
    @FXML
    private void navigateToMyReservations(MouseEvent event) {
        navigateToPage("account_my_reservation.fxml", event);
    }
    
    @FXML
    private void navigateToPenaltiesAndFines(MouseEvent event) {
        navigateToPage("penalties_fines.fxml", event);
    }
    
    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to login page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Generic method to navigate to another page
     * 
     * @param fxmlFile The FXML file to load
     * @param event    The event that triggered the navigation
     */
    private void navigateToPage(String fxmlFile, javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            // Get the controller and pass the student ID
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    // Try to call setCurrentStudentId if it exists
                    Method setStudentIdMethod = controller.getClass().getMethod("setCurrentStudentId", String.class);
                    setStudentIdMethod.invoke(controller, currentStudentId);
                    // If the controller has a refresh method, call it
                    try {
                        Method refreshMethod = controller.getClass().getMethod("refreshDashboardData");
                        refreshMethod.invoke(controller);
                    } catch (NoSuchMethodException e) {
                        // Ignore if refreshDashboardData method doesn't exist
                    }
                } catch (NoSuchMethodException e) {
                    System.out.println("Controller doesn't have setCurrentStudentId method: " + e.getMessage());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Error invoking setCurrentStudentId: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Utility method to show alerts
     * 
     * @param title The alert title
     * @param content The alert content
     * @param alertType The alert type
     */
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}