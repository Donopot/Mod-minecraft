package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.inu.DevilFruitOkuchiConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Souffle glacé dirigé</b> — Inu Inu Okuchi No Makami (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-04).
 *
 * <p>Le plan : « créer un <b>souffle glacé dirigé</b> et des <b>états de gel bornés</b> », critère
 * « <b>souffle, gel et collision</b> correspondent au contrat et <b>la cible est libérée</b> ».</p>
 *
 * <h2>« Souffle, gel ET COLLISION » — trois exigences, pas une</h2>
 * <p>Le mot <b>collision</b> est celui qui change la conception. Un souffle est une <b>matière</b> : il ne
 * traverse pas la pierre. Sans cette règle, on gèlerait des cibles <b>à travers les murs</b> — invisible
 * à l'œil, et profondément injuste : la victime se fait figer sans rien voir venir et sans comprendre
 * pourquoi elle n'était pas à couvert. Derrière un mur, on est à couvert ; c'est tout l'intérêt.</p>
 *
 * <p>Chaque rayon du cône est donc <b>tracé</b> et <b>arrêté par le premier bloc plein</b>. Le souffle
 * s'arrête là où la matière s'arrête.</p>
 *
 * <h2>Une seule géométrie, dessinée et appliquée</h2>
 * <p>{@link #cone} produit les rayons, {@link #visible} les affiche et {@link #strike} frappe — les trois
 * lisent <b>le même</b> résultat de {@code cone}. Ce qui est gelé est exactement ce qui a été montré.</p>
 *
 * <h2>« La cible est libérée »</h2>
 * <p>Le gel n'est pas géré ici : il est confié au <b>service de gel partagé</b> (celui de Hie Hie), qui
 * porte déjà le <b>plafond dur</b> et la <b>fenêtre de liberté garantie</b>. Ce n'est pas une
 * réutilisation de commodité : c'est la <b>même règle</b>, et elle ne doit exister qu'à un endroit. Deux
 * gels concurrents, ce serait deux façons d'être prisonnier — donc une qui oublie de libérer.</p>
 */
public final class IceBreath {

   /** Un rayon du cône : sa direction, sa portée utile (arrêtée par la matière) et son origine. */
   public record Ray(Vec3 direction, double reach) {
   }

   private IceBreath() {
   }

   /**
    * Construit le cône de souffle : des rayons équirépartis, chacun <b>arrêté par le premier bloc
    * plein</b> s'il y en a un.
    *
    * <p>Exposé pour que le dessin et la frappe lisent <b>la même</b> structure.</p>
    */
   public static List<Ray> cone(ServerLevel level, Vec3 origin, Vec3 look) {
      List<Ray> rays = new ArrayList<>();
      Vec3 forward = horizontalOr(look);
      double half = Math.toRadians(DevilFruitOkuchiConfig.breathHalfAngleDegrees());
      int samples = Math.max(1, DevilFruitOkuchiConfig.breathRaySamples());
      for (int i = 0; i < samples; i++) {
         // de -half à +half : le rayon central est la direction du regard
         double angle = samples == 1 ? 0.0 : -half + (2.0 * half) * i / (samples - 1);
         Vec3 dir = forward.yRot((float) angle);
         rays.add(new Ray(dir, reachOf(level, origin, dir)));
      }
      // Le rayon du milieu (regard direct) est ajouté en premier s'il manque : un cône sans axe n'aurait
      // pas de centre, et la cible pile devant serait la seule épargnée.
      if (rays.stream().noneMatch(r -> r.direction().dot(forward) > 0.9999)) {
         rays.add(0, new Ray(forward, reachOf(level, origin, forward)));
      }
      return rays;
   }

   /** Portée utile d'un rayon : la distance jusqu'au premier bloc plein, ou la portée nominale. */
   private static double reachOf(ServerLevel level, Vec3 origin, Vec3 dir) {
      double max = DevilFruitOkuchiConfig.breathRange();
      if (!DevilFruitOkuchiConfig.breathBlockedByWalls()) {
         return max;
      }
      // Pas de 0,5 bloc : assez fin pour ne pas traverser une paroi d'une seule couche.
      for (double d = 0.5; d <= max; d += 0.5) {
         Vec3 point = origin.add(dir.scale(d));
         BlockPos pos = BlockPos.containing(point.x, point.y, point.z);
         if (!level.isLoaded(pos)) {
            return d; // hors des zones chargées, on ne gèle pas
         }
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced()) {
            return d; // la matière arrête le souffle
         }
      }
      return max;
   }

   /** Affichage du souffle — lit exactement les rayons de {@link #cone}. */
   public static void visible(ServerLevel level, Vec3 origin, List<Ray> rays) {
      for (Ray ray : rays) {
         for (double d = 1.0; d <= ray.reach(); d += 1.0) {
            Vec3 p = origin.add(ray.direction().scale(d));
            level.sendParticles(MinepieceModParticles.ICE, p.x, p.y, p.z, 3, 0.15, 0.15, 0.15, 0.01);
            level.sendParticles(MinepieceModParticles.ICE, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
         }
      }
   }

   /** Une cible est-elle dans le souffle, à couvert ou non ? */
   public static boolean caught(LivingEntity target, Vec3 origin, List<Ray> rays) {
      Vec3 center = target.getBoundingBox().getCenter();
      for (Ray ray : rays) {
         Vec3 toTarget = center.subtract(origin);
         double along = toTarget.dot(ray.direction());
         if (along <= 0.0 || along > ray.reach()) {
            continue; // derrière le souffle, hors portée, ou arrêté avant
         }
         // Distance perpendiculaire au rayon : dans le cône si elle reste sous un rayon de cible.
         double perpendicular = toTarget.subtract(ray.direction().scale(along)).length();
         if (perpendicular <= 1.0) {
            return true;
         }
      }
      return false;
   }

   /**
    * Applique le souffle : gèle ce qui est dans le cône et non à couvert.
    *
    * @return nombre de cibles prises dans le souffle
    */
   public static int strike(ServerPlayer player, double damage, double knockback) {
      if (!(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      Vec3 origin = player.getEyePosition();
      List<Ray> rays = cone(level, origin, player.getLookAngle());
      visible(level, origin, rays);
      Set<LivingEntity> touchees = new HashSet<>();
      for (Ray ray : rays) {
         double reach = ray.reach();
         for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
               new net.minecraft.world.phys.AABB(origin, origin.add(ray.direction().scale(reach)))
                  .inflate(1.5))) {
            if (candidate == player || !candidate.isAlive()) {
               continue;
            }
            if (!caught(candidate, origin, rays)) {
               continue;
            }
            touchees.add(candidate);
         }
      }
      int n = 0;
      for (LivingEntity target : touchees) {
         var source = CombatSkillLogic.elementalDamageSource(level, ElementType.ICE, player, null);
         if (target.hurtServer(level, source, (float) damage)) {
            Vec3 push = target.position().subtract(player.position()).normalize();
            target.knockback(push.x, knockback, push.z, source, 0.3F);
            // Gel borné confié au service partagé : plafond dur + libération garantie.
            FreezeService.apply(target, DevilFruitOkuchiConfig.breathFreezeTicks());
            n++;
         }
      }
      return n;
   }

   /** Direction horizontale de repli si le regard est vertical (sinon le cône n'aurait pas de largeur). */
   private static Vec3 horizontalOr(Vec3 look) {
      Vec3 flat = new Vec3(look.x, 0.0, look.z);
      if (flat.lengthSqr() < 1.0E-6) {
         return new Vec3(0.0, 0.0, 1.0);
      }
      return flat.normalize();
   }
}
