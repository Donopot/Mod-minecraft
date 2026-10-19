package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import java.util.*;

/** Opt-in excavation. Width accounts for the servitor's 1.09-block hitbox. */
public final class WorkAccess {
    public static final int MAX_BLOCKS = 1024, SEARCH_NODES = 1024;
    // Surface et sous-sol meubles : sans ces blocs, un escalier d'acces ne peut pas demarrer sur
    // de l'herbe (le palier de depart exige une couronne 3x3 degageable).
    private static final Set<String> ROCK = Set.of("minecraft:stone", "minecraft:deepslate", "minecraft:dirt",
            "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:grass_block", "minecraft:podzol",
            "minecraft:mycelium", "minecraft:dirt_path", "minecraft:granite", "minecraft:andesite",
            "minecraft:diorite", "minecraft:tuff", "minecraft:calcite", "minecraft:gravel", "minecraft:sand",
            "minecraft:red_sand", "minecraft:sandstone", "minecraft:clay");
    private WorkAccess() {}
    static boolean enabled(WorkState s) { WorkMission m=MissionService.active(s); return m!=null && m.build==null && m.access.equals("excavate"); }
    static boolean contains(WorkMission m,BlockPos p) { return p.getX()>=m.min.getX() && p.getX()<=m.max.getX() && p.getY()>=m.min.getY() && p.getY()<=m.max.getY() && p.getZ()>=m.min.getZ() && p.getZ()<=m.max.getZ(); }
    static boolean safe(ServerLevel level,WorkState s,BlockPos p) {
        WorkMission m=MissionService.active(s);
        if (m==null || !contains(m,p) || level.isOutsideBuildHeight(p) || !level.getWorldBorder().isWithinBounds(p)
                || !WorkService.loadedNeighborhood(level,p) || ClaimIntegration.isClaimProtected(level,p)) return false;
        var block=level.getBlockState(p);
        // La vegetation (herbes, buissons, fleurs, pousses) se traverse sans outil : elle ne doit
        // pas bloquer un couloir d'acces.
        boolean vegetation = block.getBlock() instanceof net.minecraft.world.level.block.BushBlock;
        if (!vegetation && !ROCK.contains(BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString())
                && !MissionService.matches(m,block)) return false;
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
        // Un seul bloc porteur suffit : le compagnon peut longer une vire ou une berge.
        BlockPos floor=feet.below();
        if(!level.hasChunkAt(floor) || !level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP)
                || !level.getBlockState(floor).getFluidState().isEmpty()) return false;
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
    private static final Map<String,Integer> FLOOR_RETRIES = new HashMap<>();
    private static final Map<String,Integer> REACH_RETRIES = new HashMap<>();

    /**
     * Deplace le compagnon vers la surface porteuse 3x3 la plus proche quand il se tient sur une
     * pente. Borne a trois tentatives par mission : au-dela on laisse l'appelant pauser proprement.
     */
    private static boolean relocateToFloor(ServerLevel level, WorkState s, LivingEntity bot) {
        WorkMission m = MissionService.active(s);
        String key = m == null ? "" : m.id;
        int attempts = FLOOR_RETRIES.getOrDefault(key, 0);
        if (attempts >= 3) return false;
        if (!(bot instanceof Mob mob)) return false;
        BlockPos start = bot.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -6; dx <= 6; dx++) for (int dz = -6; dz <= 6; dz++) for (int dy = -2; dy <= 1; dy++) {
            BlockPos candidate = start.offset(dx, dy, dz);
            if (!level.hasChunkAt(candidate) || !WorkStairs.floor(level, candidate)) continue;
            if (!clearance(candidate).stream().allMatch(p -> level.getBlockState(p).isAir())) continue;
            double distance = candidate.distSqr(start);
            if (distance < bestDistance) { bestDistance = distance; best = candidate; }
        }
        if (best == null) return false;
        var path = mob.getNavigation().createPath(best, 0);
        if (path == null || !path.canReach()) return false;
        mob.getNavigation().moveTo(path, 1.0);
        FLOOR_RETRIES.put(key, attempts + 1);
        s.lastStop = "deplacement vers un palier stable " + best.toShortString() + " (tentative " + (attempts + 1) + "/3)";
        return true;
    }

    /** Work through one clearance cell at a time; every block uses the normal mining receipt path. */
    static void tick(ServerLevel level,WorkState s,LivingEntity bot,BlockPos target) {
        if(!(bot instanceof Mob mob)) return;
        WorkMission m=MissionService.active(s);
        if(s.accessRoute.isEmpty()) {
            s.accessFrom=null;
            // Un escalier ne peut pas partir d'un sol en pente ou d'une avancee : le palier de
            // depart exige une surface 3x3 porteuse. On se deplace d'abord vers une telle surface.
            if(!WorkStairs.floor(level,bot.blockPosition()) && relocateToFloor(level,s,bot)) return;
            s.accessRoute.addAll(route(level,s,bot.blockPosition(),target));
            if(s.accessRoute.isEmpty()) {
                var plan=WorkStairs.plan(level,s,bot,target);
                s.accessRoute.addAll(plan.path());
                if(!s.accessRoute.isEmpty()) s.accessFrom=plan.start().immutable();
            }
            if(s.accessRoute.isEmpty()) {
                BlockPos start=bot.blockPosition();
                s.lastStop="ACCESS_NO_SAFE_ROUTE: start="+start.toShortString()
                        +" target="+target.toShortString()
                        +" floor="+WorkStairs.floor(level,start)
                        +" safe="+WorkStairs.safe(level,s,start,start)
                        +" rise="+(start.getY()-target.getY())
                        +" enabled="+enabled(s);
                WorkService.pause(s,"ACCESS_NO_SAFE_ROUTE");
                return;
            }
            s.nextPathTick=0;
        }
        BlockPos next=s.accessRoute.getFirst();
        boolean stairs=s.accessFrom!=null;
        if(stairs ? !WorkStairs.safe(level,s,s.accessFrom,next) || s.accessRoute.stream().anyMatch(p->!WorkStairs.floor(level,p))
                : !passable(level,s,next)) {s.accessRoute.clear();s.accessFrom=null;WorkService.pause(s,"ACCESS_TERRAIN_CHANGED");return;}
        List<BlockPos> cells=stairs?WorkStairs.clearance(s.accessFrom,next):clearance(next);
        // Centre on the current landing before reaching down to the far edge of the next.
        // A spawn or a vanilla arrival can be half a block short, beyond mining reach.
        if(stairs && cells.stream().anyMatch(p->!level.getBlockState(p).isAir())
                && bot.onGround() && bot.blockPosition().getY()==s.accessFrom.getY()
                && clearance(s.accessFrom).stream().allMatch(p->level.getBlockState(p).isAir())) {
            double offset=Math.floor(bot.getBbWidth()+1.0)*.5;
            var centre=new net.minecraft.world.phys.Vec3(s.accessFrom.getX()+offset,s.accessFrom.getY(),s.accessFrom.getZ()+offset);
            double distance=bot.position().distanceToSqr(centre);
            if(distance>.04 && distance<2.25 && level.noCollision(bot,bot.getBoundingBox().move(centre.subtract(bot.position())))) {
                mob.getNavigation().stop();mob.getMoveControl().setWantedPosition(centre.x,centre.y,centre.z,1.0);return;
            }
        }
        for(BlockPos p:cells) {
            if(level.getBlockState(p).isAir()) continue;
            if(m.accessMined>=MAX_BLOCKS) {WorkService.pause(s,"ACCESS_BUDGET_EXHAUSTED");return;}
            if(WorkService.distanceSq(bot,p)>WorkService.reach()*WorkService.reach() || !WorkService.visible(level,bot,p)) continue;
            mob.getNavigation().stop();
            if(!p.equals(s.accessBlock)) {s.accessBlock=p; s.progress=0;}
            s.progress+=WorkService.speedMult()/20.0;
            if(s.progress>=WorkService.breakSeconds(level,s,p,level.getBlockState(p))) WorkService.breakTarget(level,s,bot,p,true);
            return;
        }
        boolean clear=cells.stream().allMatch(p->level.getBlockState(p).isAir());
        if(!clear) {
            // Bloc hors de portee : on s'approche au lieu d'abandonner (3 tentatives par mission).
            BlockPos stuck=cells.stream().filter(p->!level.getBlockState(p).isAir()).findFirst().orElse(next);
            String key=(m==null?"":m.id);
            int tries=REACH_RETRIES.getOrDefault(key,0);
            if(tries<3) {
                REACH_RETRIES.put(key,tries+1);
                var path=mob.getNavigation().createPath(stuck,1);
                if(path!=null && path.canReach()) mob.getNavigation().moveTo(path,1.0);
                s.lastStop="approche du bloc hors portee "+stuck.toShortString()+" (tentative "+(tries+1)+"/3)";
                return;
            }
            WorkService.pause(s,"ACCESS_OUT_OF_REACH");
            return;
        }
        // Vanilla centers wide mobs on a multi-cell node, not at block + 0.5.
        double offset=Math.floor(bot.getBbWidth()+1.0)*.5;
        if(bot.distanceToSqr(next.getX()+offset,next.getY(),next.getZ()+offset)<.36 && bot.onGround()) {
            s.accessRoute.removeFirst();s.accessBlock=null;if(stairs) s.accessFrom=next;
            if(s.accessRoute.isEmpty()) s.accessFrom=null;
            s.nextPathTick=0;return;
        }
        // Vanilla can finish short of the centre of a wide-mob node. Complete only the
        // last step within the cleared, supported landing; retain native collisions.
        var centre=new net.minecraft.world.phys.Vec3(next.getX()+offset,next.getY(),next.getZ()+offset);
        if(bot.onGround() && bot.blockPosition().getY()==next.getY() && bot.position().distanceToSqr(centre)<2.25
                && level.noCollision(bot,bot.getBoundingBox().move(centre.subtract(bot.position())))) {
            mob.getMoveControl().setWantedPosition(centre.x,centre.y,centre.z,1.0);return;
        }
        if(bot.onGround() && level.getGameTime()>=s.nextPathTick) {
            s.nextPathTick=level.getGameTime()+10;
            var path=mob.getNavigation().createPath(next,0);
            if(path==null || !path.canReach() || !mob.getNavigation().moveTo(path,1.0)) {
                s.accessRoute.clear();WorkService.pause(s,"ACCESS_PATH_CHANGED");s.lastStop="ACCESS_PATH_CHANGED: "+bot.position()+" -> "+next.toShortString();
            }
        }
    }
}
