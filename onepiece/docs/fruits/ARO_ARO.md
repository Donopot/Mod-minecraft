# Contrat actuel — lot04 alpha.13 en validation

Arrow est un projectile avec impact destructeur borné ; Move avance de4blocs au maximum et ne touche qu'une fois chaque cible ; Combo conserve deux phases18puis20HP espacées de12ticks ; Passive avance/recul de5blocs selon Maj, sans buff Strength. Les déplacements utilisent la boîte entière et ne traversent pas les murs. Les volumes et durées sont des adaptations bornées du port. Le modèle source de flèche est conservé ; les autres gestes ont leurs pistes PAL propres.

La référence est le JAR privé MinePiece ver15 ; les anciens contrats ci-dessous contiennent des interprétations remplacées. Les paramètres annoncés ici sont des budgets du port et des adaptations explicitement nommées, pas une reproduction intégrale de la source. Toute modification de blocs reste définitive. La validation native finale et la session graphique groupée restent à terminer : voir [BATCH-04](../BATCH-04.md). Aucune acceptation artistique humaine revendiquée.

---

## Archive du contrat précédent

# Contrat — Aro Aro (Arrow-Arrow Fruit)

Fruit de référence de la méthode décrite dans `FRUITS-methode-et-contrats.md` : c'est le premier Fruit
traité de bout en bout, et le modèle des 39 autres.

- **Item** : `minepiece:item_devil_fruit_aro_aro`
- **Techniques** : 4 (ver15) — Aro Arrow, Aro Move, Aro Combo, Aro Passive
- **Famille** : `paramecia` · **Transformation** : non

---

## 1. Identité, limites et contre-mesures *(FRUIT-ARO_ARO-05)*

**Identité** — le Fruit de la Flèche. Un Paramecia **non élémentaire** : aucune intangibilité, aucune
transformation. Sa signature est le **déplacement rectiligne** et le **tir directionnel**.

**Limites** opposables à un joueur :

| Limite | Valeur | Source |
|---|---|---|
| Énergie disponible | 200 (réserve commune) | CORE-005 |
| Coûts des techniques | 40 à 60 | plan ver15 |
| Recharges | 80 à 140 ticks | plan ver15 |
| Portée maximale | 12 blocs (Aro Combo) | plan ver15 |
| Déplacement | borné par la portée déclarée (6 blocs) | FRUIT-ARO_ARO-04 |
| Nombre d'impacts | 1 par activation | CORE-010 |

**Contre-mesures** — ce Fruit n'a **aucune** défense propre :

- **Eau de mer** → pouvoirs neutralisés (`isSuppressed`), y compris le déplacement (CORE-007) ;
- **Haki de l'Armement** → sans objet ici : le Fruit n'offre aucune résistance à percer ;
- **Obstacle solide** → arrête net le déplacement (arrêt avant le bloc, jamais dedans) ;
- **Retrait du Fruit** → le reset complet (FRUIT-RESET-01) rend l'état à zéro.

## 2. Classification *(FRUIT-ARO_ARO-02)*

ver15 laisse ce Fruit **`NON_CLASSE`** — le plan interdit explicitement d'en déduire Paramecia.
Décision : **`paramecia`**, au motif que le Fruit n'a ni intangibilité (pas `logia`) ni forme animale
(pas `zoan`), et qu'aucune mécanique de résistance ne lui est attachée. Le tag est posé
(`data/minepiece/tags/item/paramecia.json`) et **lu par le code** via `DevilFruitPowerLogic.family()` :
il n'est plus décoratif. Vérifié par `F1` (`/dftest fruits`) et `F4` (`Aro Aro=PARAMECIA`).

## 3. Catalogue final *(FRUIT-ARO_ARO-01)*

| Technique | Type | Dégâts | Portée / durée | Recul | Énergie | Recharge | Effet |
|---|---|---|---|---|---|---|---|
| **Aro Arrow** | projectile | 15 | 80 ticks | 0,5 | 50 | 100 | — |
| **Aro Move** | *déplacement* | 15 | 6 blocs | 1,0 | 50 | 100 | — |
| **Aro Combo** | frappe | 18 | 12 blocs | 2,0 | 60 | 140 | — |
| **Aro Passive** | buff | — | 60 ticks | — | 40 | 80 | Force II |

Toutes ces valeurs sont **conformes à ver15** — l'audit (`tools/fruit_audit.py --fruit ARO_ARO`)
ne relève aucun écart de paramètre sur ce Fruit.

## 4. Spécialisations *(FRUIT-ARO_ARO-TECH-01/02/03/04-DEV)*

Le plan interdit de « conserver seulement un nom et des dégâts différents ». État technique par
technique :

### Aro Arrow — *moteur générique conservé, assumé* *(TECH-01-DEV)*

ver15 déclare cette technique `PROJECTILE` **sans paramètre de collision propre** : dégâts, durée de
vie, recul, énergie, recharge, particule — rien d'autre. Le projectile générique
(`CombatSkillLogic.fireProjectile`) projette déjà dans la direction du regard, sur une trajectoire
validée par le serveur, avec impact unique et expiration : les critères du plan (« projectile visible
et synchronisé, bonne collision, portée mesurée, traînée correcte, impact unique et expiration ») sont
donc couverts par le moteur commun, et le visuel dédié (`aroaro_arrow` modèle + texture) est déclaré
(CORE-024).

**Décision explicite** : pas de comportement dédié — il n'y aurait rien à spécialiser qui ne soit déjà
couvert, et en inventer un s'écarterait de ver15 sans base dans le plan.

### Aro Move — **implémenté** : déplacement sur flèche *(TECH-02-DEV, FRUIT-ARO_ARO-04)*

ver15 déclarait une **frappe générique** de 15 dégâts ; le plan propose explicitement d'en faire un
**déplacement sur flèche avec arrêt à obstacle**, avec ce critère :

> « Le joueur n'est pas déplacé dans un bloc ni dans un chunk non autorisé. »

Implémentation (`DevilFruitBehaviours.aroMoveDash`) : avance par pas de **0,25 bloc** dans la
direction horizontale du regard ; chaque pas n'est retenu que si

1. la **boîte de collision du joueur** y est entièrement libre (`level.noCollision`) → jamais dans un
   bloc, et la hitbox n'entre pas dans un mur ;
2. le **chunk est chargé** (`level.hasChunkAt`) → jamais de téléportation hors zone autorisée ;

la distance totale est bornée par la portée déclarée. Un obstacle immédiat laisse la technique aboutir
**sans déplacement** et sans coût supplémentaire. Le déplacement est accompagné d'une traînée de
particules et d'un son de flèche.

**Preuves** (`/dftest aroaro`) :

| id | Résultat |
|---|---|
| `A0-technique-aro-move-presente` | Aro Move prise en charge par `DevilFruitBehaviours` |
| `A1-deplacement-sur-fleche` | parcouru **6,00 blocs** = portée déclarée, sans obstacle |
| `A2-arret-a-obstacle-sans-traverser` | mur à 4 blocs → arrêt à **3,00 blocs**, boîte de collision **libre** |
| `A3-chunk-autorise` | destination dans un chunk chargé |

### Aro Combo — *frappe conservée* *(TECH-03-DEV)*

ver15 la déclare `MELEE` (18 dégâts, 12 blocs, recul 2) : le moteur générique de frappe directionnelle
est bien ce que ver15 décrit. Portée et nombre d'impacts sont conformes ; conservée telle quelle.

### Aro Passive — *buff conservé, non cumulable* *(TECH-04-DEV)*

ver15 applique `MobEffects.STRENGTH` 60 ticks amplificateur 1. Le plan demande de « vérifier si
l'effet est un simple marqueur » : ce **n'est pas** un marqueur — Force II est un effet vanilla réel et
observable. Le plan demande aussi « pas de cumul abusif » : réappliquer **remplace** la durée au lieu
de l'empiler (comportement vanilla de `addEffect`), et l'effet **expire seul** — il ne persiste pas
après le retrait du Fruit, donc aucune contrepartie à nettoyer.

## 5. Acquisition, rareté, progression *(FRUIT-ARO_ARO-08)*

| Aspect | État |
|---|---|
| Rareté | `paramecia` → palier **commun** (poids 60) dans le loot des coffres (CORE-023) |
| Acquisition | coffre de structure (2 %), ou `/devilfruit give item_devil_fruit_aro_aro` |
| Techniques verrouillées | **aucune** : les 4 techniques sont disponibles dès l'ingestion |
| Progression par maîtrise | **non implémentée** — dépend de CORE-022, écarté par décision de projet |

Le plan conditionne la « progression » à CORE-022, qui a été explicitement écarté : les techniques ne
sont donc pas verrouillées derrière une maîtrise. C'est un écart **assumé et déclaré**, pas un oubli.

## 6. Passifs et contreparties *(FRUIT-ARO_ARO-06)*

Le contrat ne définit **aucun passif permanent** pour ce Fruit : « Aro Passive » est une technique
active (buff temporaire), pas un passif de porteur. Il n'y a donc rien à appliquer en continu, et par
conséquent **rien à nettoyer** au retrait — au-delà du reset complet déjà couvert par FRUIT-RESET-01.
Le contrôle `R3-effets-externes-preserves` garantit qu'aucun effet d'une autre origine n'est touché.

## 7. Validation visuelle *(FRUIT-ARO_ARO-07 — P2, exige un client)*

Non vérifiable en serveur headless. Grille à passer en jeu :

- [ ] icône du Fruit dans l'inventaire et au sol, conforme et non remplacée ;
- [ ] Fruit tenu en première **et** troisième personne, dans les deux mains ;
- [ ] infobulle du Fruit (nom FR « Fruit Flèche-Flèche ») ;
- [ ] **Aro Arrow** : projectile visible, traînée, impact unique, expiration ;
- [ ] **Aro Move** : traînée visible le long du déplacement, son de flèche audible ;
- [ ] **Aro Combo** : visuel de frappe, animation ;
- [ ] **Aro Passive** : particules + icône d'effet Force dans le HUD ;
- [ ] HUD du Fruit (CORE-028) : nom, énergie, `n/4`, forme.

## 8. Tests *(FRUIT-ARO_ARO-09)*

Automatisé (serveur, RCON) — `python3 tools/run_tests.py` couvre le Fruit via `/dftest all` :

- `catalogue` : les 4 techniques existent, paramètres conformes au plan (audit) ;
- `aroaro` : déplacement, arrêt à obstacle, chunk autorisé ;
- `player` / `persist` / `multi` / `death` : ingestion, persistance au redémarrage, retrait ;
- `reset` : après retrait, **aucun état du Fruit** ne subsiste (forme 0, énergie 0, sélection 0) ;
- `refus` : énergie insuffisante → aucune consommation, recharge respectée.

Reste manuel : `FRUIT-ARO_ARO-TECH-01..04-TEST` (rendu, animation, obstacles réels, alliés, deux
clients) — impossible sans client, grille en §7.
