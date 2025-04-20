package student_teacher_account;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import util.DatabaseConnector;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.Node;

public class BrowseByCategoryController implements Initializable {

    private String currentStudentId;

    // Category buttons
    @FXML
    private Button allBooksBtn;
    @FXML
    private Button thesesBtn;
    @FXML
    private Button journalsBtn;
    @FXML
    private Button referenceBtn;
    @FXML
    private Button newspaperBtn;
    @FXML
    private Button magazineBtn;

    // Book display elements (8 books)
    @FXML
    private Text book1Title, book2Title, book3Title, book4Title, book5Title, book6Title, book7Title, book8Title;
    @FXML
    private Text book1ISBN, book2ISBN, book3ISBN, book4ISBN, book5ISBN, book6ISBN, book7ISBN, book8ISBN;
    @FXML
    private Text book1Author, book2Author, book3Author, book4Author, book5Author, book6Author, book7Author, book8Author;
    @FXML
    private Text book1Publisher, book2Publisher, book3Publisher, book4Publisher, book5Publisher, book6Publisher,
            book7Publisher, book8Publisher;
    @FXML
    private Button book1Button, book2Button, book3Button, book4Button, book5Button, book6Button, book7Button,
            book8Button, homeButton;
    @FXML
    private ImageView userProfileIcon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set default category button styles
        styleSelectedButton(allBooksBtn);

        // Initialize UI components and load default books
        loadBooksByCategory("All");

        // Set up home button event handler
        homeButton.setOnAction(event -> navigateToHome(event));
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    // Category button handlers
    @FXML
    private void handleAllBooks() {
        resetCategoryButtonStyles();
        styleSelectedButton(allBooksBtn);
        loadBooksByCategory("All");
    }

    @FXML
    private void handleThesesDissertations() {
        resetCategoryButtonStyles();
        styleSelectedButton(thesesBtn);
        loadBooksByCategory("Theses & Dissertations");
    }

    @FXML
    private void handleJournalsResearch() {
        resetCategoryButtonStyles();
        styleSelectedButton(journalsBtn);
        loadBooksByCategory("Journals & Research");
    }

    @FXML
    private void handleReferenceMaterials() {
        resetCategoryButtonStyles();
        styleSelectedButton(referenceBtn);
        loadBooksByCategory("Reference Materials");
    }

    @FXML
    private void handleNewspaper() {
        resetCategoryButtonStyles();
        styleSelectedButton(newspaperBtn);
        loadBooksByCategory("Newspaper");
    }

    @FXML
    private void handleMagazine() {
        resetCategoryButtonStyles();
        styleSelectedButton(magazineBtn);
        loadBooksByCategory("Magazine");
    }

    @FXML
    private void navigateToHome(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("student_teacher_home_page.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the student ID directly
            Object controller = loader.getController();

            // Check if controller is null before using reflection
            if (controller != null) {
                try {
                    Method setStudentIdMethod = controller.getClass().getMethod("setCurrentStudentId", String.class);
                    setStudentIdMethod.invoke(controller, currentStudentId);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Warning: Failed to pass student ID to the dashboard: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with navigation even if passing studentId fails
                }
            } else {
                System.out.println("Warning: Dashboard controller is null");
                // Continue with navigation even if controller is null
            }

            Stage stage = (Stage) homeButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Home: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToProfile(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("../user_account/account_dashboard.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the student ID directly
            Object controller = loader.getController();

            // Check if controller is null before using reflection
            if (controller != null) {
                try {
                    Method setStudentIdMethod = controller.getClass().getMethod("setCurrentStudentId", String.class);
                    setStudentIdMethod.invoke(controller, currentStudentId);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println("Warning: Failed to pass student ID to the dashboard: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with navigation even if passing studentId fails
                }
            } else {
                System.out.println("Warning: Dashboard controller is null");
                // Continue with navigation even if controller is null
            }

            // Get the stage from the event source instead of homeButton
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Home: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void resetCategoryButtonStyles() {
        String defaultStyle = "-fx-background-color: #00ce63; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;";
        allBooksBtn.setStyle(defaultStyle);
        thesesBtn.setStyle(defaultStyle);
        journalsBtn.setStyle(defaultStyle);
        referenceBtn.setStyle(defaultStyle);
        newspaperBtn.setStyle(defaultStyle);
        magazineBtn.setStyle(defaultStyle);
    }

    private void styleSelectedButton(Button button) {
        button.setStyle(
                "-fx-background-color: #38793b; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;");
    }

    private void loadBooksByCategory(String category) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            String query;
            if (category.equals("All")) {
                query = "SELECT b.Book_ID, b.ISBN, b.Title, b.Author, b.Category, " +
                        "b.Available_Copies, b.Reserved_Copies, b.Times_Borrowed " +
                        "FROM Books b " +
                        "WHERE b.Available_Copies > 0 OR b.Reserved_Copies > 0 " +
                        "ORDER BY b.Times_Borrowed DESC LIMIT 8";
                stmt = conn.prepareStatement(query);
            } else {
                query = "SELECT b.Book_ID, b.ISBN, b.Title, b.Author, b.Category, " +
                        "b.Available_Copies, b.Reserved_Copies, b.Times_Borrowed " +
                        "FROM Books b " +
                        "WHERE b.Category = ? AND (b.Available_Copies > 0 OR b.Reserved_Copies > 0) " +
                        "ORDER BY b.Times_Borrowed DESC LIMIT 8";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, category);
            }

            rs = stmt.executeQuery();

            // Reset all book displays first
            resetBookDisplays();

            int bookCount = 0;
            while (rs.next() && bookCount < 8) {
                String bookId = rs.getString("Book_ID");
                String isbn = rs.getString("ISBN");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String categoryName = rs.getString("Category");
                int availableCopies = rs.getInt("Available_Copies");
                int reservedCopies = rs.getInt("Reserved_Copies");
                int timesBorrowed = rs.getInt("Times_Borrowed");

                String publisherInfo = "Category: " + categoryName;

                // Update UI based on book number
                switch (bookCount) {
                    case 0:
                        updateBookDisplay(book1Title, book1ISBN, book1Author, book1Publisher,
                                book1Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 1:
                        updateBookDisplay(book2Title, book2ISBN, book2Author, book2Publisher,
                                book2Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 2:
                        updateBookDisplay(book3Title, book3ISBN, book3Author, book3Publisher,
                                book3Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 3:
                        updateBookDisplay(book4Title, book4ISBN, book4Author, book4Publisher,
                                book4Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 4:
                        updateBookDisplay(book5Title, book5ISBN, book5Author, book5Publisher,
                                book5Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 5:
                        updateBookDisplay(book6Title, book6ISBN, book6Author, book6Publisher,
                                book6Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 6:
                        updateBookDisplay(book7Title, book7ISBN, book7Author, book7Publisher,
                                book7Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                    case 7:
                        updateBookDisplay(book8Title, book8ISBN, book8Author, book8Publisher,
                                book8Button, title, isbn, author, publisherInfo,
                                availableCopies, reservedCopies, bookId, timesBorrowed);
                        break;
                }
                bookCount++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load books: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Close statement and resultset but not connection
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // Don't close the connection as it's a singleton
        }
    }

    private void updateBookDisplay(Text titleText, Text isbnText, Text authorText,
            Text publisherText, Button actionButton,
            String title, String isbn, String author, String publisherInfo,
            int availableCopies, int reservedCopies, String bookId, int timesBorrowed) {
        titleText.setText(title);
        isbnText.setText("ISBN: " + isbn);
        authorText.setText("By: " + author);
        publisherText.setText(publisherInfo);

        actionButton.setId("book-" + bookId);

        actionButton.setOnAction(event -> handleBookAction(bookId, title, availableCopies, reservedCopies));

        if (availableCopies > 0) {
            actionButton.setText("BORROW / RESERVE");
            actionButton.setStyle(
                    "-fx-background-color: #00ce63; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        } else if (reservedCopies > 0) {
            actionButton.setText("RESERVED");
            actionButton.setStyle(
                    "-fx-background-color: #727070; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        } else {
            actionButton.setText("UNAVAILABLE");
            actionButton.setStyle(
                    "-fx-background-color: #ff0000; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        }
    }

    private void handleBookAction(String bookId, String bookTitle, int availableCopies, int reservedCopies) {
        if (availableCopies > 0) {
            // Show option to borrow or reserve
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Book Action");
            alert.setHeaderText("Choose Action for: " + bookTitle);
            alert.setContentText("Available copies: " + availableCopies + "\nWould you like to:");

            ButtonType borrowButton = new ButtonType("Borrow");
            ButtonType reserveButton = new ButtonType("Reserve");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());

            alert.getButtonTypes().setAll(borrowButton, reserveButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == borrowButton) {
                    processBorrowBook(bookId, bookTitle);
                } else if (result.get() == reserveButton) {
                    processReserveBook(bookId, bookTitle);
                }
            }
        } else if (reservedCopies > 0) {
            showAlert("Book Reserved", "This book is currently reserved by other students.",
                    Alert.AlertType.INFORMATION);
        } else {
            showAlert("Book Unavailable", "This book is currently not available for borrowing.",
                    Alert.AlertType.WARNING);
        }
    }

    private void processBorrowBook(String bookId, String bookTitle) {
        Connection conn = DatabaseConnector.getConnection();
        PreparedStatement checkStmt = null;
        PreparedStatement borrowStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Get student name first
            String studentName = getStudentName(currentStudentId);

            // Check if student already has this book borrowed
            String checkQuery = "SELECT COUNT(*) FROM Borrowed_Books WHERE Student_ID = ? AND Book_ID = ? AND Status = 'Borrowed'";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, currentStudentId);
            checkStmt.setString(2, bookId);
            rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Already Borrowed", "You have already borrowed this book.", Alert.AlertType.WARNING);
                return;
            }

            // Close the result set before reusing
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // Check available copies
            String copiesQuery = "SELECT Available_Copies FROM Books WHERE Book_ID = ?";
            checkStmt.close();
            checkStmt = conn.prepareStatement(copiesQuery);
            checkStmt.setString(1, bookId);
            rs = checkStmt.executeQuery();

            int availableCopies = 0;
            if (rs.next()) {
                availableCopies = rs.getInt(1);
            }

            if (availableCopies <= 0) {
                showAlert("Not Available", "This book is no longer available for borrowing.", Alert.AlertType.WARNING);
                return;
            }

            // Calculate due date (14 days from now)
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(14);

            // Begin transaction
            conn.setAutoCommit(false);

            try {
                // Insert into Borrowed_Books table
                String insertQuery = "INSERT INTO Borrowed_Books (Student_ID, Student_Name, Book_ID, Book_Title, " +
                        "Borrow_Date, Due_Date, Status) VALUES (?, ?, ?, ?, ?, ?, 'Borrowed')";
                borrowStmt = conn.prepareStatement(insertQuery);
                borrowStmt.setString(1, currentStudentId);
                borrowStmt.setString(2, studentName);
                borrowStmt.setString(3, bookId);
                borrowStmt.setString(4, bookTitle);
                borrowStmt.setDate(5, java.sql.Date.valueOf(borrowDate));
                borrowStmt.setDate(6, java.sql.Date.valueOf(dueDate));
                borrowStmt.executeUpdate();

                // Update book copies
                String updateQuery = "UPDATE Books SET Available_Copies = Available_Copies - 1, " +
                        "Times_Borrowed = Times_Borrowed + 1 WHERE Book_ID = ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, bookId);
                updateStmt.executeUpdate();

                // Update borrowing history
                updateBorrowingHistory(currentStudentId, studentName);

                // Commit transaction
                conn.commit();

                showAlert("Success", "Book borrowed successfully! Due date: " + dueDate,
                        Alert.AlertType.INFORMATION);
                loadBooksByCategory("All"); // Refresh the view
            } catch (SQLException e) {
                // Rollback transaction if an error occurs
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                throw e;
            } finally {
                // Reset auto-commit
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to borrow book: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Close resources but not the connection
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkStmt != null) {
                try {
                    checkStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (borrowStmt != null) {
                try {
                    borrowStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (updateStmt != null) {
                try {
                    updateStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processReserveBook(String bookId, String bookTitle) {
        Connection conn = DatabaseConnector.getConnection();
        PreparedStatement checkStmt = null;
        PreparedStatement reserveStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Get student name first
            String studentName = getStudentName(currentStudentId);

            // Check if student already has this book reserved
            String checkQuery = "SELECT COUNT(*) FROM Reserved_Books WHERE Student_ID = ? AND Book_ID = ? AND Status = 'Reserved'";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, currentStudentId);
            checkStmt.setString(2, bookId);
            rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Already Reserved", "You have already reserved this book.", Alert.AlertType.WARNING);
                return;
            }

            // Close the result set before reusing
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // Check available copies
            String copiesQuery = "SELECT Available_Copies FROM Books WHERE Book_ID = ?";
            checkStmt.close();
            checkStmt = conn.prepareStatement(copiesQuery);
            checkStmt.setString(1, bookId);
            rs = checkStmt.executeQuery();

            int availableCopies = 0;
            if (rs.next()) {
                availableCopies = rs.getInt(1);
            }

            if (availableCopies <= 0) {
                showAlert("Not Available", "This book is no longer available for reservation.",
                        Alert.AlertType.WARNING);
                return;
            }

            // Calculate expiration date (7 days from now)
            LocalDate reservationDate = LocalDate.now();
            LocalDate expirationDate = reservationDate.plusDays(7);

            // Begin transaction
            conn.setAutoCommit(false);

            try {
                // get the last reservation_id
                String lastIdQuery = "SELECT MAX(Reservation_ID) FROM Reserved_Books";
                checkStmt = conn.prepareStatement(lastIdQuery);
                rs = checkStmt.executeQuery();
                int lastId = 0;
                if (rs.next()) {
                    lastId = rs.getInt(1);
                }
                int newId = lastId + 1;

                // Insert into Reserved_Books table
                String insertQuery = "INSERT INTO Reserved_Books (Reservation_ID, Student_ID, Student_Name, Book_ID, Book_Title, "
                        +
                        "Reservation_Date, Expiration_Date, Status) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
                reserveStmt = conn.prepareStatement(insertQuery);
                reserveStmt.setInt(1, newId);
                reserveStmt.setString(2, currentStudentId);
                reserveStmt.setString(3, studentName);
                reserveStmt.setString(4, bookId);
                reserveStmt.setString(5, bookTitle);
                reserveStmt.setDate(6, java.sql.Date.valueOf(reservationDate));
                reserveStmt.setDate(7, java.sql.Date.valueOf(expirationDate));
                reserveStmt.executeUpdate();

                //INSERT INTO RESERVATION APPROVALS
                String insertApprovalQuery = "INSERT INTO Reservation_Approvals (Reservation_ID, Student_ID, Book_Title, Reservation_Date, Expiration_Date, Status)"
                + " VALUES (?, ?, ?, ?, ?, 'Pending')";
                PreparedStatement insertApprovalStmt = conn.prepareStatement(insertApprovalQuery);
                insertApprovalStmt.setInt(1, newId);
                insertApprovalStmt.setString(2, currentStudentId);
                insertApprovalStmt.setString(3, bookTitle);
                insertApprovalStmt.setDate(4, java.sql.Date.valueOf(reservationDate));
                insertApprovalStmt.setDate(5, java.sql.Date.valueOf(expirationDate));
                insertApprovalStmt.executeUpdate();

                // Update book copies
                String updateQuery = "UPDATE Books SET Available_Copies = Available_Copies - 1, " +
                        "Reserved_Copies = Reserved_Copies + 1 WHERE Book_ID = ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, bookId);
                updateStmt.executeUpdate();

                // Update borrowing history
                updateBorrowingHistory(currentStudentId, studentName);

                // Commit transaction
                conn.commit();

                showAlert("Success", "Book reserved successfully! Wait for the approval until " + expirationDate,
                        Alert.AlertType.INFORMATION);
                loadBooksByCategory("All"); // Refresh the view
            } catch (SQLException e) {
                // Rollback transaction if an error occurs
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                throw e;
            } finally {
                // Reset auto-commit
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to reserve book: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Close resources but not the connection
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkStmt != null) {
                try {
                    checkStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (reserveStmt != null) {
                try {
                    reserveStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (updateStmt != null) {
                try {
                    updateStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getStudentName(String studentId) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT Student_Name FROM Students WHERE Student_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            return rs.next() ? rs.getString("Student_Name") : "Unknown Student";
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // Don't close the connection
        }
    }

    private void updateBorrowingHistory(String studentId, String studentName) throws SQLException {
        Connection conn = DatabaseConnector.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Check if history exists
            String checkQuery = "SELECT COUNT(*) FROM Borrowing_History WHERE Student_ID = ?";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            boolean historyExists = rs.next() && rs.getInt(1) > 0;

            // Close resources before reusing
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();

            if (historyExists) {
                // Update existing record
                String updateQuery = "UPDATE Borrowing_History SET Total_Borrowed_Books = " +
                        "(SELECT COUNT(*) FROM Borrowed_Books WHERE Student_ID = ?), " +
                        "Total_Reserved_Books = (SELECT COUNT(*) FROM Reserved_Books WHERE Student_ID = ?) " +
                        "WHERE Student_ID = ?";
                stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, studentId);
                stmt.setString(2, studentId);
                stmt.setString(3, studentId);
                stmt.executeUpdate();
            } else {
                // Insert new record
                String insertQuery = "INSERT INTO Borrowing_History (Student_ID, Student_Name, " +
                        "Total_Borrowed_Books, Total_Reserved_Books) VALUES (?, ?, " +
                        "(SELECT COUNT(*) FROM Borrowed_Books WHERE Student_ID = ?), " +
                        "(SELECT COUNT(*) FROM Reserved_Books WHERE Student_ID = ?))";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, studentId);
                stmt.setString(2, studentName);
                stmt.setString(3, studentId);
                stmt.setString(4, studentId);
                stmt.executeUpdate();
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            // Don't close the connection
        }
    }

    private void resetBookDisplays() {
        Text[] titleTexts = { book1Title, book2Title, book3Title, book4Title, book5Title, book6Title, book7Title,
                book8Title };
        Text[] isbnTexts = { book1ISBN, book2ISBN, book3ISBN, book4ISBN, book5ISBN, book6ISBN, book7ISBN, book8ISBN };
        Text[] authorTexts = { book1Author, book2Author, book3Author, book4Author, book5Author, book6Author,
                book7Author, book8Author };
        Text[] publisherTexts = { book1Publisher, book2Publisher, book3Publisher, book4Publisher, book5Publisher,
                book6Publisher, book7Publisher, book8Publisher };
        Button[] buttons = { book1Button, book2Button, book3Button, book4Button, book5Button, book6Button, book7Button,
                book8Button };

        for (int i = 0; i < 8; i++) {
            titleTexts[i].setText("No book available");
            isbnTexts[i].setText("");
            authorTexts[i].setText("");
            publisherTexts[i].setText("");
            buttons[i].setText("UNAVAILABLE");
            buttons[i].setStyle(
                    "-fx-background-color: #727070; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
            buttons[i].setOnAction(event -> {
                System.out.println("No action available for this slot");
            });
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}