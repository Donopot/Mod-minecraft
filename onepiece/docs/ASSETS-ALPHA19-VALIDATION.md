# Revue des assets alpha.19 — validation en cours

État historique de l'alpha 19. La reprise des réserves Mera est documentée séparément dans [le rapport alpha 21](MERA-CAMERA-ALPHA21.md). Les réserves Phoenix et Soru restent ouvertes.

Les 40 fruits et 213 techniques sont revus au niveau de la source. Cette passe relie 53 profils visuels, avec 60 fichiers originaux contrôlés octet par octet et 8 adaptations de pistes d’animation. Voir [les décisions par fruit](ASSETS-MINEPIECE-40.md).

Le candidat de développement du 17 octobre porte l’empreinte `5b2256941311a382e2f6f22027aaa7469dbe4fdb24073b2bfe8d394821b36e93`. Il reste en recette graphique privée, sans mise à jour des parties habituelles.

Les 391 classes serveur sont identiques à la recette native `7cddc02c1e74de02b7df6604b64d2526162f050c5de8177ae07fd9479d98b202`, qui passe 882 contrôles : 59 du socle, 365 du lot 04 et 458 du lot 05. Une recette antérieure passe 594 contrôles, dont 535 du lot 06, couvrant le profil visuel distinct de Gagan. Les 1760 résultats historiques ne sont pas recomptés comme une nouvelle exécution exhaustive des 40 fruits.

Les scènes graphiques utilisent deux clients locaux, acteur et observateur. Les contrôles de soumission et d’évaluation PAL complètent l’inspection des captures ; ils ne remplacent ni une vidéo continue ni une acceptation artistique humaine. Les tentatives échouées et les reprises restent conservées séparément par périmètre et empreinte.

Les reprises confirment le masque FPS des invocations limité à leur propriétaire, le dégagement des effets Horo et Rubble près de la caméra, le geste Net en vue subjective et les matériaux additifs d’Uta et de Black Coffee. La reprise des matériaux du lot 07 a terminé ses contrôles automatiques ; l’inspection finale de ses captures reste à faire.

Réserves encore ouvertes : Mera peut afficher brièvement un mesh trop grand au départ d’un tir ; les flammes de propulsion peuvent masquer la vue arrière ; les ailes du Phoenix nécessitent une vérification supplémentaire. La caméra vanilla se rapproche aussi de l’avatar dans les cratères. Les vues éloignées ne valident pas tous les petits détails des compagnons de Soru. Les formes complètes absentes de ver15 restent des adaptations documentées. Les modifications du terrain sont définitives.

Preuve structurelle privée : `build/source-assets-structural-proof.local.json`. Les laboratoires et leurs données d’exécution ne font pas partie du dépôt de sources.
