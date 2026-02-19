package com.viperlevels.cache;

import com.gmail.nossr50.api.ExperienceAPI;
import com.viperlevels.ViperLevels;
import com.viperlevels.condition.McMMOSkill;
import com.viperlevels.database.DatabaseManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public CompletableFuture<CachedSkillData> loadFromMcMMO(UUID playerUuid, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            ViperLevels plugin = cacheManager.getPlugin();
            
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Map<String, Object> dbCache = dbManager.getCacheRepository().getCache(playerUuid).join();
            
            if (dbCache != null) {
                String skillData = (String) dbCache.get("skill_data");
                long cachedAt = (Long) dbCache.get("cached_at");
                long expiresAt = (Long) dbCache.get("expires_at");
                
                Map<McMMOSkill, Integer> skillLevels = CachedSkillData.deserialize(skillData);
                plugin.logDebug("Loaded cache from database for: " + playerName);
                return new CachedSkillData(playerUuid, playerName, skillLevels, cachedAt, expiresAt);
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            Map<McMMOSkill, Integer> skillLevels = new HashMap<>();

            for (McMMOSkill skill : McMMOSkill.values()) {
                try {
                    int level = ExperienceAPI.getLevel(offlinePlayer.getPlayer(), skill.getMcmmoName());
                    skillLevels.put(skill, level);
                } catch (Exception e) {
                    plugin.logDebug("Failed to get level for skill " + skill + ": " + e.getMessage());
                    skillLevels.put(skill, 0);
                }
            }

            long now = System.currentTimeMillis();
            int ttl = plugin.getConfigManager().getSettingsConfig().getCacheTtlSeconds();
            long expiresAt = now + (ttl * 1000L);

            CachedSkillData data = new CachedSkillData(playerUuid, playerName, skillLevels, now, expiresAt);

            dbManager.getCacheRepository().saveCache(playerUuid, playerName, data.serialize(), expiresAt);

            plugin.logDebug("Loaded fresh cache from mcMMO for: " + playerName);
            return data;
        });
    }

    public CompletableFuture<Void> refreshCache(UUID playerUuid, String playerName) {
        cacheManager.invalidate(playerUuid);
        return loadFromMcMMO(playerUuid, playerName).thenAccept(data -> {
            if (data != null) {
                cacheManager.getCache().put(playerUuid, data);
            }
        });
    }
}