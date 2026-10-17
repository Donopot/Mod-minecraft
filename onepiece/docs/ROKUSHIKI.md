# Contrat — ROKUSHIKI, l'art martial

Le Rokushiki n'appartient à **aucun Fruit du Démon**. C'est un art martial qu'on apprend, et que
certains Fruits emploient en plus. Le port l'avait implémenté comme un jeu de techniques accrochées à
Ushi Ushi et Inu Inu Wolf : cela confondait l'outil et son utilisateur. Donopot l'a rappelé.

## Ce que dit la source

| Élément de la source | Ce qu'il établit |
|---|---|
| `SkillRokushikiProcedure` | un **dispatcher** qui lit une variable de compétence et exécute Shigan, Rankyaku, TekkaiRin, IronBody, AttackSpeed, Rokuogan, SoruPassive |
| `SelectRokushikiProcedure` | un **sélecteur** (écrit une variable joueur) |
| `AIRokushikiProcedure` | les **PNJ** l'emploient — tags `can_use_shave`, `can_use_moonwalk`, `can_use_iron_body` |
| `skill_rokushiki_advancement.json` | l'apprentissage passe par un **advancement** |
| `minepiece:skill_selector` + `loot_book.json` + `icon_book.png` | un **objet** rend l'art disponible hors des Fruits |

## Répertoire (14 techniques retrouvées dans le registre)

`shigan` · `shiganmadara` · `jushigan` · `tobushigan` · `rankyaku` · `rankyakuran` · `lupusfall` ·
`amanedachi` · `kamiezanshin` · `okamihajiki` · `yurushitene` · `rokuogan` · `soru` · `attackspeed`

`gagan`, `kibasen` (Neko Neko Sabertiger) et `localareanetwork` (Inu Inu Wolf) en sont **exclus** : ce
sont des techniques de Fruit, pas de l'art martial. Les y mettre aurait inventé un répertoire.

**Aucune valeur n'est recopiée** : les techniques sont retrouvées par identifiant dans les
déclarations de Fruit (`DevilFruitRegistry.moveById`). Une seule source de vérité — un ajustement de
valeur dans un contrat de Fruit se propage à l'art martial.

## Acquisition — hors des Fruits

1. **Objet** : `minepiece:skill_selector` (Manuel de Rokushiki, texture `icon_book.png` de la source).
   Première utilisation → apprentissage ; suivantes → choix de la technique.
2. **Variable persistée** : `rokushikiLearned` (et `rokushikiSelected`), sauvées en NBT avec les autres.

## Règles (identiques à celles des Fruits)

- **Même touche** : le pouvoir et le cycle servent l'art martial quand aucun Fruit n'est porté.
- **Scellement** : l'art passe par `isSuppressed` — même porte que les Fruits, donc aucun second
  chemin de puissance.
- **Énergie et recharge validées AVANT tout coût** ; un refus ne consomme rien.
- **Le Fruit prime** : quand un Fruit est porté, c'est son répertoire qui répond.

## Preuves (section `A11`)

| Vérification | Ce qu'elle prouve |
|---|---|
| `A11a-repertoire-de-l-art-martial` | 14 techniques retrouvées, non redéclarées |
| `A11b-sans-fruit-et-sans-art-aucun-pouvoir` | sans Fruit **et** sans art : rien ne part, énergie intacte |
| `A11c-apprentissage-hors-des-fruits` | apprentissage réussi **avec zéro Fruit porté** |
| `A11d-art-utilisable-sans-fruit` | technique exécutée **sans Fruit**, énergie débitée, recharge armée |
| `A11e-cycle-du-repertoire` | un tour complet revient au départ, toutes les techniques atteignables |
| `A11f-scelle-comme-un-fruit` | scellé ⇒ refus, énergie intacte |
| `A11g-le-fruit-prime-sur-l-art` | non-régression : un porteur de Fruit garde son répertoire |

## Dette signalée

- **PNJ** : la source fait employer l'art par les PNJ (`AIRokushikiProcedure`) — le port n'a pas de
  PNJ combattants Rokushiki.
- **Advancement** : l'apprentissage par advancement (en plus de l'objet) n'est pas repris — le port
  n'a pas de système d'advancements.
- **Tekkai / Iron Body** : `tekkairin` et `ironbody` n'ont pas de déclaration dans le registre ; ils
  sont donc **écartés** du répertoire et signalés au journal, jamais inventés.
