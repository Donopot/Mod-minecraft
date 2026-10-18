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
    private static final List<String> WITHOUT_BODY = List.of("say", "where", "stop", "work_capabilities", "work_status", "work_control");

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
        tools.add(tool("work_capabilities", "Lire les ressources, plans et limites du moteur de chantier.", props(), List.of()));
        tools.add(tool("work_submit", "Soumettre une mission GATHER ou BUILD journalisee. spec est un objet JSON encode en texte. "
                + "Champs GATHER: schema=1,type,request_id stable,item,amount,min:[x,y,z],max:[x,y,z],destination:[x,y,z],origin:[x,y,z],access optionnel surface/excavate. "
                + "BUILD: schema=1,type=BUILD,request_id,blueprint,material,anchor:[x,y,z],source:[x,y,z],origin:[x,y,z]. "
                + "Le coffre doit exister. Demander les informations manquantes, ne pas inventer. L'acceptation ne signifie pas livraison.",
                props(prop("spec", "string", "Specification JSON complete de la mission, 8192 caracteres maximum.")),List.of("spec")));
        tools.add(tool("work_status", "Lire les compteurs, le sac et le rollback de la mission, sans la modifier.",
                props(prop("mission_id", "string", "UUID de la mission, ou current pour la lecture seulement.")),List.of("mission_id")));
        tools.add(tool("work_control", "Pauser, reprendre, annuler ou deposer le sac d'une mission. Ne libere jamais le journal de rollback.",
                props(prop("mission_id", "string", "UUID exact retourne par work_submit."),
                        prop("action", "string", "pause, resume, cancel ou deposit")),List.of("mission_id","action")));
        tools.add(tool("attack_nearest", "Attaquer les monstres hostiles les plus proches.",
                props(prop("radius", "integer", "Rayon de détection (4 à 24)."),
                        prop("count", "integer", "Nombre de monstres à éliminer (1 à 10).")),
                List.of()));
        tools.add(tool("pickup", "Aller ramasser les objets au sol autour de toi.",
                props(prop("radius", "integer", "Rayon de ramassage (4 à 32)."),
                        prop("count", "integer", "Nombre d'objets à ramasser (1 à 64).")),
                List.of()));
        tools.add(tool("scan", "Compter un type de bloc autour de toi, sans rien miner. Réponse immédiate.",
                props(prop("block", "string", "Identifiant du bloc (ex: iron_ore)."),
                        prop("radius", "integer", "Rayon horizontal (4 à 24), hauteur +/-4.")),
                List.of("block")));
        tools.add(tool("probe", "Observer les blocs et dangers locaux avec couverture explicite; hauteur +/-4.",
                props(prop("radius", "integer", "Rayon d'analyse (4 à 16).")), List.of()));
        tools.add(tool("find", "Localiser les blocs les plus proches d'un type (fer, charbon, coffre...)"
                        + " par coordonnees, dans les cellules chargees non protegees. Réponse immédiate.",
                props(prop("block", "string", "Identifiant du bloc (ex: iron_ore)."),
                        prop("radius", "integer", "Rayon horizontal (4 à 24), hauteur +/-4."),
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
        try { return validateChecked(order,level,bot,cfg); }
        catch(RuntimeException ex) {return "arguments invalides : " + ex.getMessage();}
    }
    private static String validateChecked(Order order,ServerLevel level,LivingEntity bot,BrainConfig cfg) {
        if(order==null || order.name()==null || order.args()==null) return "ordre vide";
        String name=order.name();
        String spec=schemaOf(name);
        if(spec==null) return "outil inconnu: "+name+"; minage via work_submit uniquement";
        JsonObject schema=com.google.gson.JsonParser.parseString(spec).getAsJsonObject();
        JsonObject fields=schema.getAsJsonObject("properties");
        for(JsonElement field:schema.getAsJsonArray("required"))
            if(!order.args().has(field.getAsString())) return "champ manquant: "+field.getAsString();
        for(var field:order.args().entrySet()) {
            if(!fields.has(field.getKey())) return "champ inconnu: "+field.getKey();
            JsonElement v=field.getValue();String type=fields.getAsJsonObject(field.getKey()).get("type").getAsString();
            if(!v.isJsonPrimitive()) return "type invalide: "+field.getKey();
            var primitive=v.getAsJsonPrimitive();
            if(type.equals("string") && !primitive.isString()) return "texte requis: "+field.getKey();
            if(type.equals("integer")) {
                if(!primitive.isNumber() || !v.getAsString().matches("-?[0-9]+")) return "entier requis: "+field.getKey();
                v.getAsBigDecimal().intValueExact();
            }
            if(type.equals("number") && (!primitive.isNumber() || !Double.isFinite(v.getAsDouble()))) return "nombre fini requis";
            if(type.equals("string") && v.getAsString().length()>(field.getKey().equals("spec")?8192:1000)) return "texte trop long";
        }
        if(!WITHOUT_BODY.contains(name) && (bot==null || bot.isRemoved() || !bot.isAlive() || bot.level()!=level)) return "aucun servitor disponible dans cette dimension";
        if(bot!=null && com.donopot.voiddynasty.work.WorkService.owns(level,bot)
                && List.of("goto","come_to_player","follow_player","attack_nearest","pickup","work_submit").contains(name)) {
            // Native submit handles identical request replay without taking ownership twice.
            if(!name.equals("work_submit")) return "ACTION_BUSY: chantier actif; work_status ou pause d'abord";
        }
        if(name.equals("say") && (order.strArg("message").isBlank() || order.strArg("message").length()>200)) return "message vide ou trop long";
        if(name.equals("remember") && order.strArg("note").isBlank()) return "note vide";
        if(order.args().has("radius")) {
            int max=switch(name) {case "probe" -> 16;case "find", "scan" -> 24;case "attack_nearest" -> 24;default -> 32;};
            if(order.intArg("radius")<4 || order.intArg("radius")>max) return "rayon hors limites";
        }
        if(order.args().has("count") && (order.intArg("count")<1 || order.intArg("count")>(name.equals("attack_nearest")?10:64))) return "quantite hors limites";
        if(order.args().has("limit") && (order.intArg("limit")<1 || order.intArg("limit")>16)) return "limite hors limites";
        if(order.args().has("seconds") && (order.intArg("seconds")<1 || order.intArg("seconds")>60)) return "attente hors limites";
        if(order.args().has("distance") && (order.doubleArg("distance")<2 || order.doubleArg("distance")>10)) return "distance hors limites";
        if(order.args().has("half") && (order.intArg("half")<1 || order.intArg("half")>4)) return "largeur hors limites";
        if(order.args().has("layers") && (order.intArg("layers")<1 || order.intArg("layers")>6)) return "hauteur hors limites";
        if(name.equals("goto") || name.equals("box")) {
            BlockPos dst=new BlockPos(order.intArg("x"),order.intArg("y"),order.intArg("z"));
            if(level.isOutsideBuildHeight(dst) || !level.getWorldBorder().isWithinBounds(dst) || !level.hasChunkAt(dst)) return "destination indisponible";
            if(Math.sqrt(dst.distSqr(bot.blockPosition()))>Math.max(1,Math.min(cfg.maxDistance,128))) return "destination trop loin";
        }
        if(List.of("come_to_player","follow_player","where").contains(name)) {
            ServerPlayer player=playerByName(level,order.strArg("player"));
            if(player==null || player.level()!=level) return "joueur absent de cette dimension";
            if(!name.equals("where") && bot.distanceTo(player)>Math.max(1,Math.min(cfg.maxDistance,128))) return "joueur trop loin";
        }
        if(name.equals("scan") || name.equals("find")) {
            Identifier id=Identifier.tryParse(normalizeBlock(order.strArg("block")));
            if(id==null || !BuiltInRegistries.BLOCK.containsKey(id)) return "bloc inconnu";
        }
        if(name.equals("work_submit")) {
            var mission=com.donopot.voiddynasty.work.MissionService.parse(com.google.gson.JsonParser.parseString(order.strArg("spec")).getAsJsonObject());
            long volume=(long)(mission.max.getX()-mission.min.getX()+1)*(mission.max.getY()-mission.min.getY()+1)*(mission.max.getZ()-mission.min.getZ()+1);
            if(volume>Math.max(1,Math.min(32768,cfg.maxVolume))) return "zone trop volumineuse pour le cerveau";
            if(Math.sqrt(mission.origin.distSqr(bot.blockPosition()))>Math.max(1,Math.min(cfg.maxDistance,128))) return "origine trop loin du servitor";
            for(BlockPos point:List.of(mission.min,mission.max,mission.destination))
                if(Math.sqrt(point.distSqr(bot.blockPosition()))>Math.max(1,Math.min(cfg.maxDistance,128))) return "mission trop loin du servitor";
            if(TaskRunner.current(bot)!=null) return "ACTION_BUSY: tache du compagnon active; stop d'abord";
        }
        if(name.equals("work_status") || name.equals("work_control")) {
            String id=order.strArg("mission_id");
            if(!(name.equals("work_status") && id.equals("current"))) java.util.UUID.fromString(id);
            if(name.equals("work_control") && !List.of("pause","resume","cancel","deposit").contains(order.strArg("action"))) return "action refusee; le journal ne peut etre efface par le modele";
        }
        return null;
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
