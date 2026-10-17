package net.mcreator.minepiece.procedures;

import java.util.List;
import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher des skills de Fruit du Démon. Routage fruit → moves (via {@link DevilFruitRegistry})
 * → primitives de combat ({@link CombatSkillLogic}). Gère l'énergie, le cooldown et la sélection
 * de technique (cyclage via {@code PlayerTechniqueUsedNumber}).
 *
 * <p>Ordre de validation (CORE-005/CORE-007/CORE-027) : fruit possédé → techniques portées →
 * état du joueur (vivant) → suppression par la mer → recharge → énergie ; le coût n'est déduit
 * qu'une seule fois, après validation complète et juste avant l'exécution.</p>
 */
public final class DevilFruitSkillDispatcher {

   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");

   /** Nombre maximal d'invocations actives par joueur (CORE-021). */
   private static final int MAX_SUMMONS = 4;

   private DevilFruitSkillDispatcher() {
   }

   /** Déclenche la technique courante du fruit possédé. Retourne vrai si une technique a été exécutée. */
   public static boolean castSkill(ServerPlayer player) {
      // CORE-027 : ignorer les états non jouables (mort, spectateur).
      if (!player.isAlive() || player.isSpectator() || ZoanCounterState.active(player) || ParameciaControl.active(player,ParameciaControl.Kind.STONE)) {
         return false;
      }

      PlayerVariables vars = MinepieceModVariables.get(player);
      ItemStack fruit = vars.PlayerDevilFruit;
      LOGGER.debug("castSkill reçu — {} (fruit={}, énergie={}, use={})", player.getName().getString(),
         fruit.isEmpty() ? "aucun" : fruit.getItem(), (int) vars.PlayerEnergy, vars.UseDevilAbilities);
      if (fruit.isEmpty()) {
         // ROKUSHIKI — l'art martial n'appartient à aucun Fruit : un porteur qui l'a appris s'en sert
         // par le MÊME chemin (même touche, même validation d'Énergie et de recharge).
         if (Rokushiki.isLearned(player)) {
            return Rokushiki.cast(player);
         }
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.no_fruit")), false);
         return false;
      }

      // ROKUSHIKI : l'art martial s'emploie SANS Fruit du Démon — même touche, même coût, même
      // recharge, seul le répertoire diffère. Un porteur sans Fruit qui a appris l'art n'est donc
      // pas « sans pouvoir » : c'est le point que Donopot a rappelé (le Rokushiki n'appartient à
      // aucun Fruit).
      List<DevilFruitMove> moves = DevilFruitRegistry.movesFor(fruit.getItem());
      if (moves.isEmpty()) {
         LOGGER.warn("{} : fruit {} sans technique portée.", player.getName().getString(), fruit.getItem());
         player.sendSystemMessage(
            Component.literal("§e").append(Component.translatable("minepiece.message.not_ported")), false);
         return false;
      }

      int index = Math.floorMod((int) vars.PlayerTechniqueUsedNumber, moves.size());
      DevilFruitMove move = moves.get(index);

      // CORE-007 : suppression par l'eau (mer) → aucun skill.
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         player.sendSystemMessage(Component.literal("§c").append(Component.translatable(
            "yami".equals(DevilFruitPowerLogic.suppressionCause(player))
               ? "minepiece.message.yami_sealed" : "minepiece.message.sea_suppressed")), false);
         return false;
      }

      // CORE-005 : valider la recharge AVANT tout coût.
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE) ? ControlledBatchTechniques.onCooldown(player,move.id()) : player.getCooldowns().isOnCooldown(fruit)) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.cooldown")), false);
         return false;
      }

      // CORE-005 : valider l'énergie AVANT de la consommer.
      if (vars.PlayerEnergy < move.energyCost()) {
         player.sendSystemMessage(
            Component.literal("§c").append(Component.translatable("minepiece.message.no_energy",
               (int) move.energyCost(), (int) vars.PlayerEnergy)), false);
         return false;
      }

      // Engagement réussi : coût déduit une seule fois, puis exécution.
      vars.PlayerEnergy -= move.energyCost();

      boolean executed;
      try {
         executed = executeMove(player, fruit, move);
      } catch (RuntimeException failure) {
         // A failed execution must settle its debit even if a handler throws.
         vars.PlayerEnergy += move.energyCost();
         vars.syncPlayerVariables(player);
         LOGGER.error("Technique {}/{} interrompue : coût rendu.", fruit.getItem(), move.id(), failure);
         player.sendSystemMessage(Component.literal("§cTechnique interrompue ; énergie rendue."), false);
         return false;
      }
      if (executed) {
         // Témoin : ce que le serveur a RÉELLEMENT demandé, nommément. Sans lui, « on ne voit rien »
         // reste indécidable — reçu ? refusé ? exécuté ? avec quel geste ? quel visuel ?
         String geste = TechniqueAnimations.nameFor(move, player);
         boolean livre = TechniqueAnimations.fichierPresent(geste);
         DevilFruitDebug.dire(player, "technique « " + move.name().getString() + " » (id " + move.id()
            + ", type " + move.type() + ") exécutée · geste demandé : " + geste
            + (livre ? " (fichier livré)" : " (FICHIER ABSENT : rien ne jouera)")
            + " · énergie restante " + (int) vars.PlayerEnergy);
      }
      if (executed) {
         // Le geste suit la technique : la source l'annonce après coup, par un paquet dédié.
         TechniqueAnimations.play(player, move);
      }
      if (!executed) {
         // Le plan exige « un refus ne consomme rien » : la technique a été refusée après validation
         // (ex. limite d'invocations atteinte) → l'Énergie est rendue, aucune recharge n'est posée.
         vars.PlayerEnergy += move.energyCost();
      }
      vars.syncPlayerVariables(player);
      return executed;
   }

   /** Cycle vers la technique suivante du fruit possédé (pour la sélection via keybind/commande). */
   public static void cycleTechnique(ServerPlayer player) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      ItemStack fruit = vars.PlayerDevilFruit;
      if (fruit.isEmpty()) {
         // Sans Fruit : la touche de cycle parcourt le répertoire du ROKUSHIKI, si l'art est appris.
         if (Rokushiki.isLearned(player)) {
            Rokushiki.cycle(player);
         }
         return;
      }
      List<DevilFruitMove> moves = DevilFruitRegistry.movesFor(fruit.getItem());
      if (moves.size() <= 1) {
         return;
      }
      vars.PlayerTechniqueUsedNumber = (vars.PlayerTechniqueUsedNumber + 1) % moves.size();
      DevilFruitMove next = moves.get((int) vars.PlayerTechniqueUsedNumber);
      player.sendSystemMessage(Component.literal("§a").append(
         Component.translatable("minepiece.message.technique_selected", next.name())), false);
      vars.syncPlayerVariables(player);
   }

   /** Exécute un move déjà validé (primitif + entité visuelle + cooldown + message). */
   /** Exécution d'une technique — accessible au paquet (Rokushiki emploie les mêmes chemins). */
   static boolean executeMove(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      try (CastContext context = CastContext.enter(fruit, move)) {
         return executeScopedMove(player, fruit, move);
      }
   }

   private static boolean executeScopedMove(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      // Un refus (ex. limite d'invocations) retourne faux : ni recharge, ni message de lancement,
      // et castSkill rend l'Énergie — c'est la condition « un refus ne consomme rien ».
      boolean refused = false;
      // FRUIT-*-TECH-*-DEV : les comportements propres à un Fruit ont la priorité sur le moteur générique.
      CustomCastResult result = DevilFruitBehaviours.dispatch(player, fruit, move);
      if (result == CustomCastResult.REFUSED) {
         player.sendSystemMessage(Component.literal("§eTechnique refusée : conditions non remplies."), true);
         return false;
      }
      boolean handled = result == CustomCastResult.EXECUTED;
      if (!handled) {
      switch (move.type()) {
         case MELEE -> {
            CombatSkillLogic.meleeStrike(player, move.damage(), move.range(), move.knockback(),
               move.effect(), move.effectDuration(), move.effectAmplifier());
            refused = !spawnMeleeVisual(player, move);
         }
         case PROJECTILE -> {
            // Les techniques déclarées comme invocations le sont par les données (CORE-021) :
            // Biscuit Warrior I et II passent par ici (FRUIT-BISU_BISU-03).
            if (DevilFruitRegistry.isSummon(move.id())) {
               String[] summonVis = DevilFruitRegistry.projectileVisual(move.id());
               if (summonVis == null) {
                  summonVis = DevilFruitRegistry.meleeVisual(move.id());
               }
               if (summonVis == null) {
                  LOGGER.warn("Invocation {} sans visuel déclaré — aucune entité créée.", move.id());
                  refused = true;
               } else {
                  refused = !spawnSummon(player, move.damage(), move.id(), summonVis[0], summonVis[1]);
               }
               break;
            }
            String[] vis = DevilFruitRegistry.projectileVisual(move.id());
            if (vis == null) {
               // CORE-017 : repli intentionnel par élément, plutôt qu'un « météore par défaut » hors-sujet.
               vis = DevilFruitElement.projectileVisualFallback(DevilFruitElement.of(fruit));
            }
            CombatSkillLogic.fireProjectile(player, move.damage(), move.knockback(),
               move.effect(), move.effectDuration(), move.effectAmplifier(), move.lifetime(), move.speed(),
               vis[0], vis[1], move.particle());
         }
         case EFFECT -> CombatSkillLogic.areaEffect(player, move.effect(), move.effectDuration(),
            move.effectAmplifier(), move.range());
         case BUFF -> {
            if (move.effect() == null) {
               // Un effet non enregistré au moment de la construction du catalogue laisserait un
               // Holder nul : on dégrade proprement au lieu de lever une exception qui annule le cast.
               LOGGER.warn("Technique {} déclarée BUFF sans effet enregistré — appliquée sans effet.", move.id());
            } else {
               player.addEffect(new MobEffectInstance(move.effect(), move.effectDuration(), move.effectAmplifier()));
            }
         }
      }

      if ("entity_gomu_arm".equals(move.spawnEntity())) {
         spawnGomuArm(player);
      }

      CombatSkillLogic.spawnBurst(player, move.particle(), 30);
      }

      if (refused) {
         return false;
      }
      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE))ControlledBatchTechniques.cooldown(player,move);else player.getCooldowns().addCooldown(fruit, move.cooldownTicks());
      player.sendSystemMessage(Component.literal("§b").append(
         Component.translatable("minepiece.message.technique_cast", move.name())), false);
      return true;
   }

   /** Bras étiré de Gomu Gomu (entité GeckoLib, suit le propriétaire, joue l'anim du coup). */
   private static void spawnGomuArm(ServerPlayer player) {
      ServerLevel level = (ServerLevel) player.level();
      EntityGomuArmEntity arm = MinepieceModEntities.ENTITY_GOMU_ARM.spawn(level, player.blockPosition(), EntitySpawnReason.MOB_SUMMONED);
      if (arm != null) {
         arm.setOwnerEntity(player);
         arm.setPos(player.getX(), player.getEyeY(), player.getZ());
         arm.setYRot(player.getYRot());
         arm.setXRot(player.getXRot());
         arm.setAnimation("punch_gomu_pistol");
         arm.setTexture("luffy");
      }
   }

   /** Entité visuelle mêlée OU invocation fonctionnelle (CORE-021) : suit/attaque pour les summons. */
   private static boolean spawnMeleeVisual(ServerPlayer player, DevilFruitMove move) {
      String[] vis = DevilFruitRegistry.meleeVisual(move.id());
      if (vis == null) return true;
      if (DevilFruitRegistry.isSummon(move.id())) {
         return spawnSummon(player, move.damage(), move.id(), vis[0], vis[1]);
      }
      ServerLevel level = (ServerLevel) player.level();
      EntityGomuArmEntity e = MinepieceModEntities.ENTITY_GOMU_ARM.spawn(level, player.blockPosition(), EntitySpawnReason.MOB_SUMMONED);
      if (e != null) {
         e.setOwnerEntity(player);
         e.setModel(vis[0]);
         e.setTexture(vis[1]);
         e.setVisualProfile(move.id());
         // Devant le joueur, à hauteur d'œil, pour être visible en FPS (pas collé aux pieds).
         Vec3 look = player.getLookAngle();
         e.setPos(player.getX() + look.x * 1.5, player.getEyeY(), player.getZ() + look.z * 1.5);
         e.setYRot(player.getYRot());
         e.setXRot(player.getXRot());
         e.followOwner(false);
         e.lifetime(20);
      }
      return true;
   }

   /** Invocation agissante (CORE-021) : IA, propriétaire, alliance, dégâts, limite et expiration. */
   private static boolean spawnSummon(ServerPlayer player, double damage, String moveId, String model, String texture) {
      ServerLevel level = (ServerLevel) player.level();
      int active = 0;
      for (SummonEntity s : level.getEntitiesOfClass(SummonEntity.class, player.getBoundingBox().inflate(128))) {
         if (s.getOwner() != null && player.getUUID().equals(s.getOwner().getUUID())) {
            active++;
         }
      }
      if (active >= MAX_SUMMONS) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.summon_limit", MAX_SUMMONS)), false);
         return false;
      }
      SummonEntity e = MinepieceModEntities.SUMMON.spawn(level, player.blockPosition(), EntitySpawnReason.MOB_SUMMONED);
      if (e != null) {
         e.setOwnerEntity(player);
         e.setDamage(damage);
         e.setModel(model);
         e.setTexture(texture);
         Vec3 look = player.getLookAngle();
         e.setPos(player.getX() + look.x * 1.5, player.getY(), player.getZ() + look.z * 1.5);
         e.setYRot(player.getYRot());
         e.lifetime(DevilFruitRegistry.summonLifetime(moveId));
         return true;
      }
      return false;
   }
}
