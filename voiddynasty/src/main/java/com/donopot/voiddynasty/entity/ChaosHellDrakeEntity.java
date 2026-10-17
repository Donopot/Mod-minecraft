package com.donopot.voiddynasty.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.AnimationTest;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * HellDrake — phase E, lot 6. Le dernier ennemi du Chaos.
 *
 * La source l'invoque comme un PHANTOM invisible (500 PV, 50 dégâts, follow_range 1000, échelle 2) sur
 * lequel elle téléporte le modèle Animated Java, plus un ghast invisible portant la seconde moitié du
 * modèle, et une barre de boss pilotée sur les PV. On étend donc Phantom, exactement comme la source :
 * le vol, la poursuite et l'attaque au contact viennent du jeu, pas d'une réinvention.
 *
 * Son animation « flying » n'existe pas dans le resource pack : elle vit dans les datapacks, sous
 * forme de 19 images de matrices 4x4 (helldrake/on_load.mcfunction). tools/convert_helldrake.py les
 * transcrit en clip GeckoLib — 133 transformations, 19 images x 7 os. Aucune animation n'est inventée.
 *
 * Limite assumée : quand la créature est chevauchée, la source rejoue « servitorwalk », l'animation
 * d'un AUTRE rig (le servitor). Ce clip ne nomme pas les os du HellDrake : le rejouer ici ne
 * déplacerait rien. On ne le simule donc pas, et on le dit plutôt que de laisser croire le contraire.
 */
public class ChaosHellDrakeEntity extends Phantom implements GeoEntity {

    private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(),
            Component.translatable("entity.voiddynasty.chaos_helldrake"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public ChaosHellDrakeEntity(EntityType<? extends Phantom> type, Level level) {
        super(type, level);
        this.setPhantomSize(1);        // Size:1 dans la source
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Les attributs du phantom vanilla ne sont pas exposés : on part donc d'une base de créature.
        // Les quatre valeurs de la source sont appliquées telles quelles. Les vitesses, elles, ne
        // viennent PAS de la source — celle-ci ne surcharge que ces quatre-là — mais d'une base ;
        // c'est dit ici pour que personne ne les prenne pour des valeurs portées.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.FOLLOW_RANGE, 1000.0)
                .add(Attributes.SCALE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.FLYING_SPEED, 0.1);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getMaxHealth() <= 0f ? 0f : this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ChaosHellDrakeEntity>("controller", 0,
                (AnimationTest<ChaosHellDrakeEntity> state) ->
                        state.setAndContinue(RawAnimation.begin().thenLoop("flying"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
