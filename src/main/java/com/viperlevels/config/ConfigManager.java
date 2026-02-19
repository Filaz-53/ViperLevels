package com.viperlevels.config;

import com.viperlevels.ViperLevels;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ConfigManager {

    private static ConfigManager instance;

    private final ViperLevels plugin;
    private FileConfiguration config;
    
    private SettingsConfig settingsConfig;
    private MessagesConfig messagesConfig;
    private PenaltiesConfig penaltiesConfig;

    private ConfigManager(ViperLevels plugin) {
        this.plugin = plugin;
        load();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        settingsConfig = new SettingsConfig(config);
        messagesConfig = new MessagesConfig(config);
        penaltiesConfig = new PenaltiesConfig(config);

        plugin.logInfo("Configuration loaded successfully");
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        settingsConfig = new SettingsConfig(config);
        messagesConfig = new MessagesConfig(config);
        penaltiesConfig = new PenaltiesConfig(config);

        plugin.logInfo("Configuration reloaded successfully");
    }

    public boolean isDebugMode() {
        return settingsConfig.isDebugMode();
    }

    public String getMessage(String path) {
        return messagesConfig.getMessage(path);
    }

    public String getMessageWithPrefix(String path) {
        return messagesConfig.getPrefix() + messagesConfig.getMessage(path);
    }
}