package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Nonspreading flame pillar. Its visible cylinder is also its damage volume. */
public final class FireRing {
   private static final class Ring {
      final ServerLevel level;
      final ServerPlayer owner;
      final Vec3 center;
      final double radius;
      final net.mcreator.minepiece.entity.EntityGomuArmEntity visual;
      int remaining = DevilFruitFlameConfig.ringLifetimeTicks();
      int elapsed;
      Ring(ServerLevel level, BlockPos pos, ServerPlayer owner, double requestedRadius) {
         this.level=level; this.owner=owner;
         center=new Vec3(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
         radius=requestedRadius;
         visual=MeraTechniques.visual(owner,"hibashira",center,remaining);
         visual.setStretch((float)radius);
      }
   }
   private static final List<Ring> RINGS=new ArrayList<>();
   private FireRing() { }
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->clearFrom(handler.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->clearAll());
   }
   /** Returns the number of visible perimeter anchors; no vanilla fire blocks can spread or harm allies. */
   public static int create(ServerLevel level, BlockPos center, ServerPlayer owner) {
      return create(level,center,owner,DevilFruitFlameConfig.ringRadius());
   }
   public static int create(ServerLevel level,BlockPos center,ServerPlayer owner,double radius) {
      if (!DevilFruitFlameConfig.ringEnabled() || DevilFruitExplosions.isProtectedPosition(level,center)) return 0;
      clearFrom(owner);
      RINGS.add(new Ring(level,center,owner,radius));
      return crownPositions(center,DevilFruitFlameConfig.ringRadius()).size();
   }
   public static List<BlockPos> crownPositions(BlockPos center,double radius) {
      var out=new ArrayList<BlockPos>();
      int r=(int)Math.ceil(radius);
      for(BlockPos pos:BlockPos.betweenClosed(center.offset(-r,0,-r),center.offset(r,0,r))) {
         double d=Math.sqrt(pos.distSqr(center));
         if(d<=radius && d>=radius-1) out.add(pos.immutable());
      }
      return out;
   }
   public static void tick() {
      for(Ring ring:new ArrayList<>(RINGS)) {
         if(--ring.remaining<=0 || !ring.owner.isAlive() || ring.owner.isRemoved()
               || ring.owner.level()!=ring.level || DevilFruitPowerLogic.isSuppressed(ring.owner)
               || !DevilFruitPowerLogic.getDevilFruit(ring.owner).is(net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA)) {
            ring.visual.discard(); RINGS.remove(ring); continue;
         }
         if(++ring.elapsed%20==0) MeraTechniques.strikeVolume(ring.owner,ring.center,ring.radius,5,
            DevilFruitFlameConfig.ringDamagePerSecond(),0);
      }
   }
   public static int activeRings() { return RINGS.size(); }
   public static void clearFrom(ServerPlayer owner) {
      for(Ring ring:new ArrayList<>(RINGS)) if(ring.owner.getUUID().equals(owner.getUUID())) {
         ring.visual.discard(); RINGS.remove(ring);
      }
   }
   public static void clearAll() {
      for(Ring ring:RINGS) ring.visual.discard();
      RINGS.clear();
   }
}
