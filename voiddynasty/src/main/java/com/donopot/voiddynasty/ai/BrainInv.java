package com.donopot.voiddynasty.ai;
import com.donopot.voiddynasty.work.WorkInventory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.List;
/** Persistent companion bag, separate from receipt-owned work inventory. */
public final class BrainInv {
    public static final int SLOTS=27;
    private BrainInv() {}
    public static List<ItemStack> of(LivingEntity bot) {
        return bot==null ? List.of() : WorkInventory.copy(BrainInventoryState.get((ServerLevel)bot.level()).bag(bot.getUUID()));
    }
    /** Atomic: a full bag leaves both input and stored stacks unchanged. */
    public static boolean add(LivingEntity bot,ItemStack stack) {
        if(bot==null || stack==null || stack.isEmpty()) return false;
        BrainInventoryState state=BrainInventoryState.get((ServerLevel)bot.level());
        List<ItemStack> result=WorkInventory.insert(state.bag(bot.getUUID()),SLOTS,List.of(stack));
        if(result==null) return false;
        state.replace(bot.getUUID(),result);stack.setCount(0);return true;
    }
    public static int count(LivingEntity bot) {return of(bot).stream().mapToInt(ItemStack::getCount).sum();}
    /** Debit only successfully spawned stacks; a failed spawn preserves the stored remainder. */
    public static void dropAll(ServerLevel level,LivingEntity bot) {
        if(bot==null || bot.level()!=level) return;
        BrainInventoryState state=BrainInventoryState.get(level);
        List<ItemStack> remaining=of(bot);
        var iterator=remaining.iterator();
        while(iterator.hasNext()) {
            ItemStack stack=iterator.next();
            if(bot.spawnAtLocation(level,stack.copy())!=null) iterator.remove();
        }
        state.replace(bot.getUUID(),remaining);
    }
    public static String summary(LivingEntity bot) {
        List<ItemStack> stacks;
        try { stacks=of(bot); }
        catch(IllegalStateException ex) {return "RECOVERY_REQUIRED: "+ex.getMessage();}
        if(stacks.isEmpty()) return "vide";
        return String.join(", ",stacks.stream().limit(8).map(s ->
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem())+" x"+s.getCount()).toList());
    }
}
