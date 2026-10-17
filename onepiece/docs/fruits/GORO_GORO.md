**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Contrat courant — Goro Goro alpha.7

Logia, sept techniques et transformation Amaru. [Livraison alpha.7](../GORO-AMARU-2026-10-16.md). Les sept budgets ci-dessous restent ceux d’alpha.6. Rapport et preuves : [Goro alpha.6](../GORO-GORO-2026-10-16.md). L'ancien contrat est conservé dans archive/GORO_GORO-legacy-2026-10-16.md ; ses descriptions de BUFF ou de cônes ne sont plus une preuve du comportement source.

| Technique | Dégâts | Portée/rayon d'exécution | Énergie | Recharge ticks |
|---|---|---|---|---|
| Vari | 14 | blink 24 puis rayon 12 | 50 | 100 |
| El Thor | 24 | point visé dans 8, rayon 4, annonce 10 ticks | 60 | 140 |
| Sango | 18 / 13,5 / 10,125 / 7,59375 | initial 10, saut 6, trois rebonds | 50 | 120 |
| Mamaragan | 10 par frappe, trois max | recherche 8, impact 2, annonce 10 ticks, durée 60 | 50 | 120 |
| Kari Krash | 18 | point 3 devant, rayon 6 | 50 | 120 |
| Passive | aucun | blink 32 max, avant/arrière | 40 | 100 |
| Raigo | 60 max, une seule application directe+zone | cible 24, hauteur 8 limitée au plafond, rayon 4, vie 80 | 150 | 400 |

Vari/El Thor/Sango/Kari : maintenir puis relâcher R, minimum 10 / maximum 100 ticks. Les dommages sont fixes au budget du port ; les multiplications de charge et immenses volumes source ne sont pas reproduits. Vari, El Thor, Sango et Kari donnent 20 ticks de ralentissement au niveau 1 après confirmation des dégâts. Mamaragan/Raigo n'ajoutent pas ce ralentissement.

Référence des sept techniques : JAR utilisateur MinePiece ver15, procédures Vari, ElThor/ElThorFall, Sango, SkillMamaragan et MamaraganOnEffectActiveTick, KariKrash, SkillGoroGoroPassive1, Raigo/AIMeteor. Les chaînes Sango, l'orage court, le Raigo borné et la colonne El Thor systématique sont des adaptations explicites. 800 dans Raigo source désigne les PV de l'entité, pas un budget de dégâts.

Gardes : corps/collisions et chunks déjà chargés sur tout blink, arrêt aux fluides/bordure/protections ; pas de garantie de sol pour une visée aérienne. Dégâts attribués, cibles autorisées, LOS et protections. Aucun feu, terrain destructible ni éclair vanilla dommageable ajouté. Chaîne : une cible refusée ne sert pas de pont. Orage : identité du joueur+niveau et nettoyage à chaque fin. Raigo : origine contrôlée malgré transfert de propriétaire par Nikyu. Résistance Goro foudre et contre Gomu retirés sous suppression ; Haki traverse le contre Gomu comme dans la source.

Preuves : 104 Goro serveur PASS, 11 gestes PAL évalués sur deux clients, trois impacts Mamaragan natifs à +10 ticks avec dégâts. Les anciennes assertions historiques seules ne prouvaient pas les sept routes. Transformations : Goro n'est pas dans le tag MinePiece source. Référence Volt Amaru retrouvée ensuite dans Mine Mine no Mi0.11.6 (DaibutsuModel partagé + ENERGY) ; adaptation et validation encore à faire, absentes de cette alpha.6. Voir ../AMARU-RESOURCE-AUDIT-2026-10-16.md. Validation humaine et artistique en attente.

## Amaru (extension explicitement demandée)

V charge20ticks, activation40énergie ; V annule ou revient à la normale. Modèle massif électrique et quatre tambours, échelle2,5, boîte2,4×6,5 (6,2 accroupi), œil5,5, caméra10 en terrain dégagé. Double saut : vol contrôlé natif. Maintien8/s, supplément12/s en vol ; régénération suspendue, durée max60s. Aucun bonus aux dégâts/armure/portées des sept techniques.

Référence visuelle Mine Mine no Mi0.11.6 (DaibutsuModel partagé / Volt Amaru), textures et effet adaptés ; garder la référence gameplay MinePiece des sept techniques. Goro reste Logia et rejoint le tag transformable (14 au total, 13 anciens préservés). Cycle temporaire avec refus d’espace, contrôle complet des blinks et retour sur suppression, fruit changé, dimension, mort, énergie/durée, déconnexion/arrêt/login. Le vol possédé est réconcilié depuis son flag persistant ; les attributs et équipements externes restent intacts.

Preuves alpha.7 :142Goro/Amaru (104+38),674serveur total, deux clients13contrôles chacun,24captures et17images distinctes inspectées, gestes du modèle évalués entre ticks distincts et UUID attendu. Vol natif/caméra10/retracking/déconnexion réels. Le journal ci-dessus des104 et de l’absence de modèle dans alpha.6 reste historique ; la nouvelle forme est intégrée et vue en jeu, avec acceptation artistique humaine en attente.
