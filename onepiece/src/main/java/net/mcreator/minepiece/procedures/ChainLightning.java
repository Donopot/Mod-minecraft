package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Éclair à <b>rebonds bornés</b> (FRUIT-GORO_GORO-03).
 *
 * <p>Le plan : « créer des éclairs avec rebonds limités entre cibles autorisées », critère
 * « nombre de rebonds, portée et dégâts correspondent au contrat et <b>excluent les alliés</b> ».</p>
 *
 * <p>ver15 déclarait Sango comme une frappe unique de 18 dégâts dans un cône : rien n'y produisait de
 * rebond, et rien n'y bornait un enchaînement. Les trois valeurs du contrat — nombre de rebonds,
 * portée de rebond, atténuation — sont donc des <b>décisions documentées</b>, pas des recopies, et
 * elles sont verrouillées par les preuves du contrat.</p>
 *
 * <p>Deux invariants, qui sont le cœur du critère :</p>
 * <ul>
 *   <li>chaque saut ne peut viser qu'une cible <b>autorisée</b> ({@link CombatSkillLogic#isValidTarget})
 *       — un allié ou le porteur n'est jamais dans la chaîne, même s'il est le plus proche ;</li>
 *   <li>une cible déjà frappée n'est <b>jamais</b> reprise : la chaîne ne tourne pas en boucle.</li>
 * </ul>
 */
public final class ChainLightning {

   /** Rebonds après l'impact initial — donc 4 cibles au maximum. */
   public static final int MAX_BOUNCES = 3;

   /** Distance maximale d'un saut d'une cible à la suivante. */
   public static final double BOUNCE_RANGE = 6.0;

   /** Atténuation appliquée à chaque saut : 18,0 → 13,5 → 10,1 → 7,6. */
   public static final double FALLOFF = 0.75;

   /** Résultat d'un enchaînement : les cibles atteintes, dans l'ordre, et les dégâts de chacune. */
   public record Chain(List<LivingEntity> targets, List<Double> damages) {

      public int bounces() {
         return Math.max(0, targets.size() - 1);
      }

      public double total() {
         return damages.stream().mapToDouble(Double::doubleValue).sum();
      }
   }

   private ChainLightning() {
   }

   /** Dégâts du n-ième maillon (0 = impact initial). */
   public static double damageAt(DevilFruitMove move, int hop) {
      return move.damage() * Math.pow(FALLOFF, hop);
   }

   /**
    * Construit la chaîne sans l'appliquer : première cible dans la portée déclarée de la technique,
    * puis rebonds de proche en proche.
    */
   public static Chain resolve(ServerPlayer player, DevilFruitMove move) {
      List<LivingEntity> targets = new ArrayList<>();
      List<Double> damages = new ArrayList<>();

      LivingEntity first = nearest(player, player, move.range(), null, List.of());
      if (first == null) {
         return new Chain(targets, damages);
      }
      targets.add(first);
      damages.add(damageAt(move, 0));

      for (int hop = 1; hop <= MAX_BOUNCES; hop++) {
         LivingEntity previous = targets.get(targets.size() - 1);
         LivingEntity next = nearest(player, previous, BOUNCE_RANGE, player, targets);
         if (next == null) {
            break;
         }
         targets.add(next);
         damages.add(damageAt(move, hop));
      }
      return new Chain(targets, damages);
   }

   /** Applique la chaîne : chaque maillon est frappé et marqué visuellement. */
   public static Chain cast(ServerPlayer player, DevilFruitMove move) {
      return cast(player,move,TechniqueTerrain.begin(player,move));
   }
   public static Chain cast(ServerPlayer player,DevilFruitMove move,TechniqueTerrain.Budget terrain) {
      ServerLevel level = (ServerLevel) player.level();
      List<LivingEntity> applied = new ArrayList<>();
      List<Double> appliedDamage = new ArrayList<>();
      List<LivingEntity> attempted=new ArrayList<>();LivingEntity origin=player;
      var source = CombatSkillLogic.elementalDamageSource(level, ElementType.LIGHTNING, player, null);
      while(applied.size()<=MAX_BOUNCES && attempted.size()<32) {
         LivingEntity target=nearest(player,origin,applied.isEmpty()?move.range():BOUNCE_RANGE,player,attempted);
         if(target==null)break;attempted.add(target);double damage=damageAt(move,applied.size());
         // A refused target never extends the path or consumes an accepted hop.
         if (!CombatSkillLogic.hurtAndConfirm(target, level, source, (float)damage)) continue;
         Vec3 start=applied.isEmpty()?player.getEyePosition():origin.getBoundingBox().getCenter();
         GoroTechniques.link(player,start,target.getBoundingBox().getCenter(),"goro_arc",10);
         CombatSkillLogic.applyElementalEffect(target, ElementType.LIGHTNING);
         if(move.effect()!=null)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(move.effect(),move.effectDuration(),move.effectAmplifier()));
         spark(level, target.position());terrain.impact(target.position().add(0,.2,0),1,4);
         applied.add(target); appliedDamage.add(damage);origin=target;
      }
      return new Chain(applied, appliedDamage);
   }

   /** Cible autorisée la plus proche d'un point, jamais le porteur ni une cible déjà prise. */
   private static LivingEntity nearest(ServerPlayer player, LivingEntity origin, double radius,
         ServerPlayer excluded, List<LivingEntity> alreadyHit) {
      Vec3 from = origin.position();
      AABB zone = new AABB(from, from).inflate(radius);
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;
      for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, zone)) {
         if (candidate == player || candidate == excluded || !candidate.isAlive()) {
            continue;
         }
         if (alreadyHit.contains(candidate)) {
            continue;
         }
         // Un allié n'est jamais un maillon, même s'il est le plus proche.
         if (!CombatSkillLogic.isValidTarget(player, candidate)) {
            continue;
         }
         if (!origin.hasLineOfSight(candidate)) continue;
         if(DevilFruitExplosions.isProtectedPosition((ServerLevel)player.level(),candidate.blockPosition()))continue;
         double distance = candidate.position().distanceTo(from);
         if (distance <= radius && distance < bestDistance) {
            bestDistance = distance;
            best = candidate;
         }
      }
      return best;
   }

   private static void spark(ServerLevel level, Vec3 point) {
      level.sendParticles(MinepieceModParticles.THUNDER_BLUE,
         point.x, point.y + 1.0, point.z, 30, 0.3, 0.5, 0.3, 0.2);
   }
}
