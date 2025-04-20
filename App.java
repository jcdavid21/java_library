import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the landing page FXML as the first screen
            Parent root = FXMLLoader.load(getClass().getResource("/landing_page/landing_page.fxml"));
            primaryStage.setTitle("OLFU Digital Library");
            primaryStage.setScene(new Scene(root, 900, 500));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load landing page: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}