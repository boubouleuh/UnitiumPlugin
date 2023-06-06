package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenameCountry {
    private final DatabaseManager databaseManager;

    public RenameCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender, args);
    }

    public void command(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /country rename <nouveau_nom>");
            return;
        }

        Player player = (Player) sender;
        String countryName = databaseManager.getCountryName(player.getUniqueId());
        int countryID = databaseManager.getCountryID(countryName);
        String newCountryName = args[1];

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }
        if (!databaseManager.isCountryLeader(player.getUniqueId())){
            sender.sendMessage(ChatColor.RED + "Vous devez être le propriétaire du pays pour expulser un joueur.");
            return;
        }

        // Vérifier si le nouveau nom de pays est déjà utilisé
        if (databaseManager.countryExists(newCountryName)) {
            sender.sendMessage(ChatColor.RED + "Le nouveau nom de pays est déjà utilisé.");
            return;
        }

        // Renommer le pays dans la base de données
        databaseManager.updateCountryName(countryID, newCountryName);

        sender.sendMessage(ChatColor.GREEN + "Votre pays a été renommé en " + ChatColor.GOLD + newCountryName + ChatColor.RESET + ".");
    }
}