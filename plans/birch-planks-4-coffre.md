# Plan — 4 planches de bouleau dans le coffre [-94,65,6]

## Objectif
Fabriquer 4 `minecraft:birch_planks` et les faire atterrir dans le coffre [-94,65,6]
(overworld), en récoltant d'abord la bûche de bouleau manquante.

## État observé (servitor_status / servitor_look / servitor_observe)
- Compagnon : voiddynasty:servitor_2, présent, awake, pos [-91,66,4], 200/200 PV,
  on_ground=true, feet_free=true, sac vide (bag_entries=0). Dimension overworld.
- Aucun joueur connecté (players_online=0). pause-when-empty-seconds=0 sur mc-263 → le
  serveur tick quand même (sampled_tick avance).
- Coffre [-94,65,6] confirmé présent (hazard container dans servitor_look).
- Table de craft présente dans la boîte [-99..-83, 62..69, -4..12] — inutile ici (recette 2x2).

## Plan d'approvisionnement (servitor_materials, radius 32)
- `minecraft:birch_planks` x4 → 1 étape, station `bag` (grille 2x2, pas de table).
  - 1 `minecraft:birch_log` → 4 `minecraft:birch_planks`.
- Coffre contient : dirt 1, oak_planks 22, chest 1, stick 4. Aucune bûche de bouleau.
- `to_gather` : birch_log manquant 1, gatherable=true, source minecraft:birch_log.

## Zone de récolte retenue
- `servitor_find` sur [-111..-71, 60..74, -14..26] : 94 birch_log (couverture complète).
- Zone retenue : **[-89,68,-18] .. [-78,78,-8]** (1452 blocs, volume < 32768).
  Couverture `complete: true`, `hazards: {}` → aucun rail, planche posée, torche
  redstone, coffre ni construction dans la boîte. 5 birch_log présents.
- Zone écartée : boîte large de `find` — elle contient rail 33, powered_rail 5,
  redstone_torch 1, birch_planks 19, 2 coffres (construction). On n'y touche pas.
- `access=surface` : aucune roche à dégager, la bûche est en surface.

## Missions
1. **GATHER t1** — `request_id=birch-planks-4-t1`, item minecraft:birch_log,
   amount 1, zone [-89,68,-18]..[-78,78,-8], destination [-94,65,6],
   origin [-91,66,4] (position réelle du compagnon).
   Réussite : `SUCCEEDED` + `delivered_total` ≥ 1 + coffre contient 1 birch_log.
2. **CRAFT t2** — `servitor_craft("minecraft:birch_planks", 4, chest=[-94,65,6])`,
   station `bag` → aucun déplacement nécessaire.
   Réussite : coffre contient 4 birch_planks, et birch_log consommée (0).

## Vérifications entre étapes
- mission_status → statut + compteurs ; mission_wait pour attendre l'issue.
- servitor_work → last_stop, done, skipped, has_depot.
- servitor_observe sur [-94,65,6] pour relire le contenu du coffre (ou gather).
- servitor_status → awake, on_ground, feet_free après la mission.

## Résultat (exécuté)
- GATHER t1 : mission `14d6ae5a-43db-4f01-99af-31806f5f48bc`, statut **SUCCEEDED**,
  collected_total=1, delivered_total=1, goal_met=true. last_stop du moteur :
  « termine: 1 cassés, 3 laissés, 0 objets en coffre — curseur termine (blocs proteges
  et butin laisses en place) ». access_blocks_mined=0.
- Relecture du coffre après gather : birch_log=1 (dirt 1, oak_planks 22, chest 1, stick 4).
- CRAFT t2 : `servitor_craft("minecraft:birch_planks", 4, chest=[-94,65,6])` →
  ok, station=bag, batches=1, crafted=4, consumed=[minecraft:birch_log].
- Relecture du coffre après craft (servitor_materials, ready=true) :
  birch_planks=4, birch_log=0, dirt 1, oak_planks 22, chest 1, stick 4.
- Zone de récolte après mission (observe, couverture complete) : birch_log 5 → 4,
  air 988 → 989. Aucun rail / planche / torche / coffre dans la boîte : hazards {} avant
  et après. construction intacte.
- Compagnon : pos [-94,65,2], awake=true, on_ground=true, feet_free=true,
  standing_on=grass_block, 200/200 PV, sac vide.

## À ne pas abîmer
rails (33 + 5 powered), birch_planks posées (19), redstone_torch (1), coffres
([-94,65,6] et [-80,63,10]), table de craft, bâtiment/chantier au sol.
