package com.viperlevels.inventory;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class InventoryGUI {
    protected final Player player;
    protected final String title;
    protected final int size;
    protected final Inventory inventory;
    protected final Map<Integer, InventoryButton> buttons;

    public InventoryGUI(Player player, String title, int rows) {
        this.player = player;
        this.title = title;
        this.size = rows * 9;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.buttons = new HashMap<>();
    }

    public abstract void build();

    public void open() {
        build();
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();
        buttons.clear();
        build();
    }

    protected void setButton(InventoryButton button) {
        buttons.put(button.getSlot(), button);
        inventory.setItem(button.getSlot(), button.getItem());
    }

    protected void fillBorder(XMaterial material) {
        ItemStack borderItem = material.parseItem();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
    }

    protected void fillEmpty(XMaterial material) {
        ItemStack fillItem = material.parseItem();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillItem);
            }
        }
    }

    public InventoryButton getButton(int slot) {
        return buttons.get(slot);
    }

    public boolean hasButton(int slot) {
        return buttons.containsKey(slot);
    }
}