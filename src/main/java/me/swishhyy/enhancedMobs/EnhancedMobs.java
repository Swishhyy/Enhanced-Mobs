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

        // Check and generate required files
        checkRequiredFiles();

        // Register the "/enhancedmobs" command
        Objects.requireNonNull(getCommand("enhancedmobs")).setExecutor(new EnhancedMobsCommand(this));

        getLogger().info(getMessage("plugin.enabled", "{plugin_name} has been enabled!"));
    }

    @Override
    public void onDisable() {
        // Send a message to the console
        getLogger().info(getMessage("plugin.disabled", "{plugin_name} has been disabled!"));
        getServer().getConsoleSender().sendMessage(getMessage("plugin.unloaded", "§4§l{plugin_name} has unloaded successfully."));
    }

    private void checkRequiredFiles() {
        // Define the files to check and their default content in resources
        String[] filesToCheck = {"mobs.yml", "config.yml", "bosses.yml", "messages.yml"};

        for (String fileName : filesToCheck) {
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                // Generate the file with default content if it exists in resources
                saveResource(fileName, false);
                getLogger().info(fileName + " was not found and has been generated with default content.");
            } else {
                // Log success for each file that exists
                getLogger().info(fileName + " loaded successfully.");
            }
        }
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
