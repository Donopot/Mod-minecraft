# Assets à mettre à jour après alpha 22

**Suivi alpha 25 :** les cinq transformations prioritaires et la passe Gomu/Gura/Ope/Mochi/Nikyu sont intégrées sur `codex/onepiece-priority-assets`. [Portée, validation et limites](PRIORITY-ASSETS-WORKORDER.md). Les paragraphes ci-dessous conservent l'inventaire préalable : la présence d'un candidat n'implique pas que tous ses compléments (hybride, Photon, forme Gear) aient été importés.

Recherche du 18 octobre 2026, comparée au JAR alpha 22 `db2e88e42a3257659e28a4f5cb836a080b8aa274fbddb7b532854ee90fa27f2c` et à la [revue des ressources actuelles](ASSETS-MINEPIECE-40.md). **40 fruits examinés, 14 avec des candidats retenus. Aucun asset ni JAR jouable modifié pendant cette recherche.**

La nouvelle réserve `trueprimepiecetwo-infusedrocknroll.jar` contient 172 géométries Gecko et 380 effets Photon. Les quatre réserves complémentaires True Prime Piece Two, Mine Mine no Mi 0.11.6, ompworld 1.3.48 et MinePieceXMineNoMi 0.1.13 ont été inventoriées. Les références MinePiece ver15 restent la base déjà revue ; un nouveau nom de fichier ne justifie pas à lui seul un remplacement.

[Décisions pour les 40 fruits en CSV](ASSETS-A-METTRE-A-JOUR.csv) · [Fichiers candidats et métadonnées en JSON](ASSETS-CANDIDATS-ALPHA22.json)

## Premier lot : cinq transformations

Ces cinq ensembles ont un modèle, une texture et une animation reliés par les classes de la référence. Les méthodes des cinq GeoModel principaux ont aussi été relues par désassemblage. Les parents et noms d’os sont valides, les 31 pistes ciblent des os présents et ont des durées finies positives. Sept fichiers de texture référencés ont été décodés et leur alpha contient des pixels visibles. Cela confirme un ensemble exploitable à adapter ; le rendu dans Fabric 26.3 et la qualité artistique ne sont pas encore validés.

Les chemins courts ci-dessous sont sous `assets/trueprimepiecetwo/` dans la référence privée.

| Priorité | Fruit | Modèle | Ensemble et intérêt |
|---|---|---|---|
| 1 | Goro / Amaru | `geo/amaru.geo.json` — 16 os, 38 cubes | `animations/amaru.animation.json`, 8 pistes : idle, course, attaque, tir, vol, Mamaragan, click, Raigo. `textures/entities/amaru.png` 512² ; couche lumineuse `textures/entities/amaruu.png` confirmée dans AmaruReworkLayer. Remplacement possible de l’adaptation actuelle issue du Daibutsu. |
| 1 | Phoenix complet | `geo/torifullform.geo.json` — 21 os, 45 cubes | `animations/torifullform.animation.json`, 9 pistes dont transformation, envol, Blue Bird et vol. Texture entité `textures/entities/torifullform2new.png` 512² ; la variante armure référence `textures/item/torifullform2.png`. Ensemble complet à comparer aux ailes additionnelles actuelles. |
| 1 | Bouddha | `geo/buddha.geo.json` — 8 os, 18 cubes | `animations/buddha.animation.json`, 7 pistes dont onde, paume, stomp et rupture. Le modèle d’armure référence `textures/item/buddha.png` 256². Permet de remplacer plusieurs poses génériques par des gestes dédiés. |
| 1 | Loup | `geo/wolfbeast.geo.json` — 11 os, 21 cubes | `animations/wolfbeast.animation.json`, 4 pistes : idle, course, attaque et dash. Le modèle d’armure référence `textures/item/wolfbeast.png` 64² ; une texture entité existe aussi. Candidat cohérent pour le quadrupède complet. |
| 1 | Léopard | `geo/leopardbeastrework.geo.json` — 11 os, 14 cubes | `animations/leopardbeastrework.animation.json`, 3 pistes : idle, course, attaque. `textures/entities/leopardbeastrework.png` 128² ; couche lumineuse `leopardbeastreworkglow.png` confirmée. L’hybride `leopardhybridrework` et ses 10 pistes sont un complément distinct. |

**Phoenix : la queue brun-jaune a la bonne couleur, confirmée par l’utilisateur.** Ce candidat constitue une alternative de modèle et d’animation, pas la correction d’un défaut de couleur des ailes ou de la queue. Les variantes de texture doivent être comparées dans leur contexte, sans remplacer une couche par une texture de nom ressemblant : ToriReworkLayer emploie actuellement `invisible.png`, pas automatiquement `torinewglow.png`.

## Deuxième lot proposé : Gomu, Gura, Ope, Mochi, Nikyu

| Fruit | Ressources retenues | Ce qu’elles peuvent améliorer et ce qui reste à adapter |
|---|---|---|
| Gomu | `gumpistol`, `gumgatling`, `gear2stuff`, `gearthree` ; modèles Java `Modelgear5`, `Modelcloudring`, `Modelcloudring2` | Bras spécialisés, rafales et overlays des Gears. `g4luffy` possède 9 pistes mais appartient à un **PNJ Luffy** : conserver le skin du joueur exige une adaptation. `Modelgear5` ne prouve pas l’existence d’une forme complète Gear 5 déjà animée et prête. Les gestes Gear 3 peuvent servir aux attaques sans ajouter silencieusement un nouvel état de transformation au catalogue. |
| Gura | `guracrack`, `guracrackground`, `guracracksea`, `gurauppercut` ; FX `gurafissure`, `guraglass`, `gurawave1` | Fissures d’air, de sol et de mer différenciées. Garder les volumes de dégâts et la destruction définitive du serveur ; le visuel ne doit pas déclencher une deuxième explosion de blocs. |
| Ope | `takt1` à `takt6`, modèle Java `Modelamputateproj` ; FX `roomopen`, `proom50x50`, `shambles` | Rochers du StoneOpe actuel, ouverture du ROOM et permutation plus lisibles. Injection Shot et Gamma Knife existent dans la réserve, mais ne correspondent pas aux quatre techniques actuelles : ne pas les présenter comme de simples remplacements d’assets. |
| Mochi | `buzzcut` — 6 os, 73 cubes ; modèles Java `Modelmochifist`, `Modelmochihands`, `Modelmochigun`, `Modelyakimochi` | Poings et bras spécialisés ; Buzz Cut pour Zangiri. Les donuts ver15 déjà utilisés restent distincts d’un donut roulant : éviter d’attribuer le mauvais modèle à Muso Donatsu. |
| Nikyu | `ursusshock`, ses 2 pistes et sa texture visible ; modèle Java `Modelpaw` ; FX `ursuskaboom`, `ursusvromm`, `pawpuff` | Empreinte, compression et impact d’Ursus Shock ; traînées de pattes à adapter. `painpaw` et `paweffectt` utilisent une texture totalement transparente : leur géométrie seule ne remplace pas un projectile visible. |

## Comparaisons optionnelles ensuite

- **Pika** : `pikamirror` pour Yata no Kagami, `Modelpikasword` et impacts `pikaswordhit`. L’épée actuelle est déjà celle de ver15 ; comparer avant de la remplacer. Les clones lumineux de la réserve ne deviennent pas une nouvelle technique implicitement.
- **Mera** : `hikenfistt`, `flamepillar`, `entai`, avec leurs textures et pistes. Préserver le dégagement de caméra de l’alpha 21 et comparer l’effet de feu à celui déjà poli.
- **Magu** : `Modelmagmafist`, `Modelmagmahandrender`, `Modelmagmahound` et `3dmagmatest`. Alternatives à comparer aux poings/chien actuels, déjà repris des originaux. Les impacts cosmétiques ne changent pas les règles de terrain permanent.
- **Seiryu** : `bolobreathbeam` avec `newbolob.png`, `Modeldragonwindblade` et recettes de souffle/lames de vent. **Le modèle du dragon, son nouveau squelette et son pilotage restent conservés sur demande.** Seules les attaques sont concernées par cette recherche.

## Ressources à ne pas copier seules

1. **Phoenix hybride** : la piste `fly` de `torihybrid.animation.json` vise `Body`, absent de `torihybrid.geo.json`. Il faut résoudre l’intention de cette piste ou en choisir une autre correctement reliée ; aucun renommage d’os au hasard.
2. **Explosion Entai** : EntaiexplosionModel référence `animations/entaiboooooooom.animation.json`, absent de l’archive. Le modèle `entai` normal est un candidat distinct dont les pistes existent.
3. **Ancien Léopard** : `leopardfullform.geo.json` existe, mais aucune liaison par chemin exact vers ce modèle n’a été retrouvée. Le modèle `leopardbeastrework` est relié explicitement à un renderer. L’absence de référence exacte n’est pas une preuve générale que le fichier ne sert jamais.
4. **Carriers invisibles** : `painpaw`, `paweffectt`, `elthor` et `elthor2` reposent sur des effets associés. `textures/entities/invisible.png` a un canal alpha entièrement vide. Le mesh seul n’améliorera pas leur visibilité.
5. **Photon** : les 380 `.fx` ont été lus comme NBT compressé, mais notre paquet Fabric n’embarque pas leur moteur. Plusieurs recettes demandent aussi des textures externes, par exemple `photon:textures/particle/laser.png` et `ring.png` pour l’aura Amaru. Ces fichiers ne sont ni dans le nouveau JAR ni dans le pack actuel. Il faut traduire les émetteurs, matériaux, courbes et sprites dans notre renderer, ou porter leur dépendance séparément. Une recette présente dans l’archive n’est pas forcément appelée par son code : les liens non retrouvés restent des pistes de recherche.

Les animations de transformation source, les animations corporelles PAL du joueur et les FX d’attaque sont trois composants à raccorder. Un fichier `.animation.json` d’entité ne remplace pas automatiquement une animation PAL.

## Ce qui reste sans remplacement approprié établi

Makami, Tigre à dents de sabre, Girafe, Batto, Ptéranodon, Brachiosaure et Pachycéphalosaure gardent les formes/références actuelles. Le nouveau JAR fournit notamment un Allosaure et un PNJ Yamato, qui ne représentent pas ces formes complètes. Pour Soru, Napoleon est une arme et ne résout pas la revue des visages de Zeus/Prometheus. Perona est un PNJ et ne remplace pas les fantômes de Horo.

Les 26 fruits sans candidat de remplacement retenu pour cette passe gardent leurs assets actuels ; cela ne constitue pas une nouvelle acceptation artistique de leur rendu. Les ressources déjà réutilisées depuis Mine Mine no Mi ne sont pas comptées comme de nouvelles découvertes.

## Reproduire l’inventaire

`python tools/inventory_reference_visuals.py --current paquet-actuel.jar --reference reference.jar --output inventaire.local.json`

L’outil lit ZIP, JSON, constantes de classes et NBT, sans charger le code des références. Il signale les liaisons par chaînes de ressources et les os d’animation manquants ; il ne remplace pas la revue des méthodes ni un test de rendu. Les dumps de désassemblage, le décodage des textures et l’inventaire complet des quatre archives sont conservés localement dans `.runtime/onepiece/asset-upgrade-audit/` du workspace. Empreinte de la nouvelle référence : `328b8ea70071750b654b07e8a03bccbcec66a155680269254b03a01de7de9db0`.

La prochaine intégration peut traiter le premier lot de cinq, puis ses tests visuels groupés, conformément au rythme demandé. Cette recherche n’a pas lancé de jeu, changé de terrain, créé de nouveau JAR ni importé les ressources privées dans Git.
