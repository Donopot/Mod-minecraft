package br.com.oneminepiece.opworldlike.flight;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.OmpWorld;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Vol des Fruits ailés — énergie et atterrissage garanti (FRUIT-BATTO_BATTO-04).
 *
 * <p>ver15 accordait le vol sans coût : une fois en l'air, le joueur y restait indéfiniment. Le plan
 * demande un vol « consommant l'énergie avec atterrissage garanti », qui « cesse à suppression ou
 * manque d'énergie sans laisser le joueur bloqué ».</p>
 *
 * <p><b>Atterrissage garanti</b> : la révocation met toujours {@code mayfly} <i>et</i> {@code flying}
 * à faux (jamais l'un sans l'autre — c'est cet état dissocié qui bloquerait un joueur en l'air), puis
 * la gravité vanilla le ramène au sol. Aucune téléportation, donc aucun risque de poser le joueur
 * dans un bloc.</p>
 *
 * <p><b>Écholocation</b> (FRUIT-BATTO_BATTO-05) : impulsion sensorielle <b>bornée</b> et périodique du
 * Fruit de la Chauve-souris — elle marque (Glowing) les créatures dans un rayon fixe. C'est une
 * attaque <i>sensorielle</i>, pas une frappe : portée bornée, aucun dégât, aucun impact.</p>
 */
public final class DevilFruitFlight {

   /** Intervalle de synchronisation de l'Énergie au client (le HUD n'a pas besoin de 20 maj/s). */
   private static final int SYNC_INTERVAL = 5;

   private static final Item[] FLYING_FRUITS = {
      MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO,
      MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX,
      MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PTERANODON,
   };

   private DevilFruitFlight() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         if (!DevilFruitFlightConfig.enabled()) {
            return;
         }
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tick(player);
         }
      });
   }

   /** Vrai si le Fruit équipé fait partie des Fruits ailés. */
   public static boolean isFlyingFruit(ItemStack fruit) {
      if (fruit == null || fruit.isEmpty()) {
         return false;
      }
      for (Item candidate : FLYING_FRUITS) {
         if (fruit.is(candidate)) {
            return true;
         }
      }
      return false;
   }

   /** Vrai si le Fruit équipé est celui de la Chauve-souris (écholocation). */
   public static boolean isBatFruit(ItemStack fruit) {
      return fruit != null && fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO);
   }

   /** Boucle de vol pour un joueur — publique pour être exerçable par le harnais de tests. */
   public static void tick(ServerPlayer player) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      ItemStack fruit = vars.PlayerDevilFruit;
      if (!isFlyingFruit(fruit)) {
         return;
      }
      // The owned Zoan form maintains its own flight and energy, preserving foreign grants.
      if(net.mcreator.minepiece.procedures.ZoanForms.handles(fruit)) {
         if(isBatFruit(fruit)&&DevilFruitFlightConfig.echolocationEnabled()&&player.tickCount%DevilFruitFlightConfig.echolocationInterval()==0)echolocation(player);
         return;
      }

      // Écholocation : passive, bornée, périodique — indépendante du vol.
      if (isBatFruit(fruit) && DevilFruitFlightConfig.echolocationEnabled()
            && player.tickCount % DevilFruitFlightConfig.echolocationInterval() == 0) {
         echolocation(player);
      }

      if (!player.getAbilities().flying) {
         return;                                        // pas en vol : rien à consommer
      }

      // 1. Forme : hors forme hybride, le vol n'est plus légitime → atterrissage.
      if (DevilFruitFlightConfig.requireHybridForm() && vars.player_form == 0.0) {
         land(player, "minepiece.message.flight_needs_form");
         return;
      }

      // 2. Suppression par la mer → atterrissage immédiat.
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         land(player, "minepiece.message.flight_suppressed");
         return;
      }

      // 3. Énergie : consommation continue, atterrissage quand la réserve ne tient plus.
      double cost = DevilFruitFlightConfig.costPerSecond() / 20.0;
      if (vars.PlayerEnergy < Math.max(cost, DevilFruitFlightConfig.minimumEnergy())) {
         vars.PlayerEnergy = 0.0;
         vars.syncPlayerVariables(player);
         land(player, "minepiece.message.flight_no_energy");
         return;
      }
      vars.PlayerEnergy -= cost;
      if (player.tickCount % SYNC_INTERVAL == 0) {
         vars.syncPlayerVariables(player);
      }
   }

   /**
    * Révoque le vol et prévient le joueur. {@code mayfly} et {@code flying} sont retirés
    * <b>ensemble</b> : c'est la seule garantie que la gravité reprenne la main et que le joueur ne
    * reste pas bloqué en l'air.
    */
   private static void land(ServerPlayer player, String messageKey) {
      var abilities = player.getAbilities();
      boolean wasFlying = abilities.flying;
      abilities.mayfly = false;
      abilities.flying = false;
      player.onUpdateAbilities();
      if (wasFlying) {
         player.sendSystemMessage(Component.literal("§7").append(Component.translatable(messageKey)), false);
      }
   }

   /**
    * Implémentation de la révocation centrale utilisée par le reset du Fruit : le joueur perd le vol
    * sans rester bloqué.
    */
   public static void revokeFlight(ServerPlayer player) {
      land(player, "minepiece.message.flight_needs_form");
   }

   /** Impulsion sensorielle bornée : marque les créatures du rayon, sans dégât ni impact. */
   /** @return nombre de créatures marquées */
   public static int echolocation(ServerPlayer player) {
      ServerLevel level = (ServerLevel) player.level();
      double radius = DevilFruitFlightConfig.echolocationRadius();
      int marked = 0;
      for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(radius))) {
         if (entity == player || entity.isSpectator()) {
            continue;
         }
         if (entity.distanceTo(player) > radius) {
            continue;                                   // borne stricte : rayon, pas la diagonale de la boîte
         }
         entity.addEffect(new MobEffectInstance(MobEffects.GLOWING,
            DevilFruitFlightConfig.echolocationGlowingTicks(), 0, false, false, false));
         marked++;
      }
      // Onde sonore visible : anneau horizontal, lui aussi borné au rayon configuré.
      int points = 24;
      for (int i = 0; i < points; i++) {
         double angle = 2.0 * Math.PI * i / points;
         double x = player.getX() + Math.cos(angle) * radius;
         double z = player.getZ() + Math.sin(angle) * radius;
         level.sendParticles(MinepieceModParticles.NEGATIVE, x, player.getY() + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
      }
      if (marked > 0) {
         OmpWorld.LOGGER.debug("Écholocation : {} créature(s) marquée(s) pour {}",
            marked, player.getName().getString());
      }
      return marked;
   }
}
