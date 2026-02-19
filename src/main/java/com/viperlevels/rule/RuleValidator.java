package com.viperlevels.rule;

import com.viperlevels.ViperLevels;
import com.viperlevels.bypass.BypassChecker;
import com.viperlevels.condition.Condition;
import com.viperlevels.condition.ConditionEvaluator;
import com.viperlevels.condition.SkillRequirement;
import org.bukkit.entity.Player;

import java.util.List;

public class RuleValidator {

    private final ViperLevels plugin;
    private final RuleManager ruleManager;
    private final BypassChecker bypassChecker;

    public RuleValidator() {
        this.plugin = ViperLevels.getInstance();
        this.ruleManager = RuleManager.getInstance();
        this.bypassChecker = new BypassChecker();
    }

    public ValidationResult validate(Player player, String identifier, RuleType type, ActionType action) {
        if (bypassChecker.hasBypass(player, identifier, type)) {
            return ValidationResult.pass();
        }

        Rule rule = ruleManager.findRuleForMaterial(identifier, type);

        if (rule == null) {
            return ValidationResult.pass();
        }

        if (!rule.appliesToAction(action)) {
            return ValidationResult.pass();
        }

        return validateCondition(player, rule);
    }

    public ValidationResult validateEnchanting(Player player, int enchantLevel) {
        if (bypassChecker.hasCategoryBypass(player, RuleType.ENCHANTING)) {
            return ValidationResult.pass();
        }

        Condition condition = ruleManager.getEnchantingRequirement(enchantLevel);

        if (condition == null) {
            return ValidationResult.pass();
        }

        boolean passed = ConditionEvaluator.evaluate(player, condition);

        if (passed) {
            return ValidationResult.pass();
        }

        List<SkillRequirement> missing = ConditionEvaluator.getMissingRequirements(player, condition);
        Rule enchantRule = new Rule("ENCHANT_LEVEL_" + enchantLevel, RuleType.ENCHANTING, 
                                    List.of(ActionType.ENCHANT), condition);
        return ValidationResult.fail(missing, enchantRule);
    }

    public ValidationResult validateDimension(Player player, String dimensionName) {
        if (bypassChecker.hasBypass(player, dimensionName, RuleType.DIMENSION)) {
            return ValidationResult.pass();
        }

        Rule rule = ruleManager.getRule(dimensionName, RuleType.DIMENSION);

        if (rule == null) {
            return ValidationResult.pass();
        }

        return validateCondition(player, rule);
    }

    private ValidationResult validateCondition(Player player, Rule rule) {
        if (!rule.hasRequirements()) {
            return ValidationResult.pass();
        }

        boolean passed = ConditionEvaluator.evaluate(player, rule.getCondition());

        if (passed) {
            plugin.logDebug("Player " + player.getName() + " passed validation for: " + rule.getIdentifier());
            return ValidationResult.pass();
        }

        List<SkillRequirement> missing = ConditionEvaluator.getMissingRequirements(player, rule.getCondition());
        plugin.logDebug("Player " + player.getName() + " failed validation for: " + rule.getIdentifier() + 
                       " (missing: " + missing.size() + " requirements)");

        return ValidationResult.fail(missing, rule);
    }

    public boolean canPlayerUse(Player player, String identifier, RuleType type, ActionType action) {
        return validate(player, identifier, type, action).isPassed();
    }
}