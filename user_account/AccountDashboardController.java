package user_account;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.DatabaseConnector;
import javafx.scene.text.Text;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.ListView;
import java.sql.Date;

public class AccountDashboardController implements Initializable {

    @FXML
    private Button homeButton;
    @FXML
    private ImageView userProfileIcon;
    @FXML
    private Text studentNameLabel;
    @FXML
    private Text studentNumberLabel;
    @FXML
    private Text totalBorrowedBooksLabel;
    @FXML
    private Text dueSoonBooksLabel;

    // Add these new Text fields to your controller class
    @FXML
    private Text totalReservedBooksLabel;
    @FXML
    private Text overdueBooksLabel;
    @FXML
    private Text totalFinesLabel;
    // Add this to your controller class
    @FXML private ListView<String> dueSoonListView;

    private String currentStudentId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization that doesn't depend on studentId
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        refreshDashboardData();
    }

    // Add this method
    private void loadDueSoonNotifications(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT Book_Title, Due_Date FROM Due_Books_Notifications WHERE Student_ID = ? ORDER BY Due_Date ASC";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            dueSoonListView.getItems().clear();
            while (rs.next()) {
                String bookTitle = rs.getString("Book_Title");
                Date dueDate = rs.getDate("Due_Date");
                String notification = String.format("%s (Due: %s)", bookTitle, dueDate.toString());
                dueSoonListView.getItems().add(notification);
            }
            
            if (dueSoonListView.getItems().isEmpty()) {
                dueSoonListView.getItems().add("No books due soon");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void refreshDashboardData() {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection is not available");
            }

            // Load all data in a single transaction
            conn.setAutoCommit(false);

            loadStudentInfo(conn);
            loadBorrowedBooksCount(conn);
            loadDueSoonBooksCount(conn);
            loadReservedBooksCount(conn); // New method
            loadOverdueBooksCount(conn); // New method
            loadTotalFines(conn); // New method
            loadDueSoonNotifications(conn); // Add this line

            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
            }
            showAlert("Database Error", "Failed to load dashboard data: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            // Note: We don't close the connection here since DatabaseConnector manages it
            // Reset auto-commit to true for future operations
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private void loadStudentInfo(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT Student_Name FROM Borrowing_History WHERE Student_ID = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String studentName = rs.getString("Student_Name");
                studentNameLabel.setText(studentName);
                studentNumberLabel.setText(currentStudentId);
            } else {
                // If not found in borrowing history, try borrowed books table
                DatabaseConnector.closeResources(null, pstmt, rs);

                query = "SELECT DISTINCT Student_Name FROM Borrowed_Books WHERE Student_ID = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, currentStudentId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    String studentName = rs.getString("Student_Name");
                    studentNameLabel.setText(studentName);
                    studentNumberLabel.setText(currentStudentId);
                } else {
                    studentNameLabel.setText("Unknown Student");
                    studentNumberLabel.setText(currentStudentId);
                }
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void loadBorrowedBooksCount(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT COUNT(*) AS total FROM Borrowed_Books WHERE Student_ID = ? AND Status = 'Borrowed'";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalBorrowed = rs.getInt("total");
                totalBorrowedBooksLabel.setText(String.valueOf(totalBorrowed));
            } else {
                totalBorrowedBooksLabel.setText("0");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void loadDueSoonBooksCount(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT COUNT(*) AS total FROM Due_Books_Notifications WHERE Student_ID = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int dueSoon = rs.getInt("total");
                dueSoonBooksLabel.setText(String.valueOf(dueSoon));
            } else {
                dueSoonBooksLabel.setText("0");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    @FXML
    private void navigateToHome(javafx.event.ActionEvent event) {
        navigateToPage("../student_teacher_account/student_teacher_home_page.fxml", event);
    }

    @FXML
    private void navigateToProfile(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_dashboard.fxml", event);
    }

    @FXML
    private void navigateToMyBorrowedBooks(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_my_borrowed_books.fxml", event);
    }

    @FXML
    private void navigateToBorrowingHistory(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_borrowing_history.fxml", event);
    }

    @FXML
    private void navigateToMyReservations(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_my_reservation.fxml", event);
    }

    @FXML
    private void navigateToReportIssue(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_report_an_issue.fxml", event);
    }

    @FXML
    private void navigateToPenaltiesAndFines(javafx.scene.input.MouseEvent event) {
        navigateToPage("account_penalties_and_fines.fxml", event);
    }

    @FXML
    private void logout(javafx.scene.input.MouseEvent event) {
        try {
            // Close database connection on logout
            DatabaseConnector.closeConnection();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../landing_page/landing_page.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not log out: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

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

    private void loadReservedBooksCount(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT COUNT(*) AS total FROM Reserved_Books WHERE Student_ID = ? AND Status = 'Reserved'";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalReserved = rs.getInt("total");
                totalReservedBooksLabel.setText(String.valueOf(totalReserved));
            } else {
                totalReservedBooksLabel.setText("0");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void loadOverdueBooksCount(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT COUNT(*) AS total FROM Borrowed_Books WHERE Student_ID = ? AND Status = 'Overdue'";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int overdue = rs.getInt("total");
                overdueBooksLabel.setText(String.valueOf(overdue));
            } else {
                overdueBooksLabel.setText("0");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void loadTotalFines(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT SUM(Fine_Amount) AS total FROM Penalties_Fines WHERE Student_ID = ? AND Status = 'Unpaid'";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentStudentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                double totalFines = rs.getDouble("total");
                totalFinesLabel.setText(String.format("₱%.2f", totalFines));
            } else {
                totalFinesLabel.setText("₱0.00");
            }
        } finally {
            DatabaseConnector.closeResources(null, pstmt, rs);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}