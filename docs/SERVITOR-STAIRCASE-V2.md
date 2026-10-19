# Servitor — escalier d'accès v2 (état au 2026-10-19)

Statut : **implémenté et partiellement validé — NON terminé**. Le moteur planifie et creuse
réellement des marches (avec reçus), mais la descente physique du compagnon dans le trou d'un
bloc reste bloquante. Détail en fin de document.

## 1. Audit de l'ancien modèle

`WorkStairs.java` (supprimé) planifiait des **paliers 3x3x3** :

- `floor()` exigeait une surface porteuse **3x3 complète** sous chaque palier (18 cellules à
  vérifier, 9 de sol) ;
- `clearance(from,to)` dégageait **deux empreintes 3x3 sur 2 niveaux** entre deux paliers ;
- `route()` ne progressait que par bonds de **3 blocs** avec ±1 de dénivelé, et refusait tout
  déplacement au-delà de `MAX_RISE` (8) ;
- `safe()` n'autorisait l'excavation que **dans la boîte de la mission**.

Diagnostics réels relevés sur le serveur (`mc-263`) :

```
[ACCESS_DIAG] start=-93,66,-3 target=-108,66,-29 floor=false safeStart=false rise=0
[STAIRS_ROUTE] north=bloc-90,68,-7:grass_block:zone=false  east=bloc-86,67,-2:bush:zone=true
              west=floorTo  south=ok
```

Trois familles de rejet :

| Règle | Symptôme | Cause |
|---|---|---|
| support 3x3 (`floor`) | `floor=false` dès une pente, une berge, une avancée | géométrie |
| `floorTo` | palier suivant sans sol 3x3 | géométrie |
| `zone=false` | blocs de corridor hors boîte mission | confinement de sécurité |
| `bush`, `birch_log` | végétation/bois non excavables | détection des matériaux |
| chunks déchargés | refus silencieux sans joueur connecté | chargement |

## 2. Nouveau modèle (`StairMining.java`, 467 lignes)

Une transition = une opération atomique, avec ses volumes séparés :

```java
record Step(Kind kind,        // DESCEND_STEP | WALK
            BlockPos from,    // palier de départ (pieds)
            BlockPos feet,    // palier d'arrivée (pieds)
            List<BlockPos> clear,    // volume de dégagement de CETTE transition
            List<BlockPos> support)  // appuis exigés, jamais minés
```

- `HEIGHT = 2` (1x2), `WIDTH = 2` (le servitor mesure 1.09 bloc : un tunnel 1 de large est
  infranchissable ; la constante reste réglable, 3 pour élargir) ;
- une marche descend **d'un bloc par bloc d'avance** : creuser la marche suivante ne retire jamais
  le support de la précédente ;
- les deux **côtés** du couloir sont essayés pour rester dans la zone autorisée ;
- l'entrée est cherchée sur une **colonne porteuse avec 2 blocs libres et une marge d'un bloc**
  (le tunnel 2 de large doit tenir entier dans la zone) ;
- support validé sous l'**empreinte de collision** réelle (`footprintSupported`, AABB du mob), pas
  sous toute la largeur du tunnel ;
- un bloc planifié n'est jamais considéré comme modifié : chaque cellule est **relue** avant et
  après la casse, et un refus de mutation invalide le plan.

## 3. Planification

- transitions `DESCEND_STEP` (1 avant / 1 bas) et `WALK` (même niveau, y compris le trajet
  d'approche depuis la position courante) ;
- garde-fous conservés : zone de mission, claims, hauteur, bordure, chunks voisins, fluides,
  blocs à entité, plafond qui tombe (`FallingBlock`), appui dans le volume interdit ;
- végétation et bois (tags `LOGS`/`LEAVES`, `BushBlock`) traversables ;
- pas de replanification infinie : `MAX_FAILURES = 4`, `forget()` sur perte d'appui ou cellule
  refusée, `MAX_STEPS = 96`, budget `WorkAccess.MAX_BLOCKS = 1024`.

## 4. Exécution

Boucle : observer → planifier → sécuriser (appuis) → creuser (1 bloc par tick, chemin de reçu
`WorkService.breakTarget(..., access=true)`) → vérifier → avancer → confirmer. Le retour est
préservé : chaque marche reste franchissable en sens inverse (sol conservé, 2 blocs de haut).

## 5. Tests exécutés (serveur réel, alpha.71)

| Test | Requête | Résultat |
|---|---|---|
| Reproduction de l'échec | `raw_iron` zone profonde, `access=excavate` | avant : `ACCESS_NO_SAFE_ROUTE`, 0 bloc ; maintenant : **plan 8 et 18 transitions** |
| Descente réelle | idem | 2 marches creusées + franchies, puis blocage mouvement |
| Terrain plat/herbe | `cobblestone` zone large | marche creusée (grass_block) et franchie |
| Forêt | zone avec bouleaux | troncs traversables (plus de refus `birch_log`) |
| Chunks | sans joueur connecté | plus de faux refus (tickets temporaires côté supply) |

Non testés individuellement : cavité, sable/gravier, liquide, retour par l'escalier.

### Trace réelle (extrait, 18 transitions planifiées)

```
[STAIR] plan 18 transitions depuis BlockPos{x=-112, y=65, z=21} vers BlockPos{x=-120, y=47, z=20}
[STAIR] franchi marche 1/18 feet=-118, 64, 19
[STAIR] marche=2/18 kind=DESCEND_STEP mine=-117, 63, 19 bloc=minecraft:grass_block from=-118, 64, 19 feet=-117, 63, 19
[STAIR] marche=2/18 kind=DESCEND_STEP mine=-117, 63, 20 bloc=minecraft:grass_block from=-118, 64, 19 feet=-117, 63, 19
[STAIR] franchi marche 2/18 feet=-117, 63, 19
… puis NAVIGATION_STALLED en -124, 62, 12
```

## 6. Limites restantes

1. **Descente dans le trou** : après avoir creusé le palier, la navigation vanilla refuse de faire
   descendre le mob d'un bloc dans un puits de 2 de large (`NAVIGATION_STALLED`). C'est le blocage
   principal ; piste : mouvement de descente dédié (marche avant + détection de chute) plutôt que
   `PathNavigation`.
2. **Placement d'appui** (`PLACE_SUPPORT`) : refusé si l'appui manque (aucun comblement
   automatique) — conforme à « placement légal + ressources + risque contrôlé », mais non implémenté.
3. **Corridor dans la zone** : la sécurité confine l'excavation à la boîte de la mission ; une zone
   trop étroite fait échouer le plan (message explicite : bloc + règle violée).
4. Tests terrain manquants listés ci-dessus.
