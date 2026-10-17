package net.mcreator.minepiece.entity;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import net.mcreator.minepiece.procedures.CombatSkillLogic;
import net.mcreator.minepiece.procedures.ElementType;
import net.minecraft.core.Holder;
import net.mcreator.minepiece.procedures.DevilFruitExplosions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Projectile générique de la couche pouvoir, rendu via GeckoLib avec un modèle + texture + animation
 * dynamiques (réutilise les assets ver15 : geo/animations/textures). Déplacement en ligne droite, dégâts
 * + knockback + effet optionnel à l'impact, expiration après une durée de vie.
 *
 * <p>CORE-012 : porte un {@link ElementType} (feu/glace/…) appliqué à la cible à l'impact.
 * CORE-018 : la particule de traînée est synchronisée au client via {@code TRAIL_PARTICLE_ID}, pour
 * éviter qu'un projectile de glace/fumée affiche une traînée de feu par défaut.</p>
 */
public class SkillProjectileEntity extends AbstractHurtingProjectile implements GeoEntity {

   public static final EntityDataAccessor<Boolean> VISUAL_READY =
      SynchedEntityData.defineId(SkillProjectileEntity.class,EntityDataSerializers.BOOLEAN);

   public static final EntityDataAccessor<String> MODEL =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> ANIMATION =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.STRING);
   /** Stable cast profile for client FX; model names alone are shared by unrelated techniques. */
   public static final EntityDataAccessor<String> VISUAL_PROFILE =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Boolean> PHASING =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Float> SCALE =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Integer> TRAIL_PARTICLE_ID =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.INT);

   /** Rayon d'explosion à l'impact (0 = projectile ordinaire). FRUIT-BOMU_BOMU-03. */
   public static final EntityDataAccessor<Float> EXPLOSION_RADIUS =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.FLOAT);
   /** Particule d'explosion, distincte de la traînée. */
   public static final EntityDataAccessor<Integer> BLAST_PARTICLE_ID =
      SynchedEntityData.defineId(SkillProjectileEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   private String prevAnim = "idle";

   public net.mcreator.minepiece.procedures.TechniqueTerrain.Budget terrainBudget;
   public SkillProjectileEntity terrain(net.mcreator.minepiece.procedures.TechniqueTerrain.Budget b){terrainBudget=b;return this;}
   private void terrainImpact(){if(terrainBudget!=null){terrainBudget.impact(position(),terrainBudgetRadius(),getVisualProfile().equals("ryuseikazan")?12:getVisualProfile().equals("higan")?2:1000);if(magu() && getOwner() instanceof ServerPlayer p)net.mcreator.minepiece.procedures.MagmaPools.create((ServerLevel)level(),blockPosition(),p,br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.poolRadius());}}
   private double terrainBudgetRadius(){return getVisualProfile().equals("hiken")?Math.min(3,2*getScale()):6;}
   private ServerPlayer maguOrigin;
   private boolean magmaMeteor;
   private Vec3 meteorVelocity;
   public SkillProjectileEntity maguOrigin(ServerPlayer player) {maguOrigin=player;return this;}
   public SkillProjectileEntity magmaMeteor(boolean enabled) {magmaMeteor=enabled;return this;}
   private boolean magu() {return net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(getVisualProfile());}
   private ServerPlayer meraOrigin;
   public net.mcreator.minepiece.procedures.ControlledBatchTechniques.Shot controlledShot;
   public net.mcreator.minepiece.procedures.MaterialBatchTechniques.Shot materialShot;
   public net.mcreator.minepiece.procedures.MythicBatchTechniques.Shot mythicShot;
   public net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.Shot zoanShot;
   public net.mcreator.minepiece.procedures.MythicalZoanTechniques.Shot mythicalZoanShot;
   public net.mcreator.minepiece.procedures.ParameciaBatchTechniques.Shot parameciaShot;
   private ServerPlayer batchOrigin;private net.minecraft.world.item.Item batchFruit;
   public SkillProjectileEntity batchOrigin(ServerPlayer p){batchOrigin=p;batchFruit=net.mcreator.minepiece.procedures.DevilFruitPowerLogic.getDevilFruit(p).getItem();return this;}
   private ServerPlayer goroOrigin;
   public SkillProjectileEntity goroOrigin(ServerPlayer player) {goroOrigin=player;return this;}
   public SkillProjectileEntity meraOrigin(ServerPlayer player) {this.meraOrigin=player;return this;}
   private double damage = 6.0;
   private double knockback = 0.5;
   private ElementType element = ElementType.PHYSICAL;
   private Holder<MobEffect> effect;
   private int effectDuration;
   private int effectAmplifier;
   private ParticleOptions trailParticle = MinepieceModParticles.FLAME;
   private int lifeTicks;
   private int maxLifeTicks = 100;
   /**
    * <b>Portée explicite, en blocs</b> — 0 signifie « pas de limite », c'est-à-dire exactement le
    * comportement d'origine. Le plan de Pika Pika demande de <b>séparer portée et durée</b>
    * (FRUIT-PIKA_PIKA-TECH-01/02 : « portée mesurée ») : un projectile qui n'expire que par durée de
    * vie parcourt {@code durée × vitesse} blocs, ce qui n'est pas une portée, c'est une conséquence.
    * La valeur par défaut à 0 garantit qu'aucun des autres projectiles du mod ne change de comportement.
    */
   private double maxRangeBlocks;
   private Vec3 origin;
   /**
    * Traversée des surfaces (FRUIT-HORO_HORO-03). Un fantôme traverse les murs — c'est sa nature — mais
    * la traversée est <b>bornée par des règles</b> : durée de vie finie, et arrêt aux frontières des
    * zones protégées. Un pouvoir qui ignore les protections serait un trou de sécurité, pas un pouvoir.
    */

   /** Constructeur usine (registration + spawn par défaut). */
   public SkillProjectileEntity(EntityType<SkillProjectileEntity> type, Level level) {
      super(type, level);
   }

   /** Constructeur de spawn avec position + vélocité initiale. */
   public SkillProjectileEntity(EntityType<SkillProjectileEntity> type, double x, double y, double z, Vec3 movement, Level level) {
      super(type, x, y, z, movement, level);
   }

   // --- configuration fluide ---
   public SkillProjectileEntity damage(double d) { this.damage = d; return this; }
   public SkillProjectileEntity knockback(double k) { this.knockback = k; return this; }
   public SkillProjectileEntity element(ElementType el) { this.element = el; return this; }
   public SkillProjectileEntity effect(Holder<MobEffect> e, int duration, int amplifier) {
      this.effect = e; this.effectDuration = duration; this.effectAmplifier = amplifier; return this;
   }
   public SkillProjectileEntity trail(ParticleOptions p) {
      this.trailParticle = p;
      this.entityData.set(TRAIL_PARTICLE_ID, BuiltInRegistries.PARTICLE_TYPE.getId(p.getType()));
      return this;
   }
   public SkillProjectileEntity lifetime(int ticks) { this.maxLifeTicks = ticks; return this; }

   /** Portée en blocs depuis le point de tir (0 = illimitée, comportement d'origine). */
   public SkillProjectileEntity range(double blocks) {
      this.maxRangeBlocks = Math.max(0.0, blocks);
      return this;
   }

   /** Portée déclarée (0 = illimitée). */
   public double rangeBlocks() {
      return maxRangeBlocks;
   }
   /** Active la traversée des surfaces (fantôme). */
   public SkillProjectileEntity phases(boolean p) { this.entityData.set(PHASING, p); return this; }
   /** Vrai si ce projectile traverse les blocs. */
   public boolean isPhasing() { return this.entityData.get(PHASING); }
   public SkillProjectileEntity model(String m) { this.entityData.set(MODEL, m); this.entityData.set(VISUAL_READY,true); return this; }
   public SkillProjectileEntity texture(String t) { this.entityData.set(TEXTURE, t); return this; }
   public SkillProjectileEntity animation(String a) { this.entityData.set(ANIMATION, a); return this; }
   public SkillProjectileEntity visualProfile(String id) {
      this.entityData.set(VISUAL_PROFILE, id == null ? "" : id);
      return this;
   }
   public SkillProjectileEntity scale(float s) { this.entityData.set(SCALE, s); return this; }

   /** Donne au projectile un comportement explosif à l'impact (rayon en blocs, 0 = aucun). */
   public SkillProjectileEntity explodeOnImpact(double radius, ParticleOptions blast) {
      this.entityData.set(EXPLOSION_RADIUS, (float) radius);
      if (blast != null) {
         this.entityData.set(BLAST_PARTICLE_ID, BuiltInRegistries.PARTICLE_TYPE.getId(blast.getType()));
      }
      return this;
   }

   public float getExplosionRadius() { return this.entityData.get(EXPLOSION_RADIUS); }
   public double getDamageValue() { return this.damage; }

   public String getModel() { return this.entityData.get(MODEL); }
   public String getTexture() { return this.entityData.get(TEXTURE); }
   public String getAnimationName() { return this.entityData.get(ANIMATION); }
   public String getVisualProfile() { return this.entityData.get(VISUAL_PROFILE); }
   public float getScale() { return this.entityData.get(SCALE); }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VISUAL_READY,false);
      builder.define(MODEL, "ball_maximum");
      builder.define(TEXTURE, "meteor");
      builder.define(ANIMATION, "idle");
      builder.define(VISUAL_PROFILE, "");
      builder.define(PHASING, false);
      builder.define(SCALE, 1.0F);
      builder.define(TRAIL_PARTICLE_ID, 0);
      builder.define(EXPLOSION_RADIUS, 0.0F);
      builder.define(BLAST_PARTICLE_ID, 0);
   }

   @Override
   protected ParticleOptions getTrailParticle() {
      // CORE-018 : reconstruit la particule depuis l'id synchronisé (visible côté client).
      ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.byId(this.entityData.get(TRAIL_PARTICLE_ID));
      if (type instanceof ParticleOptions po) {
         return po;
      }
      return this.trailParticle;
   }

   @Override
   protected boolean shouldBurn() {
      // AbstractHurtingProjectile is a fireball base. Our own elemental FX must
      // not put a vanilla flame billboard on lasers, ice and ghosts.
      return false;
   }

   @Override
   protected boolean canHitEntity(Entity target) {
      // CORE-010 : le projectile ne traverse pas alliés/animaux du propriétaire/joueurs protégés.
      return !(magmaMeteor && getDeltaMovement().y>0) && target != getOwner() && CombatSkillLogic.isValidTarget(getOwner(), target) && super.canHitEntity(target);
   }

   @Override
   protected void onHitEntity(EntityHitResult result) {
      super.onHitEntity(result);
      if (level().isClientSide()) return;
      Entity target = result.getEntity();
      if(batchOrigin!=null && target instanceof LivingEntity t && (!(getOwner() instanceof ServerPlayer p) || !net.mcreator.minepiece.procedures.BatchRules.offensiveTarget(p,t))){discard();return;}
      if (target instanceof LivingEntity living) {
         // NIKYU NIKYU — la déviation passe AVANT les dégâts : un projectile renvoyé ne blesse pas celui
         // qui le renvoie, il repart. Le transfert de propriétaire est ce qui rend l'attribution juste.
         if (net.mcreator.minepiece.procedures.NikyuRepulsion.tryDeflect(this, living)) {
            if(magmaMeteor)meteorVelocity=getDeltaMovement();
            return;
         }
         if(mythicShot!=null){net.mcreator.minepiece.procedures.MythicBatchTechniques.impact(this,result.getLocation(),living);discard();return;}
         if(zoanShot!=null){net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.impact(this,result.getLocation(),living);discard();return;}
         if(mythicalZoanShot!=null){net.mcreator.minepiece.procedures.MythicalZoanTechniques.impact(this,result.getLocation(),living);discard();return;}
         if(parameciaShot!=null){net.mcreator.minepiece.procedures.ParameciaBatchTechniques.collision(this,result.getLocation(),living);return;}
         if(materialShot!=null){net.mcreator.minepiece.procedures.MaterialBatchTechniques.impact(this,result.getLocation(),living);discard();return;}
         if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation(),living);discard();return;}
         ServerLevel serverLevel = (ServerLevel) level();
         DamageSource source = CombatSkillLogic.elementalDamageSource(serverLevel, element, getOwner(), this);
         if (!(magu() || getVisualProfile().equals("goro_raigo") || getVisualProfile().equals("daienkai") || getVisualProfile().equals("higan")) && CombatSkillLogic.hurtAndConfirm(living, serverLevel, source, (float) damage)) {
            // Attribution : le propriétaire du projectile est la source de l'effet.
            if(batchOrigin!=null && getOwner() instanceof ServerPlayer p)net.mcreator.minepiece.procedures.ElementalBatchTechniques.onConfirmed(p,living,batchFruit,getVisualProfile());
            else CombatSkillLogic.applyElementalEffect(living, element,
               getOwner() instanceof LivingEntity owner ? owner : null);
            if (effect != null) {
               living.addEffect(new MobEffectInstance(effect, effectDuration, effectAmplifier));
            }
            CombatSkillLogic.applySkillKnockback(living, getDeltaMovement(), source, knockback);
         }
      }
      if(batchOrigin!=null || magu() || getVisualProfile().equals("goro_raigo") || net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(getVisualProfile())) setPos(result.getLocation());
      impact();
      terrainImpact();
      discard();
   }

   @Override
   protected void onHitBlock(BlockHitResult result) {
      super.onHitBlock(result);
      if (level().isClientSide()) {
         return;
      }
      if(mythicShot!=null){net.mcreator.minepiece.procedures.MythicBatchTechniques.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}
      if(zoanShot!=null){net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}
      if(mythicalZoanShot!=null){net.mcreator.minepiece.procedures.MythicalZoanTechniques.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}
      if(parameciaShot!=null){net.mcreator.minepiece.procedures.ParameciaBatchTechniques.collision(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);return;}
      if(materialShot!=null){net.mcreator.minepiece.procedures.MaterialBatchTechniques.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}
      if(controlledShot!=null){net.mcreator.minepiece.procedures.ControlledBatchTechniques.impact(this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.02)),null);discard();return;}
      // Règle de traversée : un fantôme ne s'arrête pas au mur…
      if (isPhasing()) {
         return;
      }
      if(batchOrigin!=null || magu() || getVisualProfile().equals("goro_raigo") || net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(getVisualProfile())) setPos(result.getLocation().subtract(getDeltaMovement().normalize().scale(.01)));
      impact();
      terrainImpact();
      discard();
   }

   /** Rafale de particules + son à l'impact (côté serveur, visible pour tous). */
   private void impact() {
      ServerLevel serverLevel = (ServerLevel) level();
      if(batchOrigin!=null){net.mcreator.minepiece.procedures.ElementalBatchTechniques.fx(batchOrigin,getVisualProfile().startsWith("dark")?"b01_dark_impact":batchFruit==net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE?"b01_ice_impact":"b01_impact",position(),10);}
      serverLevel.sendParticles(trailParticle, getX(), getY(), getZ(), 16, 0.3, 0.3, 0.3, 0.05);
      serverLevel.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
         net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.1F);
      // FRUIT-BOMU_BOMU-03 : origine « projectile » — l'explosion naît au point d'impact, pas au
      // lanceur. Le moteur d'explosion applique les mêmes règles de porteur/alliés/blocs que les
      // origines de contact et corporelle (une seule source de vérité).
      float radius = getExplosionRadius();
      if (radius > 0.0F && getOwner() instanceof ServerPlayer shooter) {
         ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.byId(this.entityData.get(BLAST_PARTICLE_ID));
         ParticleOptions blast = type instanceof ParticleOptions po ? po : null;
         if(getVisualProfile().equals("daienkai") || getVisualProfile().equals("higan")) net.mcreator.minepiece.procedures.MeraTechniques.blast(shooter,serverLevel,position(),radius,this.damage,this.knockback);
         else if(magu()) net.mcreator.minepiece.procedures.MaguTechniques.blast(shooter,serverLevel,position(),radius,this.damage,this.knockback,terrainBudget==null);
         else if(getVisualProfile().equals("goro_raigo")) {net.mcreator.minepiece.procedures.GoroTechniques.blast(shooter,serverLevel,position(),radius,this.damage,this.knockback);net.mcreator.minepiece.procedures.GoroTechniques.visual(shooter,"goro_impact",position(),15).setStretch(radius);}
         else DevilFruitExplosions.blast(shooter, position(), radius, this.damage, blast);
      }
   }

   @Override
   public void tick() {
      if (level().isClientSide()) {
         super.tick();
         return;
      }
      if(controlledShot!=null && lifeTicks<8 && Boolean.getBoolean("opworldlike.repairTests"))org.slf4j.LoggerFactory.getLogger("B02Shot").info("[b02-shot] tick profile={} age={} at={} velocity={}",getVisualProfile(),lifeTicks,position(),getDeltaMovement());
      if(mythicShot!=null && !mythicShot.valid()){discard();return;}
      if(zoanShot!=null && !zoanShot.valid()){discard();return;}
      if(mythicalZoanShot!=null && !mythicalZoanShot.valid()){discard();return;}
      if(parameciaShot!=null && !parameciaShot.valid()){discard();return;}
      if(materialShot!=null && !materialShot.valid()){discard();return;}
      if(controlledShot!=null && !controlledShot.valid()){discard();return;}
      if(batchOrigin!=null && !net.mcreator.minepiece.procedures.BatchRules.usable(batchOrigin,(ServerLevel)level(),batchFruit)){discard();return;}
      if (net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(getVisualProfile())) {
         ServerPlayer origin=meraOrigin!=null?meraOrigin:getOwner() instanceof ServerPlayer owner?owner:null;
         if(origin==null || origin.level()!=level() || !net.mcreator.minepiece.procedures.MeraTechniques.usable(origin)) {discard();return;}
      }
      if(getVisualProfile().equals("goro_raigo") && (goroOrigin==null || goroOrigin.level()!=level() || !net.mcreator.minepiece.procedures.GoroTechniques.usable(goroOrigin))) {discard();return;}
      if(magu()) {
         ServerPlayer owner=maguOrigin!=null?maguOrigin:getOwner() instanceof ServerPlayer p?p:null;
         if(owner==null || owner.level()!=level() || !net.mcreator.minepiece.procedures.MaguTechniques.usable(owner)){discard();return;}
         if(magmaMeteor) {
            if(meteorVelocity==null)meteorVelocity=getDeltaMovement();
            meteorVelocity=new Vec3(meteorVelocity.x*.99,Math.max(-2.25,meteorVelocity.y-.1),meteorVelocity.z*.99);
            setDeltaMovement(meteorVelocity);
         }
      }
      if (origin == null) origin = position();
      // A normal skill must not become immortal when the ghost-specific option is disabled.
      if (++lifeTicks > Math.max(1, maxLifeTicks)) { if(getVisualProfile().equals("higan")) impact(); discard(); return; }
      // …mais il ne franchit pas une zone protégée : la traversée est limitée aux règles retenues.
      if (isPhasing() && DevilFruitHollowConfig.ghostsStopAtClaims()
            && level() instanceof ServerLevel serverLevel
            && DevilFruitExplosions.isProtectedPosition(serverLevel, blockPosition())) {
         discard();
         return;
      }
      super.tick();
      // PORTÉE EXPLICITE : un rayon de lumière s'arrête à sa portée, il ne continue pas parce que sa
      // durée de vie n'est pas écoulée. L'origine est fixée au premier tick (position de tir).
      if (maxRangeBlocks > 0.0 && position().distanceTo(origin) >= maxRangeBlocks - 1.0E-6) {
         discard();
      }
   }

   /** Clip the next movement segment to the explicit sphere around the firing point. */
   public void clipMovementToRange() {
      if (origin == null || maxRangeBlocks <= 0) return;
      Vec3 offset = position().subtract(origin), movement = getDeltaMovement();
      double squared = movement.lengthSqr();
      if (offset.add(movement).lengthSqr() <= maxRangeBlocks * maxRangeBlocks || squared < 1.0E-12) return;
      double b = offset.dot(movement), c = offset.lengthSqr() - maxRangeBlocks * maxRangeBlocks;
      double t = (-b + Math.sqrt(Math.max(0, b * b - squared * c))) / squared;
      setDeltaMovement(movement.scale(Math.clamp(t, 0, 1)));
   }

   // --- GeoEntity ---
   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<SkillProjectileEntity>("idle", 0,
         (AnimationTest<SkillProjectileEntity> state) -> {
            if (!"idle".equals(getAnimationName())) return PlayState.STOP;
            // These two shipped animation files have idle1/idle2, never a track named idle.
            String idle = "ghost1".equals(getModel()) || "ghost2".equals(getModel()) ? "idle1" : "idle";
            return state.setAndContinue(RawAnimation.begin().thenLoop(idle));
         }));
      controllers.add(new AnimationController<SkillProjectileEntity>("procedure", 0,
         (AnimationTest<SkillProjectileEntity> state) -> this.procedurePredicate(state)));
   }

   private PlayState procedurePredicate(AnimationTest<SkillProjectileEntity> state) {
      String anim = this.entityData.get(ANIMATION);
      if (anim.equals("idle")) {
         this.prevAnim = "idle";
         return PlayState.STOP;
      }
      if (!anim.equals(this.prevAnim)) {
         state.controller().reset();
         this.prevAnim = anim;
      }
      // Keep advancing the existing timeline on subsequent ticks instead of stopping after frame one.
      return state.setAndContinue(RawAnimation.begin().thenPlay(anim));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
