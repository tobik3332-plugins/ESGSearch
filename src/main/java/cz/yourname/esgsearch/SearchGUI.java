package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        Inventory gui = Bukkit.createInventory(null, 45, ESGSearch.getRawMessage("gui.title"));
        updateGUI(player, gui, session);
        player.openInventory(gui);
    }

    private static void updateGUI(Player player, Inventory gui, Session session) {
        gui.clear();
        
        ItemStack singleItem = new ItemStack(session.material, 1);

        double unitBuyPrice = EconomyShopGUIHook.getItemBuyPrice(singleItem);
        double unitSellPrice = EconomyShopGUIHook.getItemSellPrice(singleItem);

        double buyPriceTotal = unitBuyPrice * session.amount;
        double sellPriceTotal = unitSellPrice * session.amount;

        // Centrální předmět (Slot 22)
        ItemStack centerItem = new ItemStack(session.material, session.amount);
        ItemMeta centerMeta = centerItem.getItemMeta();
        if (centerMeta != null) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Vybrane mnozstvi: " + ChatColor.YELLOW + session.amount);
            lore.add("");
            lore.add(ChatColor.GREEN + "Cena za nakup: " + ChatColor.WHITE + "$" + String.format("%.2f", buyPriceTotal));
            if (unitSellPrice > 0) {
                lore.add(ChatColor.RED + "Cena za prodej: " + ChatColor.WHITE + "$" + String.format("%.2f", sellPriceTotal));
            } else {
                lore.add(ChatColor.RED + "Tento predmet nelze prodat.");
            }
            centerMeta.setLore(lore);
            centerItem.setItemMeta(centerMeta);
        }
        gui.setItem(22, centerItem);

        // Buy a Sell tlačítka
        gui.setItem(13, createCustomItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "KOUPIT (" + session.amount + "x)", Arrays.asList(ChatColor.GRAY + "Klikni pro nakup za $" + String.format("%.2f", buyPriceTotal))));
        gui.setItem(31, createCustomItem(Material.REDSTONE_BLOCK, ChatColor.RED + "" + ChatColor.BOLD + "PRODAT (" + session.amount + "x)", Arrays.asList(ChatColor.GRAY + "Klikni pro prodej za $" + String.format("%.2f", sellPriceTotal))));

        // Mínusová tlačítka vlevo
        gui.setItem(19, createCustomItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-32", null));
        gui.setItem(20, createCustomItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-16", null));
        gui.setItem(21, createCustomItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-1", null));

        // Plusová tlačítka vpravo
        gui.setItem(23, createCustomItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+1", null));
        gui.setItem(24, createCustomItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+16", null));
        gui.setItem(25, createCustomItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+32", null));

        // Tlačítko zpět
        gui.setItem(36, createCustomItem(Material.BARRIER, ESGSearch.getRawMessage("gui.back-button"), null));
    }

    private static ItemStack createCustomItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ESGSearch.getRawMessage("gui.title"))) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;

        int slot = event.getRawSlot();
        boolean changed = false;

        if (slot == 19) { session.amount -= 32; changed = true; }
        if (slot == 20) { session.amount -= 16; changed = true; }
        if (slot == 21) { session.amount -= 1; changed = true; }
        if (slot == 23) { session.amount += 1; changed = true; }
        if (slot == 24) { session.amount += 16; changed = true; }
        if (slot == 25) { session.amount += 32; changed = true; }

        if (changed) {
            if (session.amount < 1) session.amount = 1;
            if (session.amount > 64) session.amount = 64;
            updateGUI(player, event.getInventory(), session);
            return;
        }

        Economy eco = ESGSearch.getEconomy();
        ItemStack singleItem = new ItemStack(session.material, 1);
        double unitBuyPrice = EconomyShopGUIHook.getItemBuyPrice(singleItem);
        double unitSellPrice = EconomyShopGUIHook.getItemSellPrice(singleItem);

        // NÁKUP (Slot 13)
        if (slot == 13) {
            double cost = unitBuyPrice * session.amount;
            if (eco.has(player, cost)) {
                eco.withdrawPlayer(player, cost);
                player.getInventory().addItem(new ItemStack(session.material, session.amount));
                player.sendMessage(ChatColor.GREEN + "Uspesne jsi koupil " + session.amount + "x " + session.material.name() + " za $" + String.format("%.2f", cost));
            } else {
                player.sendMessage(ChatColor.RED + "Nemas dostatek penez na tento nakup!");
            }
        }
        
        // PRODEJ (Slot 31)
        else if (slot == 31) {
            double revenue = unitSellPrice * session.amount;
            if (unitSellPrice <= 0) {
                player.sendMessage(ChatColor.RED + "Tento predmet nelze prodat.");
                return;
            }
            
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == session.material) {
                    count += item.getAmount();
                }
            }

            if (count >= session.amount) {
                player.getInventory().removeItem(new ItemStack(session.material, session.amount));
                eco.depositPlayer(player, revenue);
                player.sendMessage(ChatColor.GREEN + "Uspesne jsi prodal " + session.amount + "x " + session.material.name() + " za $" + String.format("%.2f", revenue));
            } else {
                player.sendMessage(ChatColor.RED + "Nemas v inventari dostatek techto predmetu k prodeji!");
            }
        }

        // ZPĚT DO MENU (Slot 36)
        else if (slot == 36) {
            player.closeInventory();
            player.performCommand("shop");
        }
    }
}
