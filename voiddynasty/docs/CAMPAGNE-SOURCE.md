# La campagne de la source — relevé des apparitions

> Document **extrait**, pas écrit : chaque valeur vient des fonctions `mcfunction` de la source.
> Il remplace la sous-tâche « apparition naturelle par biome et factions », qui **n'existe pas**
> dans la source.

## Pourquoi il n'y a pas d'apparition naturelle

`gamerule doMobSpawning false` est posée au chargement du datapack GameEngine. L'extrait complet
ne contient **aucune** donnée de biome, **aucune** règle d'apparition, **aucune** notion de
faction. Tous les ennemis arrivent par des `summon` scriptés. Le seul « système d'apparition »
de la source **est** la campagne — c'est donc la campagne qu'on relève.

## Chiffres

- **59 zones** scriptées
- **185 apparitions** d'ennemis, à des coordonnées fixes
- **22 points de réapparition** (index de niveau → coordonnées)
- **18 types de créatures** invoquées

### Créatures invoquées, par nombre d'apparitions

| créature | apparitions |
|---|---|
| `marine1` | 42 |
| `marine3` | 18 |
| `servitor2` | 18 |
| `marine2` | 17 |
| `servitor1` | 17 |
| `servitor3` | 14 |
| `cultist1` | 13 |
| `raptor` | 12 |
| `cultist2` | 10 |
| `chaosdreadnought` | 6 |
| `cultist3` | 6 |
| `cultist4` | 3 |
| `helldrake` | 3 |
| `cultistboss` | 2 |
| `demonprince` | 1 |
| `hellbrute` | 1 |
| `forgefiend` | 1 |
| `marinecaptain` | 1 |

## Les zones, dans l'ordre de la campagne

| niveau | zone | titre affiché | ennemis invoqués |
|---|---|---|---|
| 0 | `cryoroom` | Cryo-Room | — |
| 1 | `corridor1` | Service Corridor | cultist2 (276.0 65.0 53.0) |
| 1 | `cryoroom7` | — | — |
| 2 | `armoury` | Armoury | cultist1 (252.0 89.0 62.0) · cultist2 (238.0 89.0 58.0) · cultist1 (238.0 89.0 67.0) · cultist4 (226.0 89.0 58.0) · cultist1 (207.0 89.0 59.0) · cultist2 (191.0 100.0 71.0) |
| 3 | `sanctum` | — | cultistboss (249.0 101.0 9.0) · cultist3 (246.0 100.0 20.0) · cultist3 (246.0 100.0 -1.0) · cultist2 (241.0 100.0 24.0) · cultist1 (222.0 100.0 -5.0) |
| 4 | `sanctum3` | — | chaosdreadnought (288.0 101.0 9.0) |
| 5 | `part2/serviceducts` | Service Ducts | servitor2 (247.0 100.0 -51.0) · servitor1 (267.0 100.0 -51.0) · servitor2 (293.0 100.0 -49.0) · servitor2 (293.0 100.0 -55.0) · servitor3 (299.0 100.0 -80.0) |
| 6 | `part2/hanger1` | Starboard Hanger | marine1 (156.0 161.0 -36.0) · servitor2 (172.0 130.0 -12.0) · servitor1 (150.0 130.0 -12.0) · servitor2 (137.0 139.0 -13.0) · marine1 (172.0 139.0 -13.0) · marine3 (180.0 161.0 -32.0) |
| 7 | `part2/hanger2` | — | — |
| 8 | `part2/porthanger` | Port Hanger | marine1 (151.0 161.0 30.0) · servitor2 (180.0 161.0 53.0) · servitor1 (160.0 161.0 53.0) · marine1 (154.0 107.0 41.0) · marine1 (153.0 107.0 54.0) · servitor3 (142.0 95.0 47.0) · servitor2 (147.0 95.0 42.0) · servitor2 (140.0 95.0 52.0) · servitor1 (139.0 90.0 30.0) · cultist3 (174.0 130.0 30.0) · cultist2 (174.0 130.0 30.0) · cultist1 (174.0 130.0 30.0) · cultist3 (144.0 139.0 29.0) · cultist2 (144.0 139.0 29.0) · cultist1 (144.0 139.0 29.0) · cultist2 (144.0 139.0 29.0) · servitor3 (170.0 95.0 47.0) |
| 9 | `part2/gunnery` | Gunnery Decks | servitor3 (122.0 113.0 24.0) · servitor3 (108.0 113.0 24.0) · servitor2 (96.0 113.0 24.0) · servitor2 (96.0 113.0 24.0) |
| 10 | `part2/crew` | Crew Quaters | marine3 (36.0 123.0 -14.0) · marine3 (4.0 123.0 -14.0) · marine3 (12.0 123.0 -20.0) · marine3 (-9.0 123.0 -16.0) |
| 11 | `part2/airlock` | Airlock Access | marine1 (-15.0 119.0 34.0) · marine1 (-15.0 119.0 34.0) · marine1 (-15.0 119.0 34.0) |
| 12 | `part2/exterior` | — | raptor (-2.0 69.0 33.0) · raptor (-56.0 67.0 33.0) · raptor (-101.0 71.0 29.0) · raptor (-117.0 81.0 21.0) |
| 13 | `part2/part3/torpedo` | — | marine1 (-167.0 77.0 43.0) · marine2 (-167.0 77.0 42.0) · marine1 (-167.0 77.0 44.0) · marine1 (-198.0 77.0 43.0) · marine1 (-198.0 77.0 42.0) · marine1 (-198.0 77.0 44.0) |
| 14 | `part2/part3/promenade` | — | cultist1 (-89.0 124.0 3.0) · cultist2 (-89.0 124.0 2.0) · cultist3 (-89.0 124.0 4.0) · cultist1 (-89.0 124.0 1.0) · cultist4 (-89.0 124.0 5.0) · cultist1 (-89.0 124.0 6.0) · cultistboss (-99.0 124.0 7.0) · cultist1 (-82.0 124.0 3.0) · cultist2 (-82.0 124.0 2.0) · cultist3 (-82.0 124.0 4.0) · cultist1 (-82.0 124.0 1.0) · cultist4 (-82.0 124.0 5.0) · cultist1 (-82.0 124.0 6.0) |
| 15 | `part2/part3/lift` | — | marine1 (186.0 77.0 -5.0) · marine2 (186.0 77.0 12.0) · marine1 (177.0 109.0 -4.0) |
| 16 | `part2/part3/storage` | — | — |
| 17 | `part2/part3/warp` | — | helldrake (275.0 187.0 168.0) · raptor (273.0 175.0 191.0) · raptor (274.0 175.0 191.0) · raptor (272.0 175.0 191.0) · raptor (389.0 179.0 190.0) · raptor (389.0 179.0 191.0) · raptor (343.0 198.0 128.0) · raptor (288.0 221.0 97.0) · raptor (294.0 226.0 68.0) |
| 18 | `part2/part3/part4/hall` | — | — |
| 19 | `part2/part3/part4/bridge` | — | demonprince (492.0 281.0 8.0) · marine1 (473.0 268.0 19.0) · marine2 (474.0 268.0 19.0) · marine3 (472.0 268.0 19.0) · servitor3 (412.0 268.0 8.0) · marine1 (493.0 268.0 -2.0) · marine1 (493.0 268.0 -1.0) · marine1 (493.0 268.0 -3.0) · servitor3 (545.0 278.0 33.0) · servitor3 (545.0 278.0 -25.0) |
| 20 | `part2/part3/part4/finalduct` | — | hellbrute (499.0 256.0 9.0) |
| 21 | `part2/part3/part4/reactor` | — | helldrake (556.0 225.0 58.0) · helldrake (549.0 132.0 -58.0) · chaosdreadnought (573.0 222.0 14.0) · marine1 (573.0 220.0 3.0) · servitor3 (584.0 126.0 45.0) · servitor3 (589.0 190.0 92.0) · chaosdreadnought (597.0 236.0 -61.0) · marine1 (vague en 595 214 9) · marine1 (vague en 595 214 9) · marine2 (vague en 595 214 9) · marine3 (vague en 595 214 9) · marine1 (vague en 646 226 9) · marine1 (vague en 646 226 9) · marine2 (vague en 646 226 9) · marine3 (vague en 646 226 9) · marine1 (vague en 596 236 78) · marine1 (vague en 596 236 78) · marine2 (vague en 596 236 78) · marine3 (vague en 596 236 78) · marine1 (vague en 523 190 98) · marine1 (vague en 523 190 98) · marine2 (vague en 523 190 98) · marine3 (vague en 523 190 98) · marine1 (vague en 524 137 8) · marine1 (vague en 524 137 8) · marine2 (vague en 524 137 8) · marine3 (vague en 524 137 8) |
| — | `armoury2` | — | — |
| — | `corridor2` | Bridge Access | — |
| — | `cryoroom2` | — | — |
| — | `cryoroom3` | — | — |
| — | `cryoroom4` | — | — |
| — | `cryoroom5` | — | — |
| — | `cryoroom6` | — | — |
| — | `part2/airlock2` | — | marine1 (5.0 89.0 29.0) · marine2 (5.0 89.0 29.0) · marine1 (5.0 89.0 29.0) |
| — | `part2/crew2` | — | — |
| — | `part2/exterior2` | — | — |
| — | `part2/gunnery2` | — | servitor2 (87.0 122.0 30.0) · servitor2 (77.0 122.0 30.0) · servitor1 (67.0 122.0 30.0) |
| — | `part2/gunnery3` | — | servitor2 (67.0 143.0 34.0) · servitor1 (54.0 143.0 34.0) |
| — | `part2/gunnery4` | — | — |
| — | `part2/hanger3` | — | — |
| — | `part2/part3/part4/bridge2` | — | — |
| — | `part2/part3/part4/bridge3` | — | — |
| — | `part2/part3/part4/bridge4` | — | — |
| — | `part2/part3/part4/endgame` | — | — |
| — | `part2/part3/part4/hall2` | — | marine1 (405.0 221.0 98.0) · marine2 (406.0 221.0 98.0) · chaosdreadnought (452.0 221.0 70.0) · marine3 (456.0 221.0 70.0) · marine1 (450.0 221.0 70.0) |
| — | `part2/part3/part4/hall3` | — | — |
| — | `part2/part3/part4/hall4` | — | — |
| — | `part2/part3/part4/marinesummon` | — | marine1 (vague en position de la zone) · marine1 (vague en position de la zone) · marine2 (vague en position de la zone) · marine3 (vague en position de la zone) |
| — | `part2/part3/promenda2` | — | servitor1 (-42.0 124.0 8.0) · servitor2 (-42.0 124.0 9.0) · servitor2 (-42.0 124.0 7.0) · servitor2 (-42.0 124.0 6.0) · servitor1 (-42.0 124.0 10.0) · servitor1 (-42.0 124.0 11.0) · servitor3 (-43.0 124.0 10.0) · servitor3 (-43.0 124.0 8.0) |
| — | `part2/part3/promenda3` | — | marine1 (79.0 124.0 10.0) · marine2 (79.0 124.0 11.0) · marine2 (79.0 124.0 9.0) · marine1 (79.0 124.0 8.0) · marine3 (77.0 124.0 8.0) · marine3 (77.0 124.0 10.0) |
| — | `part2/part3/promenda4` | — | chaosdreadnought (149.0 124.0 5.0) · chaosdreadnought (149.0 124.0 13.0) |
| — | `part2/part3/storage2` | — | forgefiend (391.0 44.0 9.0) |
| — | `part2/part3/storage3` | — | — |
| — | `part2/part3/storage4` | — | — |
| — | `part2/part3/torpedo2` | — | marine2 (-197.0 90.0 30.0) · marine3 (-197.0 90.0 31.0) · marine2 (-203.0 90.0 7.0) · servitor3 (-203.0 90.0 -4.0) |
| — | `part2/part3/torpedo3` | — | marine3 (-191.0 90.0 -12.0) · marine1 (-191.0 90.0 -4.0) · servitor1 (-166.0 90.0 -4.0) · servitor2 (-167.0 90.0 -4.0) · servitor3 (-165.0 90.0 -4.0) |
| — | `part2/part3/torpedo4` | — | marine2 (-171.0 102.0 9.0) · marine1 (-176.0 104.0 26.0) · marine3 (-170.0 118.0 7.0) |
| — | `part2/part3/torpedo5` | — | marinecaptain (-179.0 123.0 9.0) · marine1 (-167.0 123.0 14.0) · marine1 (-167.0 123.0 3.0) |
| — | `part2/part3/torpedo6` | — | — |
| — | `part2/serviceducts2` | — | servitor2 (292.0 132.0 -89.0) · servitor1 (290.0 132.0 -82.0) · servitor1 (267.0 132.0 -84.0) · servitor1 (267.0 132.0 -86.0) · servitor1 (255.0 132.0 -85.0) |
| — | `part2/serviceducts3` | — | servitor1 (237.0 130.0 -40.0) · servitor1 (237.0 130.0 -40.0) · marine2 (237.0 130.0 -40.0) · servitor1 (205.0 130.0 -30.0) · cultist1 (205.0 130.0 -30.0) · cultist2 (205.0 130.0 -30.0) |
| — | `sanctum2` | — | — |

## Points de réapparition (index de niveau → coordonnées)

| niveau | x | y | z |
|---|---|---|---|
| 0 | 316 | 90 | 111 |
| 1 | 282 | 65 | 75 |
| 2 | 264 | 89 | 55 |
| 3 | 239 | 100 | 38 |
| 4 | 273 | 101 | 9 |
| 5 | 239 | 100 | -33 |
| 6 | 180 | 130 | -15 |
| 7 | 170 | 168 | -7 |
| 8 | 170 | 168 | 26 |
| 9 | 129 | 90 | 31 |
| 10 | 49 | 143 | -10 |
| 11 | -31 | 143 | 29 |
| 12 | 5 | 64 | 29 |
| 13 | -147 | 77 | 42 |
| 14 | -134 | 124 | 8 |
| 15 | 174 | 123 | 9 |
| 16 | 185 | 44 | 8 |
| 17 | 327 | 65 | 145 |
| 18 | 314 | 221 | 79 |
| 19 | 502 | 268 | 21 |
| 20 | 481 | 256 | -2 |
| 21 | 535 | 220 | 9 |

Toutes les vagues locales ont été dépliées : chaque appel `function
mj.gameengine:…` pointe vers une fonction existante du relevé.
