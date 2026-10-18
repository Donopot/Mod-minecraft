# VOID DYNASTY 1.3.0-alpha.4 — navigation du servitor

Pour **Minecraft Java 26.3, Fabric Loader 0.19.5 et Java 25**, avec ou sans Rogue Trader. Cette alpha ajoute des accès excavés en paliers montants et descendants, corrige les arrivées sur les cases des mobs larges, détecte les oscillations sans progression et fournit une commande de collecte pour le solo.

## Installer

1. Fermer Minecraft et sauvegarder ses mondes.
2. Retirer les anciens JAR du mod principal du dossier `mods` : conserver **un seul** `voiddynasty-<version>.jar`.
3. Copier les quatre JAR de [mods](mods). Si le bridge, Fabric API et GeckoLib ci-dessous sont déjà identiques, les garder sans ajouter de doublon.

| Fichier | Rôle |
|---|---|
| `voiddynasty-1.3.0-alpha.4.jar` | Mod principal, missions et navigation. |
| `voiddynasty-bridge-0.7.1.jar` | Pad, coffres, armes et logistique existants. |
| `fabric-api-0.162.0+26.3.jar` | Fabric API, inchangé. |
| `geckolib-fabric-26.3-5.5.7.jar` | GeckoLib, inchangé. |

Le format de sauvegarde reste 5 ; les versions précédentes restent disponibles. Le kit de dépôts et l'outil initial restent fournis par l'opérateur comme dans les alphas précédentes. Cette livraison ne modifie pas l'ancien installeur inter-jeux et n'installe rien automatiquement dans une partie.

## Collecter en solo

Avec les commandes autorisées, un coffre simple ou tonneau de destination chargé et hors de la zone :

```text
/work mission gather <ressource> <quantite> <coin1 x y z> <coin2 x y z> <coffre x y z> <depart x y z> [surface|excavate]
/work mission status current
/work status
```

`surface` rejoint les ressources accessibles par les chemins existants. `excavate` autorise aussi le dégagement de roche dans la zone : passages horizontaux, puis paliers de 3 × 3 avec une marche de hauteur maximale un bloc et du plafond pour remonter. Chaque casse utilise la portée, l'outil, les drops et les reçus habituels. Le servitor rapporte également les déblais.

Inclure tout le volume nécessaire au passage dans la zone. La recherche est limitée à huit blocs de dénivelé depuis son départ, 24 blocs en X/Z et 512 positions ; le budget persistant reste de 256 blocs d'accès par mission. Le sol des paliers doit déjà exister : pas de pont, d'échelle ou d'échafaudage automatique. Une zone trop petite, un danger détecté ou un terrain modifié provoque une pause avec conservation des ressources. La recherche ne garantit pas de trouver tous les itinéraires.

Pour un ancien servitor bloqué, `/work stop` le retire en conservant le sac et le journal. `/work resume` recalcule les accès d'un chantier repris. Lire l'état avant de déposer les ressources, demander un rollback ou fermer une mission. Le [guide complet](../../voiddynasty/docs/SERVITOR-NAVIGATION-ALPHA4.md) explique les contrôles par UUID et les limites.

[Validation](VALIDATION.md) · [Versions et provenance](manifest.json) · [SHA256.txt](SHA256.txt) · [Licences](licences). Les mentions de licence d'origine sont conservées. Ce paquet contient uniquement les quatre mods et leur documentation, aucun monde ni bibliothèque privée du jeu.
