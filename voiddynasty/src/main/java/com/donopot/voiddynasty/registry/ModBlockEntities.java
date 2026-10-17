package com.donopot.voiddynasty.registry;

import com.donopot.voiddynasty.machine.MiningRigBlockEntity;
import com.donopot.voiddynasty.machine.SuperChestBlockEntity;
import com.donopot.voiddynasty.machine.SuperContainerBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Registre des BlockEntity du mod (machines + super conteneurs).
 */
public final class ModBlockEntities {

    public static final BlockEntityType<MiningRigBlockEntity> MINING_RIG = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "mining_rig"),
            new BlockEntityType<>(MiningRigBlockEntity::new, Set.of(ModBlocks.MINING_RIG))
    );

    public static final BlockEntityType<SuperChestBlockEntity> SUPER_CHEST = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "super_chest"),
            new BlockEntityType<>(SuperChestBlockEntity::new, Set.of(ModBlocks.SUPER_CHEST))
    );

    public static final BlockEntityType<SuperContainerBlockEntity> SUPER_CONTAINER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "super_container"),
            new BlockEntityType<>(SuperContainerBlockEntity::new, Set.of(ModBlocks.SUPER_CONTAINER))
    );

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
