package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
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

    private static final Map<UUID, ShopItem> activeSearches = new HashMap<>();

    public static void openSearchGUI(Player player, ShopItem shopItem) {
        String title = ESGSearch.getRawMessage("gui.title");
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack buyButton = createCustomItem(Material.GREEN_STAINED_GLASS_PANE, ESGSearch.getRawMessage("gui.buy-button"));
        ItemStack sellButton = createCustomItem(Material.RED_STAINED_GLASS_PANE, ESGSearch.getRawMessage("gui.sell-button"));
        ItemStack backButton = createCustomItem(Material.BARRIER, ESGSearch.getRawMessage("gui.back-button"));

        gui.setItem(4, buyButton);
        gui.setItem(13, shopItem.getItemStack());
        gui.setItem(22, sellButton);
        gui.setItem(18, backButton);

        activeSearches.put(player.getUniqueId(), shopItem);
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
        ShopItem shopItem = activeSearches.get(player.getUniqueId());

        if (slot == 4 && shopItem != null) {
            player.closeInventory();
            EconomyShopGUIHook.openBuyScreen(player, shopItem);
        } else if (slot == 22 && shopItem != null) {
            player.closeInventory();
            EconomyShopGUIHook.openSellScreen(player, shopItem);
        } else if (slot == 18) {
            player.closeInventory();
            player.performCommand("shop");
        }
    }
}
