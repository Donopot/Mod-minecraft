# PR — servitor : cerveau IA piloté par un LLM local

Branche : **`servitor-ia`** → `main`
Base : `043001b` · Commit : `0b76ad6` (+ ce fichier)

---

## Résumé

Ajoute un **cerveau** au servitor : un package `ai/` autonome qui comprend un ordre en français,
choisit un outil, le fait exécuter et vérifier par le serveur, et rend compte. Le modèle tourne en
local (LM Studio / Ollama / llama.cpp via une API compatible OpenAI) : aucune dépendance à un
service cloud, aucune clé à payer.

## Ce que ça change

| Avant | Après |
|---|---|
| servitor piloté par commandes (`/work …`) | ordre en langage naturel dans le chat |
| rien ne relie le mob à un modèle | `/servitor brain on`, `ask`, `status`, `mode` |
| pas de perception structurée | `probe` (plafond, sortie, lave, minerais), `find` (blocs proches), `box` (grille) |
| aucune mémoire entre sessions | `remember` / `recall` persistants avec position + dimension |
| minage en volume qui laissait le mob au fond du trou | remontée creusée après la tâche, perçage d'un mur pour rejoindre une cible |
| aucune protection contre les dangers | refuse lave/feu/magma, met de côté les blocs inatteignables, ne relance pas une tâche bloquée |

## Architecture

```
ordre chat ─► ai/ (décide)  ─► work/ (exécute) ─► monde
                 │                  ▲
                 └── outils validés ─┘
```

`ai/` ne touche jamais le monde : il propose un outil et ses arguments. Le serveur revalide
portée, volume, id de bloc, distance, budget. `TaskRunner` exécute tick par tick. Un modèle qui
hallucine ne peut donc rien casser.

## Fichiers

- `voiddynasty/src/main/java/com/donopot/voiddynasty/ai/` — 15 fichiers
  `ServitorBrain`, `BrainLlm`, `BrainTools`, `BrainOrders`, `BrainSenses`, `BrainNotes`,
  `BrainTasks`, `TaskRunner`, `BrainCommand`, `BrainConfig`, `ChatListener`, `BrainTask`,
  `BrainInv`, `BrainSpawn`, `BrainState`
- `VoidDynastyMod.java` — 3 lignes : `ServitorBrain.init()`, `ChatListener.init()`, `BrainCommand.register()`
- `entity/ChaosCultistEntity.java` — `passerEnModePilote()` : retire les buts internes, garde la navigation
- `docs/SERVITOR-VISION.md` — la cible du servitor (capacités, garde-fous, recette, feuille de route)
- `tools/lmstudio_stub.py`, `tools/mock_llm.py` — faux modèle pour tester sans GPU
- `HANDOFF_2026-10-18.md` — état du chantier

## Les 18 outils

`say` `wait` `stop` `goto` `come_to_player` `follow_player` `mine_area` `mine_block` `dig_box`
`attack_nearest` `pickup` `scan` `probe` `find` `box` `where` `remember` `recall`

## Validation effectuée

| Test | Résultat |
|---|---|
| `compileJava --offline --rerun-tasks` | BUILD SUCCESSFUL |
| `jar --offline --rerun-tasks` | jar 6 958 295 o, 1 118 entrées, sha `d57e4523…` |
| Minage 6×6×4 puis remontée | `y=-63 → -62 → -61 → -60` (trace serveur) |
| Cible derrière un mur | `14 bloc(s) creuses pour passer` |
| `probe` | `plafond 0 blocs au-dessus \| 1156 solides / 1445 vides dans 8 blocs` |
| `find` | `20 gold_block dans 16 blocs — (6406,100,6404) a 7 blocs est` |
| `box` | `vue de 6406,100,6406 (9x5x9) … legende : a=gold_block` |
| `remember` / `recall` | note écrite dans `config/voiddynasty-servitor-notes.json` puis relue |

## Risques / points à trancher

1. **Hissage d'un bloc** : `PitEscape` monte d'une marche via `teleportTo`, alors que la doc de
   `work/` dit « le servitor n'est pas téléporté ». À aligner (`WorkStairs`) ou à documenter comme
   exception.
2. **Duplication partielle** : `BrainTasks.MineArea` réimplémente ce que `work/WorkService` fait
   déjà. Cible : les outils du cerveau créent des missions `work/` au lieu de creuser eux-mêmes.
3. **Enchaînement multi-étapes** : un modèle 7B enchaîne mal `find` → `goto` → `dig_box`.
   Piste : orchestration côté serveur plutôt que dans le prompt.
4. Aucune régression connue sur le reste du mod : les seules modifications hors `ai/` sont les
   3 lignes d'init et `passerEnModePilote()`.

## Checklist

- [x] compile sans erreur
- [x] jar construit
- [x] aucun secret dans le diff (clé LM Studio absente : URL locale uniquement)
- [x] testé sur serveur dédié avec un faux modèle
- [ ] testé en client réel avec Qwen2.5-7B
- [ ] décision sur la téléportation
