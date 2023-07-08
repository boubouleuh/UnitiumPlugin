package fr.bouboule.unitiumplugin2.commands.country.claim;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerDetect implements Listener {

    private final DatabaseManager databaseManager;
    public PlayerDetect(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Chunk to = event.getTo().getChunk();
        Chunk from = event.getFrom().getChunk();

        // Vérifier si le joueur a changé de territoire
        int fromCountry = databaseManager.getCountryOfChunk(from);
        int toCountry = databaseManager.getCountryOfChunk(to);

        String fromCountryName = databaseManager.getCountryName(fromCountry);
        String toCountryName = databaseManager.getCountryName(toCountry);

        // Si le joueur entre dans un territoire
        if (databaseManager.isChunkClaimed(from) && !databaseManager.isChunkClaimed(to)) {
            player.sendTitle(ChatColor.RED + fromCountryName, ChatColor.RED + "Vous sortez du territoire", 10, 10, 20);

        }
        // Si le joueur sort d'un territoire
        else if (!databaseManager.isChunkClaimed(from) && databaseManager.isChunkClaimed(to)){
            player.sendTitle(ChatColor.GREEN + toCountryName, ChatColor.GREEN + "Vous entrez dans le territoire" , 10, 10, 20);
        }else if (databaseManager.isChunkClaimed(from) && databaseManager.isChunkClaimed(to) && (from.getX() != to.getX() || from.getZ() != to.getZ()) && databaseManager.getCountryOfChunk(from) != databaseManager.getCountryOfChunk(to)) {
            player.sendTitle(ChatColor.GREEN + toCountryName, ChatColor.GREEN + "Vous entrez dans le territoire", 10, 10, 20);
        }
    }



}
