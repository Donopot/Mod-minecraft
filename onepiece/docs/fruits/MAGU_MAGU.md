**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.

---

Historique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.

> Mise à jour terrain en développement : [décisions par technique](../TERRAIN-POLICY.md). Les preuves de version ci-dessous restent historiques.

# Finition alpha.5

Après validation positive des identités alpha.4, courbes de pose lissées, récupération et respiration, vagues Kagero, jets Volcanic et traînées allégées. Dégâts, coûts et phases conservés. [Rapport courant](../MAGU-POLISH-2026-10-16.md). Preuves 74 Magu et 8 gestes PAL sur deux clients ; appréciation de la nouvelle finition en attente.

# Mise à jour animation alpha.4

Gestes propres à chaque technique et phase : poing droit chargé/frappé, tête canine préparée/projetée, course Kagero/finition basse, ouverture volcanique, frappes alternées vers le ciel. Les anciennes charges energy_charge ne sont plus utilisées pour Magu. Lâcher après retrait de la phase de charge, annulation séparée ; pistes bornées5,1s maximum et contrôlées sur deux vrais clients. FX poing/chien/ciel distincts, sillons bas Kagero au lieu des colonnes Volcanic. Gameplay inchangé ; voir [rapport animation](../MAGU-ANIMATIONS-2026-10-16.md).

# Magu Magu — contrat du prototype, 16 octobre 2026

Priorité utilisateur : fruit suivant après l'alpha Mera. Cinq techniques reprises dans **1.0.1-alpha.4**. Leur implémentation et les adaptations ci-dessous ne constituent pas une acceptation artistique ou une reproduction exacte de chaque variante ver15. Le rapport `../MAGU-MAGU-2026-10-16.md` identifiera les preuves du JAR livré.

## Référence

Source de travail Fabric fournie, référence gameplay MinePiece-ver15-forge-1.20.1.jar réellement inspectée via javap (SHA256 f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704). Les précédents textes simplifiaient à tort Kagero en nappe immédiate et Ryusei en projectile frontal unique ; ils sont archivés dans `archive/MAGU_MAGU-legacy-2026-10-15.md`.

La référence possède des poings chargés, une tête canine, un déplacement Kagero enchaînant l'éruption et une salve montante de météores qui retombent. Les nombres de PV des entités projectiles ne sont pas leurs dégâts d'attaque : AIFist applique réellement18*(1+0,1*charge). Range est une largeur, donc rayon Range/2.

## Techniques du prototype

| Technique | Comportement | Énergie / recharge |
|---|---|---|
| Dai Funka | Charge10 à100 ticks, poing mobile, dégâts18 à27, vitesse initiale1,8, durée60 ticks, impact rayon2,5 |60 /140 ticks|
| Inugami Guren | Charge10 à100 ticks, tête canine animée, dégâts21,6 à30 plafonnés, vitesse1,8, durée60 ticks, impact rayon2 |60 /140 ticks|
| Kagero | Dash10 ticks à vitesse demandée1,2, collision de corps ; puis éruption40 ticks, dégâts20 puis15 ; aucun deuxième débit |40 /80 ticks|
| Volcanic Explosion | Télégraphe, phase à15 ticks rayon4/hauteur8 et24 dégâts, phases20/30 rayon12/hauteur3 et18 dégâts ; fin40 ticks |50 /120 ticks|
| Ryusei Kazan | Salve bornée de20 météores aux ticks20 à39 vers le point visé ; trajectoire ascendante puis chute ;40 dégâts par impact accepté, rayon4, vol borné140 ticks |70 /160 ticks|

Les quatre impacts de zones utilisent rayon réel, hauteur, ligne de vue, protection et dégâts confirmés avant brûlure/recul. Les météores utilisent gravité−0,1, chute limitée−2,25 et friction horizontale0,99 ; ils ne frappent pas les entités pendant leur montée. Les impacts de projectiles partent de la position réelle de collision, avec un seul chemin de dégâts. Coût unique et recharge à l'engagement, refus avant engagement sans débit.

## Brûlure et défense

Brûlure magma :160 ticks par défaut,2 dégâts/seconde, maximum320 ticks dans un épisode rafraîchi continuellement. L'eau ne retire pas cet état ; la glace le retire réellement, y compris au tick suivant. Les immunités natives au feu, la potion Résistance au feu et le porteur Magu non supprimé ne reçoivent pas cette brûlure. Les dégâts portent l'auteur. Un avatar réapparu ne récupère pas l'épisode de son ancienne entité.

Le témoin natif est maintenu à2 ticks pour éviter un second chemin de dégâts vanilla ; il disparaît à l'échéance. Les anciens120 ticks répétés ajoutaient des dégâts et une queue d'incendie non bornée. Un incendie extérieur n'est pas effacé arbitrairement ; si l'eau l'éteint, le magma peut reprendre son témoin. Une brûlure déjà infligée peut persister après retrait du pouvoir de son auteur, jusqu'à sa limite.

Le passif Logia commun reste en place. Immunité au feu/lave/hotFloor de Magu non supprimé : adaptation explicite de ce prototype. La référence ne prouve pas une immunité totale vanilla ; elle accorde surtout une résistance×0,25 à certaines attaques. La suppression retire l'immunité du prototype.

## Nappes temporaires

Configuration existante conservée : trois nappes par porteur,200 ticks, rayon3, résistance maximale10, interrupteur pools_enabled. Rayon de pose borné à8 blocs. Limite zéro = aucune création.

Chaque nappe connaît son monde et son propriétaire. Aucun coffre, four ou autre BlockEntity, fluide, bloc indestructible, construction temporaire existante ou emplacement protégé n'est remplacé. La plus ancienne nappe est rendue avant dépassement. Le terrain est rendu à expiration, retrait/suppression, dimension, mort, déconnexion et arrêt normal du serveur ; les modifications ultérieures du joueur ne sont pas écrasées.

Les blocs magma temporaires ne causent pas de dégâts natifs indépendants hotFloor. Le service applique son contact attribué de2 dégâts puis la brûlure, avec filtres owner/alliés/protection et confirmation des dégâts. Après arrêt brutal, le journal des blocs temporaires sert à la restitution ; l'activité offensive des nappes ne reprend pas automatiquement. Ce cas de crash n'est pas déclaré validé par le simple test de redémarrage historique.

## Rendu et adaptations

Les assets privés ver15 sont réutilisés : poing fist_small/tex_magma et tête inugami_guren avec animation idle. Échelles explicites au lieu du×2,5 générique sur le chien, orientation de la tête corrigée, marge de culling et traînée magma. La géométrie de charge/éruption est provisoire. Les projectiles sont entièrement configurés avant addFreshEntity ; le rendu attend VISUAL_READY pour éviter le modèle provisoire du paquet initial. La vue proche omet le maillage pendant les premières images où la caméra serait à l'intérieur, sans masquer le projectile aux observateurs.

Adaptations : dégâts plafonnés aux budgets du port ; tailles/vitesses choisies pour lisibilité ; ciblage de pluie jusqu'à32 blocs plutôt qu'une direction aléatoire libre ;20 météores au lieu d'environ21 à42 ; éruption espacée en phases au lieu de deux volumes chaque tick ; Kagero limité10 ticks. Variante Dai accroupie et Kagero maintenu jusqu'à60 ticks non repris. Ne pas déclarer toutes les variantes source terminées.

## Essai

`Tester-Magu.cmd` : monde de laboratoire neuf, fruit attribué,2000 énergie. R maintenir/relâcher pour Dai Funka et Inugami ; appuyer pour Kagero, Volcanic et Ryusei. B suivante, F5 caméra. La salve nécessite un temps de préparation avant la montée et la chute. Les deux lanceurs Mera et Magu gardent leurs profils distincts ; aucune progression personnelle importée.

À juger humainement : taille et lisibilité du chien/poing, première personne avec objets tenus, trajectoires et sensation du dash, éruption, nuisances visuelles et équilibre. Vérifications de mort/reconnexion réelles et multijoueur ordinaire restent distinctes des tests avec FakePlayer lanceur.
