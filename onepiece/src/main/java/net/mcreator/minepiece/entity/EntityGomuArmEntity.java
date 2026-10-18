package net.mcreator.minepiece.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import net.mcreator.minepiece.init.MinepieceModAttributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * « Bras de Gomu Gomu » — entité fake (NoAI + invulnérable) jouant l'animation du coup étiré.
 * Port Fabric 26.3 de {@code EntityGomuArmEntity} (ver15) sur GeckoLib 5.5.7.
 * Deux contrôleurs d'animation (idle + procédure one-shot), suivi du propriétaire, texture dynamique,
 * échelle via SIZE. Propriétaire synchronisé pour le skin Gomu ; armure/item et base TamableAnimal différés.
 */
public class EntityGomuArmEntity extends Monster implements GeoEntity {
   public static final EntityDataAccessor<String> MODEL =
      SynchedEntityData.defineId(EntityGomuArmEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE =
      SynchedEntityData.defineId(EntityGomuArmEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> ANIMATION =
      SynchedEntityData.defineId(EntityGomuArmEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> VISUAL_PROFILE =
      SynchedEntityData.defineId(EntityGomuArmEntity.class, EntityDataSerializers.STRING);
   /** Étirement du bras (1.0 = portée de l'animation, 3.0 = trois fois plus loin). */
   public static final EntityDataAccessor<Float> STRETCH =
      SynchedEntityData.defineId(EntityGomuArmEntity.class, EntityDataSerializers.FLOAT);

   public static final EntityDataAccessor<Float> THICKNESS=SynchedEntityData.defineId(EntityGomuArmEntity.class,EntityDataSerializers.FLOAT);
   /** Rendering ownership only; client skins must never be guessed from proximity. */
   public static final EntityDataAccessor<Integer> OWNER_ID=SynchedEntityData.defineId(EntityGomuArmEntity.class,EntityDataSerializers.INT);
   public float getThickness(){return entityData.get(THICKNESS);}
   public void setThickness(float thickness){entityData.set(THICKNESS,Math.max(.1F,Math.min(4F,thickness)));}

   private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   private String prevAnim = "empty";
   private Entity owner;
   private boolean followOwner = true;
   private int lifeTicks = 0;
   private int maxLifeTicks = 15;

   public EntityGomuArmEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.setNoAi(true);
      this.setNoGravity(true); // Pure visual anchors must not fall through the floor.
      this.setPermanentlyInvulnerable(true);
      this.noPhysics = true; // visuel pur : aucune collision ni poussée sur le joueur
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(MODEL, "entity_gomu_arm");
      builder.define(TEXTURE, "luffy");
      builder.define(ANIMATION, "empty");
      builder.define(VISUAL_PROFILE, "");
      builder.define(STRETCH, 1.0F);
      builder.define(THICKNESS,1F);
      builder.define(OWNER_ID,-1);
   }

   public String getModel() {
      return this.entityData.get(MODEL);
   }

   public void setModel(String model) {
      this.entityData.set(MODEL, model);
   }

   public String getTexture() {
      return this.entityData.get(TEXTURE);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public void setAnimation(String animation) {
      this.entityData.set(ANIMATION, animation);
   }
   public void setVisualProfile(String profile) { this.entityData.set(VISUAL_PROFILE, profile); }
   public String getVisualProfile() { return this.entityData.get(VISUAL_PROFILE); }

   /** Propriétaire (côté serveur) : l'entité suit sa position/rotation chaque tick si {@code followOwner}. */
   public void setOwnerEntity(Entity owner) {
      this.owner = owner;
      this.entityData.set(OWNER_ID,owner==null?-1:owner.getId());
   }

   public Entity ownerEntity() { return owner!=null?owner:level().getEntity(entityData.get(OWNER_ID)); }

   public EntityGomuArmEntity followOwner(boolean follow) {
      this.followOwner = follow;
      return this;
   }

   /**
    * Étirement du bras, lu par le rendu : le facteur qui porte le poing du modèle jusqu'au point
    * d'impact (FRUIT-GOMU_GOMU-03). Synchronisé car le serveur seul connaît la distance réelle.
    */
   public float getStretch() {
      return this.entityData.get(STRETCH);
   }

   public void setStretch(float stretch) {
      this.entityData.set(STRETCH, Math.max(0.05F, stretch));
   }

   /** Durée de vie maximale (bornage vérifiable : un membre ne peut pas rester indéfiniment). */
   public int getMaxLifeTicksForTest() {
      return this.maxLifeTicks;
   }

   public EntityGomuArmEntity lifetime(int ticks) {
      this.maxLifeTicks = ticks;
      return this;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 10.0)
         .add(Attributes.MOVEMENT_SPEED, 0.0)
         .add(Attributes.ATTACK_DAMAGE, 0.0)
         .add(Attributes.FOLLOW_RANGE, 0.0)
         .add(MinepieceModAttributes.SIZE, 1.0);
   }

   @Override
   protected void registerGoals() {
      // faux bras : aucune IA
   }

   @Override
   public void tick() {
      super.tick();
      if (this.followOwner && this.owner != null && !this.owner.isRemoved()) {
         // Suit à hauteur d'œil (pas aux pieds) pour être visible en FPS.
         this.setPos(this.owner.getX(), this.owner.getEyeY(), this.owner.getZ());
         this.setYRot(this.owner.getYRot());
         this.setXRot(this.owner.getXRot());
      }
      // The lifetime is server-owned. The client never receives this private budget;
      // expiring locally at its default 15 ticks hid long pillars/charges before server removal.
      if (!level().isClientSide() && ++this.lifeTicks > this.maxLifeTicks) {
         this.discard();
      }
   }

   /** Échelle (port de {@code SetSizeProcedure} + attribut SIZE, ex. Gear Third = ×1.66). */
   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      double scale = 1.0;
      AttributeInstance size = this.getAttribute(MinepieceModAttributes.SIZE);
      if (size != null) {
         scale = size.getValue();
      }
      return super.getDefaultDimensions(pose).scale((float) scale);
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      // contrôleur idle (boucle)
      controllers.add(new AnimationController<EntityGomuArmEntity>("movement", 0,
         (AnimationTest<EntityGomuArmEntity> state) -> this.entityData.get(ANIMATION).equals("empty")
            ? state.setAndContinue(RawAnimation.begin().thenLoop("idle")) : PlayState.STOP));
      // contrôleur procédure (one-shot) — port de procedurePredicate ver15
      controllers.add(new AnimationController<EntityGomuArmEntity>("procedure", 0,
         (AnimationTest<EntityGomuArmEntity> state) -> this.procedurePredicate(state)));
   }

   private PlayState procedurePredicate(AnimationTest<EntityGomuArmEntity> state) {
      String anim = this.entityData.get(ANIMATION);
      if (anim.equals("empty")) {
         this.prevAnim = "empty";
         return PlayState.STOP;
      }
      if (!anim.equals(this.prevAnim)) {
         state.controller().reset();
         this.prevAnim = anim;
      }
      return state.setAndContinue(RawAnimation.begin().thenPlay(anim));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
