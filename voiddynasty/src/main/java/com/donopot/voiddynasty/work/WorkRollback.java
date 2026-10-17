package com.donopot.voiddynasty.work;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Plan all debits before restoring a block. Failure keeps both resources and receipt. */
public final class WorkRollback {
    private WorkRollback() {}

    public static boolean apply(List<ItemStack> before, List<ItemStack> drops,
                                BooleanSupplier restore, Consumer<List<ItemStack>> write) {
        List<ItemStack> after = WorkInventory.debit(before, drops);
        if (after == null || !restore.getAsBoolean()) return false;
        write.accept(after);
        return true;
    }
}
