package cz.yourname.esgsearch;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ESGSearch extends JavaPlugin {

    private static ESGSearch instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Nenalezen plugin Vault nebo poskytovatel ekonomiky! Plugin se vypina.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        SearchCommand searchCommand = new SearchCommand();
        if (getCommand("search") != null) {
            getCommand("search").setExecutor(searchCommand);
            getCommand("search").setTabCompleter(searchCommand);
        }

        getServer().getPluginManager().registerEvents(new SearchGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getLogger().info("ESGSearch plugin byl uspesne aktivovan!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static ESGSearch getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }

    public static String getMessage(String path) {
        String msg = instance.getConfig().getString(path, "");
        String prefix = instance.getConfig().getString("messages.prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public static String getRawMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString(path, ""));
    }
}
