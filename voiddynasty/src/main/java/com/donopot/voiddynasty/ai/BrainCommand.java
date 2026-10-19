package com.donopot.voiddynasty.ai;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Commandes du servitor persistant et de son cerveau.
 *
 * <pre>
 *   /servitor spawn [1|2|3]        pose un servitor persistant devant soi
 *   /servitor despawn              le retire (et jette son sac)
 *   /servitor stop                 annule la tache en cours
 *   /servitor brain on|off|status|log
 *   /servitor brain url &lt;url&gt;       endpoint compatible OpenAI (ex: http://192.168.1.20:1234/v1)
 *   /servitor brain model &lt;id&gt;     id du modele charge dans LM Studio
 *   /servitor brain interval &lt;s&gt;   secondes entre deux decisions
 *   /servitor brain models         liste les modeles exposes par le serveur
 *   /servitor brain test           verifie la connexion et mesure la latence
 *   /servitor brain ask &lt;consigne&gt; transmet une consigne au prochain cycle
 * </pre>
 *
 * <p>Réservées aux opérateurs : faire agir un agent piloté par un modèle externe est une
 * décision de gouvernance.</p>
 */
public final class BrainCommand {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("voiddynasty-servitor");

    private static final ExecutorService NET = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "voiddynasty-servitor-cmd");
        t.setDaemon(true);
        return t;
    });

    private BrainCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("servitor")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("spawn")
                        .executes(ctx -> spawn(ctx, 2))
                        .then(Commands.argument("variante", IntegerArgumentType.integer(1, 3))
                                .executes(ctx -> spawn(ctx, IntegerArgumentType.getInteger(ctx, "variante")))))
                .then(Commands.literal("despawn").executes(BrainCommand::despawn))
                .then(Commands.literal("routine").then(Commands.literal("list").executes(ctx -> {
                    for (String line : BrainRoutine.describe().split("\n")) {
                        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                    }
                    return 1;
                })))
                .then(Commands.literal("bag")
                        .then(Commands.literal("list").executes(BrainCommand::bags))
                        .then(Commands.literal("audit").executes(BrainCommand::bagAudit))
                        .then(Commands.literal("restore")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> bagAction(ctx, "restore"))))
                        .then(Commands.literal("give")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> bagAction(ctx, "give"))))
                        .then(Commands.literal("drop")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> bagAction(ctx, "drop")))))
                .then(Commands.literal("stop").executes(BrainCommand::stop))
                // Perception : ou est le compagnon, qui est connecte, ce qu'il y a autour.
                .then(Commands.literal("view")
                        .then(Commands.literal("status").executes(ctx -> view(ctx, "status", 0)))
                        .then(Commands.literal("players").executes(ctx -> view(ctx, "players", 0)))
                        .then(Commands.literal("work").executes(ctx -> view(ctx, "work", 0)))
                        .then(Commands.literal("look")
                                .executes(ctx -> view(ctx, "look", 8))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(2, 24))
                                        .executes(ctx -> view(ctx, "look", IntegerArgumentType.getInteger(ctx, "radius"))))))
                .then(Commands.literal("move")
                        .then(Commands.literal("come")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> move(ctx, "come_to_player", StringArgumentType.getString(ctx, "player"), 0)))
                                .executes(ctx -> move(ctx, "come_to_player", "", 0)))
                        .then(Commands.literal("follow")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(ctx -> move(ctx, "follow_player", StringArgumentType.getString(ctx, "player"), 3))))
                        .then(Commands.literal("goto")
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(ctx -> moveTo(ctx))))))
                        .then(Commands.literal("status").executes(BrainCommand::moveStatus)))
                .then(Commands.literal("craft")
                        .then(Commands.literal("list")
                                .executes(ctx -> craftList(ctx, ""))
                                .then(Commands.argument("filtre", StringArgumentType.word())
                                        .executes(ctx -> craftList(ctx, StringArgumentType.getString(ctx, "filtre")))))
                        .then(Commands.literal("plan")
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("nombre", IntegerArgumentType.integer(1, 4096))
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(ctx -> craftPlan(ctx, 16))
                                                                        .then(Commands.argument("rayon", IntegerArgumentType.integer(4, 32))
                                                                                .executes(ctx -> craftPlan(ctx, IntegerArgumentType.getInteger(ctx, "rayon"))))))))))
                        .then(Commands.literal("make")
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("nombre", IntegerArgumentType.integer(1, 4096))
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(BrainCommand::craftMake))))))))
                .then(Commands.literal("smelt")
                        .then(Commands.literal("list")
                                .executes(ctx -> smeltList(ctx, ""))
                                .then(Commands.argument("filtre", StringArgumentType.word())
                                        .executes(ctx -> smeltList(ctx, StringArgumentType.getString(ctx, "filtre")))))
                        .then(Commands.literal("fuels")
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                        .executes(BrainCommand::smeltFuels)))))
                        .then(Commands.literal("make")
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("nombre", IntegerArgumentType.integer(1, 4096))
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(BrainCommand::smeltMake))))))))
                .then(Commands.literal("brain")
                        .then(Commands.literal("on").executes(BrainCommand::on))
                        .then(Commands.literal("off").executes(BrainCommand::off))
                        .then(Commands.literal("status").executes(BrainCommand::status))
                        .then(Commands.literal("log").executes(BrainCommand::log))
                        .then(Commands.literal("last").executes(BrainCommand::last))
                        .then(Commands.literal("models").executes(BrainCommand::models))
                        .then(Commands.literal("test").executes(BrainCommand::test))
                        .then(Commands.literal("url")
                                .then(Commands.argument("url", StringArgumentType.greedyString())
                                        .executes(BrainCommand::url)))
                        .then(Commands.literal("model")
                                .then(Commands.argument("id", StringArgumentType.greedyString())
                                        .executes(BrainCommand::model)))
                        .then(Commands.literal("mode")
                                .then(Commands.literal("state").executes(ctx -> mode(ctx, false)))
                                .then(Commands.literal("order").executes(ctx -> mode(ctx, true))))
                        .then(Commands.literal("chat")
                                .then(Commands.literal("all").executes(ctx -> chat(ctx, true)))
                                .then(Commands.literal("mention").executes(ctx -> chat(ctx, false))))
                        .then(Commands.literal("think")
                                .then(Commands.literal("on").executes(ctx -> think(ctx, false)))
                                .then(Commands.literal("off").executes(ctx -> think(ctx, true))))
                        .then(Commands.literal("timeout")
                                .then(Commands.argument("secondes", IntegerArgumentType.integer(5, 600))
                                        .executes(BrainCommand::timeout)))
                        .then(Commands.literal("interval")
                                .then(Commands.argument("secondes", IntegerArgumentType.integer(1, 120))
                                        .executes(BrainCommand::interval)))
                        .then(Commands.literal("ask")
                                .then(Commands.argument("consigne", StringArgumentType.greedyString())
                                        .executes(BrainCommand::ask)))));
    }

    // --------------------------------------------------------------- servitor

    // --------------------------------------------------------------- perception

    private static int view(CommandContext<CommandSourceStack> ctx, String kind, int radius) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        String json = switch (kind) {
            case "status" -> BrainView.status(level, bot);
            case "players" -> BrainView.players(level, bot);
            case "work" -> BrainView.work(level);
            default -> BrainView.look(level, bot, radius);
        };
        ctx.getSource().sendSuccess(() -> Component.literal("VDVIEW " + kind + " " + json), false);
        return 1;
    }

    /** Livre de recettes du serveur : ce que le compagnon peut fabriquer, et avec quelle station. */
    private static int craftList(CommandContext<CommandSourceStack> ctx, String filter) {
        ServerLevel level = ctx.getSource().getLevel();
        com.google.gson.JsonObject out = com.donopot.voiddynasty.work.CraftService.recipes(level, filter, 40);
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + out), false);
        return 1;
    }

    /** Liste les recettes de fonte du serveur. */
    private static int smeltList(CommandContext<CommandSourceStack> ctx, String filter) {
        var out = com.donopot.voiddynasty.work.SmeltService.recipes(ctx.getSource().getLevel(), filter, 24);
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + out), false);
        return 1;
    }

    /** Carburants presents dans un coffre, avec leur pouvoir calorifique reel. */
    private static int smeltFuels(CommandContext<CommandSourceStack> ctx) {
        var pos = new net.minecraft.core.BlockPos(
                IntegerArgumentType.getInteger(ctx, "x"),
                IntegerArgumentType.getInteger(ctx, "y"),
                IntegerArgumentType.getInteger(ctx, "z"));
        var out = com.donopot.voiddynasty.work.SmeltService.fuels(ctx.getSource().getLevel(), pos);
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + out), false);
        return out.get("ok").getAsBoolean() ? 1 : 0;
    }

    /** Fonte depuis un coffre : entree et carburant consommes, resultat remis dans le meme coffre. */
    private static int smeltMake(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor dans cette dimension."));
            return 0;
        }
        var pos = new net.minecraft.core.BlockPos(
                IntegerArgumentType.getInteger(ctx, "x"),
                IntegerArgumentType.getInteger(ctx, "y"),
                IntegerArgumentType.getInteger(ctx, "z"));
        var out = com.donopot.voiddynasty.work.SmeltService.smelt(level, bot, pos,
                StringArgumentType.getString(ctx, "item"), IntegerArgumentType.getInteger(ctx, "nombre"));
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + out), false);
        return out.get("ok").getAsBoolean() ? 1 : 0;
    }

    /** Plan d'approvisionnement : quoi fabriquer, dans quel ordre, et ce qu'il faut recolter. */
    private static int craftPlan(CommandContext<CommandSourceStack> ctx, int radius) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor dans cette dimension."));
            return 0;
        }
        var pos = new net.minecraft.core.BlockPos(
                IntegerArgumentType.getInteger(ctx, "x"),
                IntegerArgumentType.getInteger(ctx, "y"),
                IntegerArgumentType.getInteger(ctx, "z"));
        com.google.gson.JsonObject out;
        try {
            out = com.donopot.voiddynasty.work.CraftService.materials(level, bot, pos,
                    StringArgumentType.getString(ctx, "item"), IntegerArgumentType.getInteger(ctx, "nombre"), radius);
        } catch (Throwable failure) {
            out = new com.google.gson.JsonObject();
            out.addProperty("ok", false);
            out.addProperty("error", "PLANNER_FAILURE");
            out.addProperty("detail", failure.getClass().getName() + ": " + String.valueOf(failure.getMessage()));
            var frames = new com.google.gson.JsonArray();
            StackTraceElement[] trace = failure.getStackTrace();
            for (int i = 0; i < Math.min(12, trace.length); i++) frames.add(trace[i].toString());
            out.add("trace", frames);
        }
        final com.google.gson.JsonObject payload = out;
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + payload), false);
        return payload.get("ok").getAsBoolean() ? 1 : 0;
    }

    /** Fabrique depuis le contenu d'un coffre, a condition d'etre a portee d'une table si besoin. */
    private static int craftMake(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor dans cette dimension."));
            return 0;
        }
        var pos = new net.minecraft.core.BlockPos(
                IntegerArgumentType.getInteger(ctx, "x"),
                IntegerArgumentType.getInteger(ctx, "y"),
                IntegerArgumentType.getInteger(ctx, "z"));
        com.google.gson.JsonObject out = com.donopot.voiddynasty.work.CraftService.craft(level, bot, pos,
                StringArgumentType.getString(ctx, "item"), IntegerArgumentType.getInteger(ctx, "nombre"));
        ctx.getSource().sendSuccess(() -> Component.literal("VDCRAFT " + out), false);
        return out.get("ok").getAsBoolean() ? 1 : 0;
    }

    /** Ordre de deplacement : passe par BrainOrders pour reutiliser la validation et les garde-fous. */
    private static int move(CommandContext<CommandSourceStack> ctx, String order, String player, double distance) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor dans cette dimension."));
            return 0;
        }
        com.google.gson.JsonObject args = new com.google.gson.JsonObject();
        String name = player;
        if (name.isEmpty()) {
            var playerEntity = ctx.getSource().getPlayer();
            if (playerEntity == null) {
                ctx.getSource().sendFailure(Component.literal("Precise un joueur."));
                return 0;
            }
            name = playerEntity.getScoreboardName();
        }
        args.addProperty("player", name);
        if (order.equals("follow_player")) args.addProperty("distance", distance);
        return dispatchMove(ctx, level, bot, order, args);
    }

    private static int moveTo(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor dans cette dimension."));
            return 0;
        }
        com.google.gson.JsonObject args = new com.google.gson.JsonObject();
        args.addProperty("x", IntegerArgumentType.getInteger(ctx, "x"));
        args.addProperty("y", IntegerArgumentType.getInteger(ctx, "y"));
        args.addProperty("z", IntegerArgumentType.getInteger(ctx, "z"));
        return dispatchMove(ctx, level, bot, "goto", args);
    }

    private static int dispatchMove(CommandContext<CommandSourceStack> ctx, ServerLevel level, LivingEntity bot,
                                    String order, com.google.gson.JsonObject args) {
        // Un compagnon sauvegarde/recharge est fige (NoAI) : un ordre explicite le reveille.
        if (bot instanceof com.donopot.voiddynasty.entity.Servitor1Entity worker) worker.wake();
        String report = BrainOrders.apply(new BrainTools.Order(order, args), level, bot, BrainConfig.get());
        com.google.gson.JsonObject out = new com.google.gson.JsonObject();
        boolean ok = !report.startsWith("REFUSE");
        out.addProperty("ok", ok);
        out.addProperty("order", order);
        out.addProperty("report", report);
        var pos = new com.google.gson.JsonArray();
        pos.add(bot.getX()); pos.add(bot.getY()); pos.add(bot.getZ());
        out.add("bot_pos", pos);
        out.addProperty("task", TaskRunner.describe(bot));
        ctx.getSource().sendSuccess(() -> Component.literal("VDMOVE " + out), false);
        return ok ? 1 : 0;
    }

    /** Etat du deplacement en cours, sans rien modifier. */
    private static int moveStatus(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity bot = BrainSpawn.find(level);
        com.google.gson.JsonObject out = new com.google.gson.JsonObject();
        if (bot == null) {
            out.addProperty("ok", true);
            out.addProperty("present", false);
            ctx.getSource().sendSuccess(() -> Component.literal("VDMOVE " + out), false);
            return 1;
        }
        out.addProperty("ok", true);
        out.addProperty("present", true);
        out.addProperty("task", TaskRunner.describe(bot));
        out.addProperty("busy", TaskRunner.current(bot) != null);
        var pos = new com.google.gson.JsonArray();
        pos.add(bot.getX()); pos.add(bot.getY()); pos.add(bot.getZ());
        out.add("bot_pos", pos);
        ctx.getSource().sendSuccess(() -> Component.literal("VDMOVE " + out), false);
        return 1;
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, int variant) {
        ServerLevel level = ctx.getSource().getLevel();
        LivingEntity existing = BrainSpawn.find(level);
        if (existing != null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Un servitor pilote existe deja (" + existing.blockPosition().toShortString()
                            + "). /servitor despawn d'abord."));
            return 0;
        }
        var player = ctx.getSource().getPlayer();
        var at = player != null
                ? player.blockPosition().offset(2, 0, 0)
                : net.minecraft.core.BlockPos.containing(ctx.getSource().getPosition());
        LivingEntity bot = BrainSpawn.spawn(level, at, BrainSpawn.Variant.of(variant));
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Creation impossible."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Servitor pose en " + bot.blockPosition().toShortString()
                        + " (variante " + variant + "). Il attend les ordres : /servitor brain on,"
                        + " puis /servitor brain ask <consigne>."), true);
        return 1;
    }

    private static int despawn(CommandContext<CommandSourceStack> ctx) {
        ServitorBrain.invalidatePending();
        boolean any = BrainSpawn.despawn(ctx.getSource().getLevel());
        ctx.getSource().sendSuccess(() -> Component.literal(any
                ? "Servitor retire (sac vide au sol)."
                : "Aucun retrait: servitor absent ou lie a un chantier conserve. Reconciler puis /work commit."), true);
        return any ? 1 : 0;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        LivingEntity bot = BrainSpawn.find(ctx.getSource().getLevel());
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Aucun servitor en jeu."));
            return 0;
        }
        ServitorBrain.invalidatePending();
        ServerLevel level=ctx.getSource().getLevel();
        String out;
        if(com.donopot.voiddynasty.work.WorkService.owns(level,bot)) {
            com.donopot.voiddynasty.work.WorkService.stop(level);
            out="chantier pause; ressources et journal conserves";
        } else out=TaskRunner.cancel(bot,level,"commande operateur");
        ctx.getSource().sendSuccess(() -> Component.literal(out), true);
        return 1;
    }

    // ---------------------------------------------------------------- cerveau

    private static int on(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        if (cfg.apiKey().isEmpty() && !isLocal(cfg.baseUrl)) {
            ctx.getSource().sendFailure(Component.literal(
                    "Refus : endpoint distant sans cle. Definir la variable d'environnement "
                            + cfg.apiKeyEnv + " puis relancer le serveur."));
            return 0;
        }
        ServitorBrain.enable(cfg);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Cerveau ACTIF — " + modele(cfg) + " sur " + cfg.chatUrl()
                        + ", une decision toutes les " + (cfg.intervalTicksSafe() / 20) + " s, delai "
                        + cfg.timeoutSeconds + " s."), true);
        if (cfg.timeoutSeconds < 45) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Attention : " + cfg.timeoutSeconds + " s est court pour un modele local qui reflechit."
                            + " /servitor brain timeout 90 si tu vois des « timeout »."), false);
        }
        return 1;
    }

    private static int off(CommandContext<CommandSourceStack> ctx) {
        ServitorBrain.disable(BrainConfig.get(), "commande operateur");
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Cerveau arrete. Les taches deja lancees continuent, /servitor stop pour les couper."), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(ServitorBrain.status()), false);
        LivingEntity bot = BrainSpawn.find(ctx.getSource().getLevel());
        ctx.getSource().sendSuccess(() -> Component.literal("servitor : "
                + (bot == null ? "absent" : "en " + bot.blockPosition().toShortString()
                + " | pv " + (int) bot.getHealth() + "/" + (int) bot.getMaxHealth()
                + " | tache " + TaskRunner.describe(bot)
                + " | sac " + BrainInv.summary(bot))), false);
        return 1;
    }

    /** Derniere reponse brute du modele : indispensable quand il bavarde au lieu d'agir. */
    private static int last(CommandContext<CommandSourceStack> ctx) {
        String reply = ServitorBrain.lastReply();
        ctx.getSource().sendSuccess(() -> Component.literal(
                reply.isBlank() ? "Aucune reponse enregistree." : "Derniere reponse du modele : " + reply), false);
        return reply.isBlank() ? 0 : 1;
    }

    private static int log(CommandContext<CommandSourceStack> ctx) {
        List<String> lines = ServitorBrain.auditLines();
        if (lines.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Journal vide."), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Journal du cerveau (" + lines.size() + ") :"), false);
        for (String line : lines) ctx.getSource().sendSuccess(() -> Component.literal("  " + line), false);
        return 1;
    }

    private static int url(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        String value = StringArgumentType.getString(ctx, "url").trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            ctx.getSource().sendFailure(Component.literal("URL attendue : http://ip:port/v1"));
            return 0;
        }
        cfg.baseUrl = value;
        cfg.save();
        BrainLlm.forgetModel();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Endpoint : " + cfg.chatUrl() + " — /servitor brain test pour verifier."), true);
        return 1;
    }

    private static int model(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        cfg.model = StringArgumentType.getString(ctx, "id").trim();
        cfg.save();
        BrainLlm.forgetModel();
        ctx.getSource().sendSuccess(() -> Component.literal("Modele : " + cfg.model), true);
        return 1;
    }

    /** state = etat complet a chaque cycle ; order = le modele ne parle que sur ordre. */
    private static int mode(CommandContext<CommandSourceStack> ctx, boolean orderOnly) {
        BrainConfig cfg = BrainConfig.get();
        cfg.orderOnly = orderOnly;
        cfg.save();
        ctx.getSource().sendSuccess(() -> Component.literal(orderOnly
                ? "Mode ordre : le modele n'est appele que sur consigne, avec un prompt minimal."
                : "Mode etat : le modele recoit l'etat du monde a chaque cycle."), true);
        return 1;
    }

    /** all = tout le chat est une consigne ; mention = il faut nommer le servitor. */
    private static int chat(CommandContext<CommandSourceStack> ctx, boolean all) {
        BrainConfig cfg = BrainConfig.get();
        cfg.chatAll = all;
        cfg.save();
        ctx.getSource().sendSuccess(() -> Component.literal(all
                ? "Chat : tout message devient une consigne pour le servitor."
                : "Chat : il faut ecrire « servitor ... » ou « !s ... »."), true);
        return 1;
    }

    /** on = le modele reflechit comme il veut ; off = /no_think ajoute au prompt (Qwen). */
    private static int think(CommandContext<CommandSourceStack> ctx, boolean noThink) {
        BrainConfig cfg = BrainConfig.get();
        cfg.noThink = noThink;
        cfg.save();
        ctx.getSource().sendSuccess(() -> Component.literal(noThink
                ? "Raisonnement coupe : /no_think ajoute au prompt. Reponses plus courtes et plus rapides."
                : "Raisonnement laisse au modele (plus lent, parfois plus juste)."), true);
        return 1;
    }

    private static int timeout(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        int seconds = IntegerArgumentType.getInteger(ctx, "secondes");
        cfg.timeoutSeconds = seconds;
        cfg.save();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Delai maximal par appel LLM : " + seconds + " s."), true);
        return 1;
    }

    private static int interval(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        int seconds = IntegerArgumentType.getInteger(ctx, "secondes");
        cfg.intervalTicks = seconds * 20;
        cfg.save();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Une decision toutes les " + seconds + " s."), true);
        return 1;
    }

    private static int models(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        CommandSourceStack source = ctx.getSource();
        var server = source.getServer();
        source.sendSuccess(() -> Component.literal("Interrogation de " + cfg.baseUrl + "/models ..."), false);
        NET.submit(() -> {
            String message;
            try {
                List<String> ids = BrainLlm.models(cfg);
                message = ids.isEmpty() ? "Aucun modele expose (le serveur repond mais la liste est vide)."
                        : ids.size() + " modele(s) : " + String.join(", ", ids);
            } catch (Exception e) {
                message = "Echec : " + e.getClass().getSimpleName() + " — " + e.getMessage();
            }
            String finalMessage = message;
            LOGGER.info("[Servitor] modeles : {}", finalMessage);
            server.execute(() -> source.sendSuccess(() -> Component.literal(finalMessage), false));
        });
        return 1;
    }

    private static int test(CommandContext<CommandSourceStack> ctx) {
        BrainConfig cfg = BrainConfig.get();
        CommandSourceStack source = ctx.getSource();
        var server = source.getServer();
        source.sendSuccess(() -> Component.literal("Test de " + cfg.chatUrl() + " ..."), false);
        NET.submit(() -> {
            String message;
            long started = System.currentTimeMillis();
            try {
                List<String> ids = BrainLlm.models(cfg);
                long modelsMs = System.currentTimeMillis() - started;
                BrainLlm.Result r = BrainLlm.ask(cfg, "Reponds uniquement par un appel d'outil say avec un mot.",
                        "Test de connexion. Outil say obligatoire.");
                long total = System.currentTimeMillis() - started;
                if (r.isError()) {
                    message = "Serveur joignable (" + modelsMs + " ms, " + ids.size()
                            + " modeles) mais l'appel echoue : " + r.error();
                } else if (r.order() != null) {
                    message = "OK — " + ids.size() + " modeles, reponse en " + total
                            + " ms, outil compris : " + r.order().name() + " " + r.order().args();
                } else {
                    message = "OK — " + ids.size() + " modeles, reponse en " + total
                            + " ms, mais aucun outil reconnu. Texte recu : " + BrainLlm.shorten(r.text());
                }
            } catch (Exception e) {
                message = "Echec (" + (System.currentTimeMillis() - started) + " ms) : "
                        + e.getClass().getSimpleName() + " — " + e.getMessage();
            }
            String finalMessage = message;
            LOGGER.info("[Servitor] test connexion : {}", finalMessage);
            server.execute(() -> source.sendSuccess(() -> Component.literal(finalMessage), false));
        });
        return 1;
    }

    private static int ask(CommandContext<CommandSourceStack> ctx) {
        String consigne = StringArgumentType.getString(ctx, "consigne");
        ServitorBrain.instruct(consigne);
        ctx.getSource().sendSuccess(() -> Component.literal("Consigne transmise au prochain cycle : " + consigne), true);
        return 1;
    }

    // ------------------------------------------------------------------ sacs

    private static int bags(CommandContext<CommandSourceStack> ctx) {
        var server = ctx.getSource().getServer();
        String out = BrainBags.reconcile(server) + " | " + BrainBags.describe(server);
        for (String line : out.split("\n")) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int bagAudit(CommandContext<CommandSourceStack> ctx) {
        var server = ctx.getSource().getServer();
        LivingEntity bot = BrainSpawn.find(ctx.getSource().getLevel());
        String out = BrainBags.audit(server, bot == null ? null : bot.getUUID());
        for (String line : out.split("\n")) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int bagAction(CommandContext<CommandSourceStack> ctx, String action) {
        var server = ctx.getSource().getServer();
        ServerLevel level = ctx.getSource().getLevel();
        String id = StringArgumentType.getString(ctx, "id");
        String out = switch (action) {
            case "restore" -> BrainBags.restoreToBot(server, id, BrainSpawn.find(level));
            case "give" -> BrainBags.giveToPlayer(server, id, ctx.getSource().getPlayer());
            default -> BrainBags.dropAt(server, id, level, ctx.getSource().getPosition());
        };
        boolean ok = !out.startsWith("refus") && !out.startsWith("sac introuvable")
                && !out.startsWith("aucun") && !out.startsWith("joueur introuvable")
                && !out.startsWith("position");
        if (ok) ctx.getSource().sendSuccess(() -> Component.literal(out), true);
        else ctx.getSource().sendFailure(Component.literal(out));
        return ok ? 1 : 0;
    }

    /** Nom du modele pour les messages, avec la raison d'un eventuel remplacement. */
    private static String modele(BrainConfig cfg) {
        return BrainLlm.modelLabel(cfg);
    }

    private static boolean isLocal(String url) {
        return url.contains("127.0.0.1") || url.contains("localhost") || url.startsWith("http://192.168.")
                || url.startsWith("http://10.") || url.startsWith("http://172.");
    }
}
