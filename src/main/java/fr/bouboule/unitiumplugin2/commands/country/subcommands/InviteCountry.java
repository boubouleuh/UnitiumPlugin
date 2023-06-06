package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.commands.country.subcommands.utils.InvitationManager;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteCountry {
    private final DatabaseManager databaseManager;
    private final Map<UUID, UUID> pendingInvitations;
    private final Map<UUID, BukkitRunnable> invitationTasks;

    private final Plugin plugin;
    private final InvitationManager invitationManager;

    public InviteCountry(CommandSender sender, String[] args, DatabaseManager databaseManager, InvitationManager invitationManager, Plugin plugin) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.invitationManager = invitationManager;
        this.pendingInvitations = new HashMap<>();
        this.invitationTasks = new HashMap<>();
        command(sender, args);
    }

    public void command(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /country invite");
            return;
        }
        String playerName = args[1];
        // Vérifier si le joueur a déjà un pays
        if (!databaseManager.playerHasCountry(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        // Vérifier si le joueur invité est en ligne
        Player invitedPlayer = Bukkit.getPlayer(playerName);
        if (invitedPlayer == null || !invitedPlayer.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return;
        }

        // Vérifier si le joueur invité a déjà un pays
        if (databaseManager.playerHasCountry(invitedPlayer.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " a déjà un pays.");
            return;
        }

        UUID inviterUUID = player.getUniqueId();
        UUID invitedUUID = invitedPlayer.getUniqueId();

        // Supprimer l'invitation précédente si elle existe
        if (invitationManager.hasPendingInvitation(invitedUUID)) {
            invitationManager.removeInvitation(invitedUUID);
        }

        // Stocker l'invitation dans le tableau des invitations en attente
        invitationManager.addInvitation(invitedUUID, inviterUUID);

        // Créer une tâche pour supprimer l'invitation après 5 minutes
        String countryName = databaseManager.getCountryName(player.getUniqueId());

        System.out.println(countryName);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                invitationManager.removeInvitation(invitedUUID);
                invitedPlayer.sendMessage(ChatColor.YELLOW + "L'invitation pour rejoindre " + ChatColor.GOLD + countryName + ChatColor.RESET + " a expiré.");
            }
        };
        invitationTasks.put(invitedUUID, task);
        task.runTaskLater(plugin, 5 * 60 * 20); // 5 minutes

        invitedPlayer.sendMessage(ChatColor.YELLOW + "Vous avez reçu une invitation pour rejoindre " + ChatColor.GOLD + countryName + ChatColor.RESET + ".");
        sender.sendMessage(ChatColor.GREEN + "Une invitation a été envoyée au joueur " + ChatColor.GOLD + playerName + ChatColor.RESET + ".");
    }
}