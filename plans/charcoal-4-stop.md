# 4 charbon de bois dans le coffre [-94,65,6]

## Objectif
Fabriquer 4 `minecraft:charcoal` et les laisser dans le coffre [-94,65,6], sans casser aucune
construction (rails, planches posées, torche, coffres, table, four).

## Faits établis (tick 6415763–6416162, overworld)
- Compagnon `voiddynasty:servitor_2` présent, awake, pos [-103,65,4], 200/200 PV, feet_free true.
- `servitor_materials(charcoal, 4, [-94,65,6])` : `ready:false`, 1 étape `smelt` 4×,
  input `minecraft:dark_oak_log`, `accepts` = tag des 9 essences (log/wood/stripped).
  `alternative_input: minecraft:birch_log`, `found_in_box: 84` dans
  [-119,49,-12]→[-87,73,20] (couverture 27225/27225).
  `suggested_zone` = [-112,64,-6]→[-111,73,-4] → **obsolète** : observe 60/60 = 100 % air.
- Carburant déjà dans le coffre : coal ×7 (1600 ticks chacun) → largement assez pour 4×200 ticks.
- Coffre contient aussi : dirt 1, oak_planks 22, chest 1, stick 4, birch_planks 4, coal 7. Aucune bûche.

## Zone de récolte retenue (vérifiée)
[-106,62,0] → [-99,73,7] : 768/768 cellules observées, `complete:true`, hazards vides.
Contenu : air 480, dirt 129, birch_leaves 87, grass_block 62, **birch_log 10**.
Aucun rail, aucune planche, aucun coffre, aucune table, aucun four, aucune torche dans la boîte.
Deux troncs : x=-105 z=3 (y65-69) et x=-102 z=4 (y65-69).

## Missions
1. `charcoal-bois-t1` — GATHER `minecraft:birch_log`, amount 6, zone [-106,62,0]→[-99,73,7],
   destination [-94,65,6], origin [-103,65,4], access=surface.
   Critère : `SUCCEEDED` avec `delivered_total >= 4` ; coffre relu par `data get block`.
2. Contrôle coffre (birch_log ≥ 4) — sinon corriger (zone/rampe) avant l'étape suivante.
3. `servitor_move(goto)` vers un point à ≤ 5 blocs du four [-94,65,4], puis
   `servitor_move(status)` jusqu'à busy:false.
4. `servitor_smelt("minecraft:birch_log", 4, [-94,65,6])` (ou 4× dark_oak impossible) — le four doit
   être à ≤ 5 blocs, le carburant dans le même coffre.
   Critère : ok:true, 4 `minecraft:charcoal` dans [-94,65,6].
5. Relecture du coffre (`data get block`), compte rendu chiffré.

## Résultat (tick 6417663)
- Mission `charcoal-bois-t1` = e5c0d768-843b-430f-9af9-d9add517af54 → **INCOMPLETE / SOURCE_EXHAUSTED**
  (amount demandé 6, `collected_total` 4, `delivered_total` 4). Suffisant : il fallait 4 bûches.
- Coffre [-94,65,6] avant fonte : birch_log ×4, coal ×7, reste inchangé.
- Four [-94,65,4] : bien un `minecraft:furnace`, vide, non allumé. Compagnon à 4 blocs → smelt direct.
- `servitor_smelt(minecraft:charcoal, 4, [-94,65,6])` → **ok:true**, produced 4, input_consumed 4
  (birch_log via `accepts`), fuel minecraft:coal ×1, 800 ticks, XP 0.6.
  Piège d'API : `item` = le RÉSULTAT attendu, pas l'entrée. `item=birch_log` → `NO_RECIPE`.
- Coffre après fonte : **charcoal ×4**, coal 6, dirt 1, oak_planks 22, chest 1, stick 4, birch_planks 4,
  plus aucune bûche. Four vide après coup.
- Intégrité zone (même boîte 27225 cellules, 100 % couverte) : birch_log 84→80 (−4), air 9008→9012.
  rails 28, powered_rail 5, birch_planks 4, chest 1, crafting_table 1, furnace 1, redstone_torch 1 :
  **aucun changement**.

## Reste à vérifier
- Rien. Objectif atteint (4/4 charbon de bois livrés dans le coffre).

