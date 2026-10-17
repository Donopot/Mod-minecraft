package br.com.oneminepiece.opworldlike;

import net.minecraft.server.level.ServerLevel;

public final class OmpWorldGameRules {
   private static final int DEFAULT_BOSS_SPAWN_MINUTES = 10;

   private OmpWorldGameRules() {
   }

   public static void bootstrap() {
   }

   public static int minePieceBossSpawnMinutes(ServerLevel level) {
      // TODO: enregistrer une GameRule<Integer> custom (26.3 : constructeur à 8 args
      // GameRuleCategory + GameRuleType.INT + ArgumentType + VisitorCaller + Codec + ToIntFunction + default + FeatureFlagSet)
      return DEFAULT_BOSS_SPAWN_MINUTES;
   }
}
