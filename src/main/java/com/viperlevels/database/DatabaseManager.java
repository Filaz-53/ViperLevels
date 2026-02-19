package com.viperlevels.database;

import com.viperlevels.ViperLevels;
import com.viperlevels.config.SettingsConfig;
import com.viperlevels.database.repository.BypassRepository;
import com.viperlevels.database.repository.CacheRepository;
import com.viperlevels.database.repository.StatsRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

@Getter
public class DatabaseManager {

    private static DatabaseManager instance;

    private final ViperLevels plugin;
    private final DatabaseType type;
    private HikariDataSource dataSource;

    private BypassRepository bypassRepository;
    private CacheRepository cacheRepository;
    private StatsRepository statsRepository;

    private DatabaseManager(ViperLevels plugin) {
        this.plugin = plugin;
        SettingsConfig settings = plugin.getConfigManager().getSettingsConfig();
        this.type = DatabaseType.valueOf(settings.getDatabaseType());
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void initialize() {
        plugin.logInfo("Initializing database (" + type + ")...");

        HikariConfig config = new HikariConfig();
        SettingsConfig settings = plugin.getConfigManager().getSettingsConfig();

        if (type == DatabaseType.SQLITE) {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "database.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
        } else {
            config.setJdbcUrl("jdbc:mysql://" + settings.getMysqlHost() + ":" + settings.getMysqlPort() + "/" + settings.getMysqlDatabase());
            config.setUsername(settings.getMysqlUsername());
            config.setPassword(settings.getMysqlPassword());
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(settings.getMysqlPoolSize());
        }

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(10000);
        config.setLeakDetectionThreshold(60000);

        dataSource = new HikariDataSource(config);

        createTables();

        bypassRepository = new BypassRepository(this);
        cacheRepository = new CacheRepository(this);
        statsRepository = new StatsRepository(this);

        plugin.logInfo("Database initialized successfully");
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            plugin.logInfo("Closing database connections...");
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players_bypass (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "bypass_type VARCHAR(32) NOT NULL, " +
                    "bypass_target VARCHAR(128), " +
                    "expires_at BIGINT, " +
                    "created_at BIGINT NOT NULL, " +
                    "created_by VARCHAR(36) NOT NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS cached_levels (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "skill_data TEXT NOT NULL, " +
                    "cached_at BIGINT NOT NULL, " +
                    "expires_at BIGINT NOT NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS statistics (" +
                    "id INTEGER PRIMARY KEY " + (type == DatabaseType.SQLITE ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ", " +
                    "stat_type VARCHAR(32) NOT NULL, " +
                    "stat_key VARCHAR(128) NOT NULL, " +
                    "stat_value BIGINT NOT NULL, " +
                    "updated_at BIGINT NOT NULL" +
                    ")"
                );

                if (type == DatabaseType.MYSQL) {
                    stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_bypass_type ON players_bypass(bypass_type)");
                    stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_cached_expires ON cached_levels(expires_at)");
                    stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_stats_type ON statistics(stat_type, stat_key)");
                }

                plugin.logInfo("Database tables created/verified");

            } catch (SQLException e) {
                plugin.logError("Failed to create database tables: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> executeAsync(DatabaseOperation operation) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                operation.execute(conn);
            } catch (SQLException e) {
                plugin.logError("Database operation failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public <T> CompletableFuture<T> queryAsync(DatabaseQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return query.execute(conn);
            } catch (SQLException e) {
                plugin.logError("Database query failed: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface DatabaseQuery<T> {
        T execute(Connection connection) throws SQLException;
    }
}