package com.donopot.voiddynasty.ai;

import com.donopot.voiddynasty.work.MissionService;
import com.donopot.voiddynasty.work.WorkObservation;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ce que le compagnon voit : lui-meme, les joueurs connectes, et son environnement
 * immediat. Lecture seule, bornee, et jamais d'invention : une cellule non chargee
 * ou protegee reste "inconnue" et n'est jamais comptee comme de l'air.
 */
public final class BrainView {
    private BrainView() {}

    private static final int MAX_ENTITIES = 24;
    private static final int MAX_ENTITIES_OBSERVED = 64;

    private static String blockId(ServerLevel level, BlockPos pos) {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /** Le compagnon existe-t-il, ou est-il, dans quel etat. */
    public static String status(ServerLevel level, LivingEntity bot) {
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("sampled_tick", level.getGameTime());
        out.addProperty("dimension", level.dimension().identifier().toString());
        MinecraftServer server = level.getServer();
        out.addProperty("players_online", server.getPlayerList().getPlayerCount());
        if (bot == null) {
            out.addProperty("present", false);
            out.addProperty("detail", "aucun servitor pilote dans cette dimension");
            return out.toString();
        }
        BlockPos pos = bot.blockPosition();
        out.addProperty("present", true);
        out.addProperty("awake", !(bot instanceof net.minecraft.world.entity.Mob noAiMob) || !noAiMob.isNoAi());
        out.addProperty("type", EntityType.getKey(bot.getType()).toString());
        out.add("pos", MissionService.positionJson(pos));
        out.addProperty("health", round1(bot.getHealth()));
        out.addProperty("max_health", round1(bot.getMaxHealth()));
        out.addProperty("on_ground", bot.onGround());
        out.addProperty("in_water", bot.isInWater());
        out.addProperty("in_lava", bot.isInLava());
        out.addProperty("chunk_loaded", level.hasChunkAt(pos));
        out.addProperty("standing_on", blockId(level, pos.below()));
        out.addProperty("feet_free", level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir());
        out.addProperty("bag_entries", BrainBags.list(server).size());
        out.addProperty("scope", "Etat du compagnon uniquement ; aucune garantie sur les blocs voisins, voir servitor_look");
        return out.toString();
    }

    /** Qui est connecte, ou, et a quelle distance du compagnon. */
    public static String players(ServerLevel level, LivingEntity bot) {
        MinecraftServer server = level.getServer();
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("sampled_tick", level.getGameTime());
        out.addProperty("dimension", level.dimension().identifier().toString());
        if (bot != null) {
            out.add("servitor_pos", MissionService.positionJson(bot.blockPosition()));
        } else {
            out.add("servitor_pos", JsonNull.INSTANCE);
            out.addProperty("servitor_present", false);
        }
        JsonArray arr = new JsonArray();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            JsonObject j = new JsonObject();
            j.addProperty("name", player.getName().getString());
            j.addProperty("uuid", player.getUUID().toString());
            j.addProperty("dimension", player.level().dimension().identifier().toString());
            j.add("pos", MissionService.positionJson(player.blockPosition()));
            j.addProperty("health", round1(player.getHealth()));
            j.addProperty("game_mode", player.gameMode().name());
            j.addProperty("op", server.getPlayerList().isOp(new NameAndId(player.getUUID(), player.getName().getString())));
            if (bot != null && player.level() == level) {
                j.addProperty("distance", round1(Math.sqrt(bot.distanceToSqr(player))));
            } else {
                j.add("distance", JsonNull.INSTANCE);
            }
            arr.add(j);
        }
        out.addProperty("count", arr.size());
        out.add("players", arr);
        out.addProperty("scope", "Positions reelles des joueurs connectes ; distance nulle si le compagnon est dans une autre dimension");
        return out.toString();
    }

    /** Etat du chantier en cours : ce que le moteur fait vraiment, et pourquoi il s'arrete. */
    public static String work(ServerLevel level) {
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("sampled_tick", level.getGameTime());
        out.addProperty("dimension", level.dimension().identifier().toString());
        com.donopot.voiddynasty.work.WorkState state = com.donopot.voiddynasty.work.WorkState.get(level);
        out.addProperty("job", state.jobName == null ? "" : state.jobName);
        out.addProperty("mission_id", state.missionId == null ? "" : state.missionId);
        out.addProperty("has_depot", state.hasDepot);
        out.addProperty("building_depot", state.buildingDepot);
        out.addProperty("depot_planned", state.depotPlanned);
        out.addProperty("depots", state.depotCount);
        out.addProperty("supply_chests", state.supplyChests);
        out.addProperty("supply_supports", state.supplySupports);
        out.addProperty("done", state.done);
        // Compteurs separes : le quota productif ne compte que les blocs de la zone.
        out.addProperty("zone_mined", state.zoneMined);
        out.addProperty("access_broken", state.accessBroken);
        out.addProperty("total_mined", state.done);
        out.addProperty("skipped", state.skipped);
        out.addProperty("total", state.total);
        out.addProperty("last_stop", state.lastStop == null ? "" : state.lastStop);
        out.addProperty("progress", state.progress);
        out.addProperty("cursor_y", state.cursorY);
        out.addProperty("min_y", state.minY);
        out.addProperty("has_target", state.hasTarget);
        out.addProperty("target", state.hasTarget ? new BlockPos(state.targetX, state.targetY, state.targetZ).toShortString() : "");
        out.addProperty("scan_note", com.donopot.voiddynasty.work.WorkService.lastScanNote);
        out.addProperty("scope", "Etat interne du moteur de chantier ; last_stop dit pourquoi il attend");
        return out.toString();
    }

    /** Environnement immediat : blocs, dangers, entites vivantes autour du compagnon. */
    public static String look(ServerLevel level, LivingEntity bot, int radius) {
        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("sampled_tick", level.getGameTime());
        out.addProperty("dimension", level.dimension().identifier().toString());
        if (bot == null) {
            out.addProperty("ok", false);
            out.addProperty("error", "NO_SERVITOR");
            out.addProperty("detail", "aucun servitor pilote dans cette dimension");
            return out.toString();
        }
        int r = Math.max(2, Math.min(radius, 24));
        BlockPos center = bot.blockPosition();
        BlockPos min = center.offset(-r, -4, -r);
        BlockPos max = center.offset(r, 3, r);
        JsonObject spec = new JsonObject();
        spec.addProperty("schema", 1);
        spec.addProperty("type", "OBSERVE");
        spec.addProperty("limit", 10);
        spec.add("min", MissionService.positionJson(min));
        spec.add("max", MissionService.positionJson(max));
        JsonObject base;
        try {
            base = JsonParser.parseString(WorkObservation.inspect(level, spec).toString()).getAsJsonObject();
        } catch (RuntimeException error) {
            out.addProperty("ok", false);
            out.addProperty("error", "OBSERVATION_FAILED");
            out.addProperty("detail", String.valueOf(error.getMessage()));
            return out.toString();
        }
        AABB box = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        List<Entity> nearby = new ArrayList<>(level.getEntities(bot, box, e -> e != bot && e.isAlive()));
        List<Entity> living = new ArrayList<>();
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity) living.add(entity);
        }
        living.sort(Comparator.comparingDouble(e -> e.distanceToSqr(bot)));
        JsonArray entities = new JsonArray();
        int hostile = 0;
        for (Entity entity : living) {
            if (entity instanceof Monster) hostile++;
            if (entities.size() >= MAX_ENTITIES) continue;
            JsonObject j = new JsonObject();
            j.addProperty("type", EntityType.getKey(entity.getType()).toString());
            j.add("pos", MissionService.positionJson(entity.blockPosition()));
            j.addProperty("distance", round1(Math.sqrt(entity.distanceToSqr(bot))));
            if (entity instanceof ServerPlayer player) {
                j.addProperty("player", player.getName().getString());
            } else {
                j.addProperty("name", entity.getName().getString());
            }
            j.addProperty("hostile", entity instanceof Monster);
            if (entity instanceof LivingEntity le) {
                j.addProperty("health", round1(le.getHealth()));
            }
            entities.add(j);
        }
        base.addProperty("radius", r);
        base.addProperty("entities_total", living.size());
        base.addProperty("hostiles_total", hostile);
        base.addProperty("entities_truncated", living.size() > entities.size());
        base.add("entities", entities);
        base.addProperty("entities_limit", MAX_ENTITIES_OBSERVED);
        base.add("self_pos", MissionService.positionJson(center));
        base.addProperty("scope", "Blocs et entites reels dans la boite autour du compagnon ; cellules non chargees ou protegees restent inconnues");
        return base.toString();
    }
}
