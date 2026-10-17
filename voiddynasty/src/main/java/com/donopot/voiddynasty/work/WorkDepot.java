package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** The worker builds from its operator-supplied kit, never from journalled mining output. */
final class WorkDepot {
    private WorkDepot() {}
    private static final java.util.List<BlockPos> OFFSETS = offsets();
    private static java.util.List<BlockPos> offsets() {
        var list = new java.util.ArrayList<BlockPos>();
        for (int x = -6; x <= 6; x++) for (int z = -6; z <= 6; z++) for (int y = -1; y <= 1; y++)
            list.add(new BlockPos(x, y, z));
        list.sort(java.util.Comparator.comparingInt(p -> p.getX()*p.getX() + 3*p.getY()*p.getY() + p.getZ()*p.getZ()));
        return java.util.List.copyOf(list);
    }

    static void request(WorkState state) {
        state.buildingDepot = true;
        state.depotPlanned = false;
        state.depotBuildTicks = 0;
        state.depotSearchCursor = 0;
        state.setDirty();
    }

    static boolean inside(WorkState s, BlockPos p) {
        return p.getX() >= s.minX && p.getX() <= s.maxX && p.getY() >= s.minY && p.getY() <= s.maxY
                && p.getZ() >= s.minZ && p.getZ() <= s.maxZ;
    }

    private static boolean site(ServerLevel level, WorkState s, LivingEntity bot, BlockPos p) {
        if (inside(s, p) || inside(s, p.below()) || s.depots.contains(p)) return false;
        if (level.isOutsideBuildHeight(p.below()) || level.isOutsideBuildHeight(p.above())) return false;
        if (!level.hasChunkAt(p) || !level.hasChunkAt(p.below()) || !level.hasChunkAt(p.above())) return false;
        if (!level.getWorldBorder().isWithinBounds(p) || !level.getBlockState(p).isAir()
                || !level.getBlockState(p.above()).isAir()) return false;
        if (ClaimIntegration.isClaimProtected(level, p) || ClaimIntegration.isClaimProtected(level, p.below())) return false;
        if (!level.getEntities(bot, new AABB(p)).isEmpty() || bot.getBoundingBox().intersects(new AABB(p))) return false;
        for (Direction d : Direction.values()) {
            BlockPos neighbor = p.relative(d);
            if (!level.hasChunkAt(neighbor) || !level.getBlockState(neighbor).getFluidState().isEmpty()) return false;
            if (d.getAxis().isHorizontal() && level.getBlockState(neighbor).is(Blocks.CHEST)) return false;
        }
        return level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)
                || (s.supplySupports > 0 && level.getBlockState(p.below()).isAir());
    }

    static boolean tick(ServerLevel level, WorkState s, LivingEntity bot) {
        if (s.supplyChests <= 0) {
            WorkService.pause(s, "kit de depots epuise — sac conserve"); return false;
        }
        if (!s.depotPlanned) {
            // Search around the actual worker, not the upper corner of the mining selection.
            BlockPos center = bot.blockPosition();
            for (int scan = 0; scan < 64; scan++) {
                BlockPos p = center.offset(OFFSETS.get(s.depotSearchCursor++ % OFFSETS.size()));
                if (!site(level, s, bot, p)) continue;
                // An unsupported site is only usable if the worker can lay its footing now.
                if (level.getBlockState(p.below()).isAir()
                        && (WorkService.distanceSq(bot, p.below()) > WorkService.reach() * WorkService.reach()
                        || !WorkService.visible(level, bot, p.below()))) continue;
                if (WorkService.distanceSq(bot, p) > WorkService.reach() * WorkService.reach()
                        || !WorkService.visible(level, bot, p)) {
                    var path = WorkService.findWorkPath(level, bot, p);
                    // At most one expensive path search per preparation tick.
                    if (path == null || !path.canReach()) { s.setDirty(); return false; }
                    if (bot instanceof Mob mob) mob.getNavigation().moveTo(path, 1.0);
                    s.nextPathTick = level.getGameTime() + 20;
                }
                s.depotPlanned = true;
                s.planX = p.getX(); s.planY = p.getY(); s.planZ = p.getZ();
                s.lastStop = "construction du depot en " + p.toShortString();
                break;
            }
            s.setDirty();
            if (!s.depotPlanned) { s.lastStop = "recherche d'un emplacement de depot par le servitor"; return false; }
        }
        BlockPos p = new BlockPos(s.planX, s.planY, s.planZ);
        if (!site(level, s, bot, p)) { s.depotPlanned = false; s.depotBuildTicks = 0; s.setDirty(); return false; }
        BlockPos action = level.getBlockState(p.below()).isAir() ? p.below() : p;
        if (WorkService.distanceSq(bot, action) > WorkService.reach() * WorkService.reach()
                || !WorkService.visible(level, bot, action)) {
            WorkService.stepToward(level, s, bot, action); return false;
        }
        if (bot instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(action.getX() + 0.5, action.getY() + 0.5, action.getZ() + 0.5);
        }
        bot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(action.equals(p.below()) ? Blocks.COBBLESTONE : Blocks.CHEST));
        if (s.depotBuildTicks++ % 5 == 0) bot.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        s.setDirty();
        if (s.depotBuildTicks < 10) return false;
        s.depotBuildTicks = 0;
        if (action.equals(p.below())) {
            if (level.setBlock(action, Blocks.COBBLESTONE.defaultBlockState(), 3)) s.supplySupports--;
            return false;
        }
        if (!level.setBlock(p, Blocks.CHEST.defaultBlockState(), 3)) { s.depotPlanned = false; return false; }
        s.supplyChests--;
        s.hasDepot = true; s.depotX = p.getX(); s.depotY = p.getY(); s.depotZ = p.getZ();
        s.depots.add(p.immutable()); s.depotCount = s.depots.size();
        s.buildingDepot = false; s.depotPlanned = false; s.lastStop = "depot construit par le servitor";
        bot.setItemSlot(EquipmentSlot.MAINHAND, s.tool.copy());
        s.setDirty();
        return true;
    }
}
