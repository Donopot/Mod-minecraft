package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig;
import java.util.List;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Rayons lumineux et frappes à la vitesse de la lumière</b> — Pika Pika no Mi
 * (FRUIT-PIKA_PIKA-03, et le volet « impacts exacts » de TECH-03/07).
 *
 * <h2>Portée explicite, et pas seulement une durée de vie</h2>
 * <p>Le plan : « Créer des rayons lumineux avec <b>collision et portée explicites</b> », critère « le
 * rayon <b>s'arrête aux obstacles prévus</b> et <b>touche la cible située sur sa trajectoire</b> ». Un
 * projectile qui n'expire que par durée de vie parcourt {@code durée × vitesse} blocs : ce n'est pas une
 * portée, c'est une <b>conséquence</b> — et une vitesse élevée la fait varier sans que personne ne l'ait
 * décidé. Les rayons de ce fruit portent donc une <b>portée en blocs</b>, appliquée par
 * {@link SkillProjectileEntity#range} (0 = illimitée : le comportement d'origine, donc aucune régression
 * sur les autres projectiles du mod).</p>
 *
 * <h2>Pourquoi les rayons sont RAPIDES et non instantanés</h2>
 * <p>Un projectile très rapide reste un projectile : il est <b>tracé</b> (traînée visible, synchronisée)
 * et sa collision est un <b>balayage</b> ({@code ProjectileUtil.getHitResultOnMoveVector}), donc il ne
 * traverse ni un mur ni une cible. Un rayon « instantané » (résolution immédiate) n'aurait ni traînée ni
 * synchronisation : il toucherait sans que personne n'ait rien vu. La vitesse vient de ver15
 * (CORE-016 : Laser 4,0 ; Magatama 3,5) — elle n'est pas réglée ici.</p>
 *
 * <h2>Frappe « à la vitesse de la lumière »</h2>
 * <p>Rejoue la règle du moteur plutôt que de la réécrire : cône ({@link CombatSkillLogic#MELEE_CONE_COS}),
 * cible valide ({@link CombatSkillLogic#isValidTarget} — équipes et PvP respectés) et <b>ligne de vue
 * dégagée</b>. La différence avec une mêlée générique est ailleurs : <b>un seul impact par cible</b>,
 * compté sur le retour de {@code hurtServer} — un compteur qui ignore ce retour annonce des impacts qui
 * n'ont rien fait.</p>
 */
public final class LightBeams {

   private LightBeams() {
   }

   /**
    * Tire un <b>rayon lumineux à portée explicite</b> reprenant exactement les valeurs ver15 de la
    * technique (dégâts, recul, énergie, recharge, particule) : seule la <b>portée</b> est ajoutée, et
    * vient de la configuration du fruit.
    */
   public static SkillProjectileEntity fireRay(ServerPlayer player, ItemStack fruit,
                                               DevilFruitMove move, double rangeBlocks) {
      if (!(player.level() instanceof ServerLevel level)) {
         return null;
      }
      String[] visuel = DevilFruitRegistry.projectileVisual(move.id());
      if (visuel == null) {
         // Repli intentionnel par élément (CORE-017) plutôt qu'un visuel hors sujet.
         visuel = DevilFruitElement.projectileVisualFallback(DevilFruitElement.of(fruit));
      }
      SkillProjectileEntity rayon = CombatSkillLogic.fireProjectile(player, move.damage(), move.knockback(),
         move.effect(), move.effectDuration(), move.effectAmplifier(), move.lifetime(), move.speed(),
         visuel[0], visuel[1], net.minecraft.core.particles.ParticleTypes.END_ROD, rangeBlocks);
      rayon.terrain(TechniqueTerrain.begin(player,move)).visualProfile(move.id()).element(DevilFruitElement.forTechnique(fruit, move.id()));
      // Le signal sonore fait partie de l'avertissement visible (-05) : la lumière s'entend avant de
      // frapper, même quand elle va vite.
      level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS,
         0.6F, 1.8F);
      return rayon;
   }

   /**
    * Un rayon est-il <b>arrêté</b> par un obstacle sur cette distance ?
    *
    * <p>Pas de 0,5 bloc : assez fin pour ne pas traverser une paroi d'une seule couche. Sert de prédicat
    * partagé — le même que celui qui décide de la ligne de vue d'une frappe — pour que « à couvert » ait
    * une seule définition.</p>
    */
   public static boolean blocked(ServerLevel level, Vec3 origine, Vec3 direction, double distance) {
      for (double d = 0.5; d <= distance; d += 0.5) {
         Vec3 point = origine.add(direction.scale(d));
         BlockPos pos = BlockPos.containing(point.x, point.y, point.z);
         if (!level.isLoaded(pos)) {
            return true;
         }
         BlockState etat = level.getBlockState(pos);
         if (!etat.isAir() && etat.getFluidState().isEmpty() && !etat.canBeReplaced()) {
            return true;
         }
      }
      return false;
   }

   /**
    * Frappe lumineuse : cône, ligne de vue dégagée, <b>un impact par cible</b>.
    *
    * @param bonus dégâts supplémentaires bornés (0 = aucun) — « Speed is Power » alimente ce paramètre
    *              avec le bonus de lumière, plafonné par la configuration
    * @return nombre de cibles réellement touchées
    */
   public static int strike(ServerPlayer player, ItemStack fruit, DevilFruitMove move, double bonus) {
      if (!(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      // La SÉLECTION des cibles vit dans CombatSkillLogic.strikeTargets : cône, cibles valides, portée
      // effective et ligne de vue. Ce fruit n'ajoute que sa conséquence — ici, rien de plus que le
      // bonus borné de « Speed is Power » et le son propre à la lumière.
      int touches = CombatSkillLogic.strikeTargets(player, fruit, move, bonus).size();
      level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
         0.8F, 1.8F);
      return touches;
   }

   /** Dégâts d'une frappe lumineuse, bonus compris — exposé pour que le test mesure la même chose. */
   public static double damageOf(DevilFruitMove move, double bonus) {
      return move.damage() + bonus;
   }

   /**
    * <b>Bonus de « Speed is Power »</b> : la lumière active ajoute des dégâts, dans une limite bornée.
    *
    * <p>La règle est lue sur l'état RÉEL du bonus de lumière ({@link LightBuffs#isActive}) et non sur un
    * compteur parallèle : si le bonus a été rendu (échéance, suppression, retrait), le supplément
    * disparaît avec lui, sans seconde source de vérité à synchroniser.</p>
    */
   public static double lightBonus(ServerPlayer player) {
      if (player == null || !LightBuffs.isActive(player, net.minecraft.world.effect.MobEffects.SPEED)) {
         return 0.0;
      }
      return DevilFruitPikaConfig.speedPowerBonusMax();
   }

   /** Portée d'un rayon Laser (configuration du fruit). */
   public static double laserRange() {
      return DevilFruitPikaConfig.laserRangeBlocks();
   }

   /** Portée des perles de Yasakani no Magatama (configuration du fruit). */
   public static double magatamaRange() {
      return DevilFruitPikaConfig.magatamaRangeBlocks();
   }

   /** Une boîte de ciblage utile aux tests (cône de la technique). */
   public static AABB coneBox(ServerPlayer player, double range) {
      return player.getBoundingBox().inflate(CombatSkillLogic.effectiveRange(player, range));
   }
}
