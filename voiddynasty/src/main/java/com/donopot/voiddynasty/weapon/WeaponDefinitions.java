package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.registry.ModItems;
import com.donopot.voiddynasty.registry.ModSounds;
import com.donopot.voiddynasty.terrain.TerrainDamageSpec;
import com.donopot.voiddynasty.terrain.TerrainDamageSpec.StatusEffects;
import com.donopot.voiddynasty.terrain.TerrainDamageSpec.TerrainMode;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Les 15 armes du mod, alignées sur le schéma void_dynasty:weapon/v2.
 */
public final class WeaponDefinitions {

    private static final DustParticleOptions LASER_TRACER =
            new DustParticleOptions(ARGB.color(255, 60, 60), 2.5f);   // rouge laser
    private static final DustParticleOptions PLASMA_TRACER =
            new DustParticleOptions(ARGB.color(80, 255, 160), 2.0f);   // vert plasma

    private static final StatusEffects NO_HEAT = StatusEffects.NONE;

    // ---- Bolt (cinétique) ----
    public static final WeaponDefinition BOLTGUN =
            new WeaponDefinition("bolt", FireMode.SEMI,
                    15f, 72, 115, 1.5f, 0.10f, 1.5f, 0.40f, 4.0f, 1, 24, 2.6f,
                    ModItems.BOLT_SHELL,
                    new WeaponDefinition.ProjectileSpec(105f, 0.4f, 0, 0, 0),
                    List.of(EffectType.DELAYED_DETONATION, EffectType.STAGGER),
                    2.5f, 1.12f, 2.1f, 0.63f, 0.33f,
                    ModSounds.BOLTGUN_FIRE, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME,
                    null, false, 0f,
                    terrain(TerrainMode.EXPLOSIVE_PENETRATOR, 16f, 0.8f, 1, 3, 0.42f, 0.1f, NO_HEAT));

    public static final WeaponDefinition BOLT_PISTOL =
            new WeaponDefinition("bolt", FireMode.SEMI,
                    7f, 42, 67, 1.5f, 0.10f, 1.5f, 0.32f, 1.0f, 1, 12, 2.0f,
                    ModItems.BOLT_SHELL,
                    new WeaponDefinition.ProjectileSpec(85f, 0.65f, 0, 0, 0),
                    List.of(EffectType.DELAYED_DETONATION),
                    2.1f, 0.95f, 3.0f, 0.9f, 0.27f,
                    ModSounds.BOLT_PISTOL_FIRE, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME,
                    null, false, 0f,
                    terrain(TerrainMode.EXPLOSIVE_PENETRATOR, 11f, 0.55f, 1, 2, 0.3f, 0.1f, NO_HEAT));

    public static final WeaponDefinition HEAVY_BOLTER =
            new WeaponDefinition("bolt", FireMode.AUTOMATIC,
                    12f, 90, 144, 1.5f, 0.08f, 1.5f, 0.48f, 8.0f, 1, 80, 5.0f,
                    ModItems.HEAVY_BOLT_SHELL,
                    new WeaponDefinition.ProjectileSpec(115f, 0.35f, 0, 0, 0),
                    List.of(EffectType.DELAYED_DETONATION, EffectType.SUPPRESSION),
                    3.8f, 1.71f, 3.6f, 1.08f, 0.49f,
                    ModSounds.HEAVY_BOLTER_FIRE, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME,
                    null, false, 0f,
                    terrain(TerrainMode.EXPLOSIVE_PENETRATOR, 21f, 1.1f, 2, 4, 0.55f, 0.15f, NO_HEAT));

    public static final WeaponDefinition STALKER_BOLT_RIFLE =
            new WeaponDefinition("bolt", FireMode.SEMI,
                    30f, 155, 248, 2.0f, 0.25f, 2.1f, 0.65f, 0.5f, 1, 8, 2.9f,
                    ModItems.STALKER_BOLT_ROUND,
                    new WeaponDefinition.ProjectileSpec(180f, 0.15f, 1, 0, 0),
                    List.of(EffectType.DELAYED_DETONATION),
                    3.0f, 1.35f, 4.2f, 1.26f, 0.39f,
                    ModSounds.BOLTGUN_FIRE, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME,
                    null, false, 0f,
                    terrain(TerrainMode.ARMOR_PIERCING, 24f, 0.35f, 3, 3, 0.72f, 0.07f, NO_HEAT));

    // ---- Laser (hitscan, speed = 0) ----
    public static final WeaponDefinition LAS_GUN =
            new WeaponDefinition("las", FireMode.AUTOMATIC,
                    5f, 90, 144, 1.5f, 0.10f, 1.5f, 0.20f, 5.0f, 1, 40, 2.2f,
                    ModItems.LAS_CELL,
                    new WeaponDefinition.ProjectileSpec(0f, 0f, 0, 0, 0),
                    List.of(EffectType.IGNITE),
                    0.25f, 0.11f, 1.0f, 0.3f, 0.03f,
                    ModSounds.LAS_GUN_FIRE, LASER_TRACER, ParticleTypes.ENCHANTED_HIT,
                    null, false, 0f,
                    // LAS_GUN — décision de Donopot (16/10/2025) : le laser doit DÉTRUIRE la pierre après
                    // beaucoup de tirs, toucher d'autres blocs à force de tirer, et ENFLAMMER les matières
                    // inflammables. Les valeurs de la source (9 de puissance, 28 % de percement, aucun feu)
                    // ne le permettaient pas : la résistance de la pierre annulait chaque impact.
                    terrain(TerrainMode.THERMAL_ABLATION, 15f, 0.35f, 0, 6, 0.45f, 0.85f,
                            new StatusEffects(true, 0.25f, true, false, true)));

    public static final WeaponDefinition LAZ_PISTOL =
            new WeaponDefinition("las", FireMode.AUTOMATIC,
                    3f, 45, 72, 1.5f, 0.08f, 1.5f, 0.12f, 4.0f, 1, 24, 1.7f,
                    ModItems.LAS_CELL,
                    new WeaponDefinition.ProjectileSpec(0f, 0f, 0, 0, 0),
                    List.of(EffectType.IGNITE),
                    0.20f, 0.09f, 2.0f, 0.6f, 0.03f,
                    ModSounds.LAS_GUN_FIRE, LASER_TRACER, ParticleTypes.ENCHANTED_HIT,
                    null, false, 0f,
                    // LAZ_PISTOL — même décision, en plus faible : il finit par percer, et il allume.
                    terrain(TerrainMode.THERMAL_ABLATION, 9f, 0.25f, 0, 3, 0.35f, 0.7f,
                            new StatusEffects(true, 0.15f, false, false, true)));

    // ---- Plasma ----
    public static final WeaponDefinition PLASMA_PISTOL =
            new WeaponDefinition("plasma", FireMode.SEMI,
                    10f, 46, 74, 1.5f, 0.10f, 1.5f, 0.72f, 1.25f, 1, 8, 3.2f,
                    ModItems.PLASMA_CELL,
                    new WeaponDefinition.ProjectileSpec(55f, 0.2f, 0, 1.2f, 7f),
                    List.of(EffectType.BURN),
                    2.7f, 1.22f, 1.2f, 0.36f, 0.35f,
                    ModSounds.PLASMA_PISTOL_FIRE, PLASMA_TRACER, ParticleTypes.EXPLOSION_EMITTER,
                    null, false, 0f,
                    terrain(TerrainMode.PLASMA_SPLASH, 29f, 1.5f, 1, 8, 0.65f, 0.85f,
                            new StatusEffects(true, 0.26f, true, true, true)));

    public static final WeaponDefinition PLASMA_INCINERATOR =
            new WeaponDefinition("plasma", FireMode.SEMI,
                    20f, 83, 133, 1.5f, 0.12f, 1.5f, 0.82f, 0.33f, 1, 16, 4.4f,
                    ModItems.PLASMA_CELL,
                    new WeaponDefinition.ProjectileSpec(65f, 0.12f, 0, 2.3f, 15f),
                    List.of(EffectType.BURN, EffectType.ARMOR_MELT),
                    3.5f, 1.57f, 1.4f, 0.42f, 0.46f,
                    ModSounds.PLASMA_INCINERATOR_FIRE, PLASMA_TRACER, ParticleTypes.EXPLOSION_EMITTER,
                    null, false, 0f,
                    terrain(TerrainMode.PLASMA_SPLASH, 70f, 2.5f, 2, 24, 0.8f, 0.95f,
                            new StatusEffects(true, 0.28f, true, true, true)));

    // ---- Solid (fusil à pompe) ----
    public static final WeaponDefinition SHOTGUN =
            new WeaponDefinition("solid", FireMode.SEMI,
                    20f, 20, 32, 1.5f, 0.05f, 1.5f, 0.12f, 0.67f, 5, 8, 3.1f,
                    ModItems.SHOTGUN_SHELL,
                    new WeaponDefinition.ProjectileSpec(95f, 0.95f, 0, 0, 0),
                    List.of(EffectType.STAGGER),
                    3.1f, 1.4f, 9.0f, 2.7f, 0.4f,
                    ModSounds.SHOTGUN_FIRE, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME,
                    new WeaponDefinition.DamageFalloff(7f, 30f, 0.2f), true, 0.55f,
                    terrain(TerrainMode.PELLET, 5f, 0.18f, 0, 1, 0.11f, 0.02f, NO_HEAT));

    // ---- Flame ----
    public static final WeaponDefinition FLAMETHROWER =
            new WeaponDefinition("flame", FireMode.CONTINUOUS,
                    7f, 13, 21, 1.5f, 0.02f, 1.5f, 0.08f, 10.0f, 5, 100, 4.0f,
                    ModItems.PROMETHIUM,
                    new WeaponDefinition.ProjectileSpec(18f, 0f, 0, 0, 0),
                    List.of(EffectType.INFERNO, EffectType.PANIC),
                    0.1f, 0.05f, 18.0f, 3.9f, 0.01f,
                    ModSounds.FLAMETHROWER_FIRE, ParticleTypes.FLAME, ParticleTypes.FLAME,
                    null, false, 0f,
                    terrain(TerrainMode.CONE_HEAT, 3f, 1.2f, 0, 16, 0.04f, 0.95f,
                            new StatusEffects(true, 0.9f, true, true, true)));

    // ---- Melta (hitscan, speed = 0) ----
    public static final WeaponDefinition MELTA_GUN =
            new WeaponDefinition("melta", FireMode.SEMI,
                    50f, 17, 27, 1.5f, 0.15f, 1.5f, 0.95f, 0.5f, 1, 5, 3.8f,
                    ModItems.MELTA_CELL,
                    new WeaponDefinition.ProjectileSpec(80f, 0f, 0, 0, 0),
                    List.of(EffectType.ARMOR_MELT, EffectType.BURN),
                    3.5f, 1.57f, 0.6f, 0.18f, 0.46f,
                    ModSounds.MELTA_FIRE, ParticleTypes.FLAME, ParticleTypes.EXPLOSION_EMITTER,
                    new WeaponDefinition.DamageFalloff(5f, 17f, 0.35f), false, 0f,
                    terrain(TerrainMode.THERMAL_BEAM, 110f, 0.6f, 5, 20, 0.98f, 1.0f,
                            new StatusEffects(true, 0.8f, true, true, true)));

    // ---- Explosif ----
    public static final WeaponDefinition HUNTER_KILLER_MISSILE =
            new WeaponDefinition("explosive", FireMode.SEMI,
                    100f, 175, 280, 1.5f, 0.20f, 1.5f, 0.65f, 0.2f, 1, 1, 7.5f,
                    ModItems.MISSILE_AMMO,
                    new WeaponDefinition.ProjectileSpec(38f, 0.05f, 0, 5f, 28f),
                    List.of(EffectType.STAGGER),
                    4.5f, 2.02f, 0.15f, 0.04f, 0.58f,
                    ModSounds.MISSILE_FIRE, ParticleTypes.FLAME, ParticleTypes.EXPLOSION_EMITTER,
                    null, false, 0f,
                    terrain(TerrainMode.WARHEAD, 90f, 3.5f, 3, 60, 0.82f, 0.65f,
                            new StatusEffects(true, 0.2f, true, false, false)));

    // ---- Mêlée (ammo = null, munitions infinies) ----
    public static final WeaponDefinition CHAINSWORD =
            new WeaponDefinition("melee", FireMode.MELEE,
                    10f, 3.3, 5, 1.5f, 0.10f, 1.5f, 0.15f, 1.5f, 0, 0, 0f,
                    null,
                    new WeaponDefinition.ProjectileSpec(0f, 0f, 0, 0, 0),
                    List.of(EffectType.BLEED, EffectType.STAGGER),
                    0.5f, 0.23f, 0f, 0f, 0.07f,
                    ModSounds.MELEE_SWING, ParticleTypes.SWEEP_ATTACK, ParticleTypes.CRIT,
                    null, false, 0f,
                    terrain(TerrainMode.MELEE_CUT, 9f, 0f, 0, 2, 0.18f, 0f, NO_HEAT, 8));

    public static final WeaponDefinition POWERSWORD =
            new WeaponDefinition("melee", FireMode.MELEE,
                    15f, 3.5, 6, 1.5f, 0.15f, 1.5f, 0.70f, 1.2f, 0, 0, 0f,
                    null,
                    new WeaponDefinition.ProjectileSpec(0f, 0f, 0, 0, 0),
                    List.of(EffectType.ENERGY_DISRUPTION, EffectType.STAGGER),
                    0.8f, 0.36f, 0f, 0f, 0.1f,
                    ModSounds.MELEE_SWING, ParticleTypes.SWEEP_ATTACK, ParticleTypes.ENCHANTED_HIT,
                    null, false, 0f,
                    terrain(TerrainMode.POWER_CUT, 34f, 0f, 1, 4, 0.76f, 0.3f,
                            new StatusEffects(false, 0.09f, true, false, false), 8));

    public static final WeaponDefinition POWER_HAMMER =
            new WeaponDefinition("melee", FireMode.MELEE,
                    25f, 3.0, 5, 1.5f, 0.20f, 1.5f, 0.85f, 0.667f, 0, 0, 0f,
                    null,
                    new WeaponDefinition.ProjectileSpec(0f, 0f, 0, 0, 0),
                    List.of(EffectType.STAGGER, EffectType.KNOCKBACK),
                    2.0f, 0.9f, 0f, 0f, 0.26f,
                    ModSounds.MELEE_SWING, ParticleTypes.SWEEP_ATTACK, ParticleTypes.EXPLOSION_EMITTER,
                    null, false, 0f,
                    terrain(TerrainMode.SHOCKWAVE, 54f, 1.8f, 1, 12, 0.85f, 0.1f, NO_HEAT, 8));

    /** Tableau indexé des 15 définitions (index stables pour la synchro réseau des projectiles). */
    private static final WeaponDefinition[] ALL = {
            BOLTGUN, BOLT_PISTOL, HEAVY_BOLTER, STALKER_BOLT_RIFLE,
            LAS_GUN, LAZ_PISTOL, PLASMA_PISTOL, PLASMA_INCINERATOR,
            SHOTGUN, FLAMETHROWER, MELTA_GUN, HUNTER_KILLER_MISSILE,
            CHAINSWORD, POWERSWORD, POWER_HAMMER
    };

    /**
     * Les 15 armes, dans l'ordre de la table d'équilibrage. C'est la liste de référence : un contrôle
     * vérifie sa taille, pour qu'une arme ajoutée sans être équilibrée se voie tout de suite.
     */
    public static final List<WeaponDefinition> TOUTES = List.of(
            BOLTGUN, BOLT_PISTOL, HEAVY_BOLTER, STALKER_BOLT_RIFLE, LAS_GUN, LAZ_PISTOL,
            PLASMA_PISTOL, PLASMA_INCINERATOR, SHOTGUN, FLAMETHROWER, MELTA_GUN,
            HUNTER_KILLER_MISSILE, CHAINSWORD, POWERSWORD, POWER_HAMMER);

    private static final Map<WeaponDefinition, Integer> IDS = new HashMap<>();

    static {
        for (int i = 0; i < ALL.length; i++) {
            IDS.put(ALL[i], i);
        }
    }

    /** Index réseau stable d'une définition (0 = bolter par défaut). */
    public static int idOf(WeaponDefinition def) {
        return IDS.getOrDefault(def, 0);
    }

    /** Définition à partir d'un index réseau. */
    public static WeaponDefinition byId(int id) {
        if (id < 0 || id >= ALL.length) return BOLTGUN;
        return ALL[id];
    }

    private static TerrainDamageSpec terrain(TerrainMode mode, float power, float radius, int pen, int maxBlocks,
                                             float armorPen, float heat, StatusEffects status) {
        return terrain(mode, power, radius, pen, maxBlocks, armorPen, heat, status, 4);
    }

    private static TerrainDamageSpec terrain(TerrainMode mode, float power, float radius, int pen, int maxBlocks,
                                             float armorPen, float heat, StatusEffects status, int cooldown) {
        // hardnessFactor = 1.0 (et non 5.0) : sinon seuil = 10 + résistance×5 = 90 pour la pierre,
        // soit ~13 tirs pour casser un bloc → perçu comme « aucun dégât ».
        return new TerrainDamageSpec(true, mode, power, radius, pen, maxBlocks,
                1.0f, armorPen, heat, 1.6f, 0.6f, status, cooldown);
    }

    private WeaponDefinitions() {
    }
}
