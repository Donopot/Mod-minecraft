package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Ce que le Servitor « voit » du monde autour de lui, lu directement dans le serveur.
 *
 * <p>Pas de cache de chunk : un cache coûte de la mémoire, se périme dès qu'un bloc change et
 * demande un budget de scan par tick pour rester à jour. Les lectures ciblées ci-dessous sont
 * assez rapides pour être faites à la demande (quelques milliers d'appels) et toujours exactes.
 * Elles servent à trois choses : choisir par où remonter, éviter la lave, et répondre au modèle
 * quand il demande ce qu'il y a autour.</p>
 */
public final class BrainSenses {

    private BrainSenses() {}

    /** Bloc qui tue ou bloque un servitor qui creuse dedans. */
    public static boolean hazard(ServerLevel level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        return st.is(Blocks.LAVA) || st.is(Blocks.FIRE) || st.is(Blocks.MAGMA_BLOCK)
                || st.is(Blocks.SOUL_FIRE) || st.is(Blocks.CAMPFIRE) || st.is(Blocks.POWDER_SNOW);
    }

    /** Liquide non solide (eau, lave) : on ne creuse pas dedans. */
    public static boolean fluid(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getFluidState().isEmpty();
    }

    /** Nombre de blocs solides au-dessus de la tete avant l'air libre (plafond). */
    public static int roof(ServerLevel level, BlockPos from, int max) {
        for (int i = 1; i <= max; i++) {
            BlockPos p = from.above(i);
            if (level.getBlockState(p).isAir()) return i - 1;
        }
        return max;
    }

    /**
     * Direction horizontale ou le plafond est le plus fin autour du servitor : c'est la ou un
     * escalier creuse sortira le plus vite a l'air libre.
     *
     * @return {dx, dz} parmi les 4 directions, ou null si aucun plafond plus fin n'est trouve.
     */
    public static int[] thinnestRoof(ServerLevel level, BlockPos here, int maxScan) {
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int best = roof(level, here, maxScan);
        int[] bestDir = null;
        for (int[] d : dirs) {
            BlockPos p = here.offset(d[0], 0, d[1]);
            int r = roof(level, p, maxScan);
            if (r < best) {
                best = r;
                bestDir = d;
            }
        }
        return bestDir;
    }

    /** Fin de la colonne : premiere position ou l'on peut se tenir sous le ciel. */
    public static int surfaceY(ServerLevel level, int x, int z, int fromY, int maxScan) {
        for (int y = fromY; y <= fromY + maxScan; y++) {
            BlockPos feet = new BlockPos(x, y, z);
            if (level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()
                    && level.canSeeSky(feet.above())) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Resume lisible par le modele et par l'operateur : plafond, risques, minerais visibles.
     * Volontairement court : il finit dans un prompt de 300 tokens.
     */
    public static String summary(ServerLevel level, LivingEntity bot, int radius) {
        if (bot == null) return "aucun servitor en jeu";
        BlockPos here = bot.blockPosition();
        StringBuilder sb = new StringBuilder();
        sb.append("plafond ").append(roof(level, here, 32)).append(" blocs au-dessus");

        int[] thin = thinnestRoof(level, here, 32);
        if (thin != null) {
            sb.append(" | sortie la plus proche vers ").append(dirName(thin));
        } else {
            sb.append(" | pas de sortie plus fine autour");
        }

        int lava = 0, water = 0, ores = 0, solid = 0, air = 0;
        int r = Math.max(4, Math.min(radius, 16));
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = here.offset(dx, dy, dz);
                    if (hazard(level, p)) {
                        lava++;
                        continue;
                    }
                    BlockState st = level.getBlockState(p);
                    if (!st.getFluidState().isEmpty()) {
                        water++;
                        continue;
                    }
                    if (st.isAir()) {
                        air++;
                        continue;
                    }
                    solid++;
                    String id = st.getBlock().builtInRegistryHolder().key().identifier().getPath();
                    if (id.contains("_ore") || id.contains("ancient_debris")) ores++;
                }
            }
        }
        if (lava > 0) sb.append(" | ATTENTION ").append(lava).append(" bloc(s) dangereux (lave/feu)");
        if (water > 0) sb.append(" | ").append(water).append(" bloc(s) de liquide");
        sb.append(" | ").append(solid).append(" solides / ").append(air).append(" vides dans ").append(r).append(" blocs");
        if (ores > 0) sb.append(" | ").append(ores).append(" bloc(s) de minerai");
        return sb.toString();
    }

    // ------------------------------------------------------------------ view_blocks

    /**
     * Les N blocs les plus proches d'un type donne, dans les chunks charges.
     * Une liste triee par distance, avec ce qu'il y a au-dessus et au-dessous : on sait
     * tout de suite si le filon est a ciel ouvert ou sous la roche.
     */
    public static String find(ServerLevel level, LivingEntity bot, String blockName, int radius, int limit) {
        if (bot == null) return "aucun servitor en jeu";
        String wanted = blockName == null ? "" : blockName.toLowerCase(Locale.ROOT).trim();
        if (wanted.isEmpty()) return "nom de bloc manquant";
        int r = Math.max(4, Math.min(radius, 24));
        int max = Math.max(1, Math.min(limit, 16));

        java.util.List<BlockPos> found = new java.util.ArrayList<>();
        BlockPos here = bot.blockPosition();
        int count = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -Math.min(r, 16); dy <= Math.min(r, 16); dy++) {
                    BlockPos p = here.offset(dx, dy, dz);
                    String path = level.getBlockState(p).getBlock().builtInRegistryHolder().key().identifier().getPath();
                    if (!path.equals(wanted) && !path.endsWith("_" + wanted)) continue;
                    count++;
                    if (found.size() < max) found.add(p.immutable());
                }
            }
        }
        if (count == 0) return "aucun " + wanted + " dans " + r + " blocs (chunks charges)";

        found.sort(java.util.Comparator.comparingDouble(p -> p.distSqr(here)));
        StringBuilder sb = new StringBuilder();
        sb.append(count).append(" ").append(wanted).append(" dans ").append(r)
                .append(" blocs — les ").append(found.size()).append(" plus proches :");
        for (BlockPos p : found) {
            sb.append("\n  (").append(p.getX()).append(", ").append(p.getY()).append(", ").append(p.getZ())
                    .append(") a ").append((int) Math.sqrt(p.distSqr(here))).append(" blocs ").append(bearing(here, p))
                    .append(" | dessus ").append(shortName(level, p.above()))
                    .append(", dessous ").append(shortName(level, p.below()));
        }
        return sb.toString();
    }

    /**
     * Grille d'une petite zone, une couche par niveau : un dessin vaut mille coordonnees.
     * Chaque caractere est une matiere, la legende suit.
     */
    public static String box(ServerLevel level, BlockPos center, int halfWidth, int layers) {
        int hw = Math.max(1, Math.min(halfWidth, 4));
        int lh = Math.max(1, Math.min(layers, 6));
        java.util.Map<Character, String> legend = new java.util.LinkedHashMap<>();
        StringBuilder sb = new StringBuilder("vue de ").append(center.toShortString())
                .append(" (").append(hw * 2 + 1).append("x").append(lh * 2 + 1).append("x")
                .append(hw * 2 + 1).append(") :");
        for (int dy = lh; dy >= -lh; dy--) {
            sb.append("\ny=").append(center.getY() + dy).append(" ");
            for (int dx = -hw; dx <= hw; dx++) {
                for (int dz = -hw; dz <= hw; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    char c = symbol(level, p, legend);
                    sb.append(c == 0 ? '.' : c);
                }
                sb.append(' ');
            }
        }
        sb.append("\nlegende : ");
        legend.forEach((c, name) -> sb.append(c).append('=').append(name).append(' '));
        return sb.toString();
    }

    /** Symbole stable pour une matiere, enregistre dans la legende au premier passage. */
    private static char symbol(ServerLevel level, BlockPos p, java.util.Map<Character, String> legend) {
        BlockState st = level.getBlockState(p);
        String name;
        char c;
        if (hazard(level, p)) {
            name = "lave/feu";
            c = '!';
        } else if (!st.getFluidState().isEmpty()) {
            name = "liquide";
            c = '~';
        } else if (st.isAir()) {
            return 0;
        } else {
            name = shortName(level, p);
            c = 0;
        }
        if (c == 0) {
            for (var e : legend.entrySet()) if (e.getValue().equals(name)) return e.getKey();
            c = (char) ('a' + Math.min(25, legend.size()));
        } else if (!legend.containsKey(c)) {
            legend.put(c, name);
            return c;
        }
        legend.putIfAbsent(c, name);
        return c;
    }

    private static String shortName(ServerLevel level, BlockPos p) {
        return level.getBlockState(p).getBlock().builtInRegistryHolder().key().identifier().getPath();
    }

    /** Direction lisible entre deux points, pour situer un filon. */
    private static String bearing(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX(), dz = to.getZ() - from.getZ();
        int dy = to.getY() - from.getY();
        String h = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? "est" : "ouest") : (dz > 0 ? "sud" : "nord");
        String v = dy > 1 ? ", plus haut" : dy < -1 ? ", plus bas" : "";
        return h + v;
    }

    /** Nom court d'une direction, pour le prompt. */
    public static String dirName(int[] d) {
        if (d == null) return "?";
        if (d[0] > 0) return "est (+X)";
        if (d[0] < 0) return "ouest (-X)";
        return d[1] > 0 ? "sud (+Z)" : "nord (-Z)";
    }
}
