# VOID DYNASTY 1.3.0-alpha.2 — accès et construction

Alpha de la branche `codex/servitor-polyvalent`, pour **Minecraft Java 26.3, Fabric Loader 0.19.5 et Java 25**. Rogue Trader est facultatif.

Le servitor peut dégager un accès horizontal aux minerais et construire deux plans avec des matériaux réellement prélevés dans un coffre : plateforme 3×3 (9 blocs) et abri 5×5 (94 blocs). Chaque casse et chaque pose disposent d'un reçu de rollback. La reprise après redémarrage conserve le stock réservé.

## Installer dans une instance de test

1. Préparer une instance Fabric séparée avec un monde de test. Sauvegarder un monde existant avant une mise à jour : le format de chantier passe à 5.
2. Fermer l'instance concernée et retirer ses anciennes versions des quatre composants ci-dessous, en gardant les autres mods à part pour la première recette.
3. Copier les quatre JAR de [mods](mods), avec un seul exemplaire de chaque composant.
4. Pour le pilotage MCP, installer les mêmes JAR dans le serveur dédié et le client, puis suivre [le guide](../../voiddynasty/docs/SERVITOR-POLYVALENT-ALPHA2.md). Le MCP n'est pas automatiquement raccordé au serveur intégré d'une partie solo.

| Fichier | Rôle |
|---|---|
| `voiddynasty-1.3.0-alpha.2.jar` | Flotte et missions de collecte/construction du servitor. |
| `voiddynasty-bridge-0.7.1.jar` | Pad, coffres, armes et logistique existants. |
| `fabric-api-0.162.0+26.3.jar` | Fabric API. |
| `geckolib-fabric-26.3-5.5.7.jar` | Animations GeckoLib. |

Les trois dépendances restent identiques au pack 1.2.5. [manifest.json](manifest.json) fournit les identités, versions, empreintes et le commit des sources ; [SHA256.txt](SHA256.txt) permet de vérifier les fichiers. Aucun monde, secret, bibliothèque du jeu ou ressource One Piece n'est inclus. Les licences d'origine restent dans [licences](licences), avec les mentions MIT/CC0 historiques de la base.

## Utiliser les nouvelles missions

`/work mission capabilities` et `servitor_capabilities` exposent les capacités disponibles. Six outils MCP `servitor_*` permettent collecte, estimation, construction et contrôle. Le mode `--servitor-only` expose uniquement ces six outils. [Exemple pour Hermes distant](../../voiddynasty/tools/hermes/README.md).

La collecte conserve le mode `access=surface` par défaut. Choisir explicitement `excavate` autorise le dégagement de roche dans la zone, limité à 256 blocs d'accès. Le passage est adapté à la largeur du servitor ; les escaliers et ponts restent à développer.

Pour construire, choisir `platform_3x3_v1` ou `shelter_5x5_v1`, avec cobblestone, stone, oak_planks ou spruce_planks. Prévoir un site vide avec un sol porteur et un coffre simple/tonneau de matériaux hors du site. L'estimation est en lecture seule. La mission réserve le coût entier avant de poser les blocs. L'abri comprend un sol, des murs, un toit et une entrée de deux blocs de large ; il reste sans mobilier et sans porte.

Une mission acceptée n'est pas encore réussie : suivre son UUID jusqu'au résultat observé. `cancel` conserve les matériaux non consommés ; `deposit` les rapporte au dépôt de chantier. Le rollback retire uniquement les poses encore identiques aux reçus et rembourse leur coût dans le sac. Un bloc modifié par le joueur est préservé. Attendre la fin du rollback et déposer le sac avant `close`, qui abandonne les reçus.

[Règles et limites](../../voiddynasty/docs/SERVITOR-POLYVALENT-ALPHA2.md) · [Validation](VALIDATION.md). L'atomicité disque lors d'une coupure brutale et la validation visuelle client restent ouvertes. Le kit de dépôts et l'outil sont encore fournis par l'opérateur ; les blocs de construction viennent bien du coffre indiqué. Ce pack manuel ne modifie pas l'ancien installeur inter-jeux et n'a pas été installé dans l'instance habituelle du joueur.
