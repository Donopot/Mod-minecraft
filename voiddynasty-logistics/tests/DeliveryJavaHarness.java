package com.donopot.voiddynastybridge;
import com.google.gson.*;
import java.nio.file.*;

public final class DeliveryJavaHarness {
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        DeliveryInteraction interaction=new DeliveryInteraction();interaction.started("inspect");
        check(DeliveryInteraction.canShip(true,true,true,"IDLE",64,interaction.mutationBusy()),"Poll disabled confirmation");
        java.util.UUID player=java.util.UUID.randomUUID();
        check(interaction.defer(player,"ship"),"Poll discarded click");interaction.defer(player,"ship");
        check(interaction.completed().action().equals("ship")&&interaction.completed()==null,"Click not delivered exactly once");
        interaction.started("commit");check(!interaction.defer(player,"ship"),"Commit accepted duplicate click");
        check(!DeliveryInteraction.canShip(true,true,true,"IDLE",64,interaction.mutationBusy()),"Mutation kept send enabled");
        check(!DeliveryInteraction.canShip(true,false,true,"IDLE",64,false),"Paused menu allowed send");
        check(DeliveryInteraction.canShip(true,true,true,"SETTLED",64,false),"Settled depot did not allow a fresh delivery");
        check(!DeliveryInteraction.pauseMessage("PAUSED_MODE").isEmpty(),"No menu explanation");
        Path file=Path.of(args[0]);BridgeWorld s=new BridgeWorld();s.pair="00000000-0000-4000-8000-000000000003";s.campaign="00000000-0000-4000-8000-000000000002";s.checkpoint="00000000-0000-4000-8000-000000000004";s.paired=true;
        s.world="00000000-0000-4000-8000-000000000001";
        System.out.println(DeliveryService.fingerprint(s));
        for(var mission:CargoCatalog.ALL){s.delivery_mission_id=mission.id();System.out.println("HASH "+mission.id()+" "+DeliveryService.fingerprint(s));}s.delivery_mission_id=CargoCatalog.DEFAULT;
        s.phase="RESERVED";s.delivery="00000000-0000-4000-8000-000000000005";s.hash="a".repeat(64);s.save(file);
        BridgeWorld loaded=BridgeWorld.read(file);check(loaded.world.equals(s.world)&&loaded.phase.equals("RESERVED")&&loaded.delivery.equals(s.delivery),"Lost escrow journal");
        s.phase="SETTLING";s.save(file);check(BridgeWorld.read(file).phase.equals("SETTLING"),"Lost partial write marker");
        BridgeWorld legacy=new BridgeWorld();legacy.phase="SETTLED";legacy.delivery="00000000-0000-4000-8000-000000000005";legacy.hash="a".repeat(64);
        JsonObject old=JsonParser.parseString(BridgeWorld.JSON.toJson(legacy)).getAsJsonObject();old.remove("repeat_version");old.remove("completed_count");old.remove("last_receipt_id");
        Files.writeString(file,old.toString());BridgeWorld migrated=BridgeWorld.read(file);
        check(migrated.completed_count==1&&migrated.last_receipt_id.equals(legacy.delivery),"Migration lost old receipt");
        JsonObject state=new JsonObject();state.addProperty("mission_id",CargoCatalog.DEFAULT);state.addProperty("completed_count",1);state.addProperty("last_receipt_id",legacy.delivery);state.addProperty("delivery_id",legacy.delivery);state.addProperty("state","REWARD_COMMITTED");state.addProperty("payload_hash",legacy.hash);
        check(DeliveryReconciliation.inspect(migrated,state)==DeliveryReconciliation.Result.READY,"Legacy settled mismatch");
        check(DeliveryInteraction.canShip(true,true,true,"SETTLED",4,4,false),"Armour mission used iron quantity");
        check(!DeliveryInteraction.canShip(true,true,true,"SETTLED",127,128,false),"Fuel/provisions accepted incomplete cargo");
        migrated.phase="DRAFT";migrated.delivery="00000000-0000-4000-8000-000000000006";migrated.hash=DeliveryService.fingerprint(migrated);
        migrated.selected_mission_id="fuel";check(migrated.hash.equals(DeliveryService.fingerprint(migrated)),"Selection altered frozen shipment");
        check(DeliveryReconciliation.inspect(migrated,state)==DeliveryReconciliation.Result.NEED_PREPARE,"Second delivery did not start");
        state.addProperty("state","PREPARED");state.addProperty("delivery_id",migrated.delivery);state.addProperty("payload_hash",migrated.hash);
        check(DeliveryReconciliation.inspect(migrated,state)==DeliveryReconciliation.Result.PREPARED,"Second preparation failed");
        state.addProperty("completed_count",2);state.addProperty("last_receipt_id",migrated.delivery);state.addProperty("state","REWARD_COMMITTED");
        boolean blocked=false;try{DeliveryReconciliation.inspect(migrated,state);}catch(java.io.IOException e){blocked=true;}check(blocked,"Unreserved old world accepted committed receipt");
        migrated.phase="RESERVED";check(DeliveryReconciliation.inspect(migrated,state)==DeliveryReconciliation.Result.COMMITTED,"Lost commit reply did not reconcile");
        migrated.completed_count=2;migrated.last_receipt_id=migrated.delivery;migrated.phase="SETTLED";
        for(int i=0;i<20;i++)check(DeliveryReconciliation.inspect(migrated,state)==DeliveryReconciliation.Result.READY,"Duplicate acknowledgment progressed again");
        state.addProperty("completed_count",1);blocked=false;try{DeliveryReconciliation.inspect(migrated,state);}catch(java.io.IOException e){blocked=true;}check(blocked,"Old RT save accepted");
        JsonObject r=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject();String request=r.get("reply_to").getAsString();DeliveryWire.reply(r.toString(),request);
        for(String bad:new String[]{r.toString().replaceFirst("\\{","{\"reserve\":100,"),r.toString().replace("\"reserve\":10","\"reserve\":10.5"),r.toString().replace("\"protocol_version\":4","\"protocol_version\":5")}){
            boolean rejected=false;try{DeliveryWire.reply(bad,request);}catch(Exception e){rejected=true;}check(rejected,"Accepted invalid response");
        }
        System.out.println("PASS world journal, interrupted intents, strict live response");
    }
}
