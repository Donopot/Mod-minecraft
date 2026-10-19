package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import java.util.*;

/** Excavated landings: three blocks long/wide, at most one block up or down per landing.
 * Existing floors are never mined or supplied. Every return step remains climbable. */
final class WorkStairs {
    static final int MAX_RISE = 32, RADIUS = 48;
    private WorkStairs() {}

    /**
     * Support d'une marche : UN bloc porteur sous les pieds. Une marche descend d'un cran vers
     * l'avant, donc creuser la suivante ne retire jamais le support de celle ou l'on se tient.
     * Les quatre voisins du dessous sont seulement verifies comme non fluides (pas de croute
     * au-dessus de la lave).
     */
    static boolean floor(ServerLevel level, BlockPos feet) {
        BlockPos p=feet.below();
        if (!WorkService.loadedNeighborhood(level,p) || level.isOutsideBuildHeight(p)
                || !level.getWorldBorder().isWithinBounds(p) || ClaimIntegration.isClaimProtected(level,p)) return false;
        var state=level.getBlockState(p);
        if (!state.isFaceSturdy(level,p,Direction.UP) || !state.getFluidState().isEmpty()
                || state.hasBlockEntity() || state.getBlock() instanceof net.minecraft.world.level.block.FallingBlock
                || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                || WorkService.touchesFluid(level,p)) return false;
        for (Direction d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)) {
            BlockPos side=p.relative(d);
            if (!level.hasChunkAt(side)) return false;
            if (!level.getBlockState(side).getFluidState().isEmpty() || WorkService.touchesFluid(level,side)) return false;
        }
        return true;
    }

    /** Extra headroom above the lower landing permits the jump on the way back up. */
    static List<BlockPos> clearance(BlockPos from, BlockPos to) {
        var cells=new LinkedHashSet<BlockPos>();
        int ceiling=Math.max(from.getY(),to.getY())+1;
        for (BlockPos feet:List.of(from,to)) for (int y=ceiling;y>=feet.getY();y--)
            for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) cells.add(new BlockPos(feet.getX()+x,y,feet.getZ()+z));
        return List.copyOf(cells);
    }

    static boolean safe(ServerLevel level, WorkState s, BlockPos from, BlockPos to) {
        if (!floor(level,from) || !floor(level,to)) return false;
        for (BlockPos p:clearance(from,to)) {
            if (!WorkService.loadedNeighborhood(level,p) || level.isOutsideBuildHeight(p)
                    || !level.getWorldBorder().isWithinBounds(p) || ClaimIntegration.isClaimProtected(level,p)
                    || WorkService.touchesFluid(level,p)) return false;
            if (!level.getBlockState(p).isAir() && !WorkAccess.safe(level,s,p)) return false;
        }
        return true;
    }

    private record Node(BlockPos pos, int cost, int score) {}
    record Plan(BlockPos start,List<BlockPos> path) {}

    /** A resumed worker can spawn beside its depot. Walk to a free landing rather than
     * treating that chest as rock to clear, or editing the previously excavated floor. */
    static Plan plan(ServerLevel level,WorkState s,LivingEntity bot,BlockPos target) {
        BlockPos start=bot.blockPosition();
        if(safe(level,s,start,start)) return new Plan(start,route(level,s,start,target));
        if(!(bot instanceof Mob mob)) return new Plan(start,List.of());
        var candidates=new ArrayList<BlockPos>();
        for(int x=-2;x<=2;x++) for(int z=-2;z<=2;z++) candidates.add(start.offset(x,0,z));
        candidates.sort(Comparator.comparingDouble(p->p.distSqr(start)));
        int paths=0;
        for(BlockPos candidate:candidates) {
            if(!safe(level,s,candidate,candidate) || clearance(candidate,candidate).stream().anyMatch(p->!level.getBlockState(p).isAir())) continue;
            if(paths++>=8) break;
            var approach=mob.getNavigation().createPath(candidate,0);
            if(approach==null || !approach.canReach()) continue;
            var path=new ArrayList<>(route(level,s,candidate,target));
            if(!path.isEmpty()) path.addFirst(candidate);
            return new Plan(candidate,path);
        }
        return new Plan(start,List.of());
    }
    private static int estimate(BlockPos p,BlockPos target) {
        return (Math.abs(p.getX()-target.getX())+Math.abs(p.getZ()-target.getZ()))/3
                + Math.max(0,Math.abs(p.getY()-target.getY())-1)*2;
    }

    static List<BlockPos> route(ServerLevel level,WorkState s,BlockPos start,BlockPos target) {
        if (Math.abs(start.getY()-target.getY())>MAX_RISE+1 || !floor(level,start)) return List.of();
        var parents=new HashMap<BlockPos,BlockPos>();
        var costs=new HashMap<BlockPos,Integer>();
        var queue=new PriorityQueue<Node>(Comparator.comparingInt(Node::score).thenComparingInt(Node::cost));
        parents.put(start,start);costs.put(start,0);queue.add(new Node(start,0,estimate(start,target)));
        int examined=0;
        while (!queue.isEmpty() && examined++<WorkAccess.SEARCH_NODES) {
            Node current=queue.remove();BlockPos cell=current.pos;
            if (costs.get(cell)!=current.cost) continue;
            if (!cell.equals(start) && WorkAccess.clearance(cell).contains(target)) {
                var path=new ArrayList<BlockPos>();
                while (!cell.equals(start)) {path.add(cell);cell=parents.get(cell);}
                Collections.reverse(path);return path;
            }
            var nextCells=new ArrayList<BlockPos>();
            for (Direction d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST))
                for (int dy=-1;dy<=1;dy++) nextCells.add(cell.relative(d,3).above(dy));
            nextCells.sort(Comparator.comparingInt(p->estimate(p,target)));
            for (BlockPos next:nextCells) {
                if (Math.abs(next.getX()-start.getX())>RADIUS || Math.abs(next.getZ()-start.getZ())>RADIUS
                        || Math.abs(next.getY()-start.getY())>MAX_RISE || parents.containsKey(next)) continue;
                // Distinct footprints preserve all previously planned floors, including at turns.
                boolean overlaps=false;BlockPos ancestor=cell;
                while (true) {
                    if (ancestor.getX()==next.getX() && ancestor.getZ()==next.getZ()) {overlaps=true;break;}
                    if (ancestor.equals(start)) break;ancestor=parents.get(ancestor);
                }
                if (overlaps || !safe(level,s,cell,next)) continue;
                int cost=current.cost+1;
                parents.put(next,cell);costs.put(next,cost);queue.add(new Node(next,cost,cost+estimate(next,target)));
                // Bound queued nodes as well as expanded nodes.
                if (parents.size()>=WorkAccess.SEARCH_NODES) break;
            }
            if (parents.size()>=WorkAccess.SEARCH_NODES) break;
        }
        return List.of();
    }
}
