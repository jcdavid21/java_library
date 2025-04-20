package student_sign_up_page;

import student_teacher_account.StudentDashboardController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.DatabaseConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginController {

    @FXML
    private TextField studentNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Button learnMoreButton;

    private Connection connection;

    public void initialize() {
        // Initialize database connection
        connection = DatabaseConnector.getConnection();
        
        // Set up button actions
        signInButton.setOnAction(this::handleLogin);
        learnMoreButton.setOnAction(this::handleLearnMore);
    }

    private void handleLogin(ActionEvent event) {
        String studentNumber = studentNumberField.getText().trim();
        String password = passwordField.getText().trim();

        if (studentNumber.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please enter both student number and password.");
            return;
        }

        // Attempt to authenticate the user
        try {
            if (authenticateUser(studentNumber, password)) {
                navigateToHomePage(studentNumber);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid student number or password.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Could not verify login credentials. Please try again later.");
            System.err.println("Authentication error: " + e.getMessage());
        }
    }

    private void navigateToHomePage(String studentNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student_teacher_account/student_teacher_home_page.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the student ID
            StudentDashboardController controller = loader.getController();
            controller.setCurrentStudentId(studentNumber);
            
            // Get the current stage
            Stage stage = (Stage) signInButton.getScene().getWindow();
            
            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading home page: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the home page.");
        }
    }

    private boolean authenticateUser(String studentNumber, String password) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "SELECT password FROM Students WHERE Student_ID = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, studentNumber);
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                return storedPassword.equals(password);
            }
            return false;
        }
    }
    
    private void handleLearnMore(ActionEvent event) {
        // Implement the learn more functionality here
        // For example, show an information dialog or navigate to an info page
        showAlert(Alert.AlertType.INFORMATION, "About Student Login",
                "This is the student login portal for accessing course materials, " +
                "submitting assignments, and viewing grades. " +
                "If you don't have an account, please contact your administrator.");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}