package com.donopot.voiddynasty.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Base commune des « super conteneurs » (super coffre 2x2 / super conteneur 6x2) :
 * grand inventaire, persistance NBT, et détection de la fusion multi-bloc
 * (agrégation des inventaires des blocs adjacents en une seule vue).
 */
public abstract class AbstractSuperContainerBlockEntity extends BaseContainerBlockEntity {

    private NonNullList<ItemStack> items;

    protected AbstractSuperContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> incoming) {
        if (incoming == items) return;
        items.clear();
        items.addAll(incoming);
    }

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

    /**
     * Détecte un rectangle complet WxH de blocs de même type autour de la position
     * du bloc, et retourne les Container (BlockEntity) des blocs du rectangle.
     * Essaie les deux orientations (WxH et HxW) si W != H.
     */
    protected List<Container> findFusion(int w, int h) {
        Level level = this.level;
        if (level == null) return List.of();
        Block block = getBlockState().getBlock();
        List<Container> result = scanRectangle(level, worldPosition, block, w, h);
        if (!result.isEmpty()) return result;
        if (w != h) return scanRectangle(level, worldPosition, block, h, w);
        return result;
    }

    private static List<Container> scanRectangle(Level level, BlockPos pos, Block block, int w, int h) {
        for (int ox = 0; ox < w; ox++) {
            for (int oz = 0; oz < h; oz++) {
                BlockPos corner = pos.offset(-ox, 0, -oz);
                List<Container> parts = new ArrayList<>(w * h);
                boolean ok = true;
                outer:
                for (int dx = 0; dx < w; dx++) {
                    for (int dz = 0; dz < h; dz++) {
                        BlockPos p = corner.offset(dx, 0, dz);
                        if (level.getBlockState(p).is(block)) {
                            BlockEntity be = level.getBlockEntity(p);
                            if (be instanceof Container c) {
                                parts.add(c);
                            } else {
                                ok = false;
                                break outer;
                            }
                        } else {
                            ok = false;
                            break outer;
                        }
                    }
                }
                if (ok && parts.size() == w * h) {
                    return parts;
                }
            }
        }
        return List.of();
    }

    /** Crée le menu en agrégeant les blocs fusionnés (ou juste ce bloc si pas de fusion). */
    protected Container buildAggregate(int w, int h) {
        List<Container> parts = findFusion(w, h);
        if (parts.size() <= 1) return this;
        return new AggregatedContainer(parts);
    }
}
