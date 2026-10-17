# Servitor 1.2.4 — conservation, rollback et déplacements

Cette version traite d'abord les pertes et duplications observées dans le socle 1.2.3, puis remplace le déplacement case par case par la navigation native. Le document de conception AGENT-CHANTIER décrit aussi des objectifs futurs ; il ne constitue pas une liste de fonctions déjà livrées.

## Ressources

Le sac conserve des `ItemStack` complets : type, quantité, composants, nom et usure. L'insertion est simulée sur une copie avant la casse. Elle respecte les piles compatibles et les limites propres aux objets, y compris 16 et 1. Si tout le butin ne rentre pas, le bloc reste en place et le servitor repart au dépôt. Un dépôt plein conserve le reste dans le sac ; chaque nouveau dépôt est enregistré.

Chaque bloc effectivement cassé reçoit un reçu contenant son état original et ses drops exacts. Une mutation refusée ne crée ni drops ni reçu. Un outil inadapté provoque un arrêt avant la casse, afin de ne pas gaspiller les minerais. La machine ne consomme plus une pioche issue du butin journalisé : les outils restent une fourniture d'opérateur via `/work tool`.

Un nouveau `/work start` ne peut pas écraser un chantier, son sac ou son journal. Le journal ne supprime plus ses anciennes entrées lorsqu'il atteint 50 000 blocs : le chantier s'arrête. La lecture des cibles examine au plus 64 positions par tick et attend une reprise explicite si les chunks nécessaires sont déchargés.

## Rollback

`/work rollback [n]` arrête le servitor et traite un bloc par tick, du plus récent au plus ancien. Pour chaque bloc, il prépare le prélèvement exact dans le sac et les dépôts connus. Les composants doivent correspondre. Il restaure uniquement une position encore vide et non protégée. Le reçu disparaît seulement après une restauration réussie et le prélèvement correspondant.

Une ressource retirée du dépôt doit être remise dans un dépôt suivi avant de réessayer le rollback. Les objets d'autres types et les piles aux composants différents sont préservés. Des objets strictement identiques sont interchangeables : le suivi garantit les quantités et composants, pas l'identité historique d'un exemplaire.

Si une ressource manque, si un bloc a été modifié, si un chunk requis est déchargé ou si la protection refuse l'action, le rollback s'arrête en conservant le reçu concerné. Les blocs déjà restaurés restent restaurés. Un ancien journal 1.2.3 ne possède pas les reçus de drops nécessaires : son rollback est refusé, sans vider le coffre.

Après le début d'un rollback, la reprise du minage est désactivée pour éviter de reminer les blocs restaurés. Le sac restant peut toujours être vidé avec `/work deposit`. `/work commit` libère explicitement l'historique et permet un nouveau chantier ; il exige un chantier arrêté, un rollback inactif et un sac vide. Les dépôts et leur contenu restent dans le monde.

## Navigation

Le servitor utilise `PathNavigation` et ses contrôles de déplacement natifs. Ses objectifs de combat et d'errance sont désactivés uniquement lorsqu'il est marqué comme ouvrier. Il cherche une position avec sol, espace pour son volume, absence de fluide et ligne de vue vers la cible. Il doit arriver à portée avant de miner ou de déposer. Les dépôts sont placés sur un sol supporté et restent séparés des coffres voisins.

Une recherche de chemin peut contourner un obstacle en s'éloignant temporairement de la cible. Si le trajet reste bloqué pendant 400 ticks sans déplacement ou progression de minage, le chantier s'arrête et conserve son sac. Les déplacements ne téléportent plus le servitor ; seule sa création initiale place l'entité au départ.

Il ne construit pas encore d'escalier, de pont ni d'échafaudage. Une excavation profonde peut donc devenir inaccessible et demander l'aménagement d'un accès. Les boucles d'exécution et la gestion de la mort utilisent désormais la dimension du chantier ; la recette de cette version porte sur l'Overworld.

## Commandes

| Commande | Effet |
|---|---|
| `/work start <nom> <coin1> <coin2>` | Nouveau chantier, uniquement après libération du précédent. |
| `/work status` | État actif ou arrêté, sac, dépôts, journal et progression du rollback. |
| `/work stop` | Arrêt sans effacer les ressources. |
| `/work resume` | Reprise explicite après pause ou redémarrage propre. |
| `/work deposit` | Retour au dépôt uniquement, sans nouvelle casse. |
| `/work tool <item>` | Fourniture d'un outil par un opérateur ; ce n'est pas encore une économie de survie. |
| `/work rollback [n]` | Annulation contrôlée avec récupération des drops correspondants. |
| `/work commit` | Abandon explicite de l'historique, sac vide requis. |

## Sauvegardes et limites

Le format v2 utilise les codecs d'objets de Minecraft avec leur contexte de registres. Les anciennes piles `id|count` sont migrées lorsqu'elles sont résolubles ; les composants déjà perdus par l'ancien format ne peuvent pas être inventés. Un payload NBT incompatible est conservé tel quel et verrouille les commandes de mutation, au lieu de devenir un chantier vide.

Après rechargement, le minage et le rollback attendent une commande explicite. La recette valide un arrêt propre et le rechargement depuis disque. **L'état du monde, les conteneurs et le SavedData ne forment pas encore une transaction disque atomique** : une coupure brutale pendant leurs écritures n'est pas couverte. Le rollback concerne les blocs directement journalisés ; il ne rejoue pas toute la physique secondaire du monde et ne rembourse pas l'usure des outils.

## Recette du 17 octobre 2026

58 assertions : 24 contrôles natifs d'inventaire/codecs/transactions et 34 contrôles dans un serveur Fabric 26.3 jetable, avec Fabric API, GeckoLib et le bridge 0.7.1 chargés. Scénarios : sac plein, limites 1/16/64, composants, rollback partiel sur deux dépôts et sac, retrait de ressources, modification du terrain, ancien journal, refus d'écrasement, mauvais outil, protection, dépôt plein, contournement d'un mur, retour au dépôt, trajet impossible, retour du sac seul et redémarrage disque. Le mod de test est séparé et n'est pas inclus dans le JAR jouable.

Les outils `tools/run_work_tests.py` et `tools/run_work_lab.py` utilisent un candidat explicite, un JDK 25 et un fichier JSON local contenant un tableau `classpath` avec les dépendances installées. Le laboratoire exige aussi le chemin de Minecraft, le dossier des dépendances et l'acceptation explicite de l'EULA. Il crée un monde neuf accessible sur `127.0.0.1` et ne termine que son propre processus enfant.

```powershell
python tools/run_work_tests.py --candidate <jar> --classpath-json <config-locale.json> --jdk <jdk25> --output <sortie-tests>
python tools/run_work_lab.py --candidate <jar> --classpath-json <config-locale.json> --jdk <jdk25> --minecraft <instance> --dependencies <dossier-des-jar> --output <laboratoire-neuf> --accept-eula
```

La compilation locale des 191 sources utilise `javac --release 25`, les dépendances installées et les en-têtes élargis pour les accès déclarés dans `voiddynasty.accesswidener`. Le JAR embarque cette déclaration et sa licence. Le wrapper Gradle reste la voie standard de reconstruction ; une reconstruction Gradle depuis un cache vierge et une recette visuelle du client ne sont pas validées par ces essais serveur.
