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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ArmorListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public ArmorListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        ItemStack item = event.getCursor();
        if (item == null || item.getType().isAir()) {
            item = event.getCurrentItem();
        }

        if (item == null || item.getType().isAir()) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(item).name();

        if (!isArmorPiece(materialName)) {
            return;
        }

        ValidationResult result = validator.validate(player, materialName, RuleType.ARMOR, ActionType.EQUIP);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("armor-equip");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(materialName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.armor-equip"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }

    private boolean isArmorPiece(String materialName) {
        return materialName.endsWith("_HELMET") ||
               materialName.endsWith("_CHESTPLATE") ||
               materialName.endsWith("_LEGGINGS") ||
               materialName.endsWith("_BOOTS");
    }
}