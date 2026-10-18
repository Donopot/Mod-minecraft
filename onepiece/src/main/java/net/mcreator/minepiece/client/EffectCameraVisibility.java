package net.mcreator.minepiece.client;

/** Local render clearance, measured from the actual camera in every camera mode. */
public final class EffectCameraVisibility {
   private EffectCameraVisibility() { }

   public static float flameScale(double cameraDistance, float quadSize) {
      // Account for the sprite's diagonal so a large quad cannot cover the lens.
      double inner = Math.max(.45, quadSize * Math.sqrt(2));
      double t = Math.clamp((cameraDistance - inner) / 1.5, 0, 1);
      return (float)(t * t * (3 - 2 * t));
   }

   public static double meraMeshClearance(String profile, float scale) {
      return switch (profile) {
         case "hiken" -> .3 + .9 * scale;
         // Its two-block rear trail must clear the lens along with the small bullet.
         case "higan" -> 2.4;
         case "daienkai", "mera_charge_ball" -> .2 + 2.6 * scale;
         default -> 0;
      };
   }

   public static double pendingMeshClearance(float scale) {
      // The factory spawns before the caller assigns the final technique profile.
      return .3 + 3 * Math.max(1,scale);
   }
}
