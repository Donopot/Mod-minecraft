# État du 18 octobre 2026 — reprise alpha 21

La branche `codex/onepiece-mera-camera` poursuit les finitions après le HUD alpha 20. [Mera et caméra](MERA-CAMERA-ALPHA21.md) : flammes atténuées dans les trois vues, dégagement des tirs Hiken/Higan/Entei et protection pendant la synchronisation initiale du profil. Compilation 327 sources, classes serveur identiques à l'alpha 20, 59 contrôles du socle, scène de 51+24 captures et cinq planches inspectées. Les arrêts des contrôles et reprises sont détaillés dans le rapport ; aucune recette exhaustive des 40 fruits ni acceptation artistique humaine n'est annoncée. Suite : ailes du Phoenix, puis détails des compagnons de Soru. Les installations habituelles restent inchangées.

---

# État du 17 octobre 2026 — sources centralisées, alpha.19 en recette

Le dépôt demandé par l’utilisateur est https://github.com/Donopot/Mod-minecraft. Il conserve VOID DYNASTY et son bridge existants et ajoute One Piece ainsi que la suite de logistique et l’assistant Windows. Les ressources binaires privées restent locales ; leur manifeste et leur importeur figurent dans le dossier One Piece du dépôt. Les paragraphes ci-dessous conservent l’historique des versions précédentes.

Le candidat alpha.19 contient les 40 fruits et 213 techniques, 53 profils reliés aux assets d’origine, 60 fichiers originaux vérifiés et 8 adaptations de pistes. La recette graphique reste ouverte : départ des tirs et propulsion de Mera, ailes du Phoenix, puis inspection de la dernière reprise du lot 07. Voir [la validation actuelle](ASSETS-ALPHA19-VALIDATION.md). Aucune nouvelle installation normale ni acceptation artistique humaine n’est annoncée.

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

Session graphique groupée à deux clients :105 captures acteur et 105 observateur, 46 phases PAL évaluées par rôle (42 distinctes et deux gestes de suivi),56 contrôles de rendu/déplacement/expiration,10 planches inspectées. Acceptation artistique humaine en attente. Adaptations et limites : [bilan](B04-VALIDATION-2026-10-16.md).

Tester-Lot-04 propose les cinq fruits ; Tester-OnePiece et Tester-Terrain en proposent 23. Profil manuel privé neuf `run-6652adc1f52244db821763ac00839b54`, non lancé à la livraison. Anciens profils/lanceurs conservés. Toute mutation de blocs reste définitive ; les contrôles et invocations restent temporaires. Aucun mod/monde habituel ni VOID modifié.

---

Archive des étapes précédentes :

# Livraison actuelle — lot03 alpha.12

Mochi6, Mori4, Kira5, Supa4 et Riki5 :24techniques corrigées,48gestes finis distincts et24décisions terrain explicites. Les18fruits traités représentent95techniques, plus Amaru ; les40fruits ne sont pas déclarés terminés. Voir [le bilan du lot03](B03-VALIDATION-2026-10-16.md) et [la politique terrain](TERRAIN-POLICY.md).

**2102 contrôles serveur couverts PASS par des recettes complémentaires sur un code de jeu identique**. Le paquet final est `eb8a6258e66ea15701e1f86892a8be605eb37a2338383232b273f3fb707ed13e` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,418B03,307historiques,31persistance. Loot, redémarrage et migration du journal ancien inchangé PASS. Mécaniques à160ticks/s, reprise Goro à80, historiques à20. La passe160 garde son échec Goro isolé ; seule sa recette Goro est remplacée dans cette couverture par les142PASS de la reprise.325contrôles distincts ont aussi été exécutés directement sur le paquet final. Les deux seules classes modifiées après la passe complète concernent le diagnostic graphique ; tous les autres octets du JAR sont identiques. Ce résultat ne prouve pas le ressenti ou les performances à20TPS.

Session groupée initiale à deux clients, puis reprises ciblées sur le JAR final : couverture retenue120captures acteur+120observateur,50évaluations de poses par rôle (48phases distinctes et le Pistol de suivi), revêtements/expiration, déchargement et retour d’observateur, dégâts natifs DiamondBody. Dix planches inspectées par l’agent. Ressources et classes de rendu identiques octet par octet au JAR observé. Acceptation artistique humaine en attente.

Tester-Lot-03 ouvre les cinq nouveaux fruits ; Tester-OnePiece et Tester-Terrain offrent18fruits. Profil privé neuf `run-12c860c742964c8a8e4288ab573e3e08`, non lancé à la livraison ; anciens profils et lanceurs sauvegardés. Aucun mod/monde habituel ni VOID modifié. Le terrain reste définitif ; les effets sur entités gardent leurs durées.

---

Historique conservé ; les statuts anciens ne définissent pas la livraison actuelle.

# Livraison actuelle — alpha.11, terrain définitif et lot02

Les71techniques des13fruits traités et Amaru ont une décision terrain explicite. Les météores détruisent les toits au vrai contact ; les cratères, glace, grès, magma, mochi et feuilles créés par les pouvoirs sont définitifs. Aucun ancien état de bloc n’est réappliqué. Les champs et états sur entités restent bornés. [Politique](TERRAIN-POLICY.md), [migration](TERRAIN-DEFINITIF-alpha11.md).

**1684 contrôles serveur PASS** sur le JAR final `c942853ea3d445a0c0920718f331b6183f5c47ba8f8959e206199a9146bdbc1f` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,307historiques,31persistance. Loot, redémarrage, migration du journal ancien inchangé et préservation des modifications joueur PASS. Mécaniques à80ticks/s, historiques à20 ; cela ne prouve pas les performances ou le ressenti.

Les graphismes B02 ont été observés dans une session groupée à deux clients, puis Zushi/Ope repris après correction d’une texture manquante. Les ressources et classes client sont identiques octet par octet à alpha.10 vérifiée graphiquement. Aucune nouvelle session graphique alpha.11 ni acceptation artistique humaine revendiquée. [Bilan B02](B02-VALIDATION-2026-10-16.md).

Tester-OnePiece et Tester-Terrain offrent13fruits ; Tester-Lot-02 offre Gura/Bomu/Zushi/Nikyu/Ope. Profil neuf privé `run-730eb5face854de2812a08fd9c530132`, non lancé à la livraison. Anciens lanceurs/profils conservés. Aucun mod/monde habituel ni VOID modifié. Les40fruits ne sont pas déclarés terminés.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Priorité active — terrain des lots précédents

Mettre à jour les45techniques des huit fruits précédents et Amaru avec [une politique terrain explicite](TERRAIN-POLICY.md), valider, puis reprendre B02. B02 est en développement et a encore neuf échecs sur son premier passage. Les preuves historiques ci-dessous concernent leurs JAR figés.

---

# Lot01 alpha.8 — livraison courante

Les cinq fruits Hie/Pika/Moku/Suna/Yami et leurs26techniques sont implémentés, serveur-testés et observés dans une session groupée à deux clients puis reprises ciblées. [Implémentation](B01-IMPLEMENTATION-2026-10-16.md), [validation et limites](B01-VALIDATION-2026-10-16.md), [essai manuel](TESTER-LOT-01.md). 711 contrôles PASS sur le JAR final (345B01+59socle+307historiques), loot/redémarrage PASS ; reprise client ciblée sur code client identique. Tester-Lot-01 et alias Tester-OnePiece utilisent un profil neuf indépendant. Aucun mod/monde habituel ni VOID changé. L’avis artistique humain reste attendu ; les40fruits ne sont pas déclarés terminés.

Les lanceurs Goro alpha.7, Mera alpha.2 et Magu alpha.5 sont conservés. Les entrées ci-dessous sont historiques et ne définissent plus la priorité active.

---

# Goro / Amaru alpha.7 — livraison courante

Goro a été terminé en priorité avant les fruits suivants : sept techniques conservées et forme Amaru intégrée. **674 contrôles serveur PASS**, dont142Goro/Amaru ; deux clients13contrôles chacun, 24captures/17images distinctes inspectées. SHA256 `57264a351417d5962051d62ce5682cf2524b0dc03df12b043fca7829481987a6`. [Rapport courant](GORO-AMARU-2026-10-16.md).

Tester-Goro et alias Tester-OnePiece ciblent le profil neuf et non lancé `run-1f29a788ca1346d88867d16c2dfe2a9c`. V : charge/retour Amaru ; double saut : vol. Mera/Magu restent figés. Trois JAR réservés et vérifiés dans `.runtime/onepiece/reference-jars/manifest.local.json` depuis la racine du workspace. Aucun mod/monde habituel ni VOID remplacé.

Le lot B01 est différé pendant cette livraison ; conserver sa méthode de cinq fruits avant tests visuels lors de sa reprise. L’essai artistique humain de Goro reste à recueillir, distinct de l’implémentation et des preuves automatiques. Les états ci-dessous sont historiques.

---

# Méthode conservée — lot B01 de cinq fruits

Nouvelle instruction utilisateur : développer **cinq fruits avant les tests visuels**, pour réduire les relances. Prochain lot : **Hie Hie, Pika Pika, Moku Moku, Suna Suna, Yami Yami**, 26 techniques. Audits et réparations puis tests serveur ; session visuelle groupée seulement après les cinq. [Suivi du lot](BATCH-01.md).

État : audit de référence commencé, défauts source/mécaniques/rendus consignés. **Aucune nouvelle implémentation ni validation B01 n'est encore revendiquée.** Dernières preuves livrées : Goro alpha.6, ci-dessous ; profils Mera/Magu/Goro conservés. La recherche Amaru est résolue : référence DaibutsuModel + ENERGY trouvée dans Mine Mine no Mi0.11.6, adaptation encore à faire ; voir AMARU-RESOURCE-AUDIT-2026-10-16.md. AGENTS.md réécrit avec la règle active, ancien historique archivé dans .runtime/onepiece/AGENTS-before-batches-20261016.md.

---

# État courant — Goro Goro alpha.6

L'utilisateur a demandé le prochain fruit après Magu puis repris le chantier. [Rapport courant](GORO-GORO-2026-10-16.md), [contrat](fruits/GORO_GORO.md), SHA256 `b4737e646a3cbd3d1a10501299136530ab3621b1d35b304389c803b6b4463dd5`. Sept routes, quatre charges, deux blinks, choc frontal, chaîne électrique, orage borné et sphère Raigo ; onze pistes PAL distinctes. Les anciens contrats avaient confondu catalogue et comportement source. Adaptations chiffrées documentées ; Amaru absent de cette alpha.6. Une référence géométrique et électrique a ensuite été retrouvée dans Mine Mine no Mi0.11.6 ; pas encore intégrée.

**104 Goro + 74 Magu + 92 Mera + 59 socle + 307 historiques PASS** ; loot/redémarrage PASS. Serveur `run-9a6d0912c06843aeb98f30dbd714c870`. Graphique `run-e9ee337d990c4ee9833cde8b1107acbd` : 49+49 captures, 17 distinctes inspectées, 11 PAL par client, blink 32 et trois frappes natives Mamaragan avec délai10/dégâts. Ressources636JSON/0erreur/4ancienswarnings, audit11pistes+8Magu inchangées, probe17. Tous processus automatiques arrêtés.

Tester-Goro et alias Tester-OnePiece ciblent `run-f8cc0aaf09134e6996a00064a06fb7c5`, neuf et non lancé. Mera/Magu restent figés séparément. Aucun mod/sauvegarde habituel ni VOID remplacé. Manifeste `../build/verification-summary.local.json`. Prochain travail : essai humain et adaptation de la forme Amaru (référence retrouvée), sans déclarer Goro entièrement achevé. Les états ci-dessous sont historiques.

Commandes : `python onepiece/tools/build_local.py` ; `python onepiece/tools/run_local_tests.py --run --accept-eula --goro --magu --mera --historical` ; `python onepiece/tools/audit_goro_animations.py` ; `python onepiece/tools/launch_visual_lab.py --accept-eula --scenario goro --capture --run`.

---

# État courant — Magu alpha.5, finition visuelle

L'utilisateur a apprécié alpha.4 puis demandé une finition. [Rapport courant](MAGU-POLISH-2026-10-16.md), SHA256 `b991ff05a4b3dca6fc92dfb4d0c1e4788c2e8302cff947a1c645c953546fc35a`. Huit pistes avec courbes lissées, anticipation, suivi du corps et récupération ; Kagero en vagues courbes, Volcanic en jets effilés, préparation Ryusei continue. Charge suit la visée. Équilibrage inchangé. Traînée 864→84 quads par projectile ; aucun benchmark FPS annoncé.

JAR final : 74 Magu + 92 Mera + 59 socle + 307 historiques PASS ; 8 contrôles PAL sur chacun des deux clients ; 55 + 40 captures, 15 ouvertes ; ressources 625 JSON / 0 erreur / 4 anciens warnings ; probe 17 PASS. Serveur `run-7ea32bb936ba46989f35d65ca80c29da`, graphique `run-e6f94cfc104643cdbab9a6a0585abc8c`, tous enfants automatiques arrêtés. Manifeste `../build/verification-summary.local.json`.

Tester-Magu pointe vers `run-cc3436916e724597b1221682609a1bbc`, neuf et non lancé. Fermer l'ancienne fenêtre et relancer le lanceur pour charger alpha.5. Aucun changement des mods/saves habituels ou VOID ; Tester-Mera reste figé. Retour humain sur cette finition encore attendu ; variantes documentées et acceptation complète restent ouvertes. Les décomptes ci-dessous sont historiques.

---

# État courant — correction animation Magu alpha.4

L'utilisateur a constaté des gestes semblables pour les techniques2/3 et4/5. [Rapport courant](MAGU-ANIMATIONS-2026-10-16.md), SHA256 `5aa804501710b0ea91ba8cf7e2c0372d373a189f059a5904d1e7635f732a9a5a`. Huit pistes PAL authorées et distinctes ; lâcher corrigé (retirer ACTIVE avant le geste), annulation séparée du lâcher, phases dash/finition, Volcanic et barrage de2secondes. FX Kagero sillons bas, Volcanic couronne verticale, préparations poing/chien/ciel séparées. Équilibrage inchangé.

Preuves finales74Magu+92Mera+59socle+307historiques PASS ; client17 PASS ;8gestes évalués chez chacun des deux clients (UUID/temps/pose) ; audit8pistes distinctes ;625JSON/83paires/0erreur/4ancienswarnings. Serveur `run-7453dcc7f5954a589348388cc81fab05`, graphique `run-b20aff3d24724b648dbe1897e065a7e4` :45+30captures,12réellement inspectées. Tous les enfants automatiques arrêtés.

Tester-Magu pointe désormais vers `run-08a899ea88f343f0887b512d62505deb`, neuf et non lancé. L'ancienne fenêtre doit être fermée et le lanceur relancé pour charger alpha.4. Aucun JAR normal ni monde personnel changé, pas de progression importée. Tester-Mera reste figé séparément. Manifeste `../build/verification-summary.local.json`.

Commandes habituelles ci-dessous, plus `python onepiece/tools/audit_magu_animations.py`. Le runner graphique exige maintenant8contrôles PAL par client,45images acteur/30observateur ; ne pas assouplir si une pose reste neutre. Captures à24/30/49/53 pour voir les gestes courts. Les noms seuls ne valent pas acceptation. Les variantes source et l'art humain restent à compléter ;0/40acceptés.

---

## Handoff alpha.3 archivé

# Reprise technique — Magu Magu, 16 octobre 2026

Priorité courante : Magu, demandé explicitement après l'alpha Mera. [Rapport Magu](MAGU-MAGU-2026-10-16.md), [contrat](fruits/MAGU_MAGU.md). Alpha.3,258sources, SHA256 `f7f4b52e3d3c871ef6854b688554a399340d0bbe9c7d4f53caa93ee5ee536efb`. Résultats finaux66Magu+92Mera+59socle+307historiques PASS ; loot/redémarrage PASS ; client17 PASS ; assets0erreur/4ancienswarnings. Manifeste courant `../build/verification-summary.local.json`.

Serveur `run-1a0a9e6fc6124484beff5f86b9008c2a`, graphique `run-7f828379e6ae414d9d3fe6267969e34b` :35+20captures,7images réellement inspectées ; dash12.00blocs et éruption encore visible après15ticks. Tous les processus automatiques arrêtés. Profil manuel neuf `run-631674ccce1d46fdaacefc5f595c66b7`, préparé sans lancement, via `../Tester-Magu.cmd` et alias Tester-OnePiece. R charger/relâcher les deux tirs, B suivante, F5 caméra. Mera garde son lanceur figé séparé, son rapport et son ancien hash. Aucun mod/monde personnel modifié, aucune progression importée. EULA acceptée explicitement pour ces tests depuis le15octobre.

Commandes depuis codexmod :

```powershell
python onepiece/tools/build_local.py
python onepiece/tools/run_local_tests.py --run --accept-eula --magu --mera --historical
python onepiece/tools/probe_client.py
python onepiece/tools/audit_packed_assets.py
python onepiece/tools/launch_visual_lab.py --accept-eula --scenario magu --capture --run
python onepiece/tools/launch_visual_lab.py --accept-eula --scenario magu
```

Les laboratoires utilisent le runtime installé exact, leur monde jetable, loopback, authentification factice et aucune entrée bureau. Ne stopper que leurs enfants. pause-when-empty-seconds=0 est indispensable aux recettes sans joueur réel >60secondes. Tests Magu exigent66identifiants distincts et un END exact ; phase finale220vrais ticks. Les fixtures doivent avoir des victimes fraîches, alliés vivants exposés à l'impact, et un Nether dégagé avec cibles aux mêmes coordonnées.

MaguTechniques tient les cinq routes. MeteorVelocity doit adopter la déviation Nikyu ; origines distinctes pour retrait/dimension. MagmaBurn lie UUID ET identité de cible/niveau, reprend son témoin après extinction d'un feu extérieur, dégâts attribués et compteur vanilla2ticks. Pools stockent ServerLevel/owner, excluent BlockEntity et isTrackedAny, restituent au stop/expiration et désactivent le hotFloor natif de leurs blocs. La charge et les maillages Magu contenant la caméra sont omis seulement en première personne proche ; maintenir le rendu observateur.

Référence unique : archive source Fabric utilisateur + MinePiece ver15 fourni inspecté par javap. Données privées via `../../.runtime/onepiece/paths.local.json`, jamais publier. Les headers ClassTweaker sont uniquement pour javac. Reste : essai/art Magu, variante Dai accroupie et dash maintenu, vrais cycles mort/reconnexion et multijoueur. Les adaptations numériques sont documentées, pas de fidélité totale ni de fruit accepté0/40. Ne pas démarrer un autre fruit sans nouvelle demande. Avant installation personnelle : sauvegarder l'état courant et jeux fermés.
