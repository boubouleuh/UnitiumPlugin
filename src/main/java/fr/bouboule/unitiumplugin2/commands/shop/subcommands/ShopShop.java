package fr.bouboule.unitiumplugin2.commands.shop.subcommands;

import fr.bouboule.unitiumplugin2.commands.shop.core.ShopCore;
import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ShopShop {


    private final Plugin plugin;

    private final ShopCore shop;
    public ShopShop(CommandSender sender, Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.shop = new ShopCore(plugin, databaseManager);
        command(sender);
    }

    private void command(CommandSender sender){

        Player player = (Player) sender;

        shop.showShopMenu(player);

    }

}
