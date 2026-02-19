package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.ViperCommand;
import com.viperlevels.rule.Rule;
import com.viperlevels.rule.RuleManager;
import com.viperlevels.rule.RuleType;
import org.bukkit.command.CommandSender;

public class InfoCommand extends ViperCommand {

    private final RuleManager ruleManager;

    public InfoCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.admin.info");
        this.ruleManager = RuleManager.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUso: /viperlevels info <material>");
            return;
        }

        String material = args[0].toUpperCase();

        sender.sendMessage("§8§m----------§r §cInfo Regola §8§m----------");
        sender.sendMessage("§7Materiale: §e" + material);
        sender.sendMessage("");

        boolean foundAny = false;

        for (RuleType type : RuleType.values()) {
            Rule rule = ruleManager.findRuleForMaterial(material, type);
            if (rule != null) {
                foundAny = true;
                sender.sendMessage("§e" + type.name() + ":");
                sender.sendMessage("  §7Azioni: §e" + rule.getActions());
                sender.sendMessage("  §7Requisiti: §e" + rule.getCondition().toString());
            }
        }

        if (!foundAny) {
            sender.sendMessage("§cNessuna regola trovata per: §e" + material);
        }

        sender.sendMessage("§8§m--------------------------------");
    }
}