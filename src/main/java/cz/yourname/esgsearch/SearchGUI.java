package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SearchGUI implements Listener {

    private static class Session {
        Material material;
        int amount;
        Session(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }
    }

    private static final Map<UUID, Session> sessions = new HashMap<>();

    public static void openSearchGUI(Player player, Material material) {
        Session session = new Session(material, 1);
        sessions.put(player.getUniqueId(), session);
        int guiSize = ESGSearch.getInstance().getConfig().getInt("gui.size", 45);
        Inventory gui = Bukkit.createInventory(null, guiSize, ESGSearch.getRawMessage("gui.title"));
        updateGUI(player, gui, session);
        player.openInventory(gui);
    }

    private static void updateGUI(Player player, Inventory gui, Session session) {
        gui.clear();
        FileConfiguration config = ESGSearch.getInstance().getConfig();

        ItemStack singleItem = new ItemStack(session.material, 1);
        double unitBuyPrice = EconomyShopGUIHook.getItemBuyPrice(singleItem);
        double unitSellPrice = EconomyShopGUIHook.getItemSellPrice(singleItem);

        String buyPriceTotal = String.format("%.2f", unitBuyPrice * session.amount);
        String sellPriceTotal = String.format("%.2f", unitSellPrice * session.amount);

        int totalInInventory = countItems(player, session.material);
        String totalSellPrice = String.format("%.2f", unitSellPrice * totalInInventory);

        // Center item
        ItemStack centerItem = new ItemStack(session.material, session.amount);
        ItemMeta centerMeta = centerItem.getItemMeta();
        if (centerMeta != null) {
            centerMeta.setDisplayName(color(config.getString("gui.center-item.name", "&6&l{item}").replace("{item}", session.material.name())));
            List<String> lore = new ArrayList<>();
            lore.add(color(config.getString("gui.center-item.lore-selected").replace("{amount}", String.valueOf(session.amount))));
            lore.add("");
            lore.add(color(config.getString("gui.center-item.lore-buy-price").replace("{price}", buyPriceTotal)));
            if (unitSellPrice > 0) lore.add(color(config.getString("gui.center-item.lore-sell-price").replace("{price}", sellPriceTotal)));
            else lore.add(color(config.getString("gui.center-item.lore-cannot-sell")));
            centerMeta.setLore(lore);
            centerItem.setItemMeta(centerMeta);
        }
        gui.setItem(config.getInt("gui.slots.center-item", 22), centerItem);

        // Buy & Sell
        String buyName = color(config.getString("gui.buy-button.name").replace("{amount}", String.valueOf(session.amount)));
        List<String> buyLore = new ArrayList<>();
        for (String line : config.getStringList("gui.buy-button.lore")) buyLore.add(color(line.replace("{price}", buyPriceTotal)));
        gui.setItem(config.getInt("gui.slots.buy-button", 13), getConfigItem(config, "gui.buy-button", Material.EMERALD_BLOCK, buyName, buyLore));

        String sellName = color(config.getString("gui.sell-button.name").replace("{amount}", String.valueOf(session.amount)));
        List<String> sellLore = new ArrayList<>();
        for (String line : config.getStringList("gui.sell-button.lore")) {
            sellLore.add(color(line.replace("{price}", sellPriceTotal).replace("{all_amount}", String.valueOf(totalInInventory)).replace("{all_price}", totalSellPrice)));
        }
        gui.setItem(config.getInt("gui.slots.sell-button", 31), getConfigItem(config, "gui.sell-button", Material.REDSTONE_BLOCK, sellName, sellLore));

        // Navigation
        gui.setItem(config.getInt("gui.slots.back-button", 36), getConfigItem(config, "gui.back-button", Material.BARRIER, null, null));
        gui.setItem(config.getInt("gui.slots.minus-32", 19), getConfigItem(config, "gui.minus-32", Material.RED_STAINED_GLASS_PANE, null, null));
        gui.setItem(config.getInt("gui.slots.minus-16", 20), getConfigItem(config, "gui.minus-16", Material.RED_STAINED_GLASS_PANE, null, null));
        gui.setItem(config.getInt("gui.slots.minus-1", 21), getConfigItem(config, "gui.minus-1", Material.RED_STAINED_GLASS_PANE, null, null));
        gui.setItem(config.getInt("gui.slots.plus-1", 23), getConfigItem(config, "gui.plus-1", Material.GREEN_STAINED_GLASS_PANE, null, null));
        gui.setItem(config.getInt("gui.slots.plus-16", 24), getConfigItem(config, "gui.plus-16", Material.GREEN_STAINED_GLASS_PANE, null, null));
        gui.setItem(config.getInt("gui.slots.plus-32", 25), getConfigItem(config, "gui.plus-32", Material.GREEN_STAINED_GLASS_PANE, null, null));
    }

    private static ItemStack getConfigItem(FileConfiguration config, String path, Material defaultMat, String overrideName, List<String> overrideLore) {
        Material mat = Material.matchMaterial(config.getString(path + ".material", defaultMat.name()));
        if (mat == null) mat = defaultMat;
        String name = overrideName != null ? overrideName : color(config.getString(path + ".name", ""));
        
        List<String> lore = overrideLore;
        if (lore == null && config.contains(path + ".lore")) {
            lore = new ArrayList<>();
            for (String line : config.getStringList(path + ".lore")) lore.add(color(line));
        }
        
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }

    private static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) count += item.getAmount();
        }
        return count;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ESGSearch.getRawMessage("gui.title"))) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;

        FileConfiguration config = ESGSearch.getInstance().getConfig();
        int slot = event.getRawSlot();
        boolean changed = false;

        if (slot == config.getInt("gui.slots.minus-32", 19)) { session.amount -= 32; changed = true; }
        if (slot == config.getInt("gui.slots.minus-16", 20)) { session.amount -= 16; changed = true; }
        if (slot == config.getInt("gui.slots.minus-1", 21)) { session.amount -= 1; changed = true; }
        if (slot == config.getInt("gui.slots.plus-1", 23)) { session.amount += 1; changed = true; }
        if (slot == config.getInt("gui.slots.plus-16", 24)) { session.amount += 16; changed = true; }
        if (slot == config.getInt("gui.slots.plus-32", 25)) { session.amount += 32; changed = true; }

        if (changed) {
            if (session.amount < 1) session.amount = 1;
            if (session.amount > 64) session.amount = 64;
            updateGUI(player, event.getInventory(), session);
            return;
        }

        Economy eco = ESGSearch.getEconomy();
        double unitBuyPrice = EconomyShopGUIHook.getItemBuyPrice(new ItemStack(session.material));
        double unitSellPrice = EconomyShopGUIHook.getItemSellPrice(new ItemStack(session.material));

        if (slot == config.getInt("gui.slots.buy-button", 13)) {
            double cost = unitBuyPrice * session.amount;
            String priceStr = String.format("%.2f", cost);

            if (eco.has(player, cost)) {
                eco.withdrawPlayer(player, cost);
                player.getInventory().addItem(new ItemStack(session.material, session.amount));
                player.sendMessage(ESGSearch.getMessage("messages.buy-success").replace("{amount}", String.valueOf(session.amount)).replace("{item}", session.material.name()).replace("{price}", priceStr));
                DiscordWebhook.sendLog(config.getString("discord-webhook.buy-message", "").replace("{player}", player.getName()).replace("{amount}", String.valueOf(session.amount)).replace("{item}", session.material.name()).replace("{price}", priceStr));
                updateGUI(player, event.getInventory(), session);
            } else {
                player.sendMessage(ESGSearch.getMessage("messages.no-money"));
            }
        } else if (slot == config.getInt("gui.slots.sell-button", 31)) {
            if (unitSellPrice <= 0) {
                player.sendMessage(ESGSearch.getMessage("messages.cannot-sell"));
                return;
            }

            int amountToSell;
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                amountToSell = countItems(player, session.material);
                if (amountToSell <= 0) {
                    player.sendMessage(ESGSearch.getMessage("messages.no-items"));
                    return;
                }
            } else {
                amountToSell = session.amount;
                if (countItems(player, session.material) < amountToSell) {
                    player.sendMessage(ESGSearch.getMessage("messages.no-items"));
                    return;
                }
            }

            double revenue = unitSellPrice * amountToSell;
            String priceStr = String.format("%.2f", revenue);

            player.getInventory().removeItem(new ItemStack(session.material, amountToSell));
            eco.depositPlayer(player, revenue);
            player.sendMessage(ESGSearch.getMessage("messages.sell-success").replace("{amount}", String.valueOf(amountToSell)).replace("{item}", session.material.name()).replace("{price}", priceStr));
            DiscordWebhook.sendLog(config.getString("discord-webhook.sell-message", "").replace("{player}", player.getName()).replace("{amount}", String.valueOf(amountToSell)).replace("{item}", session.material.name()).replace("{price}", priceStr));
            updateGUI(player, event.getInventory(), session);
        } else if (slot == config.getInt("gui.slots.back-button", 36)) {
            player.closeInventory();
            player.performCommand("shop");
        }
    }
}
