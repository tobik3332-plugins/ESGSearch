package cz.yourname.esgsearch;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ESGSearch.getMessage("messages.only-player"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ESGSearch.getMessage("messages.usage"));
            return true;
        }

        String rawInput = String.join(" ", args).trim();
        Material matchedMaterial = findMaterialFromConfig(rawInput);

        if (matchedMaterial == null) {
            String msg = ESGSearch.getMessage("messages.not-found").replace("{input}", rawInput);
            player.sendMessage(msg);
            return true;
        }

        ItemStack itemStack = new ItemStack(matchedMaterial);
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(itemStack);

        if (shopItem == null) {
            String msg = ESGSearch.getMessage("messages.not-in-shop").replace("{item}", matchedMaterial.name());
            player.sendMessage(msg);
            return true;
        }

        SearchGUI.openSearchGUI(player, shopItem);
        return true;
    }

    private Material findMaterialFromConfig(String input) {
        ConfigurationSection section = ESGSearch.getInstance().getConfig().getConfigurationSection("items");
        if (section == null) return null;

        for (String key : section.getKeys(false)) {
            String formatsRaw = section.getString(key + ".formats");
            if (formatsRaw == null) continue;

            String[] formats = formatsRaw.split(",");
            for (String format : formats) {
                if (format.trim().equalsIgnoreCase(input)) {
                    return Material.matchMaterial(key);
                }
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            ConfigurationSection section = ESGSearch.getInstance().getConfig().getConfigurationSection("items");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String formatsRaw = section.getString(key + ".formats");
                    if (formatsRaw != null) {
                        String[] formats = formatsRaw.split(",");
                        for (String f : formats) {
                            if (f.trim().toLowerCase().startsWith(args[0].toLowerCase())) {
                                completions.add(f.trim());
                            }
                        }
                    }
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
