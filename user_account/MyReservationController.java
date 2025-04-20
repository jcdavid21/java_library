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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import util.DatabaseConnector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.text.Text;

public class MyReservationController implements Initializable {

    @FXML
    private Text studentNameLabel;

    @FXML
    private Text studentIdLabel;

    @FXML
    private Text totalReservationsLabel;

    @FXML
    private Text pendingReservationsLabel;

    @FXML
    private TableView<ReservedBook> reservationsTable;

    @FXML
    private TableColumn<ReservedBook, Integer> reservationIdColumn;

    @FXML
    private TableColumn<ReservedBook, String> bookTitleColumn;

    @FXML
    private TableColumn<ReservedBook, Date> reservationDateColumn;

    @FXML
    private TableColumn<ReservedBook, Date> expirationDateColumn;

    @FXML
    private TableColumn<ReservedBook, String> statusColumn;

    @FXML
    private ImageView userProfileImage;

    private String currentStudentId;
    private ObservableList<ReservedBook> reservedBooksList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        reservationDateColumn.setCellValueFactory(new PropertyValueFactory<>("reservationDate"));
        expirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set data to table
        reservationsTable.setItems(reservedBooksList);
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadStudentDetails();
        loadReservations();
        updateDashboardStats();
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

    private void loadReservations() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Reserved_Books WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();

            reservedBooksList.clear();

            while (rs.next()) {
                int reservationId = rs.getInt("Reservation_ID");
                String bookTitle = rs.getString("Book_Title");
                Date reservationDate = rs.getDate("Reservation_Date");
                Date expirationDate = rs.getDate("Expiration_Date");
                String status = rs.getString("Status");

                ReservedBook book = new ReservedBook(reservationId, bookTitle,
                        reservationDate, expirationDate, status);
                reservedBooksList.add(book);
            }

            reservationsTable.setItems(reservedBooksList);

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading reservations: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void updateDashboardStats() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // Count total reservations
            String totalQuery = "SELECT COUNT(*) FROM Reserved_Books WHERE Student_ID = ?";
            stmt = conn.prepareStatement(totalQuery);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int totalReservations = rs.getInt(1);
                totalReservationsLabel.setText(String.valueOf(totalReservations));
            }

            // Close resources before next query
            DatabaseConnector.closeResources(null, stmt, rs);

            // Count pending reservations
            String pendingQuery = "SELECT COUNT(*) FROM Reservation_Approvals " +
                    "WHERE Student_ID = ? AND Status = 'Pending'";
            stmt = conn.prepareStatement(pendingQuery);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int pendingReservations = rs.getInt(1);
                pendingReservationsLabel.setText(String.valueOf(pendingReservations));
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading dashboard statistics: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    @FXML
    private void handleCancelReservation() {
        ReservedBook selectedBook = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("No Selection", "Please select a reservation to cancel.", Alert.AlertType.WARNING);
            return;
        }

        if (!"Pending".equals(selectedBook.getStatus())) {
            showAlert("Invalid Status", "Only reservations with 'Pending' status can be cancelled.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Check if the selected book is expired or Pending
        LocalDate currentDate = LocalDate.now();
        LocalDate expirationDate = selectedBook.getExpirationDate().toLocalDate();
        if (currentDate.isAfter(expirationDate)) {
            showAlert("Invalid Action", "Cannot cancel an expired reservation.", Alert.AlertType.WARNING);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // Update status in Reserved_Books table
            String updateQuery = "UPDATE Reserved_Books SET Status = 'Cancelled' WHERE Reservation_ID = ?";
            stmt = conn.prepareStatement(updateQuery);
            stmt.setInt(1, selectedBook.getReservationId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update the status in Reservation_Approvals table if exists
                DatabaseConnector.closeResources(null, stmt, null);
                String approvalQuery = "UPDATE Reservation_Approvals SET Status = 'Rejected' WHERE Reservation_ID = ?";
                stmt = conn.prepareStatement(approvalQuery);
                stmt.setInt(1, selectedBook.getReservationId());
                stmt.executeUpdate();

                // Update the available copies count in the Books table
                DatabaseConnector.closeResources(null, stmt, null);
                String bookQuery = "UPDATE Books SET Available_Copies = Available_Copies + 1, Reserved_Copies = Reserved_Copies - 1 "
                        +
                        "WHERE Title = ?";
                stmt = conn.prepareStatement(bookQuery);
                stmt.setString(1, selectedBook.getBookTitle());
                stmt.executeUpdate();

                conn.commit();
                showAlert("Success", "Reservation cancelled successfully!", Alert.AlertType.INFORMATION);
                loadReservations();
                updateDashboardStats();
            } else {
                conn.rollback();
                showAlert("Error", "Failed to cancel reservation.", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert("Database Error", "Error cancelling reservation: " + e.getMessage(), Alert.AlertType.ERROR);
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
        }
    }

    public void refreshDashboardData() {
        loadReservations();
        updateDashboardStats();
    }

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

    // Model class for Reserved Books
    public static class ReservedBook {
        private final int reservationId;
        private final String bookTitle;
        private final Date reservationDate;
        private final Date expirationDate;
        private final String status;

        public ReservedBook(int reservationId, String bookTitle, Date reservationDate,
                Date expirationDate, String status) {
            this.reservationId = reservationId;
            this.bookTitle = bookTitle;
            this.reservationDate = reservationDate;
            this.expirationDate = expirationDate;
            this.status = status;
        }

        public int getReservationId() {
            return reservationId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public Date getReservationDate() {
            return reservationDate;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public String getStatus() {
            return status;
        }
    }
}