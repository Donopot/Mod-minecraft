package com.donopot.voiddynasty.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.AnimationTest;
import com.donopot.voiddynasty.registry.ModSounds;
import com.donopot.voiddynasty.weapon.BoltProjectile;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynasty.weapon.WeaponDefinitions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Char Leman Russ — PILOTABLE.
 *
 * <p>Décision de Donopot du 16/10/2025 : « non je veux du pilotable ». L'option statique a été
 * écartée après avoir été proposée.
 *
 * <p><b>Ce qui vient de la source</b> : le modèle (33 cubes) et sa texture. La source ne fournit
 * RIEN d'autre — dans la campagne, ce char était un décor posé, sans vie, sans canon jouable. Son
 * propre catalogue le classe pourtant en « véhicule » (CATALOGUE-RECUPERATION.md) : le traiter en
 * entité n'est pas une invention, c'est le ranger dans la bonne catégorie.
 *
 * <p><b>Ce qui est de la CONCEPTION, et que je déclare ici plutôt que de le déguiser en portage</b> :
 * <ul>
 *   <li>les points de vie (100) et l'armure (10) : aucune source ne les donne ;</li>
 *   <li>la vitesse de conduite (0,22) : c'est un point de départ à régler à l'œil ;</li>
 *   <li>la place du conducteur sur la caisse ;</li>
 *   <li>l'ABSENCE d'animation : la source n'en fournit aucune et je n'en invente pas. Le char ne
 *       fait donc pas défiler ses chenilles et sa tourelle ne tourne pas ;</li>
 *   <li>le canon ne tire pas : rien dans la source ne décrit une arme jouable.</li>
 * </ul>
 */
public class LemanRussEntity extends PathfinderMob implements GeoEntity {

    /** CONCEPTION : vitesse de conduite, en blocs par tick, à régler par Donopot. */
    /** Homothetie du modele, calee sur la planche technique : 4.42 m / 1.83 bloc = 2.4153.
     *  Source : docs/REFERENCE-LEMAN-RUSS.md (hauteur 4.42 m, largeur 4.86 m, caisse 7.08 m). */
    public static final float ECHELLE_PLANCHE = 2.4153f;

    public static final float VITESSE_CONDUITE = 0.22f;

    /** CONCEPTION : cadence de tir, en ticks. Un obus de char se recharge lentement. */
    public static final int CADENCE_TIR = 40;

    /** Temps de recharge restant, en ticks. */
    private int recharge = 0;

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public LemanRussEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /** CONCEPTION : 100 PV et 10 d'armure. La source ne donne aucun chiffre (c'était un décor). */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.MOVEMENT_SPEED, VITESSE_CONDUITE);
    }

    @Override
    protected void registerGoals() {
        // AUCUN but. Un véhicule sans conducteur reste où il est : il ne patrouille pas, ne poursuit
        // personne, ne fuit pas. C'est la différence entre un char garé et un monstre.
    }

    /** On ne pousse pas quarante tonnes : le char ne se laisse déplacer que par son conducteur. */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** Un véhicule garé ne disparaît pas parce que le joueur s'éloigne. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ------------------------------------------------------------------ tir
    /** Bouche du canon : devant et au-dessus du char, d'après le gabarit mesuré du modèle. */
    public Vec3 boucheDuCanon() {
        double lacet = Math.toRadians(this.getYRot());
        Vec3 avant = new Vec3(-Math.sin(lacet), 0.0, Math.cos(lacet));   // l'avant du char, à plat
        return this.position().add(avant.scale(1.5)).add(0.0, 1.5, 0.0);
    }

    /** Reste-t-il à recharger ? (lu par le harnais) */
    public boolean enRechargement() {
        return this.recharge > 0;
    }

    /**
     * Tire un obus. Prend l'origine et la direction en paramètres plutôt qu'un joueur : le harnais
     * peut ainsi éprouver le tir sans avoir à faire monter quelqu'un — ce qu'un serveur sans joueur
     * ne saurait pas faire.
     *
     * <p><b>Ce qui est de la CONCEPTION, déclaré :</b> la source ne donne aucun chiffre pour un canon
     * de char. On emprunte donc la balistique de l'arme lourde du mod (le missile Hunter-Killer) :
     * une valeur dérivée de notre propre table, pas inventée — et qui reste à régler à l'œil.
     * La tourelle ne pivote pas (le modèle est d'un seul tenant) : l'obus part dans la direction
     * visée par le conducteur, pas dans celle du canon dessiné.
     */
    public boolean tirer(ServerLevel niveau, Vec3 origine, Vec3 direction, net.minecraft.world.entity.LivingEntity tireur) {
        if (this.recharge > 0) {
            return false;
        }
        WeaponDefinition obus = WeaponDefinitions.HUNTER_KILLER_MISSILE;
        BoltProjectile projectile = new BoltProjectile(niveau, tireur, obus);
        projectile.setPos(origine.x, origine.y, origine.z);
        Vec3 d = direction.normalize();
        projectile.shoot(d.x, d.y, d.z, obus.projectile().speed() / 20f, 0.4f);
        niveau.addFreshEntity(projectile);
        niveau.playSound(null, this.blockPosition(), ModSounds.MISSILE_FIRE, SoundSource.HOSTILE, 1.4f, 0.7f);
        this.recharge = CADENCE_TIR;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.recharge > 0) {
            --this.recharge;
        }
    }

    // ------------------------------------------------------------------ conduite
    /** Le conducteur : le premier passager, s'il est joueur. C'est ce que lit le moteur du jeu. */
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player joueur ? joueur : null;
    }

    /** Un seul siège. Deux conducteurs pour un char, c'est un char qui n'obéit à personne. */
    @Override
    protected boolean canAddPassenger(Entity passager) {
        return this.getPassengers().isEmpty();
    }

    /** Entrées du conducteur : il avance, il recule (plus lentement), et il peut infléchir. */
    @Override
    protected Vec3 getRiddenInput(Player joueur, Vec3 deplacement) {
        float cote = joueur.xxa * 0.5f;
        float avant = joueur.zza;
        if (avant <= 0.0f) {
            avant *= 0.25f;                 // reculer un char se fait au ralenti
        }
        return new Vec3(cote, 0.0, avant);
    }

    @Override
    protected float getRiddenSpeed(Player joueur) {
        return VITESSE_CONDUITE;
    }

    /** Place du conducteur : sur la caisse, un peu en arrière de la tourelle. À régler à l'œil. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passager, EntityDimensions dimensions, float echelle) {
        // Le siege suit l'echelle du modele : la planche technique donne 4.42 m de haut, notre
        // source 1.83 bloc, soit 4.42/1.83 = 2.4153. Le point d'assise est donc mis a l'echelle
        // par la meme homothetie que le modele, il garde sa place sur le toit de caisse.
        return new Vec3(0.0, 1.55, -0.3).scale(ECHELLE_PLANCHE).scale(echelle);
    }

    /** Monter : main droite sur le char, sans s'accroupir, et sans rien consommer. */
    @Override
    public InteractionResult mobInteract(Player joueur, InteractionHand main) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (this.isVehicle()) {
            return InteractionResult.PASS;      // déjà conduit
        }
        return joueur.startRiding(this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // ------------------------------------------------------------------ animation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // La source ne fournit AUCUNE animation pour ce char : il n'y a rien à porter. Le clip
        // « idle » ne déplace aucun os, il maintient la pose du modèle. Je préfère un char immobile
        // et une décision déclarée à des chenilles inventées.
        controllers.add(new AnimationController<LemanRussEntity>("controller", 0,
                (AnimationTest<LemanRussEntity> state) ->
                        state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
