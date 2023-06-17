package fr.bouboule.unitiumplugin2.commands.country;


import fr.bouboule.unitiumplugin2.commands.country.subcommands.*;
import fr.bouboule.unitiumplugin2.commands.country.subcommands.utils.InvitationManager;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
        if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
            // Si la subcommande est "create", exécutez la logique de création de pays
            new CreateCountry(sender, args, databaseManager);
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("invite")) {
            new InviteCountry(sender, args, databaseManager, invitationManager, plugin);
            return true;

        } else if (args.length > 0 && args[0].equalsIgnoreCase("join")) {
            new JoinCountry(sender, databaseManager, invitationManager);
            return true;

        }else if (args[0].equalsIgnoreCase("leave")) {
            // Si la subcommande est "leave", exécutez la logique de quitter le pays
            new LeaveCountry(sender, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("disband")) {
            // Si la subcommande est "disband", exécutez la logique de dissolution du pays
            new DisbandCountry(sender, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("kick")){
            new KickCountry(sender, args, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("rename")) {
            new RenameCountry(sender, args, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("promote")) {
            new PromoteCountry(sender, args, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("demote")) {
            new DemoteCountry(sender, args, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("claim")) {
            new ClaimCountry(sender, databaseManager);
            return true;
        } else if (args[0].equalsIgnoreCase("unclaim")) {
            new UnclaimCountry(sender, databaseManager);
        }

        // Autres subcommands...

        return false;
    }


    // Implémentez d'autres méthodes pour les autres subcommands si nécessaire
}