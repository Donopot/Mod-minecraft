# Revue des 40 fruits — alpha 23, 18 octobre 2026

Les **40 fruits et leurs 213 techniques** ont reçu une nouvelle passe mécanique et graphique. Les défauts confirmés du socle d'animation et de caméra Moku/Yami sont corrigés. Des améliorations artistiques restent ouvertes ; ce rapport ne prétend pas à une reproduction complète de l'anime. Le système de Haki n'est pas modifié.

JAR final : `1.0.1-alpha.23`, SHA256 `defd0d4c9a3cf1f93dc4d13fbb9554f43c6fd84eb336db09995a00824e823706`.

## Corrections

- Le repli de mêlée avec épée sélectionne `sword_to_left` ou `sword_to_right`, réellement livrés. Les anciennes déclarations sans fichier `sword` et `swim` sont retirées ; le chemin de nage conserve le mouvement vanilla. Régression reproduite sur alpha22, trois contrôles de sélection réussis sur alpha23.
- White Blow de Moku et Dark Ball de Yami mesurent leur dégagement depuis la caméra réelle, en tenant compte de leur taille. Ils ne dessinent plus leur grand mesh autour de la lentille. La règle est limitée à ces deux profils ; contrôle numérique de visibilité : 519 assertions réussies, puis reprise graphique ciblée alpha23.
- Catalogue, 426 sélections de base, 213 décisions terrain et les 1 807 assets restent identiques à alpha22. Aucun nouvel asset de référence n'est importé. Le modèle et le squelette du dragon sont conservés.

## Contrôles réalisés

**3 874 contrôles natifs distincts réussis sur le JAR final**, par recettes complémentaires : Mera/Magu/Goro, historiques et permanence à 20 TPS ; les huit lots à 80 TPS ; reprise du lot02 isolé à 20 TPS. Le premier lot02 accéléré signale deux échecs (position après Shambles et compteur d'explosion refusée). Ses traces sont conservées ; la reprise ne prouve pas à elle seule la cause de cet écart. Seule sa suite complète réussie à 20 TPS est retenue. Les 59 contrôles communs sont comptés une seule fois. Les suites couvrent dégâts, attribution, cibles, coûts, délais, contrôles, transformations et politiques terrain selon leurs contrats. Elles utilisent en partie des joueurs de test ; elles ne mesurent pas l'équilibrage d'une campagne de survie.

Douze scènes graphiques alpha22 ont été relancées sur deux clients réels : Mera, Magu, Goro, Amaru et les huit lots. **2326 captures originales** sont regroupées en planches inspectées fruit par fruit. Les scènes couvrent lancement, phase active, impact, fin, poses et cycles de formes ; des instants discrets ne prouvent pas chaque image d'une animation. La reprise Moku/Yami utilise alpha23. La continuité des autres scènes est justifiée par les assets identiques et les modifications de code limitées aux replis génériques et aux deux profils caméra.

Les audits des huit lots, Magu et Goro examinent 408 pistes pour leurs durées, mouvements et distinctions dans leur groupe. Les 1 054 JSON et 95 couples historiques ne présentent aucune erreur bloquante. Quatre avertissements UV hérités concernent `abyss.geo.json`. Le catalogue final n'a ni erreur ni avertissement de ressource manquante.

Les tests de terrain contrôlent notamment les météores à travers un toit, la persistance après redémarrage, le loot et la conservation des modifications du joueur. Destruction et transformation restent **définitives**.

## Bilan fruit par fruit

« PASS » signifie que la recette mécanique du fruit a réussi ; les observations gardent les réserves graphiques visibles. Le CSV fournit une ligne par fruit et le JSON les comptes et identifiants de laboratoires.

| Fruit | Techniques | Suite | Observation |
|---|---:|---|---|
| mera_mera | 7 | mera PASS | Sept attaques et propulsion distinctes, flammes visibles. Entei peut occuper une grande partie du cadre lorsqu'il passe près de l'observateur. |
| magu_magu | 5 | magu PASS | Poing, chien, dash, pluie de météores et éruption distincts. Les anciennes confusions entre techniques ne sont pas observées. |
| goro_goro | 7 | goro PASS | Éclairs, chaîne, nuage d'orage et Raigo visibles. Cycle Amaru observé ; modèle encore adapté depuis Daibutsu, remplacement spécifique restant à faire. |
| hie_hie | 5 | batch01 PASS | Projectiles, pics, bulle et gel visibles. La grande sphère conserve sa transparence. |
| pika_pika | 8 | batch01 PASS | Huit techniques observées, rayons et épée visibles, déplacement contrôlé. |
| yami_yami | 4 | batch01 PASS | Quatre techniques observées. Le disque opaque Dark Ball ne couvre plus la caméra après reprise alpha23 ; ses particules restent denses en FPS, réserve de lisibilité. |
| suna_suna | 5 | batch01 PASS | Croissants, vagues et terrain sableux distincts ; aucune panne observée dans les scènes. |
| moku_moku | 4 | batch01 PASS | Quatre techniques observées. Le grand mesh White Blow ne couvre plus la caméra après reprise alpha23 ; fumée et autres techniques restent visibles sur les deux clients. |
| mori_mori | 4 | batch03 PASS | Lance de bois, branches, racines, cage et mutations végétales visibles ; terrain permanent. |
| gomu_gomu | 4 | batch05 PASS | Pistol, Bazooka et Gatling distincts ; cycle des Gears visible puis retour normal. Les Gears restent des adaptations, sans prétendre aux silhouettes complètes de l'anime. |
| inu_inu_wolf | 8 | batch06 PASS | Huit techniques observées, lames d'air et poursuite visibles. Hybride, quadrupède et retour humain présents sur les deux clients ; forme complète adaptée, nouveau modèle candidat non importé. |
| ushi_ushi_giraffe | 5 | batch06 PASS | Cinq techniques, lames d'air et frappe verticale visibles. Hybride au long cou et girafe complète distincts ; retour humain observé. |
| neko_neko_leopard | 7 | batch06 PASS | Sept techniques, charges et onde de pression distinctes. Hybride et quadrupède visibles puis retour humain ; modèle complet adapté restant à améliorer. |
| batto_batto | 6 | batch06 PASS | Six techniques, tirs et déplacements visibles. Hybride ailé puis petite forme complète présents ; retour humain observé. |
| hito_hito_daibutsu | 5 | batch07 PASS | Cinq techniques, coups et ondes dorées distincts. Hybride et géant complets visibles puis retour humain ; nouveau modèle candidat non importé. |
| ryu_ryu_pachycephalosaurus | 5 | batch08 PASS | Cinq techniques de charge, coup de tête et projectiles visibles ; cratères persistants. Hybride et forme complète adaptés, retour humain observé ; caméra proche dans le cratère. |
| inu_inu_okuchi_no_makami | 7 | batch07 PASS | Sept techniques, glace, arc électrique et protection gelée visibles. Hybride et forme complète distincts puis retour humain ; forme complète adaptée conservée. |
| tori_tori_phoenix | 6 | batch07 PASS | Six techniques, oiseau projectile et tornade de flammes bleues visibles. Hybride et vol complet présents ; queue brun-jaune conservée conformément à la confirmation utilisateur. |
| uo_uo_seiryu | 8 | batch07 PASS | Huit techniques, lames de vent, souffle et ondes visibles. Boro Breath peut brièvement envahir le cadre en FPS : réserve de lisibilité. Hybride, dragon complet, mouvement 3D et retour humain observés ; modèle/squelette alpha22 conservés. |
| ryu_ryu_pteranodon | 3 | batch07 PASS | Trois techniques de charge et de tir distinctes. Hybride ailé et forme complète en vol visibles puis retour humain. |
| ryu_ryu_brachiosaurus | 5 | batch08 PASS | Cinq techniques, roulade, rayon et tirs visibles. Hybride au long cou et grand quadrupède complets présents, puis retour humain. |
| neko_neko_sabertiger | 5 | batch06 PASS | Cinq techniques, charges et doubles lames visibles. Les trois états de forme sont observés ; quadrupède adapté conservé. |
| gura_gura | 6 | batch02 PASS | Fissures et cratères visibles et persistants. Caméra vanilla rapprochée par les parois de certains cratères. |
| horo_horo | 5 | batch04 PASS | Fantômes négatifs et miniatures visibles, détonation et invocation Kumashi observées. Modèles actuels conservés. |
| nikyu_nikyu | 5 | batch02 PASS | Pattes, onde Ursus, déplacements et impacts visibles. Certaines faces de patte restent sombres : réserve artistique de contraste. |
| zushi_zushi | 6 | batch02 PASS | Petits et grands météores descendent puis creusent le terrain définitivement. Destruction des toits couverte par les tests natifs. |
| bisu_bisu | 4 | batch04 PASS | Projectile biscuit, soldat, armure et création biscuit visibles ; les quatre comportements sont distincts. |
| mochi_mochi | 6 | batch03 PASS | Poings, rafales, poing noir, rotation, anneaux et mur distincts. Mur permanent ; bras adaptés conservés. |
| uta_uta | 7 | batch05 PASS | Notes, laser musical, parade et bouclier visibles. Tot Musica apparaît en attaque et invocation. Les petites invocations exigent encore une appréciation rapprochée en jeu. |
| imu | 3 | batch05 PASS | Flèche noire et rouge, cercles de silence et onde de souveraineté distincts ; contrôle visible sur les cibles. |
| kira_kira | 5 | batch03 PASS | Éclats, avalanche et revêtement diamant visibles ; apparence restaurée en fin d'effet. |
| supa_supa | 4 | batch03 PASS | Lames, entailles, étincelles et revêtement du corps visibles ; gestes distincts. |
| riki_riki | 5 | batch03 PASS | Arcs de force, coups, rochers et cratères distincts ; aucune panne observée dans les scènes. |
| mero_mero | 4 | batch04 PASS | Cœur projectile, coups roses et pétrification visibles ; caméra parfois proche après impact au sol. |
| kiro_kiro | 4 | batch04 PASS | Saut allégé, chutes et cratères distincts ; caméra vanilla parfois comprimée dans le terrain après impact. |
| bomu_bomu | 5 | batch02 PASS | Explosions locales, coups et attaque radiale distincts. Projectile bref, modèle adapté conservé. |
| ope_ope | 4 | batch02 PASS | ROOM filaire visible et persistant pendant sa durée, permutation Shambles et signal de contrôle observés. Finesse du ROOM : amélioration artistique possible. |
| aro_aro | 4 | batch04 PASS | Flèches directionnelles, déplacement et combinaison visibles ; effets de contrôle évalués sur les deux clients. |
| kage_kage | 6 | batch05 PASS | Chauves-souris, lance d'ombre, double et nuée sombres visibles. Island Shatter laisse son cratère ; caméra proche dans les parois. |
| soru_soru | 7 | batch05 PASS | Lames, feu, éclairs et grande onde distincts, cratères permanents. Les visages spécifiques de Zeus/Prometheus ne sont pas validés : réserve artistique maintenue. |

## Réserves avant une validation de jeu prolongée

- Les [14 fruits avec assets candidats](ASSETS-A-METTRE-A-JOUR.md) restent à améliorer par lots. Amaru est encore une adaptation de Daibutsu ; les Gears et plusieurs formes utilisent des adaptations. Aucun de ces nouveaux modèles n'est importé dans alpha23.
- Soru : détails de Zeus/Prometheus non validés. Nikyu : faces de certaines pattes sombres. ROOM : traits fins. Les petites invocations Uta méritent un essai rapproché. Ces points ne sont pas déclarés parfaits parce que leurs mécaniques passent.
- La caméra vanilla peut se rapprocher du corps dans les cratères ; les captures Kiro/Kage/Gura en montrent la limite. La queue du dragon est visuelle, avec collision de déplacement autour de la tête.
- Phoenix : la queue brun-jaune est conservée conformément à l'identification et à la confirmation de l'utilisateur ; aucun défaut de couleur des ailes n'est déduit de cette queue.
- La sélection dans le HUD et les raccourcis sont présents dans les scènes. L'ergonomie prolongée, l'équilibrage PvP/PvE et tous les mélanges de mods ne sont pas validés par cette recette.

Tous les processus de laboratoire sont arrêtés. Les mondes et mods habituels ne sont pas modifiés. Les modules de diagnostic restent séparés du JAR livré. Les premiers échecs de préparation (référence historique d'animation Goro manquante), la reproduction alpha22 des replis absents et les builds intermédiaires restent archivés localement ; ils ne sont pas comptés comme contrôles réussis.

## Reproduire la recette locale

Après restauration des ressources privées et compilation du projet, `tools/run_local_tests.py` lit le JAR et les dépendances de `build/build-local.json`. Les commandes suivantes supposent l'EULA Minecraft acceptée par l'opérateur ; elles créent des mondes de laboratoire neufs.

```powershell
python tools/run_local_tests.py --run --accept-eula --test-tick-rate 20 --mera --magu --goro --historical --permanent
python tools/run_local_tests.py --run --accept-eula --test-tick-rate 80 --batch01 --batch02 --batch03 --batch04 --batch05 --batch06 --batch07 --batch08
python tools/run_local_tests.py --run --accept-eula --test-tick-rate 20 --batch02
```

Pour une scène graphique, préparer avec `python tools/launch_visual_lab.py --accept-eula --capture --scenario batch01`, puis lancer `python tools/launch_visual_lab.py --run --lab <dossier_retourné>`. Refaire avec les scénarios `mera`, `magu`, `goro`, `amaru`, `batch02` à `batch08`. Les planches doivent ensuite être inspectées : le marqueur de fin ne vaut pas une revue d'image. La reprise caméra utilise `--scenario batch01 --retake moku_moku,yami_yami` lors de la préparation. Les diagnostics `CatalogueAuditMod` et `FruitReviewAuditMod` sont des entrypoints de modules de test séparés, pas des entrypoints à ajouter au mod de jeu.

Le prochain sujet peut être le Haki, avec ces réserves conservées dans le suivi.
