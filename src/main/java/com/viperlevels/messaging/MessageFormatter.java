package com.viperlevels.messaging;

import com.viperlevels.condition.SkillRequirement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageFormatter {

    public static String format(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatWithPrefix(String prefix, String message) {
        return format(prefix + message);
    }

    public static String formatRequirements(List<SkillRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "Nessuno";
        }

        return requirements.stream()
                .map(SkillRequirement::toString)
                .collect(Collectors.joining("&7, &e"));
    }

    public static String formatList(List<String> items, String separator) {
        if (items == null || items.isEmpty()) {
            return "Nessuno";
        }
        return String.join(separator, items);
    }

    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null || placeholders == null) {
            return message;
        }

        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    public static String formatPlayerName(Player player, boolean withColor) {
        if (player == null) {
            return "Unknown";
        }
        return withColor ? "&e" + player.getName() + "&r" : player.getName();
    }

    public static String formatBoolean(boolean value) {
        return value ? "&a✓ Sì" : "&c✗ No";
    }

    public static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "g " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public static String stripColor(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.stripColor(format(message));
    }
}