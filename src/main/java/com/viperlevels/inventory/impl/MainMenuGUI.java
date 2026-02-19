package com.viperlevels.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.InventoryGUI;
import com.viperlevels.inventory.gui.GUIManager;
import org.bukkit.entity.Player;

public class MainMenuGUI extends InventoryGUI {

    public MainMenuGUI(Player player) {
        super(player, "&8&lViperLevels Menu", 5);
    }

    @Override
    public void build() {
        fillBorder(XMaterial.GRAY_STAINED_GLASS_PANE);

        setButton(InventoryButton.builder()
            .slot(11)
            .material(XMaterial.BOOK)
            .name("&e&lVisualizza Regole")
            .lore(
                "&7Mostra tutte le regole attive",
                "&7e i requisiti necessari",
                "",
                "&aClick per aprire"
            )
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new RulesViewGUI(p)))
            .build());

        setButton(InventoryButton.builder()
            .slot(13)
            .material(XMaterial.PLAYER_HEAD)
            .name("&b&lIl Mio Stato")
            .lore(
                "&7Visualizza i tuoi livelli mcMMO",
                "&7e cosa puoi utilizzare",
                "",
                "&aClick per aprire"
            )
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new PlayerStatusGUI(p)))
            .build());

        setButton(InventoryButton.builder()
            .slot(15)
            .material(XMaterial.DIAMOND)
            .name("&d&lAnteprima Oggetti")
            .lore(
                "&7Visualizza oggetti bloccati",
                "&7e i loro requisiti",
                "",
                "&aClick per aprire"
            )
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new ItemPreviewGUI(p)))
            .build());

        if (player.hasPermission("viperlevels.admin")) {
            setButton(InventoryButton.builder()
                .slot(31)
                .material(XMaterial.COMMAND_BLOCK)
                .name("&c&lGestione Bypass")
                .lore(
                    "&7Gestisci i bypass dei giocatori",
                    "",
                    "&cSolo Admin",
                    "&aClick per aprire"
                )
                .handler((p, e) -> GUIManager.getInstance().openGUI(p, new BypassManageGUI(p)))
                .build());
        }

        setButton(InventoryButton.builder()
            .slot(40)
            .material(XMaterial.BARRIER)
            .name("&cChiudi")
            .handler((p, e) -> p.closeInventory())
            .build());
    }
}