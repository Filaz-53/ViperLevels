package com.viperlevels.bypass;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BypassEntry {
    private UUID playerUuid;
    private String playerName;
    private BypassType bypassType;
    private String target;
    private Long expiresAt;
    private long createdAt;
    private UUID createdBy;

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    public long getRemainingTime() {
        if (isPermanent()) {
            return -1;
        }
        return Math.max(0, expiresAt - System.currentTimeMillis());
    }

    public boolean matches(String materialIdentifier) {
        if (bypassType == BypassType.ALL) {
            return true;
        }

        if (bypassType == BypassType.SPECIFIC_MATERIAL) {
            return target != null && target.equalsIgnoreCase(materialIdentifier);
        }

        return false;
    }

    public boolean matchesCategory(BypassType category) {
        return bypassType == BypassType.ALL || bypassType == category;
    }
}