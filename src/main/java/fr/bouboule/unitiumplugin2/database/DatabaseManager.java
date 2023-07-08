package fr.bouboule.unitiumplugin2.database;

import fr.bouboule.unitiumplugin2.permissions.country.Perms;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

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


    public int getCountryID(UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT c.id FROM Countries c LEFT JOIN CountryPlayers cp ON c.id = cp.country_id WHERE cp.player_uuid = ? OR c.leader_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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


    public List<Integer> getAllCountryIds() {
        List<Integer> countryIds = new ArrayList<>();

        String query = "SELECT id FROM Countries";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int countryId = resultSet.getInt("id");
                countryIds.add(countryId);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all country IDs: " + e.getMessage());
        }

        return countryIds;
    }


    public List<String> getAllCountryNames() {
        List<String> countries = new ArrayList<>();

        String query = "SELECT name FROM Countries";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String countryName = resultSet.getString("name");
                countries.add(countryName);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all countries: " + e.getMessage());
        }

        return countries;
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

    public int getCountryOfChunk(Chunk chunk) {
        String query = "SELECT country_id FROM ClaimedChunks WHERE world_name = ? AND x = ? AND z = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, chunk.getWorld().getName());
            statement.setInt(2, chunk.getX());
            statement.setInt(3, chunk.getZ());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("country_id");
            }
        } catch (SQLException e) {
            System.out.println("Failed to get country of chunk: " + e.getMessage());
        }
        return -1; // Valeur par défaut si le pays n'est pas trouvé ou s'il y a une erreur
    }

    public List<Chunk> getAllClaimedChunks() {
        List<Chunk> claimedChunks = new ArrayList<>();

        String query = "SELECT world_name, x, z FROM ClaimedChunks";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String worldName = resultSet.getString("world_name");
                int chunkX = resultSet.getInt("x");
                int chunkZ = resultSet.getInt("z");

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    claimedChunks.add(chunk);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get all claimed chunks: " + e.getMessage());
        }

        return claimedChunks;
    }
    public void addClaimedChunk(int countryId, Chunk chunk) {
        String query = "INSERT INTO ClaimedChunks (country_id, world_name, x, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId);
            statement.setString(2, chunk.getWorld().getName());
            statement.setInt(3, chunk.getX());
            statement.setInt(4, chunk.getZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to add claimed chunk: " + e.getMessage());
        }
    }

    public boolean isChunkClaimed(Chunk chunk) {
        String query = "SELECT COUNT(*) AS count FROM ClaimedChunks WHERE x = ? AND z = ? AND world_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chunk.getX());
            statement.setInt(2, chunk.getZ());
            statement.setString(3, chunk.getWorld().getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if chunk is claimed: " + e.getMessage());
        }
        return false;
    }
    public void removeClaimedChunk(int countryId, Chunk chunk) {
        String query = "DELETE FROM ClaimedChunks WHERE country_id = ? AND x = ? AND z = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId);
            statement.setInt(2, chunk.getX());
            statement.setInt(3, chunk.getZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to remove claimed chunk: " + e.getMessage());
        }
    }

    public List<Chunk> getClaimedChunksByCountry(int countryId) {
        List<Chunk> claimedChunks = new ArrayList<>();
        String query = "SELECT world_name, x, z FROM ClaimedChunks WHERE country_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String worldName = resultSet.getString("world_name");
                    int x = resultSet.getInt("x");
                    int z = resultSet.getInt("z");
                    Chunk claimedChunk = Bukkit.getWorld(worldName).getChunkAt(x,z);
                    claimedChunks.add(claimedChunk);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get claimed chunks: " + e.getMessage());
        }
        return claimedChunks;
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
    public String getCountryName(int countryID) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT name FROM Countries WHERE id = ?")) {
            statement.setInt(1, countryID);
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
            System.err.println("Erreur lors de la vérification du leader du pays : " + e.getMessage());
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
                    if (permissions != null){
                        addCountryRankPermissions(rankID, permissions);
                    }
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
        Perms perms = new Perms(this);
        String rankName1 = "Citoyen";
        createCountryRank(countryID, rankName1, null);

        String rankName2 = "Membre";
        List<String> permissions2 = Arrays.asList(perms.buildPerm, perms.usePerm);
        createCountryRank(countryID, rankName2, permissions2);

        // Créer d'autres rangs par défaut si nécessaire avec leurs permissions respectives
        String rankName3 = "Officier";
        List<String> permissions3 = Arrays.asList(perms.buildPerm, perms.usePerm, perms.managePerm);
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

    public boolean isPlayerInCountry(UUID playerUUID, int countryID) {
        String query = "SELECT COUNT(*) FROM CountryPlayers WHERE player_uuid = ? AND country_id = ?";
        int count = 0;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, countryID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);

                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if player is in country: " + e.getMessage());
        }

        // Si le joueur n'est pas trouvé dans CountryPlayers, recherche dans Countries
        query = "SELECT COUNT(*) FROM Countries WHERE leader_uuid = ? AND id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, countryID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt(1);

                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check if player is in country (from Countries table): " + e.getMessage());
        }
        return count > 0;
    }

    public String getPlayerCountryRank(UUID playerUUID, int countryID) {
        String query = "SELECT rank_id FROM CountryPlayers " +
                "WHERE player_uuid = ? AND country_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, countryID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int rankID = resultSet.getInt("rank_id");
                    return getCountryRankName(rankID);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get player country rank: " + e.getMessage());
        }

        return null;
    }

    private String getCountryRankName(int rankID) {
        String query = "SELECT rank_name FROM CountryRanks WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, rankID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rank_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get rank name: " + e.getMessage());
        }

        return null;
    }


    public String getLowerCountryRank(int countryID, String currentRank) {
        String query = "SELECT rank_name FROM CountryRanks " +
                "WHERE country_id = ? AND rank_name < ? " +
                "ORDER BY rank_name DESC LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryID);
            statement.setString(2, currentRank);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rank_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get lower country rank: " + e.getMessage());
        }

        return null;
    }

    public String getHigherCountryRank(int countryID, String currentRank) {
        String query = "SELECT rank_name FROM CountryRanks " +
                "WHERE country_id = ? AND rank_name > ? " +
                "ORDER BY rank_name ASC LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryID);
            statement.setString(2, currentRank);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("rank_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get higher country rank: " + e.getMessage());
        }

        return null;
    }

    public void setCountryRank(UUID playerUUID, int countryID, String rankName) {
        try {
            // Récupérer l'ID du rang en fonction de son nom
            int rankID = getCountryRankIDByName(rankName);

            if (rankID == -1) {
                System.out.println("Rank not found: " + rankName);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE CountryPlayers SET rank_id = ? WHERE player_uuid = ? AND country_id = ?")) {
                statement.setInt(1, rankID);
                statement.setString(2, playerUUID.toString());
                statement.setInt(3, countryID);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Failed to set country rank: " + e.getMessage());
        }
    }

    public int getCountryRankIDByName(String rankName) {
        int rankID = -1;

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM CountryRanks WHERE rank_name = ?")) {
            statement.setString(1, rankName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    rankID = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get rank ID: " + e.getMessage());
        }

        return rankID;
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

    public void updatePlayerBalance(UUID playerUUID, double balance) {
        String query = "UPDATE Players SET balance = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, balance);
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();
            System.out.println("Player balance updated successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to update player balance: " + e.getMessage());
        }
    }

    public void removePlayerBalance(UUID playerUUID, double amount) {
        String query = "UPDATE Players SET balance = balance - ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, amount);
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();
            System.out.println("Player balance updated successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to update player balance: " + e.getMessage());
        }
    }

    public double getPlayerBalance(UUID playerUUID) {
        String query = "SELECT balance FROM Players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get player balance: " + e.getMessage());
        }

        return 0.0; // Retourne 0 par défaut si le solde n'a pas été trouvé ou s'il y a eu une erreur
    }

    public void addPlayerBalance(UUID playerUUID, double amount) {
        double currentBalance = getPlayerBalance(playerUUID);
        double newBalance = currentBalance + amount;

        String query = "UPDATE Players SET balance = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, newBalance);
            statement.setString(2, playerUUID.toString());
            statement.executeUpdate();
            System.out.println("Player balance updated successfully. New balance: " + newBalance);
        } catch (SQLException e) {
            System.out.println("Failed to update player balance: " + e.getMessage());
        }
    }
    public boolean playerExists(UUID playerUUID) {
        String query = "SELECT COUNT(*) AS count FROM Players WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
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

    public void addPlayer(UUID playerUUID, String playerName, double balance, int rank_id) {
        String query = "INSERT INTO Players (uuid, name, balance, rank_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setDouble(3, balance);
            statement.setInt(4, rank_id);
            statement.executeUpdate();
            System.out.println("Player added to Players table successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add player to Players table: " + e.getMessage());
        }
    }

    public void addRelation(int countryId1, int countryId2, String status) {
        String query = "INSERT INTO Relations (country_id1, country_id2, status) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId1);
            statement.setInt(2, countryId2);
            statement.setString(3, status);
            statement.executeUpdate();
            System.out.println("Relation added to Relations table successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add relation to Relations table: " + e.getMessage());
        }
    }

    public void addAlliance(int founderCountryId, String allianceName) {
        String query = "INSERT INTO Alliances (founder_country_id, name) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, founderCountryId);
            statement.setString(2, allianceName);
            statement.executeUpdate();
            System.out.println("Alliance added to Alliances table successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add alliance to Alliances table: " + e.getMessage());
        }
    }

    public List<Integer> getAlliancesCreatedByCountry(int countryId) {
        List<Integer> allianceIds = new ArrayList<>();
        String query = "SELECT id FROM Alliances WHERE founder_country_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, countryId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int allianceId = resultSet.getInt("id");
                allianceIds.add(allianceId);
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve alliances created by country from Alliances table: " + e.getMessage());
        }
        return allianceIds;
    }

    public boolean allianceExists(String allianceName) {
        String query = "SELECT COUNT(*) AS count FROM Alliances WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, allianceName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("Failed to check alliance existence in Alliances table: " + e.getMessage());
        }
        return false;
    }

    public int getAllianceIdByName(String allianceName) {
        String query = "SELECT id FROM Alliances WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, allianceName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve alliance ID by name from Alliances table: " + e.getMessage());
        }
        return -1; // Valeur de retour par défaut si l'alliance n'est pas trouvée ou en cas d'erreur
    }


    //TODO Faire un systeme qui met automatiquement en ennemi toute les alliances du pays cible contre le pays ayant envoyer la commande

    public void addAllianceMember(int allianceId, int countryId) {
        String query = "INSERT INTO AllianceMembers (alliance_id, country_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, allianceId);
            statement.setInt(2, countryId);
            statement.executeUpdate();
            System.out.println("Alliance member added to AllianceMembers table successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to add alliance member to AllianceMembers table: " + e.getMessage());
        }
    }

    public List<Integer> getAllianceMembers(int allianceId) {
        List<Integer> members = new ArrayList<>();
        String query = "SELECT country_id FROM AllianceMembers WHERE alliance_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, allianceId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(resultSet.getInt("country_id"));
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve alliance members from AllianceMembers table: " + e.getMessage());
        }
        return members;
    }

    // Ajoutez d'autres méthodes nécessaires pour vos opérations sur la base de données

}