package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.ViperCommand;
import com.viperlevels.rule.ActionType;
import com.viperlevels.rule.RuleType;
import com.viperlevels.rule.RuleValidator;
import com.viperlevels.rule.ValidationResult;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckCommand extends ViperCommand {

    private final RuleValidator validator;

    public CheckCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.admin.check");
        this.validator = new RuleValidator();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /viperlevels check <player> <material>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("player-not-found"));
            return;
        }

        String material = args[1].toUpperCase();

        sender.sendMessage("§8§m----------§r §cControllo Requisiti §8§m----------");
        sender.sendMessage("§7Giocatore: §e" + target.getName());
        sender.sendMessage("§7Materiale: §e" + material);
        sender.sendMessage("");

        ValidationResult itemResult = validator.validate(target, material, RuleType.ITEM, ActionType.USE);
        sendResult(sender, "ITEM (USE)", itemResult);

        ValidationResult blockResult = validator.validate(target, material, RuleType.BLOCK, ActionType.BREAK);
        sendResult(sender, "BLOCK (BREAK)", blockResult);

        ValidationResult armorResult = validator.validate(target, material, RuleType.ARMOR, ActionType.EQUIP);
        sendResult(sender, "ARMOR (EQUIP)", armorResult);

        sender.sendMessage("§8§m--------------------------------");
    }

    private void sendResult(CommandSender sender, String category, ValidationResult result) {
        if (result.isPassed()) {
            sender.sendMessage("§a✓ " + category + " §7- Requisiti soddisfatti");
        } else if (result.hasMissingRequirements()) {
            sender.sendMessage("§c✗ " + category + " §7- Mancanti: §e" + result.formatMissingRequirements());
        }
    }
}