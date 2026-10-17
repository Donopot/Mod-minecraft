package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.wolf.DevilFruitWolfConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Poursuite avec endurance</b> — Inu Inu Wolf (FRUIT-INU_INU_WOLF-04).
 *
 * <p>Le plan : « ajouter bonds, morsures et <b>poursuite avec endurance</b> », critère « les trois
 * fonctions respectent <b>collisions, coûts et permissions</b> ».</p>
 *
 * <h2>Pourquoi l'endurance est la pièce qui compte</h2>
 * <p>Une poursuite <b>sans endurance</b> n'est pas une capacité, c'est une <b>condamnation</b> : le
 * poursuivant ne s'arrête jamais, la cible ne peut pas se sauver, et le seul recours est de quitter le
 * serveur. L'exact opposé du soin mis partout ailleurs à garantir une sortie.</p>
 *
 * <p>L'endurance est donc un <b>budget</b> qui se vide — et surtout qui <b>ne se recharge pas en
 * courant</b> : sinon il suffirait de poursuivre sans relâche pour poursuivre indéfiniment. Se
 * <b>reposer</b> est la seule façon de la retrouver. C'est ce qui transforme une course sans fin en
 * <b>décision</b> : « puis-je me permettre ces trois secondes ? »</p>
 *
 * <h2>Les trois garde-fous du critère, vérifiés séparément</h2>
 * <table border="1">
 *   <caption>Collisions, coûts, permissions</caption>
 *   <tr><th>Garde-fou</th><th>Ici</th></tr>
 *   <tr><td><b>Collisions</b></td><td>l'accrochage exige une <b>ligne dégagée</b> jusqu'à la cible ; la
 *       course s'arrête si un mur se dresse devant</td></tr>
 *   <tr><td><b>Coûts</b></td><td>{@code pursuit_cost_per_second} É/s, prélevés sur la réserve commune</td></tr>
 *   <tr><td><b>Permissions</b></td><td>pouvoir scellé (mer), joueur mort : la poursuite est refusée et
 *       <b>rend</b> ce qu'elle n'a pas utilisé</td></tr>
 * </table>
 */
public final class WolfPursuit {

   private static final class Chase {
      final ServerPlayer owner;
      final ServerLevel world;
      UUID target;
      int endurance;
      int energyCarry;

      Chase(ServerPlayer owner, UUID target, int endurance) {
         this.owner=owner;this.world=(ServerLevel)owner.level();
         this.target = target;
         this.endurance = endurance;
      }
   }

   private static final Map<UUID, Chase> ACTIVE = new HashMap<>();

   /** Raison de la dernière fin, par joueur (lisible par les preuves). */
   private static final Map<UUID, String> LAST_END = new HashMap<>();

   private WolfPursuit() {
   }

   /** Branche l'entretien et la sortie sur déconnexion. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Chase chase : new ArrayList<>(ACTIVE.values())) if(chase.world.getServer()==server) tick(chase.owner);
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> endAll("arret"));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            end(player, "deconnexion");
         }
      });
   }

   /** Le joueur poursuit-il ? */
   public static boolean isPursuing(ServerPlayer player) {
      return player != null && ACTIVE.containsKey(player.getUUID()) && ACTIVE.get(player.getUUID()).owner==player;
   }

   /** Endurance restante, en ticks. */
   public static int enduranceLeft(ServerPlayer player) {
      Chase chase = player == null ? null : ACTIVE.get(player.getUUID());
      return chase == null ? 0 : chase.endurance;
   }

   /** Raison de la dernière fin. */
   public static String lastEndReason(ServerPlayer player) {
      return player == null ? "aucune" : LAST_END.getOrDefault(player.getUUID(), "aucune");
   }

   /** Poursuites actives (observable). */
   public static int activeCount() {
      return ACTIVE.size();
   }

   /**
    * Accroche une cible : la poursuite démarre si les <b>trois</b> garde-fous sont satisfaits.
    *
    * @return vrai si la poursuite a démarré
    */
   public static boolean begin(ServerPlayer player, net.minecraft.world.entity.LivingEntity target) {
      if (player == null || target == null || isPursuing(player) || !BatchRules.usable(player,(ServerLevel)player.level(),net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF) || !BatchRules.target(player,target)) {
         return false;
      }
      // PERMISSIONS
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return false;
      }
      // COÛT : il faut de quoi tenir au moins une seconde.
      var vars = net.mcreator.minepiece.network.MinepieceModVariables.get(player);
      if (vars.PlayerEnergy < DevilFruitWolfConfig.pursuitCostPerSecond()) {
         return false;
      }
      // COLLISIONS : pas d'accrochage à travers un mur. Une ligne dégagée est exigée jusqu'à la cible.
      if (!hasClearLine(player, target)) {
         return false;
      }
      if (player.distanceTo(target) > DevilFruitWolfConfig.pursuitMaxRange()) {
         return false;
      }
      ACTIVE.put(player.getUUID(), new Chase(player,target.getUUID(), DevilFruitWolfConfig.pursuitEnduranceTicks()));
      if (player.level() instanceof ServerLevel level) {
         // 26.3 n'expose plus WOLF_GROWL adulte en constante simple (les vocalises adultes passent par
         // les variantes WOLF_SOUNDS) : WOLF_SHAKE existe et est un son de loup, plutôt qu'un
         // grognement de bébé pour un prédateur adulte.
         level.playSound(null, player.blockPosition(), SoundEvents.WOLF_SHAKE,
            SoundSource.PLAYERS, 0.9F, 1.0F);
      }
      return true;
   }

   /** Résout une cible par identifiant, dans toutes les dimensions chargées. */
   private static net.minecraft.world.entity.LivingEntity find(UUID id) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return null;
      }
      for (ServerLevel level : server.getAllLevels()) {
         if (level.getEntity(id) instanceof net.minecraft.world.entity.LivingEntity living) {
            return living;
         }
      }
      return null;
   }

   /**
    * Ligne de vue dégagée jusqu'à la cible — exposé pour que les preuves puissent vérifier que le mur
    * est bien <b>détecté</b> et pas seulement que l'accrochage échoue (sans quoi « refuse tout »
    * ressemblerait à un succès).
    */
   public static boolean clearedLine(ServerPlayer player, net.minecraft.world.entity.LivingEntity target) {
      return hasClearLine(player, target);
   }

   /** Ligne de vue dégagée jusqu'à la cible (respect des collisions). */
   private static boolean hasClearLine(ServerPlayer player, net.minecraft.world.entity.LivingEntity target) {
      if (!(player.level() instanceof ServerLevel level)) {
         return false;
      }
      Vec3 from = player.getEyePosition();
      Vec3 to = target.getBoundingBox().getCenter();
      Vec3 delta = to.subtract(from);
      double distance = delta.length();
      if (distance < 0.1) {
         return true;
      }
      Vec3 step = delta.normalize().scale(0.5);
      for (double d = 0.5; d < distance; d += 0.5) {
         Vec3 point = from.add(step.scale(d / 0.5));
         BlockPos pos = BlockPos.containing(point.x, point.y, point.z);
         if (!level.isLoaded(pos)) {
            return false;
         }
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced()) {
            return false;
         }
      }
      return true;
   }

   /** Termine la poursuite (nominalement ou par interruption). */
   public static void end(ServerPlayer player, String reason) {
      if (player == null || !isPursuing(player)) {
         return;
      }
      ACTIVE.remove(player.getUUID());
      LAST_END.put(player.getUUID(), reason);
      // Libération : on retire CE que la poursuite a posé.
      // Pursuit moves explicitly and never owns the player's SPEED potion.
      if (player.isAlive()) {
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§7Poursuite terminée (" + reason + ")."), true);
      }
   }

   /** Termine toutes les poursuites (retrait du Fruit, reset, fin de test). */
   public static void endAll(String reason) {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         ACTIVE.clear();
         return;
      }
      for (Chase chase : new ArrayList<>(ACTIVE.values())) end(chase.owner, reason);
      ACTIVE.clear();
   }

   /** Entretien : endurance, énergie, cible perdue, obstacle, permissions. Exposé pour les tests. */
   public static void tick(ServerPlayer player) {
      Chase chase = ACTIVE.get(player.getUUID());
      if (chase == null || chase.owner!=player) {
         return;
      }
      if (!player.isAlive()) {
         end(player, "mort");
         return;
      }
      if(!BatchRules.usable(player,chase.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF)){end(player,"fruit-ou-monde");return;}
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         end(player, "suppression");
         return;
      }
      net.minecraft.world.entity.LivingEntity target = chase.world.getEntity(chase.target) instanceof net.minecraft.world.entity.LivingEntity living?living:null;
      if (target == null || !BatchRules.target(player,target)) {
         end(player, "cible-perdue");
         return;
      }
      // Endurance : le budget se vide, et ne se recharge pas en courant.
      chase.endurance--;
      if (chase.endurance <= 0) {
         end(player, "endurance");
         return;
      }
      if (player.distanceTo(target) > DevilFruitWolfConfig.pursuitMaxRange()) {
         end(player, "trop-loin");
         return;
      }
      if (!(player.level() instanceof ServerLevel level)) {
         return;
      }
      // Collisions : un mur devant met fin à la course. On ne traverse pas la pierre en courant.
      if (!hasClearLine(player, target)) {
         end(player, "obstacle");
         return;
      }
      // Coût : l'énergie se paie chaque seconde.
      var vars = net.mcreator.minepiece.network.MinepieceModVariables.get(player);
      chase.energyCarry++;
      if (chase.energyCarry >= 20) {
         chase.energyCarry = 0;
         if (vars.PlayerEnergy < DevilFruitWolfConfig.pursuitCostPerSecond()) {
            vars.PlayerEnergy = 0.0;
            vars.syncPlayerVariables(player);
            end(player, "energie");
            return;
         }
         vars.PlayerEnergy -= DevilFruitWolfConfig.pursuitCostPerSecond();
         vars.syncPlayerVariables(player);
      }
      // Course : on pousse vers la cible. L'endurance est le seul frein — c'est elle qui fait la décision.
      double distance = player.distanceTo(target);
      if (distance <= DevilFruitWolfConfig.pursuitStopDistance()) {
         end(player, "rattrapee");
         return;
      }
      // Déplacement d'un PAS BORNÉ vers la cible. Un joueur est autoritaire côté client : on ne le
      // pousse donc pas par `setDeltaMovement` (sans effet serveur), on le déplace explicitement — et
      // seulement si le pas est libre ET dans un chunk chargé (le mécanisme employé par Aro Aro).
      Vec3 step = target.position().subtract(player.position()).normalize()
         .scale(DevilFruitWolfConfig.pursuitSpeed());
      AABB cible = player.getBoundingBox().move(step);
      if (!level.isLoaded(BlockPos.containing(player.position().add(step)))) {
         end(player, "hors-charge");
         return;
      }
      if (!SafeDestination.isSafe(level,player,player.position().add(step.x,0,step.z))) {
         end(player, "obstacle");
         return;
      }
      player.teleportTo(player.getX() + step.x, player.getY(), player.getZ() + step.z);
      level.sendParticles(MinepieceModParticles.BIG_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(),
         4, 0.2, 0.05, 0.2, 0.02);
   }
}
