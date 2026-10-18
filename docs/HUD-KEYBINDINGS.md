# HUD et raccourcis — VOID DYNASTY / One Piece

Versions : **VOID DYNASTY 1.3.0-alpha.5**, **One Piece 1.0.1-alpha.20** ; bridge 0.7.1 inchangé. Minecraft 26.3, Fabric Loader 0.19.5, Java 25. Le pack combiné utilise aussi Fabric API, GeckoLib et Player Animation Library.

## Touches par défaut

Les raccourcis sont regroupés dans **Options → Contrôles → Affectation des touches**, catégories **VOID DYNASTY** et **One Piece — Fruits du Démon**.

| Action | Touche par défaut |
|---|---|
| Utiliser la technique One Piece sélectionnée | **G** |
| Technique suivante | **B** |
| Changer de forme | **V** |
| Afficher / masquer le HUD One Piece | **H** |
| Recharger l'arme VOID DYNASTY tenue | **R** |
| Afficher / masquer le HUD des armes | **J** |

Les identifiants des anciens raccourcis sont conservés : une touche déjà personnalisée dans `options.txt` n'est pas remplacée. **G est le nouveau défaut pour les installations neuves**, et non une réinitialisation des préférences existantes. Le HUD montre la touche réellement choisie, y compris après remappage sur une autre touche ou un bouton de souris.

Si une ancienne configuration affecte la même touche à la technique et au rechargement, tenir une arme à feu VOID DYNASTY donne priorité au rechargement. Sans cette arme, la technique conserve sa touche. Il reste possible d'affecter deux touches distinctes dans les contrôles. Ce comportement partagé suppose les deux nouvelles versions.

Les actions sont ignorées et leurs clics en attente vidés dans les écrans et le chat. Une technique maintenue est relâchée quand son bouton est relâché ou qu'un écran est ouvert. Le rechargement nécessite une arme à munitions tenue et le canal réseau disponible. Aucun changement des dégâts, coûts, cadences ou recettes n'est inclus.

## Affichage

- **One Piece, en haut à gauche** : fruit, énergie, technique et index, forme (dont Gear et Amaru), poids lorsqu'il est modifié, puis rappels des trois touches courantes. Les noms longs disposent de deux lignes bornées. Le panneau adapte sa hauteur aux lignes optionnelles et se termine avant la zone de chat dans les formats vérifiés.
- **VOID DYNASTY, à droite au-dessus des notifications centrales** : arme tenue, chargeur, réserve disponible, type de munition, état prêt/vide/rechargement et touche courante. Les valeurs du chargeur et du rechargement viennent des composants synchronisés ; la réserve utilise les mêmes emplacements que le service de rechargement.
- Les panneaux s'adaptent à la largeur de l'interface et respectent **F1**, les écrans et le mode spectateur. Les masquages H/J sont mémorisés séparément et survivent à un redémarrage.
- Les rappels de charge de Mera et d'annulation d'Amaru résolvent aussi la touche configurée sur le client. Les langues française et anglaise sont mises à jour. Les popups transitoires de Minecraft conservent leur priorité d'affichage.

## Livraison et vérification

Fermer Minecraft, sauvegarder les mondes, remplacer les deux JAR principaux et garder un seul exemplaire de chaque mod et de chaque dépendance. Le bridge reste en 0.7.1. Le format de sauvegarde du servitor et les paquets réseau One Piece sont inchangés.

La recette `voiddynasty/tools/run_hud_lab.py` compile un mod de test séparé, ouvre un monde neuf en loopback, puis un client privé à deux reprises. Elle vérifie l'enregistrement des six touches, leurs catégories, le remappage, l'absence d'action en menu, un lancement de technique et un rechargement réellement reçus sur le serveur, la priorité d'un ancien R partagé, F1 et la conservation des préférences. Elle produit huit captures, dont une interface étroite avec un libellé long et la ligne de poids. Cette dernière est une fixture synthétique de rendu ; elle ne prétend pas valider une nouvelle mécanique de poids.

Les anciennes recettes de gameplay ne sont pas recomptées comme une nouvelle exécution exhaustive. Les classes de gameplay VOID sont identiques à l'alpha 4 ; côté serveur One Piece, les trois classes modifiées n'ajoutent que les rappels de touches localisés. Les autres classes serveur sont identiques à l'alpha 19. Les réserves artistiques concernant les effets et transformations One Piece restent celles de l'[alpha 19](../onepiece/docs/ASSETS-ALPHA19-VALIDATION.md).
