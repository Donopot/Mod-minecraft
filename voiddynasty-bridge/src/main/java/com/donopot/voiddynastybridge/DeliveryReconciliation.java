package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import java.io.IOException;

public final class DeliveryReconciliation {
   private static String text(JsonObject var0, String var1) {
      return var0.get(var1).getAsString();
   }

   private static void require(boolean var0) throws IOException {
      if (!var0) {
         throw new IOException("Historique de livraison divergent : checkpoint appaire requis");
      }
   }

   public static DeliveryReconciliation.Result inspect(BridgeWorld var0, JsonObject var1) throws IOException {
      int var2 = var1.get("completed_count").getAsInt();
      String var3 = text(var1, "last_receipt_id");
      String var4 = text(var1, "delivery_id");
      String var5 = text(var1, "state");
      if (var0.phase.equals("RESERVED") && var2 == var0.completed_count + 1) {
         require(
            var5.equals("REWARD_COMMITTED")
               && var4.equals(var0.delivery)
               && var3.equals(var0.delivery)
               && text(var1, "payload_hash").equals(var0.hash)
               && text(var1, "mission_id").equals(var0.delivery_mission_id)
         );
         return DeliveryReconciliation.Result.COMMITTED;
      } else {
         require(var2 == var0.completed_count && var3.equals(var0.last_receipt_id));
         if (var0.phase.equals("SETTLED")) {
            require(
               var5.equals("REWARD_COMMITTED")
                  && var4.equals(var0.delivery)
                  && text(var1, "payload_hash").equals(var0.hash)
                  && text(var1, "mission_id").equals(var0.delivery_mission_id)
            );
            return DeliveryReconciliation.Result.READY;
         } else if (var0.phase.equals("IDLE")) {
            require(var2 == 0 && var4.isEmpty() && var5.equals("IDLE"));
            return DeliveryReconciliation.Result.READY;
         } else if (!var0.phase.equals("DRAFT") && !var0.phase.equals("RESERVED")) {
            throw new IOException("Etat local incompatible");
         } else if (var5.equals("PREPARED")) {
            require(var4.equals(var0.delivery) && text(var1, "payload_hash").equals(var0.hash) && text(var1, "mission_id").equals(var0.delivery_mission_id));
            return DeliveryReconciliation.Result.PREPARED;
         } else {
            require(
               var0.phase.equals("DRAFT")
                  && (var2 == 0 && var4.isEmpty() && var5.equals("IDLE") || var2 > 0 && var4.equals(var3) && var5.equals("REWARD_COMMITTED"))
            );
            return DeliveryReconciliation.Result.NEED_PREPARE;
         }
      }
   }

   public static enum Result {
      READY,
      NEED_PREPARE,
      PREPARED,
      COMMITTED;
   }
}
