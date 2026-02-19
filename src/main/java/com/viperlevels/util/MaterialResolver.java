package com.viperlevels.util;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.integration.ItemsAdderIntegration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MaterialResolver {

    private static final ViperLevels plugin = ViperLevels.getInstance();
    private static final ItemsAdderIntegration itemsAdder = ItemsAdderIntegration.getInstance();

    public static String resolveItemIdentifier(ItemStack item) {
        if (item == null) {
            return null;
        }

        if (itemsAdder.isEnabled() && itemsAdder.isCustomItem(item)) {
            String customId = itemsAdder.getCustomItemId(item);
            if (customId != null) {
                plugin.logDebug("Resolved custom item: " + customId);
                return customId;
            }
        }

        return resolveVanillaMaterial(item.getType());
    }

    public static String resolveBlockIdentifier(Block block) {
        if (block == null) {
            return null;
        }

        return resolveVanillaMaterial(block.getType());
    }

    public static String resolveMaterialIdentifier(Material material) {
        return resolveVanillaMaterial(material);
    }

    private static String resolveVanillaMaterial(Material material) {
        XMaterial xMaterial = XMaterial.matchXMaterial(material);

        return xMaterial.name();

    }

    public static Material parseMaterial(String identifier) {
        if (identifier == null) {
            return null;
        }

        if (identifier.contains(":") && itemsAdder.isEnabled()) {
            ItemStack customItem = itemsAdder.getCustomItem(identifier);
            if (customItem != null) {
                return customItem.getType();
            }
        }

        XMaterial xMaterial = XMaterial.matchXMaterial(Material.valueOf(identifier));
        return xMaterial.parseMaterial();

    }

    public static boolean isCustomItem(String identifier) {
        return identifier != null && identifier.contains(":") && itemsAdder.isEnabled() && itemsAdder.exists(identifier);
    }

    public static boolean matchesIdentifier(ItemStack item, String identifier) {
        String itemIdentifier = resolveItemIdentifier(item);
        return itemIdentifier != null && itemIdentifier.equalsIgnoreCase(identifier);
    }

    public static boolean matchesIdentifier(Block block, String identifier) {
        String blockIdentifier = resolveBlockIdentifier(block);
        return blockIdentifier != null && blockIdentifier.equalsIgnoreCase(identifier);
    }
}