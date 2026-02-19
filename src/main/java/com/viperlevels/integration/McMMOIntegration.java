package com.viperlevels.integration;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.viperlevels.ViperLevels;
import com.viperlevels.condition.McMMOSkill;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class McMMOIntegration {

    private static McMMOIntegration instance;

    private final ViperLevels plugin;
    private final boolean enabled;

    private McMMOIntegration() {
        this.plugin = ViperLevels.getInstance();
        this.enabled = plugin.isMcMMOEnabled();
        
        if (enabled) {
            plugin.logInfo("McMMO integration enabled");
        }
    }

    public static McMMOIntegration getInstance() {
        if (instance == null) {
            instance = new McMMOIntegration();
        }
        return instance;
    }

    public int getSkillLevel(Player player, McMMOSkill skill) {
        if (!enabled) {
            return 0;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return 0;
        }

        PrimarySkillType skillType = convertSkill(skill);
        if (skillType == null) {
            return 0;
        }

        return mcMMOPlayer.getSkillLevel(skillType);
    }

    public int getSkillLevelOffline(OfflinePlayer player, McMMOSkill skill) {
        if (!enabled) {
            return 0;
        }

        try {
            return ExperienceAPI.getLevel(player.getPlayer(), skill.getMcmmoName());
        } catch (Exception e) {
            plugin.logDebug("Failed to get offline skill level: " + e.getMessage());
            return 0;
        }
    }

    public Map<McMMOSkill, Integer> getAllSkillLevels(Player player) {
        Map<McMMOSkill, Integer> levels = new HashMap<>();
        
        if (!enabled) {
            return levels;
        }

        for (McMMOSkill skill : McMMOSkill.values()) {
            levels.put(skill, getSkillLevel(player, skill));
        }

        return levels;
    }

    public int getPowerLevel(Player player) {
        if (!enabled) {
            return 0;
        }

        return ExperienceAPI.getPowerLevel(player);
    }

    public boolean hasSkillLevel(Player player, McMMOSkill skill, int requiredLevel) {
        return getSkillLevel(player, skill) >= requiredLevel;
    }

    private PrimarySkillType convertSkill(McMMOSkill skill) {
        try {
            return PrimarySkillType.valueOf(skill.name());
        } catch (IllegalArgumentException e) {
            plugin.logWarning("Unknown mcMMO skill: " + skill.name());
            return null;
        }
    }
}