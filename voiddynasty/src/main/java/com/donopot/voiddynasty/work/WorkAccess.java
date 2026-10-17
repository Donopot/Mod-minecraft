package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import java.util.*;

/** Opt-in horizontal excavation. Width accounts for the servitor's 1.09-block hitbox. */
public final class WorkAccess {
    public static final int MAX_BLOCKS = 256, SEARCH_NODES = 512;
    private static final Set<String> ROCK = Set.of("minecraft:stone", "minecraft:deepslate", "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:granite", "minecraft:andesite", "minecraft:diorite", "minecraft:tuff");
    private WorkAccess() {}
    static boolean enabled(WorkState s) { WorkMission m=MissionService.active(s); return m!=null && m.build==null && m.access.equals("excavate"); }
    static boolean contains(WorkMission m,BlockPos p) { return p.getX()>=m.min.getX() && p.getX()<=m.max.getX() && p.getY()>=m.min.getY() && p.getY()<=m.max.getY() && p.getZ()>=m.min.getZ() && p.getZ()<=m.max.getZ(); }
    static boolean safe(ServerLevel level,WorkState s,BlockPos p) {
        WorkMission m=MissionService.active(s);
        if (m==null || !contains(m,p) || !WorkService.loadedNeighborhood(level,p) || ClaimIntegration.isClaimProtected(level,p)) return false;
        var block=level.getBlockState(p);
        if (!ROCK.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString()) && !MissionService.matches(m,block)) return false;
        if (block.hasBlockEntity() || !block.getFluidState().isEmpty() || WorkService.touchesFluid(level,p)) return false;
        // Never open a falling ceiling or undermine attached containers.
        var above=level.getBlockState(p.above());
        return !(above.getBlock() instanceof net.minecraft.world.level.block.FallingBlock) && !above.hasBlockEntity();
    }
    static List<BlockPos> clearance(BlockPos feet) {
        List<BlockPos> cells=new ArrayList<>();
        for(int y=1;y>=0;y--) for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) cells.add(feet.offset(x,y,z));
        return cells;
    }
    private static boolean passable(ServerLevel level,WorkState s,BlockPos feet) {
        if (level.isOutsideBuildHeight(feet.above()) || !level.getWorldBorder().isWithinBounds(feet)) return false;
        for(BlockPos cell:clearance(feet)) {
            if(!WorkService.loadedNeighborhood(level,cell) || ClaimIntegration.isClaimProtected(level,cell)) return false;
            var block=level.getBlockState(cell);
            if(!block.isAir() && !safe(level,s,cell)) return false;
            if(WorkService.touchesFluid(level,cell)) return false;
        }
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) {
            BlockPos floor=feet.offset(x,-1,z);
            if(!level.hasChunkAt(floor) || !level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP)
                    || !level.getBlockState(floor).getFluidState().isEmpty()) return false;
        }
        return true;
    }
    static List<BlockPos> route(ServerLevel level,WorkState s,BlockPos start,BlockPos target) {
        if(target.getY()<start.getY() || target.getY()>start.getY()+1) return List.of();
        Map<BlockPos,BlockPos> parents=new HashMap<>(); ArrayDeque<BlockPos> queue=new ArrayDeque<>();
        parents.put(start,start); queue.add(start);
        while(!queue.isEmpty() && parents.size()<=SEARCH_NODES) {
            BlockPos cell=queue.removeFirst();
            if(!cell.equals(start) && clearance(cell).contains(target)) {
                List<BlockPos> path=new ArrayList<>();
                while(!cell.equals(start)) {path.add(cell);cell=parents.get(cell);}
                Collections.reverse(path);return path;
            }
            // Aim toward the target first, but allow a bounded detour around protected obstacles.
            List<BlockPos> neighbors=new ArrayList<>();
            for(Direction d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)) neighbors.add(cell.relative(d));
            neighbors.sort(Comparator.comparingDouble(p->p.distSqr(target)));
            for(BlockPos next:neighbors) {
                if(parents.containsKey(next) || Math.abs(next.getX()-start.getX())>24 || Math.abs(next.getZ()-start.getZ())>24) continue;
                if(!passable(level,s,next)) continue;
                parents.put(next,cell); queue.addLast(next);
            }
        }
        return List.of();
    }
    /** Work through one clearance cell at a time; every block uses the normal mining receipt path. */
    static void tick(ServerLevel level,WorkState s,LivingEntity bot,BlockPos target) {
        if(!(bot instanceof Mob mob)) return;
        WorkMission m=MissionService.active(s);
        if(s.accessRoute.isEmpty()) {
            s.accessRoute.addAll(route(level,s,bot.blockPosition(),target));
            if(s.accessRoute.isEmpty()) {WorkService.pause(s,"ACCESS_NO_SAFE_ROUTE");return;}
        }
        BlockPos next=s.accessRoute.getFirst();
        if(!passable(level,s,next)) {s.accessRoute.clear();WorkService.pause(s,"ACCESS_TERRAIN_CHANGED");return;}
        for(BlockPos p:clearance(next)) {
            if(level.getBlockState(p).isAir()) continue;
            if(m.accessMined>=MAX_BLOCKS) {WorkService.pause(s,"ACCESS_BUDGET_EXHAUSTED");return;}
            if(WorkService.distanceSq(bot,p)>WorkService.reach()*WorkService.reach() || !WorkService.visible(level,bot,p)) continue;
            mob.getNavigation().stop();
            if(!p.equals(s.accessBlock)) {s.accessBlock=p; s.progress=0;}
            s.progress+=WorkService.speedMult()/20.0;
            if(s.progress>=WorkService.breakSeconds(level,s,p,level.getBlockState(p))) WorkService.breakTarget(level,s,bot,p,true);
            return;
        }
        boolean clear=clearance(next).stream().allMatch(p->level.getBlockState(p).isAir());
        if(!clear) {WorkService.pause(s,"ACCESS_OUT_OF_REACH");return;}
        // Vanilla centers wide mobs on a multi-cell node, not at block + 0.5.
        double offset=Math.floor(bot.getBbWidth()+1.0)*.5;
        if(bot.distanceToSqr(next.getX()+offset,next.getY(),next.getZ()+offset)<1.0) {s.accessRoute.removeFirst();s.accessBlock=null;return;}
        if(level.getGameTime()>=s.nextPathTick) {
            s.nextPathTick=level.getGameTime()+10;
            var path=mob.getNavigation().createPath(next,0);
            if(path==null || !path.canReach() || !mob.getNavigation().moveTo(path,1.0)) {
                s.accessRoute.clear();WorkService.pause(s,"ACCESS_PATH_CHANGED");s.lastStop="ACCESS_PATH_CHANGED: "+bot.position()+" -> "+next.toShortString();
            }
        }
    }
}
