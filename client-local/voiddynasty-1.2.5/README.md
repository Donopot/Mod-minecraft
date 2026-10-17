# VOID DYNASTY — client local 1.2.5

Minecraft Java **26.3**, Fabric Loader **0.19.5**, Java **25**. Rogue Trader reste facultatif. Le servitor construit lui-même son dépôt après acceptation de la zone de minage, en conservant les protections de ressources et de rollback de la 1.2.4.

## Installation manuelle

1. Télécharger le dépôt et extraire l'archive, ou le cloner.
2. Utiliser une instance Fabric 0.19.5 pour Minecraft 26.3, avec Java 25. Pour la première recette du servitor, créer un monde de test séparé.
3. Fermer Minecraft. Sauvegarder les mondes avant une mise à jour d'une instance existante.
4. Dans le dossier `mods` de cette instance, mettre de côté les anciennes versions de VOID DYNASTY et de son bridge. Garder un seul JAR de chaque mod/dépendance.
5. Copier les **quatre JAR** de [mods](mods), puis lancer le profil Fabric.

| Fichier | Fonction |
|---|---|
| `voiddynasty-1.2.5.jar` | Mod principal, flotte et dépôt construit par le servitor. |
| `voiddynasty-bridge-0.7.1.jar` | Correctifs du pad, coffres, armes et logistique. |
| `fabric-api-0.162.0+26.3.jar` | Fabric API. |
| `geckolib-fabric-26.3-5.5.7.jar` | Animations GeckoLib. |

Les trois derniers fichiers sont identiques au pack historique 0.7.1. Le nouveau numéro désigne le mod principal ; le bridge reste en 0.7.1. Les identités, versions et empreintes figurent dans [manifest.json](manifest.json) et [SHA256.txt](SHA256.txt).

L'ancien installeur inter-jeux et son contrôle de provenance restent attachés à leur base 1.0.0. Cette mise à jour du servitor se distribue par ce pack manuel ; ne pas changer les empreintes attendues de l'ancien installeur pour la forcer.

## Tester le servitor

Avec les commandes d'opérateur autorisées, sélectionner une petite zone : le servitor recherche lui-même un emplacement, construit un support si nécessaire, puis pose son coffre avant de miner. Il n’est plus nécessaire de préparer un dépôt près du coin supérieur de la zone. Exemple de commandes :

```text
/work start essai <coin1> <coin2>
/work status
/work stop
/work rollback 1
```

Le rollback récupère les drops exacts dans le sac et les dépôts connus avant de valider la restauration. Si les ressources ont été retirées, les remettre dans un dépôt suivi puis redemander le rollback. Il préserve les autres objets et s'arrête si le terrain a été modifié. Les anciens journaux sans reçus ne sont pas annulés automatiquement.

`/work deposit` vide le sac sans nouvelle casse. `/work commit` abandonne explicitement l'historique d'un chantier arrêté dont le sac est vide, puis permet un nouveau chantier. Un redémarrage attend `/work resume`. Les outils sont encore une fourniture d'opérateur via `/work tool`. Un kit séparé de 64 coffres et 64 supports équipe chaque nouveau chantier ; le constructeur ne consomme pas le butin miné. Un coffre plein déclenche la construction d'un dépôt supplémentaire.

Voir [les règles et limites détaillées](../../voiddynasty/docs/SERVITOR-1.2.5.md) et [la validation](VALIDATION.md). Le servitor ne construit pas encore d'accès pour une excavation profonde. La conservation après une coupure brutale pendant les écritures disque n'est pas garantie.

## Sans Rogue Trader

Le contenu Minecraft, les vaisseaux et le chantier fonctionnent sans RT. Le statut `Rogue Trader unavailable` concerne les livraisons entre jeux. `/give @s voiddynasty:control_pad` donne le pad et `/fleet list` affiche la flotte. Ce pack ne contient ni One Piece, ni monde, compte ou configuration personnelle.

Les licences d'origine sont conservées dans les archives et [licences](licences). La base déclare MIT dans son manifeste et fournit un fichier CC0, comme le paquet historique ; ces mentions sont conservées.
