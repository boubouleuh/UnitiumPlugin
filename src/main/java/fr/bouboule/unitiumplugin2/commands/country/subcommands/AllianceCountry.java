package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import fr.bouboule.unitiumplugin2.permissions.country.Perms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public class AllianceCountry {

    private final DatabaseManager databaseManager;

    public AllianceCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {

        this.databaseManager = databaseManager;
        command(sender, args);
    }

    public void command(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        //todo faire le tabcompleter pour la subcommand
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /country alliance <commande> <*nom>");
            return;
        }

        if (!databaseManager.playerHasCountry(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur est le propriétaire du pays
        if (!(databaseManager.isCountryLeader(playerUUID))) {
            player.sendMessage(ChatColor.RED + "Vous devez être propriétaire du pays pour gérer les alliances.");
            return;
        }



        String subCommand = args[1].toLowerCase();

        String name = args[2];

        int countryID = databaseManager.getCountryID(playerUUID);
        player.sendMessage(subCommand);
        switch (subCommand) {
            case "create":


                if (databaseManager.allianceExists(name)){
                    player.sendMessage(ChatColor.RED + "Cette alliance éxiste déjà !");
                    return;
                }

                databaseManager.addAlliance(countryID, name);
                player.sendMessage(ChatColor.GREEN + "L'alliance " + name + " à été crée avec succès !");

            case "delete":
                //todo delete / invite / kick
        }


    }
}
