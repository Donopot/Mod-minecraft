from pathlib import Path
P=Path(__file__).resolve().parents[1]/'src/main/java/net/mcreator/minepiece/procedures'
(P/'LightBuffs.java').write_text('''package net.mcreator.minepiece.procedures;
import java.util.*;
import br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.mcreator.minepiece.init.MinepieceModItems;

/** Light owns only named attributes. It never removes, snapshots or extends a vanilla potion. */
public final class LightBuffs {
 private static final class Buff {final ServerPlayer owner;final ServerLevel world;final Holder<MobEffect> effect;final Identifier id;final int amplifier;int ttl;Buff(ServerPlayer p,Holder<MobEffect> e,int a,int t){owner=p;world=(ServerLevel)p.level();effect=e;amplifier=a;ttl=t;id=Identifier.fromNamespaceAndPath("minepiece",e==MobEffects.SPEED?"light_speed":"light_strength");}}
 private static final Map<String,Buff> BUFFS=new HashMap<>();private static final Map<String,String> LAST=new HashMap<>();private static int applied,refused,removed,preserved;
 private LightBuffs(){}
 private static String key(ServerPlayer p,Holder<MobEffect> e){return p.getUUID()+"|"+e.unwrapKey();}
 private static AttributeInstance attribute(Buff b){return b.owner.getAttribute(b.effect==MobEffects.SPEED?Attributes.MOVEMENT_SPEED:Attributes.ATTACK_DAMAGE);}
 public static boolean apply(ServerPlayer p,Holder<MobEffect> e,int ticks,int amplifier){if(p==null || !(p.level() instanceof ServerLevel l) || !BatchRules.usable(p,l,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || e!=MobEffects.SPEED && e!=MobEffects.STRENGTH)return false;
  int amp=Math.max(0,Math.min(DevilFruitPikaConfig.buffMaxAmplifier(),amplifier));var external=p.getEffect(e);if(external!=null && external.getAmplifier()>amp){refused++;return false;}
  String k=key(p,e);Buff b=BUFFS.get(k);if(b!=null && b.owner!=p){remove(k,"remplacement");b=null;}if(b==null){b=new Buff(p,e,amp,Math.min(200,Math.max(1,ticks)));BUFFS.put(k,b);applied++;}else b.ttl=Math.max(b.ttl,Math.min(200,Math.max(1,ticks)));update(b);return true;}
 private static void update(Buff b){var a=attribute(b);if(a==null)return;a.removeModifier(b.id);var external=b.owner.getEffect(b.effect);int ext=external==null?0:external.getAmplifier()+1;double increment=Math.max(0,b.amplifier+1-ext)*(b.effect==MobEffects.SPEED?.2:3);if(increment>0)a.addTransientModifier(new AttributeModifier(b.id,increment,b.effect==MobEffects.SPEED?AttributeModifier.Operation.ADD_MULTIPLIED_BASE:AttributeModifier.Operation.ADD_VALUE));}
 private static boolean remove(String k,String why){var b=BUFFS.remove(k);if(b==null)return false;var a=attribute(b);if(a!=null)a.removeModifier(b.id);if(b.owner.hasEffect(b.effect))preserved++;removed++;LAST.put(k,why);return true;}
 public static boolean isActive(ServerPlayer p,Holder<MobEffect> e){var b=p==null?null:BUFFS.get(key(p,e));return b!=null && b.owner==p;}
 public static int activeCount(){return BUFFS.size();}
 public static String lastEndReason(ServerPlayer p,Holder<MobEffect> e){return p==null?"aucune":LAST.getOrDefault(key(p,e),"aucune");}
 public static boolean restore(ServerPlayer p,Holder<MobEffect> e,String why){return p!=null && remove(key(p,e),why);}
 public static void restoreAll(ServerPlayer p,String why){for(var entry:new ArrayList<>(BUFFS.entrySet()))if(entry.getValue().owner==p)remove(entry.getKey(),why);}
 public static void clearAll(String why){for(String k:new ArrayList<>(BUFFS.keySet()))remove(k,why);}
 public static void tick(){for(var entry:new ArrayList<>(BUFFS.entrySet())){var b=entry.getValue();if(--b.ttl<=0){remove(entry.getKey(),"echeance");continue;}if(!BatchRules.usable(b.owner,b.world,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA)){remove(entry.getKey(),"retrait");continue;}update(b);}}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->tick());ServerPlayConnectionEvents.DISCONNECT.register((h,s)->restoreAll(h.player,"deconnexion"));ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll("arret"));}
 public static int refusedCount(){return refused;}public static int externalRestoredCount(){return preserved;}
 public static void resetReport(){applied=refused=removed=preserved=0;}
 public static String buffReport(){return "applied="+applied+" refused="+refused+" removed="+removed+" external_preserved="+preserved;}
}
''',encoding='utf-8')
f=P/'LightCharge.java';s=f.read_text(encoding='utf-8')
s=s.replace('      final UUID owner;','      final UUID owner;\n      final ServerPlayer reference;final ServerLevel world;\n      int fired,interval;')
s=s.replace('Charge(UUID owner, ItemStack fruit, DevilFruitMove move, int ttl) {','Charge(ServerPlayer reference,UUID owner, ItemStack fruit, DevilFruitMove move, int ttl) {\n         this.reference=reference;this.world=(ServerLevel)reference.level();')
start=s.index('      ServerTickEvents.END_SERVER_TICK.register');end=s.index('      ServerPlayConnectionEvents.DISCONNECT',start)
s=s[:start]+'''      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
'''+s[end:]
s=s.replace('if (DevilFruitPowerLogic.isSuppressed(player)) {','if (!BatchRules.usable(player,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || CHARGES.containsKey(player.getUUID())) {')
s=s.replace('new Charge(player.getUUID(), fruit, move, ttl)','new Charge(player,player.getUUID(), fruit.copy(), move, ttl)')
s=s.replace('      // Signal VISIBLE','      LAST_IMPACTS.remove(player.getUUID());LAST_END.remove(player.getUUID());\n      // Signal VISIBLE')
s=s.replace('ServerPlayer porteur = OWNER_REFS.get(c.owner);','ServerPlayer porteur = c.reference;')
s=s.replace('if (porteur == null || porteur.isRemoved() || !porteur.isAlive()','if (!BatchRules.usable(porteur,c.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || porteur.isRemoved() || !porteur.isAlive()')
s=s.replace('         int impacts = tirerVolee(level, porteur, c);','         if(c.interval++%2!=0)continue;\n         tirerVolee(level,porteur,c);c.fired++;\n         int impacts=c.fired;LAST_IMPACTS.put(entree.getKey(),impacts);\n         if(c.fired<DevilFruitPikaConfig.barrageMaxImpacts())continue;\n         porteur.removeEffect(MinepieceModMobEffects.LIGHT_CHARGE);OWNER_REFS.remove(c.owner);')
s=s.replace('for (int i = 0; i < impacts; i++) {','for (int i = c.fired; i < c.fired+1; i++) {')
s=s.replace('         rayon.visualProfile(c.move.id());','         rayon.visualProfile(c.move.id()).batchOrigin(porteur);')
s=s.replace('      CHARGES.clear();','      CHARGES.clear();OWNER_REFS.clear();')
s=s.replace('      Charge c = CHARGES.remove(player.getUUID());','      Charge c = CHARGES.remove(player.getUUID());OWNER_REFS.remove(player.getUUID());')
f.write_text(s,encoding='utf-8')
print('Light services migrated')
