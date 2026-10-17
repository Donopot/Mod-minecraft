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
  Cloud(ServerPlayer p){this(p,p.position(),Math.min(12,DevilFruitMokuConfig.cloudRadius()),Math.min(600,DevilFruitMokuConfig.cloudDurationTicks()));}
  Cloud(ServerPlayer p,Vec3 point,double r,int t){owner=p;world=(ServerLevel)p.level();center=point;radius=r;ttl=t;}
  boolean contains(LivingEntity t){return t.level()==world && t.getY()>=center.y-.25 && t.getY()<=center.y+2.25 && Math.hypot(t.getX()-center.x,t.getZ()-center.z)<=radius;}
 }
 private static final class Capture {final LivingEntity target;final Cloud cloud;int ttl;Capture(LivingEntity t,Cloud c){target=t;cloud=c;ttl=Math.min(100,DevilFruitMokuConfig.captureDurationTicks());}}
 private static final List<Cloud> CLOUDS=new ArrayList<>();
 private static final Map<UUID,Capture> CAPTURED=new HashMap<>();
 // One capture per target per cloud: no immediate recapture after the hard duration cap.
 private static final Map<Cloud,Set<UUID>> SERVED=new IdentityHashMap<>();
 private static final Map<UUID,String> LAST_RELEASE=new HashMap<>();
 private SmokeCloud(){}
 private static boolean valid(Cloud c){return BatchRules.usable(c.owner,c.world,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU);}
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(s->{tickClouds();for(ServerPlayer p:CLOUDS.stream().map(c->c.owner).distinct().toList())tickMobility(p);tickCaptures();});ServerPlayConnectionEvents.DISCONNECT.register((h,s)->endCloudsFrom(h.player,"deconnexion"));ServerLifecycleEvents.SERVER_STOPPING.register(s->endAllClouds("arret"));}
 public static boolean create(ServerPlayer p){if(p==null || !(p.level() instanceof ServerLevel l) || !BatchRules.usable(p,l,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU))return false;
  while(CLOUDS.size()>=Math.min(8,DevilFruitMokuConfig.cloudMaxActive()))end(CLOUDS.get(0),"plafond");
  var c=new Cloud(p);CLOUDS.add(c);SERVED.put(c,new HashSet<>());render(c,40);return true;}
 private static void render(Cloud c,int n){for(int i=0;i<n;i++){double angle=i*2.399+c.ttl*.11,r=c.radius*Math.sqrt((i+.5)/n);c.world.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,c.center.x+Math.cos(angle)*r,c.center.y+.3+(i%4)*.6,c.center.z+Math.sin(angle)*r,1,.15,.2,.15,.01);}}
 public static boolean createAt(ServerPlayer p,Vec3 point){if(!(p.level() instanceof ServerLevel l) || !BatchRules.usable(p,l,MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU))return false;while(CLOUDS.size()>=Math.min(8,DevilFruitMokuConfig.cloudMaxActive()))end(CLOUDS.get(0),"plafond");var c=new Cloud(p,point,2,80);CLOUDS.add(c);SERVED.put(c,new HashSet<>());render(c,20);captureInClouds(l);return true;}
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
 private static boolean canCapture(Cloud c,LivingEntity t){return valid(c) && c.contains(t) && BatchRules.target(c.owner,t) && !t.isInvulnerableTo(c.world,CombatSkillLogic.elementalDamageSource(c.world,ElementType.PHYSICAL,c.owner,null)) && !(t instanceof ServerPlayer p && (p.isCreative() || p.isSpectator()));}
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
