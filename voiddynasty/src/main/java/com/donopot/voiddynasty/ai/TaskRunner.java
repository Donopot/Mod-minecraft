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
        int ticks;

        Running(BrainTask task, LivingEntity bot) {
            this.task = task;
            this.bot = bot;
        }
    }

    private static final Map<UUID, Running> TASKS = new HashMap<>();

    private TaskRunner() {}

    public static void assign(ServerLevel level, LivingEntity bot, BrainTask task) {
        if (bot == null || task == null) return;
        Running previous = TASKS.get(bot.getUUID());
        if (previous != null && previous.task != null) {
            try {
                previous.task.cancel(new BrainTask.Ctx(level, bot, BrainConfig.get()));
            } catch (RuntimeException ignored) {
            }
        }
        Running running = new Running(task, bot);
        TASKS.put(bot.getUUID(), running);
        try {
            task.start(new BrainTask.Ctx(level, bot, BrainConfig.get()));
        } catch (RuntimeException e) {
            running.task = null;
            running.lastOutcome = "tache impossible a demarrer : " + e.getMessage();
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
        try {
            r.task.cancel(new BrainTask.Ctx(level, bot, BrainConfig.get()));
        } catch (RuntimeException ignored) {
        }
        r.task = null;
        r.lastOutcome = "tache annulee (" + reason + ") : " + what;
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
            if (bot == null || bot.isRemoved() || !bot.isAlive()) {
                r.task = null;
                r.lastOutcome = "tache interrompue : le servitor n'existe plus";
                continue;
            }
            ServerLevel level = (ServerLevel) bot.level();
            BrainTask.Ctx ctx = new BrainTask.Ctx(level, bot, BrainConfig.get());
            if (r.ticks++ % 10 == 0) BrainTasks.suckNearby(ctx);

            BrainTask.Status status;
            try {
                status = r.task.tick(ctx);
            } catch (RuntimeException e) {
                r.lastOutcome = "tache echouee sur erreur : " + e.getClass().getSimpleName();
                r.task = null;
                ServitorBrain.onTaskFinished(r.lastOutcome);
                continue;
            }
            if (status == BrainTask.Status.DONE) {
                r.lastOutcome = "tache terminee : " + r.task.describe();
                ServitorBrain.onTaskFinished(r.lastOutcome);
                r.task = null;
            } else if (status == BrainTask.Status.FAILED) {
                r.lastOutcome = "tache echouee : " + r.task.describe();
                ServitorBrain.onTaskFinished(r.lastOutcome);
                r.task = null;
            }
        }
    }
}
