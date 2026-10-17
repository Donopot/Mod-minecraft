package com.donopot.voiddynastybridge;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;

public final class BridgeWorld {
   public int schema = 2;
   public String world = UUID.randomUUID().toString();
   public String pair = "";
   public String campaign = "";
   public String checkpoint = "";
   public String crate = "";
   public String dimension = "";
   public String delivery = "";
   public String phase = "IDLE";
   public String hash = "";
   public int x;
   public int y;
   public int z;
   public int repeat_version = 2;
   public int completed_count;
   public String last_receipt_id = "";
   public int catalog_version = 1;
   public String selected_mission_id = "logistics_first_delivery";
   public String delivery_mission_id = "logistics_first_delivery";
   public boolean paired;
   public static final Gson JSON = new Gson();

   public static BridgeWorld read(Path var0) throws IOException {
      if (!Files.exists(var0)) {
         return new BridgeWorld();
      } else if (Files.size(var0) > 16384L) {
         throw new IOException("Journal trop grand");
      } else {
         String var1 = Files.readString(var0);
         BridgeWorld var2 = (BridgeWorld)JSON.fromJson(var1, BridgeWorld.class);
         if (var2 != null && !JsonParser.parseString(var1).getAsJsonObject().has("repeat_version")) {
            var2.repeat_version = 0;
         }

         if (var2 != null
            && var2.phase != null
            && !JsonParser.parseString(var1).getAsJsonObject().has("catalog_version")
            && !var2.phase.equals("IDLE")
            && !var2.phase.equals("SETTLED")) {
            throw new IOException("Terminer la livraison precedente avant mise a jour");
         } else if (var2 != null
            && var2.schema == 2
            && uuid(var2.world)
            && var2.phase != null
            && Set.of("IDLE", "DRAFT", "RESERVING", "RESERVED", "SETTLING", "SETTLED", "RECOVERY_REQUIRED").contains(var2.phase)) {
            if (!var2.paired || uuid(var2.pair) && uuid(var2.campaign) && uuid(var2.checkpoint)) {
               if (var2.delivery.isEmpty() || uuid(var2.delivery) && var2.hash.length() == 64) {
                  var2.upgrade();
                  return var2;
               } else {
                  throw new IOException("Livraison invalide");
               }
            } else {
               throw new IOException("Identite invalide");
            }
         } else {
            throw new IOException("Journal invalide");
         }
      }
   }

   public void upgrade() throws IOException {
      if (this.catalog_version != 1) {
         throw new IOException("Catalogue inconnu");
      } else {
         CargoCatalog.get(this.selected_mission_id);
         CargoCatalog.get(this.delivery_mission_id);
         if (this.repeat_version == 0) {
            if (!this.phase.equals("IDLE") && !this.phase.equals("SETTLED")) {
               throw new IOException("Terminer la livraison en 0.4.1 avant migration");
            }

            this.completed_count = this.phase.equals("SETTLED") ? 1 : 0;
            this.last_receipt_id = this.completed_count == 1 ? this.delivery : "";
            this.repeat_version = 2;
         }

         if (this.repeat_version == 2
            && this.completed_count >= 0
            && this.completed_count <= 256
            && this.last_receipt_id != null
            && (this.completed_count == 0 ? this.last_receipt_id.isEmpty() : uuid(this.last_receipt_id))) {
            if (!this.phase.equals("SETTLED") || this.completed_count != 0 && this.delivery.equals(this.last_receipt_id)) {
               if (this.phase.equals("IDLE") && (this.completed_count != 0 || !this.delivery.isEmpty())) {
                  throw new IOException("Etat initial invalide");
               }
            } else {
               throw new IOException("Recu final invalide");
            }
         } else {
            throw new IOException("Historique invalide");
         }
      }
   }

   public void save(Path var1) throws IOException {
      Files.createDirectories(var1.getParent());
      Path var2 = var1.resolveSibling(var1.getFileName() + ".tmp");
      byte[] var3 = JSON.toJson(this).getBytes(StandardCharsets.UTF_8);

      try (FileChannel var4 = FileChannel.open(var2, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
         ByteBuffer var5 = ByteBuffer.wrap(var3);

         while (var5.hasRemaining()) {
            var4.write(var5);
         }

         var4.force(true);
      }

      Files.move(var2, var1, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
   }

   public static boolean uuid(String var0) {
      try {
         return var0 != null && UUID.fromString(var0).toString().equals(var0);
      } catch (Exception var2) {
         return false;
      }
   }
}
