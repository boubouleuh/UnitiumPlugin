package fr.bouboule.unitiumplugin2.commands.country;


import fr.bouboule.unitiumplugin2.commands.country.claim.Protection;
import fr.bouboule.unitiumplugin2.commands.country.subcommands.*;
import fr.bouboule.unitiumplugin2.commands.country.subcommands.utils.InvitationManager;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getPluginManager;

public class Country implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final Plugin plugin;

    private final InvitationManager invitationManager;
    public Country(DatabaseManager databaseManager, Plugin plugin) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
        this.invitationManager = new InvitationManager();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Gérer le cas où aucune sous-commande n'est spécifiée
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                new CreateCountry(sender, args, databaseManager);
                return true;

            case "invite":
                new InviteCountry(sender, args, databaseManager, invitationManager, plugin);
                return true;

            case "join":
                new JoinCountry(sender, databaseManager, invitationManager);
                return true;

            case "leave":
                new LeaveCountry(sender, databaseManager);
                return true;

            case "disband":
                new DisbandCountry(sender, databaseManager);
                return true;

            case "kick":
                new KickCountry(sender, args, databaseManager);
                return true;

            case "rename":
                new RenameCountry(sender, args, databaseManager);
                return true;

            case "promote":
                new PromoteCountry(sender, args, databaseManager);
                return true;

            case "demote":
                new DemoteCountry(sender, args, databaseManager);
                return true;

            case "claim":
                new ClaimCountry(sender, databaseManager, plugin);
                return true;

            case "unclaim":
                new UnclaimCountry(sender, databaseManager);
                return true;

            case "alliance":
                new AllianceCountry(sender, args, databaseManager);
                return true;
            // Gérer d'autres sous-commandes ici...

            default:
                // Gérer le cas où la sous-commande spécifiée n'est pas reconnue
                return false;
        }


    }


    // Implémentez d'autres méthodes pour les autres subcommands si nécessaire
}