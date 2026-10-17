package br.com.oneminepiece.opworldlike.compat;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;

public final class MinePieceSingleplayerBlockBreakCompat {
   private MinePieceSingleplayerBlockBreakCompat() {
   }

   public static void register() {
      // Forge hack (re-allow break canceled by another mod in singleplayer) ne se transpose pas
      // directement en Fabric : les callbacks Fabric ne peuvent pas "uncancel" un FAIL précédent.
      AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
         if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) {
            return InteractionResult.PASS;
         }
         return InteractionResult.PASS;
      });
   }
}
