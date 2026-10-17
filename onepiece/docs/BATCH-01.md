> Reprise autorisée : Goro alpha.7 livré, l’utilisateur demande de continuer les autres fruits. B01 est actif ; conserver les cinq fruits avant les tests visuels.

# Lot B01 — cinq fruits avant les tests visuels

Demande utilisateur du16octobre2026 : développer par lots de cinq avant validation visuelle pour accélérer le travail. Cette méthode remplace le passage en client après chaque fruit.

Lot courant : Hie Hie(5), Pika Pika(8), Moku Moku(4), Suna Suna(5), Yami Yami(4), soit26techniques. Audits de référence et réparations, tests serveur par fruit et régressions communes ; seulement après les cinq, scénario graphique groupé. Les résultats restent distincts par fruit, jamais déduits d'un compteur global. Sources ver15 uniques et adaptations documentées, pas de modification des parties ordinaires.

État : les26routes alpha.8 sont implémentées.345contrôles B01+59socle+307historiques PASS sur le JAR final ; session groupée des cinq fruits et reprise ciblée Pika/Moku/Yami inspectées. [Validation, hash et limites](B01-VALIDATION-2026-10-16.md). L’acceptation artistique humaine reste à recueillir. Goro/Amaru alpha.7 est livré et conservé séparément, ainsi que Mera/Magu et les trois JAR de référence.

## Audit initial du lot — lecture seule

Trois revues parallèles ont comparé les sources ver15, le port et les ressources. Aucun Minecraft ni test graphique lancé. Les éléments ci-dessous sont des constats d'audit, pas des réparations testées.

| Fruit | Priorités mécaniques | Identité/rendu à restituer |
|---|---|---|
| Hie Hie | IceTerrain doit protéger inventaires/blocs spéciaux ; dégâts confirmés avant gel ; LOS et protections ; FreezeService lié à entité+niveau, restituer les effets externes | Partisan en lances, Pheasant en oiseau de glace, pics source TREE_ROOT glace, onde Ice Age et attaque Ice Ball dirigée |
| Pika Pika | LightCharge lié à entité+niveau+fruit ; nettoyage ; SafeDestination pour corps entier/chunks/bordure/fluides ; respecter distanceMax | vraie épée Ama no Murakumo, blink passif, phases des rayons/volées/coup de pied ; retirer invisibility inapproprié |
| Moku Moku | Capture d'entrants tardifs ; nuage du bon propriétaire ; coût de mobilité dès énergie zéro ; nettoyage changement de fruit/sortie ; LOS | White Launcher déplacement/frappe ; White Out frappe/attraction ; White Vine slash_white et non slash bleu |
| Suna Suna | SandStorm avec propriétaire/alliance/LOS/protections/bornes ; GroundDeath ne dessèche qu'après dommage confirmé ; Desiccation attribuée et effets externes conservés | Barjan déplacement/frappes source ; slash_sand pour les techniques de sable, actuellement texture bleue erronée |
| Yami Yami | Pull et absorption validés à chaque tick (entité, fruit, dimension, vie, spectateur, suppression) ; marqueur/état synchronisés ; traction corporelle bornée et nettoyage | Dark Ball sphérique distinct de Dark Matter ; ce dernier utilise bien rubble/tex_black dans sa procédure source |

Ressources : slash_white et slash_sand existent dans le JAR original mais manquent dans la baseline portée ; récupération seulement dans l'espace privé ou rendu propre équivalent. Pheasant emploie phoenix/tex_ice dans la source ; son échelle doit être contrôlée pour éviter un volume immense en FPS. Ama no Murakumo possède un item/modèle source réel. Les gestes actuels sont parfois erronés ou regroupent plusieurs phases ; ne pas simplement renommer des copies.

## Ordre de production du lot

1. Corriger les services partagés de ciblage, propriété/dimension, blocs temporaires et restauration d'effets ; un seul build de référence.
2. Reprendre les cinq catalogues et routes, leurs phases et identités ; adaptations et ressources manquantes consignées par fruit.
3. Ajouter des recettes de combat/lifecycle qui peuvent réellement échouer : victimes vivantes insérées, alliés/invulnérables exposés, murs/portées, zéro énergie, vraie échéance, retrait/fruit/dimension et contenu des coffres.
4. Compiler le lot, tests serveur regroupés avec résultats par fruit, contrôles de ressources et de pistes. Corriger les échecs avant tout lancement graphique.
5. Une session graphique pour les26techniques sur le même JAR, deux clients si l'observateur est nécessaire : gestes PAL réellement évalués, FPS/observateur, tailles/impacts/fins et inspection de captures. Seules les scènes en échec sont reprises.
6. Livrer un lanceur de lot et un rapport avec statut par fruit ; conserver les profils précédents. Ne déclarer ni art ni formes terminés sans preuve.

**Implémentation actuelle :** voir [les26techniques et adaptations](B01-IMPLEMENTATION-2026-10-16.md). L’audit initial ci-dessus reste historique. Les preuves finales et l’essai humain sont suivis séparément.
