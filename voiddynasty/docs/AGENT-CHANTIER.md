# AGENT DE CHANTIER — conception

*Établi le 17/10/2026. Demande de Donopot : « une IA à qui je donne une zone, il mine tout,
en prenant en compte tous les soucis que ça peut occasionner », généralisable à l'agriculture
et à la construction.*

---

## 1 · La décision qui commande tout : serveur, pas client

Le réflexe serait un bot qui se connecte comme un joueur (Mineflayer). **C'est impossible ici, et
c'est vérifié** : la base de versions de Mineflayer s'arrête à 1.21.9 et 26.1, notre serveur est en
**26.3** (protocole 777). Aucun bot de ce type ne peut s'y connecter.

Donc l'agent vit **dans le serveur**, pas à côté :

| | Bot client (Mineflayer) | **Agent serveur (retenu)** |
|---|---|---|
| Connexion à 26.3 | ✗ impossible | ✓ natif |
| Connaît les blocs du mod | ✗ vanilla seulement | ✓ tout, céramite comprise |
| GPU / client Minecraft | requis | **aucun** |
| Vitesse | limitée par le protocole | limitée par le budget serveur, qu'on choisit |
| Voir le jeu | ✓ (rendu réel) | ✗ aucun rendu — mais on n'en a pas besoin pour miner |

**Conséquence heureuse** : l'agent peut réutiliser le code du mod (`ClaimIntegration` pour les
zones protégées, les machines, les blocs du mod) et nos formats de plan (`.bin.gz`).

## 2 · Ce qui rend la chose généralisable

Ce n'est pas le LLM qui généralise, c'est le **substrat commun**. Trois tâches très différentes
(miner, cultiver, construire) partagent six briques identiques :

1. **Modèle du monde** — scan d'une zone en blocs, avec budget par tick (jamais un scan de 100 000
   blocs d'un coup). Sortie : une carte des blocs et de leurs propriétés.
2. **Détecteur de dangers** — classe chaque bloc et chaque situation (voir §4). C'est le cœur.
3. **Inventaire et outils** — ce qu'on a, ce qui casse, ce qu'il faut aller chercher, où déposer.
4. **Déplacement** — cheminement, échafaudage, ne jamais creuser sous soi.
5. **Exécuteur budgeté** — N actions par tick, mesure du TPS, pause automatique si le serveur souffre.
6. **Journal et reprise** — chaque bloc cassé/posé est noté ; reprise après redémarrage ; **annulation**.

Une tâche = un planificateur qui produit des **étapes** à partir de ces six briques.
Ajouter l'agriculture ou la construction ne touche pas au substrat : ça ajoute un planificateur.

## 3 · Le rôle du LLM, et sa limite

```
« nettoie cette colline, fais attention à la lave »
        │
        ▼  LLM (Hermes) — interprète l'intention, choisit la stratégie, replanifie sur imprévu
   TaskSpec déterministe : {type, zone, options, priorités}
        │
        ▼  planificateur déterministe — génère les étapes
   file d'étapes → exécuteur → journal → vérificateur
```

**Le LLM ne décide jamais bloc par bloc.** Sinon : coût absurde, latence, et comportement
imprévisible sur un monde qui contient tes construction. Le LLM intervient au début et quand
quelque chose d'inattendu arrive (lave découverte, coffre, monstre) — pas dans la boucle.

## 4 · Les problèmes à gérer — la liste qui fait la différence

C'est ici que se joue « prendre en compte tous les soucis ». Chaque ligne est une réaction attendue.

| # | Problème | Réaction |
|---|---|---|
| 1 | **Blocs incassables** (bedrock, barrier, portail de l'End) | Ne pas boucler : marquer et contourner, signaler en fin de rapport |
| 2 | **Lave** | Dégâts + propagation. Détecter avant de creuser, ne jamais ouvrir une poche sans être au-dessus, seau ou bloc de remplissage |
| 3 | **Eau** (mer, rivière, nappe) | Une fouille ouverte se noie. Cloisonner d'abord, écoper, ou travailler par caissons |
| 4 | **Gravité** (sable, gravier, béton poudreux) | Chute et étouffement. Miner par couches descendantes, soutenir au-dessus |
| 5 | **Chute du bot** | Ne jamais creuser la case sous soi ; garder un bloc de secours ; échafaudage |
| 6 | **Mobs** (spawn dans le noir, creeper) | Creuser consomme la lumière : éclairer au fil de l'eau, ou armer l'agent, ou work dans les zones déjà éclairées |
| 7 | **Coffres et blocs à butin** | Ne jamais casser à l'aveugle : ouvrir, inventorier, mettre de côté, signaler la valeur |
| 8 | **Blocs à entité** (fourneaux, machines du mod) | Casser proprement, récupérer le contenu, ne pas détruire une machine en cours de production |
| 9 | **Spawners** | À casser en priorité : ils repeuplent la zone pendant le chantier |
| 10 | **Supports** (torche, rail, cactus, pancarte, chorus) | Casser le support fait tomber l'objet : ramasser, ne pas perdre |
| 11 | **Inventaire plein** | Dépôt intermédiaire (coffre ou conteneur du mod), tri, ne jamais jeter un drop |
| 12 | **Durabilité des outils** | Suivre l'usure, remplacer avant la casse, prévoir le ravitaillement |
| 13 | **Chunks non chargés** | `forceload` (limite 256 chunks par commande), reprise si le monde se décharge |
| 14 | **Zones protégées** | `ClaimIntegration` existe déjà : refus net, pas de « presque » |
| 15 | **Budget serveur** | N actions par tick, mesure du TPS, pause si ça s'effondre ; un chantier ne doit pas pourrir la partie |
| 16 | **Redémarrage / crash** | Journal persistant : reprise exacte, jamais de reprise à zéro |
| 17 | **Monde moddé** | Blocs du mod à reconnaître (céramite, minerais), et machines à traiter comme des blocs à entité |
| 18 | **Rollback** | Chaque bloc cassé est noté avec son état : « annule les 10 dernières minutes » doit être possible |

## 5 · Les trois tâches

### EXCAVATE — « mine tout dans cette zone »
Zone = deux coins. Options : conserver les minerais, ignorer les coffres, éclairer ou non.
Ordre de travail : par couches, en commençant par le haut ; caissons si liquides ; ramassage continu.
Rapport final : blocs cassés, blocs impossibles, blocs précieux mis de côté, temps, TPS moyen.

### FARM — « occupe-toi de ce champ »
Labour, irrigation (eau à moins de 4 blocs), lumière ≥ 9, plantation, récolte et **replantation**
dans le même passage, compostage. Généralisable aux quatre cultures et à la canne à sucre.

### BUILD — « construis ça ici »
Plan au format `.bin.gz` du mod (celui des vaisseaux : on l'a déjà et il est vérifié).
Liste de courses des matériaux, ordre de pose bas → haut, blocs temporaires d'échafaudage retirés à
la fin, vérification finale bloc par bloc (le même contrôle que `verif_ship.py`).

## 6 · Sécurité — non négociable

- **Zone confirmée explicitement** : coordonnées relues à voix haute avant de commencer.
- **Refus des zones protégées**, sans exception.
- **Journal obligatoire** et rollback disponible.
- **Arrêt d'urgence** : commande `/work stop` qui fige l'agent au tick suivant.
- **Budget TPS** : si le serveur descend sous un seuil, l'agent se met en pause tout seul.
- **Jamais d'armes ni d'explosifs** dans l'espace d'actions : un creeper suffit à ruiner la zone.

## 7 · Plan de réalisation

| Étape | Contenu | Critère d'acceptation |
|---|---|---|
| **1** | Substrat : scan, dangers, exécuteur budgeté, journal | `/work scan <zone>` liste les dangers sans rien casser |
| **2** | EXCAVATE, dangers 1-5 et 11-13 | Une zone de test creusée à 100 %, 0 perte de drop, reprise après redémarrage |
| **3** | FARM puis BUILD | Un champ récolté et replanté ; un vaisseau construit depuis `.bin.gz` et vérifié |
| **4** | Le LLM au-dessus | « nettoie cette colline » produit un TaskSpec correct et un chantier conforme |

Chaque étape se prouve par un contrôle automatique **et** par ton œil quand c'est visible.

## 8 · Honnêteté sur les limites

- L'agent ne **voit** rien : pas de rendu. Il sait, il ne regarde pas. Tout ce qui est visuel reste à toi.
- Les cas tordus (mer ouverte, lave en cascade, gravité en chaîne, village) demandent des **essais
  réels** — c'est là que passe le temps, pas dans le code de minage.
- « Généralisable » veut dire : même substrat, nouveau planificateur. Ça ne veut pas dire que
  l'agriculture marchera du premier coup parce que le minage marche.
