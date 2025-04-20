package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import util.DatabaseConnector;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button dashboardButton;

    @FXML
    private Text totalBooksText;

    @FXML
    private Text totalStudentsText;

    @FXML
    private Text totalTeachersText;

    @FXML
    private Rectangle dashboardRect;

    @FXML
    private Rectangle totalBooksRect;

    @FXML
    private Rectangle totalStudentsRect;

    @FXML
    private Rectangle totalTeachersRect;

    @FXML
    private VBox recentBorrowingsVBox;

    @FXML
    private VBox popularBooksVBox;

    @FXML
    private VBox overdueBookVBox;

    @FXML
    private VBox systemLogsVBox;

    @FXML
    private Text todayBorrowedText;

    @FXML
    private Text todayReturnedText;

    @FXML
    private Text todayReservationsText;

    @FXML
    private Text todayVisitorsText;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilter;

    private int adminId;
    private String adminName;
    private String adminRole;
    private Connection conn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize the class-level conn field
            this.conn = DatabaseConnector.getConnection();

            if (conn.isValid(5)) {
                applyCardShadow(totalBooksRect);
                applyCardShadow(totalStudentsRect);
                applyCardShadow(totalTeachersRect);

                loadDashboardStatistics(conn);
                setupNavigationListeners();
            } else {
                System.err.println("Database connection is not valid");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
        }
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
        
        try {
            // Check if conn is null or closed, and get a new connection if needed
            if (this.conn == null || this.conn.isClosed()) {
                this.conn = DatabaseConnector.getConnection();
            }
            loadAdminInfo();
        } catch (SQLException e) {
            System.err.println("Error in setAdminData: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Connection Error", 
                     "Error establishing database connection. Please try again.");
        }
    }

    private void loadAdminInfo() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT First_Name, Last_Name, Role FROM Admin_Accounts WHERE Admin_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, adminId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                adminName = rs.getString("First_Name") + " " + rs.getString("Last_Name");
                adminRole = rs.getString("Role");

                // Here you can update UI elements with the admin's name if needed
                System.out.println("Logged in as: " + adminName + " (" + adminRole + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error loading admin info: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadDashboardStatistics(Connection conn) {
        loadTotalBooks(conn);
        loadTotalStudents(conn);
        loadTotalAdmins(conn);
    }

    // Added overloaded method with no parameters to use the class connection field
    private void loadDashboardStatistics() {
        loadTotalBooks(conn);
        loadTotalStudents(conn);
        loadTotalAdmins(conn);
    }

    private void loadTotalBooks(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT SUM(Total_Copies) as TotalBooks FROM Books";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int totalBooks = rs.getInt("TotalBooks");
                totalBooksText.setText(String.valueOf(totalBooks));
            }
        } catch (SQLException e) {
            System.err.println("Error loading total books: " + e.getMessage());
            totalBooksText.setText("N/A");
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadTotalStudents(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) as TotalStudents FROM Students";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int totalStudents = rs.getInt("TotalStudents");
                totalStudentsText.setText(String.valueOf(totalStudents));
            }
        } catch (SQLException e) {
            System.err.println("Error loading total students: " + e.getMessage());
            totalStudentsText.setText("N/A");
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadTotalAdmins(Connection conn) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) as TotalTeachers FROM Admin_Accounts WHERE Role = 'Librarian'";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int totalTeachers = rs.getInt("TotalTeachers");
                totalTeachersText.setText(String.valueOf(totalTeachers));
            }
        } catch (SQLException e) {
            System.err.println("Error loading total teachers: " + e.getMessage());
            totalTeachersText.setText("N/A");
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void applyCardShadow(Rectangle rect) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetX(3.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0.4, 0.4, 0.4, 0.3));
        rect.setEffect(shadow);
    }

    private void setupNavigationListeners() {
        // Dashboard button - already handled at top of the method
        dashboardButton.setOnAction(event -> {
            // Already on dashboard, could refresh
            loadDashboardStatistics();
        });

        // Set hover effects for all the menu rectangles
        dashboardRect.setOnMouseEntered(e -> dashboardRect.setOpacity(0.8));
        dashboardRect.setOnMouseExited(e -> dashboardRect.setOpacity(1.0));

        totalBooksRect.setOnMouseEntered(e -> totalBooksRect.setOpacity(0.8));
        totalBooksRect.setOnMouseExited(e -> totalBooksRect.setOpacity(1.0));
        totalBooksRect.setOnMouseClicked(e -> navigateToTotalBooks());

        totalStudentsRect.setOnMouseEntered(e -> totalStudentsRect.setOpacity(0.8));
        totalStudentsRect.setOnMouseExited(e -> totalStudentsRect.setOpacity(1.0));
        totalStudentsRect.setOnMouseClicked(e -> navigateToViewStudents());

        totalTeachersRect.setOnMouseEntered(e -> totalTeachersRect.setOpacity(0.8));
        totalTeachersRect.setOnMouseExited(e -> totalTeachersRect.setOpacity(1.0));

        // Add proper tooltips to all navigation elements
        addTooltipToNode(dashboardRect, "Dashboard");
        addTooltipToNode(totalBooksRect, "View all available books");
        addTooltipToNode(totalStudentsRect, "View all registered students");
        addTooltipToNode(totalTeachersRect, "View all librarians/teachers");

        // For accessibility, ensure all clickable elements have proper focus handling
        setupFocusHandling(dashboardRect);
        setupFocusHandling(totalBooksRect);
        setupFocusHandling(totalStudentsRect);
        setupFocusHandling(totalTeachersRect);

        // Set up quick access functions for common tasks
        setupQuickAccessButtons();

        // Load initial data for dashboard widgets
        if (recentBorrowingsVBox != null)
            loadRecentBorrowings();
        if (popularBooksVBox != null)
            loadPopularBooks();
        if (overdueBookVBox != null)
            loadOverdueBooks();
        loadTodayStatistics();
        if (systemLogsVBox != null)
            loadRecentSystemLogs();
    }

    // Helper method to add tooltips to nodes
    private void addTooltipToNode(javafx.scene.Node node, String tooltipText) {
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);
        javafx.scene.control.Tooltip.install(node, tooltip);
    }

    // Helper method to setup focus handling for accessibility
    private void setupFocusHandling(javafx.scene.Node node) {
        node.setFocusTraversable(true);
        node.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER ||
                    event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                // Trigger the same action as clicking would
                if (node == dashboardRect) {
                    loadDashboardStatistics();
                } else if (node == totalBooksRect) {
                    navigateToTotalBooks();
                } else if (node == totalStudentsRect) {
                    navigateToViewStudents();
                } else if (node == totalTeachersRect) {
                    // Action for teachers rectangle
                }
            }
        });
    }

    // Set up quick access buttons for common tasks
    private void setupQuickAccessButtons() {
        // Implementation would add buttons for quick tasks
        // like adding a new book, processing a return, etc.
    }

    // Methods to load data for dashboard widgets
    private void loadRecentBorrowings() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT b.Title, bb.Student_Name, bb.Borrow_Date " +
                    "FROM Borrowed_Books bb " +
                    "JOIN Books b ON bb.Book_ID = b.Book_ID " +
                    "ORDER BY bb.Borrow_Date DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            recentBorrowingsVBox.getChildren().clear();

            while (rs.next()) {
                String title = rs.getString("Title");
                String studentName = rs.getString("Student_Name");
                java.sql.Date borrowDate = rs.getDate("Borrow_Date");

                Label borrowLabel = new Label(title + " - borrowed by " + studentName +
                        " on " + borrowDate.toString());
                borrowLabel.setStyle("-fx-text-fill: #545454;");

                recentBorrowingsVBox.getChildren().add(borrowLabel);
            }

            if (recentBorrowingsVBox.getChildren().isEmpty()) {
                Label noDataLabel = new Label("No recent borrowings");
                noDataLabel.setStyle("-fx-text-fill: #545454; -fx-font-style: italic;");
                recentBorrowingsVBox.getChildren().add(noDataLabel);
            }

        } catch (SQLException e) {
            System.err.println("Error loading recent borrowings: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadPopularBooks() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT b.Title, b.Times_Borrowed " +
                    "FROM Books b " +
                    "ORDER BY b.Times_Borrowed DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            popularBooksVBox.getChildren().clear();

            while (rs.next()) {
                String title = rs.getString("Title");
                int count = rs.getInt("Times_Borrowed");

                Label bookLabel = new Label(title + " - borrowed " + count + " times");
                bookLabel.setStyle("-fx-text-fill: #545454;");

                popularBooksVBox.getChildren().add(bookLabel);
            }

            if (popularBooksVBox.getChildren().isEmpty()) {
                Label noDataLabel = new Label("No borrowing data available");
                noDataLabel.setStyle("-fx-text-fill: #545454; -fx-font-style: italic;");
                popularBooksVBox.getChildren().add(noDataLabel);
            }

        } catch (SQLException e) {
            System.err.println("Error loading popular books: " + e.getMessage());
            Label errorLabel = new Label("Could not load popular books");
            errorLabel.setStyle("-fx-text-fill: red;");
            popularBooksVBox.getChildren().add(errorLabel);
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadOverdueBooks() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT b.Book_Title, b.Student_Name, b.Due_Date, b.Days_Overdue " +
                    "FROM Due_Books_Notifications b " +
                    "WHERE b.Status = 'Overdue' " +
                    "ORDER BY b.Days_Overdue DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            overdueBookVBox.getChildren().clear();

            while (rs.next()) {
                String title = rs.getString("Book_Title");
                String studentName = rs.getString("Student_Name");
                java.sql.Date dueDate = rs.getDate("Due_Date");
                int daysOverdue = rs.getInt("Days_Overdue");

                Label overdueLabel = new Label(title + " - by " + studentName +
                        " (due: " + dueDate.toString() + ", " +
                        daysOverdue + " days overdue)");
                overdueLabel.setStyle("-fx-text-fill: #C62828;"); // Red for overdue

                overdueBookVBox.getChildren().add(overdueLabel);
            }

            if (overdueBookVBox.getChildren().isEmpty()) {
                Label noDataLabel = new Label("No overdue books");
                noDataLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-style: italic;"); // Green for good
                overdueBookVBox.getChildren().add(noDataLabel);
            }

        } catch (SQLException e) {
            System.err.println("Error loading overdue books: " + e.getMessage());
            Label errorLabel = new Label("Could not load overdue books");
            errorLabel.setStyle("-fx-text-fill: red;");
            overdueBookVBox.getChildren().add(errorLabel);
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadTodayStatistics() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Check if UI elements exist
            if (todayBorrowedText == null || todayReturnedText == null ||
                    todayReservationsText == null || todayVisitorsText == null) {
                return;
            }

            // Count today's borrowings
            String borrowQuery = "SELECT COUNT(*) as Count FROM Borrowed_Books WHERE DATE(Borrow_Date) = CURRENT_DATE";
            stmt = conn.prepareStatement(borrowQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                todayBorrowedText.setText(String.valueOf(rs.getInt("Count")));
            }
            rs.close();
            stmt.close();

            // Count today's returns
            String returnQuery = "SELECT COUNT(*) as Count FROM Borrowed_Books WHERE DATE(Return_Date) = CURRENT_DATE";
            stmt = conn.prepareStatement(returnQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                todayReturnedText.setText(String.valueOf(rs.getInt("Count")));
            }
            rs.close();
            stmt.close();

            // Count today's reservations
            String reserveQuery = "SELECT COUNT(*) as Count FROM Reserved_Books WHERE DATE(Reservation_Date) = CURRENT_DATE";
            stmt = conn.prepareStatement(reserveQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                todayReservationsText.setText(String.valueOf(rs.getInt("Count")));
            }
            rs.close();
            stmt.close();

            // No Library_Visits table in the schema, so we'll use System_Logs
            String visitorQuery = "SELECT COUNT(*) as Count FROM System_Logs WHERE DATE(Timestamp) = CURRENT_DATE";
            stmt = conn.prepareStatement(visitorQuery);
            rs = stmt.executeQuery();
            if (rs.next()) {
                todayVisitorsText.setText(String.valueOf(rs.getInt("Count")));
            }

        } catch (SQLException e) {
            System.err.println("Error loading today's statistics: " + e.getMessage());
            if (todayBorrowedText != null)
                todayBorrowedText.setText("N/A");
            if (todayReturnedText != null)
                todayReturnedText.setText("N/A");
            if (todayReservationsText != null)
                todayReservationsText.setText("N/A");
            if (todayVisitorsText != null)
                todayVisitorsText.setText("N/A");
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    private void loadRecentSystemLogs() {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT Timestamp, User_Type, User_ID, Action_Performed " +
                    "FROM System_Logs " +
                    "ORDER BY Timestamp DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            systemLogsVBox.getChildren().clear();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while (rs.next()) {
                java.sql.Timestamp logDate = rs.getTimestamp("Timestamp");
                String userType = rs.getString("User_Type");
                String userId = rs.getString("User_ID");
                String action = rs.getString("Action_Performed");

                String formattedDate = dateFormat.format(logDate);
                Label logLabel = new Label(formattedDate + " - " + userType + " (" + userId + "): " + action);

                // Style based on user type
                if ("Admin".equals(userType)) {
                    logLabel.setStyle("-fx-text-fill: #1565C0;"); // Blue for admin
                } else {
                    logLabel.setStyle("-fx-text-fill: #545454;"); // Normal for students
                }

                systemLogsVBox.getChildren().add(logLabel);
            }

            if (systemLogsVBox.getChildren().isEmpty()) {
                Label noDataLabel = new Label("No recent system logs");
                noDataLabel.setStyle("-fx-text-fill: #545454; -fx-font-style: italic;");
                systemLogsVBox.getChildren().add(noDataLabel);
            }

        } catch (SQLException e) {
            System.err.println("Error loading system logs: " + e.getMessage());
            Label errorLabel = new Label("Could not load system logs");
            errorLabel.setStyle("-fx-text-fill: red;");
            systemLogsVBox.getChildren().add(errorLabel);
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs);
        }
    }

    @FXML
    private void navigateToTotalBooks() {
        navigateToPage("admin_total_books_available.fxml", "Total Books");
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
    private void navigateToPenaltiesFines() {
        navigateToPage("admin_penalties_and_fines.fxml", "Penalties & Fines");
    }

    @FXML
    private void navigateToLostDamagedReports() {
        navigateToPage("admin_lost_damaged_reports.fxml", "Lost & Damaged Reports");
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
    private void navigateToLibraryVisitLogs() {
        navigateToPage("admin_library_logs.fxml", "Library Visit Logs");
    }

    @FXML
    private void logout() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) dashboardButton.getScene().getWindow();

            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();

            // Create the scene and set it on the stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - Login");
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
        }
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) dashboardButton.getScene().getWindow();

            // Load the selected view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/" + fxmlFile));
            Parent root = loader.load();

            // Try to pass admin ID to the controller if it has the method
            try {
                Object controller = loader.getController();
                if (controller != null) {
                    try {
                        controller.getClass().getMethod("setAdminData", int.class).invoke(controller, adminId);
                    } catch (NoSuchMethodException e) {
                        // Try alternate method name
                        try {
                            controller.getClass().getMethod("setAdminId", int.class).invoke(controller, adminId);
                        } catch (NoSuchMethodException ex) {
                            System.out.println("Controller doesn't have setAdminData or setAdminId method");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not pass admin ID to controller: " + e.getMessage());
            }

            // Create the scene and set it on the stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - " + title);
            currentStage.show();

        } catch (Exception e) {
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTable() {
        try {
            if (conn.isValid(5)) {
                loadBooks();
                if (searchField != null) {
                    searchField.clear();
                }
                if (categoryFilter != null) {
                    categoryFilter.getSelectionModel().selectFirst();
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to load books data (placeholder implementation since it was missing)
    private void loadBooks() {
        // Implementation would load books data into a table or list view
        System.out.println("Loading books data...");
        // Actual implementation would depend on your UI structure
    }

    // Helper method for showing alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}