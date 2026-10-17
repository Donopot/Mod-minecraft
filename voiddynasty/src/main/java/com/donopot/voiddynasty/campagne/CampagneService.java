package com.donopot.voiddynasty.campagne;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Le moteur de campagne — phase F.
 *
 * La source pilote sa campagne par des commandes en dur : un compteur `active` par zone, des tests de
 * position dans `leveldetect.mcfunction`, des portes qui s'ouvrent quand on les frappe. On reproduit ce
 * COMPORTEMENT avec des classes et des données, comme le recommandait le rapport de mécaniques lui-même.
 * Aucune coordonnée n'est écrite dans le code : tout vient de campagne.json.
 *
 * Trois points sont DITS, parce qu'ils ne sont pas un portage à la lettre :
 *   · les règles qui testaient `type=zombie` sont traduites en « aucun ennemi de la campagne » : les
 *     ennemis de la source ÉTAIENT des zombies, les nôtres sont nos propres créatures ;
 *   · la position des marqueurs de porte est DÉDUITE du centre de la région que la porte ouvre : la
 *     source, elle, a ses shulkers déjà posés dans le monde. Les panneaux du réacteur, qui n'ouvrent
 *     aucune région, ne sont donc pas plaçables — ils sont signalés, pas devinés ;
 *   · les gamerules de la source ne sont posées qu'au démarrage de la campagne, pas à chaque lancement
 *     du serveur : le mod n'est pas toujours en mode campagne.
 */
public final class CampagneService {

    private static final Map<String, Integer> compteurs = new LinkedHashMap<>();
    private static final Set<Integer> reglesUneFois = new HashSet<>();
    private static final Map<UUID, String> marqueurs = new HashMap<>();
    private static final List<Attente> appelsDifferes = new ArrayList<>();
    private static final Set<String> zonesExecutees = new HashSet<>();
    private static final Set<Integer> reglesGlobalesPassees = new HashSet<>();
    private static ServerBossEvent barreReacteur = null;
    private static boolean finDeclenchee = false;
    private static final List<String> problemes = new ArrayList<>();
    private static Set<String> mobsCampagne = Set.of();
    private static int niveau = 0;
    private static int invoquees = 0;
    private static boolean active = false;

    /** Un appel de zone à rejouer dans N ticks (les `schedule function` de la source). */
    private static final class Attente {
        int ticks;
        final String chemin;

        Attente(int ticks, String chemin) {
            this.ticks = ticks;
            this.chemin = chemin;
        }
    }

    private CampagneService() {}

    public static boolean active() { return active; }
    public static int niveauCourant() { return niveau; }
    public static int invoquees() { return invoquees; }
    public static List<String> problemes() { return List.copyOf(problemes); }

    public static int compteur(String zone) { return compteurs.getOrDefault(zone, 0); }

    public static Map<String, Integer> compteurs() { return Map.copyOf(compteurs); }

    /** Appelé au démarrage du serveur : on lit les données, on ne démarre pas la campagne. */
    public static void charger(MinecraftServer serveur) {
        CampagneDonnees.charger(serveur);
        Set<String> mobs = new HashSet<>();
        for (ZoneCampagne zone : CampagneDonnees.zones()) {
            for (ZoneCampagne.Apparition a : zone.apparitions()) mobs.add(a.mob());
        }
        mobsCampagne = Set.copyOf(mobs);
        com.donopot.voiddynasty.VoidDynastyMod.LOGGER.info(
                "[campagne] {} zones · {} règles · {} mécanismes · {} réapparitions{}",
                CampagneDonnees.zones().size(), CampagneDonnees.regles().size(),
                CampagneDonnees.mecanismes().size(), CampagneDonnees.reapparitions().size(),
                CampagneDonnees.problemes().isEmpty() ? "" : " · PROBLEMES : " + CampagneDonnees.problemes());
        remettreAZero();
        active = false;
    }

    private static void remettreAZero() {
        compteurs.clear();
        reglesUneFois.clear();
        marqueurs.clear();
        appelsDifferes.clear();
        zonesExecutees.clear();
        reglesGlobalesPassees.clear();
        finDeclenchee = false;
        niveau = 0;
        invoquees = 0;
    }

    public static String demarrer(MinecraftServer serveur) {
        if (CampagneDonnees.zones().isEmpty()) {
            return "aucune zone chargee : " + CampagneDonnees.problemes();
        }
        var source = serveur.createCommandSourceStack().withSuppressedOutput();
        for (String regle : new String[]{"spawn_mobs false", "mob_griefing false", "keep_inventory true"}) {
            serveur.getCommands().performPrefixedCommand(source, "gamerule " + regle);
        }
        remettreAZero();
        poserMarqueurs(serveur.overworld());
        active = true;
        return "campagne demarree : " + CampagneDonnees.zones().size() + " zones · "
                + CampagneDonnees.regles().size() + " regles · " + CampagneDonnees.mecanismes().size()
                + " mecanismes · " + marqueurs.size() + " marqueurs poses";
    }

    public static void arreter() { active = false; }

    public static void reset() { remettreAZero(); }

    public static void tick(MinecraftServer serveur) {
        if (!active) return;
        ServerLevel niveau0 = serveur.overworld();
        // appels différés (les `schedule function` de la source)
        for (int i = appelsDifferes.size() - 1; i >= 0; i--) {
            Attente attente = appelsDifferes.get(i);
            attente.ticks -= 1;
            if (attente.ticks <= 0) {
                appelsDifferes.remove(i);
                declencherParId(niveau0, attente.chemin, null);
            }
        }
        // mécanismes frappés
        if (!marqueurs.isEmpty()) {
            for (Map.Entry<UUID, String> entree : new ArrayList<>(marqueurs.entrySet())) {
                Entity marqueur = niveau0.getEntity(entree.getKey());
                if (!(marqueur instanceof LivingEntity vivant)) continue;
                // la source teste `nbt={HurtTime:1s}` : c'est la frappe qui ouvre la porte
                if (vivant.hurtTime > 0) {
                    vivant.discard();
                    marqueurs.remove(entree.getKey());
                    ouvrirMecanisme(niveau0, entree.getValue());
                }
            }
        }
        TickCampagne tick = CampagneDonnees.tick();
        if (tick != null) {
            // règles sans position : seul le compteur décide
            cranPourTest(serveur);
            // règles sur entité : approcher un guide fait avancer la zone
            for (ServerPlayer joueur : serveur.getPlayerList().getPlayers()) {
                appliquerReglesEntite(niveau0, joueur.getX(), joueur.getY(), joueur.getZ());
            }
            // retour au point de spawn : la source relance la campagne
            if (tick.reinitialisation() != null) {
                double[] ou = tick.reinitialisation().position();
                for (ServerPlayer joueur : serveur.getPlayerList().getPlayers()) {
                    double dx = joueur.getX() - ou[0], dy = joueur.getY() - ou[1], dz = joueur.getZ() - ou[2];
                    if (dx * dx + dy * dy + dz * dz <= tick.reinitialisation().rayon()
                            * tick.reinitialisation().rayon()) {
                        remettreAZero();
                        poserMarqueurs(niveau0);
                        break;
                    }
                }
            }
        }
        tenirBarreReacteur(serveur);
        for (ServerPlayer joueur : serveur.getPlayerList().getPlayers()) {
            appliquerPowerArmour(joueur);
            if (joueur.isDeadOrDying()) {
                double[] point = pointReapparition();
                if (point != null) joueur.teleportTo(point[0], point[1], point[2]);
                continue;
            }
            evaluer(niveau0, joueur.getX(), joueur.getY(), joueur.getZ(), joueur);
        }
    }

    /**
     * Évalue toutes les règles pour une position donnée — c'est le cerveau de la campagne. La position est
     * passée en paramètre, pas prise d'un joueur : le harnais peut donc éprouver la campagne entière sans
     * client. Renvoie la description des règles déclenchées.
     */
    public static List<String> evaluer(ServerLevel monde, double x, double y, double z, ServerPlayer joueur) {
        List<String> declenchees = new ArrayList<>();
        List<RegleCampagne> regles = CampagneDonnees.regles();
        for (int i = 0; i < regles.size(); i++) {
            RegleCampagne regle = regles.get(i);
            if (regle.compteurValeur() == null && reglesUneFois.contains(i)) continue;
            double dx = x - regle.position()[0], dy = y - regle.position()[1], dz = z - regle.position()[2];
            if (dx * dx + dy * dy + dz * dz > regle.rayon() * regle.rayon()) continue;
            if ("sans_ennemi".equals(regle.condition()) && ennemisProches(monde, regle, x, y, z)) continue;
            if (regle.compteurZone() != null
                    && compteur(regle.compteurZone()) != regle.compteurValeur()) continue;
            reglesUneFois.add(i);
            for (String zone : regle.appelle()) {
                declencherParId(monde, zone, joueur);
            }
            for (String[] pose : regle.poseCompteur()) {
                compteurs.put(pose[0], Integer.parseInt(pose[1]));
            }
            for (int n : regle.poseNiveau()) niveau = n;
            declenchees.add(description(regle));
        }
        return declenchees;
    }

    private static String description(RegleCampagne regle) {
        return "regle " + (int) regle.position()[0] + " " + (int) regle.position()[1] + " "
                + (int) regle.position()[2] + " -> " + regle.appelle();
    }

    private static boolean ennemisProches(ServerLevel monde, RegleCampagne regle, double x, double y, double z) {
        double r = regle.rayon();
        AABB zone = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
        for (Entity e : monde.getEntities((Entity) null, zone, ent -> true)) {
            if (e instanceof Mob && mobsCampagne.contains(
                    BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath())) {
                return true;
            }
        }
        return false;
    }

    /** Déclenche une zone par son identifiant de source (« part2/part3/part4/reactor »). */
    public static int declencherParId(ServerLevel monde, String chemin, ServerPlayer joueur) {
        String id = idDepuisChemin(chemin);
        return CampagneDonnees.parId(id).map(zone -> executerZone(monde, zone, joueur)).orElseGet(() -> {
            problemes.add("zone inconnue : " + chemin);
            return -1;
        });
    }

    private static String idDepuisChemin(String chemin) {
        return chemin.replace("/", "_");
    }

    private static int executerZone(ServerLevel monde, ZoneCampagne zone) {
        return executerZone(monde, zone, null);
    }

    /** Exécute une zone : compteurs, niveau, répliques, guide, créatures, appels différés. */
    private static int executerZone(ServerLevel monde, ZoneCampagne zone, ServerPlayer joueur) {
        zonesExecutees.add(zone.zone());
        for (String[] pose : zone.poseCompteur()) compteurs.put(pose[0], Integer.parseInt(pose[1]));
        for (int n : zone.poseNiveau()) niveau = n;
        MinecraftServer serveur = monde.getServer();
        Component titre = Component.literal(zone.titres().isEmpty() ? zone.zone() : zone.titres().get(0));
        for (String replique : zone.repliques()) {
            if (serveur != null) {
                for (ServerPlayer p : serveur.getPlayerList().getPlayers()) {
                    p.sendSystemMessage(Component.literal(replique));
                }
            }
        }
        if (serveur != null) {
            for (ServerPlayer p : serveur.getPlayerList().getPlayers()) {
                p.sendSystemMessage(titre, true);
            }
        }
        for (ZoneCampagne.Waypoint point : zone.waypoints()) poserWaypoint(monde, point);
        for (ZoneCampagne.Objet objet : zone.objets()) poserRecompense(monde, objet);
        int compte = 0;
        for (ZoneCampagne.Apparition a : zone.apparitions()) {
            if (poserCreature(monde, a)) compte++;
        }
        invoquees += compte;
        for (ZoneCampagne.AppelDiffere differe : zone.appelsDifferes()) {
            appelsDifferes.add(new Attente(differe.ticks(), differe.chemin()));
        }
        return compte;
    }

    /** Pose un objet donné par la source, avec NOTRE objet correspondant. */
    private static void poserRecompense(ServerLevel monde, ZoneCampagne.Objet objet) {
        if (objet.objet() == null) {
            problemes.add("objet donne sans correspondance : " + objet.nom());
            return;
        }
        Item item = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath("voiddynasty", objet.objet()));
        if (item == null) {
            problemes.add("objet inconnu : " + objet.objet());
            return;
        }
        ItemEntity entite = new ItemEntity(monde, objet.x(), objet.y(), objet.z(), new ItemStack(item));
        entite.setPos(objet.x(), objet.y(), objet.z());
        monde.addFreshEntity(entite);
    }

    private static boolean poserCreature(ServerLevel monde, ZoneCampagne.Apparition a) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("voiddynasty", a.mob()));
        if (type == null) {
            problemes.add("entite inconnue : " + a.mob());
            return false;
        }
        Entity entite = type.spawn(monde, BlockPos.containing(a.x(), a.y(), a.z()),
                EntitySpawnReason.MOB_SUMMONED);
        if (entite == null) {
            problemes.add("apparition impossible : " + a.mob());
            return false;
        }
        entite.setPos(a.x(), a.y(), a.z());
        return true;
    }

    /** Le guide lumineux de la source : un shulker invisible, luisant, sans IA, à l'échelle 0,3. */
    private static void poserWaypoint(ServerLevel monde, ZoneCampagne.Waypoint point) {
        Shulker guide = EntityTypes.SHULKER.spawn(monde,
                BlockPos.containing(point.x(), point.y(), point.z()), EntitySpawnReason.MOB_SUMMONED);
        if (guide == null) return;
        guide.setPos(point.x(), point.y(), point.z());
        guide.setNoAi(true);
        guide.setSilent(true);
        guide.setInvisible(true);
        guide.setGlowingTag(true);
        guide.setPersistenceRequired();
        guide.setPermanentlyInvulnerable(true);
        for (String tag : point.tags()) guide.addTag(tag);
        if (guide.getAttribute(Attributes.SCALE) != null) guide.getAttribute(Attributes.SCALE).setBaseValue(0.3);
    }

    /** Pose les marqueurs des mécanismes : invisibles, à frapper, comme les shulkers de la source. */
    private static void poserMarqueurs(ServerLevel monde) {
        for (MecanismeCampagne mecanisme : CampagneDonnees.mecanismes()) {
            double[] centre = centre(mecanisme);
            if (centre == null) {
                problemes.add("mecanisme " + mecanisme.tag() + " : aucune region, position non deductible");
                continue;
            }
            Shulker marqueur = EntityTypes.SHULKER.spawn(monde,
                    BlockPos.containing(centre[0], centre[1], centre[2]), EntitySpawnReason.MOB_SUMMONED);
            if (marqueur == null) {
                problemes.add("marqueur impossible : " + mecanisme.tag());
                continue;
            }
            marqueur.setPos(centre[0], centre[1], centre[2]);
            marqueur.setNoAi(true);
            marqueur.setSilent(true);
            marqueur.setInvisible(true);
            marqueur.setPermanentlyInvulnerable(true);
            marqueur.setPersistenceRequired();
            marqueur.addTag(mecanisme.tag());
            marqueurs.put(marqueur.getUUID(), mecanisme.tag());
        }
    }

    /** Centre de la région ouverte par le mécanisme — une DÉDUCTION, déclarée comme telle. */
    private static double[] centre(MecanismeCampagne mecanisme) {
        if (mecanisme.regions().isEmpty()) return null;
        int[] r = mecanisme.regions().get(0);
        return new double[]{(r[0] + r[3]) / 2.0 + 0.5, (r[1] + r[4]) / 2.0 + 0.5, (r[2] + r[5]) / 2.0 + 0.5};
    }

    /** Ouvre un mécanisme : région vidée, son, zone appelée. */
    public static boolean ouvrirMecanisme(ServerLevel monde, String tag) {
        var trouve = CampagneDonnees.mecanisme(tag);
        if (trouve.isEmpty()) {
            problemes.add("mecanisme inconnu : " + tag);
            return false;
        }
        MecanismeCampagne mecanisme = trouve.get();
        if (mecanisme.conditionZone() != null
                && compteur(mecanisme.conditionZone()) < mecanisme.conditionValeur()) {
            return false;   // la source ne l'ouvre qu'à partir d'un certain avancement
        }
        for (int[] region : mecanisme.regions()) {
            for (BlockPos pos : BlockPos.betweenClosed(Math.min(region[0], region[3]),
                    Math.min(region[1], region[4]), Math.min(region[2], region[5]),
                    Math.max(region[0], region[3]), Math.max(region[1], region[4]),
                    Math.max(region[2], region[5]))) {
                // la source fait `fill ... air` : tout ce qui n'est pas de l'air est retiré
                if (!monde.getBlockState(pos).isAir()) {
                    monde.setBlockAndUpdate(pos.immutable(), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (String son : mecanisme.sons()) {
            monde.playSound(null, centre(mecanisme) == null ? 0 : centre(mecanisme)[0],
                    centre(mecanisme) == null ? 0 : centre(mecanisme)[1],
                    centre(mecanisme) == null ? 0 : centre(mecanisme)[2],
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        for (String chemin : mecanisme.appelle()) declencherParId(monde, chemin, null);
        if (mecanisme.compteRetire() != null) {
            compteurs.merge(mecanisme.compteRetire()[0], -Integer.parseInt(mecanisme.compteRetire()[2]),
                    Integer::sum);
        }
        return true;
    }

    public static double[] pointReapparition() {
        double[] point = CampagneDonnees.reapparitions().get(niveau);
        if (point != null) return point;
        return CampagneDonnees.reapparitions().entrySet().stream()
                .filter(e -> e.getKey() <= niveau)
                .max(Map.Entry.comparingByKey()).map(Map.Entry::getValue).orElse(null);
    }

    /** Réservé au harnais : éprouve la fin — compteur à zéro, puis la barre décide. */
    public static boolean eprouverFinPourTest(MinecraftServer serveur) {
        TickCampagne tick = CampagneDonnees.tick();
        if (tick == null || tick.reacteur() == null) return false;
        compteurs.put(tick.reacteur().compteur(), 0);
        tenirBarreReacteur(serveur);
        return finDeclenchee;
    }

    /** Réservé au harnais : où en est le compteur du cœur du réacteur ? */
    public static int compteurPourTest(String zone) { return compteur(zone); }

    /** Réservé au harnais : pose un compteur (la source part toujours de 0). */
    public static void poserCompteurPourTest(String zone, Integer valeur) {
        if (zone == null || valeur == null) return;
        compteurs.put(zone, valeur);
    }

    /** Réservé au harnais : remet les compteurs et l'état à zéro entre deux essais. */
    public static void remettreAZeroPourTest() { remettreAZero(); }

    /** Réservé au harnais : déclenche une zone par identifiant, sans joueur. */
    public static int declencherPourTest(ServerLevel monde, String id) {
        return CampagneDonnees.parId(id).map(z -> executerZone(monde, z)).orElse(-1);
    }

    /** Réservé au harnais : efface les créatures d'une zone de test. */
    public static void nettoyerPourTest(ServerLevel monde, double cx, double cz, double rayon) {
        AABB zone = new AABB(cx - rayon, monde.getMinY(), cz - rayon, cx + rayon, monde.getMaxY(), cz + rayon);
        monde.getEntities((Entity) null, zone, ent -> true).forEach(Entity::discard);
    }

    public static int marqueursPoses() { return marqueurs.size(); }

    /**
     * La Power Armour : la source regarde le casque du joueur et pose la vie maximale à 40.
     *
     * Elle ne regardait QU'UN objet (un casque de diamant) ; notre port a deux tenues complètes, et les
     * deux comptent comme armure de puissance. C'est une extension déclarée, pas un portage.
     */
    public static double pvMaxPour(ItemStack casque) {
        TickCampagne tick = CampagneDonnees.tick();
        if (tick == null || tick.powerArmour() == null) return 20.0;
        if (casque == null || casque.isEmpty()) return 20.0;
        boolean tenue = casque.is(com.donopot.voiddynasty.registry.ModArmor.HELMET)
                || casque.is(com.donopot.voiddynasty.registry.ModArmor.TERMINATOR_HELMET);
        return tenue ? tick.powerArmour().maxHealth() : 20.0;
    }

    private static void appliquerPowerArmour(ServerPlayer joueur) {
        var attribut = joueur.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (attribut == null) return;
        double cible = pvMaxPour(joueur.getItemBySlot(EquipmentSlot.HEAD));
        if (attribut.getBaseValue() != cible) {
            attribut.setBaseValue(cible);
            if (joueur.getHealth() > cible) joueur.setHealth((float) cible);
        }
    }

    /**
     * Les règles déclenchées par une entité-guide : un waypoint portant le tag de la source, et le
     * joueur à portée. On passe la POSITION — pas le joueur — pour que le harnais puisse l'éprouver.
     */
    private static void appliquerReglesEntite(ServerLevel monde, double x, double y, double z) {
        TickCampagne tick = CampagneDonnees.tick();
        if (tick == null) return;
        for (TickCampagne.RegleEntite regle : tick.reglesEntite()) {
            AABB autour = new AABB(x - regle.rayon(), y - regle.rayon(), z - regle.rayon(),
                    x + regle.rayon(), y + regle.rayon(), z + regle.rayon());
            boolean guide = !monde.getEntities((Entity) null, autour,
                    e -> e.entityTags().contains(regle.tag())).isEmpty();
            if (guide) compteurs.put(regle.poseCompteur()[0], Integer.parseInt(regle.poseCompteur()[1]));
        }
    }

    /** Réservé au harnais : joue un cran du cerveau par tick (règles sans position), sans joueur. */
    public static void cranPourTest(MinecraftServer serveur) {
        TickCampagne tick = CampagneDonnees.tick();
        if (tick == null) return;
        for (int i = 0; i < tick.reglesSansPosition().size(); i++) {
            TickCampagne.RegleGlobale regle = tick.reglesSansPosition().get(i);
            if (reglesGlobalesPassees.contains(i)) continue;
            if (compteur(regle.compteurZone()) == regle.compteurValeur()) {
                reglesGlobalesPassees.add(i);
                declencherParId(serveur.overworld(), regle.appelle(), null);
            }
        }
    }

    /** Réservé au harnais : pose un guide portant le tag, et éprouve la règle comme si on était dessus. */
    public static boolean eprouverRegleEntitePourTest(ServerLevel monde, String tag, double rayon) {
        Shulker guide = EntityTypes.SHULKER.spawn(monde, BlockPos.containing(0, 100, 0),
                EntitySpawnReason.MOB_SUMMONED);
        if (guide == null) return false;
        guide.addTag(tag);
        appliquerReglesEntite(monde, guide.getX(), guide.getY(), guide.getZ());
        boolean pose = CampagneDonnees.tick().reglesEntite().stream()
                .anyMatch(r -> r.tag().equals(tag) && compteur(r.poseCompteur()[0]) == Integer.parseInt(r.poseCompteur()[1]));
        guide.discard();
        return pose;
    }

    /** La barre du cœur du réacteur : elle suit son compteur, et zéro déclenche la fin. */
    private static void tenirBarreReacteur(MinecraftServer serveur) {
        TickCampagne tick = CampagneDonnees.tick();
        if (tick == null || tick.reacteur() == null || finDeclenchee) return;
        TickCampagne.Reacteur reacteur = tick.reacteur();
        if (!compteurs.containsKey(reacteur.compteur())) return;
        if (barreReacteur == null) {
            barreReacteur = new ServerBossEvent(UUID.randomUUID(),
                    Component.literal(reacteur.nom() == null ? reacteur.barre() : reacteur.nom()),
                    BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        }
        int valeur = compteur(reacteur.compteur());
        // la source pose « max 16 » et laisse la valeur suivre le compteur ; en 26.3 la barre se règle en
        // progression flottante, donc on donne le rapport — le même affichage qu'elle.
        barreReacteur.setProgress(Math.max(0f, Math.min(1f, valeur / (float) Math.max(1, reacteur.max()))));
        for (ServerPlayer joueur : serveur.getPlayerList().getPlayers()) barreReacteur.addPlayer(joueur);
        if (valeur <= reacteur.seuil()) declencherFin(serveur, reacteur);
    }

    /** L'écran de fin, transcrit de la zone de fin de la source. */
    private static void declencherFin(MinecraftServer serveur, TickCampagne.Reacteur reacteur) {
        finDeclenchee = true;
        ServerLevel monde = serveur.overworld();
        for (String son : reacteur.sons()) {
            SoundEvent evenement = BuiltInRegistries.SOUND_EVENT.getValue(
                    Identifier.withDefaultNamespace(son));
            if (evenement == null) {
                problemes.add("son introuvable : " + son);
                continue;
            }
            if (reacteur.teleport() != null) {
                monde.playSound(null, reacteur.teleport()[0], reacteur.teleport()[1], reacteur.teleport()[2],
                        evenement, SoundSource.HOSTILE, 1.0f, 1.0f);
            }
        }
        for (ServerPlayer joueur : serveur.getPlayerList().getPlayers()) {
            if (reacteur.titre() != null) joueur.sendSystemMessage(Component.literal(reacteur.titre()));
            if (reacteur.aveuglement()) {
                joueur.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 252, true, false));
            }
            if (reacteur.teleport() != null) {
                joueur.teleportTo(reacteur.teleport()[0], reacteur.teleport()[1], reacteur.teleport()[2]);
            }
        }
        // la source retire les guides, les interactions, les allays et le servo-crâne
        monde.getEntities((Entity) null, new AABB(-30000, monde.getMinY(), -30000, 30000, monde.getMaxY(), 30000),
                e -> e.entityTags().contains("waypoint")
                        || BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals("servo_skull"))
                .forEach(Entity::discard);
        if (reacteur.remetCompteur() != null) {
            compteurs.put(reacteur.remetCompteur()[0], Integer.parseInt(reacteur.remetCompteur()[2]));
        }
        if (barreReacteur != null) {
            barreReacteur.removeAllPlayers();
            barreReacteur = null;
        }
    }

    public static boolean finDeclenchee() { return finDeclenchee; }

    public static double valeurBarreReacteur() { return barreReacteur == null ? -1 : barreReacteur.getProgress(); }

    /** Réservé au harnais : la zone a-t-elle été exécutée ? */
    public static boolean zoneExecutee(String chemin) { return zonesExecutees.contains(chemin); }

    public static String etat() {
        int total = CampagneDonnees.zones().stream().mapToInt(z -> z.apparitions().size()).sum();
        return "campagne " + (active ? "en cours" : "arretee")
                + " · niveau " + niveau
                + " · compteurs " + compteurs.size()
                + " · creatures invoquees " + invoquees + "/" + total
                + " · marqueurs " + marqueurs.size()
                + (problemes.isEmpty() ? "" : " · problemes : " + problemes);
    }
}
