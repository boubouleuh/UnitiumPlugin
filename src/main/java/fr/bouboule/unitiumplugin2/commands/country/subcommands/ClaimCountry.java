package fr.bouboule.unitiumplugin2.commands.country.subcommands;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import fr.bouboule.unitiumplugin2.permissions.country.Perms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.UUID;

import static org.bukkit.World.Environment.NETHER;
import static org.bukkit.World.Environment.THE_END;
import static org.bukkit.World.Environment.NORMAL;
public class ClaimCountry {
    private final DatabaseManager databaseManager;
    private final Plugin plugin;
    private final Perms perms;
    public ClaimCountry(CommandSender sender, DatabaseManager databaseManager, Plugin plugin) {
        this.databaseManager = databaseManager;
        this.perms = new Perms(databaseManager);
        this.plugin = plugin;
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
        if (player.getWorld().getEnvironment().equals(NETHER) && !plugin.getConfig().getBoolean("allow-nether-claim")){
            player.sendMessage(ChatColor.RED + "Tu ne peux pas claim dans le nether.");
            return;
        }
        if (player.getWorld().getEnvironment().equals(THE_END) && !plugin.getConfig().getBoolean("allow-ender-claim")){
            player.sendMessage(ChatColor.RED + "Tu ne peux pas claim dans l'ender.");
            return;
        }
        if (!Objects.equals(player.getWorld().getName(), plugin.getConfig().getString("default-world")) && player.getWorld().getEnvironment().equals(NORMAL) && !plugin.getConfig().getBoolean("allow-otherworld-claim")){
            player.sendMessage(ChatColor.RED + "Tu ne peux pas claim dans ce monde.");
            return;
        }
        String countryName = databaseManager.getCountryName(playerUUID);
        int countryID = databaseManager.getCountryID(countryName);

        // Vérifier si le joueur est le propriétaire du pays
        if (!(databaseManager.isCountryLeader(playerUUID) || perms.hasPerm(player, perms.managePerm))) {
            player.sendMessage(ChatColor.RED + "Vous devez être propriétaire ou officier du pays pour agrandir le territoire.");
            return;
        }

        if (databaseManager.isChunkClaimed(player.getLocation().getChunk())){
            player.sendMessage(ChatColor.RED + "Ce chunk est déjà claim !");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Vous venez de claim le chunk en X : " + player.getLocation().getChunk().getX() + " Z : " + player.getLocation().getChunk().getZ() + " dans " + player.getWorld().getName());

        databaseManager.addClaimedChunk(countryID, player.getLocation().getChunk());


    }
}
