# Servitor — 1.3.0-alpha.94

Cette mise à jour vise Minecraft Java 26.3, Java 25 et Fabric Loader 0.19.5, avec Fabric API 0.162.0+26.3 et GeckoLib 5.5.7. Elle corrige l'exécution déterministe des ordres et le suivi des routines.

## Installer

Arrêter Minecraft ou le serveur. Dans `mods`, remplacer uniquement l'ancien `voiddynasty-*.jar` par `voiddynasty-1.3.0-alpha.94.jar`, en gardant un seul JAR du mod principal. Conserver Fabric API, GeckoLib et les autres mods nécessaires au pack. Redémarrer.

Les tests de cette version utilisent des mondes jetables. Ils ne modifient aucune sauvegarde de jeu existante.

## Ce qui change

- Chaque tâche réactive le déplacement du compagnon et entretient son chunk pendant l'exécution, y compris après un rechargement de l'entité. L'arrêt, l'échec et le remplacement d'une tâche libèrent ses tickets.
- Les tickets de chunks sont propres à chaque dimension et propriétaire. Le déplacement, le chantier et le retour peuvent partager un chunk sans se retirer mutuellement leur ticket. Ils expirent et ne sont pas persistés comme des chunks forcés permanents.
- Une routine distingue la récolte, le retour en cours et l'arrivée confirmée. Une source épuisée ou une mission annulée produit son résultat réel. Une erreur ne laisse plus le suivi attendre indéfiniment un succès impossible.
- Les routines sont sauvegardées avec le monde. Après redémarrage elles affichent `PAUSED / SERVER_RESTART`, puis attendent une reprise explicite. Une routine en erreur reste consultable.
- Un chantier classique avec des objets en attente construit son dépôt avant de vider le sac, au lieu de boucler en fin de chantier.
- Les notes affichent leur dimension et leur date. Un fichier corrompu est conservé et bloque les ajouts suivants ; l'écriture passe par un remplacement atomique. Lire les notes ne consomme plus la consigne en attente du cerveau.
- Un plafond de gravier est refusé même lorsque la cellule de passage située dessous est déjà de l'air.

## Commandes utiles

```text
/servitor routine list
/servitor routine resume <id-mission>
/servitor routine cancel <id-mission>
/servitor stop
```

La reprise exige que le compagnon soit vivant et chargé dans la même dimension : se rapprocher de sa dernière position si nécessaire. Annuler le suivi met le chantier associé en pause et conserve les ressources. Le journal du chantier reste disponible pour la réconciliation et le rollback habituels ; terminer une routine n'efface pas ces reçus.

## Compilation et tests

Depuis `voiddynasty`, copier le GeckoLib du pack versionné dans `libs`, puis lancer `./gradlew build` avec Java 25. Le workflow GitHub est désormais à la racine du dépôt ; `check` exécute réellement `WorkConservationTest`, en plus de compiler les fixtures serveur. Les fixtures HUD couplées à One Piece utilisent le laboratoire HUD existant ou la propriété facultative `-PonepieceTestJar=<mod-onepiece.jar>`.

Une compilation locale avec des dépendances déjà installées est aussi possible :

```text
python voiddynasty/tools/build_local.py --classpath-json <dependances.local.json> --jdk <jdk25> --output <sortie>
python voiddynasty/tools/run_work_tests.py --candidate <mod.jar> --classpath-json <sortie/build.local.json> --jdk <jdk25> --output <tests>
python -m unittest discover -s voiddynasty/tools/tests -v
```

Le fichier des dépendances contient un tableau JSON `classpath` de chemins absolus vers les JAR Minecraft, Fabric, GeckoLib et leurs bibliothèques. Le builder exclut les anciennes classes du mod et crée une vue d'accès réservée à la compilation ; aucune classe Minecraft n'est incluse dans le mod livré.

`run_work_lab.py --reliability-only` couvre le réveil après `NoAI`, le déplacement sans joueur, les erreurs de tâche, les missions incomplètes, la récolte/dépôt/retour, les notes corrompues et la reprise après redémarrage. `--recovery-only` vérifie les sacs après mort, refus d'insertion dans un sac plein et redémarrage. Le lancement exige les chemins explicites de dépendances et `--accept-eula` après acceptation de l'EULA Minecraft.

## Limites

La mémoire des routines est écrite par les sauvegardes normales du monde. Les tests de redémarrage sont des arrêts propres ; cette version ne garantit pas une transaction atomique de tous les inventaires lors d'une coupure brutale du processus ou du disque. Les essais du moteur n'utilisent aucun modèle de langage distant et ne certifient pas les réponses d'un modèle particulier.

Le laboratoire de navigation valide les sols dangereux, les conteneurs, les fluides, les plafonds de gravier, le refus d'une zone trop étroite, la montée d'un escalier existant et son rollback. Il échoue encore sur le creusement d'un escalier montant (`STAIR_NO_ROUTE`, aucun bloc modifié). La suite complète de navigation n'est donc pas déclarée réussie et les phases suivantes de cette suite ne sont pas certifiées par ce lancement.

## Vérifications du 21 octobre 2026

| Vérification | Résultat |
|---|---|
| `gradlew build` (Java 25, Loom 1.18.4) | Réussi ; 46 assertions de conservation exécutées |
| Outils Python de mission/observation | 24 tests réussis |
| Serveur, routine et mémoire | Récolte de 3 blocs, dépôt exact, retour, source vide, notes corrompues, reprise après arrêt propre |
| Serveur, récupération | Mort, restitution unique, sac plein, transfert de stockage de dimension et redémarrage |
| Serveur et MCP réel | 170 assertions natives et 8 vérifications MCP réussies ; livraison, rollback et déplacement |
| Navigation verticale complète | Échec connu au creusement montant ; voir la limite ci-dessus |

La vérification du stockage entre dimensions appelle le gestionnaire de changement de dimension ; elle ne simule pas une traversée de portail. Les essais serveur ne valident pas le rendu graphique du client.
