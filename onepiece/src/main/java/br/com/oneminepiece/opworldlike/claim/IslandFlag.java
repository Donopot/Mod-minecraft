package br.com.oneminepiece.opworldlike.claim;

import java.util.Locale;

public enum IslandFlag {
   BUILD(false),
   CONTAINERS(false),
   INTERACT(true),
   EXPLOSIONS(false),
   FIRE_SPREAD(false),
   PVP(true),
   MOB_GRIEFING(true);

   public final boolean defaultValue;

   IslandFlag(boolean defaultValue) {
      this.defaultValue = defaultValue;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
   }

   public static IslandFlag byId(String id) {
      String normalized = id.toUpperCase(Locale.ROOT).replace('-', '_');

      for (IslandFlag flag : values()) {
         if (flag.name().equals(normalized)) {
            return flag;
         }
      }

      return null;
   }
}
