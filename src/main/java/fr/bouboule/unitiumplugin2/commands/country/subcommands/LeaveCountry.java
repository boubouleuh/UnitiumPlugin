package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LeaveCountry {
    private final DatabaseManager databaseManager;

    public LeaveCountry(CommandSender sender, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender);
    }

    public void command(CommandSender sender) {

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous n'êtes pas membre d'un pays.");
            return;
        }

        // Vérifier si le joueur est le leader du pays
        if (databaseManager.isCountryLeader(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous êtes le leader du pays. Dissolvez le pays pour le quitter.");
            return;
        }

        String countryName = databaseManager.getCountryName(playerUUID);

        int countryId = databaseManager.getCountryID(countryName);
        // Supprimer le joueur du pays
        databaseManager.removePlayerFromCountry(playerUUID, countryId);

        sender.sendMessage(ChatColor.GREEN + "Vous avez quitté " + ChatColor.GOLD + countryName);
    }
}