package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.wolf.DevilFruitWolfConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Bond et morsure du loup</b> — Inu Inu Wolf (FRUIT-INU_INU_WOLF-04).
 *
 * <p>Deux des trois fonctions du critère (« bonds, morsures et poursuite »). Les deux vérifient les
 * <b>trois garde-fous</b> — <b>collisions, coûts, permissions</b> — séparément : une fonction peut
 * respecter le coût et ignorer les collisions, ou respecter les collisions et ignorer les permissions.</p>
 *
 * <h2>Le bond : « collisions » veut dire qu'on ne traverse pas la pierre en sautant</h2>
 * <p>Un bond est le déplacement le plus tentant à implémenter par simple ajout de vitesse : c'est aussi
 * celui qui traverse les murs, parce qu'un déplacement rapide peut franchir un obstacle <b>entre deux
 * ticks</b>. Le bond est donc <b>échantillonné</b> le long de sa trajectoire : si un bloc plein se trouve
 * sur le chemin, le bond s'arrête <b>juste avant</b>. On saute sur le mur, on ne le traverse pas.</p>
 *
 * <h2>La morsure : le loup secoue sa prise</h2>
 * <p>Portée courte, recul fort. Le recul n'est pas décoratif : dans un loup, mordre sert à <b>écarter</b>
 * — un prédateur qui ne repousse pas sa proie la laisse riposter. Le recul du contrat (2,0) est donc un
 * choix, pas un réglage.</p>
 */
public final class WolfStrike {

   private WolfStrike() {
   }

   /**
    * Bond dirigé. Le déplacement est <b>échantillonné</b> et s'arrête au premier obstacle.
    *
    * @return distance réellement parcourue, en blocs
    */
   public static double leap(ServerPlayer player, double distance) {
      if (player == null || !player.isAlive() || !(player.level() instanceof ServerLevel level)) {
         return 0.0;
      }
      // PERMISSIONS
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return 0.0;
      }
      Vec3 look = player.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0, look.z);
      if (flat.lengthSqr() < 1.0E-6) {
         flat = new Vec3(0.0, 0.0, 1.0);
      }
      flat = flat.normalize();
      double max = Math.min(distance, DevilFruitWolfConfig.leapDistance());
      double parcouru = 0.0;
      // Pas de 0,5 bloc : assez fin pour ne pas franchir une paroi d'une seule couche entre deux ticks.
      for (double d = 0.5; d <= max; d += 0.5) {
         Vec3 next = player.position().add(flat.scale(0.5));
         BlockPos pos = BlockPos.containing(next);
         if (!level.isLoaded(pos)) {
            break;
         }
         AABB box = player.getBoundingBox().move(flat.scale(0.5));
         if (!level.noCollision(player, box)) {
            break; // un obstacle : on s'arrête juste avant, on ne traverse pas
         }
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced()) {
            break;
         }
         player.teleportTo(next.x, player.getY(), next.z);
         parcouru = d;
      }
      if (parcouru > 0.0) {
         // Part verticale : un bond purement horizontal serait plat et illisible.
         player.setDeltaMovement(player.getDeltaMovement().add(0.0, DevilFruitWolfConfig.leapUpward(), 0.0));
         level.playSound(null, player.blockPosition(), SoundEvents.WOLF_STEP.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
         level.sendParticles(MinepieceModParticles.BIG_SMOKE, player.getX(), player.getY() + 0.1, player.getZ(),
            12, 0.3, 0.1, 0.3, 0.03);
      }
      return parcouru;
   }

   /**
    * Morsure : frappe courte et recul fort. Lit la distance réellement atteinte.
    *
    * @return nombre de cibles mordues
    */
   public static int bite(ServerPlayer player, double damage, double knockback) {
      if (player == null || !(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return 0;
      }
      double range = DevilFruitWolfConfig.biteRange();
      int n = 0;
      for (net.minecraft.world.entity.LivingEntity target : level.getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            player.getBoundingBox().inflate(range, 1.5, range))) {
         if (target == player || !target.isAlive()) {
            continue;
         }
         if (!CombatSkillLogic.isValidTarget(player, target)) {
            continue;
         }
         // La morsure ne porte que DEVANT : on ne mord pas dans son dos.
         Vec3 toTarget = target.position().subtract(player.position());
         Vec3 flat = new Vec3(toTarget.x, 0.0, toTarget.z);
         if (flat.lengthSqr() > 1.0E-6) {
            Vec3 look = player.getLookAngle();
            Vec3 lookFlat = new Vec3(look.x, 0.0, look.z);
            if (lookFlat.lengthSqr() > 1.0E-6
                  && lookFlat.normalize().dot(flat.normalize()) < CombatSkillLogic.MELEE_CONE_COS) {
               continue;
            }
         }
         var source = CombatSkillLogic.elementalDamageSource(level, ElementType.PHYSICAL, player, null);
         if (target.hurtServer(level, source, (float) damage)) {
            Vec3 push = flat.lengthSqr() > 1.0E-6 ? flat.normalize() : new Vec3(0.0, 0.0, 1.0);
            target.knockback(push.x, DevilFruitWolfConfig.biteKnockback(), push.z, source, 0.35F);
            n++;
         }
      }
      if (n > 0) {
         level.playSound(null, player.blockPosition(), SoundEvents.WOLF_SHAKE, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
      return n;
   }
}
