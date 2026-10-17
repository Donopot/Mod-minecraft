# État lot05 : implémenté, testé serveur et vu en laboratoire

Voir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.

---

# Correction source B05

ImuArrowProcedure choisit volontairement la géométrie de bras, punch_gomu_pistol2 et la texture imu_arrow. Le texte historique ci-dessous sur un défaut Luffy par défaut est réfuté. Le nouveau rendu conserve la flèche noire source et résout la collision réelle ;25est un budget du port (source StrongPunch11avant puissance). Silent Command/Grasp/voile restent des choix du mod. Voir [lot05](../BATCH-05.md).

---

# Contrat — Imu (Fruit ????)

Seizième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Ce contrat est d'une nature
différente des quinze précédents**, et il faut le dire d'entrée.

> ## ⚠️ Avertissement : ce Fruit n'a pas d'identité connue
>
> L'identité du Fruit d'Imu **n'est pas révélée** dans l'œuvre. Le plan le sait et l'écrit noir sur
> blanc : « définir explicitement la nature du pouvoir souhaité et ses limites », critère « un contrat
> écrit précise ses capacités et **évite d'inventer une fidélité au canon** ».
>
> **Rien dans ce document ne prétend reconstituer l'œuvre.** Ce qui suit est une **décision de mod**,
> assumée et documentée comme telle : un choix de conception cohérent avec le contenu déjà présent dans
> le port (la seule technique existante), pas une reconstitution.
>
> La façon dont je le formule compte : je n'écris jamais « Imu possède tel pouvoir », mais « ce mod
> donne à ce Fruit tel pouvoir ». C'est la différence entre documenter un choix et prétendre à une
> vérité.

- **Item** : `minepiece:item_devil_fruit_imu` — nom affiché **« ???? Fruit »**
- **Techniques** : **1** dans ver15 (Imu Arrow) → **3** après extension (décision)
- **Famille** : **paramecia** — vérifiée sur le tag lu (`T0`)

---

## 1. Nature du pouvoir, définie explicitement *(FRUIT-IMU-03)*

Puisque le canon est muet, le plan demande une définition **explicite**. La voici :

> **Le Fruit ???? est, dans ce mod, un Fruit d'« autorité » : il frappe par une flèche qui ne manque pas
> sa cible, suspend le geste de ceux qui l'entourent, et protège son porteur d'un voile tant qu'il le
> détient.**

Cohérence de ce choix : il s'appuie sur la **seule** chose que le port d'origine contient réellement —
« Imu Arrow », une flèche — et l'étend dans une direction unique et lisible. Aucune technique ne
contredit une autre, et l'ensemble tient en une phrase, ce qui est le test d'un catalogue cohérent.

**Limites, elles aussi explicites :**

| Limite | Valeur |
|---|---|
| Énergie | 200 (réserve commune) |
| Coûts | 50 à 70 |
| Recharges | 120 à 180 ticks |
| **Voile : durée d'application** | **200 ticks**, rafraîchi tous les 160 |
| Voile : bonus | **+4 d'armure** |
| Voile : condition | détenir le Fruit, ne pas être scellé |
| Voile : retrait | suppression, retrait du Fruit, mort, **déconnexion** |

## 2. Classification *(FRUIT-IMU-02)*

ver15 dit `NON_CLASSE` et le plan interdit d'en déduire Paramecia. Le raisonnement part de ce que la
classe détermine : le corps du porteur **ne devient pas** un élément (pas Logia) et il **ne se
transforme pas** (pas Zoan). `T0` **lit** le tag : `paramecia=true`.

## 3. Catalogue *(FRUIT-IMU-04)*

| Technique | Type | Ciblage | Dégâts | Énergie | Recharge | Source |
|---|---|---|---|---|---|---|
| **Imu Arrow** | mêlée | cône frontal, portée 6 | 25 | 60 | 140 | **ver15** (conservée) |
| **Silent Command** | zone | **rayon 8** autour du porteur, Ralentissement ampl. 3 | 0 | 50 | 120 | **décision de mod** |
| **Sovereign Grasp** | mêlée | cône court, portée 5, recul 2,5 | 30 | 70 | 180 | **décision de mod** |

`T2` **lit le registre** : trois techniques, trois ciblages distincts (cône / rayon / cône court), chacune
avec effet, coût, recharge et portée propres — exactement ce que le critère exige.

⚠️ **Écart d'audit assumé** : `fruit_audit.py` compare au plan de ver15, qui ne déclare qu'**une**
technique. Les deux ajouts apparaissent donc comme « présents dans le port, absents du plan » — c'est la
conséquence **directe et voulue** de l'action -04 (« développer un catalogue cohérent au-delà de l'unique
Imu Arrow »). Même statut que le Gear Change de Gomu Gomu : un écart déclaré, pas dissimulé.

⚠️ **Traductions obligatoires** : le harnais refuse toute technique sans clé de traduction. Les deux
ajouts ont leurs clés en **FR** et **EN** (`T3`) — *Commandement Silencieux* / *Silent Command*,
*Poigne du Souverain* / *Sovereign Grasp*.

## 4. Le défaut corrigé : « n'utilise plus Luffy par défaut » *(FRUIT-IMU-05)*

C'est l'action la plus concrète du contrat, et c'était un **vrai défaut** du port :

> La technique **Imu Arrow** déclarait `entity_gomu_arm` comme entité visuelle — une valeur par défaut
> MCreator jamais remplacée. Concrètement, lancer « Imu Arrow » faisait apparaître **le bras élastique de
> Luffy**.

**La correction respecte deux contraintes à la fois :**

1. **Ne pas toucher aux paramètres déclarés** — dégâts, portée, coût, recharge restent ceux de ver15,
   donc l'audit des paramètres reste **propre**. Le défaut était un problème de **rendu**, pas de
   réglage : on corrige le rendu.
2. **Donner un rendu adapté** — une **flèche** visible (modèle `aroaro_arrow`, déjà présent dans les
   ressources du mod, partagé avec Aro Aro comme le mod partage déjà ses autres meshes) file dans la
   direction du regard pendant que la frappe est résolue par le moteur de mêlée commun.

**Et le défaut est *compté*, pas seulement corrigé.** `T1` compte les entités « bras de Gomu » **avant et
après** le lancement : **0 → 0**. Un test qui vérifierait seulement « une flèche apparaît » laisserait
passer un bras de Gomu en plus ; celui-ci ne le peut pas.

## 5. Passif : le voile souverain *(FRUIT-IMU-06)*

Le plan : « implémenter **uniquement** les passifs et contreparties définis dans son contrat, avec durée
et nettoyage », critère « passifs appliqués au **bon joueur** et **retirés sous suppression ou retrait**
sans toucher aux **états externes** ».

**Un seul passif, et c'est voulu** : le contrat définit le **voile** — une protection du porteur tant
qu'il détient le Fruit. Le plan insiste sur « uniquement » : un passif qu'on n'a pas écrit n'a rien à
faire dans le code.

### Le choix d'implémentation qui décide de tout

La tentation est d'appliquer `RESISTANCE` au porteur. **Mauvaise idée** : à la révocation, on ne saurait
plus distinguer *notre* résistance de celle d'une pomme bue dix secondes plus tôt — et on la lui
retirerait. Le voile utilise donc :

1. un **effet marqueur dédié** (`sovereign_veil`), qui n'existe que pour ce pouvoir ;
2. un **modificateur d'attribut identifié par ressource** (`sovereign_veil`), retiré **nominativement**.

`T6` le prouve : un modificateur d'armure **étranger** posé avant la révocation **survit**, et le compte
passe de 2 → 1 — **le nôtre seul est parti**. C'est le critère « sans toucher aux états externes », tenu
par construction plutôt que par vigilance.

### Durée et nettoyage

- Chaque application est **bornée** (200 ticks) et rafraîchie cycliquement : le passif n'est pas un état
  éternel posé une fois, donc la révocation n'a jamais à démêler un état permanent (`T7`).
- Il est retiré sous **suppression** (`T5`), au **retrait du Fruit** (`T6`), à la **mort** et à la
  **déconnexion** — la leçon de Horo Horo appliquée d'emblée, sans attendre qu'un test la réclame.
- Il ne s'applique **qu'au bon joueur** (`T4`) : un porteur d'un autre Fruit ne reçoit rien.

## 6. Acquisition, rareté, progression *(FRUIT-IMU-08)*

| Aspect | État |
|---|---|
| Rareté | **mythique** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_imu` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 7. Validation visuelle *(FRUIT-IMU-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ᵉ personne, objet au sol, infobulle ;
- [ ] **la flèche doit être reconnaissable** : c'est tout l'objet de la correction -05. À juger en jeu :
      taille (échelle 2,5×), orientation, et absence totale de bras élastique ;
- [ ] **Silent Command** : l'onde doit montrer qu'elle s'arrête à 8 blocs ;
- [ ] **le voile** : sa présence doit être perceptible sans être envahissante (particules
      `SOUL_FIRE_FLAME` aujourd'hui) — le joueur doit comprendre qu'il est protégé ;
- [ ] le nom **« ???? Fruit »** doit rester lisible et volontairement énigmatique (c'est une décision, pas
      un oubli de traduction).

## 8. Tests *(FRUIT-IMU-09)*

`/dftest imu` — **8/8 PASS** (du premier coup) :

| id | Résultat |
|---|---|
| `T0-decor-classification-et-nom` | paramecia=true · nom « **???? Fruit** » |
| `T1-imu-arrow-sans-le-bras-de-gomu` | bras de Gomu **0 → 0**, flèche `aroaro_arrow` présente |
| `T2-catalogue-etendu-et-propre` | **3** techniques, 3 ciblages, coûts/recharges propres |
| `T3-techniques-ajoutees-traduites` | clés **FR + EN** présentes |
| `T4-passif-au-bon-joueur` | porteur Imu **oui**, autre Fruit **non** |
| `T5-retire-sous-suppression` | voile **retiré** |
| `T6-retrait-propre-sans-toucher-aux-etats-externes` | 2 → 1 modificateurs, **l'étranger survit** |
| `T7-passif-a-duree-bornee` | 200 ticks, rafraîchi tous les 160 |

Reste manuel : tout le §7.
