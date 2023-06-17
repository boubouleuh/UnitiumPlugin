package fr.bouboule.unitiumplugin2.permissions.country;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Perms {

    private final DatabaseManager databaseManager;
    public String buildPerm = "country.build";
    public String usePerm = "country.use";
    public String managePerm = "country.manage";

    public Perms(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    public boolean hasPerm(Player player, String perm) {
        UUID playerUUID = player.getUniqueId();
        String playerCountryName = databaseManager.getCountryName(playerUUID);
        int playerCountryID = databaseManager.getCountryID(playerCountryName);

        String playerRank = databaseManager.getPlayerCountryRank(playerUUID, playerCountryID);

        int playerRankID = databaseManager.getCountryRankIDByName(playerRank);

        List<String> countryRankPermissions = databaseManager.getCountryRankPermissions(playerRankID);
        System.out.println(countryRankPermissions);
        // Le joueur a la permission recherchée dans le rang du pays
        return countryRankPermissions != null && countryRankPermissions.contains(perm);
    }


}
