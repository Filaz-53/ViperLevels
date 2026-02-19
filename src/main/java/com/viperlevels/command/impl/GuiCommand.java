package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.ViperCommand;
import com.viperlevels.inventory.gui.GUIManager;
import com.viperlevels.inventory.impl.MainMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuiCommand extends ViperCommand {

    private final GUIManager guiManager;

    public GuiCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.gui");
        this.guiManager = GUIManager.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 0) {
            if (!sender.hasPermission("viperlevels.gui.others")) {
                sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
                return;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("player-not-found"));
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cDevi essere un giocatore per usare questo comando!");
                return;
            }
            target = (Player) sender;
        }

        MainMenuGUI gui = new MainMenuGUI(target);
        guiManager.openGUI(target, gui);

        if (target != sender) {
            sender.sendMessage("§aGUI aperta per §e" + target.getName());
        }
    }
}