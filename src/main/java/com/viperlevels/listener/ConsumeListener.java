package com.viperlevels.listener;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

public class ConsumeListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public ConsumeListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        String materialName = XMaterial.matchXMaterial(item).name();

        RuleType ruleType = RuleType.FOOD;
        String checkName = materialName;

        if (item.getItemMeta() instanceof PotionMeta) {
            ruleType = RuleType.POTION;
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta.hasCustomEffects()) {
                PotionEffect effect = meta.getCustomEffects().get(0);
                checkName = effect.getType().getName().toUpperCase();
            }
        }

        ValidationResult result = validator.validate(player, checkName, ruleType, ActionType.CONSUME);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("consume");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(checkName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.consume"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }
}