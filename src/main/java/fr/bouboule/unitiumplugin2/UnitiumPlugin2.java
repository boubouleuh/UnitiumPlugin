package fr.bouboule.unitiumplugin2;

//import fr.bouboule.unitiumplugin2.api.Dynmap;
import fr.bouboule.unitiumplugin2.commands.country.Country;
import fr.bouboule.unitiumplugin2.commands.country.claim.PlayerDetect;
import fr.bouboule.unitiumplugin2.commands.country.claim.Protection;
import fr.bouboule.unitiumplugin2.commands.country.tabcompleter.CountryTabCompleter;
import fr.bouboule.unitiumplugin2.commands.shop.Shop;
import fr.bouboule.unitiumplugin2.commands.shop.core.ShopCore;
import fr.bouboule.unitiumplugin2.database.DatabaseConnection;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import fr.bouboule.unitiumplugin2.database.TableCreator;

import fr.bouboule.unitiumplugin2.register.PlayerRegister;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPIListener;

import java.io.File;

public final class UnitiumPlugin2 extends JavaPlugin {

    public final DatabaseConnection databaseConnectionForTable = new DatabaseConnection(getDataFolder().getAbsolutePath() + File.separator + "database.db");
    private final TableCreator tableCreator = new TableCreator(databaseConnectionForTable);

    //for table est fermé après l'appel donc il faut une nouvelle connection
    public final DatabaseConnection databaseConnectionForRequest = new DatabaseConnection(getDataFolder().getAbsolutePath() + File.separator + "database.db");

    public final DatabaseManager databaseManager = new DatabaseManager(databaseConnectionForRequest);

    public final Tags tags = new Tags(this, databaseManager);

    public final PlayerRegister playerRegister = new PlayerRegister(databaseManager);

    public final Protection protection = new Protection(databaseManager);

    public final PlayerDetect playerDetect = new PlayerDetect(databaseManager);

    public final ShopCore shopCore = new ShopCore(this, databaseManager);

//    public final Dynmap dynmap = new Dynmap(this, databaseManager);


    @Override
    public void onEnable() {
        File configFile = new File(this.getDataFolder() + "/config.yml");
        if(!configFile.exists())
        {
            saveDefaultConfig();
        }
        tableCreator.createTables();
        // Plugin startup logic
        this.saveDefaultConfig();
        registerCommands();

        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
//            dynmap.startMarkerUpdateTask();
        }
    }


    private void registerCommands() {
        getCommand("country").setExecutor(new Country(databaseManager, this));
        getCommand("shop").setExecutor(new Shop(databaseManager, this));
        getCommand("country").setTabCompleter(new CountryTabCompleter(databaseManager));
        getServer().getPluginManager().registerEvents(tags, this);
        getServer().getPluginManager().registerEvents(playerRegister, this);
        getServer().getPluginManager().registerEvents(protection, this);
        getServer().getPluginManager().registerEvents(playerDetect, this);
        getServer().getPluginManager().registerEvents(shopCore, this);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
