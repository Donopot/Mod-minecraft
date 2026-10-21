# Avancement NeoForge — 21 octobre 2026

Référence : `plans/2026-10-21-PLAN-COMPLET-PORTAGE-NEOFORGE-1.21.1.md` sur la branche `neoforge`. Source gelée : `020fa9cf5efc10e7e697829e228001336b2c016a`, contenant le Fabric alpha.95 et le laser drill.

| Phase | État | Preuve / suite |
|---|---|---|
| P0 — cadastre | **PASS** | Contrat canonique versionné, 88 IDs, 24 empreintes de sources ; GameTest compilé avec NeoForge 21.1.264 / Java 21 |
| P1 — extraction | **EN ATTENTE HERMES** | 173 commandes d'export préparées en neuf lots ; aucun schematic réel validé ici |
| P2 à P12 | **NON DÉMARRÉES** | Verrou du §10 : attendre PASS P1 avant P2 |

## Vérifications effectuées

- `verify_contract.py` : 88 identifiants cohérents, six blocs de map, un servitor et deux items requis, sept mappings de block entities/capacités, 24 sources inchangées.
- 12 tests Python réussis, incluant perte d'ID de map, confusion de BE de four, casse des clés NBT, doublons et bornes inclusives des exports.
- `p0-compiler/gradlew classes` : **BUILD SUCCESSFUL**, ModDevGradle 2.0.148 / NeoForge 21.1.264 / Gradle 9.2.1 / Temurin Java 21.0.12.1+1. Classe compilée `ContratIdsTest` en version 65 (Java 21) et ressource de contrat identique.
- Le GameTest **n'a pas été exécuté en jeu**. C'est conforme au critère P0 : compilation exigée, échec attendu jusqu'à P3. Aucun registre factice n'a été créé pour obtenir un test vert.
- Aucune connexion au serveur de production, aucune sauvegarde de monde ouverte ou modifiée.

## Points corrigés dans le cadastre

Les sons étaient absents de l'annexe ; les treize enregistrements réels sont dans le contrat. L'annexe armure mélangeait clés d'assets et tag de réparation avec les huit vrais items. Les quatre machines au prométhium conservent des IDs de BE vanilla. Les minuteries des fours, le verrou et les composants nécessitent des adaptateurs à P5. Les sacs/routines du servitor sont dans des SavedData externes au schematic.

Les bornes hautes du CSV sont exclusives : WorldEdit doit utiliser x2-1/z2-1/Y=319. Le suivi d'exports ne déclare jamais P1 réussie sur la seule présence des fichiers ou une enveloppe gzip valide.

## Prochaine action

Hermes exécute le relais `HERMES-P1-EXPORTS.md` sur la copie du monde 26.3. Il retourne les preuves d'export et les NBT bruts pour préparer les adaptateurs. Le dossier `.runtime/neoforge-p1-hermes` généré localement contient les neuf lots et le manifeste vierge ; ils se régénèrent avec le script versionné, sans secret et sans connexion serveur.
