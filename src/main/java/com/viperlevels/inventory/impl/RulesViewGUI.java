package com.viperlevels.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.PaginatedGUI;
import com.viperlevels.inventory.gui.GUIManager;
import com.viperlevels.rule.Rule;
import com.viperlevels.rule.RuleManager;
import com.viperlevels.rule.RuleType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RulesViewGUI extends PaginatedGUI {

    private final RuleManager ruleManager;
    private RuleType filterType;

    public RulesViewGUI(Player player) {
        super(player, "&8&lRegole Attive", 6);
        this.ruleManager = RuleManager.getInstance();
        this.filterType = null;
    }

    @Override
    protected List<?> getItems() {
        List<Rule> allRules = new ArrayList<>();
        
        if (filterType == null) {
            for (RuleType type : RuleType.values()) {
                allRules.addAll(ruleManager.getAllRulesForType(type));
            }
        } else {
            allRules.addAll(ruleManager.getAllRulesForType(filterType));
        }
        
        return allRules;
    }

    @Override
    protected InventoryButton createItemButton(Object item, int slot) {
        Rule rule = (Rule) item;
        
        XMaterial displayMaterial = getDisplayMaterial(rule.getType());
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Tipo: &e" + rule.getType().name());
        lore.add("&7Azioni: &e" + rule.getActions().size());
        lore.add("");
        lore.add("&7Requisiti:");
        lore.add("&e" + rule.getCondition().toString());
        
        return InventoryButton.builder()
            .slot(slot)
            .material(displayMaterial)
            .name("&6" + rule.getIdentifier())
            .lore(lore)
            .build();
    }

    @Override
    public void build() {
        super.build();
        
        setButton(InventoryButton.builder()
            .slot(0)
            .material(XMaterial.COMPASS)
            .name("&e&lFiltri")
            .lore(
                "&7Filtro attuale: &e" + (filterType == null ? "Tutti" : filterType.name()),
                "",
                "&aClick sinistro: &7Prossimo filtro",
                "&aClick destro: &7Reset filtro"
            )
            .handler((p, e) -> {
                if (e.isLeftClick()) {
                    cycleFilter();
                } else if (e.isRightClick()) {
                    filterType = null;
                    currentPage = 0;
                    refresh();
                }
            })
            .build());
        
        setButton(InventoryButton.builder()
            .slot(8)
            .material(XMaterial.ARROW)
            .name("&aIndietro")
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new MainMenuGUI(p)))
            .build());
    }

    private void cycleFilter() {
        if (filterType == null) {
            filterType = RuleType.values()[0];
        } else {
            int nextIndex = (filterType.ordinal() + 1) % RuleType.values().length;
            filterType = RuleType.values()[nextIndex];
        }
        currentPage = 0;
        refresh();
    }

    private XMaterial getDisplayMaterial(RuleType type) {
        switch (type) {
            case ITEM: return XMaterial.DIAMOND_SWORD;
            case BLOCK: return XMaterial.STONE;
            case ARMOR: return XMaterial.DIAMOND_CHESTPLATE;
            case POTION: return XMaterial.POTION;
            case FOOD: return XMaterial.COOKED_BEEF;
            case MOB: return XMaterial.ZOMBIE_HEAD;
            case DIMENSION: return XMaterial.ENDER_PEARL;
            case ENCHANTING: return XMaterial.ENCHANTING_TABLE;
            default: return XMaterial.PAPER;
        }
    }
}