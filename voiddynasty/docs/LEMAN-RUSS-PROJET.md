# Leman Russ — projet, repris du début

Décision de Donopot, 16 octobre 2026 : « redémarre le projet du leman russ du début ».

Ce document remplace tous les essais précédents. Il dit **ce qu'on construit, d'après quoi, et
comment on juge**. Les fichiers des essais abandonnés sont déplacés dans `voxels/archive/`
(rien n'est supprimé).

---

## 1. Les sources, et ce que chacune donne

| Source | Ce qu'on en prend |
|---|---|
| **Planche technique** (`docs/references/lemunruss-planche-technique.jpeg`) | les **cotes** et la **nomenclature** |
| **Rendu 3D tournant** (`docs/references/lemunruss-rendu-3d.gif`, 61 images) | la **forme** des pièces, à comparer à chaque étape |
| **Script de Donopot** (palette + camouflage + intention) | les **6 tons Cadian** et l'esprit du modèle |

## 2. Les cotes (planche, 1 bloc = 1 m)

caisse **7,08 m** · largeur **4,86 m** · hauteur **4,42 m** · canon **3,07 m** · garde au sol
**0,45 m**. Chenilles : bandeau d'environ **1,8 m**, soit ~40 % de la hauteur (mesuré sur la vue
de profil de la planche et confirmé sur le rendu).

Grille de travail : **0,12 m par voxel** → **41 large × 80 long × 37 haut**.

Repère : **X** = largeur, **Y** = longueur (l'avant vers les petits Y), **Z** = hauteur.
L'écriture `.vox` échange Y et Z, MagicaVoxel voulant Y vers le haut.

## 3. Les pièces (nomenclature de la planche)

`chenilles tous terrains` · `caisse et glacis` · `jupes de blindage` (2 trappes + rivets) ·
`sponsons Gryphonne IV` (bolters lourds) · `tourelle` (caisson chanfreiné) · `coupole du chef de
char` (3 antennes) · `lance-grenades fumigènes` · `masque et canon 120 mm` (frein de bouche) ·
`lascanon Lucius de caisse` · `lame de bulldozer` · `moteur Lucifer 1500` (échappements).

## 4. La règle de travail

**Une pièce à la fois.** Pour chaque pièce :

1. on la pose sur les cotes de la planche ;
2. on la compare au **rendu 3D** ;
3. on mesure le gain avec la **note de ressemblance** ;
4. on commit.

## 5. Le juge : `tools/ecart_profil.py`

Il compare le profil du modèle au profil **dessiné** de la planche, cellule par cellule, et rend
deux nombres : **MANQUANT** (matière absente) et **EN TROP** (matière en trop), plus le détail par
dixième de longueur. **Objectif : au moins 90 % du dessin recouvert**, et « en trop » proche de
zéro, avant de montrer quoi que ce soit.

## 6. Le rendu : `tools/rendu_gif.py`

Rotation du modèle, cadre fixe, sortie GIF — pour comparer à la rotation de Donopot.

## 7. Ordre de construction

```
1. blocage général        les volumes aux bonnes proportions, sans détail
2. chenilles              bandeau, extrémités arrondies, galets, maillons
3. caisse et glacis
4. jupes de flanc         2 trappes, rivets
5. tourelle               caisson chanfreiné, coupole, 3 antennes, fumigènes
6. masque et canon        frein de bouche
7. avant                  lascanon de caisse, lame de bulldozer
8. arrière                échappements
9. finitions              rivets, optiques, insigne
```
