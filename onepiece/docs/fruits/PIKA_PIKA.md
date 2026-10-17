**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mise à jour alpha.8 — lot01,16octobre2026

Les8techniques de ce fruit ont été reprises avec les quatre autres fruits du lot. Les budgets, routes, adaptations et limites actuelles sont décrits dans [B01-IMPLEMENTATION](../B01-IMPLEMENTATION-2026-10-16.md), qui remplace les anciennes descriptions de comportement ci-dessous lorsqu’elles divergent. [Résultats et hash des tests](../B01-VALIDATION-2026-10-16.md). Implémentation, preuve serveur, observation client et acceptation artistique restent distinctes ; l’essai humain est attendu.

---

# Contrat — Pika Pika no Mi (Glint-Glint Fruit)

Vingt-sixième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Logia** de lumière, et
le Fruit dont le catalogue est le plus fourni jusqu'ici : **8 techniques**, **toutes** issues de ver15,
**aucune ajoutée**. C'est aussi le premier dont le plan demande d'écrire un **avertissement visible avant
l'impact** — une contrainte qui oblige à séparer la charge de la frappe, donc à introduire du **temps**
là où le moteur générique était instantané.

- **Item** : `minepiece:item_devil_fruit_pika_pika`
- **Techniques** : **8** (ver15), aucune ajoutée ni retirée
- **Famille** : **`logia`** — lue et **confirmée par ses conséquences** (`A7a`)
- **Rayons** : portée **explicite** — Laser **40** blocs, Magatama **24** (plafond dur **96**)
- **Déplacement lumineux** : **24** blocs max (plafond dur **48**), destination **calculée côté serveur**
- **Avertissement** : **20** ticks (plafond **60**) · **Volée** : **6** impacts (plafond **12**)
- **Bonus de lumière** : amplificateur plafonné à **2** (plafond dur **4**), « Speed is Power » **+12** max

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **est la lumière** : il se déplace à sa vitesse, frappe avec elle, et les coups
physiques le traversent. Comme tout Logia, il perd son pouvoir sous **suppression** (mer, scellement).

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 60 | **ver15** (inchangés) |
| Recharges | 80 à 120 ticks | **ver15** (inchangées) |
| Vitesse des rayons | Laser **4,0** · Magatama **3,5** | CORE-016 (inchangées) |
| **Portée des rayons** | 40 / 24 blocs | **ajoutée** (plan -03), plafonnée dans le code |
| **Déplacement lumineux** | 24 blocs | **ajouté** (plan -04), plafonné dans le code |
| **Avertissement / volée** | 20 ticks / 6 impacts | **ajouté** (plan -05), plafonnés dans le code |

## 2. Classification : lue, puis **vérifiée par ses conséquences** *(-02)*

Le tag `logia` était déjà présent. Un tag écrit n'est pas un tag **lu** : `A7a` le lit **et** mesure ce
qu'il implique.

**Et cette fois la valeur seule ne suffisait pas** : « ×0 » pourrait venir d'un **refus général** du
moteur aussi bien que de l'intangibilité. Le test mesure donc **le même coup sur un témoin sans Fruit** —
c'est la comparaison qui prouve, pas le chiffre.

| Vérification | Mesuré (`A7a`) |
|---|---|
| tag `logia` | **true** |
| coup physique sur le **porteur** | **×0,0** |
| **même** coup sur un témoin **sans Fruit** | **×1,0** ← la contre-épreuve |
| sous suppression | **×1,0** — le passif disparaît |

## 3. Catalogue *(ver15 — rien n'est ajouté, rien n'est retiré)*

| Id | Nom | Type | Dégâts | Portée/vie | Effet | Énergie | Recharge |
|---|---|---|---|---|---|---|---|
| `laser1` | Laser | PROJECTILE | 17,0 | 80 ticks | — | 50 | 100 |
| `laser2` | Laser Barrage | PROJECTILE | 24,0 | 80 ticks | — | 60 | 120 |
| `kick` | Light Speed Kick | MELEE | 17,0 | 6,0 | — | 50 | 120 |
| `yatanokagami` | Yata no Kagami | BUFF | — | 40 ticks | Speed II | 40 | 80 |
| `magatama` | Yasakani no Magatama | PROJECTILE | 16,0 | 60 ticks | — | 50 | 100 |
| `amanomurakumo` | Ama no Murakumo | BUFF | — | 60 ticks | Strength I | 50 | 120 |
| `speedispower` | Speed is Power | MELEE | 15,0 | 6,0 | — | 50 | 120 |
| `passive` | Pika Pika Passive | BUFF | — | 60 ticks | Speed | 40 | 100 |

## 4. Spécialisations

### Les rayons : une **portée qui se déclare** *(-03)*

Le plan : « rayons lumineux avec **collision et portée explicites** », critère « le rayon **s'arrête aux
obstacles prévus** et **touche la cible située sur sa trajectoire** ».

Le moteur générique ne donnait qu'une **durée de vie**. Un projectile qui n'expire que par durée de vie
parcourt `durée × vitesse` blocs : ce n'est pas une portée, c'est une **conséquence** — et une vitesse
élevée la fait varier sans que personne ne l'ait décidé (Laser à 4,0 bloc/tick × 80 ticks = **320 blocs**,
soit la carte entière). Le rayon porte donc une **portée en blocs**, appliquée par un champ dont la valeur
par défaut (**0 = illimitée**) garantit qu'**aucun des autres projectiles du mod ne change de
comportement** — l'ajout est une surcharge, pas une modification.

**Rendu repris du jar source (corrigé après coup).** Les rayons utilisaient `ELECTRIC_SPARK`, une
particule **vanilla** — alors que le jar d'origine embarque ses **propres** particules, dont
`particle_light_large` (5 images) et `particle_light_small` (5 images). Le portage initial les avait
perdues ; elles sont désormais **extraites** et utilisées (voir `YAMI_YAMI.md` §3 pour la mécanique
d'extraction, appliquée ici aussi). Mêmes valeurs ver15, seul le rendu change — et il devient fidèle.

**Pourquoi les rayons restent des projectiles rapides et non des rayons instantanés** : un projectile très
rapide est **tracé** (traînée visible, synchronisée) et sa collision est un **balayage** — il ne traverse
ni un mur ni une cible. Une résolution instantanée n'aurait ni traînée ni synchronisation : elle toucherait
sans que personne n'ait rien vu, et le critère « un adversaire voit » serait impossible à tenir.

`A7b` mesure : **portée 40,0 déclarée**, la fonction d'obstacle sépare **air libre (non arrêté)** et **mur
à 4 blocs (arrêté)**, et — à un autre tick — **la cible en air libre est touchée, la cible derrière le mur
est intacte**.

### Le déplacement lumineux : destination **calculée**, pas demandée *(-04)*

Le plan : « destination calculée **côté serveur** », critère « **aucune téléportation dans un bloc**, hors
des limites ou vers un **chunk non autorisé** ».

Le serveur ne fait pas confiance à une position envoyée par le client : il **marche le long du regard**,
par pas de 0,5 bloc, et retient le **dernier point sûr**. Conséquences directes : la lumière **s'arrête au
mur**, ne sort jamais des zones chargées, et **ne franchit pas une zone protégée** — vérifié par
`SafeDestination`, le prédicat **partagé** qui garde aussi l'échange de positions d'Ope Ope. La
factorisation a eu lieu **à la deuxième copie**, pas à la cinquième : deux versions de cette règle
auraient fini par diverger, et l'une des deux aurait téléporté dans la pierre.

`A7d` mesure trois cas : **24 blocs parcourus** en air libre avec destination déclarée sûre ; **1,0 bloc
seulement** face à une zone protégée (la lumière s'arrête **avant** d'y entrer) ; et **emmuré, aucun
déplacement** — « tout ou rien », lisible par la raison (`collision`).

### L'avertissement et la volée bornée : séparer la charge de l'impact *(-05)*

Le plan : « charge ou **avertissement visible** », critère « un adversaire **voit le signal avant
l'impact** et la rafale a un **nombre d'impacts borné** ».

**Il n'y a qu'une façon honnête de « voir avant » : mettre du temps entre le signal et le coup.** Une
attaque instantanée ne s'annonce pas. `Laser Barrage` **arme donc une charge** de 20 ticks pendant
laquelle le signal est **visible de tous** — halo de 16 particules `END_ROD` + éclat, **marqueur d'effet**
sur le porteur, son de charge — puis la volée part. L'adversaire dispose d'une fenêtre réelle, et cette
fenêtre est **mesurée** : à l'armement, `impacts = -1` (aucun) ; la volée n'existe qu'à un tick ultérieur.

**La rafale est bornée par construction** : 6 impacts (plafond **12 dans le code**). Une rafale n'est pas
« autant de projectiles que possible », c'est un nombre **décidé** — et le test l'affirme, il ne l'espère
pas : `impacts = 6`, la valeur exacte du plafond configuré.

### Les exceptions de ce Logia : **aucune à écrire**, et c'est mesuré *(-06)*

Le plan : « Définir et tester les **exceptions** de ce Logia, son **rendu élémentaire** et les règles
**Haki/eau/neutralisation** ».

Le résultat honnête est le même que pour Moku Moku et Mori Mori : **il n'y a pas d'exception à écrire**,
et cela se **démontre**. La table « physique / non physique » du moteur est **déjà** la bonne : elle
exclut les explosions, le feu, les dégâts magiques et toute source sans entité directe, qui sont donc
**déjà conservés** ; et les attaques **avec Haki** traversent un Logia par construction (`ARMAMENT_HAKI` /
`SOVEREIGN_HAKI`). Écrire une exception pour annuler ce qui est déjà annulé produirait du **code mort** —
l'erreur exacte commise sur Moku Moku, où un test « passait pour la mauvaise raison ».

Ce qui devait réellement être vérifié, et l'est : le coup physique **×0** (contre-épreuve témoin **×1**),
et la **suppression qui enlève le passif** (**×1** sous scellement) — le correctif partagé né sur Moku
Moku, vérifié ici pour ce Logia.

**Deux conséquences assumées et documentées.** (1) Pika Pika n'est **pas** mappé à un élément : ses rayons
infligent des dégâts **physiques**, donc un Logia adverse les **annule** sans Haki. C'est cohérent
(Logia contre Logia ne fonctionne pas) mais ce n'est pas la « matrice de contres élémentaires » complète,
dont l'absence est une simplification **déjà déclarée** du port. (2) Le **rendu élémentaire** du plan est
en partie **client** : traînée et éclats se valident en jeu (§6).

### Les bonus de lumière : bornés, et **rendus sans voler les effets d'autrui**

Le plan, pour *Yata no Kagami*, *Ama no Murakumo* et le passif : « son **cumul**, sa durée, son nettoyage »,
critère « **pas de cumul abusif** ; disparition à échéance/suppression **sans retirer les effets
externes** ».

Le moteur générique faisait **une ligne** : `player.addEffect(...)`. Le piège n'est pas d'appliquer, c'est
de **retirer** : si un joueur tenait déjà une **potion de Vitesse** avant d'activer la lumière, retirer
« l'effet Vitesse » supprime **la potion de quelqu'un d'autre**. Trois règles, dont deux tirées du moteur
lui-même plutôt que réinventées :

1. **Pas d'empilement** — une réactivation **prolonge** la durée ; deux activations laissent **un** bonus.
2. **Pas de vol d'un effet meilleur** — une potion **plus forte** déjà présente n'est pas rabaissée : le
   pouvoir **ne fait rien**, et le refuse est **compté** (`A7f` : Vitesse III conservée face à un bonus II).
3. **Restitution** — l'état **externe** déplacé est réappliqué à la fin, avec son amplificateur et sa durée
   d'origine (`A7f` : potion **0 / 600 ticks** rendue telle quelle), et le compteur de restitutions
   externes distingue « j'ai rendu » de « j'ai supprimé ».

`A7g` ferme la boucle : « **Speed is Power** » ajoute **+12** bornés quand la lumière est active, et le
supplément **disparaît quand le bonus est rendu** — la règle est lue sur l'**état réel** du bonus, sans
seconde source de vérité à synchroniser.

## 5. Acquisition, rareté, progression *(-08)*

Identique aux autres Fruits : obtenable par les mêmes règles, **aucun déblocage par maîtrise** (lacune
**générale** du port, déjà consignée pour les Fruits précédents).

## 6. Validation visuelle *(-07 — exige un client)*

- [ ] icône, modèle, **deux mains**, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **traînée des rayons** : particules `light_large` du jar source, continue (et non en pointillés)
      malgré la vitesse, sur toute la portée ;
- [ ] **signal de charge** : halo + éclat + son **lisibles à distance**, pendant **toute** la fenêtre ;
- [ ] **volée** : 6 impacts visibles, répartis en éventail ;
- [ ] **déplacement lumineux** : la traînée montre le trajet, et **s'arrête net** au mur ;
- [ ] bonus de lumière : marqueurs visibles, **aucun résidu** à l'extinction.

## 7. Tests *(-09)*

**Section `A7` — 15 vérifications** (`dftest pika`), plus **1 phase de recette** (`3 ter`, impact réel et
volée bornée). **Recette complète : 261/261 PASS, 0 FAIL.**

| Vérification | Ce qu'elle prouve |
|---|---|
| `A7a-logia-lu-et-intangible` | ×0 **contre** témoin ×1 : la comparaison, pas la valeur |
| `A7a-suppression-enleve-le-passif` | le scellement retire le passif |
| `A7b-rayon-portee-explicite` | portée **déclarée** (40,0), plus une conséquence durée × vitesse |
| `A7b-mur-arrete-le-rayon` | obstacle **arrêté** / air libre non arrêté, **même** prédicat que la ligne de vue |
| `A7c-rayons-tires-vers-les-temoins` | l'impact n'existe pas encore : il demande du **temps** |
| `A7c-sonde-impact-direct` | sonde : le projectile **avance et touche** (17 dégâts) quand on le fait voler |
| `A7c-rayon-touche-et-mur-arrete` *(3 ter)* | cible libre **touchée**, cible abritée **intacte** |
| `A7d-deplacement-lumineux-valide` | 24 blocs, destination **sûre** déclarée |
| `A7d-deplacement-refuse-en-zone-protegee` | **1,0 bloc** : la lumière s'arrête **avant** la zone |
| `A7d-emmure-aucun-deplacement` | « tout ou rien », raison lisible |
| `A7e-charge-visible-avant-impact` | signal posé, **impacts = −1** : la fenêtre existe |
| `A7e-plafond-dur-de-la-volee` | **999 → 12** : la borne est dans le code |
| `A7e-volee-tiree-et-bornee` *(3 ter)* | volée tirée, **6 = plafond** exact |
| `A7f-bonus-pose-et-prolonge-sans-empiler` | deux activations ⇒ **un** bonus |
| `A7f-ne-rabaisse-pas-un-effet-plus-fort` | Vitesse III **conservée** face à un bonus II |
| `A7f-rend-leffet-externe-au-lieu-de-le-supprimer` | potion **0 / 600** rendue telle quelle |
| `A7g-speed-is-power-bonus-borne` | +12 bornés, et **0** après retrait |

**La preuve d'impact se fait à un autre tick, et c'est structurel.** Une invocation de test tient dans
**un seul tick**, alors que le vol d'un rayon demande du **temps**. La portée `pika` **tire** les rayons et
arme la charge ; la phase **3 ter** les **relit**, dans la même vie du serveur (un état en mémoire ne
traverse pas un redémarrage). C'est la seule façon d'affirmer « le rayon a touché » plutôt que « le rayon
devrait toucher ».

## Résultat de la recette

| Phase | Attendu | Mesuré |
|---|---|---|
| `dftest all` | — | **249/249 PASS** |
| `3 bis. dftest ope_expiry` | échéance Ope (non-régression) | **1/1** |
| `3 ter. dftest pika_impact` | impact réel + volée bornée | **2/2** — cible libre **3,27 PV**, cible abritée **20,0 PV** |
| `6 ter. dftest ope_restart` | aucun état fantôme | **1/1** |
| **Global** | — | **261/261 PASS, 0 FAIL** |
| **Stabilité** | 3 passages consécutifs | **261/261 × 3** — 4 passages verts au total, les phases `ope_expiry`, `pika_impact` et `ope_restart` vertes à chaque passage |

## ⚠️ Trois erreurs de MESURE, dont deux qui accusaient le code à tort

Ce fruit a demandé **quatre itérations de test** avant d'être vert, alors que le code n'a pas changé une
seule fois après sa première écriture. Les trois fautes étaient **dans le harnais**, et la leçon vaut
d'être écrite parce qu'elle se reproduira.

**1. Deux cibles sur le même axe : la mesure était impossible.** Mon test « le rayon touche une cible en
air libre / le rayon s'arrête à un mur » plaçait la cible libre, le mur **et** la cible abritée sur **une
seule ligne**. Le mur bloquait donc **les deux** rayons : la cible « en air libre » ne pouvait
mathématiquement jamais être touchée. Le test était **irréalisable** — et il échouait en accusant le code.
Chaque mesure a désormais **son axe** et sa zone, et la seconde cible est placée **après** que la première
soit derrière le tireur.

**2. Un projectile tiré hors du chunk de spawn ne vole pas.** Le rayon était tiré à `x = 96` : il existait,
sa portée était correcte, sa vitesse aussi — et il **restait sur place**. La cible gardait ses 20 PV.
La preuve décisive n'est pas venue d'une relecture, mais d'une **sonde** : faire avancer le projectile de
6 ticks **dans le test** a donné **3,27 PV** et montré qu'il parcourt **3,7 blocs au premier tick** — le
chemin d'impact fonctionnait, c'est le **vol** qui n'existait pas. Le même rayon tiré dans le **chunk de
spawn** (`x = 8`) touche pour de vrai (17 dégâts) pendant que son jumeau derrière un mur reste à 20,0.
Seul le chunk de spawn est **entity-ticking** de façon garantie : les preuves d'état (une ROOM qui expire,
une section qui se restaure) fonctionnent partout, une preuve de **déplacement d'entité** non.

**3. Deux scènes voisines se détruisaient mutuellement.** En ajoutant la portée de Pika, son nettoyage de
mobs (± 40 blocs) a supprimé la **victime témoin** de la portée d'**Ope Ope** — la preuve d'échéance de
Mori/Ope est tombée en « victime introuvable ». Le symptôme était d'autant plus trompeur que le test
**existant** devenait rouge **sans qu'on ait touché à son fruit**. Les nettoyages sont maintenant
**resserrés au rectangle réellement construit** par chaque scène, et les portées qui arment des témoins
relus plus tard sont enregistrées **en fin de chaîne**.

**Une quatrième faute, plus discrète** : le nombre d'impacts de la volée était relu **par l'objet charge**,
qui est **supprimé** au moment même où la volée part — le test lisait donc « aucune volée » (−1) pendant
que le rapport du service comptait **6** impacts. Une mesure qui interroge un objet disparu ne mesure rien :
les derniers résultats vivent maintenant dans une table **séparée de l'état vivant**.

## Dette signalée

**1. `Yata no Kagami` cumule deux effets là où le plan n'en demandait qu'un.** La technique de ver15 est un
**élan de Vitesse** (40 ticks, amplificateur II) ; le plan demande en plus un **déplacement lumineux**
(-04). J'ai gardé les deux — le déplacement **et** le bonus de vitesse d'origine — parce que **retirer**
l'effet ver15 aurait été un changement de comportement non demandé. Si Donopot préfère un pur déplacement
sans bonus, c'est une ligne à retirer, pas une refonte.

**2. Le déplacement consomme l'énergie d'une technique mais n'a pas de coût propre.** Il est borné par la
recharge de la technique ; un coût de maintien (« coût de maintien éventuel » du plan) n'a pas été ajouté,
faute de valeur dans le plan. À trancher si un coût est souhaité.

**3. `SafeDestination` est désormais partagé par deux Fruits.** C'est la factorisation voulue — mais elle
crée une dépendance d'Ope Ope vers un prédicat que Pika Pika a fait évoluer. Toute modification devra être
validée **par les deux** contrats (les tests `A6c` et `A7d` couvrent les deux usages).
