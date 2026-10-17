package com.donopot.voiddynasty.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Intégration douce des mods de claims (FTB Chunks). Vérifie si un bloc est
 * dans un chunk revendiqué, afin d'empêcher les dégâts de terrain sur les
 * constructions des joueurs (spec : respect_claims, claim_check_fail_closed).
 *
 * <p>Aucune dépendance de compilation : tout passe par réflexion, donc le mod
 * reste un jar unique qui fonctionne avec ou sans FTB Chunks.</p>
 */
public final class ClaimIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger("voiddynasty/claims");

    private static final boolean FTB_CHUNKS_LOADED = FabricLoader.getInstance().isModLoaded("ftbchunks");
    private static boolean warned = false;

    /**
     * Point d'essai : force le verdict de protection. {@code null} = comportement réel.
     *
     * Pourquoi il existe : le mode {@code wilderness_only} est le défaut, mais sans FTB Chunks
     * installé le seul chemin jamais exercé était « aucun claim → dégâts autorisés ». La décision de
     * Donopot (①a « hors zones revendiquées ») n'était donc prouvée par rien. Le harnais force le
     * verdict pour éprouver la règle — et ce contrôle échoue si l'on retire la vérification de
     * {@code TerrainDamageManager.isDamageable}.
     */
    private static volatile Boolean forceProtege = null;

    /** Point d'essai réservé au harnais : {@code null} rétablit le comportement réel. */
    public static void forcerProtege(Boolean valeur) {
        forceProtege = valeur;
    }

    private ClaimIntegration() {}

    /** Vrai si un mod de claim reconnu est chargé. */
    public static boolean available() {
        return FTB_CHUNKS_LOADED;
    }

    /**
     * Vrai si le bloc est dans un chunk revendiqué (donc protégé).
     * Fail-closed : toute erreur renvoie {@code true} (on protège par défaut).
     */
    public static boolean isClaimProtected(ServerLevel level, BlockPos pos) {
        if (forceProtege != null) return forceProtege;   // point d'essai, voir plus haut
        if (!FTB_CHUNKS_LOADED) return false;
        try {
            Object api = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI")
                    .getMethod("api").invoke(null);
            if (api == null) return true;

            Object manager = unwrap(api.getClass().getMethod("getManager").invoke(api));
            if (manager == null) return true; // fail closed

            Object chunk = getChunk(manager, level, pos);
            return chunk != null;
        } catch (Throwable t) {
            warnOnce(t);
            return true; // fail closed
        }
    }

    private static Object unwrap(Object o) {
        if (o instanceof Optional<?> opt) return opt.orElse(null);
        return o;
    }

    private static Object getChunk(Object manager, ServerLevel level, BlockPos pos) throws Exception {
        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        Class<?> managerClass = manager.getClass();
        // Forme récente : getChunk(ChunkDimPos).
        try {
            Class<?> cdpClass = Class.forName("dev.ftb.mods.ftblibrary.math.ChunkDimPos");
            Object cdp = cdpClass.getConstructor(net.minecraft.resources.ResourceKey.class, ChunkPos.class)
                    .newInstance(level.dimension(), chunkPos);
            return managerClass.getMethod("getChunk", cdpClass).invoke(manager, cdp);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            // Forme ancienne : getChunk(ChunkPos).
            return managerClass.getMethod("getChunk", ChunkPos.class).invoke(manager, chunkPos);
        }
    }

    private static void warnOnce(Throwable t) {
        if (!warned) {
            warned = true;
            LOGGER.warn("[voiddynasty] Échec de la vérification de claim FTB Chunks (API différente ?) — "
                    + "les dégâts de terrain seront refusés (fail-closed). Cause : {}", t.toString());
        }
    }
}
