package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * <b>Forme géante du Daibutsu</b> — Hito Hito Daibutsu (FRUIT-HITO_HITO_DAIBUTSU-03/05).
 *
 * <p>Le plan demande une « forme géante visible avec <b>limite de taille</b> » dont « caméra,
 * collision, portée et <b>espace nécessaire</b> sont cohérents », puis de « définir mobilité et
 * <b>coût de maintien</b> de la forme ». Les quatre exigences de cohérence se traduisent ainsi :</p>
 *
 * <table border="1">
 *   <caption>Cohérences exigées</caption>
 *   <tr><th>Exigence</th><th>Comment elle est tenue</th></tr>
 *   <tr><td><b>Caméra</b></td><td>l'échelle passe par {@code Attributes.SCALE} (vanille) : la caméra
 *       et le point de vue suivent automatiquement — rien à recoder</td></tr>
 *   <tr><td><b>Collision</b></td><td>{@code refreshDimensions()} après l'échelle : la boîte de
 *       collision correspond à ce qui est vu, sinon le géant traverserait les murs</td></tr>
 *   <tr><td><b>Portée</b></td><td>{@link #effectiveRange} — un bras trois fois et demie plus long
 *       frappe trois fois et demie plus loin : la portée suit le corps</td></tr>
 *   <tr><td><b>Espace nécessaire</b></td><td>{@link #hasRoom} — un géant ne se déploie pas dans un
 *       couloir de deux blocs : la transformation est <b>refusée</b> et le joueur sait pourquoi</td></tr>
 * </table>
 *
 * <h2>La borne de taille, et pourquoi elle est dans le code</h2>
 * <p>L'échelle demandée passe par {@link #clampedScale} : {@code max_scale} est un plafond
 * <b>appliqué</b>, pas une valeur espérée. Une table de transformation ou un fichier mal édité ne peut
 * pas produire un géant démesuré.</p>
 *
 * <h2>Le maintien, et le retour à la normale</h2>
 * <p>La forme coûte de l'énergie <b>en continu</b> et a une borne de durée : elle ne s'installe pas
 * indéfiniment. Et surtout, elle <b>se démonte</b> — les modificateurs qu'elle a posés sont retirés
 * nominativement, jamais « tous les modificateurs » : les attributs de transformation du système
 * commun restent intacts, et réciproquement. C'est ce qui rend le critère « les attributs reviennent à
 * la normale après la fin de transformation » vérifiable plutôt qu'espéré.</p>
 */
public final class GiantForm {

   /** Modificateur de mobilité propre à la forme géante (réversible, identifié par ressource). */
   private static final Identifier MOD_GIANT_SPEED =
      Identifier.fromNamespaceAndPath("minepiece", "giant_speed");

   private static final class State {
      double scale;
      int remaining;
      int energyCarry;

      State(double scale, int remaining) {
         this.scale = scale;
         this.remaining = remaining;
      }
   }

   private static final Map<UUID, State> FORMS = new HashMap<>();

   private GiantForm() {
   }

   /** Branche l'entretien des formes géantes sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tick(player);
         }
      });
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> FORMS.clear());
   }

   /** Échelle bornée par {@code max_scale} — le plafond est appliqué, pas espéré. */
   public static double clampedScale(double requested) {
      return Math.min(DevilFruitDaibutsuConfig.maxScale(), Math.max(1.0, requested));
   }

   /**
    * Volume qu'occuperait un joueur à cette échelle, en croissance depuis sa position.
    *
    * <p>Exposé pour que la preuve compare le volume exigé et la place réellement disponible.</p>
    */
   public static AABB requiredVolume(ServerPlayer player, double scale) {
      AABB box = player.getBoundingBox();
      double grow = Math.max(0.0, scale / player.getScale() - 1.0);
      // Géométrie réelle d'une mise à l'échelle : la boîte s'étend DEPUIS LES PIEDS vers le haut, et
      // s'élargit autour de l'axe vertical. Un `inflate` global exigerait aussi de la place SOUS le
      // joueur — donc dans le sol — et refuserait la transformation partout, y compris en plein champ.
      double dx = box.getXsize() * grow / 2.0;
      double dz = box.getZsize() * grow / 2.0;
      double dy = box.getYsize() * grow;
      return new AABB(box.minX - dx, box.minY, box.minZ - dz,
         box.maxX + dx, box.maxY + dy, box.maxZ + dz);
   }

   /**
    * Y a-t-il la place de se déployer ?
    *
    * <p>Un géant ne se transforme pas dans un couloir de deux blocs. On teste les blocs que la boîte
    * agrandie occuperait : si l'un d'eux est plein, la transformation est <b>refusée</b> — mieux vaut
    * un refus expliqué qu'un joueur encastré dans la pierre.</p>
    *
    * @return vrai si l'espace nécessaire est libre
    */
   public static boolean hasRoom(ServerPlayer player, double scale) {
      if (!(player.level() instanceof ServerLevel level)) {
         return false;
      }
      AABB volume = requiredVolume(player, scale);
      int minX = (int) Math.floor(volume.minX);
      int maxX = (int) Math.floor(volume.maxX);
      int minY = (int) Math.floor(volume.minY);
      int maxY = (int) Math.floor(volume.maxY);
      int minZ = (int) Math.floor(volume.minZ);
      int maxZ = (int) Math.floor(volume.maxZ);
      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (!level.isLoaded(pos)) {
                  return false; // on ne se déploie pas dans du non chargé
               }
               BlockState state = level.getBlockState(pos);
               if (state.isAir() || !state.getFluidState().isEmpty()) {
                  continue;
               }
               if (state.getCollisionShape(level, pos).isEmpty()) {
                  continue; // herbes, fleurs… traversables
               }
               return false;
            }
         }
      }
      return true;
   }

   /**
    * Portée effective d'une technique pour un porteur : la portée suit le corps.
    *
    * <p>Sans cela, un géant de 3,5× frapperait à la portée d'un humain — sa main serait dans le vide
    * alors que le modèle montre un poing qui passe le mur. C'est exactement le genre d'incohérence
    * « portée » que le critère demande d'éviter.</p>
    */
   public static double effectiveRange(ServerPlayer player, double baseRange) {
      // La formule appartient au module de combat (une seule implémentation, cf. CombatSkillLogic).
      return CombatSkillLogic.effectiveRange(player, baseRange);
   }

   /** Échelle actuellement appliquée par cette forme (1,0 si aucune). */
   public static double currentScale(ServerPlayer player) {
      State state = player == null ? null : FORMS.get(player.getUUID());
      return state == null ? 1.0 : state.scale;
   }

   /** Vrai si la forme géante est active. */
   public static boolean isGiant(ServerPlayer player) {
      return player != null && FORMS.containsKey(player.getUUID());
   }

   /** Ticks de forme restants. */
   public static int remainingTicks(ServerPlayer player) {
      State state = player == null ? null : FORMS.get(player.getUUID());
      return state == null ? 0 : state.remaining;
   }

   /** Formes actives (observable). */
   public static int activeForms() {
      return FORMS.size();
   }

   /**
    * Enregistre la forme géante et pose ses modificateurs propres.
    *
    * @return échelle retenue, ou {@code 0} si le refus est prononcé (pas la place)
    */
   public static double begin(ServerPlayer player, double requestedScale) {
      double scale = clampedScale(requestedScale);
      if (!hasRoom(player, scale)) {
         player.sendSystemMessage(Component.literal(
            "§cPas assez de place pour la forme géante (il faut de l'air autour de vous)."), false);
         return 0.0;
      }
      State state = FORMS.get(player.getUUID());
      if (state == null) {
         state = new State(scale, DevilFruitDaibutsuConfig.formMaxTicks());
         FORMS.put(player.getUUID(), state);
      }
      state.scale = scale;
      state.remaining = DevilFruitDaibutsuConfig.formMaxTicks();
      MinepieceModVariables.get(player).formRemainingTicks = state.remaining;
      // Mobilité : le géant est plus lent. Modificateur identifié, donc retirable nominativement.
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(MOD_GIANT_SPEED);
         speed.addTransientModifier(new AttributeModifier(MOD_GIANT_SPEED,
            DevilFruitDaibutsuConfig.mobilityPenalty(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      }
      return scale;
   }

   /** Restore a saved budget instead of resetting its duration on every login. */
   public static boolean restore(ServerPlayer player, double scale, int savedRemaining) {
      int budget = savedRemaining == -1 ? DevilFruitDaibutsuConfig.formMaxTicks()
         : Math.min(savedRemaining, DevilFruitDaibutsuConfig.formMaxTicks());
      if (budget <= 0 || begin(player, scale) == 0.0) return false;
      FORMS.get(player.getUUID()).remaining = budget;
      MinepieceModVariables.get(player).formRemainingTicks = budget;
      return true;
   }

   /**
    * Fin de forme : retire <b>uniquement</b> les modificateurs de cette forme.
    *
    * <p>{@code removeModifier(MOD_GIANT_SPEED)} et non « retirer tous les modificateurs » : les
    * attributs de la transformation commune (force, résistance, échelle) appartiennent à un autre
    * propriétaire et doivent survivre.</p>
    */
   public static void end(ServerPlayer player) {
      if (player == null) return;
      FORMS.remove(player.getUUID());
      MinepieceModVariables.get(player).formRemainingTicks = 0;
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         speed.removeModifier(MOD_GIANT_SPEED);
      }
   }

   /** Fin de forme pour toutes les formes actives (retrait du Fruit). */
   public static void endAll() {
      var server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         FORMS.clear();
         return;
      }
      for (ServerPlayer player : new java.util.ArrayList<>(server.getPlayerList().getPlayers())) {
         end(player);
      }
      FORMS.clear();
   }

   /**
    * Entretien : énergie de maintien, borne de durée, suppression. Exposé pour les tests.
    */
   public static void tick(ServerPlayer player) {
      State state = FORMS.get(player.getUUID());
      if (state == null) {
         return;
      }
      // Le pouvoir scellé ne maintient pas une forme géante.
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         DevilFruitTransformation.revertForm(player);
         return;
      }
      state.remaining--;
      if (state.remaining <= 0) {
         DevilFruitTransformation.revertForm(player);
         return;
      }
      // Coût de maintien : fractionné par tick pour ne pas dépendre de la granularité du tick.
      var vars = net.mcreator.minepiece.network.MinepieceModVariables.get(player);
      vars.formRemainingTicks = state.remaining;
      double perTick = Math.max(0.0, DevilFruitDaibutsuConfig.maintenanceCostPerSecond() / 20.0);
      vars.PlayerEnergy = Math.max(0.0, vars.PlayerEnergy - perTick);
      if (perTick > 0.0 && vars.PlayerEnergy <= 0.0) {
         DevilFruitTransformation.revertForm(player);
         player.sendSystemMessage(Component.literal("§7Énergie épuisée : la forme retombe."), false);
      }
      if (++state.energyCarry >= 20) {
         state.energyCarry = 0;
         vars.syncPlayerVariables(player);
      }
   }
}
