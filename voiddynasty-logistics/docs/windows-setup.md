# Assistant Windows et premiere utilisation — 0.7.0

Le paquet joueur est `dist/VOID-DYNASTY-Joueur-0.7.0.zip`. Il contient l'executable autonome `VOID-DYNASTY-Assistant-0.7.0.exe`, un guide et son empreinte SHA-256. L'executable embarque les deux modules du pont. Python, un compilateur et les outils de developpement ne sont pas necessaires chez le joueur.

## Installation

1. Extraire le ZIP et ouvrir l'executable, sans demander de droits administrateur.
2. Fermer Minecraft et Rogue Trader. Un processus Java actif bloque les operations sur les fichiers, meme s'il s'agit d'une autre application Java ; l'assistant ne termine aucun processus.
3. Choisir l'instance Minecraft contenant `versions` et `mods`, puis le dossier Rogue Trader contenant `WH40KRT.exe`. Le dossier de donnees RT est distinct : il contient `Saved Games` et `UnityModManager`.
4. Selectionner le JAR VOID DYNASTY original. Un exemplaire deja installe est reconnu automatiquement et son emplacement conserve.
5. Pour une partie existante, choisir le monde et la sauvegarde RT correspondante dans les deux listes. Ces choix permettent de copier les parties avant l'installation ; ils n'effectuent aucune association et ne prouvent pas que les deux parties correspondent. Pour une nouvelle partie, laisser les selections vides.
6. Consulter **Verification** : profil Minecraft, dependances, original, assemblage RT et fermeture des jeux. Corriger les points bloquants. Les versions de cette alpha sont volontairement restreintes a l'environnement valide.
7. Consulter **Installation**, qui affiche les quatre destinations et le dossier de sauvegarde. Cliquer **Sauvegarder et installer**. Attendre la confirmation et ouvrir **Jouer et entretien**.

Versions requises : Minecraft 26.3, profil Fabric 0.19.5, Fabric API 0.162.0+26.3, GeckoLib 5.5.7, JAR original 1.0.0 reconnu et assemblage RT cible 1.6.1.514 reconnu. Le lanceur Minecraft doit fournir Java 25. L'assistant verifie le profil, pas le processus Java que le lanceur choisira ensuite. .NET Framework 4.8 est la cible de l'assistant Windows. Aucun chargeur, dependance ou jeu n'est fourni ou telecharge.

Les dossiers Steam usuels sont recherches pour RT ; un autre emplacement reste selectionnable. Les profils de lanceurs alternatifs doivent disposer du dossier `versions` du profil Fabric : une instance qui ne l'expose pas sera refusee plutot que declaree compatible sans preuve. Cette premiere version ne configure pas automatiquement les lanceurs tiers.

## Assistant dans les jeux

**Nouvelle association :** charger une campagne RT dans une zone stable, hors combat, ouvrir le panneau VOID DYNASTY avec Ctrl+F10 et utiliser **Assistant de premiere utilisation / Preparer cette campagne**. Le controle de persistance existant cree et recharge des copies ; attendre PASS. Il refuse une campagne contenant deja l'etat du pont.

Dans un monde Minecraft solo local, `/voidbridge` ouvre le guide tant que le depot et l'association ne sont pas presents. `/voidbridge assistant` et le bouton **?** du terminal permettent de le retrouver. Il indique l'etape actuelle : charger RT, creer le depot, demander le lien et obtenir l'approbation RT, puis choisir et livrer une mission. Les boutons passent par les actions serveur existantes ; ils ne modifient jamais directement le journal ou les recus.

**Association existante :** conserver le meme monde et la derniere sauvegarde RT `REWARD_COMMITTED` correspondante. Ne pas refaire la preparation ni l'association. Le guide rappelle cette regle. Les recettes, le protocole v4 et les recompenses natives de 0.6.0 restent inchanges dans 0.7.0.

## Mise a jour, restauration et retrait

Repasser par **Verification / Installation** pour mettre a jour ou reparer les deux modules ensemble. Le nom d'un JAR deja installe peut rester ancien : son identifiant et sa version interne font foi. Les doublons de mod sont refuses.

Dans **Jouer et entretien**, choisir un journal puis :

- **Restaurer l'etat precedent** : remettre les fichiers de mods tels qu'ils etaient avant cette operation. Cela permet aussi d'annuler un retrait recent.
- **Retirer les deux modules du pont** : retirer le compagnon Minecraft et le module RT. L'original VOID DYNASTY reste installe.
- **Ouvrir les sauvegardes** : consulter les copies du monde, de la sauvegarde RT et des anciens fichiers de mods.

Le retrait ne nettoie pas les donnees de campagne ou du monde. Conserver une copie complete d'un monde contenant le depot avant de le charger sans le compagnon, car Minecraft peut supprimer les blocs inconnus. Aucune sauvegarde de partie n'est automatiquement restauree, effacee ou ecrasee.

## Journal et limites

Les preferences et journaux sont sous `%LOCALAPPDATA%/VoidDynastyBridge/Installer`. Chaque operation garde son dossier `backups/<horodatage-UUID>/installation.json`, les anciennes versions des fichiers et les copies de parties selectionnees. Ces fichiers restent locaux et ne sont jamais inclus dans le paquet joueur. Le jeton du pont est distinct et n'est pas lu par l'assistant.

Les preimages sont sauvegardees et verifiees avant toute modification, puis un journal `prepared` est ecrit durablement. Chaque remplacement est atomique au niveau du fichier et son empreinte verifiee. En cas d'erreur, les ecritures deja effectuees sont annulees. Il n'existe pas de transaction atomique Windows couvrant les quatre fichiers : une coupure brutale peut laisser un journal `prepared`. Celui-ci bloque une nouvelle installation ; choisir ce journal et **Restaurer l'etat precedent** avant de recommencer.

La maintenance refuse les fichiers modifies depuis l'operation plutot que de les ecraser. Les chemins hors des dossiers de mods, les jonctions et liens symboliques sont refuses. Les copies de sauvegardes ne constituent pas un checkpoint appaire valide sans verification dans les jeux.

L'executable n'est pas signe numeriquement. Ses empreintes servent a verifier les copies, sans fournir une signature d'editeur. Les tests du moteur de l'installeur s'executent dans des installations synthetiques isolees ; ils couvrent installation, mise a jour, restauration, retrait, conservation des parties, erreurs d'ecriture, changements de fichiers et interruption. Le rendu de l'application est controle par des images de ses cinq ecrans. L'utilisation interactive par le joueur et le rendu du nouveau guide Minecraft restent a confirmer.

## Construction

Apres les deux modules : `python tools/build-installer.py`. L'outil compile `installer/` avec le compilateur .NET Framework local et embarque exclusivement nos propres JAR/DLL/manifestes. L'empreinte de `Code.dll` est un controle de compatibilite ; le DLL du jeu n'est jamais embarque. Le JAR original, les bibliotheques de jeux et les assets extraits restent exclus.

La version 0.7.0 ajoute les corrections du pad, des coffres, des effets d’armes et des blocs decrites dans [refit-0.7.md](refit-0.7.md). Le fonctionnement du pont et ses recus restent inchanges.
