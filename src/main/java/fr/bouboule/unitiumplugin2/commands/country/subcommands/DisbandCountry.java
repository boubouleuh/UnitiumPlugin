package fr.bouboule.unitiumplugin2.commands.country.subcommands;


import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DisbandCountry {
    private final DatabaseManager databaseManager;

    public DisbandCountry(CommandSender sender, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender);
    }

    public void command(CommandSender sender) {

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur a déjà un pays
        if (!databaseManager.playerHasCountry(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le leader du pays
        if (!databaseManager.isCountryLeader(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous devez être le leader du pays pour dissoudre le pays.");
            return;
        }
        String countryName = databaseManager.getCountryName(playerUUID);
        int countryId = databaseManager.getCountryID(countryName);
        // Dissoudre le pays
        databaseManager.removeCountry(countryId);
        // Vous devrez ajouter la logique pour dissoudre le pays dans la base de données et effectuer les actions nécessaires

        sender.sendMessage(ChatColor.GREEN + "Le pays a été dissous.");
    }
}