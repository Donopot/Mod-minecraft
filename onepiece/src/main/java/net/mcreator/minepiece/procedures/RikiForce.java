package net.mcreator.minepiece.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * <b>RIKI RIKI — les trois mécaniques propres du Fruit (la force, la saisie, la projection).</b>
 *
 * <h2>Ce que dit la source</h2>
 * <ul>
 *   <li>{@code GalleonLariatProcedure}, {@code StrikeThrowingProcedure} et leurs sœurs lisent
 *       {@code Attributes.f_22276_} — c'est-à-dire <b>ATTACK_DAMAGE</b>. La force du Fruit n'est donc
 *       pas un chiffre fixe : c'est un <b>multiplicateur</b> adossé à l'attribut de la cible qui le
 *       porte.</li>
 *   <li>{@code HadoElbowProcedure} écrit {@code BlockDamage} et {@code BlockRange} avant d'appeler
 *       {@code BlockDestroyAllDirectionProcedure} : le Fruit <b>manipule des blocs</b>, sur une portée
 *       déclarée — et non en rayant le monde au hasard.</li>
 *   <li>{@code StoneThrowingLargeProcedure} porte la constante {@code 1000.0} et le son
 *       {@code minepiece:stone_crash} : un lancer qui <b>percute</b>, avec un impact qui compte.</li>
 * </ul>
 *
 * <h2>Les trois règles tenues</h2>
 * <ol>
 *   <li><b>Multiplicateur borné et conditionné.</b> Adossé à {@code ATTACK_DAMAGE}, <b>plafonné</b>, et
 *       qui <b>disparaît</b> sous scellement : un porteur scellé frappe à sa force nue, pas à celle du
 *       Fruit. Une force qui survit à l'eau de mer serait un contournement du scellement.</li>
 *   <li><b>Saisie brève, toujours rendue.</b> Une cible est tenue quelques dizaines de ticks, puis
 *       relâchée — et elle l'est <b>dans tous les cas</b> : échéance, mort de la cible, mort du porteur,
 *       déconnexion. Une prise qui survit à son porteur est une cible prisonnière pour toujours.</li>
 *   <li><b>Projection bornée, blocs protégés intacts.</b> Le lancer s'arrête au mur (il ne traverse pas
 *       le décor) et la casse de blocs <b>exclut</b> une liste explicite : lit, barrière, coffres,
 *       générateurs, obsidienne. Casser un coffre à distance n'est pas de la force, c'est du pillage.</li>
 * </ol>
 */
public final class RikiForce {

   /** Plafond du multiplicateur de force : au-delà, ce n'est plus un Fruit, c'est une anomalie. */
   public static final double STRENGTH_MAX = 1.5;

   /** Valeur d'ATTACK_DAMAGE pour laquelle le multiplicateur atteint son plafond. */
   public static final double STRENGTH_FULL_AT = 40.0;

   /** Durée de la saisie, en ticks (les compteurs de la source montent par pas de 5). */
   public static final int GRAB_TICKS = 40;

   /** Force de la projection. */
   public static final double THROW_SPEED = 1.8;

   /** Rayon de casse des blocs manipulables — la {@code BlockRange} de la source, plafonnée. */
   public static final double BREAK_RADIUS = 3.0;

   private static final class Prise {
      final ServerLevel level;
      final net.minecraft.world.item.Item fruit;
      /** Référence gardée au porteur : un joueur de test n'est pas dans la liste du serveur. */
      final ServerPlayer porteur;
      final LivingEntity cible;
      int restant;

      Prise(ServerPlayer porteur, LivingEntity cible) {
         this.porteur = porteur;
         this.level=(ServerLevel)porteur.level();this.fruit=DevilFruitPowerLogic.getDevilFruit(porteur).getItem();
         this.cible = cible;
         this.restant = GRAB_TICKS;
      }
   }

   private static final Map<UUID, Prise> PRISES = new HashMap<>();
   private static final net.minecraft.resources.Identifier HOLD=net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece","riki_hold");

   // compteurs de preuve
   private static int saisies;
   private static int liberations;
   private static int projections;
   private static int blocsCasses;
   private static int blocsEpargnes;

   private RikiForce() {
   }

   public static int saisies() {
      return saisies;
   }

   public static int liberations() {
      return liberations;
   }

   public static int projections() {
      return projections;
   }

   public static int blocsCasses() {
      return blocsCasses;
   }

   public static int blocsEpargnes() {
      return blocsEpargnes;
   }

   public static void resetCompteurs() {
      for(var prise:new ArrayList<>(PRISES.values()))release(prise.porteur,"reset");
      saisies = 0;
      liberations = 0;
      projections = 0;
      blocsCasses = 0;
      blocsEpargnes = 0;
      PRISES.clear();
   }

   // ============================================================================================
   // ① LE MULTIPLICATEUR — borné, conditionné
   // ============================================================================================

   /**
    * Multiplicateur de force du porteur.
    *
    * <p>Trois conditions, chacune vérifiable : il suit {@code ATTACK_DAMAGE} (la source lit cet
    * attribut), il est <b>plafonné</b> à {@value #STRENGTH_MAX}, et il vaut {@code 1,0} — c'est-à-dire
    * <b>rien</b> — dès que le porteur est scellé.</p>
    */
   public static double strengthMultiplier(ServerPlayer player) {
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         return 1.0;
      }
      double attaque = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      return Math.min(STRENGTH_MAX, 1.0 + Math.max(0.0, attaque) / STRENGTH_FULL_AT);
   }

   // ============================================================================================
   // ② LA SAISIE — brève, et toujours rendue
   // ============================================================================================

   /**
    * Saisit une cible autorisée.
    *
    * <p>Une seule prise par porteur : saisir une seconde cible <b>relâche la première</b>, avec sa cause.
    * Une prise laissée en suspens serait une cible tenue par un joueur qui n'y pense plus.</p>
    */
   public static boolean grab(ServerPlayer player, LivingEntity cible) {
      if (cible==null || !BatchRules.target(player,cible) || DevilFruitPowerLogic.isSuppressed(player)) {
         return false;
      }
      Prise ancienne = PRISES.get(player.getUUID());
      if (ancienne != null) {
         release(player, "remplacement");
      }
      PRISES.put(player.getUUID(), new Prise(player, cible));
      var speed=cible.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null){speed.removeModifier(HOLD);speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(HOLD,-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}cible.setDeltaMovement(Vec3.ZERO);
      saisies++;
      return true;
   }

   /** La cible tenue par ce porteur, ou {@code null}. */
   public static LivingEntity held(ServerPlayer player) {
      Prise p = PRISES.get(player.getUUID());
      return p == null ? null : p.cible;
   }

   /** Le porteur tient-il une cible encore valable ? */
   public static boolean isHolding(ServerPlayer player) {
      Prise p = PRISES.get(player.getUUID());
      return p != null && p.cible.isAlive();
   }

   /** Ticks restants de la prise (0 si aucune). */
   public static int grabRemaining(ServerPlayer player) {
      Prise p = PRISES.get(player.getUUID());
      return p == null ? 0 : p.restant;
   }

   /**
    * Relâche la cible — la sortie de la prise est <b>unique</b>, quelle qu'en soit la cause.
    *
    * @param cause « echeance », « mort_cible », « mort_porteur », « deconnexion », « remplacement »
    */
   public static void release(ServerPlayer player, String cause) {
      Prise p = PRISES.remove(player.getUUID());
      if (p == null) {
         return;
      }
      var speed=p.cible.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.removeModifier(HOLD);
      liberations++;
   }

   public static int liberationsTotal() {
      return liberations;
   }

   // ============================================================================================
   // ③ LA PROJECTION — bornée, et le décor a le dernier mot
   // ============================================================================================

   /**
    * Projette la cible tenue (ou la cible désignée) le long du regard.
    *
    * <p>La projection <b>ne traverse pas</b> le décor : on mesure le trajet avant de pousser, et si un
    * bloc solide le barre, la cible s'arrête là. Puis la casse éventuelle épargne les blocs protégés.</p>
    */
   public static boolean throwTarget(ServerPlayer player, LivingEntity cible) {
      if (cible == null || !cible.isAlive()) {
         return false;
      }
      ServerLevel level = player.level() instanceof ServerLevel s ? s : null;
      if (level == null) {
         return false;
      }
      Vec3 direction = player.getViewVector(1.0F).normalize();
      double libre = distanceLibre(level, cible.position(), direction, 12.0);
      double portee = Math.min(libre, 12.0);
      Vec3 poussee = direction.scale(THROW_SPEED * Math.max(0.25, portee / 12.0));
      cible.push(poussee.x, Math.max(0.25, direction.y + 0.35), poussee.z);
      release(player, "projection");
      projections++;
      // Les blocs percutés : manipulables seulement, la portée déclarée bornant la casse.
      // Terrain changes require an actual collision handled by MaterialBatchTechniques.
      return true;
   }

   /** Distance libre devant la cible, par pas d'un quart de bloc — jamais à travers un mur. */
   public static double distanceLibre(ServerLevel level, Vec3 depart, Vec3 direction, double max) {
      Vec3 pas = direction.normalize().scale(0.25);
      Vec3 point = depart;
      for (double parcouru = 0.0; parcouru < max; parcouru += 0.25) {
         point = point.add(pas);
         BlockPos pos = BlockPos.containing(point);
         if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return parcouru;
         }
      }
      return max;
   }

   /**
    * Un bloc est-il <b>protégé</b> — donc jamais manipulé par la force, même à pleine puissance ?
    *
    * <p>La liste est explicite et testable. Elle n'est pas une politique de confort : c'est la limite
    * entre « je déplace de la matière » et « je casse la maison de quelqu'un ».</p>
    */
   public static boolean isProtected(BlockState state) {
      return state.is(Blocks.BEDROCK)
         || state.is(Blocks.BARRIER)
         || state.is(Blocks.LIGHT)
         || state.is(Blocks.OBSIDIAN)
         || state.is(Blocks.CRYING_OBSIDIAN)
         || state.is(Blocks.ENDER_CHEST)
         || state.is(Blocks.CHEST)
         || state.is(Blocks.TRAPPED_CHEST)
         || state.is(Blocks.BARREL)
         || state.is(Blocks.SPAWNER)
         || state.is(Blocks.END_PORTAL_FRAME)
         || state.is(Blocks.RESPAWN_ANCHOR);
   }

   /** Casse les blocs manipulables autour d'un point, en <b>épargnant</b> les protégés. */
   public static int casserBlocsManipulables(ServerLevel level, Vec3 centre, double rayon) {
      int casses = 0;
      BlockPos origine = BlockPos.containing(centre);
      int r = (int) Math.ceil(Math.min(rayon, BREAK_RADIUS));
      for (int dx = -r; dx <= r; dx++) {
         for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
               BlockPos pos = origine.offset(dx, dy, dz);
               BlockState etat = level.getBlockState(pos);
               if (etat.isAir()) {
                  continue;
               }
               if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || DevilFruitExplosions.isProtectedPosition(level,pos) || etat.hasBlockEntity() || level.getBlockEntity(pos)!=null || etat.getBlock().getExplosionResistance()>6 || !etat.getFluidState().isEmpty() || PowerBlockService.isTrackedAny(level,pos) || isProtected(etat)) {
                  blocsEpargnes++;
                  continue;
               }
               if (etat.getDestroySpeed(level, pos) < 0.0F) {
                  // Un bloc non cassable (dureté négative) n'est pas une cible : le décor reste.
                  blocsEpargnes++;
                  continue;
               }
               if(pos.distSqr(origine)<=rayon*rayon && casses<96){var cover=level.clip(new net.minecraft.world.level.ClipContext(centre,Vec3.atCenterOf(pos),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));if(cover.getType()==net.minecraft.world.phys.HitResult.Type.MISS || cover.getBlockPos().equals(pos)){level.destroyBlock(pos,false);casses++;}}
            }
         }
      }
      blocsCasses += casses;
      return casses;
   }

   // ============================================================================================
   // ENTRETIEN — l'échéance et les deux morts
   // ============================================================================================

   /**
    * Enregistre l'entretien des saisies.
    *
    * <p>Trois sorties sont surveillées ici : la cible qui meurt, le porteur qui meurt, et l'échéance.
    * La quatrième — la déconnexion — est branchée juste après, sur l'événement de connexion, parce
    * qu'un joueur qui s'en va n'existe plus pour le tick suivant.</p>
    */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Map.Entry<UUID, Prise> entree : new ArrayList<>(PRISES.entrySet())) {
            Prise prise = entree.getValue();
            ServerPlayer porteur = prise.porteur;
            if (porteur == null || porteur.isRemoved() || porteur.level()!=prise.level || prise.cible.level()!=prise.level || !DevilFruitPowerLogic.getDevilFruit(porteur).is(prise.fruit) || DevilFruitPowerLogic.isSuppressed(porteur) || !CombatSkillLogic.isValidTarget(porteur,prise.cible)) {
               release(porteur,"retrait");continue;
            }
            if (!prise.cible.isAlive()) {
               release(porteur, "mort_cible");
               continue;
            }
            if (!porteur.isAlive()) {
               release(porteur, "mort_porteur");
               continue;
            }
            if (--prise.restant <= 0) {
               release(porteur, "echeance");
            }
            else prise.cible.setDeltaMovement(Vec3.ZERO);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer joueur = handler.getPlayer();
         if (joueur != null) {
            release(joueur, "deconnexion");
         }
      });
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->{for(var prise:new ArrayList<>(PRISES.values()))release(prise.porteur,"arret");});
   }
}
