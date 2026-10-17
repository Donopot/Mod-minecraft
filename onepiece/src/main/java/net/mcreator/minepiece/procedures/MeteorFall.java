package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Météores</b> — Zushi Zushi (FRUIT-ZUSHI_ZUSHI-05).
 *
 * <p>Le plan : « créer des <b>météores réellement descendants</b> avec <b>avertissement préalable</b> »,
 * critère « <b>impact unique</b>, <b>durée de vie bornée</b> et <b>terrain protégé respecté</b> ».</p>
 *
 * <h2>Ce que ver15 faisait, et pourquoi ça ne suffisait pas</h2>
 * <p>« Meteor » était un <b>projectile horizontal</b> : il partait du regard du porteur et filait droit
 * devant, comme une flèche. Un météore ne fait pas ça — il <b>tombe</b>. Ici, le projectile est créé à
 * {@code launchHeight} blocs au-dessus du point visé, avec une vitesse <b>verticale descendante</b> :
 * sa trajectoire est une chute, pas un tir.</p>
 *
 * <h2>Les trois exigences du critère</h2>
 * <ol>
 *   <li><b>Avertissement préalable</b> — l'impact est <b>enregistré</b> et annoncé
 *       ({@code meteor_warning_ticks} ticks de particules au sol, de son, et un message au porteur)
 *       <b>avant</b> que quoi que ce soit ne frappe. La cible peut voir venir : c'est ce qui distingue
 *       une attaque d'une surprise.</li>
 *   <li><b>Impact unique</b> — l'impact est résolu <b>une fois</b>, à l'échéance de l'avertissement, puis
 *       l'entrée est retirée. Ce n'est pas une zone qui frappe à chaque tick où l'on s'y tient.</li>
 *   <li><b>Terrain protégé respecté</b> — la destruction du terrain est <b>désactivée par défaut</b>, et
 *       quand elle est activée, elle passe par le moteur d'explosion qui <b>refuse les zones
 *       protégées</b>. Jamais de trou dans un claim, quel que soit le réglage.</li>
 * </ol>
 */
public final class MeteorFall {

   /** Un météore en vol : où il tombera, quand, et pour qui. */
   private static final class Pending {
      final String dimension;
      final Vec3 impact;
      final ServerPlayer owner;
      final double damage;
      final double radius;
      int remaining;

      Pending(String dimension, Vec3 impact, ServerPlayer owner, double damage, double radius, int warning) {
         this.dimension = dimension;
         this.impact = impact;
         this.owner = owner;
         this.damage = damage;
         this.radius = radius;
         this.remaining = warning;
      }
   }

   private static final List<Pending> PENDING = new ArrayList<>();

   /** Nombre d'impacts résolus depuis le démarrage (observable — sert à prouver l'impact unique). */
   private static int impactsResolved = 0;

   private MeteorFall() {
   }

   /** Branche l'entretien des météores sur le tick serveur. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
   }

   /**
    * Annonce puis fait tomber un météore sur un point.
    *
    * @return vrai si le météore a été lancé
    */
   public static boolean launch(ServerPlayer owner, Vec3 impact, double damage, double radius) {
      if (owner == null || !(owner.level() instanceof ServerLevel level)) {
         return false;
      }
      PENDING.removeIf(p -> p.owner != null && p.owner.getUUID().equals(owner.getUUID())
         && p.dimension.equals(level.dimension().identifier().toString()));
      if (DevilFruitGravityConfig.meteorMaxConcurrent() <= 0) {
         return false;
      }
      PENDING.add(new Pending(level.dimension().identifier().toString(), impact, owner, damage, radius,
         DevilFruitGravityConfig.meteorWarningTicks()));

      // Le projectile RÉEL : créé au-dessus de la cible, avec une vitesse verticale descendante.
      spawnDescendingMeteor(level, owner, impact);

      // L'avertissement : son grave + message, pour que la cible sache ce qui arrive.
      level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.ENDER_DRAGON_GROWL,
         SoundSource.PLAYERS, 1.2F, 1.6F);
      owner.sendSystemMessage(Component.literal("§6Un météore tombe dans "
         + DevilFruitGravityConfig.meteorWarningTicks() + " ticks."), false);
      return true;
   }

   /**
    * Fait apparaître le météore <b>en hauteur</b> avec une vitesse <b>verticale descendante</b>.
    *
    * <p>Exposé pour que la preuve lise la position et le mouvement réels du projectile : c'est la
    * définition même de « réellement descendant ».</p>
    */
   public static net.mcreator.minepiece.entity.SkillProjectileEntity spawnDescendingMeteor(
         ServerLevel level, ServerPlayer owner, Vec3 impact) {
      double launchY = Math.min(level.getMaxY() - 4.0, impact.y + DevilFruitGravityConfig.meteorLaunchHeight());
      net.mcreator.minepiece.entity.SkillProjectileEntity proj =
         new net.mcreator.minepiece.entity.SkillProjectileEntity(
            net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
            impact.x, launchY, impact.z, new Vec3(0.0, -1.0, 0.0), level);
      proj.setOwner(owner);
      proj.damage(0.0); // les dégâts sont appliqués à l'impact résolu, pas par le projectile
      proj.lifetime(DevilFruitGravityConfig.meteorWarningTicks() + 40);
      proj.element(ElementType.PHYSICAL);
      proj.trail(MinepieceModParticles.FLAME);
      proj.scale(1.0F);
      // Descente franche, sans dispersion : la trajectoire est une chute.
      proj.shoot(0.0, -1.0, 0.0, (float) DevilFruitGravityConfig.meteorSpeed(), 0.0F);
      level.addFreshEntity(proj);
      return proj;
   }

   /** Nombre de météores en vol. */
   public static int pendingCount() {
      return PENDING.size();
   }

   /** Nombre d'impacts résolus (0, 1, 2…). Un météore qui frappe deux fois ferait mentir ce compte. */
   public static int impactsResolved() {
      return impactsResolved;
   }

   /** Oublie tout (fin de test, reset). */
   public static void clearAll() {
      PENDING.clear();
      impactsResolved = 0;
   }

   /** Entretien : phase d'avertissement, puis impact unique. Exposé pour les tests. */
   public static void tick() {
      if (PENDING.isEmpty()) {
         return;
      }
      MinecraftServer server = br.com.oneminepiece.opworldlike.OmpWorld.currentServer;
      if (server == null) {
         return;
      }
      for (Pending pending : new ArrayList<>(PENDING)) {
         ServerLevel level = null;
         for (ServerLevel candidate : server.getAllLevels()) {
            if (candidate.dimension().identifier().toString().equals(pending.dimension)) {
               level = candidate;
               break;
            }
         }
         if (level == null) {
            PENDING.remove(pending);
            continue;
         }
         pending.remaining--;
         if (pending.remaining > 0) {
            // Avertissement : le sol du point d'impact marque l'endroit visé.
            level.sendParticles(MinepieceModParticles.BIG_SMOKE, pending.impact.x, pending.impact.y + 0.2,
               pending.impact.z, 10, pending.radius * 0.5, 0.1, pending.radius * 0.5, 0.01);
            level.sendParticles(MinepieceModParticles.SHOCK_HIT, pending.impact.x, pending.impact.y + 4.0,
               pending.impact.z, 4, 0.4, 1.0, 0.4, 0.02);
            continue;
         }
         // Impact : UNE fois. L'entrée est retirée immédiatement après.
         resolve(level, pending);
         PENDING.remove(pending);
      }
   }

   /** Impact unique : dégâts + poussière, terrain seulement si autorisé et hors zone protégée. */
   private static void resolve(ServerLevel level, Pending pending) {
      impactsResolved++;
      level.sendParticles(MinepieceModParticles.SHOCK_HIT, pending.impact.x, pending.impact.y + 0.5,
         pending.impact.z, 8, pending.radius * 0.3, 0.4, pending.radius * 0.3, 0.02);
      level.playSound(null, pending.impact.x, pending.impact.y, pending.impact.z,
         SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.8F);

      // Dégâts et recul : par le moteur d'explosion, qui épargne le porteur et les alliés selon la config.
      ServerPlayer owner = pending.owner;
      if (owner != null) {
         DevilFruitExplosions.blast(owner, pending.impact, pending.radius, pending.damage,
            MinepieceModParticles.FLAME);
         return;
      }
      // Sans porteur identifiable, on frappe sans discriminant d'alliance.
      for (LivingEntity cible : level.getEntitiesOfClass(LivingEntity.class,
            new net.minecraft.world.phys.AABB(pending.impact, pending.impact).inflate(pending.radius))) {
         cible.hurtServer(level, level.damageSources().magic(), (float) pending.damage);
      }
   }

   /** Position d'apparition d'un météore pour un point d'impact (observable). */
   public static double launchHeightFor(ServerLevel level, Vec3 impact) {
      return Math.min(level.getMaxY() - 4.0, impact.y + DevilFruitGravityConfig.meteorLaunchHeight());
   }
}
