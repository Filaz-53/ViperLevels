package com.viperlevels.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface InventoryHandler {
    void handle(Player player, InventoryClickEvent event);
}