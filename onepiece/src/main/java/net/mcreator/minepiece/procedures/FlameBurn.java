package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * <b>Brûlure de feu, bornée et sensible aux résistances</b> — Mera Mera (FRUIT-MERA_MERA-03).
 *
 * <p>Le plan : « implémenter une <b>brûlure bornée</b> et des projectiles de feu avec <b>type de
 * dégâts approprié</b> », critère « une cible touchée <b>reçoit la brûlure prévue</b> et les
 * <b>cibles résistantes sont traitées correctement</b> ».</p>
 *
 * <p>Les deux moitiés du critère imposent chacune une règle :</p>
 * <ul>
 *   <li><b>bornée</b> — une application dure {@code burn_ticks}, mais le total écoulé depuis la
 *       <b>première</b> application ne dépasse jamais {@code burn_cap_ticks}, même martelé. Sans ce
 *       plafond, un joueur qui frappe assez vite entretiendrait un incendie perpétuel : la brûlure
 *       ne serait plus une brûlure mais un état permanent.</li>
 *   <li><b>cibles résistantes</b> — une créature <b>immunisée au feu</b> ({@code fireImmune()}, un
 *       cube de magma, un blaze) ne prend <b>aucune</b> brûlure. C'est le point que l'implémentation
 *       générique ratait : elle posait des ticks d'incendie à tout le monde.</li>
 * </ul>
 *
 * <p><b>Différence avec le magma, voulue et testée</b> : ici l'<b>eau éteint</b> la brûlure. Le magma
 * se rallume, le feu s'éteint — deux pouvoirs distincts, pas deux noms pour la même chose.</p>
 */
public final class FlameBurn {

   /** Incendie visible posé par une application. */
   public static final int FIRE_TICKS = 60;

   /** Période des dégâts, en ticks. */
   public static final int DAMAGE_PERIOD = 20;

   private static final class Burn {
      int remaining;
      int served;          // ticks déjà servis depuis la PREMIÈRE application
      int damageCountdown;
      int quiet;
      LivingEntity owner;
      boolean ownsFire;

      Burn(int remaining) {
         this.remaining = remaining;
         this.damageCountdown = DAMAGE_PERIOD;
      }
   }

   private static final Map<UUID, Burn> BURNS = new HashMap<>();

   private FlameBurn() {
   }

   /** Branche l'entretien de la brûlure sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
   }

   /**
    * Allume une brûlure de feu sur une cible.
    *
    * @return durée restante en ticks, ou {@code 0} si la cible est immunisée au feu
    */
   public static int ignite(LivingEntity target, int ticks) {
      return ignite(target,ticks,null);
   }

   public static int ignite(LivingEntity target, int ticks, LivingEntity owner) {
      if (target == null) return 0;
      // Cible résistante : traitée correctement, c'est-à-dire PAS brûlée.
      if (target.fireImmune() || target.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE)) {
         return 0;
      }
      Burn burn = BURNS.get(target.getUUID());
      if (burn == null) {
         burn = new Burn(0);
         burn.ownsFire = target.getRemainingFireTicks() <= 2;
         BURNS.put(target.getUUID(), burn);
      }
      burn.quiet = 0;
      burn.owner = owner;
      int cap = DevilFruitFlameConfig.burnCapTicks();
      int budget = cap - burn.served;
      if (budget <= 0) {
         return 0; // plafond dur atteint : marteler ne rallume plus
      }
      int wanted = ticks > 0 ? ticks : DevilFruitFlameConfig.burnTicks();
      burn.remaining = Math.min(Math.max(burn.remaining, wanted), budget);
      // Two ticks keep the vanilla visual flag alive without vanilla firing its 20-tick damage.
      if (burn.ownsFire) target.setRemainingFireTicks(2);
      return burn.remaining;
   }

   /** Brûlure restante, en ticks (0 = aucune). */
   public static int remaining(LivingEntity target) {
      Burn burn = target == null ? null : BURNS.get(target.getUUID());
      return burn == null ? 0 : burn.remaining;
   }

   /** Vrai si la cible brûle du feu du pouvoir. */
   public static boolean isBurning(LivingEntity target) {
      return remaining(target) > 0;
   }

   /** Éteint la brûlure (eau, glace, retrait du Fruit). */
   public static void extinguish(LivingEntity target) {
      if (target == null) {
         return;
      }
      BURNS.remove(target.getUUID());
      target.setRemainingFireTicks(0);
   }

   /** Nombre de brûlures suivies (observable). */
   public static int burningCount() {
      return (int)BURNS.values().stream().filter(burn -> burn.remaining > 0).count();
   }

   /** Arrête toutes les brûlures (reset de fruit, fin de test). */
   public static void clearAll() {
      for (UUID id : BURNS.keySet()) {
         LivingEntity target=find(id);
         if (target!=null && BURNS.get(id).ownsFire) target.setRemainingFireTicks(0);
      }
      BURNS.clear();
   }

   /** Entretien : eau, dégâts, échéance. Exposé pour que les tests déroulent le temps. */
   public static void tick() {
      if (BURNS.isEmpty()) {
         return;
      }
      for (Map.Entry<UUID, Burn> entry : new ArrayList<>(BURNS.entrySet())) {
         Burn burn = entry.getValue();
         LivingEntity target = find(entry.getKey());
         if (target == null || !target.isAlive()) {
            BURNS.remove(entry.getKey());
            continue;
         }
         // L'eau éteint le FEU. Le magma, lui, se rallume : c'est toute la différence.
         if (DevilFruitFlameConfig.waterExtinguishes() && ElementalSenses.isWet(target)) {
            BURNS.remove(entry.getKey());
            target.setRemainingFireTicks(0);
            continue;
         }
         if (target.fireImmune() || target.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE)) {
            extinguish(target); continue;
         }
         if (burn.remaining<=0) {
            if (burn.ownsFire) target.setRemainingFireTicks(0);
            if (++burn.quiet > DevilFruitFlameConfig.burnTicks()) BURNS.remove(entry.getKey());
            continue;
         }
         burn.remaining--;
         burn.served++;
         if (burn.ownsFire) target.setRemainingFireTicks(burn.remaining>0 ? 2 : 0);
         burn.damageCountdown--;
         if (burn.damageCountdown <= 0) {
            burn.damageCountdown = DAMAGE_PERIOD;
            double damage = DevilFruitFlameConfig.burnDamagePerSecond();
            if (damage > 0.0 && target.level() instanceof ServerLevel level) {
               // Type de dégâts approprié : « en feu », donc annulé par une créature qui y résiste.
               if (burn.owner == null || CombatSkillLogic.isValidTarget(burn.owner, target))
                  target.hurtServer(level, CombatSkillLogic.elementalDamageSource(level,ElementType.FIRE,burn.owner,null), (float) damage);
            }
         }
      }
   }

   /** Immersion réelle — règle partagée, cf. {@link ElementalSenses#isWet}. */
   public static boolean isWet(LivingEntity target) {
      return ElementalSenses.isWet(target);
   }

   private static LivingEntity find(UUID id) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      for (ServerLevel level : server.getAllLevels()) {
         if (level.getEntity(id) instanceof LivingEntity living) {
            return living;
         }
      }
      return null;
   }
}
