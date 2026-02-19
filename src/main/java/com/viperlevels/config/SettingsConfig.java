package com.viperlevels.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class SettingsConfig {

    private final boolean debugMode;
    
    private final String databaseType;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final int mysqlPoolSize;
    
    private final boolean cacheEnabled;
    private final int cacheTtlSeconds;
    private final int cacheCleanupInterval;
    
    private final int guiUpdateInterval;
    private final boolean guiCloseOnAction;

    public SettingsConfig(FileConfiguration config) {
        ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings == null) {
            this.debugMode = false;
            this.databaseType = "SQLITE";
            this.mysqlHost = "localhost";
            this.mysqlPort = 3306;
            this.mysqlDatabase = "viperlevels";
            this.mysqlUsername = "root";
            this.mysqlPassword = "password";
            this.mysqlPoolSize = 10;
            this.cacheEnabled = true;
            this.cacheTtlSeconds = 300;
            this.cacheCleanupInterval = 600;
            this.guiUpdateInterval = 20;
            this.guiCloseOnAction = false;
            return;
        }

        this.debugMode = settings.getBoolean("debug-mode", false);

        ConfigurationSection database = settings.getConfigurationSection("database");
        if (database != null) {
            this.databaseType = database.getString("type", "SQLITE").toUpperCase();
            
            ConfigurationSection mysql = database.getConfigurationSection("mysql");
            if (mysql != null) {
                this.mysqlHost = mysql.getString("host", "localhost");
                this.mysqlPort = mysql.getInt("port", 3306);
                this.mysqlDatabase = mysql.getString("database", "viperlevels");
                this.mysqlUsername = mysql.getString("username", "root");
                this.mysqlPassword = mysql.getString("password", "password");
                this.mysqlPoolSize = mysql.getInt("pool-size", 10);
            } else {
                this.mysqlHost = "localhost";
                this.mysqlPort = 3306;
                this.mysqlDatabase = "viperlevels";
                this.mysqlUsername = "root";
                this.mysqlPassword = "password";
                this.mysqlPoolSize = 10;
            }
        } else {
            this.databaseType = "SQLITE";
            this.mysqlHost = "localhost";
            this.mysqlPort = 3306;
            this.mysqlDatabase = "viperlevels";
            this.mysqlUsername = "root";
            this.mysqlPassword = "password";
            this.mysqlPoolSize = 10;
        }

        ConfigurationSection cache = settings.getConfigurationSection("cache");
        if (cache != null) {
            this.cacheEnabled = cache.getBoolean("enabled", true);
            this.cacheTtlSeconds = cache.getInt("ttl-seconds", 300);
            this.cacheCleanupInterval = cache.getInt("cleanup-interval", 600);
        } else {
            this.cacheEnabled = true;
            this.cacheTtlSeconds = 300;
            this.cacheCleanupInterval = 600;
        }

        ConfigurationSection gui = settings.getConfigurationSection("gui");
        if (gui != null) {
            this.guiUpdateInterval = gui.getInt("update-interval", 20);
            this.guiCloseOnAction = gui.getBoolean("close-on-action", false);
        } else {
            this.guiUpdateInterval = 20;
            this.guiCloseOnAction = false;
        }
    }
}