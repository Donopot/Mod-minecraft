# Contrat actuel — lot04 alpha.13 en validation

Crash Pretzel est une estocade de15HP/portée12, sans destruction (pas d'appel BlockDestroy dans cette procédure source). Warrior I/II produisent de véritables soldats natifs avec IA, propriété exacte et vie240/320ticks, limite4. Les budgets du port15/20HP et armure10/20 sont conservés explicitement ; ce ne sont pas les dégâts source de base1modifiés par le propriétaire. Leur maillage et animations source ont été rétablis, ramenés à la taille de collision du port ; un UV de tête débordant de2pixels a été corrigé. II ajoute un signal de renforcement ; le catalogue ne prétend pas reconstruire toute la logique source de formation d'armée. Biscuit produit trois objets comestibles minepiece:biscuit aux trois claquements, au lieu de Resistance ; le surplus d'inventaire tombe au sol. La variante source supplémentaire de Crash n'ajoute pas une cinquième entrée au catalogue de ce lot.

La référence est le JAR privé MinePiece ver15 ; les anciens contrats ci-dessous contiennent des interprétations remplacées. Les paramètres annoncés ici sont des budgets du port et des adaptations explicitement nommées, pas une reproduction intégrale de la source. Toute modification de blocs reste définitive. La validation native finale et la session graphique groupée restent à terminer : voir [BATCH-04](../BATCH-04.md). Aucune acceptation artistique humaine revendiquée.

---

## Archive du contrat précédent

# Contrat — Bisu Bisu (Biscuit-Biscuit Fruit)

Troisième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. C'est le premier Fruit où
le plan demande de **transformer une technique en invocation** et le premier à recevoir des **ordres**
donnés aux créatures invoquées.

- **Item** : `minepiece:item_devil_fruit_bisu_bisu`
- **Techniques** : 4 (ver15) — Crash Pretzel, Biscuit Warrior, Biscuit Warrior II, Biscuit
- **Famille** : `paramecia` · **Transformation** : non

---

## 1. Identité, limites et contre-mesures *(FRUIT-BISU_BISU-01)*

**Identité** — Paramecia du Biscuit. Sa signature : **fabriquer des soldats** à partir de son propre
corps, qu'il envoie au combat. Non élémentaire : aucune intangibilité, aucune transformation.

**Limites** :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 | CORE-005 |
| Coûts des techniques | 50 à 60 | plan ver15 |
| Recharges | 120 à 140 ticks | plan ver15 |
| **Nombre de soldats simultanés** | **4** (`MAX_SUMMONS`) | CORE-021 |
| **Durée des soldats** | 240 ticks (12 s) / 320 ticks (16 s) | registre, FRUIT-BISU_BISU-05 |
| Portée des ordres | **32 blocs** | `/devilfruit order` |
| Portée de la garde | 8 blocs (ordre DEFENDRE) / chasse 24 blocs (ATTAQUER) | `SummonEntity` |

**Contre-mesures** :

- **Eau de mer** → pouvoirs scellés : plus de fabrication de soldats (CORE-007) ;
- **Épuisement d'Énergie** → refus **sans coût** (C2) ;
- **Limite atteinte** → refus **sans coût** : ni Énergie, ni recharge consommées (C2) ;
- **Durée** → les soldats se désagrègent d'eux-mêmes à l'échéance ;
- **Haki de l'Armement** → sans objet : pas de résistance à percer ;
- **Retrait du Fruit** → reset complet (FRUIT-RESET-01) : les soldats du joueur sont nettoyés (R2).

## 2. Classification *(FRUIT-BISU_BISU-02)*

ver15 laisse ce Fruit **`NON_CLASSE`**. Décision : **`paramecia`**, au motif qu'il n'a ni intangibilité
(pas `logia`) ni forme animale (pas `zoan`), et aucune mécanique de résistance attachée. Le tag est
posé et **lu par le code** (`DevilFruitPowerLogic.family()`) — plus décoratif.

Vérifié : `python3 tools/fruit_audit.py --fruit BISU_BISU` → **0 divergence**.

## 3. Catalogue final *(FRUIT-BISU_BISU-01)*

| Technique | Type ver15 | Type retenu | Dégâts | Portée / durée | Recul | Énergie | Recharge |
|---|---|---|---|---|---|---|---|
| **Crash Pretzel** | frappe | frappe | 15 | 12 blocs | 1,5 | 50 | 120 |
| **Biscuit Warrior** | *projectile* | **invocation** | 15 | **240 ticks** | — | 60 | 140 |
| **Biscuit Warrior II** | *projectile* | **invocation** | 20 | **320 ticks** | — | 60 | 140 |
| **Biscuit** | buff | buff | — | 60 ticks | — | 50 | 120 |

⚠️ **Les paramètres numériques sont conformes à ver15 ; seul le *type* des deux Biscuit Warriors
change** — et c'est le plan qui le demande (§4). L'audit ne relève donc aucun écart de valeur.

## 4. Spécialisations

### Soldats-biscuits : de projectile à invocation *(FRUIT-BISU_BISU-03)*

**Le constat du plan** — Biscuit Warrior et Biscuit Warrior II sont déclarés `PROJECTILE` dans ver15,
mais les actions 03 à 05 demandent des **soldats** avec « IA, santé et propriétaire », capables
de « suivre, défendre et attaquer ». Un projectile ne fait rien de tout cela.

**Ce qui existait déjà** — CORE-021 a bâti `SummonEntity` : entité GeckoLib avec propriétaire, IA
(attaque, riposte, poursuite), alliance (jamais le propriétaire ni un coéquipier), limite et
expiration. **5 techniques** l'utilisaient déjà (Doppelman, Raitei, Hebunrifire, Note Warrior,
Tot Musica) — mais via une **liste d'identifiants codée en dur dans le lanceur**.

**Ce qui a été fait** :

1. la liste codée en dur est remplacée par un **registre de données**
   (`DevilFruitRegistry.SUMMON_MOVES` : identifiant → durée de vie) ; convertir un Fruit en invocation
   ne demande plus de toucher au moteur ;
2. Biscuit Warrior et Biscuit Warrior II y sont inscrits, avec des durées propres (240 et 320 ticks —
   un soldat doit tenir un combat, pas 10 secondes) ;
3. le lanceur traite aussi les techniques **PROJECTILE** déclarées comme invocations, pas seulement
   les frappes.

**Preuve** (`C1`) : après un lancement, **1 invocation** existe avec un **propriétaire résolu**, dans
un monde où il n'y en avait aucune.

### Ordres : suivre, défendre, attaquer *(FRUIT-BISU_BISU-04)*

Nouveau. Chaque invocation porte un ordre, changeable par son propriétaire :

| Ordre | Comportement |
|---|---|
| **SUIVRE** | escorte : suit le propriétaire, engage ce qui le menace (comportement CORE-021 d'origine) |
| **DEFENDRE** | garde rapprochée : ne s'éloigne pas ; **abandonne** toute poursuite qui l'éloignerait de plus de 8 blocs ; revient au contact |
| **ATTAQUER** | chasse : cherche activement une créature hostile dans **24 blocs**, sans rester près du propriétaire |

Commande : `/devilfruit order suivre|defendre|attaquer`.

**Deux garanties, qui sont le critère du plan** (« les ordres sont autorisés uniquement au
propriétaire et fonctionnent à distance bornée ») :

- **propriétaire uniquement** — un ordre ne s'applique qu'aux invocations dont le joueur est le
  propriétaire ; celles d'un autre joueur ne sont jamais touchées ;
- **distance bornée** — 32 blocs.

La règle vit dans `DevilFruitSummons.applyOrder(...)`, **pas dans la commande** : une règle qui ne
vit que dans une commande n'est pas testable sans joueur connecté.

**Preuve** (`C3`) : 4 invocations commandées → ATTAQUER ; **celle de l'autre joueur reste SUIVRE**.
Les invocations sont prises dans l'ordre des techniques (index 0 = Crash Pretzel → SUIVRE) : la
première invocation listée est donc logiquement en SUIVRE.

### Limite, durée et coût *(FRUIT-BISU_BISU-05)*

| Exigence du plan | État | Preuve |
|---|---|---|
| « Limiter le nombre » | 4 simultanées (`MAX_SUMMONS`) | `C2` |
| « La durée » | portée par le registre (240/320 ticks) | `C4` |
| « Le coût » | coût d'Énergie normal (60) | `C2` |
| « Les soldats expirent » | `SummonEntity.tick()` désagrège à l'échéance | héritage CORE-021 |
| « Les limites persistent après reconnexion » | la limite est **recalculée depuis les entités vivantes**, il n'y a aucun compteur à réinitialiser — donc une déconnexion ne peut pas la contourner | `C5` |

### **Un refus ne consomme rien** — correction d'un défaut réel *(critère transverse du plan)*

Trouvé en implémentant ce contrat, et **indépendant de Bisu Bisu** : `castSkill` déduisait l'Énergie
**avant** `executeMove`. Quand une invocation était refusée (limite atteinte), l'Énergie était
consommée **et** la recharge posée, alors que rien ne s'était produit — en violation directe du
critère du plan « un succès consomme une fois ; **un refus ne consomme rien** ».

Corrigé : le refus remonte (`executeMove` retourne faux), l'Énergie est **rendue** et **aucune
recharge** n'est posée.

**Preuve** (`C2`) : limite atteinte (4 soldats) → lancement refusé, **Énergie 200 → 200**, recharge
non posée.

### Crash Pretzel et Biscuit — *conservés*

- **Crash Pretzel** — ver15 la déclare `MELEE` (15 dégâts, 12 blocs, recul 1,5) : le moteur de frappe
  directionnelle est bien ce que ver15 décrit (cône borné, alliés protégés, un seul impact par
  activation). Conservée.
- **Biscuit** — ver15 applique `MobEffects.RESISTANCE` 60 ticks amplificateur 1. **Ce n'est pas** un
  marqueur : l'effet est vanilla, réel et observable. Pas de cumul : réappliquer **remplace** la durée,
  et l'effet **expire seul** — rien à nettoyer au retrait.

### Passifs et contreparties *(FRUIT-BISU_BISU-06)*

Aucun passif permanent : les soldats sont des **invocations temporaires**, pas un état du porteur.
Il n'y a donc rien à appliquer en continu, et rien à nettoyer au retrait au-delà du reset complet
(dont `R2-invocations-nettoyees` prouve qu'il supprime bien les invocations du porteur).

## 5. Acquisition, rareté, progression *(FRUIT-BISU_BISU-08)*

| Aspect | État |
|---|---|
| Rareté | `paramecia` → palier **commun** (poids 60), loot des coffres (CORE-023) |
| Acquisition | coffre de structure (2 %), ou `/devilfruit give item_devil_fruit_bisu_bisu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(FRUIT-BISU_BISU-07 — P2, exige un client)*

- [ ] icône du Fruit, inventaire et au sol ;
- [ ] Fruit tenu première **et** troisième personne, deux mains ;
- [ ] infobulle (nom FR « Fruit Biscuit-Biscuit ») ;
- [ ] **soldats-biscuits** : modèle et texture corrects à l'écran, animation d'attaque ;
- [ ] **ordres** : comportement visiblement différent entre SUIVRE, DEFENDRE et ATTAQUER ;
- [ ] limite atteinte : message lisible, et aucune Énergie perdue (voir HUD) ;
- [ ] Crash Pretzel : visuel de frappe.

## 7. Tests *(FRUIT-BISU_BISU-09)*

`/dftest bisu` — **6/6 PASS** :

| id | Résultat |
|---|---|
| `C0-chunk-de-test-charge` | terrain d'essai dans le chunk de spawn, chargé et tické |
| `C1-biscuit-warrior-est-une-invocation` | 0 → **1** invocation, propriétaire résolu |
| `C2-refus-ne-consomme-rien` | limite (4) → refus, **Énergie 200 → 200**, pas de recharge |
| `C3-ordres-proprietaire-uniquement` | 4 commandées, **celle de l'autre joueur inchangée** |
| `C4-duree-de-vie-des-soldats` | 240 / 320 ticks portés par le registre |
| `C5-limite-persistante` | recalculée depuis les entités vivantes |

⚠️ **Limite du harnais découverte ici** : `EntityType.spawn` **construit l'entité mais ne l'ajoute pas
au monde** si le chunk n'est pas **tické** — sans lever d'erreur. Le test passait à (0,0,0) et
échouait à (3200,100,3200) même chunk forcé (le premier passage après démarrage échouait, le second
réussissait). Les tests qui créent des entités doivent se placer **dans le chunk de spawn**.
Consigné dans le skill `minecraft-fabric-modding`.

Reste manuel : rendu, animations, comportement comparé des trois ordres, deux clients (§6).
