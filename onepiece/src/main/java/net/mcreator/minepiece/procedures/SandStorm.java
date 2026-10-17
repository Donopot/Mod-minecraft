package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.sand.DevilFruitSandConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Tempête de sable</b> — Suna Suna (FRUIT-SUNA_SUNA-03).
 *
 * <p>Le plan : « créer une <b>tempête</b> avec <b>volume visible</b>, durée et effets cohérents »,
 * critère « <b>seules les cibles dans le volume autorisé</b> subissent les effets ».</p>
 *
 * <h2>Ce que le critère interdit</h2>
 * <p>Il ne dit pas « la tempête inflige des dégâts » — il dit que <b>seules</b> les cibles du volume
 * autorisé sont touchées. Cela impose deux choses :</p>
 * <ol>
 *   <li><b>Une seule géométrie</b> — le rayon passé aux particules est <b>le même objet</b> que celui
 *       passé à la recherche de cibles ({@link #volume}). Deux constantes séparées dériveraient, et la
 *       tempête finirait par frapper hors de ce qu'elle montre.</li>
 *   <li><b>Aucun effet rétroactif</b> — une cible qui sort du volume n'est plus touchée, et rien ne
 *       subsiste après l'expiration : le volume <i>est</i> la zone d'effet, à l'instant présent.</li>
 * </ol>
 *
 * <p>La tempête ne modifie <b>aucun bloc</b> : elle n'a donc rien à restaurer, et aucune zone protégée
 * ne peut être atteinte par elle. Les effets passent par {@link Desiccation}, qui refuse les cibles
 * humides — l'interaction avec l'eau est ainsi appliquée en un seul endroit.</p>
 */
public final class SandStorm {

   private static final class Storm {
      final String dimension;
      final ServerPlayer owner;final ServerLevel world;
      final Vec3 centre;
      final double radius;
      final double height;
      int remaining;
      int damageCountdown;
      net.mcreator.minepiece.entity.EntityGomuArmEntity visual;

      Storm(ServerPlayer owner,String dimension, Vec3 centre, double radius, double height, int lifetime) {
         this.owner=owner;this.world=(ServerLevel)owner.level();
         this.dimension = dimension;
         this.centre = centre;
         this.radius = radius;
         this.height = height;
         this.remaining = lifetime;
         this.damageCountdown = 20;
         visual=ElementalBatchTechniques.fx(owner,"sables-native",centre,Math.min(600,lifetime));visual.setModel("tornado");visual.setTexture("slash_sand");visual.setAnimation("idle");
      }
   }

   private static final List<Storm> STORMS = new ArrayList<>();

   private SandStorm() {
   }

   /** Branche l'entretien des tempêtes sur le tick serveur. */
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> tick());
   }

   /**
    * Déchaîne une tempête centrée sur un point.
    *
    * @return volume (rayon, hauteur) retenu par la tempête
    */
   public static double[] create(ServerPlayer owner) {
      if(owner==null || !(owner.level() instanceof ServerLevel level) || !BatchRules.usable(owner,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA))return new double[]{0,0};
      for(var storm:new ArrayList<>(STORMS))if(storm.owner==owner){storm.visual.discard();STORMS.remove(storm);}if(STORMS.size()>=8){STORMS.get(0).visual.discard();STORMS.remove(0);}
      double radius = DevilFruitSandConfig.stormRadius();
      double height = DevilFruitSandConfig.stormHeight();
      STORMS.add(new Storm(owner,level.dimension().identifier().toString(), owner.position(),
         radius, height, DevilFruitSandConfig.stormLifetimeTicks()));
      return new double[] {radius, height};
   }

   /**
    * Volume d'effet d'une tempête : <b>la seule</b> définition de sa zone.
    *
    * <p>Utilisé à la fois pour dessiner et pour frapper — c'est ce qui garantit l'égalité entre les
    * deux, plutôt que de l'espérer.</p>
    */
   public static AABB volume(Vec3 centre, double radius, double height) {
      return new AABB(centre.x - radius, centre.y - 0.5, centre.z - radius,
         centre.x + radius, centre.y + height, centre.z + radius);
   }

   /** Nombre de tempêtes vivantes (observable). */
   public static int activeStorms() {
      return STORMS.size();
   }

   /** Oublie toutes les tempêtes (fin de test, reset). */
   public static void clearFrom(ServerPlayer p){for(var storm:new ArrayList<>(STORMS))if(storm.owner==p){storm.visual.discard();STORMS.remove(storm);}}
   public static void clearAll() {
      for(var storm:STORMS)storm.visual.discard();STORMS.clear();
   }

   /** Positions des particules : tirées du même volume que les effets. */
   public static List<Vec3> samplePositions(Vec3 centre, double radius, double height, int count) {
      AABB box = volume(centre, radius, height);
      List<Vec3> out = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
         double angle = (i * 2.399) % (Math.PI * 2);
         double r = radius * (0.3 + 0.7 * ((i % 7) / 7.0));
         out.add(new Vec3(centre.x + Math.cos(angle) * r,
            box.minY + height * ((i % 5) / 5.0),
            centre.z + Math.sin(angle) * r));
      }
      return out;
   }

   /** Entretien : effets dans le volume, expiration. Exposé pour les tests. */
   public static void tick() {
      if (STORMS.isEmpty()) {
         return;
      }
      MinecraftServer server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return;
      }
      for (Storm storm : new ArrayList<>(STORMS)) {
         if(!BatchRules.usable(storm.owner,storm.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA)){storm.visual.discard();STORMS.remove(storm);continue;}
         storm.remaining--;
         if (storm.remaining <= 0) {
            storm.visual.discard();STORMS.remove(storm);
            continue;
         }
         ServerLevel level = null;
         for (ServerLevel candidate : server.getAllLevels()) {
            if (candidate.dimension().identifier().toString().equals(storm.dimension)) {
               level = candidate;
               break;
            }
         }
         if (level == null) {
            continue;
         }
         AABB box = volume(storm.centre, storm.radius, storm.height);
         boolean frapper = --storm.damageCountdown <= 0;
         if (frapper) {
            storm.damageCountdown = 20;
         }
         for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if(!BatchRules.target(storm.owner,cible) || Math.hypot(cible.getX()-storm.centre.x,cible.getZ()-storm.centre.z)>storm.radius)continue;
            if(frapper && CombatSkillLogic.hurtAndConfirm(cible,level,CombatSkillLogic.elementalDamageSource(level,ElementType.PHYSICAL,storm.owner,null),(float)DevilFruitSandConfig.stormDamagePerSecond()))Desiccation.apply(cible,storm.owner);
         }
         // Rendu : particules tirées du même volume.
         for (Vec3 pos : samplePositions(storm.centre, storm.radius, storm.height, 10)) {
            level.sendParticles(ParticleTypes.ASH, pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.01);
         }
         level.sendParticles(ParticleTypes.SMOKE, storm.centre.x, storm.centre.y + 0.4,
            storm.centre.z, 6, storm.radius * 0.7, 0.3, storm.radius * 0.7, 0.01);
      }
   }
}
