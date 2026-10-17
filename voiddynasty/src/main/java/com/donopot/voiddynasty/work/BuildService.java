package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import java.util.*;

/** Real material reservation and one verified physical placement at a time. */
public final class BuildService {
    private BuildService() {}
    static String validate(ServerLevel level,WorkMission m) {
        Set<BlockPos> cells=new HashSet<>(m.build.blocks());
        for(BlockPos pos:BlockPos.betweenClosed(m.min,m.max)) {
            if(level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !WorkService.loadedNeighborhood(level,pos) || ClaimIntegration.isClaimProtected(level,pos)) return "BUILD_SITE_UNAVAILABLE";
            if(!level.getBlockState(pos).isAir()) return "BUILD_SITE_OCCUPIED";
            if(WorkService.touchesFluid(level,pos)) return "BUILD_SITE_UNSAFE";
            if(pos.getY()==m.min.getY()) {
                BlockPos floor=pos.below();
                if(!level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP)
                        || ClaimIntegration.isClaimProtected(level,floor)) return "BUILD_FOUNDATION_REQUIRED";
            }
        }
        if(cells.contains(m.destination) || (m.destination.getX()>=m.min.getX() && m.destination.getX()<=m.max.getX()
                && m.destination.getY()>=m.min.getY() && m.destination.getY()<=m.max.getY()
                && m.destination.getZ()>=m.min.getZ() && m.destination.getZ()<=m.max.getZ())) return "BUILD_SOURCE_INSIDE_SITE";
        return "";
    }
    static void tick(ServerLevel level,WorkState s,LivingEntity bot,WorkMission m) {
        if(s.buildingDepot || !s.hasDepot) {WorkDepot.tick(level,s,bot);return;}
        if(!m.materialsReserved) {
            m.status="RESERVING";
            Container source=MissionService.destination(level,m);
            if(source==null) {WorkService.pause(s,"MATERIAL_SOURCE_UNAVAILABLE");return;}
            if(WorkService.distanceSq(bot,m.destination)>WorkService.reach()*WorkService.reach() || !WorkService.visible(level,bot,m.destination)) {
                WorkService.stepToward(level,s,bot,m.destination);return;
            }
            String invalid=validate(level,m);if(!invalid.isEmpty()) {WorkService.pause(s,invalid);return;}
            List<ItemStack> before=new ArrayList<>();for(int i=0;i<source.getContainerSize();i++) before.add(source.getItem(i).copy());
            ItemStack cost=m.build.cost(m.amount);
            List<ItemStack> after=WorkInventory.debit(before,List.of(cost));
            List<ItemStack> bag=WorkInventory.insert(s.invSlots,WorkState.INV_SLOTS,List.of(cost));
            if(after==null) {WorkService.pause(s,"MATERIALS_MISSING");return;}
            if(bag==null) {WorkService.pause(s,"MATERIAL_CARRY_FULL");return;}
            for(int i=0;i<after.size();i++) source.setItem(i,after.get(i));source.setChanged();
            s.invSlots.clear();s.invSlots.addAll(bag);m.materialsReserved=true;m.collected=m.amount;s.setDirty();
        }
        List<BlockPos> blocks=m.build.blocks();
        if(m.placed>=blocks.size()) {
            for(BlockPos pos:blocks) if(!level.hasChunkAt(pos) || !level.getBlockState(pos).equals(m.build.state())) {WorkService.pause(s,"BUILD_VERIFICATION_FAILED");return;}
            if(!s.invSlots.isEmpty()) {WorkService.pause(s,"BUILD_UNEXPECTED_REMAINDER");return;}
            WorkService.conclude(level,s);return;
        }
        m.status="BUILDING";s.setDirty();
        BlockPos pos=blocks.get(m.placed);
        if(!WorkService.loadedNeighborhood(level,pos) || ClaimIntegration.isClaimProtected(level,pos)) {WorkService.pause(s,"BUILD_SITE_UNAVAILABLE");return;}
        if(!level.getBlockState(pos).isAir()) {WorkService.pause(s,"BUILD_TERRAIN_CHANGED");return;}
        if(WorkService.touchesFluid(level,pos)) {WorkService.pause(s,"BUILD_SITE_UNSAFE");return;}
        AABB box=new AABB(pos);
        if(WorkService.distanceSq(bot,pos)>WorkService.reach()*WorkService.reach() || !WorkService.visible(level,bot,pos) || bot.getBoundingBox().intersects(box)) {
            WorkService.stepToPlacement(level,s,bot,pos);return;
        }
        if(!level.getEntities(bot,box,e->!e.isSpectator()).isEmpty()) {WorkService.pause(s,"BUILD_ENTITY_IN_WAY");return;}
        boolean attached=false;
        for(Direction d:Direction.values()) {BlockPos neighbor=pos.relative(d);if(level.getBlockState(neighbor).isFaceSturdy(level,neighbor,d.getOpposite())) attached=true;}
        if(!attached || !m.build.state().canSurvive(level,pos)) {WorkService.pause(s,"BUILD_SUPPORT_MISSING");return;}
        if(bot instanceof Mob mob) {mob.getNavigation().stop();mob.getLookControl().setLookAt(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);}
        bot.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,m.build.cost(1));
        if(m.buildTicks==0) bot.swing(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT,true);
        if(m.buildTicks<6) m.buildTicks++;
        if(m.buildTicks<6) return;
        if(s.journal.size()>=WorkState.JOURNAL_MAX) {WorkService.pause(s,"BUILD_JOURNAL_FULL");return;}
        ItemStack cost=m.build.cost(1);
        List<ItemStack> after=WorkInventory.debit(s.invSlots,List.of(cost));
        if(after==null) {WorkService.pause(s,"BUILD_MATERIALS_LOST");return;}
        if(!level.setBlock(pos,m.build.state(),2|16) || !level.getBlockState(pos).equals(m.build.state())) {WorkService.pause(s,"BUILD_PLACEMENT_REFUSED");return;}
        WorkState.Entry entry=new WorkState.Entry(pos.immutable(),NbtUtils.writeBlockState(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()),level.getGameTime());
        entry.placed=NbtUtils.writeBlockState(m.build.state());entry.dropsKnown=true;entry.drops=List.of(cost.copy());
        s.journal.add(entry);s.invSlots.clear();after.stream().filter(stack->!stack.isEmpty()).forEach(s.invSlots::add);
        m.placed++;m.delivered=m.placed;m.buildTicks=0;s.done++;s.setDirty();
    }
}
