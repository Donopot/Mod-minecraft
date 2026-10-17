# Vérification des assets — VOID DYNASTY

**Question posée** : nos assets sont-ils complets ?

**Réponse** : **oui, sur tous les contrôles bloquants** — avec un avertissement qui relève de l'œil,
pas de la structure, et **un défaut réel trouvé puis corrigé**.

Date : 14 octobre 2026 · Minecraft 26.3 · outil : `tools/audit_assets.py` (rejouable à tout moment).

---

## Pourquoi ce contrôle existe

Un serveur de test **ne charge jamais les assets client**. Une texture manquante, un fichier
`items/<id>.json` absent ou un clip d'animation mal nommé passent donc **tous les tests serveur** et
ne se voient que sur ton écran — après un aller-retour manuel. Ce contrôle-ci lit les fichiers et le
code directement, en quelques secondes.

Il travaille dans les **deux sens** :

- ce que le code déclare → le fichier existe-t-il ?
- ce qui existe sur le disque → est-ce déclaré quelque part ?

---

## Résultat

| Contrôle | Résultat |
|---|---|
| Objets : `items/<id>.json` (exigence 26.3) | **93/93** |
| Objets : `models/item/<id>.json` | **93/93** |
| Objets : textures résolues | **84/84** |
| Blocs : `blockstates/<id>.json` | **23/23** |
| Blocs : modèles + objets de bloc | **23/23** |
| Blocs : blockstates pointant vers un modèle réel | **24/24** |
| Blocs : textures résolues | **35/35** |
| Créatures : modèle géométrique GeckoLib | **12/12** |
| Créatures : fichier d'animation | **12/12** |
| Créatures : texture | **12/12** |
| Créatures : renderer client | **15/15 entités** |
| Clips d'animation demandés par le code | **36/36** |
| Langues : objets + blocs traduits | **93/93 et 23/23**, en anglais **et** en français |
| Cohérence entre les deux fichiers de langue | 147 clés identiques |
| Recettes : valides et au bon format | **78/78** |
| Sons présents | **13/13** |
| Textures : dimensions multiples de 16 | **131/131** |
| Icônes d'objets avec canal alpha | **toutes** |
| Atteignabilité en créatif | **87/87** |
| Définitions d'armure et leurs textures | **2/2**, 18 textures présentes |

---

## Le défaut réel trouvé, et corrigé

**L'entité « servo-crâne » n'avait pas de nom.** L'*objet* en avait un (« Servo-crâne »), l'*entité*
non : la clé `entity.voiddynasty.servo_skull` manquait dans **les deux** fichiers de langue. En jeu,
le nom affiché aurait été la clé brute (`entity.voiddynasty.servo_skull`) au lieu d'un nom lisible.

Corrigé dans `en_us.json` (« Servo-skull ») et `fr_fr.json` (« Servo-crâne »), en reprenant
exactement le nom déjà utilisé par l'objet — pour que les deux s'accordent.

---

## L'avertissement qui reste : 43 textures très pauvres

43 des 131 textures font moins de 250 octets. Ce sont, au choix, des aplats ou des dessins
quasi uniformes : blocs de céramite colorés, cellules d'énergie, lingots, œufs d'apparition, quelques
machines.

**Ce n'est pas un défaut de complétude** — toutes ces textures existent, sont aux bonnes dimensions
et sont correctement référencées. C'est une question de **rendu**, et le rendu, c'est ton œil, pas le
mien. La liste complète sort à chaque exécution de l'outil :

```
python3 tools/audit_assets.py
```

Les plus pauvres : `block/promethium_smoker.png` (225 o), `block/voiddynasty_block.png` (116 o),
`block/super_chest.png` (102 o), les onze blocs de céramite (290–398 o).

---

## Le point qui mérite d'être connu : les deux formes de référence de texture

En écrivant ce contrôle, je m'y suis cassé les dents — et c'est une information utile :

- un **modèle d'objet** écrit `voiddynasty:item/boltgun`, chemin **relatif à `textures/`** ;
- un **modèle GeckoLib** écrit `voiddynasty:textures/entity/carnifex.png`, chemin **relatif à la
  racine des assets**.

Les deux formes coexistent dans ce mod, et elles sont toutes les deux justes. Un contrôle qui compare
l'une avec la convention de l'autre déclare 12 textures manquantes qui existent très bien. C'est
exactement ce que mon outil a fait avant correction.

---

## Ce que ce contrôle ne peut PAS vérifier

Dit franchement, pour que « complet » ait un sens précis :

1. **L'apparence réelle.** Que la texture soit jolie, cohérente, au bon endroit sur le modèle — seul
   un rendu à l'écran le montre.
2. **Le rendu des modèles 3D.** Qu'un modèle d'arme s'affiche correctement en main ne se vérifie
   qu'en jeu.
3. **Le servo-crâne sans clip littéral.** Son animation est pilotée dynamiquement (il utilise le
   modèle du Vex), donc `thenLoop("…")` n'apparaît nulle part à lire : 1 entité sur 13 non
   vérifiable statiquement. Ce n'est pas une anomalie, c'est une limite de la méthode.

---

## L'outil peut échouer — et c'est prouvé

Un contrôle qui ne sait pas dire non ne vaut rien. Trois sabotages ont été commis volontairement,
puis défaits :

| Sabotage | Résultat attendu | Constaté |
|---|---|---|
| Suppression d'une texture d'objet référencée | échec | **détecté**, code de sortie 1 |
| Suppression d'une définition `items/<id>.json` | échec | **détecté**, code de sortie 1 |
| Renommage d'un clip d'animation | échec | **détecté**, code de sortie 1 |
| Suppression d'une texture de créature | échec | **détecté**, code de sortie 1 |
| Remise en état | succès | code de sortie 0 |

Quatre faux positifs de l'outil ont également été corrigés en chemin (extraction d'objets trop
étroite, renderers cherchés au mauvais endroit, ressources de modèles mal appariées, double
extension `.png`). Un faux positif détruit la confiance autant qu'un faux négatif : ils ont été
traités comme des bugs, pas comme du bruit.

---

## Rejouer le contrôle

```bash
cd /opt/data/workspace/um-test-mod
python3 tools/audit_assets.py          # code 0 = complet, 1 = anomalie
```
