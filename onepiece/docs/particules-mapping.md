# Particules : correspondance Fruit → particule du jar source

Établie par `tools/particle_wiring.py` sur l'**élément** de chaque Fruit, en employant les
particules **du jar source** (42, toutes portées).

Les particules employées comme bouche-trou (`CRIT`, `ELECTRIC_SPARK`, `FLAME`, `CLOUD`,
`SNOWFLAKE`, `SMOKE`…) sont remplacées ; celles choisies **pour une raison** (`EXPLOSION`,
`LAVA`, `PORTAL`, `SWEEP_ATTACK`, `SCULK_SOUL`…) sont **conservées** : les remplacer
effacerait une intention, pas un oubli.

| Fruit | Particule du mod | Remplacements | Conservées |
|---|---|---|---|
| `aro_aro` | SLASH | 0 | — |
| `batto_batto` | BAT | 0 | — |
| `bisu_bisu` | — (incertain) | 4 | — |
| `bomu_bomu` | SHOCK_HIT | 0 | — |
| `gomu_gomu` | SHOCKWAVE | 0 | — |
| `goro_goro` | THUNDER_BLUE | 0 | — |
| `gura_gura` | SHOCKWAVE | 0 | — |
| `hie_hie` | ICE | 0 | — |
| `hito_hito_daibutsu` | SHOCK_HIT | 0 | — |
| `horo_horo` | NEGATIVE | 0 | — |
| `imu` | NEGATIVE | 0 | — |
| `inu_inu_okuchi_no_makami` | CLAWS | 0 | — |
| `kage_kage` | DARK | 0 | — |
| `kira_kira` | LIGHT_LARGE | 0 | END_ROD |
| `kiro_kiro` | GRAVITY | 0 | — |
| `magu_magu` | MAGMA | 0 | — |
| `mera_mera` | FLAME | 0 | — |
| `mero_mero` | FLOWER_PINK | 0 | — |
| `mochi_mochi` | SHOCK_HIT | 0 | — |
| `moku_moku` | BIG_SMOKE | 0 | — |
| `mori_mori` | FLOWER | 0 | — |
| `neko_neko_leopard` | CLAWS | 0 | — |
| `neko_neko_sabertiger` | CLAWS | 0 | — |
| `nikyu_nikyu` | SHOCK_HIT | 0 | — |
| `ope_ope` | LIGHT_SMALL | 0 | END_ROD, PORTAL |
| `pika_pika` | LIGHT_LARGE | 0 | — |
| `riki_riki` | SHOCK_HIT | 0 | — |
| `ryu_ryu_brachiosaurus` | — (incertain) | 5 | — |
| `ryu_ryu_pachycephalosaurus` | SHOCK_HIT | 0 | — |
| `ryu_ryu_pteranodon` | — (incertain) | 3 | — |
| `soru_soru` | THUNDER_BLUE | 0 | — |
| `suna_suna` | SAND | 0 | — |
| `supa_supa` | SLASH | 0 | — |
| `tori_tori_phoenix` | FLAME_BLUE | 0 | — |
| `uo_uo_seiryu` | WATER | 0 | — |
| `ushi_ushi_giraffe` | CLAWS | 0 | — |
| `uta_uta` | NOTE | 0 | — |
| `yami_yami` | DARK | 0 | — |
| `zushi_zushi` | GRAVITY | 0 | — |

## Fruits sans correspondance certaine

Aucune particule attribuée (élément non tranché) : `ryu_ryu_pteranodon`, `ryu_ryu_brachiosaurus`, `bisu_bisu`

Ces techniques gardent leur particule actuelle. Leur attribuer une particule « au
jugé » aurait l'air d'un progrès et serait une invention.

## Services câblés (leur nom porte l'élément)

| Fichier | Remplacements |
|---|---|
| `procedures/MagmaBurn.java` | FLAME → `MAGMA` |
| `procedures/IceBreath.java` | CLOUD, SNOWFLAKE → `ICE` |
| `procedures/IceDefense.java` | SNOWFLAKE → `ICE` |
| `procedures/SandStorm.java` | CRIT → `SAND` |
| `procedures/SmokeCloud.java` | LARGE_SMOKE, SMOKE → `BIG_SMOKE` |
| `procedures/NegativeState.java` | SCULK_SOUL → `NEGATIVE` |
| `procedures/Roots.java` | HAPPY_VILLAGER → `FLOWER` |
| `procedures/SeismicWave.java` | CRIT → `SHOCKWAVE` |
| `procedures/GiantShockWave.java` | CLOUD, CRIT → `SHOCK_HIT` |
| `procedures/MeteorFall.java` | FLAME, SMOKE, CRIT → `FLAME`, `BIG_SMOKE`, `SHOCK_HIT` |
| `procedures/ShadowLedger.java` | SQUID_INK → `DARK` |
| `procedures/RemoteControl.java` | SCULK_SOUL → `DARK` |
| `procedures/Levitation.java` | END_ROD → `GRAVITY` |
| `procedures/MochiBuild.java` | CLOUD → `BIG_SMOKE` |

## Remplacement COMPLET (demandé par Donopot)

Les particules « choisies pour une raison » ont été remplacées à leur tour, avec une table qui dit
**par quoi et pourquoi** : `EXPLOSION` → `SHOCK_HIT`, `LAVA` → `MAGMA`, `PORTAL` → `DARK`,
`END_ROD` → `LIGHT_SMALL`, `SWEEP_ATTACK` → `SLASH`, `SCULK_SOUL` → `NEGATIVE`, `HEART` → `FLOWER_PINK`
(la source n'a pas de cœur), `SOUL_FIRE_FLAME` → `FLAME_BLUE`, `POOF`/`WHITE_ASH`/`CAMPFIRE_COSY_SMOKE`
→ `BIG_SMOKE`, `HAPPY_VILLAGER` → `FLOWER`, `CRIT` → la particule du Fruit (contexte par fichier :
`GravityField` → `GRAVITY`, `Desiccation` → `SAND`, `Petrification` → `FLOWER_PINK`, le bloc
Rokushiki → `CLAWS`).

Les 3 fruits à élément non tranché ont reçu une particule sur décision explicite de Donopot :
`bisu_bisu` → `SHOCK_HIT`, `ryu_ryu_brachiosaurus` → `MAGMA` (son « Sparking Queen »), `ryu_ryu_pteranodon`
→ `SLASH` (piqué perforant).

**Résultat mesuré** : usages de particules vanilla **276 → 0** (hors harnais de test), usages des
particules du mod **326**. Seul `DevilFruitTestCommands` garde du vanilla : c'est un harnais de mesure.
