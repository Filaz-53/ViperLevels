package com.viperlevels.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.PaginatedGUI;
import com.viperlevels.inventory.gui.GUIManager;
import com.viperlevels.rule.Rule;
import com.viperlevels.rule.RuleManager;
import com.viperlevels.rule.RuleType;
import com.viperlevels.rule.RuleValidator;
import com.viperlevels.rule.ValidationResult;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ItemPreviewGUI extends PaginatedGUI {

    private final RuleManager ruleManager;
    private final RuleValidator ruleValidator;

    public ItemPreviewGUI(Player player) {
        super(player, "&8&lAnteprima Oggetti", 6);
        this.ruleManager = RuleManager.getInstance();
        this.ruleValidator = new RuleValidator();
    }

    @Override
    protected List<?> getItems() {
        List<Rule> itemRules = new ArrayList<>();
        itemRules.addAll(ruleManager.getAllRulesForType(RuleType.ITEM));
        itemRules.addAll(ruleManager.getAllRulesForType(RuleType.ARMOR));
        return itemRules;
    }

    @Override
    protected InventoryButton createItemButton(Object item, int slot) {
        Rule rule = (Rule) item;
        
        XMaterial material = XMaterial.matchXMaterial(rule.getIdentifier()).orElse(XMaterial.BARRIER);
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Tipo: &e" + rule.getType().name());
        lore.add("");
        lore.add("&7Requisiti:");
        lore.add("&e" + rule.getCondition().toString());
        lore.add("");
        
        ValidationResult validation = ruleValidator.validate(player, rule.getIdentifier(), 
                                                             rule.getType(), rule.getActions().get(0));
        
        if (validation.isPassed()) {
            lore.add("&a&l✓ Puoi usare questo oggetto");
        } else {
            lore.add("&c&l✗ Non puoi usare questo oggetto");
            lore.add("&7Mancanti:");
            for (String req : validation.formatMissingRequirements().split(", ")) {
                lore.add("  &c- " + req);
            }
        }
        
        return InventoryButton.builder()
            .slot(slot)
            .material(material)
            .name((validation.isPassed() ? "&a" : "&c") + rule.getIdentifier())
            .lore(lore)
            .build();
    }

    @Override
    public void build() {
        super.build();
        
        setButton(InventoryButton.builder()
            .slot(8)
            .material(XMaterial.ARROW)
            .name("&aIndietro")
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new MainMenuGUI(p)))
            .build());
    }
}