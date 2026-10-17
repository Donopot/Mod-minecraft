# Goro Goro — chantier courant

L'utilisateur a demandé le fruit suivant après Magu alpha.5. Goro est choisi pour cette itération. Trois audits en lecture seule ont relevé les procédures du JAR MinePiece ver15 ; les anciens contrats décrivaient trop souvent le catalogue plutôt que les effets réels.

Identités source : Vari charge puis blink offensif/décharge ; El Thor chargé puis foudre ; Sango chargé puis orage ; Mamaragan effet d'orage réel, pas marqueur inerte ; Kari Krash choc frontal, sans dash ; Passive blink avant/arrière, pas Célérité ; Raigo sphère suspendue, croissance puis chute. Le `800` du constructeur Raigo concerne les PV de l'entité, pas ses dégâts. Les cornes Yamato associées au Goro étaient sans preuve. Source non transformable, aucune forme Amaru fournie.

Implémentation alpha.6 en cours : sept routes dédiées, quatre charges bornées à 100 ticks / libération minimale 10 ticks ; Vari blink 24 puis décharge rayon 12 ; El Thor point visé dans 8, annonce 10 ticks puis frappe rayon 4 ; Sango conserve l'adaptation déjà choisie de 3 rebonds / portée 6 / atténuation .75 ; Mamaragan conserve 60 ticks / 3 frappes de 10 / annonce de 10 ticks, impacts espacés pour respecter l'invulnérabilité vanilla ; Kari point 3 devant / choc rayon 6 ; blink sans dégâts plafonné à 32 ; Raigo préparation 20 ticks / hauteur 8 limitée par plafond / cible 24 / vie 80 / rayon 4 / dégâts plafonnés existants 60. Pas d'explosion vanilla ni destruction de terrain.

Ces bornes, chaîne Sango et petit orage Mamaragan sont des adaptations de jouabilité, pas une copie exacte des immenses zones, charges et durées de la source. Coûts/recharges et plafonds du catalogue existant conservés. Onze pistes PAL nouvelles, courbes lissées, retour neutre. Géométrie électrique cyan/blanc, points d'annonce, colonnes et sphère Raigo remplacent les maillages rocheux.

Mamaragan est lié à l'identité du porteur et au niveau, vérifie fruit/vie/suppression/LOS/protections, dégâts confirmés avant effets, reset/expiration/déconnexion/arrêt nettoyés. Les blinks contrôlent capsule corporelle, chunks déjà chargés, bordure, fluides et protections sur le trajet. Résistance propre à la foudre supprimée sous scellement.

**Pas encore livré ni validé** : première compilation intermédiaire réussie ; tests serveur dédiés et scénario graphique en construction. Ne pas utiliser l'ancien manifeste alpha.5 comme preuve pour Goro. Les profils Mera/Magu restent figés séparément. Ne pas remplacer les launchers avant les preuves finales ; ne pas modifier mods/saves habituels ni importer de progression.
