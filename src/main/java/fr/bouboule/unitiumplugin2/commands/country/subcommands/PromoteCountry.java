package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PromoteCountry {
    private DatabaseManager databaseManager;

    public PromoteCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender,args);
    }

    public void command(CommandSender sender, String[] args) {
        Player playerSender = (Player) sender;
        UUID playerSenderUUID = playerSender.getUniqueId();

        if (args.length < 2) {
            playerSender.sendMessage(ChatColor.RED + "Usage: /country promote <joueur>");
            return;
        }

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(playerSenderUUID)) {
            playerSender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le leader du pays
        if (!databaseManager.isCountryLeader(playerSenderUUID)) {
            playerSender.sendMessage(ChatColor.RED + "Vous devez être le leader du pays pour promouvoir un membre.");
            return;
        }

        UUID playerUUID = databaseManager.getPlayerUUID(args[1]);




        if (playerUUID == null) {
            playerSender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas.");
            return;
        }

        String playerCountryName = databaseManager.getCountryName(playerUUID);
        if (playerCountryName == null) {
            playerSender.sendMessage(ChatColor.RED + "Ce joueur ne fait pas partie d'un pays.");
            return;
        }

        int playerCountryId = databaseManager.getCountryID(playerCountryName);

        // Vérifier si le joueur cible est un membre du même pays
        if (!databaseManager.isPlayerInCountry(playerUUID, playerCountryId)) {
            playerSender.sendMessage(ChatColor.RED + "Ce joueur n'est pas membre de votre pays.");
            return;
        }

        // Récupérer le rang actuel du joueur cible
        String currentRank = databaseManager.getPlayerCountryRank(playerUUID, playerCountryId);
        System.out.println(playerUUID + " " + playerCountryId + " " + playerCountryName);
        if (currentRank == null) {
            playerSender.sendMessage(ChatColor.RED + "Impossible de récupérer le rang actuel du joueur.");
            return;
        }

        String higherRank = databaseManager.getHigherCountryRank(playerCountryId, currentRank);

        if (higherRank != null) {
            databaseManager.setCountryRank(playerUUID, playerCountryId, higherRank);
            playerSender.sendMessage(ChatColor.GREEN + args[1] + " a été promu au rang de " + higherRank + " dans le pays " + playerCountryName + ".");
        } else {
            playerSender.sendMessage(ChatColor.RED + args[1] + " a déjà le rang le plus élevé dans le pays " + playerCountryName + ".");
        }
    }
}