package com.donopot.voiddynasty.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Appel au modèle par l'API compatible OpenAI ({@code /chat/completions}) — LM Studio,
 * Ollama, llama.cpp, vLLM, ou un service distant.
 *
 * <p>Deux formats de réponse sont acceptés, parce que tous les modèles locaux ne savent pas
 * appeler un outil :</p>
 * <ol>
 *   <li>l'appel d'outil natif ({@code tool_calls}) ;</li>
 *   <li>à défaut, un JSON dans le texte ({@code {"tool": "...", "args": {...}}}), ce que
 *       produisent très bien les modèles qui n'ont pas le mode outil.</li>
 * </ol>
 *
 * <p>Cette méthode est bloquante : elle est toujours appelée depuis le fil dédié du cerveau,
 * jamais depuis le fil du serveur.</p>
 */
public final class BrainLlm {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private BrainLlm() {}

    /** Résultat d'un cycle : soit un ordre, soit une explication, soit une erreur. */
    public record Result(BrainTools.Order order, String text, String error) {

        public static Result order(BrainTools.Order o, String text) {
            return new Result(o, text == null ? "" : text, null);
        }

        public static Result none(String text) {
            return new Result(null, text == null ? "" : text, null);
        }

        public static Result error(String message) {
            return new Result(null, "", message);
        }

        public boolean isError() {
            return error != null;
        }
    }

    /** Dernier id de modele resolu automatiquement, et l'URL qui l'a fourni. */
    private static volatile String cachedUrl = "";
    private static volatile String cachedModel = "";
    private static volatile List<String> modelsCache = new ArrayList<>();
    private static volatile String modelsUrl = "";
    private static volatile long modelsAt = 0L;

    /**
     * Id du modele a envoyer : celui de la config, ou a defaut le premier que le serveur
     * expose sur {@code /models}. LM Studio nomme ses modeles avec un chemin complet
     * (« qwen2.5-7b-instruct »), donc deviner ce nom est le premier motif d'echec.
     */
    public static String resolveModel(BrainConfig cfg) {
        String configured = cfg.model == null ? "" : cfg.model.trim();
        boolean auto = configured.isEmpty() || "auto".equalsIgnoreCase(configured);
        List<String> ids = knownModels(cfg);
        if (auto) {
            if (ids.isEmpty()) return cachedModel;
            cachedUrl = cfg.baseUrl;
            cachedModel = ids.get(0);
            return cachedModel;
        }
        // Un id configure mais inconnu du serveur (ancien nom Ollama, modele decharge) ne doit pas
        // bloquer le cerveau : LM Studio repond quand meme, avec le modele charge. On prend celui-la.
        if (!ids.isEmpty() && !ids.contains(configured)) {
            cachedUrl = cfg.baseUrl;
            cachedModel = ids.get(0);
            return cachedModel;
        }
        return configured;
    }

    /** Libelle lisible du modele : signale un id configure que le serveur ne connait pas. */
    public static String modelLabel(BrainConfig cfg) {
        String configured = cfg.model == null ? "" : cfg.model.trim();
        String resolved = resolveModel(cfg);
        if (resolved.isEmpty()) return "auto (aucun modele charge)";
        if (configured.isEmpty() || "auto".equalsIgnoreCase(configured)) return resolved + " (auto)";
        if (!resolved.equals(configured)) return resolved + " (auto : " + configured + " inconnu)";
        return resolved;
    }

    /** Liste des modeles du serveur, memoisee 30 s pour ne pas interroger /models a chaque cycle. */
    private static List<String> knownModels(BrainConfig cfg) {
        long now = System.currentTimeMillis();
        if (!cfg.baseUrl.equals(modelsUrl) || now - modelsAt > 30_000L) {
            try {
                modelsCache = models(cfg);
                modelsUrl = cfg.baseUrl;
                modelsAt = now;
            } catch (Exception ignored) {
                // serveur muet : on garde la derniere liste connue (souvent vide)
            }
        }
        return modelsCache;
    }

    /** Oublie le modele resolu (apres un changement d'URL ou de modele par commande). */
    public static void forgetModel() {
        cachedUrl = "";
        cachedModel = "";
        modelsCache = new ArrayList<>();
        modelsUrl = "";
        modelsAt = 0L;
    }

    /**
     * Un tour de cerveau. Si le modele bavarde au lieu d'appeler un outil, on lui pose UNE
     * question de rappel en imposant {@code tool_choice: required} : les petits modeles locaux
     * (Llama 3 8B, Mistral 7B) ignorent regulierement la consigne « reponds par un outil ».
     */
    public static Result ask(BrainConfig cfg, String systemPrompt, String userContent) {
        try {
            Result first = askOnce(cfg, systemPrompt, userContent, "auto", null);
            if (first.isError() || first.order() != null) return first;
            // Sortie degeneree (le modele recopie la consigne en boucle) : relancer l'aggrave.
            if (first.text().length() > 1500) return first;
            // Deuxieme et dernier essai : on impose l'appel d'outil.
            Result forced = askOnce(cfg, systemPrompt, userContent, "required", first.text());
            if (forced.order() != null) return forced;

            // Troisieme cas, observe avec Qwen3 dans LM Studio : le modele ecrit juste le nom de
            // l'outil (« mine_block ») sans appel structure ni JSON. On lui demande alors les
            // arguments dans un echange a part, sans outils, pour obtenir un objet JSON propre.
            String spoken = forced.text().isBlank() ? first.text() : forced.text();
            String tool = bareTool(spoken);
            if (tool != null) {
                JsonObject args = askArguments(cfg, tool, userContent);
                return Result.order(new BrainTools.Order(tool, args), spoken);
            }
            return first;
        } catch (java.net.http.HttpTimeoutException e) {
            return Result.error("timeout (" + cfg.timeoutSeconds + " s) — modele trop lent pour ce delai, voir /servitor brain timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error("interrompu");
        } catch (Exception e) {
            String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return Result.error(m);
        }
    }

    private static Result askOnce(BrainConfig cfg, String systemPrompt, String userContent,
                                  String toolChoice, String previousText) throws Exception {
        String model = resolveModel(cfg);
        if (model.isEmpty()) {
            return Result.error("aucun modele expose par " + cfg.modelsUrl()
                    + " — demarrer LM Studio et charger un modele");
        }
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", cfg.temperature);
        body.addProperty("max_tokens", 500);
        body.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        String system = systemPrompt + "\n\nOutils disponibles : " + toolNames();
        if (cfg.noThink) system = system + "\n/no_think";
        messages.add(message("system", system));
        messages.add(message("user", userContent));
        if (previousText != null) {
            if (!previousText.isBlank()) messages.add(message("assistant", shorten(previousText)));
            messages.add(message("user",
                    "Tu n'as pas appele d'outil. Appelle MAINTENANT exactement un outil, sans texte."));
        }
        body.add("messages", messages);
        body.add("tools", BrainTools.schema());
        body.addProperty("tool_choice", toolChoice);

        HttpResponse<String> resp = send(cfg, "/chat/completions", body);
        if (resp.statusCode() / 100 != 2) {
            return Result.error("HTTP " + resp.statusCode() + " : " + shorten(resp.body()));
        }
        return parse(resp.body());
    }

    /** Reconnait {@code nom{json}} ou {@code nom(json)} ecrit en clair par le modele. */
    private static BrainTools.Order namedCall(String text) {
        String t = text.trim().replace("`", "").trim();
        int brace = t.indexOf('{');
        int paren = t.indexOf('(');
        int cut = brace >= 0 ? brace : paren;
        if (cut <= 0) return null;
        String name = t.substring(0, cut).trim().toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z_]", "");
        if (name.isEmpty() || !BrainTools.names().contains(name)) return null;
        JsonObject args = firstJsonObject(t.substring(cut));
        return new BrainTools.Order(name, args == null ? new JsonObject() : args);
    }

    static String bareTool(String text) {
        if (text == null) return null;
        String t = text.trim().replaceAll("[\\s`\"'*.!:]+", "");
        if (t.isEmpty() || t.length() > 40) return null;
        for (String name : BrainTools.names()) {
            if (name.equalsIgnoreCase(t)) return name;
        }
        return null;
    }

    /** Demande uniquement les arguments JSON d'un outil, sans outils ni texte libre. */
    private static JsonObject askArguments(BrainConfig cfg, String tool, String state) throws Exception {
        String schema = BrainTools.schemaOf(tool);
        String prompt = "ETAT ACTUEL (JSON)\n" + state
                + "\n\nOutil choisi : " + tool
                + "\nSchema de ses arguments : " + (schema == null ? "{}" : schema);
        String system = "Tu produis uniquement un objet JSON d'arguments pour un outil. "
                + "Aucun texte, aucune balise, aucun commentaire : juste l'objet JSON.";
        JsonObject args = firstJsonObject(rawContent(cfg, system, prompt));
        return args == null ? new JsonObject() : args;
    }

    /** Contenu texte d'une reponse, sans outils — sert a obtenir un JSON d'arguments. */
    private static String rawContent(BrainConfig cfg, String system, String user) throws Exception {
        String model = resolveModel(cfg);
        if (model.isEmpty()) return "";
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("temperature", 0);
        body.addProperty("max_tokens", 300);
        body.addProperty("stream", false);
        JsonArray messages = new JsonArray();
        messages.add(message("system", system));
        messages.add(message("user", user));
        body.add("messages", messages);
        HttpResponse<String> resp = send(cfg, "/chat/completions", body);
        if (resp.statusCode() / 100 != 2) return "";
        try {
            JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) return "";
            JsonObject msg = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (msg == null || !msg.has("content") || msg.get("content").isJsonNull()) return "";
            return msg.get("content").getAsString();
        } catch (RuntimeException e) {
            return "";
        }
    }

    /** Premier objet JSON trouve dans un texte, ou null. */
    static JsonObject firstJsonObject(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        while (start >= 0 && end > start) {
            try {
                return JsonParser.parseString(text.substring(start, end + 1)).getAsJsonObject();
            } catch (RuntimeException e) {
                end = text.lastIndexOf('}', end - 1);
            }
        }
        return null;
    }

    /** Liste des modes exposes par le serveur, pour verifier un id a la main. */
    public static List<String> models(BrainConfig cfg) throws Exception {
        HttpResponse<String> resp = send(cfg, "/models", null, "GET");
        List<String> ids = new ArrayList<>();
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("HTTP " + resp.statusCode() + " : " + shorten(resp.body()));
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(resp.body()).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IllegalStateException("reponse de " + cfg.modelsUrl()
                    + " illisible (HTTP 2xx mais pas du JSON) : " + shorten(resp.body()));
        }
        JsonArray data = root.getAsJsonArray("data");
        if (data == null) {
            // LM Studio repond 200 sur une route inconnue : signe d'une URL sans /v1.
            throw new IllegalStateException("pas de liste de modeles sur " + cfg.modelsUrl()
                    + " : " + shorten(resp.body()));
        }
        for (JsonElement el : data) {
            JsonObject o = el.getAsJsonObject();
            if (o.has("id")) ids.add(o.get("id").getAsString());
        }
        return ids;
    }

    private static HttpResponse<String> send(BrainConfig cfg, String path, JsonObject body) throws Exception {
        return send(cfg, path, body, "POST");
    }

    private static HttpResponse<String> send(BrainConfig cfg, String path, JsonObject body, String method) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(cfg.apiBase() + path))
                .timeout(Duration.ofSeconds(Math.max(5, cfg.timeoutSeconds)))
                .header("Content-Type", "application/json");
        String key = cfg.apiKey();
        if (!key.isEmpty()) req.header("Authorization", "Bearer " + key);
        if ("GET".equals(method)) {
            req.GET();
        } else {
            req.POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
        }
        return HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static Result parse(String raw) {
        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return Result.error("reponse sans choix sur " + shorten(raw)
                    + " — verifier que l'endpoint se termine par /v1");
        }
        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) return Result.error("reponse sans message");

        String text = message.has("content") && !message.get("content").isJsonNull()
                ? message.get("content").getAsString() : "";
        // Qwen3 & co. renvoient toute leur reflexion dans reasoning_content et laissent content
        // vide : sans cela le cerveau croit que le modele n'a rien dit et n'agit pas.
        if (text.isBlank() && message.has("reasoning_content") && !message.get("reasoning_content").isJsonNull()) {
            text = message.get("reasoning_content").getAsString();
        }

        JsonArray calls = message.getAsJsonArray("tool_calls");
        if (calls != null && !calls.isEmpty()) {
            JsonObject fn = calls.get(0).getAsJsonObject().getAsJsonObject("function");
            if (fn != null) {
                String name = fn.get("name").getAsString();
                return Result.order(new BrainTools.Order(name, argsOf(fn.get("arguments"))), text);
            }
        }

        // Repli : un JSON dans le texte (modèles sans mode outil).
        BrainTools.Order fromText = orderFromText(text);
        if (fromText != null) return Result.order(fromText, text);
        return Result.none(text);
    }

    private static JsonObject argsOf(JsonElement rawArgs) {
        if (rawArgs == null || rawArgs.isJsonNull()) return new JsonObject();
        String s = rawArgs.isJsonPrimitive() ? rawArgs.getAsString() : rawArgs.toString();
        if (s == null || s.isBlank()) return new JsonObject();
        try {
            return JsonParser.parseString(s).getAsJsonObject();
        } catch (RuntimeException e) {
            return new JsonObject();
        }
    }

    /** Cherche {@code {"tool": "..."}} ou {@code {"name": "...", "args": {...}}} dans le texte. */
    static BrainTools.Order orderFromText(String text) {
        if (text == null) return null;
        // Certains modeles ecrivent l'appel comme du texte : `come_to_player{"player":"Donopot"}`.
        // Le nom est alors HORS du JSON, invisible pour la recherche d'accolades ci-dessous.
        BrainTools.Order named = namedCall(text);
        if (named != null) return named;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        while (start >= 0 && end > start) {
            String candidate = text.substring(start, end + 1);
            try {
                JsonObject o = JsonParser.parseString(candidate).getAsJsonObject();
                String name = null;
                if (o.has("tool") && o.get("tool").isJsonPrimitive()) name = o.get("tool").getAsString();
                else if (o.has("name") && o.get("name").isJsonPrimitive()) name = o.get("name").getAsString();
                if (name != null && !name.isBlank()) {
                    JsonObject args = new JsonObject();
                    for (String key : new String[]{"args", "arguments", "parameters"}) {
                        if (o.has(key) && o.get(key).isJsonObject()) {
                            args = o.getAsJsonObject(key);
                            break;
                        }
                    }
                    if (args.isEmpty()) {
                        // arguments à plat : {"tool":"goto","x":1,"y":2,"z":3}
                        JsonObject flat = new JsonObject();
                        for (String key : o.keySet()) {
                            if (!key.equals("tool") && !key.equals("name") && !key.equals("reasoning")) {
                                flat.add(key, o.get(key));
                            }
                        }
                        args = flat;
                    }
                    return new BrainTools.Order(name.trim().toLowerCase(java.util.Locale.ROOT), args);
                }
            } catch (RuntimeException ignored) {
            }
            end = text.lastIndexOf('}', end - 1);
            if (end <= start) break;
        }
        return null;
    }

    private static String toolNames() {
        StringBuilder sb = new StringBuilder();
        for (JsonElement el : BrainTools.schema()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(el.getAsJsonObject().getAsJsonObject("function").get("name").getAsString());
        }
        return sb.toString();
    }

    private static JsonObject message(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    static String shorten(String s) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= 300 ? t : t.substring(0, 300) + "…";
    }
}
