package com.donopot.voiddynasty.campagne;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lit les données de campagne (data/voiddynasty/campagne/campagne.json), transcrites de la source par
 * tools/preparer_campagne.py. Le fichier vit dans le jar : il est relu à chaque démarrage, et une donnée
 * manquante se voit tout de suite au lieu de rester silencieuse.
 */
public final class CampagneDonnees {

    private static final Identifier FICHIER =
            Identifier.fromNamespaceAndPath("voiddynasty", "campagne/campagne.json");

    private static List<ZoneCampagne> zones = List.of();
    private static List<RegleCampagne> regles = List.of();
    private static List<MecanismeCampagne> mecanismes = List.of();
    private static Map<Integer, double[]> reapparitions = Map.of();
    private static TickCampagne tick = null;
    private static List<String> problemes = new ArrayList<>();

    private CampagneDonnees() {}

    public static List<ZoneCampagne> zones() { return zones; }
    public static List<RegleCampagne> regles() { return regles; }
    public static List<MecanismeCampagne> mecanismes() { return mecanismes; }
    public static Map<Integer, double[]> reapparitions() { return reapparitions; }
    public static TickCampagne tick() { return tick; }
    public static List<String> problemes() { return List.copyOf(problemes); }

    public static Optional<ZoneCampagne> parId(String id) {
        return zones.stream().filter(z -> z.id().equals(id) || z.zone().equals(id)).findFirst();
    }

    public static Optional<MecanismeCampagne> mecanisme(String tag) {
        return mecanismes.stream().filter(m -> m.tag().equals(tag)).findFirst();
    }

    private static double[] reels(JsonArray a) {
        double[] v = new double[a.size()];
        for (int i = 0; i < a.size(); i++) v[i] = a.get(i).getAsDouble();
        return v;
    }

    private static List<String> chaines(JsonArray a) {
        List<String> v = new ArrayList<>();
        if (a != null) for (JsonElement e : a) v.add(e.getAsString());
        return v;
    }

    /** Recharge depuis les ressources du serveur. Renvoie le nombre de zones lues. */
    public static int charger(MinecraftServer serveur) {
        problemes = new ArrayList<>();
        List<ZoneCampagne> luesZones = new ArrayList<>();
        List<RegleCampagne> luesRegles = new ArrayList<>();
        List<MecanismeCampagne> lusMecanismes = new ArrayList<>();
        Map<Integer, double[]> luesReapparitions = new LinkedHashMap<>();
        try {
            Optional<Resource> ressource = serveur.getResourceManager().getResource(FICHIER);
            if (ressource.isEmpty()) {
                problemes.add("fichier de campagne introuvable : " + FICHIER);
                return 0;
            }
            try (var lecteur = new InputStreamReader(ressource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject racine = JsonParser.parseReader(lecteur).getAsJsonObject();
                for (JsonElement e : racine.getAsJsonArray("zones")) {
                    JsonObject o = e.getAsJsonObject();
                    List<ZoneCampagne.Apparition> mobs = new ArrayList<>();
                    for (JsonElement ae : o.getAsJsonArray("apparitions")) {
                        JsonObject a = ae.getAsJsonObject();
                        mobs.add(new ZoneCampagne.Apparition(a.get("mob").getAsString(),
                                a.has("mob_source") ? a.get("mob_source").getAsString() : "",
                                a.get("x").getAsDouble(), a.get("y").getAsDouble(), a.get("z").getAsDouble()));
                    }
                    List<ZoneCampagne.Waypoint> points = new ArrayList<>();
                    for (JsonElement we : o.getAsJsonArray("waypoints")) {
                        JsonObject w = we.getAsJsonObject();
                        points.add(new ZoneCampagne.Waypoint(w.get("x").getAsDouble(),
                                w.get("y").getAsDouble(), w.get("z").getAsDouble(),
                                chaines(w.getAsJsonArray("tags"))));
                    }
                    List<ZoneCampagne.Objet> objets = new ArrayList<>();
                    for (JsonElement oe : o.getAsJsonArray("objets")) {
                        JsonObject ob = oe.getAsJsonObject();
                        objets.add(new ZoneCampagne.Objet(ob.get("x").getAsDouble(),
                                ob.get("y").getAsDouble(), ob.get("z").getAsDouble(),
                                ob.has("nom_source") ? ob.get("nom_source").getAsString()
                                        : ob.get("nom").getAsString(),
                                ob.has("objet") ? ob.get("objet").getAsString() : null));
                    }
                    List<ZoneCampagne.AppelDiffere> differes = new ArrayList<>();
                    for (JsonElement de : o.getAsJsonArray("appels_differes")) {
                        JsonArray d = de.getAsJsonArray();
                        int ticks = d.get(1).getAsInt() * ("s".equals(d.get(2).getAsString()) ? 20 : 1);
                        differes.add(new ZoneCampagne.AppelDiffere(d.get(0).getAsString(), ticks));
                    }
                    List<String[]> poseCompteurs = new ArrayList<>();
                    for (JsonElement pe : o.getAsJsonArray("pose_compteur")) {
                        JsonArray p = pe.getAsJsonArray();
                        poseCompteurs.add(new String[]{p.get(0).getAsString(), p.get(1).getAsString()});
                    }
                    List<Integer> poseNiveaux = new ArrayList<>();
                    for (JsonElement ne : o.getAsJsonArray("pose_niveau")) poseNiveaux.add(ne.getAsInt());
                    luesZones.add(new ZoneCampagne(o.get("zone").getAsString(),
                            o.get("zone").getAsString(), chaines(o.getAsJsonArray("repliques")),
                            chaines(o.getAsJsonArray("titres")), points, mobs, objets,
                            differes, poseCompteurs, poseNiveaux));
                }
                for (JsonElement e : racine.getAsJsonArray("regles")) {
                    JsonObject o = e.getAsJsonObject();
                    JsonObject c = o.getAsJsonObject("condition");
                    List<String[]> poseCompteurs = new ArrayList<>();
                    for (JsonElement pe : o.getAsJsonArray("pose_compteur")) {
                        JsonArray p = pe.getAsJsonArray();
                        poseCompteurs.add(new String[]{p.get(0).getAsString(), p.get(1).getAsString()});
                    }
                    List<Integer> poseNiveaux = new ArrayList<>();
                    for (JsonElement ne : o.getAsJsonArray("pose_niveau")) poseNiveaux.add(ne.getAsInt());
                    luesRegles.add(new RegleCampagne(reels(o.getAsJsonArray("position")),
                            c.get("type").getAsString(),
                            c.has("rayon") ? c.get("rayon").getAsDouble() : 0.0,
                            c.has("type_source") ? c.get("type_source").getAsString() : null,
                            o.has("compteur_zone") && !o.get("compteur_zone").isJsonNull()
                                    ? o.get("compteur_zone").getAsString() : null,
                            o.has("compteur_valeur") && !o.get("compteur_valeur").isJsonNull()
                                    ? o.get("compteur_valeur").getAsInt() : null,
                            chaines(o.getAsJsonArray("appelle")), poseCompteurs, poseNiveaux,
                            chaines(o.getAsJsonArray("tue"))));
                }
                for (JsonElement e : racine.getAsJsonArray("portes")) {
                    JsonObject o = e.getAsJsonObject();
                    List<int[]> regions = new ArrayList<>();
                    for (JsonElement re : o.getAsJsonArray("regions")) {
                        JsonArray r = re.getAsJsonArray();
                        int[] v = new int[r.size()];
                        for (int i = 0; i < r.size(); i++) v[i] = r.get(i).getAsInt();
                        regions.add(v);
                    }
                    lusMecanismes.add(new MecanismeCampagne(o.get("tag").getAsString(),
                            o.has("condition_zone") && !o.get("condition_zone").isJsonNull()
                                    ? o.get("condition_zone").getAsString() : null,
                            o.has("condition_valeur") && !o.get("condition_valeur").isJsonNull()
                                    ? o.get("condition_valeur").getAsInt() : null,
                            regions, chaines(o.getAsJsonArray("sons")),
                            chaines(o.getAsJsonArray("appelle")), chaines(o.getAsJsonArray("tue")),
                            o.has("compte_retire") && !o.get("compte_retire").isJsonNull()
                                    ? new String[]{o.getAsJsonArray("compte_retire").get(0).getAsString(),
                                                   o.getAsJsonArray("compte_retire").get(1).getAsString(),
                                                   o.getAsJsonArray("compte_retire").get(2).getAsString()}
                                    : null));
                }
                if (racine.has("tick") && !racine.get("tick").isJsonNull()) {
                    JsonObject t = racine.getAsJsonObject("tick");
                    List<TickCampagne.RegleGlobale> globales = new ArrayList<>();
                    for (JsonElement e : t.getAsJsonArray("regles_sans_position")) {
                        JsonObject o = e.getAsJsonObject();
                        globales.add(new TickCampagne.RegleGlobale(o.get("compteur_zone").getAsString(),
                                o.get("compteur_valeur").getAsInt(), o.get("appelle").getAsString()));
                    }
                    List<TickCampagne.RegleEntite> surEntite = new ArrayList<>();
                    for (JsonElement e : t.getAsJsonArray("regles_entite")) {
                        JsonObject o = e.getAsJsonObject();
                        JsonArray pose = o.getAsJsonArray("pose_compteur");
                        surEntite.add(new TickCampagne.RegleEntite(o.get("tag").getAsString(),
                                o.get("rayon").getAsDouble(),
                                new String[]{pose.get(0).getAsString(), pose.get(1).getAsString()}));
                    }
                    TickCampagne.Reinitialisation reinit = null;
                    if (!t.get("reinitialisation").isJsonNull()) {
                        JsonObject o = t.getAsJsonObject("reinitialisation");
                        reinit = new TickCampagne.Reinitialisation(reels(o.getAsJsonArray("position")),
                                o.get("rayon").getAsDouble());
                    }
                    TickCampagne.PowerArmour armure = null;
                    if (!t.get("power_armour").isJsonNull()) {
                        JsonObject o = t.getAsJsonObject("power_armour");
                        armure = new TickCampagne.PowerArmour(o.get("slot").getAsInt(),
                                o.get("objet_source").getAsString(), o.get("max_health").getAsDouble());
                    }
                    TickCampagne.Reacteur reacteur = null;
                    if (!t.get("reacteur").isJsonNull() && !t.get("fin").isJsonNull()) {
                        JsonObject r = t.getAsJsonObject("reacteur");
                        JsonObject f = t.getAsJsonObject("fin");
                        reacteur = new TickCampagne.Reacteur(r.get("barre").getAsString(),
                                r.get("compteur").getAsString(), r.get("objectif").getAsString(),
                                r.has("nom") && !r.get("nom").isJsonNull() ? r.get("nom").getAsString() : null,
                                r.has("max") && !r.get("max").isJsonNull() ? r.get("max").getAsInt() : 0,
                                r.has("depart") ? r.get("depart").getAsInt() : 0,
                                f.get("seuil").getAsInt(), f.get("zone").getAsString(),
                                f.has("teleport") && !f.get("teleport").isJsonNull()
                                        ? reels(f.getAsJsonArray("teleport")) : null,
                                f.has("titre") && !f.get("titre").isJsonNull()
                                        ? f.get("titre").getAsString() : null,
                                chaines(f.getAsJsonArray("sons")),
                                f.has("aveuglement") && f.get("aveuglement").getAsBoolean(),
                                f.has("remet_compteur") && !f.get("remet_compteur").isJsonNull()
                                        ? chaines(f.getAsJsonArray("remet_compteur")).toArray(new String[0])
                                        : null);
                    }
                    tick = new TickCampagne(List.copyOf(globales), List.copyOf(surEntite), reinit, armure, reacteur);
                }
                for (Map.Entry<String, JsonElement> entree :
                        racine.getAsJsonObject("reapparitions").entrySet()) {
                    luesReapparitions.put(Integer.parseInt(entree.getKey()),
                            reels(entree.getValue().getAsJsonArray()));
                }
            }
        } catch (Exception e) {
            problemes.add("lecture impossible : " + e);
            return 0;
        }
        zones = List.copyOf(luesZones);
        regles = List.copyOf(luesRegles);
        mecanismes = List.copyOf(lusMecanismes);
        reapparitions = Map.copyOf(luesReapparitions);
        return zones.size();
    }
}
