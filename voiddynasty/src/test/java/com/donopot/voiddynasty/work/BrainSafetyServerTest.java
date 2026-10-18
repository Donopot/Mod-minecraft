package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.ai.*;
import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Native tests in a disposable world only; no external model endpoint is contacted. */
public final class BrainSafetyServerTest implements ModInitializer {
    private static int checks;
    private static final BlockPos ORIGIN=new BlockPos(30,64,4), TARGET=new BlockPos(34,64,4), CHEST=new BlockPos(42,64,4);
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(Commands.literal("braincheck")
            .requires(src->src.getServer().isDedicatedServer())
            .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
                String phase=StringArgumentType.getString(ctx,"phase");
                try {run(ctx.getSource().getLevel(),phase);ctx.getSource().sendSuccess(()->Component.literal("PASS "+phase+" checks="+checks),false);return 1;}
                catch(Throwable e) {e.printStackTrace();ctx.getSource().sendFailure(Component.literal("FAIL "+phase+": "+e));return 0;}
            }))));
    }
    private static void check(boolean yes,String message) {if(!yes) throw new AssertionError(message);checks++;}
    private static JsonObject json(String text) {return JsonParser.parseString(text).getAsJsonObject();}
    private static BrainTools.Order order(String name,String args) {return new BrainTools.Order(name,json(args));}
    private static String apply(ServerLevel l,LivingEntity b,String name,String args) {return BrainOrders.apply(order(name,args),l,b,BrainConfig.get());}
    private static JsonObject request(String key) {return json("{\"schema\":1,\"type\":\"GATHER\",\"request_id\":\""+key+"\",\"item\":\"minecraft:cobblestone\",\"amount\":2,\"min\":[34,64,4],\"max\":[38,64,4],\"destination\":[42,64,4],\"origin\":[30,64,4]}");}
    private static String submit(ServerLevel l,LivingEntity b,String key) {
        JsonObject arg=new JsonObject();arg.addProperty("spec",request(key).toString());return BrainOrders.apply(new BrainTools.Order("work_submit",arg),l,b,BrainConfig.get());
    }
    private static JsonObject observe(ServerLevel l,String type,BlockPos a,BlockPos b) {
        JsonObject q=new JsonObject();q.addProperty("schema",1);q.addProperty("type",type);q.addProperty("limit",1);
        q.add("min",MissionService.positionJson(a));q.add("max",MissionService.positionJson(b));
        if(type.equals("FIND"))q.addProperty("item","minecraft:cobblestone");return WorkObservation.inspect(l,q);
    }
    private static void run(ServerLevel l,String phase) {
        WorkState s=WorkState.get(l);LivingEntity bot=BrainSpawn.find(l);
        switch(phase) {
            case "setup" -> {
                WorkService.stop(l);WorkService.discardServitor(l,s);
                s.jobName=null;s.missionId="";s.missions.clear();s.journal.clear();s.invSlots.clear();s.depots.clear();s.hasDepot=false;s.rollbackRemaining=0;s.servitorUuid="";s.finished=true;
                for(int x=28;x<=51;x++) for(int z=0;z<=15;z++) {
                    l.setBlock(new BlockPos(x,63,z),Blocks.SEA_LANTERN.defaultBlockState(),3);
                    for(int y=64;y<=70;y++)l.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
                }
                l.setBlock(CHEST,Blocks.CHEST.defaultBlockState(),3);
                for(int i=0;i<5;i++)l.setBlock(TARGET.east(i),Blocks.STONE.defaultBlockState(),3);
                bot=BrainSpawn.spawn(l,ORIGIN,BrainSpawn.Variant.TIR);check(bot!=null,"spawn native companion");
                check(!BrainConfig.get().enabled,"test never enables or contacts an LLM");
            }
            case "validation" -> {
                for(String bad:List.of("{}","{\"x\":true,\"y\":64,\"z\":4}","{\"x\":\"31\",\"y\":64,\"z\":4}","{\"x\":31.5,\"y\":64,\"z\":4}","{\"x\":31,\"y\":64,\"z\":4,\"extra\":1}"))
                    check(BrainTools.validate(order("goto",bad),l,bot,BrainConfig.get())!=null,"malformed movement refused: "+bad);
                check(BrainTools.validate(order("scan","{\"block\":\"minecraft:fake_ore\"}"),l,bot,BrainConfig.get())!=null,"registry fallback must not accept unknown block");
                check(apply(l,bot,"mine_area","{}").startsWith("REFUSE"),"legacy destructive engine unavailable even via direct apply");
                check(apply(l,bot,"work_control","{\"mission_id\":\"00000000-0000-0000-0000-000000000000\",\"action\":\"close\"}").startsWith("REFUSE"),"model cannot release rollback history");
                check(TaskRunner.current(bot)==null && s.jobName==null,"refusals do not allocate physical tasks");
                var found=json(BrainSenses.find(l,bot,"minecraft:stone",16,1));
                check(found.get("matches_total").getAsInt()==5 && found.get("positions").getAsJsonArray().size()==1,"exact namespaced search and truncation");
                check(found.get("truncated").getAsBoolean(),"find honestly reports capped results");
                JsonObject q=observe(l,"FIND",TARGET,TARGET.east(4));
                check(q.get("matches_total").getAsInt()==5 && q.get("matches").getAsJsonArray().size()==1,"native resource observation");
                check(s.journal.isEmpty() && s.invSlots.isEmpty() && l.getBlockState(TARGET).is(Blocks.STONE),"perception has no mining side effect");
                BlockPos remote=new BlockPos(100000,64,100000);check(!l.hasChunkAt(remote),"fixture starts unloaded");
                q=observe(l,"FIND",remote,remote.east());
                check(!q.getAsJsonObject("coverage").get("complete").getAsBoolean() && !q.get("conclusive_absence").getAsBoolean(),"unloaded area is unknown not absent");
                check(!l.hasChunkAt(remote),"inspection did not load a chunk");
                q=observe(l,"OBSERVE",new BlockPos(-29999984,-2048,-29999984),new BlockPos(29999984,2047,29999984));
                check(!q.get("ok").getAsBoolean(),"oversize scan rejected before iteration");
                var raw=new net.minecraft.nbt.CompoundTag();raw.putString("bags","invalid payload");raw.putString("keep","original");
                var ops=net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,l.registryAccess());
                var locked=BrainInventoryState.CODEC.parse(ops,raw).getOrThrow();
                check(BrainInventoryState.CODEC.encodeStart(ops,locked).getOrThrow().equals(raw),"invalid bag payload is retained instead of replaced by empty data");
            }
            case "bag" -> {
                ItemStack seed=new ItemStack(Items.IRON_INGOT,63);check(BrainInv.add(bot,seed),"seed first slot");
                for(int i=1;i<27;i++) {ItemStack stack=new ItemStack(Items.DIAMOND,64);stack.set(DataComponents.CUSTOM_NAME,Component.literal("slot-"+i));check(BrainInv.add(bot,stack),"distinct components occupy exact slots");}
                int before=BrainInv.count(bot);ItemStack extra=new ItemStack(Items.IRON_INGOT,2);
                check(!BrainInv.add(bot,extra) && extra.getCount()==2 && BrainInv.count(bot)==before,"full bag cannot partially merge then duplicate a world item");
                check(BrainInv.of(bot).getFirst().getCount()==63,"rejected merge did not change stored stack");
                List<ItemStack> snapshot=BrainInv.of(bot);snapshot.getFirst().grow(1);
                check(BrainInv.of(bot).getFirst().getCount()==63,"public inventory snapshots are detached copies");
                ItemEntity worldItem=new ItemEntity(l,bot.getX(),bot.getY(),bot.getZ(),new ItemStack(Items.IRON_INGOT,2));l.addFreshEntity(worldItem);
                BrainTasks.Pickup pickup=new BrainTasks.Pickup(4,2);pickup.tick(new BrainTask.Ctx(l,bot,BrainConfig.get()));
                check(worldItem.isAlive() && worldItem.getItem().getCount()==2 && BrainInv.count(bot)==before,"full-bag pickup leaves complete item on ground");
                BrainInv.dropAll(l,bot);check(BrainInv.of(bot).isEmpty(),"explicit withdrawal empties persisted bag");
                for(var entity:l.getEntitiesOfClass(ItemEntity.class,bot.getBoundingBox().inflate(8)))entity.discard();
                ItemStack durable=new ItemStack(Items.DIAMOND,9);durable.set(DataComponents.CUSTOM_NAME,Component.literal("Durable diamond"));
                worldItem=new ItemEntity(l,bot.getX(),bot.getY(),bot.getZ(),durable);l.addFreshEntity(worldItem);
                pickup=new BrainTasks.Pickup(4,3);pickup.tick(new BrainTask.Ctx(l,bot,BrainConfig.get()));
                check(BrainInv.count(bot)==3 && worldItem.getItem().getCount()==6,"pickup counts items and respects requested maximum");
                check(BrainInv.of(bot).getFirst().getHoverName().getString().equals("Durable diamond"),"pickup keeps custom components");
                worldItem.discard();
            }
            case "longObservation" -> {
                for(int x=48;x<=51;x++)for(int z=0;z<16;z++)l.setBlock(new BlockPos(x,68,z),Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),3);
                check(l.getBlockState(new BlockPos(48,68,0)).is(Blocks.DEEPSLATE_DIAMOND_ORE),"large response fixture");
            }
            case "submit" -> {
                JsonObject out=json(submit(l,bot,"brain-roundtrip"));
                check(out.get("ok").getAsBoolean() && !out.get("mission").getAsJsonObject().get("goal_met").getAsBoolean(),"submission is acceptance not completion");
                check(bot.getUUID().toString().equals(s.servitorUuid),"native work adopts existing body");
                check(WorkService.owns(l,bot),"one physical owner");
                check(((net.minecraft.world.entity.Mob)bot).getDropChances().byEquipment(net.minecraft.world.entity.EquipmentSlot.MAINHAND)==0.0f,"work tool projection never becomes death loot");
                check(apply(l,bot,"goto","{\"x\":31,\"y\":64,\"z\":4}").contains("ACTION_BUSY"),"LLM movement cannot fight native route");
                boolean rejected=false;try {TaskRunner.assign(l,bot,new BrainTasks.Wait(1));}catch(IllegalStateException expected){rejected=true;}
                check(rejected && TaskRunner.current(bot)==null,"direct task assignment enforces owner too");
                out=json(submit(l,bot,"brain-roundtrip"));check(out.get("replayed").getAsBoolean(),"stable request replay does not create a second body");
                long workers=0;for(var e:l.getAllEntities())if(!e.isRemoved() && (e.entityTags().contains(BrainSpawn.TAG)||e.entityTags().contains("voiddynasty_work")))workers++;
                check(workers==1,"one companion, no shadow worker");
                String id=s.missionId;
                check(json(apply(l,bot,"work_control","{\"mission_id\":\""+id+"\",\"action\":\"pause\"}")).get("ok").getAsBoolean(),"typed pause");
                check(!bot.isRemoved() && !s.hasJob() && s.servitorUuid.equals(bot.getUUID().toString()),"pause preserves identity and body");
                String actualWorker=s.servitorUuid;s.servitorUuid=UUID.randomUUID().toString();String absent=s.servitorUuid;
                check(!MissionService.control(l,id,"resume").get("ok").getAsBoolean() && s.servitorUuid.equals(absent),"absent worker identity is retained instead of spawning replacement");
                s.servitorUuid=actualWorker;
                TaskRunner.assign(l,bot,new BrainTasks.Wait(60));
                check(!MissionService.control(l,id,"resume").get("ok").getAsBoolean(),"native resume cannot steal body from companion task");
                TaskRunner.cancel(bot,l,"test");
                check(MissionService.control(l,id,"resume").get("ok").getAsBoolean(),"explicit resume returns same worker");
                check(s.servitorUuid.equals(bot.getUUID().toString()),"resume identity stable");
                WorkService.stop(l); // Leave a paused mission and exact bag for a clean restart.
            }
            case "restart" -> {
                check(bot!=null && bot.getUUID().toString().equals(s.servitorUuid),"same persistent body after restart");
                check(!s.hasJob() && MissionService.active(s).status.equals("PAUSED"),"restart does not replay unapproved work");
                check(BrainInv.count(bot)==3 && BrainInv.of(bot).getFirst().getHoverName().getString().equals("Durable diamond"),"bag quantity and components survive real server restart");
                check(TaskRunner.current(bot)==null,"transient companion tasks not replayed after restart");
                check(MissionService.control(l,s.missionId,"resume").get("ok").getAsBoolean(),"resume saved native mission");
            }
            case "success" -> {
                check(MissionService.active(s).status.equals("SUCCEEDED") && MissionService.active(s).delivered==2,"observed goal delivery");
                check(s.journal.size()==2 && s.invSlots.isEmpty(),"every mined output has a receipt and is delivered");
                check(BrainInv.count(bot)==3,"native mining cannot mix or consume companion's personal bag");
                check(!bot.isRemoved(),"mission completion keeps companion alive");
                check(((Container)l.getBlockEntity(CHEST)).getItem(0).getCount()==2,"physical delivery in real chest");
                check(MissionService.control(l,s.missionId,"rollback").get("ok").getAsBoolean(),"operator rollback accepted");
            }
            case "rollback" -> {
                check(s.journal.isEmpty() && s.rollbackRemaining==0,"rollback completed every receipt");
                check(l.getBlockState(TARGET).is(Blocks.STONE) && l.getBlockState(TARGET.east()).is(Blocks.STONE),"original terrain restored");
                check(((Container)l.getBlockEntity(CHEST)).isEmpty() && s.invSlots.isEmpty(),"restoration debited exact outputs, no duplication");
                check(BrainInv.count(bot)==3,"rollback preserves unrelated custom items");
                check(MissionService.control(l,s.missionId,"close").get("ok").getAsBoolean(),"operator explicitly releases history");
                check(s.servitorUuid.isEmpty() && !bot.isRemoved(),"close releases body without discarding companion");
            }
            case "pathSetup" -> {
                bot.teleportTo(30.5,64,10.5); // Test fixture placement only; production AI has no teleport path.
                for(int x=28;x<=33;x++)for(int z=8;z<=13;z++)if(x==28||x==33||z==8||z==13)
                    for(int y=64;y<=67;y++)l.setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),3);
                TaskRunner.assign(l,bot,new BrainTasks.Goto(new BlockPos(38,64,10),40));
                check(TaskRunner.current(bot)!=null,"start enclosed navigation fixture");
            }
            case "pathCheck" -> {
                check(TaskRunner.current(bot)==null && TaskRunner.consumeOutcome(bot).contains("echouee"),"blocked navigation reports failure and stops");
                check(bot.getX()<33,"no wall traversal teleport");
                for(int x=28;x<=33;x++)for(int z=8;z<=13;z++)if(x==28||x==33||z==8||z==13)
                    for(int y=64;y<=67;y++)check(l.getBlockState(new BlockPos(x,y,z)).is(Blocks.STONE),"movement preserved wall block");
                check(s.journal.isEmpty() && BrainInv.count(bot)==3,"movement did not mine or collect resources");
            }
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
