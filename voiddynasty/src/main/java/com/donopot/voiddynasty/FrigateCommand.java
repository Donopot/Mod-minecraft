package com.donopot.voiddynasty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Frégate Warhammer extraite de l'inbox (Create Railways -> format binaire voiddynasty).
 * Commande : /frigate sky | /frigate ground
 */
public final class FrigateCommand {
    private static VoxelData cached;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("frigate")
                        .then(Commands.literal("sky").executes(ctx -> spawn(ctx.getSource(), true)))
                        .then(Commands.literal("ground").executes(ctx -> spawn(ctx.getSource(), false)))
                        .executes(ctx -> spawn(ctx.getSource(), true))
        );
    }

    private static int spawn(CommandSourceStack source, boolean sky) {
        try {
            VoxelData d = load();
            ServerLevel level = source.getLevel();
            BlockPos center = BlockPos.containing(source.getPosition());
            int bx = center.getX() - d.nx / 2;
            int bz = center.getZ() - d.nz / 2;
            int by = sky ? center.getY() + 60 : center.getY();

            long t0 = System.currentTimeMillis();
            for (int i = 0; i < d.count; i++) {
                BlockState st = d.states[d.ids[i] & 0xFFFF];
                BlockPos p = new BlockPos(bx + d.xs[i], by + d.ys[i], bz + d.zs[i]);
                int f = st.getLightEmission() > 0 ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS;
                level.setBlock(p, st, f);
            }
            long ms = System.currentTimeMillis() - t0;
            final long elapsed = ms;
            source.sendSuccess(() -> Component.literal("Frégate posée : " + d.count + " blocs ("
                    + d.nx + "x" + d.ny + "x" + d.nz + ") base=" + bx + "," + by + "," + bz + " en " + elapsed + " ms"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Échec frégate : " + e));
            return 0;
        }
    }

    private static VoxelData load() throws IOException {
        if (cached != null) return cached;
        InputStream in = FrigateCommand.class.getResourceAsStream("/assets/voiddynasty/structures/frigate.bin.gz");
        if (in == null) throw new IOException("frigate.bin.gz introuvable");
        byte[] raw;
        try (GZIPInputStream gz = new GZIPInputStream(in)) {
            raw = gz.readAllBytes();
        }
        ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        int nx = bb.getInt(), ny = bb.getInt(), nz = bb.getInt(), count = bb.getInt();
        int paletteLen = bb.getInt();
        String[] palette = new String[paletteLen];
        for (int i = 0; i < paletteLen; i++) {
            int l = bb.getInt();
            byte[] name = new byte[l];
            bb.get(name);
            palette[i] = new String(name, StandardCharsets.UTF_8);
        }
        BlockState[] states = new BlockState[paletteLen];
        for (int i = 0; i < paletteLen; i++) {
            try {
                states[i] = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, palette[i], false).blockState();
            } catch (CommandSyntaxException e) {
                states[i] = Blocks.AIR.defaultBlockState();
            }
        }
        short[] ids = new short[count];
        int[] xs = new int[count], ys = new int[count], zs = new int[count];
        for (int i = 0; i < count; i++) {
            ids[i] = bb.getShort();
            xs[i] = bb.getShort() & 0xFFFF;
            ys[i] = bb.getShort() & 0xFFFF;
            zs[i] = bb.getShort() & 0xFFFF;
        }
        cached = new VoxelData(nx, ny, nz, count, ids, xs, ys, zs, states);
        return cached;
    }

    private record VoxelData(int nx, int ny, int nz, int count, short[] ids,
                             int[] xs, int[] ys, int[] zs, BlockState[] states) {}
}
