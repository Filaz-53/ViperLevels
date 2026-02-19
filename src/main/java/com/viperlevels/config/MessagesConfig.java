package com.viperlevels.config;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MessagesConfig {

    private final String prefix;
    private final Map<String, String> messages;

    public MessagesConfig(FileConfiguration config) {
        this.messages = new HashMap<>();
        
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            this.prefix = "&8[&cViperLevels&8]&r ";
            loadDefaults();
            return;
        }

        this.prefix = colorize(messagesSection.getString("prefix", "&8[&cViperLevels&8]&r "));
        
        loadMessages(messagesSection, "");
    }

    private void loadMessages(ConfigurationSection section, String path) {
        for (String key : section.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            
            if (section.isConfigurationSection(key)) {
                loadMessages(section.getConfigurationSection(key), fullPath);
            } else {
                String value = section.getString(key);
                if (value != null) {
                    messages.put(fullPath, colorize(value));
                }
            }
        }
    }

    private void loadDefaults() {
        messages.put("no-permission", colorize("&cNon hai il permesso per fare questo!"));
        messages.put("player-not-found", colorize("&cGiocatore non trovato!"));
        messages.put("reload-success", colorize("&aConfigurazione ricaricata con successo!"));
    }

    public String getMessage(String path) {
        return messages.getOrDefault(path, colorize("&cMessaggio non trovato: " + path));
    }

    public String getMessageWithPrefix(String path) {
        return prefix + getMessage(path);
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}