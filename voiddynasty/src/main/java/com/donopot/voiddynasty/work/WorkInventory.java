package com.donopot.voiddynasty.work;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import java.util.ArrayList;
import java.util.List;

/** Copy-on-write inventory operations shared by mining and rollback. Never discard a remainder. */
public final class WorkInventory {
    private WorkInventory() {}

    public static List<ItemStack> copy(List<ItemStack> input) {
        return new ArrayList<>(input.stream().map(ItemStack::copy).toList());
    }

    /** Returns a complete replacement, or null without touching the original inventory. */
    public static List<ItemStack> insert(List<ItemStack> inventory, int slots, List<ItemStack> incoming) {
        List<ItemStack> result = copy(inventory);
        result.removeIf(ItemStack::isEmpty);
        for (ItemStack source : incoming) {
            ItemStack left = source.copy();
            for (ItemStack slot : result) {
                if (left.isEmpty()) break;
                if (!ItemStack.isSameItemSameComponents(slot, left)) continue;
                int put = Math.min(left.getCount(), Math.max(0, slot.getMaxStackSize() - slot.getCount()));
                slot.grow(put);
                left.shrink(put);
            }
            while (!left.isEmpty()) {
                if (result.size() >= slots) return null;
                int put = Math.min(left.getCount(), left.getMaxStackSize());
                if (put <= 0) return null;
                result.add(left.copyWithCount(put));
                left.shrink(put);
            }
        }
        return result;
    }

    /** Exact component-aware debit across fixed slots. Empty slots stay in place. */
    public static List<ItemStack> debit(List<ItemStack> inventory, List<ItemStack> required) {
        List<ItemStack> result = copy(inventory);
        for (ItemStack request : required) {
            int left = request.getCount();
            for (ItemStack slot : result) {
                if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, request)) continue;
                int take = Math.min(left, slot.getCount());
                slot.shrink(take);
                left -= take;
                if (left == 0) break;
            }
            if (left != 0) return null;
        }
        return result;
    }

    public record Transfer(List<ItemStack> bag, List<ItemStack> container, int count) {}

    /** Plan a capped delivery, respecting the real destination's stack limits, without mutations. */
    public static Transfer transfer(List<ItemStack> bag, Container destination,
                                    java.util.function.Predicate<ItemStack> matches, int limit) {
        List<ItemStack> source = copy(bag), target = new ArrayList<>();
        for (int i = 0; i < destination.getContainerSize(); i++) target.add(destination.getItem(i).copy());
        int moved = 0;
        for (ItemStack stack : source) {
            if (stack.isEmpty() || !matches.test(stack)) continue;
            for (int pass = 0; pass < 2 && moved < limit; pass++) {
                for (int i = 0; i < target.size() && !stack.isEmpty() && moved < limit; i++) {
                    ItemStack slot = target.get(i);
                    if (pass == 0 ? slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, stack) : !slot.isEmpty()) continue;
                    int capacity = Math.min(destination.getMaxStackSize(stack), stack.getMaxStackSize());
                    int put = Math.min(Math.min(limit - moved, stack.getCount()), Math.max(0, capacity - slot.getCount()));
                    if (put <= 0) continue;
                    if (slot.isEmpty()) target.set(i, stack.copyWithCount(put)); else slot.grow(put);
                    stack.shrink(put); moved += put;
                }
            }
        }
        source.removeIf(ItemStack::isEmpty);
        return new Transfer(source, target, moved);
    }
}
