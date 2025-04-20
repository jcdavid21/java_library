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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import user_account.PenaltyFine;
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

public class PenaltiesAndFinesController implements Initializable {

    @FXML
    private Label studentNameLabel;
    
    @FXML
    private Label studentIdLabel;
    
    @FXML
    private Label totalFinesLabel;
    
    @FXML
    private TableView<PenaltyFine> penaltiesTable;
    
    @FXML
    private TableColumn<PenaltyFine, String> bookTitleColumn;
    
    @FXML
    private TableColumn<PenaltyFine, String> issueTypeColumn;
    
    @FXML
    private TableColumn<PenaltyFine, Double> fineAmountColumn;
    
    @FXML
    private TableColumn<PenaltyFine, LocalDate> dueDateColumn;
    
    @FXML
    private TableColumn<PenaltyFine, LocalDate> returnDateColumn;
    
    @FXML
    private TableColumn<PenaltyFine, String> statusColumn;
    
    private String currentStudentId;
    private ObservableList<PenaltyFine> penaltyData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        issueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("issueType"));
        fineAmountColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set table data
        penaltiesTable.setItems(penaltyData);
    }
    
    /**
     * Set the current student ID and load data
     * @param studentId The ID of the current student
     */
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadStudentDetails();
        loadPenaltyData();
    }
    
    /**
     * Load student details from the database
     */
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
                String studentName = rs.getString("Student_Name");
                studentNameLabel.setText(studentName);
                studentIdLabel.setText(currentStudentId);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading student details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    /**
     * Load penalty and fine data for the current student
     */
    private void loadPenaltyData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Penalties_Fines WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            
            // Clear existing data
            penaltyData.clear();
            
            double totalFines = 0.0;
            
            while (rs.next()) {
                String bookTitle = rs.getString("Book_Title");
                String issueType = rs.getString("Issue_Type");
                double fineAmount = rs.getDouble("Fine_Amount");
                LocalDate dueDate = rs.getDate("Due_Date") != null ? rs.getDate("Due_Date").toLocalDate() : null;
                LocalDate returnDate = rs.getDate("Return_Date") != null ? rs.getDate("Return_Date").toLocalDate() : null;
                String status = rs.getString("Status");
                
                // Add to the list
                penaltyData.add(new PenaltyFine(currentStudentId, bookTitle, issueType, fineAmount, dueDate, returnDate, status));
                
                // Calculate total fines for unpaid items
                if (status.equalsIgnoreCase("Unpaid")) {
                    totalFines += fineAmount;
                }
            }
            
            // Update total fines label
            totalFinesLabel.setText(String.format("₱%.2f", totalFines));
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading penalty data: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }
    
    /**
     * Refresh dashboard data
     */
    public void refreshDashboardData() {
        loadStudentDetails();
        loadPenaltyData();
    }
    
    /**
     * Show an alert with the specified type and message
     * @param title The title of the alert
     * @param message The message to display
     * @param alertType The type of alert
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    private void navigateToReportIssue(MouseEvent event) {
        navigateToPage("account_report_an_issue.fxml", event);
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
}