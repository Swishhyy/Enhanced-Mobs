package me.swishhyy.enhancedMobs.commands;

import me.swishhyy.enhancedMobs.EnhancedMobs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnhancedMobsCommand implements CommandExecutor, TabExecutor {

    private final EnhancedMobs plugin;

    public EnhancedMobsCommand(EnhancedMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 0) {
            // No subcommands provided, show usage or help
            sender.sendMessage(plugin.getMessage("commands.enhancedmobs.usage",
                    "{prefix}Use /enhancedmobs reload <#All|#Config|#Messages> to reload files."));
            return true;
        }

        // Check if the subcommand is reload
        if (args[0].equalsIgnoreCase("reload")) {
            // Check permissions
            if (!sender.hasPermission("enhancedmobs.command.admin")) {
                sender.sendMessage(plugin.getMessage("commands.enhancedmobs.no_permission",
                        "{prefix}You do not have permission to use this command!"));
                return true;
            }

            // Check the file argument
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload_usage",
                        "{prefix}Usage: /enhancedmobs reload <#All|#Config|#Messages>"));
                return true;
            }

            String target = args[1].toLowerCase();

            switch (target) {
                case "#all":
                    plugin.reloadConfig();
                    plugin.reloadMessages();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.all",
                            "{prefix}All configurations and messages have been reloaded!"));
                    break;

                case "#config":
                    plugin.reloadConfig();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.config",
                            "{prefix}Configuration file has been reloaded!"));
                    break;

                case "#messages":
                    plugin.reloadMessages();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.messages",
                            "{prefix}Messages file has been reloaded!"));
                    break;

                default:
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.unknown_reload_target",
                            "{prefix}Unknown file target! Use <#All|#Config|#Messages>."));
                    break;
            }
            return true;
        }

        // If no valid subcommand is provided
        sender.sendMessage(plugin.getMessage("commands.enhancedmobs.unknown_subcommand",
                "{prefix}Unknown subcommand! Usage: /enhancedmobs reload <#All|#Config|#Messages>"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        // Provide suggestions for the first argument
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Provide suggestions for the second argument when the first argument is "reload"
        if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            return Stream.of("#All", "#Config", "#Messages")
                    .filter(option -> option.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Return an empty list for other cases
        return Collections.emptyList();
    }
}
