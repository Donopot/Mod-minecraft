package com.donopot.voiddynastybridge;

import com.google.gson.*;
import com.google.gson.stream.*;
import java.io.*;
import java.util.*;

/** The live protocol is a bounded flat envelope; duplicate and extra keys fail closed. */
public final class DeliveryWire {
    private static final Set<String> STRINGS=Set.of("reply_to","code","session_id","campaign_id","world_id","pair_id","checkpoint_id","delivery_id","state","payload_hash","zone","campaign_name","last_receipt_id","mission_id");
    public static JsonObject reply(String json,String requestId)throws IOException{
        JsonObject result=new JsonObject();Set<String> seen=new HashSet<>();
        try(JsonReader reader=new JsonReader(new StringReader(json))){
            reader.setStrictness(Strictness.STRICT);reader.beginObject();
            while(reader.hasNext()){
                String name=reader.nextName();if(!seen.add(name))throw new IOException("Duplicate field");
                if(STRINGS.contains(name)){
                    if(reader.peek()!=JsonToken.STRING)throw new IOException("Invalid string");String v=reader.nextString();if(v.length()>1024)throw new IOException("String too large");result.addProperty(name,v);
                }else if(name.equals("protocol_version")||name.equals("reserve")||name.equals("completed_count")||name.equals("native_count")){
                    if(reader.peek()!=JsonToken.NUMBER)throw new IOException("Invalid integer");String v=reader.nextString();if(!v.matches("0|[1-9][0-9]{0,3}"))throw new IOException("Invalid integer");result.addProperty(name,Integer.parseInt(v));
                }else if(name.equals("available")){
                    if(reader.peek()!=JsonToken.BOOLEAN)throw new IOException("Invalid availability");result.addProperty(name,reader.nextBoolean());
                }else throw new IOException("Unexpected field");
            }
            reader.endObject();if(reader.peek()!=JsonToken.END_DOCUMENT||seen.size()!=19)throw new IOException("Invalid envelope");
        }
        int count=result.get("completed_count").getAsInt();String last=result.get("last_receipt_id").getAsString();
        int nativeCount=result.get("native_count").getAsInt();
        if(result.get("protocol_version").getAsInt()!=4||!result.get("reply_to").getAsString().equals(requestId)
            ||!BridgeWorld.uuid(result.get("session_id").getAsString())
            ||count<0||count>256||nativeCount<0||nativeCount>count||result.get("reserve").getAsInt()!=(count-nativeCount)*10
            ||(count==0?!last.isEmpty():!BridgeWorld.uuid(last)))throw new IOException("Invalid reply");
        return result;
    }
}
