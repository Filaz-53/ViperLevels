package com.viperlevels.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private final CommandManager commandManager;

    public TabCompleter(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(commandManager.getCommands().keySet(), args[0]);
        }

        if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "check":
                    if (args.length == 2) {
                        return getPlayerNames(args[1]);
                    }
                    break;

                case "bypass":
                    if (args.length == 2) {
                        return filter(Arrays.asList("add", "remove", "list"), args[1]);
                    }
                    if (args.length == 3) {
                        return getPlayerNames(args[2]);
                    }
                    if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
                        return filter(Arrays.asList("ALL", "BLOCKS", "ITEMS", "ARMOR", "ENCHANTING", "POTIONS", "FOOD", "MOBS", "DIMENSIONS"), args[3]);
               }
                    if (args.length == 5 && args[1].equalsIgnoreCase("add")) {
                        return filter(Arrays.asList("1h", "1d", "7d", "30d", "permanent"), args[4]);
                    }
                    break;

                case "gui":
                    if (args.length == 2 && sender.hasPermission("viperlevels.gui.others")) {
                        return getPlayerNames(args[1]);
                    }
                    break;
            }
        }

        return new ArrayList<>();
    }

    private List<String> filter(Iterable<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerPrefix)) {
                result.add(option);
            }
        }
        
        return result;
    }

    private List<String> getPlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}