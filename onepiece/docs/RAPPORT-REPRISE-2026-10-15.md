> Historique du 15 octobre. État courant et priorité Mera : [rapport du 16 octobre](MERA-MERA-2026-10-16.md).

# One Piece — reprise du socle, 15 octobre 2026

L'alpha privée `1.0.1-alpha.1` est compilée et testée dans des instances jetables. Une cause commune des effets incohérents est corrigée : les 42 particules lisaient l'atlas des blocs au lieu de celui des particules. Les captures avant/après confirment le changement sur Shigan. L'objectif des 40 fruits entièrement fonctionnels, avec leurs nouvelles formes et tous leurs effets fidèles, reste ouvert.

JAR testé : `onepiece/build/libs/opworldlike-1.0.1-alpha.1.jar` ; SHA256 `35e3c14f1c9d815fff2d8d812b13275c5f8bdb7a2a109a75c928b8fcb41faf66`. Les 252 sources Java compilent sur Java 25, Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, GeckoLib 5.5.7 et PAL 1.2.8.

## Ce qui a changé

| Domaine | Correction |
|---|---|
| Particules | Atlas correct pour les 42 types ; cadence cyclique des sprites conforme à la formule de la source. |
| Affichage des projectiles | 19 profils explicites, dont les références transparentes ; White Launcher passe aussi par son profil de fumée. Les lasers et fantômes n'héritent plus de l'affichage en feu des boules de feu. Les profils nouveaux restent à accepter visuellement individuellement. |
| Animation | Les contrôleurs Gecko continuent leur séquence ; état de transformation par UUID et poses conservées pour le rendu différé ; diffusion aux joueurs suivis. |
| Combat commun | Garde de dégâts équilibrée en `finally`, source élémentaire avec propriétaire/projectile, recul orienté, conséquences offensives après un impact réellement accepté et consommant vie/absorption. |
| Déclenchement | Refus personnalisé distinct d'un repli générique ; cinq identifiants de techniques corrigés ; énergie rendue au refus/à l'exception. Une attaque effectuée à vide ne devient pas un déplacement gratuit. |
| Combos Zoan | Coups espacés pour respecter la fenêtre d'invincibilité vanilla ; total des dégâts conservé ; un coût ; interruption au changement de fruit, mort, suppression, déconnexion ou monde. Les projectiles ne sont plus absorbés par ce routage de mêlée. |
| Riki | Hado Elbow saisit les cibles de son unique impact, au lieu de tenter une seconde frappe bloquée par l'invincibilité. |
| Formes | Vitesse à +20 % plutôt qu'une addition de 0,2 ; attributs réappliqués sans cumul à la reconnexion ; durée des formes maintenues sauvegardée ; coût de maintien corrigé ; expiration révoquant réellement le corps agrandi. |
| Nettoyage | Retrait des états détenus : poursuite, diamant, défense de glace, contrôle distant, prise Riki, invocations Soru, domaine Uta et lames Supa, en plus du nettoyage préexistant. |
| Collisions | Les fantômes tracent les entités au-delà des murs ; les rayons sont limités avant le calcul d'impact. Les rebonds Goro respectent les murs et ne comptent pas une cible invulnérable comme touchée. |
| Persistance | Fichiers atomiques dans `data/opworldlike` du monde ; mémoire vidée entre serveurs ; refus d'écraser un fichier illisible ou de schéma plus récent. Aucune copie silencieuse de l'ancien fichier global vers tous les mondes. |
| Ressources | Deux rotations NaN neutralisées dans Marco ; son de garde absent remplacé par le son vanilla de bouclier ; six UV de Murakumogiri remis dans son atlas, après erreur réellement observée au chargement client. |

Les entités temporaires de combat ne sont pas sauvegardées comme créatures permanentes : elles perdaient leur configuration de pouvoir au rechargement. Le projectile reçoit une mise à jour réseau par tick. Les commandes destructrices des harnais n'existent qu'avec l'option explicite `opworldlike.repairTests`.

## Vérifications réellement exécutées

| Vérification | Résultat | Limite |
|---|---|---|
| Scénarios ciblés | **59/59** | Victimes ordinaires ; FakePlayer uniquement lanceur. Plusieurs contrôles de forme appellent leur service directement ; impacts, combos et traversées attendent des ticks réels. |
| Scénarios historiques relancés localement | **307/307 identifiants distincts** | C1 couvre 213 lancements préparés avec cibles et préconditions ; il ne certifie pas 213 effets complets. |
| Redémarrage, expiration, loot | Réussis | Monde jetable, loot forcé à 100 % seulement dans ce laboratoire. |
| Helpers de garde/dégâts | **128 assertions** | Vérification autonome des politiques ; exécutée avant les derniers changements graphiques. |
| Bytecode client et pack présent | **17 classes chargées**, états indépendants et 19 profils contrôlés | Aucun chargement de classe ne remplace une recette graphique. VOID DYNASTY présent dans ce contrôle sans monde. |
| Ressources dans le JAR final | **616 JSON, 82 paires de visuels, 0 erreur bloquante** | Quatre avertissements UV sur des faces dégénérées d'Abyss restent signalés. Pas une certification artistique de tous les modèles. |
| Client graphique | **9 captures, 4 inspectées dans la dernière série** | Trois scènes partielles : Léopard hybride/Shigan, Laser Pika, Sango Goro. Aucun fruit complètement accepté. |

Preuve serveur : `C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-0fe26ac800f7469d97747a4d0ee0b0d1/report.local.json`.

Preuve graphique : `C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-ad5491e5587044b7a43bcc3ae22126d6/visual-lab.local.json`. Les processus de cette série ont été arrêtés normalement. Les journaux signalent les refus attendus des services de compte/Realms avec l'identité locale de test et les avertissements Windows Perflib ; le rapport ne prétend pas à un démarrage sans aucun avertissement système.

Avant correction de l'atlas :

![Shigan avant correction](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-5211061d5189472aa5dc422e48546511/client/screenshots/02-leopard-technique.png)

Après correction, avec changement de pose du modèle hybride :

![Shigan après correction](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-ad5491e5587044b7a43bcc3ae22126d6/client/screenshots/02-leopard-technique.png)

La vue de côté confirme un rayon Pika sans flamme parasite ; la grande étoile lumineuse de la ressource source domine encore l'image. Sango montre des sprites électriques autour des cibles ; leur palette rose/violette d'origine n'a pas été recolorée. Ces aspects restent à affiner selon la recette par technique.

## Tester et continuer

[Tester-OnePiece.cmd](C:/Users/Anwender/Documents/ChatGPT/codexmod/onepiece/Tester-OnePiece.cmd) ouvre un nouveau laboratoire manuel préparé, avec monde et progression de test. Fermer sa fenêtre Minecraft arrête son serveur. R : technique ; B : suivante ; V : forme ; F5 : vue. Le JAR n'est pas installé dans les mods habituels. Les deux JAR VOID DYNASTY ont encore leurs hashes installés précédents, vérifiés pendant cette reprise.

Restent notamment : recettes complètes des 213 techniques, première personne et observateur réel, conditions/résistances de chaque fruit, cycles mort/reconnexion/changement de monde avec vrais joueurs, tous les profils graphiques, créations des formes animales complètes et des Gears. L'utilisateur a choisi l'anime/manga pour les formes manquantes. L'utilisateur a confirmé un départ de zéro dans le monde de test : aucune ancienne progression ne sera importée dans ce profil. Aucun monde personnel n'a été migré ou réinitialisé.

La matrice conserve **0/40 fruits entièrement acceptés**. Elle distingue maintenant les lancements locaux, les quelques scénarios de dégâts et les scènes graphiques partielles. Le prochain lot est une recette complète du Léopard, puis Pika et Goro, et la création des modèles/animations absents.
