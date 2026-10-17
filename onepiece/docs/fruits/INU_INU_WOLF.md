# Lot06 : implémenté, testé serveur et vu en laboratoire

Voir [bilan](../B06-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-06.md). Le cycle comporte maintenant une forme animale complète. Les affirmations historiques ci-dessous doivent être lues avec les corrections de source ; aucune acceptation artistique humaine acquise.

---

# Contrat — Inu Inu Wolf (Dog-Dog Fruit, Model: Wolf)

Dix-huitième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Zoan** de Jabra. Il
contient l'action la plus concrète du projet (**une texture signalée absente**) et une décision de
conception à assumer (**une forme animale qui n'existe nulle part**).

- **Item** : `minepiece:item_devil_fruit_inu_inu_wolf`
- **Techniques** : **7** (ver15) + **1** (poursuite, ajout demandé par le plan) = **8**
- **Famille** : **zoan** + `transformable` — vérifiée sur le tag lu (`X0`)
- **Échelle de forme** : **1,5**, force **1** (l'Okuchi est à 2 — les deux formes restent distinctes)

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Un **loup** : il bondit, mord, et **poursuit** — mais pas indéfiniment.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 60 | plan ver15 |
| Recharges | 80 à 160 ticks | plan ver15 |
| **Bond : distance** | **6,0 blocs**, arrêté par les obstacles | config |
| **Morsure : portée / recul** | **4,0 blocs / 2,0** | config |
| **Poursuite : endurance** | **160 ticks** | config |
| Poursuite : coût | 4,0 É/s | config |
| Poursuite : portée d'accrochage | 24,0 blocs | config |

**Contre-mesures** : eau de mer (pouvoir scellé → **poursuite rompue**), Haki, énergie insuffisante,
retrait du Fruit.

## 2. Classification *(-02)*

`X0` **lit** le tag : `zoan=true`. Le fruit transforme son porteur.

## 3. Catalogue

| Technique | Type | Ciblage | Dégâts | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Shigan** | mêlée | portée 2 | 10 | 40 | 80 | conservée (Rokushiki partagé) |
| **Soru** | état | soi | 0 | 40 | 80 | **→ bond du loup** |
| **Rankyaku** | projectile | — | 30 | 50 | 100 | conservée |
| **Rankyaku: Lupus Fall** | projectile | — | 30 | 50 | 120 | conservée (**texture corrigée**, §4) |
| **Okami Hajiki** | mêlée | portée 4, recul 2 | 12 | 50 | 100 | **→ morsure** |
| **Jushigan** | mêlée | portée 4 | 12 | 50 | 100 | conservée |
| **Local Area Network** | mêlée | portée 6 | 15 | 60 | 140 | conservée |
| **Poursuite** | mêlée | portée 24 | 0 | **60** | 160 | **ajout demandé par le plan** |

⚠️ **Écart d'audit assumé** : `Poursuite` est un ajout — mais **c'est le plan qui le demande** :
« **Ajouter** bonds, morsures et **poursuite** avec endurance ». L'ajout *est* l'instruction. Traduit
FR/EN (*Poursuite* / *Pursuit*).

⚠️ **Soru, Shigan et Rankyaku sont partagés** par plusieurs Zoan (les Rokushiki). Les hooks du loup sont
dans la branche **de ce fruit uniquement** : les autres gardent le comportement commun — et le test
vérifie qu'un fruit témoin (la Girafe) est **inchangé** (5 techniques).

## 4. Spécialisations

### Une texture signalée ABSENTE — vérifiée, puis prouvée *(-05)*

C'est l'action la plus concrète du contrat, et elle était déjà résolue :

> **Constat du plan** : « le mapping **Lupus Fall** référence
> `textures/entities/jabra_wolf_projectile.png`, **absent du JAR** ».

`X1` **charge les trois ressources depuis le jar** et les rapporte :

| Ressource | Chargée |
|---|---|
| `geckolib/models/jabra_wolf_projectile.geo.json` | **true** |
| `geckolib/animations/jabra_wolf_projectile.animation.json` | **true** |
| `textures/entities/jabra_wolf_projectile.png` | **true** |

Le critère (« la texture référencée existe et le projectile est rendu sans erreur ») est donc tenu. La
vérification **charge** les ressources au lieu de vérifier un chemin : c'est la seule façon de prouver
que le rendu ne partira pas en erreur.

### Bonds, morsures, poursuite — trois garde-fous × trois fonctions *(-04)*

Le critère nomme **trois** garde-fous : « les trois fonctions respectent **collisions, coûts et
permissions** ». Il faut les lire comme **trois**, car une fonction peut respecter le coût et ignorer les
collisions, ou respecter les collisions et ignorer les permissions.

**Le bond (`X3`)** — « collisions » veut dire qu'on **ne traverse pas la pierre en sautant**. Un bond est
le déplacement le plus tentant à implémenter par simple ajout de vitesse : c'est aussi celui qui traverse
les murs, parce qu'un déplacement rapide franchit un obstacle **entre deux ticks**. Le bond est donc
**échantillonné** tous les 0,5 bloc.

| | Résultat |
|---|---|
| Bond en plaine | **6,0 blocs** |
| Bond face à un mur de 2 blocs | **1,0 bloc**, arrêté à **z=9,5** (mur à z=10) |

**La morsure (`X4`)** — portée courte, recul **2,0**, et **devant** seulement : DEVANT **8,19 PV**,
DERRIÈRE **20,0 PV intacte**. Le recul n'est pas décoratif : mordre sert à **écarter** ; un prédateur qui
ne repousse pas sa proie la laisse riposter.

**La poursuite avec endurance (`X5`)** — c'est la pièce qui compte. Une poursuite **sans endurance** n'est
pas une capacité, c'est une **condamnation** : le poursuivant ne s'arrête jamais, la cible ne peut pas
s'échapper, et le seul recours est de quitter le serveur. L'exact opposé du soin mis partout ailleurs à
garantir une sortie.

Et surtout, l'endurance **ne se recharge pas en courant** : sinon il suffirait de poursuivre sans relâche
pour poursuivre indéfiniment. **Se reposer** est la seule façon de la retrouver — ce qui transforme une
course sans fin en **décision** : « puis-je me permettre ces trois secondes ? »

`X5` arrête après **exactement 160 ticks**, raison « **endurance** ».

⚠️ **Première version de ce test : elle ne prouvait rien.** La poursuite **rattrapait** sa proie en
21 ticks — comportement **correct**, mais l'endurance n'était alors **jamais exercée**. Le test passait à
côté de son propre objet. Corrigé en donnant à la proie une **fuite** qui maintient ses distances :
l'endurance devient le **seul terme possible**, et c'est seulement là que le test démontre quelque chose.

**Le refus ne consomme rien (`X6`)** — pouvoir scellé → refus ; cible derrière un mur → refus ;
**mur bien détecté = true** ; **même configuration sans mur ni sceau → accrochée = true** ; énergie
**200,0 → 200,0**. Le contrôle positif est indispensable : sans lui, un pouvoir qui refuserait **tout**
passerait le test… et ne ferait rien.

**Arrêt en pleine course (`X7`)** — un mur dressé entre les deux : la poursuite s'arrête, raison
« **obstacle** ». La course ne traverse pas la pierre.

### Formes : ce qui existe, et ce qui n'existe pas *(-03/-06)*

Le plan demande « formes **loup** et **hybride** avec modèles et animations », critère « **gabarit,
caméra et collisions** sont cohérents et **réversibles** ».

**Ce qui existe** (`X2`) : la transformation est câblée, la texture d'hybride et la texture de
transformation sont présentes, et la variante « **slim** » est déclarée séparément (l'Okuchi ne l'a pas —
les deux Fruits restent distincts). La réversibilité est **prouvée** (`X8`) : échelle **1,5** posée,
modificateurs de vitesse **0 → 1 → 0**.

> ### ⚠️ Écart déclaré : la forme animale complète n'existe nulle part
>
> **Vérifié dans le jar source ver15** : il contient `jabra_humanbeast.geo.json` (l'hybride) et
> `jabra_wolf_projectile.*` — et **aucun modèle quadrupède de joueur**. Les 31 modèles de transformation
> du port sont **tous** des `*Humanbeast`.
>
> Autrement dit : la forme loup complète **n'a jamais existé**, ni en ver15 ni dans le port. Ce n'est
> **pas** un morceau manquant du portage — c'est **du contenu neuf à créer** (un modèle 3D complet, ses
> animations, ses dimensions).
>
> **Je ne le fabrique pas.** Produire un modèle inventé et le présenter comme un portage serait
> exactement ce que le contrat du Fruit Imu interdit (« ne pas inventer une fidélité au canon »). C'est
> une **décision** qui appartient à Donopot : soit on laisse l'hybride seul, soit on commande le modèle.
>
> Ce qui **est** livré ici : le gabarit (échelle), la réversibilité, les collisions et la caméra, tous
> prouvés sur la forme existante.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** (poids 4) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_inu_inu_wolf` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **Rankyaku: Lupus Fall** : le projectile doit s'afficher **avec sa texture** (c'est l'objet de
      -05) — à confirmer à l'écran, la vérification ci-dessus ne prouve que la présence des fichiers ;
- [ ] **le bond** : la trajectoire doit se lire comme un saut, et l'arrêt sur un mur doit être
      compréhensible (pas d'à-coup) ;
- [ ] **la morsure** : le recul doit se voir ;
- [ ] **la poursuite** : l'endurance doit être **perceptible** — sans retour visuel, le joueur ne
      comprendra pas pourquoi sa course s'arrête ;
- [ ] **la forme** : l'hybride du loup vs celui de l'Okuchi no Makami, à distinguer au premier regard.

## 7. Tests *(-09)*

`/dftest wolf` — **9/9 PASS** :

| id | Résultat |
|---|---|
| `X0-decor-et-classification` | zoan=true |
| `X1-texture-lupus-fall-presente` | modèle **true**, animation **true**, texture **true** |
| `X2-formes-reellement-disponibles` | hybride + transformation présents · **forme animale absente (déclarée)** |
| `X3-bond-respecte-les-collisions` | plaine **6,0** / mur **1,0** (arrêt z=9,5) |
| `X4-morsure-porte-devant` | devant **8,19 PV** / derrière **20,0 PV** |
| `X5-poursuite-a-endurance` | arrêt à **160 ticks**, raison « endurance » |
| `X6-refus-sans-cout-permissions-et-collisions` | 3 garde-fous + contrôle positif, énergie intacte |
| `X7-poursuite-sarrete-sur-obstacle` | arrêt raison « obstacle » |
| `X8-forme-reversible` | vitesse **0 → 1 → 0** |

Reste manuel : tout le §6.
