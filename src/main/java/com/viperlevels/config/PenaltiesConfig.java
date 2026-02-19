package com.viperlevels.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PenaltiesConfig {

    private final Map<String, PenaltySettings> penalties;

    public PenaltiesConfig(FileConfiguration config) {
        this.penalties = new HashMap<>();
        
        ConfigurationSection penaltiesSection = config.getConfigurationSection("penalties");
        if (penaltiesSection == null) {
            loadDefaults();
            return;
        }

        for (String key : penaltiesSection.getKeys(false)) {
            ConfigurationSection penaltySection = penaltiesSection.getConfigurationSection(key);
            if (penaltySection != null) {
                boolean cancelEvent = penaltySection.getBoolean("cancel-event", true);
                boolean damageItem = penaltySection.getBoolean("damage-item", false);
                int damageAmount = penaltySection.getInt("damage-amount", 0);
                
                penalties.put(key, new PenaltySettings(cancelEvent, damageItem, damageAmount));
            }
        }
    }

    private void loadDefaults() {
        penalties.put("item-use", new PenaltySettings(true, false, 1));
        penalties.put("block-break", new PenaltySettings(true, true, 5));
        penalties.put("block-place", new PenaltySettings(true, false, 0));
        penalties.put("craft", new PenaltySettings(true, false, 0));
        penalties.put("armor-equip", new PenaltySettings(true, false, 0));
        penalties.put("enchant", new PenaltySettings(true, false, 0));
        penalties.put("consume", new PenaltySettings(true, false, 0));
        penalties.put("mob-hit", new PenaltySettings(true, true, 2));
        penalties.put("dimension", new PenaltySettings(true, false, 0));
    }

    public PenaltySettings getPenalty(String type) {
        return penalties.getOrDefault(type, new PenaltySettings(true, false, 0));
    }

    @Data
    @AllArgsConstructor
    public static class PenaltySettings {
        private boolean cancelEvent;
        private boolean damageItem;
        private int damageAmount;
    }
}