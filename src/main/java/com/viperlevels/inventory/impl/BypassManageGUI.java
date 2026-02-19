package com.viperlevels.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.database.DatabaseManager;
import com.viperlevels.database.repository.BypassRepository;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.PaginatedGUI;
import com.viperlevels.inventory.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BypassManageGUI extends PaginatedGUI {

    private final BypassRepository bypassRepository;
    private List<Map<String, Object>> bypasses;

    public BypassManageGUI(Player player) {
        super(player, "&8&lGestione Bypass", 6);
        this.bypassRepository = DatabaseManager.getInstance().getBypassRepository();
        this.bypasses = new ArrayList<>();
        loadBypasses();
    }

    private void loadBypasses() {
        bypassRepository.getAllBypasses().thenAccept(data -> {
            this.bypasses = data;
            Bukkit.getScheduler().runTask(ViperLevels.getInstance(), this::refresh);
        });
    }

    @Override
    protected List<?> getItems() {
        return bypasses;
    }

    @Override
    protected InventoryButton createItemButton(Object item, int slot) {
        Map<String, Object> bypassData = (Map<String, Object>) item;
        
        String playerName = (String) bypassData.get("player_name");
        String bypassType = (String) bypassData.get("bypass_type");
        String bypassTarget = (String) bypassData.get("bypass_target");
        Long expiresAt = (Long) bypassData.get("expires_at");
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Giocatore: &e" + playerName);
        lore.add("&7Tipo: &e" + bypassType);
        
        if (bypassTarget != null) {
            lore.add("&7Target: &e" + bypassTarget);
        }
        
        if (expiresAt != null) {
            long remaining = expiresAt - System.currentTimeMillis();
            if (remaining > 0) {
                long hours = remaining / (1000 * 60 * 60);
                lore.add("&7Scade tra: &e" + hours + " ore");
            } else {
                lore.add("&cScaduto");
            }
        } else {
            lore.add("&7Durata: &aPermanente");
        }
        
        lore.add("");
        lore.add("&cClick destro per rimuovere");
        
        return InventoryButton.builder()
            .slot(slot)
            .material(XMaterial.PLAYER_HEAD)
            .name("&6Bypass: " + playerName)
            .lore(lore)
            .handler((p, e) -> {
                if (e.isRightClick()) {
                    String uuidStr = (String) bypassData.get("uuid");
                    UUID uuid = UUID.fromString(uuidStr);
                    bypassRepository.removeBypass(uuid).thenRun(() -> {
                        p.sendMessage("&aBypass rimosso per " + playerName);
                        loadBypasses();
                    });
                }
            })
            .build();
    }

    @Override
    public void build() {
        super.build();
        
        setButton(InventoryButton.builder()
            .slot(0)
            .material(XMaterial.EMERALD)
            .name("&a&lAggiungi Bypass")
            .lore(
                "&7Usa il comando:",
                "&e/viperlevels bypass <player> <type>",
                "",
                "&7per aggiungere un nuovo bypass"
            )
            .build());
        
        setButton(InventoryButton.builder()
            .slot(8)
            .material(XMaterial.ARROW)
            .name("&aIndietro")
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new MainMenuGUI(p)))
            .build());
    }
}