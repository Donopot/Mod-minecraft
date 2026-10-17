# VOID DYNASTY 1.3.0-alpha.1 — missions du servitor

Paquet de développement de la branche `codex/servitor-polyvalent`. Cible : **Minecraft Java 26.3, Fabric Loader 0.19.5, Java 25**. Rogue Trader est facultatif. La branche contient un premier jalon de collecte pilotable par MCP ; la construction de maisons et la prospection souterraine restent à développer.

## Installation de test

1. Préparer une instance Fabric séparée et un monde de test. Cette alpha écrit le format de chantier 4 ; conserver une sauvegarde avant de tester un monde existant.
2. Fermer l'instance à modifier. Garder un seul exemplaire de chaque mod/dépendance dans son dossier `mods`.
3. Copier les quatre JAR de [mods](mods). Retirer les anciennes versions des mêmes quatre composants de cette instance.
4. Pour un serveur dédié, utiliser ces mêmes JAR côté serveur et côté client. L'usage MCP demande RCON sur le serveur et le processus Python décrit dans [le guide](../../voiddynasty/docs/SERVITOR-POLYVALENT.md).

| Fichier | Fonction |
|---|---|
| `voiddynasty-1.3.0-alpha.1.jar` | Flotte, servitor, missions persistantes et commandes typées. |
| `voiddynasty-bridge-0.7.1.jar` | Pad, coffres, armes et logistique existants. |
| `fabric-api-0.162.0+26.3.jar` | Fabric API. |
| `geckolib-fabric-26.3-5.5.7.jar` | GeckoLib. |

Les trois dépendances sont identiques au pack 1.2.5. [manifest.json](manifest.json) associe les binaires aux sources ; [SHA256.txt](SHA256.txt) permet de vérifier leur intégrité. Aucun monde, compte, mot de passe RCON, bibliothèque du jeu ou asset One Piece n'est inclus. Les licences d'origine sont conservées dans [licences](licences). La déclaration MIT et le fichier CC0 de la base sont conservés comme dans les paquets précédents.

## Tester la collecte

Les commandes classiques `/work` restent disponibles. `/work mission capabilities` affiche le catalogue. Les quatre outils MCP `servitor_*` ajoutent la soumission, le suivi, la pause/reprise, l'annulation, la récupération du sac et le rollback. Le servitor construit son dépôt puis livre dans un coffre simple ou un tonneau déjà présent, hors de la zone sélectionnée.

Une mission acceptée ne signifie pas qu'elle est réussie : attendre `SUCCEEDED` et les quantités observées. Le coffre plein met en pause ; les ressources absentes donnent `INCOMPLETE`. Après annulation, récupérer le sac via `deposit` avant `close`. La fermeture abandonne les reçus permettant le rollback. La même clé de demande et les mêmes paramètres ne créent jamais un deuxième travail.

Les minerais doivent être accessibles physiquement ; le servitor ne creuse pas encore de tunnel à travers les blocs ignorés. Il dispose toujours d'un kit d'opérateur pour l'outil et les dépôts. Le raccordement à ton agent Hermes et le test visuel en client restent à faire. Voir [le guide et la suite du développement](../../voiddynasty/docs/SERVITOR-POLYVALENT.md) et [la validation](VALIDATION.md).

L'ancien installeur inter-jeux reste attaché à ses versions historiques. Cette alpha est un pack manuel distinct ; elle n'a pas été installée dans l'instance habituelle du joueur.
