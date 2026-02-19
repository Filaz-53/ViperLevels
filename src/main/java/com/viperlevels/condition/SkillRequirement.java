package com.viperlevels.condition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillRequirement {
    private McMMOSkill skill;
    private int requiredLevel;

    public String toString() {
        return skill.name() + "(" + requiredLevel + ")";
    }

    public static SkillRequirement parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        int openParen = input.indexOf('(');
        int closeParen = input.indexOf(')');

        if (openParen == -1 || closeParen == -1 || closeParen < openParen) {
            return null;
        }

        String skillName = input.substring(0, openParen).trim();
        String levelStr = input.substring(openParen + 1, closeParen).trim();

        McMMOSkill skill = McMMOSkill.fromString(skillName);
        if (skill == null) {
            return null;
        }

        try {
            int level = Integer.parseInt(levelStr);
            return new SkillRequirement(skill, level);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}