package com.donopot.voiddynasty.machine;

import com.donopot.voiddynasty.registry.ModBlockEntities;
import com.donopot.voiddynasty.registry.ModBlocks;
import com.donopot.voiddynasty.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extracteur Minier : extrait automatiquement le minerai de céramite dans un rayon
 * en consommant du prométhium, et stocke les lingots dans son buffer de sortie.
 * Slot 0 = carburant, slots 1-2 = sortie.
 */
public class MiningRigBlockEntity extends BlockEntity {

    public static final int RADIUS = 5;
    private static final int CYCLE_TICKS = 40; // 2 secondes

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public MiningRigBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINING_RIG, pos, state);
    }

    public NonNullList<ItemStack> items() {
        return items;
    }

    // ---- persistance NBT ----
    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        ContainerHelper.loadAllItems(in, items);
    }

    // ---- tick ----
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (level.isClientSide() || !(be instanceof MiningRigBlockEntity rig)) return;
        rig.serverTick((ServerLevel) level, pos);
    }

    private void serverTick(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % CYCLE_TICKS != 0) return;
        ItemStack fuel = items.get(0);
        if (fuel.isEmpty() || fuel.getItem() != ModItems.PROMETHIUM) return;

        BlockPos ore = findOre(level, pos);
        if (ore == null) return;

        // mine le minerai (sans drop) + consomme 1 carburant
        level.setBlock(ore, Blocks.AIR.defaultBlockState(), 3);
        fuel.shrink(1);
        if (fuel.isEmpty()) items.set(0, ItemStack.EMPTY);

        // produit un lingot de céramite (drop au monde si le buffer est plein)
        ItemStack leftover = addOutput(new ItemStack(ModItems.CERAMITE_INGOT));
        if (!leftover.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover);
        }
        setChanged();
    }

    private BlockPos findOre(ServerLevel level, BlockPos center) {
        int r = RADIUS;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (level.getBlockState(p).is(ModBlocks.CERAMITE_ORE)) return p;
                }
            }
        }
        return null;
    }

    /** Ajoute l'item au buffer de sortie ; retourne le reliquat non stocké. */
    private ItemStack addOutput(ItemStack out) {
        for (int i = 1; i < items.size(); i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                items.set(i, out);
                return ItemStack.EMPTY;
            }
            if (slot.getItem() == out.getItem() && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int add = Math.min(space, out.getCount());
                slot.grow(add);
                out.shrink(add);
                if (out.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return out;
    }

    /** Insère du prométhium comme carburant. Retourne true si du carburant a été consommé. */
    public boolean insertFuel(ItemStack held) {
        if (held.getItem() != ModItems.PROMETHIUM) return false;
        ItemStack fuel = items.get(0);
        if (fuel.isEmpty()) {
            items.set(0, held.copy());
            held.setCount(0);
        } else if (fuel.getCount() < fuel.getMaxStackSize()) {
            int space = fuel.getMaxStackSize() - fuel.getCount();
            int add = Math.min(space, held.getCount());
            fuel.grow(add);
            held.shrink(add);
        } else {
            return false;
        }
        setChanged();
        return true;
    }

    /** Récupère les lingots produits (dans l'inventaire du joueur, sinon au sol). */
    public void popOutputs(Player player) {
        for (int i = 1; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (!s.isEmpty()) {
                if (!player.getInventory().add(s)) {
                    Containers.dropItemStack(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), s);
                }
                items.set(i, ItemStack.EMPTY);
            }
        }
        setChanged();
    }
}
