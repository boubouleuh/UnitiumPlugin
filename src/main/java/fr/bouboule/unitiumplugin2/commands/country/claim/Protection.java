package fr.bouboule.unitiumplugin2.commands.country.claim;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class Protection implements Listener {


    public final DatabaseManager databaseManager;

    public Protection(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }


    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Chunk chunk = event.getBlock().getChunk();

        if (databaseManager.isChunkClaimed(chunk)){

            int countryID = databaseManager.getCountryOfChunk(chunk);

            if (!databaseManager.isPlayerInCountry(playerUUID, countryID)){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Tu ne peux pas détruire de blocs ici !");

            }
        }
    }

    @EventHandler()
    public void PlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR){
            return;
        }
        Chunk chunk = event.getClickedBlock().getChunk();
        if (databaseManager.isChunkClaimed(chunk)){

            int countryID = databaseManager.getCountryOfChunk(chunk);

            if (!databaseManager.isPlayerInCountry(playerUUID, countryID)) {
                if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if(event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Tu ne peux pas intéragir ici !");
                    }else{
                        event.setCancelled(true);
                    }
               }
            }
        }
    }

}
