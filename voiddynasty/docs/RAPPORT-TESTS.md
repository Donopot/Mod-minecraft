# Test report — vdyntest

- Date: 2026-10-16 17:25:50
- Server: `/opt/data/workspace/mc-server`
- Jar: `build/libs/voiddynasty-1.0.0.jar` (5720651 bytes)
- Result: **38/38 PASS**, 0 FAIL

| phase | check | status | detail |
|---|---|---|---|
| boot1 | `S0-demarrage` | PASS | server ready=True |
| boot1 | `S1-sans-erreur` | PASS | 0 ERROR/FATAL/Exception |
| all | `R-armes` | PASS | 15 définitions d'armes (attendu 15) |
| all | `R-terrain-declare` | PASS | 0 armes sans dégâts de terrain |
| all | `R-degats-armes` | PASS | 0 armes à dégâts nuls |
| all | `R-entites` | PASS | 15 entités résolvables |
| all | `R-blocs` | PASS | blocs du mod instanciés |
| all | `C-props-posables` | PASS | 21/21 props posés |
| all | `D-animations-armes` | PASS | recul 3 ticks posé/expiré, rechargement posé/expiré, cas limites OK |
| all | `B-melta-pierre` | PASS | pierre détruite en 1 coup(s) — attendu 1 · cumul=0.00 seuil=26.00 rayon=0.60 puissance=110.0 |
| all | `B-bolter-pierre-1coup` | PASS | pierre intacte après 1 coup de bolter (attendu : intacte) · cumul=8.15 seuil=26.00 rayon=0.80 puissance=16.0 |
| all | `B-bolter-pierre-plusieurs-coups` | PASS | pierre détruite en 4 coup(s) de bolter — attendu 3 à 5 (accumulation) · cumul=0.00 seuil=26.00 rayon=0.80 puissance=16.0 |
| all | `B-laser-perce-pierre` | PASS | le laser DOIT finir par percer la pierre (décision du 16/10/2025) : percée en 4 coups · cumul=0.00 seuil=26.00 rayon=0.35 puissance=15.0 |
| all | `B-laser-terre` | PASS | terre détruite par le laser en 2 coup(s) (attendu : détruite) · cumul=0.00 seuil=13.00 rayon=0.35 puissance=15.0 |
| all | `B-bedrock-protegee` | PASS | bedrock intacte après 8 coups de melta (attendu : intacte) |
| all | `B-derriere-un-mur` | PASS | bloc caché intact (attendu : intact) — la ligne de vue est respectée |
| all | `B-mode-cosmetic` | PASS | pierre intacte en mode cosmétique (attendu : intacte) |
| all | `B-mode-off` | PASS | pierre intacte en mode off (attendu : intacte) |
| all | `B-claim-protege` | PASS | pierre intacte sous 4 coups de melta dans un chunk revendiqué (attendu : intacte) |
| all | `B-hors-claim-casse` | PASS | hors chunk revendiqué, la même pierre tombe en 1 coup(s) (attendu 1 — témoin positif : sans lui, le contrôle ci-dessus ne prouve rien) |
| all | `V-lemanruss-pilotable` | PASS | type lemunruss enregistre (gabarit 3,0x1,9), les trois fichiers du rendu presents, les six crochets de conduite declares dans la classe, conducteur nul sans passager, le char ne disparait pas et ne se laisse pas pousser, oeuf d'apparition en place |
| all | `B-armes-pierre` | PASS | coups pour casser la pierre (même ordre que WeaponDefinitions.TOUTES) — bol0=4  bol1=11  bol2=3  bol3=2  las4=4  las5=20  pla6=2  pla7=1  sol8=>20  fla9=>20  mel10=1  exp11=1  mel12=>20  mel13=2  mel14=1 · laser 4 coups · pistolet laser 20 coups · laser allume bien le bois |
| all | `E-cible-joueur` | PASS | coups pour tuer un joueur — FER (armure 15) : bol0=2  bol1=6  bol2=3  bol3=1  las4=8  las5=15  pla6=4  pla7=2  sol8=2  fla9=6  mel10=1  exp11=1  mel12=4  mel13=2  mel14=1 // DIAMANT (armure 20) : bol0=4  bol1=11  bol2=6  bol3=2  las4=16  las5=29  pla6=7  pla7=3  sol8=3  fla9=11  mel10=1  exp11=1  mel12=7  mel13=4  mel14=2 |
| all | `S-apparitions` | PASS | 31/31 créatures apparues |
| all | `E-pv-source` | PASS | 18/18 PV conformes à la source |
| all | `E-profils-source` | PASS | 16/16 profils conformes à la source |
| all | `E-butin-source` | PASS | 24/24 tables chargées et productives |
| all | `F-campagne-zones` | PASS | 59 zones · armoury conforme (6 créatures, compteur 1, niveau 2) · réapparition du niveau 2 = 264 89 55 · déclenchement réel vérifié |
| all | `F-campagne-regles` | PASS | 46 règles (44 d'entrée + 2 « zone nettoyée ») · la cryo-salle conforme à leveldetect.mcfunction ligne 1 |
| all | `F-portes-source` | PASS | 16 mécanismes · door1 conforme (région 284 65 78 → 280 70 78, son, cryoroom7) · ouverture réelle vérifiée (verre retiré) |
| all | `F-campagne-parcours` | PASS | 49/49 règles répondent (position, compteur, entité) comme la source les déclenche |
| all | `F-campagne-tick` | PASS | 2 règles sans position · 1 règle sur entité (cryo2 à 3 blocs) · reset au spawn 380 105 -366 · barre « Reactor Core » max 16 départ 4 · fin téléporte en 610 251 -55 avec le titre de la source · fin RÉELLEMENT déclenchée |
| all | `F-campagne-recompenses` | PASS | 24 objets donnés, tous avec NOTRE objet (19 objets distincts) · armurerie éprouvée : 10 objet(s) posé(s) au sol |
| all | `F-power-armour` | PASS | notre casque → 40 PV · sans la tenue → 20 PV · tête nue → 20 PV (la source regardait un casque de diamant, nos deux tenues comptent — extension déclarée) |
| all | `H-minerai-diamant` | PASS | les deux minerais de céramite exigent une pioche en DIAMANT (au fer : refusé, au diamant : accepté) — témoin vérifié : le minerai de fer vanilla cède au fer |
| all | `H-minerai-ardoise` | PASS | bloc deepslate_ceramite_ore enregistré, mineable à la pioche et exigé au diamant, comme la version pierre (les deux exigences sont alignées) |
| all | `H-minerai-genere` | PASS | filon enregistré (4 modificateurs, chaîne résolue) · lu depuis le jar · présent dans les features du biome plains à l'étape UNDERGROUND_ORES, au même endroit que le minerai de fer (témoin) — le monde de test est plat, donc pas de comptage de blocs possible ici |
| boot2 | `S2-redemarrage` | PASS | 2nd boot ok=True |

## Not automatable headless (manual pass required)

Visual rendering, keybinds, mouse immersion, combat feel, physical death animation.
See the manual checklist shipped with the mod.
