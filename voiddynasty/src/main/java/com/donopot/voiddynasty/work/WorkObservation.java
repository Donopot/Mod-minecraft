package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;

/** Live bounded perception on the server thread. Never loads chunks, moves or edits blocks. */
public final class WorkObservation {
    public static final int MAX_VOLUME=32768, MAX_RESULTS=64;
    private WorkObservation() {}
    private static final class Category {
        int count; final JsonArray positions=new JsonArray();
        void add(BlockPos position,int limit){count++;if(positions.size()<limit)positions.add(MissionService.positionJson(position));}
        JsonObject json(){var result=new JsonObject();result.addProperty("count",count);result.add("positions",positions);result.addProperty("truncated",positions.size()<count);return result;}
    }
    public static JsonObject inspect(ServerLevel level,JsonObject request){
        try{return read(level,request);}catch(IllegalArgumentException|IllegalStateException|ArithmeticException e){return MissionService.error("INVALID_SPEC","Invalid observation fields, bounds or resource");}
    }
    private static JsonObject read(ServerLevel level,JsonObject request){
        if(!request.has("type")||!request.get("type").isJsonPrimitive()||!request.get("type").getAsJsonPrimitive().isString())throw new IllegalArgumentException();
        boolean find=request.get("type").getAsString().equals("FIND");
        if(!find&&!request.get("type").getAsString().equals("OBSERVE"))throw new IllegalArgumentException();
        var fields=new HashSet<>(Set.of("schema","type","min","max","limit"));if(find)fields.add("item");
        if(!request.keySet().equals(fields)||MissionService.integer(request.get("schema"))!=1)throw new IllegalArgumentException();
        int limit=MissionService.integer(request.get("limit"));if(limit<1||limit>MAX_RESULTS)throw new IllegalArgumentException();
        BlockPos a=MissionService.position(request.get("min")),b=MissionService.position(request.get("max"));
        BlockPos min=new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));
        BlockPos max=new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
        long volume=1;for(long extent:new long[]{(long)max.getX()-min.getX()+1,(long)max.getY()-min.getY()+1,(long)max.getZ()-min.getZ()+1}){if(extent>MAX_VOLUME)throw new IllegalArgumentException();volume*=extent;if(volume>MAX_VOLUME)throw new IllegalArgumentException();}
        String item="";Set<String> sources=Set.of();
        if(find){var value=request.get("item");if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isString())throw new IllegalArgumentException();item=value.getAsString();sources=MissionService.SOURCES.get(item);if(sources==null)throw new IllegalArgumentException();}
        int observed=0,unloaded=0,protectedCount=0,outside=0,matchesCount=0;var matches=new JsonArray();
        var blocks=new TreeMap<String,Integer>();var hazards=new TreeMap<String,Category>();
        for(BlockPos position:BlockPos.betweenClosed(min,max)){
            if(level.isOutsideBuildHeight(position)||!level.getWorldBorder().isWithinBounds(position)){outside++;continue;}
            if(!level.hasChunkAt(position)){unloaded++;continue;}
            if(ClaimIntegration.isClaimProtected(level,position)){protectedCount++;continue;}
            var state=level.getBlockState(position);String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();observed++;blocks.merge(id,1,Integer::sum);
            if(find&&sources.contains(id)){matchesCount++;if(matches.size()<limit){var match=new JsonObject();match.add("position",MissionService.positionJson(position));match.addProperty("block",id);matches.add(match);}}
            String kind=state.is(Blocks.LAVA)?"lava":state.is(Blocks.FIRE)||state.is(Blocks.SOUL_FIRE)?"fire":state.is(Blocks.POWDER_SNOW)?"powder_snow":state.is(Blocks.MAGMA_BLOCK)?"magma":state.is(Blocks.BEDROCK)?"bedrock":state.getBlock() instanceof FallingBlock?"falling_block":"";
            if(!kind.isEmpty())hazards.computeIfAbsent(kind,k->new Category()).add(position,Math.min(16,limit));
            if(state.hasBlockEntity())hazards.computeIfAbsent(level.getBlockEntity(position) instanceof Container?"container":"block_entity",k->new Category()).add(position,Math.min(16,limit));
        }
        var result=new JsonObject();result.addProperty("ok",true);result.addProperty("type",find?"FIND":"OBSERVE");result.addProperty("dimension",level.dimension().identifier().toString());result.addProperty("sampled_tick",level.getGameTime());result.add("min",MissionService.positionJson(min));result.add("max",MissionService.positionJson(max));
        var coverage=new JsonObject();coverage.addProperty("requested_blocks",volume);coverage.addProperty("observed_blocks",observed);coverage.addProperty("unloaded_blocks",unloaded);coverage.addProperty("protected_blocks",protectedCount);coverage.addProperty("outside_blocks",outside);coverage.addProperty("complete",observed==volume);result.add("coverage",coverage);
        if(find){result.addProperty("item",item);result.addProperty("matches_total",matchesCount);result.add("matches",matches);result.addProperty("truncated",matches.size()<matchesCount);result.addProperty("conclusive_absence",matchesCount==0&&observed==volume);}
        var counts=new JsonObject();blocks.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(32).forEach(e->counts.addProperty(e.getKey(),e.getValue()));result.add("blocks",counts);result.addProperty("block_types_truncated",blocks.size()>32);
        var warnings=new JsonObject();hazards.forEach((key,value)->warnings.add(key,value.json()));result.add("hazards",warnings);
        result.addProperty("scope","Loaded unprotected cells in the requested box only; not a safe-route or harvestability guarantee; counts are blocks, not predicted drops");return result;
    }
}
