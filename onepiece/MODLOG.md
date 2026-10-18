# Reprise du 18 octobre 2026 — alpha 21

Mera : protection des flammes dans les trois vues, dégagement Hiken/Higan/Entei et synchronisation initiale des profils. Classes serveur identiques à l'alpha 20 ; 59 contrôles du socle et 51+24 captures avec reprise du contrôle de caméra, cinq planches inspectées. Voir le rapport MERA-CAMERA-ALPHA21.md dans docs pour les résultats et les limites. Prochaine réserve : ailes du Phoenix. Aucun monde ou mod habituel modifié.

---

Mise à jour du 17 octobre — Mera alpha.18 : les sept attaques reprennent les particules animées FLAME/FLAME_SMALL de Fire Propulsion. Traînées sur les tirs, colonne sur Hibashira, souffle sur Kaenryuo, petites flammes sur Hotarubi et enveloppe sur Entei. Émission limitée à96particules par2ticks côté client ; extinction naturelle en15ticks. Dégâts et comportement serveur identiques au paquet natif validé. La scène réelle fournit48captures acteur et24observateur, avec les sept émetteurs présents dans les journaux de chaque rôle ; les deux planches ont été inspectées. Le mesh opaque préexistant d’Entei peut encore traverser la caméra. Acceptation artistique humaine en attente. Preuve : build/mera-flames-alpha18-proof.local.json. Tester-Mera.cmd ouvre un nouveau laboratoire privé alpha.18 ; installation normale inchangée. Revue des assets des40fruits toujours en cours.

# Les 40 fruits sont implémentés et couverts en laboratoire — revue des assets en cours

Les lots07 et08 ajoutent les sept derniers Zoans et39 techniques : le catalogue atteint40fruits/213techniques, plus Amaru. Cela ne constitue pas une acceptation artistique humaine. À la demande de l’utilisateur, la prochaine passe vérifie chaque fruit et chaque asset dans MinePiece ver15 pour remplacer les simplifications lorsqu’un original existe.

La recette native `run-9ffbd06d22d0478787462b1d99e70db4` passe1760 contrôles sur le paquet `bda6b257113be5f9d196822df933af62be93a107f099ef6bec2da5929da74ed4` :59socle,143Mochi,183B08,502B07,535B06,307historiques,31permanence. Le candidat actuel `79ab21c2251900d88117a3e17843b3bd60039ea67a1a29c3ed77dd0e41ed2ffd` conserve exactement toutes les classes serveur de cette recette ; ses sept différences sont client/assets/diagnostic graphique. Ce total n’est pas une nouvelle exécution exhaustive de tous les anciens scénarios des40fruits.

B07 :175+175 captures groupées,47 contrôles de rendu par rôle,72 évaluations PAL par rôle. La recette garde son échec sur6 évaluations répétées manquantes ; la reprise ciblée ajoute18+18 captures,10 contrôles et12 évaluations PAL par rôle, couvrant les78 requises. Seize planches inspectées. Kaido utilise maintenant le modèle complet original (51os/346cubes) au lieu du dragon simplifié refusé par l’utilisateur.

B08 :62+62 captures,28 évaluations PAL par rôle ;16 contrôles acteur et15/16 observateur. La forme du Pachy a expiré pendant une pause de capture avant son échantillon de dimensions. La reprise inchangée de son cycle passe3contrôles et4évaluations PAL par rôle sur6+6images. Six planches inspectées. Toutes les tentatives échouées restent conservées.

`Tester-OnePiece.cmd` et `Tester-Terrain.cmd` proposent40fruits avec sélection numérique. `Tester-Lot-07.cmd`, `Tester-Lot-08.cmd` et `Tester-Kaido.cmd` utilisent un monde neuf non lancé à la préparation. R technique, B suivante, V formes, F5 vue. Changer de fruit reconstruit explicitement l’arène de laboratoire ; les pouvoirs ne restaurent jamais les blocs. Installation habituelle et VOID inchangés.

Les finitions historiques Mochi (pivot), statues Gecko et revêtements de transformation sont codées ; leurs preuves graphiques ciblées restent à compléter pendant la revue visuelle. Les modèles et effets simplifiés encore présents ne sont pas présentés comme les assets originaux. Voir docs/ASSETS-MINEPIECE-40.md, docs/BATCH-07.md, docs/BATCH-08.md et build/b08-alpha17-verification.local.json.

---

# Lot06 vérifié en laboratoire — 17 octobre 2026

Loup, Léopard, Tigre à dents de sabre, Girafe et Batto :31 techniques,62 phases PAL distinctes,5 hybrides et5 formes animales complètes. **33 fruits et174 techniques traités**, plus Amaru. Il reste7 Zoans et39 techniques ; le travail continue jusqu'aux40.

Paquet alpha.15 `4b0d5130ba89165318439b28bc1197c8194ee3d4afb1425b586ed51659be6c1a` :535 contrôles B06,59 du socle et307 historiques PASS sur ce même paquet, soit901 contrôles serveur distincts répartis sur deux recettes. 80ticks/s pour les mécaniques,20 pour les historiques ; ce n'est pas une mesure de performances. Les premières recettes échouées sont conservées avec leurs corrections : cibles chevauchant le mur, anciens tests synchrones, immunité intrinsèque des faux joueurs et horloge de capture de l'observateur.

Session graphique complète acteur+observateur :185 captures par rôle,82 évaluations PAL par rôle,46 contrôles par rôle,10 planches effectivement inspectées. Les dimensions/yeux des formes sont contrôlés sur les deux clients. Trois contrôles sur un vrai joueur vérifient le coup normal, l'esquive/immobilisation du contre et son expiration. Les classes client et toutes les ressources sont identiques au paquet final ; seuls les anciens scénarios et les conditions serveur de coupure du vol ont changé depuis ces captures. Acceptation artistique humaine en attente.

Tester-Lot-05 et Tester-Lot-06 sont disponibles ; Tester-OnePiece et Tester-Terrain proposent33fruits. Profil manuel neuf `run-412cf90e9d7f46eaa636a905a6ae4f30`, jamais lancé à la livraison. Changer de fruit reconstruit explicitement le décor du laboratoire ; les pouvoirs ne restaurent jamais le terrain. Aucun monde/mod habituel ni VOID modifié. Adaptations : docs/BATCH-06.md. Preuves : build/b06-alpha15-verification.local.json.

---

# Lot05 vérifié en laboratoire — 17 octobre 2026

Kage, Soru, Uta, Imu et Gomu : 27 techniques, 54 phases PAL distinctes et finies. Cela porte le travail à **28 fruits et 143 techniques**, plus Amaru. Il reste 12 Zoans ; les 40 fruits ne sont pas déclarés terminés.

Paquet alpha.14 `746f21dccb3e8d951d25a277c5fb1f8f7b103da22bc70e4e499e3a01279e2c4e`. 458 contrôles B05 couverts par plusieurs recettes complémentaires, 59 du socle, 307 historiques et 31 de permanence. Ce total de 855 est une couverture multi-paquets, pas une exécution unique sur le paquet final. Le dernier contrôle natif ciblé couvre directement Gomu/Imu et le socle/cycle de vie des cinq fruits ; la dernière modification ne déplace qu'une cible du décor graphique. Les erreurs des premières recettes et la première revue graphique échouée sont conservées avec leurs corrections dans le manifeste privé.

Captures groupées acteur/observateur : 136+136 au premier passage, 106+106 après corrections, puis 21+21 pour dégager la caméra de face des Gears. Planches effectivement inspectées, poses évaluées et erreurs de ressources contrôlées. Acceptation artistique humaine en attente. Les cratères peuvent rapprocher la caméra troisième personne vanilla. La correction équivalente d'alignement des anciens bras Mochi reste dans la liste de finition.

Destruction et transformation de blocs définitives ; effets sur les entités bornés. Aucun monde/mod habituel ni VOID modifié. Preuves : `build/b05-alpha14-verification.local.json`. Le développement continue avec Loup, Léopard, Tigre à dents de sabre, Girafe et Batto.

---

# Livraison actuelle — lot04 alpha.13

Aro (4), Horo (5), Mero (4), Kiro (4) et Bisu (4) : 21 techniques, 42 phases d'animation distinctes et 21 décisions terrain. Avec les lots précédents,116 techniques sur 23 fruits sont traités, plus Amaru ; les 40 fruits ne sont pas déclarés terminés.

**1180 contrôles serveur couverts PASS par trois recettes complémentaires** :59 du socle, 418 du lot03, 365 du lot04, 307 historiques et 31 de persistance, loot et redémarrage compris. Mécaniques à 80 ticks/s et historiques à 20. Ces résultats ne constituent pas une mesure de performances ou une validation artistique. Paquet `3bfd7f643b45f782962020246a487460b001e46a958513ae66dae1ab1089e227`. La première recette conserve un échec de mur contournable par Kumacy et cinq anciens tests synchrones incompatibles avec les nouvelles charges/prérequis. Seuls ses 418 contrôles PASS du lot03 sont retenus. La recette finale exécute directement les 365 contrôles du lot04 et 59 du socle ; la recette historique corrigée apporte 307 contrôles et 31 de persistance. Les classes de mécanique du lot03 restent identiques ; les écarts sont circonscrits aux ajouts visuels/réseau du lot04, au nom Aro et aux fixtures. La recette historique diffère du paquet final uniquement par la traînée B04 et trois classes de rendu/décor. Aucun résultat échoué n'est effacé. Les anciennes preuves des autres lots restent distinctes.

Session graphique groupée à deux clients :105 captures acteur et 105 observateur, 46 phases PAL évaluées par rôle (42 distinctes et deux gestes de suivi),56 contrôles de rendu/déplacement/expiration,10 planches inspectées. Acceptation artistique humaine en attente. Adaptations et limites : [bilan](docs/B04-VALIDATION-2026-10-16.md).

Tester-Lot-04 propose les cinq fruits ; Tester-OnePiece et Tester-Terrain en proposent 23. Profil manuel privé neuf `run-6652adc1f52244db821763ac00839b54`, non lancé à la livraison. Anciens profils/lanceurs conservés. Toute mutation de blocs reste définitive ; les contrôles et invocations restent temporaires. Aucun mod/monde habituel ni VOID modifié.

---

Archive des étapes précédentes :

# Livraison actuelle — lot03 alpha.12

Mochi6, Mori4, Kira5, Supa4 et Riki5 :24techniques corrigées,48gestes finis distincts et24décisions terrain explicites. Les18fruits traités représentent95techniques, plus Amaru ; les40fruits ne sont pas déclarés terminés. Voir [le bilan du lot03](docs/B03-VALIDATION-2026-10-16.md) et [la politique terrain](docs/TERRAIN-POLICY.md).

**2102 contrôles serveur couverts PASS par des recettes complémentaires sur un code de jeu identique**. Le paquet final est `eb8a6258e66ea15701e1f86892a8be605eb37a2338383232b273f3fb707ed13e` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,418B03,307historiques,31persistance. Loot, redémarrage et migration du journal ancien inchangé PASS. Mécaniques à160ticks/s, reprise Goro à80, historiques à20. La passe160 garde son échec Goro isolé ; seule sa recette Goro est remplacée dans cette couverture par les142PASS de la reprise.325contrôles distincts ont aussi été exécutés directement sur le paquet final. Les deux seules classes modifiées après la passe complète concernent le diagnostic graphique ; tous les autres octets du JAR sont identiques. Ce résultat ne prouve pas le ressenti ou les performances à20TPS.

Session groupée initiale à deux clients, puis reprises ciblées sur le JAR final : couverture retenue120captures acteur+120observateur,50évaluations de poses par rôle (48phases distinctes et le Pistol de suivi), revêtements/expiration, déchargement et retour d’observateur, dégâts natifs DiamondBody. Dix planches inspectées par l’agent. Ressources et classes de rendu identiques octet par octet au JAR observé. Acceptation artistique humaine en attente.

Tester-Lot-03 ouvre les cinq nouveaux fruits ; Tester-OnePiece et Tester-Terrain offrent18fruits. Profil privé neuf `run-12c860c742964c8a8e4288ab573e3e08`, non lancé à la livraison ; anciens profils et lanceurs sauvegardés. Aucun mod/monde habituel ni VOID modifié. Le terrain reste définitif ; les effets sur entités gardent leurs durées.

---

Historique conservé ; les statuts anciens ne définissent pas la livraison actuelle.

# Livraison actuelle — alpha.11, terrain définitif et lot02

Les71techniques des13fruits traités et Amaru ont une décision terrain explicite. Les météores détruisent les toits au vrai contact ; les cratères, glace, grès, magma, mochi et feuilles créés par les pouvoirs sont définitifs. Aucun ancien état de bloc n’est réappliqué. Les champs et états sur entités restent bornés. [Politique](docs/TERRAIN-POLICY.md), [migration](docs/TERRAIN-DEFINITIF-alpha11.md).

**1684 contrôles serveur PASS** sur le JAR final `c942853ea3d445a0c0920718f331b6183f5c47ba8f8959e206199a9146bdbc1f` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,307historiques,31persistance. Loot, redémarrage, migration du journal ancien inchangé et préservation des modifications joueur PASS. Mécaniques à80ticks/s, historiques à20 ; cela ne prouve pas les performances ou le ressenti.

Les graphismes B02 ont été observés dans une session groupée à deux clients, puis Zushi/Ope repris après correction d’une texture manquante. Les ressources et classes client sont identiques octet par octet à alpha.10 vérifiée graphiquement. Aucune nouvelle session graphique alpha.11 ni acceptation artistique humaine revendiquée. [Bilan B02](docs/B02-VALIDATION-2026-10-16.md).

Tester-OnePiece et Tester-Terrain offrent13fruits ; Tester-Lot-02 offre Gura/Bomu/Zushi/Nikyu/Ope. Profil neuf privé `run-730eb5face854de2812a08fd9c530132`, non lancé à la livraison. Anciens lanceurs/profils conservés. Aucun mod/monde habituel ni VOID modifié. Les40fruits ne sont pas déclarés terminés.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Private One Piece repair journal

## 2026-10-15 — source recovery and foundation

User resumed One Piece, requested all 40 fruits/213 catalogued techniques, complete transformations and correct effects/animation. User selected anime/manga references for missing animal forms and Gomu Gears. This replaces the earlier pause. VOID DYNASTY remains untouched.

Source archive SHA256: 4e4a6fe7a8196aeb3b4d7ddb69ae1505a1709e2b43fc8178017e310e373483da. Private resources/libraries reside outside the repository in OS temporary storage; see `.runtime/onepiece/paths.local.json` in the parent workspace. Original MinePiece ver15 remains the mechanics reference, except explicitly requested new canonical forms. No publishing or installation performed.

Offline Java 25 build uses actual installed Minecraft 26.3, Loader 0.19.5, API 0.162.0+26.3, GeckoLib 5.5.7 and source PAL 1.2.8. Compile-only widened class headers must never enter the runtime or delivered archive. Current private alpha 1.0.1-alpha.1 compiles 250 Java sources. Latest tested hash: 2700809398195fda3355a75e6d310f12d71d6d40f24ae1d088ae9447a17389d2.

Implemented, pending broader acceptance: scoped cast context and explicit custom refusal, five catalogue ID corrections, exception energy refund, accepted-and-health/absorption-loss checks before offensive effects/recoil, attributed elemental sources, finally-balanced damage call stack, procedure animation continuation, UUID transformation state and observer tracking, deferred-render pose snapshots, selected transparent-projectile visual profiles, atomic per-world fruit/temporary-block storage. New forms/Gears are not implemented. Existing global save data is not imported automatically into every world.

User explicitly accepted Minecraft EULA for isolated local tests on this date. Labs are loopback-only, fresh disposable worlds; the runner owns and stops only its child processes. No user save, installed JAR or game settings touched.

Initial server launch failed because extracted Fabric API modules lack the umbrella dependency identity. Runner now stages actual installed Fabric API umbrella and lets Loader discover nested modules. First combat run had 14/22 passes: melee fixtures queried before their chunks became entity-ticking. Waiting for chunk activation fixed the fixture. Counter-run `.runtime/onepiece-lab/run-5c11b1ea4b1d4077921f8b597aaf49d5/report.local.json` passed **22/22** on actual server ticks. This proves selected common melee/projectile/cost/refusal paths, not all 213 move semantics or client rendering.

Extended historical run `.runtime/onepiece-lab/run-e0af6f8d7a9b4792a71346499fa94481` currently being evaluated: legacy `all` 293/296, Ope expiry victim missing; persistence write/read and Pika/Yami/Nikyu deferred scenarios pass. Investigate fixtures and real regressions individually; do not replace historical 312/312 report or loosen expected mechanics to make it green.

Next: complete regression analysis, guard and persistence evidence; headless CLIENT Mixin transformation; transformation lifecycle fixes; then actual rendered acceptance and missing form/animation creation. Keep the per-technique and per-fruit CSV matrices honest.

## 2026-10-15 — local server and graphical counter-evidence completed

Final private alpha SHA256 **35e3c14f1c9d815fff2d8d812b13275c5f8bdb7a2a109a75c928b8fcb41faf66**, 252 Java sources. Same artifact passed **59 targeted checks**, **307 distinct legacy checks**, expiry, restart and deterministic structure loot. Server evidence: parent `.runtime/onepiece-lab/run-0fe26ac800f7469d97747a4d0ee0b0d1/report.local.json`. Historical C1 now has prepared arena/targets and per-move cleanup plus explicit ROOM/grab preconditions; **213/213 launches** pass, without treating that as 213 complete semantic validations. Early legacy failures in pursuit/expiry/combo were diagnosed against missing entity-ticking chunks; full private arena force-load fixed those fixtures. Actual bugs were fixed rather than loosening their expectations.

Additional implemented and tested repairs: persisted form timer and idempotent login/respawn reconciliation; +20% speed operation; giant shape returns on expiry/suppression/exhaustion; single maintenance debit; no double-scaling its room requirement; spaced and cancellable Zoan melee combo (real cow loses exactly 12 HP over real ticks); Riki grab consumes the first unique hit; targeted power cleanup preserves foreign armor; front-only pursuit; elemental ownership and fruit+technique overrides; actual ghost traversal and range clipping before collision; Goro chain LOS and confirmed-hit counting. Combat entities are ephemeral rather than reloading without their custom state. Test commands are opt-in only.

The first actual GPU scene exposed the systemic visual defect: **42 SpriteParticles used the terrain atlas**. Shigan displayed green atlas fragments. Changed both Java and generation template to particle-atlas layers; source frame cadence now cycles rather than stretching over lifetime. Before evidence: `run-5211061d5189472aa5dc422e48546511/client/screenshots/02-leopard-technique.png`. Counter-evidence on final artifact: `run-ad5491e5587044b7a43bcc3ae22126d6/client/screenshots/02-leopard-technique.png` — real hybrid pose moves, actual claw sprites appear. A side scene also exposed inherited fireball flames on light/ghost projectiles; shouldBurn now returns false and two server regressions cover this. Source flame FX remain their own particles/geometry.

Final graphical run `run-ad5491e5587044b7a43bcc3ae22126d6` captured nine PNGs; inspected final images 01, 02, 05 and 08. Partial evidence only: Leopard hybrid and Shigan pose/sprites, Pika laser from the side without vanilla flame, Goro Sango electrical sprites on targets. Pika's source flare is still large; Goro retains the source pink/purple palette. No whole fruit, all 19 profiles, first-person hands or second observer client is declared visually accepted. Helpers: 128 standalone assertions; final CLIENT probe: 17 classes, per-avatar/UUID state and 19 named profiles. Resource audit of packed JAR: 616 JSON, 82 visual pairs, zero blocking issues; four warnings on degenerate Abyss UV faces remain. Marco NaNs, missing guard sound and actual Murakumogiri model-bake UV error were corrected with local JSON overrides.

Runtime resolver uses the installed launch manifest's 81 libraries and available SHA1 hashes, rather than mixing cached versions. Graphical captures use Minecraft's API and an opt-in timeline, with no desktop input. Local server white-list/secure-profile defaults had to be set explicitly for the offline loopback identity. No personal launcher account or access token is read. Expected offline account/Realms and Windows Perflib warnings remain recorded; no claim of completely warning-free startup.

Manual test profile prepared only: parent `.runtime/onepiece-lab/run-06468a09a122442c91386c2e995a5e8d`, launched by `Tester-OnePiece.cmd`. All automated server/client processes stopped normally. No live mods/config/saves installed or migrated. Original VOID and companion hashes still match their installed 0.7.0 state. Pending optional user question: which world owns the old global One Piece progression; never auto-import it into every world. Full animal forms and Gomu Gears remain to create, per user's anime/manga choice. Full fruit acceptance remains **0/40**; matrices mark partial evidence only. Resume from `docs/HANDOFF.md` and the French report.


## 2026-10-15 — Fresh test progression confirmed

The user chose to start from zero in a test world. Do not import the legacy global fruit progression into the test profile. Verified the prepared manual lab remains unlaunched, with no repair-world or client saves yet. No existing world was reset; no game was launched for this preference update. This resolves the earlier optional migration question.


## 2026-10-16 — Mera Mera alpha.2, private laboratory

User priority: complete Mera first, then one fruit at a time. Read-only layered agent audits informed implementation. Rechecked provided MinePiece ver15 bytecode; old port contract falsely simplified Kagero as a speed buff, Hiken as melee and Entei as a ring. Dedicated seven routes now implement charge/release, moving fire fist/bullets, phased pillar, swept offensive dash, targeted fireflies, frontal dragon/eruption and overhead charged projectile. Port budgets and documented adaptations remain explicit; no source-exact or anime-art acceptance claim.

Bounded attributed burns, circular height/LOS/ally checks, confirmed damage before effects, no spreading FIRE blocks. Propulsion ownership persisted and revoked, body collision, energy, dimension/spectator/disconnect/clearAll cleanup; S2C state prevents native grounded flight cancellation. Real client displacement19.38555 blocks. Fixed Gecko data extraction, visual gravity, client15tick premature expiry, phase geometry and Kaenryuo animation length. Procedural art still needs refinement.

Final255source JAR SHA256 `6eb13be551faf635624c7c8cacad10c276db2cd75756d9a165e46a35b033f18f`. Server `run-397281e2ae624f1f911405706d37c1ae`:92 Mera+59 foundation+307 historical PASS, loot/restart PASS, no missing cases, owned processes stopped. Graphical `run-95ad0414a1644a23895d6515384a184c`:48 actor+24 observer captures, three live checks PASS, both clients/server stopped. Thirteen final images opened; Hiken emission missed in late final snapshots (profile logged, prior lot shows fist); first-person held-item/artistic acceptance partial. Client probe17 PASS including three VOID classes; assets617JSON/83pairs/0errors/4preexistingAbysswarnings. Fresh manual profile `run-67f86efa4df74b01a4188a15257c4718` prepared unlaunched; both launchers target it. Normal mods/saves unchanged; no progression import. Full acceptance0/40. Report/contract/matrices/handoff/verification manifest updated. Next: human Mera play/visual refinement and real-player lifecycle/multiplayer before next fruit.


## 2026-10-16 — Magu Magu alpha.3

User requested next fruit then resume. Read-only gameplay/visual/test audits against supplied ver15 identified fake instantaneous Kagero, frontal single-shot Ryusei, dimensionless pools, inventory loss, unbounded/unattributed double burns and misleading historical tests. Implemented five explicit routes, charged native fist/dog meshes, dash then phased eruption and twenty ascending/falling meteors. Burn bound to entity identity+level, attributed, limited refresh, water/ice/resistance policy; pools own dimension, protect inventories/temporary structures/allies, restore naturally and on cleanup, zero limit works, native hotFloor duplication removed. Meteor reflection preserves changed trajectory. Inside-mesh/charge occlusion and default-meteor flash before metadata found in real captures; projectiles fully configured before network publication, renderer waits for VISUAL_READY and uses camera proximity independently of late owner packet; no raster asset needed. Source variants/adaptations remain documented, human/art acceptance pending.

Final258source JAR `f7f4b52e3d3c871ef6854b688554a399340d0bbe9c7d4f53caa93ee5ee536efb` :66Magu+92Mera+59foundation+307historical PASS, loot/restart PASS. Server `run-1a0a9e6fc6124484beff5f86b9008c2a`, graphical `run-7f828379e6ae414d9d3fe6267969e34b` :35actor+20observer captures,7final images inspected, dash12.00blocks and maintained eruption PASS. All owned automated processes stopped. Probe17targets PASS, assets617JSON/83pairs/0errors/4preexistingAbysswarnings. Fresh manual profile `run-631674ccce1d46fdaacefc5f595c66b7` unlaunched via Tester-Magu; Tester-OnePiece targets Magu, Tester-Mera keeps alpha2. Original user mods/worlds/VOID untouched. Empty-server pause fixed in laboratory; pool contact fixture now fresh, unlike its reused invulnerable predecessor. Docs/contract/matrices/verification/handoff current. Still0/40 fully accepted; next human Magu trial, source variants/real-player lifecycle/multiplayer remain.


## 2026-10-16 — Magu animation identity, alpha.4

User correctly reported similar gestures for moves2/3,4/5. Read-only audits confirmed shared charges, shoot-before-end replaying360s energy_charge, identical Kagero/Volcanic FX and Ryusei gesture ending before meteor emission. Added8different finite authored PAL bone tracks; explicit charge/release/dash/finish selection, cancellation reset separate from normal release; distinct hand-fist, canine, low wake, vertical volcano and sky-preparation FX. Gameplay budgets unchanged. Actual PAL diagnostics compare expected/current UUID, active state, time advancement and evaluated non-neutral bone transforms on both clients. No screenshot input or new raster assets.

JAR `5aa804501710b0ea91ba8cf7e2c0372d373a189f059a5904d1e7635f732a9a5a`; server `run-7453dcc7f5954a589348388cc81fab05`74Magu+92Mera+59foundation+307historical PASS, loot/restart PASS. Graphical `run-b20aff3d24724b648dbe1897e065a7e4`45actor+30observer captures,12inspected;8/8PAL gestures pass on each client plus dash/FX live checks. Asset audit625JSON/83pairs/0errors/4oldAbysswarnings;8tracks distinct by bone content and bounded duration; transform probe17PASS. All automated children stopped. Tester-Magu now points to unlaunched fresh `run-08a899ea88f343f0887b512d62505deb`. No normal mods/worlds/VOID changed; source variants and artistic acceptance remain pending. Report MAGU-ANIMATIONS-2026-10-16.md, contract/handoff/status/matrix/manifest updated.


## 2026-10-16 — Magu animation polish alpha.5

User approved alpha.4 then requested polish. Eight tracks now bake smooth curves, secondary movement and recovery; impacts retain timing. Charge follows look. Curved low wake with stable inner radius, tapered volcanic jets with smooth expansion, continuous sky preparation and trails reduced 864→84 quads per projectile; no measured FPS claim. Gameplay unchanged. Read-only layered reviews informed timing/radius refinements.

Final JAR `b991ff05a4b3dca6fc92dfb4d0c1e4788c2e8302cff947a1c645c953546fc35a`; server `run-7ea32bb936ba46989f35d65ca80c29da` 74 Magu+92 Mera+59 foundation+307 historical PASS, loot/restart PASS. Graphics `run-e6f94cfc104643cdbab9a6a0585abc8c` 55+40 captures/15 inspected, 8 PAL gestures evaluated on each client, dash/lifetime PASS. Audits 8 tracks, 625 JSON/83 pairs/0 errors/4 old warnings; probe17 PASS. Automated children stopped. Tester-Magu targets unlaunched `run-cc3436916e724597b1221682609a1bbc`. No normal worlds/mods/VOID changed. Report onepiece/docs/MAGU-POLISH-2026-10-16.md; human polish trial pending.


## 2026-10-16 — Goro alpha.6 in progress

User requested next fruit; Goro chosen, then resumed. Three read-only ver15 audits found real charge/blink/shock/storm identities and false old catalogue assumptions. Seven dedicated routes, eleven PAL tracks, electric geometry, safe blinks, storm lifecycle and confirmed attributed hits implemented. First server run 91/91 Goro but foundation chain and historical fixture cleanup failures; fixed invulnerable retry origin and actual dimension-teleported FakePlayer teardown/vars. Added slow, Gomu counter, reflected Raigo and further tests (104 expected). First graphics stopped before launching clients after preflight58/59. Current exact JAR b4737e646a3cbd3d1a10501299136530ab3621b1d35b304389c803b6b4463dd5: server run-9a6d0912c06843aeb98f30dbd714c870 and graphics run-e9ee337d990c4ee9833cde8b1107acbd currently running, NOT yet proof. Prior frozen launchers untouched.


## 2026-10-16 — Goro techniques alpha.6 verified

Seven dedicated routes and eleven finite neutral-ending PAL tracks, charged Vari/ElThor/Sango/Kari, safe blink, announced storm and bounded electric Raigo. Hardened storm ownership/level/lifecycle/LOS/protections and confirmed damage. Invulnerable chain candidates cannot extend path, slow retained, reflected Raigo credits Nikyu, Gomu/Goro electric counters. Old Goro source assumptions corrected; adaptations and absent Amaru model documented, resource question sent. Initial fixture failures led to explicit removal/variable clearing of dimension-inserted FakePlayer, preventing historical loot reservation and monster despawn leaks.

Final JAR `b4737e646a3cbd3d1a10501299136530ab3621b1d35b304389c803b6b4463dd5`; server `run-9a6d0912c06843aeb98f30dbd714c870` 104Goro+74Magu+92Mera+59foundation+307historical PASS, loot/restart PASS. Graphics `run-e9ee337d990c4ee9833cde8b1107acbd`49+49 captures/17 distinct inspected,11 actual PAL UUID/time/pose checks per client, real blink32,3 native storm hits each delay10 and hurt1. Assets636JSON/83pairs/0errors/4oldwarnings;11animation tracks audited,8Magu tracks unchanged;probe17PASS. Automated children stopped. Tester-Goro and alias target fresh unlaunched `run-f8cc0aaf09134e6996a00064a06fb7c5`; Mera/Magu launchers untouched. No normal mods/saves/VOID changed. Full fruit acceptance remains pending (Amaru and human art/play).


## 2026-10-16 — Five-fruit batch workflow adopted

User explicitly requested five fruits before visual tests. Replaced old one-fruit workflow in AGENTS; B01 = Hie/Pika/Moku/Suna/Yami, 26 techniques. Audit/develop and server checks first, one grouped graphical session after all five are ready, targeted retakes on failure. Prior frozen Mera/Magu/Goro profiles and evidence preserved. B01 source/layer audits begun; findings in docs/BATCH-01.md, no new implementation/test claims and no game launched for this workflow update.


## 2026-10-16 — Amaru resource search in user-provided JARs

Read-only inventory and class/model review of Downloads MinePiece ver15 (same f9ecbb... reference) and ompworld1.3.48 (e5cc7523...). MinePiece contains Enel NPC/clothes/drums but no identified giant Amaru form; Java drum roots have no humanoid geometry and source Goro has no transform route. Ompworld has only lang/log_pose assets,391region files, no character geometry/textures or nested JAR; mandatory dependency mineminenomi [0.11.5,0.12). Asked for that dependency JAR path as next candidate, not proof it contains Amaru. No games launched/installed files changed, gameplay reference unchanged, B01 workflow preserved. Report onepiece/docs/AMARU-RESOURCE-AUDIT-2026-10-16.md.

## 2026-10-16 — Volt Amaru reference located

User supplied Mine Mine no Mi1.20.1-0.11.6 (SHA256 ceab4866a28236c5f74fc0245dc8a079ce9284b93eee7865ec8414c487de30cc). Bytecode review plus independent read-only model audit confirms VOLT_AMARU uses shared DaibutsuModel through PlayerMorphRenderer, scale2.5, standing2.4x6.5, player skin and textureless ENERGY overlay. Humanoid animations inherited/adjusted; no dedicated Amaru animation file or body PNG. Resource question resolved. Forge1.20.1 reference requires adaptation to Fabric26.3; not implemented or visually tested. No game launch/install, normal saves/mods/VOID untouched; B01 five-fruit workflow and MinePiece gameplay baseline preserved. Report onepiece/docs/AMARU-RESOURCE-AUDIT-2026-10-16.md.

## 2026-10-16 — Goro priority completed: Amaru alpha.7

User requested finish Goro before B01 and retain reference JARs. Three JARs copied to .runtime/onepiece/reference-jars with verified SHA256. Added privately adapted Daibutsu/Volt Amaru mesh, electric skin+energySwirl, Enel drums, finite transform gesture, UUID-scoped dimensions2.4x6.5/eye5.5/SCALE2.5 and native camera10. Owned native flight, 20tick charge,40 activation,.4/tick maintenance+.6 flying,1200tick duration, no combat bonus and no regen during form. Ownership saved before granting flight and reconciled on login/respawn; full-volume deployment/blink checks, lifecycle cleanup and external attributes/equipment preserved.

Read-only reviews caught regen/charge-scale guards, stale saved flight, and a too-weak pose oracle; corrected with durable ownership and multi-tick UUID-scoped mesh checks. Visual witness ran ahead of actor; added actor clock without relaxing assertions. Transformable tag initially replaced baseline entries at packaging; 59-check preflight rejected it. Rebuilt tag from frozen13 baseline entries plus Goro14 and strengthened F2 to assert Goro remains Logia. Final complete rerun passed on SHA256 57264a351417d5962051d62ce5682cf2524b0dc03df12b043fca7829481987a6.

Final server run-a1d3b558c2cc4c45a875a72887225243:142Goro(104+38)+92Mera+74Magu+59foundation+307historical=674PASS; loot/restartPASS. Two-client run-c97e96d93b04464f98804120676525be:13checks each,12+12captures,17distinct actually inspected, six actual PAL IDs each, native flight/camera10/retracking/disconnect cleanupPASS. Twelve distinct finite Goro tracks audited,8Magu tracks unchanged;637JSON/0errors/4oldwarnings;clientprobe17PASS. All owned children stopped. Tester-Goro/alias target fresh unlaunched run-1f29a788ca1346d88867d16c2dfe2a9c with same hash. Mera/Magu launchers and normal mods/saves/VOID unchanged. Human artistic acceptance pending, no B01 implementation or public publication.
