package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.commands.country.subcommands.utils.InvitationManager;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinCountry {
    private final DatabaseManager databaseManager;
    private final InvitationManager invitationManager;

    public JoinCountry(CommandSender sender, DatabaseManager databaseManager, InvitationManager invitationManager) {
        this.databaseManager = databaseManager;
        this.invitationManager = invitationManager;
        command(sender);
    }

    public void command(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit être exécutée par un joueur.");
            return;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur a déjà un pays
        if (databaseManager.playerHasCountry(playerUUID)) {
            sender.sendMessage(ChatColor.RED + "Vous avez déjà un pays.");
            return;
        }

        // Vérifier si le joueur a une invitation en attente
        if (invitationManager.hasPendingInvitation(playerUUID)) {
            UUID inviterUUID = invitationManager.getInviterUUID(playerUUID);

            if (inviterUUID != null) {
                // Rejoindre le pays du joueur qui a envoyé l'invitation
                String countryName = databaseManager.getCountryName(inviterUUID);
                int countryId = databaseManager.getCountryID(countryName);
                databaseManager.addPlayerToCountry(playerUUID, countryId, 0);

                invitationManager.removeInvitation(playerUUID);

                sender.sendMessage(ChatColor.GREEN + "Vous avez rejoint " + ChatColor.GOLD + countryName + ChatColor.RESET + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Une erreur s'est produite lors de la récupération de l'inviteur.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas d'invitation en attente.");
        }
    }
}
