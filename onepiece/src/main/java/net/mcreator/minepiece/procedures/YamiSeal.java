package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig;
import java.util.List;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * <b>Scellement temporaire du pouvoir d'autrui</b> — Yami Yami no Mi (FRUIT-YAMI_YAMI-05).
 *
 * <p>Le plan : « Créer une <b>suppression temporaire des fruits</b> et décider explicitement
 * l'exception à l'intangibilité Logia générique », critère « la cible <b>perd temporairement</b> les
 * capacités prévues puis <b>les retrouve</b> ; le passif de Yami suit la règle retenue ».</p>
 *
 * <h2>Un seul état, et toutes les portes le consultent déjà</h2>
 * <p>Ce scellement n'ajoute <b>aucune</b> mécanique de verrouillage : il pose un marqueur d'effet que
 * {@code DevilFruitPowerLogic.isSuppressed} reconnaît, et <b>tout ce qui existait consulte déjà ce
 * prédicat</b> — le lancement de techniques ({@code DevilFruitSkillDispatcher}), l'intangibilité Logia,
 * l'attraction et l'absorption des ténèbres, les aires maintenues (ROOM d'Ope Ope, maintien de Mochi,
 * racines de Mori), la charge lumineuse de Pika Pika, la forme hybride. C'est le bénéfice d'avoir gardé
 * <b>une</b> définition de « pouvoir scellé » depuis Moku Moku : la nouvelle mécanique n'a rien à
 * propager.</p>
 *
 * <h2>« Puis les retrouve » est un fait, pas une intention</h2>
 * <p>La restitution ne demande aucun code : l'effet <b>expire par le temps du moteur</b>, et le même
 * prédicat redevient faux. Le test le <b>constate</b> en relisant la cible à un autre tick (une
 * invocation de test tient dans un seul tick, donc l'échéance ne peut pas être observée dans la même
 * invocation).</p>
 *
 * <h2>L'exception de ce Logia est décidée, et elle est l'inverse des autres</h2>
 * <p>Le passif de Yami n'est pas une protection : ce fruit <b>ne rend pas intangible</b>. Être scellé
 * ne le lui rend donc pas — un pouvoir qu'on n'avait pas ne peut pas être perdu. Le scellement retire
 * les <b>capacités actives</b>, il n'ajoute aucune défense. Voir
 * {@code DevilFruitPowerLogic.computeDamageMagnification} pour la règle complète, y compris la
 * contrepartie (subir davantage), qui est une <b>pénalité</b> et suit donc son propre sort.</p>
 */
public final class YamiSeal {

   private static int scellements;
   private static int ciblesDistinctes;

   private YamiSeal() {
   }

   /**
    * Scelle le pouvoir d'une cible pour la durée de configuration (0 = durée de configuration).
    *
    * @return vrai si le scellement a été posé
    */
   public static boolean apply(LivingEntity cible, int ticksDemandes) {
      if (cible == null || cible.level().isClientSide()) {
         return false;
      }
      int ticks = ticksDemandes <= 0 ? DevilFruitYamiConfig.sealTicks()
         : Math.min(DevilFruitYamiConfig.HARD_MAX_SEAL_TICKS, ticksDemandes);
      boolean dejaScellee = cible.hasEffect(MinepieceModMobEffects.YAMI_SEALED);
      boolean posee = cible.addEffect(new MobEffectInstance(MinepieceModMobEffects.YAMI_SEALED, ticks, 0,
         false, true, true));
      if (!dejaScellee && posee) {
         ciblesDistinctes++;
      }
      scellements++;
      if (cible.level() instanceof ServerLevel level) {
         level.sendParticles(net.mcreator.minepiece.init.MinepieceModParticles.DARK,
            cible.getX(), cible.getY() + 1.0, cible.getZ(), 12, 0.4, 0.6, 0.4, 0.02);
         level.playSound(null, cible.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
            SoundSource.PLAYERS, 0.7F, 0.6F);
      }
      return posee;
   }

   /** Le pouvoir de cette cible est-il scellé ? */
   public static boolean isSealed(LivingEntity cible) {
      return cible != null && cible.hasEffect(MinepieceModMobEffects.YAMI_SEALED);
   }

   /** Ticks de scellement restants (0 si aucun). */
   public static int sealTicksLeft(LivingEntity cible) {
      if (cible == null) {
         return 0;
      }
      MobEffectInstance effet = cible.getEffect(MinepieceModMobEffects.YAMI_SEALED);
      return effet == null ? 0 : effet.getDuration();
   }

   /**
    * Frappe de ténèbres : la <b>sélection</b> des cibles est celle du moteur
    * ({@link CombatSkillLogic#strikeTargets}), et la <b>conséquence</b> propre à ce fruit est le
    * scellement de chaque cible réellement touchée.
    *
    * @return nombre de cibles touchées ET scellées
    */
   public static int strikeAndSeal(ServerPlayer porteur, ItemStack fruit, DevilFruitMove move) {
      List<LivingEntity> touchees = CombatSkillLogic.strikeTargets(porteur, fruit, move, 0.0);
      int scellees = 0;
      for (LivingEntity cible : touchees) {
         if (apply(cible, 0)) {
            scellees++;
         }
      }
      if (porteur.level() instanceof ServerLevel level) {
         level.playSound(null, porteur.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
            0.8F, 0.6F);
      }
      return scellees;
   }

   /** Rapport de diagnostic. */
   public static String sealReport() {
      return "scellements=" + scellements + " cibles_distinctes=" + ciblesDistinctes;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      scellements = 0;
      ciblesDistinctes = 0;
   }

   /** Nombre de scellements posés. */
   public static int sealCount() {
      return scellements;
   }
}
