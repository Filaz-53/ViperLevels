package com.viperlevels.cache;

import com.viperlevels.condition.McMMOSkill;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CachedSkillData {
    
    private final UUID playerUuid;
    private final String playerName;
    private final Map<McMMOSkill, Integer> skillLevels;
    private final long cachedAt;
    private final long expiresAt;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public int getSkillLevel(McMMOSkill skill) {
        return skillLevels.getOrDefault(skill, 0);
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<McMMOSkill, Integer> entry : skillLevels.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey().name()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    public static Map<McMMOSkill, Integer> deserialize(String data) {
        Map<McMMOSkill, Integer> skillLevels = new java.util.HashMap<>();
        if (data == null || data.isEmpty()) {
            return skillLevels;
        }
        
        String[] pairs = data.split(";");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                try {
                    McMMOSkill skill = McMMOSkill.valueOf(parts[0]);
                    int level = Integer.parseInt(parts[1]);
                    skillLevels.put(skill, level);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return skillLevels;
    }
}