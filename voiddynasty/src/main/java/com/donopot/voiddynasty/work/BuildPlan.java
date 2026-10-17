package com.donopot.voiddynasty.work;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Versioned, deterministic plans. Changing an existing plan requires a new plan id. */
public record BuildPlan(String blueprint, String material, BlockPos anchor) {
    public static final Set<String> IDS = Set.of("platform_3x3_v1", "shelter_5x5_v1");
    public static final Set<String> MATERIALS = Set.of("minecraft:cobblestone", "minecraft:stone", "minecraft:oak_planks", "minecraft:spruce_planks");
    public BuildPlan {
        if (!IDS.contains(blueprint) || !MATERIALS.contains(material)) throw new IllegalArgumentException("Unsupported blueprint or material");
        anchor = anchor.immutable();
    }
    public List<BlockPos> blocks() {
        List<BlockPos> blocks = new ArrayList<>();
        int width = blueprint.equals("platform_3x3_v1") ? 3 : 5;
        int height = width == 3 ? 1 : 5;
        // Bottom up, perimeter before inner roof. Door faces negative Z.
        for (int y=0;y<height;y++) for (int z=0;z<width;z++) for (int x=0;x<width;x++) {
            if (y>0 && y<4 && x>0 && x<4 && z>0 && z<4) continue;
            if (width==5 && z==0 && (x==1 || x==2) && (y==1 || y==2)) continue;
            blocks.add(anchor.offset(x,y,z));
        }
        return List.copyOf(blocks);
    }
    public BlockPos max() { return anchor.offset(blueprint.equals("platform_3x3_v1") ? 2 : 4, blueprint.equals("platform_3x3_v1") ? 0 : 4, blueprint.equals("platform_3x3_v1") ? 2 : 4); }
    public BlockState state() { return BuiltInRegistries.BLOCK.getValue(Identifier.parse(material)).defaultBlockState(); }
    public ItemStack cost(int count) { return new ItemStack(state().getBlock().asItem(), count); }
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag(); tag.putString("id",blueprint); tag.putString("material",material); tag.putLong("anchor",anchor.asLong()); return tag;
    }
    public static BuildPlan load(CompoundTag tag) { return new BuildPlan(tag.getString("id").orElseThrow(),tag.getString("material").orElseThrow(),BlockPos.of(tag.getLong("anchor").orElseThrow())); }
}
