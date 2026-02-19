package com.viperlevels.inventory;

import com.cryptomorin.xseries.XMaterial;
import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class InventoryButton {
    private int slot;
    private ItemStack item;
    private InventoryHandler handler;

    public static class InventoryButtonBuilder {
        public InventoryButtonBuilder material(XMaterial material) {
            this.item = material.parseItem();
            return this;
        }

        public InventoryButtonBuilder name(String name) {
            if (this.item == null) {
                this.item = XMaterial.STONE.parseItem();
            }
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                this.item.setItemMeta(meta);
            }
            return this;
        }

        public InventoryButtonBuilder lore(List<String> lore) {
            if (this.item == null) {
                this.item = XMaterial.STONE.parseItem();
            }
            ItemMeta meta = this.item.getItemMeta();
            if (meta != null) {
                meta.setLore(lore);
                this.item.setItemMeta(meta);
            }
            return this;
        }

        public InventoryButtonBuilder lore(String... lines) {
            List<String> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(line);
            }
            return lore(lore);
        }

        public InventoryButtonBuilder amount(int amount) {
            if (this.item != null) {
                this.item.setAmount(amount);
            }
            return this;
        }
    }
}