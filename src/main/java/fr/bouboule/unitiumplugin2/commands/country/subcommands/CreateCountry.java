package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCountry {
    private final DatabaseManager databaseManager;



    public CreateCountry(CommandSender sender, String[] args, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        command(sender, args);
    }


    private void command(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /country create <nom>");
            return;
        }
        String name = args[1];
        // Vérifier si le joueur a déjà un pays
        if (databaseManager.playerHasCountry(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous avez déjà un pays.");
            return;
        }
        if (databaseManager.countryExists(name)){
            sender.sendMessage(ChatColor.RED + "Ce pays existe déjà.");
            return;
        }
        // Vérifier si les arguments nécessaires sont présents


        double bank = 0.0; // Valeur par défaut pour la banque, vous pouvez la modifier selon vos besoins

        // Appeler la méthode pour créer un pays dans la base de données
        databaseManager.createCountry(player.getUniqueId(), name, "Ce pays n'a défini aucune description" , bank);

        // Envoyer un message à tous les joueurs pour annoncer le nouveau pays
        String message = ChatColor.GREEN + "Un nouveau pays a été créé !\n"
                + "Nom : " + ChatColor.WHITE + name + "\n"
                + "Créé par : " + ChatColor.WHITE + player.getName();
        Bukkit.getServer().broadcastMessage(message);
    }
}