package fr.bouboule.unitiumplugin2;

import fr.bouboule.unitiumplugin2.commands.country.Country;
import fr.bouboule.unitiumplugin2.commands.country.tabcompleter.CountryTabCompleter;
import fr.bouboule.unitiumplugin2.database.DatabaseConnection;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import fr.bouboule.unitiumplugin2.database.TableCreator;

import fr.bouboule.unitiumplugin2.register.PlayerRegister;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class UnitiumPlugin2 extends JavaPlugin {

    public final DatabaseConnection databaseConnectionForTable = new DatabaseConnection(getDataFolder().getAbsolutePath() + File.separator + "database.db");
    private final TableCreator tableCreator = new TableCreator(databaseConnectionForTable);

    //for table est fermé après l'appel donc il faut une nouvelle connection
    public final DatabaseConnection databaseConnectionForRequest = new DatabaseConnection(getDataFolder().getAbsolutePath() + File.separator + "database.db");

    public final DatabaseManager databaseManager = new DatabaseManager(databaseConnectionForRequest);

    public final Tags tags = new Tags(this, databaseManager);

    public final PlayerRegister playerRegister = new PlayerRegister(databaseManager);

    @Override
    public void onEnable() {
        File configFile = new File(this.getDataFolder() + "/config.yml");
        if(!configFile.exists())
        {
            saveDefaultConfig();
        }
        tableCreator.createTables();
        // Plugin startup logic

        registerCommands();
    }

    private void registerCommands() {
        getCommand("country").setExecutor(new Country(databaseManager, this));
        getCommand("country").setTabCompleter(new CountryTabCompleter(databaseManager));
        getServer().getPluginManager().registerEvents(tags, this);
        getServer().getPluginManager().registerEvents(playerRegister, this);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
