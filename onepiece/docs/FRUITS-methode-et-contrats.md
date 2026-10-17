# FRUIT-* — méthode, classification et contrats de Fruit

Le plan de référence contient **769 actions FRUIT-*** réparties sur 40 Fruits (+ la réinitialisation) :

| Nature | Nombre | Ce que c'est |
|---|---|---|
| `Conception proposée` | 218 | comportement propre au Fruit à définir (flèches orientables, déplacement, charge…) |
| `Validation en jeu` | 284 | à tester avec un client — **hors de portée d'un serveur headless** |
| `Complétion à définir` | 203 | paramètres de chaque technique à spécialiser |
| `Vérification à faire` | 49 | classification, passifs, cohérence |
| `Complétion observée` | 13 | déjà présent, à confirmer |
| `Correction observée` | 2 | bugs identifiés |

Cette page fixe la **méthode** et le **contrat** que chaque Fruit doit respecter. Le détail par Fruit
vit dans `docs/fruits/<ID>.md`.

---

## 1. Méthode par Fruit

Un Fruit est considéré traité lorsque ces cinq étapes sont faites, dans cet ordre :

1. **Auditer** — `python3 tools/fruit_audit.py` compare, pour ce Fruit, les valeurs relevées dans
   ver15 (`Parametres_JAR`, `Famille_JAR` du plan) avec ce que le port déclare réellement.
   Les écarts se corrigent **dans le registre**, pas dans la documentation.
2. **Classer** — décider la famille (`logia` / `zoan` / `paramecia`) et la capacité de transformation
   (`transformable`). Un Fruit `NON_CLASSE` dans ver15 n'est **jamais** supposé Paramecia : la décision
   est motivée et écrite (voir §2).
3. **Spécialiser** — pour chaque technique `-TECH-*-DEV`, remplacer le moteur générique par le
   comportement décrit, ou **déclarer explicitement** que le comportement générique est conservé et
   pourquoi. « Ne pas conserver seulement un nom et des dégâts différents. »
4. **Passifs et contreparties** — n'implémenter que ce que le contrat définit, avec durée et
   nettoyage. Un passif doit disparaître sous suppression (mer) et au retrait du Fruit, sans toucher
   aux effets externes (cf. `FRUIT-RESET-01`).
5. **Vérifier** — garde-fous automatiques (`/dftest fruits`, `--fruit <ID>` de l'audit) + grille
   manuelle pour ce qui exige un client.

## 2. Décision de classification (les 40 Fruits)

Les trois familles sont portées par des tags (`data/minepiece/tags/item/`) et **lues par le code** —
`DevilFruitPowerLogic.family(item)`. Le rôle de chacune :

- **logia** → intangibilité aux attaques physiques (sauf Haki) — la seule famille qui porte une
  résistance, cf. `computeDamageMagnification` ;
- **zoan** → famille animale (aucun effet propre) ;
- **paramecia** → ni intangibilité ni transformation ;
- **transformable** → tag **séparé** : c'est une *capacité* (forme hybride), pas une famille.

Cette séparation famille / capacité est la décision structurante de cette étape. Elle corrige un
mélange présent dans ver15, qui classait **Gomu Gomu en ZOAN** alors que c'est un Paramecia canonique
capable de transformation. La liste codée en dur `TRANSFORMABLE` (13 Fruits) a été remplacée par le
tag `minepiece:transformable` : la capacité est désormais observable et vérifiable.

**Gomu Gomu** → `paramecia` + `transformable` : écart assumé vis-à-vis de ver15, verrouillé par le
contrôle `F2-transformation-par-tag`.

### Table de classification

Les 18 Fruits que ver15 laisse `NON_CLASSE` sont classés **paramecia** après vérification canonique
individuelle (aucun n'est élémentaire, aucun n'a d'intangibilité). Les 9 `LOGIA` et 13 des 14 `ZOAN`
de ver15 sont conservés à l'identique.

| Fruit | ver15 | Tag du port | Transformation | Techniques |
|---|---|---|---|---|
| Aro Aro | NON_CLASSE | paramecia | non | 4 |
| Batto Batto | ZOAN | zoan | oui | 6 |
| Bisu Bisu | NON_CLASSE | paramecia | non | 4 |
| Bomu Bomu | NON_CLASSE | paramecia | non | 5 |
| Gomu Gomu | ZOAN | **paramecia** (écart assumé) | oui | 3 |
| Goro Goro | LOGIA | logia | non | 7 |
| Gura Gura | NON_CLASSE | paramecia | non | 6 |
| Hie Hie | LOGIA | logia | non | 5 |
| Hito Hito Daibutsu | ZOAN | zoan | oui | 5 |
| Horo Horo | NON_CLASSE | paramecia | non | 5 |
| Imu | NON_CLASSE | paramecia | non | 1 |
| Inu Inu Okuchi No Makami | ZOAN | zoan | oui | 6 |
| Inu Inu Wolf | ZOAN | zoan | oui | 7 |
| Kage Kage | NON_CLASSE | paramecia | non | 6 |
| Kira Kira | NON_CLASSE | paramecia | non | 4 |
| Kiro Kiro | NON_CLASSE | paramecia | non | 4 |
| Magu Magu | LOGIA | logia | non | 5 |
| Mera Mera | LOGIA | logia | non | 7 |
| Mero Mero | NON_CLASSE | paramecia | non | 4 |
| Mochi Mochi | NON_CLASSE | paramecia | non | 5 |
| Moku Moku | LOGIA | logia | non | 4 |
| Mori Mori | LOGIA | logia | non | 4 |
| Neko Neko Leopard | ZOAN | zoan | oui | 7 |
| Neko Neko Sabertiger | ZOAN | zoan | oui | 5 |
| Nikyu Nikyu | NON_CLASSE | paramecia | non | 5 |
| Ope Ope | NON_CLASSE | paramecia | non | 1 |
| Pika Pika | LOGIA | logia | non | 8 |
| Riki Riki | NON_CLASSE | paramecia | non | 5 |
| Ryu Ryu Brachiosaurus | ZOAN | zoan | oui | 5 |
| Ryu Ryu Pachycephalosaurus | ZOAN | zoan | oui | 5 |
| Ryu Ryu Pteranodon | ZOAN | zoan | oui | 3 |
| Soru Soru | NON_CLASSE | paramecia | non | 7 |
| Suna Suna | LOGIA | logia | non | 5 |
| Supa Supa | NON_CLASSE | paramecia | non | 4 |
| Tori Tori Phoenix | ZOAN | zoan | oui | 6 |
| Uo Uo Seiryu | ZOAN | zoan | oui | 8 |
| Ushi Ushi Giraffe | ZOAN | zoan | oui | 5 |
| Uta Uta | NON_CLASSE | paramecia | non | 7 |
| Yami Yami | LOGIA | logia | non | 4 |
| Zushi Zushi | NON_CLASSE | paramecia | non | 6 |

## 3. Audit des paramètres — résultat de référence

`python3 tools/fruit_audit.py` compare **1 008 valeurs** (dégâts, portée ou durée, recul, énergie,
recharge, durée d'effet, amplificateur, rayon, **type de technique**) relevées dans ver15 avec le
registre du port.

```
Résultat après corrections : 1007 conformes, 1 écart (Gear Change, ajout du port hors plan ver15)
```

Quatre écarts réels ont été trouvés et corrigés :

| Fruit | Technique | Constat | Correction |
|---|---|---|---|
| Gomu Gomu | Gomu Gomu no Gatling | dégâts 4 au lieu de 6 | → **6** (valeur ver15) |
| Gomu Gomu | Gomu Gomu no Rocket | absente du plan ver15 | → renommée **Gomu Gomu no Bazooka**, dégâts **20**, portée 5, recul 3, énergie 60, recharge 140 |
| Goro Goro | Mamaragan | déclarée en **frappe** (24/12) alors que ver15 en fait un **BUFF** (`MAMARAGAN`, 60 ticks, amp 0) | → **BUFF** |
| Mera Mera | Kagero (Heat Haze) | déclarée en **frappe** (15/8) alors que ver15 en fait un **BUFF** (`SPEED`, 40 ticks, amp 1) | → **Heat Haze, BUFF** |

Deux faux écarts initiaux venaient de l'outil lui-même et ont été corrigés : une **5ᵉ signature**
`melee(id, nom, dégâts, portée, recul, effet, durée, amp, énergie, recharge, particule)` que
l'analyse ignorait (l'effet était lu comme l'énergie), et une expression régulière qui tronquait la
liste d'arguments au premier `)` interne.

Ces quatre corrections sont verrouillées par le contrôle `F3-corrections-du-plan-appliquees`.

## 4. Ce qui reste

- **244 `-TECH-*-DEV`** : comportements propres par technique. Traités Fruit par Fruit (§1 étape 3) ;
  Aro Aro est le premier contrat complet (`docs/fruits/ARO_ARO.md`).
- **284 `-TECH-*-TEST`** et les validations en jeu : **exigent un client**. Le serveur headless prouve
  le coût, la recharge, le refus d'énergie et la persistance via `/dftest`, mais pas le rendu,
  l'animation ni le ressenti — une grille manuelle est fournie par contrat.

## 5. Vérifier

```bash
python3 tools/fruit_audit.py                  # audit complet des 40 Fruits
python3 tools/fruit_audit.py --fruit ARO_ARO  # un seul Fruit
python3 tools/fruit_audit.py --tags           # classification seule
```

En jeu / RCON : `/dftest fruits` (familles, tag de transformation, garde-fous de régression).
