package com.donopot.voiddynastybridge;

import java.util.UUID;

public final class DeliveryInteraction {
   private String request = "";
   private DeliveryInteraction.Click pending;

   public void started(String var1) {
      this.request = var1;
   }

   public boolean mutationBusy() {
      return !this.request.isEmpty() && !this.request.equals("inspect");
   }

   public boolean defer(UUID var1, String var2) {
      if (!this.request.equals("inspect")) {
         return false;
      } else {
         if (this.pending == null) {
            this.pending = new DeliveryInteraction.Click(var1, var2);
         }

         return true;
      }
   }

   public DeliveryInteraction.Click completed() {
      this.request = "";
      DeliveryInteraction.Click var1 = this.pending;
      this.pending = null;
      return var1;
   }

   public static boolean canShip(boolean var0, boolean var1, boolean var2, String var3, int var4, boolean var5) {
      return canShip(var0, var1, var2, var3, var4, 64, var5);
   }

   public static boolean canShip(boolean var0, boolean var1, boolean var2, String var3, int var4, int var5, boolean var6) {
      return var0 && var1 && var2 && (var3.equals("IDLE") || var3.equals("SETTLED")) && var4 >= var5 && !var6;
   }

   public static String pauseMessage(String var0) {
      return switch (var0) {
         case "PAUSED_MODE" -> "Menu ou dialogue Rogue Trader ouvert : envoi suspendu";
         case "PAUSED_COMBAT" -> "Combat Rogue Trader : envoi suspendu";
         case "UNAVAILABLE" -> "Campagne RT absente ou en chargement";
         case "PERSISTENCE_NOT_VERIFIED" -> "Charger la sauvegarde RT PERSISTANCE VALIDEE ou REWARD_COMMITTED";
         case "NATIVE_CARGO_UNAVAILABLE" -> "Cargaison native indisponible dans cette campagne";
         case "NATIVE_CARGO_NOT_FULL" -> "Cargaison native incomplete : conserver les sauvegardes et le depot";
         default -> "";
      };
   }

   public static record Click(UUID player, String action) {
   }
}
