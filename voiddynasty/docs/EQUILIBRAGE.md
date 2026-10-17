# ÉQUILIBRAGE — VOID DYNASTY

Table des dégâts du mod, **posée à plat** et comparée à la source quand elle en donne une.

> Document **généré** par `tools/table_equilibrage.py` : ne pas modifier à la main.
> `python3 tools/table_equilibrage.py --verifier` échoue si ce fichier a divergé du code.

Repères : le joueur a **20 PV** (10 cœurs), et **40 PV** avec la Power Armour. Les dégâts de la source sont ceux de son rapport de mécaniques.

## 1 · Les armes

| Arme | Famille | Mode | Dégâts | Cadence | Chargeur | Recharg. | Perce l'armure | Critique | Projectiles | Munition |
|---|---|---|---|---|---|---|---|---|---|---|
| Hunter Killer Missile | explosive | SEMI | **100** | 0.2/s | 1 | 7.5 s | 65 % | ×1.5 (20 %) | 1 | MISSILE_AMMO |
| Melta Gun | melta | SEMI | **50** | 0.5/s | 5 | 3.8 s | 95 % | ×1.5 (15 %) | 1 | MELTA_CELL |
| Stalker Bolt Rifle | bolt | SEMI | **30** | 0.5/s | 8 | 2.9 s | 65 % | ×2 (25 %) | 1 | STALKER_BOLT_ROUND |
| Power Hammer | melee | MELEE | **25** | 0.667/s | 0 | 0 s | 85 % | ×1.5 (20 %) | 0 | null |
| Plasma Incinerator | plasma | SEMI | **20** | 0.33/s | 16 | 4.4 s | 82 % | ×1.5 (12 %) | 1 | PLASMA_CELL |
| Shotgun | solid | SEMI | **20** | 0.67/s | 8 | 3.1 s | 12 % | ×1.5 (5 %) | 5 | SHOTGUN_SHELL |
| Boltgun | bolt | SEMI | **15** | 4/s | 24 | 2.6 s | 40 % | ×1.5 (10 %) | 1 | BOLT_SHELL |
| Powersword | melee | MELEE | **15** | 1.2/s | 0 | 0 s | 70 % | ×1.5 (15 %) | 0 | null |
| Heavy Bolter | bolt | AUTOMATIC | **12** | 8/s | 80 | 5 s | 48 % | ×1.5 (8 %) | 1 | HEAVY_BOLT_SHELL |
| Plasma Pistol | plasma | SEMI | **10** | 1.25/s | 8 | 3.2 s | 72 % | ×1.5 (10 %) | 1 | PLASMA_CELL |
| Chainsword | melee | MELEE | **10** | 1.5/s | 0 | 0 s | 15 % | ×1.5 (10 %) | 0 | null |
| Bolt Pistol | bolt | SEMI | **7** | 1/s | 12 | 2 s | 32 % | ×1.5 (10 %) | 1 | BOLT_SHELL |
| Flamethrower | flame | CONTINUOUS | **7** | 10/s | 100 | 4 s | 8 % | ×1.5 (2 %) | 5 | PROMETHIUM |
| Las Gun | las | AUTOMATIC | **5** | 5/s | 40 | 2.2 s | 20 % | ×1.5 (10 %) | 1 | LAS_CELL |
| Laz Pistol | las | AUTOMATIC | **3** | 4/s | 24 | 1.7 s | 12 % | ×1.5 (8 %) | 1 | LAS_CELL |

### Dégâts de terrain, et ce que ça donne en blocs

Formule du mod, pour que l'arithmétique soit vérifiable :

```
dégâts d'un impact au point d'impact = puissance − résistance × (1 − percement) × 0,5
un bloc casse quand le cumul atteint   = 10 + résistance × dureté de l'arme
```

| Arme | Mode de terrain | Puissance | Dureté de l'arme | Perce le matériau | Rayon | Blocs modifiés au plus |
|---|---|---|---|---|---|---|
| Melta Gun | THERMAL_BEAM | 110 | 0.98 | 100 % | 0.6 | 20 |
| Hunter Killer Missile | WARHEAD | 90 | 0.82 | 65 % | 3.5 | 60 |
| Plasma Incinerator | PLASMA_SPLASH | 70 | 0.8 | 95 % | 2.5 | 24 |
| Power Hammer | SHOCKWAVE | 54 | 0.85 | 10 % | 1.8 | 12 |
| Powersword | POWER_CUT | 34 | 0.76 | 30 % | 0 | 4 |
| Plasma Pistol | PLASMA_SPLASH | 29 | 0.65 | 85 % | 1.5 | 8 |
| Stalker Bolt Rifle | ARMOR_PIERCING | 24 | 0.72 | 7 % | 0.35 | 3 |
| Heavy Bolter | EXPLOSIVE_PENETRATOR | 21 | 0.55 | 15 % | 1.1 | 4 |
| Boltgun | EXPLOSIVE_PENETRATOR | 16 | 0.42 | 10 % | 0.8 | 3 |
| Bolt Pistol | EXPLOSIVE_PENETRATOR | 11 | 0.3 | 10 % | 0.55 | 2 |
| Chainsword | MELEE_CUT | 9 | 0.18 | 0 % | 0 | 2 |
| Shotgun | PELLET | 5 | 0.11 | 2 % | 0.18 | 1 |
| Flamethrower | CONE_HEAT | 3 | 0.04 | 95 % | 1.2 | 16 |

⚠️ **Ces chiffres ne disent PAS combien de coups il faut.** L'accumulation de dégâts **retombe** entre les tirs : c'est la cadence contre la retombée qui décide, et un calcul posé sur une feuille se trompe. Les valeurs réelles sont **mesurées en jeu** au paragraphe suivant — c'est le harnais qui les a prises, pas moi.

## 2 · Les armures

| Tenue | Casque | Plastron | Jambières | Bottes | Total | Robustesse | Ténacité | Recul |
|---|---|---|---|---|---|---|---|---|
| space_marine | 4 | 10 | 8 | 4 | **26** | 40 | 4 | 50 % |
| terminator | 6 | 14 | 11 | 6 | **37** | 60 | 5 | 70 % |

La **Power Armour** (porter un casque de la tenue) porte la vie maximale du joueur à **40** — mécanisme de la source, qui regardait un casque et posait `max_health base 40`.

## 3 · Les créatures

| Créature | PV | Source (PV) | Contact | Tir | Portée tir | Portée contact | Cooldown tir | Cooldown contact | Source (dégâts) |
|---|---|---|---|---|---|---|---|---|---|
| `daemon_prince` | **1000** | **1000** (`max_health base 1000`, scale 2) | 10 | — | — | 3–5 | — | 1 s | 10 mob_attack (strike), 20 mob_attack (smash, radius 8), charge (speed) |
| `chaos_helldrake` | **500** | **500** (`max_health base 500`) | 50 | — | — | — | — | — | attack_damage 50 (phantom/ghast), follow_range 1000, ghast tire des boules de feu vanilla |
| `chaos_forgefiend` | **400** | **400** (`max_health base 400`) | 30 | 10 ×5 | 7–100 | 0–6 | 0.5 s | 3 s | 30 mob_attack (strike + knockback tp), 10 explosion ×5 raytraces (tir), attack_damage 50 ⚠️ |
| `large_khorne_daemon` | **400** | — | 40 | — | — | — | — | — | — |
| `norn_emissary` | **350** | — | 35 | — | — | — | — | — | — |
| `lord_of_change` | **320** | — | 30 | — | — | — | — | — | — |
| `great_unclean_one` | **300** | — | 28 | — | — | — | — | — | — |
| `keeper_of_secrets` | **280** | — | 26 | — | — | — | — | — | — |
| `chaos_captain` | **250** | **250** (`max_health base 250`) | 12.5 | 12.5 | 4–15 | 0–3 | 2 s | 1 s | 15 fireball (tir), 10 mob_attack (càc) ⚠️ |
| `ghazghkull` | **250** | — | 30 | — | — | — | — | — | — |
| `carnifex` | **200** | — | 25 | — | — | — | — | — | — |
| `servitor_1` | **200** | **200** | 8 | 8 | 0–30 | — | 1 s | — | 8 explosion (tir) |
| `servitor_2` | **200** | **200** | 10 | — | — | 0–2 | — | 1 s | 10 mob_attack (càc) |
| `carnifex_leviathan` | **160** | — | 20 | — | — | — | — | — | — |
| `chaos_dreadnought` | **150** | **150** | 10 | 10 | 3–20 | 0–2 | 3 s | 2 s | 10 falling_anvil (càc), 10 on_fire (tir) |
| `chaos_hellbrute` | **150** | **150** (`generic.max_health base 150`) | 15 | — | — | 0–10 | — | 2 s | 15 falling_anvil (càc), 10 on_fire (collision) |
| `cultist_boss` | **100** | **100** (`/attribute max_health base set 100` ; NBT Health 50/200 incohérent) | 6.5 | 6.5 | 4–10 | 2–4 | 3 s | 3 s | 8 magic (tir), 5 mob_attack (càc+spin) ⚠️ |
| `servitor_3` | **100** | **100** | 15 | — | — | 0–3 | — | 3 s | 15 mob_attack (càc, radius 4) |
| `khorne_juggernaut` | **80** | — | 14 | — | — | — | — | — | — |
| `lictor` | **60** | — | 12 | — | — | — | — | — | — |
| `chaos_marine_1` | **50** | **50** (`max_health base 50`) | 17.5 | 17.5 | 0–30 | — | 0.5 s | — | 10 explosion (tir) ⚠️ |
| `chaos_marine_2` | **50** | 50 | 17.5 | 17.5 | 4–15 | 0–3 | 1.5 s | 1 s | 10 explosion (tir), 10 mob_attack (càc) ⚠️ |
| `chaos_marine_3` | **50** | 50 | 17.5 | — | — | 0–3 | — | 1 s | 25 mob_attack (càc) ⚠️ |
| `chaos_raptor` | **50** | 50 | 10 | 10 | 4–15 | 0–3 | 1.5 s | 1 s | 10 explosion (tir), 10 mob_attack (càc) |
| `tyranid_warrior` | **50** | — | 9 | — | — | — | — | — | — |
| `plaguetoad` | **40** | — | 8 | — | — | — | — | — | — |
| `cultist_1` | **20** | 20 (défaut zombie) | 4.5 | 4.5 | 0–25 | — | 1.5 s | — | 5 explosion (tir) ⚠️ |
| `cultist_2` | **20** | 20 | 4.5 | 4.5 | 5–10 | 0–3 | 1 s | 1 s | 5 explosion (tir), 5 mob_attack (càc) ⚠️ |
| `cultist_3` | **20** | 20 | 4.5 | 4.5 | 4–6 | 0–2 | 2 s | 2 s | 6 mob_attack (tir), 3 mob_attack (càc) ⚠️ |
| `cultist_4` | **20** | 20 | 4.5 | — | — | 0–3 | — | 2 s | 3 mob_attack (càc) ⚠️ |
Les créatures sans profil d'attaque (`ProfilAttaque.AUCUN`) ne frappent pas : elles sont décoratives ou purement défensives.

⚠️ marque une créature dont la valeur du port **diffère du chiffre de la source** : ce sont les médianes assumées (la source donnait des fourchettes, le port a pris le milieu) ou un écart à trancher. La colonne « Source (dégâts) » cite le rapport de mécaniques, qui donne un chiffre par attaque là où le port a parfois enregistré une fourchette.

## 4 · Dégâts de terrain : ce que ça donne vraiment

### Ce que le harnais a MESURÉ en jeu

- `E-pv-source` (PASS) : 18/18 PV conformes à la source
- `E-profils-source` (PASS) : 16/16 profils conformes à la source
- `E-butin-source` (PASS) : 24/24 tables chargées et productives
- `B-melta-pierre` (PASS) : pierre détruite en 1 coup(s) — attendu 1 · cumul=0.00 seuil=26.00 rayon=0.60 puissance=110.0
- `B-bolter-pierre-1coup` (PASS) : pierre intacte après 1 coup de bolter (attendu : intacte) · cumul=8.15 seuil=26.00 rayon=0.80 puissance=16.0
- `B-bolter-pierre-plusieurs-coups` (PASS) : pierre détruite en 4 coup(s) de bolter — attendu 3 à 5 (accumulation) · cumul=0.00 seuil=26.00 rayon=0.80 puissance=16.0
- `B-laser-perce-pierre` (PASS) : le laser DOIT finir par percer la pierre (décision du 16/10/2025) : percée en 4 coups · cumul=0.00 seuil=26.00 rayon=0.35 puissance=15.0
- `B-laser-terre` (PASS) : terre détruite par le laser en 2 coup(s) (attendu : détruite) · cumul=0.00 seuil=13.00 rayon=0.35 puissance=15.0
- `B-bedrock-protegee` (PASS) : bedrock intacte après 8 coups de melta (attendu : intacte)
- `B-derriere-un-mur` (PASS) : bloc caché intact (attendu : intact) — la ligne de vue est respectée
- `B-mode-cosmetic` (PASS) : pierre intacte en mode cosmétique (attendu : intacte)
- `B-mode-off` (PASS) : pierre intacte en mode off (attendu : intacte)
**Coups pour casser un bloc de pierre** (mesuré, même ordre que la table des armes) :

| Arme | Coups sur la pierre |
|---|---|
| Boltgun | **4** |
| Bolt Pistol | **11** |
| Heavy Bolter | **3** |
| Stalker Bolt Rifle | **2** |
| Las Gun | **4** |
| Laz Pistol | **20** |
| Plasma Pistol | **2** |
| Plasma Incinerator | **1** |
| Shotgun | >20 |
| Flamethrower | >20 |
| Melta Gun | **1** |
| Hunter Killer Missile | **1** |
| Chainsword | >20 |
| Powersword | **2** |
| Power Hammer | **1** |

Les deux lasers finissent par percer : **4** coups pour le fusil laser, **20** pour le pistolet laser. Le harnais vérifie aussi que le laser **allume le bois**.

**Coups pour tuer un joueur** — l'armure est lue sur les vrais objets vanilla :

*Joueur en fer complet (armure 15)* :

| Arme | Coups |
|---|---|
| Boltgun | 2 |
| Bolt Pistol | 6 |
| Heavy Bolter | 3 |
| Stalker Bolt Rifle | 1 |
| Las Gun | 8 |
| Laz Pistol | 15 |
| Plasma Pistol | 4 |
| Plasma Incinerator | 2 |
| Shotgun | 2 |
| Flamethrower | 6 |
| Melta Gun | 1 |
| Hunter Killer Missile | 1 |
| Chainsword | 4 |
| Powersword | 2 |
| Power Hammer | 1 |


### Les matériaux, et leur résistance

| Matériau | Résistance |
|---|---|
| foliage | 0.5 |
| protected | 1 |
| glass | 1.5 |
| dirt_sand_gravel | 3 |
| wood | 7 |
| stone | 16 |
| deepslate | 24 |
| brick | 26 |
| iron_block | 48 |
| obsidian | 120 |

Seuils **mesurés** : **26** pour la pierre, **13** pour la terre. La résistance seule ne suffit pas à prédire un nombre de coups — la cadence et la retombée s'en mêlent.

## 5 · Ce qui reste à juger — et qui t'appartient

1. **Les fourchettes de la source sont devenues des médianes.** Les cultistes frappaient 3 à 6, les marines 10 à 25 : le port a pris le milieu (4,5 et 17,5). Si tu veux le maximum, c'est une valeur par créature, dans son `baseAttributes`.
2. **La vitesse de percement de la pierre** (colonne « impacts pour casser » du tableau des armes) : c'est le réglage qui décide si le décor tient ou s'effondre. Aucune valeur n'est juste dans l'absolu.
3. **Le laser ne perce pas la pierre du tout** (mesuré : intacte après 6 tirs). Sa cadence est plus rapide que la retombée de l'accumulation : le cumul n'atteint jamais le seuil. Est-ce voulu pour une arme de tir rapide, ou faut-il qu'elle finisse par entamer la pierre ?
4. **Les PV de la source sont-ils la cible ?** Le Prince Démon en a 1 000. Calibrer les armes et les armures autour de cette décision change tout le reste.

