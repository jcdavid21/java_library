package student_teacher_account;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class BrowseByCategoryController implements Initializable {

    private String currentStudentId;
    private String currentCategory = "All";
    private List<String> categories = new ArrayList<>();

    // Category buttons container
    @FXML
    private FlowPane categoryButtonsContainer;

    // Container for dynamically created book display cards
    @FXML
    private FlowPane booksContainer;

    @FXML
    private Button homeButton;

    @FXML
    private ImageView userProfileIcon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure the FlowPane for category buttons
        setupCategoryButtonsContainer();

        // Configure the FlowPane for books display
        setupBooksContainer();

        // Load categories from database and create buttons
        loadCategoriesFromDatabase();

        // Load default books (All category)
        loadBooksByCategory("All");

        // Set up home button event handler
        homeButton.setOnAction(event -> navigateToHome(event));

        // Setup profile icon click handler
        if (userProfileIcon != null) {
            userProfileIcon.setOnMouseClicked(event -> navigateToProfile(event));
        }
    }

    private void setupCategoryButtonsContainer() {
        if (categoryButtonsContainer != null) {
            categoryButtonsContainer.setHgap(10); // Horizontal gap between buttons
            categoryButtonsContainer.setVgap(10); // Vertical gap between rows
            categoryButtonsContainer.setPadding(new Insets(5, 5, 5, 5));
            categoryButtonsContainer.setPrefWrapLength(800); // Adjust based on your UI width
        } else {
            System.err.println("Category buttons container is null! Check your FXML file.");
        }
    }

    private void setupBooksContainer() {
        if (booksContainer != null) {
            booksContainer.setHgap(20); // Match the horizontal spacing from reference
            booksContainer.setVgap(20); // Match the vertical spacing from reference
            booksContainer.setPadding(new Insets(10, 10, 10, 10));
            booksContainer.setPrefWrapLength(950); // Adjust based on your UI width
            booksContainer.setAlignment(Pos.CENTER_LEFT); // Align items to the left
        } else {
            System.err.println("Books container is null! Check your FXML file.");
        }
    }

    private void loadCategoriesFromDatabase() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnector.getConnection();

            // First, add the "All" category
            categories.add("All");

            // Query to get all categories from the Book_Categories table
            String query = "SELECT DISTINCT Category_Name FROM Book_Categories ORDER BY Category_Name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("Category_Name");
                categories.add(category);
            }

            // Create buttons for all categories
            createCategoryButtons();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Close resources but not the connection
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
        }
    }

    private void createCategoryButtons() {
        if (categoryButtonsContainer == null) {
            System.err.println("Category buttons container is null!");
            return;
        }

        // Clear existing buttons
        categoryButtonsContainer.getChildren().clear();

        // Create a button for each category
        for (String category : categories) {
            Button categoryBtn = new Button(category.toUpperCase());
            categoryBtn.setPrefHeight(24.0);
            categoryBtn.setPrefWidth(163.0);

            // Set the default button style
            String buttonStyle = "-fx-background-color: #00ce63; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;";
            if (category.equals(currentCategory)) {
                buttonStyle = "-fx-background-color: #38793b; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;";
            }
            categoryBtn.setStyle(buttonStyle);

            // Set the font size
            categoryBtn.setFont(new Font(11.0));

            // Add event handler for button click
            categoryBtn.setOnAction(event -> {
                currentCategory = category;
                updateCategoryButtonStyles();
                loadBooksByCategory(category);
            });

            // Add button to the container
            categoryButtonsContainer.getChildren().add(categoryBtn);
        }
    }

    private void updateCategoryButtonStyles() {
        // Update styles of all category buttons
        for (Node node : categoryButtonsContainer.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String category = btn.getText().toLowerCase();

                if (category.equalsIgnoreCase(currentCategory)) {
                    btn.setStyle(
                            "-fx-background-color: #38793b; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;");
                } else {
                    btn.setStyle(
                            "-fx-background-color: #00ce63; -fx-text-fill: white; -fx-background-radius: 3px; -fx-font-weight: bold;");
                }
            }
        }
    }

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
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
                        "ORDER BY b.Times_Borrowed DESC";
                stmt = conn.prepareStatement(query);
            } else {
                query = "SELECT b.Book_ID, b.ISBN, b.Title, b.Author, b.Category, " +
                        "b.Available_Copies, b.Reserved_Copies, b.Times_Borrowed " +
                        "FROM Books b " +
                        "JOIN Book_Categories bc ON b.Category = bc.Category_Name " +
                        "WHERE bc.Category_Name = ? AND (b.Available_Copies > 0 OR b.Reserved_Copies > 0) " +
                        "ORDER BY b.Times_Borrowed DESC";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, category);
            }
    
            rs = stmt.executeQuery();
    
            // Clear existing book displays
            booksContainer.getChildren().clear();
    
            // Create book cards for each book
            while (rs.next()) {
                String bookId = rs.getString("Book_ID");
                String isbn = rs.getString("ISBN");
                String title = rs.getString("Title");
                String author = rs.getString("Author");
                String categoryName = rs.getString("Category");
                int availableCopies = rs.getInt("Available_Copies");
                int reservedCopies = rs.getInt("Reserved_Copies");
                int timesBorrowed = rs.getInt("Times_Borrowed");
    
                // Create and add a book card to the container
                VBox bookCard = createBookCard(bookId, isbn, title, author, categoryName, 
                                              availableCopies, reservedCopies, timesBorrowed);
                booksContainer.getChildren().add(bookCard);
            }
    
            // If no books were found, display a message
            if (booksContainer.getChildren().isEmpty()) {
                Text noBookMsg = new Text("No books available in this category.");
                noBookMsg.setFont(new Font(16));
                booksContainer.getChildren().add(noBookMsg);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load books: " + e.getMessage(), Alert.AlertType.ERROR);
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
        }
    }

    private VBox createBookCard(String bookId, String isbn, String title, String author,
            String category, int availableCopies, int reservedCopies, int timesBorrowed) {
        // Main container
        VBox bookCard = new VBox(5);
        bookCard.setPrefWidth(193.0);
        bookCard.setPrefHeight(111.0);
        bookCard.setPadding(new Insets(10));
        bookCard.setStyle("-fx-background-color: WHITE; -fx-border-color: #c5bebe; -fx-border-radius: 15; " +
                "-fx-background-radius: 15; -fx-border-width: 1.5;");

        // Book Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Segoe UI Semibold", 13.0));
        titleText.setWrappingWidth(170);

        // ISBN
        Text isbnText = new Text(isbn);
        isbnText.setFont(new Font(10.0));
        isbnText.setFill(javafx.scene.paint.Color.valueOf("#727070"));

        // Author
        Text authorText = new Text(author);
        authorText.setFont(new Font(10.0));
        authorText.setFill(javafx.scene.paint.Color.valueOf("#727070"));

        // Publisher/Edition (using category as the publisher for now)
        Text publisherText = new Text(category + " | 1st Edition");
        publisherText.setFont(new Font(10.0));
        publisherText.setFill(javafx.scene.paint.Color.valueOf("#727070"));

        // Borrowed times container
        HBox borrowedTimesBox = new HBox(5);
        borrowedTimesBox.setAlignment(Pos.CENTER_LEFT);

        // Create ImageView for borrowed times icon
        ImageView borrowedIcon = new ImageView();
        borrowedIcon.setFitHeight(14.0);
        borrowedIcon.setFitWidth(14.0);
        // Set a placeholder or load the actual image
        // borrowedIcon.setImage(new
        // Image(getClass().getResource("../images/user_home/borrowed-times.png").toExternalForm()));

        // Borrowed times text
        Text borrowedTimesText = new Text("Borrowed " + timesBorrowed + " Times");
        borrowedTimesText.setFont(new Font(9.0));
        borrowedTimesText.setFill(javafx.scene.paint.Color.valueOf("#242424"));

        borrowedTimesBox.getChildren().addAll(borrowedIcon, borrowedTimesText);

        // Action Button
        Button actionButton = new Button();
        actionButton.setPrefHeight(17.0);
        actionButton.setPrefWidth(174.0);
        actionButton.setFont(new Font(8.0));
        actionButton.setTextFill(javafx.scene.paint.Color.WHITE);

        if (availableCopies > 0) {
            actionButton.setText("BORROW / RESERVE");
            actionButton.setStyle(
                    "-fx-background-color: #00ce63; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        } else if (reservedCopies > 0) {
            actionButton.setText("RESERVED");
            actionButton.setStyle(
                    "-fx-background-color: #727070; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        } else {
            actionButton.setText("BORROWED");
            actionButton.setStyle(
                    "-fx-background-color: #ff0000; -fx-text-fill: white; -fx-background-radius: 3.5; -fx-font-weight: bold;");
        }

        // Set button action
        actionButton.setOnAction(event -> handleBookAction(bookId, title, availableCopies, reservedCopies));

        // Add a small spacing between elements
        bookCard.setSpacing(2);

        // Add all elements to the card
        bookCard.getChildren().addAll(titleText, isbnText, authorText, publisherText, borrowedTimesBox, actionButton);

        return bookCard;
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
                loadBooksByCategory(currentCategory); // Refresh the view with current category
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

                // INSERT INTO RESERVATION APPROVALS
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
                loadBooksByCategory(currentCategory); // Refresh with current category
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}