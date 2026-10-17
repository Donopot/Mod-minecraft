package net.mcreator.minepiece.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Invocation de Fruit du Démon (CORE-021) — entité fonctionnelle avec IA, propriétaire et expiration.
 *
 * <p>Contrairement au bras visuel {@link EntityGomuArmEntity} (NoAI, dégâts 0), cette entité <b>agit</b> :
 * elle suit son propriétaire, attaque les monstres hostiles proches, riposte à l'agresseur du propriétaire
 * et inflige des dégâts réels ({@code ATTACK_DAMAGE}). L'alliance est respectée (pas d'attaque du propriétaire
 * ni de ses coéquipiers). Le visuel (modèle/texture) est piloté dynamiquement via les data accessors
 * (réutilise les assets ver15), comme le bras de Gomu.</p>
 */
public class SummonEntity extends PathfinderMob implements OwnableEntity, GeoEntity {

   public static final EntityDataAccessor<String> MODEL =
      SynchedEntityData.defineId(SummonEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE =
      SynchedEntityData.defineId(SummonEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> ANIMATION =
      SynchedEntityData.defineId(SummonEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> VISUAL_OWNER =
      SynchedEntityData.defineId(SummonEntity.class, EntityDataSerializers.STRING);

   private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   private EntityReference<LivingEntity> ownerRef;
   private int lifeTicks = 0;
   private int maxLifeTicks = 200; // 10 s par défaut

   public SummonEntity(EntityType<? extends PathfinderMob> type, Level level) {
      super(type, level);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(MODEL, "entity_gomu_arm");
      builder.define(TEXTURE, "luffy");
      builder.define(ANIMATION, "empty");
      builder.define(VISUAL_OWNER, "");
   }

   public String getModel() {
      return this.entityData.get(MODEL);
   }

   public void setModel(String model) {
      this.entityData.set(MODEL, model);
      this.refreshDimensions();
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getAnimation() {
      return this.entityData.get(ANIMATION);
   }

   public void setAnimation(String animation) {
      this.entityData.set(ANIMATION, animation);
   }

   /** Propriétaire (côté serveur). */
   public void setOwnerEntity(LivingEntity owner) {
      this.ownerRef = owner == null ? null : EntityReference.of(owner);
      this.entityData.set(VISUAL_OWNER,owner==null?"":owner.getUUID().toString());
   }

   public void setOwnerUuid(UUID uuid) {
      this.ownerRef = uuid == null ? null : EntityReference.of(uuid);
      this.entityData.set(VISUAL_OWNER,uuid==null?"":uuid.toString());
   }

   /** Render-only ownership marker: another player's summon must remain visible at close range. */
   public String getVisualOwnerId() { return this.entityData.get(VISUAL_OWNER); }

   /** Durée de vie en ticks avant désintégration. */
   /** Durée de vie restante déclarée — exposé pour que les preuves puissent la LIRE et la comparer. */
   public int getLifetimePourTest() {
      return this.maxLifeTicks;
   }

   public SummonEntity lifetime(int ticks) {
      this.maxLifeTicks = ticks;
      return this;
   }

   /** Dégâts d'attaque réels (remplace le 0 du bras visuel). */
   public void setDamage(double damage) {
      if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
      }
   }

   @Override
   public EntityReference<LivingEntity> getOwnerReference() {
      return this.ownerRef;
   }

   @Override
   public LivingEntity getOwner() {
      return this.ownerRef == null ? null : this.ownerRef.getEntity(this.level(), LivingEntity.class);
   }

   @Override protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose){return getModel().equals("tot_musica_1")?net.minecraft.world.entity.EntityDimensions.scalable(2.4F,3.6F).withEyeHeight(2.8F):super.getDefaultDimensions(pose);}
   @Override public void onSyncedDataUpdated(net.minecraft.network.syncher.EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(MODEL.equals(key))refreshDimensions();}

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 30.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
      this.goalSelector.addGoal(2, new FollowOwnerGoal());
      this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));

      this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal());
      this.targetSelector.addGoal(2, new OwnerHurtTargetGoal());
      this.targetSelector.addGoal(3,
         new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
            (target, level) -> canAttack(target)));
   }

   /** Alliance : jamais le propriétaire ni un coéquipier. */
   @Override
   public boolean canAttack(LivingEntity target) {
      if(target==null)return false;
      if(net.mcreator.minepiece.procedures.ParameciaBatchTechniques.owned(this) && !net.mcreator.minepiece.procedures.ParameciaBatchTechniques.summonTarget(this,target))return false;
      if(net.mcreator.minepiece.procedures.MythicBatchTechniques.owned(this)&&!net.mcreator.minepiece.procedures.MythicBatchTechniques.summonTarget(this,target))return false;
      LivingEntity owner = getOwner();
      if (owner != null) {
         if (target == owner || target.getUUID().equals(owner.getUUID())) {
            return false;
         }
         if (target.getTeam() != null && owner.getTeam() != null && target.getTeam().isAlliedTo(owner.getTeam())) {
            return false;
         }
      }
      return super.canAttack(target);
   }

   /**
    * Ordres donnés à une invocation par son propriétaire (FRUIT-BISU_BISU-04).
    *
    * <p>{@code SUIVRE} — escorte : suit le propriétaire, engage ce qui le menace (comportement d'origine,
    * CORE-021). {@code DEFENDRE} — garde rapprochée : ne s'éloigne pas du propriétaire et abandonne
    * toute poursuite qui l'en éloignerait. {@code ATTAQUER} — chasse : recherche activement les
    * créatures hostiles dans un rayon élargi, sans rester près du propriétaire.</p>
    */
   public enum Order {
      SUIVRE, DEFENDRE, ATTAQUER
   }

   /** Rayon de chasse en mode ATTAQUER ; rayon de garde en mode DEFENDRE. */
   private static final double HUNT_RADIUS = 24.0;
   private static final double GUARD_RADIUS = 8.0;

   private Order order = Order.SUIVRE;

   public Order getOrder() {
      return this.order;
   }

   public void setOrder(Order order) {
      this.order = order == null ? Order.SUIVRE : order;
      if (this.order != Order.ATTAQUER) {
         setTarget(null);
      }
   }

   @Override
   public void tick() {
      if(!level().isClientSide()&&net.mcreator.minepiece.procedures.MythicBatchTechniques.owned(this)){if(!net.mcreator.minepiece.procedures.MythicBatchTechniques.summonValid(this)){discard();return;}if(getTarget()!=null&&!net.mcreator.minepiece.procedures.MythicBatchTechniques.summonTarget(this,getTarget()))setTarget(null);}
      if(!level().isClientSide() && net.mcreator.minepiece.procedures.ParameciaBatchTechniques.owned(this) && !net.mcreator.minepiece.procedures.ParameciaBatchTechniques.summonValid(this)){discard();return;}
      if(!level().isClientSide() && net.mcreator.minepiece.procedures.ParameciaBatchTechniques.owned(this)){
         if(getTarget()!=null && !net.mcreator.minepiece.procedures.ParameciaBatchTechniques.summonTarget(this,getTarget()))setTarget(null);
         setAnimation(getTarget()!=null && distanceTo(getTarget())<3 && tickCount%20<8?(getModel().equals("biscuit_warrrior")?"sword_to_right":"punch_right"):"empty");
      }
      super.tick();
      if (!level().isClientSide() && ++this.lifeTicks > this.maxLifeTicks) {
         this.discard();
         return;
      }
      LivingEntity owner = getOwner();
      switch (this.order) {
         case ATTAQUER -> {
            // Chasse : cherche une cible hostile dans un rayon élargi, sans suivre le propriétaire.
            if (getTarget() == null && owner != null) {
               LivingEntity prey = level().getEntitiesOfClass(Monster.class,
                  getBoundingBox().inflate(HUNT_RADIUS),
                  candidate -> candidate.isAlive() && canAttack(candidate) && distanceTo(candidate) <= HUNT_RADIUS)
                  .stream()
                  .min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b)))
                  .orElse(null);
               setTarget(prey);
            }
         }
         case DEFENDRE -> {
            // Garde rapprochée : renonce à une poursuite qui éloignerait de son propriétaire.
            LivingEntity target = getTarget();
            if (target != null && owner != null && target.distanceTo(owner) > GUARD_RADIUS) {
               setTarget(null);
            }
            if (owner != null && distanceTo(owner) > 4.0) {
               getNavigation().moveTo(owner, 1.2);
            }
         }
         default -> { /* SUIVRE : objectifs d'origine */ }
      }
   }

   /** Applique un ordre à cette invocation. */
   public void applyOrder(Order order) {
      setOrder(order);
   }

   /** Suit le propriétaire quand il n'a pas de cible et qu'il est trop loin. */
   private class FollowOwnerGoal extends Goal {
      FollowOwnerGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         LivingEntity owner = getOwner();
         if (owner == null || owner.isRemoved()) {
            return false;
         }
         if (getTarget() != null) {
            return false;
         }
         return distanceToSqr(owner) > 12.0 * 12.0;
      }

      @Override
      public boolean canContinueToUse() {
         LivingEntity owner = getOwner();
         if (owner == null || getTarget() != null) {
            return false;
         }
         return distanceToSqr(owner) > 4.0 * 4.0;
      }

      @Override
      public void tick() {
         LivingEntity owner = getOwner();
         if (owner != null) {
            getNavigation().moveTo(owner, 1.0);
         }
      }
   }

   /** Cible l'agresseur du propriétaire. */
   private class OwnerHurtByTargetGoal extends TargetGoal {
      private LivingEntity lastHurtBy;
      private int timestamp;

      OwnerHurtByTargetGoal() {
         super(SummonEntity.this, false);
      }

      @Override
      public boolean canUse() {
         LivingEntity owner = getOwner();
         if (owner == null) {
            return false;
         }
         this.lastHurtBy = owner.getLastHurtByMob();
         int ts = owner.getLastHurtByMobTimestamp();
         return this.lastHurtBy != null && ts != this.timestamp && SummonEntity.this.canAttack(this.lastHurtBy);
      }

      @Override
      public void start() {
         setTarget(this.lastHurtBy);
         LivingEntity owner = getOwner();
         if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
         }
         super.start();
      }
   }

   /** Cible la dernière entité blessée par le propriétaire. */
   private class OwnerHurtTargetGoal extends TargetGoal {
      private LivingEntity lastHurt;
      private int timestamp;

      OwnerHurtTargetGoal() {
         super(SummonEntity.this, false);
      }

      @Override
      public boolean canUse() {
         LivingEntity owner = getOwner();
         if (owner == null) {
            return false;
         }
         this.lastHurt = owner.getLastHurtMob();
         int ts = owner.getLastHurtMobTimestamp();
         return this.lastHurt != null && ts != this.timestamp && SummonEntity.this.canAttack(this.lastHurt);
      }

      @Override
      public void start() {
         setTarget(this.lastHurt);
         LivingEntity owner = getOwner();
         if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
         }
         super.start();
      }
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<SummonEntity>("movement", 0,
         (AnimationTest<SummonEntity> state) -> {
            if(((net.mcreator.minepiece.procedures.ParameciaStoneData)this).minepiece$stoneFlag())return PlayState.STOP;
            String requested = getAnimation();
            if (!requested.equals("empty")) return state.setAndContinue(RawAnimation.begin().thenPlay(requested));
            String locomotion = state.isMoving() && (getModel().equals("gecko_moria")
               || getModel().startsWith("note_warrior") || getModel().equals("kumac") || getModel().equals("biscuit_warrrior")) ? "walk" : "idle";
            return state.setAndContinue(RawAnimation.begin().thenLoop(locomotion));
         }));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
