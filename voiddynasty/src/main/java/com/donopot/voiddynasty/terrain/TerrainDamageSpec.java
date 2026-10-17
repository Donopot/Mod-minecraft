package com.donopot.voiddynasty.terrain;

/**
 * Configuration de dégâts de terrain d'une arme (spec
 * void_dynasty:weapon/v2 → terrain_damage).
 */
public record TerrainDamageSpec(
        boolean enabled,
        TerrainMode mode,
        float impactPower,
        float impactRadius,
        int maxPenetrationBlocks,
        int maxBlocksModified,
        float hardnessFactor,
        float armorMaterialPenetration,
        float heatFactor,
        float radiusExponent,
        float distanceExponent,
        StatusEffects status,
        int cooldownTicks) {

    public enum TerrainMode {
        EXPLOSIVE_PENETRATOR, ARMOR_PIERCING, PELLET, THERMAL_ABLATION,
        PLASMA_SPLASH, CONE_HEAT, THERMAL_BEAM, WARHEAD,
        MELEE_CUT, POWER_CUT, SHOCKWAVE
    }

    /** Effets de statut appliqués aux blocs chauffés. */
    public record StatusEffects(
            boolean igniteFlammable,
            float igniteChance,
            boolean meltIce,
            boolean transformSandToGlass,
            boolean charWood) {

        public static final StatusEffects NONE = new StatusEffects(false, 0f, false, false, false);
    }

    /** Modes qui percent en ligne (rayon). */
    public boolean pierces() {
        return maxPenetrationBlocks > 0 && switch (mode) {
            case ARMOR_PIERCING, THERMAL_BEAM, POWER_CUT, EXPLOSIVE_PENETRATOR -> true;
            default -> false;
        };
    }
}
