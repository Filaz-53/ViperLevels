package com.viperlevels.inventory.gui;

import com.viperlevels.ViperLevels;
import com.viperlevels.inventory.InventoryGUI;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class GUIManager {

    private static GUIManager instance;

    private final ViperLevels plugin;
    private final Map<UUID, InventoryGUI> openGUIs;

    private GUIManager(ViperLevels plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
    }

    public static GUIManager getInstance() {
        if (instance == null) {
            instance = new GUIManager(ViperLevels.getInstance());
        }
        return instance;
    }

    public void openGUI(Player player, InventoryGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
        gui.open();
    }

    public InventoryGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID uuid : openGUIs.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.closeInventory();
            }
        }
        openGUIs.clear();
    }
}