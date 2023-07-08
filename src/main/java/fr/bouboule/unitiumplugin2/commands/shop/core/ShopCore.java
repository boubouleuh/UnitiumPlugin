package fr.bouboule.unitiumplugin2.commands.shop.core;

import fr.bouboule.unitiumplugin2.database.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ShopCore implements Listener {
    private final Plugin plugin;
    private final Map<String, Category> categories = new HashMap<>();

    private final DatabaseManager databaseManager;

    public ShopCore(Plugin plugin, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;





        // Chargement de la configuration du plugin
        plugin.getConfig().options().copyDefaults(true);
        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("shop.categories");
        if (categoriesSection != null) {
            // Chargement des catégories et des articles de la configuration
            for (String categoryName : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryName);
                if (categorySection != null) {
                    // Chargement de l'icone de la catégorie
                    Material categoryIcon = Material.getMaterial(categorySection.getString("icon"));
                    if (categoryIcon == null) {
                        categoryIcon = Material.STONE;
                    }
                    Category category = new Category(categoryName, new ItemStack(categoryIcon));
                    // Chargement des items de la catégorie
                    ConfigurationSection itemsSection = categorySection.getConfigurationSection("items");
                    if (itemsSection != null) {
                        for (String itemName : itemsSection.getKeys(false)) {
                            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemName);
                            if (itemSection != null) {
                                Material itemMaterial = Material.getMaterial(itemSection.getString("material"));
                                if (itemMaterial == null) {
                                    itemMaterial = Material.STONE;
                                }
                                int itemAmount = itemSection.getInt("amount", 1);
                                double itemPrice = itemSection.getDouble("price", 0);
                                double itemSellPrice = itemSection.getDouble("sell-price", 0);
                                boolean canSell = itemSection.getBoolean("canSell", false);
                                boolean canBuy = itemSection.getBoolean("canBuy", false);
                                ShopItem shopItem = new ShopItem(new ItemStack(itemMaterial, itemAmount), itemPrice, itemSellPrice, itemMaterial.getMaxStackSize(), canSell, canBuy);
                                category.addItem(itemName, shopItem);
                            }
                        }
                    }
                    categories.put(categoryName, category);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory != null) {
            String title = inventory.getTitle();
            Player player = (Player) event.getWhoClicked();
            if (title != null && title.equals("Shop")) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null) {
                    // Check if item is in player's inventory
                    if (!player.getInventory().containsAtLeast(clickedItem, clickedItem.getAmount())) {
                        // Show category menu if item not in inventory
                        showCategoryMenu(player, categories.get(clickedItem.getItemMeta().getLocalizedName().substring(2)));
                    }
                }
            } else if (title != null && categories.containsKey(title)) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if(event.isRightClick())
                    if (clickedItem != null && canSell(clickedItem)) {
                        if (event.isShiftClick()){
                            int amount = 0;
                            for (ItemStack item : player.getInventory().getContents()) {
                                if(item != null){
                                    if (item.getType() == clickedItem.getType()) {
                                        amount += item.getAmount();
                                    }
                                }
                            }

                            // Check if player has enough of the item to sell
                            if (clickedItem.getAmount() > player.getInventory().all(clickedItem.getType()).values().stream().mapToInt(ItemStack::getAmount).sum()) {
                                player.sendMessage(ChatColor.RED + "You do not have enough " + clickedItem.getType().toString() + " to sell.");
                                return;
                            }


                            double sellPrice = getSellPrice(clickedItem) * clickedItem.getMaxStackSize();

                            ItemStack finalItem = new ItemStack(clickedItem.getType());
                            player.getInventory().remove(finalItem.getType());
                            player.sendMessage(ChatColor.GREEN + "Sold " + amount + " " + clickedItem.getType().toString() + " for $" + sellPrice + ".");
                            databaseManager.addPlayerBalance(player.getUniqueId(), sellPrice);

                            return;

                        }




                        // Check if player has enough of the item to sell
                        if (clickedItem.getAmount() > player.getInventory().all(clickedItem.getType()).values().stream().mapToInt(ItemStack::getAmount).sum()) {
                            player.sendMessage(ChatColor.RED + "You do not have enough " + clickedItem.getType().toString() + " to sell.");
                            return;
                        }


                        double sellPrice = getSellPrice(clickedItem);

                        ItemStack finalItem = new ItemStack(clickedItem.getType());
                        player.getInventory().removeItem(finalItem);
                        player.sendMessage(ChatColor.GREEN + "Sold " + 1 + " " + clickedItem.getType().toString() + " for $" + sellPrice + ".");
                        databaseManager.addPlayerBalance(player.getUniqueId(), sellPrice);
                    } else {
                        player.sendMessage(ChatColor.RED + "You cant do that");
                    }
                else if(event.isLeftClick()) {
                    int amount = 1;

                    if (clickedItem.getItemMeta() != null){
                        if (Objects.equals(clickedItem.getItemMeta().getLocalizedName(), ChatColor.RED + "Back")) {
                            showShopMenu((Player) event.getWhoClicked());
                            return;
                        }

                        if (canBuy(clickedItem)) {


                            if (event.isShiftClick()) {
                                amount = clickedItem.getMaxStackSize();


                                // Check if player has enough money
                                double price = getPrice(clickedItem) * amount;
                                if (price > 0 && databaseManager.getPlayerBalance(player.getUniqueId()) < price) {
                                    player.sendMessage(ChatColor.RED + "You don't have enough money to buy " + clickedItem.getType().toString() + ".");
                                    return;
                                }
                                // Buy the item
                                ItemStack finalItem = new ItemStack(clickedItem.getType());
                                int remainingSpace = player.getInventory().addItem(finalItem).size();
                                if (remainingSpace > 0) {
                                    player.sendMessage(ChatColor.RED + "Your inventory is full.");
                                } else {
                                    player.sendMessage(ChatColor.GREEN + "You have bought " + amount + " " + clickedItem.getType().toString() + "for $" + price + ".");
                                    if (price > 0) {
                                        databaseManager.removePlayerBalance(player.getUniqueId(), price);
                                    }
                                }
                                return;
                            }


                            // Check if player has enough money
                            double price = getPrice(clickedItem);
                            if (price > 0 && databaseManager.getPlayerBalance(player.getUniqueId()) < price) {
                                player.sendMessage(ChatColor.RED + "You don't have enough money to buy " + clickedItem.getType().toString() + ".");
                                return;
                            }

                        // Buy the item
                        ItemStack finalItem = new ItemStack(clickedItem.getType());
                        int remainingSpace = player.getInventory().addItem(finalItem).size();
                        if (remainingSpace > 0) {
                            player.sendMessage(ChatColor.RED + "Your inventory is full.");
                        } else {
                            player.sendMessage(ChatColor.GREEN + "You have bought " + amount + " " + clickedItem.getType().toString() + "for $" + price + ".");
                            if (price > 0) {
                                databaseManager.removePlayerBalance(player.getUniqueId(), price);
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You cant do that");
                    }

                }
                }
            }
        }
    }






    private double getSellPrice(ItemStack item) {
        for (Category category : categories.values()) {
            for (ShopItem shopItem : category.getItems().values()) {
                if (shopItem.getItemStack().isSimilar(item)) {
                    return shopItem.getSellPrice();
                }
            }
        }
        return 0;
    }
    /**
     * Obtient le prix d'un article.
     *
     * @param item L'article.
     * @return Le prix de l'article, ou 0 s'il n'a pas de prix défini.
     */
    private double getPrice(ItemStack item) {
        for (Category category : categories.values()) {
            for (ShopItem shopItem : category.getItems().values()) {
                if (shopItem.getItemStack().isSimilar(item)) {
                    return shopItem.getPrice();
                }
            }
        }
        return 0;
    }


    public boolean canBuy(ItemStack item) {
        for (Category category : categories.values()) {
            for (ShopItem shopItem : category.getItems().values()) {
                if (shopItem.getItemStack().isSimilar(item)) {
                    return shopItem.canBuy();
                }
            }
        }
        return false;
    }

    public boolean canSell(ItemStack item) {
        for (Category category : categories.values()) {
            for (ShopItem shopItem : category.getItems().values()) {
                if (shopItem.getItemStack().isSimilar(item)) {
                    return shopItem.canSell();
                }
            }
        }
        return false;
    }

    /**
     * Affiche le menu du shop au joueur.
     *
     * @param player Le joueur.
     */
    public void showShopMenu(Player player) {
        Inventory inventory = plugin.getServer().createInventory(null, 9, "Shop");

        for (Category category : categories.values()) {
            ItemStack categoryItemStack = new ItemStack(category.getIcon());


            ItemMeta categoryItemStackMeta = categoryItemStack.getItemMeta();

            categoryItemStackMeta.setLocalizedName(ChatColor.GREEN + category.getName());


            categoryItemStack.setItemMeta(categoryItemStackMeta);

            inventory.addItem(categoryItemStack);


        }

        player.openInventory(inventory);
    }

    public void showCategoryMenu(Player player, Category category) {
        Inventory inventory = plugin.getServer().createInventory(null, 27, category.getName());

        for (Map.Entry<String, ShopItem> entry : category.getItems().entrySet()) {
            List<String> lore = new ArrayList<>(); // Création d'une liste pour le lore
            if (entry.getValue().canBuy){
                lore.add(ChatColor.GOLD + "Price : " + entry.getValue().getPrice()); // Ajout d'une ligne de lore
                lore.add(ChatColor.YELLOW + "MAJ click to buy one stack instantly for : " + entry.getValue().getPrice() * entry.getValue().maxStackSize + " $");
            }
            if (entry.getValue().canSell){
                lore.add(ChatColor.GOLD + "Sell price : " + entry.getValue().getSellPrice()); // Ajout d'une ligne de lore

                int amount = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getType() == entry.getValue().itemStack.getType()) {
                            amount += item.getAmount();
                        }
                    }
                }
                lore.add(ChatColor.YELLOW + "MAJ click to sell all for : " + amount * entry.getValue().maxStackSize + " $");




            }




            ItemStack itemStack = entry.getValue().getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.addItem(itemStack);
        }

        ItemStack backItem = new ItemStack(Material.BARRIER);

        ItemMeta backItemMeta = backItem.getItemMeta();

        backItemMeta.setLocalizedName(ChatColor.RED + "Back");

        backItem.setItemMeta(backItemMeta);

        inventory.setItem(26, backItem);

        player.openInventory(inventory);
    }




    /**
     * Représente une catégorie d'articles du shop.
     */
    private static class Category {
        private final String name;
        private final Map<String, ShopItem> items = new HashMap<>();
        private ItemStack icon;

        public Category(String name, ItemStack icon) {
            this.name = name;
            this.icon = icon;
        }

        public void addItem(String name, ShopItem item) {
            items.put(name, item);
        }

        public String getName() {
            return name;
        }

        public Map<String, ShopItem> getItems() {
            return items;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
        }
    }

    /**
     * Représente un article du shop.
     */
    private static class ShopItem {
        private final ItemStack itemStack;
        private final double price;
        private final double sellprice;
        private final int maxStackSize;

        private final boolean canSell;

        private final boolean canBuy;
        public ShopItem(ItemStack itemStack, double price, double sellprice, int maxStackSize, boolean canSell, boolean canBuy) {
            this.itemStack = itemStack;
            this.price = price;
            this.sellprice = sellprice;
            this.maxStackSize = maxStackSize;
            this.canSell = canSell;
            this.canBuy = canBuy;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public double getPrice() {
            return price;
        }

        public double getSellPrice() {
            return sellprice;
        }

        public boolean canSell() {
            return canSell;
        }

        public boolean canBuy() {
            return canBuy;
        }

        public int getMaxStackSize() {
            return maxStackSize;
        }
    }

}