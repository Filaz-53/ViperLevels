package com.viperlevels.inventory;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class PaginatedGUI extends InventoryGUI {
    protected int currentPage;
    protected final int itemsPerPage;
    protected final List<Integer> itemSlots;

    public PaginatedGUI(Player player, String title, int rows) {
        super(player, title, rows);
        this.currentPage = 0;
        this.itemSlots = new ArrayList<>();
        calculateItemSlots(rows);
        this.itemsPerPage = itemSlots.size();
    }

    private void calculateItemSlots(int rows) {
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                itemSlots.add(row * 9 + col);
            }
        }
    }

    protected abstract List<?> getItems();

    protected abstract InventoryButton createItemButton(Object item, int slot);

    @Override
    public void build() {
        fillBorder(XMaterial.GRAY_STAINED_GLASS_PANE);

        List<?> items = getItems();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < itemSlots.size()) {
                int slot = itemSlots.get(slotIndex);
                InventoryButton button = createItemButton(items.get(i), slot);
                setButton(button);
            }
        }

        if (currentPage > 0) {
            setButton(InventoryButton.builder()
                .slot(size - 9)
                .material(XMaterial.ARROW)
                .name("&aPagina Precedente")
                .lore("&7Pagina " + currentPage + "/" + totalPages)
                .handler((p, e) -> {
                    currentPage--;
                    refresh();
                })
                .build());
        }

        if (currentPage < totalPages - 1) {
            setButton(InventoryButton.builder()
                .slot(size - 1)
                .material(XMaterial.ARROW)
                .name("&aPagina Successiva")
                .lore("&7Pagina " + (currentPage + 2) + "/" + totalPages)
                .handler((p, e) -> {
                    currentPage++;
                    refresh();
                })
                .build());
        }

        setButton(InventoryButton.builder()
            .slot(size - 5)
            .material(XMaterial.BARRIER)
            .name("&cChiudi")
            .handler((p, e) -> p.closeInventory())
            .build());
    }

    public void nextPage() {
        currentPage++;
        refresh();
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
        }
    }
}