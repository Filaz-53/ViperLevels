package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.ViperCommand;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends ViperCommand {

    public ReloadCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.admin.reload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("reload-success").replace("§a", "§e") + " §7Reloading...");

        plugin.getConfigManager().reload();
        plugin.getRuleManager().loadRules();
        plugin.getCacheManager().invalidateAll();

        sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("reload-success"));
    }
}