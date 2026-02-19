package com.viperlevels.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.viperlevels.ViperLevels;
import com.viperlevels.cache.CacheManager;
import com.viperlevels.cache.CachedSkillData;
import com.viperlevels.condition.ConditionEvaluator;
import com.viperlevels.condition.McMMOSkill;
import com.viperlevels.inventory.InventoryButton;
import com.viperlevels.inventory.InventoryGUI;
import com.viperlevels.inventory.gui.GUIManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerStatusGUI extends InventoryGUI {

    private final CacheManager cacheManager;

    public PlayerStatusGUI(Player player) {
        super(player, "&8&lIl Mio Stato", 6);
        this.cacheManager = CacheManager.getInstance();
    }

    @Override
    public void build() {
        fillBorder(XMaterial.GRAY_STAINED_GLASS_PANE);

        cacheManager.getOrLoad(player.getUniqueId(), player.getName()).thenAccept(data -> {
            if (data == null) {
                setButton(InventoryButton.builder()
                    .slot(22)
                    .material(XMaterial.BARRIER)
                    .name("&cErrore")
                    .lore("&7Impossibile caricare i dati mcMMO")
                    .build());
                return;
            }

            displaySkills(data);
        });

        setButton(InventoryButton.builder()
            .slot(49)
            .material(XMaterial.ARROW)
            .name("&aIndietro")
            .handler((p, e) -> GUIManager.getInstance().openGUI(p, new MainMenuGUI(p)))
            .build());
    }

    private void displaySkills(CachedSkillData data) {
        Map<McMMOSkill, Integer> levels = data.getSkillLevels();
        
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30};
        int index = 0;

        for (McMMOSkill skill : McMMOSkill.values()) {
            if (index >= slots.length) break;
            
            int level = levels.getOrDefault(skill, 0);
            XMaterial displayMaterial = getSkillMaterial(skill);
            
            List<String> lore = new ArrayList<>();
            lore.add("&7Livello: &e" + level);
            lore.add("");
            
            if (level >= 100) {
                lore.add("&a&lMASTER");
            } else if (level >= 50) {
                lore.add("&e&lEXPERT");
            } else if (level >= 25) {
                lore.add("&6&lADVANCED");
            } else {
                lore.add("&7&lBEGINNER");
            }
            
            setButton(InventoryButton.builder()
                .slot(slots[index])
                .material(displayMaterial)
                .name("&6" + skill.getMcmmoName())
                .lore(lore)
                .amount(Math.max(1, Math.min(64, level)))
                .build());
            
            index++;
        }
    }

    private XMaterial getSkillMaterial(McMMOSkill skill) {
        switch (skill) {
            case MINING: return XMaterial.DIAMOND_PICKAXE;
            case WOODCUTTING: return XMaterial.DIAMOND_AXE;
            case EXCAVATION: return XMaterial.DIAMOND_SHOVEL;
            case HERBALISM: return XMaterial.WHEAT;
            case FISHING: return XMaterial.FISHING_ROD;
            case ARCHERY: return XMaterial.BOW;
            case SWORDS: return XMaterial.DIAMOND_SWORD;
            case AXES: return XMaterial.IRON_AXE;
            case UNARMED: return XMaterial.LEATHER;
            case TAMING: return XMaterial.BONE;
            case REPAIR: return XMaterial.ANVIL;
            case ACROBATICS: return XMaterial.FEATHER;
            case ALCHEMY: return XMaterial.BREWING_STAND;
            case SMELTING: return XMaterial.FURNACE;
            case SALVAGE: return XMaterial.IRON_INGOT;
            case CROSSBOWS: return XMaterial.CROSSBOW;
            case TRIDENTS: return XMaterial.TRIDENT;
            default: return XMaterial.PAPER;
        }
    }
}