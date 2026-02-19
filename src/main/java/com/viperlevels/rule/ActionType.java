package com.viperlevels.rule;

public enum ActionType {
    BREAK,
    PLACE,
    CRAFT,
    USE,
    EQUIP,
    ENCHANT,
    CONSUME,
    HIT;

    public static ActionType fromString(String name) {
        for (ActionType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}