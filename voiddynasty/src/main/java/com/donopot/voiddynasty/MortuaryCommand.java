package com.donopot.voiddynasty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Mission « Monde Mortuaire » : place la ruine (avant-poste abandonné) + la relique.
 * /mortuary           -> place la ruine sous le joueur
 * /mortuary <x> <z>   -> place la ruine à (x, z) à la hauteur standard
 */
public final class MortuaryCommand {

    private static final int CHEST_X = 40, CHEST_Y = 2, CHEST_Z = 40; // position relative du coffre dans ruin.bin.gz

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mortuary")
                .executes(ctx -> run(ctx.getSource(), null, null))
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(ctx -> run(ctx.getSource(),
                                        ctx.getArgument("x", Integer.class),
                                        ctx.getArgument("z", Integer.class))))));
    }

    private static int run(CommandSourceStack src, Integer x, Integer z) {
        ServerLevel level = src.getLevel();
        BlockPos base;
        if (x == null || z == null) {
            Entity e = src.getEntity();
            if (e == null) {
                src.sendFailure(Component.literal("Donne X et Z, ou exécute la commande en tant que joueur."));
                return 0;
            }
            base = new BlockPos(e.getBlockX(), e.getBlockY() - 1, e.getBlockZ());
        } else {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            base = new BlockPos(x, surfaceY, z);
        }

        long t0 = System.currentTimeMillis();
        TemplateData d = load();
        place(level, d, base);
        fillChest(level, base);
        spawnMobs(level, base);
        MissionState state = MissionState.get(level);
        state.ruinPos = base;
        state.setDirty();
        long elapsed = System.currentTimeMillis() - t0;
        src.sendSuccess(() -> Component.literal(
                "Ruine posée : " + d.count + " blocs en " + elapsed + " ms à " + base.toShortString()), false);
        if (src.getEntity() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(MissionState.missionMessage());
        }
        return 1;
    }

    private static void fillChest(ServerLevel level, BlockPos base) {
        BlockPos chestPos = base.offset(CHEST_X, CHEST_Y, CHEST_Z);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(VoidDynastyMod.GELLAR_SHARD));
            chest.setChanged();
        }
    }

    private static void spawnMobs(ServerLevel level, BlockPos base) {
        String[] types = {"zombie", "skeleton", "husk", "drowned"};
        int[][] offs = {
                {15, 32}, {15, 48}, {65, 32}, {65, 48},
                {28, 19}, {52, 19}, {28, 61}, {52, 61}
        };
        int spawned = 0;
        for (int i = 0; i < offs.length; i++) {
            String typeName = types[i % types.length];
            EntityType<?> et = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("minecraft", typeName));
            if (et == null) {
                VoidDynastyMod.LOGGER.warn("[voiddynasty] spawnMobs: type null pour {}", typeName);
                continue;
            }
            BlockPos p = new BlockPos(base.getX() + offs[i][0], base.getY() + 2, base.getZ() + offs[i][1]);
            Entity e = et.spawn(level, p, EntitySpawnReason.MOB_SUMMONED);
            if (e == null) {
                VoidDynastyMod.LOGGER.warn("[voiddynasty] spawnMobs: spawn null pour {} à {}", typeName, p.toShortString());
            } else {
                spawned++;
            }
        }
        VoidDynastyMod.LOGGER.info("[voiddynasty] spawnMobs: {} mobs spawnés", spawned);
    }

    private static void place(ServerLevel level, TemplateData d, BlockPos base) {
        for (int i = 0; i < d.count; i++) {
            BlockState st = d.states[d.ids[i] & 0xFFFF];
            BlockPos p = new BlockPos(base.getX() + d.xs[i], base.getY() + d.ys[i], base.getZ() + d.zs[i]);
            int f = st.getLightEmission() > 0 ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS;
            level.setBlock(p, st, f);
        }
    }

    private static TemplateData load() {
        try (InputStream in = MortuaryCommand.class.getResourceAsStream("/assets/voiddynasty/structures/ruin.bin.gz")) {
            if (in == null) throw new IOException("ruin.bin.gz introuvable");
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
            for (int i = 0; i < palLen; i++) {
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
            return new TemplateData(nx, ny, nz, count, ids, xs, ys, zs, states);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TemplateData(int nx, int ny, int nz, int count, short[] ids,
                                int[] xs, int[] ys, int[] zs, BlockState[] states) {}
}
