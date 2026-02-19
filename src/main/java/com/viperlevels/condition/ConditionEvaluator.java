package com.viperlevels.condition;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.viperlevels.ViperLevels;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ConditionEvaluator {

    public static boolean evaluate(Player player, Condition condition) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            ViperLevels.getInstance().logWarning("McMMOPlayer not found for: " + player.getName());
            return false;
        }

        List<SkillRequirement> requirements = condition.getRequirements();
        Condition.ConditionOperator operator = condition.getOperator();

        if (operator == Condition.ConditionOperator.AND) {
            for (SkillRequirement req : requirements) {
                if (!checkRequirement(mcMMOPlayer, req)) {
                    return false;
                }
            }
            return true;
        } else {
            for (SkillRequirement req : requirements) {
                if (checkRequirement(mcMMOPlayer, req)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean checkRequirement(McMMOPlayer mcMMOPlayer, SkillRequirement requirement) {
        PrimarySkillType skillType = convertSkill(requirement.getSkill());
        if (skillType == null) {
            return false;
        }

        int playerLevel = mcMMOPlayer.getSkillLevel(skillType);
        return playerLevel >= requirement.getRequiredLevel();
    }

    public static List<SkillRequirement> getMissingRequirements(Player player, Condition condition) {
        List<SkillRequirement> missing = new ArrayList<>();

        if (condition == null || condition.isEmpty()) {
            return missing;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return condition.getRequirements();
        }

        for (SkillRequirement req : condition.getRequirements()) {
            if (!checkRequirement(mcMMOPlayer, req)) {
                missing.add(req);
            }
        }

        return missing;
    }

    public static int getPlayerSkillLevel(Player player, McMMOSkill skill) {
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

    private static PrimarySkillType convertSkill(McMMOSkill skill) {
        try {
            return PrimarySkillType.valueOf(skill.name());
        } catch (IllegalArgumentException e) {
            ViperLevels.getInstance().logWarning("Unknown mcMMO skill: " + skill.name());
            return null;
        }
    }
}