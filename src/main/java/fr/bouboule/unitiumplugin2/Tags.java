package fr.bouboule.unitiumplugin2;

import com.mojang.authlib.GameProfile;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Tags implements Listener {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;

    public Tags(Plugin plugin, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
    }



    public static void setPrefixTag(Player player, String prefix) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(player.getName());

        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }

        String displayName = prefix + player.getName();
        team.setPrefix(prefix);
        team.setSuffix("");
        team.setDisplayName(displayName);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setScoreboard(scoreboard);
        }
    }


    public static void setTabListHeaderAndFooter(Player player, String header, String footer) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        IChatBaseComponent headerComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
        IChatBaseComponent footerComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, headerComponent);

            Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerComponent);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        entityPlayer.playerConnection.sendPacket(packet);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Obtention des informations sur le pays du joueur
        String country = databaseManager.getCountryName(player.getUniqueId());
        if (country == null) {
            country = "None";
        }



        // Création du header et du footer avec les informations du serveur et une belle décoration
        String header = "\n§6§l===[ §f§lFragTernity §6§l]===\n"
                + "§r§7Welcome, §e§l" + player.getName() + "§r§7!\n"
                + "§r§8» §b§lDynmap: §fhttps://map.fragternity.fr\n"
                + "§r§8» §b§lDiscord: §fhttps://fragternity.fr\n";

        // Convertit les codes de couleurs Minecraft en caractères spéciaux pour l'affichage
        header = ChatColor.translateAlternateColorCodes('&', header);

        // Tâche pour mettre à jour régulièrement le header et le footer du tableau des joueurs

        String finalHeader = header;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            // Obtention des informations sur le pays du joueur (mise à jour à chaque itération)
            String finalCountry = databaseManager.getCountryName(player.getUniqueId());
            if (finalCountry == null) {
                finalCountry = "None";
            }
            String prefix = ChatColor.YELLOW + "[" + finalCountry + "] " + ChatColor.RESET;
            // Définition du préfixe et du nom affiché du joueur
            setPrefixTag(player, prefix);
            // Mise à jour du nom affiché du joueur avec le préfixe et le pays
            player.setPlayerListName(prefix + player.getName());

            // Mise à jour du préfixe dans l'équipe du joueur

            // Construction du footer avec les informations du serveur et du joueur
            String footer = "\n§r§7TPS: §a§l" + Lag.getTPS() + "  §r§7Players: §e§l" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + "  §r§7Country: §e§l" + (finalCountry.equalsIgnoreCase("None") ? "None" : finalCountry) + "\n";

            // Convertit les codes de couleurs Minecraft en caractères spéciaux pour l'affichage
            footer = ChatColor.translateAlternateColorCodes('&', footer);

            // Mise à jour du header et du footer dans le tableau des joueurs du joueur
            setTabListHeaderAndFooter(player, finalHeader, footer);
        }, 0L, 20L);
    }

}