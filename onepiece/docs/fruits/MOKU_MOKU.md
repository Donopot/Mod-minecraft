**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mise à jour alpha.8 — lot01,16octobre2026

Les4techniques de ce fruit ont été reprises avec les quatre autres fruits du lot. Les budgets, routes, adaptations et limites actuelles sont décrits dans [B01-IMPLEMENTATION](../B01-IMPLEMENTATION-2026-10-16.md), qui remplace les anciennes descriptions de comportement ci-dessous lorsqu’elles divergent. [Résultats et hash des tests](../B01-VALIDATION-2026-10-16.md). Implémentation, preuve serveur, observation client et acceptation artistique restent distinctes ; l’essai humain est attendu.

---

# Contrat — Moku Moku no Mi (Smoke-Smoke Fruit)

Vingt-troisième Fruit traité selon la méthode de `FRUITS-methode-et-contrats.md`. **Logia** de Smoker.
C'est le premier contrat dont le cœur est **l'intangibilité elle-même** : le plan reproche que tous les
Logias partagent un comportement **générique**, sans exceptions propres.

- **Item** : `minepiece:item_devil_fruit_moku_moku`
- **Techniques** : **4** (ver15), aucune ajoutée, **aucun écart d'audit**
- **Famille** : **`logia`** — lue et confirmée (`A4a`)
- **Nuage** : 200 ticks, rayon 6, **plafond 3** · **Mobilité** : +35 %, **4 É/s**
- **Capture** : 80 ticks, **4 cibles** max

---

## 1. Identité, limites et contre-mesures *(-01)*

**Identité** — Le porteur **devient de la fumée** : les coups le traversent, il étouffe la zone et s'y
déplace plus vite. La fumée est un **lieu**, pas un projectile.

| Limite | Valeur | Source |
|---|---|---|
| Énergie | 200 | CORE-005 |
| Coûts | 50 à 70 | plan ver15 |
| **Nuage : durée / rayon / plafond** | 200 ticks / 6,0 / **3** | config |
| **Mobilité : bonus / coût** | **+35 %** / **4 É/s** | config |
| **Capture : durée / cibles** | **80 ticks** / 4 | config |

**Contre-mesures** : eau de mer (**le passif tombe**), Haki de l'Armement, énergie insuffisante, retrait.

## 2. Classification : lue, pas supposée *(-02)*

`A4a` lit les trois tags — `logia` **true**, `zoan` false, `paramecia` false.

**L'argument** : le corps du porteur se change en **matière** (la fumée) et les attaques **physiques le
traversent** — c'est la définition d'un **Logia**. Le tag existant est donc **confirmé**, pas hérité.

## 3. Catalogue

| Technique | Type | Dégâts | Portée | Énergie | Recharge | Traitement |
|---|---|---|---|---|---|---|
| **White Blow** | projectile | 50 | — | 70 | 160 | conservée |
| **White Vine** | projectile | 15 | — | 50 | 100 | conservée |
| **White Launcher** | mêlée | 11 | 4 | 50 | 100 | conservée |
| **White Out** | mêlée | 18 | 32 | 60 | 160 | **→ crée le nuage** |

Aucun ajout, aucun renommage : **aucun écart d'audit** pour ce Fruit.

## 4. Spécialisations

### Les exceptions de **ce** Logia, source par source *(-06)*

Le constat du plan est net : `computeDamageMagnification` « applique l'**intangibilité physique à tous les
fruits du tag LOGIA** sans exceptions propres complètes ». Autrement dit, chaque Logia héritait du **même**
comportement, alors que leurs corps ne se comportent pas de la même façon.

La fumée a **une** propriété que les autres n'ont pas : **un souffle la DISPERSE**. Une onde de choc ne
traverse pas la fumée comme elle traverse un mur — elle l'**écarte**, ce qui fait que l'attaque **PORTE**.

`A4e` lit la table complète, source par source :

| Source | Multiplicateur | Résultat |
|---|---|---|
| Physique **sans** Haki | **×0** | **annulée** |
| **Explosion** (souffle) | **×1** | **conservée** ← *l'exception propre à ce Logia* |
| Magie | ×1 | conservée |
| Physique **avec** Haki | ×1 | conservée |
| Physique **sous scellement** | ×1 | conservée |
| Sans Fruit (contrôle) | ×1 | conservée |

**« La suppression enlève le passif » — c'était FAUX, et c'est corrigé.** Le moteur ne consultait
**pas** l'état de suppression : un porteur **immergé** restait intangible, donc **protégé par un pouvoir
que le jeu venait précisément de lui retirer**. La condition est désormais lue (`isSuppressed(victim)`),
pour ce Fruit comme pour les autres Logias — et c'est exactement ce que le critère exige.

### Le nuage : borné, et **visible des autres** *(-03)*

`A4b` vérifie les deux moitiés du critère : **1** nuage créé, un **observateur à 9 blocs**, et la
**dissipation après 200 ticks**. Un nuage **permanent** finirait par couvrir la carte sans que personne ne
puisse dire d'où il vient.

Et il est **diffusé au monde** (120 particules de fumée émises au niveau du serveur + un son au volume
normal) : les autres joueurs le **voient**. Un effet que seules les victimes subissent, sans pouvoir le
rattacher à quelqu'un, n'est pas un pouvoir — c'est une gêne anonyme.

### La mobilité : **payée**, et **rendue** *(-04)*

Se déplacer plus vite dans son propre nuage n'est pas un cadeau : c'est une **dépense**. `A4c` mesure les
trois états exigés par le critère :

| Situation | Mesure |
|---|---|
| Dans le nuage | vitesse **0,10 → 0,14**, énergie **200 → 196,0** sur 20 ticks (coût déclaré **4/s**) |
| **Sortie** du nuage | mobilité **false**, vitesse **0,10** = base |
| **Scellement** (dans le nuage) | mobilité **false**, vitesse **0,10** = base |

Sans coût, le nuage serait une amélioration gratuite et le porteur n'aurait aucune raison d'en sortir.
Et un bonus laissé en place après la sortie serait un **avantage permanent déguisé en pouvoir
temporaire** : le propriétaire unique de ce modificateur **retire avant de poser**.

### La capture : bornée, **toujours libérée**, et les siens épargnés *(-05)*

Capturer, c'est **décider à la place de quelqu'un** qu'il ne bougera pas. D'où la durée **bornée** :
`A4d` mesure la capture (vitesse **0,23 → 0,0**) puis la **libération** après 80 ticks (raison
« échéance », vitesse **rendue**, marqueur **parti**). Sans cette borne, un joueur resterait prisonnier
d'un pouvoir dont le propriétaire a **quitté le serveur**.

Et les **cibles protégées sont exclues** : le porteur lui-même, ses **invocations**, ses **alliés**, les
joueurs en spectateur ou créatif, et toute cible en **zone protégée**. Un pouvoir qui emprisonne les
siens n'est pas un pouvoir, c'est un **piège**.

## 5. Acquisition, rareté, progression *(-08)*

| Aspect | État |
|---|---|
| Rareté | **rare** |
| Acquisition | coffre de structure (CORE-023) ou `/devilfruit give item_devil_fruit_moku_moku` |
| Techniques verrouillées | **aucune** |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

## 6. Validation visuelle *(-07 — P2, exige un client)*

- [ ] icône, deux mains, 1ʳᵉ/3ʳᵉ personne, objet au sol, infobulle ;
- [ ] **le nuage doit être lisible à distance** et ne pas être confondu avec une simple fumée de décor :
      c'est le cœur du critère -03 et il ne se vérifie qu'à l'écran ;
- [ ] **le rendu élémentaire du corps** (translucidité, particules au déplacement) — le plan le demande
      explicitement dans -06 ;
- [ ] **la capture doit se voir** sur sa cible : une victime immobilisée sans retour visuel croira à un bug ;
- [ ] la mobilité de fumée doit **se ressentir** (le +35 % doit être perceptible sans être gratuit).

## 7. Tests *(-09)*

`/dftest moku` — **5/5 PASS** :

| id | Résultat |
|---|---|
| `A4a-classification-argumentee` | logia **true**, zoan false, paramecia false |
| `A4b-nuage-borne-et-visible-des-observateurs` | 1 nuage, observateur à 9 blocs, dissipé à 200 ticks |
| `A4c-mobilite-rendue-a-la-sortie-et-sous-suppression` | 0,10 → **0,14**, énergie **-4,0/s**, puis vitesse **rendue** |
| `A4d-capture-liberee-et-cibles-protegees-exclues` | 0,23 → **0,0**, libérée à l'échéance, invocation **épargnée** |
| `A4e-exceptions-du-logia-source-par-source` | **6** sources lues, explosion **conservée**, scellement **conservé** |

Reste manuel : tout le §6.

---

## ⚠️ Trois défauts réels trouvés et corrigés

1. **La suppression ne retirait pas le passif** (*le plus important*). Le moteur ne consultait pas l'état
   de suppression : un porteur **immergé** restait **intangible**, donc protégé par un pouvoir que le jeu
   venait de lui retirer. Corrigé pour **tous** les Logias — c'est un changement de comportement partagé,
   signalé comme tel.
2. **Une exclusion qui échouait EN SILENCE vers l'autorisation** (*fail-open*). L'exclusion des invocations
   du porteur dépendait de la résolution du porteur — or `getPlayerList()` **ne contient pas les joueurs
   virtuels** : le porteur était introuvable, la condition était **sautée**, et le nuage **emprisonnait les
   invocations de son propre maître**. Le test l'a montré (`invocation ÉPARGNÉE = false`) ; en jeu, ce
   seraient des **alliés**. Corrigé par une référence de porteur revalidée + `resolveOwner` robuste.
   **Une garde qui échoue en autorisant est pire qu'une garde absente : elle donne le sentiment d'être
   protégé.**
3. **Une mesure de coût sur un seul tick ne prouve rien** : la dépense est prélevée par **unités
   entières**, donc un tick coûte 0. Le test exigeait une baisse d'énergie après **un** appel — il
   échouait pour une raison arithmétique, pas fonctionnelle. Corrigé en mesurant sur **20 ticks**
   (une seconde), ce qui donne exactement le coût déclaré.
