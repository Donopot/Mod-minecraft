package com.donopot.voiddynasty.machine;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Vue combinée de plusieurs {@link Container} (blocs fusionnés) en un seul
 * inventaire continu, exposée à la GUI scrollable.
 */
public class AggregatedContainer implements Container {

    private final List<Container> parts;
    private final int[] offsets;

    public AggregatedContainer(List<Container> parts) {
        this.parts = parts;
        this.offsets = new int[parts.size() + 1];
        for (int i = 0; i < parts.size(); i++) {
            offsets[i + 1] = offsets[i] + parts.get(i).getContainerSize();
        }
    }

    private int locate(int slot) {
        for (int i = 0; i < parts.size(); i++) {
            if (slot < offsets[i + 1]) return i;
        }
        return parts.size() - 1;
    }

    @Override
    public int getContainerSize() {
        return offsets[parts.size()];
    }

    @Override
    public boolean isEmpty() {
        for (Container c : parts) {
            if (!c.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int i = locate(slot);
        return parts.get(i).getItem(slot - offsets[i]);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        int i = locate(slot);
        return parts.get(i).removeItem(slot - offsets[i], count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int i = locate(slot);
        return parts.get(i).removeItemNoUpdate(slot - offsets[i]);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int i = locate(slot);
        parts.get(i).setItem(slot - offsets[i], stack);
        parts.get(i).setChanged();
    }

    @Override
    public void setChanged() {
        for (Container c : parts) c.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        for (Container c : parts) {
            if (!c.stillValid(player)) return false;
        }
        return true;
    }

    @Override
    public void clearContent() {
        for (Container c : parts) c.clearContent();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        int i = locate(slot);
        return parts.get(i).canPlaceItem(slot - offsets[i], stack);
    }
}
