package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Moteur d'explosion des Fruits (FRUIT-BOMU_BOMU-03/04/05).
 *
 * <p>Le plan refuse le moteur générique : « les déclarations actuelles utilisent le moteur générique
 * et ne prouvent pas cette mécanique spécifique ». Il demande de <b>différencier explosion de
 * contact, projectile et explosion corporelle</b>, chaque technique ayant « un point d'origine, rayon
 * et coût propres ». C'est ce que fait {@link Origin} + la table {@link #PROFILES}.</p>
 *
 * <p><b>Résistance du porteur (BOMU_BOMU-04).</b> Le porteur est épargné par les explosions
 * <b>qu'il a lui-même provoquées</b>, et rien de plus : l'exclusion vit dans {@link #blast} et n'est
 * pas un crochet global sur les dégâts. Aucune immunité générale n'est donc accordée par accident —
 * une TNT, un creeper ou le Fruit d'un autre joueur le blessent normalement, ce que la preuve
 * {@code D3} vérifie explicitement.</p>
 */
public final class DevilFruitExplosions {

   /**
    * Point d'origine d'une explosion — c'est la différenciation demandée par BOMU_BOMU-03.
    */
   public enum Origin {
      /** <b>Contact</b> : l'explosion naît là où le coup porte (poing, pied, arme). */
      CONTACT,
      /** <b>Aérienne</b> : l'explosion naît au-dessus, portée devant le tireur. */
      OVERHEAD,
      /** <b>Corporelle</b> : l'explosion est centrée sur le corps du porteur. */
      BODY,
      /** <b>Projectile</b> : l'explosion naît à l'impact du projectile. */
      PROJECTILE
   }

   /**
    * Profil d'une technique explosive : origine, rayon, et décalage du point d'origine.
    *
    * @param origin  type d'origine
    * @param radius  rayon de l'explosion, en blocs
    * @param forward distance devant les yeux pour les origines non corporelles
    * @param upward  élévation pour l'origine aérienne
    */
   private record Profile(Origin origin, double radius, double forward, double upward) {
   }

   private static final Map<String, Profile> PROFILES = new HashMap<>();

   static {
      // FRUIT-BOMU_BOMU-03 : trois origines distinctes, rayons et coûts propres.
      // (Le coût vient du registre, pas d'ici : Énergie et recharge y sont déjà déclarées.)
      PROFILES.put("punchbomb", new Profile(Origin.CONTACT, 3.0, 1.0, 0.0));
      PROFILES.put("kickbomb", new Profile(Origin.CONTACT, 3.5, 1.0, -0.6));
      PROFILES.put("bomboverhead", new Profile(Origin.OVERHEAD, 4.0, 3.0, 2.5));
      PROFILES.put("zenshinkibaku", new Profile(Origin.BODY, 6.0, 0.0, 0.0));
      PROFILES.put("nosefancycannon", new Profile(Origin.PROJECTILE, 2.5, 0.0, 0.0));
   }

   private DevilFruitExplosions() {
   }

   /** Vrai si cette technique produit une explosion gérée par ce moteur. */
   public static boolean isExplosive(String moveId) {
      return PROFILES.containsKey(moveId);
   }

   public static double radius(String moveId) {
      Profile profile = PROFILES.get(moveId);
      return profile == null ? 0.0 : profile.radius();
   }

   public static Origin origin(String moveId) {
      Profile profile = PROFILES.get(moveId);
      return profile == null ? null : profile.origin();
   }

   /**
    * Déclenche l'explosion d'une technique de contact, aérienne ou corporelle.
    *
    * <p>Pour une origine {@code CONTACT}, le point est cherché par lancer de rayon jusqu'à la portée
    * déclarée de la technique : l'explosion naît donc <b>là où le coup porte</b> (mur ou créature), et
    * non à une distance fixe. Si rien n'est touché, elle naît juste devant le porteur.</p>
    *
    * @return nombre de créatures effectivement touchées
    */
   public static int detonate(ServerPlayer player, DevilFruitMove move) {
      Profile profile = PROFILES.get(move.id());
      if (profile == null) {
         return 0;
      }
      Vec3 origin = resolveOrigin(player, move, profile);
      double damage = move.damage();
      int touched = blast(player, origin, profile.radius(), damage, move.particle());
      return touched;
   }

   /** Point d'origine concret, selon le type d'explosion. */
   public static Vec3 resolveOrigin(ServerPlayer player, DevilFruitMove move, Profile profile) {
      Vec3 eyes = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      return switch (profile.origin()) {
         case BODY -> new Vec3(player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ());
         case OVERHEAD -> eyes.add(look.scale(profile.forward())).add(0.0, profile.upward(), 0.0);
         case CONTACT -> contactPoint(player, move.range(), profile.forward());
         case PROJECTILE -> eyes.add(look.scale(profile.forward() + 1.0));
      };
   }

   /**
    * Point de contact : intersection du regard avec le premier bloc ou la première créature, sinon
    * juste devant le porteur.
    */
   private static Vec3 contactPoint(ServerPlayer player, double range, double fallbackForward) {
      if (!(player.level() instanceof ServerLevel level)) {
         return player.getEyePosition().add(player.getLookAngle().scale(fallbackForward));
      }
      Vec3 from = player.getEyePosition();
      Vec3 to = from.add(player.getLookAngle().scale(Math.max(range, fallbackForward)));
      HitResult blockHit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
         ClipContext.Fluid.NONE, player));
      if (blockHit.getType() == HitResult.Type.BLOCK) {
         LivingEntity nearest=firstAlongRay(player,from,blockHit.getLocation());
         return nearest==null?blockHit.getLocation():nearest.getBoundingBox().getCenter();
      }
      // Aucun bloc : une créature sur le trajet prend le contact.
      LivingEntity struck = firstAlongRay(player, from, to);
      if (struck != null) {
         return struck.position().add(0.0, struck.getBbHeight() * 0.5, 0.0);
      }
      return from.add(player.getLookAngle().scale(fallbackForward));
   }

   /** Première créature valide le long du segment, dans un tube de 1,2 bloc. */
   private static LivingEntity firstAlongRay(ServerPlayer player, Vec3 from, Vec3 to) {
      AABB tube = new AABB(from, to).inflate(1.2);
      List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, tube);
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;
      Vec3 direction = to.subtract(from);
      double length = direction.length();
      if (length < 1.0E-6) {
         return null;
      }
      Vec3 unit = direction.scale(1.0 / length);
      for (LivingEntity candidate : candidates) {
         if (!BatchRules.target(player,candidate)) {
            continue;
         }
         Vec3 offset = candidate.position().add(0.0, candidate.getBbHeight() * 0.5, 0.0).subtract(from);
         double along = offset.dot(unit);
         if (along < 0.0 || along > length) {
            continue;
         }
         double perpendicular = offset.subtract(unit.scale(along)).length();
         if (perpendicular <= 1.2 && along < bestDistance) {
            bestDistance = along;
            best = candidate;
         }
      }
      return best;
   }

   /**
    * Applique une explosion : dégâts dégressifs, recul, et destruction de blocs <b>selon les
    * réglages</b> de {@link DevilFruitExplosionConfig} (BOMU_BOMU-05).
    *
    * <p>Le porteur est exclu quand {@code owner-immune-to-own} est actif — exclusion locale à cette
    * méthode, donc limitée à ses propres explosions. Les alliés sont épargnés quand
    * {@code spare-allies} est actif. Les blocs ne sont touchés que si {@code block-damage} est actif
    * <b>et</b> que la position n'est pas protégée.</p>
    *
    * @return nombre de créatures touchées
    */
   public static int blast(ServerPlayer owner, Vec3 origin, double radius, double damage,
         ParticleOptions particle) {
      return blast(owner,origin,radius,damage,particle,true);
   }
   /** Controlled casts resolve terrain through TechniqueTerrain, never through the legacy toggle. */
   public static int damageOnly(ServerPlayer owner,Vec3 origin,double radius,double damage,ParticleOptions particle){return blast(owner,origin,radius,damage,particle,false);}
   private static int blast(ServerPlayer owner,Vec3 origin,double radius,double damage,ParticleOptions particle,boolean legacyTerrain){
      if (!(owner.level() instanceof ServerLevel level) || radius <= 0.0 || !owner.isAlive() || owner.isRemoved() || owner.isSpectator() || DevilFruitPowerLogic.isSuppressed(owner) || isProtectedPosition(level,BlockPos.containing(origin))) {
         return 0;
      }
      int touched = 0;

      AABB zone = new AABB(origin, origin).inflate(radius);
      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, zone)) {
         if (!wouldAffect(owner, target, origin, radius)) {
            continue;
         }
         double distance = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).distanceTo(origin);
         double falloff = 0.4 + 0.6 * (1.0 - distance / radius);
         var source = level.damageSources().explosion(owner, owner);
         if(!CombatSkillLogic.hurtAndConfirm(target,level,source,(float)(damage*falloff)))continue;
         Vec3 push = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(origin);
         if (push.lengthSqr() > 1.0E-6) {
            Vec3 unit = push.normalize();
            // Signature réelle : knockback(force, x, z, source, float). L'ancien appel passait la
            // direction en premier argument — donc la FORCE valait unit.x (souvent 0, parfois
            // négative) : le recul d'explosion variait selon l'orientation de la victime et
            // disparaissait à l'est/à l'ouest. Le vecteur est en outre retranché du mouvement, il va
            // de la cible vers l'origine de l'explosion : on passe l'opposé.
            target.knockback((float) (1.0 + radius * 0.15), -unit.x, -unit.z, source, 0.0F);
            target.push(0.0, 0.3, 0.0);
         }
         touched++;
      }

      if (legacyTerrain && DevilFruitExplosionConfig.blockDamage()) {
         breakBlocks(level, origin, radius);
      }

      level.sendParticles(ParticleTypes.SMOKE,origin.x,origin.y,origin.z,Math.min(24,(int)(radius*3)),radius*.3,radius*.3,radius*.3,.02);
      level.playSound(null, BlockPos.containing(origin), SoundEvents.GENERIC_EXPLODE.value(),
         SoundSource.PLAYERS, 1.0F, 1.0F);
      return touched;
   }

   /**
    * Position protégée contre une explosion du Fruit (BOMU_BOMU-05).
    *
    * <p>Deux sources, dans cet ordre : la <b>protection d'île existante</b>
    * ({@link br.com.oneminepiece.opworldlike.claim.ProtectedSkillEffectGuard}, celle qui protège déjà
    * les constructions des compétences destructrices) puis les zones déclarées par l'administrateur
    * dans la configuration. On réutilise la garde existante au lieu d'en écrire une seconde : deux
    * systèmes de protection en parallèle finiraient par diverger.</p>
    */
   public static boolean isProtectedPosition(ServerLevel level, BlockPos pos) {
      return br.com.oneminepiece.opworldlike.claim.ProtectedSkillEffectGuard.shouldBlockSkillEffect(level, pos)
         || DevilFruitExplosionConfig.isProtected(level, pos);
   }

   /**
    * Décision d'impact sur une créature — <b>le</b> prédicat utilisé par {@link #blast}, exposé pour
    * être vérifiable sans dépendre de la santé effective d'un joueur.
    *
    * <p>C'est ici, et nulle part ailleurs, que le porteur est épargné : {@code ownerImmuneToOwn}
    * n'accorde donc <b>aucune immunité générale</b>. Un porteur est épargné par l'explosion qu'il
    * provoque, mais une explosion provoquée par quelqu'un d'autre le touche normalement
    * (preuve {@code D3}).</p>
    */
   public static boolean wouldAffect(ServerPlayer owner, LivingEntity target, Vec3 origin, double radius) {
      if (radius <= 0.0 || target == null || !target.isAlive()) {
         return false;
      }
      if(target.level()!=owner.level() || target.getBoundingBox().getCenter().distanceTo(origin)>radius || isProtectedPosition((ServerLevel)owner.level(),target.blockPosition()))return false;
      var ray=((ServerLevel)owner.level()).clip(new ClipContext(origin,target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));
      if(ray.getType()!=HitResult.Type.MISS)return false;
      if (target == owner) {
         return !DevilFruitExplosionConfig.ownerImmuneToOwn();
      }
      if (DevilFruitExplosionConfig.spareAllies()) {
         // Deux notions d'allié, celles déjà utilisées ailleurs dans le mod (cf. SummonEntity.canAttack) :
         // les créatures non valides comme cible, et les membres de la même équipe de scoreboard.
         if (!CombatSkillLogic.isValidTarget(owner, target)) {
            return false;
         }
         var equipePorteur = owner.getTeam();
         var equipeCible = target.getTeam();
         if (equipePorteur != null && equipeCible != null && equipePorteur.isAlliedTo(equipeCible)) {
            return false;
         }
      }
      double distance = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).distanceTo(origin);
      return distance <= radius;
   }

   /** Résolution du point d'origine d'une technique, par son identifiant. */
   public static Vec3 resolveOriginFor(ServerPlayer player, DevilFruitMove move) {
      Profile profile = PROFILES.get(move.id());
      return profile == null ? null : resolveOrigin(player, move, profile);
   }

   /** Destruction de blocs bornée : jamais hors du rayon, jamais dans une zone protégée. */
   private static void breakBlocks(ServerLevel level, Vec3 origin, double radius) {
      int span = (int) Math.ceil(Math.min(radius, 6.0));
      BlockPos center = BlockPos.containing(origin);
      double limit = DevilFruitExplosionConfig.blockDamageResistanceLimit();
      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-span, -span, -span), center.offset(span, span, span))) {
         if (pos.distSqr(center) > radius * radius) {
            continue;
         }
         if (isProtectedPosition(level, pos)) {
            continue;
         }
         BlockState state = level.getBlockState(pos);
         if (!level.isLoaded(pos) || state.hasBlockEntity() || state.getDestroySpeed(level,pos)<0 || PowerBlockService.isTrackedAny(level,pos) || state.isAir() || state.getBlock().getExplosionResistance() > limit) {
            continue;
         }
         level.destroyBlock(pos, false);
      }
   }
}
