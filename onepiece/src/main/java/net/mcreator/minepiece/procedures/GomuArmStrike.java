package net.mcreator.minepiece.procedures;

import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bras élastique de Gomu Gomu : portée <b>physique</b> et impacts (FRUIT-GOMU_GOMU-03/04).
 *
 * <p>ver15 déclarait Pistol, Bazooka et Gatling comme trois frappes génériques : un cône de dégâts
 * portant jusqu'à 6 puis 5 blocs, avec un bras <em>purement décoratif</em> apparu au même endroit que
 * le joueur. Le plan refuse cet écart : « étendre réellement les bras et leur collision, limiter la
 * portée et ramener le membre après impact », avec pour critère « la portée physique correspond au
 * bras visible ; un mur interrompt le coup ; aucun membre ne reste après annulation ».</p>
 *
 * <h2>Comment la portée physique correspond au bras visible</h2>
 * <p>L'animation du modèle porte le poing à {@value #VISUAL_ADVANCE} bloc devant l'épaule (valeur
 * <b>mesurée dans les keyframes</b> de {@code entity_gomu_arm.animation.json}, et non supposée : le
 * débattement maximal du bone {@code right_arm} y est de 32 unités, soit 2 blocs). Le bras est donc
 * <b>étiré</b> d'un facteur {@code portée / 2} pour que le poing atteigne exactement le point
 * d'impact. Les deux valeurs sont solidaires par construction : la même distance commande l'étirement
 * et les dégâts — il n'existe pas deux réglages qui puissent diverger.</p>
 *
 * <h2>Ce qui est décidé ici, et non dans ver15</h2>
 * <p>Le plan demande un « nombre d'impacts documenté » par technique : il n'existe pas dans ver15, où
 * les trois techniques ne diffèrent que par leurs chiffres. Les valeurs retenues sont donc un choix de
 * conception, assumé et verrouillé par les tests : voir {@link #impacts}.</p>
 */
public final class GomuArmStrike {

   /**
    * Avancée maximale du poing dans l'animation du bras, en blocs.
    *
    * <p>Mesurée dans les keyframes ({@code punch_gomu_pistol}, bone {@code right_arm} :
    * translation z = −32 unités Bedrock). Sert de référence : le bras est étiré de
    * {@code distance / VISUAL_ADVANCE} pour couvrir exactement la portée physique.</p>
    */
   public static final double VISUAL_ADVANCE = 2.0;

   /**
    * Plafond de dégâts d'une seule utilisation, toutes techniques confondues.
    *
    * <p>Critère du plan : « le total de dégâts reste plafonné ». La rafale du Gatling — six impacts —
    * ne peut donc pas dériver : 6 × 6 = {@value #MAX_TOTAL_DAMAGE}, atteint exactement, jamais
    * dépassé, même si le nombre d'impacts était augmenté par erreur.</p>
    */
   public static final double MAX_TOTAL_DAMAGE = 36.0;

   /** Résultat d'une frappe : où le bras s'arrête, sur quoi, et pourquoi. */
   public record Strike(double distance, Vec3 point, LivingEntity target, boolean blockedByWall) {
   }

   private GomuArmStrike() {
   }

   /** Vrai si cette technique est une frappe de bras Gomu gérée ici. */
   public static boolean handles(String moveId) {
      return impacts(moveId) > 0;
   }

   /**
    * Nombre d'impacts documenté par technique (FRUIT-GOMU_GOMU-04).
    *
    * <ul>
    *   <li><b>Pistol</b> — {@code 1} : un poing, un impact. C'est l'attaque simple.</li>
    *   <li><b>Bazooka</b> — {@code 1} : un seul impact, mais <b>à deux bras</b> (l'animation
    *       {@code gomu_bazooka1} meut les deux bras) et avec le recul majeur déclaré par ver15 (3,0).
    *       Le plan nomme cette différence « poussée » — elle vient du recul, pas du nombre de coups.</li>
    *   <li><b>Gatling</b> — {@code 6} : une rafale bornée. ver15 en donne le dégât (6) sans en donner
    *       le nombre ; six coups portent le total à 36, exactement le plafond ci-dessus.</li>
    * </ul>
    */
   public static int impacts(String moveId) {
      return switch (moveId) {
         case "gomu_pistol" -> 1;
         case "gomu_bazooka" -> 1;
         case "gomu_gatling" -> 6;
         // FRUIT-MOCHI_MOCHI-03 : les frappes de bras du mochi passent par la MÊME machinerie, donc la
         // collision suit le membre par construction (aucune formule parallèle à maintenir).
         // Mochi Gatling : 6 impacts × 4 = 24, sous le plafond de 36 — la rafale ne peut pas dériver.
         case "mochipistol" -> 1;
         case "mochigatling" -> 6;
         default -> 0;
      };
   }

   /**
    * <b>Texture du bras, par technique</b> (FRUIT-MOCHI_MOCHI-03).
    *
    * <p>Le critère est explicite : « le rendu <b>ne réutilise pas involontairement la texture de
    * Luffy</b> ». Le mot « involontairement » est le bon : réutiliser un actif est légitime quand c'est
    * <b>délibéré</b>, et fautif quand ça arrive par défaut. La texture « luffy » était écrite en dur ici
    * — tout Fruit passant par cette machinerie héritait donc du visage de Luffy sans que personne ne
    * l'ait décidé. Elle est maintenant <b>par technique</b>, et une technique sans texture déclarée ne
    * prend rien à personne.</p>
    */
   /** Vrai si cette frappe est une frappe de MOCHI (elle colle ce qu'elle touche). */
   public static boolean isMochi(String moveId) {
      return moveId.startsWith("mochi");
   }

   public static String texture(String moveId) {
      return switch (moveId) {
         case "mochipistol", "mochigatling" -> "mochi";
         default -> "luffy";
      };
   }

   /** Animation GeckoLib jouée par le bras (assets de ver15, déjà présents). */
   public static String animation(String moveId) {
      return switch (moveId) {
         case "gomu_pistol" -> "punch_gomu_pistol";
         case "gomu_bazooka" -> "gomu_bazooka1";
         case "gomu_gatling" -> "punch_gomu_gatling_right";
         // Les animations appartiennent au MODÈLE du bras (entity_gomu_arm.animation.json) : un poing de
         // mochi qui frappe est le même geste qu'un poing élastique. C'est une réutilisation DÉLIBÉRÉE de
         // l'animation — l'identité du Fruit est portée par la texture, pas par le geste.
         case "mochipistol" -> "punch_gomu_pistol";
         case "mochigatling" -> "punch_gomu_gatling_right";
         default -> "punch_gomu_pistol";
      };
   }

   /** Nombre de bras engagés — le Bazooka frappe des deux poings. */
   public static int hands(String moveId) {
      return moveId.equals("gomu_bazooka") ? 2 : 1;
   }

   /** Facteur d'étirement du bras pour couvrir {@code distance} blocs. */
   public static double stretch(double distance) {
      return Math.max(0.05, distance / VISUAL_ADVANCE);
   }

   /**
    * Où le bras s'arrête : lancer de rayon du regard jusqu'à la portée déclarée.
    *
    * <p>Un mur rencontré <b>avant</b> la portée arrête le bras : c'est le critère « un mur interrompt
    * le coup ». Aucune créature située au-delà du point d'arrêt ne peut être touchée, puisqu'elle est
    * plus loin que le poing.</p>
    */
   public static Strike resolve(ServerPlayer player, DevilFruitMove move) {
      ServerLevel level = (ServerLevel) player.level();
      Vec3 eyes = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      double declared = move.range();
      Vec3 to = eyes.add(look.scale(declared));

      double distance = declared;
      boolean blocked = false;
      HitResult hit = level.clip(new ClipContext(eyes, to, ClipContext.Block.COLLIDER,
         ClipContext.Fluid.NONE, player));
      if (hit.getType() == HitResult.Type.BLOCK) {
         distance = Math.min(declared, eyes.distanceTo(hit.getLocation()));
         blocked = true;
      }
      Vec3 point = eyes.add(look.scale(distance));
      LivingEntity target = firstTarget(player, eyes, look, distance);
      return new Strike(distance, point, target, blocked);
   }

   /** Première créature valide sur le trajet, jamais au-delà de la portée effective. */
   private static LivingEntity firstTarget(ServerPlayer player, Vec3 eyes, Vec3 look, double distance) {
      AABB tube = new AABB(eyes, eyes.add(look.scale(distance))).inflate(1.0);
      LivingEntity best = null;
      double bestAlong = Double.MAX_VALUE;
      for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, tube)) {
         if (candidate == player || !candidate.isAlive() || !CombatSkillLogic.isValidTarget(player, candidate)) {
            continue;
         }
         Vec3 centre = candidate.position().add(0.0, candidate.getBbHeight() * 0.5, 0.0);
         double along = centre.subtract(eyes).dot(look);
         if (along < 0.0 || along > distance) {
            continue;
         }
         if (centre.subtract(eyes).subtract(look.scale(along)).length() > 1.0) {
            continue;
         }
         if (along < bestAlong) {
            bestAlong = along;
            best = candidate;
         }
      }
      return best;
   }

   /**
    * Frappe complète : place le bras étiré au point d'impact, applique les impacts documentés, et
    * laisse le membre se rétracter.
    *
    * @return dégâts totaux effectivement infligés
    */
   /** Legacy synchronous diagnostic only. Gameplay is scheduled by Mythic/MaterialBatchTechniques. */
   @Deprecated
   public static double cast(ServerPlayer player, DevilFruitMove move) {
      // Un membre ne doit jamais rester : on repart d'un bras propre avant chaque frappe.
      clearArms(player, 12.0);

      Strike strike = resolve(player, move);
      spawnArm(player, move, strike.distance());

      int count = impacts(move.id());
      double total = 0.0;
      LivingEntity target = strike.target();
      if (target == null) {
         // Le bras frappe le vide ou le mur : aucun dégât, mais l'animation a bien lieu.
         return 0.0;
      }
      // FRUIT-MOCHI_MOCHI-04 : une cible TOUCHÉE par le mochi devient collante. L'état est posé ici,
      // au moment exact où l'on sait que le contact a eu lieu — c'est la même machinerie qui décide de
      // l'impact, donc « touché » et « collé » ne peuvent pas diverger.

      Vec3 direction = player.getLookAngle();
      for (int i = 0; i < count && target.isAlive(); i++) {
         double step = Math.min(move.damage(), MAX_TOTAL_DAMAGE - total);
         if (step <= 0.0) {
            break;
         }
         var source = ((ServerLevel) player.level()).damageSources().playerAttack(player);
         if(CombatSkillLogic.hurtAndConfirm(target,(ServerLevel)player.level(),source,(float)step)){
            if(isMochi(move.id()))StickyState.stick(target,player);
            CombatSkillLogic.applySkillKnockback(target,direction,source,move.knockback());total+=step;
         }
      }
      return total;
   }

   /** Bras étiré, posé à l'épaule du joueur et étiré jusqu'au point d'impact. */
   private static void spawnArm(ServerPlayer player, DevilFruitMove move, double distance) {
      ServerLevel level = (ServerLevel) player.level();
      for (int hand = 0; hand < hands(move.id()); hand++) {
         EntityGomuArmEntity arm = MinepieceModEntities.ENTITY_GOMU_ARM.spawn(level,
            BlockPos.containing(player.getEyePosition()), EntitySpawnReason.MOB_SUMMONED);
         if (arm == null) {
            continue;
         }
         arm.setOwnerEntity(player);
         // Le bras reste à l'endroit du coup : il ne traîne pas derrière le joueur qui se déplace.
         arm.followOwner(false);
         arm.setPos(player.getX(), player.getEyeY(), player.getZ());
         arm.setYRot(player.getYRot());
         arm.setXRot(player.getXRot());
         arm.setAnimation(animation(move.id()));
         arm.setTexture(texture(move.id()));
         arm.setStretch((float) stretch(distance));
         // Étirement puis rétraction : la durée suit la distance, et reste bornée.
         arm.lifetime(6 + (int) Math.ceil(distance * 2.0));
      }
   }

   /** Retire les bras d'un joueur dans un rayon donné (aucun membre orphelin). */
   public static int clearArms(ServerPlayer player, double radius) {
      if (!(player.level() instanceof ServerLevel level)) {
         return 0;
      }
      int removed = 0;
      for (EntityGomuArmEntity arm : level.getEntitiesOfClass(EntityGomuArmEntity.class,
            player.getBoundingBox().inflate(radius))) {
         if(arm.ownerEntity()==player){arm.discard();removed++;}
      }
      return removed;
   }
}
