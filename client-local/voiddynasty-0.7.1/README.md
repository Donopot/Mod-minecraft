# VOID DYNASTY — client local 0.7.1

Pack pour **Minecraft Java 26.3**, **Fabric Loader 0.19.5**, **Java 25**, en solo local. Rogue Trader n’est pas requis pour jouer au contenu Minecraft.

## Installation manuelle

1. Télécharger le dépôt avec **Code → Download ZIP**, puis extraire l’archive, ou cloner le dépôt.
2. Dans le lanceur Minecraft, installer ou sélectionner le profil **Fabric 0.19.5 pour Minecraft 26.3**. Utiliser Java 25.
3. Fermer Minecraft. Ouvrir le dossier de cette instance, puis son sous-dossier `mods` ; le créer si nécessaire.
4. Mettre de côté les anciennes versions de VOID DYNASTY et de son bridge dans ce dossier, notamment `voiddynasty-bridge-diagnostic-0.2.0.jar`. Conserver un seul exemplaire de chaque mod et de chaque dépendance.
5. Copier les **quatre fichiers JAR** du [dossier mods de ce pack](mods) dans le dossier `mods` de l’instance. Copier les fichiers, pas l’archive ZIP ou le dossier parent.
6. Lancer le profil Fabric et créer ou charger un monde solo. Pour un premier essai, un monde de test en Créatif permet d’accéder facilement aux objets du mod.

| JAR | Fonction |
|---|---|
| `voiddynasty-1.0.0.jar` | Mod principal, incluant les six types de vaisseaux. |
| `voiddynasty-bridge-0.7.1.jar` | Pad dynamique, relais de flotte, correctifs des coffres et effets des armes. |
| `fabric-api-0.162.0+26.3.jar` | Dépendance Fabric API. |
| `geckolib-fabric-26.3-5.5.7.jar` | Dépendance d’animation GeckoLib. |

Le JAR du mod principal garde sa version interne `1.0.0` ; le numéro `0.7.1` désigne le bridge et ce pack. Les fichiers sont identifiés précisément dans [manifest.json](manifest.json) et [SHA256.txt](SHA256.txt).

## Sans Rogue Trader

Les armes, munitions, ennemis, blocs, coffres et vaisseaux restent utilisables dans Minecraft. Les objets se trouvent dans l’onglet Créatif du mod. Avec les commandes autorisées, `/give @s voiddynasty:control_pad` donne le pad ; clic droit pour l’ouvrir. `/fleet list` permet de consulter la flotte.

Il n’est pas nécessaire d’utiliser `/voidbridge assistant`, de créer une association ou de lancer l’assistant Windows destiné aux deux jeux. Le module de logistique peut afficher **Rogue Trader unavailable** : ce statut concerne uniquement les livraisons entre les jeux. Le client continue de fonctionner ; aucune cargaison n’est attribuée dans Rogue Trader lorsqu’il est absent.

Ce dossier ne contient ni One Piece ni les JAR Forge de référence. Il vise le solo local ; son contrôle ne vaut pas validation d’un serveur dédié ou de tous les autres mods installés.

## Fichiers et licences

Les licences livrées avec les JAR sont conservées à l’intérieur des archives et recopiées dans [licences](licences). Le fichier de licence de VOID DYNASTY fourni dans son JAR est CC0 ; son manifeste déclare MIT. Ces indications d’origine sont conservées sans modification. Fabric API fournit sa licence Apache-2.0 et GeckoLib sa licence MIT.

Aucune configuration personnelle, aucun jeton, compte, monde ou sauvegarde ne fait partie de ce pack. Les contrôles techniques et leurs limites figurent dans [VALIDATION.md](VALIDATION.md).
