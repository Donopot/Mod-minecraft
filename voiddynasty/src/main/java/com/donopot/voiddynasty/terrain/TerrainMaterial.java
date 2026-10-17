package com.donopot.voiddynasty.terrain;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Catégorie de matériau d'un bloc, avec sa résistance aux armes (spec
 * void_dynasty:terrain_rules/v1).
 */
public enum TerrainMaterial {
    FOLIAGE("foliage", 0.5f),
    GLASS("glass", 1.5f),
    DIRT("dirt_sand_gravel", 3f),
    WOOD("wood", 7f),
    STONE("stone", 16f),
    DEEPSLATE("deepslate", 24f),
    BRICK("brick", 26f),
    IRON("iron_block", 48f),
    OBSIDIAN("obsidian", 120f),
    /** bedrock, barrière, commandes… jamais endommagés. */
    PROTECTED("protected", 1_000_000f);

    public final String id;
    public final float resistance;

    TerrainMaterial(String id, float resistance) {
        this.id = id;
        this.resistance = resistance;
    }

    /** Catégorise un état de bloc. */
    public static TerrainMaterial categorize(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER
                || block == Blocks.COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK
                || block == Blocks.REPEATING_COMMAND_BLOCK || block == Blocks.STRUCTURE_BLOCK
                || block == Blocks.REINFORCED_DEEPSLATE
                || block.getExplosionResistance() >= 3_600_000f) {
            return PROTECTED;
        }
        if (state.is(BlockTags.LEAVES)) return FOLIAGE;
        SoundType st = state.getSoundType();
        if (st == SoundType.GLASS) return GLASS;
        if (st == SoundType.WOOD || state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) return WOOD;
        if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || st == SoundType.GRAVEL || st == SoundType.GRASS) return DIRT;
        if (state.is(BlockTags.STONE_BRICKS)) return BRICK;
        if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN) return OBSIDIAN;
        if (block == Blocks.IRON_BLOCK || block == Blocks.RAW_IRON_BLOCK) return IRON;
        if (isDeepslate(block)) return DEEPSLATE;
        if (st == SoundType.STONE || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)) return STONE;
        if (st == SoundType.METAL) return IRON;
        return STONE;
    }

    private static boolean isDeepslate(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath().contains("deepslate");
    }

    /** Blocs fragiles (feuillage, verre) : dégâts amplifiés. */
    public static boolean isFragile(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.getSoundType() == SoundType.GLASS;
    }

    /** Blocs sensibles à la chaleur (glace, neige, sable). */
    public static boolean isHeatSensitive(BlockState state) {
        return state.is(BlockTags.ICE) || state.is(BlockTags.SNOW) || state.is(BlockTags.SAND);
    }

    /** Blocs inflammables (bois, feuillage, laine). */
    public static boolean isFlammable(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.LEAVES)
                || state.getSoundType() == SoundType.WOOL;
    }
}
