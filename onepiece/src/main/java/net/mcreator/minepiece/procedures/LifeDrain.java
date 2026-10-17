package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.mori.DevilFruitMoriConfig;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Drainage de vie</b> — Mori Mori no Mi (FRUIT-MORI_MORI-05).
 *
 * <p>Le plan : « ajouter un <b>drainage de vie ou énergie</b> avec <b>coût et plafond</b> », critère
 * « <b>aucun gain infini</b> sur les <b>invocations</b>, <b>alliés</b> ou <b>cibles invulnérables</b> ».</p>
 *
 * <h2>Un drainage est une promesse de gain — donc le lieu exact de l'abus</h2>
 * <p>Un pouvoir qui <b>retire</b> quelque chose à autrui peut être équilibré par son coût. Un pouvoir qui
 * <b>rend</b> quelque chose à son porteur ne l'est pas : il faut une <b>borne</b>. Trois exclusions et
 * deux plafonds, chacun répondant à une façon précise d'obtenir un gain infini :</p>
 *
 * <table>
 *   <tr><th>Situation</th><th>Ce qui se passerait sans la règle</th></tr>
 *   <tr><td><b>Invocations du porteur</b></td><td>Un serviteur bon marché devient une <b>batterie</b> :
 *       on le draine, on le ressert, on recommence.</td></tr>
 *   <tr><td><b>Alliés</b></td><td>Deux joueurs complices se drainent mutuellement en boucle — ou, plus
 *       simplement, le porteur draine son équipe sans qu'elle ait rien demandé.</td></tr>
 *   <tr><td><b>Cibles invulnérables</b></td><td>Une cible qui ne subit aucun dégât mais rapporte quand
 *       même transforme l'invulnérabilité en <b>source</b>.</td></tr>
 *   <tr><td><b>Plafond par cible</b></td><td>Un usage viderait une cible entière au lieu d'en prendre une
 *       part.</td></tr>
 *   <tr><td><b>Plafond d'énergie gagnée</b></td><td>Le gain serait proportionnel à la vie de la cible :
 *       une cible à 200 PV remplirait la réserve d'un coup.</td></tr>
 * </table>
 *
 * <p>Les trois exclusions sont vérifiées <b>avant</b> d'infliger quoi que ce soit : une cible exclue n'est
 * pas seulement « non rentable », elle n'est <b>pas touchée</b>. C'est la distinction qui compte : un
 * drainage « à gain nul » laisserait croire à un bug, et pousserait à re-tester.</p>
 */
public final class LifeDrain {

   private LifeDrain() {
   }

   /**
    * Drainage complet : dégâts plafonnés à la cible, énergie gagnée plafonnée pour le porteur.
    *
    * @return énergie effectivement gagnée (0 si la cible est exclue ou n'a rien subi)
    */
   public static double drain(ServerPlayer porteur, LivingEntity cible) {
      if (porteur == null || cible == null || !porteur.isAlive() || !cible.isAlive()) {
         return 0.0;
      }
      if (porteur.level() != cible.level() || !(porteur.level() instanceof ServerLevel level)) {
         return 0.0;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur) || !isDrainable(porteur, cible, level)) {
         return 0.0;
      }
      // PLAFOND par cible : on ne prend qu'une part, jamais la totalité.
      double aPrendre = Math.min(DevilFruitMoriConfig.drainMaxHealth(), cible.getHealth());
      if (aPrendre <= 0.0) {
         return 0.0;
      }
      var source = level.damageSources().indirectMagic(porteur, porteur);
      float avant = cible.getHealth();
      cible.hurtServer(level, source, (float) aPrendre);
      double reel = Math.max(0.0, avant - cible.getHealth());
      if (reel <= 0.0) {
         // La cible n'a rien subi (résistance, immunité découverte au dernier moment) : aucun gain.
         return 0.0;
      }
      // PLAFOND de gain : la conversion est bornée, donc le gain l'est aussi.
      double gain = Math.min(reel * DevilFruitMoriConfig.drainRatio(),
         DevilFruitMoriConfig.drainEnergyCap());
      var vars = MinepieceModVariables.get(porteur);
      double avantEnergie = vars.PlayerEnergy;
      vars.PlayerEnergy = Math.min(200.0, vars.PlayerEnergy + gain);
      double gagne = Math.max(0.0, vars.PlayerEnergy - avantEnergie);
      level.sendParticles(MinepieceModParticles.FLOWER, cible.getX(), cible.getY() + 1.0, cible.getZ(),
         30, 0.5, 0.6, 0.5, 0.02);
      level.playSound(null, porteur.blockPosition(), SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS,
         0.8F, 0.7F);
      return gagne;
   }

   /**
    * <b>Les trois exclusions du critère</b>, plus les cas évidents. Une cible exclue n'est <b>pas
    * touchée du tout</b> : il n'y a pas de « drainage à zéro », qui ferait croire à une panne.
    */
   public static boolean isDrainable(ServerPlayer porteur, LivingEntity cible, ServerLevel level) {
      if (porteur.getUUID().equals(cible.getUUID())) {
         return false;
      }
      if (cible instanceof ServerPlayer p && (p.isSpectator() || p.isCreative())) {
         return false;
      }
      // 1. INVOCATIONS : on ne se sert pas dans ses propres serviteurs.
      if (cible instanceof SummonEntity somme && DevilFruitSummons.isOwnedBy(somme, porteur)) {
         return false;
      }
      // 2. ALLIÉS : le drainage ne se retourne jamais contre son camp.
      if (porteur.isAlliedTo(cible)) {
         return false;
      }
      // 3. CIBLES INVULNÉRABLES : une cible qui ne peut pas être blessée ne peut pas nourrir.
      var source = level.damageSources().indirectMagic(porteur, porteur);
      if (cible.isInvulnerableTo(level, source)) {
         return false;
      }
      // Une invocation qui n'appartient à personne reste drainable — c'est un adversaire.
      return true;
   }

   /** Autres invocations alliées (propriétaire allié du porteur), exclues elles aussi. */
   public static boolean isFriendlySummon(ServerPlayer porteur, LivingEntity cible) {
      if (!(cible instanceof SummonEntity somme)) {
         return false;
      }
      LivingEntity proprietaire = somme.getOwner();
      return proprietaire != null && (proprietaire.getUUID().equals(porteur.getUUID())
         || porteur.isAlliedTo(proprietaire));
   }
}
