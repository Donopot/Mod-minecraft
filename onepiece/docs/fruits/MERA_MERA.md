Mise à jour du 17 octobre — Mera alpha.18 : les sept attaques reprennent les particules animées FLAME/FLAME_SMALL de Fire Propulsion. Traînées sur les tirs, colonne sur Hibashira, souffle sur Kaenryuo, petites flammes sur Hotarubi et enveloppe sur Entei. Émission limitée à96particules par2ticks côté client ; extinction naturelle en15ticks. Dégâts et comportement serveur identiques au paquet natif validé. La scène réelle fournit48captures acteur et24observateur, avec les sept émetteurs présents dans les journaux de chaque rôle ; les deux planches ont été inspectées. Le mesh opaque préexistant d’Entei peut encore traverser la caméra. Acceptation artistique humaine en attente. Preuve : build/mera-flames-alpha18-proof.local.json. Tester-Mera.cmd ouvre un nouveau laboratoire privé alpha.18 ; installation normale inchangée. Revue des assets des40fruits toujours en cours.

**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Mera Mera — contrat du prototype, 16 octobre 2026

Priorité utilisateur : terminer Mera avant de passer à un autre fruit. Les sept techniques sont reprises en alpha privée 1.0.1-alpha.2. La validation technique, les scènes graphiques et l'acceptation artistique restent distinguées ; consulter le rapport MERA-MERA-2026-10-16.md pour les résultats du JAR courant.

## Référence et corrections de spécification

Référence originale effectivement inspectée par javap : MinePiece-ver15-forge-1.20.1.jar, SHA256 f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704. Sources de travail : archive Fabric fournie par l'utilisateur. Projet privé ; aucune redistribution des sources ou ressources.

L'ancienne fiche décrivait des simplifications du port comme des règles originales. Le bytecode montre un poing mobile pour Hiken, une boule préparée au-dessus du lanceur pour Daienkai, un déplacement offensif pour Kagero et un champ ciblé pour Hotarubi. RangeAttackProcedure traite Range comme une largeur : rayon Range/2. L'ancienne fiche est conservée dans archive/MERA_MERA-legacy-2026-10-15.md et ne fait plus autorité.

## Techniques livrées

| Technique | Comportement du prototype | Coût / recharge |
|---|---|---|
| Hiken | Charge puis poing mobile ; relâchement après minimum 10 ticks ; 18 à 27 dégâts selon charge ; vitesse initiale 8, durée 20 ticks | 50 /100 ticks |
| Higan | Tir rapide ; quatre tirs si charge >=10 ticks ; 15 dégâts par projectile, impact de feu rayon 4 ; durée 15 ticks et explosion à expiration | 50 /100 ticks |
| Hibashira | Télégraphe 10 ticks, colonne rayon 4/hauteur 8, puis éruption rayon 12/hauteur 3 ; phases 24 puis 18 dégâts ; durée 40 ticks | 50 /120 ticks |
| Kagero | Déplacement offensif 10 ticks, vitesse demandée 1,8, collision du corps ; 15 dégâts par cible au contact de la trajectoire | 40 /80 ticks |
| Hotarubi | Visée jusqu'à 30 blocs, champ fixe de lucioles 15 ticks ; rayon 4 à 6,1, hauteur 4 ; dégâts croissants 15+0,05/tick soumis aux invulnérabilités natives | 50 /100 ticks |
| Kaenryuo | Coup frontal portée 6 puis éruption rayon 6/hauteur 4 ; phases à 26 puis 26,1–26,3 ; durée 40 ticks | 60 /140 ticks |
| Daienkai / Entei | Charge en hauteur puis boule projetée vitesse initiale 3 ; 35 à 52,5 dégâts selon charge ; rayon d'impact 6 à 9 ; plafond de dommage du catalogue conservé 60 | 100 /300 ticks |

Les charges se relâchent automatiquement après 100 ticks maximum. Coût unique au début de la technique, recharge immédiatement engagée ; une interruption ultérieure ne rembourse pas le coût engagé. Une technique refusée avant engagement ne dépense rien.

## Feu, défense et mobilité

Brûlure suivie :60 ticks par application, plafond de rafraîchissement 120 ticks par épisode continu. Une période sans nouvelle application permet un nouvel épisode ; l'eau éteint, les cibles immunisées ou sous Résistance au feu ne brûlent pas. Le service maintient un indicateur de feu de 2 ticks pour le visuel vanilla et applique son propre DPS, afin d'éviter un second compteur de dégâts vanilla.

Les volumes de feu vérifient leur rayon réel, leur hauteur, la ligne de vue, les alliés et le retour effectif des dégâts avant d'ajouter la brûlure. Les zones sont des effets : aucun bloc FIRE vanilla n'est créé par Mera, donc aucun feu ne se propage hors du pouvoir. Le service FireRing historique reste un outil de diagnostic configurable ; Hibashira utilise désormais ses phases propres.

V active/arrête la propulsion de feu. Elle coûte 0,6 énergie/tick par défaut, s'arrête devant un obstacle, à épuisement, suppression, retrait, déconnexion ou changement de dimension. Le droit de vol accordé par Mera est identifié dans la sauvegarde, puis révoqué correctement. Propulsion et Kagero sont exclusifs.

Le passif Logia physique/Haki/eau existant est conservé. L'immunité au feu ordinaire est une adaptation du prototype ; la lave et le magma restent des contres. L'ancienne affirmation d'une immunité source générale à la lave était infondée et n'est pas reprise.

## Adaptations explicites et limites

Ce prototype ne reproduit pas chaque tick de ver 15 à l'identique. Les coûts/recharges du port sont conservés. Le tir chargé Higan conserve 15 dégâts par projectile ; le verrouillage Hotarubi suit une visée étroite avec ligne de vue ; Kagero utilise un contact balayé borné plutôt que la large zone source ; le premier geste RyunoKagizume de Kaenryuo est représenté par un coup frontal et un dragon de flammes procédural. Les formes de flammes sont créées en géométrie, sans nouveaux bitmaps. La fidélité artistique à l'anime reste à valider humainement.

Les correctifs de rendu comprennent l'extraction des données GeckoLib au bon moment, l'absence de gravité pour les effets et l'expiration pilotée uniquement par le serveur. L'animation Kaenryuo est bornée à 0,5 s ; l'asset générique Jinbe indiquait 60 s. Kagero ne cache plus le corps avec l'ancien geste invisibility.

## Essai humain

Tester-Mera.cmd ouvre un monde neuf séparé, avec Mera attribué et 2000 énergie de test. R : technique (maintenir puis relâcher pour les quatre charges) ; B : suivante ; V : propulsion ; F5 : caméra. Des cibles et obstacles de laboratoire sont préparés. Ce profil réinitialise ses équipements et sa sélection à la connexion ; il sert aux tests, pas à conserver une partie personnelle. Aucun ancien fichier de pouvoirs n'est importé.

À accepter en jouant : lisibilité du poing et des petits tirs, taille du pilier/Entei, gestes avec les objets tenus, sensation du dash et du vol, équilibre des phases multiples. Ne pas déclarer les 40 fruits terminés à partir de ce seul lot.
