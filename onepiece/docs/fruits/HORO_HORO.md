# Contrat actuel — lot04 alpha.13 en validation

Negative Hollow applique un contrôle de100ticks avec ralentissement et baisse d'attaque via attributs possédés. Mini Hollow produit trois petits fantômes qui s'arrêtent après30ticks et attendent Rap, avec une vie maximale240ticks. Rap commande uniquement les fantômes du porteur ; une commande vide est refusée sans coût. Les trois explosions partagent un seul ensemble de cibles et quota terrain48blocs. Toku est un gros fantôme explosif (rayon3, quota96blocs), pas une caméra astrale. Kumacy est une invocation avec IA et vie300ticks, pas un projectile. Le port conserve l'arrêt sur obstacle et la ligne de vue des contrôles/explosions : c'est une adaptation de jouabilité explicite, distincte des anciens fantômes génériques traversants. L'ancien RemoteControl n'est plus la technique Toku ; aucune véritable projection astrale n'est revendiquée.

La référence est le JAR privé MinePiece ver15 ; les anciens contrats ci-dessous contiennent des interprétations remplacées. Les paramètres annoncés ici sont des budgets du port et des adaptations explicitement nommées, pas une reproduction intégrale de la source. Toute modification de blocs reste définitive. La validation native finale et la session graphique groupée restent à terminer : voir [BATCH-04](../BATCH-04.md). Aucune acceptation artistique humaine revendiquée.

---

## Archive du contrat précédent

# Contrat — Horo Horo (Hollow-Hollow Fruit)

Quinzième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. Le fruit des **fantômes**
(Perona). Ce contrat contient la formulation la plus exigeante rencontrée jusqu'ici, et elle tient en
cinq mots : **« le joueur retrouve son contrôle normal »**.

- **Item** : `minepiece:item_devil_fruit_horo_horo`
- **Techniques** : 5 (ver15) — Negative Hollow, Mini Hollow, Toku Horo, Rap, Kumac
- **Famille** : **paramecia** — vérifiée sur le tag lu (`S0`)
- **Élément** : **`GHOST`** (nouveau) — porte la traversée des surfaces **et** l'état négatif

---

## 1. Identité, limites et contre-mesures *(FRUIT-HORO_HORO-01)*

**Identité** — Le porteur produit des **fantômes** : ils passent à travers les murs, démoralisent ceux
qu'ils touchent, et il peut **sortir de son corps** pour agir à distance.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 50 | plan ver15 |
| Recharges | 80 à 120 ticks | plan ver15 |
| **Fantôme : durée de vie** | **80 ticks** (bornée, même dans un mur) | config |
| **Fantôme : arrêt aux claims** | **oui** | config |
| État négatif : paliers | **3 (plafond)** | config |
| État négatif : durée d'un palier | 100 ticks | config |
| **Contrôle : durée max** | **120 ticks** | config |
| Contrôle : coût | 2,0 É/s | config |
| Contrôle : sorties | **5** (échéance, énergie, suppression, mort, **déconnexion**) | config |

**Contre-mesures** : eau de mer (SEA_POWER → pouvoir scellé, **contrôle terminé**), Haki de l'Armement,
énergie insuffisante, retrait du Fruit (reset + `NegativeState.releaseAll()` +
`RemoteControl.endAll()`), et — propre à ce fruit — **les zones protégées dissipent les fantômes**.

## 2. Classification *(FRUIT-HORO_HORO-02)*

ver15 dit `NON_CLASSE`. Le raisonnement part de ce que la classe détermine : le corps du porteur **ne
devient pas** un élément (pas Logia) et il **ne se transforme pas** (pas Zoan) — même quand son esprit
sort, son corps reste un corps. `S0` **lit** le tag : `paramecia=true`.

Vérifié : `python3 tools/fruit_audit.py --fruit HORO_HORO` → **0 écart de paramètre**.

## 3. Catalogue final

| Technique | Type ver15 | Traitement | Dégâts | Énergie | Recharge |
|---|---|---|---|---|---|
| **Negative Hollow** | projectile | **fantôme** + état négatif à l'impact | 10 | 50 | 120 |
| **Mini Hollow** | projectile | **fantôme** + état négatif | 10 | 50 | 100 |
| **Toku Horo** | projectile | **contrôle à distance** | 10 | 50 | 100 |
| **Rap** | buff | conservée (SPEED 40 ticks) | 0 | 40 | 80 |
| **Kumac** | projectile | **fantôme** | 15 | 50 | 120 |

## 4. Spécialisations

### Fantômes : trajectoires et collisions spécifiques *(FRUIT-HORO_HORO-03)*

Le plan : « créer des **fantômes** avec **trajectoires et collisions spécifiques** », critère
« **traversée des surfaces limitée aux règles retenues** et **durée de vie bornée** ».

**« Traversée limitée aux règles retenues » est une phrase prudente, et elle a raison de l'être.** Un
fantôme qui traverse *tout* n'est pas un fantôme, c'est un bug de collision — et s'il traverse aussi un
claim, c'est un trou de sécurité. Les règles retenues sont donc **explicites et testées** :

| Règle | Traitement | Preuve |
|---|---|---|
| **Traverse les blocs pleins** | `onHitBlock` n'interrompt plus le projectile | `S1` : mur de 3 blocs, fantôme à **z=7,04, vivant** |
| **Durée de vie bornée** | se dissipe à l'échéance, **même dans la pierre** | `S2` : placé dans le mur → **mort** après 85 ticks |
| **Ne franchit pas une zone protégée** | dissipé à l'entrée | `S3` : **dissipé** |
| **Ne détruit rien** | aucun effet sur les blocs | par construction |

`S1` est le test qui compte : le fantôme est **à l'intérieur du mur** au bout de 20 ticks et toujours
vivant. C'est la nature du fantôme, prouvée plutôt que supposée.

### État négatif : « distinct du simple ralentissement » *(FRUIT-HORO_HORO-04)*

Le plan : « créer un **état négatif distinct du simple ralentissement** si retenu », critère « le
**malus est plafonné** et **disparaît après sa durée** ».

**La phrase « distinct du simple ralentissement » décide de toute la conception.** Le moteur commun
applique déjà Ralentissement sur ces techniques. Si l'état négatif n'était qu'un ralentissement plus
fort, le plan aurait écrit « amplifier le ralentissement ». L'état négatif est donc **autre chose** :

| | Ralentissement (commun) | **État négatif (ce fruit)** |
|---|---|---|
| Effet | la cible se **déplace** moins vite | la cible **frappe** moins fort et **travaille mal** |
| Effets posés | SLOWNESS | **WEAKNESS + MINING_FATIGUE** |
| Accumulation | non | oui, par paliers, **plafonnée** |

`S4` vérifie les trois faits **ensemble** : faiblesse **true**, fatigue **true**, ralentissement
**false**. Une cible peut être ralentie *et* négative : deux malus indépendants, et les confondre aurait
fait perdre l'un des deux.

`S5` : dix aggravations → palier **3** (plafond), puis disparition complète à l'échéance — et le porteur
lit le verdict affiché (« *sombre dans le négatif (palier 1/3)* »). `S6` : l'impact d'un fantôme applique
l'état par le chemin de l'élément, donc **toutes** les techniques du fruit en héritent sans recopie.

### Contrôle à distance : la sortie la plus exigeante du projet *(FRUIT-HORO_HORO-05)*

Le plan : « ajouter un **contrôle à distance borné** avec **sortie garantie** », critère « le joueur
**retrouve son contrôle normal** après **interruption** ou **déconnexion** ».

**Le critère ne dit pas « le contrôle se termine ».** Il dit que le joueur **retrouve son contrôle
normal**, et il nomme explicitement la **déconnexion**. C'est la formulation la plus exigeante de tous
les contrats : un état qui altère la **liberté de mouvement du joueur** doit avoir une sortie pour
**chaque** façon de le quitter, y compris celles qu'on oublie.

Le pouvoir fonctionne comme dans l'œuvre : **le porteur sort de son corps**. Son corps reste sur place,
immobile, et son esprit agit à distance. L'immobilisation est le cœur du mécanisme — ce qui la rend
utile, et ce qui impose les sorties.

**Les cinq sorties, toutes obligatoires :**

| Sortie | Raison |
|---|---|
| **Échéance** | 120 ticks maximum |
| **Énergie** | 2,0 É/s |
| **Suppression** | le pouvoir scellé ne contrôle plus rien |
| **Mort** | un joueur mort n'est pas immobilisé |
| **Déconnexion** | **nommée par le critère** : abonnement à `ServerPlayConnectionEvents.DISCONNECT` |

`S7` : le contrôle démarre, le corps est immobilisé, et après l'échéance le contrôle est **false**,
l'immobilisation **levée**, raison « echeance ».
`S8` : après **déconnexion**, contrôle **false**, immobilisation **levée**, raison « deconnexion ».

> **Pourquoi ce cas est le pire de tous** : un joueur qui reste figé après une déconnexion **ne peut même
> pas signaler le problème**. Il se reconnecte, il ne peut pas bouger, et rien dans le jeu ne lui dit
> pourquoi. C'est le seul cas où le bug est silencieux pour tout le monde.

⚠️ **Leçon personnelle de ce contrat** : j'ai écrit `MinepieceModVariables vars = …`, l'erreur exacte
que j'avais **moi-même documentée** dans le skill deux fruits plus tôt (`get()` renvoie une classe
interne, `PlayerVariables`). La règle du skill a été durcie : **ne pas déclarer le type, consommer la
valeur** (`double energie = MinepieceModVariables.get(player).PlayerEnergy;`). Une règle qu'on écrit mais
qu'on ne suit pas ne vaut rien.

### Passifs et nettoyage *(FRUIT-HORO_HORO-06)*

Deux états créés par ce fruit : `NegativeState` (les cibles) et `RemoteControl` (le porteur). Tous deux
sont nettoyés nominativement — le retrait ne touche **que** les effets posés par ce pouvoir (WEAKNESS,
MINING_FATIGUE pour l'état négatif ; SLOWNESS et le marqueur de corps inerte pour le contrôle). Les
autres sources d'effets ne sont pas affectées.

## 5. Acquisition, rareté, progression *(FRUIT-HORO_HORO-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** (poids 4) |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_horo_horo` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-HORO_HORO-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **les fantômes doivent se voir sortir du porteur** et non naître devant lui : c'est ce qui rend la
      traversée des murs lisible. Un fantôme qui « apparaît de l'autre côté » ressemble à un bug ;
- [ ] **la traversée doit être visible** — on doit voir le fantôme *dans* le mur, sinon personne ne
      comprend pourquoi il continue ;
- [ ] l'**état négatif** doit se distinguer visuellement d'un ralentissement (couleur, particules) : le
      plan insiste sur la distinction, l'écran doit la montrer ;
- [ ] **le contrôle à distance** : le corps laissé sur place doit être clairement identifiable comme
      « inerte », et le retour doit être net ;
- [ ] intangibilité paramecia à deux clients.

## 7. Tests *(FRUIT-HORO_HORO-09)*

`/dftest horo` — **9/9 PASS** (du premier coup) :

| id | Résultat |
|---|---|
| `S0-decor-et-classification` | paramecia=true · élément **GHOST** |
| `S1-fantome-traverse-le-mur` | mur de 3 blocs → fantôme à **z=7,04, vivant** |
| `S2-duree-de-vie-bornee` | dans le mur → **mort** après 85 ticks |
| `S3-traversee-limitee-aux-regles-retenues` | entrée en claim → **dissipé** |
| `S4-etat-negatif-distinct-du-ralentissement` | faiblesse **true**, fatigue **true**, ralentissement **false** |
| `S5-malus-plafonne-et-disparait` | palier **3**, puis disparition complète |
| `S6-impact-applique-l-etat-negatif` | élément GHOST → état négatif |
| `S7-controle-borne-et-sortie-sur-echeance` | immobilisation **levée**, raison « echeance » |
| `S8-sortie-sur-deconnexion` | immobilisation **levée**, raison « deconnexion » |

Reste manuel : tout le §6.
