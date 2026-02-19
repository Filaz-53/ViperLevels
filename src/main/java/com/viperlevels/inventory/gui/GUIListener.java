package com.viperlevels.inventory.gui;

import com.viperlevels.ViperLevels;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.InventoryGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GUIListener implements Listener {

    private final ViperLevels plugin;
    private final GUIManager guiManager;

    public GUIListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.guiManager = GUIManager.getInstance();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryGUI gui = guiManager.getOpenGUI(player);

        if (gui == null) {
            return;
        }

        if (!event.getInventory().equals(gui.getInventory())) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= gui.getSize()) {
            return;
        }

        InventoryButton button = gui.getButton(slot);
        if (button != null && button.getHandler() != null) {
            button.getHandler().handle(player, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        guiManager.closeGUI(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        guiManager.closeGUI(event.getPlayer());
    }
}