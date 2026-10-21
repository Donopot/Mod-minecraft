# VOID DYNASTY 1.3.0-alpha.8 — mod unifié

Minecraft **Java 26.3**, Fabric Loader **0.19.5+**, Java **25**. Fonctionne sans Rogue Trader.

Fermer Minecraft et copier son monde avant le test. Retirer l'ancien JAR principal VOID DYNASTY et le bridge séparé, puis copier les trois fichiers de `mods/` dans le dossier `mods` du client. Garder un seul exemplaire de chaque dépendance déjà présente. Sur serveur dédié, installer les mêmes versions côté serveur.

- `voiddynasty-1.3.0-alpha.8.jar` : flotte, armes/HUD, coffres, servitor alpha 7, IA, pad et logistique Minecraft réunis.
- `fabric-api-0.162.0+26.3.jar` : dépendance Fabric API.
- `geckolib-fabric-26.3-5.5.7.jar` : dépendance d'animation.

**Ne pas conserver `voiddynasty-bridge-*.jar` / `voiddynasty_bridge-*.jar` avec cette version.** Fabric les refuse pour empêcher le double chargement. L'ancien assistant 0.7.1 ne doit pas installer ce paquet unifié ; cette installation est manuelle.

La DLL Rogue Trader reste nécessaire côté RT uniquement pour les livraisons vers ce jeu. Le protocole, les identifiants des blocs et les formats de sauvegarde sont conservés. Aucun changement de DLL, de liaison RT, de configuration Hermes ni de profil personnel n'est effectué par ce paquet.

Commandes utiles : `/servitor spawn`, `/servitor brain status`, `/servitor bag list`, `/work status`, `/voidbridge` en solo local. Le cerveau IA nécessite un modèle configuré et reste désactivé par défaut. Les raccourcis d'armes R et J restent modifiables dans les contrôles.

[Validation](VALIDATION.md) · [Manifeste](manifest.json) · [Empreintes](SHA256.txt) · [Détails du build](BUILD-UNIFIE-ALPHA8.md).
