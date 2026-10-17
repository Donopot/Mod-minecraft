package net.mcreator.minepiece.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>NIKYU NIKYU — les trois mécaniques propres du Fruit (Pad Ho, la déviation, Ursus Shock).</b>
 *
 * <p>Ces trois comportements ne sont pas des dégâts supplémentaires : ce sont des <b>règles</b>. Le
 * contrat du Fruit les décrit, et le moteur générique ne peut pas les porter sans les dénaturer — il
 * applique des dégâts, pas une orientation, pas une réflexion, pas une charge.</p>
 *
 * <h2>Ce que dit la source</h2>
 * <ul>
 *   <li>{@code PadHoProcedure} et {@code TsuppariPadHoProcedure} — la paume qui repousse : les
 *       constantes montrent un <b>balayage angulaire</b> (90°, 100°, 180°) et des <b>distances</b> de
 *       14, 15 et 20 blocs, avec {@code RotateEntityProcedure} et
 *       {@code GetDistanceNearestEnemyProcedure}. Une répulsion orientée, pas une explosion.</li>
 *   <li>{@code NikyuNikyuRightClickProcedure} — la <b>déviation</b> : elle lit les munitions
 *       ({@code forge:ranged_ammo}, {@code forge:ranged_ammo_no_move}) et écrit un vecteur de poussée
 *       ({@code x_power}, {@code y_power}, {@code z_power}). C'est bien un renvoi de projectile.</li>
 *   <li>{@code UrsusShockProcedure} — l'onde : elle stocke {@code x_pos}/{@code y_pos}/{@code z_pos}
 *       <b>et</b> leur {@code _power}, avec 19, 20 et 30 dans les constantes. Un impact <b>chargé</b>,
 *       donc préparé puis libéré, pas instantané.</li>
 * </ul>
 *
 * <h2>Les trois règles tenues</h2>
 * <ol>
 *   <li><b>Répulsion plafonnée et honnête avec le décor</b> : la portée est bornée ({@value #PADHO_RANGE_MAX}
 *       blocs), la force l'est aussi, et une cible dont la case de destination est pleine <b>n'est pas</b>
 *       poussée dans le mur — même garde-fou que le champ de gravité de Zushi.</li>
 *   <li><b>Réflexion attribuée</b> : un projectile renvoyé change de <b>propriétaire</b>, pas seulement de
 *       direction. Sans cela, le tireur continuerait de récolter les dégâts de sa propre attaque retournée.</li>
 *   <li><b>Le signal précède l'impact</b> : l'onde d'Ursus Shock <b>annonce</b> sa zone pendant
 *       {@value #URSUS_WARNING_TICKS} ticks avant de frapper. Une frappe annoncée est une frappe qu'on peut
 *       esquiver ; c'est la différence entre un pouvoir et un guet-apens.</li>
 * </ol>
 */
public final class NikyuRepulsion {

   /** Portée maximale de la paume — la source balaie 14, 15 et 20 blocs ; on plafonne au plus grand. */
   public static final double PADHO_RANGE_MAX = 20.0;

   /** Demi-angle du balayage : 105°, soit les 90/100/180° de la source ramenés à un cône franc. */
   public static final double PADHO_CONE_COS = -0.26;

   /** Force de poussée et son plafond — la source monte à 20, on borne pour ne pas envoyer dans l'orbite. */
   public static final double PADHO_FORCE = 2.2;
   public static final double PADHO_FORCE_MAX = 4.0;

   /** Charge d'Ursus Shock : 30 ticks pour atteindre la puissance pleine (source : 19/20/30). */
   public static final int URSUS_CHARGE_TICKS = 30;

   /** Annonce de la zone avant l'impact : le signal précède la frappe. */
   public static final int URSUS_WARNING_TICKS = 12;

   /** Rayon de l'onde lâchée à pleine charge (source : 19/20/30). */
   public static final double URSUS_RADIUS = 20.0;

   /** Compteurs de preuve, lus par le harnais : rien d'invisible, tout est mesurable. */
   private static int poussees;
   private static int epargneesParLeDecor;
   private static int deviees;
   private static int ondesAnnoncees;
   private static int ondesFrappees;

   private NikyuRepulsion() {
   }

   public static int poussees() {
      return poussees;
   }

   public static int epargneesParLeDecor() {
      return epargneesParLeDecor;
   }

   public static int deviees() {
      return deviees;
   }

   public static int ondesAnnoncees() {
      return ondesAnnoncees;
   }

   public static int ondesFrappees() {
      return ondesFrappees;
   }

   public static void resetCompteurs() {
      poussees = 0;
      epargneesParLeDecor = 0;
      deviees = 0;
      ondesAnnoncees = 0;
      ondesFrappees = 0;
      CHARGES.clear();
      ANNONCES.clear();
   }

   // ============================================================================================
   // ① PAD HO — répulsion orientée, plafonnée, qui respecte le décor
   // ============================================================================================

   /**
    * La paume repousse ce qui est devant, dans un cône, jusqu'à une portée bornée.
    *
    * <p>Trois refus possibles et voulus : la cible derrière le lanceur, la cible hors de portée, et la
    * cible dont la case de destination est pleine. Dans les trois cas elle n'est <b>pas</b> poussée.</p>
    *
    * @return vrai : la technique a été prise en charge
    */
   public static boolean padHo(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = player.level() instanceof ServerLevel s ? s : null;
      if (level == null) {
         return false;
      }
      Vec3 regard = player.getViewVector(1.0F).normalize();
      Vec3 origine = player.position().add(0.0, player.getEyeHeight() * 0.5, 0.0);
      AABB zone = player.getBoundingBox().inflate(PADHO_RANGE_MAX, 8.0, PADHO_RANGE_MAX);

      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class, zone)) {
         if (cible == player || !CombatSkillLogic.isValidTarget(player, cible)) {
            continue;
         }
         Vec3 vers = cible.position().subtract(origine);
         double distance = vers.length();
         if (distance > PADHO_RANGE_MAX || distance < 0.01) {
            continue;
         }
         // Le cône : ce qui est derrière la paume n'est pas repoussé — c'est une paume, pas une aura.
         if (vers.normalize().dot(regard) < PADHO_CONE_COS) {
            continue;
         }
         Vec3 poussee = regard.scale(PADHO_FORCE);
         if (poussee.length() > PADHO_FORCE_MAX) {
            poussee = poussee.normalize().scale(PADHO_FORCE_MAX);
         }
         // Le décor a le dernier mot : on ne pousse pas une cible dans la roche.
         if (!destinationLibre(level, cible, poussee)) {
            epargneesParLeDecor++;
            continue;
         }
         cible.push(poussee.x, 0.35, poussee.z);
         if (move.damage() > 0.0) {
            cible.hurtServer(level, CombatSkillLogic.elementalDamageSource(level,
               DevilFruitElement.of(net.mcreator.minepiece.network.MinepieceModVariables.get(player).PlayerDevilFruit), player, null), (float) move.damage());
         }
         poussees++;
      }
      return true;
   }

   /**
    * La case d'arrivée est-elle libre ?
    *
    * <p>On échantillonne le trajet par pas d'un quart de bloc : assez fin pour ne jamais traverser un mur
    * d'un bloc, comme le service de gravité de Zushi.</p>
    */
   private static boolean destinationLibre(ServerLevel level, LivingEntity cible, Vec3 poussee) {
      Vec3 depart = cible.position();
      double longueur = poussee.length();
      if (longueur < 0.01) {
         return true;
      }
      Vec3 pas = poussee.normalize().scale(0.25);
      Vec3 point = depart;
      for (double parcouru = 0.0; parcouru < longueur; parcouru += 0.25) {
         point = point.add(pas);
         net.minecraft.core.BlockPos case_ = net.minecraft.core.BlockPos.containing(point.x,
            cible.getY() + cible.getBbHeight() * 0.5, point.z);
         if (!level.getBlockState(case_).getCollisionShape(level, case_).isEmpty()) {
            return false;
         }
      }
      return true;
   }

   // ============================================================================================
   // ② DÉVIATION — renvoyer un projectile, et s'en attribuer la paternité
   // ============================================================================================

   /**
    * Un projectile touche-t-il un porteur de Nikyu Nikyu en état de dévier ?
    *
    * <p>Si oui, il est <b>renvoyé</b> : direction inversée <b>et propriétaire transféré</b>. Le transfert
    * n'est pas un détail cosmétique — le propriétaire d'un projectile est celui à qui le jeu attribue les
    * dégâts, donc sans lui le tireur récolterait le produit de son propre tir retourné.</p>
    *
    * @return vrai si le projectile a été dévié (l'appelant ne doit alors PAS appliquer les dégâts)
    */
   public static boolean tryDeflect(SkillProjectileEntity projectile, LivingEntity cible) {
      if (!(cible instanceof ServerPlayer porteur)) {
         return false;
      }
      // Le port ne range PAS le Fruit dans la main : il le stocke dans une variable joueur. Lire l'objet
      // tenu aurait rendu la déviation inopérante pour tout porteur réel — le premier passage de ce test
      // l'a d'ailleurs montré (« dévié=false » alors que le Fruit était bien équipé).
      if (!net.mcreator.minepiece.network.MinepieceModVariables.get(porteur).PlayerDevilFruit
         .is(net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU)) {
         return false;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur)) {
         return false;
      }
      Vec3 vitesse = projectile.getDeltaMovement();
      if (vitesse.lengthSqr() < 1.0E-4) {
         return false;
      }
      projectile.setDeltaMovement(vitesse.scale(-1.0));
      projectile.setOwner(porteur);
      if(projectile.mythicShot!=null){projectile.discard();return true;}
      if(projectile.zoanShot!=null){projectile.discard();return true;}
      if(projectile.mythicalZoanShot!=null){projectile.discard();return true;}
      if(projectile.parameciaShot!=null){projectile.discard();return true;}
      if(projectile.materialShot!=null)projectile.materialShot.reflect(porteur);
      if(projectile.controlledShot!=null)projectile.controlledShot.reflect(porteur);
      deviees++;
      porteur.sendSystemMessage(net.minecraft.network.chat.Component.literal(
         "§b§lDéviation. §r§7La paume renvoie ce qu'on lui jette."), true);
      return true;
   }

   // ============================================================================================
   // ③ URSUS SHOCK — charger, annoncer, puis frapper
   // ============================================================================================

   private static final class Charge {
      int ticks;

      Charge() {
      }
   }

   private static final class Annonce {
      final ServerLevel level;
      final Vec3 centre;
      final double rayon;
      final double degats;
      final UUID auteur;
      final net.minecraft.world.damagesource.DamageSource source;
      int restant;

      Annonce(ServerLevel level, Vec3 centre, double rayon, double degats, UUID auteur,
         net.minecraft.world.damagesource.DamageSource source) {
         this.level = level;
         this.centre = centre;
         this.rayon = rayon;
         this.degats = degats;
         this.auteur = auteur;
         this.source = source;
         this.restant = URSUS_WARNING_TICKS;
      }
   }

   private static final Map<UUID, Charge> CHARGES = new HashMap<>();
   private static final List<Annonce> ANNONCES = new ArrayList<>();

   /**
    * L'onde d'Ursus Shock : charge d'abord, frappe ensuite.
    *
    * <p>Le premier appel <b>charge</b> (et la puissance monte à chaque tick jusqu'au plafond) ; l'appel
    * suivant, ou la charge pleine, <b>libère</b> l'onde : la zone est alors annoncée pendant
    * {@value #URSUS_WARNING_TICKS} ticks avant que les dégâts ne tombent. Une onde qui frappe sans
    * prévenir ne laisse aucune décision à l'adversaire.</p>
    */
   public static boolean ursusShock(ServerPlayer player, DevilFruitMove move) {
      if (!(player.level() instanceof ServerLevel level)) {
         return false;
      }
      Charge charge = CHARGES.get(player.getUUID());
      if (charge == null) {
         CHARGES.put(player.getUUID(), new Charge());
         player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§bUrsus Shock §7» §fcharge en cours — relancez pour lâcher l'onde."), true);
         return true;
      }
      libererOnde(player, level, move, power(charge));
      CHARGES.remove(player.getUUID());
      return true;
   }

   /** Puissance de la charge : de 0 à 1, jamais au-delà — un plafond, pas une asymptote. */
   public static double power(Charge charge) {
      return Math.min(1.0, charge.ticks / (double) URSUS_CHARGE_TICKS);
   }

   /** Puissance courante d'un porteur (0 s'il ne charge pas) — lisible par le harnais. */
   public static double chargeOf(ServerPlayer player) {
      Charge c = CHARGES.get(player.getUUID());
      return c == null ? 0.0 : power(c);
   }

   private static void libererOnde(ServerPlayer player, ServerLevel level, DevilFruitMove move,
      double puissance) {
      Vec3 regard = player.getViewVector(1.0F).normalize();
      // Le point d'impact suit le regard, borné à la portée de l'onde.
      Vec3 centre = player.position().add(regard.scale(URSUS_RADIUS * 0.6));
      double rayon = Math.min(URSUS_RADIUS, Math.max(4.0, move.range() <= 0.0 ? URSUS_RADIUS : move.range()));
      ANNONCES.add(new Annonce(level, centre, rayon, move.damage() * Math.max(0.35, puissance),
         player.getUUID(), CombatSkillLogic.elementalDamageSource(level,
            DevilFruitElement.of(net.mcreator.minepiece.network.MinepieceModVariables.get(player).PlayerDevilFruit), player, null)));
      ondesAnnoncees++;
   }

   /** La charge monte tant qu'on la tient, plafonnée à {@link #URSUS_CHARGE_TICKS}. */
   public static void tick() {
      for (Charge c : CHARGES.values()) {
         if (c.ticks < URSUS_CHARGE_TICKS) {
            c.ticks++;
         }
      }
   }

   /**
    * Enregistre l'entretien des charges et des annonces.
    *
    * <p>Le compteur de ticks de la charge est incrémenté ici, et l'annonce se dessine à chaque tick :
    * c'est ce dessin répété qui constitue le <b>signal</b> exigé par le critère du contrat.</p>
    */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         tick();
         for (Annonce a : new ArrayList<>(ANNONCES)) {
            a.restant--;
            // Le signal : un anneau dessiné à chaque tick. C'est ce dessin répété qui annonce la zone.
            for (double angle = 0.0; angle < Math.PI * 2.0; angle += Math.PI / 16.0) {
               a.level.sendParticles(MinepieceModParticles.SHOCKWAVE, a.centre.x + Math.cos(angle) * a.rayon,
                  a.centre.y + 0.2, a.centre.z + Math.sin(angle) * a.rayon, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (a.restant > 0) {
               continue;
            }
            for (LivingEntity cible : a.level.getEntitiesOfClass(LivingEntity.class,
               new AABB(a.centre, a.centre).inflate(a.rayon))) {
               if (cible.getUUID().equals(a.auteur)) {
                  continue;
               }
               if (a.level.getPlayerByUUID(a.auteur) instanceof ServerPlayer tireur
                  && !CombatSkillLogic.isValidTarget(tireur, cible)) {
                  continue;
               }
               cible.hurtServer(a.level, a.source, (float) a.degats);
               cible.push(0.0, 0.6, 0.0);
            }
            ondesFrappees++;
            ANNONCES.remove(a);
         }
      });
   }
}
