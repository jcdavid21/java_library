package student_teacher_account;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.DatabaseConnector;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import user_account.AccountDashboardController;

public class StudentDashboardController implements Initializable {

    private String currentStudentId;

    // Navigation buttons
    @FXML
    private Button browseByCategoryButton;
    @FXML
    private Button aboutUniversityButton;
    @FXML
    private Button helpSupportButton;

    // Book 1-4 elements (Frequently Borrowed Books)
    @FXML
    private Text book1Title;
    @FXML
    private Text book1ISBN;
    @FXML
    private Text book1Author;
    @FXML
    private Text book1Publisher;
    @FXML
    private Text book1BorrowCount;
    @FXML
    private Button book1Button;

    @FXML
    private Text book2Title;
    @FXML
    private Text book2ISBN;
    @FXML
    private Text book2Author;
    @FXML
    private Text book2Publisher;
    @FXML
    private Text book2BorrowCount;
    @FXML
    private Button book2Button;

    @FXML
    private Text book3Title;
    @FXML
    private Text book3ISBN;
    @FXML
    private Text book3Author;
    @FXML
    private Text book3Publisher;
    @FXML
    private Text book3BorrowCount;
    @FXML
    private Button book3Button;

    @FXML
    private Text book4Title;
    @FXML
    private Text book4ISBN;
    @FXML
    private Text book4Author;
    @FXML
    private Text book4Publisher;
    @FXML
    private Text book4BorrowCount;
    @FXML
    private Button book4Button;

    // Book 5-8 elements (Recommended Books)
    @FXML
    private Text book5Title;
    @FXML
    private Text book5ISBN;
    @FXML
    private Text book5Author;
    @FXML
    private Text book5Publisher;
    @FXML
    private Text book5BorrowCount;
    @FXML
    private Button book5Button;

    @FXML
    private Text book6Title;
    @FXML
    private Text book6ISBN;
    @FXML
    private Text book6Author;
    @FXML
    private Text book6Publisher;
    @FXML
    private Text book6BorrowCount;
    @FXML
    private Button book6Button;

    @FXML
    private Text book7Title;
    @FXML
    private Text book7ISBN;
    @FXML
    private Text book7Author;
    @FXML
    private Text book7Publisher;
    @FXML
    private Text book7BorrowCount;
    @FXML
    private Button book7Button;

    @FXML
    private Text book8Title;
    @FXML
    private Text book8ISBN;
    @FXML
    private Text book8Author;
    @FXML
    private Text book8Publisher;
    @FXML
    private Text book8BorrowCount;
    @FXML
    private Button book8Button;

    @FXML
    private ImageView userProfileIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNavigationButtons();
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadData(); // Load data when student ID is set
    }

    private void loadData() {
        if (currentStudentId != null && !currentStudentId.isEmpty()) {
            loadMostBorrowedBooks();
            loadRecommendedBooks();
        }
    }

    private void setupNavigationButtons() {
        browseByCategoryButton.setOnAction(event -> navigateToBrowseByCategory());
        aboutUniversityButton.setOnAction(event -> navigateToAboutUniversity());
        helpSupportButton.setOnAction(event -> navigateToHelpSupport());
    }

    @FXML
    private void navigateToProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../user_account/account_dashboard.fxml"));
            Parent root = loader.load();

            AccountDashboardController controller = loader.getController();
            controller.setCurrentStudentId(currentStudentId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load profile: " + e.getMessage());
        }
    }

    private void navigateToBrowseByCategory() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("student_teacher_browse_by_category.fxml"));

            // Verify the FXML file was found
            if (loader.getLocation() == null) {
                showAlert("Error", "FXML file not found!");
                return;
            }

            Parent root = loader.load();

            // Get the controller and verify it's not null
            BrowseByCategoryController controller = loader.getController();
            if (controller == null) {
                showAlert("Error", "Controller not initialized in FXML!");
                return;
            }

            // Pass the student ID
            controller.setCurrentStudentId(currentStudentId);

            // Switch scenes
            Stage stage = (Stage) browseByCategoryButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Browse by Category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToAboutUniversity() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("../about_our_university/about_our_university.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the student ID directly
            Object controller = loader.getController();

            // Use a string-based method invocation to avoid direct class reference
            if (controller != null) {
                try {
                    java.lang.reflect.Method setIdMethod = controller.getClass().getMethod("setCurrentStudentId",
                            String.class);
                    setIdMethod.invoke(controller, currentStudentId);
                } catch (Exception e) {
                    System.err.println("Could not set student ID: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            Stage stage = (Stage) aboutUniversityButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to About Our University: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToHelpSupport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../student_help/student_help.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) helpSupportButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Help & Support: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMostBorrowedBooks() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            // Updated query to match your database schema
            String query = "SELECT b.Book_ID, b.ISBN, b.Title, b.Author, " +
                    "b.Total_Copies, b.Available_Copies, b.Reserved_Copies, " +
                    "COALESCE(m.Total_Borrowed_Times, b.Times_Borrowed, 0) as Borrow_Count " +
                    "FROM Books b " +
                    "LEFT JOIN Most_Borrowed_Books m ON b.Book_ID = m.Book_ID " +
                    "ORDER BY Borrow_Count DESC LIMIT 4";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            Text[] titleFields = { book1Title, book2Title, book3Title, book4Title };
            Text[] isbnFields = { book1ISBN, book2ISBN, book3ISBN, book4ISBN };
            Text[] authorFields = { book1Author, book2Author, book3Author, book4Author };
            Text[] publisherFields = { book1Publisher, book2Publisher, book3Publisher, book4Publisher };
            Text[] countFields = { book1BorrowCount, book2BorrowCount, book3BorrowCount, book4BorrowCount };
            Button[] buttons = { book1Button, book2Button, book3Button, book4Button };

            int bookCount = 0;
            while (rs.next() && bookCount < 4) {
                String title = rs.getString("Title");
                String isbn = rs.getString("ISBN");
                String author = rs.getString("Author");
                int borrowedTimes = rs.getInt("Borrow_Count");
                int available = rs.getInt("Available_Copies");

                // Update UI
                titleFields[bookCount].setText(title);
                isbnFields[bookCount].setText(isbn);
                authorFields[bookCount].setText(author);
                publisherFields[bookCount].setText("Publisher info not available"); // Your schema doesn't have
                                                                                    // publisher
                countFields[bookCount].setText("Borrowed " + borrowedTimes + " Times");

                // Set button style based on availability
                if (available > 0) {
                    buttons[bookCount].setText("BORROW");
                    buttons[bookCount].setStyle("-fx-background-color: #00ce63;");
                } else {
                    buttons[bookCount].setText("RESERVE");
                    buttons[bookCount].setStyle("-fx-background-color: #727070;");
                }

                bookCount++;
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }

    private void loadRecommendedBooks() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // Updated query to match your database schema
            String query = "SELECT b.Book_ID, b.ISBN, b.Title, b.Author, " +
                    "b.Total_Copies, b.Available_Copies, b.Reserved_Copies " +
                    "FROM Books b " +
                    "WHERE b.Book_ID NOT IN (" +
                    "    SELECT Book_ID FROM Borrowed_Books " +
                    "    WHERE Student_ID = ? AND Status = 'Borrowed'" +
                    ") " +
                    "ORDER BY RAND() LIMIT 4";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();

            Text[] titleFields = { book5Title, book6Title, book7Title, book8Title };
            Text[] isbnFields = { book5ISBN, book6ISBN, book7ISBN, book8ISBN };
            Text[] authorFields = { book5Author, book6Author, book7Author, book8Author };
            Text[] publisherFields = { book5Publisher, book6Publisher, book7Publisher, book8Publisher };
            Text[] countFields = { book5BorrowCount, book6BorrowCount, book7BorrowCount, book8BorrowCount };
            Button[] buttons = { book5Button, book6Button, book7Button, book8Button };

            int bookCount = 0;
            while (rs.next() && bookCount < 4) {
                String title = rs.getString("Title");
                String isbn = rs.getString("ISBN");
                String author = rs.getString("Author");
                int available = rs.getInt("Available_Copies");

                // Add null checks before setting text
                if (titleFields[bookCount] != null)
                    titleFields[bookCount].setText(title);
                if (isbnFields[bookCount] != null)
                    isbnFields[bookCount].setText(isbn);
                if (authorFields[bookCount] != null)
                    authorFields[bookCount].setText(author);
                if (publisherFields[bookCount] != null)
                    publisherFields[bookCount].setText("Publisher info not available");
                if (countFields[bookCount] != null)
                    countFields[bookCount].setText("Recommended for you");

                // Set button style based on availability
                if (buttons[bookCount] != null) {
                    if (available > 0) {
                        buttons[bookCount].setText("BORROW");
                        buttons[bookCount].setStyle("-fx-background-color: #00ce63;");
                    } else {
                        buttons[bookCount].setText("RESERVE");
                        buttons[bookCount].setStyle("-fx-background-color: #727070;");
                    }
                }

                bookCount++;
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load recommended books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }

    private void updateBookDisplay(Text titleText, Text isbnText, Text authorText,
            Text publisherText, Text borrowCountText,
            String title, String isbn, String author, String publisher,
            int borrowCount) {
        // Update text fields
        titleText.setText(title);
        isbnText.setText(isbn);
        authorText.setText(author);
        publisherText.setText(publisher);
        borrowCountText.setText("Borrowed " + borrowCount + " Times");
    }

    private String getStudentName(String studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String name = "";

        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT Student_Name FROM Students WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                name = rs.getString("Student_Name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return name;
    }

    private void updateBorrowingHistory(String studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // Check if student exists in borrowing history
            String checkQuery = "SELECT * FROM Borrowing_History WHERE Student_ID = ?";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Update existing record
                stmt.close();
                String updateQuery = "UPDATE Borrowing_History SET Total_Borrowed_Books = Total_Borrowed_Books + 1 " +
                        "WHERE Student_ID = ?";
                stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, studentId);
                stmt.executeUpdate();
            } else {
                // Insert new record
                String studentName = getStudentName(studentId);
                stmt.close();
                String insertQuery = "INSERT INTO Borrowing_History (Student_ID, Student_Name, Total_Borrowed_Books, Total_Reserved_Books) "
                        +
                        "VALUES (?, ?, 1, 0)";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, studentId);
                stmt.setString(2, studentName);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateReservationHistory(String studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // Check if student exists in borrowing history
            String checkQuery = "SELECT * FROM Borrowing_History WHERE Student_ID = ?";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Update existing record
                stmt.close();
                String updateQuery = "UPDATE Borrowing_History SET Total_Reserved_Books = Total_Reserved_Books + 1 " +
                        "WHERE Student_ID = ?";
                stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, studentId);
                stmt.executeUpdate();
            } else {
                // Insert new record
                String studentName = getStudentName(studentId);
                stmt.close();
                String insertQuery = "INSERT INTO Borrowing_History (Student_ID, Student_Name, Total_Borrowed_Books, Total_Reserved_Books) "
                        +
                        "VALUES (?, ?, 0, 1)";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, studentId);
                stmt.setString(2, studentName);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addSystemLog(String userId, String userType, String action) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnector.getConnection();
            String insertQuery = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) " +
                    "VALUES (NOW(), ?, ?, ?)";
            stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, userType);
            stmt.setString(2, userId);
            stmt.setString(3, action);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}