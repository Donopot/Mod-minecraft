package net.mcreator.minepiece.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;

/** Transformation ownership is per UUID and never survives a client level change. */
public final class TransformClientState {
   public record Form(double value, String fruit) {}
   private static final Map<UUID, Form> FORMS = new HashMap<>();
   private static final Map<UUID,Integer> RECEIVED=new HashMap<>();
   private static ClientLevel level;
   private TransformClientState() {}
   public static void useLevel(ClientLevel current) {
      if (level != current) { FORMS.clear(); RECEIVED.clear();level = current; }
   }
   public static void put(UUID player, double value, String fruit) {
      FORMS.put(player, new Form(value, fruit));
      RECEIVED.merge(player,1,Integer::sum);
   }
   public static Form get(UUID player) { return FORMS.getOrDefault(player, new Form(0, "")); }
   public static int received(UUID player){return RECEIVED.getOrDefault(player,0);}
   public static void clear() { FORMS.clear();RECEIVED.clear(); level = null; }
}
