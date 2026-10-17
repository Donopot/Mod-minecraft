package com.donopot.voiddynasty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class CruiserCommand {
    private static VoxelData cached;
    private static final Map<ResourceKey<Level>, BlockPos> SHIP_BASES = new HashMap<>();

    private static BlockPos getShipBase(ServerLevel level) {
        return SHIP_BASES.get(level.dimension());
    }

    private static void setShipBase(ServerLevel level, BlockPos pos) {
        SHIP_BASES.put(level.dimension(), pos);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cruiser")
                        .then(Commands.literal("sky").executes(ctx -> spawn(ctx.getSource(), true)))
                        .then(Commands.literal("ground").executes(ctx -> spawn(ctx.getSource(), false)))
                        .executes(ctx -> spawn(ctx.getSource(), true))
        );
        dispatcher.register(Commands.literal("bridge").executes(ctx -> bridge(ctx.getSource())));
        dispatcher.register(Commands.literal("hyperjump")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(ctx -> jump(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "x"),
                                        IntegerArgumentType.getInteger(ctx, "z"))))));
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
            setShipBase(level, new BlockPos(bx, by, bz));
            final long elapsed = ms;
            source.sendSuccess(() -> Component.literal("Cruiser posé : " + d.count + " blocs (" + d.nx + "x" + d.ny + "x" + d.nz + ") base=" + bx + "," + by + "," + bz + " en " + elapsed + " ms"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Échec cruiser : " + e));
            return 0;
        }
    }

    private static int bridge(CommandSourceStack source) {
        try {
            ServerLevel level = source.getLevel();
            BlockPos base = getShipBase(level);
            if (base == null) {
                source.sendFailure(Component.literal("Pas de vaisseau posé — /cruiser sky d'abord"));
                return 0;
            }
            VoxelData d = load();
            ServerPlayer player = source.getPlayerOrException();
            double px = base.getX() + d.nx / 2.0 + 0.5;
            double py = base.getY() + 88;
            double pz = base.getZ() + d.nz / 2.0 + 0.5;
            player.teleportTo(px, py, pz);
            source.sendSuccess(() -> Component.literal("Téléporté sur la passerelle"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Échec bridge : " + e));
            return 0;
        }
    }

    private static int jump(CommandSourceStack source, int targetX, int targetZ) {
        try {
            ServerLevel level = source.getLevel();
            BlockPos base = getShipBase(level);
            if (base == null) {
                source.sendFailure(Component.literal("Pas de vaisseau posé — /cruiser sky d'abord"));
                return 0;
            }
            VoxelData d = load();
            ServerPlayer player = source.getPlayer();
            int newBx = targetX - d.nx / 2;
            int newBz = targetZ - d.nz / 2;
            int newBy = base.getY();
            int dx = newBx - base.getX();
            int dz = newBz - base.getZ();
            if (dx == 0 && dz == 0) {
                source.sendFailure(Component.literal("Déjà sur place"));
                return 0;
            }

            long t0 = System.currentTimeMillis();
            double cx = base.getX() + d.nx / 2.0;
            double cy = base.getY() + d.ny / 2.0;
            double cz = base.getZ() + d.nz / 2.0;

            // 1. sauvegarder le contenu des coffres
            List<ItemStack[]> saved = new ArrayList<>();
            for (int idx : d.chestIndices) {
                BlockPos p = new BlockPos(base.getX() + d.xs[idx], base.getY() + d.ys[idx], base.getZ() + d.zs[idx]);
                BlockEntity be = level.getBlockEntity(p);
                ItemStack[] items;
                if (be instanceof ChestBlockEntity chest) {
                    int n = chest.getContainerSize();
                    items = new ItemStack[n];
                    for (int s = 0; s < n; s++) items[s] = chest.getItem(s).copy();
                } else {
                    items = new ItemStack[0];
                }
                saved.add(items);
            }

            // 2. effet d'entrée
            if (player != null) player.sendOverlayMessage(Component.literal("☄ Entrée en hyperespace…"));
            level.sendParticles(ParticleTypes.PORTAL, cx, cy, cz, 800, d.nx / 2.0, d.ny / 2.0, d.nz / 2.0, 0.05);
            level.playSound(null, cx, cy, cz, SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0f, 1.0f);

            // 3. effacer l'ancien (rapide, sans drop ni notification)
            int eraseFlag = Block.UPDATE_SUPPRESS_DROPS;
            for (int i = 0; i < d.count; i++) {
                BlockPos p = new BlockPos(base.getX() + d.xs[i], base.getY() + d.ys[i], base.getZ() + d.zs[i]);
                level.setBlock(p, Blocks.AIR.defaultBlockState(), eraseFlag);
            }

            // 4. replacer
            for (int i = 0; i < d.count; i++) {
                BlockState st = d.states[d.ids[i] & 0xFFFF];
                BlockPos p = new BlockPos(newBx + d.xs[i], newBy + d.ys[i], newBz + d.zs[i]);
                level.setBlock(p, st, Block.UPDATE_NONE);
            }

            // 5. restaurer le contenu des coffres
            int restored = 0;
            for (int k = 0; k < d.chestIndices.length; k++) {
                int idx = d.chestIndices[k];
                BlockPos p = new BlockPos(newBx + d.xs[idx], newBy + d.ys[idx], newBz + d.zs[idx]);
                BlockEntity be = level.getBlockEntity(p);
                if (be instanceof ChestBlockEntity chest) {
                    ItemStack[] items = saved.get(k);
                    for (int s = 0; s < items.length; s++) chest.setItem(s, items[s]);
                    chest.setChanged();
                    restored++;
                }
            }

            // 6. téléporter toutes les entités à bord (joueurs, mobs, items au sol)
            AABB box = new AABB(base.getX(), base.getY(), base.getZ(),
                    base.getX() + d.nx, base.getY() + d.ny, base.getZ() + d.nz);
            List<Entity> ents = level.getEntities((Entity) null, box, e -> true);
            for (Entity e : ents) e.teleportTo(e.getX() + dx, e.getY(), e.getZ() + dz);

            // 7. effet de sortie
            double ncx = newBx + d.nx / 2.0;
            double ncz = newBz + d.nz / 2.0;
            level.sendParticles(ParticleTypes.PORTAL, ncx, cy, ncz, 400, d.nx / 2.0, d.ny / 2.0, d.nz / 2.0, 0.05);
            if (player != null) player.sendOverlayMessage(Component.literal("☄ Saut terminé"));

            long ms = System.currentTimeMillis() - t0;
            setShipBase(level, new BlockPos(newBx, newBy, newBz));
            final long elapsed = ms;
            final int nmoved = ents.size();
            final int nrest = restored;
            source.sendSuccess(() -> Component.literal("Saut hyperespace : " + d.count + " blocs → (" + targetX + "," + targetZ + ") en " + elapsed + " ms — " + nmoved + " entités, " + nrest + " coffres restaurés"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Échec hyperjump : " + e));
            return 0;
        }
    }

    private static VoxelData load() throws IOException {
        if (cached != null) return cached;
        InputStream in = CruiserCommand.class.getResourceAsStream("/assets/voiddynasty/structures/emperor.bin.gz");
        if (in == null) throw new IOException("emperor.bin.gz introuvable");
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
        List<Integer> chest = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (states[ids[i] & 0xFFFF].getBlock() instanceof ChestBlock) chest.add(i);
        }
        int[] chestIndices = new int[chest.size()];
        for (int i = 0; i < chest.size(); i++) chestIndices[i] = chest.get(i);
        cached = new VoxelData(nx, ny, nz, count, ids, xs, ys, zs, states, chestIndices);
        return cached;
    }

    private record VoxelData(int nx, int ny, int nz, int count, short[] ids,
                             int[] xs, int[] ys, int[] zs, BlockState[] states, int[] chestIndices) {}
}
