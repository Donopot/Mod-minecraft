> Rapport antérieur : le correctif courant est [Magu animations alpha.4](MAGU-ANIMATIONS-2026-10-16.md).

# Magu Magu — travail en cours

L'utilisateur a demandé « fruit suivant », puis « reprend ». Priorité courante : Magu Magu, après l'alpha Mera. Cela ne constitue pas une acceptation artistique de Mera. Les trois audits en lecture seule exigés par le projet ont confirmé les comportements source ver15 via javap.

Implémentation alpha.3 : MaguTechniques (cinq routes), MagmaBurn attribué/borné/indicateur natif deux ticks, MagmaPools avec ServerLevel explicite, exclusion BlockEntity, limite zéro, restitution et filtrage owner/alliés, glace qui éteint vraiment. Les MAGMA_BLOCK temporaires ne doivent jamais infliger de dégâts hotFloor indépendants ; garde avant recherche du fruit dans PowerLogic. Immunité feu/lave du porteur = adaptation du prototype, pas règle source prouvée.

Dai/Inugami : charge minimum10 maximum100ticks, dégâts18..27 et21.6..30, tailles explicites, assets source privés fist_small/tex_magma et inugami_guren. Kagero : dash10ticks vitesse1.2 puis éruption, sans second débit ; ne pas le décrire comme frappe de contact. Volcanic :40ticks, télégraphe puis24 et18, volumes bornés r4/r12. Ryusei :20météores20..39, départ ascendant puis gravité−.1/terminal−2.25, cible visée jusqu'à32blocs. Ces valeurs/rythmes sont adaptations documentées, pas reproduction exacte des21..42météores et coups chaque tick source. Pas encore variante Dai accroupie ni dash maintenu60ticks.

Build alpha.3 compilé :258sources, SHA256 a22659edcb30afa8229aa7840786fdcdf2ac22cc113f51899bfd54e61d1292ff. Première compilation a révélé un import ServerLevel manquant, corrigé. Tests en cours, ne pas déclarer réussis : serveur run-99a3015cb11743f6937e3f7b9afaa9da (session9355, --magu --mera --historical) ; graphique run-5ee4feb42fe641808ca242dcb604af7d (session7310, --scenario magu --capture --run). 65 contrôles Magu attendus,92Mera,59socle,307historiques. Graphique attend30images acteur/20observateur, contrôle éruption après15ticks et déplacement dash>=3.

À faire : lire résultats, corriger défauts mesurés/audits, inspecter captures (sens têtecanine important), probeclient/assets, refaire tests requis après changement, préparer nouveauprofil manuel et Tester-Magu.cmd, mettreàjour contrats/rapports/matrices/handoff/MODLOG. Ne pas modifier lanceurMera figéalpha2 ; alias général peut pointerMagu après validation. Aucun changement au profil habituel, aux mondes personnels ou VOID. EULA déjà acceptée, pas de nouvelle confirmation.


## Résultat final

Journal ci-dessus historique. f7f4b52e3d3c871ef6854b688554a399340d0bbe9c7d4f53caa93ee5ee536efb :66Magu+92Mera+59socle+307historiques PASS ; rapports et limites dans MAGU-MAGU-2026-10-16.md. Tests finaux run-1a0a9e6fc6124484beff5f86b9008c2a / run-7f828379e6ae414d9d3fe6267969e34b ; manuel run-631674ccce1d46fdaacefc5f595c66b7. Les premiers correctifs caméra ne couvraient pas le paquet propriétaire tardif ni le modèle météore par défaut avant métadonnées ; Construction entièrement configurée avant addFreshEntity, VISUAL_READY et masquage de proximité indépendant du propriétaire remplacent cette hypothèse, puis sont revérifiés.
