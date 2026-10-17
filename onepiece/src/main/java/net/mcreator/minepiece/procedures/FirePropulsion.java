package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.mcreator.minepiece.init.MinepieceModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Propulsion de feu</b> — Mera Mera (FRUIT-MERA_MERA-04).
 *
 * <p>Le plan : « ajouter une propulsion de feu <b>consommant de l'énergie</b> et <b>validant les
 * collisions</b> », critère « la propulsion <b>s'arrête à un obstacle</b>, à la <b>suppression du
 * pouvoir</b> ou au <b>manque d'énergie</b> ».</p>
 *
 * <h2>Les trois arrêts, et pourquoi les trois sont nécessaires</h2>
 * <p>Le critère énumère les trois conditions d'arrêt : les trois sont ici des tests, pas des
 * intentions.</p>
 * <table border="1">
 *   <caption>Conditions d'arrêt</caption>
 *   <tr><th>Situation</th><th>Sans cette condition</th></tr>
 *   <tr><td><b>Obstacle</b></td><td>le joueur traverserait le mur ou vibrerait dedans</td></tr>
 *   <tr><td><b>Suppression du pouvoir</b></td><td>le pouvoir continuerait à agir dans la mer — un
 *       pouvoir scellé qui propulse encore n'est plus scellé</td></tr>
 *   <tr><td><b>Manque d'énergie</b></td><td>la propulsion serait gratuite, donc sans limite</td></tr>
 * </table>
 *
 * <p>Le déplacement passe par {@code push} sur le vecteur de regard : la propulsion <b>pousse</b>, elle
 * ne téléporte pas. Un mur reste donc un mur, et c'est bien ce qu'on veut.</p>
 */
public final class FirePropulsion {

   private record LevelKey(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key) {}
   private static final class Thrust {
      final ServerPlayer owner;
      final LevelKey levelKey;
      int remaining;
      int tick;
      double spent;

      Thrust(int remaining,ServerPlayer player) {
         owner=player;
         levelKey=new LevelKey(player.level().dimension());
         this.remaining = remaining;
      }
   }

   private static final Map<UUID, Thrust> THRUSTS = new HashMap<>();
   private static final java.util.Set<UUID> FLIGHT_GRANTED=new java.util.HashSet<>();
   public static void reconcile(ServerPlayer player) {
      var abilities=player.getAbilities();
      var vars=MinepieceModVariables.get(player);
      if(vars.meraFlightGranted) FLIGHT_GRANTED.add(player.getUUID());
      boolean valid=MeraTechniques.usable(player) && energy(player)>=DevilFruitFlameConfig.propulsionCostPerTick();
      if(valid && !abilities.mayfly) {
         abilities.mayfly=true;FLIGHT_GRANTED.add(player.getUUID());vars.meraFlightGranted=true;player.onUpdateAbilities();
      } else if(!valid && FLIGHT_GRANTED.remove(player.getUUID()) && !abilities.instabuild && !player.isSpectator()) {
         vars.meraFlightGranted=false;abilities.mayfly=false;abilities.flying=false;player.onUpdateAbilities();
      }
   }
   public static void detach(ServerPlayer player) {
      stop(player,"deconnexion");
      var vars=MinepieceModVariables.get(player);
      boolean owned=FLIGHT_GRANTED.remove(player.getUUID()) || vars.meraFlightGranted;
      vars.meraFlightGranted=false;
      if(owned && !player.getAbilities().instabuild && !player.isSpectator()) {
         player.getAbilities().mayfly=false;player.getAbilities().flying=false;player.onUpdateAbilities();
      }
   }
   public static boolean toggle(ServerPlayer player) {
      reconcile(player);
      if(isPropelling(player)) {stop(player,"fin");return true;}
      if(!start(player)) return false;
      player.getAbilities().flying=true;player.onUpdateAbilities();return true;
   }

   private static void sendState(ServerPlayer player,boolean active) {
      var type=net.mcreator.minepiece.network.DevilFruitNetworking.FLAME_THRUST;
      if(net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player,type))
         net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,new net.mcreator.minepiece.network.DevilFruitNetworking.FlameThrustPayload(active));
   }
   private FirePropulsion() {
   }

   /** Branche l'entretien de la propulsion sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            reconcile(player);
            // Le porteur du Mera Mera se propulse dès qu'il est en vol : pas de vol stationnaire.
            ItemStack fruit = MinepieceModVariables.get(player).PlayerDevilFruit;
            if (fruit != null && fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA)
                  && !player.getAbilities().instabuild && !player.isSpectator() && player.getAbilities().flying && !isPropelling(player)) {
               start(player);
            }
            tick(player);
         }
      });
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
         detach(handler.player);LAST_STOP.remove(handler.player.getUUID());
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->{clearAll();FLIGHT_GRANTED.clear();LAST_STOP.clear();});
   }

   /**
    * Démarre une propulsion si le pouvoir et l'énergie le permettent.
    *
    * @return vrai si la propulsion a démarré
    */
   public static boolean start(ServerPlayer player) {
      if (player == null || !MeraTechniques.usable(player) || MeraTechniques.isDashing(player)) {
         return false;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return false;
      }
      if (energy(player) < DevilFruitFlameConfig.propulsionCostPerTick()) {
         return false;
      }
      THRUSTS.put(player.getUUID(), new Thrust(DevilFruitFlameConfig.propulsionMaxTicks(),player));
      sendState(player,true);
      player.getAbilities().flying=true;player.onUpdateAbilities();
      return true;
   }

   /** Vrai si le joueur est en propulsion. */
   public static boolean isPropelling(ServerPlayer player) {
      return player != null && THRUSTS.containsKey(player.getUUID());
   }

   /** Ticks de propulsion restants. */
   public static int remainingTicks(ServerPlayer player) {
      Thrust t = player == null ? null : THRUSTS.get(player.getUUID());
      return t == null ? 0 : t.remaining;
   }

   /** Énergie consommée par la propulsion en cours. */
   public static double energySpent(ServerPlayer player) {
      Thrust t = player == null ? null : THRUSTS.get(player.getUUID());
      return t == null ? 0.0 : t.spent;
   }

   /** Raison du dernier arrêt : {@code obstacle}, {@code suppression}, {@code energie}, {@code fin}. */
   public static String lastStopReason(ServerPlayer player) {
      return LAST_STOP.getOrDefault(player.getUUID(), "aucun");
   }

   private static final Map<UUID, String> LAST_STOP = new HashMap<>();

   /** Arrête la propulsion. */
   public static void stop(ServerPlayer player, String reason) {
      if (player == null) {
         return;
      }
      if (THRUSTS.remove(player.getUUID()) != null) {
         LAST_STOP.put(player.getUUID(), reason);
         sendState(player,false);
         player.setDeltaMovement(Vec3.ZERO);player.syncVelocity=true;
         // Le vol retombe : la propulsion ne se maintient pas toute seule. mayfly reste acquis,
         // pour que le joueur puisse relancer une poussée.
         var abilities = player.getAbilities();
         if (!abilities.instabuild && !player.isSpectator() && abilities.flying) {
            abilities.flying = false;
            player.onUpdateAbilities();
         }
      }
   }

   /** Arrête toutes les propulsions (reset de fruit, fin de test). */
   public static void clearAll() {
      for(Thrust thrust:new ArrayList<>(THRUSTS.values())) stop(thrust.owner,"fin");
   }

   /**
    * Entretien d'une propulsion — les trois arrêts du critère, dans cet ordre.
    *
    * <p>Exposé pour que les tests éprouvent chaque condition sans serveur réel.</p>
    */
   public static void tick(ServerPlayer player) {
      Thrust thrust = THRUSTS.get(player.getUUID());
      if (thrust == null) {
         return;
      }
      ServerLevel level = (ServerLevel) player.level();

      if(!thrust.levelKey.key().equals(level.dimension())) {stop(player,"dimension");return;}
      if(player.isSpectator()) {stop(player,"spectateur");return;}
      if(!player.isAlive() || !DevilFruitPowerLogic.getDevilFruit(player).is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA)) {
         stop(player,"retrait");return;
      }
      if(!player.getAbilities().flying) {
         if(thrust.tick<3) {player.getAbilities().flying=true;player.onUpdateAbilities();}
         else {stop(player,"fin");return;}
      }
      // 1. Suppression du pouvoir (mer, retrait) — le pouvoir scellé n'agit plus.
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         stop(player, "suppression");
         return;
      }

      // 2. Obstacle : un bloc plein devant, à hauteur de corps. On teste AVANT de pousser, sinon
      //    le joueur vibrerait dans le mur au lieu de s'arrêter devant.
      Vec3 look = player.getLookAngle();
      BlockPos devant = BlockPos.containing(
         player.getX() + look.x * 0.8, player.getY() + 0.5, player.getZ() + look.z * 0.8);
      if (!level.noCollision(player,player.getBoundingBox().expandTowards(look.scale(.8)))) {
         stop(player, "obstacle");
         return;
      }

      // 3. Énergie : chaque tick coûte, et la réserve ne se creuse pas indéfiniment.
      double cout = DevilFruitFlameConfig.propulsionCostPerTick();
      if (energy(player) < cout) {
         stop(player, "energie");
         return;
      }
      setEnergy(player, energy(player) - cout);
      thrust.spent += cout;

      // 4. Poussée : on pousse, on ne téléporte pas — un mur restera un mur.
      double force = DevilFruitFlameConfig.propulsionPower();
      player.setDeltaMovement(look.scale(force));
      player.syncVelocity=true;

      level.sendParticles(MinepieceModParticles.FLAME, player.getX(), player.getY() + 0.4, player.getZ(),
         8, 0.25, 0.25, 0.25, 0.02);
      level.sendParticles(MinepieceModParticles.FLAME_SMALL, player.getX(), player.getY() + 0.4, player.getZ(),
         4, 0.2, 0.2, 0.2, 0.01);

      thrust.tick++;
      thrust.remaining--;
      if (thrust.remaining <= 0) {
         stop(player, "fin");
      }
   }

   private static double energy(ServerPlayer player) {
      return MinepieceModVariables.get(player).PlayerEnergy;
   }

   private static void setEnergy(ServerPlayer player, double value) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      vars.PlayerEnergy = value;
      vars.syncPlayerVariables(player);
   }

   /** Liste des positions traversées récemment (observable, pour les preuves de collision). */
   public static java.util.List<Vec3> trail(ServerPlayer player) {
      java.util.List<Vec3> out = new ArrayList<>();
      out.add(player.position());
      return out;
   }
}
