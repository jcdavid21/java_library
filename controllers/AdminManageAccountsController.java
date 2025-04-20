package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import util.DatabaseConnector;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminManageAccountsController implements Initializable {

    @FXML private TableView<AdminAccount> accountsTable;
    @FXML private TableColumn<AdminAccount, Integer> idColumn;
    @FXML private TableColumn<AdminAccount, String> usernameColumn;
    @FXML private TableColumn<AdminAccount, String> firstNameColumn;
    @FXML private TableColumn<AdminAccount, String> lastNameColumn;
    @FXML private TableColumn<AdminAccount, String> emailColumn;
    @FXML private TableColumn<AdminAccount, String> roleColumn;
    @FXML private TableColumn<AdminAccount, String> statusColumn;
    @FXML private TableColumn<AdminAccount, Void> actionsColumn;

    @FXML private TextField adminIdField;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button clearButton;

    private ObservableList<AdminAccount> accountsList = FXCollections.observableArrayList();
    private int adminId; // Current logged-in admin ID
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize role and status combo boxes
        roleComboBox.setItems(FXCollections.observableArrayList("Librarian", "Assistant"));
        statusComboBox.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("adminId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add action buttons column
        setupActionsColumn();
        
        // Load admin accounts
        loadAdminAccounts();
        
        // Add listener for table selection
        accountsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayAdminDetails(newSelection);
                updateButton.setDisable(false);
            } else {
                clearFields();
                updateButton.setDisable(true);
            }
        });
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<AdminAccount, Void>, TableCell<AdminAccount, Void>> cellFactory = 
            new Callback<TableColumn<AdminAccount, Void>, TableCell<AdminAccount, Void>>() {
                @Override
                public TableCell<AdminAccount, Void> call(final TableColumn<AdminAccount, Void> param) {
                    return new TableCell<AdminAccount, Void>() {
                        private final Button editButton = new Button("Edit");
                        private final Button deleteButton = new Button("Delete");
                        
                        {
                            editButton.setOnAction((ActionEvent event) -> {
                                AdminAccount admin = getTableView().getItems().get(getIndex());
                                displayAdminDetails(admin);
                            });
                            
                            deleteButton.setOnAction((ActionEvent event) -> {
                                AdminAccount admin = getTableView().getItems().get(getIndex());
                                confirmAndDeleteAdmin(admin);
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                // Create a pane to hold buttons side by side
                                javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5);
                                pane.getChildren().addAll(editButton, deleteButton);
                                setGraphic(pane);
                            }
                        }
                    };
                }
            };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadAdminAccounts() {
        accountsList.clear();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM Admin_Accounts ORDER BY Last_Name, First_Name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                AdminAccount admin = new AdminAccount(
                    rs.getInt("Admin_ID"),
                    rs.getString("Username"),
                    rs.getString("First_Name"),
                    rs.getString("Last_Name"),
                    rs.getString("Email"),
                    rs.getString("Password"),
                    rs.getString("Role"),
                    rs.getString("Status")
                );
                accountsList.add(admin);
            }
            
            accountsTable.setItems(accountsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading admin accounts: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    private void displayAdminDetails(AdminAccount admin) {
        adminIdField.setText(String.valueOf(admin.getAdminId()));
        usernameField.setText(admin.getUsername());
        firstNameField.setText(admin.getFirstName());
        lastNameField.setText(admin.getLastName());
        emailField.setText(admin.getEmail());
        passwordField.setText(admin.getPassword());
        roleComboBox.setValue(admin.getRole());
        statusComboBox.setValue(admin.getStatus());
        
        // Change button text to reflect we're in edit mode
        addButton.setText("Save Changes");
    }
    
    @FXML
    private void handleAddOrUpdateAdmin() {
        if (!validateFields()) {
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            if (adminIdField.getText().isEmpty()) {
                // Add new admin account
                //generate a new random ID
                int newAdminId = (int) (Math.random() * 1000000);
                String query = "INSERT INTO Admin_Accounts (Admin_ID, Username, Password, Last_Name, First_Name, Email, Role, Status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, newAdminId);
                stmt.setString(2, usernameField.getText());
                stmt.setString(3, passwordField.getText());
                stmt.setString(4, lastNameField.getText());
                stmt.setString(5, firstNameField.getText());
                stmt.setString(6, emailField.getText());
                stmt.setString(7, roleComboBox.getValue());
                stmt.setString(8, statusComboBox.getValue());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Admin account created successfully!");
                    logAction("Created new admin account: " + usernameField.getText());
                }
            } else {
                // Update existing admin account
                String query = "UPDATE Admin_Accounts SET Username = ?, Password = ?, Last_Name = ?, " +
                               "First_Name = ?, Email = ?, Role = ?, Status = ? WHERE Admin_ID = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, usernameField.getText());
                stmt.setString(2, passwordField.getText());
                stmt.setString(3, lastNameField.getText());
                stmt.setString(4, firstNameField.getText());
                stmt.setString(5, emailField.getText());
                stmt.setString(6, roleComboBox.getValue());
                stmt.setString(7, statusComboBox.getValue());
                stmt.setInt(8, Integer.parseInt(adminIdField.getText()));
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Admin account updated successfully!");
                    logAction("Updated admin account: " + usernameField.getText());
                }
            }
            
            // Reload the table and clear fields
            loadAdminAccounts();
            clearFields();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving admin account: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    private void confirmAndDeleteAdmin(AdminAccount admin) {
        // Don't allow admins to delete their own account
        if (admin.getAdminId() == this.adminId) {
            showAlert(Alert.AlertType.WARNING, "Action Denied", "You cannot delete your own account.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Admin Account");
        alert.setContentText("Are you sure you want to delete the admin account for " +
                admin.getFirstName() + " " + admin.getLastName() + "?");
                
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteAdmin(admin);
        }
    }
    
    private void deleteAdmin(AdminAccount admin) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "DELETE FROM Admin_Accounts WHERE Admin_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, admin.getAdminId());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Admin account deleted successfully!");
                logAction("Deleted admin account: " + admin.getUsername());
                loadAdminAccounts();
                clearFields();
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting admin account: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    @FXML
    private void handleClearFields() {
        clearFields();
    }
    
    private void clearFields() {
        adminIdField.clear();
        usernameField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        
        // Reset button text
        addButton.setText("Add Admin");
        
        // Deselect table rows
        accountsTable.getSelectionModel().clearSelection();
    }
    
    private boolean validateFields() {
        StringBuilder errorMsg = new StringBuilder();
        
        if (usernameField.getText().isEmpty()) errorMsg.append("Username is required.\n");
        if (passwordField.getText().isEmpty()) errorMsg.append("Password is required.\n");
        if (firstNameField.getText().isEmpty()) errorMsg.append("First name is required.\n");
        if (lastNameField.getText().isEmpty()) errorMsg.append("Last name is required.\n");
        if (emailField.getText().isEmpty()) errorMsg.append("Email is required.\n");
        if (roleComboBox.getValue() == null) errorMsg.append("Role must be selected.\n");
        if (statusComboBox.getValue() == null) errorMsg.append("Status must be selected.\n");
        
        // Add email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!emailField.getText().isEmpty() && !emailField.getText().matches(emailRegex)) {
            errorMsg.append("Email format is invalid.\n");
        }
        
        if (errorMsg.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMsg.toString());
            return false;
        }
        
        return true;
    }
    
    private void logAction(String action) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO System_Logs (Timestamp, User_Type, User_ID, Action_Performed) VALUES (NOW(), ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "Admin");
            stmt.setString(2, String.valueOf(adminId));
            stmt.setString(3, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        } finally {
            DatabaseConnector.closeResources(conn, stmt, null);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Model class for Admin Account
    public static class AdminAccount {
        private final int adminId;
        private final String username;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String password;
        private final String role;
        private final String status;
        
        public AdminAccount(int adminId, String username, String firstName, String lastName, 
                           String email, String password, String role, String status) {
            this.adminId = adminId;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.role = role;
            this.status = status;
        }
        
        public int getAdminId() { return adminId; }
        public String getUsername() { return username; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
    }
    
    // Navigation Methods for Sidebar
    @FXML
    private void navigateToDashboard() {
        navigateToPage("admin_dashboard.fxml", "Dashboard");
    }

    @FXML
    private void navigateToTotalBooks() {
        navigateToPage("admin_total_books_available.fxml", "Total Books Available");
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
    private void navigateToPenaltiesAndFines() {
        navigateToPage("admin_penalties_fines.fxml", "Penalties and Fines");
    }
    
    @FXML
    private void navigateToLibraryVisitLogs() {
        navigateToPage("admin_library_logs.fxml", "Library Visit Logs");
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
    private void logout() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) accountsTable.getScene().getWindow();
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Parent root = loader.load();
            
            // Create and set new scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - Login");
            currentStage.show();
            
            // Log the logout action
            logAction("Admin logged out");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) accountsTable.getScene().getWindow();
            
            // Load the selected view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/" + fxmlFile));
            Parent root = loader.load();
            
            // Pass admin ID to the new controller
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setAdminData", int.class).invoke(controller, adminId);
                } catch (NoSuchMethodException e) {
                    try {
                        controller.getClass().getMethod("setAdminId", int.class).invoke(controller, adminId);
                    } catch (NoSuchMethodException ex) {
                        System.out.println("Controller doesn't have setAdminData or setAdminId method");
                    }
                }
            }
            
            // Create and set new scene
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - " + title);
            currentStage.show();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Error navigating to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to receive admin ID from previous screen
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setAdminData(int adminId) {
        this.adminId = adminId;
    }
}