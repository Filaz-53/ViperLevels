package com.viperlevels.cache;

import com.viperlevels.ViperLevels;
import com.viperlevels.condition.McMMOSkill;
import com.viperlevels.database.DatabaseManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CacheManager {

    private static CacheManager instance;

    private final ViperLevels plugin;
    private final Map<UUID, CachedSkillData> cache;
    private final CacheService cacheService;
    
    private int taskId = -1;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    private CacheManager(ViperLevels plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.cacheService = new CacheService(this);
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void initialize() {
        if (!plugin.getConfigManager().getSettingsConfig().isCacheEnabled()) {
            plugin.logInfo("Cache system disabled in config");
            return;
        }

        plugin.logInfo("Initializing cache system...");

        int cleanupInterval = plugin.getConfigManager().getSettingsConfig().getCacheCleanupInterval();
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanupExpired, 
                                                                   cleanupInterval * 20L, 
                                                                   cleanupInterval * 20L).getTaskId();

        preloadOnlinePlayers();

        plugin.logInfo("Cache system initialized (cleanup every " + cleanupInterval + "s)");
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        cache.clear();
        plugin.logInfo("Cache system shutdown (Hits: " + cacheHits + ", Misses: " + cacheMisses + ")");
    }

    public CompletableFuture<CachedSkillData> getOrLoad(UUID playerUuid, String playerName) {
        CachedSkillData cached = cache.get(playerUuid);
        
        if (cached != null && !cached.isExpired()) {
            cacheHits++;
            return CompletableFuture.completedFuture(cached);
        }

        cacheMisses++;
        return cacheService.loadFromMcMMO(playerUuid, playerName).thenApply(data -> {
            if (data != null) {
                cache.put(playerUuid, data);
            }
            return data;
        });
    }

    public void invalidate(UUID playerUuid) {
        cache.remove(playerUuid);
        DatabaseManager.getInstance().getCacheRepository().invalidateCache(playerUuid);
        plugin.logDebug("Cache invalidated for player: " + playerUuid);
    }

    public void invalidateAll() {
        cache.clear();
        plugin.logInfo("All cache entries invalidated");
    }

    public void cleanupExpired() {
        int removed = 0;
        for (Map.Entry<UUID, CachedSkillData> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }
        
        if (removed > 0) {
            plugin.logDebug("Cleaned up " + removed + " expired cache entries");
        }

        DatabaseManager.getInstance().getCacheRepository().cleanupExpired();
    }

    private void preloadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getOrLoad(player.getUniqueId(), player.getName());
        }
        plugin.logDebug("Preloaded cache for " + Bukkit.getOnlinePlayers().size() + " online players");
    }

    public int getCacheSize() {
        return cache.size();
    }

    public double getHitRate() {
        long total = cacheHits + cacheMisses;
        if (total == 0) return 0.0;
        return (double) cacheHits / total * 100.0;
    }
}