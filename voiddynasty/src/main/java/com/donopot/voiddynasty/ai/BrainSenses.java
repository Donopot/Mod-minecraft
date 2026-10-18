package com.donopot.voiddynasty.ai;
import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.donopot.voiddynasty.work.MissionService;
import com.donopot.voiddynasty.work.WorkObservation;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** Bounded live readings; unknown/protected cells are never interpreted as empty. */
public final class BrainSenses {
    private BrainSenses() {}
    private static boolean readable(ServerLevel level,BlockPos p) {
        return !level.isOutsideBuildHeight(p) && level.getWorldBorder().isWithinBounds(p)
                && level.hasChunkAt(p) && !ClaimIntegration.isClaimProtected(level,p);
    }
    public static String summary(ServerLevel level,LivingEntity bot,int radius) {
        if(bot==null) return "aucun servitor";
        int r=Math.max(4,Math.min(radius,16));BlockPos c=bot.blockPosition();
        JsonObject spec=new JsonObject();spec.addProperty("schema",1);spec.addProperty("type","OBSERVE");spec.addProperty("limit",6);
        spec.add("min",MissionService.positionJson(c.offset(-r,-4,-r)));spec.add("max",MissionService.positionJson(c.offset(r,4,r)));
        return WorkObservation.inspect(level,spec).toString();
    }
    public static String find(ServerLevel level,LivingEntity bot,String block,int radius,int limit) {
        if(bot==null) return "aucun servitor";
        int r=Math.max(4,Math.min(radius,24)),max=Math.max(1,Math.min(limit,16));
        String wanted=BrainTools.normalizeBlock(block);BlockPos center=bot.blockPosition();
        List<BlockPos> found=new ArrayList<>();int observed=0,unknown=0;
        for(BlockPos p:BlockPos.betweenClosed(center.offset(-r,-4,-r),center.offset(r,4,r))) {
            if(!readable(level,p)) {unknown++;continue;}
            observed++;
            if(BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString().equals(wanted)) found.add(p.immutable());
        }
        found.sort(Comparator.<BlockPos>comparingDouble(p->p.distSqr(center)).thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ));
        JsonObject result=new JsonObject();result.addProperty("ok",true);result.addProperty("block",wanted);
        result.addProperty("sampled_tick",level.getGameTime());result.addProperty("dimension",level.dimension().identifier().toString());
        result.add("min",MissionService.positionJson(center.offset(-r,-4,-r)));result.add("max",MissionService.positionJson(center.offset(r,4,r)));
        result.addProperty("observed_cells",observed);result.addProperty("unknown_cells",unknown);
        result.addProperty("complete",unknown==0);result.addProperty("matches_total",found.size());
        result.addProperty("conclusive_absence",found.isEmpty() && unknown==0);
        result.addProperty("truncated",found.size()>max);JsonArray positions=new JsonArray();
        found.stream().limit(max).forEach(p->positions.add(MissionService.positionJson(p)));result.add("positions",positions);
        result.addProperty("scope","Exact blocks, vertical range +/-4 only; no route or drop guarantee");return result.toString();
    }
    public static String box(ServerLevel level,BlockPos center,int halfWidth,int layers) {
        int hw=Math.max(1,Math.min(halfWidth,4)),lh=Math.max(1,Math.min(layers,6));
        Map<String,Character> legend=new LinkedHashMap<>();StringBuilder out=new StringBuilder("?=inconnu/protege; .=air");
        for(int dy=lh;dy>=-lh;dy--) {
            out.append("\ny=").append(center.getY()+dy).append(" ");
            for(int x=-hw;x<=hw;x++) {
                for(int z=-hw;z<=hw;z++) {
                    BlockPos p=center.offset(x,dy,z);
                    if(!readable(level,p)) {out.append('?');continue;}
                    var state=level.getBlockState(p);if(state.isAir()) {out.append('.');continue;}
                    String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    char symbol=legend.computeIfAbsent(id,k->(char)(0x100+legend.size()));out.append(symbol);
                }
                out.append(' ');
            }
        }
        legend.forEach((name,c)->out.append("\n").append(c).append('=').append(name));return out.toString();
    }
}
