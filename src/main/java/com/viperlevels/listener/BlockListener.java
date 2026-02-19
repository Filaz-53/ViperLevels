package com.viperlevels.listener;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.config.MessagesConfig;
import com.viperlevels.config.PenaltiesConfig;
import com.viperlevels.config.PlaceholderReplacer;
import com.viperlevels.rule.ActionType;
import com.viperlevels.rule.RuleType;
import com.viperlevels.rule.RuleValidator;
import com.viperlevels.rule.ValidationResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BlockListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public BlockListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(event.getBlock().getType()).name();

        ValidationResult result = validator.validate(player, materialName, RuleType.BLOCK, ActionType.BREAK);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("block-break");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            if (penalty.isDamageItem() && penalty.getDamageAmount() > 0) {
                ItemStack tool = player.getInventory().getItemInMainHand();
                if (tool != null && tool.getType().getMaxDurability() > 0) {
                    short newDurability = (short) (tool.getDurability() + penalty.getDamageAmount());
                    if (newDurability >= tool.getType().getMaxDurability()) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        tool.setDurability(newDurability);
                    }
                }
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(materialName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.block-break"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(event.getBlock().getType()).name();

        ValidationResult result = validator.validate(player, materialName, RuleType.BLOCK, ActionType.PLACE);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("block-place");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(materialName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.block-place"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }
}