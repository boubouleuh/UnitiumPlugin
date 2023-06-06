package fr.bouboule.unitiumplugin2.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final DatabaseConnection database;
    private final Connection connection;
    public DatabaseManager(DatabaseConnection database) {
        this.database = database;
        database.connect();
        this.connection = database.getConnection();
    }



    // COUNTRY METHODS

    public int getCountryID(String countryName) {
        String query = "SELECT id FROM Countries WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, countryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get country ID: " + e.getMessage());
        }
        return -1; // Retourne -1 si le pays n'est pas trouvé ou s'il y a une erreur
    }

    public boolean playerHasCountry(UUID playerUUID) {
        String query = "SELECT COUNT(*) AS count FROM CountryPlayers WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(playerUUID));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    if (count > 0) {
                        // Le joueur a au moins un pays en tant que membre
                        return true;
                    } else {
                        // Vérifier si le joueur est le leader d'un pays
                        query = "SELECT COUNT(*) AS leaderCount FROM Countries WHERE leader_uuid = ?";
                        try (PreparedStatement leaderStatement = connection.prepareStatement(query)) {
                            leaderStatement.setString(1, String.valueOf(playerUUID));
                            try (ResultSet leaderResultSet = leaderStatement.executeQuery()) {
                                if (leaderResultSet.next()) {
                                    int leaderCount = leaderResultSet.getInt("leaderCount");
                                    return leaderCount > 0; // Retourne true si le joueur est le leader d'un pays
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if player has country: " + e.getMessage());
        }
        return false; // Retourne false en cas d'erreur ou si le joueur n'a pas de pays
    }

    public boolean countryExists(String countryName) {
        String query = "SELECT COUNT(*) AS count FROM Countries WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, countryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if country exists: " + e.getMessage());
        }
        return false; // Retourne false en cas d'erreur ou si le pays n'existe pas
    }

    public void addPlayerToCountry(UUID playerUUID, int countryID, int rankId) {
        String query = "INSERT INTO CountryPlayers (player_uuid, country_id, rank_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(playerUUID));
            statement.setInt(2, countryID);
            statement.setInt(3, rankId);
            statement.executeUpdate();
            System.out.println("Player added to country successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add player to country: " + e.getMessage());
        }
    }

    public String getCountryName(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT c.name FROM Countries c LEFT JOIN CountryPlayers cp ON c.id = cp.country_id WHERE cp.player_uuid = ? OR c.leader_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void removePlayerFromCountry(UUID playerUUID, int countryID) {
        String query = "DELETE FROM CountryPlayers WHERE player_uuid = ? AND country_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(playerUUID));
            statement.setInt(2, countryID);
            statement.executeUpdate();
            System.out.println("Player removed from country successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to remove player from country: " + e.getMessage());
        }
    }

    public void createCountry(UUID leader_uuid, String name, String description, double bank) {
        String query = "INSERT INTO Countries (leader_uuid, name, description, bank) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(leader_uuid));
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setDouble(4, bank);
            statement.executeUpdate();

            int countryID = getCountryID(name);
            createDefaultCountryRanks(countryID);
            System.out.println("Country created successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to create country: " + e.getMessage());
        }
    }

    public void removeCountry(int countryID) {
        String query = "DELETE FROM Countries WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryID);
            statement.executeUpdate();
            System.out.println("Country deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to delete country: " + e.getMessage());
        }
    }

    public boolean isCountryLeader(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT leader_uuid FROM Countries WHERE leader_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getCountryPlayers(String countryName) {
        List<String> countryPlayers = new ArrayList<>();

        String query = "SELECT cp.player_uuid AS player_uuid FROM CountryPlayers cp JOIN Countries c ON cp.country_id = c.id WHERE c.name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, countryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String playerUUID = resultSet.getString("player_uuid");
                    // Ajouter le nom du joueur à la liste
                    String playerName = getPlayerName(UUID.fromString(playerUUID));
                    if (playerName != null) {
                        countryPlayers.add(playerName);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get country players: " + e.getMessage());
        }

        return countryPlayers;
    }

    public void updateCountryName(int countryId, String newName) {
        String query = "UPDATE Countries SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newName);
            statement.setInt(2, countryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to update country name: " + e.getMessage());
        }
    }


    //CountryPermissions


    public boolean isCountryRankExists(String rankName, UUID countryLeaderUUID) {
        String query = "SELECT COUNT(*) FROM CountryRanks WHERE rank_name = ? AND country_id IN (SELECT id FROM Countries WHERE leader_uuid = ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rankName);
            statement.setString(2, countryLeaderUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if country rank exists: " + e.getMessage());
        }

        return false;
    }


    public void createCountryRank(int countryID, String rankName, List<String> permissions) {
        String query = "INSERT INTO CountryRanks (country_id, rank_name) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, countryID);
            statement.setString(2, rankName);
            statement.executeUpdate();

            // Récupérer l'ID du rang nouvellement créé
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int rankID = generatedKeys.getInt(1);
                    // Ajouter les permissions au rang
                    addCountryRankPermissions(rankID, permissions);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to create default country rank: " + e.getMessage());
        }
    }

    private void addCountryRankPermissions(int rankID, List<String> permissions) {
        String query = "INSERT INTO CountryPermissions (country_rank_id, permission_name) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (String permission : permissions) {
                // Ajouter l'association entre le rang et la permission dans la table "CountryPermissions"
                statement.setInt(1, rankID);
                statement.setString(2, permission);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Failed to add permissions to country rank: " + e.getMessage());
        }
    }

    private void createDefaultCountryRanks(int countryID) {
        // Créer les rangs par défaut avec les permissions associées
        String rankName1 = "Citoyen";
        List<String> permissions1 = Arrays.asList("permission1", "permission2");
        createCountryRank(countryID, rankName1, permissions1);

        String rankName2 = "Membre";
        List<String> permissions2 = Arrays.asList("permission1", "permission2");
        createCountryRank(countryID, rankName2, permissions2);

        // Créer d'autres rangs par défaut si nécessaire avec leurs permissions respectives
        String rankName3 = "Officier";
        List<String> permissions3 = Arrays.asList("permission3", "permission4");
        createCountryRank(countryID, rankName3, permissions3);

        // ...
    }


    public List<String> getCountryRankPermissions(int countryRankID) {
        List<String> permissions = new ArrayList<>();

        String query = "SELECT permission_name FROM CountryPermissions WHERE country_rank_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryRankID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String permission = resultSet.getString("permission_name");
                    permissions.add(permission);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve country rank permissions: " + e.getMessage());
        }

        return permissions;
    }

    public void addPermissionToCountryRank(int countryRankID, String permission) {
        String query = "INSERT INTO CountryPermissions (country_rank_id, permission_name) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryRankID);
            statement.setString(2, permission);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to add permission to country rank: " + e.getMessage());
        }
    }

    public void removePermissionFromCountryRank(int countryRankID, String permission) {
        String query = "DELETE FROM CountryPermissions WHERE country_rank_id = ? AND permission_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryRankID);
            statement.setString(2, permission);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to remove permission from country rank: " + e.getMessage());
        }
    }
    //PLAYER METHODS



    private String getPlayerName(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM Players WHERE uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get player name: " + e.getMessage());
        }

        return null;
    }


    public UUID getPlayerUUID(String playerName) {
        String query = "SELECT uuid FROM Players WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String uuidString = resultSet.getString("uuid");
                    return UUID.fromString(uuidString);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get player UUID: " + e.getMessage());
        }
        return null; // Retourne null si le joueur n'est pas trouvé ou s'il y a une erreur
    }

    public void updatePlayerBalance(String playerUUID, double balance) {
        String query = "UPDATE Players SET balance = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, balance);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
            System.out.println("Player balance updated successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to update player balance: " + e.getMessage());
        }
    }

    public boolean playerExists(String playerUUID) {
        String query = "SELECT COUNT(*) AS count FROM Players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if player exists: " + e.getMessage());
        }
        return false;
    }

    public void addPlayer(String playerUUID, String playerName, double balance, int rank_id) {
        String query = "INSERT INTO Players (uuid, name, balance, rank_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            statement.setString(2, playerName);
            statement.setDouble(3, balance);
            statement.setInt(4, rank_id);
            statement.executeUpdate();
            System.out.println("Player added to Players table successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add player to Players table: " + e.getMessage());
        }
    }


    // Ajoutez d'autres méthodes nécessaires pour vos opérations sur la base de données

}