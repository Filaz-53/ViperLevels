package com.viperlevels.config;

import com.viperlevels.condition.SkillRequirement;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceholderReplacer {

    public static String replace(String message, Map<String, String> placeholders) {
        if (message == null || placeholders == null) {
            return message;
        }

        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    public static String replaceRequirements(String message, List<SkillRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return message.replace("%requirements%", "Nessuno");
        }

        String reqString = requirements.stream()
                .map(SkillRequirement::toString)
                .collect(Collectors.joining(", "));

        return message.replace("%requirements%", reqString);
    }

    public static String replacePlayer(String message, Player player) {
        if (player == null) {
            return message;
        }
        return message.replace("%player%", player.getName());
    }

    public static String replaceMaterial(String message, String material) {
        if (material == null) {
            return message;
        }
        return message.replace("%material%", material)
                      .replace("%target%", material);
    }

    public static Map<String, String> createPlaceholders() {
        return new HashMap<>();
    }

    public static Map<String, String> withPlayer(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        if (player != null) {
            placeholders.put("player", player.getName());
        }
        return placeholders;
    }

    public static Map<String, String> withMaterial(String material) {
        Map<String, String> placeholders = new HashMap<>();
        if (material != null) {
            placeholders.put("material", material);
            placeholders.put("target", material);
        }
        return placeholders;
    }

    public static Map<String, String> withRequirements(List<SkillRequirement> requirements) {
        Map<String, String> placeholders = new HashMap<>();
        if (requirements != null && !requirements.isEmpty()) {
            String reqString = requirements.stream()
                    .map(SkillRequirement::toString)
                    .collect(Collectors.joining(", "));
            placeholders.put("requirements", reqString);
        } else {
            placeholders.put("requirements", "Nessuno");
        }
        return placeholders;
    }

    public static Map<String, String> combine(Map<String, String>... maps) {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> map : maps) {
            if (map != null) {
                result.putAll(map);
            }
        }
        return result;
    }
}