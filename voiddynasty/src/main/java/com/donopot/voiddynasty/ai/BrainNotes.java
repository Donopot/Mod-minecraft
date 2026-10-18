package com.donopot.voiddynasty.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Memoire de faits du Servitor : ce qu'il a appris et qui doit survivre a la session.
 *
 * <p>Un fichier JSON a cote de la config du monde. On n'y met que des faits durables et
 * verifiables (« filon de fer en 120, -54, 88 », « tunnel vers la base depuis -30, 65, 12 »),
 * jamais l'etat runtime : une note perimee coute plus cher qu'une note absente.</p>
 */
public final class BrainNotes {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_NOTES = 200;

    private BrainNotes() {}

    private static Path path() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        return dir.resolve("voiddynasty-servitor-notes.json");
    }

    /** Ajoute une note, avec la position et la dimension ou elle a ete prise. */
    public static String add(ServerLevel level, LivingEntity bot, String text) {
        String note = text == null ? "" : text.trim();
        if (note.isEmpty()) return "note vide : rien enregistre";
        if (note.length() > 400) note = note.substring(0, 400);

        JsonArray notes = load();
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
        save(notes);
        return "note #" + notes.size() + " enregistree (" + p.toShortString() + ") : " + note;
    }

    /** Les N dernieres notes, de la plus ancienne a la plus recente. */
    public static String list(int count) {
        JsonArray notes = load();
        if (notes.isEmpty()) return "aucune note enregistree";
        int from = Math.max(0, notes.size() - Math.max(1, count));
        StringBuilder sb = new StringBuilder("memoire (").append(notes.size()).append(" notes) : ");
        for (int i = from; i < notes.size(); i++) {
            JsonObject o = notes.get(i).getAsJsonObject();
            sb.append("\n[").append(i + 1).append("] ").append(str(o, "note"))
                    .append(" — ").append(str(o, "x")).append(", ").append(str(o, "y"))
                    .append(", ").append(str(o, "z"));
        }
        return sb.toString();
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) ? o.get(key).getAsString() : "?";
    }

    private static JsonArray load() {
        Path p = path();
        if (!Files.exists(p)) return new JsonArray();
        try {
            String json = Files.readString(p, StandardCharsets.UTF_8);
            if (json.isBlank()) return new JsonArray();
            return JsonParser.parseString(json).getAsJsonArray();
        } catch (IOException | RuntimeException e) {
            return new JsonArray();
        }
    }

    private static void save(JsonArray notes) {
        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(notes), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    /** Pour les tests : tout oublier. */
    public static String clear() {
        save(new JsonArray());
        return "memoire videe";
    }

    /** Recherche simple par mot-cle, pour retrouver un filon ou un tunnel. */
    public static String search(String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase();
        if (k.isBlank()) return list(10);
        JsonArray notes = load();
        List<String> hits = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            JsonObject o = notes.get(i).getAsJsonObject();
            if (str(o, "note").toLowerCase().contains(k)) {
                hits.add("[" + (i + 1) + "] " + str(o, "note") + " — " + str(o, "x") + ", "
                        + str(o, "y") + ", " + str(o, "z"));
            }
        }
        return hits.isEmpty() ? "aucune note ne contient '" + keyword + "'"
                : hits.size() + " note(s) :\n" + String.join("\n", hits);
    }
}
