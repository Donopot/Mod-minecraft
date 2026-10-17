package com.donopot.voiddynasty.weapon;

import com.donopot.voiddynasty.terrain.TerrainDamageSpec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Caractéristiques complètes d'une arme, alignées sur le schéma void_dynasty:weapon/v1.
 *
 * @param family            famille (bolt, las, plasma, melee, flame, melta, solid, explosive)
 * @param mode              mode de tir (semi, rafale, automatique, continu, mêlée)
 * @param damage            dégâts en points de vie (20 PV = 10 cœurs)
 * @param effectiveRange    portée sans perte de dégâts (blocs)
 * @param maxRange          portée max (le projectile est détruit au-delà)
 * @param criticalMultiplier multiplicateur de coup critique
 * @param critChance        chance de coup critique (0-1)
 * @param headshotMultiplier multiplicateur de tir à la tête
 * @param armorPenetration  fraction d'armure ignorée (0-1)
 * @param roundsPerSecond   cadence (tirs/seconde)
 * @param projectilesPerShot nombre de projectiles par tir (plombs du fusil à pompe)
 * @param magazineSize      capacité du chargeur
 * @param reloadSeconds     durée de rechargement (secondes)
 * @param ammo              munition consommée (null = mêlée, munitions infinies)
 * @param projectile        caractéristiques du projectile physique
 * @param effects           effets appliqués à la cible
 * @param recoilVertical    recul vertical (caméra)
 * @param recoilHorizontal  recul horizontal (caméra)
 * @param hipSpread         dispersion sans viser (degrés)
 * @param aimSpread         dispersion en visant (degrés)
 * @param screenShake       intensité du tremblement d'écran (0-1)
 * @param fireSound         son de tir
 * @param tracer            particule du faisceau / traceur
 * @param impact            particule d'impact
 * @param falloff           perte de dégâts selon la distance (null = aucun)
 * @param perShellReload    rechargement cartouche par cartouche (fusil à pompe)
 * @param secondsPerShell   durée par cartouche (rechargement par cartouche)
 * @param terrain           dégâts de terrain (fissures, destruction)
 */
public record WeaponDefinition(
        String family,
        FireMode mode,
        float damage,
        double effectiveRange,
        double maxRange,
        float criticalMultiplier,
        float critChance,
        float headshotMultiplier,
        float armorPenetration,
        float roundsPerSecond,
        int projectilesPerShot,
        int magazineSize,
        float reloadSeconds,
        Item ammo,
        ProjectileSpec projectile,
        List<EffectType> effects,
        float recoilVertical,
        float recoilHorizontal,
        float hipSpread,
        float aimSpread,
        float screenShake,
        SoundEvent fireSound,
        ParticleOptions tracer,
        ParticleOptions impact,
        DamageFalloff falloff,
        boolean perShellReload,
        float secondsPerShell,
        TerrainDamageSpec terrain
) {

    /** Caractéristiques du projectile physique. */
    public record ProjectileSpec(
            float speed,                // blocs/seconde
            float gravity,              // blocs/seconde²
            int penetrationEntities,    // entités traversées (0 = s'arrête au premier)
            float explosionRadius,      // blocs (0 = pas d'explosion)
            float explosionDamage       // PV de dégâts d'explosion
    ) {}

    /** Perte de dégâts selon la distance. */
    public record DamageFalloff(float start, float end, float minMultiplier) {}

    /** Cadence en ticks entre deux tirs (dérivée de roundsPerSecond). */
    public int fireIntervalTicks() {
        if (roundsPerSecond <= 0f) return 20;
        return Math.max(1, Math.round(20f / roundsPerSecond));
    }

    /** Durée de rechargement en ticks. */
    public int reloadTicks() {
        return Math.max(1, Math.round(reloadSeconds * 20f));
    }
}
