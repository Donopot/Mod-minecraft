package net.mcreator.minepiece.procedures;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;

/**
 * Un move (technique) de Fruit du Démon — déclaration déclarative d'un skill.
 * Port des valeurs ver15 (dégâts/effets) extraites des procédures MCreator ; l'exécution réelle est
 * déléguée aux primitives de {@link CombatSkillLogic} (mêlée / projectile / effet).
 *
 * <p>CORE-016 : les unités sont explicites et séparées — {@code range} = portée/rayon en <b>blocs</b>
 * (mêlée/zone), {@code lifetime} = durée de vie en <b>ticks</b> (projectile), {@code speed} = vitesse en
 * <b>blocs/tick</b> (projectile). Plus de confusion « 80 ticks vs 80 blocs ».</p>
 */
public record DevilFruitMove(
      String id,                 // identifiant technique (ex. "hiken")
      String displayName,        // nom affiché (ex. "Fire Fist")
      MoveType type,             // primitive de combat
      double damage,             // dégâts (0 si N/A)
      double range,              // portée cône (mêlée) ou rayon (zone), en BLOCS (0 si projectile)
      double knockback,          // knockback
      Holder<MobEffect> effect,  // effet de statut optionnel (peut être null)
      int effectDuration,        // durée de l'effet en ticks
      int effectAmplifier,       // amplificateur de l'effet
      double energyCost,         // coût en Curse Power
      int cooldownTicks,         // cooldown en ticks
      ParticleOptions particle,  // particule de rendu (peut être null)
      String spawnEntity,        // id d'entité visuelle optionnel (ex. "entity_gomu_arm")
      int lifetime,              // durée de vie en TICKS (projectile ; 0 sinon)
      double speed               // vitesse en BLOCS/TICK (projectile ; 0 sinon)
) {

   /** Vitesse par défaut des projectiles (blocs/tick) quand une technique n'en spécifie pas. */
   public static final double DEFAULT_PROJECTILE_SPEED = 2.5;

   /** Copie avec des dégâts remplacés — utilisé par la couche d'équilibrage (CORE-025). */
   public DevilFruitMove withDamage(double newDamage) {
      return new DevilFruitMove(id, displayName, type, newDamage, range, knockback, effect,
         effectDuration, effectAmplifier, energyCost, cooldownTicks, particle, spawnEntity, lifetime, speed);
   }

   /**
    * Nom de la technique tel qu'affiché au joueur (CORE-028).
    *
    * <p>Passe par une clé de traduction {@code minepiece.technique.<id>} pour être localisé
    * (fr_fr / en_us / ja_jp) ; {@link #displayName()} reste le nom de repli quand la clé manque.</p>
    */
   public Component name() {
      if(id.equals("passive") && displayName.equals("Aro Passive"))return Component.translatableWithFallback("minepiece.technique.aro_passive",displayName);
      if(id.equals("passive") && displayName.equals("Nikyu Passive"))return Component.translatableWithFallback("minepiece.technique.nikyu_passive",displayName);
      if (id.equals("passive") && displayName.equals("Pika Pika Passive"))
         return Component.translatableWithFallback("minepiece.technique.pika_passive", displayName);
      return Component.translatableWithFallback("minepiece.technique." + id, displayName);
   }

   public static DevilFruitMove melee(String id, String name, double damage, double range, double knockback,
                                      double energy, int cooldown, ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.MELEE, damage, range, knockback, null, 0, 0,
         energy, cooldown, particle, null, 0, 0.0);
   }

   public static DevilFruitMove melee(String id, String name, double damage, double range, double knockback,
                                      Holder<MobEffect> effect, int dur, int amp, double energy, int cooldown,
                                      ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.MELEE, damage, range, knockback, effect, dur, amp,
         energy, cooldown, particle, null, 0, 0.0);
   }

   public static DevilFruitMove projectile(String id, String name, double damage, double lifetime,
                                           double knockback, double energy, int cooldown, ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.PROJECTILE, damage, 0.0, knockback, null, 0, 0,
         energy, cooldown, particle, null, (int) lifetime, DEFAULT_PROJECTILE_SPEED);
   }

   public static DevilFruitMove projectile(String id, String name, double damage, double lifetime, double knockback,
                                           Holder<MobEffect> effect, int dur, int amp, double energy, int cooldown,
                                           ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.PROJECTILE, damage, 0.0, knockback, effect, dur, amp,
         energy, cooldown, particle, null, (int) lifetime, DEFAULT_PROJECTILE_SPEED);
   }

   public static DevilFruitMove projectile(String id, String name, double damage, double lifetime, double knockback,
                                           double energy, int cooldown, ParticleOptions particle, double speed) {
      return new DevilFruitMove(id, name, MoveType.PROJECTILE, damage, 0.0, knockback, null, 0, 0,
         energy, cooldown, particle, null, (int) lifetime, speed);
   }

   public static DevilFruitMove projectile(String id, String name, double damage, double lifetime, double knockback,
                                           Holder<MobEffect> effect, int dur, int amp, double energy, int cooldown,
                                           ParticleOptions particle, double speed) {
      return new DevilFruitMove(id, name, MoveType.PROJECTILE, damage, 0.0, knockback, effect, dur, amp,
         energy, cooldown, particle, null, (int) lifetime, speed);
   }

   public static DevilFruitMove areaEffect(String id, String name, Holder<MobEffect> effect, int dur, int amp,
                                           double radius, double energy, int cooldown, ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.EFFECT, 0, radius, 0, effect, dur, amp,
         energy, cooldown, particle, null, 0, 0.0);
   }

   public static DevilFruitMove buff(String id, String name, Holder<MobEffect> effect, int dur, int amp,
                                     double energy, int cooldown, ParticleOptions particle) {
      return new DevilFruitMove(id, name, MoveType.BUFF, 0, 0, 0, effect, dur, amp,
         energy, cooldown, particle, null, 0, 0.0);
   }

   public boolean hasEffect() {
      return effect != null;
   }
}
