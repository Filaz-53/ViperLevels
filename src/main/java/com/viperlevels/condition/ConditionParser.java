package com.viperlevels.condition;

import com.viperlevels.ViperLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionParser {

    private static final Pattern SKILL_PATTERN = Pattern.compile("([A-Z_]+)\\((\\d+)\\)");

    public static Condition parse(String input) {
        if (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("NONE")) {
            return new Condition();
        }

        input = input.trim().toUpperCase();

        Condition.ConditionOperator operator = Condition.ConditionOperator.AND;
        if (input.contains(" OR ")) {
            operator = Condition.ConditionOperator.OR;
        }

        String[] parts;
        if (operator == Condition.ConditionOperator.OR) {
            parts = input.split("\\s+OR\\s+");
        } else {
            parts = input.split("\\s+AND\\s+");
        }

        List<SkillRequirement> requirements = new ArrayList<>();

        for (String part : parts) {
            part = part.trim();

            if (part.startsWith("(") && part.endsWith(")")) {
                part = part.substring(1, part.length() - 1).trim();

                Condition nestedCondition = parseNested(part);
                if (nestedCondition != null && !nestedCondition.isEmpty()) {
                    requirements.addAll(nestedCondition.getRequirements());
                }
            } else {
                SkillRequirement req = SkillRequirement.parse(part);
                if (req != null) {
                    requirements.add(req);
                } else {
                    ViperLevels.getInstance().logWarning("Failed to parse skill requirement: " + part);
                }
            }
        }

        return new Condition(requirements, operator);
    }

    private static Condition parseNested(String input) {
        input = input.trim();

        Condition.ConditionOperator operator = Condition.ConditionOperator.OR;
        if (input.contains(" AND ")) {
            operator = Condition.ConditionOperator.AND;
        }

        String[] parts;
        if (operator == Condition.ConditionOperator.AND) {
            parts = input.split("\\s+AND\\s+");
        } else {
            parts = input.split("\\s+OR\\s+");
        }

        List<SkillRequirement> requirements = new ArrayList<>();

        for (String part : parts) {
            part = part.trim();
            SkillRequirement req = SkillRequirement.parse(part);
            if (req != null) {
                requirements.add(req);
            }
        }

        return new Condition(requirements, operator);
    }

    public static List<SkillRequirement> extractAllRequirements(String input) {
        List<SkillRequirement> allRequirements = new ArrayList<>();

        if (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("NONE")) {
            return allRequirements;
        }

        Matcher matcher = SKILL_PATTERN.matcher(input.toUpperCase());

        while (matcher.find()) {
            String skillName = matcher.group(1);
            int level = Integer.parseInt(matcher.group(2));

            McMMOSkill skill = McMMOSkill.fromString(skillName);
            if (skill != null) {
                allRequirements.add(new SkillRequirement(skill, level));
            }
        }

        return allRequirements;
    }
}