# Servitor — la cible

> Ce document décrit **ce que le servitor doit être au final**, pas ce qui est codé aujourd'hui.
> Il sert de référence : une fonctionnalité qui n'est pas ici n'a pas à être développée ; une
> fonctionnalité listée qui manque est un écart à combler.
> Version 1 — 18/10/2026.

---

## 1. En une phrase

**Un compagnon qui comprend un ordre en français, l'exécute dans le monde sans se mettre en
danger ni détruire ce qui compte, se souvient de ce qu'il a appris, et rend compte honnêtement
de ce qu'il a fait.**

Le servitor n'est pas un automate à commandes, ni un chatbot qui décrit des actions : c'est un
**agent** — il décide, agit, vérifie, et recommence.

---

## 2. L'expérience visée

Tu es en survie. Tu tapes dans le chat :

```
creuse-moi une salle de 9x9x4 à côté de la base, laisse un pilier au milieu
```

Ce qui doit se passer, sans autre intervention :

1. Il répond en une ligne ce qu'il a compris : *« salle 9x9x4 au nord-est de la base, pilier central, 324 blocs »*.
2. Il va sur place, vérifie le terrain (`probe`), signale s'il y a de la lave ou un coffre dans la zone.
3. Il creuse par paliers, évacue les déblais dans le coffre de chantier, laisse le pilier intact.
4. Il perce un accès praticable (escalier) depuis la base, pas un puits vertical.
5. À la fin : *« salle terminée, 318 blocs, 12 inaccessibles (sous le coffre), 96 cobblestone déposés au coffre de chantier. Je remonte. »*

Puis, plus tard, sans qu'on lui redemande :

```
tu as déjà vu du diamant par ici ?
```
→ *« oui, filon en 112, -54, 87, 3 blocs estimés, noté le 17/10. Tunnel d'accès depuis 108, -52, 84. »*

---

## 3. Les sept capacités

| # | Capacité | Ce que ça veut dire concrètement | Aujourd'hui |
|---|---|---|---|
| 1 | **Comprendre un ordre** | Ordre en français libre, ou en plusieurs étapes. Il reformule avant d'agir si l'ordre est ambigu. | partiel — un ordre à la fois, mode `ordre` |
| 2 | **Se déplacer** | Chemin natif d'abord, puis perçage borné d'un mur, puis paliers d'escalier. Jamais bloqué au fond d'un trou. | partiel — perçage OK, remontée par hissage (à aligner) |
| 3 | **Miner avec méthode** | Cible un filon précis, ou un volume ; respecte la lave, les coffres, le bedrock ; évacue les déblais ; budget de blocs par mission. | partiel — minage en volume OK, filon via `find` non enchaîné |
| 4 | **Construire** | Pilier, escalier, mur, plateforme, plan complet (blueprint) à partir des matériaux du sac ou d'un coffre. | **absent** (existe dans `work/BuildService`, non branché au cerveau) |
| 5 | **Gérer son sac** | Compte ce qu'il a, dépose au coffre de chantier, va rechercher un matériau manquant, ne perd rien au respawn. | partiel — sac lu, dépôt non branché |
| 6 | **Se souvenir** | Faits durables : filons vus, tunnels creusés, coffres, zones dangereuses, échecs et leur cause. | ✅ `remember` / `recall` |
| 7 | **Survivre et rendre compte** | Ne creuse pas dans la lave, ne tombe pas, mange, fuit ou combat, et raconte ce qui s'est réellement passé. | partiel — lave/feu évités, pas de fuite ni de nourriture |

---

## 4. Comment il décide

```
        ┌─────────────────────────────────────────────────────┐
        │  BOUCLE (1 cycle = 1 décision)                       │
        │                                                      │
        │  percevoir ──► planifier ──► agir ──► vérifier       │
        │      ▲                                  │            │
        │      └──────── mémoire ◄────────────────┘            │
        └─────────────────────────────────────────────────────┘
```

| Étape | Qui | Entrée | Sortie |
|---|---|---|---|
| **Percevoir** | serveur (`BrainSenses`) | position, sac, chunks chargés | `probe`, `find`, `box`, `recall`, état de la tâche |
| **Planifier** | LLM | ces faits + l'ordre + la mémoire | **un** appel d'outil, argumenté |
| **Agir** | serveur (`work/`, `TaskRunner`) | l'outil validé | tâche exécutée tick par tick |
| **Vérifier** | serveur | résultat réel de la tâche | compte rendu factuel renvoyé au LLM |
| **Mémoire** | serveur (`BrainNotes`) | faits durables | notes persistantes |

**Trois règles non négociables de la boucle :**

1. **Le LLM ne touche jamais le monde.** Il propose ; le serveur valide (portée, volume, bloc,
   distance, budget) ; l'exécution est côté serveur. Un modèle qui hallucine ne peut rien casser.
2. **Pas de boucle infinie.** Une tâche sans progression > 400 ticks est mise en pause, pas
   relancée en silence.
3. **Le compte rendu est factuel.** « 172/384, 33 inaccessibles » — jamais « c'est fait » quand
   ce n'est pas fait.

### Deux modes, un seul cerveau

| Mode | Quand | Comportement |
|---|---|---|
| **ordre** (défaut) | tu donnes une consigne | le LLM n'est appelé qu'à ce moment-là ; prompt minimal (~300 tokens) ; décision en 20-30 ms |
| **autonome** | long chantier, ou tu le laisses vivre | cycle périodique (30 s), il choisit parmi ses objectifs en cours, s'arrête si rien à faire |

### Modèle

| VRAM | Modèle | Attendu |
|---|---|---|
| 8 Go | Qwen2.5-7B-Instruct Q4 | tool-calling correct, enchaînements simples |
| 10-12 Go | Mistral-Nemo-12B | enchaînements `find` → `goto` → `dig_box` fiables |
| 16 Go+ | Qwen2.5-14B-Instruct | planification multi-étapes |

Le modèle est **interchangeable** (API OpenAI) : LM Studio, Ollama, llama.cpp, vLLM.

---

## 5. Architecture cible

```
                 ordre en français
                        │
                 ┌──────▼───────┐
                 │  ai/ (LLM)   │  ServitorBrain · BrainLlm · BrainTools
                 │  décide      │  BrainSenses · BrainNotes · ChatListener
                 └──────┬───────┘
                        │  outil + arguments  (validés)
                 ┌──────▼───────┐
                 │  work/       │  WorkService · WorkMission · WorkStairs
                 │  exécute     │  BuildService · WorkDepot · WorkRollback
                 └──────┬───────┘
                        │  tick par tick
                    le monde (serveur)

  Frontière : ai/ ne connaît que des outils. work/ ne connaît pas le LLM.
```

| Couche | Rôle | Ne fait jamais |
|---|---|---|
| `ai/` | comprendre, décider, se souvenir, parler | casser un bloc, se déplacer |
| `work/` | exécuter, prouver, annuler, déposer | appeler un modèle, inventer un ordre |

C'est cette séparation qui rend le système testable : `work/` se recette avec
`tools/run_work_lab.py`, `ai/` se teste avec un faux LM Studio (`tools/lmstudio_stub.py`).

---

## 6. Garde-fous

| Garde-fou | Règle |
|---|---|
| **Intégrité du monde** | jamais de conteneur, de bloc de joueur (lit, bannière), de bedrock ; volume max par tâche ; budget de blocs par mission |
| **Sécurité physique** | jamais creuser dans la lave, le feu, la neige poudrée ; pas de chute > 3 blocs ; remonte toujours avant de rendre la main |
| **Mouvement** | navigation, sauts et gravité normaux ; **pas de téléportation** hors hissage d'un bloc documenté |
| **Portée** | casse uniquement ce qui est à portée réelle et visible |
| **Destructif** | un ordre qui détruit une construction existante demande confirmation |
| **Honnêteté** | jamais « terminé » sans preuve ; les blocs inaccessibles sont comptés et dits |
| **Traçabilité** | chaque action laisse un reçu (bloc, tick, outil usé, drops) consultable |

---

## 7. Hors périmètre (assumé)

- Il ne joue pas à ta place : pas de combat de boss, pas d'End, pas de farm automatique de masse.
- Il ne remplace pas un mod de stockage : il utilise les coffres existants, il n'invente pas de réseau logistique.
- Il ne parle pas aux autres joueurs à ta place sans ordre explicite.
- Il ne fonctionne pas hors des chunks chargés (pas de « travail au loin » quand tu dors).
- Il n'apprend pas de ses erreurs par entraînement : sa mémoire est explicite (`remember`), pas un fine-tuning.

---

## 8. Recette de fin de chantier

Le servitor est « fini » quand ces huit scénarios passent, en jeu, sans intervention.

| # | Scénario | Critère de réussite |
|---|---|---|
| 1 | « creuse un trou de 10x10x6 » | ≥ 90 % des blocs, et il **remonte seul** au niveau du sol |
| 2 | « reviens » avec un mur entre vous | il perce et arrive en < 60 s |
| 3 | « trouve du fer et mine-le » | il localise, va au filon, le mine, dit la quantité |
| 4 | Zone avec lave sous la cible | il refuse ou contourne, **0 dégât** |
| 5 | « range ton sac dans le coffre » | sac vidé dans le bon coffre, inventaire vérifié |
| 6 | « construis un mur de 5x3 en pierre » | mur conforme, matériaux comptés, manque signalé |
| 7 | Redémarrage du serveur en pleine tâche | il reprend ou explique pourquoi il ne peut pas |
| 8 | « qu'est-ce que tu sais de cette zone ? » | il cite ses notes, positions exactes, sans invention |

Chaque scénario doit produire une **preuve** : extrait de log, capture, ou sortie de commande.
Un scénario sans preuve est un scénario non validé.

---

## 9. Feuille de route

| Palier | Contenu | Sortie |
|---|---|---|
| **V1 — agir sans se piéger** | paliers d'escalier de `work/` branchés au cerveau ; minage ciblé via `find` ; refus lave ; remontée garantie | scénarios 1, 2, 4 |
| **V2 — enchaîner** | objectifs à plusieurs étapes (`find` → `goto` → `mine`) gérés côté serveur, pas par le LLM ; reprise après échec | scénarios 3, 7 |
| **V3 — construire et stocker** | `BuildService` + `WorkDepot` exposés comme outils ; plans de construction | scénarios 5, 6 |
| **V4 — vivre seul** | survie (manger, fuir, dormir), autonomie longue durée, mémoire enrichie (carte des tunnels) | scénario 8 + tenue de plusieurs heures |

---

## 10. Décisions ouvertes

| Question | Options | Qui tranche |
|---|---|---|
| Hissage d'un bloc (`teleportTo`) | l'interdire et passer par `WorkStairs`, ou l'assumer comme exception | Donopot |
| LLM ou scripts pour les enchaînements | modèle plus gros, ou orchestration côté serveur | Donopot |
| Multi-servitors | un seul piloté, ou plusieurs avec rôles | plus tard |
| Budget de tokens par session | plafond dur, ou pas de plafond | Donopot |
