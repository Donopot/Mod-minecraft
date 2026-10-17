# Goro Goro — Amaru, alpha.7

**Version prête pour l’essai humain : 674 contrôles serveur PASS, scène Amaru vérifiée sur deux clients.**

Priorité demandée : terminer Goro avant de reprendre les autres fruits. Le lot B01 est différé. Les trois JAR fournis sont copiés et vérifiés dans `.runtime/onepiece/reference-jars/manifest.local.json`, depuis la racine du workspace ; ils restent des références privées, pas des mods Forge à installer dans Fabric.

## Jouer

Le lanceur `Tester-Goro.cmd` cible le profil alpha.7 validé. R : technique ; B : suivante ; maintenir puis relâcher R pour Vari, El Thor, Sango et Kari Krash. Maj choisit le blink arrière. F5 change la vue.

**V** lance une charge d'une seconde vers **Amaru**. V annule la charge ou rend la forme normale. En Amaru, le double saut active le vol Minecraft ; saut/accroupissement montent/descendent. Le retour supprime seulement le vol accordé par cette forme. Les objets tenus restent utilisables et les bras suivent les gestes des techniques.

## Choix du port

Le tag transformable conserve ses13 entrées précédentes et ajoute Goro ; les commandes présentent donc correctement cette capacité, sans changer sa famille Logia.

Les sept techniques et leurs budgets alpha.6 sont conservés : [contrat Goro](fruits/GORO_GORO.md). La forme ne donne aucun bonus caché de dégâts, armure ou portée aux techniques.

La nouvelle référence visuelle explicitement fournie est Mine Mine no Mi1.20.1-0.11.6, SHA256 `ceab4866a28236c5f74fc0245dc8a079ce9284b93eee7865ec8414c487de30cc`. Volt Amaru y utilise le modèle Java Daibutsu partagé, pas un fichier géométrique Amaru autonome. [Audit de ressource](AMARU-RESOURCE-AUDIT-2026-10-16.md).

L'adaptation reprend le torse et les membres épaissis, les bras écartés, la tête relevée, l'échelle2,5, la boîte2,4×6,5 (6,2 accroupi) et les yeux5,5. Les dimensions spécifiques précèdent l'échelle vanilla ; elles fonctionnent sur le serveur et les clients. La caméra vanilla recule à10 en espace dégagé ; ses collisions réduisent cette distance près des obstacles.

Le rendu combine un corps électrique cyan avec des lignes animées, des yeux lumineux, des oreilles allongées et les quatre tambours Enel déjà disponibles dans la référence MinePiece. Les textures procédurales, ces détails et la nouvelle animation finie de transformation sont des adaptations artistiques, pas une prétendue animation source retrouvée. Les gestes des sept techniques sont repris depuis le modèle joueur animé, puis transmis par snapshot au modèle électrique différé. Le placement des objets tenus suit le même parent élargi. En première personne, les mains et objets restent ceux du rendu natif ; le corps géant ne recouvre pas la caméra.

Le vol est ici le vol contrôlé natif, pas l'implémentation PropelledFlightAbility de Forge. Budgets propres au port : charge20ticks, activation40énergie, maintien8/s et supplément12/s pendant le vol, durée maximale1200ticks (60s). La régénération de4/s est suspendue pendant Amaru. Ces choix évitent d'importer silencieusement les statistiques de combat de Mine Mine no Mi.

## Cycle de vie et compatibilité

Le déploiement vérifie le volume entier : espace, chunks chargés, fluides, bordure, hauteur du monde et protections. La charge revalide espace, énergie et taille avant débit. Une échelle externe incompatible empêche le déploiement ; un changement incompatible pendant la forme l'arrête, en conservant les modificateurs externes.

Les blinks d'Amaru contrôlent aussi le volume entier pour éviter qu'une tête, un côté ou un pied franchisse un fluide, une protection ou la limite du monde alors que le centre serait autorisé. Les budgets et routes humaines restent inchangés.

L'état appartient à l'objet joueur et au niveau, avec arrêt sur suppression, remplacement du fruit, mort, changement de dimension, déconnexion, fin d'énergie, fin de durée et arrêt du serveur. Amaru est temporaire : le login/respawn ne restaure pas cette forme. L'ownership du vol est enregistré avant de l'accorder et retiré durablement à la fin ; un ancien flag de sauvegarde est réconcilié même sans map de formes en mémoire. Aucun effet ni attribut externe n'est supprimé globalement.

La synchronisation porte l'UUID, le fruit et la forme. Les observateurs et nouveaux observateurs reçoivent l'état ; la map client est effacée au changement de niveau. Le corps humain est masqué seulement pour cette forme complète, y compris en contour lumineux. L'invisibilité complète reste respectée ; la forme partiellement visible emploie une opacité réduite.

## Validation

La compilation et les audits de ressources/animations passent. Premier passage de développement :137Goro/Amaru +59socle PASS. Des contrôles supplémentaires de persistance et d'échelle sont ensuite ajoutés ; les résultats du JAR final doivent être consignés ci-dessous avant livraison.

La première scène graphique a confirmé sur deux clients les dimensions réelles, les gestes du nouveau modèle, les retours et la suppression. La scène étendue a confirmé le double saut natif et la distance de caméra10 ; son observateur pouvait contrôler les retours avant l'action de l'acteur. Le harnais est corrigé par une horloge transmise par l'acteur, sans retirer les assertions. Ces passages de développement ne valent pas encore validation du JAR final.

Les tests FakePlayer vérifient les attributs et collisions réels du serveur, avec ticks de charge/entretien explicitement pilotés. Les appels de réconciliation ne prétendent pas simuler une véritable reconnexion ou un véritable écran de mort. La scène graphique vérifie les gestes effectivement évalués à des instants distincts, l'UUID réel, le vol natif, la caméra, la resynchronisation après retracking et la déconnexion réelle. La validation artistique humaine reste distincte.

JAR **1.0.1-alpha.7**, SHA256 `57264a351417d5962051d62ce5682cf2524b0dc03df12b043fca7829481987a6`.

- Serveur `run-a1d3b558c2cc4c45a875a72887225243` : **142 Goro/Amaru +92 Mera +74 Magu +59 socle +307 historiques =674 PASS**. Loot et redémarrage réussis. Les 142 incluent104 techniques et38 contrôles Amaru.
- Deux clients `run-c97e96d93b04464f98804120676525be` : **13 contrôles chacun**, dont dimensions/yeux par UUID, gestes effectivement évalués, retours, vol par double saut, caméra10 et resynchronisation après retracking. Déconnexion réelle : `AMARU_ACTIVE=0`.
- 12 captures acteur +12 observateur ; **17 images distinctes inspectées**. Aucun défaut visuel bloquant constaté ; contraste et visage restent des choix stylisés à apprécier en jeu. Six pistes PAL distinctes effectivement évaluées par client dans cette scène ciblée ; douze pistes Goro finies auditées dans le JAR.
- Audit ressources637JSON/0erreur/4anciensavertissements ; huit pistes Magu inchangées ; chargement client17cibles PASS, sans prétendre couvrir tout le modpack.
- Profil manuel `run-1f29a788ca1346d88867d16c2dfe2a9c`, même SHA256, neuf et non lancé ; Tester-Goro et alias Tester-OnePiece prêts. Profils Mera/Magu conservés. Aucun monde/mod habituel ni VOID remplacé ; enfants automatiques arrêtés.

Les résultats et chemins sont dans `build/verification-summary.local.json`. L’appréciation artistique humaine reste à recueillir ; les autres fruits ne sont pas déclarés terminés.
