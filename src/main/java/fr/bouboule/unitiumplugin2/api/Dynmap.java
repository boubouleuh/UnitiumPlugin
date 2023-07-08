package fr.bouboule.unitiumplugin2.api;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dynmap {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private DynmapAPI dynmapAPI;
    private MarkerSet markerSet;
    private Map<String, AreaMarker> countryMarkers;

    public Dynmap(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.countryMarkers = new HashMap<>();
    }



}
