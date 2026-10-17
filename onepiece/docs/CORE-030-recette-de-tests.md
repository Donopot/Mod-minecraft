# CORE-030 — recette de tests

Deux niveaux : **automatisé** (rejouable, sans client) et **manuel** (nécessite un client graphique).
Le rapport de la dernière exécution automatique est dans `docs/CORE-030-rapport-tests.md`.

---

## 1. Tests automatisés (aucun client requis)

```bash
cd /opt/data/workspace/opworldlike-fabric
./gradlew jar
python3 tools/validate_assets.py     # CORE-024 : ressources et clips d'animation
python3 tools/run_tests.py           # CORE-030 : cycle serveur complet + rapport
```

`tools/run_tests.py` enchaîne, sur un serveur de test dédié :

| # | Phase | Ce qui est prouvé |
|---|---|---|
| 1 | Démarrage dédié | serveur prêt, catalogue chargé (`40 fruits, 204 techniques`), 0 erreur |
| 2 | `/dftest all` | catalogue, immersion des 40 fruits, **exécution des 204 techniques**, transformation + échelle, changement de fruit, mort/réincarnation, 2 joueurs, persistance, blocs temporaires, invocations |
| 3 | `/dftest persistwrite` | état écrit sur disque (fruit + forme) |
| 4 | Arrêt | sauvegarde propre |
| 5 | **Redémarrage** | 2ᵉ démarrage, `devilfruit-players.dat` relu |
| 6 | `/dftest persistverify` | l'état a survécu au redémarrage |
| 7 | Loot | un Fruit apparaît dans un coffre de structure |
| 8 | Ressources | `validate_assets.py` : 0 anomalie |

Scopes disponibles : `dftest all | catalogue | player | death | multi | persist | world | persistwrite | persistverify`.

Détail des lignes `[dftest] [PASS|FAIL] <id>` dans `mc-server/logs/latest.log`.

---

## 2. Tests manuels (client graphique)

### 2.0 Prérequis client
- Dossier `donopot-devilfruits-client/` : `opworldlike-1.0.0.jar`, `geckolib-fabric-26.3-5.5.7.jar`, `fabric-api-0.162.0+26.3.jar`
- Lancer Minecraft 26.3 avec Fabric Loader 0.19.5

### 2.1 Keybinds à vérifier (Options → Contrôles)
| Touche | Action | Attendu |
|---|---|---|
| **R** | Lancer la technique sélectionnée | animation + projectile/impact visibles ; message `§b<technique> !` |
| **V** | Basculer la forme hybride (Zoan) | le modèle se superpose au joueur, l'échelle change, la caméra suit |
| **B** | Technique suivante | le nom affiché change |

### 2.2 Immersion (1 joueur)
1. `/devilfruit give item_devil_fruit_gomu_gomu`
2. Manger le fruit (clic droit maintenu)
3. **Attendu** : message `§3§lTu as mangé un Fruit du Démon !`, nausée ~5 s, HUD énergie à 200/200
4. `/devilfruit info` → le fruit est affiché

### 2.3 Rendu par technique (CORE-024)
Pour chaque visuel du tableau (voir `docs/CORE-024-rendu-par-technique.md`), en jeu :

```
/dfvisual <modèle> <texture>
```

- **Attendu** : le modèle apparaît 2 blocs devant le joueur, 10 s, animé en boucle (`idle`), **sans** texture violette/noire « missing texture ».
- Cas à contrôler en priorité (corrigés en CORE-024) :
  - `/dfvisual jabra_wolf_projectile jabra_wolf_projectile` (texture récupérée du JAR ver15)
  - `/dfvisual ghost1 ghost1_1` et `/dfvisual ghost2 clear` (clip `idle` ajouté)

### 2.4 Transformation (Zoan)
1. `/devilfruit give item_devil_fruit_ushi_ushi_giraffe`, manger
2. **V** → forme hybride : échelle ×1,75, caméra relevée, hitbox élargie
3. **V** → retour base : échelle 1,0
4. Roue de la hotbar / F5 : le modèle doit suivre correctement

### 2.5 Deux joueurs
1. Deux comptes simultanés, fruits différents (`..._mera_mera` / `..._hie_hie`)
2. **Attendu** : chacun garde son fruit, les visuels des techniques de A ne s'affichent pas sur B, `/devilfruit info` diffère
3. Un joueur mange un 2ᵉ fruit → **il meurt** (règle One Piece)

### 2.6 Mort et réincarnation
1. Joueur avec un fruit → `/kill` (ou mourir)
2. **Attendu** : le fruit est libéré à la mort (avec `reincarnation-on-death = true`)
3. Repartir en survie et retrouver ce fruit dans un coffre de structure

### 2.7 Redémarrage
1. `/devilfruit give item_devil_fruit_ushi_ushi_giraffe`, manger, **V** (forme active)
2. Arrêter le serveur, le redémarrer
3. Reconnecter → **attendu** : le fruit et la forme sont restaurés

### 2.8 Changement de fruit
1. `/devilfruit clear` → pouvoir retiré, forme annulée
2. Manger un nouveau fruit → **attendu** : nouveau fruit actif, énergie pleine, forme à base

---

## 3. Limites du harnais automatisé (à connaître)

Ces points sont **volontairement hors périmètre automatique** et doivent être validés manuellement :

| Limite | Raison | Contrôle manuel |
|---|---|---|
| Mort **physique** d'un joueur | `FakePlayer` (Fabric) est invulnérable par conception (`isInvulnerableTo → true`) et n'est pas réellement ajouté au monde : sa vie reste à 20 même après `die()`. Le **gestionnaire de mort** (`ServerLivingEntityEvents.AFTER_DEATH` → libération du fruit) est bien déclenché et testé (`F1`, `F2`), mais l'animation de mort et le respawn ne le sont pas. | §2.6 (vrai joueur) |
| Rendu visuel | Aucun client graphique en headless. | §2.3 |
| Keybinds R/V/B | Idem. | §2.1 |
| Ressenti combat / équilibrage | Subjectif. | CORE-025 |
| Immersion souris (clic droit) | Remplacée par un appel direct à `finishUsingItem` (même code). | §2.2 |

Les identifiants `E1`, `F1`… correspondent aux lignes `[dftest] [PASS|FAIL] <id>` du journal serveur.

---

## 4. Grille de recette (à cocher)

| # | Scénario | Automatique | Manuel |
|---|---|---|---|
| 1 | Démarrage dédié sans erreur | `S0`, `S1` | |
| 2 | Catalogue 40 fruits / 204 techniques | `A1`–`A4` | |
| 3 | Immersion des 40 fruits | `B1` | 2.2 |
| 4 | Exécution des 204 techniques | `C1`, `C2` | 2.3 |
| 5 | Transformation + échelle | `D1`–`D3` | 2.4 |
| 6 | Changement de fruit | `E1`, `E2` | 2.8 |
| 7 | Mort + réincarnation | `F1`, `F2` | 2.6 |
| 8 | Deux joueurs | `G1`, `G2` | 2.5 |
| 9 | Persistance (in-process) | `H1` | |
| 10 | Persistance après redémarrage | `J1`, `J2`, `S2` | 2.7 |
| 11 | Blocs temporaires / invocations | `I1`–`I3` | |
| 12 | Loot en coffre de structure | `loot-coffre` | 2.6 |
| 13 | Ressources / clips d'animation | `ressources-CORE-024` | 2.3 |
| 14 | Keybinds R/V/B | — | 2.1 |
