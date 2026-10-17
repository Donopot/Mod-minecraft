# Lot08 — derniers dinosaures

Implémentation privée alpha.17 : Brachiosaurus et Pachycephalosaurus, dix techniques. Le catalogue atteint 40 fruits / 213 techniques. Cette quantité décrit le code ; le contrôle graphique du lot reste à terminer.

Référence des mécaniques : MinePiece ver15 uniquement. Le relevé des procédures et délégués est réservé sous `.runtime/onepiece/b08-source-audit`.

Brachio Launcher crée un MissileEntity dans la source. Le port réunit le contact catalogue 2,2 et le souffle natif 13,5 en un seul coup accepté de 15,7 sur la cible directe, puis 13,5 aux voisins valides. Un quota de terrain et une victime frappée une seule fois restent partagés par lancement. Sparking Queen est le projectile laser rouge, pas une flamme ; Black Cofee est une véritable attaque de rayon avec obstacle, à 17 dégâts. Brachio Bober endommage et détruit au vrai atterrissage.

Ul Zugan reste une charge sans destruction de blocs : sa chaîne source n’appelle pas BlockDestroy. Headbutt détruit seulement au contact borné. Ul Meteor détruit au véritable atterrissage ; sa trajectoire n’est plus arrondie aux pas de déplacement de 0,25 bloc. Attack Jump suit Attack Overhead au lieu de rester un simple buff à zéro dégât. Ces adaptations explicites ne changent pas les coûts/recharges du catalogue.

Les deux fruits ont un cycle hybride → animal complet → humain, dimensions et hauteur des yeux natives, vérification de place avant coût, maintien et expiration. La géométrie complète Brachiosaurus vient de la réserve MMnM visuelle ; Pachy est une adaptation originale. La prochaine passe demandée par l’utilisateur recherchera leurs originaux MinePiece, comme pour tous les fruits. Les mutations de terrain sont permanentes et respectent inventaires, protections, budgets et blocs résistants.

Le premier test B08 (173/175) révélait la quantification de déplacement de l’ancien helper : Ul Meteor s’arrêtait prématurément en l’air. Le déplacement exact a été réparé et cinq contrôles de distance ainsi que trois contrôles de propriété du contre ont été ajoutés. La recette finale `run-9ffbd06d22d0478787462b1d99e70db4` passe les 183 contrôles B08, les 502 B07, les 535 B06, les 143 Mochi, les 59 du socle, les 307 historiques et les 31 de permanence : 1760 contrôles serveur. Le paquet testé est identifié dans `build/final-working-proof.local.json` ; les changements ultérieurs actuels sont exclusivement client/assets/diagnostic graphique.

Aucune validation artistique humaine revendiquée. Installation normale inchangée.
