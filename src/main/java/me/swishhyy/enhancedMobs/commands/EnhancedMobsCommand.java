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
            sender.sendMessage(plugin.getMessage("commands.enhancedmobs.usage",
                    "{prefix}Use /enhancedmobs reload <#All|#Config|#Messages|#Mobs|#Bosses> to reload files."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("enhancedmobs.command.admin")) {
                sender.sendMessage(plugin.getMessage("commands.enhancedmobs.no_permission",
                        "{prefix}You do not have permission to use this command!"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload_usage",
                        "{prefix}Usage: /enhancedmobs reload <#All|#Config|#Messages|#Mobs|#Bosses>"));
                return true;
            }

            String target = args[1].toLowerCase();

            switch (target) {
                case "#all":
                    plugin.reloadConfig();
                    plugin.reloadMessages();
                    plugin.reloadMobsConfig();
                    plugin.reloadBossesConfig();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.all",
                            "{prefix}All configurations, mobs, and bosses files have been reloaded!"));
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

                case "#mobs":
                    plugin.reloadMobsConfig();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.mobs",
                            "{prefix}Mobs configuration file has been reloaded!"));
                    break;

                case "#bosses":
                    plugin.reloadBossesConfig();
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.reload.bosses",
                            "{prefix}Bosses configuration file has been reloaded!"));
                    break;

                default:
                    sender.sendMessage(plugin.getMessage("commands.enhancedmobs.unknown_reload_target",
                            "{prefix}Unknown file target! Use <#All|#Config|#Messages|#Mobs|#Bosses>."));
                    break;
            }
            return true;
        }

        sender.sendMessage(plugin.getMessage("commands.enhancedmobs.unknown_subcommand",
                "{prefix}Unknown subcommand! Usage: /enhancedmobs reload <#All|#Config|#Messages|#Mobs|#Bosses>."));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            return Stream.of("#All", "#Config", "#Messages", "#Mobs", "#Bosses")
                    .filter(option -> option.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
