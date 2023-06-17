package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DemoteCountry {
    private DatabaseManager databaseManager;

    public DemoteCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender, args);
    }

    public void command(CommandSender sender, String[] args) {
        Player playerSender = (Player) sender;
        UUID playerSenderUUID = playerSender.getUniqueId();

        if (args.length < 2) {
            playerSender.sendMessage(ChatColor.RED + "Usage: /country demote <joueur>");
            return;
        }

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(playerSenderUUID)) {
            playerSender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le leader du pays
        if (!databaseManager.isCountryLeader(playerSenderUUID)) {
            playerSender.sendMessage(ChatColor.RED + "Vous devez être le leader du pays pour rétrograder un membre.");
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

        if (currentRank == null) {
            playerSender.sendMessage(ChatColor.RED + "Impossible de récupérer le rang actuel du joueur.");
            return;
        }

        String lowerRank = databaseManager.getLowerCountryRank(playerCountryId, currentRank);

        if (lowerRank != null) {
            databaseManager.setCountryRank(playerUUID, playerCountryId, lowerRank);
            playerSender.sendMessage(ChatColor.GREEN + args[1] + " a été rétrogradé au rang de " + lowerRank + " dans le pays " + playerCountryName + ".");
        } else {
            playerSender.sendMessage(ChatColor.RED + args[1] + " a déjà le rang le plus bas dans le pays " + playerCountryName + ".");
        }
    }
}