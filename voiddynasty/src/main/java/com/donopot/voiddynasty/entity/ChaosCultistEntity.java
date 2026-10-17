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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Base commune des Cultistes du Chaos (phase E).
 *
 * Les valeurs viennent de la source, pas de l'intuition : les datapacks Astartes donnent 20 PV et
 * 3-6 dégâts par cultiste, 100 PV et 5-8 dégâts pour la Prêtresse, sur une base zombie
 * (vitesse 0.23) sans armure. Les dégâts sont des bornes dans la source ; on prend leur milieu
 * (4.5 / 6.5) faute de pouvoir exprimer un intervalle dans un attribut.
 *
 * Les noms de clips sont ceux RÉELLEMENT présents après conversion des blueprints Animated Java.
 * La source ne fournit pas de clip d'arrêt pour les cultistes 2, 3, 4 ni pour la Prêtresse : la passe
 * de finition a repris l'idle du culte 1 (même squelette, 14 à 15 os communs) — décision déclarée,
 * voir tools/ajouter_idle_cultistes.py. Le repli sur la marche reste, en secours.
 */
public abstract class ChaosCultistEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_ATTACKING =
            SynchedEntityData.defineId(ChaosCultistEntity.class, EntityDataSerializers.BOOLEAN);

    /** Distingue l'attaque en cours : tir ou contact. La source a deux animations différentes. */
    private static final EntityDataAccessor<Boolean> DATA_TIR =
            SynchedEntityData.defineId(ChaosCultistEntity.class, EntityDataSerializers.BOOLEAN);

    /** Capacité en cours, au-delà des deux attaques de base (0 = aucune). Voir clipSupplementaire. */
    private static final EntityDataAccessor<Integer> DATA_CAPACITE =
            SynchedEntityData.defineId(ChaosCultistEntity.class, EntityDataSerializers.INT);

    private int cooldownTir = 0;
    private int cooldownCac = 0;

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private int attackAnimTick = 0;

    protected ChaosCultistEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ---------------------------------------------------------------- profil
    /**
     * Plages de détection et cooldowns de ce mob, transcrits de la source. Abstrait à dessein :
     * chaque créature DOIT déclarer les siens, on ne laisse pas un défaut silencieux décider à sa place.
     */
    public abstract ProfilAttaque profil();

    /**
     * Dégâts d'un projectile. Par défaut ceux de l'attribut d'attaque, mais la source en donne parfois
     * DEUX différents pour un même mob : le forgefiend frappe à 30 au contact et tire à 10 par obus.
     */
    protected float degatsTir() {
        return (float) getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
    }

    /**
     * Nombre de projectiles par tir. La source en fait partir CINQ au forgefiend (« 5 raytraces » d'une
     * même salve) ; partout ailleurs un seul. La valeur n'est pas devinée : elle vient de la source.
     */
    protected int nombreProjectiles() {
        return 1;
    }

    // ---------------------------------------------------------- cooldowns
    public int cooldownTir() {
        return this.cooldownTir;
    }

    public void setCooldownTir(int ticks) {
        this.cooldownTir = Math.max(0, ticks);
    }

    public int cooldownCac() {
        return this.cooldownCac;
    }

    public void setCooldownCac(int ticks) {
        this.cooldownCac = Math.max(0, ticks);
    }

    // ---------------------------------------------------------------- clips
    /** Clip joué à l'arrêt. Retombe sur la marche si la source n'en fournit pas. */
    protected String clipIdle() {
        return "walk";
    }

    protected abstract String clipWalk();

    /** Clip d'attaque au contact, ou {@code null} si la source n'en fournit pas. */
    protected String clipAttack() {
        return null;
    }

    /**
     * Clip de tir. La source distingue le tir de la mêlée : certaines de ses créatures n'ont que l'un
     * des deux. On préfère le clip de mêlée quand il existe, sinon le tir — mais AUCUNE attaque n'est
     * inventée : si la source ne fournit rien, la créature attaque sans animation dédiée.
     */
    protected String clipShoot() {
        return null;
    }

    /** Clip effectivement joué à l'attaque : mêlée si la source en donne un, sinon tir. */
    protected String clipAttaque() {
        String melee = clipAttack();
        return melee != null ? melee : clipShoot();
    }

    /** Clip de mort, ou {@code null} si la source n'en fournit pas. */
    protected String clipDeath() {
        return null;
    }

    /**
     * Clip d'une capacité nommée, pour les créatures qui en ont PLUS de deux (le Prince Démon a une
     * charge et un smash en plus de sa frappe). Défaut : aucune — une créature ne gagne pas d'attaque
     * qu'on n'a pas écrite.
     *
     * @param capacite identifiant de capacité, choisi par la créature elle-même
     */
    protected String clipSupplementaire(int capacite) {
        return null;
    }

    /** Lance une capacité nommée : joue son clip et pose sa durée d'affichage. */
    public void declencherCapacite(int capacite, int dureeTicks) {
        this.attackAnimTick = dureeTicks;
        this.entityData.set(DATA_ATTACKING, true);
        this.entityData.set(DATA_CAPACITE, capacite);
    }

    public int capaciteEnCours() {
        return this.entityData.get(DATA_CAPACITE);
    }

    /** Durée d'affichage de la pose d'attaque, en ticks (borne haute de la fenêtre de test). */
    protected int attackAnimTicks() {
        return 20;
    }

    // ------------------------------------------------------------ attributs
    /** Vitesse et portée de détection de la source (base zombie) — dites ici, pas héritées en silence. */
    protected static AttributeSupplier.Builder baseAttributes(double pv, double degats, double vitesse) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, pv)
                .add(Attributes.ATTACK_DAMAGE, degats)
                .add(Attributes.MOVEMENT_SPEED, vitesse)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 0.0);
    }

    // ------------------------------------------------------ attaque animée
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, false);
        builder.define(DATA_TIR, false);
        builder.define(DATA_CAPACITE, 0);
    }

    /** Déclenche la pose d'attaque : {@code tir} = true pour le tir, false pour le contact. */
    public void declencherAttaque(boolean tir) {
        this.attackAnimTick = attackAnimTicks();
        this.entityData.set(DATA_ATTACKING, true);
        this.entityData.set(DATA_TIR, tir);
    }

    public boolean estTir() {
        return this.entityData.get(DATA_TIR);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.entityData.set(DATA_ATTACKING, true);
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackAnimTick > 0 && --this.attackAnimTick == 0) {
            this.entityData.set(DATA_ATTACKING, false);
            this.entityData.set(DATA_CAPACITE, 0);
        }
        if (this.cooldownTir > 0) {
            this.cooldownTir--;
        }
        if (this.cooldownCac > 0) {
            this.cooldownCac--;
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CacChaosGoal(this));
        this.goalSelector.addGoal(3, new TirChaosGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ChaosCultistEntity>("controller", 0,
                (AnimationTest<ChaosCultistEntity> state) -> {
                    ChaosCultistEntity mob = state.animatable();
                    String mort = mob.clipDeath();
                    if (mort != null && mob.isDeadOrDying()) {
                        return state.setAndContinue(RawAnimation.begin().thenPlay(mort));
                    }
                    String capacite = mob.clipSupplementaire(mob.capaciteEnCours());
                    String tir = mob.clipShoot();
                    String attaque = capacite != null ? capacite
                            : (mob.estTir() && tir != null) ? tir : mob.clipAttaque();
                    if (mob.isAttacking() && attaque != null) {
                        return state.setAndContinue(
                                RawAnimation.begin().thenPlay(attaque).thenLoop(mob.clipIdle()));
                    }
                    if (state.isMoving()) {
                        return state.setAndContinue(RawAnimation.begin().thenLoop(mob.clipWalk()));
                    }
                    return state.setAndContinue(RawAnimation.begin().thenLoop(mob.clipIdle()));
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
