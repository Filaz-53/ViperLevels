package com.viperlevels.integration;

import com.viperlevels.ViperLevels;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemsAdderIntegration {

    private static ItemsAdderIntegration instance;

    private final ViperLevels plugin;
    private final boolean enabled;

    private ItemsAdderIntegration() {
        this.plugin = ViperLevels.getInstance();
        this.enabled = plugin.isItemsAdderEnabled();
        
        if (enabled) {
            plugin.logInfo("ItemsAdder integration enabled");
        }
    }

    public static ItemsAdderIntegration getInstance() {
        if (instance == null) {
            instance = new ItemsAdderIntegration();
        }
        return instance;
    }

    public boolean isCustomItem(ItemStack item) {
        if (!enabled || item == null) {
            return false;
        }

        try {
            CustomStack customStack = CustomStack.byItemStack(item);
            return customStack != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCustomItemId(ItemStack item) {
        if (!enabled || item == null) {
            return null;
        }

        try {
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack != null) {
                return customStack.getNamespacedID();
            }
        } catch (Exception e) {
            plugin.logDebug("Error getting custom item ID: " + e.getMessage());
        }

        return null;
    }

    public ItemStack getCustomItem(String namespacedId) {
        if (!enabled || namespacedId == null) {
            return null;
        }

        try {
            CustomStack customStack = CustomStack.getInstance(namespacedId);
            if (customStack != null) {
                return customStack.getItemStack();
            }
        } catch (Exception e) {
            plugin.logDebug("Error getting custom item: " + e.getMessage());
        }

        return null;
    }

    public boolean exists(String namespacedId) {
        if (!enabled || namespacedId == null) {
            return false;
        }

        try {
            return CustomStack.getInstance(namespacedId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}