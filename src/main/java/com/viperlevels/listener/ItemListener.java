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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public ItemListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(item).name();

        ValidationResult result = validator.validate(player, materialName, RuleType.ITEM, ActionType.USE);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("item-use");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            if (penalty.isDamageItem() && penalty.getDamageAmount() > 0) {
                if (item.getType().getMaxDurability() > 0) {
                    short newDurability = (short) (item.getDurability() + penalty.getDamageAmount());
                    if (newDurability >= item.getType().getMaxDurability()) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        item.setDurability(newDurability);
                    }
                }
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(materialName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.item-use"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }
}