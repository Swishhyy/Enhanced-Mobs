package me.swishhyy.enhancedMobs;

import me.swishhyy.enhancedMobs.commands.EnhancedMobsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class EnhancedMobs extends JavaPlugin {

    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // Save default configuration
        saveDefaultConfig();
        loadMessages();

        getLogger().info(getMessage("plugin.enabled", "{plugin_name} has been enabled!"));

        // Register the "/enhancedmobs" command
        Objects.requireNonNull(getCommand("enhancedmobs")).setExecutor(new EnhancedMobsCommand(this));
    }

    @Override
    public void onDisable() {
        // Send a message to the console
        getLogger().info(getMessage("plugin.disabled", "{plugin_name} has been disabled!"));
        getServer().getConsoleSender().sendMessage(getMessage("plugin.unloaded", "§4§l{plugin_name} has unloaded successfully."));
    }

    private void loadMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key, String defaultMessage) {
        String message = messagesConfig.getString(key, defaultMessage);
        String prefix = messagesConfig.getString("prefix", "§5§l[Enhanced§cMobs] §e");
        return message.replace("{prefix}", prefix).replace("{plugin_name}", getFormattedPluginName());
    }

    public static String getFormattedPluginName() {
        return "§5§lEnhanced§cMobs";
    }

    public void reloadMessages() {
        try {
            messagesConfig.load(messagesFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            getLogger().severe("Failed to reload messages.yml!");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (label.equalsIgnoreCase("reloadconfig")) {
            this.reloadConfig();
            sender.sendMessage(getMessage("commands.reload", "Configuration reloaded successfully!"));
            return true;
        }
        return false;
    }
}
