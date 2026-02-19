package com.viperlevels.bypass;

import com.viperlevels.ViperLevels;
import com.viperlevels.database.repository.BypassRepository;
import com.viperlevels.rule.RuleType;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BypassManager {

    private static BypassManager instance;

    private final ViperLevels plugin;
    private final BypassRepository repository;
    private final Map<UUID, List<BypassEntry>> bypassCache;

    private BypassManager(ViperLevels plugin) {
        this.plugin = plugin;
        this.repository = plugin.getDatabaseManager().getBypassRepository();
        this.bypassCache = new ConcurrentHashMap<>();
    }

    public static BypassManager getInstance() {
        if (instance == null) {
            instance = new BypassManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void initialize() {
        loadAllBypasses();
        startCleanupTask();
    }

    private void loadAllBypasses() {
        repository.getAllBypasses().thenAccept(bypasses -> {
            int loaded = 0;
            for (Map<String, Object> data : bypasses) {
                BypassEntry entry = deserializeBypass(data);
                if (entry != null && !entry.isExpired()) {
                    bypassCache.computeIfAbsent(entry.getPlayerUuid(), k -> new ArrayList<>()).add(entry);
                    loaded++;
                }
            }
            plugin.logInfo("Loaded " + loaded + " bypass entries from database");
        });
    }

    public void addBypass(UUID playerUuid, String playerName, BypassType type, String target, 
                         Long expiresAt, UUID createdBy) {
        BypassEntry entry = new BypassEntry(playerUuid, playerName, type, target, 
                                            expiresAt, System.currentTimeMillis(), createdBy);

        bypassCache.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(entry);

        repository.saveBypass(playerUuid, playerName, type.name(), target, expiresAt, createdBy);

        plugin.logDebug("Added bypass for " + playerName + ": " + type + " -> " + target);
    }

    public void addPermanentBypass(UUID playerUuid, String playerName, BypassType type, 
                                   String target, UUID createdBy) {
        addBypass(playerUuid, playerName, type, target, null, createdBy);
    }

    public void addTemporaryBypass(UUID playerUuid, String playerName, BypassType type, 
                                   String target, long durationMillis, UUID createdBy) {
        long expiresAt = System.currentTimeMillis() + durationMillis;
        addBypass(playerUuid, playerName, type, target, expiresAt, createdBy);
    }

    public void removeBypass(UUID playerUuid) {
        bypassCache.remove(playerUuid);
        repository.removeBypass(playerUuid);
        plugin.logDebug("Removed all bypasses for UUID: " + playerUuid);
    }

    public void removeSpecificBypass(UUID playerUuid, String target) {
        List<BypassEntry> entries = bypassCache.get(playerUuid);
        if (entries != null) {
            entries.removeIf(entry -> target.equalsIgnoreCase(entry.getTarget()));
            if (entries.isEmpty()) {
                bypassCache.remove(playerUuid);
            }
        }
    }

    public boolean hasActiveBypass(UUID playerUuid, String materialIdentifier, RuleType ruleType) {
        List<BypassEntry> entries = bypassCache.get(playerUuid);
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        BypassType categoryType = convertRuleType(ruleType);

        for (BypassEntry entry : entries) {
            if (entry.isExpired()) {
                continue;
            }

            if (entry.getBypassType() == BypassType.ALL) {
                return true;
            }

            if (entry.matchesCategory(categoryType)) {
                return true;
            }

            if (entry.matches(materialIdentifier)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasCategoryBypass(UUID playerUuid, BypassType category) {
        List<BypassEntry> entries = bypassCache.get(playerUuid);
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        for (BypassEntry entry : entries) {
            if (entry.isExpired()) {
                continue;
            }

            if (entry.getBypassType() == BypassType.ALL || entry.matchesCategory(category)) {
                return true;
            }
        }

        return false;
    }

    public List<BypassEntry> getPlayerBypasses(UUID playerUuid) {
        List<BypassEntry> entries = bypassCache.get(playerUuid);
        if (entries == null) {
            return new ArrayList<>();
        }

        entries.removeIf(BypassEntry::isExpired);
        return new ArrayList<>(entries);
    }

    public Map<UUID, List<BypassEntry>> getAllBypasses() {
        return new HashMap<>(bypassCache);
    }

    private void cleanupExpired() {
        int removed = 0;
        Iterator<Map.Entry<UUID, List<BypassEntry>>> iterator = bypassCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<BypassEntry>> entry = iterator.next();
            List<BypassEntry> bypasses = entry.getValue();

            int sizeBefore = bypasses.size();
            bypasses.removeIf(BypassEntry::isExpired);
            removed += sizeBefore - bypasses.size();

            if (bypasses.isEmpty()) {
                iterator.remove();
            }
        }

        if (removed > 0) {
            plugin.logDebug("Cleaned up " + removed + " expired bypass entries");
            repository.cleanupExpired();
        }
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::cleanupExpired, 
                                                          6000L, 6000L);
    }

    private BypassType convertRuleType(RuleType ruleType) {
        switch (ruleType) {
            case BLOCK:
                return BypassType.BLOCKS;
            case ITEM:
                return BypassType.ITEMS;
            case ARMOR:
                return BypassType.ARMOR;
            case ENCHANTING:
                return BypassType.ENCHANTING;
            case POTION:
                return BypassType.POTIONS;
            case FOOD:
                return BypassType.FOOD;
            case MOB:
                return BypassType.MOBS;
            case DIMENSION:
                return BypassType.DIMENSIONS;
            default:
                return BypassType.ITEMS;
        }
    }

    private BypassEntry deserializeBypass(Map<String, Object> data) {
        try {
            UUID uuid = UUID.fromString((String) data.get("uuid"));
            String playerName = (String) data.get("player_name");
            BypassType type = BypassType.valueOf((String) data.get("bypass_type"));
            String target = (String) data.get("bypass_target");
            Long expiresAt = (Long) data.get("expires_at");
            long createdAt = (Long) data.get("created_at");
            UUID createdBy = UUID.fromString((String) data.get("created_by"));

            return new BypassEntry(uuid, playerName, type, target, expiresAt, createdAt, createdBy);
        } catch (Exception e) {
            plugin.logError("Failed to deserialize bypass entry: " + e.getMessage());
            return null;
        }
    }
}