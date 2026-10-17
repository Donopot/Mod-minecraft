package com.donopot.voiddynasty;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Flotte de vaisseaux par joueur (1 déployé + N rangés).
 * Chaque vaisseau appartient à son propriétaire (UUID) — seules les actions du proprio sont autorisées.
 */
public final class FleetManager {

    public static final int MAX_SHIPS_PER_PLAYER = 10;

    private FleetManager() {}

    // ---------------- API publique ----------------

    public static List<ShipData> list(ServerPlayer player) {
        return FleetState.get(player.level()).shipsOf(player.getUUID());
    }

    public static String add(ServerPlayer player, ShipType type, String name) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        if (ships.size() >= MAX_SHIPS_PER_PLAYER)
            return "Limite atteinte (" + MAX_SHIPS_PER_PLAYER + " vaisseaux max)";
        if (find(ships, name) != null) return "Nom déjà pris : " + name;
        ships.add(new ShipData(name, type));
        st.setDirty();
        return "Vaisseau \"" + name + "\" (" + type.displayName + ") ajouté";
    }

    public static String deploy(ServerPlayer player, String name, BlockPos target) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        ShipData sh = find(ships, name);
        if (sh == null) return "Vaisseau introuvable : " + name;
        if (sh.isDeployed()) return "Déjà déployé";
        ShipData dep = deployed(ships);
        if (dep != null) return "Ranger d'abord \"" + dep.name + "\" (1 seul vaisseau déployé à la fois)";
        try {
            long t0 = System.currentTimeMillis();
            if (sh.storedBytes != null) {
                placeBuffer(player.level(), gunzip(sh.storedBytes), target.getX(), target.getY(), target.getZ());
            } else {
                placeTemplate(player.level(), target.getX(), target.getY(), target.getZ(), sh.type, true);
            }
            sh.base = target;
            st.setDirty();
            return "Déployé en " + (System.currentTimeMillis() - t0) + " ms à ("
                    + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")";
        } catch (Exception e) {
            VoidDynastyMod.LOGGER.error("[voiddynasty] échec deploy", e);
            return "Erreur deploy : " + e;
        }
    }

    public static String stow(ServerPlayer player, String name) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        ShipData sh = find(ships, name);
        if (sh == null) return "Vaisseau introuvable : " + name;
        if (!sh.isDeployed()) return "Pas déployé";
        byte[] buf = scanAndClear(player.level(), sh.base, sh.type);
        sh.storedBytes = gzip(buf);
        sh.base = null;
        st.setDirty();
        int n = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return "Rangé (" + n + " blocs)";
    }

    public static String teleport(ServerPlayer player, String name, BlockPos target) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        ShipData sh = find(ships, name);
        if (sh == null) return "Vaisseau introuvable : " + name;
        if (!sh.isDeployed()) return "Pas déployé";
        byte[] buf = scanAndClear(player.level(), sh.base, sh.type);
        placeBuffer(player.level(), buf, target.getX(), target.getY(), target.getZ());
        sh.base = target;
        st.setDirty();
        return "Téléporté à (" + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")";
    }

    public static String rename(ServerPlayer player, String name, String newName) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        ShipData sh = find(ships, name);
        if (sh == null) return "Vaisseau introuvable : " + name;
        if (find(ships, newName) != null) return "Nom déjà pris : " + newName;
        sh.name = newName;
        st.setDirty();
        return "Renommé en \"" + newName + "\"";
    }

    public static String delete(ServerPlayer player, String name) {
        FleetState st = FleetState.get(player.level());
        List<ShipData> ships = st.shipsOf(player.getUUID());
        ShipData sh = find(ships, name);
        if (sh == null) return "Vaisseau introuvable : " + name;
        if (sh.isDeployed()) scanAndClear(player.level(), sh.base, sh.type);
        ships.remove(sh);
        st.setDirty();
        return "Supprimé \"" + name + "\"";
    }

    // ---------------- diagnostic des templates ----------------

    /** Contrôle d'un template embarqué : dimensions, blocs, et surtout états de blocs NON reconnus. */
    public static List<String> templateCheck(ShipType type) {
        List<String> out = new ArrayList<>();
        TemplateData d;
        try {
            d = loadTemplate(type);
        } catch (Exception e) {
            out.add("Template " + type.template + " : ECHEC DE LECTURE — " + e);
            return out;
        }
        out.add("Template " + type.template + " (" + type.displayName + ") : "
                + d.nx + "x" + d.ny + "x" + d.nz + ", " + d.count + " blocs, palette "
                + d.palette.length + " etats, non reconnus : " + d.bad);
        if (d.bad == 0) {
            out.add("  -> tous les etats de blocs sont valides sur cette version");
            return out;
        }
        int n = 0;
        for (int i = 0; i < d.palette.length && n < 25; i++) {
            if (d.states[i].isAir() && !d.palette[i].startsWith("minecraft:air")) {
                out.add("  MANQUANT : " + d.palette[i]);
                n++;
            }
        }
        if (d.bad > n) out.add("  ... et " + (d.bad - n) + " autres");
        return out;
    }

    /** Pose directe d'un template à des coordonnées données (contrôle, sans passer par un vaisseau de la flotte). */
    public static String templatePlace(ServerLevel level, ShipType type, BlockPos base) {
        long t0 = System.currentTimeMillis();
        TemplateData d = loadTemplate(type);
        placeTemplate(level, base.getX(), base.getY(), base.getZ(), type, false);
        long ms = System.currentTimeMillis() - t0;
        // relecture immediate : combien de blocs sont REELLEMENT la, en memoire, juste apres la pose
        int identiques = 0, manquants = 0, autres = 0;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        for (int i = 0; i < d.count; i++) {
            BlockState attendu = d.states[d.ids[i] & 0xFFFF];
            mp.set(base.getX() + d.xs[i], base.getY() + d.ys[i], base.getZ() + d.zs[i]);
            BlockState trouve = level.getBlockState(mp);
            if (trouve == attendu) identiques++;
            else if (trouve.isAir()) manquants++;
            else autres++;
        }
        return "Pose " + type.template + " : " + d.count + " blocs en base ("
                + base.getX() + ", " + base.getY() + ", " + base.getZ() + ") en " + ms + " ms"
                + " | relu aussitot : " + identiques + " identiques, " + manquants + " vides, " + autres + " autres"
                + (d.bad > 0 ? " — ATTENTION : " + d.bad + " etats non reconnus" : "");
    }

    // ---------------- helpers ----------------

    private static ShipData find(List<ShipData> ships, String name) {
        for (ShipData s : ships) if (s.name.equalsIgnoreCase(name)) return s;
        return null;
    }

    private static ShipData deployed(List<ShipData> ships) {
        for (ShipData s : ships) if (s.isDeployed()) return s;
        return null;
    }

    // ---------------- scan / place / template (format binaire) ----------------

    private static byte[] scanAndClear(ServerLevel level, BlockPos base, ShipType type) {
        int nx = type.nx, ny = type.ny, nz = type.nz;
        int cx0 = base.getX() >> 4, cz0 = base.getZ() >> 4;
        int cx1 = (base.getX() + nx - 1) >> 4, cz1 = (base.getZ() + nz - 1) >> 4;
        for (int cx = cx0; cx <= cx1; cx++)
            for (int cz = cz0; cz <= cz1; cz++)
                level.getChunk(cx, cz);

        ByteBuffer bb = ByteBuffer.allocate(12_000_000).order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream beStream = new ByteArrayOutputStream();
        DataOutputStream beOut = new DataOutputStream(beStream);
        int beCount = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int count = 0;
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                for (int z = 0; z < nz; z++) {
                    mpos.set(base.getX() + x, base.getY() + y, base.getZ() + z);
                    BlockState st = level.getBlockState(mpos);
                    if (st.isAir()) continue;
                    BlockEntity be = level.getBlockEntity(mpos);
                    if (be != null) {
                        try {
                            CompoundTag tag = be.saveWithFullMetadata(level.registryAccess());
                            ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                            DataOutputStream tmpOut = new DataOutputStream(tmp);
                            NbtIo.write(tag, tmpOut);
                            tmpOut.flush();
                            byte[] nbt = tmp.toByteArray();
                            beOut.writeShort(x);
                            beOut.writeShort(y);
                            beOut.writeShort(z);
                            beOut.writeInt(nbt.length);
                            beOut.write(nbt);
                            beCount++;
                        } catch (IOException ignored) {
                        }
                    }
                    if (bb.remaining() < 10) {
                        ByteBuffer nb = ByteBuffer.allocate(bb.capacity() * 2).order(ByteOrder.LITTLE_ENDIAN);
                        nb.put((ByteBuffer) bb.flip());
                        bb = nb;
                    }
                    bb.putInt(Block.BLOCK_STATE_REGISTRY.getId(st));
                    bb.putShort((short) x);
                    bb.putShort((short) y);
                    bb.putShort((short) z);
                    level.setBlock(mpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
                    count++;
                }
            }
        }
        byte[] beBytes = beStream.toByteArray();
        ByteBuffer out = ByteBuffer.allocate(4 + bb.position() + 4 + beBytes.length).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(count);
        out.put((ByteBuffer) bb.flip());
        out.putInt(beCount);
        out.put(beBytes);
        return out.array();
    }

    private static void placeBuffer(ServerLevel level, byte[] buf, int bx, int by, int bz) {
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        int count = bb.getInt();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < count; i++) {
            int id = bb.getInt();
            int x = bb.getShort() & 0xFFFF;
            int y = bb.getShort() & 0xFFFF;
            int z = bb.getShort() & 0xFFFF;
            BlockState st = Block.stateById(id);
            mpos.set(bx + x, by + y, bz + z);
            int f = st.getLightEmission() > 0 ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS;
            level.setBlock(mpos, st, f);
        }
        int beCount = bb.getInt();
        for (int i = 0; i < beCount; i++) {
            int x = bb.getShort() & 0xFFFF;
            int y = bb.getShort() & 0xFFFF;
            int z = bb.getShort() & 0xFFFF;
            int nbtLen = bb.getInt();
            byte[] nbt = new byte[nbtLen];
            bb.get(nbt);
            mpos.set(bx + x, by + y, bz + z);
            BlockEntity be = level.getBlockEntity(mpos);
            if (be != null) {
                try {
                    CompoundTag tag = NbtIo.read(new DataInputStream(new ByteArrayInputStream(nbt)));
                    ValueInput vi = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
                    be.loadWithComponents(vi);
                    be.setChanged();
                } catch (IOException e) {
                    VoidDynastyMod.LOGGER.warn("[voiddynasty] échec restauration BlockEntity à {}", mpos.toShortString());
                }
            }
        }
    }

    private static void placeTemplate(ServerLevel level, int bx, int by, int bz, ShipType type, boolean logBad) {
        TemplateData d = loadTemplate(type);
        // Charge ET termine la generation de toute la zone AVANT de poser le moindre bloc.
        // Sans ca, un bloc pose dans un chunk encore en cours de generation est ecrase
        // quand la generation se termine : le vaisseau arrive tronque (1 colonne sur 5).
        int cx0 = bx >> 4, cz0 = bz >> 4;
        int cx1 = (bx + d.nx - 1) >> 4, cz1 = (bz + d.nz - 1) >> 4;
        for (int cx = cx0; cx <= cx1; cx++)
            for (int cz = cz0; cz <= cz1; cz++)
                level.getChunk(cx, cz);
        if (logBad && d.bad > 0) {
            VoidDynastyMod.LOGGER.warn("[voiddynasty] pose {} : {}/{} etats de blocs non reconnus, poses en air",
                    type.template, d.bad, d.palette.length);
        }
        for (int i = 0; i < d.count; i++) {
            BlockState st = d.states[d.ids[i] & 0xFFFF];
            BlockPos p = new BlockPos(bx + d.xs[i], by + d.ys[i], bz + d.zs[i]);
            int f = st.getLightEmission() > 0 ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS;
            level.setBlock(p, st, f);
        }
    }

    private static TemplateData loadTemplate(ShipType type) {
        try (InputStream in = FleetManager.class.getResourceAsStream(type.resourcePath())) {
            if (in == null) throw new IOException(type.resourcePath() + " introuvable");
            byte[] raw;
            try (GZIPInputStream gz = new GZIPInputStream(in)) {
                raw = gz.readAllBytes();
            }
            ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            int nx = bb.getInt(), ny = bb.getInt(), nz = bb.getInt(), count = bb.getInt();
            int palLen = bb.getInt();
            String[] palette = new String[palLen];
            for (int i = 0; i < palLen; i++) {
                int l = bb.getInt();
                byte[] name = new byte[l];
                bb.get(name);
                palette[i] = new String(name, StandardCharsets.UTF_8);
            }
            BlockState[] states = new BlockState[palLen];
            int bad = 0;
            for (int i = 0; i < palLen; i++) {
                try {
                    states[i] = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, palette[i], false).blockState();
                    if (states[i].isAir()) bad++;
                } catch (CommandSyntaxException e) {
                    states[i] = Blocks.AIR.defaultBlockState();
                    bad++;
                }
            }
            if (bad > 0) {
                StringBuilder sb = new StringBuilder();
                int shown = 0;
                for (int i = 0; i < palLen && shown < 15; i++) {
                    if (states[i].isAir() && !palette[i].startsWith("minecraft:air")) {
                        sb.append("\n  - ").append(palette[i]);
                        shown++;
                    }
                }
                VoidDynastyMod.LOGGER.warn("[voiddynasty] template {} : {}/{} etats non reconnus -> air :{}",
                        type.template, bad, palLen, sb);
            }
            short[] ids = new short[count];
            int[] xs = new int[count], ys = new int[count], zs = new int[count];
            for (int i = 0; i < count; i++) {
                ids[i] = bb.getShort();
                xs[i] = bb.getShort() & 0xFFFF;
                ys[i] = bb.getShort() & 0xFFFF;
                zs[i] = bb.getShort() & 0xFFFF;
            }
            return new TemplateData(nx, ny, nz, count, ids, xs, ys, zs, states, palette, bad);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] gzip(byte[] raw) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
                gz.write(raw);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] gunzip(byte[] gz) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TemplateData(int nx, int ny, int nz, int count, short[] ids,
                                 int[] xs, int[] ys, int[] zs, BlockState[] states,
                                 String[] palette, int bad) {}
}
