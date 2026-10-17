# Compatibilité des mods installés — 15 octobre 2026

**Les six JAR peuvent se charger ensemble sur l'installation actuelle. Des interactions de combat et des défauts de ressources restent présents.** Aucun JAR, réglage ou monde n'a été modifié pendant cet audit.

## Versions examinées

Instance : dossier `mods` de l'installation Minecraft officielle. Profil observé dans le journal : Minecraft 26.3, Fabric Loader 0.19.5, Java 25.

| JAR | Version interne | Résultat des dépendances |
|---|---|---|
| fabric-api-0.162.0+26.3.jar | 0.162.0+26.3 | Satisfaites |
| geckolib-fabric-26.3-5.5.7.jar | 5.5.7 | Satisfaites |
| playeranimlib-26.3-1.2.8.jar | 1.2.8+mc.26.3 | Satisfaites |
| opworldlike-1.0.0.jar | 1.0.0 | Satisfaites |
| voiddynasty-1.0.0.jar | 1.0.0 | Satisfaites |
| voiddynasty-bridge-diagnostic-0.2.0.jar | **0.7.0** | Satisfaites |

Le nom ancien du compagnon n'indique pas sa version chargée. Fabric confirme bien 0.7.0. OP World Like dépend de GeckoLib et Player Animation Library ; ces deux bibliothèques sont présentes. VOID DYNASTY dépend aussi de GeckoLib. Une seule copie partagée convient aux deux mods.

## Contrôles effectués

- Intégrité ZIP/CRC des six archives et de leurs archives imbriquées : **54 archives valides**.
- **136 contraintes** `depends`, `breaks` et `conflicts` évaluées avec le vrai `VersionPredicate` de Fabric : **aucun échec**. Les versions de Minecraft, Java et Fabric correspondent au journal de cette session.
- Aucun identifiant de mod dupliqué et aucune classe Java dupliquée dans les archives analysées. Aucune collision de chemins de ressources racine `assets/` ou `data/` entre ces archives. Les classes de variantes Java multi-release et `module-info` sont exclues du contrôle de doublons.
- Test Fabric isolé avec les six JAR copiés dans un dossier temporaire : **16 classes sensibles chargées avec leurs transformations**, dont les menus/coffres/armes du compagnon, LivingEntity, Level, AvatarRenderer, PlayerModel, les inventaires et le renderer de première personne. Le nom obsolète ItemInHandRenderer dans le premier essai du harnais a été remplacé par le nom vérifié FirstPersonHandsAndItemsRenderer ; cet échec initial appartenait au harnais.
- Le test isolé n'exécute pas les points d'entrée des mods, ne lance pas Minecraft.main, n'ouvre pas de fenêtre et ne charge aucun monde. Il valide la résolution et les transformations, pas le comportement complet en partie.
- Le vrai journal de 16:33:38 à 16:35:48 montre les six JAR chargés, une partie solo démarrée et un arrêt normal. Aucun échec de Mixin ni entrée FATAL n'y figure. Le pack `voiddynasty_bridge:refit` est chargé.

## Interaction de combat confirmée dans le code

Le calculateur `DamageCalculator.applyDirect` de VOID DYNASTY crée une source `playerAttack`. Le laser corrigé utilise ce calculateur. De son côté, OP World Like injecte un multiplicateur dans `LivingEntity.hurtServer`, via `DevilFruitPowerLogic.computeDamageMagnification`.

Cette logique traite `playerAttack` comme une attaque physique. Pour une cible ayant un fruit valide, elle peut notamment :

- appliquer un multiplicateur nul contre un Logia non neutralisé, sans Haki chez l'attaquant ;
- ramener les dégâts physiques contre Gomu à un multiplicateur de 0,0001 sans Haki ;
- absorber certains coups avec les conditions Yami, ou amplifier d'autres dégâts.

**Conséquence : un laser visible peut toucher sans produire les dégâts attendus sur ces cibles.** C'est une interaction entre les règles des deux mods, indépendante de l'ancien défaut d'affichage du rayon. Les conditions sont confirmées par inspection du bytecode installé ; aucun tir réel comparatif n'a été exécuté pendant cet audit. Le choix d'une règle commune pour les armes énergétiques reste à implémenter/valider, sans supprimer les résistances de tous les fruits par défaut.

## Défauts constatés dans OP World Like

| Constat | Preuve locale | Effet attendu |
|---|---|---|
| Animation Marco invalide | `marco_humanbeast.animation.json`, animation `sword_to_left`, deux occurrences de `nan` dans la ressource ; erreur de compilation GeckoLib dans le journal | Animation concernée susceptible de manquer ou d'être ignorée |
| Modèle Murakumogiri invalide | `minepiece:murakumogiri` : coordonnées hors texture, zone `[44, 0, 65, 23]` pour une image 64×64 | Échec de préparation du modèle de cet objet |
| Son de garde absent | Référence `minepiece:sounds/guard.ogg` introuvable | Son de garde manquant |
| Blocages de dégâts récursifs | 20 messages `Blocked recursive combat damage`, sur des chauves-souris avec feu/lave | Le garde d'OP World Like annule certains appels de dégâts ; origine de la répétition à isoler |

Ces trois erreurs de ressources appartiennent au namespace MinePiece livré dans OP World Like. Elles ne démontrent pas une incompatibilité de version de GeckoLib et ne justifient pas, à elles seules, de remplacer la bibliothèque.

Le garde de combat s'enregistre globalement sur ALLOW_DAMAGE/AFTER_DAMAGE. Ses messages confirment une suppression de dégâts, mais le journal observé ne prouve pas qu'il a annulé un tir VOID DYNASTY. Son suivi de pile et les chemins de sortie sans AFTER_DAMAGE méritent un test ciblé avant toute modification.

Autres avertissements : 35 techniques partagent un visuel d'après le diagnostic MinePiece ; un post-effet `minecraft:end_of_frame` est demandé mais absent. L'auteur du post-effet n'a pas été établi par cet audit. Aucun de ces messages n'est une preuve de conflit de classes entre les JAR.

## Touches et autres zones de contact

Le rechargement VOID DYNASTY et la technique MinePiece utilisent tous deux **R par défaut**. Dans les options actuellement sauvegardées, la technique MinePiece est déjà sur **Flèche haut**, la transformation sur **Flèche bas**, et le cycle sur **B** : la collision de R est donc évitée dans cette configuration. Le rechargement original n'est pas enregistré avec KeyMappingHelper dans son initialiseur et n'apparaît pas parmi les touches personnalisées sauvegardées ; sa configuration mérite d'être améliorée.

Les protections de blocs OP World Like inspectées ciblent les piles d'appels de capacités MinePiece/addons dans les zones protégées. Aucune condition visant explicitement VOID DYNASTY n'a été trouvée dans ce garde. Cela ne constitue pas un essai réel de déploiement d'un vaisseau dans une île revendiquée. Le journal montre aussi la configuration de frontière et de point d'apparition par OP World Like sur le monde chargé : ces règles de monde restent à prendre en compte pour les déplacements.

Les bibliothèques GeckoLib et Player Animation Library modifient plusieurs classes de rendu communes, mais le test combiné n'a trouvé aucun échec de transformation sur les classes contrôlées. Les animations d'armes, transformations et équipements combinés restent à observer en jeu.

## Priorités résultantes

1. Définir et tester les dégâts des armes VOID DYNASTY contre les résistances MinePiece, avec une cible ordinaire comme témoin.
2. Corriger les trois ressources OP World Like identifiées : animation Marco, modèle Murakumogiri et son de garde.
3. Reproduire les blocages de dégâts récursifs sur un monde de test et vérifier la libération du garde après chaque tentative.
4. Rendre le rechargement VOID DYNASTY configurable et tester les animations/transferts/pad avec les six mods actifs.

Les preuves détaillées privées sont conservées dans `.runtime/compatibility/` : inventaire/empreintes, résultat des contraintes, test combiné et copie du journal. Les sorties d'inspection dérivées des JAR restent dans le dossier temporaire de recherche, hors du dépôt.
