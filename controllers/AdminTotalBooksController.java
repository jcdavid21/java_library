package controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import util.DatabaseConnector;

public class AdminTotalBooksController implements Initializable {

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, Integer> colBookId;

    @FXML
    private TableColumn<Book, String> colISBN;

    @FXML
    private TableColumn<Book, String> colTitle;

    @FXML
    private TableColumn<Book, String> colAuthor;

    @FXML
    private TableColumn<Book, String> colCategory;

    @FXML
    private TableColumn<Book, Integer> colTotalCopies;

    @FXML
    private TableColumn<Book, Integer> colAvailableCopies;

    @FXML
    private TableColumn<Book, Integer> colReservedCopies;

    @FXML
    private TableColumn<Book, Integer> colTimesBorrowed;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Button dashboardButton;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    private ObservableList<Book> filteredList = FXCollections.observableArrayList();
    private int adminId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection conn = DatabaseConnector.getConnection();
            if (conn.isValid(5)) { // Check if connection is still valid
                initializeTableColumns();
                loadBooks(conn);
                loadCategories(conn);
                setupSearchFilter();
                setupCategoryFilter();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not valid");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTableColumns() {
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colISBN.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTotalCopies.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        colAvailableCopies.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        colReservedCopies.setCellValueFactory(new PropertyValueFactory<>("reservedCopies"));
        colTimesBorrowed.setCellValueFactory(new PropertyValueFactory<>("timesBorrowed"));
    }

    private void loadBooks(Connection conn) {
        String query = "SELECT * FROM Books";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            booksList.clear();

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("Book_ID"),
                        rs.getString("ISBN"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Category"),
                        rs.getInt("Total_Copies"),
                        rs.getInt("Available_Copies"),
                        rs.getInt("Reserved_Copies"),
                        rs.getInt("Lost_Damaged_Copies"),
                        rs.getDate("Added_Date") != null ? rs.getDate("Added_Date").toLocalDate() : null,
                        rs.getDate("Last_Borrowed_Date") != null ? rs.getDate("Last_Borrowed_Date").toLocalDate()
                                : null,
                        rs.getInt("Times_Borrowed"));
                booksList.add(book);
            }

            filteredList.setAll(booksList);
            booksTable.setItems(filteredList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading books: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs); // Don't close connection
        }
    }

    private void loadCategories(Connection conn) {
        String query = "SELECT DISTINCT Category FROM Books ORDER BY Category";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("All Categories");

        try {
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("Category");
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                }
            }

            categoryFilter.setItems(categories);
            categoryFilter.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading categories: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(null, stmt, rs); // Don't close connection
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks();
        });
    }

    private void setupCategoryFilter() {
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterBooks();
        });
    }

    private void filterBooks() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryFilter.getValue();

        filteredList.clear();

        for (Book book : booksList) {
            boolean matchesCategory = selectedCategory.equals("All Categories") ||
                    (book.getCategory() != null && book.getCategory().equals(selectedCategory));

            boolean matchesSearch = searchText.isEmpty() ||
                    (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchText)) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(searchText)) ||
                    (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(searchText));

            if (matchesCategory && matchesSearch) {
                filteredList.add(book);
            }
        }

        booksTable.setItems(filteredList);
    }

    @FXML
    private void refreshTable() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            loadBooks(conn);
            searchField.clear();
            categoryFilter.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void printReport() {
        // Implement printing functionality
        showAlert(Alert.AlertType.INFORMATION, "Print Report", "Printing book inventory report...");
        // Future implementation: Generate PDF report
    }

    @FXML
    private void exportToExcel() {
        // Implement export functionality
        showAlert(Alert.AlertType.INFORMATION, "Export Data", "Exporting book data to Excel...");
        // Future implementation: Export to Excel file
    }

    // Navigation methods
    @FXML
    private void navigateToDashboard() {
        navigateToPage("admin_dashboard.fxml", "Dashboard");
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
    private void navigateToVisitLogs(ActionEvent event) {
        navigateToPage("../library_visits/library_visits_page.fxml", "Library Visit Logs");
    }

    @FXML void showVisitLogsTable(ActionEvent event) {
        // Implementation would show visit logs table
        navigateToPage("../library_visits/visits_table.fxml", "Library Visit Logs Table");
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
    private void navigateToAddBook() {
        navigateToPage("admin_add_new_books.fxml", "Add New Books");
    }

    @FXML
    private void navigateToUpdateBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/admin_page/admin_update_book_details.fxml"));
                Parent root = loader.load();

                AdminUpdateBookDetailsController controller = loader.getController();
                if (controller == null) {
                    showAlert(Alert.AlertType.ERROR, "Controller Error",
                            "Failed to get controller for update book page.");
                    return;
                }

                controller.setAdminId(adminId);
                controller.setBookData(selectedBook);

                Stage currentStage = (Stage) booksTable.getScene().getWindow();
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.setTitle("Library Management System - Update Book Details");
                currentStage.show();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error",
                        "Error navigating to update book: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a book to update.");
        }
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Book model class
    public static class Book {
        private final int bookId;
        private final String isbn;
        private final String title;
        private final String author;
        private final String category;
        private final int totalCopies;
        private final int availableCopies;
        private final int reservedCopies;
        private final int lostDamagedCopies;
        private final LocalDate addedDate;
        private final LocalDate lastBorrowedDate;
        private final int timesBorrowed;

        public Book(int bookId, String isbn, String title, String author, String category,
                int totalCopies, int availableCopies, int reservedCopies, int lostDamagedCopies,
                LocalDate addedDate, LocalDate lastBorrowedDate, int timesBorrowed) {
            this.bookId = bookId;
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.category = category;
            this.totalCopies = totalCopies;
            this.availableCopies = availableCopies;
            this.reservedCopies = reservedCopies;
            this.lostDamagedCopies = lostDamagedCopies;
            this.addedDate = addedDate;
            this.lastBorrowedDate = lastBorrowedDate;
            this.timesBorrowed = timesBorrowed;
        }

        // Getters
        public int getBookId() {
            return bookId;
        }

        public String getIsbn() {
            return isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getCategory() {
            return category;
        }

        public int getTotalCopies() {
            return totalCopies;
        }

        public int getAvailableCopies() {
            return availableCopies;
        }

        public int getReservedCopies() {
            return reservedCopies;
        }

        public int getLostDamagedCopies() {
            return lostDamagedCopies;
        }

        public LocalDate getAddedDate() {
            return addedDate;
        }

        public LocalDate getLastBorrowedDate() {
            return lastBorrowedDate;
        }

        public int getTimesBorrowed() {
            return timesBorrowed;
        }
    }
}