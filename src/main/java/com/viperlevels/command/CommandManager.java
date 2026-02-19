package com.viperlevels.command;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.impl.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final ViperLevels plugin;
    private final Map<String, ViperCommand> commands;
    private final TabCompleter tabCompleter;

    public CommandManager(ViperLevels plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.tabCompleter = new TabCompleter(this);

        registerCommands();
        
        plugin.getCommand("viperlevels").setExecutor(this);
        plugin.getCommand("viperlevels").setTabCompleter(tabCompleter);
    }

    private void registerCommands() {
        commands.put("reload", new ReloadCommand(plugin));
        commands.put("check", new CheckCommand(plugin));
        commands.put("info", new InfoCommand(plugin));
        commands.put("bypass", new BypassCommand(plugin));
        commands.put("gui", new GuiCommand(plugin));
        commands.put("stats", new StatsCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        ViperCommand viperCommand = commands.get(subCommand);

        if (viperCommand == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        if (!viperCommand.hasPermission(sender)) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);

        viperCommand.execute(sender, subArgs);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------§r §cViperLevels Commands §8§m----------");
        sender.sendMessage("§e/viperlevels reload §7- Ricarica la configurazione");
        sender.sendMessage("§e/viperlevels check <player> <material> §7- Verifica requisiti");
        sender.sendMessage("§e/viperlevels info <material> §7- Info su una regola");
        sender.sendMessage("§e/viperlevels bypass <player> <target> [duration] §7- Gestisci bypass");
        sender.sendMessage("§e/viperlevels gui [player] §7- Apri GUI");
        sender.sendMessage("§e/viperlevels stats §7- Statistiche plugin");
        sender.sendMessage("§8§m--------------------------------");
    }

    public Map<String, ViperCommand> getCommands() {
        return commands;
    }
}