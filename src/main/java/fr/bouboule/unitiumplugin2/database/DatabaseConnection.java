package fr.bouboule.unitiumplugin2.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private Connection connection;
    private final String connectionString;

    public DatabaseConnection(String databaseName) {
        this.connectionString = "jdbc:sqlite:" + databaseName;
    }

    public boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(connectionString);
            System.out.println("Database connected successfully!");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database disconnected successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Failed to disconnect from the database: " + e.getMessage());
        }
    }

    // Ajoute d'autres méthodes pour exécuter des requêtes, des mises à jour, etc.

    public Connection getConnection() {
        return connection;
    }
}