# Servitor IA — intégration alpha 6

Cette version raccorde le cerveau ajouté dans `servitor-ia` au moteur déterministe `work/`. Elle poursuit la vision de `SERVITOR-VISION.md` avec la conservation des ressources comme priorité. La vision reste une cible : toutes ses fonctions ne sont pas réalisées.

## Comportement livré

- Le cerveau propose `work_submit` pour une mission native GATHER ou BUILD, consulte `work_status` et utilise `work_control` pour pause, resume, cancel ou deposit. `work_capabilities` fournit les ressources et plans réellement disponibles.
- Le chantier emprunte le servitor persistant déjà présent, y compris depuis les commandes natives et MCP. Une tâche du compagnon empêche cette prise de contrôle. Pendant le chantier, les ordres concurrents de déplacement, combat et ramassage sont refusés. Pause, reprise, fin et fermeture conservent le même compagnon ; une absence ne crée pas silencieusement un remplaçant.
- Le minage IA séparé (`mine_area`, `dig_box`, `mine_block`) et son escalier par téléportation sont retirés. La récolte passe par les missions : portée, outil, durée, conteneurs, claims, inventaire, accès autorisés et reçus de rollback sont ceux de `work/`. `goto` et `come_to_player` marchent sans casser de murs. Une navigation bloquée peut échouer ; ce refus est préférable à une destruction non autorisée.
- Le sac personnel de 27 emplacements est enregistré dans les données du monde et de la dimension, par UUID, avec les composants des objets. Il reste distinct du sac de chantier possédant des reçus. Un ramassage qui ne tient pas est refusé intégralement ; une collecte demandée de trois objets laisse le surplus au sol. Le ramassage implicite pendant les autres tâches est retiré.
- Les paramètres manquants, booléens à la place d'entiers, nombres fractionnaires, propriétés inconnues, blocs inconnus et valeurs hors limites sont refusés côté serveur, même si l'appel ne vient pas du modèle. Le chat de commande est réservé aux opérateurs, comme `/servitor`.
- Un nouveau cerveau utilise le mode « ordre » par défaut. Le désactiver ou utiliser `/servitor stop` invalide les réponses HTTP encore en vol ; une nouvelle consigne invalide la proposition précédente. Éteindre le cerveau laisse continuer une tâche déjà acceptée, comme indiqué par la commande existante.
- Le modèle ne dispose pas d'une action `close` ni `rollback` : la libération du journal et la restauration restent des actions opérateur. `/work mission control <UUID> rollback` conserve les vérifications natives. `/work mission control <UUID> close` abandonne explicitement l'historique après réconciliation.

Le démarrage d'un chantier refuse une main principale déjà occupée pour ne pas écraser l'objet d'un compagnon. L'outil affiché pendant le travail est une projection de l'outil appartenant à l'état du chantier ; il est retiré lorsque le contrôle est relâché.

## Perception et MCP

Le MCP restreint publie neuf outils : capabilities, gather, build, build_estimate, mission_status, mission_control, observe, find et mission_wait, tous préfixés `servitor_`.

`observe` et `find` lisent au plus 32 768 cellules, sans charger de chunks, déplacer le servitor, lire le contenu des coffres ou modifier le terrain. Les cellules non chargées, protégées ou hors limites sont comptées séparément. Une absence n'est concluante que si toute la zone demandée a été observée. Les positions retournées sont plafonnées et la troncature est explicite. Un minerai observé ne prouve ni un chemin possible ni une quantité de drops.

`mission_wait` observe un UUID fixe pendant au plus 30 secondes demandées, avec un court délai de transport possible. Les états sont pending, paused, settled, attention et unknown. Une attente écoulée ou annulée ne modifie pas la mission. « settled » signifie une issue observée, pas nécessairement un succès : consulter le statut et les compteurs.

Le cerveau local partage les règles de lecture sans chargement et sans divulgation des claims. Son `find` cherche un identifiant de bloc exact dans une tranche verticale de ±4 blocs, avec au plus 24 blocs de rayon horizontal ; il trie réellement toutes les correspondances avant de plafonner les positions. Le MCP `find` cherche les blocs sources d'une ressource récoltable dans une boîte explicite. Ces deux conventions sont distinctes et décrites dans leurs outils.

RCON reconstitue les réponses fragmentées jusqu'à une réponse `list` corrélée. Le marqueur est envoyé après réception du premier fragment, pour éviter la perte d'une seconde requête regroupée dans une même lecture réseau par certaines versions du serveur. Une réponse incomplète n'est jamais présentée comme un résultat valide.

## Utilisation locale

Installer le JAR alpha 6 avec les dépendances Fabric 26.3 déjà utilisées par VOID DYNASTY, dans une instance de test. Remplacer l'ancien JAR principal, conserver les modules compagnons nécessaires à cette instance et éviter deux versions du même mod. Aucun monde personnel ni profil client n'est modifié par les tests de développement.

```
/servitor spawn
/servitor brain mode order
/servitor brain url http://localhost:1234/v1
/servitor brain model <identifiant-du-modele>
/servitor brain on
/servitor brain ask <consigne avec zone, coffre et origine>
/servitor brain status
/servitor brain log
/servitor stop
```

Pour un endpoint distant, la commande existante exige une clé dans la variable d'environnement définie par `apiKeyEnv`. La connexion réelle à Hermes reste à configurer ; les exemples dans `tools/hermes/` ne contiennent pas d'adresse personnelle et ne modifient aucun serveur distant.

Exemple de spécification GATHER acceptée par le moteur, à adapter à un coffre et une zone réellement présents :

```json
{"schema":1,"type":"GATHER","request_id":"fer-base-001","item":"minecraft:raw_iron","amount":10,"min":[10,50,10],"max":[25,65,25],"destination":[5,64,5],"origin":[7,64,5],"access":"excavate"}
```

Le cerveau reçoit ce JSON dans l'argument texte `spec` de `work_submit`. Réutiliser le même `request_id` pour une même demande ; un identifiant réutilisé avec d'autres paramètres est refusé. La boîte autorise aussi les passages d'accès éventuels ; aucune excavation n'est permise à l'extérieur. Demander au joueur les coordonnées et le coffre manquants. La décision du modèle de respecter cette intention reste à évaluer avec le modèle réellement choisi.

## Validation et limites

Le laboratoire `run_work_lab.py --brain-only` construit un monde neuf, un serveur limité à la boucle locale et un mod de fixtures absent du JAR livré. Il teste les refus typés, le sac plein sans mutation partielle, les composants, les lectures non chargées, les réponses MCP longues, l'exclusivité du corps, une pause et une reprise, un vrai arrêt/redémarrage, la livraison et le rollback. Un servitor enfermé doit échouer sans casser son mur ni se téléporter. La suite `--missions` couvre aussi les dépôts, constructions et accès du moteur existant. Les résultats exacts du candidat sont fournis dans le paquet de test.

Ces essais déterministes n'évaluent pas encore la qualité des décisions d'un modèle réel ni la connexion réseau à Hermes. Les animations et les essais prolongés avec joueurs restent à faire. Un redémarrage propre est couvert ; une coupure électrique entre mutations du monde et sauvegarde n'est pas une transaction atomique garantie.

Le prototype conserve le kit opérateur du moteur existant (outil et fournitures de dépôt). Il ne constitue pas encore une économie intégralement survie. La récupération ergonomique du sac d'un compagnon mort ou passé dans une autre dimension reste à développer : les données restent stockées, mais aucun outil de récupération dédié n'est livré ici. La mémoire textuelle `BrainNotes` héritée reste un fichier de configuration global ; ce lot ne la transforme pas encore en mémoire par monde vérifiée et durable. Les tâches simples du compagnon ne sont pas reprises après redémarrage ; les missions natives restent consultables et demandent une reprise explicite.

Prochain lot recommandé, dans cet ordre : récupération des ressources après mort et changement de dimension ; mémoire par monde avec erreurs d'écriture explicites ; routines déterministes « trouver → accéder → récolter → déposer → revenir » ; configuration et essais Hermes ; essais prolongés de navigation et construction avec un joueur.

## Référence étudiée

[mine-ai-mcp](https://github.com/aibengineering/mine-ai-mcp) et ses documents [actions asynchrones](https://github.com/aibengineering/mine-ai-mcp/blob/main/docs/mcp/async-actions.md), [concepts](https://github.com/aibengineering/mine-ai-mcp/blob/main/docs/mcp/concepts.md) et [navigation](https://github.com/aibengineering/mine-ai-mcp/blob/main/docs/navigation/README.md) ont servi de référence : identité stable d'une demande, résultat observé distinct de l'acceptation, attente distincte de l'annulation et propriétaire unique des mouvements. Aucun code ni runtime Mineflayer n'a été importé dans cette intégration native Fabric.
