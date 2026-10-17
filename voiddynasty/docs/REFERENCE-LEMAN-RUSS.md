# Référence — Leman Russ Battle Tank

**Source :** planche technique fournie par Donopot le 14 octobre 2026 (archivée :
`docs/references/lemunruss-planche-technique.jpeg`).

> « LEMAN RUSS BATTLE TANK — ANVILUS IMPERATOR — 3RD COMPANY OF THE 124TH CADIAN ARMOURED REGIMENT »

C'est la **première source chiffrée** du chantier : les dimensions du char étaient jusqu'ici des
valeurs de conception déclarées, faute de données. Cette planche les remplace, et elle a servi à
deux choses distinctes :

1. **remettre à l'échelle le modèle de la source** utilisé par l'entité `leman_russ` ;
2. **générer un modèle voxel** construit directement sur ces cotes
   (`tools/generer_voxel_lemanruss.py`).

---

## 1. Données techniques (relevées à la lettre)

| Grandeur | Valeur |
|---|---|
| Poids | 61 tonnes |
| **Longueur de caisse** | **7,08 m** |
| **Largeur de caisse** | **4,86 m** |
| **Hauteur** | **4,42 m** |
| **Longueur du canon** | **3,07 m** |
| Garde au sol | 0,45 m |
| Blindage | 45 – 200 mm |
| Vitesse max | **29 km/h sur route / 19 km/h hors route** |
| Équipage | 6 (conducteur, chef de char, tireur, chargeur, 2 servants de bolters latéraux) |
| Armement | canon de bataille en tourelle, lascanon en caisse, 2 bolters lourds latéraux |
| Munitions | 36 obus en magasin protégé, 3 types (AT, Conqueror, etc.) |
| Passage à gué | 1,9 m |

Autres cotes lues sur le dessin : canon principal **120 mm à âme lisse**, élévation **−10° à +32°**,
anneau de tourelle **360°**, moteur **Lucifer 1500 à turbine à gaz**, périscope du tireur 45°,
fente de vision du conducteur 90°, bolters de flanc « Gryphonne IV » 90°, lame de bulldozer tous
terrains, amortisseurs à ressorts d'acier.

## 2. Nomenclature (annotations de la planche)

`trappe du chef de char` · `fente de vision du conducteur (90°)` · `bolters lourds Gryphonne IV
(90°)` · `trappe d'accès de la boîte de désignation` · `lance-grenades fumigènes à trois tubes` ·
`chenilles tous terrains à verrouillage` · `périscope du tireur (45°)` · `canon principal 120 mm` ·
`moteur Lucifer 1500` · `anneau de tourelle rotatif (360°)` · `trappe d'équipage` · `lascanon
Lucius sur affût 90°` · `lame de bulldozer` · `amortisseurs`.

Ces noms servent à **nommer les pièces** du modèle voxel (chaque groupe de voxels porte le nom de
l'annotation correspondante).

## 3. Conversion en blocs Minecraft

**Convention : 1 bloc = 1 mètre.** Ce n'est pas arbitraire : dans Minecraft le joueur mesure
**1,8 bloc**, l'échelle usuelle est 1:1. Un char de 4,42 m doit donc mesurer **4,42 blocs** — deux
joueurs et demi de haut.

| Grandeur | Cible en blocs |
|---|---|
| Largeur (X) | **4,86** |
| Hauteur (Y) | **4,42** |
| Longueur de caisse (Z) | **7,08** |
| Canon | **3,07** |
| Garde au sol | **0,45** |

## 4. Ce que la planche dit du modèle source — et de mon erreur

Comparaison des **rapports** (indépendants de l'échelle) :

| Rapport | Planche | Modèle source | Écart |
|---|---|---|---|
| Longueur / largeur | 1,457 | 1,515 | **+4 %** |
| Longueur / hauteur | 1,602 | 1,639 | **+2,3 %** |
| Largeur / hauteur | 1,100 | 1,082 | **−1,6 %** |

**Conclusion : les proportions de la source sont justes à 4 % près.** Le modèle n'était pas
« trapu » : il était **deux fois et demie trop petit**.

> **Erreur corrigée.** Après avoir reçu une photo de figurine, j'avais étiré le modèle de **30 % en
> longueur** en croyant ses proportions fautives. La lecture de la photo était fausse
> (perspective) ; la planche chiffrée la dément : cet étirement portait le rapport longueur/hauteur
> à **2,13** au lieu de 1,60. L'étirement est **annulé**, l'option `--allonger` supprimée du
> convertisseur.

## 5. Transformation appliquée au modèle de la source

**Homothétie uniforme de 2,4153** (= 4,42 / 1,83, calée sur la hauteur de la planche), appliquée
dans `tools/convert_modele_java.py` au même endroit que toutes les autres transformations — donc
aux coins des cubes **et** aux origines de rotation.

L'homothétie est **uniforme et non par axe**, délibérément : un facteur différent par axe
déformerait les angles des 12 cubes pivotés du modèle (une anisotropie ne commute pas avec une
rotation). L'écart résiduel est de 2 %, invisible, et la géométrie reste exacte.

| | Largeur | Hauteur | Longueur |
|---|---|---|---|
| Mesuré après transformation | 4,77 | **4,42** | 7,25 |
| Cible planche | 4,86 | 4,42 | 7,08 |
| Écart | −1,8 % | 0 % | +2,4 % |

Conséquences appliquées dans le même mouvement :

- **hitbox** `ModEntities.LEMAN_RUSS` : 3,9 × 1,9 → **4,77 × 4,42** (mesurée sur le modèle, plus
  choisie à part) ;
- **siège du conducteur** mis à l'échelle par la même constante (`ECHELLE_PLANCHE`) ;
- **champ de contrôle** `V-lemanruss-pilotable` mis à jour sur la mesure, sinon il échoue à juste
  titre.

### Compromis assumé sur la hitbox

Une hitbox Minecraft est une **boîte alignée sur les axes, qui ne tourne pas**. Le char mesure 4,77
de large mais **7,25 de long** : une boîte carrée de 7,25 bloquerait le passage partout. La boîte
retenue (4,77) est fidèle en largeur mais **ne couvre pas le museau et la queue sur environ 1,2 bloc
de chaque côté**. Alternatives : boîte pleine (7,25 × 4,42) ou intermédiaire. **Décision à prendre,
déclarée ici et non cachée.**

## 6. Modèle voxel généré (outil `tools/generer_voxel_lemanruss.py`)

Base de départ : le script fourni par Donopot. Son écriture des chunks `.vox` est conservée telle
quelle ; **le modèle a été refait**, parce que dans la base les axes étaient mélangés (la coque
montait comme un mur vertical, le canon partait vers le bas, la tourelle n'était pas posée sur la
caisse).

Repère MagicaVoxel : **X largeur, Y hauteur, Z longueur** (l'avant vers les Z croissants).
Résolution par défaut : **0,15 m par voxel** (`--resolution`).

### Mesures du fichier écrit — relu, pas cru sur parole

Le contrôle **relit le `.vox` produit** et remesure chaque pièce :

| Contrôle | Cible planche | Mesure | Écart |
|---|---|---|---|
| Largeur hors chenilles | 4,86 m | **4,80 m** | −0,06 m |
| Hauteur totale | 4,42 m | **4,35 m** | −0,07 m |
| Longueur de caisse | 7,08 m | **7,05 m** | −0,03 m |
| Canon principal | 3,07 m | **3,00 m** | −0,07 m |

Palette : les **5 tons de Donopot** sont conservés ; un **6e** a été ajouté, gris acier
`(96, 98, 104)`, pour les maillons et les moyeux — sans lui, ils se confondent avec la bande
sombre et la chenille ne se lit pas.

**Chenilles** (reprises après retour de Donopot : « revois les chenilles pour plus ressembler au
Leman Russ »). La vue de profil de la planche montre une **boucle** aux deux extrémités arrondies
(grand barbotin à l'avant, galet tendeur plus petit à l'arrière), un **brin supérieur qui remonte
vers l'avant**, une **rangée de 7 petits galets de route** en bas, **5 disques de suspension** à
ressorts au-dessus, et une **ligne de maillons** tout du long. Les premières chenilles étaient un
pavé à bouts carrés — refaites sur ces cinq traits.

Limite assumée : à **0,15 m par voxel**, un cercle de voxels ne peut pas être rond. La rangée de
galets donne la lecture ; une résolution de 0,10 m les rendrait circulaires (à décider).

`20 328 voxels` répartis en pièces nommées : chenilles (boucle, galets, suspensions, maillons), caisse
(glacis + flancs en escalier), jupes de blindage latérales, sponsons et leurs bolters, tourelle
(chanfreinée, coupole, écouteilles, lance-grenades fumigènes), canon principal (masque, fourreau,
frein de bouche), lascanon de caisse, lame de bulldozer, échappements Lucifer.

### Épreuve négative

Un contrôle qui ne peut pas échouer ne prouve rien : l'outil sait **saboter** le modèle
(`--epreuve-negative`, canon raccourci de 0,90 m) et le contrôle doit alors échouer. Vérifié :

```
canon principal (planche 3,07 m) : mesure 2,55 m, ecart -0,52 m   -> FAUX  (attendu)
=> epreuve negative : le controle SAIT echouer (correct)
```

Aperçu : `python3 tools/apercu_vox.py --vox voxels/leman_russ_model.vox --vues troisquarts,profil,face`
(rendu isométrique qui **relit le fichier**, donc ce qu'on voit est bien ce qu'il contient).

## 7. Écarts et points ouverts

- **Canon du modèle de la source** : la planche donne 3,07 m, soit 43 % de la caisse. Les pièces
  fines qui y ressemblent mesurent **1,47 bloc** après transformation : environ deux fois trop
  court. À reprendre pièce par pièce (nomenclature ci-dessus).
- **Garde au sol du modèle de la source** : 0,45 m attendus (10 % de la hauteur) ; les chenilles y
  descendent proportionnellement plus bas.
- **Socle** : le modèle de la source porte un élément de type caillebotis sous l'avant, présent
  aussi sur les deux chasseurs — très probablement un socle de présentation hérité du modèle de
  décor. Identification en cours.
- **Hitbox** : compromis ci-dessus à trancher.
- **Vitesse, blindage, équipage, munitions** : extraits mais **non convertis**. Le passage des km/h
  en blocs/tick dépend du chemin de calcul du jeu : il sera **mesuré**, pas déduit. Aucun chiffre
  inventé.

## 8. Note de méthode

Le **dessin** de la planche est une illustration, pas un plan à l'échelle : son propre rapport
longueur/hauteur mesure environ 1,3, contre 1,60 dans son tableau de données. **C'est le tableau qui
fait foi.** Le dessin sert à la nomenclature et à la lecture de la silhouette.
