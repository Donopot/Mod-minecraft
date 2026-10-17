# CORE-024 — essai de rendu par technique

Validation automatique : `python3 tools/validate_assets.py` (0 anomalie requise).

Test visuel : en jeu (client), tape la commande — le visuel apparaît 2 blocs devant toi pendant 10 s.

## 1. Visuels de projectiles (60) — move id / modèle / texture

| technique (move id) | commande |
|---|---|
| `daifunka` | `/dfvisual fist_small tex_magma` |
| `inugamiguren` | `/dfvisual inugami_guren inugami_guren` |
| `ryuseikazan` | `/dfvisual fist_small tex_magma` |
| `higan` | `/dfvisual fist_small flame_tiger` |
| `hotarubi` | `/dfvisual ball_maximum flame_tiger` |
| `raigo` | `/dfvisual ball_maximum meteor` |
| `icepartisan` | `/dfvisual partisan tex_ice` |
| `pheasantbeak` | `/dfvisual phoenix tex_ice` |
| `icespikes` | `/dfvisual partisan tex_ice` |
| `laser1` | `/dfvisual ball_maximum meteor` |
| `laser2` | `/dfvisual ball_maximum meteor` |
| `magatama` | `/dfvisual projectile_slash clear` |
| `darkball` | `/dfvisual rubble tex_black` |
| `darkmatter` | `/dfvisual rubble tex_black` |
| `desertspada` | `/dfvisual projectile_slash projectile_slash` |
| `barjan` | `/dfvisual entity_slash slash_blue` |
| `sables` | `/dfvisual tornado slash_blue` |
| `desertlaspada` | `/dfvisual projectile_slash projectile_slash` |
| `whiteblow` | `/dfvisual fist_small clear` |
| `whitevine` | `/dfvisual tornado slash_blue` |
| `root` | `/dfvisual rock_fragment rock_fragment` |
| `rootgiant` | `/dfvisual rock_fragment rock_fragment` |
| `rootsurround` | `/dfvisual rock_fragment rock_fragment` |
| `rankyaku` | `/dfvisual projectile_slash projectile_slash` |
| `lupusfall` | `/dfvisual jabra_wolf_projectile jabra_wolf_projectile` |
| `gaicho` | `/dfvisual projectile_slash projectile_slash_gaicho` |
| `impactball` | `/dfvisual rubble clear` |
| `narikabura` | `/dfvisual projectile_tooth tex_ice` |
| `bluebird` | `/dfvisual phoenix tex_ice` |
| `kongokabura` | `/dfvisual projectile_tooth clear` |
| `kaifu` | `/dfvisual projectile_slash projectile_slash` |
| `borobreath` | `/dfvisual rubble clear` |
| `barizodon` | `/dfvisual projectile_slash projectile_slash` |
| `blackcofee` | `/dfvisual rubble clear` |
| `brachiolauncher` | `/dfvisual ball_maximum meteor` |
| `sparkingqueen` | `/dfvisual ball_maximum meteor` |
| `shigan` | `/dfvisual projectile_slash projectile_slash` |
| `gagan` | `/dfvisual projectile_tooth clear` |
| `negativehollow` | `/dfvisual ghost1 ghost1_1` |
| `minihollow` | `/dfvisual ghost2 clear` |
| `tokuhoro` | `/dfvisual ghost2 clear` |
| `kumac` | `/dfvisual kumac kumac` |
| `padho` | `/dfvisual pad clear` |
| `ursusshock` | `/dfvisual pad clear` |
| `meteorsmall` | `/dfvisual ball_maximum meteor` |
| `rubble` | `/dfvisual rubble clear` |
| `meteor` | `/dfvisual ball_maximum meteor` |
| `biscuitwarrior` | `/dfvisual rock_fragment rock_fragment` |
| `biscuitwarrior2` | `/dfvisual rock_fragment rock_fragment` |
| `yakimochi` | `/dfvisual fist_small flame_tiger` |
| `musodonatsu` | `/dfvisual muso_donatsu tex_white3` |
| `noteattack` | `/dfvisual note_3d clear` |
| `notelaser` | `/dfvisual note_3d clear` |
| `stonethrowing` | `/dfvisual rock_fragment rock_fragment` |
| `stonethrowinglarge` | `/dfvisual rock_fragment rock_fragment` |
| `projectileslash` | `/dfvisual projectile_slash projectile_slash` |
| `pistolkiss` | `/dfvisual ball_maximum clear` |
| `nosefancycannon` | `/dfvisual ball_maximum clear` |
| `arrow` | `/dfvisual aroaro_arrow aroaro_arrow` |
| `brickbat` | `/dfvisual rubble tex_black` |

## 2. Visuels de mêlée / invocations (18)

| technique (move id) | commande |
|---|---|
| `hiken` | `/dfvisual fist_small flame_tiger` |
| `volcanicexplosion` | `/dfvisual fist_small tex_magma` |
| `daienkai` | `/dfvisual fist_small flame_tiger` |
| `iceball` | `/dfvisual ice_ball tex_ice` |
| `iceage` | `/dfvisual ice_ball tex_ice` |
| `elthor` | `/dfvisual ball_maximum meteor` |
| `sango` | `/dfvisual ball_maximum meteor` |
| `kurouzu` | `/dfvisual rubble tex_black` |
| `blackhole` | `/dfvisual rubble tex_black` |
| `whitelauncher` | `/dfvisual fist_small clear` |
| `whiteout` | `/dfvisual tornado slash_blue` |
| `hibashira` | `/dfvisual tornado slash_blue` |
| `kaenryuo` | `/dfvisual fist_small flame_tiger` |
| `doppelman` | `/dfvisual gecko_moria tex_black` |
| `raitei` | `/dfvisual zeus zeus` |
| `hebunrifire` | `/dfvisual prometheus prometheus` |
| `notewarrior` | `/dfvisual note_warrior1 note_warrior1` |
| `summontotmusica` | `/dfvisual muso_donatsu tex_white3` |

## 3. Replis par élément (projectile sans mapping explicite)

| élément | commande |
|---|---|
| repli 1 | `/dfvisual fist_small flame_tiger` |
| repli 2 | `/dfvisual partisan tex_ice` |
| repli 3 | `/dfvisual ball_maximum meteor` |
| repli 4 | `/dfvisual ball_maximum clear` |
| repli 5 | `/dfvisual fist_small clear` |

## 4. Formes hybrides Zoan — `/devilfruit transform` avec le fruit équipé (15 textures empilées)

| texture | usage |
|---|---|
| `inu_inu_okuchinomakami_humanbeast` | couche de transformation |
| `inu_inu_wolf_humanbeast` | couche de transformation |
| `nekoneko_leopard_humanbeast` | couche de transformation |
| `nekoneko_sabertiger_humanbeast` | couche de transformation |
| `ryu_ryu_brachiosaurus_humanbeast` | couche de transformation |
| `ryu_ryu_pachycephalosaurus_humanbeast` | couche de transformation |
| `ryu_ryu_pteranodon_humanbeast` | couche de transformation |
| `toritori_phoenix_humanbeast` | couche de transformation |
| `transform_hito_hito_daibutsu_humanbeast` | couche de transformation |
| `transform_inu_inu_wolf_humanbeast` | couche de transformation |
| `transform_ryu_ryu_pachycephalosaurus_humanbeast` | couche de transformation |
| `uouo_dragon_human_beast` | couche de transformation |
| `uouo_dragon_human_beast_skin` | couche de transformation |
| `ushi_ushi_giraffe_humanbeast` | couche de transformation |
| `wing_bat_bat` | couche de transformation |

## 5. Animation ciblée (bras Gomu)

`/dfsummon <x> <y> <z> <ticks> <modèle> <texture>` accepte désormais un modèle et une texture explicites.
Le bras Gomu joue le clip `punch_gomu_pistol` de `entity_gomu_arm`.
