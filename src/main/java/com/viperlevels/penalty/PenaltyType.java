package com.viperlevels.penalty;

public enum PenaltyType {
    ITEM_USE("item-use"),
    BLOCK_BREAK("block-break"),
    BLOCK_PLACE("block-place"),
    CRAFT("craft"),
    ARMOR_EQUIP("armor-equip"),
    ENCHANT("enchant"),
    CONSUME("consume"),
    MOB_HIT("mob-hit"),
    DIMENSION("dimension");

    private final String configKey;

    PenaltyType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static PenaltyType fromActionType(com.viperlevels.rule.ActionType actionType) {
        switch (actionType) {
            case USE:
                return ITEM_USE;
            case BREAK:
                return BLOCK_BREAK;
            case PLACE:
                return BLOCK_PLACE;
            case CRAFT:
                return CRAFT;
            case EQUIP:
                return ARMOR_EQUIP;
            case ENCHANT:
                return ENCHANT;
            case CONSUME:
                return CONSUME;
            case HIT:
                return MOB_HIT;
            default:
                return ITEM_USE;
        }
    }
}