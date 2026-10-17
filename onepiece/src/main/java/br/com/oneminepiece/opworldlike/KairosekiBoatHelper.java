package br.com.oneminepiece.opworldlike;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;

final class KairosekiBoatHelper {
   private static final String FALLBACK_COATING_TAG = "opworldlikeKairosekiCoatingLevel";
   private static final int MAX_COATING_LEVEL = 5;

   private KairosekiBoatHelper() {
   }

   static boolean isSupportedBoat(Entity entity) {
      return entity instanceof Boat || isSmallShip(entity);
   }

   static boolean isFullyCoated(Entity entity) {
      return coatingLevel(entity) >= 5;
   }

   static int coatingLevel(Entity entity) {
      return 0; // TODO: persister le coating level via un attachement d'entité 26.3 (getPersistentData() a disparu)
   }

   static boolean hasNativeCoating(Entity entity) {
      return false;
   }

   static boolean isDenseKairoseki(ItemStack stack) {
      return false;
   }

   static boolean applyFallbackCoating(ServerPlayer player, Entity target, ItemStack stack) {
      return false;
   }

   static void sendProgress(ServerPlayer player, Entity target) {
      int clamped = Math.max(0, Math.min(5, coatingLevel(target)));
      player.sendSystemMessage(
         OmpMessages.component(
            player,
            "Revestimento de kairoseki indisponivel no Mine Piece: " + clamped + "/5",
            "Kairoseki coating is unavailable in Mine Piece: " + clamped + "/5"
         ),
         true
      );
   }

   private static boolean isSmallShip(Entity entity) {
      if (entity == null) {
         return false;
      }

      Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return key != null && "smallships".equals(key.getNamespace())
         ? true
         : entity.getClass().getName().startsWith("com.talhanation.smallships.world.entity.ship.");
   }
}
