# Validation du HUD alpha 5

**PASS**, sur les octets exacts de ce JAR : `35b4ef7d55080d99c46dbef6c341177d6c29976f58edaee7c8fbeca3c7ef9758`.

Sources : commit `30989a306efa5df64d636cd32e5f54ef96a8db7f`. Compilation des 199 sources Java principales avec JDK 25. Les deux seules classes modifiées par rapport à l'alpha 4 sont `BolterHud` et `BolterKeybindings` ; les classes serveur sont identiques. Les traductions, métadonnées et le tag de cohabitation des armes à distance sont inclus dans le manifeste de sources.

Recette combinée avec One Piece alpha 20, bridge 0.7.1, Fabric API, GeckoLib et Player Animation Library : serveur neuf sur loopback, deux lancements du client privé et mod de test séparé. Aucun mod de test n'est embarqué dans le JAR jouable.

17 assertions client et cinq phases serveur : six touches enregistrées dans les options, catégories localisées, défauts distincts, remappage, aucune technique ni recharge en menu, technique et rechargement reçus réellement, priorité d'un ancien R partagé, F1, interface étroite et persistance après redémarrage. Huit captures finales inspectées, dont le nom des munitions, le rechargement, les nouvelles touches, les écrans masqués et un libellé long avec ligne de poids synthétique.

Les popups transitoires de Minecraft restent prioritaires et peuvent recouvrir une partie des panneaux à grande échelle d'interface. Aucun changement des parties, options ou processus Minecraft du joueur ; les processus enfants du laboratoire ont été arrêtés. Les recettes complètes de gameplay de l'alpha 4 ne sont pas recomptées comme de nouvelles exécutions. L'ancien installeur inter-jeux n'a pas été modifié.
