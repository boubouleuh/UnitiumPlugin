package fr.bouboule.unitiumplugin2.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableCreator {
    private final DatabaseConnection database;

    public TableCreator(DatabaseConnection database) {
        this.database = database;
    }

    public void createTables() {
        database.connect();

        createCountryTable();
        createPlayerTable();
        createCountryPlayersTable();
        createRankTable();
        createPermissionTable();
        createCountryRanksTable();
        createCountryPermissionsTable();
        createRelationsTable();
        createWarsTable();

        database.disconnect();
    }

    private void createCountryTable() {
        String query = "CREATE TABLE IF NOT EXISTS Countries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "leader_uuid VARCHAR(36) NOT NULL," +
                "name VARCHAR(255) NOT NULL UNIQUE," +
                "description VARCHAR(255) NOT NULL," +
                "bank DOUBLE NOT NULL" +
                ")";

        executeQuery(query);
    }

    private void createPlayerTable() {
        String query = "CREATE TABLE IF NOT EXISTS Players (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "balance DOUBLE NOT NULL," +
                "rank_id INT UNSIGNED NOT NULL" +
                ")";

        executeQuery(query);
    }

    private void createCountryPlayersTable() {
        String query = "CREATE TABLE IF NOT EXISTS CountryPlayers (" +
                "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY ," +
                "country_id INT UNSIGNED NOT NULL," +
                "rank_id INT UNSIGNED NOT NULL," +
                "FOREIGN KEY (country_id) REFERENCES Countries(id) ON DELETE CASCADE," +
                "FOREIGN KEY (player_uuid) REFERENCES Players(uuid) ON DELETE CASCADE," +
                "FOREIGN KEY (rank_id) REFERENCES Ranks(id) ON DELETE CASCADE" +
                ")";

        executeQuery(query);
    }

    private void createRankTable() {
        String query = "CREATE TABLE IF NOT EXISTS Ranks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(255) NOT NULL UNIQUE," +
                ")";

        executeQuery(query);
    }

    private void createPermissionTable() {
        String query = "CREATE TABLE IF NOT EXISTS Permissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(255) NOT NULL UNIQUE" +
                ")";

        executeQuery(query);
    }


    private void createCountryRanksTable() {
        String query = "CREATE TABLE IF NOT EXISTS CountryRanks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "country_id INT UNSIGNED NOT NULL," +
                "rank_name VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (country_id) REFERENCES Countries(id) ON DELETE CASCADE" +
                ")";

        executeQuery(query);
    }

    private void createCountryPermissionsTable() {
        String query = "CREATE TABLE IF NOT EXISTS CountryPermissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "country_rank_id INT UNSIGNED NOT NULL," +
                "permission_name VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (country_rank_id) REFERENCES CountryRanks(id) ON DELETE CASCADE" +
                ")";

        executeQuery(query);
    }

    private void createRelationsTable() {
        String query = "CREATE TABLE IF NOT EXISTS Relations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "country_id1 INT UNSIGNED NOT NULL," +
                "country_id2 INT UNSIGNED NOT NULL," +
                "status VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (country_id1) REFERENCES Countries(id) ON DELETE CASCADE," +
                "FOREIGN KEY (country_id2) REFERENCES Countries(id) ON DELETE CASCADE" +
                ")";

        executeQuery(query);
    }

    private void createWarsTable() {
        String query = "CREATE TABLE IF NOT EXISTS Wars (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "country_id1 INT UNSIGNED NOT NULL," +
                "country_id2 INT UNSIGNED NOT NULL," +
                "status VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (country_id1) REFERENCES Countries(id) ON DELETE CASCADE," +
                "FOREIGN KEY (country_id2) REFERENCES Countries(id) ON DELETE CASCADE" +
                ")";

        executeQuery(query);
    }

    private void executeQuery(String query) {
        Connection connection = database.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Failed to execute query: " + e.getMessage());
        }
    }
}