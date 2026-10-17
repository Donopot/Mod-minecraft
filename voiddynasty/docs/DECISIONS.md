# DÉCISIONS À PRENDRE — VOID DYNASTY

État au 16/10/2026. Treize décisions t'attendent, en trois familles. Pour chacune : les options, et ce
que je recommande (tu n'es pas obligé de suivre).

---

## Réponses de Donopot — 16/10/2025

| # | Décision | Ta réponse | État |
|---|---|---|---|
| ① | Dégâts de terrain | **a) hors zones revendiquées** (`wilderness_only`) | ✅ **vérifié** — c'était déjà le défaut et c'était déjà branché ; la protection des claims n'était éprouvée par rien, elle l'est maintenant (2 contrôles, dont une épreuve négative) |
| ② | Distribution | **a) usage personnel** | ✅ rien à faire — les assets Games Workshop restent chez toi |
| ③ | Phase G (sons) | **a) rester sur le refus** | ✅ seule pièce de contenu de la source non portée |
| ④ | Les 3 décors non portés | chercher des mods sources, priorité **Leman Russ** | ✅ **rien à chercher** : la source fournit les modèles (33/25/25 cubes) — les 3 sont convertis. Le **char est pilotable** (précision de Donopot : « non je veux du pilotable ») : entité enregistrée, 6 crochets de conduite, un siège, il ne disparaît pas et ne se laisse pas pousser. Contrôle `V-lemanruss-pilotable` + épreuve négative |
| ⑤ | Minerai en profondeur | **b) scinder une variante « ardoise »** | ✅ **fait et prouvé** dans un monde neuf : le filon pose deux blocs (`ceramite_ore`, `deepslate_ceramite_ore`), texture d'ardoise générée par composition |

### Précision ajoutée par Donopot — 16/10/2025 : « non je veux du pilotable »

Le Leman Russ a d'abord été monté en entité **statique** (l'option fidèle aux décors de la campagne), puis
Donopot a tranché : **pilotable**. Le char se monte (main droite), un seul siège, il avance, recule au
ralenti et s'infléchit. Ce qui reste de la **conception** et est déclaré dans le code : les 100 PV, les 10
d'armure, la vitesse (0,22) et la place du conducteur. Ce qui **n'est pas** fait, faute de source : les
chenilles ne défilent pas (aucune animation dans la source, je n'en invente pas) et le canon ne tire pas.

### Règle ajoutée par Donopot — 16/10/2025 : le titane se mérite

> « le titane ne doit pouvoir etre miner que avec une pioche en diamand »

Les deux minerais de céramite sont passés de `needs_iron_tool` à **`needs_diamond_tool`**. Ce n'est pas
seulement déclaré, c'est **éprouvé par le jeu** : le contrôle `H-minerai-diamant` demande au jeu
lui-même (`ItemStack.isCorrectToolForDrops`) si une pioche en fer puis une pioche en diamant viennent à
bout du bloc. Épreuve négative : tag diamant vidé → le message du harnais est « **se mine encore à la
pioche en fer** », ce qui montre que la règle tient à ce tag et à rien d'autre. Un témoin (minerai de fer
vanilla, qui cède au fer) garantit que le contrôle sait faire la différence.
| ⑥ | Textures d'armure | **a) je remplis** | ✅ 6 variantes `_s` remplies et vérifiées — reste la **taille** (128×64 contre 64×32) à trancher à l'écran |
| ⑦ | HellDrake | corps détaché **confirmé** | ✅ **corrigé, cause trouvée et mesurée** : le convertisseur passait par un pivot `P = (I-R)⁻¹·T`, une équation qui n'a pas toujours de solution, et qu'il abandonnait en silence au-delà de 64 unités. La géométrie livrée s'écartait de la source de **95 à 98 unités en moyenne sur les ailes (210 au pire, 13 blocs)** — d'où le corps détaché. Nouvelle dérivation sans pivot (`o = R₀⁻¹·T₀` cuit dans la géométrie, pivot à zéro, rotation absolue, hiérarchie aplatie) : vérifiée sur les **fichiers livrés**, écart **0,0002 unité** sur les 19 images |
| ⑧ | Échelle des 30 créatures | pas encore testé | ⏳ ton œil |
| ⑨ | Cubes plats | « revoir certaines textures comme le noir sur les ailes » | ⏳ **besoin de savoir : la forme ou la couleur ?** |
| ⑩ | Recul / rechargement | pas encore testé | ⏳ ton œil |
| ⑪ | lictor / tyranid_warrior / norn_emissary | « montre les moi » → montrés | ⏳ ton œil |
| ⑫ | Ghazghkull | pas encore testé | ⏳ ton œil |
| ⑬ | Idle des cultistes | **a) garder** | ✅ conservé |

---

## A · Décisions de conception — elles engagent du travail

### ① Dégâts de terrain en multijoueur

Les armes cassent les blocs. Reste à dire **où**.

| Option | Effet |
|---|---|
| `wilderness_only` | hors des zones revendiquées : on ne peut pas trouer la base d'un autre joueur |
| `all_unprotected` | partout où le bloc n'est pas protégé : plus brutal, plus destructeur |

**Je recommande `wilderness_only`** — sur un serveur partagé, pouvoir trouer la base d'un voisin se
paie toujours plus tard. C'est un réglage, pas un chantier : une ligne à changer.

### ② Distribution — perso ou public

| Option | Conséquence |
|---|---|
| **Usage personnel** | rien à faire. Les assets Warhammer (Games Workshop, Relic) restent chez toi |
| **Partage public** | il faut **remplacer** les modèles, sons et textures venus du pack Astartes. C'est un gros chantier, et il touche les 30 créatures |

**Je recommande de rester en perso jusqu'à ce que tu sois sûr** — et de décider du remplacement le
jour où tu voudras diffuser, pas avant.

### ③ Phase G — les sons et l'ambiance

Tu as dit « on ne fait pas G ». C'est **la seule** pièce de contenu de la source qui reste non portée :
cris de faction au spawn et à l'attaque, ambiance tyranide, alarmes.

| Option | Effet |
|---|---|
| **Rester sur ton refus** | rien à faire, le mod est complet sans |
| **La reprendre** | chantier court (les sons sont déjà extraits), mais c'est le gisement le plus exposé aux droits d'usage |

**Je recommande de rester sur ton refus** pour l'instant. Le jour où le silence des combats te gênera,
c'est trois jours de travail, pas trois semaines.

### ④ Les trois décors non portés

Sur les props décoratifs de la source, **21 sont portés** en blocs posables — caisses, 6 cadavres,
décals de sang, panneau gothique, canon laser, tourelle multifuseur, obus, batterie, sceau de cire.
Les trois **écartés volontairement** sont nommés dans la roadmap : `lemunruss` (le Leman Russ) et
`fightership` ×2. La raison est simple : ils font **3 blocs et plus**, leur place est dans une
**structure**, pas dans un bloc isolé.

| Option | Effet |
|---|---|
| **Les laisser de côté** | rien à faire, le décor reste riche de 21 pièces |
| **Les poser en structures** | il faut d'abord une génération de structures — c'est un chantier, pas une conversion |

**Je recommande de les laisser** pour l'instant. Ce n'est pas un manque de conversion : c'est un
chantier de génération de monde, à ouvrir seulement si tu veux ces véhicules dans le décor.

### ⑤ Le minerai de céramite en profondeur

Il n'existe **qu'un seul bloc** de minerai dans le mod : le filon a donc la même apparence partout,
même dans l'ardoise des profondeurs.

| Option | Effet |
|---|---|
| **Laisser tel quel** | une seule apparence, cohérente |
| **Scinder une variante « ardoise »** | plus juste en profondeur, deux blocs au lieu d'un |

**Je recommande d'attendre ton verdict en jeu.** Si le minerai clair dans la roche sombre te choque
visuellement, on la scinde — c'est une heure de travail.

### ⑥ Les textures des armures

Elles viennent de **textures de corps**, pas de couches d'armure vanilla. Le rendu est livré, mais il
peut se découper bizarrement sur certaines silhouettes.

| Option | Effet |
|---|---|
| **Laisser tel quel** | le rendu actuel |
| **Redécouper** | reprendre les textures d'armure proprement |

**Je recommande d'attendre ton verdict en jeu** — tu es le seul à pouvoir dire si ça se voit.

---

## B · Décisions d'œil — six points finis, jamais validés à l'écran

Ces six-là ne se mesurent pas, ils se regardent. Tout est dans `RECETTE-TEST.md`, avec la commande
exacte pour chacun.

| # | Ce qu'il faut juger | Ma question, en une ligne |
|---|---|---|
| ⑦ | **Le HellDrake** | son corps est-il rattaché à la tête et aux ailes, ou détaché ? |
| ⑧ | **L'échelle des 30 créatures** | cultiste 1,7 bloc · carnifex 6,2 · Prince Démon 6,3 · Grand Immonde 8,3 : crédible ? |
| ⑨ | **Les 99 cubes plats** (ailes, tissu, tentacules) | ça choque, ou c'est voulu ? |
| ⑩ | **Le recul et le rechargement** | recul de 2,2 unités et 5°, rechargement à 32° : trop, pas assez ? |
| ⑪ | **Les trois modèles à 60–67 % de gris** — `lictor`, `tyranid_warrior`, `norn_emissary` | ils ont l'air ternes à l'écran, ou c'est leur palette ? |
| ⑫ | **Ghazghkull** | 4,9 à 6,9 blocs de large pour 2,8 de haut : écrasé ou normal ? |

**Aucune recommandation ici** : c'est ton œil, et c'est précisément ce que je ne peux pas faire à ta
place. Une capture d'écran (`F2`) vaut mieux qu'une phrase.

---

## C · Une décision que j'ai prise, que tu peux défaire

### ⑬ L'animation d'arrêt des cultistes

La source ne fournit **aucun** clip d'arrêt pour les cultistes 2, 3, 4 ni pour la Prêtresse : ils
**marchaient sur place** à l'arrêt. J'ai vérifié que les cinq cultistes sont le **même squelette** (14 à
15 os communs), et je leur ai **repris l'idle du culte 1** — une animation de la source, sur le même
rig. Ce n'est pas en inventer une, mais c'est **ma** décision, et elle se déclare.

| Option | Effet |
|---|---|
| **Garder** | les cinq cultistes ont une vraie pause |
| **Revenir en arrière** | `clipIdle()` repasse sur `"walk"`, une commande |

**Je recommande de garder** — sauf si tu trouves qu'un cultiste d'assaut qui se dandine comme un
cultiste de base, ça ne va pas.

---

## Comment je m'en sers

| Ta réponse | Ce que je fais |
|---|---|
| « ① a, ② a, ③ a… » | j'applique, je mesure, je te livre |
| « ⑦ à ⑫ : tout va » | je passe directement au §A |
| « le ⑨, refais » | je reprends ce point précis et je te le remontre |

Rien de tout ça n'est bloquant pour jouer : **le mod est complet et jouable en l'état.**
