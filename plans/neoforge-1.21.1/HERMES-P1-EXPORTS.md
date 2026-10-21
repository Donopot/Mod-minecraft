# Relais Hermes — P1, extraction des 173 zones

Hermes seul accède au serveur et au monde `data-263`. Codex a terminé P0 côté dépôt ; aucun accès au serveur, aucun export réel et aucun collage n'ont été effectués depuis ce poste.

**Suite aux exports rapportés dans `a807882` : lire d'abord la [revue P1](P1-REVUE-EXPORTS.md).** Les scripts initiaux avaient des défauts de format, géométrie et conservation des données. Leurs exports remappés ne sont pas autorisés au collage ; les 159 fichiers et 14 zones sans contenu ne suffisent pas à valider P1.

## À exécuter par Hermes

1. Récupérer la branche `neoforge`, lire le plan complet et `contrat-ids.json`.
2. Sur le serveur accessible à Hermes, relever le chemin réel du monde, vérifier la sauvegarde annoncée et calculer son SHA-256. Faire les opérations sur une copie cohérente et sauvegardée du monde 26.3. Ne pas lancer le futur mod incomplet sur ce monde.
3. Générer les neuf lots de commandes (20 zones par lot, dernier lot de 13) :

```sh
python3 scripts/world-migration/prepare_neoforge_exports.py prepare --output /chemin/controle/hermes-p1
```

Le script ne se connecte à rien et ne lance aucune commande Minecraft. Les fichiers `hermes-batch-01.txt` à `hermes-batch-09.txt` doivent être exécutés commande par commande par le joueur OP sur la copie 26.3 équipée de WorldEdit 7.4.6. Vérifier une première zone avant d'enchaîner les lots, arrêter sur erreur et conserver les exports déjà réussis.

4. Après chaque export, noter son succès dans la copie de `export-manifest.json`. Ne jamais remplacer un export existant sans avoir vérifié son origine et conservé son empreinte.
5. Après les 173 exports, effectuer le contrôle d'intégrité :

```sh
python3 scripts/world-migration/prepare_neoforge_exports.py verify --exports /chemin/worldedit/schematics --report /chemin/controle/hermes-p1/integrity.json
```

Ce contrôle vérifie les noms attendus, l'enveloppe gzip/NBT, la lecture complète avec CRC et les SHA-256. Il ne valide ni le format Sponge v3 complet, ni les palettes cibles, ni les inventaires. Son champ `p1_pass` reste donc toujours `false` : Hermes doit fournir les preuves WorldEdit et visuelles séparément.

6. Vérifier que WorldEdit liste et recharge les 173 schematics sans erreur, avec entités et biomes. Relever leurs dimensions et leur origine. Contrôler visuellement au moins les zones 001 à 005 ; joindre les captures et leurs coordonnées. Aucun collage sur la cible 1.21.1 à cette étape.

## Corrections importantes par rapport aux exemples du plan

- Le CSV utilise des bornes hautes **exclusives** : `x2`, `z2`, `y2=320`. Les commandes WorldEdit sont inclusives : sélectionner `x2-1`, `z2-1`, et Y maximum **319**. Le script applique ces conversions. Exemple zone_001 : `//pos1 -272,-64,-192`, `//pos2 303,319,175`, soit 576 × 384 × 368 blocs.
- L'origine de copie est fixée à `(x1,100,z1)` par la téléportation avant `//copy`. La conserver dans le manifeste pour éviter un décalage des collages futurs.
- Les bounding boxes couvrent toute la hauteur du monde : zone_001 représente 81 395 712 cellules. Mesurer le budget mémoire du serveur sur la copie avant de lancer les grands lots ; rapporter tout refus WorldEdit sans réduire silencieusement la sélection.
- Les exemples historiques « 20 conteneurs » ne constituent pas un inventaire fiable : les nombres de la table du plan totalisent 23 machines (13+5+1+1+1+2). Fournir les positions et comptes réels des block entities plutôt que reprendre ce total comme une preuve.

## Données supplémentaires à conserver pour P5/P7/P10

- Exporter les NBT bruts des machines rencontrées, avec type de bloc, type de BE, coordonnées, dimensions et inventaires complets. Les fourneaux au prométhium réutilisent `minecraft:furnace`, `minecraft:blast_furnace`, `minecraft:smoker` et `minecraft:brewing_stand`.
- Conserver les champs des fours 26.3 (`cooking_time_spent`, `cooking_total_time`, `lit_time_remaining`, `lit_total_time`, `speed_multiplier`, `RecipesUsed`), le nom et `lock`. La cible 1.21.1 demande des adaptateurs ; l'égalité des noms de blocs ne suffit pas à prouver la compatibilité.
- Recenser **tous** les items et composants dans les inventaires de machines et de joueurs. Un item hors du périmètre sélectif doit être signalé avant tout collage ; il ne doit pas devenir de l'air ou un slot vide.
- Conserver séparément les SavedData `brain_inventory`, `brain_bag_ledger`, `brain_routines`, `work_state` dans toutes les dimensions concernées, ainsi que les notes du monde. Le schematic d'un servitor n'inclut pas ces fichiers.
- Conserver les UUID du servitor et les inventaires joueur. Ne pas supposer que WorldEdit `//copy -e` exporte le joueur ou ses objets.
- Recenser les noms de blocs, propriétés, biomes et entités dans les palettes Sponge. Des identifiants vanilla apparus après 1.21.1 peuvent être inconnus de la cible : leur conversion doit être décidée avant P10, jamais remplacée silencieusement.

## Retour attendu pour débloquer la suite

Retourner un bilan versionné ou accessible avec : empreinte de la sauvegarde source, 173 fichiers et empreintes, manifeste complété, contrôle de rechargement WorldEdit, dimensions/origines, cinq captures, inventaire/NBT des machines, inventaire des palettes et liste des SavedData conservés. Les mondes et schematics restent chez Hermes ; ne pas les ajouter au dépôt Git public.

P1 sera validée uniquement sur ces preuves. P2 et les phases suivantes restent non démarrées conformément au verrou séquentiel du plan. Le collage n'est autorisé qu'après les validations de machines et d'adaptateurs, puis P10.
