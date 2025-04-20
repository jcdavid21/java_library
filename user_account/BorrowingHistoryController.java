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
import user_account.BorrowingHistoryEntry;
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

public class BorrowingHistoryController implements Initializable {

    @FXML
    private Text studentNameText;
    
    @FXML
    private Text studentIdText;
    
    @FXML
    private Text totalBorrowedText;
    
    @FXML
    private Text totalReservedText;
    
    @FXML
    private TableView<BorrowingHistoryEntry> historyTable;
    
    @FXML
    private TableColumn<BorrowingHistoryEntry, String> bookTitleColumn;
    
    @FXML
    private TableColumn<BorrowingHistoryEntry, LocalDate> borrowDateColumn;
    
    @FXML
    private TableColumn<BorrowingHistoryEntry, LocalDate> returnDateColumn;
    
    @FXML
    private TableColumn<BorrowingHistoryEntry, String> statusColumn;
    
    private String currentStudentId;
    private ObservableList<BorrowingHistoryEntry> borrowingHistoryData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set the items to the table
        historyTable.setItems(borrowingHistoryData);
    }
    
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadStudentDetails();
        loadBorrowingHistory();
    }
    
    private void loadStudentDetails() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Get student details and borrowing summary
            String query = "SELECT s.Student_Name, bh.Total_Borrowed_Books, bh.Total_Reserved_Books " +
                           "FROM Students s " +
                           "LEFT JOIN Borrowing_History bh ON s.Student_ID = bh.Student_ID " +
                           "WHERE s.Student_ID = ?";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String studentName = rs.getString("Student_Name");
                int totalBorrowed = rs.getInt("Total_Borrowed_Books");
                int totalReserved = rs.getInt("Total_Reserved_Books");
                
                // Set the data to UI components
                studentNameText.setText(studentName);
                studentIdText.setText(currentStudentId);
                totalBorrowedText.setText(String.valueOf(totalBorrowed));
                totalReservedText.setText(String.valueOf(totalReserved));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading student details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    private void loadBorrowingHistory() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            borrowingHistoryData.clear();
            
            // Get all borrowed books for this student
            String query = "SELECT Book_Title, Borrow_Date, Return_Date, Status " +
                           "FROM Borrowed_Books " +
                           "WHERE Student_ID = ? " +
                           "ORDER BY Borrow_Date DESC";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String bookTitle = rs.getString("Book_Title");
                LocalDate borrowDate = rs.getDate("Borrow_Date") != null ? 
                    rs.getDate("Borrow_Date").toLocalDate() : null;
                LocalDate returnDate = rs.getDate("Return_Date") != null ? 
                    rs.getDate("Return_Date").toLocalDate() : null;
                String status = rs.getString("Status");
                
                BorrowingHistoryEntry entry = new BorrowingHistoryEntry(
                    bookTitle, borrowDate, returnDate, status
                );
                
                borrowingHistoryData.add(entry);
            }
            
            // Update the table
            historyTable.setItems(borrowingHistoryData);
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading borrowing history: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
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
    private void navigateToMyReservations(MouseEvent event) {
        navigateToPage("account_my_reservation.fxml", event);
    }

    @FXML
    private void navigateToReportIssue(MouseEvent event) {
        navigateToPage("account_report_an_issue.fxml", event);
    }

    @FXML
    private void navigateToPenaltiesAndFines(MouseEvent event) {
        navigateToPage("account_penalties_and_fines.fxml", event);
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
    
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}