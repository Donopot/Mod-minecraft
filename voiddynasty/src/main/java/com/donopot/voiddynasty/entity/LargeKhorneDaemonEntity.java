package com.donopot.voiddynasty.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.AnimationTest;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Grand démon de Khorne : entité volante gigantesque
 */
public class LargeKhorneDaemonEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(LargeKhorneDaemonEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private int attackAnimTick = 0;

    public LargeKhorneDaemonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 11.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, false);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.attackAnimTick = 20;
        this.entityData.set(DATA_ATTACKING, true);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackAnimTick > 0 && --this.attackAnimTick == 0) {
            this.entityData.set(DATA_ATTACKING, false);
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<LargeKhorneDaemonEntity>("controller", 0,
                (AnimationTest<LargeKhorneDaemonEntity> state) -> {
                    if (state.animatable().isAttacking()) {
                        return state.setAndContinue(RawAnimation.begin().thenPlay("attack").thenLoop("largekhornedaemon.model.idle"));
                    }
                    if (state.isMoving()) {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("largekhornedaemon.model.flight"));
                    }
                    return state.setAndContinue(RawAnimation.begin().thenLoop("largekhornedaemon.model.idle"));
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
