package com.donopot.voiddynasty.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.LevelResource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Routines déterministes : trouver → accéder → récolter → déposer → revenir.
 *
 * <p>Une routine n'est <b>pas</b> une tâche du compagnon : elle ne prend jamais son corps.
 * Elle soumet une mission native ({@code MissionService}), la surveille sans la contrôler, puis,
 * quand elle est livrée, rend le corps disponible et demande simplement le retour au point
 * d'origine. C'est cette séparation qui évite le blocage « ACTION_BUSY » : une tâche du cerveau
 * et un chantier natif ne peuvent pas posséder le même corps en même temps.</p>
 *
 * <p>Une mission en pause ou en échec n'est jamais reprise ni relancée ici : la routine le dit et
 * s'arrête, l'opérateur décide. Une relance silencieuse pourrait doubler du travail déjà récolté.</p>
 */
public final class BrainRoutine {

    /** Une routine en cours d'observation. */
    private record Running(String missionId, UUID bot, BlockPos origin, ServerLevel level,
                           long startedAt, String label) {}

    private static final Map<String, Running> ACTIVE = new LinkedHashMap<>();
    private static final int POLL_TICKS = 20;
    private static final long MAX_TICKS = 24000L * 2;

    private BrainRoutine() {}

    /** Soumet la mission et enregistre la routine. Ne prend pas le corps du compagnon. */
    public static String start(ServerLevel level, LivingEntity bot, String spec) {
        if (level == null || bot == null) return "routine impossible : aucun servitor";
        JsonObject parsed;
        try {
            parsed = JsonParser.parseString(spec).getAsJsonObject();
        } catch (RuntimeException e) {
            return "routine refusee : specification illisible (" + e.getMessage() + ")";
        }
        BlockPos origin = bot.blockPosition();
        if (parsed.has("origin") && parsed.getAsJsonArray("origin").size() == 3) {
            var o = parsed.getAsJsonArray("origin");
            origin = new BlockPos(o.get(0).getAsInt(), o.get(1).getAsInt(), o.get(2).getAsInt());
        }
        JsonObject result = com.donopot.voiddynasty.work.MissionService.submit(level, parsed, bot);
        if (!result.has("ok") || !result.get("ok").getAsBoolean()) {
            String detail = result.has("detail") ? result.get("detail").getAsString() : result.toString();
            return "routine refusee : " + detail;
        }
        String missionId = result.getAsJsonObject("mission").get("id").getAsString();
        String label = parsed.has("request_id") ? parsed.get("request_id").getAsString() : missionId;
        ACTIVE.put(missionId, new Running(missionId, bot.getUUID(), origin, level,
                level.getGameTime(), label));
        return "routine lancee : mission " + missionId + " (" + label + "), retour en "
                + origin.toShortString() + " a la fin";
    }

    /** À appeler à chaque tick serveur : observe les routines, ne possède jamais le corps. */
    public static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;
        for (Running run : ACTIVE.values().toArray(new Running[0])) {
            ServerLevel level = run.level();
            if (level == null || level.getServer() != server) continue;
            if (level.getGameTime() - run.startedAt() > MAX_TICKS) {
                finish(run, "routine abandonnee : delai de deux jours de jeu depasse, mission "
                        + run.missionId() + " laissee en l'etat");
                continue;
            }
            if (level.getGameTime() % POLL_TICKS != 0) continue;

            JsonObject status = com.donopot.voiddynasty.work.MissionService.status(level, run.missionId());
            if (!status.has("mission")) {
                finish(run, "routine interrompue : mission " + run.missionId() + " introuvable dans cette dimension");
                continue;
            }
            JsonObject mission = status.getAsJsonObject("mission");
            String state = mission.get("status").getAsString();
            switch (state) {
                case "SUCCEEDED" -> {
                    LivingEntity bot = findBot(level, run.bot());
                    if (bot == null) {
                        finish(run, "routine terminee : recolte livree, mais le compagnon a disparu avant le retour");
                        continue;
                    }
                    int delivered = mission.has("delivered_total") ? mission.get("delivered_total").getAsInt() : 0;
                    TaskRunner.assign(level, bot, new BrainTasks.Goto(run.origin(), 180));
                    finish(run, "routine terminee : " + delivered + " objet(s) livres, retour en "
                            + run.origin().toShortString() + " lance");
                }
                case "FAILED", "CANCELLED" -> finish(run, "routine echouee : mission " + state
                        + (mission.has("reason") ? " (" + mission.get("reason").getAsString() + ")" : ""));
                case "PAUSED" -> finish(run, "routine en pause : reprise operateur requise (/work mission control "
                        + run.missionId() + " resume)");
                default -> { /* PREPARING, RUNNING : on continue d'observer */ }
            }
        }
    }

    private static LivingEntity findBot(ServerLevel level, UUID uuid) {
        for (var entity : level.getAllEntities()) {
            if (entity.getUUID().equals(uuid) && entity instanceof LivingEntity living && living.isAlive()) {
                return living;
            }
        }
        return null;
    }

    private static void finish(Running run, String report) {
        ACTIVE.remove(run.missionId());
        ServitorBrain.note(report);
    }

    /** Routines en cours, pour l'affichage operateur. */
    public static String describe() {
        if (ACTIVE.isEmpty()) return "aucune routine en cours";
        StringBuilder sb = new StringBuilder(ACTIVE.size() + " routine(s) :");
        for (Running run : ACTIVE.values()) {
            sb.append("\n  ").append(run.label()).append(" → mission ").append(run.missionId())
                    .append(" → retour ").append(run.origin().toShortString());
        }
        return sb.toString();
    }

    /** Oublie une routine interrompue par un arret ou un retrait du compagnon. */
    public static void cancel(UUID bot) {
        ACTIVE.values().removeIf(run -> run.bot().equals(bot));
    }
}
