package com.viperlevels.condition;

import lombok.Getter;

@Getter
public enum McMMOSkill {
    ACROBATICS("Acrobatics"),
    ALCHEMY("Alchemy"),
    ARCHERY("Archery"),
    AXES("Axes"),
    EXCAVATION("Excavation"),
    FISHING("Fishing"),
    HERBALISM("Herbalism"),
    MINING("Mining"),
    REPAIR("Repair"),
    SALVAGE("Salvage"),
    SMELTING("Smelting"),
    SWORDS("Swords"),
    TAMING("Taming"),
    UNARMED("Unarmed"),
    WOODCUTTING("Woodcutting"),
    CROSSBOWS("Crossbows"),
    TRIDENTS("Tridents");

    private final String mcmmoName;

    McMMOSkill(String mcmmoName) {
        this.mcmmoName = mcmmoName;
    }

    public static McMMOSkill fromString(String name) {
        for (McMMOSkill skill : values()) {
            if (skill.name().equalsIgnoreCase(name) || skill.mcmmoName.equalsIgnoreCase(name)) {
                return skill;
            }
        }
        return null;
    }
}