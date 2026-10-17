package com.donopot.voiddynasty.terrain;

import com.donopot.voiddynasty.compat.ClaimIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gère les dégâts de terrain (spec void_dynasty:terrain_rules/v1) :
 * reçoit un impact, évalue la résistance matériau de chaque bloc, accumule
 * des fissures (intégrité) et détruit le bloc quand le seuil est atteint.
 */
public final class TerrainDamageManager {
    private TerrainDamageManager() {}

    /** Durée de régénération complète (spec : decay_to_full_after_seconds). */
    private static final float DECAY_SECONDS = 300f;
    /** Budget global de blocs modifiés par tick (spec : max_blocks_per_tick_global). */
    private static final int MAX_BLOCKS_PER_TICK = 128;

    /**
     * Mode d'application des dégâts de terrain (spec : terrain_rules.world_modes).
     */
    public enum WorldMode {
        /** Aucun dégât de terrain. */
        OFF,
        /** Fissures visuelles uniquement, jamais de destruction. */
        COSMETIC,
        /** Uniquement hors des chunks revendiqués (claims). */
        WILDERNESS_ONLY,
        /** Partout sauf blocs protégés (bedrock, commandes…). */
        ALL_UNPROTECTED
    }

    private static volatile WorldMode worldMode = WorldMode.WILDERNESS_ONLY;

    public static WorldMode getWorldMode() {
        return worldMode;
    }

    public static void setWorldMode(WorldMode mode) {
        worldMode = mode == null ? WorldMode.WILDERNESS_ONLY : mode;
    }

    private static final Pattern WORLD_MODE_PATTERN =
            Pattern.compile("\"world_mode\"\\s*:\\s*\"([a-z_]+)\"");

    /** Charge {@code config/voiddynasty.json} (clé {@code world_mode}). */
    public static void loadConfig(Path configDir) {
        Path file = configDir.resolve("voiddynasty.json");
        if (!Files.exists(file)) return;
        try {
            Matcher m = WORLD_MODE_PATTERN.matcher(Files.readString(file));
            if (m.find()) {
                switch (m.group(1)) {
                    case "off" -> worldMode = WorldMode.OFF;
                    case "cosmetic" -> worldMode = WorldMode.COSMETIC;
                    case "all_unprotected" -> worldMode = WorldMode.ALL_UNPROTECTED;
                    default -> worldMode = WorldMode.WILDERNESS_ONLY;
                }
            }
        } catch (Exception ignored) {
            // config malformée : on garde la valeur par défaut (wilderness_only)
        }
    }

    /** Intégrité + tick serveur monotone d'origine (décroissance via effectiveDamage). */
    private record IntegrityEntry(float damage, long gameTime) {}

    /** Intégrité persistante par dimension (fissures accumulées). */
    private static final Map<ResourceKey<Level>, Map<BlockPos, IntegrityEntry>> INTEGRITY = new ConcurrentHashMap<>();

    private static int blocksThisTick = 0;
    private static long lastTick = -1;

    // ------------------------------------------------------------------
    // Point d'entrée : appelé à chaque impact d'arme sur un bloc.
    // ------------------------------------------------------------------
    public static void onImpact(ServerLevel level, BlockPos impactPos, Vec3 shooterPos, Vec3 direction, TerrainDamageSpec spec) {
        if (!spec.enabled() || spec.impactPower() <= 0f) return;
        if (worldMode == WorldMode.OFF) return;

        tickBudget(level);

        float shooterDist = (float) shooterPos.distanceTo(Vec3.atCenterOf(impactPos));

        // 1) Zone radiale autour du point d'impact.
        int processed = 0;
        int r = Math.max(0, (int) Math.ceil(spec.impactRadius()));
        for (BlockPos pos : BlockPos.betweenClosed(impactPos.offset(-r, -r, -r), impactPos.offset(r, r, r))) {
            if (processed >= spec.maxBlocksModified()) break;
            if (blocksThisTick >= MAX_BLOCKS_PER_TICK) break;
            double dist = Math.sqrt(Vec3.atCenterOf(pos).distanceToSqr(Vec3.atCenterOf(impactPos)));
            if (dist > spec.impactRadius() + 0.6) continue;
            // Ligne de vue : le feu / les dégâts ne traversent pas les murs.
            if (!pos.equals(impactPos) && !hasLineOfSight(level, shooterPos, pos)) continue;
            if (processBlock(level, pos, (float) dist, shooterDist, spec)) processed++;
        }

        // 2) Pénétration en ligne (modes perforants).
        if (spec.pierces() && direction != null) {
            pierce(level, impactPos, direction, spec, shooterDist);
        }
    }

    // ------------------------------------------------------------------
    // Traitement d'un bloc.
    // ------------------------------------------------------------------
    private static boolean processBlock(ServerLevel level, BlockPos pos, float radialDist, float shooterDist, TerrainDamageSpec spec) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (!isDamageable(level, pos, state)) return false;

        TerrainMaterial mat = TerrainMaterial.categorize(state);
        if (mat == TerrainMaterial.PROTECTED) return false;

        // Chaleur (feu / fonte) appliquée indépendamment des dégâts de terrain :
        // le lance-flammes doit enflammer même sans casser le bloc.
        applyHeatEffects(level, pos, state, spec, mat);

        float dmg = computeDamage(spec, mat, state, radialDist, shooterDist);
        if (dmg <= 0f) return false;
        blocksThisTick++;

        // Accumulation d'intégrité (fissures).
        Map<BlockPos, IntegrityEntry> integ = integrity(level);
        IntegrityEntry cur = integ.get(pos);
        float effective = cur == null ? 0f : effectiveDamage(level, cur);
        float accumulated = effective + dmg;

        float threshold = 10f + mat.resistance * spec.hardnessFactor();
        if (accumulated >= threshold) {
            integ.remove(pos.immutable());
            if (worldMode == WorldMode.COSMETIC) {
                spawnCrackFeedback(level, pos, state); // fissures seules, pas de destruction
            } else {
                destroyBlock(level, pos, state);
            }
            return true;
        }

        // `.immutable()` est INDISPENSABLE : `pos` vient de BlockPos.betweenClosed, qui réutilise la
        // même instance mutable à chaque itération. Stockée telle quelle comme clé de table de
        // hachage, ses coordonnées changent après l'insertion : l'entrée devient introuvable et le
        // cumul de fissures n'est JAMAIS relu. Conséquence mesurée par le banc d'essai : seules les
        // armes qui détruisent en un coup fonctionnaient (melta, plasma, missile) ; un bolter, un
        // laser ou une épée tronçonneuse n'abîmaient rien, quoi qu'on tire.
        integ.put(pos.immutable(), new IntegrityEntry(accumulated, level.getServer().getTickCount()));
        spawnCrackFeedback(level, pos, state);
        return true;
    }

    private static boolean isDamageable(ServerLevel level, BlockPos pos, BlockState state) {
        // Blocs incassables (lit de pierre, etc.)
        if (state.getDestroySpeed(level, pos) < 0f) return false;
        // Respect des entités de bloc (coffres, fours…)
        if (level.getBlockEntity(pos) != null) return false;
        // Respect de la frontière du monde.
        if (!level.getWorldBorder().isWithinBounds(pos)) return false;
        // Respect des claims (mode wilderness_only : uniquement hors des chunks revendiqués).
        if (worldMode == WorldMode.WILDERNESS_ONLY && ClaimIntegration.isClaimProtected(level, pos)) return false;
        return true;
    }

    // ------------------------------------------------------------------
    // Formule de dégâts (spec : calculation).
    // ------------------------------------------------------------------
    private static float computeDamage(TerrainDamageSpec spec, TerrainMaterial mat, BlockState state, float radialDist, float shooterDist) {
        float radial = radialFalloff(spec, radialDist);
        float distF = distanceFalloff(spec, shooterDist);
        float matMod = TerrainMaterial.isFragile(state) ? 1.5f : 1.0f;
        float effectiveResistance = mat.resistance * (1f - spec.armorMaterialPenetration());

        float dmg = spec.impactPower() * radial * distF * matMod - effectiveResistance * 0.5f;
        if (dmg <= 0f) return 0f;

        // Bonus chaleur sur matériaux sensibles.
        if (spec.heatFactor() > 0f && TerrainMaterial.isHeatSensitive(state)) {
            dmg *= (1f + spec.heatFactor() * 2f);
        }
        return dmg;
    }

    private static float radialFalloff(TerrainDamageSpec spec, float dist) {
        float radius = Math.max(0.1f, spec.impactRadius());
        float t = dist / radius;
        return (float) (1.0 / (1.0 + Math.pow(t, spec.radiusExponent())));
    }

    private static float distanceFalloff(TerrainDamageSpec spec, float dist) {
        if (dist <= 1f) return 1f;
        return (float) (1.0 / (1.0 + Math.pow(dist / 100.0, spec.distanceExponent())));
    }

    // ------------------------------------------------------------------
    // Effets de chaleur sur les blocs.
    // ------------------------------------------------------------------
    private static void applyHeatEffects(ServerLevel level, BlockPos pos, BlockState state, TerrainDamageSpec spec, TerrainMaterial mat) {
        if (spec.heatFactor() <= 0f) return;
        TerrainDamageSpec.StatusEffects st = spec.status();
        RandomSource rng = RandomSource.create();

        if (st.meltIce() && state.is(BlockTags.ICE)) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            return;
        }
        if (st.transformSandToGlass() && state.is(BlockTags.SAND) && spec.heatFactor() >= 0.8f) {
            level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            return;
        }
        if (st.igniteFlammable() && TerrainMaterial.isFlammable(state) && rng.nextFloat() < st.igniteChance()) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    /** Ligne de vue : true si aucun bloc solide ne bloque la vue entre `from` et `target`. */
    private static boolean hasLineOfSight(ServerLevel level, Vec3 from, BlockPos target) {
        Vec3 to = Vec3.atCenterOf(target);
        ClipContext ctx = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        BlockHitResult hit = level.clip(ctx);
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target);
    }

    // ------------------------------------------------------------------
    // Pénétration en ligne (armor_piercing, thermal_beam…).
    // ------------------------------------------------------------------
    private static void pierce(ServerLevel level, BlockPos impactPos, Vec3 direction, TerrainDamageSpec spec, float shooterDist) {
        Vec3 dir = direction.normalize();
        Vec3 cur = Vec3.atCenterOf(impactPos);
        int pierced = 0;
        for (int i = 0; i < spec.maxPenetrationBlocks() * 2 && pierced < spec.maxPenetrationBlocks(); i++) {
            cur = cur.add(dir.scale(0.5));
            BlockPos pos = BlockPos.containing(cur);
            if (pos.equals(impactPos)) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (!isDamageable(level, pos, state)) return; // stop au premier bloc protégé
            TerrainMaterial mat = TerrainMaterial.categorize(state);
            if (mat == TerrainMaterial.PROTECTED) return;
            float radialDist = (float) (cur.distanceTo(Vec3.atCenterOf(impactPos)));
            // atténuation par bloc traversé (spec : attenuation_per_block).
            float attenuation = 1f - 0.22f * pierced;
            float dmg = computeDamage(spec, mat, state, radialDist, shooterDist) * Math.max(0.2f, attenuation);
            if (dmg > 0f) {
                blocksThisTick++;
                applyHeatEffects(level, pos, state, spec, mat);
                Map<BlockPos, IntegrityEntry> integ = integrity(level);
                IntegrityEntry e = integ.get(pos);
                float eff = e == null ? 0f : effectiveDamage(level, e);
                float acc = eff + dmg;
                float threshold = 10f + mat.resistance * spec.hardnessFactor();
                if (acc >= threshold) {
                    integ.remove(pos.immutable());
                    if (worldMode == WorldMode.COSMETIC) {
                        spawnCrackFeedback(level, pos, state);
                    } else {
                        destroyBlock(level, pos, state);
                    }
                } else {
                    integ.put(pos.immutable(), new IntegrityEntry(acc, level.getServer().getTickCount()));
                    spawnCrackFeedback(level, pos, state);
                }
            }
            pierced++;
        }
    }

    // ------------------------------------------------------------------
    // Destruction + feedback.
    // ------------------------------------------------------------------
    private static void destroyBlock(ServerLevel level, BlockPos pos, BlockState state) {
        // drops = none → destruction sans butin.
        level.destroyBlock(pos, false);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                20, 0.35, 0.35, 0.35, 0.08);
    }

    private static void spawnCrackFeedback(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                20, 0.25, 0.25, 0.25, 0.05);
    }

    // ------------------------------------------------------------------
    // Intégrité + décroissance.
    // ------------------------------------------------------------------
    private static Map<BlockPos, IntegrityEntry> integrity(ServerLevel level) {
        return INTEGRITY.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
    }

    /**
     * Diagnostic : cumul de dégâts actuellement connu pour une position (0 s'il n'y en a pas).
     *
     * Ajouté après un échec de test incompréhensible (« le melta détruit la pierre en un coup, mais
     * le bolter ne l'abîme pas en trois ») : sans cette valeur, impossible de distinguer « le bloc
     * n'est jamais traité » de « le bloc est traité mais le seuil n'est pas atteint ». Un test qui
     * échoue sans chiffre coûte une hypothèse par essai.
     */
    public static float accumulatedAt(ServerLevel level, BlockPos pos) {
        IntegrityEntry e = integrity(level).get(pos);
        return e == null ? 0f : effectiveDamage(level, e);
    }

    /** Seuil de destruction pour un état de bloc donné, tel que le code le calcule. */
    public static float thresholdFor(BlockState state, TerrainDamageSpec spec) {
        return 10f + TerrainMaterial.categorize(state).resistance * spec.hardnessFactor();
    }

    /**
     * Efface le cumul de dégâts d'une position.
     *
     * Sert au banc d'essai : sans cela, le contrôle « détruite en N coups » hérite du cumul laissé par
     * le contrôle précédent sur la même position et mesure N moins les coups déjà portés. Une mesure
     * doit partir d'une ardoise propre.
     */
    public static void clearIntegrity(ServerLevel level, BlockPos pos) {
        integrity(level).remove(pos);
    }

    private static float effectiveDamage(ServerLevel level, IntegrityEntry e) {
        long elapsed = level.getServer().getTickCount() - e.gameTime();
        float seconds = elapsed / 20f;
        if (seconds <= 0f) return e.damage();
        return Math.max(0f, e.damage() * (1f - seconds / DECAY_SECONDS));
    }

    /** Appelé chaque tick serveur : purge les entrées guéries (la décroissance linéaire est dans effectiveDamage). */
    public static void tickDecay(MinecraftServer server) {
        if (server.getTickCount() % 40 != 0) return; // toutes les 2 s
        long now = server.getTickCount();
        long decayTicks = (long) (DECAY_SECONDS * 20f);
        for (Map<BlockPos, IntegrityEntry> map : INTEGRITY.values()) {
            Iterator<Map.Entry<BlockPos, IntegrityEntry>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, IntegrityEntry> e = it.next();
                if (now - e.getValue().gameTime() >= decayTicks) it.remove();
            }
        }
    }

    private static void tickBudget(ServerLevel level) {
        long now = level.getGameTime();
        if (now != lastTick) {
            lastTick = now;
            blocksThisTick = 0;
        }
    }

    public static void onImpact(ServerLevel level, BlockPos pos, TerrainDamageSpec spec) {
        onImpact(level, pos, Vec3.atCenterOf(pos), Vec3.ZERO, spec);
    }
}
