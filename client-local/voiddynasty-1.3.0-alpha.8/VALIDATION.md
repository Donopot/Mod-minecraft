# Validation du JAR unifié — 19 octobre 2026

- Compilation complète : **249 sources Java, 864 ressources**, ancien JAR principal et ancien bridge exclus du classpath.
- Deux builds indépendants depuis des classes neuves : même SHA-256, JAR déterministe.
- Une seule identité de mod VOID DYNASTY (`voiddynasty`), aucun chemin ZIP dupliqué, aucune classe Minecraft ou classe de test embarquée.
- **24 tests Python MCP** réussis.
- **46 contrôles natifs** de conservation, inventaires, codecs et rollback réussis.
- Vrai chargeur client Fabric/Knot : **7 classes transformées**, pack refit enregistré depuis le mod principal, **30 assertions** d'inventaire et de pad réussies. Ancien bridge installé en plus : refus attendu vérifié.
- **57 modèles refit** : textures, parents, variantes de blocs et bornes vérifiés.
- Harness Java du bridge compilé contre le JAR unique : journal de livraison, ancienne sauvegarde, réponse perdue, réconciliation, rejets de réponses invalides et empreintes des **14 missions** vérifiés.
- Serveur neuf, boucle locale, sans bridge séparé : sélections vides, dépôt automatique, inventaire plein, ressources inaccessibles, livraisons MCP, annulation, réserve de matériaux, plateforme et maison, rollback, altération extérieure, escaliers et redémarrages réels : toutes les phases passent.
- Récupération alpha 7 : mort réelle du compagnon, restitution une seule fois, sac plein sans débit partiel, gestionnaire de migration entre dimensions, registre persistant après arrêt/redémarrage : toutes les phases passent. Les états initiaux des fixtures ont été corrigés pour ne pas mélanger les scénarios.

Les preuves par phase et l'empreinte commune des JAR testés figurent dans `validation-results.json`. Les tests utilisent des mondes jetables, aucune sauvegarde du joueur.

Limites : pas de recette visuelle humaine ni de fenêtre Minecraft lancée pour la vérification client ; pas de nouveau test complet Minecraft + Rogue Trader ; pas de test de modèle réel ou de transport distant Hermes. La fixture de dimension appelle le gestionnaire Fabric, pas une traversée réelle de portail. Gradle n'a pas été lancé sur ce poste : le build Java local complet et le vrai chargement Fabric ont été vérifiés. L'ancien assistant 0.7.1 reste destiné aux distributions séparées.
