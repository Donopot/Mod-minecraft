package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Application d'un ordre validé : soit une tâche qui va durer, soit une réponse immédiate.
 *
 * <p>Le cerveau ne casse jamais un bloc lui-même. Il pose une tâche, et c'est
 * {@link TaskRunner} qui l'exécute tick par tick, avec ses propres garde-fous (portée,
 * chemin, délai). Une tâche interrompue laisse toujours une trace lisible.</p>
 *
 * <p>Toujours appelé sur le fil du serveur.</p>
 */
public final class BrainOrders {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("voiddynasty-servitor");

    private BrainOrders() {}

    /** @return un compte rendu court, réinjecté dans le prompt du cycle suivant. */
    public static String apply(BrainTools.Order order, ServerLevel level, LivingEntity bot, BrainConfig cfg) {
        String refusal=BrainTools.validate(order,level,bot,cfg);
        if(refusal!=null) return "REFUSE: "+refusal;
        String name = order.name();
        return switch (name) {
            case "say" -> {
                String msg = order.strArg("message").trim();
                broadcast(level, "[Servitor] " + msg);
                yield "message dit : " + msg;
            }
            case "wait" -> {
                // Attendre ne remplace JAMAIS une tache en cours : sinon le simple fait de dire
                // « patiente » annulerait le minage en cours. C'est le piege classique.
                if(com.donopot.voiddynasty.work.WorkService.owns(level,bot)) yield "chantier en cours; work_status pour les compteurs";
                if (TaskRunner.current(bot) != null) {
                    yield "attente : la tache en cours continue (" + TaskRunner.describe(bot) + ")";
                }
                int seconds = BrainTools.clamp(order.args().has("seconds")?order.intArg("seconds"):1, 1, 60);
                TaskRunner.assign(level, bot, new BrainTasks.Wait(seconds));
                yield "attente de " + seconds + " s";
            }
            case "stop" -> {
                if(com.donopot.voiddynasty.work.WorkService.owns(level,bot)) {
                    com.donopot.voiddynasty.work.WorkService.stop(level);
                    yield "chantier pause; ressources et journal conserves";
                }
                yield TaskRunner.cancel(bot,level,"demande du modele");
            }
            case "goto" -> {
                BlockPos dst = new BlockPos(order.intArg("x"), order.intArg("y"), order.intArg("z"));
                TaskRunner.assign(level, bot, new BrainTasks.Goto(dst, cfg.timeoutSeconds * 3));
                yield "deplacement lance vers " + dst.toShortString();
            }
            case "come_to_player" -> {
                String player = order.strArg("player");
                TaskRunner.assign(level, bot, new BrainTasks.ComeTo(player));
                yield "deplacement vers " + player;
            }
            case "follow_player" -> {
                String player = order.strArg("player");
                double distance = order.args().has("distance") ? order.doubleArg("distance") : 3.0;
                distance = Math.max(2.0, Math.min(10.0, distance));
                TaskRunner.assign(level, bot, new BrainTasks.Follow(player, distance));
                yield "suivi de " + player + " a " + (int) distance + " blocs";
            }
            case "work_capabilities" -> com.donopot.voiddynasty.work.MissionService.capabilities().toString();
            case "work_submit" -> com.donopot.voiddynasty.work.MissionService.submit(level,
                    com.google.gson.JsonParser.parseString(order.strArg("spec")).getAsJsonObject(),bot).toString();
            case "work_status" -> com.donopot.voiddynasty.work.MissionService.status(level,order.strArg("mission_id")).toString();
            case "work_control" -> com.donopot.voiddynasty.work.MissionService.control(level,order.strArg("mission_id"),order.strArg("action")).toString();
            case "attack_nearest" -> {
                int radius = BrainTools.clamp(order.args().has("radius") ? order.intArg("radius") : 12, 4, 24);
                int count = BrainTools.clamp(order.args().has("count") ? order.intArg("count") : 1, 1, 10);
                TaskRunner.assign(level, bot, new BrainTasks.AttackNearest(radius, count));
                yield "combat lance (rayon " + radius + ", " + count + " cibles)";
            }
            case "pickup" -> {
                int radius = BrainTools.clamp(order.args().has("radius") ? order.intArg("radius") : 16, 4, 32);
                int count = BrainTools.clamp(order.args().has("count") ? order.intArg("count") : 32, 1, 64);
                TaskRunner.assign(level, bot, new BrainTasks.Pickup(radius, count));
                yield "ramassage lance (rayon " + radius + ")";
            }
            case "scan" -> BrainSenses.find(level,bot,order.strArg("block"),clamp(order,cfg),1);
            case "probe" -> BrainSenses.summary(level, bot, clamp(order, cfg));
            case "find" -> BrainSenses.find(level, bot, order.strArg("block"),
                    order.args().has("radius") ? order.intArg("radius") : 16,
                    order.args().has("limit") ? order.intArg("limit") : 6);
            case "box" -> BrainSenses.box(level,
                    new BlockPos(order.intArg("x"), order.intArg("y"), order.intArg("z")),
                    order.args().has("half") ? order.intArg("half") : 2,
                    order.args().has("layers") ? order.intArg("layers") : 2);
            case "remember" -> BrainNotes.add(level, bot, order.strArg("note"));
            case "recall" -> BrainNotes.search(order.strArg("query"));
            case "where" -> where(level, bot, order.strArg("player"));
            default -> "ordre ignore : " + name;
        };
    }

    private static int clamp(BrainTools.Order order, BrainConfig cfg) {
        return BrainTools.clamp(order.args().has("radius") ? order.intArg("radius") : 24, 4, 32);
    }

    private static String where(ServerLevel level, LivingEntity bot, String player) {
        ServerPlayer p = BrainTools.playerByName(level, player);
        if (p == null) return "joueur introuvable : " + player;
        String pos = p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ();
        if (bot == null) return player + " est en " + pos;
        int d = (int) Math.sqrt(bot.distanceToSqr(p));
        return player + " est en " + pos + " (" + d + " blocs, " + p.level().dimension().identifier().getPath() + ")";
    }

    private static void broadcast(ServerLevel level, String message) {
        // Journalise TOUJOURS : sans joueur connecte, un message aux joueurs ne laisse aucune
        // trace verifiable. La ligne console est la preuve que l'ordre a bien ete execute.
        LOGGER.info("[Servitor] {}", message);
        level.getServer().getPlayerList().broadcastSystemMessage(
                net.minecraft.network.chat.Component.literal(message), false);
    }
}
