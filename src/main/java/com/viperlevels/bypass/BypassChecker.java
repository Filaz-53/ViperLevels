package com.viperlevels.bypass;

import com.viperlevels.rule.RuleType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BypassChecker {

    private final BypassManager bypassManager;

    public BypassChecker() {
        this.bypassManager = BypassManager.getInstance();
    }

    public boolean hasPermissionBypass(Player player) {
        return player.hasPermission("viperlevels.bypass.all");
    }

    public boolean hasBypass(Player player, String materialIdentifier, RuleType ruleType) {
        if (hasPermissionBypass(player)) {
            return true;
        }

        return bypassManager.hasActiveBypass(player.getUniqueId(), materialIdentifier, ruleType);
    }

    public boolean hasCategoryBypass(Player player, RuleType ruleType) {
        if (hasPermissionBypass(player)) {
            return true;
        }

        BypassType bypassType = convertRuleTypeToBypassType(ruleType);
        return bypassManager.hasCategoryBypass(player.getUniqueId(), bypassType);
    }

    public boolean hasBypassForUUID(UUID playerUuid, String materialIdentifier, RuleType ruleType) {
        return bypassManager.hasActiveBypass(playerUuid, materialIdentifier, ruleType);
    }

    private BypassType convertRuleTypeToBypassType(RuleType ruleType) {
        switch (ruleType) {
            case BLOCK:
                return BypassType.BLOCKS;
            case ITEM:
                return BypassType.ITEMS;
            case ARMOR:
                return BypassType.ARMOR;
            case ENCHANTING:
                return BypassType.ENCHANTING;
            case POTION:
                return BypassType.POTIONS;
            case FOOD:
                return BypassType.FOOD;
            case MOB:
                return BypassType.MOBS;
            case DIMENSION:
                return BypassType.DIMENSIONS;
            default:
                return BypassType.ITEMS;
        }
    }
}