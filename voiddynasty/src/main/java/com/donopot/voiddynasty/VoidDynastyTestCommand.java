package com.donopot.voiddynasty;

import com.donopot.voiddynasty.entity.ChaosCultistEntity;
import com.donopot.voiddynasty.entity.ProfilAttaque;
import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.registry.ModBlocks;
import com.donopot.voiddynasty.registry.ModEntities;
import com.donopot.voiddynasty.registry.ModItems;
import com.donopot.voiddynasty.compat.ClaimIntegration;
import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import com.donopot.voiddynasty.terrain.TerrainDamageSpec;
import com.donopot.voiddynasty.weapon.BoltProjectile;
import com.donopot.voiddynasty.weapon.WeaponDefinition;
import com.donopot.voiddynasty.weapon.WeaponDefinitions;
import com.donopot.voiddynasty.weapon.WeaponService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande de test headless : {@code /vdyntest <scope>}.
 *
 * Elle journalise une ligne par contrôle, dans un format qu'un script peut lire :
 * <pre>
 *   [vdyntest] [PASS] B-melta-pierre — pierre détruite en 1 coup (attendu : 1)
 *   [vdyntest] [FAIL] B-laser-terre — 0 coup, cible intacte
 *   [vdyntest] ===== RÉSULTAT terrain : 8/9 PASS, 1 FAIL =====
 * </pre>
 *
 * Pourquoi une commande dans le mod plutôt qu'un test externe : un serveur headless n'a pas de
 * client, donc la seule façon d'exercer le vrai chemin de code est de le faire exécuter par le
 * serveur lui-même. C'est aussi ce qui rend le test rejouable après chaque modification.
 *
 * Règle de conception : chaque contrôle qui peut dire OUI doit avoir son pendant qui dit NON.
 * Un test qui n'échoue jamais ne prouve rien — d'où les paires « le melta casse la pierre » /
 * « le laser ne la casse pas », et « mur à découvert détruit » / « mur caché intact ».
 */
public final class VoidDynastyTestCommand {

    private VoidDynastyTestCommand() {}

    private static int pass;
    private static int fail;
    private static final List<String> echecs = new ArrayList<>();

    private static void check(String id, boolean ok, String detail) {
        if (ok) {
            pass++;
            VoidDynastyMod.LOGGER.info("[vdyntest] [PASS] {} — {}", id, detail);
        } else {
            fail++;
            echecs.add(id + " (" + detail + ")");
            VoidDynastyMod.LOGGER.error("[vdyntest] [FAIL] {} — {}", id, detail);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 26.3 : plus de hasPermission(int) — les permissions sont typées
        // (CommandSourceStack.permissions().hasPermission(Permissions.X))
        dispatcher.register(Commands.literal("vdyntest")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("registry").executes(ctx -> run(ctx, "registry")))
                .then(Commands.literal("props").executes(ctx -> run(ctx, "props")))
                .then(Commands.literal("weaponanim").executes(ctx -> run(ctx, "weaponanim")))
                .then(Commands.literal("pv").executes(ctx -> run(ctx, "pv")))
                .then(Commands.literal("profils").executes(ctx -> run(ctx, "profils")))
                .then(Commands.literal("butin").executes(ctx -> run(ctx, "butin")))
                .then(Commands.literal("campagne").executes(ctx -> run(ctx, "campagne")))
                .then(Commands.literal("minerai").executes(ctx -> run(ctx, "minerai")))
                .then(Commands.literal("terrain").executes(ctx -> run(ctx, "terrain")))
                .then(Commands.literal("vehicules").executes(ctx -> run(ctx, "vehicules")))
                .then(Commands.literal("equilibrage").executes(ctx -> run(ctx, "equilibrage")))
                .then(Commands.literal("spawn").executes(ctx -> run(ctx, "spawn")))
                .then(Commands.literal("all").executes(ctx -> run(ctx, "all"))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, String scope) {
        pass = 0;
        fail = 0;
        echecs.clear();
        try {
            if (scope.equals("registry") || scope.equals("all")) testerRegistre();
            if (scope.equals("props") || scope.equals("all")) testerProps(ctx.getSource().getLevel());
            if (scope.equals("weaponanim") || scope.equals("all")) testerAnimationsArmes();
            if (scope.equals("terrain") || scope.equals("all")) testerTerrain(ctx.getSource().getLevel());
            if (scope.equals("vehicules") || scope.equals("all")) testerVehicules(ctx.getSource().getLevel());
            if (scope.equals("equilibrage") || scope.equals("all")) testerEquilibrage(ctx.getSource().getLevel());
            if (scope.equals("spawn") || scope.equals("all")) testerApparitions(ctx.getSource().getLevel());
            if (scope.equals("pv") || scope.equals("all")) testerPvCultistes(ctx.getSource().getLevel());
            if (scope.equals("profils") || scope.equals("all")) testerProfilsAttaque(ctx.getSource().getLevel());
            if (scope.equals("butin") || scope.equals("all")) testerButin(ctx.getSource().getLevel());
            if (scope.equals("campagne") || scope.equals("all")) testerCampagne(ctx.getSource().getLevel());
            if (scope.equals("minerai") || scope.equals("all")) testerMinerai(ctx.getSource().getLevel());
        } catch (Throwable t) {
            check(scope + "-exception", false, "exception : " + t);
        }
        int total = pass + fail;
        VoidDynastyMod.LOGGER.info("[vdyntest] ===== RÉSULTAT {} : {}/{} PASS, {} FAIL =====",
                scope, pass, total, fail);
        for (String e : echecs) {
            VoidDynastyMod.LOGGER.error("[vdyntest]     échec : {}", e);
        }
        final int p = pass;
        final int f = fail;
        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                "[vdyntest] " + scope + " → " + p + " PASS, " + f + " FAIL"), false);
        return pass;
    }

    // ==================================================================
    // Registre : tout ce que le mod déclare doit être résolvable.
    // ==================================================================
    private static void testerRegistre() {
        WeaponDefinition[] armes = {
                WeaponDefinitions.BOLTGUN, WeaponDefinitions.BOLT_PISTOL, WeaponDefinitions.HEAVY_BOLTER,
                WeaponDefinitions.STALKER_BOLT_RIFLE, WeaponDefinitions.LAS_GUN, WeaponDefinitions.LAZ_PISTOL,
                WeaponDefinitions.PLASMA_PISTOL, WeaponDefinitions.PLASMA_INCINERATOR, WeaponDefinitions.SHOTGUN,
                WeaponDefinitions.FLAMETHROWER, WeaponDefinitions.MELTA_GUN,
                WeaponDefinitions.HUNTER_KILLER_MISSILE, WeaponDefinitions.CHAINSWORD,
                WeaponDefinitions.POWERSWORD, WeaponDefinitions.POWER_HAMMER};
        check("R-armes", armes.length == 15, armes.length + " définitions d'armes (attendu 15)");

        int sansTerrain = 0;
        int sansDegats = 0;
        for (WeaponDefinition a : armes) {
            if (!a.terrain().enabled() || a.terrain().impactPower() <= 0f) sansTerrain++;
            if (a.damage() <= 0f) sansDegats++;
        }
        check("R-terrain-declare", sansTerrain == 0, sansTerrain + " armes sans dégâts de terrain");
        check("R-degats-armes", sansDegats == 0, sansDegats + " armes à dégâts nuls");

        String[] entites = {"bolt_projectile", "laser_beam", "tyranid_warrior", "carnifex", "carnifex_leviathan",
                "lictor", "norn_emissary", "great_unclean_one", "lord_of_change", "keeper_of_secrets",
                "khorne_juggernaut", "large_khorne_daemon", "plaguetoad", "ghazghkull", "servo_skull"};
        List<String> absentes = new ArrayList<>();
        for (String id : entites) {
            EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("voiddynasty", id));
            if (t == null) absentes.add(id);
        }
        check("R-entites", absentes.isEmpty(), absentes.isEmpty()
                ? entites.length + " entités résolvables"
                : "introuvables : " + absentes);

        check("R-blocs", ModBlocks.BARREL != null && ModBlocks.CERAMITE_ORE != null,
                "blocs du mod instanciés");
    }

    // ==================================================================
    // Phase F — la campagne doit être CELLE DE LA SOURCE, couche par couche, et son moteur doit marcher.
    // La source pilote sa campagne par des commandes en dur ; on la transcrit en données. Les contrôles
    // comparent donc aux valeurs de la source, puis DÉCLENCHENT réellement : les zones, les règles et les
    // portes. Le parcours rejoue les 46 règles du cerveau de la campagne et compte celles qui répondent.
    // ==================================================================
    // ==================================================================
    // Phase H — le minerai de céramite doit VRAIMENT apparaître dans le monde.
    // Le monde de test est plat (aucune pierre) : on ne peut donc pas y faire naître un chunk pour
    // compter les blocs. On vérifie ce qui est en notre pouvoir — le filon est enregistré, il est lu
    // depuis le jar, et il est dans la MÊME liste, à la MÊME étape de génération, que le minerai de fer
    // vanilla. Le fer sert de témoin : s'il n'est pas là, c'est le contrôle qui ne prouve rien, et il
    // échoue au lieu de passer en silence.
    // ==================================================================
    private static void testerMinerai(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        var registres = level.registryAccess();
        var placement = registres.lookupOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE)
                .get(com.donopot.voiddynasty.worldgen.ModWorldGen.CERAMITE_ORE);
        if (placement.isEmpty()) {
            problemes.add("le filon de céramite n'est pas enregistré");
        } else {
            // Registry.get rend un Holder : c'est la valeur qui porte le placement
            var filon = placement.get().value();
            if (filon.placement().size() != 4) {
                problemes.add("le filon a " + filon.placement().size() + " modificateurs de placement au lieu de 4");
            }
            // la chaîne doit se résoudre : le placement doit pointer une feature réellement enregistrée
            if (filon.getFeatures().findAny().isEmpty()) {
                problemes.add("le placement ne résout aucune feature : le fichier ne chaîne pas");
            }
        }
        boolean notre = false, fer = false;
        var biomes = registres.lookupOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var plaines = biomes.get(net.minecraft.world.level.biome.Biomes.PLAINS);
        if (plaines.isEmpty()) {
            problemes.add("biome plains introuvable");
        } else {
            var etapes = plaines.get().value().getGenerationSettings().features();
            int etape = net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
            if (etape >= etapes.size()) {
                problemes.add("étape UNDERGROUND_ORES absente du biome plains");
            } else {
                for (net.minecraft.core.Holder<net.minecraft.world.level.levelgen.placement.PlacedFeature> h
                        : etapes.get(etape)) {
                    var cle = h.unwrapKey();
                    if (cle.isEmpty()) continue;
                    if (cle.get().equals(com.donopot.voiddynasty.worldgen.ModWorldGen.CERAMITE_ORE)) notre = true;
                    // témoin : un des trois placements du minerai de fer vanilla
                    for (String nom : new String[]{"ore_iron_middle", "ore_iron_upper", "ore_iron_small"}) {
                        if (cle.get().equals(net.minecraft.resources.ResourceKey.create(
                                net.minecraft.core.registries.Registries.PLACED_FEATURE,
                                net.minecraft.resources.Identifier.withDefaultNamespace(nom)))) {
                            fer = true;
                        }
                    }
                }
            }
        }
        if (!notre) problemes.add("le filon n'est pas dans les features du biome plains");
        if (!fer) problemes.add("témoin absent : le minerai de fer n'y est pas non plus, le contrôle ne prouverait rien");
        boolean fichier = level.getServer().getResourceManager().getResource(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("voiddynasty",
                        "worldgen/placed_feature/ceramite_ore.json")).isPresent();
        if (!fichier) problemes.add("le fichier de placement est absent du jar");

        // --- Décision de Donopot (16/10/2025) : le titane ne cède qu'à une pioche en DIAMANT.
        // Éprouvé par le COMPORTEMENT du jeu (`isCorrectToolForDrops`) et non par la lecture du tag :
        // le fichier dit ce qui est déclaré, le jeu dit ce qui arrive. Le témoin (minerai de fer
        // vanilla, qui se mine à la pioche en fer) prouve que ce contrôle sait faire la différence —
        // sans lui, une erreur de ma part ferait passer tout le monde pour conforme.
        ItemStack piocheFer = new ItemStack(Items.IRON_PICKAXE);
        ItemStack piocheDiamant = new ItemStack(Items.DIAMOND_PICKAXE);
        boolean temoinFer = piocheFer.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState());
        java.util.List<String> fautes = new java.util.ArrayList<>();
        for (Block minerai : new Block[]{ModBlocks.CERAMITE_ORE, ModBlocks.DEEPSLATE_CERAMITE_ORE}) {
            String nom = BuiltInRegistries.BLOCK.getKey(minerai).getPath();
            boolean auFer = piocheFer.isCorrectToolForDrops(minerai.defaultBlockState());
            boolean auDiamant = piocheDiamant.isCorrectToolForDrops(minerai.defaultBlockState());
            if (auFer) fautes.add(nom + " se mine encore à la pioche en fer");
            if (!auDiamant) fautes.add(nom + " résiste même à la pioche en diamant");
        }
        if (!temoinFer) fautes.add("témoin cassé : le minerai de fer vanilla devrait céder au fer");
        check("H-minerai-diamant", fautes.isEmpty(),
                fautes.isEmpty()
                        ? "les deux minerais de céramite exigent une pioche en DIAMANT (au fer : refusé, "
                        + "au diamant : accepté) — témoin vérifié : le minerai de fer vanilla cède au fer"
                        : String.join(" — ", fautes));

        // --- Variante « ardoise » (décision ⑤b) : le filon doit poser DEUX blocs, un par matériau.
        java.util.List<String> fautesArdoise = new java.util.ArrayList<>();
        if (!BuiltInRegistries.BLOCK.containsKey(
                Identifier.fromNamespaceAndPath("voiddynasty", "deepslate_ceramite_ore"))) {
            fautesArdoise.add("le bloc deepslate_ceramite_ore n'est pas enregistré");
        } else {
            var etatArdoise = ModBlocks.DEEPSLATE_CERAMITE_ORE.defaultBlockState();
            if (!etatArdoise.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)) {
                fautesArdoise.add("absent du tag mineable/pickaxe");
            }
            if (!etatArdoise.is(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL)) {
                fautesArdoise.add("absent du tag needs_diamond_tool");
            }
            if (etatArdoise.is(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL)) {
                fautesArdoise.add("encore déclaré dans needs_iron_tool : deux exigences contradictoires");
            }
            // la version pierre doit exiger la même chose, sinon les deux variantes du même minerai
            // ne se comportent pas pareil et le joueur ne comprend pas pourquoi
            if (!ModBlocks.CERAMITE_ORE.defaultBlockState().is(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL)
                    || ModBlocks.CERAMITE_ORE.defaultBlockState().is(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL)) {
                fautesArdoise.add("le minerai de pierre n'est pas aligné sur l'ardoise (diamant exigé, fer retiré)");
            }
        }
        check("H-minerai-ardoise", fautesArdoise.isEmpty(),
                fautesArdoise.isEmpty()
                        ? "bloc deepslate_ceramite_ore enregistré, mineable à la pioche et exigé au "
                        + "diamant, comme la version pierre (les deux exigences sont alignées)"
                        : String.join(" — ", fautesArdoise));

        check("H-minerai-genere", problemes.isEmpty(),
                problemes.isEmpty() ? "filon enregistré (4 modificateurs, chaîne résolue) · lu depuis le jar "
                        + "· présent dans les features du biome plains à l'étape UNDERGROUND_ORES, "
                        + "au même endroit que le minerai de fer (témoin) — le monde de test est plat, "
                        + "donc pas de comptage de blocs possible ici"
                        : String.join(" — ", problemes));
    }

    private static void testerCampagne(ServerLevel level) {
        testerZonesCampagne(level);
        testerReglesCampagne(level);
        testerPortesCampagne(level);
        testerParcoursCampagne(level);
        testerTickCampagne(level);
        testerRecompensesCampagne(level);
        testerPowerArmour(level);
    }

    // ------------------------------------------------------------------ le cerveau par tick
    private static void testerTickCampagne(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        var tick = com.donopot.voiddynasty.campagne.CampagneDonnees.tick();
        if (tick == null) {
            problemes.add("bloc tick absent des données");
        } else {
            if (tick.reglesSansPosition().size() != 2) {
                problemes.add(tick.reglesSansPosition().size() + " règles sans position au lieu de 2");
            }
            if (tick.reglesEntite().size() != 1) {
                problemes.add(tick.reglesEntite().size() + " règles sur entité au lieu de 1");
            } else if (!tick.reglesEntite().get(0).tag().equals("cryo2")
                    || tick.reglesEntite().get(0).rayon() != 3.0) {
                problemes.add("la règle sur entité attendue est cryo2 à 3 blocs, trouvée "
                        + tick.reglesEntite().get(0).tag() + " à " + tick.reglesEntite().get(0).rayon());
            }
            if (tick.reinitialisation() == null || tick.reinitialisation().position()[0] != 380
                    || tick.reinitialisation().position()[2] != -366) {
                problemes.add("le reset au spawn n'est pas au point 380 105 -366");
            }
            if (tick.reacteur() == null) {
                problemes.add("barre du réacteur absente");
            } else {
                var r = tick.reacteur();
                if (!"Reactor Core".equals(r.nom())) problemes.add("barre nommée « " + r.nom() + " » au lieu de « Reactor Core »");
                if (r.max() != 16) problemes.add("barre : max " + r.max() + " au lieu de 16");
                if (r.depart() != 4) problemes.add("barre : départ " + r.depart() + " au lieu de 4");
                if (r.seuil() != 0) problemes.add("barre : seuil de fin " + r.seuil() + " au lieu de 0");
                if (r.teleport() == null || r.teleport()[0] != 610 || r.teleport()[1] != 251 || r.teleport()[2] != -55) {
                    problemes.add("fin : téléportation " + java.util.Arrays.toString(r.teleport())
                            + " au lieu de [610, 251, -55]");
                }
                if (!"THANKS FOR PLAYING".equals(r.titre())) {
                    problemes.add("fin : titre « " + r.titre() + " » au lieu de « THANKS FOR PLAYING »");
                }
                if (r.sons().size() != 2 || !r.aveuglement()) {
                    problemes.add("fin : sons " + r.sons() + " · aveuglement " + r.aveuglement());
                }
                if (r.remetCompteur() == null || Integer.parseInt(r.remetCompteur()[2]) != 4) {
                    problemes.add("fin : le compteur n'est pas remis à 4");
                }
                // la fin ÉPROUVÉE : compteur à zéro, et on laisse la barre décider
                com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
                boolean finie = com.donopot.voiddynasty.campagne.CampagneService.eprouverFinPourTest(level.getServer());
                if (!finie) {
                    problemes.add("la fin ne s'est pas déclenchée avec le compteur à zéro");
                } else if (com.donopot.voiddynasty.campagne.CampagneService.compteurPourTest("reactorcore") != 4) {
                    problemes.add("après la fin, le compteur du cœur n'est pas revenu à 4");
                }
                com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
            }
        }
        check("F-campagne-tick", problemes.isEmpty(),
                problemes.isEmpty() ? "2 règles sans position · 1 règle sur entité (cryo2 à 3 blocs) · "
                        + "reset au spawn 380 105 -366 · barre « Reactor Core » max 16 départ 4 · "
                        + "fin téléporte en 610 251 -55 avec le titre de la source · fin RÉELLEMENT déclenchée"
                        : String.join(" — ", problemes));
    }

    // ------------------------------------------------------------------ les objets donnés
    private static void testerRecompensesCampagne(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        int total = 0, sansObjet = 0;
        java.util.Set<String> objets = new java.util.TreeSet<>();
        for (var zone : com.donopot.voiddynasty.campagne.CampagneDonnees.zones()) {
            for (var objet : zone.objets()) {
                total++;
                if (objet.objet() == null) {
                    sansObjet++;
                    problemes.add(zone.zone() + " : « " + objet.nom() + " » sans correspondance");
                } else {
                    objets.add(objet.objet());
                }
            }
        }
        if (total != 24) problemes.add(total + " objets donnés au lieu de 24");
        // pose RÉELLE : on déclenche l'armurerie et on compte les objets apparus
        com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
        com.donopot.voiddynasty.campagne.CampagneService.declencherPourTest(level, "armoury");
        int poses = 0;
        for (net.minecraft.world.entity.Entity e : level.getEntities((net.minecraft.world.entity.Entity) null,
                new net.minecraft.world.phys.AABB(180, 60, 40, 270, 110, 80),
                ent -> ent instanceof net.minecraft.world.entity.item.ItemEntity)) {
            poses++;
            e.discard();
        }
        if (poses == 0) problemes.add("l'armurerie n'a posé aucun objet au sol");
        com.donopot.voiddynasty.campagne.CampagneService.nettoyerPourTest(level, 250, 60, 40);
        com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
        check("F-campagne-recompenses", problemes.isEmpty(),
                problemes.isEmpty() ? total + " objets donnés, tous avec NOTRE objet ("
                        + objets.size() + " objets distincts) · armurerie éprouvée : " + poses
                        + " objet(s) posé(s) au sol" : String.join(" — ", problemes));
    }

    // ------------------------------------------------------------------ la Power Armour
    private static void testerPowerArmour(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        var tick = com.donopot.voiddynasty.campagne.CampagneDonnees.tick();
        if (tick == null || tick.powerArmour() == null) {
            problemes.add("la règle de Power Armour est absente des données");
        } else {
            if (tick.powerArmour().maxHealth() != 40.0) {
                problemes.add("vie maximale de la tenue : " + tick.powerArmour().maxHealth() + " au lieu de 40");
            }
            if (!"minecraft:diamond_helmet".equals(tick.powerArmour().objetSource())) {
                problemes.add("la source regardait " + tick.powerArmour().objetSource()
                        + " au lieu d'un casque de diamant");
            }
            double avec = com.donopot.voiddynasty.campagne.CampagneService.pvMaxPour(
                    new net.minecraft.world.item.ItemStack(com.donopot.voiddynasty.registry.ModArmor.HELMET));
            double sans = com.donopot.voiddynasty.campagne.CampagneService.pvMaxPour(
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
            double nu = com.donopot.voiddynasty.campagne.CampagneService.pvMaxPour(
                    net.minecraft.world.item.ItemStack.EMPTY);
            if (avec != 40.0) problemes.add("avec notre casque : " + avec + " PV au lieu de 40");
            if (sans != 20.0) problemes.add("sans notre casque : " + sans + " PV au lieu de 20");
            if (nu != 20.0) problemes.add("tête nue : " + nu + " PV au lieu de 20");
        }
        check("F-power-armour", problemes.isEmpty(),
                problemes.isEmpty() ? "notre casque → 40 PV · sans la tenue → 20 PV · tête nue → 20 PV "
                        + "(la source regardait un casque de diamant, nos deux tenues comptent — extension déclarée)"
                        : String.join(" — ", problemes));
    }

    private static void testerZonesCampagne(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        int zones = com.donopot.voiddynasty.campagne.CampagneDonnees.zones().size();
        if (zones != 59) {
            problemes.add(zones + " zones au lieu de 59");
        }
        var armoury = com.donopot.voiddynasty.campagne.CampagneDonnees.parId("armoury");
        if (armoury.isEmpty()) {
            problemes.add("zone armoury absente");
        } else {
            var z = armoury.get();
            if (z.apparitions().size() != 6) {
                problemes.add("armoury : " + z.apparitions().size() + " créatures au lieu de 6");
            }
            int apparues = com.donopot.voiddynasty.campagne.CampagneService.declencherPourTest(level, "armoury");
            if (apparues != 6) {
                problemes.add("déclenchement d'armoury : " + apparues + " créatures au lieu de 6");
            }
            if (com.donopot.voiddynasty.campagne.CampagneService.compteur("armoury") != 1) {
                problemes.add("armoury n'a pas posé son compteur à 1 (la source le fait)");
            }
            // la FONCTION d'armurerie pose le niveau 2 ; c'est la RÈGLE de leveldetect qui pose le 1
            if (com.donopot.voiddynasty.campagne.CampagneService.niveauCourant() != 2) {
                problemes.add("armoury n'a pas posé le niveau 2 (la source le fait dans armoury.mcfunction)");
            }
            com.donopot.voiddynasty.campagne.CampagneService.nettoyerPourTest(level, 250, 60, 40);
        }
        double[] reapparition = com.donopot.voiddynasty.campagne.CampagneDonnees.reapparitions()
                .getOrDefault(2, null);
        if (reapparition == null || reapparition[0] != 264 || reapparition[1] != 89 || reapparition[2] != 55) {
            problemes.add("réapparition du niveau 2 : " + java.util.Arrays.toString(reapparition)
                    + " au lieu de [264, 89, 55]");
        }
        com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
        if (!com.donopot.voiddynasty.campagne.CampagneDonnees.problemes().isEmpty()) {
            problemes.add("lecture : " + com.donopot.voiddynasty.campagne.CampagneDonnees.problemes());
        }
        check("F-campagne-zones", problemes.isEmpty(),
                problemes.isEmpty() ? "59 zones · armoury conforme (6 créatures, compteur 1, niveau 2) "
                        + "· réapparition du niveau 2 = 264 89 55 · déclenchement réel vérifié"
                        : String.join(" — ", problemes));
    }

    private static void testerReglesCampagne(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        var regles = com.donopot.voiddynasty.campagne.CampagneDonnees.regles();
        if (regles.size() != 46) {
            problemes.add(regles.size() + " règles au lieu de 46");
        }
        int entrees = 0, nettoyees = 0;
        for (var r : regles) {
            if ("joueur_proche".equals(r.condition())) entrees++;
            if ("sans_ennemi".equals(r.condition())) nettoyees++;
        }
        if (entrees != 44) problemes.add(entrees + " règles d'entrée au lieu de 44");
        if (nettoyees != 2) problemes.add(nettoyees + " règles « zone nettoyée » au lieu de 2");
        // la première règle de la campagne, telle que la source l'écrit (leveldetect.mcfunction, ligne 1)
        var premiere = regles.stream().filter(r -> r.position()[0] == 316 && r.position()[1] == 90
                && r.position()[2] == 111).findFirst().orElse(null);
        if (premiere == null) {
            problemes.add("règle de la cryo-salle (316 90 111) absente");
        } else {
            if (!"joueur_proche".equals(premiere.condition()) || premiere.rayon() != 2.0) {
                problemes.add("cryo-salle : condition " + premiere.condition() + " rayon " + premiere.rayon()
                        + " au lieu de joueur_proche rayon 2");
            }
            if (!"cryoroom".equals(premiere.compteurZone()) || premiere.compteurValeur() != 0) {
                problemes.add("cryo-salle : compteur " + premiere.compteurZone() + "="
                        + premiere.compteurValeur() + " au lieu de cryoroom=0");
            }
            if (!premiere.appelle().contains("cryoroom")) {
                problemes.add("cryo-salle n'appelle pas levels/cryoroom : " + premiere.appelle());
            }
        }
        check("F-campagne-regles", problemes.isEmpty(),
                problemes.isEmpty() ? "46 règles (44 d'entrée + 2 « zone nettoyée ») · la cryo-salle "
                        + "conforme à leveldetect.mcfunction ligne 1" : String.join(" — ", problemes));
    }

    private static void testerPortesCampagne(ServerLevel level) {
        java.util.List<String> problemes = new java.util.ArrayList<>();
        var mecanismes = com.donopot.voiddynasty.campagne.CampagneDonnees.mecanismes();
        if (mecanismes.size() != 16) {
            problemes.add(mecanismes.size() + " mécanismes au lieu de 16");
        }
        var door1 = com.donopot.voiddynasty.campagne.CampagneDonnees.mecanisme("door1").orElse(null);
        if (door1 == null) {
            problemes.add("porte door1 absente");
        } else {
            int[] region = door1.regions().isEmpty() ? null : door1.regions().get(0);
            if (region == null || !java.util.Arrays.equals(region, new int[]{284, 65, 78, 280, 70, 78})) {
                problemes.add("door1 : région " + java.util.Arrays.toString(region) + " au lieu de 284 65 78 280 70 78");
            }
            if (!door1.sons().contains("block.beacon.deactivate")) {
                problemes.add("door1 : son " + door1.sons() + " au lieu de block.beacon.deactivate");
            }
            if (!door1.appelle().contains("cryoroom7")) {
                problemes.add("door1 n'appelle pas cryoroom7 : " + door1.appelle());
            }
        }
        // ouverture RÉELLE : on pose le verre, on ouvre, on vérifie que la région est vidée
        if (door1 != null && !door1.regions().isEmpty()) {
            int[] r = door1.regions().get(0);
            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                    Math.min(r[0], r[3]), Math.min(r[1], r[4]), Math.min(r[2], r[5]),
                    Math.max(r[0], r[3]), Math.max(r[1], r[4]), Math.max(r[2], r[5]))) {
                level.setBlockAndUpdate(pos.immutable(),
                        net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            }
            com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
            boolean ouverte = com.donopot.voiddynasty.campagne.CampagneService.ouvrirMecanisme(level, "door1");
            if (!ouverte) {
                problemes.add("door1 n'a pas pu s'ouvrir");
            } else {
                int restants = 0;
                for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                        Math.min(r[0], r[3]), Math.min(r[1], r[4]), Math.min(r[2], r[5]),
                        Math.max(r[0], r[3]), Math.max(r[1], r[4]), Math.max(r[2], r[5]))) {
                    if (!level.getBlockState(pos).isAir()) restants++;
                }
                if (restants != 0) {
                    problemes.add("door1 ouverte mais " + restants + " bloc de verre restant(s)");
                }
                if (!com.donopot.voiddynasty.campagne.CampagneService.zoneExecutee("cryoroom7")) {
                    problemes.add("door1 ouverte sans déclencher cryoroom7");
                }
            }
            com.donopot.voiddynasty.campagne.CampagneService.nettoyerPourTest(level, 282, 74, 30);
        }
        com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
        check("F-portes-source", problemes.isEmpty(),
                problemes.isEmpty() ? "16 mécanismes · door1 conforme (région 284 65 78 → 280 70 78, son, "
                        + "cryoroom7) · ouverture réelle vérifiée (verre retiré)" : String.join(" — ", problemes));
    }

    private static void testerParcoursCampagne(ServerLevel level) {
        java.util.List<String> echecs = new java.util.ArrayList<>();
        int ok = 0, total = 0;
        if (com.donopot.voiddynasty.campagne.CampagneDonnees.regles().isEmpty()) {
            echecs.add("aucune règle chargée : le parcours ne prouverait rien");
        }
        for (var regle : com.donopot.voiddynasty.campagne.CampagneDonnees.regles()) {
            total++;
            com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
            com.donopot.voiddynasty.campagne.CampagneService.poserCompteurPourTest(
                    regle.compteurZone(), regle.compteurValeur());
            java.util.List<String> declenchees = com.donopot.voiddynasty.campagne.CampagneService.evaluer(
                    level, regle.position()[0], regle.position()[1], regle.position()[2], null);
            boolean repond = !declenchees.isEmpty();
            if (repond) {
                ok++;
            } else {
                echecs.add((int) regle.position()[0] + " " + (int) regle.position()[1] + " "
                        + (int) regle.position()[2] + " (" + regle.condition() + ", "
                        + regle.compteurZone() + "=" + regle.compteurValeur() + ")");
            }
            com.donopot.voiddynasty.campagne.CampagneService.nettoyerPourTest(
                    level, regle.position()[0], regle.position()[2], 40);
        }
        // les règles du cerveau par tick : sans position, et sur entité
        var tick = com.donopot.voiddynasty.campagne.CampagneDonnees.tick();
        if (tick != null) {
            for (var regle : tick.reglesSansPosition()) {
                total++;
                com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
                com.donopot.voiddynasty.campagne.CampagneService.poserCompteurPourTest(
                        regle.compteurZone(), regle.compteurValeur());
                com.donopot.voiddynasty.campagne.CampagneService.cranPourTest(level.getServer());
                if (com.donopot.voiddynasty.campagne.CampagneService.zoneExecutee(regle.appelle())) {
                    ok++;
                } else {
                    echecs.add("sans position : " + regle.compteurZone() + "=" + regle.compteurValeur()
                            + " → " + regle.appelle());
                }
            }
            for (var regle : tick.reglesEntite()) {
                total++;
                com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
                boolean pose = com.donopot.voiddynasty.campagne.CampagneService.eprouverRegleEntitePourTest(
                        level, regle.tag(), regle.rayon());
                if (pose) {
                    ok++;
                } else {
                    echecs.add("sur entité : tag " + regle.tag() + " à " + regle.rayon() + " blocs");
                }
            }
        }
        com.donopot.voiddynasty.campagne.CampagneService.remettreAZeroPourTest();
        check("F-campagne-parcours", echecs.isEmpty() && total > 0,
                echecs.isEmpty() ? ok + "/" + total + " règles répondent (position, compteur, entité) "
                        + "comme la source les déclenche" : ok + "/" + total + " règles — muettes : "
                        + String.join(", ", echecs));
    }

    // ==================================================================
    // Phase E — TOUTES les tables de butin doivent se charger ET produire.
    // Une table mal écrite ne provoque AUCUNE erreur visible : le mob ne laisse simplement rien, et
    // personne ne s'en aperçoit avant de le tuer cent fois. On lit donc chaque table dans le registre
    // du serveur — c'est le même registre que celui interrogé par la commande /loot — et on la roule
    // pour de vrai. Vérifié en direct par RCON avant d'écrire ce contrôle (« Dropped 3 items »).
    // ==================================================================
    private static void testerButin(ServerLevel level) {
        String[] tables = {
                "objects/cultist_death",
                "ammo_crate/ammo_tier_1", "ammo_crate/ammo_tier_2", "ammo_crate/ammo_tier_3",
                "ammo_crate/ammo_tier_4", "ammo_crate/ammo_tier_5", "ammo_crate/ammo_tier_6",
                "entities/cultist_1", "entities/cultist_2", "entities/cultist_3", "entities/cultist_4",
                "entities/chaos_marine_1", "entities/chaos_marine_2", "entities/chaos_marine_3",
                "entities/chaos_raptor", "entities/servitor_1", "entities/servitor_2", "entities/servitor_3",
                "entities/chaos_captain", "entities/chaos_dreadnought", "entities/chaos_hellbrute",
                "entities/daemon_prince",
                // décidées hors source (références mortes) : voir generer_tables_loot.py
                "entities/cultist_boss", "entities/chaos_forgefiend",
        };
        // Les objets bruts de la source ne doivent JAMAIS tomber tels quels : leur présence signifierait
        // que la correspondance vers nos vraies munitions n'a pas été appliquée.
        java.util.Set<String> interdits = java.util.Set.of(
                "minecraft:iron_nugget", "minecraft:gold_nugget", "minecraft:coal",
                "minecraft:carrot_on_a_stick");
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY);
        List<String> problemes = new ArrayList<>();
        int bonnes = 0;
        for (String chemin : tables) {
            net.minecraft.world.level.storage.loot.LootTable table = level.getServer().reloadableRegistries()
                    .getLootTable(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.LOOT_TABLE,
                            Identifier.fromNamespaceAndPath("voiddynasty", chemin)));
            if (table == net.minecraft.world.level.storage.loot.LootTable.EMPTY) {
                problemes.add(chemin + " (absente du registre)");
                continue;
            }
            List<net.minecraft.world.item.ItemStack> tombes = new ArrayList<>();
            table.getRandomItems(params, tombes::add);
            if (tombes.isEmpty()) {
                problemes.add(chemin + " (ne produit rien)");
                continue;
            }
            List<String> fautifs = tombes.stream()
                    .map(i -> BuiltInRegistries.ITEM.getKey(i.getItem()).toString())
                    .filter(interdits::contains).distinct().toList();
            if (!fautifs.isEmpty()) {
                problemes.add(chemin + " (objets bruts de la source : " + fautifs + ")");
            } else {
                bonnes++;
            }
        }
        check("E-butin-source", problemes.isEmpty(),
                bonnes + "/" + tables.length + " tables chargées et productives"
                        + (problemes.isEmpty() ? "" : " — " + problemes));
    }


    // ==================================================================
    // Phase E — les profils d'attaque doivent être CEUX DE LA SOURCE.
    // La source donne à chaque mob une plage de tir, une plage de contact et un cooldown pour chacune.
    // Recopier ces valeurs à la main dans treize classes, c'est treize occasions de se tromper en
    // silence : ce contrôle les relit depuis le code et les compare.
    // ==================================================================
    private static void testerProfilsAttaque(ServerLevel level) {
        // {mob, tirMax attendu, cdTir attendu (ticks), cacMax attendu, cdCac attendu (ticks)}
        String[][] attendu = {
                {"cultist_1", "25", "30", "0", "0"},      // tir 0..25, CD 1.5s
                {"cultist_2", "10", "20", "3", "20"},     // tir 5..10 CD 1s ; càc 0..3 CD 1s
                {"cultist_3", "6", "40", "2", "40"},      // tir 4..6 CD 2s ; càc 0..2 CD 2s
                {"cultist_4", "0", "0", "3", "40"},       // càc 0..3 CD 2s
                {"cultist_boss", "10", "60", "4", "60"},  // tir 4..10 CD 3s ; càc 2..4
                {"chaos_marine_1", "30", "10", "0", "0"}, // tir 0..30 CD 0.5s
                {"chaos_marine_3", "0", "0", "3", "20"},  // càc 0..3 CD 1s
                {"chaos_captain", "15", "40", "3", "20"},
                {"chaos_raptor", "15", "30", "3", "20"},
                {"servitor_1", "30", "20", "0", "0"},     // tir 0..30 CD 1s
                {"servitor_2", "0", "0", "2", "20"},      // càc 0..2 CD 1s
                {"servitor_3", "0", "0", "3", "60"},      // càc 0..3 CD 3s
                // lot 4 : machines
                {"chaos_dreadnought", "20", "60", "2", "40"},   // tir 3..20 CD 3s ; càc 0..2 CD 2s
                {"chaos_hellbrute", "0", "0", "10", "40"},      // càc 0..10 CD 2s, aucun tir
                {"chaos_forgefiend", "100", "10", "6", "60"},   // tir 7..100 CD 0,5s ; càc 0..6 CD 3s
                // lot 5 : frappe 3..5 CD 1s ; charge et smash ont leur propre comportement
                {"daemon_prince", "0", "0", "5", "20"},
        };
        List<String> ecarts = new ArrayList<>();
        int bons = 0;
        for (int i = 0; i < attendu.length; i++) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("voiddynasty", attendu[i][0]));
            if (type == null) {
                ecarts.add(attendu[i][0] + " (type introuvable)");
                continue;
            }
            Entity e = type.spawn(level, new BlockPos(6 + (i % 4) * 2, 100, 30 + (i / 4) * 2),
                    EntitySpawnReason.MOB_SUMMONED);
            if (!(e instanceof ChaosCultistEntity mob)) {
                ecarts.add(attendu[i][0] + " (n'hérite pas de la base des ennemis du Chaos)");
                if (e != null) e.discard();
                continue;
            }
            ProfilAttaque profil = mob.profil();
            boolean tire_bon = Math.abs(profil.tirMax() - Double.parseDouble(attendu[i][1])) < 0.01
                    && profil.tirCooldownTicks() == Integer.parseInt(attendu[i][2]);
            boolean cac_bon = Math.abs(profil.cacMax() - Double.parseDouble(attendu[i][3])) < 0.01
                    && profil.cacCooldownTicks() == Integer.parseInt(attendu[i][4]);
            if (tire_bon && cac_bon) {
                bons++;
            } else {
                ecarts.add(attendu[i][0] + " : tir max " + profil.tirMax() + "/CD " + profil.tirCooldownTicks()
                        + " (attendu " + attendu[i][1] + "/" + attendu[i][2] + "), contact max "
                        + profil.cacMax() + "/CD " + profil.cacCooldownTicks()
                        + " (attendu " + attendu[i][3] + "/" + attendu[i][4] + ")");
            }
            e.discard();
        }
        check("E-profils-source", ecarts.isEmpty(),
                bons + "/" + attendu.length + " profils conformes à la source"
                        + (ecarts.isEmpty() ? "" : " — " + ecarts));
    }

    // ==================================================================
    // Phase E, lot 1 — les PV des cultistes doivent être CEUX DE LA SOURCE.
    // Un portage silencieusement faux (valeur par défaut, attribut oublié) ne se voit qu'en jeu ;
    // ici il casse le test. Valeurs : datapacks Astartes (20 pour les cultistes, 100 pour la boss).
    // ==================================================================
    private static void testerPvCultistes(ServerLevel level) {
        String[][] attendu = {{"cultist_1", "20"}, {"cultist_2", "20"}, {"cultist_3", "20"},
                {"cultist_4", "20"}, {"cultist_boss", "100"},
                // lot 2 : Spaces Marines du Chaos (source : 50 PV, capitaine 250)
                {"chaos_marine_1", "50"}, {"chaos_marine_2", "50"}, {"chaos_marine_3", "50"},
                {"chaos_captain", "250"}, {"chaos_raptor", "50"},
                // lot 3 : servitors (valeurs exactes, pas des fourchettes)
                {"servitor_1", "200"}, {"servitor_2", "200"}, {"servitor_3", "100"},
                // lot 4 : machines (source : 400 pour le forgefiend, 150 pour les deux autres)
                {"chaos_dreadnought", "150"}, {"chaos_hellbrute", "150"}, {"chaos_forgefiend", "400"},
                // lot 5 : le boss final
                {"daemon_prince", "1000"}, {"chaos_helldrake", "500"}};
        List<String> ecarts = new ArrayList<>();
        int bons = 0;
        for (int i = 0; i < attendu.length; i++) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("voiddynasty", attendu[i][0]));
            if (type == null) {
                ecarts.add(attendu[i][0] + " (type introuvable)");
                continue;
            }
            Entity e = type.spawn(level, new BlockPos(6 + (i % 4) * 2, 100, 20 + (i / 4) * 2),
                    EntitySpawnReason.MOB_SUMMONED);
            if (!(e instanceof LivingEntity vivant)) {
                ecarts.add(attendu[i][0] + " (non vivant)");
                continue;
            }
            float max = vivant.getMaxHealth();
            if (Math.abs(max - Float.parseFloat(attendu[i][1])) > 0.01f) {
                ecarts.add(attendu[i][0] + " : " + max + " au lieu de " + attendu[i][1]);
            } else {
                bons++;
            }
            e.discard();
        }
        check("E-pv-source", ecarts.isEmpty(),
                bons + "/" + attendu.length + " PV conformes à la source" + (ecarts.isEmpty() ? "" : " — " + ecarts));
    }

    // ==================================================================
    // Phase D — marqueurs d'animation d'arme : cycle de vie complet, et contrôles NÉGATIFS.
    // Testable sans client ni joueur : l'état vit sur l'objet, pas sur le joueur.
    // ==================================================================
    private static void testerAnimationsArmes() {
        List<String> problemes = new ArrayList<>();
        ItemStack arme = new ItemStack(ModItems.BOLTGUN);

        // 1. aucun marqueur au départ : un objet neuf ne doit pas être « en recul »
        if (arme.get(ModDataComponents.RECOIL) != null) problemes.add("marqueur présent sur un objet neuf");
        if (WeaponService.tickMarqueurs(arme) != 0) problemes.add("des marqueurs retirés alors qu'il n'y en a pas");

        // 2. le tir pose le recul... (appel direct du marqueur : le chemin de tir complet exige un joueur)
        WeaponService.marquerRecul(arme);
        Integer apres = arme.get(ModDataComponents.RECOIL);
        if (apres == null || apres != WeaponService.RECUL_TICKS) {
            problemes.add("recul non posé (attendu " + WeaponService.RECUL_TICKS + ", obtenu " + apres + ")");
        }
        if (WeaponService.RECUL_TICKS <= 1) problemes.add("recul trop court pour être visible");

        // 3. il se décrémente puis DISPARAÎT (et ne reste pas bloqué à 0)
        int retires = 0;
        for (int i = 0; i < WeaponService.RECUL_TICKS + 2; i++) retires += WeaponService.tickMarqueurs(arme);
        if (retires != 1) problemes.add("marqueur retiré " + retires + " fois au lieu d'une");
        if (arme.get(ModDataComponents.RECOIL) != null) problemes.add("marqueur de recul encore présent après expiration");

        // 4. rechargement : posé puis expiré de la même façon
        WeaponService.marquerRechargement(arme, 25);
        Integer rech = arme.get(ModDataComponents.RELOADING);
        if (rech == null || rech != 25) problemes.add("rechargement non posé (obtenu " + rech + ")");
        for (int i = 0; i < 30; i++) WeaponService.tickMarqueurs(arme);
        if (arme.get(ModDataComponents.RELOADING) != null) problemes.add("marqueur de rechargement encore présent");

        // 5. CONTRÔLE NÉGATIF : une valeur aberrante ne doit pas produire un marqueur immortel
        WeaponService.marquerRechargement(arme, 0);
        if (arme.get(ModDataComponents.RELOADING) == null) problemes.add("durée 0 non corrigée en durée minimale");
        for (int i = 0; i < 3; i++) WeaponService.tickMarqueurs(arme);
        if (arme.get(ModDataComponents.RELOADING) != null) problemes.add("marqueur de durée 0 non expiré");

        check("D-animations-armes", problemes.isEmpty(),
                problemes.isEmpty()
                        ? "recul " + WeaponService.RECUL_TICKS + " ticks posé/expiré, rechargement posé/expiré, cas limites OK"
                        : String.join(" ; ", problemes));
    }

    // ==================================================================
    // Props décoratifs (phase C) : déclarés ET réellement posables.
    // ==================================================================
    private static void testerProps(ServerLevel level) {
        String[] props = {"crate", "crate_green", "crate_red", "crate_shut", "gothic_panel",
                "corpse_1", "corpse_2", "corpse_3", "corpse_4", "corpse_5", "corpse_6",
                "blood_stain", "blood_arrow", "blast_mark", "blood_god",
                "spent_shell", "battery_pack", "wax_seal_prop", "corpse_starch",
                "laz_cannon", "multimelta_turret"};
        List<String> problemes = new ArrayList<>();
        int poses = 0;
        for (int i = 0; i < props.length; i++) {
            Identifier id = Identifier.fromNamespaceAndPath("voiddynasty", props[i]);
            Block bloc = BuiltInRegistries.BLOCK.getValue(id);
            if (bloc == null) {
                problemes.add(props[i] + " (bloc non enregistré)");
                continue;
            }
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (!(item instanceof BlockItem)) {
                problemes.add(props[i] + " (pas d'objet posable)");
                continue;
            }
            // le vrai test : le poser et vérifier qu'il tient
            BlockPos p = new BlockPos(30 + (i % 7) * 2, 100, 30 + (i / 7) * 2);
            level.setBlock(p, bloc.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockState(p).isAir()) {
                problemes.add(props[i] + " (posé puis disparu)");
            } else {
                poses++;
            }
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);   // nettoyage
        }
        check("C-props-posables", problemes.isEmpty(),
                poses + "/" + props.length + " props posés"
                        + (problemes.isEmpty() ? "" : " — problèmes : " + problemes));
    }

    // ==================================================================
    // Dégâts de terrain : la preuve attendue pour la phase B.
    // ==================================================================
    // ==================================================================
    // VEHICULES — decision de Donopot du 16/10/2025 : « je veux du pilotable ».
    //
    // Ce que ce controle prouve : le type est enregistre avec le gabarit mesure du modele, les trois
    // fichiers du rendu sont la, et les crochets de conduite sont declares DANS la classe du char.
    // Ce dernier point est le coeur du pilotage : une methode heritee fonctionnerait sans rien faire
    // de ce qu'on veut — le char serait un objet decoratif qui ne repond pas.
    //
    // Ce qu'il NE prouve PAS, et je le dis plutot que de le laisser croire : la SENSATION de
    // conduite. Un harnais headless n'a pas de joueur a faire monter dessus — monter, piloter,
    // tourner, descendre sont des gestes qui se jugent manette en main. Le controle verifie le
    // branchement, pas l'agrement.
    private static void testerVehicules(ServerLevel level) {
        java.util.List<String> fautes = new java.util.ArrayList<>();
        var cle = Identifier.fromNamespaceAndPath("voiddynasty", "lemunruss");
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(cle)) {
            fautes.add("le type d'entite lemunruss n'est pas enregistre");
        } else if (Math.abs(ModEntities.LEMAN_RUSS.getWidth() - 4.77f) > 0.02f
                || Math.abs(ModEntities.LEMAN_RUSS.getHeight() - 4.42f) > 0.02f) {
            fautes.add("gabarit " + ModEntities.LEMAN_RUSS.getWidth() + "x"
                    + ModEntities.LEMAN_RUSS.getHeight()
                    + " au lieu de 3,9x1,9 (modele livre : 1,98 x 1,83 x 3,90 blocs, longueur "
                    + "etiree de 30 % pour se rapprocher de la reference de Donopot)");
        }
        // Les trois fichiers du rendu. Verifies par le CLASSPATH du mod et non par le gestionnaire
        // de ressources du serveur : celui-ci ne voit que les data/ (c'est ainsi qu'il est construit
        // pour un serveur), donc un fichier d'assets y paraitrait toujours absent — un controle qui
        // dit « absent » pour tout ne prouve rien. Le classpath, lui, est celui du mod : s'il y est,
        // le fichier voyage avec le mod, en developpement comme dans le jar.
        for (String chemin : new String[]{"geckolib/models/lemunruss.geo.json",
                "geckolib/animations/lemunruss.animation.json", "textures/entity/lemunruss.png"}) {
            if (VoidDynastyTestCommand.class.getResource("/assets/voiddynasty/" + chemin) == null) {
                fautes.add(chemin + " ne voyage pas avec le mod");
            }
        }
        // les crochets de conduite, declares dans SA classe
        try {
            var classe = Class.forName("com.donopot.voiddynasty.entity.LemanRussEntity");
            var declarees = new java.util.HashSet<String>();
            for (var m : classe.getDeclaredMethods()) {
                declarees.add(m.getName());
            }
            for (String attendue : new String[]{"getControllingPassenger", "getRiddenInput",
                    "getRiddenSpeed", "mobInteract", "canAddPassenger", "getPassengerAttachmentPoint"}) {
                if (!declarees.contains(attendue)) {
                    fautes.add("« " + attendue + " » n'est pas declaree dans la classe du char : "
                            + "declaree ailleurs, elle ne ferait rien de ce qu'on veut");
                }
            }
            // et le comportement de base, mesure sur une vraie instance
            var charVerif = (net.minecraft.world.entity.PathfinderMob) classe
                    .getConstructor(net.minecraft.world.entity.EntityType.class, net.minecraft.world.level.Level.class)
                    .newInstance(ModEntities.LEMAN_RUSS, level);
            if (charVerif.getControllingPassenger() != null) {
                fautes.add("un char SANS conducteur designe deja quelqu'un comme conducteur");
            }
            if (charVerif.removeWhenFarAway(1000.0)) {
                fautes.add("le char disparait quand le joueur s'eloigne : un vehicule gare s'efface");
            }
            if (charVerif.isPushable()) {
                fautes.add("le char se laisse pousser");
            }
        } catch (ReflectiveOperationException e) {
            fautes.add("impossible d'instancier le char : " + e);
        }
        // --- LE TIR, eprouve sur une vraie instance dans le monde ---
        // Decision de Donopot : « le char est pilotable et on peut tirer ». Un tir se prouve en
        // verifiant qu'un projectile apparait, et qu'une cadence l'empeche de mitrailler.
        var charTir = ModEntities.LEMAN_RUSS.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (charTir == null) {
            fautes.add("impossible de creer le char pour l'epreuve de tir");
        } else {
            charTir.setPos(12.5, 100.0, 12.5);
            charTir.setYRot(0.0f);
            level.addFreshEntity(charTir);
            var zone = charTir.getBoundingBox().inflate(96.0);
            Vec3 direction = new Vec3(0.0, 0.0, -1.0);
            int avant = level.getEntitiesOfClass(BoltProjectile.class, zone).size();
            boolean premier = charTir.tirer(level, charTir.boucheDuCanon(), direction, charTir);
            int apresPremier = level.getEntitiesOfClass(BoltProjectile.class, zone).size();
            boolean deuxieme = charTir.tirer(level, charTir.boucheDuCanon(), direction, charTir);
            int apresSecond = level.getEntitiesOfClass(BoltProjectile.class, zone).size();
            if (!premier) {
                fautes.add("le premier obus n'est pas parti");
            }
            if (apresPremier != avant + 1) {
                fautes.add("le tir n'a cree aucun projectile (" + avant + " → " + apresPremier + ")");
            }
            if (deuxieme) {
                fautes.add("un deuxieme obus est parti sans attendre le rechargement");
            }
            if (apresSecond != apresPremier) {
                fautes.add("un projectile de plus malgre le rechargement");
            }
            if (!charTir.enRechargement()) {
                fautes.add("le char ne se declare pas en rechargement apres un tir");
            }
            // TEMOIN POSITIF : le refus doit venir du DELAI, pas d'un canon casse pour toujours.
            // On fait avancer le char du nombre de ticks de la cadence, puis on retire.
            for (int i = 0; i < com.donopot.voiddynasty.entity.LemanRussEntity.CADENCE_TIR; i++) {
                charTir.tick();
            }
            boolean troisieme = charTir.tirer(level, charTir.boucheDuCanon(), direction, charTir);
            if (!troisieme) {
                fautes.add("le canon ne repart pas apres le delai : le refus ne vient pas de la cadence");
            }
            charTir.discard();
        }

        // l'oeuf d'apparition doit exister, sinon le vehicule est inatteignable en jeu
        if (ModItems.LEMAN_RUSS_SPAWN_EGG == null) {
            fautes.add("l'oeuf d'apparition du char n'est pas enregistre");
        }
        check("V-lemanruss-pilotable", fautes.isEmpty(),
                fautes.isEmpty()
                        ? "type lemunruss enregistre (gabarit 4,77x4,42 mesure sur le modele, planche 4,86x4,42), les trois fichiers du rendu presents, "
                        + "les six crochets de conduite declares dans la classe, conducteur nul sans passager, "
                        + "le char ne disparait pas et ne se laisse pas pousser, oeuf d'apparition en place, "
                        + "et le tir : un obus part, la cadence bloque le suivant, le canon repart apres le delai"
                        : String.join(" — ", fautes));
    }

    private static void testerTerrain(ServerLevel level) {
        // Zone de test FIXE, dans le chunk du spawn : c'est le seul dont le tick est garanti.
        BlockPos cible = new BlockPos(12, 100, 12);
        Vec3 tireur = new Vec3(12.5, 100.5, 22.5);   // 10 blocs devant, ligne de vue dégagée
        Vec3 direction = new Vec3(0, 0, -1);

        // dégagement de la zone de test : LARGE et PROFOND.
        // Le pourquoi : chaque arme a un budget de blocs modifiés par impact (`maxBlocksModified`,
        // 3 pour un bolter, 20 pour un melta). Ce budget est consommé par le PREMIER bloc non-air
        // rencontré, y compris ceux du terrain naturel sous la plateforme — d'où un bolter qui
        // « n'arrive jamais » à la cible alors que le melta y arrive. Un banc d'essai doit laisser
        // la cible comme SEUL bloc solide de la zone.
        degager(level);
        TerrainDamageManager.WorldMode modeInitial = TerrainDamageManager.getWorldMode();
        TerrainDamageManager.setWorldMode(TerrainDamageManager.WorldMode.ALL_UNPROTECTED);

        // --- 1. le melta détruit la pierre en un coup (impact direct, à découvert)
        int melta1 = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 4);
        check("B-melta-pierre", melta1 == 1,
                "pierre détruite en " + melta1 + " coup(s) — attendu 1 · " + derniereDiag);

        // --- 2. le bolter NE détruit PAS la pierre en un coup (accumulation), mais en 3 au plus
        int bolter1 = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.BOLTGUN, tireur, direction, 1);
        String diagBolter1 = derniereDiag;
        check("B-bolter-pierre-1coup", bolter1 != 1,
                "pierre intacte après 1 coup de bolter (attendu : intacte) · " + diagBolter1);

        // on repart d'une ardoise propre : le cumul du contrôle précédent est effacé pour que
        // « détruite en N coups » mesure bien N coups et pas N moins les coups déjà portés
        int bolter3 = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.BOLTGUN, tireur, direction, 5);
        check("B-bolter-pierre-plusieurs-coups", bolter3 >= 3 && bolter3 <= 5,
                "pierre détruite en " + bolter3 + " coup(s) de bolter — attendu 3 à 5 (accumulation) · "
                        + derniereDiag);

        // --- 3. le laser DOIT percer la pierre (décision de Donopot du 16/10/2025 : « détruit la pierre
        //        et d'autres blocs s'il y a beaucoup de tirs dessus ») — l'ancien contrôle affirmait
        //        l'inverse, il a été retourné en même temps que le comportement a changé.
        int laserPierre = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.LAS_GUN,
                tireur, direction, 8);
        check("B-laser-perce-pierre", laserPierre > 0,
                "le laser DOIT finir par percer la pierre (décision du 16/10/2025) : "
                        + (laserPierre > 0 ? "percée en " + laserPierre + " coups" : "aucune percée en 8 coups")
                        + " · " + derniereDiag);

        // --- 4. contrôle POSITIF de la même arme : le laser abîme bien la terre
        int laserTerre = coupsAvantDestruction(level, cible, Blocks.DIRT, WeaponDefinitions.LAS_GUN,
                tireur, direction, 8);
        check("B-laser-terre", laserTerre > 0,
                "terre détruite par le laser en " + laserTerre + " coup(s) (attendu : détruite) · " + derniereDiag);

        // --- 5. la bedrock est protégée, même du melta
        check("B-bedrock-protegee", coupsAvantDestruction(level, cible, Blocks.BEDROCK, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 8) == 0,
                "bedrock intacte après 8 coups de melta (attendu : intacte)");

        // --- 6. ligne de vue : un bloc caché derrière un mur n'est pas touché
        level.setBlock(cible, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockPos mur = new BlockPos(12, 100, 16);
        level.setBlock(mur, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        BlockPos cache = new BlockPos(12, 100, 18);     // derrière le mur, vu du tireur
        level.setBlock(cache, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        for (int i = 0; i < 4; i++) {
            TerrainDamageManager.onImpact(level, mur, tireur, direction, WeaponDefinitions.MELTA_GUN.terrain());
        }
        check("B-derriere-un-mur", level.getBlockState(cache).is(Blocks.STONE),
                "bloc caché intact (attendu : intact) — la ligne de vue est respectée");

        // --- 7. mode COSMETIQUE : fissures sans destruction
        TerrainDamageManager.setWorldMode(TerrainDamageManager.WorldMode.COSMETIC);
        check("B-mode-cosmetic", coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 3) == 0,
                "pierre intacte en mode cosmétique (attendu : intacte)");

        // --- 8. mode OFF : aucun dégât
        TerrainDamageManager.setWorldMode(TerrainDamageManager.WorldMode.OFF);
        check("B-mode-off", coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 3) == 0,
                "pierre intacte en mode off (attendu : intacte)");

        // --- 9. CLAIMS — décision de Donopot ①a : « hors zones revendiquées » (wilderness_only).
        // Ce contrôle manquait, et c'était un trou : sans FTB Chunks installé, le seul chemin jamais
        // exercé était « aucun claim → dégâts autorisés ». Autrement dit, la décision reposait sur du
        // code jamais éprouvé. On force donc le verdict de protection par le point d'essai.
        // La paire est faite pour pouvoir échouer : le second contrôle prouve que la cible est bien
        // cassable quand rien ne la protège. Si le premier passe alors que le second aussi, c'est que
        // la protection ne protège rien.
        TerrainDamageManager.setWorldMode(TerrainDamageManager.WorldMode.WILDERNESS_ONLY);
        ClaimIntegration.forcerProtege(Boolean.TRUE);
        int sousClaim = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 4);
        check("B-claim-protege", sousClaim == 0,
                "pierre intacte sous 4 coups de melta dans un chunk revendiqué (attendu : intacte)");
        ClaimIntegration.forcerProtege(Boolean.FALSE);
        int horsClaim = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.MELTA_GUN,
                tireur, direction, 4);
        check("B-hors-claim-casse", horsClaim == 1,
                "hors chunk revendiqué, la même pierre tombe en " + horsClaim + " coup(s) "
                        + "(attendu 1 — témoin positif : sans lui, le contrôle ci-dessus ne prouve rien)");
        ClaimIntegration.forcerProtege(null);

        // remise en état : mode d'origine + zone nettoyée
        TerrainDamageManager.setWorldMode(modeInitial);
        degager(level);
    }

    /** Vide la zone d'essai (assez large et assez profonde : voir le commentaire dans testerTerrain). */
    private static void degager(ServerLevel level) {
        for (int x = 8; x <= 16; x++) {
            for (int z = 4; z <= 24; z++) {
                for (int y = 96; y <= 106; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    /**
     * Pose un bloc du matériau voulu, tire jusqu'à {@code maxCoups} fois, et renvoie le numéro du coup
     * qui l'a détruit (0 = jamais détruit dans la limite). Le bloc est reposé à chaque essai.
     *
     * Renseigne {@link #derniereDiag} : cumul de dégâts réellement enregistré et seuil calculé par le
     * code lui-même. Sans ces deux chiffres, un échec ne dit pas s'il faut chercher du côté du calcul
     * des dégâts ou du côté du traitement du bloc.
     */
    private static String derniereDiag = "";

    private static int coupsAvantDestruction(ServerLevel level, BlockPos pos, Block materiau, WeaponDefinition arme,
                                             Vec3 tireur, Vec3 direction, int maxCoups) {
        // ardoise propre : sans cela, la mesure hérite du cumul laissé par le contrôle précédent sur
        // la même position (« détruite en 2 coups » pouvait vouloir dire « 2 coups après les 4 d'avant »)
        TerrainDamageManager.clearIntegrity(level, pos);
        level.setBlock(pos, materiau.defaultBlockState(), Block.UPDATE_ALL);
        TerrainDamageSpec spec = arme.terrain();
        for (int coup = 1; coup <= maxCoups; coup++) {
            TerrainDamageManager.onImpact(level, pos, tireur, direction, spec);
            derniereDiag = String.format("cumul=%.2f seuil=%.2f rayon=%.2f puissance=%.1f",
                    TerrainDamageManager.accumulatedAt(level, pos),
                    TerrainDamageManager.thresholdFor(materiau.defaultBlockState(), spec),
                    spec.impactRadius(), spec.impactPower());
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                return coup;
            }
        }
        return 0;
    }

    // ==================================================================
    // ÉQUILIBRAGE (décisions de Donopot du 16/10/2025) — mesuré, pas calculé.
    //
    //   1. les armes lourdes doivent détruire la pierre ;
    //   2. le laser doit finir par la détruire (beaucoup de tirs), et enflammer le bois ;
    //   3. la cible de référence n'est pas la source, mais un joueur en armure de FER,
    //      calibré pour que les armes lourdes restent très puissantes contre du DIAMANT.
    // ==================================================================
    private static void testerEquilibrage(ServerLevel level) {
        Vec3 tireur = new Vec3(0.5, 120.0, 0.5);
        Vec3 direction = new Vec3(0, -1, 0);
        BlockPos cible = new BlockPos(0, 100, 0);
        StringBuilder table = new StringBuilder();
        java.util.List<String> trop = new java.util.ArrayList<>();
        // les familles « lourdes » : elles DOIVENT casser la pierre
        java.util.Set<String> lourdes = java.util.Set.of("melta", "plasma", "explosive");
        for (WeaponDefinition arme : WeaponDefinitions.TOUTES) {
            int coups = coupsAvantDestruction(level, cible, Blocks.STONE, arme, tireur, direction, 20);
            table.append(nomArme(arme)).append("=").append(coups == 0 ? ">20" : coups).append("  ");
            if (lourdes.contains(arme.family()) && coups == 0) {
                trop.add(nomArme(arme) + " (" + arme.family() + ")");
            }
        }
        // le laser doit finir par casser la pierre, et enflammer le bois
        int laserPierre = coupsAvantDestruction(level, cible, Blocks.STONE, WeaponDefinitions.LAS_GUN,
                tireur, direction, 30);
        int pistoletLaserPierre = coupsAvantDestruction(level, cible, Blocks.STONE,
                WeaponDefinitions.LAZ_PISTOL, tireur, direction, 30);
        boolean laserEnflamme = WeaponDefinitions.LAS_GUN.terrain().status().igniteFlammable();
        // le laser doit ALLUMER le bois : on regarde s'il y a du FEU, pas si le bloc a disparu
        boolean laserBois = false;
        if (laserEnflamme) {
            level.setBlock(cible.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            int feux = 0;
            for (int essai = 0; essai < 40 && !laserBois; essai++) {   // 8 ne suffisaient pas : allumage probabiliste
                TerrainDamageManager.clearIntegrity(level, cible);
                level.setBlock(cible, Blocks.OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(cible.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                TerrainDamageManager.onImpact(level, cible, tireur, direction,
                        WeaponDefinitions.LAS_GUN.terrain());
                if (level.getBlockState(cible.above()).is(Blocks.FIRE)) {
                    laserBois = true;
                    feux = essai + 1;
                }
            }
            level.setBlock(cible.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        java.util.List<String> problemes = new java.util.ArrayList<>();
        if (!trop.isEmpty()) problemes.add("armes lourdes qui ne cassent pas la pierre : " + trop);
        if (laserPierre == 0) problemes.add("le laser (Las Gun) ne casse pas la pierre même en 30 tirs");
        if (pistoletLaserPierre == 0) {
            problemes.add("le pistolet laser ne casse pas la pierre même en 30 tirs");
        }
        if (!laserEnflamme) {
            problemes.add("le laser n'enflamme pas les matières inflammables");
        } else if (!laserBois) {
            problemes.add("le laser est déclaré incendiaire mais n'a allumé aucun bois en 40 essais");
        }
        check("B-armes-pierre", problemes.isEmpty(),
                "coups pour casser la pierre (même ordre que WeaponDefinitions.TOUTES) — "
                        + table.toString().trim()
                        + " · laser " + (laserPierre == 0 ? ">30" : laserPierre) + " coups · pistolet laser "
                        + (pistoletLaserPierre == 0 ? ">30" : pistoletLaserPierre) + " coups · laser "
                        + (laserBois ? "allume bien le bois" : "n'allume pas le bois")
                        + (problemes.isEmpty() ? "" : " || PROBLÈMES : " + String.join(" — ", problemes)));

        // ---- la cible de référence : joueur en FER, et les armes lourdes contre du DIAMANT
        float[] ferTenue = armureVanille(level, "iron");
        float[] diamantTenue = armureVanille(level, "diamond");
        float fer = ferTenue[0], diamant = diamantTenue[0];
        var zombie = net.minecraft.world.entity.EntityTypes.ZOMBIE.create(level,
                net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (zombie == null) {
            check("E-cible-joueur", false, "impossible de créer un porteur de test");
            return;
        }
        zombie.setPos(0.5, 120, 0.5);
        StringBuilder ferTexte = new StringBuilder();
        StringBuilder diamantTexte = new StringBuilder();
        java.util.List<String> tropMou = new java.util.ArrayList<>();
        for (WeaponDefinition arme : WeaponDefinitions.TOUTES) {
            float degats = arme.damage();
            if (degats <= 0) continue;
            var source = level.damageSources().generic();
            float surFer = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    zombie, degats, source, fer, ferTenue[1]);
            float surDiamant = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    zombie, degats, source, diamant, diamantTenue[1]);
            int coupsFer = surFer <= 0 ? 99 : (int) Math.ceil(20.0 / surFer);
            int coupsDiamant = surDiamant <= 0 ? 99 : (int) Math.ceil(20.0 / surDiamant);
            ferTexte.append(nomArme(arme)).append("=").append(coupsFer).append("  ");
            diamantTexte.append(nomArme(arme)).append("=").append(coupsDiamant).append("  ");
            if ("melta".equals(arme.family()) && coupsDiamant > 2) {
                tropMou.add(nomArme(arme) + " (" + coupsDiamant + " coups sur du diamant)");
            }
        }
        zombie.discard();
        check("E-cible-joueur", tropMou.isEmpty(),
                (tropMou.isEmpty() ? "" : String.join(" — ", tropMou) + " || ")
                        + "coups pour tuer un joueur — FER (armure " + (int) fer + ") : "
                        + ferTexte.toString().trim() + " || DIAMANT (armure " + (int) diamant + ") : "
                        + diamantTexte.toString().trim());
    }

    /** Nom court mais DISCRIMINANT : trois armes partagent la famille « bolt », d'où la longueur. */
    private static String nomArme(WeaponDefinition arme) {
        String nom = WeaponDefinitions.TOUTES.indexOf(arme) >= 0
                ? String.valueOf(WeaponDefinitions.TOUTES.indexOf(arme)) : "?";
        return arme.family().substring(0, Math.min(3, arme.family().length())) + nom;
    }

    /**
     * Armure et ténacité d'une tenue vanilla complète, lues **sur les vrais objets** (leurs
     * modificateurs d'attribut) plutôt que recopiées : si Mojang change une valeur, la mesure suit.
     */
    private static float[] armureVanille(ServerLevel level, String tenue) {
        net.minecraft.world.item.Item[] pieces = tenue.equals("iron")
                ? new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.IRON_HELMET,
                        net.minecraft.world.item.Items.IRON_CHESTPLATE, net.minecraft.world.item.Items.IRON_LEGGINGS,
                        net.minecraft.world.item.Items.IRON_BOOTS}
                : new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.DIAMOND_HELMET,
                        net.minecraft.world.item.Items.DIAMOND_CHESTPLATE, net.minecraft.world.item.Items.DIAMOND_LEGGINGS,
                        net.minecraft.world.item.Items.DIAMOND_BOOTS};
        float armure = 0f, tenacite = 0f;
        for (net.minecraft.world.item.Item piece : pieces) {
            var modificateurs = new net.minecraft.world.item.ItemStack(piece)
                    .get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
            if (modificateurs == null) continue;
            for (var entree : modificateurs.modifiers()) {
                var attribut = entree.attribute();
                if (attribut == net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) {
                    armure += entree.modifier().amount();
                } else if (attribut == net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS) {
                    tenacite += entree.modifier().amount();
                }
            }
        }
        return new float[]{armure, tenacite};
    }

    // ==================================================================
    // Apparitions : chaque créature doit pouvoir naître (sinon elle n'existe qu'en théorie).
    // ==================================================================
    private static void testerApparitions(ServerLevel level) {
        String[] creatures = {"tyranid_warrior", "carnifex", "carnifex_leviathan", "lictor", "norn_emissary",
                "great_unclean_one", "lord_of_change", "keeper_of_secrets", "khorne_juggernaut",
                "large_khorne_daemon", "plaguetoad", "ghazghkull", "servo_skull",
                // phase E : cultistes (lot 1) puis Spaces Marines du Chaos (lot 2)
                "cultist_1", "cultist_2", "cultist_3", "cultist_4", "cultist_boss",
                "chaos_marine_1", "chaos_marine_2", "chaos_marine_3", "chaos_captain", "chaos_raptor",
                "servitor_1", "servitor_2", "servitor_3",
                "chaos_dreadnought", "chaos_hellbrute", "chaos_forgefiend", "daemon_prince",
                "chaos_helldrake"};
        List<String> rates = new ArrayList<>();
        int ok = 0;
        // dans le chunk du spawn, seul dont le tick d'entités est garanti au démarrage
        for (int i = 0; i < creatures.length; i++) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("voiddynasty", creatures[i]));
            if (type == null) {
                rates.add(creatures[i] + " (type introuvable)");
                continue;
            }
            BlockPos p = new BlockPos(4 + (i % 4) * 2, 100, 4 + (i / 4) * 2);
            // spawn est une méthode d'INSTANCE de EntityType (vérifié par javap sur le jar déobfusqué)
            Entity e = type.spawn(level, p, EntitySpawnReason.MOB_SUMMONED);
            if (e == null || e.isRemoved() || !e.isAlive()) {
                rates.add(creatures[i]);
            } else {
                ok++;
                e.discard();      // nettoyage : le monde de test ne doit pas garder de résidus
            }
        }
        check("S-apparitions", rates.isEmpty(), ok + "/" + creatures.length + " créatures apparues"
                + (rates.isEmpty() ? "" : " — échecs : " + rates));
    }
}
