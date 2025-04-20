package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DATABASE_URL = "jdbc:mysql://localhost/library_db";
    private static final String USERNAME = "root";  // Using default MySQL username since password is empty
    private static final String PASSWORD = "";
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Create connection
                connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
                System.out.println("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes database resources (Connection, PreparedStatement, ResultSet)
     * @param conn The Connection to close
     * @param stmt The PreparedStatement to close
     * @param rs The ResultSet to close
     */
    public static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        // Close ResultSet
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
            }
        }
        
        // Close PreparedStatement
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing PreparedStatement: " + e.getMessage());
            }
        }
        
        // Note: We don't close the connection here since it's a singleton
        // If you need to close it, call closeConnection() explicitly
    }
}