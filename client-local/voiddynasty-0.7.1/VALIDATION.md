# Contrôles du pack local — 17 octobre 2026

Le pack contient exactement les quatre JAR listés dans `manifest.json`. Leurs identifiants, versions, tailles et empreintes SHA256 ont été contrôlés après copie. Le bridge est le même binaire 0.7.1 que celui de la distribution unifiée ; le mod de base est celui à six modèles de vaisseaux utilisé pour le compiler.

Un laboratoire temporaire a reçu uniquement ces quatre mods, sans One Piece et sans module Rogue Trader. Avec Minecraft 26.3 et Fabric Loader 0.19.5 :

- Le vrai transformeur Fabric charge les sept classes ciblées par les correctifs.
- Le relais de la liste de flotte est présent dans la classe du pad transformée.
- Trente assertions utilisant les vrais conteneurs et objets Minecraft passent : pagination, conservation des objets, synchronisation, disposition du pad, types de vaisseaux et premier obstacle du laser.
- Le worker du client est exécuté avec une configuration Rogue Trader absente : il reste en attente, ne conserve aucune campagne et se ferme normalement. L’absence de Rogue Trader ne fait pas échouer ce test.
- Les six fichiers de structure des vaisseaux sont présents dans le JAR principal.

Ces contrôles n’appellent pas `Minecraft.main`, n’ouvrent pas de fenêtre et ne chargent pas de monde. Ils ne remplacent pas un essai visuel complet du pack dans une partie. Aucun fichier de l’installation du joueur ni aucune sauvegarde n’a été modifié par cette préparation.

## Reproduire le paquet

Depuis `voiddynasty-logistics/`, après compilation du bridge canonique, lancer :

```powershell
python tools/package-local-client.py --base-jar 'C:\chemin\voiddynasty-1.0.0.jar'
```

Le JAR de base doit avoir exactement la même empreinte que celui de la compilation. Un simple changement de nom de fichier est accepté ; un changement de contenu est refusé. Le script copie Fabric API et GeckoLib depuis l’instance locale, produit le manifeste et les licences et crée un ZIP de téléchargement dans `dist/`. Il ne copie aucun fichier dans les jeux.
