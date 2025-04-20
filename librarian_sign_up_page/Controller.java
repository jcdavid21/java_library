package librarian_sign_up_page;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.DatabaseConnector;
import controllers.AdminDashboardController;

public class Controller implements Initializable {

    @FXML
    private TextField studentNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Button learnMoreButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label descriptionLabel;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database connection
        connection = DatabaseConnector.getConnection();

        // Set up event handlers
        signInButton.setOnAction(this::handleSignIn);
        learnMoreButton.setOnAction(this::handleLearnMore);
    }

    /**
     * Handles sign in button click event
     * 
     * @param event The ActionEvent triggered by the button
     */
    private void handleSignIn(ActionEvent event) {
        String employeeNumber = studentNumberField.getText().trim();
        String password = passwordField.getText();

        // Validation - check if fields are empty
        if (employeeNumber.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Please enter both employee number and password.");
            return;
        }

        // Authenticate user and get Admin_ID
        String adminId = authenticateUser(employeeNumber, password);
        if (adminId != null) {
            navigateToAdminDashboard(adminId);
        } else {
            showAlert(AlertType.ERROR, "Login Failed", "Invalid employee number or password.");
            passwordField.clear();
        }
    }

    /**
     * Authenticates the user against the database
     * 
     * @param employeeNumber The employee number entered by the user
     * @param password       The password entered by the user
     * @return The Admin_ID if authentication is successful, null otherwise
     */
    private String authenticateUser(String employeeNumber, String password) {
        String query = "SELECT * FROM Admin_Accounts WHERE Admin_ID = ? AND Password = ?";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, employeeNumber);
            preparedStatement.setString(2, password);

            resultSet = preparedStatement.executeQuery();

            // If the resultSet has at least one row, authentication is successful
            if (resultSet.next()) {
                return resultSet.getString("Admin_ID");
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
            return null;
        } finally {
            DatabaseConnector.closeResources(null, preparedStatement, resultSet);
        }
    }

    /**
     * Navigates to the admin dashboard after successful login
     * 
     * @param adminId The ID of the logged-in admin
     */
    // In your login controller (Controller.java)
    private void navigateToAdminDashboard(String adminId) {
        try {
            // Load the admin dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/admin_dashboard.fxml"));
            Parent adminDashboard = loader.load();

            // Get the controller of the admin dashboard
            AdminDashboardController dashboardController = loader.getController();

            // Pass the Admin_ID to the dashboard controller - convert to int first
            dashboardController.setAdminData(Integer.parseInt(adminId));

            // Get the current stage from any component (e.g., the sign in button)
            Stage stage = (Stage) signInButton.getScene().getWindow();

            // Create a new scene with the admin dashboard
            Scene scene = new Scene(adminDashboard);

            // Set the new scene on the stage
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            showAlert(AlertType.ERROR, "Navigation Error", "Could not load the admin dashboard.");
        }
    }

    /**
     * Handles the learn more button click
     * 
     * @param event The ActionEvent triggered by the button
     */
    private void handleLearnMore(ActionEvent event) {
        // Implement functionality for the learn more button
        // For example, show an about dialog or navigate to information page
        showAlert(AlertType.INFORMATION, "About Library System",
                "Our Lady of Fatima University Library System provides an easy way to manage and borrow books online.\n\n"
                        +
                        "For more information, please contact the library administration.");
    }

    /**
     * Shows an alert dialog with the specified type, title, and message
     * 
     * @param type    The type of alert (INFORMATION, WARNING, ERROR, etc.)
     * @param title   The title of the alert dialog
     * @param message The message to display in the alert dialog
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}