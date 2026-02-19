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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CraftListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public CraftListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(result).name();

        ValidationResult validationResult = validator.validate(player, materialName, RuleType.ITEM, ActionType.CRAFT);

        if (!validationResult.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("craft");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(materialName),
                    PlaceholderReplacer.withRequirements(validationResult.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.craft"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }
}