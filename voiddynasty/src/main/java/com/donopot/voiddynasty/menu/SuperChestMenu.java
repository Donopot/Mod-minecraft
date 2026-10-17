package com.donopot.voiddynasty.menu;

import com.donopot.voiddynasty.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu scrollable des « super conteneurs ». Affiche 6 rangées (54 slots) et
 * fait défiler une fenêtre sur l'inventaire agrégé via la molette.
 */
public class SuperChestMenu extends AbstractContainerMenu {

    private static final int SLOTS_PER_ROW = 9;
    private static final int VISIBLE_ROWS = 6;
    private static final int CONTAINER_SLOTS = SLOTS_PER_ROW * VISIBLE_ROWS; // 54

    private final Container container;
    private int scrollRow;
    private final int maxScrollRow;

    /** Côté client (supplier du MenuType). */
    public SuperChestMenu(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(CONTAINER_SLOTS));
    }

    /** Côté serveur (conteneur agrégé ou simple). */
    public SuperChestMenu(int syncId, Inventory playerInv, Container container) {
        super(ModMenuTypes.SUPER_CHEST, syncId);
        this.container = container;
        this.maxScrollRow = Math.max(0, (container.getContainerSize() + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW - VISIBLE_ROWS);
        this.scrollRow = 0;

        boolean scrollable = container.getContainerSize() > CONTAINER_SLOTS;
        for (int i = 0; i < CONTAINER_SLOTS; i++) {
            int x = 8 + (i % SLOTS_PER_ROW) * 18;
            int y = 18 + (i / SLOTS_PER_ROW) * 18;
            if (scrollable) {
                addSlot(new ScrolledSlot(container, i, x, y));
            } else {
                addSlot(new Slot(container, i, x, y));
            }
        }
        addStandardInventorySlots(playerInv, 8, 140);
    }

    public int getScrollRow() {
        return scrollRow;
    }

    public int getMaxScrollRow() {
        return maxScrollRow;
    }

    /** Met à jour la rangée de scroll (clampée). Le broadcast est fait par l'appelant serveur. */
    public void setScrollRow(int row) {
        this.scrollRow = Math.max(0, Math.min(row, maxScrollRow));
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ItemStack result = stack.copy();
            if (index < CONTAINER_SLOTS) {
                if (!this.moveItemStackTo(stack, CONTAINER_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            return result;
        }
        return ItemStack.EMPTY;
    }

    /** Slot dont l'index réel est décalé par le scroll. */
    private class ScrolledSlot extends Slot {
        private final int baseIndex;

        ScrolledSlot(Container container, int baseIndex, int x, int y) {
            super(container, baseIndex, x, y);
            this.baseIndex = baseIndex;
        }

        private int realIndex() {
            return baseIndex + scrollRow * SLOTS_PER_ROW;
        }

        @Override
        public int getContainerSlot() {
            return realIndex();
        }

        @Override
        public ItemStack getItem() {
            return container.getItem(realIndex());
        }

        @Override
        public void set(ItemStack stack) {
            container.setItem(realIndex(), stack);
            setChanged();
        }

        @Override
        public ItemStack remove(int count) {
            return container.removeItem(realIndex(), count);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(realIndex(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return container.getMaxStackSize();
        }
    }
}
