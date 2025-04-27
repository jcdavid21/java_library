package library_visits;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.DatabaseConnector;
import models.VisitLog;

public class LibraryVisitLogsController implements Initializable {

    @FXML
    private TableView<VisitLog> visitLogsTable;
    
    @FXML
    private TableColumn<VisitLog, Integer> visitIdColumn;
    
    @FXML
    private TableColumn<VisitLog, String> visitorTypeColumn;
    
    @FXML
    private TableColumn<VisitLog, String> idColumn;
    
    @FXML
    private TableColumn<VisitLog, String> nameColumn;
    
    @FXML
    private TableColumn<VisitLog, LocalDateTime> visitDateColumn;
    
    @FXML
    private TableColumn<VisitLog, String> purposeColumn;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private ComboBox<String> visitorTypeFilter;
    
    @FXML
    private TextField idFilter;
    
    @FXML
    private TextField purposeFilter;
    
    @FXML
    private Button filterButton;
    
    @FXML
    private Button resetButton;
    
    private ObservableList<VisitLog> visitLogsList = FXCollections.observableArrayList();
    
    // Added adminId field to store the admin's ID
    private int adminId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize visitor type filter
        visitorTypeFilter.setItems(FXCollections.observableArrayList("All", "Student", "Employee"));
        visitorTypeFilter.setValue("All");
        
        // Initialize table columns 
        visitIdColumn.setCellValueFactory(new PropertyValueFactory<>("visitId"));
        visitorTypeColumn.setCellValueFactory(new PropertyValueFactory<>("visitorType"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("visitorId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        visitDateColumn.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        
        // Set date format for visit date column
        visitDateColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<VisitLog, LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
        
        // Load all visit logs initially
        loadVisitLogs();
    }
    
    /**
     * Sets the admin ID for this controller
     * @param adminId The ID of the admin user
     */
    public void setAdminId(int adminId) {
        this.adminId = adminId;
        System.out.println("Admin ID set in VisitLogs: " + adminId);
    }
    
    /**
     * Alternative method name for setting admin data
     * @param adminId The ID of the admin user
     */
    public void setAdminData(int adminId) {
        setAdminId(adminId);
    }
    
    /**
     * Navigate back to the dashboard
     */
    @FXML
    private void navigateToDashboard(ActionEvent event) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) filterButton.getScene().getWindow();
            
            // Load the dashboard view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_page/admin_dashboard.fxml"));
            Parent root = loader.load();
            
            // Pass the admin ID to the dashboard controller
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    // Try to call setAdminId method
                    controller.getClass().getMethod("setAdminId", int.class).invoke(controller, adminId);
                } catch (NoSuchMethodException e) {
                    // Try alternate method name
                    try {
                        controller.getClass().getMethod("setAdminData", int.class).invoke(controller, adminId);
                    } catch (NoSuchMethodException ex) {
                        System.out.println("Dashboard controller doesn't have setAdminData or setAdminId method");
                    }
                }
            }
            
            // Create the scene and set it on the stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Library Management System - Admin Dashboard");
            currentStage.show();
            
        } catch (Exception e) {
            System.err.println("Error navigating to dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to dashboard", e.getMessage());
        }
    }
    
    /**
     * Loads visit logs from the database based on the current filters
     */
    private void loadVisitLogs() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnector.getConnection();
            
            // Build query with possible filters
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM Visit_Logs WHERE 1=1");
            
            // Add date range filter if dates are selected
            if (fromDatePicker.getValue() != null) {
                queryBuilder.append(" AND Visit_Date >= ?");
            }
            
            if (toDatePicker.getValue() != null) {
                queryBuilder.append(" AND Visit_Date <= ?");
            }
            
            // Add visitor type filter if selected
            if (visitorTypeFilter.getValue() != null && !visitorTypeFilter.getValue().equals("All")) {
                queryBuilder.append(" AND Visitor_Type = ?");
            }
            
            // Add ID filter if provided
            if (idFilter.getText() != null && !idFilter.getText().isEmpty()) {
                queryBuilder.append(" AND (Student_ID LIKE ? OR Employee_ID LIKE ?)");
            }
            
            // Add purpose filter if provided
            if (purposeFilter.getText() != null && !purposeFilter.getText().isEmpty()) {
                queryBuilder.append(" AND Purpose LIKE ?");
            }
            
            queryBuilder.append(" ORDER BY Visit_Date DESC");
            
            stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Set parameters for the query
            int paramIndex = 1;
            
            if (fromDatePicker.getValue() != null) {
                stmt.setString(paramIndex++, fromDatePicker.getValue().toString() + " 00:00:00");
            }
            
            if (toDatePicker.getValue() != null) {
                stmt.setString(paramIndex++, toDatePicker.getValue().toString() + " 23:59:59");
            }
            
            if (visitorTypeFilter.getValue() != null && !visitorTypeFilter.getValue().equals("All")) {
                stmt.setString(paramIndex++, visitorTypeFilter.getValue());
            }
            
            if (idFilter.getText() != null && !idFilter.getText().isEmpty()) {
                String idSearch = "%" + idFilter.getText() + "%";
                stmt.setString(paramIndex++, idSearch);
                stmt.setString(paramIndex++, idSearch);
            }
            
            if (purposeFilter.getText() != null && !purposeFilter.getText().isEmpty()) {
                stmt.setString(paramIndex++, "%" + purposeFilter.getText() + "%");
            }
            
            rs = stmt.executeQuery();
            
            // Clear existing list
            visitLogsList.clear();
            
            // Process results
            while (rs.next()) {
                int visitId = rs.getInt("Visit_ID");
                String visitorType = rs.getString("Visitor_Type");
                String visitorId = visitorType.equals("Student") ? rs.getString("Student_ID") : rs.getString("Employee_ID");
                String visitorName = visitorType.equals("Student") ? rs.getString("Student_Name") : rs.getString("Employee_Name");
                LocalDateTime visitDate = rs.getTimestamp("Visit_Date").toLocalDateTime();
                String purpose = rs.getString("Purpose");
                
                VisitLog visitLog = new VisitLog(visitId, visitorType, visitorId, visitorName, visitDate, purpose);
                visitLogsList.add(visitLog);
            }
            
            // Set items to table
            visitLogsTable.setItems(visitLogsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load visit logs", e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnector.closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Apply filters to the visit logs table
     */
    @FXML
    private void applyFilters() {
        loadVisitLogs();
    }
    
    /**
     * Reset all filters to default values
     */
    @FXML
    private void resetFilters() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        visitorTypeFilter.setValue("All");
        idFilter.clear();
        purposeFilter.clear();
        
        loadVisitLogs();
    }
    
    /**
     * Export the current filtered data to a CSV file
     */
    @FXML
    private void exportToCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Visit Logs Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("visit_logs_report_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv");
        
        // Show save file dialog
        File file = fileChooser.showSaveDialog(visitLogsTable.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                writer.println("Visit ID,Visitor Type,ID,Name,Visit Date,Purpose");
                
                // Write data
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (VisitLog log : visitLogsList) {
                    writer.println(
                        log.getVisitId() + "," +
                        log.getVisitorType() + "," +
                        log.getVisitorId() + "," +
                        "\"" + log.getVisitorName() + "\"," +
                        log.getVisitDate().format(formatter) + "," +
                        "\"" + log.getPurpose() + "\""
                    );
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                        "Visit logs exported successfully", 
                        "File saved to: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Error", 
                        "Failed to export visit logs", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Generate a formatted report from the current filtered data
     */
    @FXML
    private void generateReport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Visit Logs Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("visit_logs_detailed_report_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".txt");
        
        // Show save file dialog
        File file = fileChooser.showSaveDialog(visitLogsTable.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write report header
                writer.println("======================================================");
                writer.println("              LIBRARY VISIT LOGS REPORT               ");
                writer.println("======================================================");
                writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("Total Entries: " + visitLogsList.size());
                writer.println();
                
                // Write filter information
                writer.println("Filter Parameters:");
                writer.println("  Date Range: " + 
                    (fromDatePicker.getValue() != null ? fromDatePicker.getValue() : "Not specified") + " to " + 
                    (toDatePicker.getValue() != null ? toDatePicker.getValue() : "Not specified"));
                writer.println("  Visitor Type: " + visitorTypeFilter.getValue());
                writer.println("  ID Filter: " + (idFilter.getText().isEmpty() ? "None" : idFilter.getText()));
                writer.println("  Purpose Filter: " + (purposeFilter.getText().isEmpty() ? "None" : purposeFilter.getText()));
                writer.println();
                
                // Write detailed visit logs
                writer.println("VISIT DETAILS:");
                writer.println("======================================================");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (VisitLog log : visitLogsList) {
                    writer.println("Visit ID: " + log.getVisitId());
                    writer.println("Visitor Type: " + log.getVisitorType());
                    writer.println("ID: " + log.getVisitorId());
                    writer.println("Name: " + log.getVisitorName());
                    writer.println("Visit Date: " + log.getVisitDate().format(formatter));
                    writer.println("Purpose: " + log.getPurpose());
                    writer.println("------------------------------------------------------");
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Report Generated", 
                        "Visit logs report generated successfully", 
                        "File saved to: " + file.getAbsolutePath());
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Report Generation Error", 
                        "Failed to generate visit logs report", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Open a form to add a new visit log entry
     */
    @FXML
    private void addNewVisit(ActionEvent event) {
        try {
            // Load the add visit form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/library_visits/add_visit.fxml"));
            Parent root = loader.load();
            
            // Get controller and pass admin ID if possible
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setAdminId", int.class).invoke(controller, adminId);
                } catch (NoSuchMethodException e) {
                    try {
                        controller.getClass().getMethod("setAdminData", int.class).invoke(controller, adminId);
                    } catch (NoSuchMethodException ex) {
                        System.out.println("AddVisit controller doesn't have setAdminData or setAdminId method");
                    }
                }
            }
            
            // Create a new stage for the add visit form
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Add New Library Visit");
            stage.setScene(new Scene(root));
            
            // Show the form and wait until it's closed
            stage.showAndWait();
            
            // Reload visit logs after form is closed
            loadVisitLogs();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Form Error", 
                    "Failed to open Add Visit form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an alert dialog with the specified parameters
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}