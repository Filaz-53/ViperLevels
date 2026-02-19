package com.viperlevels.database.repository;

import com.viperlevels.database.DatabaseManager;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class StatsRepository {

    private final DatabaseManager databaseManager;

    public CompletableFuture<Void> incrementStat(String statType, String statKey, long amount) {
        return databaseManager.executeAsync(conn -> {
            String selectSql = "SELECT stat_value FROM statistics WHERE stat_type = ? AND stat_key = ?";
            String insertSql = "INSERT INTO statistics (stat_type, stat_key, stat_value, updated_at) VALUES (?, ?, ?, ?)";
            String updateSql = "UPDATE statistics SET stat_value = stat_value + ?, updated_at = ? WHERE stat_type = ? AND stat_key = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, statType);
                selectStmt.setString(2, statKey);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setLong(1, amount);
                            updateStmt.setLong(2, System.currentTimeMillis());
                            updateStmt.setString(3, statType);
                            updateStmt.setString(4, statKey);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, statType);
                            insertStmt.setString(2, statKey);
                            insertStmt.setLong(3, amount);
                            insertStmt.setLong(4, System.currentTimeMillis());
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        });
    }

    public CompletableFuture<Long> getStat(String statType, String statKey) {
        return databaseManager.queryAsync(conn -> {
            String sql = "SELECT stat_value FROM statistics WHERE stat_type = ? AND stat_key = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, statType);
                stmt.setString(2, statKey);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("stat_value");
                    }
                }
            }
            return 0L;
        });
    }

    public CompletableFuture<Map<String, Long>> getAllStats(String statType) {
        return databaseManager.queryAsync(conn -> {
            Map<String, Long> stats = new HashMap<>();
            String sql = "SELECT stat_key, stat_value FROM statistics WHERE stat_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, statType);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        stats.put(rs.getString("stat_key"), rs.getLong("stat_value"));
                    }
                }
            }
            return stats;
        });
    }

    public CompletableFuture<Void> resetStats(String statType) {
        return databaseManager.executeAsync(conn -> {
            String sql = "DELETE FROM statistics WHERE stat_type = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, statType);
                stmt.executeUpdate();
            }
        });
    }
}