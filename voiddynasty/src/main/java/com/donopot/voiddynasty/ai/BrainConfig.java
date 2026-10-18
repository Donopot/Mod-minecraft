package com.donopot.voiddynasty.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Réglages du cerveau du Servitor, lus dans {@code config/voiddynasty-servitor.json}.
 *
 * <p>La clé d'API n'est JAMAIS écrite dans ce fichier : on y met seulement le NOM de la
 * variable d'environnement qui la porte ({@link #apiKeyEnv}). Un fichier de config peut
 * finir dans un dépôt ou une sauvegarde ; une variable d'environnement, non.</p>
 *
 * <p>Désactivé par défaut : aucun appel réseau tant que {@code /servitor brain on} n'a pas
 * été tapé par un opérateur.</p>
 */
public final class BrainConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "voiddynasty-servitor.json";

    /** Interrupteur principal, persisté. */
    public boolean enabled = false;

    /** Endpoint compatible OpenAI, sans le suffixe /chat/completions. LM Studio = port 1234. */
    public String baseUrl = "http://localhost:1234/v1";

    /** Id du modele. Vide ou "auto" = premier modele expose par le serveur sur /models. */
    public String model = "auto";

    /** Nom de la variable d'environnement contenant la clé (vide = pas d'en-tête Authorization). */
    public String apiKeyEnv = "VOIDDYNASTY_LLM_KEY";

    /** Une décision toutes les N ticks (100 ticks = 5 s). */
    public int intervalTicks = 100;
    public int timeoutSeconds = 60;

    /**
     * Coupe la reflexion des modeles « thinking » (Qwen3 et derives) : on ajoute /no_think au
     * prompt systeme, leur interrupteur documente. Reponses plus rapides et moins de jetons
     * brules en raisonnement quand on veut juste un appel d'outil.
     */
    public boolean noThink = false;

    /**
     * true : chaque message du chat devient une consigne pour le servitor (pratique en solo).
     * false : il faut ecrire « servitor … » ou commencer par « !s ».
     */
    public boolean chatAll = false;

    /**
     * true : le modele n'est appele QUE quand l'operateur donne un ordre, avec un prompt minimal.
     * false : il recoit l'etat complet du monde a chaque cycle (comportement historique).
     */
    public boolean orderOnly = true;
    public double temperature = 0.2;

    /** Garde-fous d'action. */
    public int maxRadius = 24;
    public int maxVolume = 8192;
    public int maxDistance = 96;

    /** Après N échecs consécutifs, le cerveau se coupe tout seul. */
    public int maxErrorsBeforeDisable = 3;

    private static BrainConfig instance;

    public static synchronized BrainConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static BrainConfig load() {
        Path path = path();
        BrainConfig cfg = new BrainConfig();
        if (Files.isRegularFile(path)) {
            try {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (json.has("enabled")) cfg.enabled = json.get("enabled").getAsBoolean();
                if (json.has("baseUrl")) cfg.baseUrl = json.get("baseUrl").getAsString();
                if (json.has("model")) cfg.model = json.get("model").getAsString();
                if (json.has("apiKeyEnv")) cfg.apiKeyEnv = json.get("apiKeyEnv").getAsString();
                if (json.has("intervalTicks")) cfg.intervalTicks = json.get("intervalTicks").getAsInt();
                if (json.has("timeoutSeconds")) cfg.timeoutSeconds = json.get("timeoutSeconds").getAsInt();
                if (json.has("noThink")) cfg.noThink = json.get("noThink").getAsBoolean();
                if (json.has("chatAll")) cfg.chatAll = json.get("chatAll").getAsBoolean();
                if (json.has("orderOnly")) cfg.orderOnly = json.get("orderOnly").getAsBoolean();
                if (json.has("temperature")) cfg.temperature = json.get("temperature").getAsDouble();
                if (json.has("maxRadius")) cfg.maxRadius = json.get("maxRadius").getAsInt();
                if (json.has("maxVolume")) cfg.maxVolume = json.get("maxVolume").getAsInt();
                if (json.has("maxDistance")) cfg.maxDistance = json.get("maxDistance").getAsInt();
                if (json.has("maxErrorsBeforeDisable")) {
                    cfg.maxErrorsBeforeDisable = json.get("maxErrorsBeforeDisable").getAsInt();
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("[servitor] config illisible, valeurs par defaut : " + e.getMessage());
            }
        }
        cfg.save();
        return cfg;
    }

    public void save() {
        JsonObject json = new JsonObject();
        json.addProperty("_commentaire", "Endpoint compatible OpenAI. La cle n'est pas ici : voir apiKeyEnv.");
        json.addProperty("enabled", enabled);
        json.addProperty("baseUrl", baseUrl);
        json.addProperty("model", model);
        json.addProperty("apiKeyEnv", apiKeyEnv);
        json.addProperty("intervalTicks", intervalTicks);
        json.addProperty("timeoutSeconds", timeoutSeconds);
        json.addProperty("noThink", noThink);
        json.addProperty("chatAll", chatAll);
        json.addProperty("orderOnly", orderOnly);
        json.addProperty("temperature", temperature);
        json.addProperty("maxRadius", maxRadius);
        json.addProperty("maxVolume", maxVolume);
        json.addProperty("maxDistance", maxDistance);
        json.addProperty("maxErrorsBeforeDisable", maxErrorsBeforeDisable);
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(json));
        } catch (IOException e) {
            System.err.println("[servitor] config non ecrite : " + e.getMessage());
        }
    }

    /** Clé lue dans l'environnement, jamais depuis le fichier. */
    public String apiKey() {
        if (apiKeyEnv == null || apiKeyEnv.isBlank()) return "";
        String v = System.getenv(apiKeyEnv);
        return v == null ? "" : v.trim();
    }

    /**
     * Racine reellement appelee. Les serveurs compatibles OpenAI (LM Studio, Ollama, llama.cpp,
     * vLLM) exposent leurs routes sous {@code /v1} : une URL donnee sans chemin
     * (« http://localhost:1234 ») recoit donc un 200 trompeur sur /models et /chat/completions,
     * avec un corps qui n'est pas une reponse de chat. On ajoute /v1 nous-memes plutot que de
     * dependre de la mémoire de l'operateur.
     */
    public String apiBase() {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String path = base.replaceFirst("^https?://[^/]+", "");
        if (path.isEmpty()) base = base + "/v1";
        return base;
    }

    public String chatUrl() {
        return apiBase() + "/chat/completions";
    }

    public String modelsUrl() {
        return apiBase() + "/models";
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE);
    }

    public int intervalTicksSafe() {
        return Math.max(20, intervalTicks);
    }
}
