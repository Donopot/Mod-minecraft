package com.donopot.voiddynasty.ai;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exécuteur de tâches, tick par tick.
 *
 * <p>Une seule tâche à la fois par servitor : c'est ce qui empêche deux ordres de se disputer
 * la même paire de jambes. Le cerveau peut remplacer une tâche, l'opérateur peut l'annuler, et
 * dans tous les cas l'issue (terminée, échouée, annulée) est renvoyée au cerveau pour le cycle
 * suivant — il ne devine pas, il lit.</p>
 */
public final class TaskRunner {

    private static final class Running {
        BrainTask task;
        LivingEntity bot;
        String lastOutcome = "";
        ServerLevel level;
        Result result = Result.RUNNING;
        BrainTask lastTask;

        Running(BrainTask task, LivingEntity bot) {
            this.task = task;
            this.bot = bot;
        }
    }

    public enum Result { RUNNING, DONE, FAILED, CANCELLED }

    private static final Map<UUID, Running> TASKS = new HashMap<>();

    private TaskRunner() {}
    static void reset() {
        for (UUID id : TASKS.keySet()) com.donopot.voiddynasty.work.WorkChunkTickets.releaseEntity(id);
        TASKS.clear();
    }

    public static Result result(LivingEntity bot, BrainTask task) {
        Running r = bot == null ? null : TASKS.get(bot.getUUID());
        if (r == null || r.lastTask != task) return Result.CANCELLED;
        return r.result;
    }

    private static void finish(Running r, Result result, String outcome) {
        BrainTask task = r.task;
        r.task = null;
        r.result = result;
        r.lastOutcome = outcome;
        try { if (task != null) task.cancel(new BrainTask.Ctx(r.level, r.bot, BrainConfig.get())); }
        catch (RuntimeException e) { r.lastOutcome += " (arret: " + e.getClass().getSimpleName() + ")"; }
        com.donopot.voiddynasty.work.WorkChunkTickets.releaseEntity(r.bot.getUUID());
        ServitorBrain.onTaskFinished(r.lastOutcome);
    }

    public static void assign(ServerLevel level, LivingEntity bot, BrainTask task) {
        if (bot == null || task == null) throw new IllegalArgumentException("aucun servitor/tache");
        if (com.donopot.voiddynasty.work.WorkService.owns(level, bot)) throw new IllegalStateException("ACTION_BUSY: chantier actif");
        Running previous = TASKS.get(bot.getUUID());
        if (previous != null && previous.task != null)
            finish(previous, Result.CANCELLED, "tache remplacee par un nouvel ordre");
        Running running = new Running(task, bot);
        running.level = level;
        running.lastTask = task;
        TASKS.put(bot.getUUID(), running);
        try {
            com.donopot.voiddynasty.work.WorkService.activateMovement(bot);
            com.donopot.voiddynasty.work.WorkService.keepTicking(level, bot);
            task.start(new BrainTask.Ctx(level, bot, BrainConfig.get()));
        } catch (RuntimeException e) {
            finish(running, Result.FAILED, "tache impossible a demarrer : " + e.getMessage());
        }
    }

    public static BrainTask current(LivingEntity bot) {
        Running r = bot == null ? null : TASKS.get(bot.getUUID());
        return r == null ? null : r.task;
    }

    /** Dernière issue non lue (terminée / échouée / annulée), puis remise à zéro. */
    public static String consumeOutcome(LivingEntity bot) {
        Running r = bot == null ? null : TASKS.get(bot.getUUID());
        if (r == null || r.lastOutcome.isEmpty()) return "";
        String out = r.lastOutcome;
        r.lastOutcome = "";
        return out;
    }

    public static String cancel(LivingEntity bot, ServerLevel level, String reason) {
        Running r = bot == null ? null : TASKS.get(bot.getUUID());
        if (r == null || r.task == null) return "aucune tache en cours";
        String what = r.task.describe();
        finish(r, Result.CANCELLED, "tache annulee (" + reason + ") : " + what);
        return "tache annulee : " + what;
    }

    public static String describe(LivingEntity bot) {
        BrainTask t = current(bot);
        if (t == null) return "aucune";
        int p = t.progress();
        return t.describe() + (p >= 0 ? " [" + p + " %]" : "");
    }

    /** Appelé à chaque tick serveur, même cerveau éteint : une tâche lancée se termine. */
    public static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;
        for (Map.Entry<UUID, Running> entry : TASKS.entrySet()) {
            Running r = entry.getValue();
            if (r.task == null) continue;
            LivingEntity bot = r.bot;
            if (bot == null || bot.isRemoved() || !bot.isAlive() || bot.level() != r.level) {
                finish(r, Result.CANCELLED, "tache interrompue : compagnon absent ou dimension changee");
                continue;
            }
            ServerLevel level = r.level;
            BrainTask.Ctx ctx = new BrainTask.Ctx(level, bot, BrainConfig.get());
            if (com.donopot.voiddynasty.work.WorkService.owns(level, bot)) {
                cancel(bot, level, "controle reserve au chantier");
                continue;
            }

            BrainTask.Status status;
            try {
                com.donopot.voiddynasty.work.WorkService.keepTicking(level, bot);
                status = r.task.tick(ctx);
            } catch (RuntimeException e) {
                finish(r, Result.FAILED, "tache echouee sur erreur : " + e.getClass().getSimpleName());
                continue;
            }
            if (status == BrainTask.Status.DONE)
                finish(r, Result.DONE, "tache terminee : " + r.task.describe());
            else if (status == BrainTask.Status.FAILED)
                finish(r, Result.FAILED, "tache echouee : " + r.task.describe());
        }
    }
}
