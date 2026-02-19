package com.viperlevels.rule;

import com.viperlevels.ViperLevels;
import com.viperlevels.condition.Condition;
import com.viperlevels.condition.ConditionParser;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RuleManager {

    private static RuleManager instance;

    private final ViperLevels plugin;
    private final Map<String, Rule> itemRules;
    private final Map<String, Rule> blockRules;
    private final Map<String, Rule> armorRules;
    private final Map<String, Rule> potionRules;
    private final Map<String, Rule> foodRules;
    private final Map<String, Rule> mobRules;
    private final Map<String, Rule> dimensionRules;
    private final Map<Integer, Condition> enchantingLevels;
    private final List<MaterialGroup> materialGroups;

    private RuleManager(ViperLevels plugin) {
        this.plugin = plugin;
        this.itemRules = new ConcurrentHashMap<>();
        this.blockRules = new ConcurrentHashMap<>();
        this.armorRules = new ConcurrentHashMap<>();
        this.potionRules = new ConcurrentHashMap<>();
        this.foodRules = new ConcurrentHashMap<>();
        this.mobRules = new ConcurrentHashMap<>();
        this.dimensionRules = new ConcurrentHashMap<>();
        this.enchantingLevels = new ConcurrentHashMap<>();
        this.materialGroups = new ArrayList<>();
    }

    public static RuleManager getInstance() {
        if (instance == null) {
            instance = new RuleManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void loadRules() {
        plugin.logInfo("Loading rules from config...");

        clearAll();

        ConfigurationSection rulesSection = plugin.getConfig().getConfigurationSection("rules");
        if (rulesSection == null) {
            plugin.logWarning("No rules section found in config!");
            return;
        }

        loadRuleSection(rulesSection.getConfigurationSection("items"), RuleType.ITEM, itemRules);
        loadRuleSection(rulesSection.getConfigurationSection("blocks"), RuleType.BLOCK, blockRules);
        loadRuleSection(rulesSection.getConfigurationSection("armor"), RuleType.ARMOR, armorRules);
        loadRuleSection(rulesSection.getConfigurationSection("potions"), RuleType.POTION, potionRules);
        loadRuleSection(rulesSection.getConfigurationSection("food"), RuleType.FOOD, foodRules);
        loadRuleSection(rulesSection.getConfigurationSection("mobs"), RuleType.MOB, mobRules);
        loadRuleSection(rulesSection.getConfigurationSection("dimensions"), RuleType.DIMENSION, dimensionRules);

        loadEnchantingRules(rulesSection.getConfigurationSection("enchanting"));
        loadMaterialGroups();

        int totalRules = itemRules.size() + blockRules.size() + armorRules.size() + 
                        potionRules.size() + foodRules.size() + mobRules.size() + 
                        dimensionRules.size() + enchantingLevels.size();

        plugin.logInfo("Loaded " + totalRules + " rules and " + materialGroups.size() + " material groups");
    }

    private void loadRuleSection(ConfigurationSection section, RuleType type, Map<String, Rule> targetMap) {
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection ruleConfig = section.getConfigurationSection(key);
            if (ruleConfig == null) {
                continue;
            }

            List<String> actionStrings = ruleConfig.getStringList("actions");
            List<ActionType> actions = new ArrayList<>();
            for (String actionStr : actionStrings) {
                ActionType action = ActionType.fromString(actionStr);
                if (action != null) {
                    actions.add(action);
                }
            }

            if (actions.isEmpty() && type != RuleType.DIMENSION) {
                plugin.logWarning("Rule " + key + " has no valid actions, skipping");
                continue;
            }

            String requirementsStr = ruleConfig.getString("requirements");
            Condition condition = ConditionParser.parse(requirementsStr);

            if (condition == null || condition.isEmpty()) {
                plugin.logWarning("Rule " + key + " has invalid requirements: " + requirementsStr);
                continue;
            }

            Rule rule = new Rule(key, type, actions, condition);
            targetMap.put(key.toUpperCase(), rule);

            plugin.logDebug("Loaded rule: " + key + " (" + type + ") - " + actions.size() + " actions");
        }
    }

    private void loadEnchantingRules(ConfigurationSection section) {
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            if (key.startsWith("LEVEL_")) {
                try {
                    int level = Integer.parseInt(key.substring(6));
                    String requirementsStr = section.getString(key + ".requirements");
                    Condition condition = ConditionParser.parse(requirementsStr);

                    if (condition != null && !condition.isEmpty()) {
                        enchantingLevels.put(level, condition);
                        plugin.logDebug("Loaded enchanting level rule: " + level);
                    }
                } catch (NumberFormatException e) {
                    plugin.logWarning("Invalid enchanting level: " + key);
                }
            }
        }
    }

    private void loadMaterialGroups() {
        ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("material-groups");
        if (groupsSection == null) {
            return;
        }

        for (String groupName : groupsSection.getKeys(false)) {
            ConfigurationSection groupConfig = groupsSection.getConfigurationSection(groupName);
            if (groupConfig == null) {
                continue;
            }

            List<String> materials = groupConfig.getStringList("materials");
            String requirementsStr = groupConfig.getString("requirements");
            Condition condition = ConditionParser.parse(requirementsStr);

            if (condition != null && !condition.isEmpty() && !materials.isEmpty()) {
                MaterialGroup group = new MaterialGroup(groupName, materials, condition);
                materialGroups.add(group);
                plugin.logDebug("Loaded material group: " + groupName + " (" + materials.size() + " materials)");
            }
        }
    }

    public Rule getRule(String identifier, RuleType type) {
        Map<String, Rule> targetMap = getRuleMap(type);
        return targetMap.get(identifier.toUpperCase());
    }

    public Rule findRuleForMaterial(String material, RuleType type) {
        Rule directRule = getRule(material, type);
        if (directRule != null) {
            return directRule;
        }

        for (MaterialGroup group : materialGroups) {
            if (group.containsMaterial(material)) {
                return new Rule(group.getName(), type, Arrays.asList(ActionType.values()), group.getCondition());
            }
        }

        return null;
    }

    public Condition getEnchantingRequirement(int level) {
        Condition exactMatch = enchantingLevels.get(level);
        if (exactMatch != null) {
            return exactMatch;
        }

        int closestLevel = -1;
        for (int configuredLevel : enchantingLevels.keySet()) {
            if (configuredLevel <= level && configuredLevel > closestLevel) {
                closestLevel = configuredLevel;
            }
        }

        return closestLevel > 0 ? enchantingLevels.get(closestLevel) : null;
    }

    public List<Rule> getAllRulesForType(RuleType type) {
        return new ArrayList<>(getRuleMap(type).values());
    }

    private Map<String, Rule> getRuleMap(RuleType type) {
        switch (type) {
            case ITEM: return itemRules;
            case BLOCK: return blockRules;
            case ARMOR: return armorRules;
            case POTION: return potionRules;
            case FOOD: return foodRules;
            case MOB: return mobRules;
            case DIMENSION: return dimensionRules;
            default: return new HashMap<>();
        }
    }

    private void clearAll() {
        itemRules.clear();
        blockRules.clear();
        armorRules.clear();
        potionRules.clear();
        foodRules.clear();
        mobRules.clear();
        dimensionRules.clear();
        enchantingLevels.clear();
        materialGroups.clear();
    }

    public int getTotalRulesCount() {
        return itemRules.size() + blockRules.size() + armorRules.size() + 
               potionRules.size() + foodRules.size() + mobRules.size() + 
               dimensionRules.size() + enchantingLevels.size();
    }
}