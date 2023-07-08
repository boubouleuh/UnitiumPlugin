package fr.bouboule.unitiumplugin2.register;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerRegister implements Listener {
    DatabaseManager databaseManager;

    public PlayerRegister(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur est déjà dans la table Players
        if (!databaseManager.playerExists(playerUUID)) {
            // Ajouter le joueur à la table Players avec les infos par défaut
            databaseManager.addPlayer(playerUUID, playerName, 0, 0); //todo change rankid here later
        }
    }
}
