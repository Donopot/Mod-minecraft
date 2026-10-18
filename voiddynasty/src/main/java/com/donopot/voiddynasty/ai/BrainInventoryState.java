package com.donopot.voiddynasty.ai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.*;

/** Dimension-scoped, component-preserving bags. Invalid data is retained and locked. */
public final class BrainInventoryState extends SavedData {
    private record Bag(String owner, List<ItemStack> stacks) {}
    private static final Codec<Bag> BAG = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("owner").forGetter(Bag::owner),
            ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(Bag::stacks)
    ).apply(i, Bag::new));
    private static final Codec<BrainInventoryState> FORMAT = BAG.listOf().fieldOf("bags").codec()
            .xmap(BrainInventoryState::restore, s -> s.bags.entrySet().stream()
                    .map(e -> new Bag(e.getKey().toString(), e.getValue())).toList());
    public static final Codec<BrainInventoryState> CODEC = new Codec<>() {
        public <T> DataResult<T> encode(BrainInventoryState s, DynamicOps<T> ops, T prefix) {
            return s.recovery == null ? FORMAT.encode(s, ops, prefix) : CompoundTag.CODEC.encode(s.recovery, ops, prefix);
        }
        public <T> DataResult<Pair<BrainInventoryState,T>> decode(DynamicOps<T> ops,T input) {
            try {
                var decoded=FORMAT.decode(ops,input);
                if(decoded.result().isPresent()) return decoded;
            } catch(RuntimeException ignored) {}
            return CompoundTag.CODEC.parse(ops,input).map(raw -> {
                BrainInventoryState locked=new BrainInventoryState();locked.recovery=raw.copy();
                return Pair.of(locked,ops.empty());
            });
        }
    };
    private final Map<UUID,List<ItemStack>> bags=new LinkedHashMap<>();
    private CompoundTag recovery;
    public static final SavedDataType<BrainInventoryState> TYPE=new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty","brain_inventory"),BrainInventoryState::new,CODEC,
            DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    private static BrainInventoryState restore(List<Bag> saved) {
        BrainInventoryState s=new BrainInventoryState();
        for(Bag b:saved) {
            UUID id=UUID.fromString(b.owner());
            if(s.bags.containsKey(id) || b.stacks().size()>BrainInv.SLOTS) throw new IllegalArgumentException("Invalid bag");
            for(ItemStack stack:b.stacks()) if(stack.isEmpty() || stack.getCount()>stack.getMaxStackSize())
                throw new IllegalArgumentException("Invalid stack");
            s.bags.put(id,com.donopot.voiddynasty.work.WorkInventory.copy(b.stacks()));
        }
        return s;
    }
    static BrainInventoryState get(ServerLevel level) {return level.getDataStorage().computeIfAbsent(TYPE);}
    List<ItemStack> bag(UUID id) {
        if(recovery!=null) throw new IllegalStateException("Sac incompatible: original conserve, intervention requise");
        return bags.getOrDefault(id,List.of());
    }
    void replace(UUID id,List<ItemStack> stacks) {
        bag(id);
        if(stacks.isEmpty()) bags.remove(id); else bags.put(id,com.donopot.voiddynasty.work.WorkInventory.copy(stacks));
        setDirty();
    }
}
