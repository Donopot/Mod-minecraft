package net.mcreator.minepiece.potion;


import net.mcreator.minepiece.init.MinepieceModParticles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.mcreator.minepiece.procedures.CombatSkillLogic;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.mcreator.minepiece.procedures.ElementType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>Mamaragan</b> — orage personnel (FRUIT-GORO_GORO-05).
 *
 * <p>Le plan relève le défaut précisément : « Mamaragan est déclaré BUFF et son effet utilise
 * {@code SimpleMarkerMobEffect} <b>sans comportement propre</b> » ; il demande de « donner un
 * comportement à Mamaragan plutôt qu'un simple effet marqueur », avec pour critère « des impacts de
 * foudre <b>annoncés</b> se produisent pendant une <b>durée bornée</b> et <b>cessent à la
 * suppression</b> ».</p>
 *
 * <h2>Pourquoi le comportement vit dans l'effet</h2>
 * <p>La durée de l'effet est exactement le bornage demandé : en sortant de l'effet, plus rien ne
 * tourne, sans minuterie ni nettoyage à écrire. Le cycle comporte deux temps — une <b>annonce</b>
 * (croix d'étincelles + coup de tonnerre à l'endroit visé) puis la <b>frappe</b> quelques ticks plus
 * tard : c'est ce qui rend l'impact annoncé plutôt que subi.</p>
 *
 * <h2>Décision de contrat</h2>
 * <p>ver15 ne donne <b>aucun</b> dégât à Mamaragan : la technique y est classée BUFF avec
 * {@code degats=0}. Les valeurs de frappe ci-dessous sont donc un choix de contrat, assumé et
 * verrouillé par les tests — pas une recopie.</p>
 */
public class MamaraganMobEffect extends MobEffect {

   /** Rayon où l'orage cherche une cible à annoncer. */
   public static final double ANNOUNCE_RADIUS = 8.0;

   /** Rayon de l'impact : ce qui se trouve là au moment de la frappe est touché. */
   public static final double STRIKE_RADIUS = 2.0;

   /** Dégâts d'un impact (choix de contrat : ver15 en déclarait zéro). */
   public static final double STRIKE_DAMAGE = 10.0;

   /** Délai entre l'annonce et la frappe, en ticks : le temps de s'écarter. */
   public static final int WARNING_TICKS = 10;

   /** Nombre maximal d'impacts sur la durée de l'effet (60 ticks) : le bornage du critère. */
   public static final int MAX_STRIKES = 3;

   private record Pending(Vec3 point, int ticksLeft, ServerLevel level, ServerPlayer owner, long announcedAt) {
   }

   private static final Map<UUID, Pending> PENDING = new HashMap<>();
   private static final Map<UUID, Integer> STRIKES = new HashMap<>();
   private static final Map<UUID,ServerPlayer> OWNERS=new HashMap<>();
   private static final Map<UUID,Long> NEXT_ANNOUNCE=new HashMap<>();

   public MamaraganMobEffect(MobEffectCategory category, int color) {
      super(category, color);
   }

   /** Cadence fine : la logique interne décide du rythme annonce/frappe. */
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration % 5 == 0;
   }

   @Override
   public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
      if (!(entity instanceof ServerPlayer player)) {
         return true;
      }
      UUID id = player.getUUID();
      OWNERS.put(id,player);

      // « Cessent à la suppression » : sous l'eau de mer, l'orage s'arrête et l'annonce en cours
      // est annulée — un impact ne doit pas frapper après que le pouvoir a été scellé.
      if (!net.mcreator.minepiece.procedures.GoroTechniques.usable(player)) {
         clearState(id);return true;
      }

      Pending pending = PENDING.get(id);
      if (pending != null) {
         if(pending.level()!=level || pending.owner()!=player) {clearState(id);return true;}
         if (pending.ticksLeft() > 5) {
            PENDING.put(id, new Pending(pending.point(), pending.ticksLeft() - 5,level,player,pending.announcedAt()));
            return true;
         }
         PENDING.remove(id);
         int touched=strike(level, player, pending.point(), amplifier);
         NEXT_ANNOUNCE.put(id,level.getGameTime()+10);
         if(Boolean.getBoolean("opworldlike.visualCapture"))org.slf4j.LoggerFactory.getLogger("GoroStorm").info("[goro-storm] strike delay={} strikes={} hits={}",level.getGameTime()-pending.announcedAt(),strikesDone(id),touched);
         return true;
      }

      // Bornage : une fois le quota d'impacts atteint, l'orage ne fait plus que durer.
      if (STRIKES.getOrDefault(id, 0) >= MAX_STRIKES) {
         return true;
      }
      if(level.getGameTime()<NEXT_ANNOUNCE.getOrDefault(id,0L))return true;
      LivingEntity target = nearestHostile(player, ANNOUNCE_RADIUS);
      if (target != null) {
         announce(level, player, target.position());
         PENDING.put(id, new Pending(target.position(), WARNING_TICKS,level,player,level.getGameTime()));
      }
      return true;
   }

   @Override
   public void onEffectStarted(LivingEntity entity, int amplifier) {
      if (entity instanceof ServerPlayer player) {
         STRIKES.put(player.getUUID(), 0);
         PENDING.remove(player.getUUID());
         NEXT_ANNOUNCE.remove(player.getUUID());OWNERS.put(player.getUUID(),player);
      }
   }

   /** Cible la plus proche qui peut être frappée : jamais un allié, jamais le porteur. */
   private static LivingEntity nearestHostile(ServerPlayer player, double radius) {
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;
      AABB zone = player.getBoundingBox().inflate(radius);
      for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, zone)) {
         if (candidate == player || !candidate.isAlive() || !CombatSkillLogic.isValidTarget(player, candidate)) {
            continue;
         }
         if(!player.hasLineOfSight(candidate) || net.mcreator.minepiece.procedures.DevilFruitExplosions.isProtectedPosition((ServerLevel)player.level(),candidate.blockPosition()))continue;
         double distance = candidate.distanceTo(player);
         if (distance <= radius && distance < bestDistance) {
            bestDistance = distance;
            best = candidate;
         }
      }
      return best;
   }

   /** Annonce : on voit et on entend où l'éclair va tomber, avant qu'il tombe. */
   public static void announce(ServerLevel level, ServerPlayer caster, Vec3 point) {
      net.mcreator.minepiece.procedures.GoroTechniques.visual(caster,"goro_warning",point,11).setStretch(2);
      level.sendParticles(MinepieceModParticles.THUNDER_BLUE, point.x, point.y + 1.0, point.z, 40, 0.4, 0.6, 0.4, 0.1);
      level.playSound(null, net.minecraft.core.BlockPos.containing(point), SoundEvents.LIGHTNING_BOLT_THUNDER,
         SoundSource.PLAYERS, 0.7F, 1.4F);
   }

   /**
    * Frappe : tout ce qui se trouve dans {@link #STRIKE_RADIUS} du point annoncé est touché, sauf le
    * porteur et ses alliés.
    *
    * @return nombre de créatures touchées
    */
   public static int strike(ServerLevel level, ServerPlayer caster, Vec3 point, int amplifier) {
      if(caster.level()!=level || !net.mcreator.minepiece.procedures.GoroTechniques.usable(caster)
            || net.mcreator.minepiece.procedures.DevilFruitExplosions.isProtectedPosition(level,net.minecraft.core.BlockPos.containing(point)))return 0;
      int touched = 0;
      AABB zone = new AABB(point, point).inflate(STRIKE_RADIUS);
      var source = CombatSkillLogic.elementalDamageSource(level, ElementType.LIGHTNING, caster, null);
      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, zone)) {
         if (target == caster || !target.isAlive() || !CombatSkillLogic.isValidTarget(caster, target)) {
            continue;
         }
         if (target.position().distanceTo(point) > STRIKE_RADIUS) {
            continue;
         }
         if(net.mcreator.minepiece.procedures.DevilFruitExplosions.isProtectedPosition(level,target.blockPosition())
               || level.clip(new net.minecraft.world.level.ClipContext(point.add(0,1,0),target.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,caster)).getType()!=net.minecraft.world.phys.HitResult.Type.MISS)continue;
         // Seules les créatures réellement blessées sont comptées : la fenêtre d'invulnérabilité
         // vanille (20 ticks) peut absorber une frappe, et un compteur qui l'ignorerait annoncerait
         // un impact qui n'a rien fait.
         if (CombatSkillLogic.hurtAndConfirm(target,level, source, (float) STRIKE_DAMAGE)) {
            CombatSkillLogic.applyElementalEffect(target, ElementType.LIGHTNING,caster);
            touched++;
         }
      }
      level.sendParticles(MinepieceModParticles.THUNDER_BLUE, point.x, point.y + 1.0, point.z, 60, 0.2, 1.2, 0.2, 0.4);
      level.playSound(null, net.minecraft.core.BlockPos.containing(point), SoundEvents.LIGHTNING_BOLT_IMPACT,
         SoundSource.PLAYERS, 1.0F, 1.0F);
      STRIKES.merge(caster.getUUID(), 1, Integer::sum);
      net.mcreator.minepiece.procedures.GoroTechniques.column(caster,point,12);
      return touched;
   }

   /**
    * Exerce la logique de tick de l'effet <b>enregistré</b>, sans attendre un serveur réel.
    *
    * <p>Passe par l'instance enregistrée pour que les tests exercent exactement l'effet utilisé en
    * jeu, et non une seconde instance construite pour la circonstance.</p>
    */
   public static void tick(net.minecraft.server.level.ServerLevel level, ServerPlayer player, int amplifier) {
      MobEffect effect = net.mcreator.minepiece.init.MinepieceModMobEffects.MAMARAGAN.value();
      if (effect instanceof MamaraganMobEffect mamaragan) {
         mamaragan.applyEffectTick(level, player, amplifier);
      }
   }

   /** Nombre d'impacts déjà frappés (bornage vérifiable). */
   public static int strikesDone(UUID playerId) {
      return STRIKES.getOrDefault(playerId, 0);
   }

   /** Une annonce est-elle en attente pour ce joueur ? (état du cycle, vérifiable) */
   public static boolean hasPending(UUID playerId) {
      return PENDING.containsKey(playerId);
   }

   /** Point annoncé en attente, ou {@code null}. */
   public static Vec3 pendingPoint(UUID playerId) {
      Pending pending = PENDING.get(playerId);
      return pending == null ? null : pending.point();
   }

   /** Oublie l'état d'un joueur (retrait du Fruit, fin de partie, tests). */
   public static void clearState(UUID playerId) {
      PENDING.remove(playerId);
      STRIKES.remove(playerId);
      OWNERS.remove(playerId);NEXT_ANNOUNCE.remove(playerId);
   }
   public static void prune() {
      for(var entry:new java.util.ArrayList<>(OWNERS.entrySet())) {
         var p=entry.getValue();var pending=PENDING.get(entry.getKey());
         if(!p.hasEffect(net.mcreator.minepiece.init.MinepieceModMobEffects.MAMARAGAN)
               || !net.mcreator.minepiece.procedures.GoroTechniques.usable(p) || pending!=null && pending.level()!=p.level())clearState(entry.getKey());
      }
   }
   public static void clearAll() {PENDING.clear();STRIKES.clear();OWNERS.clear();NEXT_ANNOUNCE.clear();}
}
