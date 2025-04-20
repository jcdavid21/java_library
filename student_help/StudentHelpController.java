package student_help;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StudentHelpController {
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    public void handleDashboardButton(ActionEvent event) {
        try {
            // Get the absolute path from the current class location
            Parent landingPage = FXMLLoader.load(getClass().getResource("/student_teacher_account/student_teacher_home_page.fxml"));
            
            // Get the current stage
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            
            // Create new scene with landing page
            Scene scene = new Scene(landingPage, 900, 525);
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("OLFU Digital Library - Dashboard");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load landing page: " + e.getMessage());
        }
    }
}