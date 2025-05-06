package student_help;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class StudentHelpController {
    
    @FXML
    private Button dashboardButton;
    
    private String currentStudentId;
    
    // Setter method for the student ID
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }
    
    @FXML
    public void handleDashboardButton(ActionEvent event) {
        try {
            // Use FXMLLoader to get the controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student_teacher_account/student_teacher_home_page.fxml"));
            Parent landingPage = loader.load();
            
            // Get the controller and pass back the student ID
            Object controller = loader.getController();
            
            // Use reflection to set the student ID on the dashboard controller
            if (controller != null) {
                try {
                    java.lang.reflect.Method setIdMethod = controller.getClass().getMethod("setCurrentStudentId", String.class);
                    setIdMethod.invoke(controller, currentStudentId);
                } catch (Exception e) {
                    System.err.println("Could not set student ID: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            
            // Create new scene with landing page
            Scene scene = new Scene(landingPage);
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("OLFU Digital Library - Dashboard");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load landing page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method for showing alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}