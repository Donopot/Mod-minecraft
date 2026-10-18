package com.donopot.voiddynasty.ai;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cerveau du Servitor : un cycle = une photo d'état, un appel au modèle, un ordre validé,
 * une tâche qui s'exécute.
 *
 * <p>Règles de sûreté, dans cet ordre :</p>
 * <ol>
 *   <li>le fil du serveur n'attend jamais le réseau : l'appel HTTP part sur un fil dédié ;</li>
 *   <li>un seul appel en vol à la fois, cadencé par {@code intervalTicks} ;</li>
 *   <li>tout ordre est revérifié côté serveur avant exécution ({@link BrainTools#validate}) ;</li>
 *   <li>une tâche longue n'est pas interrompue par le cycle suivant : le modèle voit qu'elle
 *       tourne et décide de la laisser finir ;</li>
 *   <li>après N échecs consécutifs, le cerveau se coupe de lui-même et le dit ;</li>
 *   <li>chaque décision et chaque refus restent dans un journal consultable.</li>
 * </ol>
 */
public final class ServitorBrain {

    private static final int AUDIT_MAX = 80;
    private static final Deque<String> AUDIT = new ArrayDeque<>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "voiddynasty-servitor-brain");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean IN_FLIGHT = new AtomicBoolean(false);

    private static long lastCycleTick = Long.MIN_VALUE;
    /** Un ordre vient d'arriver : on traite tout de suite, sans attendre l'intervalle. */
    private static boolean forceCycle = false;
    private static String feedback = "";

    /** Derniere reponse brute du modele, pour diagnostiquer un modele qui bavarde. */
    private static volatile String lastReply = "";

    /** Signature du dernier ordre applique, pour ne pas relancer une tache identique en cours. */
    private static String lastOrder = "";

    /** Nombre d'ordres de lecture (scan, where…) d'affilee, sans action. */
    private static int idleOrders = 0;
    private static String instruction = "";
    private static int instructionAge = 0;
    /** La tache en cours a ete annulee pour obeir a la consigne : le modele doit le savoir. */
    private static boolean instructionForced = false;
    /** Nombre de consignes recues depuis l'activation : prouve que le chat arrive au cerveau. */
    private static int instructionsReceived = 0;
    /** Nom du dernier operateur qui a parle : le modele en a besoin pour come_to_player. */
    private static String operator = "";
    private static int consecutiveErrors = 0;
    private static String lastDecision = "";
    private static long cycles = 0;
    private static long lastLatencyMs = 0;

    private ServitorBrain() {}

    public static void init() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(TaskRunner::tick);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(ServitorBrain::tick);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(s -> IN_FLIGHT.set(false));
        log("cerveau initialise (desactive par defaut)");
    }

    // ------------------------------------------------------------------ boucle

    private static void tick(MinecraftServer server) {
        BrainConfig cfg = BrainConfig.get();
        if (!cfg.enabled || IN_FLIGHT.get()) return;

        ServerLevel level = server.overworld();
        LivingEntity bot = BrainSpawn.find(level);
        if (bot == null) {
            // le servitor persistant peut vivre dans une autre dimension
            for (ServerLevel other : server.getAllLevels()) {
                LivingEntity found = BrainSpawn.find(other);
                if (found != null) {
                    level = other;
                    bot = found;
                    break;
                }
            }
        }

        // Mode « a la demande » : sans consigne en attente, on ne reveille pas le modele du tout.
        // C'est ce qui evite les cycles de suivi/wait a repetition et les tokens inutiles.
        if (cfg.orderOnly && instruction.isEmpty()) return;

        long now = level.getGameTime();
        if (lastCycleTick == Long.MIN_VALUE || now < lastCycleTick) lastCycleTick = now;
        // Une consigne doit partir tout de suite : sinon l'operateur attend l'intervalle complet
        // et croit que le servitor est deconnecte. En mode ordre, l'intervalle ne sert a rien :
        // le modele n'est appele que la.
        boolean forced = forceCycle;
        forceCycle = false;
        if (!forced && !cfg.orderOnly && now - lastCycleTick < cfg.intervalTicksSafe()) return;
        lastCycleTick = now;

        String outcome = bot == null ? "" : TaskRunner.consumeOutcome(bot);
        if (!outcome.isEmpty()) feedback = outcome;

        String user;
        if (cfg.orderOnly) {
            // Prompt minimal : l'ordre, une ligne de contexte, rien de plus. Les outils sont
            // toujours envoyes dans la requete (le protocole OpenAI l'exige) mais le prompt
            // reste court, donc la reponse arrive vite.
            user = "ORDRE DE " + (operator.isEmpty() ? "L'OPERATEUR" : operator.toUpperCase()) + "\n" + instruction
                    + (instructionForced
                    ? "\nJ'ai annule la tache en cours pour executer cet ordre : realise-le maintenant."
                    : "")
                    + "\n\nCONTEXTE\n" + contextLine(level, bot)
                    + (feedback.isEmpty() ? "" : "\n\nDERNIER RESULTAT\n" + feedback);
        } else {
            String snapshot = BrainState.snapshot(level, bot).toString();
            user = "ETAT ACTUEL (JSON)\n" + snapshot
                    + (instruction.isEmpty() ? "" : "\n\nCONSIGNE DE L'OPERATEUR (prioritaire)\n" + instruction
                            + (instructionForced
                            ? "\nJ'ai annule la tache en cours pour executer cette consigne : realise-la maintenant."
                            : ""))
                    + (feedback.isEmpty() ? "" : "\n\nRESULTAT PRECEDENT\n" + feedback);
        }
        // La consigne reste en attente tant qu'aucun ordre ne l'execute : un petit modele peut
        // l'ignorer un tour ou deux, et jusqu'ici elle etait perdue au premier cycle.
        if (!instruction.isEmpty() && ++instructionAge > 6) {
            instruction = "";
            instructionAge = 0;
            instructionForced = false;
        }

        IN_FLIGHT.set(true);
        final ServerLevel target = level;
        final long started = System.currentTimeMillis();
        WORKER.submit(() -> {
            BrainLlm.Result result = BrainLlm.ask(cfg, systemPrompt(), user);
            long elapsed = System.currentTimeMillis() - started;
            server.execute(() -> {
                try {
                    lastLatencyMs = elapsed;
                    handleResult(result, target, cfg);
                } finally {
                    IN_FLIGHT.set(false);
                }
            });
        });
    }

    private static void handleResult(BrainLlm.Result result, ServerLevel level, BrainConfig cfg) {
        cycles++;
        lastReply = result.isError() ? "ERREUR " + result.error() : trim(result.text());
        if (result.isError()) {
            consecutiveErrors++;
            feedback = "erreur technique : " + result.error();
            audit("ERREUR " + result.error());
            if (consecutiveErrors >= Math.max(1, cfg.maxErrorsBeforeDisable)) {
                disable(cfg, "coupe apres " + consecutiveErrors + " erreurs : " + result.error());
            }
            return;
        }
        consecutiveErrors = 0;

        if (result.order() == null) {
            feedback = "aucun ordre donne" + (result.text().isBlank() ? "" : " — " + trim(result.text()));
            audit("aucune action" + (result.text().isBlank() ? "" : " (" + trim(result.text()) + ")"));
            return;
        }

        LivingEntity bot = BrainSpawn.find(level);
        String refusal = BrainTools.validate(result.order(), level, bot, cfg);
        if (refusal != null) {
            feedback = "ordre REFUSE : " + refusal;
            lastDecision = result.order().describe();
            audit("REFUS " + result.order().describe() + " — " + refusal);
            return;
        }

        // Un modele faible (ou tete en l'air) redemande la meme chose a chaque cycle : relancer
        // la tache a chaque tour l'empecherait de finir. On ignore l'ordre identique tant que la
        // tache en cours tourne encore.
        String signature = result.order().describe();
        boolean running = bot != null && TaskRunner.current(bot) != null;
        if (running && signature.equals(lastOrder)) {
            if (!instruction.isEmpty()) {
                // L'operateur a demande autre chose et le modele ressort la meme tache : on libere
                // le servitor pour que la consigne puisse enfin s'executer.
                TaskRunner.cancel(bot, level, "consigne prioritaire de l'operateur");
                instructionForced = true;
                feedback = "tache '" + signature + "' annulee pour executer la consigne de"
                        + " l'operateur : choisis maintenant l'outil qui la realise"
                        + " (dig_box, mine_block, mine_area, goto).";
                audit("tache annulee pour la consigne");
                return;
            }
            feedback = "tache identique deja en cours : " + TaskRunner.describe(bot)
                    + " — inutile de la relancer ou de repondre wait : attends une consigne"
                    + " de l'operateur, ou lance une autre action";
            audit("identique ignore (" + signature + ")");
            return;
        }
        String outcome = BrainOrders.apply(result.order(), level, bot, cfg);
        lastOrder = bot != null && TaskRunner.current(bot) != null ? signature : "";
        lastDecision = signature;
        if (!instruction.isEmpty()) {
            instruction = "";
            instructionAge = 0;
            instructionForced = false;
        }
        feedback = "ordre applique : " + outcome;
        audit(signature + " → " + outcome);

        // Un petit modele peut boucler sur scan/where sans jamais agir : on le pousse.
        boolean readOnly = switch (result.order().name()) {
            case "scan", "where", "say", "wait" -> true;
            default -> false;
        };
        if (readOnly) {
            idleOrders++;
            if (idleOrders >= 2) {
                feedback = feedback + " | tu enchaines " + idleOrders
                        + " ordres de lecture sans agir : lance maintenant une action"
                        + " (dig_box pour creuser, mine_area pour une zone, mine_block pour un minerai, goto)";
                audit("rappel d'action apres " + idleOrders + " lectures");
            }
        } else {
            idleOrders = 0;
        }
    }

    /** Appelé par {@link TaskRunner} quand une tâche se termine : le modèle l'apprend. */
    static void onTaskFinished(String outcome) {
        feedback = outcome;
        lastOrder = "";
    }

    // ---------------------------------------------------------------- pilotage

    public static void enable(BrainConfig cfg) {
        cfg.enabled = true;
        consecutiveErrors = 0;
        feedback = "";
        lastCycleTick = Long.MIN_VALUE;
        cfg.save();
        audit("active");
    }

    public static void disable(BrainConfig cfg, String reason) {
        cfg.enabled = false;
        cfg.save();
        audit("desactive : " + reason);
        log("desactive (" + reason + ")");
    }

    /** Contexte compact pour le mode « a la demande » : une ligne, pas un JSON complet. */
    private static String contextLine(ServerLevel level, LivingEntity bot) {
        if (bot == null) return "aucun servitor en jeu : /servitor spawn d'abord";
        String task = TaskRunner.current(bot) == null ? "aucune" : TaskRunner.describe(bot);
        return "position " + bot.blockPosition().toShortString()
                + " | vie " + (int) bot.getHealth()
                + " | tache " + task
                + " | operateur " + (operator.isEmpty() ? "inconnu" : operator);
    }

    /** Consigne ponctuelle de l'opérateur, consommée au cycle suivant. */
    public static void instruct(String consigne, String from) {
        if (from != null && !from.isBlank()) operator = from;
        instruct(consigne);
    }

    public static void instruct(String consigne) {
        instructionAge = 0;
        instructionsReceived++;
        forceCycle = true;
        instruction = consigne == null ? "" : consigne.trim();
        lastCycleTick = Long.MIN_VALUE;
        audit("consigne operateur : " + trim(instruction));
    }

    public static boolean enabled() {
        return BrainConfig.get().enabled;
    }

    public static String lastReply() {
        return lastReply;
    }

    public static String status() {
        BrainConfig cfg = BrainConfig.get();
        return "cerveau " + (cfg.enabled ? "ACTIF" : "inactif")
                + " | modele " + BrainLlm.modelLabel(cfg)
                + " | " + cfg.chatUrl()
                + " | cle " + (cfg.apiKey().isEmpty() ? "absente (env " + cfg.apiKeyEnv + ")" : "presente")
                + " | cycle " + (cfg.intervalTicksSafe() / 20) + " s | delai " + cfg.timeoutSeconds + " s"
                + " | raisonnement " + (cfg.noThink ? "coupe" : "libre")
                + " | consignes " + instructionsReceived
                + " | mode " + (cfg.orderOnly ? "ordre" : "etat")
                + " | cycles " + cycles
                + " | derniere latence " + lastLatencyMs + " ms"
                + " | erreurs " + consecutiveErrors
                + " | decision : " + (lastDecision.isBlank() ? "aucune" : trim(lastDecision))
                + " | retour : " + (feedback.isBlank() ? "aucun" : trim(feedback));
    }

    public static java.util.List<String> auditLines() {
        synchronized (AUDIT) {
            return java.util.List.copyOf(AUDIT);
        }
    }

    private static void audit(String line) {
        String entry = "[" + cycles + "] " + line;
        synchronized (AUDIT) {
            AUDIT.addFirst(entry);
            while (AUDIT.size() > AUDIT_MAX) AUDIT.removeLast();
        }
    }

    private static void log(String message) {
        System.out.println("[servitor] " + message);
    }

    private static String trim(String s) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= 200 ? t : t.substring(0, 200) + "…";
    }

    // ------------------------------------------------------------------ prompt

    private static String systemPrompt() {
        return """
                Tu es un SERVITOR dans Minecraft : une machine de travail au service de l'operateur.

                Tu ne decris pas, tu agis. A chaque tour tu choisis EXACTEMENT UN outil parmi ceux
                fournis, avec des arguments valides. Tu ne peux pas executer de commande.

                Comment travailler :
                - Une tache longue (miner, suivre, combattre) continue apres ta reponse. Regarde le
                  champ "tache" : si elle est encore en cours et progresse, laisse-la finir avec wait.
                - L'outil probe te dit ce qu'il y a autour (plafond, sortie, lave, minerais) : utilise-le
                  avant de creuser dans une direction au hasard.
                - find localise un bloc precis (fer, coffre) avec sa distance, box dessine une zone en
                  grille : demande-les avant de creuser au hasard ou de partir a l'aveugle.
                - remember enregistre un fait durable (filon, tunnel, base) et recall le relit : sers-t'en
                  au lieu de re-explorer ce que tu connais deja.
                - S'il est bloque par un mur en allant quelque part, il creuse un passage tout seul :
                  ne lui demande pas de casser bloc par bloc, donne juste la destination.
                - Apres un minage il remonte par un escalier qu'il creuse lui-meme : la tache ne
                  se termine qu'une fois revenu au niveau du sol.
                - Un suivi (follow_player) ne se termine jamais : ne le renvoies pas a chaque tour
                  et ne reponds pas wait en boucle. Attends une consigne, ou fais autre chose.
                - Si l'etat contient une CONSIGNE DE L'OPERATEUR, elle passe AVANT la tache en
                  cours : abandonne ou remplace ce que tu faisais pour l'executer.
                - Si la tache est "aucune" et que l'operateur attend quelque chose, lance l'action.
                - Un ordre refuse au tour precedent ne se repete pas : change d'approche.
                - scan sert au maximum UNE fois : ensuite tu lances l'action. Ne boucle jamais sur
                  scan ou where, ils ne changent rien au monde.
                - Pour creuser un trou rectangulaire sous toi (un trou de 10 sur 18, une cave, un
                  puits), utilise dig_box avec width, length et depth.
                - Pour creuser une zone autour d'un point, mine_area. Pour un minerai precis, mine_block.
                - Si rien ne justifie une action, utilise wait. Ne fais rien "pour occuper".
                - Tu parles francais, en phrases courtes, seulement via say.

                Reponds uniquement par un appel d'outil.
                """;
    }
}
