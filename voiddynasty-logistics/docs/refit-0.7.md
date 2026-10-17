# Mise à jour 0.7.1

Le bridge est désormais construit depuis une seule source, `../../voiddynasty-bridge/`. Le pad reprend tous les types de vaisseaux et le relais de liste est réactivé. Voir [la migration et les validations](../../docs/BRIDGE-UNIQUE-0.7.1.md). La section suivante décrit les correctifs et la recette historiques de 0.7.0.

---

# Refonte du pad, des coffres et des effets — 0.7.0

Cette version ajoute les corrections au compagnon `voiddynasty_bridge`. Le JAR original VOID DYNASTY 1.0.0 reste identique. Les fonctions du pad, des coffres et des armes fonctionnent sans connexion a Rogue Trader ; le pont reste disponible avec son protocole v4 et ses recettes existantes.

## Pad

Le pad ouvre une interface en trois onglets : **Flotte**, **Commandes**, **Construction**. La liste est paginee selon la hauteur d'ecran. Les commandes de deploiement, rangement, deplacement, renommage et suppression restent celles du mod original. Les actions de rangement, deplacement et suppression demandent une seconde confirmation dans le pad. Les noms sont limites a un mot de 32 lettres/chiffres/tirets maximum.

La selection est conservee par nom au rafraichissement, et les confirmations sont abandonnees si le vaisseau selectionne disparait. Le nouveau pad utilise toujours les reponses de flotte du serveur : il ne reconstruit pas le registre des vaisseaux et ne modifie pas leurs templates ou leur physique.

## Coffres

Le menu original creait 54 cases cote client, alors que le serveur utilisait des inventaires plus grands et des indices qui se deplacaient avec le defilement. Le client ne recevait pas la capacite ou la position de defilement. La correction fournit une **fenetre de 54 cases** sur le stockage serveur ; les indices des paquets restent identiques des deux cotes. La capacite et la ligne sont synchronisees par les donnees de menu natives, avec prise en charge des mots signes de 16 bits.

Le fond du coffre, les cases et les controles de page sont redessines. La molette au-dessus de la soute avance par ligne ; les fleches avancent par six lignes. Apres un changement de page, les clics d'inventaire attendent la ligne confirmee par le serveur. Le changement de page est refuse par l'interface pendant qu'un objet est tenu au curseur ou pendant une repartition d'objets.

Le format des block entities et les objets sauvegardes ne sont pas convertis. Le stockage existant reste la source de verite. Les transferts affectent uniquement les indices physiques de la page selectionnee.

## Armes

- **Laser** : le renderer fourni dans le JAR ne dessinait aucune geometrie. Il est remplace par un rayon lumineux avec coeur clair et halo rouge. Son volume de visibilite couvre toute la longueur du rayon. Le tir utilise la direction serveur courante et choisit la cible la plus proche avant le premier bloc solide. Les degats, effets, munitions, cadence et rechargement passent toujours par le service de l'arme et son calculateur existants.
- **Bolts** : les gros emetteurs d'explosion sont remplaces par des impacts courts, avec nombre et dispersion de particules limites. La correction couvre l'impact direct et l'effet de souffle ; elle ne change pas les valeurs de degats ou le rayon de souffle.
- **Plasma** : ajout d'un coeur et d'une trainee cyan claire, echantillonnee entre les positions du projectile pour rester visible a haute vitesse. Les impacts plasma utilisent une lueur et des etincelles plus lisibles.

## Blocs

Un pack integre toujours actif remplace les modeles de 22 blocs du mod et du depot du pont : ceramite, coffres, conteneurs, caisses, barils, obus, sacs, cloisons et machines. Les modeles sont nos propres geometries et referencent des textures deja installees. Aucun bitmap extrait n'est inclus. Les furnaces montrent leur orientation et leur etat allume ; le laboratoire montre les bouteilles presentes. Les particules des sacs disposent maintenant d'une texture definie.

Les identifiants de blocs, leurs proprietes, leur collision et leur stockage restent ceux de l'original. Les changements de modeles ne deplacent pas de blocs dans les vaisseaux. Le pack peut etre recouvert par un pack de textures utilisateur prioritaire.

## Verification effectuee

La suite complete comporte **57 tests**, tous passes. Un profil Fabric temporaire charge le vrai Minecraft 26.3, l'original, le compagnon et les dependances : les sept classes ciblees acceptent les transformations. Ce test n'appelle jamais Minecraft.main, n'ouvre pas de fenetre et ne charge aucun monde.

Un second volet de ce meme test utilise les vrais `SimpleContainer` et `ItemStack` de Minecraft en memoire, avec des composants de fixture lies explicitement : conservation des quantites, indices de la derniere page, insertion/retrait, capacite client, transmission au-dela de 65535 cases, bornes des petits inventaires, dimensions du pad et choix du premier obstacle du laser. Le codec du jeu accepte les metadonnees du pack. Les references de textures/modeles et les proprietes des variantes sont controlees contre les deux JAR installes.

Ces resultats valident le chargement et la logique testee. Ils ne remplacent pas le controle du rendu, des clics, des transferts et des degats dans une partie reelle, notamment avec les autres mods installes.

## Essai en jeu

1. Fermer Minecraft et RT, sauvegarder le monde utilise et installer les deux modules 0.7.0 avec l'assistant. Garder le JAR original.
2. Ouvrir le pad. Tester les trois onglets, la selection de flotte et le redimensionnement de la fenetre. Ne supprimer un vaisseau que pour un test volontaire sur une copie.
3. Dans un coffre de test, placer des piles reconnaissables, passer aux lignes suivantes, retirer/deposer une pile et faire un transfert rapide. Revenir a la premiere page, puis fermer/recharger le monde et verifier les quantites.
4. Tester le fusil et le pistolet laser contre un mob proche, puis a distance et derriere un mur. En survie, fournir les cellules et recharger avec R. Verifier le rayon et la perte de vie effective.
5. Comparer un impact de bolter avec un projectile plasma. Observer les deux depuis la premiere et la troisieme personne.
6. Verifier coffres, ceramite et machines ; tourner une furnace et l'allumer. Ajouter/retirer des bouteilles au laboratoire. Si un ancien pack utilisateur remplace les modeles, le desactiver temporairement pour comparer.

Pour une partie deja liee a RT, garder son monde et sa derniere sauvegarde REWARD_COMMITTED correspondante ; ne refaire ni l'association ni le test de persistance.
