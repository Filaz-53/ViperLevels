package com.viperlevels.listener;

import com.viperlevels.ViperLevels;
import com.viperlevels.config.MessagesConfig;
import com.viperlevels.config.PenaltiesConfig;
import com.viperlevels.config.PlaceholderReplacer;
import com.viperlevels.rule.ActionType;
import com.viperlevels.rule.RuleType;
import com.viperlevels.rule.RuleValidator;
import com.viperlevels.rule.ValidationResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CombatListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public CombatListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        Entity target = event.getEntity();
        String mobType = target.getType().name();

        ValidationResult result = validator.validate(player, mobType, RuleType.MOB, ActionType.HIT);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("mob-hit");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            if (penalty.isDamageItem() && penalty.getDamageAmount() > 0) {
                ItemStack weapon = player.getInventory().getItemInMainHand();
                if (weapon != null && weapon.getType().getMaxDurability() > 0) {
                    short newDurability = (short) (weapon.getDurability() + penalty.getDamageAmount());
                    if (newDurability >= weapon.getType().getMaxDurability()) {
                        player.getInventory().setItemInMainHand(null);
                    } else {
                        weapon.setDurability(newDurability);
                    }
                }
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(mobType),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.mob-hit"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }
}