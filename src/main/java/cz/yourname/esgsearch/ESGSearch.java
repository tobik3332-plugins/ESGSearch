package cz.yourname.esgsearch;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class ESGSearch extends JavaPlugin {

    private static ESGSearch instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        SearchCommand searchCommand = new SearchCommand();
        if (getCommand("search") != null) {
            getCommand("search").setExecutor(searchCommand);
            getCommand("search").setTabCompleter(searchCommand);
        }

        getServer().getPluginManager().registerEvents(new SearchGUI(), this);
        getLogger().info("ESGSearch plugin byl uspesne aktivovan!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ESGSearch plugin byl vypnut.");
    }

    public static ESGSearch getInstance() {
        return instance;
    }

    public static String getMessage(String path) {
        String msg = instance.getConfig().getString(path, "");
        String prefix = instance.getConfig().getString("messages.prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public static String getRawMessage(String path) {
        String msg = instance.getConfig().getString(path, "");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
