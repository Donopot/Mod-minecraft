> Journal intermédiaire archivé : hashes et actions en attente ci-dessous dépassés. Résultat final : [rapport Mera](MERA-MERA-2026-10-16.md), 92 Mera +59 socle +307 historiques et propulsion client 19,39 blocs.

# Mera Mera — travail en cours le 16 octobre 2026

Priorité explicite utilisateur : terminer Mera, puis un fruit à la fois. Les autres fruits ne sont pas déclarés terminés. Pas d'installation dans le profil habituel.

Référence réellement inspectée : MinePiece-ver15-forge-1.20.1.jar, bytecode javap. Les anciens contrats du port simplifiaient à tort Kagero en SPEED, Hiken en mêlée et Daienkai en anneau identique à Hibashira. RangeAttack interprète Range comme une largeur (rayon Range/2).

Changements alpha.2 en cours : sept routes dédiées ; charge/relâchement C2S pour Hiken/Higan/Hotarubi/Daienkai ; Higan vitesse8 et impact feu ; Hotarubi champ ciblé de lucioles ; Hibashira télégraphe/colonne/rayon4 puis12 ; Kagero déplacement offensif borné ; Kaenryuo poing puis éruption ; Daienkai boule préparée en hauteur puis projetée et impact feu. Les coûts/recharges restent ceux du catalogue, le plafond équilibré de Daienkai reste60. Les formules de charge et la fidélité fine au bytecode ne sont pas déclarées exactes à ce stade.

Feu persistant repris : géométrie circulaire, hauteur, murs, cibles alliées, dégâts confirmés ; effets sans blocs FIRE vanilla pour éviter propagation et dégâts indépendants sur les alliés. Burn a un budget de rafraîchissement et un seul chemin de dégâts ; potion résistance au feu respectée. Propulsion accessible via V ou vol, coût/tick, collision de corps, reset et fin de monde. Immunité au feu ordinaire ajoutée comme adaptation Logia, lave non immunisée (ce n'est pas une preuve du comportement original).

Visuels procéduraux dédiés sans nouveaux bitmaps ; alias Kaenryuo .5s pour éviter l'animation source Jinbe longue60s. Premier test serveur :64/66 Mera,59/59 socle,306/307 historiques ; deux tirs au-dessus de vaches trop basses, fixture remplacée par husks adultes sans armure ; un défaut réel de redémarrage de propulsion corrigé. Nouvelle vérification en cours : run-a3ad3123f38749fab4dadfa4e54199f4.

Build actuel avant prochaines éditions :255 sources, SHA256916bdcf424442323b7024e209f1644580fa56beb2274ea7d9394783eda60f980. Client transform probe17 cibles passe. Assets617JSON/82paires,0erreur,4ancienswarningsAbyss. Nouveaux scripts --mera serveur et --scenario mera --capture client ; scénario deux vrais clients FruitLab/FruitWitness écrit, pas encore lancé ni vu. Ne pas déclarer ces captures vérifiées.

Prochaines actions : finir les tests, lancer/inspecter les captures Mera, corriger les écarts visuels et de mouvement, documenter les différences au original, préparer un profil manuel Mera neuf et mettre à jour HANDOFF/MIGRATION_STATUS/MODLOG/manifeste.


Point de reprise plus récent : JAR f266536c6f0e4a248219ce3bc0b1e7dd5376f3e7efe550221d4183fa4152435c validé90Mera+59socle+307historiques run-23fdf7d5c46c4ade8600e98b96d72201. Captures run-a54306bfaee14db9a66588935e9eaa36 ont confirmé7effets après correctifs addRenderData, gravité et durée client15ticks ; 10images de cette série réellement inspectées. Contrôle mouvement ajouté dans MeraVisualLab : run-2c28800fbcf243ed8d8944b328e0e474 échoue propulsion moved=.4924 (une seule impulsion). Le 16/10 en cours : ajout S2C FlameThrustPayload + flagClient STARTtick pour empêcher vanilla de couper flying au sol ; fenêtre de3ticks serveur pour synchronisation. À compiler/tester maintenant. Ne pas déclarer le vol final accepté avant preuve client >=3blocs. Aucun jeu personnel modifié. Tester-Mera.cmd pointe encore sur l'ancien profilDBC ; nouveauprofil C99 préparé f266 mais pas encore attaché au lanceur. Rafraîchir après buildfinal. Canonicaldocs/fruits/MERA_MERA.md actualisé, anciencontrat archivé. Résumés HANDOFF/MIGRATION/verification-summary restent encorealpha1, à remplacer après résultats finaux.
