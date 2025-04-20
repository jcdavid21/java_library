package about_our_university;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutUniversityController {

    @FXML private Button dashboardButton;
    @FXML private ImageView userProfileIcon;
    private String currentStudentId;

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../student_teacher_account/student_teacher_home_page.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the student ID using reflection
            Object controller = loader.getController();
            setStudentIdUsingReflection(controller);
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void navigateToProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../user_account/account_dashboard.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the student ID using reflection
            Object controller = loader.getController();
            setStudentIdUsingReflection(controller);
            
            Stage stage = (Stage) userProfileIcon.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to Profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setStudentIdUsingReflection(Object controller) {
        if (controller != null) {
            try {
                java.lang.reflect.Method setIdMethod = controller.getClass().getMethod("setCurrentStudentId", String.class);
                setIdMethod.invoke(controller, currentStudentId);
            } catch (Exception e) {
                System.err.println("Could not set student ID: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}