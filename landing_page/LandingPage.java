package landing_page;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LandingPage {
    
    @FXML
    private Button studentButton;
    
    @FXML
    private Button teacherButton;
    
    @FXML
    private Button librarianButton;
    
    @FXML
    private void initialize() {
        // Initialization code if needed
    }
    
    @FXML
    private void handleStudentButtonClick(ActionEvent event) {
        try {
            // Navigate to the student help page
            Parent helpPage = FXMLLoader.load(getClass().getResource("/student_sign_up_page/signup_page.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(helpPage, 900, 525);
            stage.setScene(scene);
            stage.setTitle("OLFU Digital Library - Help & Support");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load student help page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleTeacherButtonClick(ActionEvent event) {
        // Navigate to teacher page when implemented
        System.out.println("Teacher button clicked - functionality to be implemented");
    }
    
    @FXML
    private void handleLibrarianButtonClick(ActionEvent event) {
        try {
            // Navigate to the student help page
            Parent helpPage = FXMLLoader.load(getClass().getResource("/librarian_sign_up_page/signup_page.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(helpPage, 900, 525);
            stage.setScene(scene);
            stage.setTitle("OLFU Digital Library - Help & Support");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load student help page: " + e.getMessage());
        }
    }
}