package net.mcreator.minepiece.client;
import java.util.*;
/** The per-level cache never survives disconnect or transfers a coat to another avatar. */
public final class MaterialClientState {
 private static final Map<UUID,Integer> FLAGS=new HashMap<>(),RECEIVED=new HashMap<>();private static net.minecraft.client.multiplayer.ClientLevel level;
 public static void useLevel(net.minecraft.client.multiplayer.ClientLevel current){if(level!=current){FLAGS.clear();RECEIVED.clear();level=current;}}
 public static int received(UUID p){return RECEIVED.getOrDefault(p,0);}
 public static int get(UUID p){return FLAGS.getOrDefault(p,0);}
 public static void register(){net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(net.mcreator.minepiece.network.MaterialBodyState.TYPE,(p,c)->c.client().execute(()->{useLevel(c.client().level);FLAGS.put(p.player(),p.flags());RECEIVED.merge(p.player(),1,Integer::sum);}));net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{FLAGS.clear();RECEIVED.clear();level=null;});}
}
