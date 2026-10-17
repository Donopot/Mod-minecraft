"""Local one-shot B01 edit, kept for review of the service migration."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
P=ROOT/'src/main/java/net/mcreator/minepiece/procedures'
def put(name,text): (P/name).write_text(text.strip()+'\n',encoding='utf-8')
put('SmokeCloud.java',r'''
package net.mcreator.minepiece.procedures;
import java.util.*;
import br.com.oneminepiece.opworldlike.moku.DevilFruitMokuConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.*;

/** Owned finite cylinders, late-entry capture, separate reversible modifiers and paid mobility. */
public final class SmokeCloud {
 private static final Identifier MOB=Identifier.fromNamespaceAndPath("minepiece","smoke_mobility"), CAP=Identifier.fromNamespaceAndPath("minepiece","smoke_capture");
 private static final class Cloud {
  final ServerPlayer owner;final ServerLevel world;final Vec3 center;final double radius;int ttl;
  Cloud(ServerPlayer p){owner=p;world=(ServerLevel)p.level();center=p.position();radius=Math.min(12,DevilFruitMokuConfig.cloudRadius());ttl=Math.min(600,DevilFruitMokuConfig.cloudDurationTicks());}
  boolean contains(LivingEntity t){return t.level()==world && Math.abs(t.getY()-center.y)<=radius && Math.hypot(t.getX()-center.x,t.getZ()-center.z)<=radius;}
 }
 private static final class Capture {final LivingEntity target;final Cloud cloud;int ttl;Capture(LivingEntity t,Cloud c){target=t;cloud=c;ttl=Math.min(100,DevilFruitMokuConfig.captureDurationTicks());}}
 private static final List<Cloud> CLOUDS=new ArrayList<>();
 private static final Map<UUID,Capture> CAPTURED=new HashMap<>();
 // One capture per target per cloud: no immediate recapture after the hard duration cap.
 private static final Map<Cloud,Set<UUID>> SERVED=new IdentityHashMap<>();
 private static final Map<UUID,String> LAST_RELEASE=new HashMap<>();
 private SmokeCloud(){}
 private static boolean valid(Cloud c){return BatchRules.usable(c.owner,c.world,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU);}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->{tickClouds();for(Cloud c:new ArrayList<>(CLOUDS))tickMobility(c.owner);tickCaptures();});ServerPlayConnectionEvents.DISCONNECT.register((h,s)->endCloudsFrom(h.player,"deconnexion"));ServerLifecycleEvents.SERVER_STOPPING.register(s->endAllClouds("arret"));}
 public static boolean create(ServerPlayer p){if(p==null || !(p.level() instanceof ServerLevel l) || !BatchRules.usable(p,l,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU))return false;
  while(CLOUDS.size()>=Math.min(8,DevilFruitMokuConfig.cloudMaxActive()))end(CLOUDS.get(0),"plafond");
  var c=new Cloud(p);CLOUDS.add(c);SERVED.put(c,new HashSet<>());render(c,40);return true;}
 private static void render(Cloud c,int n){for(int i=0;i<n;i++){double angle=i*2.399+c.ttl*.11,r=c.radius*Math.sqrt((i+.5)/n);c.world.sendParticles(MinepieceModParticles.BIG_SMOKE,c.center.x+Math.cos(angle)*r,c.center.y+.3+(i%4)*.6,c.center.z+Math.sin(angle)*r,1,.15,.2,.15,.01);}}
 public static int cloudCount(){return CLOUDS.size();}
 public static int cloudCountFor(UUID id){return (int)CLOUDS.stream().filter(c->c.owner.getUUID().equals(id)).count();}
 public static int capturedCount(){return CAPTURED.size();}
 public static boolean inOwnCloud(ServerPlayer p){return p!=null && CLOUDS.stream().anyMatch(c->c.owner==p && valid(c) && c.contains(p));}
 public static boolean isCaptured(LivingEntity t){var c=t==null?null:CAPTURED.get(t.getUUID());return c!=null && c.target==t;}
 public static String lastReleaseReason(LivingEntity t){return t==null?"aucune":LAST_RELEASE.getOrDefault(t.getUUID(),"aucune");}
 public static boolean hasMobility(ServerPlayer p){var a=p==null?null:p.getAttribute(Attributes.MOVEMENT_SPEED);return a!=null && a.getModifier(MOB)!=null;}
 private static void removeMobility(ServerPlayer p){var a=p.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(MOB);}
 public static void tickMobility(ServerPlayer p){removeMobility(p);if(!inOwnCloud(p) || isCaptured(p))return;var v=MinepieceModVariables.get(p);double cost=DevilFruitMokuConfig.mobilityEnergyPerSecond()/20;
  if(v.PlayerEnergy<cost || cost>0 && v.PlayerEnergy<=0)return;v.PlayerEnergy=Math.max(0,v.PlayerEnergy-cost);
  var a=p.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.addTransientModifier(new AttributeModifier(MOB,Math.min(1,DevilFruitMokuConfig.mobilitySpeedBonus()),AttributeModifier.Operation.ADD_MULTIPLIED_BASE));}
 public static ServerPlayer resolveOwner(UUID id){for(Cloud c:CLOUDS)if(c.owner.getUUID().equals(id) && valid(c))return c.owner;return null;}
 private static boolean canCapture(Cloud c,LivingEntity t){return valid(c) && c.contains(t) && BatchRules.target(c.owner,t) && !(t instanceof ServerPlayer p && (p.isCreative() || p.isSpectator()));}
 public static int captureInClouds(ServerLevel level){int n=0;for(Cloud c:new ArrayList<>(CLOUDS)){if(c.world!=level || !valid(c))continue;for(var t:level.getEntitiesOfClass(LivingEntity.class,new AABB(c.center,c.center).inflate(c.radius))){
  if(CAPTURED.size()>=Math.min(16,DevilFruitMokuConfig.captureMaxTargets()))return n;
  if(CAPTURED.containsKey(t.getUUID()) || SERVED.get(c).contains(t.getUUID()) || !canCapture(c,t))continue;
  var a=t.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null){a.removeModifier(CAP);a.addTransientModifier(new AttributeModifier(CAP,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
  var capture=new Capture(t,c);CAPTURED.put(t.getUUID(),capture);SERVED.get(c).add(t.getUUID());t.addEffect(new MobEffectInstance(MinepieceModMobEffects.SMOKE_CAPTURED,capture.ttl,0,false,false,true));n++;
 }}return n;}
 public static void tickClouds(){Set<ServerLevel> worlds=new HashSet<>();for(Cloud c:new ArrayList<>(CLOUDS)){if(!valid(c) || --c.ttl<=0){end(c,"echeance");continue;}worlds.add(c.world);if(c.ttl%5==0)render(c,16);}for(var l:worlds)captureInClouds(l);}
 public static void tickCaptures(){for(Capture c:new ArrayList<>(CAPTURED.values())){if(!CLOUDS.contains(c.cloud) || !canCapture(c.cloud,c.target)){release(c.target,"sortie");continue;}if(--c.ttl<=0){release(c.target,"echeance");continue;}var m=c.target.getDeltaMovement();c.target.setDeltaMovement(0,m.y,0);c.target.syncVelocity=true;}}
 public static void tickOwner(ServerPlayer p){for(Cloud c:new ArrayList<>(CLOUDS))if(c.owner==p && !valid(c))end(c,"retrait");if(!inOwnCloud(p))removeMobility(p);}
 public static void release(LivingEntity t,String why){if(t==null)return;var c=CAPTURED.get(t.getUUID());if(c==null || c.target!=t)return;CAPTURED.remove(t.getUUID());var a=t.getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(CAP);t.removeEffect(MinepieceModMobEffects.SMOKE_CAPTURED);LAST_RELEASE.put(t.getUUID(),why);}
 public static int releaseAllFrom(LivingEntity p,String why){int n=0;for(var c:new ArrayList<>(CAPTURED.values()))if(c.cloud.owner==p){release(c.target,why);n++;}return n;}
 public static int releaseEverything(String why){int n=CAPTURED.size();for(var c:new ArrayList<>(CAPTURED.values()))release(c.target,why);return n;}
 private static void end(Cloud c,String why){CLOUDS.remove(c);SERVED.remove(c);for(var a:new ArrayList<>(CAPTURED.values()))if(a.cloud==c)release(a.target,why);removeMobility(c.owner);}
 public static int endCloudsFrom(LivingEntity p,String why){int n=0;for(Cloud c:new ArrayList<>(CLOUDS))if(c.owner==p){end(c,why);n++;}if(p instanceof ServerPlayer sp)removeMobility(sp);return n;}
 public static int endAllClouds(String why){int n=CLOUDS.size();for(Cloud c:new ArrayList<>(CLOUDS))end(c,why);releaseEverything(why);return n;}
}
''')
f=P/'Desiccation.java';s=f.read_text(encoding='utf-8').replace('t.level()!=s.world || isWet(t)','t.level()!=s.world || DevilFruitSandConfig.wetResists() && isWet(t)');f.write_text(s,encoding='utf-8')
f=P/'SandStorm.java';s=f.read_text(encoding='utf-8');s=s.replace('final String dimension;','final String dimension;\n      final ServerPlayer owner;final ServerLevel world;').replace('Storm(String dimension, Vec3 centre, double radius, double height, int lifetime) {','Storm(ServerPlayer owner,String dimension, Vec3 centre, double radius, double height, int lifetime) {\n         this.owner=owner;this.world=(ServerLevel)owner.level();')
s=s.replace('ServerLevel level = (ServerLevel) owner.level();','if(owner==null || !(owner.level() instanceof ServerLevel level) || !BatchRules.usable(owner,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA))return new double[]{0,0};\n      STORMS.removeIf(storm->storm.owner==owner);if(STORMS.size()>=8)STORMS.remove(0);')
s=s.replace('STORMS.add(new Storm(level.dimension()', 'STORMS.add(new Storm(owner,level.dimension()')
s=s.replace('storm.remaining--;','if(!BatchRules.usable(storm.owner,storm.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA)){STORMS.remove(storm);continue;}\n         storm.remaining--;')
start=s.index('            // Le volume visible');end=s.index('\n         // Rendu',start)
s=s[:start]+'''            if(!BatchRules.target(storm.owner,cible) || Math.hypot(cible.getX()-storm.centre.x,cible.getZ()-storm.centre.z)>storm.radius)continue;
            if(frapper && CombatSkillLogic.hurtAndConfirm(cible,level,CombatSkillLogic.elementalDamageSource(level,ElementType.PHYSICAL,storm.owner,null),(float)DevilFruitSandConfig.stormDamagePerSecond()))Desiccation.apply(cible,storm.owner);
         }'''+s[end:];f.write_text(s,encoding='utf-8')
f=P/'YamiDarkness.java';s=f.read_text(encoding='utf-8');s=s.replace('final UUID porteur;','final UUID porteur;\n      final ServerPlayer owner;final ServerLevel world;').replace('Pull(UUID porteur, double rayon, double forceMax, int ttl) {','Pull(ServerPlayer owner,UUID porteur, double rayon, double forceMax, int ttl) {\n         this.owner=owner;this.world=(ServerLevel)owner.level();').replace('new Pull(porteur.getUUID(),','new Pull(porteur,porteur.getUUID(),')
s=s.replace('      ServerTickEvents.END_SERVER_TICK.register(server -> {\n         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {\n            OWNER_REFS.put(player.getUUID(), player);\n         }\n         tick();\n      });','      ServerTickEvents.END_SERVER_TICK.register(server -> tick());\n      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());')
s=s.replace('   private static int attractionsLancees;', '   private static final Map<UUID,Integer> ABSORB_TTL=new HashMap<>();\n   private static final Map<UUID,ServerLevel> ABSORB_WORLD=new HashMap<>();\n   private static boolean valid(ServerPlayer p,ServerLevel l){return BatchRules.usable(p,l,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI);}\n   private static int attractionsLancees;')
s=s.replace('if (DevilFruitPowerLogic.isSuppressed(porteur)) {','if (!(porteur.level() instanceof ServerLevel current) || !valid(porteur,current)) {')
s=s.replace('      ABSORB_REMAINING.put(porteur.getUUID(), DevilFruitYamiConfig.absorbCapacityHits());','      ABSORB_REMAINING.put(porteur.getUUID(), DevilFruitYamiConfig.absorbCapacityHits());\n      ABSORB_TTL.put(porteur.getUUID(),600);ABSORB_WORLD.put(porteur.getUUID(),(ServerLevel)porteur.level());')
s=s.replace('      Integer reste = ABSORB_REMAINING.get(porteur.getUUID());','      if(OWNER_REFS.get(porteur.getUUID())!=porteur || !valid(porteur,ABSORB_WORLD.get(porteur.getUUID())) || ABSORB_TTL.getOrDefault(porteur.getUUID(),0)<=0){clearFor(porteur,"invalide");return false;}\n      Integer reste = ABSORB_REMAINING.get(porteur.getUUID());')
s=s.replace('MinepieceModMobEffects.YAMI_ABSORBING, 600, 0, false, false));\n      return true;', 'MinepieceModMobEffects.YAMI_ABSORBING, ABSORB_TTL.getOrDefault(porteur.getUUID(),1), 0, false, false));\n      return true;')
s=s.replace('      PULLS.remove(porteur.getUUID());','      PULLS.remove(porteur.getUUID());\n      ABSORB_TTL.remove(porteur.getUUID());ABSORB_WORLD.remove(porteur.getUUID());OWNER_REFS.remove(porteur.getUUID());')
s=s.replace('      PULLS.clear();\n      ABSORB_REMAINING.clear();','      for(ServerPlayer p:new ArrayList<>(OWNER_REFS.values()))clearFor(p,"reset");\n      PULLS.clear();ABSORB_REMAINING.clear();ABSORB_TTL.clear();ABSORB_WORLD.clear();OWNER_REFS.clear();')
s=s.replace('   private static void tick() {','   private static void tick() {\n      for(var id:new ArrayList<>(ABSORB_TTL.keySet())){var p=OWNER_REFS.get(id);int left=ABSORB_TTL.get(id)-1;if(p==null || left<=0 || !valid(p,ABSORB_WORLD.get(id))){if(p!=null)clearFor(p,"echeance");else{ABSORB_TTL.remove(id);ABSORB_WORLD.remove(id);ABSORB_REMAINING.remove(id);}continue;}ABSORB_TTL.put(id,left);}')
s=s.replace('ServerPlayer porteur = OWNER_REFS.get(p.porteur);','ServerPlayer porteur = p.owner;').replace('if (porteur == null || porteur.isRemoved() || !porteur.isAlive()','if (!valid(porteur,p.world) || porteur.isRemoved() || !porteur.isAlive()')
s=s.replace('         if (!porteur.hasLineOfSight(cible)) {','         if (!BatchRules.target(porteur,cible)) {')
s=s.replace('         cible.push(direction.x * force, 0.0, direction.z * force);','         Vec3 movement=new Vec3(direction.x*force,cible.getDeltaMovement().y,direction.z*force);\n         if(!SafeDestination.isSafe(level,cible,cible.position().add(movement)))continue;\n         cible.setDeltaMovement(movement);cible.syncVelocity=true;')
f.write_text(s,encoding='utf-8')
print('B01 core service edits written')
