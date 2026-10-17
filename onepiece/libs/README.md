# Dépendances locales

Placer ici GeckoLib `geckolib-fabric-26.3-5.5.7.jar` et Player Animation Library `playeranimlib-26.3-1.2.8.jar`, obtenus auprès de leurs auteurs ou de l’installation locale. Ces bibliothèques ne sont pas versionnées.

Les ressources graphiques binaires restent locales. Le manifeste `docs/private-assets-manifest.json` donne les empreintes attendues. Depuis le paquet privé correspondant :

```powershell
python tools/import-private-assets.py --jar "C:\chemin\opworldlike-1.0.1-alpha.19.jar" --check
python tools/import-private-assets.py --jar "C:\chemin\opworldlike-1.0.1-alpha.19.jar"
```

L’importeur ne copie aucune classe Java compilée, dépendance ou sauvegarde. Les JAR Forge de référence ne doivent pas être placés dans une installation Fabric.
