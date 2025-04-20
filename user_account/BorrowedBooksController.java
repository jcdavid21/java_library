package user_account;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import util.DatabaseConnector;
import user_account.BorrowedBook;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class BorrowedBooksController implements Initializable {

    // FXML UI components
    @FXML
    private Text studentNameText;
    @FXML
    private Text studentIdText;
    @FXML
    private Text currentBorrowedCountLabel; // Changed from Label to Text
    @FXML
    private Text dueSoonCountLabel; // Changed from Label to Text
    @FXML
    private ImageView profileImageView;

    @FXML
    private TableView<BorrowedBook> borrowedBooksTable;
    @FXML
    private TableColumn<BorrowedBook, Integer> idColumn;
    @FXML
    private TableColumn<BorrowedBook, String> titleColumn;
    @FXML
    private TableColumn<BorrowedBook, String> authorColumn;
    @FXML
    private TableColumn<BorrowedBook, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<BorrowedBook, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<BorrowedBook, String> statusColumn;
    @FXML
    private TableColumn<BorrowedBook, Button> actionColumn;

    // Student ID passed from previous page
    private String currentStudentId;
    // Observable list to store borrowed books
    private ObservableList<BorrowedBook> borrowedBooksList = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeTableColumns();
    }

    /**
     * Set up the table columns for the borrowed books table
     */
    private void initializeTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("returnButton"));
    }

    /**
     * Set the current student ID and load their data
     * 
     * @param studentId The ID of the current student
     */
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadStudentDetails();
        loadBorrowedBooks();
    }

    /**
     * Load the student's personal details
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
                studentNameText.setText(studentName);
                studentIdText.setText(currentStudentId);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading student details: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }

    /**
     * Load all borrowed books for the current student
     */
    private void loadBorrowedBooks() {
        borrowedBooksList.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();
            // Query to get borrowed books for this student
            String query = "SELECT bb.Borrow_ID, bb.Book_ID, bb.Book_Title, b.Author, " +
                    "bb.Borrow_Date, bb.Due_Date, bb.Return_Date, bb.Status, bb.Penalty_Fee " +
                    "FROM Borrowed_Books bb " +
                    "JOIN Books b ON bb.Book_ID = b.Book_ID " +
                    "WHERE bb.Student_ID = ? AND (bb.Status = 'Borrowed' OR bb.Status = 'Overdue')";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, currentStudentId);

            rs = stmt.executeQuery();

            int currentBorrowedCount = 0;
            int dueSoonCount = 0;
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                int borrowId = rs.getInt("Borrow_ID");
                int bookId = rs.getInt("Book_ID");
                String title = rs.getString("Book_Title");
                String author = rs.getString("Author");
                LocalDate borrowDate = rs.getDate("Borrow_Date").toLocalDate();
                LocalDate dueDate = rs.getDate("Due_Date").toLocalDate();
                String status = rs.getString("Status");
                double fine = rs.getDouble("Penalty_Fee");

                // Check if the book is due soon (within 3 days)
                long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
                if (daysUntilDue >= 0 && daysUntilDue <= 3) {
                    dueSoonCount++;
                }

                // Update status if the book is overdue
                if (dueDate.isBefore(today) && "Borrowed".equals(status)) {
                    status = "Overdue";
                    // Update the status in the database
                    updateBookStatus(borrowId, "Overdue");
                }

                // Create return button for each row
                Button returnButton = new Button("Return");
                returnButton.getStyleClass().add("return-button");
                returnButton.setOnAction(e -> handleBookReturn(borrowId, bookId));

                BorrowedBook book = new BorrowedBook(borrowId, bookId, title, author, borrowDate, dueDate, status,
                        fine);
                book.setReturnButton(returnButton);
                borrowedBooksList.add(book);
                currentBorrowedCount++;
            }

            // Update UI with the counts
            currentBorrowedCountLabel.setText(String.valueOf(currentBorrowedCount));
            dueSoonCountLabel.setText(String.valueOf(dueSoonCount));

            // Set the table items
            borrowedBooksTable.setItems(borrowedBooksList);

        } catch (SQLException e) {
            showAlert("Database Error", "Error loading borrowed books: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }

    /**
     * Update the status of a book in the database
     * 
     * @param borrowId  The ID of the borrow record
     * @param newStatus The new status to set
     */
    private void updateBookStatus(int borrowId, String newStatus) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String updateQuery = "UPDATE Borrowed_Books SET Status = ? WHERE Borrow_ID = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, borrowId);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle returning a book
     * 
     * @param borrowId The ID of the borrow record
     * @param bookId   The ID of the book being returned
     */
    private void handleBookReturn(int borrowId, int bookId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false); // Start transaction
    
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
            // 1. Update the Borrowed_Books table
            String updateBorrowQuery = "UPDATE Borrowed_Books SET Return_Date = ?, Status = 'Returned' WHERE Borrow_ID = ?";
            stmt = conn.prepareStatement(updateBorrowQuery);
            stmt.setString(1, today.format(formatter));
            stmt.setInt(2, borrowId);
            stmt.executeUpdate();
            stmt.close();
    
            // 2. Update the available copies in the Books table
            String updateBookQuery = "UPDATE Books SET Available_Copies = Available_Copies + 1 WHERE Book_ID = ?";
            stmt = conn.prepareStatement(updateBookQuery);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            stmt.close();
    
            // 3. Add a system log entry
            String logQuery = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), 'Student', ?, ?)";
            stmt = conn.prepareStatement(logQuery);
            stmt.setString(1, currentStudentId);
            stmt.setString(2, "Returned book ID: " + bookId);
            stmt.executeUpdate();
            stmt.close();
    
            // GET TOTAL BORROWED BOOKS
            String totalBorrowedQuery = "SELECT COUNT(*) AS Total_Borrowed_Books FROM Borrowed_Books WHERE Student_ID = ? and Status = 'Returned'";
            stmt = conn.prepareStatement(totalBorrowedQuery);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            int totalBorrowedBooks = 0;
            if (rs.next()) {
                totalBorrowedBooks = rs.getInt("Total_Borrowed_Books");
            }
            rs.close();
            stmt.close();
    
            // GET TOTAL RESERVED BOOKS
            String totalReservedQuery = "SELECT COUNT(*) AS Total_Reserved_Books FROM Reserved_Books WHERE Student_ID = ? and Status = 'Reserved'";
            stmt = conn.prepareStatement(totalReservedQuery);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            int totalReservedBooks = 0;
            if (rs.next()) {
                totalReservedBooks = rs.getInt("Total_Reserved_Books");
            }
            rs.close();
            stmt.close();
    
            // Check if the student has data in the borrowing history table
            String checkQuery = "SELECT COUNT(*) AS Total_Borrowed_Books FROM Borrowing_History WHERE Student_ID = ?";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, currentStudentId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int totalBorrowed = rs.getInt("Total_Borrowed_Books");
                rs.close();
                stmt.close();
                
                if (totalBorrowed > 0) {
                    // Update the data
                    String updateQuery2 = "UPDATE Borrowing_History SET Total_Borrowed_Books = ?, Total_Reserved_Books = ? WHERE Student_ID = ?";
                    stmt = conn.prepareStatement(updateQuery2);
                    stmt.setInt(1, totalBorrowedBooks);
                    stmt.setInt(2, totalReservedBooks);
                    stmt.setString(3, currentStudentId);
                    stmt.executeUpdate();
                } else {
                    // Insert the data
                    String insertQuery = "INSERT INTO Borrowing_History (Student_ID, Student_Name, Total_Borrowed_Books, Total_Reserved_Books) VALUES (?, ?, ?, ?)";
                    stmt = conn.prepareStatement(insertQuery);
                    stmt.setString(1, currentStudentId);
                    stmt.setString(2, studentNameText.getText());
                    stmt.setInt(3, totalBorrowedBooks);
                    stmt.setInt(4, totalReservedBooks);
                    stmt.executeUpdate();
                }
            }
    
            conn.commit(); // Commit transaction
            showAlert("Success", "Book returned successfully!", Alert.AlertType.INFORMATION);
            loadBorrowedBooks(); // Refresh the table
            
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    showAlert("Database Error", "Error during rollback: " + ex.getMessage(), Alert.AlertType.ERROR);
                    ex.printStackTrace();
                }
            }
            showAlert("Database Error", "Error returning book: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    

    /**
     * Refresh dashboard data
     */
    public void refreshDashboardData() {
        if (currentStudentId != null && !currentStudentId.isEmpty()) {
            loadStudentDetails();
            loadBorrowedBooks();
        }
    }

    /**
     * Show an alert dialog
     * 
     * @param title   The title of the alert
     * @param content The message content
     * @param type    The alert type
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
}