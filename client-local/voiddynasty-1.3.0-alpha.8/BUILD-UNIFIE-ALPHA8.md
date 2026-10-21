# VOID DYNASTY 1.3.0-alpha.8 — JAR Minecraft unique

Cette version part de `main` après la PR #5 (`bf8a0c1`) : elle conserve le servitor alpha 7 d'Hermes, sa récupération des sacs, sa mémoire par monde et ses routines. Le mod principal et le compagnon Minecraft `voiddynasty_bridge` sont désormais compilés dans un seul mod Fabric, identifié `voiddynasty`.

## Contenu

Le même JAR contient la flotte à six classes, les entités et factions, les armes et leur HUD, les missions du servitor, le cerveau IA et les commandes natives utilisées par MCP, les correctifs du pad et des coffres, les rayons laser, les impacts de bolt et le plasma, ainsi que le terminal et la logistique Minecraft vers Rogue Trader.

Les sources restent uniques : `voiddynasty/src/main/` et `voiddynasty-bridge/src/main/` sont compilés ensemble. Aucune copie des classes n'est créée entre ces dossiers et aucun ancien JAR de VOID DYNASTY n'est utilisé comme dépendance de compilation. Le deuxième `fabric.mod.json` est exclu ; les points d'entrée communs, les points d'entrée client et les deux listes de mixins sont déclarés par le mod principal. Le pack de rendu intégré conserve son identifiant et retrouve son conteneur dans le mod principal.

Les identifiants des blocs, y compris `voiddynasty_bridge:cargo`, les formats de sauvegarde, les reçus et le protocole de livraison v4 sont conservés. Les registres communs du bridge chargent aussi sur serveur dédié ; la connexion de livraison reste limitée au solo local comme auparavant.

## Installation manuelle

Minecraft Java **26.3**, Fabric Loader **0.19.5 ou supérieur**, Java **25**.

1. Fermer Minecraft et sauvegarder le monde.
2. Retirer du dossier `mods` l'ancien JAR principal `voiddynasty-*.jar` et le compagnon `voiddynasty-bridge-*.jar` / `voiddynasty_bridge-*.jar`.
3. Placer `voiddynasty-1.3.0-alpha.8.jar` dans `mods`.
4. Conserver un seul Fabric API `0.162.0+26.3` et un seul GeckoLib `5.5.7` pour Fabric 26.3. Ils sont fournis dans le pack client : **trois fichiers au total, dont un seul mod VOID DYNASTY**.
5. Pour jouer sur serveur dédié, utiliser le même JAR unifié côté client et serveur.

Le mod fonctionne sans Rogue Trader. Pour les livraisons RT, sa DLL et son chargeur restent côté Rogue Trader ; ils ne peuvent pas être chargés par Minecraft. Le paquet ne modifie pas cette DLL ni une liaison existante. Les outils Python MCP sont des outils externes pour l'agent, pas une dépendance requise pour jouer dans Minecraft.

L'ancien assistant Windows 0.7.1 installe les deux anciens modules séparés et n'est pas adapté à ce JAR unifié. Utiliser le pack manuel. Fabric refuse explicitement le vieux bridge avec le nouveau JAR afin d'empêcher des mixins et initialisations en double.

## Compilation

Build Gradle depuis `voiddynasty/` avec Java 25, GeckoLib dans `libs/` et les dépendances Fabric disponibles :

```
./gradlew build
```

Le projet Gradle compile maintenant les deux dossiers de sources. Le chemin local sans téléchargement de Gradle, réellement exécuté pour ce paquet, est :

```
python voiddynasty/tools/build_unified.py --classpath-json <bibliotheques.local.json> --jdk <jdk25> --output <sortie>
```

Le JSON privé contient un tableau `classpath` de JAR du jeu et de ses bibliothèques. Les anciens JAR VOID DYNASTY, les répertoires de classes et les anciens fichiers auxiliaires de compilation sont exclus. `CompileAccess.java` utilise le ClassTweaker de Fabric Loader pour générer une vue de compilation du jeu conforme à l'access widener déclaré. Cette vue reste dans le dossier de build : aucune classe Minecraft, bibliothèque du jeu ni classe de test n'entre dans le JAR livré.

Chaque build utilise des classes neuves, refuse les collisions de sources et de ressources, vérifie que les sources n'ont pas changé pendant la compilation et produit une empreinte SHA-256. Les entrées ZIP ont un ordre et des horodatages fixes. Le build Gradle n'a pas été exécuté sur ce poste ; la compilation Java complète et le chargement Fabric réels sont ceux vérifiés ci-dessous.

## Vérification

- 249 sources Java et 864 ressources réunies, sans doublon de chemin d'archive.
- Chargement client par le vrai Knot/Fabric : sept classes cibles transformées, pack refit enregistré depuis le conteneur principal, 30 assertions d'inventaire et de mise en page.
- Présence de l'ancien bridge : refus explicite par Fabric avant double chargement.
- 57 modèles du pack refit : textures, parents, variantes de blocs et bornes géométriques vérifiés.
- Journal de livraison, migration des anciens reçus, réconciliation d'une réponse perdue et empreintes des 14 missions vérifiés par le harness Java contre le JAR unifié.
- Tests Python MCP, conservation native, laboratoire serveur de chantier/construction/navigation et laboratoire de récupération : résultats détaillés dans `client-local/voiddynasty-1.3.0-alpha.8/VALIDATION.md`.

Les fixtures de récupération héritées avaient des états initiaux qui se mélangeaient : sac encore rempli par le scénario précédent et donneur recherché par « premier compagnon » au lieu de sa référence. Seules les fixtures ont été corrigées ; le code de récupération d'Hermes est conservé. Le test de dimension invoque le gestionnaire Fabric dans les deux sens avec un UUID conservé ; il ne constitue pas un essai de traversée de portail par un joueur.

La validation client sans fenêtre confirme les transformations et l'enregistrement du pack, pas une recette visuelle humaine. Un nouveau test réel des deux jeux pour les livraisons RT, la qualité des décisions du modèle et la connexion à Hermes restent à faire. Aucun profil personnel, dossier `mods` installé ni monde du joueur n'a été remplacé.
