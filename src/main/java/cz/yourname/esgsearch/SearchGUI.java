package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SearchGUI implements Listener {

    private static final Map<UUID, ItemStack> activeSearches = new HashMap<>();

    public static void openSearchGUI(Player player, ItemStack itemStack) {
        String title = ESGSearch.getRawMessage("gui.title");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack buyButton = createCustomItem(Material.GREEN_STAINED_GLASS_PANE, ESGSearch.getRawMessage("gui.buy-button"));
        ItemStack sellButton = createCustomItem(Material.RED_STAINED_GLASS_PANE, ESGSearch.getRawMessage("gui.sell-button"));
        ItemStack backButton = createCustomItem(Material.BARRIER, ESGSearch.getRawMessage("gui.back-button"));

        gui.setItem(4, buyButton);
        gui.setItem(13, itemStack);
        gui.setItem(22, sellButton);
        gui.setItem(18, backButton);

        activeSearches.put(player.getUniqueId(), itemStack);
        player.openInventory(gui);
    }

    private static ItemStack createCustomItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String expectedTitle = ESGSearch.getRawMessage("gui.title");
        if (!event.getView().getTitle().equals(expectedTitle)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        ItemStack itemStack = activeSearches.get(player.getUniqueId());

        if (itemStack != null) {
            if (slot == 4) {
                player.closeInventory();
                EconomyShopGUIHook.openItemBuyScreen(player, itemStack);
            } else if (slot == 22) {
                player.closeInventory();
                EconomyShopGUIHook.openItemSellScreen(player, itemStack);
            }
        }

        if (slot == 18) {
            player.closeInventory();
            player.performCommand("shop");
        }
    }
}
