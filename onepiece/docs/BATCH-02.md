# Livraison actuelle — alpha.11, terrain définitif et lot02

Les71techniques des13fruits traités et Amaru ont une décision terrain explicite. Les météores détruisent les toits au vrai contact ; les cratères, glace, grès, magma, mochi et feuilles créés par les pouvoirs sont définitifs. Aucun ancien état de bloc n’est réappliqué. Les champs et états sur entités restent bornés. [Politique](TERRAIN-POLICY.md), [migration](TERRAIN-DEFINITIF-alpha11.md).

**1684 contrôles serveur PASS** sur le JAR final `c942853ea3d445a0c0920718f331b6183f5c47ba8f8959e206199a9146bdbc1f` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,307historiques,31persistance. Loot, redémarrage, migration du journal ancien inchangé et préservation des modifications joueur PASS. Mécaniques à80ticks/s, historiques à20 ; cela ne prouve pas les performances ou le ressenti.

Les graphismes B02 ont été observés dans une session groupée à deux clients, puis Zushi/Ope repris après correction d’une texture manquante. Les ressources et classes client sont identiques octet par octet à alpha.10 vérifiée graphiquement. Aucune nouvelle session graphique alpha.11 ni acceptation artistique humaine revendiquée. [Bilan B02](B02-VALIDATION-2026-10-16.md).

Tester-OnePiece et Tester-Terrain offrent13fruits ; Tester-Lot-02 offre Gura/Bomu/Zushi/Nikyu/Ope. Profil neuf privé `run-730eb5face854de2812a08fd9c530132`, non lancé à la livraison. Anciens lanceurs/profils conservés. Aucun mod/monde habituel ni VOID modifié. Les40fruits ne sont pas déclarés terminés.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

# Lot02 — Gura, Bomu, Zushi, Nikyu, Ope

Demande de poursuivre les cinq fruits suivants.26techniques : Gura6, Bomu5, Zushi6, Nikyu5, Ope4. Référence mécanique MinePiece ver15 privé. Les contrats antérieurs sont des hypothèses à contrôler contre les procédures : notamment Stone Ope Ope n’est pas une simple frappe dans la source.

Méthode : trois audits parallèles en lecture seule, root seul modifie ; développer les cinq avant une session visuelle groupée à deux clients. Tester dégâts/portées, origine effective des impacts, effets après dommages acceptés, alliés/invulnérables/murs, nettoyage fruit/dimension/mort/suppression et attributs/effets externes. Captures réellement inspectées, poses évaluées, aucun compteur transformé en acceptation artistique.

Priorités sûres relevées : les ondes sismiques avancent avant validation du propriétaire ; gravité sans propriétaire et accélération additive ; météore temporel qui blesse indépendamment de sa collision ; répulsion avant dommage accepté ; ROOM sans LOS ni contrôle de fruit au tick ; section stockée par UUID et restauration d’effets externes risquée. Corriger les services avant le rendu. Correction de l’audit : quake terrainDestruction était true, mais restaurait des trous temporaires. Le joueur demande maintenant des destructions permanentes évaluées par technique, dont les météores sur les toits. Préserver inventaires.

État : implémentation en cours ; premier passage B02 sur ef9fd010 : 328PASS/9FAIL, donc non livré. Les307historiques+59socle passent sur ce hash. Priorité utilisateur : mettre à jour les45techniques des lots précédents avec une politique terrain, puis reprendre B02. Les neuf échecs B02 restent à corriger/valider ; aucun rendu B02 exécuté. B01 alpha.8 et ses profils restent figés. Les ajouts ROOM/Shambles/Amputation sont des adaptations du plan, pas des techniques source cachées. Déviations chiffrées et proofs par hash seront consignées ici.

## Reprise après la livraison terrain alpha.9

Les45techniques précédentes ont267contrôles terrainPASS surd20b7251 et les classes production identiques au passage1019régressions321940df. Le lot reprend enalpha.10 ; ne pas écraser le profil/manifestalpha.9.

Décisions B02 :22techniques destructrices,4sans mutation (NikyuPassive et ROOM/Shambles/Amputation). Source parcourue jusqu’aux handlersPAD/RUBBLE/METEOR ; aucune transformation temporaire terrain retenue pour ce lot. Quotas partagés sur les2ondes de Tenchi et4pierres Ope. Météores destructeurs au vrai toit atteint, sans dégâts fictifs en dessous. L’ancien toggle global d’explosion est contourné par damageOnly pour éviter un second moteur horsquota. Les ondes contrôlées ne posent plus d’AIR temporaire ; leur ancien helper reste réservé aux diagnostics historiques.

Premier passage terrainB02 alpha.10 11971694 : toutes les26politiques terrain ont passé leurs vrais contrôles, BombOverhead passe dégâts/attribution ;5échecs restent sur Rubble/Stone. Traces puis bytecode26.3 : la marge native de collision grandit entre ticks et AABB.clip omet le départ intérieur ; tir lent traverse la boîte au lieu d’entrer par une face. Correctif ciblé SkillProjectileCollisionMixin, aucun changement de vitesse/dégâts. Revalidation en cours sur257ff1e0.

Origines distinctes : Kabutowari creuse seulement si la cible saisie a été blessée ; Kaishin reste radial autour du porteur ; OverheadSuper possède préparation montante puis impact descendant ; BombOverhead utilise une vraie collision verticale (verre/dalles compris) et une impulsion native à vérifier aussi avec le client réel. Le FakePlayer de recette n’est pas une preuve du saut.

Différences choisies :26techniques bornées ; Stone4pierres décalées mais20dégâts max par cible ; ROOM/Shambles/Amputation sont ajouts du plan ; météo depuis28blocs au lieu de200 ; Ursus rayoncombat20 et terrain6 ; registres d’effets attribués par propriétaire et dimension. Aucun avis artistique humain ni renduB02 inspecté à ce stade.
