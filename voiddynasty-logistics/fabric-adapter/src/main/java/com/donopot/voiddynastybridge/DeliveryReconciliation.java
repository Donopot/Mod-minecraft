package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import java.io.IOException;

/** Compare the committed prefix before progressing any new delivery. */
public final class DeliveryReconciliation {
    public enum Result{READY,NEED_PREPARE,PREPARED,COMMITTED}
    private static String text(JsonObject r,String key){return r.get(key).getAsString();}
    private static void require(boolean value)throws IOException{if(!value)throw new IOException("Historique de livraison divergent : checkpoint appaire requis");}
    public static Result inspect(BridgeWorld s,JsonObject r)throws IOException{
        int count=r.get("completed_count").getAsInt();String last=text(r,"last_receipt_id"),id=text(r,"delivery_id"),state=text(r,"state");
        if(s.phase.equals("RESERVED")&&count==s.completed_count+1){
            require(state.equals("REWARD_COMMITTED")&&id.equals(s.delivery)&&last.equals(s.delivery)&&text(r,"payload_hash").equals(s.hash)&&text(r,"mission_id").equals(s.delivery_mission_id));return Result.COMMITTED;
        }
        require(count==s.completed_count&&last.equals(s.last_receipt_id));
        if(s.phase.equals("SETTLED")){
            require(state.equals("REWARD_COMMITTED")&&id.equals(s.delivery)&&text(r,"payload_hash").equals(s.hash)&&text(r,"mission_id").equals(s.delivery_mission_id));return Result.READY;
        }
        if(s.phase.equals("IDLE")){require(count==0&&id.isEmpty()&&state.equals("IDLE"));return Result.READY;}
        if(s.phase.equals("DRAFT")||s.phase.equals("RESERVED")){
            if(state.equals("PREPARED")){
                require(id.equals(s.delivery)&&text(r,"payload_hash").equals(s.hash)&&text(r,"mission_id").equals(s.delivery_mission_id));return Result.PREPARED;
            }
            require(s.phase.equals("DRAFT")&&((count==0&&id.isEmpty()&&state.equals("IDLE"))
                ||(count>0&&id.equals(last)&&state.equals("REWARD_COMMITTED"))));return Result.NEED_PREPARE;
        }
        throw new IOException("Etat local incompatible");
    }
}
