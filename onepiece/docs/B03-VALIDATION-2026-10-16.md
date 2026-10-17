# Lot 03 — réalisation, preuves et limites

Mochi Mochi (6), Mori Mori (4), Kira Kira (5), Supa Supa (4) et Riki Riki (5) : **24 techniques**, développées avant la première session graphique groupée. La version privée est `1.0.1-alpha.12`. Les 40 fruits ne sont pas déclarés terminés.

Les attaques ont une préparation finie, des dégâts confirmés, une attribution au porteur, une sélection protégée des cibles et un nettoyage lié au joueur exact, au fruit et à la dimension. Gatling Mochi distribue six impacts sur de vrais ticks. Muso suit le porteur avec deux anneaux réactifs ; ses poings convergent vers le point visé et partagent leur budget. Mori lance des racines et résout plusieurs phases radiales sans redonner un budget à une cible déjà touchée. Les revêtements diamant et lames utilisent les modèles natifs sans écraser une forme Zoan. Les pierres Riki sont de vrais projectiles ; elles ne détruisent plus un point distant dès l'appui.

La référence mécanique reste MinePiece ver15. Le désassemblage a distingué MAX_HEALTH des vrais dégâts : les constantes de durabilité 15/20/50/200 ne deviennent pas des dégâts. Les budgets équilibrés du catalogue, les bornes de volume, Diamond Body, l'identité diamant des pierres et la construction Mochi sont des adaptations explicites. [Contrats et adaptations](BATCH-03.md).

Chaque technique possède une [décision terrain](TERRAIN-POLICY.md). Les mutations restent définitives ; les champs et contrôles sur les entités expirent. Mori convertit sélectivement le sol en herbe ; la construction Mochi évite les entités présentes. Les quotas sont partagés et les inventaires, protections et blocs résistants restent protégés. Un projectile dévié par Nikyu conserve son effet offensif sous l'autorité du déviateur, mais cesse de modifier les blocs.

## Recette serveur

**418 contrôles B03 passent**, dont cinq scènes par technique : ouverte, mur résistant, mur destructible, allié seul dans l'axe, invulnérable seul dans l'axe. Les dégâts exacts, le débit Gatling, l'absence de frappe autonome des anneaux, les vrais coups de suivi, les branches à budget partagé, la cadence, l'attribution et les quotas sont mesurés. La recette comprend les expirations natives, la conservation des potions/modificateurs externes, la suppression après départ d'un projectile, le décès, les arrivants tardifs dans Surround et l'herbe restant après réinitialisation et échéance.

La régression couvre 2 102 exigences : 59 socle, 92 Mera, 74 Magu, 142 Goro/Amaru, 345 lot 01, 271 terrain, 363 lot 02, 418 lot 03, 307 historiques et 31 persistance. Loot, redémarrage et ancien journal inchangé sont vérifiés.

La provenance est conservée sans transformer un essai échoué en succès :

- `run-ed514f9ad45642dca7588be9598071a7` : première passe complète, **2 102 PASS** à 80 ticks/s, avant les derniers ajustements visuels.
- `run-555f3f75e91a486f86f6a3e90c413ccd` : code de jeu/rendu corrigé, 2 101 PASS et **un échec Goro** (`chain-refused-no-bridge`) à 160 ticks/s. La cause de cet échec isolé n'est pas établie ; ce rapport reste marqué échoué.
- `run-5690c4c04253486799c932bbd6af94a2` : **142 Goro PASS** à 80 ticks/s sur le paquet final ; cette recette remplace uniquement la couverture Goro dans le bilan assemblé.
- `run-604ed1bcf135406ebb370e53b14dd77a` : **59 socle + 124 Kira/services communs PASS** sur le paquet final. Avec la recette Goro, 325 contrôles distincts ont été exécutés directement sur ce paquet.

Entre la grande passe corrigée et le paquet final, seules deux classes de diagnostic graphique changent : `RepairTestCommands` et `Batch03VisualLab`. Tous les autres octets du JAR sont identiques, y compris les mécaniques, modèles, textures et classes de rendu. Cette comparaison est enregistrée dans `build/b03-test-only-delta.local.json`. Le bilan de 2 102 exigences est une couverture par recettes complémentaires sur code de jeu identique, **pas une passe unique entièrement verte sur le hash final**. Les historiques restent à 20 ticks/s ; l'accélération mécanique ne prouve pas les performances ni le ressenti à cadence normale.

## Images et clients réels

La première session groupée `run-d33da89b5d1345839ee5fcfc793f9492` a produit 120 images acteur et 120 observateur. Les dix planches ont été examinées. Elle a révélé un décor Mochi laissé devant Mori, un bras natif trop long, des effets masquant la caméra et deux erreurs dans les sondes Kira.

La reprise ciblée `run-a1a73c96ad2c412d85f9e30a05294336` corrige les scènes Mochi/Mori/Riki : décor de laboratoire explicitement reconstruit, portée visuelle des bras réajustée, particules compactes et masquage des projectiles contenant la caméra. Ses sondes Kira échouées ne sont pas retenues comme preuve.

La reprise `run-a538bd7abe864a2ca2954ce8f79589cd` valide Kira et vérifie Supa avec le rendu commun corrigé. Le retour de l'observateur attend son constat réel de retrait de l'avatar, puis un nouvel état reçu pour le bon UUID. La réduction de Diamond Body compare deux coups sur le même joueur à difficulté identique : **5 HP sans le revêtement, environ 2,93 HP avec**, sous le plafond configuré de 60 %. La noyade ignore normalement cette armure : **4 HP**. L'ancienne sonde confondait les dégâts d'entrée avec ceux réduits par la difficulté facile.

La couverture retenue assemble **120 images acteur + 120 observateur**, 50 évaluations de poses par rôle (48 phases distinctes et le Pistol de suivi), 38 contrôles acteur, 36 observateur et deux contrôles de dégâts sur joueur réel. Dix nouvelles planches ont été examinées. Le détail et les composants sont dans `build/b03-composite/visual-lab.local.json` et `build/batch03-inspection.local.json`. Les ressources et le rendu sont identiques entre les deux reprises ; seule l'instrumentation Kira évolue.

Les corps expirent et le cache observateur se resynchronise ; le diamant possède une icône valide. La transformation garde un bref éclat visuel. Les vues observateur sont des plans larges. Les captures et les poses évaluées ne remplacent pas une longue partie de survie ni l'avis artistique humain, qui reste attendu.

## Essai et dette conservée

`Tester-Lot-03.cmd` propose les cinq nouveaux fruits. `Tester-OnePiece.cmd` et `Tester-Terrain.cmd` proposent les 18 fruits traités. R lance, B sélectionne la suivante, F5 change la caméra. La reprise du même fruit conserve le monde sauvegardé ; changer de fruit ou utiliser `/dvisual` reconstruit volontairement le décor de laboratoire. Cette commande de scénario est distincte des pouvoirs permanents.

L'ancien helper Gatling Gomu n'est pas déclaré terminé : ses six tentatives dans un même tick ne valent qu'un coup accepté par le moteur. Son diagnostic confronte désormais le compteur à la perte réelle sur un témoin neutre. La vraie cadence Gomu et ses formes appartiennent à son futur lot ; Mochi dispose déjà de sa cadence native vérifiée.

Les profils précédents et les JAR de référence sont conservés. Aucun monde/mod habituel ni VOID n'est modifié. Le profil manuel final est neuf et non lancé à la livraison ; son chemin exact et le hash sont enregistrés dans `build/b03-alpha12-verification.local.json`.
