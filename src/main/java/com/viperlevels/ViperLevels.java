package com.viperlevels;

import com.viperlevels.bypass.BypassManager;
import com.viperlevels.cache.CacheManager;
import com.viperlevels.command.CommandManager;
import com.viperlevels.config.ConfigManager;
import com.viperlevels.database.DatabaseManager;
import com.viperlevels.integration.ItemsAdderIntegration;
import com.viperlevels.integration.McMMOIntegration;
import com.viperlevels.inventory.gui.GUIListener;
import com.viperlevels.inventory.gui.GUIManager;
import com.viperlevels.listener.*;
import com.viperlevels.rule.RuleManager;
import com.viperlevels.util.DebugLogger;
import com.viperlevels.util.MetricsCollector;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ViperLevels extends JavaPlugin {

    @Getter
    private static ViperLevels instance;

    private boolean mcMMOEnabled = false;
    private boolean itemsAdderEnabled = false;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private RuleManager ruleManager;
    private GUIManager guiManager;
    private BypassManager bypassManager;
    private CommandManager commandManager;
    
    private McMMOIntegration mcmmoIntegration;
    private ItemsAdderIntegration itemsAdderIntegration;
    private MetricsCollector metricsCollector;

    @Override
    public void onEnable() {
        instance = this;

        logInfo("=================================");
        logInfo("  ViperLevels v" + getDescription().getVersion());
        logInfo("  Initializing plugin...");
        logInfo("=================================");

        if (!checkDependencies()) {
            logError("Missing required dependencies! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        configManager = ConfigManager.getInstance();
        configManager.load();

        DebugLogger.initialize();
        metricsCollector = MetricsCollector.getInstance();

        databaseManager = DatabaseManager.getInstance();
        databaseManager.initialize();

        cacheManager = CacheManager.getInstance();
        cacheManager.initialize();

        ruleManager = RuleManager.getInstance();
        ruleManager.loadRules();

        metricsCollector.setMetric("rules.loaded", ruleManager.getTotalRulesCount());

        guiManager = GUIManager.getInstance();

        bypassManager = BypassManager.getInstance();
        bypassManager.initialize();

        mcmmoIntegration = McMMOIntegration.getInstance();
        itemsAdderIntegration = ItemsAdderIntegration.getInstance();

        registerListeners();
        
        commandManager = new CommandManager(this);

        logInfo("ViperLevels successfully enabled!");
        logInfo("=================================");
    }

    @Override
    public void onDisable() {
        logInfo("=================================");
        logInfo("  ViperLevels v" + getDescription().getVersion());
        logInfo("  Shutting down...");
        logInfo("=================================");

        if (metricsCollector != null) {
            metricsCollector.saveMetrics().join();
        }

        if (guiManager != null) {
            guiManager.closeAll();
        }

        if (cacheManager != null) {
            cacheManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        DebugLogger.shutdown();

        logInfo("ViperLevels disabled successfully!");
    }

    private boolean checkDependencies() {
        logInfo("Checking dependencies...");

        Plugin mcMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
        if (mcMMO == null || !mcMMO.isEnabled()) {
            logError("mcMMO not found! This plugin requires mcMMO to function.");
            return false;
        }
        mcMMOEnabled = true;
        logInfo("mcMMO found: v" + mcMMO.getDescription().getVersion());

        Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        if (itemsAdder != null && itemsAdder.isEnabled()) {
            itemsAdderEnabled = true;
            logInfo("ItemsAdder found: v" + itemsAdder.getDescription().getVersion());
        } else {
            logInfo("ItemsAdder not found (optional)");
        }

        return true;
    }

    private void registerListeners() {
        logInfo("Registering event listeners...");

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new ItemListener(this), this);
        pm.registerEvents(new CraftListener(this), this);
        pm.registerEvents(new ArmorListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new ConsumeListener(this), this);
        pm.registerEvents(new DimensionListener(this), this);
        pm.registerEvents(new EnchantListener(this), this);
        pm.registerEvents(new GUIListener(this), this);

        logInfo("Registered 9 event listeners");
    }

    public void logInfo(String message) {
        getLogger().info(message);
    }

    public void logWarning(String message) {
        getLogger().warning(message);
    }

    public void logError(String message) {
        getLogger().severe(message);
    }

    public void logDebug(String message) {
        if (configManager != null && configManager.isDebugMode()) {
            getLogger().info("[DEBUG] " + message);
            DebugLogger.log(message);
        }
    }
}