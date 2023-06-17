package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import fr.bouboule.unitiumplugin2.permissions.country.Perms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnclaimCountry {
    private final DatabaseManager databaseManager;

    private final Perms perms;
    public UnclaimCountry(CommandSender sender, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.perms = new Perms(databaseManager);
        command(sender);
    }

    public void command(CommandSender sender){

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur a un pays
        if (!databaseManager.playerHasCountry(playerUUID)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas de pays.");
            return;
        }

        String countryName = databaseManager.getCountryName(playerUUID);
        int countryID = databaseManager.getCountryID(countryName);

        // Vérifier si le joueur est le propriétaire du pays
        if (!(databaseManager.isCountryLeader(playerUUID) || perms.hasPerm(player, perms.managePerm))) {
            player.sendMessage(ChatColor.RED + "Vous devez être propriétaire ou officier du pays pour réduire le territoire.");
            return;
        }

        if (!databaseManager.isChunkClaimed(player.getLocation().getChunk())){
            player.sendMessage(ChatColor.RED + "Ce chunk n'est pas claim !");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Vous venez de unclaim le chunk en X : " + player.getLocation().getChunk().getX() + " Z : " + player.getLocation().getChunk().getZ() + " dans " + player.getWorld().getName());

        databaseManager.removeClaimedChunk(countryID, player.getLocation().getChunk());


    }
}
