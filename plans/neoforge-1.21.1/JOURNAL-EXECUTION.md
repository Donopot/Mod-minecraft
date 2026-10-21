# Journal d'exécution — migration 26.3 → 1.21.1

> Données mesurées, pas d'estimation. Mis à jour au fil des phases.

> **Revue du code après `a807882` : P1 non validée.** Les résultats ci-dessous
> décrivent l'exécution historique des scripts initiaux. Leur vérificateur acceptait
> un faux format v3 ; la géométrie et les types NBT n'étaient pas préservés et le
> remappage pouvait supprimer les machines VoidDynasty. Voir les corrections et la
> procédure de reprise dans [P1-REVUE-EXPORTS.md](P1-REVUE-EXPORTS.md). Ne pas coller
> les exports remappés existants. Le pack tiers démarré ne valide pas P2.

## 2026-10-21 — Carte

### Extraction des zones
- Commande : `python3 scripts/world-migration/extract_zones.py <monde> annexe-zones-export.csv <sortie>`
- Résultat : **173 zones traitées → 159 `.schem` (24 Mo)**, **14 zones vides** (aucun contenu détecté), **0 erreur**
- Emplacement HP Mini : `~/minecraft-server/migration-schematics/`
- Durée : ~15 min (le plus gros, `zone_001` : 576×320×368 = 67 829 760 blocs, palette 112, 979 block entities, 10 s)
- Validation : `verify_schem.py` **PASS** sur `zone_001`, `zone_002`, `zone_010`, `zone_100`
  (Version 3, `BlockData` == Width×Height×Length, block entities présentes)

### Format source constaté (Minecraft 26.3)
- `DataVersion` du monde : **5023**
- `block_states.palette` = **liste de strings** (`"minecraft:tuff_bricks"`), plus un compound `{Name,Properties}`
- `data` = longs packés, bpe = `max(4, ceil(log2(len(palette))))`, ordre YZX
- Block entities : `block_entities` au niveau du chunk, coordonnées absolues

### Blocs incompatibles 1.21.1 (mesure sur 3 zones, 14 909 440 blocs)
| Zone | États inconnus | Blocs concernés | Détail |
|---|---|---|---|
| `zone_002` | 21 | **1 459 601 (18,56 %)** | `sulfur` 735 821, `cinnabar` 720 598, `sulfur_spike` 2 887, `potent_sulfur` 59 |
| `zone_010` | 2 | 146 (0,00 %) | `bush` 112, `iron_chain` 34 |
| `zone_100` | 1 | 9 (0,00 %) | `iron_chain` 9 |

Conclusion : les blocs post-1.21.1 sont **du terrain** (biomes à soufre/cinnabar de 26.x), pas des
constructions. Les constructions n'utilisent quasiment que des blocs 1.21.1 valides.

### Traitement retenu
1. `scripts/world-migration/remap_schem.py --version 1.21.1` : neutralise les états inconnus
   - substitutions : `iron_chain → chain`, `bush → short_grass`, `potent_sulfur → sulfur`
   - le reste → `minecraft:air`
   - les block entities dont le bloc n'existe plus sont supprimées
   - sortie : `~/minecraft-server/migration-schematics-1211/`
2. Collage **obligatoirement avec `-a`** (ignorer l'air) pour que le terrain 1.21.1 reste intact :
   ```
   //schem load zone_001
   //paste -a
   ```
   Sans `-a`, les blocs neutralisés en air creuseraient des trous dans le terrain neuf.

## 2026-10-21 — Serveur NeoForge 1.21.1

- Conteneur `mc-neoforge-1211` (image `itzg/minecraft-server`), port **25566**,
  bind `/home/donopot/minecraft-server/neoforge-1211`, `MEMORY=2G`, `ONLINE_MODE=TRUE`
- Version confirmée : **NeoForge 21.1.264**, FML 4.0.45, MC 1.21.1
- Démarrage : `Done (31.073s)` puis `Done (23.702s)` après ajout des mods — **0 erreur, 0 crash**
- Mods chargés (9) :

| Mod | Version |
|---|---|
| geckolib | 4.9.3 |
| create | 6.0.10 (`+mc1.21.1`) — 157 recettes injectées |
| thewarhammermod | 1.6.0 |
| waystones | 21.1.46 (+ Balm 21.0.66) |
| openpartiesandclaims | 0.33.0 |
| worldedit | 7.3.8 |
| flywheel | 1.0.7-48 |

- `ponderjs-neoforge-1.21.1-2.4.0.jar` **neutralisé** (déplacé dans `~/minecraft-server/mods-disabled/`) :
  ce n'est pas le mod `ponder` attendu par Create — Create 6.0.10 charge néanmoins sans lui (dépendance
  embarquée), vérifié en logs.
- RAM hôte après démarrage : 5,6 / 7,1 Gi utilisés (le serveur Fabric `mc-263` tourne en parallèle).

## Reste à faire
- P2 (squelette du mod NeoForge) : `voiddynasty-neoforge/` — le commit P0 d'un autre agent
  (`34ed744` : contrat NBT, `ContratIdsTest`, `verify_contract.py`) est déjà sur la branche.
- Test de collage réel sur le serveur 1.21.1 (monde neuf) : `zone_001`, `zone_002`, `zone_100`,
  puis contrôle visuel + inventaires des `super_chest`/`super_container`.
