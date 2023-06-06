package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateRank {
    private final DatabaseManager databaseManager;

    public CreateRank(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender, args);
    }
    public void command(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /country createrank <name> <permissions[]>");
            return;
        }

        if (!databaseManager.playerHasCountry(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le leader du pays
        if (!databaseManager.isCountryLeader(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous devez être le leader du pays pour créer un rang.");
            return;
        }

        String rankName = args[1];

        // Vérifier si le rang existe déjà dans le pays
        if (databaseManager.isCountryRankExists(rankName, playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Le rang '" + rankName + "' existe déjà dans votre pays.");
            return;
        }

        // Récupérer les permissions à partir des arguments
        List<String> permissions = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        // Créer le rang dans le pays
        String countryName = databaseManager.getCountryName(playerUUID);
        int countryId = databaseManager.getCountryID(countryName);
        databaseManager.createCountryRank(countryId, rankName, permissions);

        sender.sendMessage(ChatColor.GREEN + "Le rang '" + rankName + "' a été créé avec succès avec les permissions suivantes : " + permissions);
    }


}
