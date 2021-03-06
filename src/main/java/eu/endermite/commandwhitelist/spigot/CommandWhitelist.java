package eu.endermite.commandwhitelist.spigot;

import eu.endermite.commandwhitelist.spigot.command.MainCommand;
import eu.endermite.commandwhitelist.spigot.config.ConfigCache;
import eu.endermite.commandwhitelist.spigot.listeners.LegacyPlayerTabChatCompleteListener;
import eu.endermite.commandwhitelist.spigot.listeners.PlayerCommandPreProcessListener;
import eu.endermite.commandwhitelist.spigot.listeners.PlayerCommandSendListener;
import eu.endermite.commandwhitelist.spigot.listeners.TabCompleteBlockerListener;
import eu.endermite.commandwhitelist.spigot.metrics.BukkitMetrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandWhitelist extends JavaPlugin {

    private static CommandWhitelist commandWhitelist;
    private static ConfigCache configCache;
    private static boolean isLegacy;

    @Override
    public void onEnable() {

        commandWhitelist = this;

        isLegacy = checkLegacy();

        reloadPluginConfig();

        getServer().getPluginManager().registerEvents(new PlayerCommandPreProcessListener(), this);
        if (!isLegacy) {
            getServer().getPluginManager().registerEvents(new PlayerCommandSendListener(), this);
        } else {
            getLogger().info(ChatColor.AQUA+"Running in legacy mode...");
            if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
                LegacyPlayerTabChatCompleteListener.protocol(this);
            } else {
                getLogger().info(ChatColor.YELLOW+"ProtocolLib is required for tab completion blocking!");
            }
        }

        getServer().getPluginManager().registerEvents(new TabCompleteBlockerListener(), this);

        getCommand("commandwhitelist").setExecutor(new MainCommand());
        getCommand("commandwhitelist").setTabCompleter(new MainCommand());

        int pluginId = 8705;
        new BukkitMetrics(this, pluginId);
    }

    public void reloadPluginConfig() {
        saveDefaultConfig();
        reloadConfig();
        configCache = new ConfigCache(getConfig());
    }

    public void reloadPluginConfig(CommandSender sender) {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            reloadPluginConfig();
            if (!isLegacy()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.updateCommands();
                }
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandWhitelist.getConfigCache().getPrefix() + CommandWhitelist.getConfigCache().getConfigReloaded()));
        });
    }

    public boolean isLegacy() {
        return isLegacy;
    }

    private boolean checkLegacy() {

        String version = Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit", "").replace(".", "");

        if (version.contains("v1_8_")) {
            return true;
        } else if (version.contains("v1_9_")) {
            return true;
        } else if (version.contains("v1_10_")) {
            return true;
        } else if (version.contains("v1_11_")) {
            return true;
        } else if (version.contains("v1_12_")) {
            return true;
        }

        return false;
    }

    public static CommandWhitelist getPlugin() {return commandWhitelist;}
    public static ConfigCache getConfigCache() {return configCache;}
}
