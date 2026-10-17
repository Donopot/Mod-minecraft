# Lot04 — validé en laboratoire privé

Voir [bilan final](B04-VALIDATION-2026-10-16.md).

# Lot 04 — en développement

Aro Aro (4), Horo Horo (5), Mero Mero (4), Kiro Kiro (4), Bisu Bisu (4) : 21 techniques. La livraison alpha.12 et son laboratoire restent figés. Le nouveau lot n'est pas encore validé visuellement.

Référence privée : MinePiece ver15 Forge 1.20.1, SHA256 f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704. Les désassemblages sont conservés dans `.runtime/onepiece/b04-audit` à la racine privée.

Corrections établies par lecture des procédures :

- Aro Passive est un déplacement, sans Strength. Combo comporte deux contacts espacés (18 puis20 source), Arrow détruit au contact. Les déplacements ne creusent pas.
- Horo Toku est un gros Mini Hollow explosif, pas un contrôle astral. Rap commande les Mini Hollow possédés. Kumacy est une invocation. Negative est un contrôle distinct des explosions.
- Bisu Biscuit produit trois biscuits aux trois claquements, pas Resistance. Les soldats utilisent leur véritable géométrie/texture. Le catalogue conserve ses quatre entrées ; la variante source supplémentaire de Crash Pretzel reste hors de ce lot.
- Kiro Jump prépare une trajectoire puis une frappe, pas Speed. Weight devient un état serveur borné et lié au porteur exact. L'érosion source continue au sol est adaptée en un impact de chute borné, sans creusement à l'arrêt.
- Mero ne doit pas pétrifier une cible dont les dégâts ont été refusés. Les contrôles doivent respecter les obstacles, les alliances et les effets tiers.

Les dégâts/coûts du catalogue restent les budgets du port, sauf adaptation explicitement documentée. Les valeurs MAX_HEALTH des projectiles source ne sont pas des dégâts. Toute mutation de bloc reste définitive ; les entités et contrôles ont des durées finies. Coffres, protections et blocs résistants conservent les protections du service commun.

Validation à faire : mécaniques natives des cinq fruits, régression, session graphique groupée acteur/observateur, inspection réelle des images, puis profil manuel neuf. Aucune installation normale ni partie utilisateur modifiée.

## Paquet candidat et preuves intermédiaires

Alpha.13 est compilée ;42pistes PAL distinctes et finies,836JSON et95paires de ressources vérifiés sans erreur. Quatre avertissements UV anciens du modèle Abyss restent hors de ce lot. Les117décisions terrain couvrent116techniques/23fruits plus Amaru.

Première recette `run-67cdfd3c4a0742648cf7ae8eb88fd790` :346PASS/2FAIL sur348contrôles du lot ; les deux échecs concernent la disparition prématurée de petits fantômes. Cause : accélération résiduelle native des boules de feu. Corrigée pour les Mini Hollow. Deuxième recette `run-8f05a73f8e334502b19a386777f90d83` :348/348PASS et59soclePASS sur le paquet0b6a8da084472def8958094167884a6a20dde5f3354501ca3326a3d818d4d9df. Ces rapports restent intacts.

Le candidat suivant ajoute la ruée source de Perfume Femur, la vraie callback d'atterrissage lourd, l'expiration native des soldats et les preuves de pétrification/annulation des projectiles. La recette finale est encore en cours ; les résultats intermédiaires ne valent pas validation de son nouveau hash.
