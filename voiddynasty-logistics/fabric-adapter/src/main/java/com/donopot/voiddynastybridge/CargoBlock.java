package com.donopot.voiddynastybridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class CargoBlock extends BaseEntityBlock {
    public CargoBlock(Properties p){super(p);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new CargoBlockEntity(p,s);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!l.isClientSide() && l.getBlockEntity(p) instanceof CargoBlockEntity c && BridgeMod.service!=null && BridgeMod.service.matches(c) && !c.locked())player.openMenu(c);
        return InteractionResult.SUCCESS;
    }
}
