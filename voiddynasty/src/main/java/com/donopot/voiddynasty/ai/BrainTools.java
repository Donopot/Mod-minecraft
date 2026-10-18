package com.donopot.voiddynasty.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vocabulaire d'action du Servitor : ce que le modèle a le droit de demander, et sous
 * quelles conditions.
 *
 * <p>Le modèle ne produit jamais une commande : il produit un NOM d'outil et des arguments
 * typés. Tout est revérifié ici, côté serveur, avant la moindre action. Un refus est
 * journalisé et renvoyé au modèle au cycle suivant.</p>
 */
public final class BrainTools {

    private BrainTools() {}

    /** Noms des outils, dans l'ordre du schéma. */
    public static List<String> names() {
        List<String> out = new ArrayList<>();
        for (JsonElement el : schema()) {
            out.add(el.getAsJsonObject().getAsJsonObject("function").get("name").getAsString());
        }
        return out;
    }

    /** Schéma JSON des paramètres d'un outil, ou null s'il n'existe pas. */
    public static String schemaOf(String name) {
        for (JsonElement el : schema()) {
            JsonObject fn = el.getAsJsonObject().getAsJsonObject("function");
            if (name.equals(fn.get("name").getAsString())) return fn.get("parameters").toString();
        }
        return null;
    }

    /** Un ordre validé en forme, pas encore validé en contexte. */
    public record Order(String name, JsonObject args) {

        public int intArg(String key) {
            JsonElement e = args.get(key);
            return e == null || !e.isJsonPrimitive() ? 0 : e.getAsInt();
        }

        public double doubleArg(String key) {
            JsonElement e = args.get(key);
            return e == null || !e.isJsonPrimitive() ? 0.0 : e.getAsDouble();
        }

        public String strArg(String key) {
            JsonElement e = args.get(key);
            return e == null || !e.isJsonPrimitive() ? "" : e.getAsString();
        }

        public String describe() {
            return args == null || args.isEmpty() ? name : name + " " + args;
        }
    }

    /** Outils qui n'ont pas besoin d'un servitor en jeu. */
    private static final List<String> WITHOUT_BODY = List.of("say", "wait", "where", "stop");

    // ------------------------------------------------------------------ schéma

    public static JsonArray schema() {
        JsonArray tools = new JsonArray();
        tools.add(tool("say", "Parler dans le chat, en tant que Servitor (une phrase courte).",
                props(prop("message", "string", "Phrase à dire, 200 caractères maximum.")), List.of("message")));
        tools.add(tool("goto", "Marcher jusqu'à des coordonnées précises.",
                props(prop("x", "integer", "X"), prop("y", "integer", "Y"), prop("z", "integer", "Z")),
                List.of("x", "y", "z")));
        tools.add(tool("come_to_player", "Rejoindre un joueur et s'arrêter à côté de lui.",
                props(prop("player", "string", "Nom exact du joueur.")), List.of("player")));
        tools.add(tool("follow_player",
                "Suivre un joueur en continu, jusqu'à ce qu'on te dise d'arrêter (stop).",
                props(prop("player", "string", "Nom exact du joueur."),
                        prop("distance", "number", "Distance à conserver, en blocs (2 à 10).")),
                List.of("player")));
        tools.add(tool("stop", "Arrêter la tâche en cours.", props(), List.of()));
        tools.add(tool("mine_area",
                "Miner toute une zone cubique, bloc par bloc, en marchant de l'un à l'autre.",
                props(prop("x", "integer", "Centre X"), prop("y", "integer", "Centre Y"),
                        prop("z", "integer", "Centre Z"),
                        prop("radius", "integer", "Demi-taille de la zone, en blocs (1 à 8).")),
                List.of("x", "y", "z", "radius")));
        tools.add(tool("dig_box",
                "Creuser un trou rectangulaire sous toi (largeur x longueur x profondeur, en blocs).",
                props(prop("width", "integer", "Largeur en blocs (1 à 16)."),
                        prop("length", "integer", "Longueur en blocs (1 à 16)."),
                        prop("depth", "integer", "Profondeur vers le bas en blocs (1 à 16).")),
                List.of("width", "length", "depth")));
        tools.add(tool("mine_block",
                "Chercher un type de bloc autour de toi et en miner un nombre précis.",
                props(prop("block", "string", "Identifiant du bloc (ex: iron_ore, oak_log)."),
                        prop("count", "integer", "Nombre de blocs à miner (1 à 64)."),
                        prop("radius", "integer", "Rayon de recherche en blocs (4 à 32).")),
                List.of("block", "count")));
        tools.add(tool("attack_nearest", "Attaquer les monstres hostiles les plus proches.",
                props(prop("radius", "integer", "Rayon de détection (4 à 24)."),
                        prop("count", "integer", "Nombre de monstres à éliminer (1 à 10).")),
                List.of()));
        tools.add(tool("pickup", "Aller ramasser les objets au sol autour de toi.",
                props(prop("radius", "integer", "Rayon de recherche (4 à 32)."),
                        prop("count", "integer", "Nombre d'objets à ramasser (1 à 64).")),
                List.of()));
        tools.add(tool("scan", "Compter un type de bloc autour de toi, sans rien miner. Réponse immédiate.",
                props(prop("block", "string", "Identifiant du bloc (ex: iron_ore)."),
                        prop("radius", "integer", "Rayon de recherche (4 à 32).")),
                List.of("block")));
        tools.add(tool("probe", "Voir ce qu'il y a autour de toi : epaisseur du plafond, sortie la plus"
                        + " proche, lave, minerais. Réponse immédiate.", 
                props(prop("radius", "integer", "Rayon d'analyse (4 à 16).")), List.of()));
        tools.add(tool("find", "Localiser les blocs les plus proches d'un type (fer, charbon, coffre...)"
                        + " avec distance et direction. Réponse immédiate.",
                props(prop("block", "string", "Identifiant du bloc (ex: iron_ore)."),
                        prop("radius", "integer", "Rayon de recherche (4 à 24)."),
                        prop("limit", "integer", "Nombre de resultats (1 à 16).")),
                List.of("block")));
        tools.add(tool("box", "Voir une petite zone en grille, une couche par niveau (dessin + legende)."
                        + " Réponse immédiate.",
                props(prop("x", "integer", "Centre X."), prop("y", "integer", "Centre Y."),
                        prop("z", "integer", "Centre Z."),
                        prop("half", "integer", "Demi-largeur 1 à 4 (defaut 2)."),
                        prop("layers", "integer", "Demi-hauteur 1 à 6 (defaut 2).")),
                List.of("x", "y", "z")));
        tools.add(tool("remember", "Enregistrer un fait durable (filon, tunnel, base) avec ta position."
                        + " Il survit aux redemarrages. Réponse immédiate.",
                props(prop("note", "string", "Le fait à retenir, en une phrase.")), List.of("note")));
        tools.add(tool("recall", "Relire ta memoire, ou chercher un mot-cle dedans. Réponse immédiate.",
                props(prop("query", "string", "Mot-cle (facultatif).")), List.of()));
        tools.add(tool("where", "Donner la position d'un joueur et sa distance. Réponse immédiate.",
                props(prop("player", "string", "Nom exact du joueur.")), List.of("player")));
        tools.add(tool("wait", "Ne rien faire pendant N secondes.",
                props(prop("seconds", "integer", "Durée d'attente (1 à 60).")), List.of()));
        return tools;
    }

    private static JsonObject tool(String name, String description, JsonObject properties, List<String> required) {
        JsonObject t = new JsonObject();
        t.addProperty("type", "function");
        JsonObject f = new JsonObject();
        f.addProperty("name", name);
        f.addProperty("description", description);
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        params.add("properties", properties);
        JsonArray req = new JsonArray();
        required.forEach(req::add);
        params.add("required", req);
        params.addProperty("additionalProperties", false);
        f.add("parameters", params);
        t.add("function", f);
        return t;
    }

    private static JsonObject props(JsonObject... items) {
        JsonObject o = new JsonObject();
        for (JsonObject item : items) o.add(item.get("name").getAsString(), item.get("schema"));
        return o;
    }

    private static JsonObject prop(String name, String type, String description) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", type);
        schema.addProperty("description", description);
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("name", name);
        wrapper.add("schema", schema);
        return wrapper;
    }

    // -------------------------------------------------------------- validation

    /** @return {@code null} si l'ordre est acceptable, sinon la raison du refus. */
    public static String validate(Order order, ServerLevel level, LivingEntity bot, BrainConfig cfg) {
        if (order == null || order.name() == null) return "ordre vide";
        String name = order.name().toLowerCase(Locale.ROOT);
        if (!known(name)) return "outil inconnu : " + name;

        boolean needsBody = !WITHOUT_BODY.contains(name);
        BlockPos botPos = bot == null ? null : bot.blockPosition();
        if (needsBody && botPos == null) {
            return "aucun servitor en jeu : /servitor spawn d'abord";
        }

        switch (name) {
            case "say" -> {
                String msg = order.strArg("message").trim();
                if (msg.isEmpty()) return "message vide";
                if (msg.length() > 200) return "message trop long (200 max)";
                return null;
            }
            case "wait" -> {
                return null;
            }
            case "stop" -> {
                return null;
            }
            case "goto" -> {
                BlockPos dst = new BlockPos(order.intArg("x"), order.intArg("y"), order.intArg("z"));
                double d = Math.sqrt(dst.distSqr(botPos));
                if (d > cfg.maxDistance) return "destination trop loin (" + (int) d + " blocs, max " + cfg.maxDistance + ")";
                return null;
            }
            case "come_to_player", "follow_player" -> {
                String target = order.strArg("player");
                return playerByName(level, target) == null ? "joueur inconnu : " + target : null;
            }
            case "where" -> {
                String target = order.strArg("player");
                return playerByName(level, target) == null ? "joueur inconnu : " + target : null;
            }
            case "dig_box" -> {
                int w = clamp(order.intArg("width"), 1, 16);
                int l = clamp(order.intArg("length"), 1, 16);
                int d = clamp(order.intArg("depth"), 1, 16);
                int volume = w * l * d;
                if (volume > cfg.maxVolume) {
                    return "trou trop volumineux (" + volume + " blocs, max " + cfg.maxVolume + ")";
                }
                if (bot == null) return "aucun servitor en jeu : /servitor spawn d'abord";
                return null;
            }
            case "mine_area" -> {
                int radius = clamp(order.intArg("radius"), 1, cfg.maxRadius);
                int volume = (2 * radius + 1) * (2 * radius + 1) * (2 * radius + 1);
                if (volume > cfg.maxVolume) return "zone trop volumineuse (" + volume + " blocs, max " + cfg.maxVolume + ")";
                BlockPos center = new BlockPos(order.intArg("x"), order.intArg("y"), order.intArg("z"));
                double d = Math.sqrt(center.distSqr(botPos));
                if (d > cfg.maxDistance) return "zone trop loin du servitor (" + (int) d + " blocs, max " + cfg.maxDistance + ")";
                return null;
            }
            case "mine_block", "scan", "find" -> {
                String block = order.strArg("block").toLowerCase(Locale.ROOT);
                if (block.isBlank()) return "bloc non precise";
                Identifier id = Identifier.tryParse(block.contains(":") ? block : "minecraft:" + block);
                if (id == null || BuiltInRegistries.BLOCK.getValue(id) == null) return "bloc inconnu : " + block;
                return null;
            }
            case "attack_nearest", "pickup", "probe", "recall", "box" -> {
                return null;
            }
            case "remember" -> {
                if (order.strArg("note").isBlank()) return "note vide";
                return null;
            }
            default -> {
                return "outil inconnu : " + name;
            }
        }
    }

    private static boolean known(String name) {
        return switch (name) {
            case "say", "wait", "stop", "goto", "come_to_player", "follow_player",
                 "mine_area", "dig_box", "mine_block", "attack_nearest", "pickup", "scan",
                 "probe", "find", "box", "remember", "recall", "where" -> true;
            default -> false;
        };
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static ServerPlayer playerByName(ServerLevel level, String name) {
        if (name == null || name.isBlank()) return null;
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (p.getGameProfile().name().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /** "iron_ore" -> "minecraft:iron_ore". */
    public static String normalizeBlock(String block) {
        if (block == null || block.isBlank()) return "";
        return block.contains(":") ? block.toLowerCase(Locale.ROOT) : "minecraft:" + block.toLowerCase(Locale.ROOT);
    }
}
