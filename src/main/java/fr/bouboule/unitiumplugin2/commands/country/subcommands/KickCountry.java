package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class KickCountry {
    private final DatabaseManager databaseManager;

    public KickCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender, args);
    }

    public void command(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /country kick <joueur>");
            return;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le propriétaire du pays
        if (!databaseManager.isCountryLeader(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous devez être le propriétaire du pays pour expulser un joueur.");
            return;
        }

        String playerName = args[1];

        UUID targetUUID = databaseManager.getPlayerUUID(playerName);

        System.out.println(targetUUID + " " + playerUUID);

        if (Objects.equals(playerUUID,targetUUID)){
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous expulser.");
            return;
        }

        // Vérifier si le joueur ciblé existe
        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur spécifié n'existe pas.");
            return;
        }

        // Vérifier si le joueur ciblé fait partie du même pays

        String targetCountry = databaseManager.getCountryName(targetUUID);
        if (!Objects.equals(targetCountry, databaseManager.getCountryName(playerUUID))){
            sender.sendMessage(ChatColor.RED + "Le joueur spécifié ne fait pas partie de votre pays.");
            return;
        }

        int countryId = databaseManager.getCountryID(targetCountry);
        // Expulser le joueur du pays
        databaseManager.removePlayerFromCountry(targetUUID,countryId);

        sender.sendMessage(ChatColor.GREEN + "Le joueur " + ChatColor.GOLD + playerName + ChatColor.GREEN + " a été expulsé du pays " + ChatColor.GOLD + targetCountry + ChatColor.RESET + ".");

        Player targetPlayer = sender.getServer().getPlayer(targetUUID);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.RED + "Vous avez été expulsé du pays " + ChatColor.GOLD + targetCountry + ChatColor.RESET + ".");
        }
    }
}