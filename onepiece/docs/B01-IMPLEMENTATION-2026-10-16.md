# B01 — Hie, Pika, Moku, Suna, Yami

Version candidate `1.0.1-alpha.8`. Ce document décrit l’implémentation ; les résultats par hash sont dans le rapport de validation. Aucun monde ou mod habituel n’est remplacé. Goro alpha.7, Mera alpha.2, Magu alpha.5 et les trois JAR de référence restent conservés.

## Référence et choix du port

La référence mécanique est MinePiece ver15 fourni par l’utilisateur, vérifiée dans les procédures et le bytecode. Les JAR Mine Mine no Mi et ompworld restent en réserve ; aucune mécanique de ces deux versions n’est importée dans ce lot. Les budgets du catalogue porté restent la base. Les durées, limites de terrain, portée explicite des projectiles et services supplémentaires ci-dessous sont des adaptations déclarées, pas une reproduction intégrale des scripts Forge.

### Hie — cinq techniques

| Technique | Comportement actuel |
|---|---|
| Partisan | Charge10ticks, projectile40, modèle `partisan/tex_ice`, portée32blocs et durée80ticks |
| Pheasant Beak | Charge14ticks, oiseau60, modèle `phoenix/tex_ice`, portée32blocs et durée80ticks |
| Ice Spikes | Six pics successifs devant le joueur, frappe20 une fois par cible, arrêt à un mur ; cristaux visuels temporaires |
| Ice Ball | Charge10ticks, frappe15 dirigée dans un cône de16blocs, boule de glace et chemin gelé avec la même visée de libération |
| Ice Age | Charge20ticks, onde progressive2→16blocs, frappe20 par cible, terrain aquatique et effet visuel sur le même centre |

Les pics source sont des entités TREE_ROOT de glace. Le port emploie des grappes cristallines propres et non solides, pour éviter les cages permanentes. Ice Age gèle l’eau, pas la lave ; le chemin peut recouvrir un sol naturel. Le service de blocs temporaires refuse les inventaires, blocs spéciaux indestructibles et cases déjà détenues par un autre effet. Il conserve la restauration conditionnelle existante.

Chaque dégât accepté peut faire monter le gel de25%,50%,100% de ralentissement via un attribut nommé. Plafond100ticks, fenêtre de liberté100ticks ; la potion vanilla de lenteur reste intacte. Le marqueur FROZEN préexistant est préservé. Les brûlures Mera/Magu s’éteignent après l’impact accepté. L’état est lié à l’entité et au niveau, avec le fruit réel du propriétaire pour supporter aussi un projectile réfléchi par Nikyu.

### Pika — huit techniques

| Technique | Comportement actuel |
|---|---|
| Laser | Avertissement10ticks, rayon17, vitesse4, portée40 configurée, collision balayée |
| Laser barrage | Charge20ticks, six rayons24, espacés de2ticks ; le compteur de volée compte les tirs, pas les dégâts acceptés |
| Light Kick | Déplacement sûr jusqu’à3blocs puis frappe17 dans la portée6 du catalogue |
| Yata no Kagami | Déplacement lumineux sûr jusqu’à24blocs par défaut, configuration plafonnée48 ; contribution de vitesse40ticks |
| Yasakani no Magatama | Trois perles16, espacées de4ticks et écartées de7°, portée24 ; configuration1à8 conservée |
| Ama no Murakumo | Véritable sabre natif et modèle source ; attribut dégâts+7, joueur standard8, vitesse1,5attaque/s |
| Speed is Power | Déplacement sûr puis coup15, avec silhouette de coup de pied distincte |
| Passive | Blink8blocs, variante arrière avec Maj ; aucun dégât |

Le sabre remplace l’ancien bonus de force : un seul octroi, mains occupées conservées, inventaire plein refusé avec remboursement. Bail explicite600ticks, retrait/suppression/mort/dimension/déconnexion nettoyés. Seul le stack portant le token créé par ce pouvoir peut être retiré. Un objet jeté se dissipe ; un exemplaire rangé dans un coffre devient invalide lorsqu’il revient dans un inventaire. L’octroi est immédiat au lancement ; le geste fini et l’éclat de création se jouent ensuite. Le matériau de durabilité Minecraft est une adaptation, les dégâts et la vitesse sont rétablis du bytecode ver15.

La vitesse de lumière utilise son propre modificateur : aucune potion tierce supprimée, prolongée ou ressuscitée. Le complément est recalculé par rapport à la potion existante pour ne pas dépasser le niveau demandé. L’ancien helper de bonus Speed is Power est conservé pour compatibilité des tests historiques ; la nouvelle route frappe15, conformément au budget du catalogue. L’ancien enchaînement bonus40ticks/recharge80ticks n’était pas accessible normalement. Tous les mouvements B01 passent par la vérification du corps entier : tête/pieds, fluides, bordure, chunks chargés et protections. Aucune traînée électrique Goro empruntée.

### Moku — quatre techniques

| Technique | Comportement actuel |
|---|---|
| White Blow | Charge10ticks, poing de fumée50 avec doigts et traîne, collision réelle |
| White Vine | Vortex blanc15 ; après impact accepté, poche de fumée rayon2/durée80ticks et capture bornée |
| White Launcher | Déplacement jusqu’à3blocs, frappe11 à portée4, finition de fumée |
| White Out | Charge14ticks, frappe18 à portée32, recul puis attraction progressive bornée et nuage temporaire |

Le nuage et sa mobilité/capture sont des adaptations du port. Le nuage standard dure200ticks, rayon6 ; hauteur de capture alignée avec la fumée renouvelée. Population standard3, captures4 ; une cible ne peut être reprise indéfiniment par le même nuage. Les entrants tardifs sont pris en compte. La sortie, l’échéance ou l’invalidation du propriétaire libère la cible. Le ralentissement de capture et le bonus de mobilité ont deux identifiants distincts. Seul le bon porteur a la mobilité dans son nuage, débitée même en fraction d’énergie ; zéro énergie ne donne aucun tick gratuit. Les alliés, invulnérables et cibles protégées sont exclus.

### Suna — cinq techniques

| Technique | Comportement actuel |
|---|---|
| Desert Spada | Slash vertical12 et texture source `slash_sand` |
| Barjan | Déplacement jusqu’à3blocs puis slash12 à portée8, sans créer accidentellement une tempête statique |
| Sables | Coup initial50 à proximité, vortex et tempête persistante sur le même centre |
| Desert La Spada | Slash vertical15 distinct par son geste et son budget |
| Ground Death | Charge20ticks, onde2→16blocs, frappe20 puis dessiccation après dommage accepté |

Barjan conserve un seul budget12 ; le contact11 supplémentaire du script source n’est pas ajouté silencieusement. Ground Death est une adaptation non destructive, sans effacement des constructions. Sables conserve le service de tempête du port : par défaut rayon4, hauteur3, durée160ticks, trois dégâts par seconde ; propriétaire exact, volume cylindrique, LOS, alliés et protections vérifiés. Le mesh source et les particules persistent jusqu’à la même échéance. Population plafonnée8, remplacement de la tempête précédente du même porteur.

La dessiccation a son propre modificateur de vitesse, plafonné90%, et garde le palier maximum3/durée80ticks par défaut. L’eau la retire suivant la configuration. Les dégâts périodiques au maximum restent attribués ; aucun effet de lenteur externe retiré. Les slashes emploient la piste `vertical`, car certaines pistes `idle` du modèle source masquent le mesh.

### Yami — quatre techniques

| Technique | Comportement actuel |
|---|---|
| Kurouzu | Charge10ticks, frappe6 dirigée/portée10, attraction progressive20ticks |
| Black Hole | Charge20ticks, frappe24 dans le rayon24 correspondant à la largeur source48, attraction et absorption |
| Dark Ball | Charge10ticks, sphère noire40, portée32/durée80ticks |
| Dark Matter | Charge20ticks, masse noire200, modèle source `rubble/tex_black`, taille distincte et fin80ticks |

Dark Matter utilise bien `rubble` dans la source : il ne fallait pas supprimer ce modèle sur la seule base de son nom. Dark Ball a en revanche une silhouette sphérique propre. Il n’y a pas de cumul dégâts directs + explosion cachée dans cette adaptation.

L’attraction pilote une vitesse horizontale bornée, garde la gravité, s’arrête au contact et retire son élan à la fin. La collision corporelle, la ligne de vue et les protections restent contrôlées. Les états stockent l’instance du lanceur et le niveau, sans substituer un joueur réapparu ayant la même UUID.

L’absorption reste une adaptation : six coups physiques/projectiles, vingt énergie par coup, fenêtre600ticks, pénalité de douleur×2 conservée ; feu/explosion/magie ne sont pas absorbés. Un coup sans fenêtre d’absorption ne ferme plus Kurouzu ; l’expiration d’une absorption ne ferme plus une attraction plus récente. L’invalidation du propriétaire ferme les deux. Le scellement après dégât accepté dure100ticks par défaut.

## Vérification et limites

Le harnais `/dbatch01` emploie des FakePlayers seulement comme lanceurs et de vrais mobs adultes insérés/tickés comme victimes : coûts, refus de spam, gestes, dégâts, attribution, alliés, invulnérables, murs, déplacement réel et nettoyage. Les cas d’absorption par `hurtServer` sont complétés sur un vrai joueur dans la session graphique, car un FakePlayer peut avoir une invulnérabilité particulière.

Les oracles historiques K1/K3/N2/A7f lisent désormais les marqueurs/attributs propriétaires et la conservation des potions. A8b lit la vitesse totale plafonnée plutôt qu’une impulsion additive croissante. Ces changements suivent les défauts corrigés ; les IDs historiques sont conservés et les contrôles de progression, couverture et plafond restent exigés.

52pistes finies distinctes ont été écrites, avec retour neutre. Les blinks utilisent une seule piste courte, sans charge artificiellement prolongée. La session graphique doit vérifier50phases effectivement jouées par client, les formes dans le monde, les déplacements et le sabre natif. Un appel de rendu ne prouve pas à lui seul qu’une silhouette est lisible : l’inspection des captures reste séparée. L’acceptation artistique humaine reste à recueillir.

### Finition après la première revue groupée

Les captures ont montré des sprites source trop grands en FPS (lumière, fumée et ténèbres), des meshes de départ traversant la caméra et un nom passif Pika hérité de Goro. Les accents de particules utilisent désormais de petites particules vanilla ; les silhouettes principales et couleurs restent dans les meshes/profils propres. Les charges sont déplacées devant et sous la visée, leur rayon est réduit ; le coup de pied Pika reçoit un arc doré fin et fini. Les meshes natifs Partisan/Pheasant/White Vine/Dark Matter et Sables sont omis uniquement près de la caméra FPS ; leur rendu observateur reste entier. Le passif Pika a sa clé de traduction dédiée. Les projectiles conservent leurs collisions, budgets et portées.

Le test natif Yami remet explicitement `LivingEntity.damageCooldownTime` à zéro avant chaque `hurtServer`. `Entity.setInvulnerableTime(0)` ne remet pas ce timer distinct à zéro dans Minecraft26.3 : l’échec initial venait du harnais. Cette preuve vérifie la chaîne native de dégâts et l’absorption avec cooldown neutralisé, pas une cadence naturelle de coups. Le scénario d’expiration accélérée du service reste signalé comme horloge manuelle.
