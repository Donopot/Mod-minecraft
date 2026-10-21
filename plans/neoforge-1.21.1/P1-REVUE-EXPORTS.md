# Revue des exports P1 — 21 octobre 2026

Références examinées : `b10a0d5` et `a807882` sur `neoforge`. Les chiffres du
[journal Hermes](JOURNAL-EXECUTION.md) sont des résultats rapportés depuis son serveur.
Codex n'a ni ouvert ce monde ni reçu les schematics réels. **P1 reste en attente de
validation ; P2 n'est pas démarrée.** Le démarrage du pack tiers ne valide pas P2 :
le mod VoidDynasty NeoForge n'existe pas encore comme mod exécutable.

## Défauts constatés dans les scripts reçus

| Défaut | Conséquence | Correction dans le dépôt |
|---|---|---|
| `Version=3` avec `Palette/BlockData` directement à la racine | Le contrôle local acceptait une structure différente du schéma Sponge v3 | Compound `Schematic`, conteneur `Blocks` avec `Palette/Data/BlockEntities`, inventaires sous `BlockEntities[].Data` ; vérification stricte |
| Écriture des blocs par chunk, puis Y/Z locaux | Le nombre de blocs est correct mais leurs positions changent dès qu'une zone couvre plusieurs chunks | Index global `x + z*Width + y*Width*Length`, testé sur plusieurs axes, sections, bornes négatives et palettes >256 |
| Perte des types NBT à la lecture et inférence à la réécriture | `Slot` byte devient int, tableaux UUID deviennent listes, float devient double | Lecture typée ; conservation des payloads NBT d'origine pour les inventaires et autres données |
| Registre vanilla et remplacement systématique des inconnus par air | Suppression des machines VoidDynasty ; filtrage des BE par nom de bloc, pourtant distinct de leur type | Refus des états inconnus ; aucun remplacement par air ; aucun filtrage des BE ; substitution sous une BE refusée sans adaptateur |
| Erreurs de chunk ignorées, ancien fichier réutilisé sur sa taille seule | Une lecture ratée peut être présentée comme une zone vide, un ancien export invalide comme terminé | Erreur explicite sur chunk corrompu/compression non prise en charge ; fichiers existants conservés et nouveau dossier exigé |
| Aucun export des entités ni des biomes | Le servitor et les biomes ne sont pas conservés par cet extracteur | Sorties signalées comme **prévisualisations incomplètes**, jamais comme PASS P1 |

Format de référence : [spécification officielle Sponge v3](https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-3.md).
Le seul changement de palette ne convertit pas les composants d'items, les données
d'entités ou les biomes 26.3 vers 1.21.1. La DataVersion source est conservée ; aucun
faux downgrade n'est déclaré.

## Reprise à effectuer par Hermes

1. Conserver les exports bruts et remappés actuels et leurs empreintes. **Ne pas
   coller `migration-schematics-1211`** : il peut déjà manquer des machines et leurs
   inventaires. Le mode `//paste -a` ne répare ni ces pertes ni les positions erronées.
2. Repartir de la copie sauvegardée du monde source, jamais d'un schematic déjà
   remappé. Privilégier les exports WorldEdit complets du [relais P1](HERMES-P1-EXPORTS.md).
   L'extracteur Python corrigé sert à examiner les blocs/BE dans **un nouveau dossier** :

   ```sh
   python3 scripts/world-migration/extract_zones.py COPIE_MONDE plans/neoforge-1.21.1/annexe-zones-export.csv NOUVEAU_DOSSIER --zone zone_001
   python3 scripts/world-migration/verify_schem.py NOUVEAU_DOSSIER/zone_001.schem
   ```

   Le résultat `structure_pass=true` signifie uniquement que la structure est
   cohérente. `p1_pass=false` et `paste_allowed=false` restent explicites. Les
   prévisualisations Python n'exportent toujours pas les entités et les biomes.
3. Justifier individuellement les **14 zones sans contenu lu** : chemin/dimension,
   présence et statut des chunks, comparaison à la copie source. Elles ne sont pas
   prouvées vides par le journal. La réduction de hauteur, de 384 à 320 blocs pour
   zone_001, doit aussi être documentée ; le CSV reste la sélection de référence.
4. Fournir le bilan des **173 zones** et leurs empreintes, les rechargements
   WorldEdit, les cinq comparaisons visuelles, les origines, les comptes/NBT des
   machines, le servitor et ses SavedData externes. Voir le relais pour la liste.
5. Pour une future conversion de palette, fournir les **états exacts** du registre
   cible, propriétés comprises et avec les états du mod, au format `reports/blocks.json`.
   Une liste vanilla de noms de blocs ne suffit plus. Exemple d'interface :

   ```sh
   python3 scripts/world-migration/remap_schem.py --version 1.21.1 --registry REGISTRE_CIBLE.json --subst SUBSTITUTIONS_REVUES.json --out NOUVELLE_SORTIE --dir EXPORTS_VALIDES
   ```

   Aucun téléchargement de registre approximatif ni substitution automatique.
   En P1, le registre complet VoidDynasty cible n'est pas encore disponible : les
   originaux restent la référence. Le collage attend P5/P10.

## Validation locale des corrections

25 tests Python réussis : les 12 contrôles P0/préparation existants et 13 tests de
migration avec données synthétiques. Vérification des payloads d'inventaires,
entités, UUID, biomes, Offset et DataVersion avant/après remappage ; géométrie sur
plusieurs chunks et refus des formats/indices corrompus. Contrat P0 inchangé :
88 identifiants et 24 empreintes de sources vérifiés.

Ces tests ne prouvent pas la validité des fichiers déjà produits sur le serveur.
