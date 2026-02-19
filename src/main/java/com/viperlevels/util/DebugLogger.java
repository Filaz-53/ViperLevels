package com.viperlevels.util;

import com.viperlevels.ViperLevels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLogger {

    private static ViperLevels plugin;
    private static File logFile;
    private static PrintWriter writer;
    private static boolean initialized = false;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void initialize() {
        plugin = ViperLevels.getInstance();

        if (!plugin.getConfigManager().isDebugMode()) {
            return;
        }

        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            logFile = new File(dataFolder, "debug.log");
            writer = new PrintWriter(new FileWriter(logFile, true), true);
            initialized = true;

            log("=".repeat(50));
            log("ViperLevels Debug Logger Initialized");
            log("Version: " + plugin.getDescription().getVersion());
            log("=".repeat(50));

        } catch (IOException e) {
            plugin.logError("Failed to initialize debug logger: " + e.getMessage());
        }
    }

    public static void log(String message) {
        if (!initialized || writer == null) {
            return;
        }

        String timestamp = dateFormat.format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        
        writer.println(logMessage);
        writer.flush();
    }

    public static void logException(Throwable throwable) {
        if (!initialized || writer == null) {
            return;
        }

        log("Exception occurred: " + throwable.getClass().getSimpleName());
        log("Message: " + throwable.getMessage());
        throwable.printStackTrace(writer);
        writer.flush();
    }

    public static void logValidation(String player, String material, boolean passed, String requirements) {
        if (!initialized) {
            return;
        }

        log("VALIDATION | Player: " + player + " | Material: " + material + 
            " | Result: " + (passed ? "PASS" : "FAIL") + 
            (requirements != null ? " | Requirements: " + requirements : ""));
    }

    public static void logBypass(String player, String target, String action) {
        if (!initialized) {
            return;
        }

        log("BYPASS | Player: " + player + " | Target: " + target + " | Action: " + action);
    }

    public static void shutdown() {
        if (writer != null) {
            log("Debug logger shutting down");
            log("=".repeat(50));
            writer.close();
            writer = null;
            initialized = false;
        }
    }
}