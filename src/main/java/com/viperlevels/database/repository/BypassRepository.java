package com.viperlevels.database.repository;

import com.viperlevels.database.DatabaseManager;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class BypassRepository {

    private final DatabaseManager databaseManager;

    public CompletableFuture<Void> saveBypass(UUID playerUuid, String playerName, String bypassType, 
                                                String bypassTarget, Long expiresAt, UUID createdBy) {
        return databaseManager.executeAsync(conn -> {
            String sql = "INSERT OR REPLACE INTO players_bypass (uuid, player_name, bypass_type, bypass_target, expires_at, created_at, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            if (databaseManager.getType() == com.viperlevels.database.DatabaseType.MYSQL) {
                sql = "INSERT INTO players_bypass (uuid, player_name, bypass_type, bypass_target, expires_at, created_at, created_by) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                      "player_name=VALUES(player_name), bypass_type=VALUES(bypass_type), bypass_target=VALUES(bypass_target), " +
                      "expires_at=VALUES(expires_at), created_at=VALUES(created_at), created_by=VALUES(created_by)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, playerName);
                stmt.setString(3, bypassType);
                stmt.setString(4, bypassTarget);
                stmt.setObject(5, expiresAt);
                stmt.setLong(6, System.currentTimeMillis());
                stmt.setString(7, createdBy.toString());
                stmt.executeUpdate();
            }
        });
    }

    public CompletableFuture<Map<String, Object>> getBypass(UUID playerUuid) {
        return databaseManager.queryAsync(conn -> {
            String sql = "SELECT * FROM players_bypass WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("uuid", rs.getString("uuid"));
                        data.put("player_name", rs.getString("player_name"));
                        data.put("bypass_type", rs.getString("bypass_type"));
                        data.put("bypass_target", rs.getString("bypass_target"));
                        data.put("expires_at", rs.getObject("expires_at"));
                        data.put("created_at", rs.getLong("created_at"));
                        data.put("created_by", rs.getString("created_by"));
                        return data;
                    }
                }
            }
            return null;
        });
    }

    public CompletableFuture<List<Map<String, Object>>> getAllBypasses() {
        return databaseManager.queryAsync(conn -> {
            List<Map<String, Object>> bypasses = new ArrayList<>();
            String sql = "SELECT * FROM players_bypass";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("uuid", rs.getString("uuid"));
                    data.put("player_name", rs.getString("player_name"));
                    data.put("bypass_type", rs.getString("bypass_type"));
                    data.put("bypass_target", rs.getString("bypass_target"));
                    data.put("expires_at", rs.getObject("expires_at"));
                    data.put("created_at", rs.getLong("created_at"));
                    data.put("created_by", rs.getString("created_by"));
                    bypasses.add(data);
                }
            }
            return bypasses;
        });
    }

    public CompletableFuture<Void> removeBypass(UUID playerUuid) {
        return databaseManager.executeAsync(conn -> {
            String sql = "DELETE FROM players_bypass WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
            }
        });
    }

    public CompletableFuture<Void> cleanupExpired() {
        return databaseManager.executeAsync(conn -> {
            String sql = "DELETE FROM players_bypass WHERE expires_at IS NOT NULL AND expires_at < ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, System.currentTimeMillis());
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    databaseManager.getPlugin().logDebug("Cleaned up " + deleted + " expired bypass entries");
                }
            }
        });
    }
}