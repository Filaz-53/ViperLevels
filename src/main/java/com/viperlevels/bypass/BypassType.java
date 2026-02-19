package com.viperlevels.bypass;

public enum BypassType {
    ALL,
    BLOCKS,
    ITEMS,
    ARMOR,
    ENCHANTING,
    POTIONS,
    FOOD,
    MOBS,
    DIMENSIONS,
    SPECIFIC_MATERIAL;

    public static BypassType fromString(String name) {
        for (BypassType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public boolean isCategory() {
        return this != SPECIFIC_MATERIAL;
    }

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
    }
}