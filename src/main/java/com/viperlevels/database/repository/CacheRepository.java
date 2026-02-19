package com.viperlevels.database.repository;

import com.viperlevels.database.DatabaseManager;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CacheRepository {

    private final DatabaseManager databaseManager;

    public CompletableFuture<Void> saveCache(UUID playerUuid, String playerName, String skillData, long expiresAt) {
        return databaseManager.executeAsync(conn -> {
            String sql = "INSERT OR REPLACE INTO cached_levels (uuid, player_name, skill_data, cached_at, expires_at) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            if (databaseManager.getType() == com.viperlevels.database.DatabaseType.MYSQL) {
                sql = "INSERT INTO cached_levels (uuid, player_name, skill_data, cached_at, expires_at) " +
                      "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                      "player_name=VALUES(player_name), skill_data=VALUES(skill_data), cached_at=VALUES(cached_at), expires_at=VALUES(expires_at)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, playerName);
                stmt.setString(3, skillData);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.setLong(5, expiresAt);
                stmt.executeUpdate();
            }
        });
    }

    public CompletableFuture<Map<String, Object>> getCache(UUID playerUuid) {
        return databaseManager.queryAsync(conn -> {
            String sql = "SELECT * FROM cached_levels WHERE uuid = ? AND expires_at > ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setLong(2, System.currentTimeMillis());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("uuid", rs.getString("uuid"));
                        data.put("player_name", rs.getString("player_name"));
                        data.put("skill_data", rs.getString("skill_data"));
                        data.put("cached_at", rs.getLong("cached_at"));
                        data.put("expires_at", rs.getLong("expires_at"));
                        return data;
                    }
                }
            }
            return null;
        });
    }

    public CompletableFuture<Void> invalidateCache(UUID playerUuid) {
        return databaseManager.executeAsync(conn -> {
            String sql = "DELETE FROM cached_levels WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
            }
        });
    }

    public CompletableFuture<Void> cleanupExpired() {
        return databaseManager.executeAsync(conn -> {
            String sql = "DELETE FROM cached_levels WHERE expires_at < ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, System.currentTimeMillis());
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    databaseManager.getPlugin().logDebug("Cleaned up " + deleted + " expired cache entries");
                }
            }
        });
    }
}