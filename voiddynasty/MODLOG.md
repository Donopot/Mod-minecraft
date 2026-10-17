# MODLOG — umtest (validation du pipeline universal-modder)

Journal du mod `umtest`. Rempli au fil de l'eau, deviendra la field note finale.

## Intake
- **Objectif :** valider le pipeline universal-modder de bout en bout avec un mod **minimal, logique pure, 0 assets**.
- **Cible :** Minecraft Java Edition, mod **côté serveur** (testable headless).
- **Machine :** tout est headless (HP Mini conteneur Docker + Pi 5). Pas de client GUI.
- **Done =** mod buildé + chargé par un serveur Fabric headless + logique vérifiée dans les logs (`logs/latest.log`).

## Recon (sources réelles, pas de mémoire)
- `um kb search "minecraft"` → notes existantes (Fabric 26.3, Loader 0.19.5, API 0.161→0.162, Java 25).
- `um scan --list` → « no Steam/Epic/Xbox installs found » (normal, headless, aucun jeu installé).
- **API meta Fabric (source of truth) :** game stable `26.3`, loader `0.19.5`, installer `1.1.2`.
- **Template officiel `FabricMC/fabric-example-mod` branche `26.3` :** minecraft 26.3, loader 0.19.5, loom 1.18-SNAPSHOT, fabric-api **0.162.0+26.3**, Java 25, Gradle 9.7.1.

## Route choisie
- **Loader API** (Fabric API : `ServerLifecycleEvents` + `CommandRegistrationCallback`), pas de Mixin.
- Raison : mod purement logique, c'est la route la moins chère et la plus robuste. Mixin inutile pour ce vertical slice.

## Vertical slice
- `UmTestMod` (main) : log au `onInitialize`, log `SERVER_STARTED` (oracle n°1), commande `/umtest` (oracle n°2).

## Versions verrouillées
- JDK : Temurin 25.0.4.1 LTS (portable dans `/opt/data/jdk/jdk-25.0.4.1+1`, pas de sudo dispo).
- Minecraft 26.3 / Fabric Loader 0.19.5 / Fabric API 0.162.0+26.3 / Loom 1.18-SNAPSHOT / Gradle 9.7.1.

## Gotchas / écueils
- Pas de `sudo` dans le conteneur → JDK installé en portable (tarball Temurin), pas via apt.
- 26.3 exige **Java 25** (pas 21) : le premier téléchargement JDK 21 a été corrigé.
- Signature commande Fabric (v2) confirmée dans la source : `register(CommandDispatcher<ServerCommandSource>, CommandRegistryAccess, CommandManager.RegistrationEnvironment)` — `ServerCommandSource` (pas l'ancien `CommandSourceStack`).
- `curl | python3` déclenche l'approbation sécurité → privilégier `git clone` / fichiers temporaires.

## Journal d'exécution
- [x] JDK 25 portable OK (`java -version` → 25.0.4.1 LTS). JDK 21 initial corrigé en 25 (26.3 exige Java 25).
- [x] Template adapté : modid `umtest`, package `com.donopot.umtest`, mixins supprimés.
- [x] `./gradlew build` → **BUILD SUCCESSFUL** (`build/libs/umtest-1.0.0.jar`).
- [x] Serveur Fabric headless (`fabric-server-launch.jar nogui`) → `Done (6.097s)!`.
- [x] Vérif logs : `[umtest] Mod initializing` + `[umtest] SERVER_STARTED — version=26.3`.
- [x] Commande `/umtest` exécutée via RCON → réponse `[umtest] pipeline OK — mod loaded and responding`.

## Résultat
Pipeline validé de bout en bout, headless, 0 € : **build → déploiement → chargement → événement serveur →
commande répondante**. Field note écrite dans `knowledge/games/minecraft/` + `um kb check` PASS.
