package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {
    private static final Set<UUID> searchingPlayers = new HashSet<>();

    public static void startSearching(Player player) {
        searchingPlayers.add(player.getUniqueId());
        player.sendMessage(ESGSearch.getMessage("messages.chat-prompt"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!searchingPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("cancel")) {
            searchingPlayers.remove(player.getUniqueId());
            player.sendMessage(ESGSearch.getMessage("messages.chat-cancelled"));
            return;
        }

        Material matchedMaterial = findMaterialFromConfig(message);
        if (matchedMaterial == null) {
            player.sendMessage(ESGSearch.getMessage("messages.not-found").replace("{input}", message));
            player.sendMessage(ESGSearch.getMessage("messages.chat-prompt"));
            return;
        }

        if (EconomyShopGUIHook.getShopItem(new ItemStack(matchedMaterial)) == null) {
            player.sendMessage(ESGSearch.getMessage("messages.not-in-shop").replace("{item}", matchedMaterial.name()));
            player.sendMessage(ESGSearch.getMessage("messages.chat-prompt"));
            return;
        }

        searchingPlayers.remove(player.getUniqueId());
        ESGSearch.getInstance().getServer().getScheduler().runTask(ESGSearch.getInstance(), () -> {
            SearchGUI.openSearchGUI(player, matchedMaterial);
        });
    }

    private Material findMaterialFromConfig(String input) {
        ConfigurationSection section = ESGSearch.getInstance().getConfig().getConfigurationSection("items");
        if (section == null) return null;
        for (String key : section.getKeys(false)) {
            String formatsRaw = section.getString(key + ".formats");
            if (formatsRaw == null) continue;
            for (String format : formatsRaw.split(",")) {
                if (format.trim().equalsIgnoreCase(input)) return Material.matchMaterial(key);
            }
        }
        return null;
    }
}
