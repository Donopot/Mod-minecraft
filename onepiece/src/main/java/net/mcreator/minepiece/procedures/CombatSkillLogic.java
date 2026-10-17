package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import java.util.List;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Moteur de combat générique de la couche pouvoir — port simplifié du cœur ver15
 * ({@code RangeAttackProcedure} + {@code AttackStrongPunchProcedure}). Primitives :
 * mêlée (cône), projectile (ligne droite), effet de zone et buff. Tous les skills de Fruit du Démon
 * s'appuient sur ce moteur ; seuls les valeurs/entités/particules changent.
 *
 * <p>CORE-012 : chaque coup utilise une source de dégâts élémentaire (feu, glace, foudre, explosion)
 * et applique l'effet correspondant (brûlure, gel, étincelles), au lieu d'un simple coup physique.</p>
 */
public final class CombatSkillLogic {

   /** Cosinus du demi-angle du cône de mêlée (~90°, cos 45°). Public : les techniques qui portent
    * « devant » (morsure du loup, souffle) doivent partager LA définition du cône, pas la recopier. */
   public static final double MELEE_CONE_COS = 0.707;

   private CombatSkillLogic() {
   }

   /**
    * Politique de ciblage centralisée (CORE-010) : une cible n'est hostile que si elle est vivante,
    * n'est pas le lanceur, n'est pas un joueur protégé (PvP désactivé / même équipe) et n'est pas un
    * animal apprivoisé du lanceur. Appliquée avant dégâts, recul et effets.
    */
   public static boolean isValidTarget(Entity attacker, Entity target) {
      if (target instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity) return false;
      if (!(target instanceof LivingEntity living)) {
         return false;
      }
      if (!living.isAlive()) {
         return false;
      }
      if (target == attacker) {
         return false;
      }
      // PvP désactivé ou même équipe → joueurs intouchables entre eux.
      if (attacker instanceof Player me && target instanceof Player other && !me.canHarmPlayer(other)) {
         return false;
      }
      // Animal apprivoisé du lanceur (chien, chat, …) protégé.
      if (attacker != null && target instanceof OwnableEntity ownable && ownable.getOwner() == attacker) {
         return false;
      }
      return true;
   }

   /**
    * Source de dégâts adaptée à l'élément (CORE-012). Les sources élémentaires (feu/glace/foudre/
    * explosion) sont non-physiques : l'intangibilité Logia ne les bloque pas comme un coup physique.
    *
    * @param level      niveau serveur (pour les sources ambiantes)
    * @param element    élément du coup
    * @param attacker   responsable du coup (le joueur)
    * @param projectile entité projectile, ou {@code null} pour une mêlée
    */
   public static DamageSource elementalDamageSource(ServerLevel level, ElementType element, Entity attacker, Entity projectile) {
      DamageSource typed = switch (element) {
         case FIRE -> level.damageSources().onFire();
         case MAGMA -> level.damageSources().lava();
         case ICE -> level.damageSources().freeze();
         case LIGHTNING -> level.damageSources().lightningBolt();
         case EXPLOSIVE -> level.damageSources().explosion(projectile != null ? projectile : attacker, attacker);
         case PHYSICAL -> attacker instanceof Player p
            ? p.damageSources().playerAttack(p)
            : attacker instanceof LivingEntity le ? level.damageSources().mobAttack(le) : level.damageSources().generic();
         default -> level.damageSources().indirectMagic(projectile != null ? projectile : attacker, attacker);
      };
      // Preserve vanilla damage types/tags while retaining both the direct projectile and its owner.
      // Ambient onFire/lava/freeze/lightningBolt sources alone lose PvP, kill and Haki attribution.
      return attacker == null && projectile == null ? typed
         : new DamageSource(typed.typeHolder(), projectile != null ? projectile : attacker, attacker);
   }

   /**
    * Offensive follow-ups require an accepted hit that actually consumes health or absorption.
    * Do not evaluate computeDamageMagnification here: Yami absorption changes state when evaluated.
    * Explicit status-only area powers continue to use their independent effect path.
    */
   public static boolean hurtAndConfirm(LivingEntity target, ServerLevel level, DamageSource source, float damage) {
      if (!(damage > 0.0F) || !Float.isFinite(damage)) return false;
      double before = (double) target.getHealth() + target.getAbsorptionAmount();
      boolean accepted = target.hurtServer(level, source, damage);
      double after = (double) target.getHealth() + target.getAbsorptionAmount();
      return CombatHitPolicy.confirmed(accepted, before, after);
   }

   /** Native API order begins strength, opposite horizontal X/Z, then damage context. */
   public static void applySkillKnockback(LivingEntity target, Vec3 outward, DamageSource source, double strength) {
      if (!(strength > 0.0) || !Double.isFinite(strength)) return;
      Vec3 horizontal = new Vec3(outward.x, 0.0, outward.z);
      if (horizontal.lengthSqr() < 1.0E-8) return;
      horizontal = horizontal.normalize();
      target.knockback(strength, -horizontal.x, -horizontal.z, source, 0.0F);
   }

   /** Coup de mêlée (cône) sans effet de statut. */
   public static int meleeStrike(ServerPlayer player, double damage, double range, double knockback) {
      return meleeStrike(player, damage, range, knockback, null, 0, 0);
   }

   /**
    * Portée effective d'une technique pour une entité : proportionnelle à son échelle réelle.
    *
    * <p>Propriétaire unique de la règle — les formes agrandies (Daibutsu, girafe,ptéranodon…) n'ont pas
    * chacune leur formule, elles héritent de celle-ci.</p>
    */
   public static double effectiveRange(LivingEntity entity, double baseRange) {
      if (entity == null) {
         return baseRange;
      }
      double scale = entity.getScale();
      return scale <= 0.0 ? baseRange : baseRange * scale;
   }

   /**
    * <b>Sélection d'une frappe de mêlée : qui est touché, et une seule fois.</b>
    *
    * <p>Règle unique du mod : cône ({@link #MELEE_CONE_COS}), cible valide ({@link #isValidTarget} —
    * équipes et PvP respectés), portée effective selon l'échelle du corps, et <b>ligne de vue
    * dégagée</b> (un obstacle protège). Les dégâts ne sont comptés que si le moteur les a réellement
    * appliqués ({@code hurtServer} renvoie faux sur une cible invulnérable) — un compteur qui ignore ce
    * retour annonce des impacts qui n'ont rien fait.</p>
    *
    * <p>Elle existe parce que <b>deuxième</b> fruit à avoir besoin d'une frappe dont il connaît les
    * cibles : Pika Pika pour ajouter un bonus borné, Yami Yami pour poser un scellement. Chaque fruit
    * apporte désormais sa <b>conséquence</b>, mais la question « qui est touché » n'a qu'un
    * propriétaire — une seconde copie de cette règle aurait fini par diverger sur la ligne de vue ou
    * sur les équipes.</p>
    *
    * @return les cibles réellement blessées, dans l'ordre de rencontre
    */
   public static List<LivingEntity> strikeTargets(ServerPlayer player, ItemStack fruit, DevilFruitMove move,
                                                  double bonus) {
      List<LivingEntity> touches = new java.util.ArrayList<>();
      if (!(player.level() instanceof ServerLevel level) || move == null) {
         return touches;
      }
      double portee = effectiveRange(player, move.range());
      Vec3 regard = player.getLookAngle();
      Vec3 yeux = player.getEyePosition();
      ElementType element = DevilFruitElement.forTechnique(fruit, move.id());
      DamageSource source = elementalDamageSource(level, element, player, null);

      List<Entity> candidats = level.getEntities(player, player.getBoundingBox().inflate(portee),
         e -> isValidTarget(player, e));

      for (Entity e : candidats) {
         if (!(e instanceof LivingEntity cible)) {
            continue;
         }
         Vec3 vers = cible.position().subtract(yeux);
         if (vers.lengthSqr() < 1.0E-6) {
            continue;
         }
         if (vers.normalize().dot(regard) <= MELEE_CONE_COS) {
            continue;
         }
         if (vers.lengthSqr() > (portee + 1.0) * (portee + 1.0)) {
            continue;
         }
         if (!player.hasLineOfSight(cible)) {
            continue; // à couvert : l'obstacle protège
         }
         if (hurtAndConfirm(cible, level, source, (float) (move.damage() + bonus))) {
            Vec3 poussee = cible.position().subtract(player.position()).normalize();
            // Ordre des arguments vérifié : (force, x, z, source, hauteur) — le vecteur est SOUSTRAIT
            // du déplacement, donc on passe l'OPPOSÉ de la poussée voulue.
            applySkillKnockback(cible, poussee, source, move.knockback());
            touches.add(cible);
         }
      }
      return touches;
   }

   /** Coup de mêlée (cône) avec effet de statut optionnel sur les cibles. */
   public static int meleeStrike(ServerPlayer player, double damage, double range, double knockback,
                                 Holder<MobEffect> effect, int effectDuration, int effectAmplifier) {
      ServerLevel level = (ServerLevel) player.level();
      Vec3 look = player.getLookAngle();
      Vec3 eye = player.getEyePosition();
      // COHÉRENCE DE PORTÉE (FRUIT-HITO_HITO_DAIBUTSU-03) : la portée suit le corps. Une forme
      // agrandie frappe plus loin — sinon le poing visible passerait le mur sans rien toucher.
      // Une seule formule, pour toutes les formes agrandies, Daibutsu compris.
      range = effectiveRange(player, range);
      AABB box = player.getBoundingBox().inflate(range);
      ElementType element = elementFor(player);
      DamageSource source = elementalDamageSource(level, element, player, null);

      List<Entity> entities = level.getEntities(player, box, e -> isValidTarget(player, e));

      int hit = 0;
      for (Entity e : entities) {
         LivingEntity target = (LivingEntity) e;
         Vec3 toTarget = target.position().subtract(eye);
         Vec3 toTargetNorm = toTarget.normalize();
         if (toTargetNorm.dot(look) > MELEE_CONE_COS && toTarget.lengthSqr() <= (range + 1.0) * (range + 1.0)
               && player.hasLineOfSight(target)) {
            // A declared zero-damage status is independent (e.g. Phoenix Attack Speed), not a hit.
            // It neither knocks back nor adds an automatic elemental consequence.
            if (damage == 0.0 && effect != null) {
               if (!target.isInvulnerableTo(level, source)
                     && target.addEffect(new MobEffectInstance(effect, effectDuration, effectAmplifier))) hit++;
               continue;
            }
            if (!hurtAndConfirm(target, level, source, (float) damage)) continue;
            applySkillKnockback(target, target.position().subtract(player.position()), source, knockback);
            applyElementalEffect(target, element, player);
            if (effect != null) {
               target.addEffect(new MobEffectInstance(effect, effectDuration, effectAmplifier));
            }
            hit++;
         }
      }

      level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8F, 1.0F);
      return hit;
   }

   /** Projectile (sans effet de statut), vitesse par défaut. */
   public static SkillProjectileEntity fireProjectile(ServerPlayer player, double damage, double knockback, int lifetimeTicks) {
      return fireProjectile(player, damage, knockback, null, 0, 0, lifetimeTicks,
         DevilFruitMove.DEFAULT_PROJECTILE_SPEED, null, null, null);
   }

   /** Projectile avec effet de statut optionnel à l'impact, vitesse par défaut. */
   public static SkillProjectileEntity fireProjectile(ServerPlayer player, double damage, double knockback,
                                                      Holder<MobEffect> effect, int effectDuration, int effectAmplifier,
                                                      int lifetimeTicks) {
      return fireProjectile(player, damage, knockback, effect, effectDuration, effectAmplifier, lifetimeTicks,
         DevilFruitMove.DEFAULT_PROJECTILE_SPEED, null, null, null);
   }

   /** Projectile complet : effet + modèle/texture GeckoLib dynamiques + particule de traînée/impact + vitesse. */
   public static SkillProjectileEntity fireProjectile(ServerPlayer player, double damage, double knockback,
                                                      Holder<MobEffect> effect, int effectDuration, int effectAmplifier,
                                                      int lifetimeTicks, double speed, String modelId, String textureId,
                                                      ParticleOptions trailParticle) {
      ServerLevel level = (ServerLevel) player.level();
      Vec3 look = player.getLookAngle();
      Vec3 eye = player.getEyePosition();

      SkillProjectileEntity proj = new SkillProjectileEntity(
         MinepieceModEntities.SKILL_PROJECTILE, eye.x, eye.y, eye.z, look, level);
      proj.setOwner(player);
      proj.visualProfile(CastContext.currentId());
      proj.damage(damage).knockback(knockback).lifetime(lifetimeTicks);
      proj.element(elementFor(player));
      if (elementFor(player) == ElementType.GHOST) {
         // Traversée des surfaces + durée de vie bornée propre aux fantômes.
         proj.phases(true);
         proj.lifetime(DevilFruitHollowConfig.ghostLifetimeTicks());
      }
      // Échelle du modèle : grossit les petits projectiles (poing, slash…), laisse les gros (météore) à 1×.
      proj.scale("ball_maximum".equals(modelId) ? 1.0F : 2.5F);
      if (effect != null) {
         proj.effect(effect, effectDuration, effectAmplifier);
      }
      if (modelId != null) {
         proj.model(modelId);
      }
      if (textureId != null) {
         proj.texture(textureId);
      }
      if (trailParticle != null) {
         proj.trail(trailParticle);
      }
      proj.shoot(look.x, look.y, look.z, (float) speed, 0.0F);
      level.addFreshEntity(proj);
      return proj;
   }

   /**
    * Projectile complet <b>avec portée explicite en blocs</b> (0 = illimitée).
    *
    * <p>Surcharge plutôt que modification : les quelque deux cents autres projectiles du mod gardent
    * leur comportement, et un seul fruit change de règle — celui dont le contrat le demande.</p>
    */
   public static SkillProjectileEntity fireProjectile(ServerPlayer player, double damage, double knockback,
                                                      Holder<MobEffect> effect, int effectDuration, int effectAmplifier,
                                                      int lifetimeTicks, double speed, String modelId, String textureId,
                                                      ParticleOptions trailParticle, double rangeBlocks) {
      SkillProjectileEntity proj = fireProjectile(player, damage, knockback, effect, effectDuration,
         effectAmplifier, lifetimeTicks, speed, modelId, textureId, trailParticle);
      if (rangeBlocks > 0.0) {
         proj.range(rangeBlocks);
      }
      return proj;
   }

   /** Effet de zone (rayon) sur les ennemis autour du joueur. */
   public static int areaEffect(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier, double range) {
      ServerLevel level = (ServerLevel) player.level();
      AABB box = player.getBoundingBox().inflate(range);
      ElementType element = elementFor(player);
      List<Entity> entities = level.getEntities(player, box, e -> isValidTarget(player, e));

      int hit = 0;
      for (Entity e : entities) {
         LivingEntity target = (LivingEntity) e;
         if (target.position().distanceToSqr(player.position()) <= range * range) {
            target.addEffect(new MobEffectInstance(effect, duration, amplifier));
            applyElementalEffect(target, element, player);
            hit++;
         }
      }
      return hit;
   }

   /** Rafale de particules autour du joueur (retour visuel d'un move). */
   public static void spawnBurst(ServerPlayer player, ParticleOptions particle, int count) {
      if (particle == null) return;
      ServerLevel level = (ServerLevel) player.level();
      level.sendParticles(particle, player.getX(), player.getY() + 1.0, player.getZ(), count, 0.6, 0.6, 0.6, 0.05);
   }

   /** Élément dominant du fruit possédé par le joueur. */
   private static ElementType elementFor(ServerPlayer player) {
      ItemStack fruit = DevilFruitPowerLogic.getDevilFruit(player);
      var context = CastContext.current();
      if (context != null && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(fruit.getItem()).toString().equals(context.fruit()))
         return DevilFruitElement.forTechnique(fruit, context.technique());
      return DevilFruitElement.of(fruit);
   }

   /**
    * Effet élémentaire additionnel appliqué à une cible touchée (CORE-012) :
    * feu/magma → brûlure, glace → ralentissement + extinction du feu, foudre → étincelles.
    */
   public static void applyElementalEffect(LivingEntity target, ElementType element) {
      applyElementalEffect(target, element, null);
   }

   /**
    * Effet élémentaire avec <b>attribution</b> — le charme a besoin de savoir <i>qui</i> charme, pour
    * afficher la condition au bon joueur ; les autres éléments l'ignorent.
    */
   public static void applyElementalEffect(LivingEntity target, ElementType element, LivingEntity source) {
      switch (element) {
         // Charme (Mero Mero) : pétrification des cibles admissibles, condition affichée au porteur.
         case CHARM -> Petrification.applyFrom(source, target);
         // Fantôme (Horo Horo) : état négatif — distinct du ralentissement, plafonné, expirant.
         case GHOST -> NegativeState.applyFrom(source, target);
         // Feu : brûlure BORNÉE et sensible aux résistances (FRUIT-MERA_MERA-03) — une créature
         // immunisée au feu n'est pas brûlée, et le plafond interdit l'incendie perpétuel.
         // L'eau l'éteint : c'est exactement ce que le magma ne fait pas.
         case FIRE -> FlameBurn.ignite(target, br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig.burnTicks(), source);
         // Magma : brûlure SUIVIE par MagmaBurn, distincte du feu générique — elle n'est pas éteinte
         // par l'eau (cf. FRUIT-MAGU_MAGU-03). Le point d'application est ici, donc TOUT impact de
         // magma en bénéficie : projectiles comme mêlée, sans exception à maintenir.
         case MAGMA -> MagmaBurn.ignite(target, br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.magmaBurnTicks(), source);
         case ICE -> {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, false, true));
            FlameBurn.extinguish(target);
            MagmaBurn.extinguish(target);
         }
         case LIGHTNING -> {
            if (target.level() instanceof ServerLevel sl) {
               sl.sendParticles(MinepieceModParticles.THUNDER_BLUE, target.getX(), target.getY() + 1.0, target.getZ(),
                  20, 0.4, 0.6, 0.4, 0.1);
            }
         }
         default -> { /* PHYSICAL / EXPLOSIVE / DEFAULT : recul et particules déjà gérés ailleurs */ }
      }
   }
}
