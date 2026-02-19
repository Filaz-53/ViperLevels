package com.viperlevels.penalty;

import com.cryptomorin.xseries.XSound;
import com.viperlevels.ViperLevels;
import com.viperlevels.config.PenaltiesConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class PenaltyApplier {

    private final ViperLevels plugin;
    private final PenaltiesConfig penalties;

    public PenaltyApplier() {
        this.plugin = ViperLevels.getInstance();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    public void apply(Player player, PenaltyType type, Cancellable event, ItemStack itemInHand) {
        PenaltiesConfig.PenaltySettings settings = penalties.getPenalty(type.getConfigKey());

        if (settings.isCancelEvent() && event != null) {
            event.setCancelled(true);
        }

        if (settings.isDamageItem() && settings.getDamageAmount() > 0 && itemInHand != null) {
            damageItem(player, itemInHand, settings.getDamageAmount());
        }

        playFeedback(player);
    }

    public void applyWithoutItem(Player player, PenaltyType type, Cancellable event) {
        apply(player, type, event, null);
    }

    private void damageItem(Player player, ItemStack item, int damageAmount) {
        if (item == null || item.getType().getMaxDurability() == 0) {
            return;
        }

        short currentDurability = item.getDurability();
        short maxDurability = item.getType().getMaxDurability();
        short newDurability = (short) (currentDurability + damageAmount);

        if (newDurability >= maxDurability) {
            player.getInventory().remove(item);
            XSound.matchXSound("ENTITY_ITEM_BREAK").ifPresent(sound -> sound.play(player));
            plugin.logDebug("Item broke for player " + player.getName() + " due to penalty");
        } else {
            item.setDurability(newDurability);
        }
    }

    private void playFeedback(Player player) {
        XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(sound -> sound.play(player, 1.0f, 1.0f));
    }

    public boolean shouldCancel(PenaltyType type) {
        return penalties.getPenalty(type.getConfigKey()).isCancelEvent();
    }

    public boolean shouldDamageItem(PenaltyType type) {
        PenaltiesConfig.PenaltySettings settings = penalties.getPenalty(type.getConfigKey());
        return settings.isDamageItem() && settings.getDamageAmount() > 0;
    }

    public int getDamageAmount(PenaltyType type) {
        return penalties.getPenalty(type.getConfigKey()).getDamageAmount();
    }
}