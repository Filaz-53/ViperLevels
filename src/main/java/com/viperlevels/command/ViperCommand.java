package com.viperlevels.command;

import com.viperlevels.ViperLevels;
import lombok.Getter;
import org.bukkit.command.CommandSender;

@Getter
public abstract class ViperCommand {

    protected final ViperLevels plugin;
    protected final String permission;

    public ViperCommand(ViperLevels plugin, String permission) {
        this.plugin = plugin;
        this.permission = permission;
    }

    public abstract void execute(CommandSender sender, String[] args);

    public boolean hasPermission(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }
}