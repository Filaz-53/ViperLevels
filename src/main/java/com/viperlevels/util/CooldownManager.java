package com.viperlevels.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Long> cooldowns;
    private final long cooldownMillis;

    public CooldownManager(long cooldownMillis) {
        this.cooldowns = new ConcurrentHashMap<>();
        this.cooldownMillis = cooldownMillis;
    }

    public boolean hasCooldown(UUID uuid) {
        Long lastTime = cooldowns.get(uuid);
        if (lastTime == null) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - lastTime;
        if (elapsed >= cooldownMillis) {
            cooldowns.remove(uuid);
            return false;
        }

        return true;
    }

    public void setCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public long getRemainingCooldown(UUID uuid) {
        Long lastTime = cooldowns.get(uuid);
        if (lastTime == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastTime;
        long remaining = cooldownMillis - elapsed;
        return Math.max(0, remaining);
    }

    public void removeCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void clearAll() {
        cooldowns.clear();
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> (now - entry.getValue()) >= cooldownMillis);
    }

    public int getActiveCooldowns() {
        cleanup();
        return cooldowns.size();
    }
}