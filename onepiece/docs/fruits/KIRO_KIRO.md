# Contrat actuel — lot04 alpha.13 en validation

Overhead est un coup frontal de15HP. Jump et Kiro Overhead décrivent une trajectoire bornée (24ticks, portée6, sommet3) puis un impact de15/25HP, avec contrôle de la boîte entière ; ils ne posent pas Speed. Weight propose0,1/1/50/100, confirme le serveur, relie vitesse et gravité sans accumulation et rejette NaN/infini. Le service est lié au joueur et au monde exacts ; suppression, mort, changement de fruit et arrêt retirent ses seuls attributs. La vraie callback Player.causeFallDamage déclenche l'impact lourd avant la remise à zéro vanilla de fallDistance. L'érosion continue au sol de la source est adaptée en un seul impact par chute, plafonné et avec recharge80ticks ; rester immobile ne creuse rien. Un bond scénarisé ne cumule pas ce second impact.

La référence est le JAR privé MinePiece ver15 ; les anciens contrats ci-dessous contiennent des interprétations remplacées. Les paramètres annoncés ici sont des budgets du port et des adaptations explicitement nommées, pas une reproduction intégrale de la source. Toute modification de blocs reste définitive. La validation native finale et la session graphique groupée restent à terminer : voir [BATCH-04](../BATCH-04.md). Aucune acceptation artistique humaine revendiquée.

---

## Archive du contrat précédent

# Contrat — Kiro Kiro no Mi (Kilo-Kilo Fruit)

Vingt-et-unième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Paramecia** de
Miss Valentine. Son contrat introduit deux exigences que les autres n'avaient pas : une valeur **venue du
client** que le serveur doit borner, et un état qui doit revenir **exactement** à la normale.

- **Item** : `minepiece:item_devil_fruit_kiro_kiro`
- **Techniques** : **4** (ver15), aucune ajoutée, aucun écart d'audit
- **Famille** : **`paramecia`** — argumentée, pas déduite (`A2a`)
- **Poids** : borné **0,1 → 100,0**, normal **1,0**
- **Impact de chute** : plafonné à **40,0** dégâts

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **change son poids** : léger il file, lourd il tombe comme une enclume et
écrase ce qu'il touche.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 40 à 60 | plan ver15 |
| **Poids : bornes** | **0,1 / 100,0** | config (serveur) |
| Vitesse au poids max | **−25 %** | config |
| Gravité au poids max | **×3,0** | config |
| Impact : poids minimal | **20,0** | config |
| Impact : plafond | **40,0** dégâts | config |
| Impact : recharge | **80 ticks** | config |

**Contre-mesures** : eau de mer (pouvoir scellé → **poids rendu normal**), retrait du Fruit (idem), Haki,
énergie insuffisante.

## 2. Classification : argumentée, pas déduite *(-02)*

`A2a` lit les trois tags — `paramecia` **true**, `logia` false, `zoan` false.

**L'argument** : ce Fruit ne transforme pas le corps en élément (pas **Logia**) ni le porteur en animal
(pas **Zoan**) — il change le **poids** de son porteur, propriété de son corps, **sans changer ce
corps**. C'est un **Paramecia**.

## 3. Catalogue

| Technique | Type | Dégâts | Portée | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **Attack Overhead** | mêlée | 15 | 12 | 50 | 120 | conservée |
| **Attack Jump** | état | 0 | — | 40 | 80 | conservée |
| **Kiro Overhead** | mêlée | 25 | 10 | 60 | 140 | conservée |
| **Weight Manipulation** | état | 0 | — | 50 | 120 | **→ réglage du poids** |

Aucun ajout, aucun renommage : l'audit ne signale **aucun écart** pour ce Fruit.

## 4. Spécialisations

### « Le serveur borne la valeur » — et pourquoi ce n'est pas un détail d'interface *(-03)*

Le poids est une valeur que le joueur **demande**. Comme toute valeur venue d'un client, elle peut être
fausse : un client modifié demandera **100 000**, et si le serveur l'accepte, le joueur **traversera le
monde** sans qu'aucune erreur n'apparaisse nulle part — le jeu aura simplement obéi. La borne n'est donc
pas une politesse d'interface : c'est **le seul endroit où la règle existe vraiment**.

`A2b` le vérifie sur les trois cas :

| Demande | Confirmé |
|---|---|
| **100 000** (client modifié) | **100,0** (borne max) — demande rejetée **mémorisée** |
| **−50** | **0,1** (borne min) |
| **50** | **50,0** (valide, retenue) |

Et la valeur diffusée à l'interface est la valeur **confirmée** (« l'interface suit l'état confirmé ») :
elle montre ce qui est **vrai**, pas ce qui a été **demandé**. Afficher la demande ferait croire à un
pouvoir qu'on n'a pas.

**Le protocole aussi a été vérifié** (`A2c`). Le champ `weight` a été ajouté au paquet de
synchronisation ; une inversion entre l'écriture et la lecture ne se serait vue que **chez un joueur, sur
un vrai client** — c'est-à-dire nulle part dans nos preuves. Le test **réécrit et relit** le paquet
complet et compare les huit champs : c'est la seule partie du contrat qui parle au client, donc la seule
qu'on ne peut pas prouver autrement.

### Poids → vitesse, chute, saut : « sans accumulation » *(-04)*

C'est l'invariant le plus facile à casser. Relier le poids à la vitesse, à la chute et au saut invite à
**ajouter** un modificateur à chaque réglage. Après vingt réglages, le joueur serait vingt fois modifié,
et revenir au poids normal **ne rendrait rien** : le pouvoir laisserait une **trace permanente** sur un
personnage qui ne s'en apercevrait que des heures plus tard, sans jamais pouvoir relier la cause à
l'effet.

`A2d` régle le poids **15 fois** de suite, puis revient au normal :

| | Base | Pendant (poids max) | Après retour |
|---|---|---|---|
| Vitesse | 0,10 | **0,08** | **0,10** |
| Gravité | 0,08 | **0,24** | **0,08** |
| Modificateurs | 0 | — | **0** |

**Un seul propriétaire** (`WeightState.applyDerived`), et **retrait avant pose** à chaque appel.

⚠️ **Le premier jet était faux, et pour une raison de conception** : l'interpolation partait du poids
**minimal**, si bien qu'au poids **normal** il restait un modificateur résiduel de **−0,0025** —
invisible à l'œil, mais c'était **exactement** l'accumulation interdite. Le critère dit « revenir au
poids **normal** restaure les valeurs » : l'ancrage devait donc être le **poids normal** (valeur neutre
= **aucun modificateur**), avec deux branches de part et d'autre.

### « Un seul impact » — le piège du contact prolongé *(-05)*

Un impact de chute se détecte au **contact avec le sol**. Or ce contact **dure** : un joueur à l'arrêt
reste « au sol » image après image, et une détection naïve de « est au sol et lourd » frappe **vingt fois
par seconde**. Le pouvoir le plus lourd du monde deviendrait alors le plus fort de tous, non par sa
puissance mais par un **défaut de détection** — et personne ne le verrait dans un test rapide, puisque la
**première** frappe est correcte.

L'impact est donc lié à une **chute**, pas à un état : il faut avoir chuté, être lourd, et **ne pas avoir
déjà frappé pour cette chute**. `A2e` le mesure : **1ᵉʳ impact = 40 dégâts** (cible 20 → 0 PV), puis
**10 tentatives supplémentaires cumulées = 0 dégât**, impacts comptés = **1**, refus « déjà frappé pour
cette chute ». Sans poids : **0** (contrôle positif).

**Le coût interdit le spam gratuit** : la recharge de **80 ticks** ferme la porte du saut en boucle sur
place, et rend l'attaque **délibérée** au lieu d'être un état permanent.

**Les dégâts sont bornés** (`A2f`) : une chute de **1 000 blocs** au poids **max** produit **40,0**
dégâts, soit le plafond — pas un nombre absurde qu'aucune armure ne pourrait arrêter.

### Nettoyage *(-06)*

`A2g` : après **retrait du Fruit**, le poids revient à **1,0** (normal) et les modificateurs à **0**
(base). Le joueur ne reste pas lourd à vie.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **peu commun** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_kiro_kiro` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **la ligne « Poids » du HUD** doit apparaître dès que le poids n'est plus normal, et **disparaître**
      quand il revient à 1,0 — c'est l'« interface » du critère -03, non vérifiable en headless ;
- [ ] **le poids doit se ressentir** : plus lourd = plus lent, chute plus rapide, et l'impact visible ;
- [ ] les transitions (poussière/son à l'engraissement, nuage/améthyste à l'allègement) doivent
      communiquer le changement d'état.

## 7. Tests *(-09)*

`/dftest kiro` — **7/7 PASS** :

| id | Résultat |
|---|---|
| `A2a-classification-argumentee` | paramecia **true**, logia false, zoan false |
| `A2b-serveur-borne-et-interface-suit-letat-confirme` | 100000 → **100,0** · −50 → **0,1** · 50 → **50,0** |
| `A2c-codec-de-synchronisation-complet` | aller-retour du paquet **identique** (8 champs) |
| `A2d-reversible-sans-accumulation` | 15 réglages → vitesse **0,10 → 0,08 → 0,10**, mods **0 → 0** |
| `A2e-un-seul-impact-et-cout-anti-spam` | **40** dégâts, puis **0** sur 10 tentatives, impacts = **1** |
| `A2f-degats-bornes` | chute de 1000 blocs → **40,0** (plafond) |
| `A2g-retrait-rend-le-poids-normal` | après retrait : poids **1,0**, mods **0** |

Reste manuel : tout le §6 (dont la ligne de HUD).

---

## ⚠️ Quatre erreurs corrigées pendant ce contrat

1. **Ancrage de l'interpolation sur le mauvais repère** : au poids normal, il restait un modificateur de
   −0,0025 — l'accumulation interdite, en miniature. Corrigé en ancrant sur la valeur **neutre**.
2. **Mesure faite au mauvais moment** : `A2d` mesurait l'état « pendant » après que la boucle était
   **déjà revenue au poids normal** — il comparait donc l'état normal à lui-même, et l'écart attendu
   était nul par construction. Corrigé en mesurant à un poids **extrême**.
3. **`FALLING_DUST` incompatible avec `CLOUD`** dans un ternaire : `FALLING_DUST` est un
   `BlockParticleOption`, pas une particule simple — le ternaire ne peut pas inférer un type commun.
4. **`SoundEvents.FEATHER_FALL` n'existe pas** en 26.3 (vérifié avec `javap`) : remplacé par
   `AMETHYST_BLOCK_CHIME`.
