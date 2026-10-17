package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.quake.DevilFruitQuakeConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Ondes sismiques</b> — Gekishin, Kaishin, Tenchi Meido (FRUIT-GURA_GURA-03/04/05).
 *
 * <p>ver15 frappait avec le moteur générique : un cône instantané, et pour Tenchi Meido une portée de
 * 48 blocs appliquée <b>d'un seul coup</b>. Le plan demande « des ondes sismiques qui se propagent
 * plutôt qu'une frappe générique », critère « <b>front d'onde, collision et instant des dégâts</b>
 * sont cohérents ».</p>
 *
 * <h2>Ce que le front change réellement</h2>
 * <ul>
 *   <li><b>Le front voyage</b> — à 2 blocs/tick (configurable), une onde de 48 blocs met 24 ticks à
 *       parcourir son chemin. Elle se voit venir, et <b>elle s'esquive</b> : c'est une différence de
 *       jeu, pas une différence d'affichage.</li>
 *   <li><b>L'instant des dégâts est celui de l'arrivée du front</b>, pas celui du lancement. Un
 *       adversaire qui s'écarte avant l'arrivée ne prend rien ; un autre qui entre dans le couloir
 *       après le passage est fauché.</li>
 *   <li><b>Une cible n'est frappée qu'une fois</b> — le front est épais de plusieurs blocs et balayé
 *       par échantillons : sans registre des cibles déjà atteintes, un même adversaire serait frappé
 *       à chaque échantillon.</li>
 *   <li><b>Le recul suit la direction de l'onde</b> (et non un recul radial), avec une composante
 *       verticale propre à certaines techniques — c'est le « recul directionnel distinct selon la
 *       technique » demandé.</li>
 * </ul>
 *
 * <h2>Fissures</h2>
 * <p>Le front fend le sol sur son passage, via {@link PowerBlockService} (CORE-020) : les
 * fissures restent définitives ; seule leur attribution expire. La protection du terrain est appliquée par le
 * service lui-même — c'est le point d'intégration CORE-010, désormais branché.</p>
 */
public final class SeismicWave {

   /** Une onde en cours de propagation. */
   private static final class Wave {
      final ServerPlayer owner;
      final ServerLevel level;
      final Vec3 origin;
      final Vec3 direction;
      final DevilFruitMove move;
      Set<UUID> alreadyHit = new HashSet<>();
      TechniqueTerrain.Budget terrain;
      double travelled;
      int cracks;
      int hits;

      Wave(ServerPlayer owner, ServerLevel level, Vec3 origin, Vec3 direction, DevilFruitMove move) {
         this.owner = owner;
         this.level = level;
         this.origin = origin;
         this.direction = direction;
         this.move = move;
      }
   }

   /**
    * Élévation additionnelle du recul, par technique — c'est ce qui rend le recul « distinct selon la
    * technique » plutôt que dépendant du seul nombre de recul.
    */
   private static final java.util.Map<String, Double> VERTICAL_LIFT = java.util.Map.of(
      "kabutowari", 0.4,   // fend le casque : la cible est soulevée
      "tenchimeido", 0.3); // ciel et terre : l'onde soulève le sol

   private static final List<Wave> ACTIVE = new ArrayList<>();

   private SeismicWave() {
   }

   /** Branche l'avancement des ondes sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((h,s)->clearFrom(h.player));
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s->clearAll());
   }

   /** Vrai si cette technique se propage en onde plutôt que de frapper en cône. */
   public static boolean isWave(String moveId) {
      return switch (moveId) {
         case "gekishin", "kaishin", "tenchimeido" -> true;
         default -> false;
      };
   }

   /** Lance une onde depuis le porteur, dans sa direction horizontale. */
   public static void cast(ServerPlayer player, DevilFruitMove move) {
      castDirected(player,move,horizontalFacing(player),new HashSet<>());

   }

   public static void castDirected(ServerPlayer player,DevilFruitMove move,Vec3 direction,Set<UUID> hits){
      if(!(player.level() instanceof ServerLevel l) || !BatchRules.usable(player,l,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA))return;
      Wave wave=new Wave(player,l,player.position(),direction.normalize(),move);wave.alreadyHit=hits;ACTIVE.add(wave);
   }
   public static void castDirected(ServerPlayer player,DevilFruitMove move,Vec3 direction,Set<UUID> hits,TechniqueTerrain.Budget terrain){int before=ACTIVE.size();castDirected(player,move,direction,hits);if(ACTIVE.size()>before)ACTIVE.getLast().terrain=terrain;}
   public static void clearFrom(ServerPlayer owner){ACTIVE.removeIf(w->w.owner==owner);}
   /**
    * Fait avancer toutes les ondes actives d'un tick.
    *
    * <p>Exposé pour que les tests puissent dérouler le temps de façon déterministe, sans attendre un
    * serveur réel.</p>
    */
   public static void tick() {
      if (ACTIVE.isEmpty()) {
         return;
      }
      List<Wave> finished = new ArrayList<>();
      for (Wave wave : ACTIVE) {
         if (!BatchRules.usable(wave.owner,wave.level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA) || advance(wave)) {
            finished.add(wave);
         }
      }
      ACTIVE.removeAll(finished);
   }

   /** Avance une onde d'un pas ; renvoie {@code true} si elle est terminée. */
   private static boolean advance(Wave wave) {
      double speed = Math.min(4,Math.max(.25,DevilFruitQuakeConfig.waveSpeed()));
      double from = wave.travelled;
      double to = Math.min(wave.travelled + speed, wave.move.range());
      wave.travelled = to;

      // Échantillonnage du segment parcouru : un front ne doit pas « sauter » par-dessus une cible
      // lorsque la vitesse dépasse son épaisseur.
      int samples = Math.max(1, (int) Math.ceil((to - from) / 0.5));
      for (int i = 0; i <= samples; i++) {
         double along = from + (to - from) * (i / (double) samples);
         Vec3 front = wave.origin.add(wave.direction.scale(along));
         if(LightBeams.blocked(wave.level,wave.origin.add(0,1,0),wave.direction,along)){if(wave.terrain!=null){var contact=wave.level.clip(new net.minecraft.world.level.ClipContext(wave.origin.add(0,1,0),front.add(0,1,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,wave.owner));if(contact.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK)wave.terrain.impact(contact.getLocation().subtract(wave.direction.scale(.02)),3,8);}return true;}
         sweep(wave, front);
         if (i < samples) {
            placeCrack(wave, front);
         }
      }
      ControlledBatchTechniques.visual(wave.owner,"b02_quake_front",wave.origin.add(wave.direction.scale(to)).add(0,1,0),3);
      return to >= wave.move.range();
   }

   /** Fauche ce qui se trouve dans l'épaisseur du front, une seule fois par cible. */
   private static void sweep(Wave wave, Vec3 front) {
      double thickness = DevilFruitQuakeConfig.waveThickness();
      double halfHeight = DevilFruitQuakeConfig.waveHalfHeight();
      AABB zone = new AABB(front.x - thickness, front.y - halfHeight, front.z - thickness,
         front.x + thickness, front.y + halfHeight, front.z + thickness);
      var source = CombatSkillLogic.elementalDamageSource(wave.level, ElementType.EXPLOSIVE, wave.owner, null);
      for (LivingEntity target : wave.level.getEntitiesOfClass(LivingEntity.class, zone)) {
         if (target == wave.owner || !target.isAlive() || wave.alreadyHit.contains(target.getUUID())) {
            continue;
         }
         if (!BatchRules.target(wave.owner, target)) {
            continue;
         }
         if (target.getBoundingBox().getCenter().distanceTo(front) > thickness + halfHeight) {
            continue;
         }
         if (!CombatSkillLogic.hurtAndConfirm(target,wave.level,source,(float)wave.move.damage())) {
            continue;
         }
         wave.alreadyHit.add(target.getUUID());
         wave.hits++;
         // Recul DIRECTIONNEL : la cible est poussée dans le sens de l'onde, pas à la ronde.
         //
         // Signature réelle (lue dans le bytecode, car elle est piégeuse) :
         //   knockback(double force, double x, double z, DamageSource, float)
         // — le premier argument est la FORCE, pas une composante ;
         // — le vecteur (x, z) est RETRANCHÉ du mouvement, il va donc de la cible vers l'origine du
         //   coup : on passe l'opposé de la poussée voulue. L'appeler dans le désordre donne une
         //   force nulle (la composante x d'une onde plein nord vaut 0) et un recul qui n'existe pas.
         target.knockback((float) wave.move.knockback(), -wave.direction.x, -wave.direction.z, source, 0.0F);
         // Élévation propre à la technique : elle ne peut pas venir de la force de recul, la part
         // verticale de `knockback` étant plafonnée à 0,4 quel que soit l'appelant.
         Double lift = VERTICAL_LIFT.get(wave.move.id());
         if (lift != null) {
            target.push(0.0, lift, 0.0);
         }
      }
   }

   /**
    * Fend le sol au passage du front : fissure visible, ou vrai trou si la destruction est activée.
    *
    * <p>La pose passe par {@link PowerBlockService}, donc : terrain définitif, attribution bornée et
    * <b>refus dans une zone protégée</b>.</p>
    */
   private static void placeCrack(Wave wave, Vec3 front) {
      if(wave.terrain!=null){wave.terrain.impact(front.add(0,.3,0),3,8);return;}
      if (wave.cracks >= DevilFruitQuakeConfig.maxCracksPerWave()) {
         return;
      }
      BlockPos surface = surfaceBelow(wave.level, BlockPos.containing(front));
      if (surface == null || PowerBlockService.isTrackedAny(wave.level,surface)) {
         return;
      }
      BlockState crack = DevilFruitQuakeConfig.terrainDestruction()
         ? Blocks.AIR.defaultBlockState()
         : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
      int before = PowerBlockService.activeCount();
      PowerBlockService.place(wave.level, surface, crack,
         DevilFruitQuakeConfig.crackLifetimeTicks(), wave.owner.getUUID());
      if (PowerBlockService.activeCount() > before) {
         wave.cracks++;
      }
   }

   /** Premier bloc solide sous un point, dans une fenêtre bornée (une fissure suit le relief). */
   private static BlockPos surfaceBelow(ServerLevel level, BlockPos from) {
      for (int dy = 1; dy <= 5; dy++) {
         BlockPos pos = from.below(dy);
         if (!level.isLoaded(pos)) {
            return null;
         }
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.getFluidState().isEmpty()) {
            return pos;
         }
      }
      return null;
   }

   /** Direction horizontale du regard, normalisée (une onde se propage à plat, pas vers le sol). */
   private static Vec3 horizontalFacing(ServerPlayer player) {
      Vec3 look = player.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0, look.z);
      return flat.length() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : flat.normalize();
   }

   /** Nombre d'ondes en cours (état observable, utilisé par les preuves). */
   public static int activeWaves() {
      return ACTIVE.size();
   }

   /** Nombre de cibles fauchées par les ondes en cours. */
   public static int hitsSoFar() {
      return ACTIVE.stream().mapToInt(w -> w.hits).sum();
   }

   /** Arrête toutes les ondes en cours (retrait du Fruit, fin de test). */
   public static void clearAll() {
      ACTIVE.clear();
   }
}
