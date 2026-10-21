package com.donopot.voiddynasty.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Mémoire de faits du Servitor, <b>propre à chaque monde</b>.
 *
 * <p>Un fichier par monde, dans le dossier du monde : deux sauvegardes différentes ne partagent
 * donc plus la même mémoire, et supprimer un monde supprime ses notes. Une note est un fait
 * durable et vérifiable (« filon de fer en 120, -54, 88 »), jamais un état d'exécution.</p>
 *
 * <p>Aucune erreur n'est avalée : une écriture qui échoue lève {@link BrainNotesException} et
 * l'appelant doit le dire à l'opérateur. Une lecture impossible bloque les ajouts et conserve le fichier original.</p>
 */
public final class BrainNotes {

    /** Erreur de persistance, à remonter telle quelle à l'opérateur. */
    public static final class BrainNotesException extends RuntimeException {
        public BrainNotesException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_NOTES = 200;
    private static final String FILE_NAME = "voiddynasty-servitor-notes.json";

    private BrainNotes() {}

    /** Fichier de notes du monde courant : {@code <monde>/voiddynasty/voiddynasty-servitor-notes.json}. */
    static Path path(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("voiddynasty").resolve(FILE_NAME);
    }

    /** Ancien emplacement global, repris une seule fois dans le premier monde qui en a besoin. */
    private static Path legacyPath() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    /** Ajoute une note avec la position et la dimension où elle a été prise. */
    public static String add(ServerLevel level, LivingEntity bot, String text) {
        String note = text == null ? "" : text.trim();
        if (note.isEmpty()) return "note vide : rien enregistre";
        if (note.length() > 400) note = note.substring(0, 400);

        JsonArray notes = load(level);
        JsonObject entry = new JsonObject();
        entry.addProperty("note", note);
        entry.addProperty("monde", level.dimension().identifier().toString());
        BlockPos p = bot == null ? BlockPos.ZERO : bot.blockPosition();
        entry.addProperty("x", p.getX());
        entry.addProperty("y", p.getY());
        entry.addProperty("z", p.getZ());
        entry.addProperty("quand", Instant.now().toString());
        notes.add(entry);

        while (notes.size() > MAX_NOTES) notes.remove(0);
        save(level, notes);
        return "note #" + notes.size() + " enregistree (" + p.toShortString() + ") : " + note;
    }

    /** Les N dernières notes du monde, de la plus ancienne à la plus récente. */
    public static String list(ServerLevel level, int count) {
        JsonArray notes = load(level);
        if (notes.isEmpty()) return "aucune note enregistree dans ce monde";
        int from = Math.max(0, notes.size() - Math.max(1, count));
        StringBuilder sb = new StringBuilder("memoire de ce monde (").append(notes.size()).append(" notes) : ");
        for (int i = from; i < notes.size(); i++) {
            JsonObject o = notes.get(i).getAsJsonObject();
            sb.append("\n[").append(i + 1).append("] ").append(str(o, "note"))
                    .append(" — ").append(str(o, "x")).append(", ").append(str(o, "y"))
                    .append(", ").append(str(o, "z")).append(" [").append(str(o, "monde")).append("] ").append(str(o, "quand"));
        }
        return sb.toString();
    }

    /** Recherche par mot-clé, ou les 10 dernières notes si le mot-clé est vide. */
    public static String search(ServerLevel level, String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase();
        if (k.isBlank()) return list(level, 10);
        JsonArray notes = load(level);
        List<String> hits = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            JsonObject o = notes.get(i).getAsJsonObject();
            if (str(o, "note").toLowerCase().contains(k)) {
                hits.add("[" + (i + 1) + "] " + str(o, "note") + " — " + str(o, "x") + ", "
                        + str(o, "y") + ", " + str(o, "z") + " [" + str(o, "monde") + "] " + str(o, "quand"));
            }
        }
        return hits.isEmpty() ? "aucune note de ce monde ne contient '" + keyword + "'"
                : hits.size() + " note(s) :\n" + String.join("\n", hits);
    }

    /** Vide la mémoire du monde. Explicite : le fichier est réécrit, pas supprimé. */
    public static String clear(ServerLevel level) {
        save(level, new JsonArray());
        return "memoire de ce monde videe";
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) ? o.get(key).getAsString() : "?";
    }

    /** Charge les notes du monde ; reprend une fois l'ancien fichier global s'il existe. */
    static JsonArray load(ServerLevel level) {
        Path p = path(level);
        if (!Files.exists(p)) {
            JsonArray legacy = loadLegacy();
            if (legacy != null) {
                save(level, legacy);
                return legacy;
            }
            return new JsonArray();
        }
        try {
            String json = Files.readString(p, StandardCharsets.UTF_8);
            if (json.isBlank()) throw new IllegalStateException("fichier de notes vide");
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonArray()) throw new IllegalStateException("tableau de notes attendu");
            for (JsonElement entry : parsed.getAsJsonArray()) {
                if (!entry.isJsonObject() || !entry.getAsJsonObject().has("note")
                        || !entry.getAsJsonObject().get("note").isJsonPrimitive())
                    throw new IllegalStateException("note invalide");
            }
            return parsed.getAsJsonArray();
        } catch (IOException | RuntimeException e) {
            // On ne détruit rien : le fichier illisible est mis de côté, l'erreur est dite.
            Path backup = p.resolveSibling(FILE_NAME + ".corrompu-" + System.currentTimeMillis());
            try {
                Files.copy(p, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // le message ci-dessous dit déjà que la lecture a échoué
            }
            throw new BrainNotesException("memoire de ce monde illisible (archive : "
                    + backup.getFileName() + ")", e);
        }
    }

    /** Reprise de l'ancien fichier global, une seule fois par monde. */
    private static JsonArray loadLegacy() {
        Path legacy = legacyPath();
        if (!Files.exists(legacy)) return null;
        try {
            String json = Files.readString(legacy, StandardCharsets.UTF_8);
            if (json.isBlank()) return null;
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonArray() || parsed.getAsJsonArray().isEmpty()) return null;
            return parsed.getAsJsonArray();
        } catch (IOException | RuntimeException e) {
            return null; // legacy illisible : on ne bloque pas le monde pour autant
        }
    }

    /** Écrit les notes ; toute erreur est levée, jamais ignorée. */
    static void save(ServerLevel level, JsonArray notes) {
        Path p = path(level);
        try {
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, GSON.toJson(notes), StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BrainNotesException("ecriture impossible dans " + p.getParent(), e);
        }
    }
}
