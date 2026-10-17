# Validation du pack 1.2.4

Date : 17 octobre 2026. Minecraft 26.3, Fabric Loader 0.19.5, JDK 25, Fabric API 0.162.0+26.3, GeckoLib 5.5.7 et bridge 0.7.1.

Le JAR principal provient de la compilation locale complète des **191 sources Java** du mod. Il inclut les ressources, sa licence et la déclaration d'access widener utilisée par le jeu. Ses octets sont ceux du candidat soumis aux tests ; son SHA-256 est `5277476ec97b1be0d7d5e3c3123fe86c993ebc38cb4a50183c7185cdf54cd461`. Aucun mod ou harnais de test n'est embarqué dans les quatre JAR jouables.

**58 assertions passent sur ce candidat** :

- 24 contrôles natifs : capacité incompatible, limites 1/16/64, conservation des composants, plan de prélèvement, refus de mutation, codecs, migration et conservation verrouillée d'un payload incompatible.
- 34 contrôles dans un serveur Minecraft Fabric neuf accessible uniquement sur le PC : rollback partiel sur deux dépôts et sac, ressources retirées, terrain modifié, ancien reçu incomplet, refus d'écrasement, minage réel, mauvais outil, protection, dépôt plein, détour autour d'un mur, retour au dépôt, trajet impossible et récupération du sac sans nouvelle excavation.
- Le dernier groupe inclut quatre assertions après arrêt et relancement du serveur : sac nommé, usure de l'outil, reçus complets et liste des dépôts relus depuis disque ; reprise explicitement requise.

Fabric API, GeckoLib et le bridge 0.7.1 sont réellement chargés dans le laboratoire serveur. Leurs empreintes sont inchangées depuis le pack 0.7.1. Rogue Trader est absent pendant cette recette.

Les sources des tests et les lanceurs reproductibles sont publiés dans `voiddynasty/src/test` et `voiddynasty/tools/run_work_*.py`. Les chemins locaux de bibliothèques, mots de passe RCON, fichiers de laboratoire et sauvegardes restent exclus du dépôt. La méthode et les commandes figurent dans [la note technique](../../voiddynasty/docs/SERVITOR-1.2.4.md).

Cette recette valide l'exécution serveur et un redémarrage propre. Elle ne valide pas une transaction disque atomique en cas de coupure brutale, le rendu visuel du client, tous les autres mods, la logistique RT, une excavation autonome avec échafaudages ou une reconstruction Gradle sur un poste vierge. Les parties habituelles du joueur n'ont pas été modifiées.
