# Magu Magu — correction des animations, 16 octobre 2026

La remarque utilisateur était fondée : les charges de Dai Funka et Inugami partageaient energy_charge ; le lâcher rejouait cette charge au lieu du geste de tir. Kagero reprenait exactement les colonnes de Volcanic. Ryusei levait les bras seulement cinq ticks, alors que sa salve ne commençait qu'au tick20.

La version **1.0.1-alpha.4** donne des gestes et effets distincts aux cinq techniques. Les nouvelles poses sont une adaptation visuelle demandée, et non une reproduction exacte des pistes ver15 ; les dégâts/coûts sont conservés.

| Technique | Geste et effet actuels |
|---|---|
| 1 — Dai Funka | Poing droit ramené, charge autour de la main, frappe droite au lâcher |
| 2 — Inugami Guren | Bras droit soutenu par le gauche, silhouette canine de charge, projection avec torsion du torse |
| 3 — Kagero | Corps incliné pendant la course, finition basse avec sillons au sol, sans colonnes de Volcanic |
| 4 — Volcanic Explosion | Position basse, ouverture des bras et éruption verticale à couronne large |
| 5 — Ryusei Kazan | Préparation au-dessus du personnage, bras levés puis frappes alternées vers le ciel pendant la salve |

Deux animations différentes sont utilisées pour charge/lâcher et dash/finition. Les charges sont bornées à5,1secondes, les deux lâchers à0,65/0,75seconde et les gestes maintenus àenviron2secondes. L'annulation remet la pose àzéro ; elle n'écrase pas le tir normal. Le visuel de finition Kagero possède une nouvelle entité d'effet, avec son propre âge de phase.

## Essai

Fermer l'ancienne fenêtre Minecraft de test puis relancer [Tester-Magu.cmd](C:/Users/Anwender/Documents/ChatGPT/codexmod/onepiece/Tester-Magu.cmd). Une ancienne fenêtre conserve son ancien JAR en mémoire. Le nouveau profil est neuf et séparé, sans import de progression. R maintenir/relâcher pour les tirs, B suivante, F5 caméra. Les mondes et mods habituels restent inchangés ; le profil Mera figé reste disponible.

## Vérification du JAR livré

SHA256 `5aa804501710b0ea91ba8cf7e2c0372d373a189f059a5904d1e7635f732a9a5a`,258sources Java. Serveur [run-7453dcc7f5954a589348388cc81fab05](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-7453dcc7f5954a589348388cc81fab05/report.local.json) : **74Magu +92Mera +59socle +307historiques PASS**, loot et redémarrage réussis.

Le [laboratoire graphique](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-b20aff3d24724b648dbe1897e065a7e4/visual-lab.local.json) a ouvert deux vrais clients et produit45captures acteur et30observateur. Douze images ont été réellement ouvertes : huit vues observateur (charges et tirs, dash et finition, Volcanic, Ryusei) et quatre vues acteur (deux tirs et préparations Volcanic/Ryusei). Les rendus montrent des poses/effets distincts ; leur finition artistique reste à apprécier en jouant.

Le nouveau contrôle PAL constate **8/8 gestes chez l'acteur et8/8 chez l'observateur** : UUID de l'animation attendue, couche active, temps qui avance entre deux échantillons et transformation non neutre des bras/corps réellement évaluée. Ce contrôle dépasse la simple réception d'un paquet. L'[audit des huit fichiers](C:/Users/Anwender/Documents/ChatGPT/codexmod/onepiece/build/magu-animation-audit.local.json) vérifie aussi des pistes d'os différentes, des durées bornées et des vecteurs finis ; un même fichier renommé échouerait.

Chargement client17cibles PASS ; ressources625JSON/83paires,0erreur et4anciens avertissements UV Abyss. Tous les processus automatiques ont été arrêtés. Profil manuel `run-08a899ea88f343f0887b512d62505deb` préparé sans lancement avec exactement ce JAR.

## Limites conservées

L'appréciation artistique et l'équilibre humain restent ouverts. Les variantes source Dai accroupi et Kagero prolongé, ainsi que les cycles réels de mort/reconnexion et le multijoueur ordinaire, ne sont pas ajoutés par ce correctif. Aucune acceptation complète des40fruits n'est déclarée. Le [contrat Magu](fruits/MAGU_MAGU.md) reste la référence des adaptations de gameplay.
