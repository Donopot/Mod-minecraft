package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

public final class DeliveryWire {
   private static final Set<String> STRINGS = Set.of(
      "reply_to",
      "code",
      "session_id",
      "campaign_id",
      "world_id",
      "pair_id",
      "checkpoint_id",
      "delivery_id",
      "state",
      "payload_hash",
      "zone",
      "campaign_name",
      "last_receipt_id",
      "mission_id"
   );

   public static JsonObject reply(String var0, String var1) throws IOException {
      JsonObject var2 = new JsonObject();
      HashSet var3 = new HashSet();
      JsonReader var4 = new JsonReader(new StringReader(var0));

      try {
         var4.setStrictness(Strictness.STRICT);
         var4.beginObject();

         while (var4.hasNext()) {
            String var5 = var4.nextName();
            if (!var3.add(var5)) {
               throw new IOException("Duplicate field");
            }

            if (STRINGS.contains(var5)) {
               if (var4.peek() != JsonToken.STRING) {
                  throw new IOException("Invalid string");
               }

               String var6 = var4.nextString();
               if (var6.length() > 1024) {
                  throw new IOException("String too large");
               }

               var2.addProperty(var5, var6);
            } else if (!var5.equals("protocol_version") && !var5.equals("reserve") && !var5.equals("completed_count") && !var5.equals("native_count")) {
               if (!var5.equals("available")) {
                  throw new IOException("Unexpected field");
               }

               if (var4.peek() != JsonToken.BOOLEAN) {
                  throw new IOException("Invalid availability");
               }

               var2.addProperty(var5, var4.nextBoolean());
            } else {
               if (var4.peek() != JsonToken.NUMBER) {
                  throw new IOException("Invalid integer");
               }

               String var11 = var4.nextString();
               if (!var11.matches("0|[1-9][0-9]{0,3}")) {
                  throw new IOException("Invalid integer");
               }

               var2.addProperty(var5, Integer.parseInt(var11));
            }
         }

         var4.endObject();
         if (var4.peek() != JsonToken.END_DOCUMENT || var3.size() != 19) {
            throw new IOException("Invalid envelope");
         }
      } catch (Throwable var8) {
         try {
            var4.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      var4.close();
      int var9 = var2.get("completed_count").getAsInt();
      String var10 = var2.get("last_receipt_id").getAsString();
      int var12 = var2.get("native_count").getAsInt();
      if (var2.get("protocol_version").getAsInt() == 4
         && var2.get("reply_to").getAsString().equals(var1)
         && BridgeWorld.uuid(var2.get("session_id").getAsString())
         && var9 >= 0
         && var9 <= 256
         && var12 >= 0
         && var12 <= var9
         && var2.get("reserve").getAsInt() == (var9 - var12) * 10
         && (var9 == 0 ? var10.isEmpty() : BridgeWorld.uuid(var10))) {
         return var2;
      } else {
         throw new IOException("Invalid reply");
      }
   }
}
