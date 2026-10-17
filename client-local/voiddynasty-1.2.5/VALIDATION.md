# Validation du pack 1.2.5

Date : 17 octobre 2026. Minecraft Java 26.3, Fabric Loader 0.19.5, JDK 25, Fabric API 0.162.0+26.3, GeckoLib 5.5.7 et bridge 0.7.1.

Le JAR principal est issu de la compilation locale des **192 sources Java**. Son SHA-256 est `2e4fef36f4e2971c0e684e001a0a07cf97c89ece0af0d7f756bf64198166e532`. Les trois dépendances sont inchangées. Les six structures de vaisseaux, les ressources et la licence restent incluses ; les harnais de test restent séparés du JAR jouable.

**74 assertions passent sur les octets livrés** : 26 contrôles natifs d'inventaire/codecs et 48 contrôles serveur, dont cinq après arrêt et relancement complet. Le laboratoire charge réellement Fabric API, GeckoLib et le bridge 0.7.1, sans Rogue Trader.

Les nouveaux essais valident le lancement sans coffre ni sol près du coin de sélection, la construction d'un support puis d'un coffre par le servitor, la livraison du minerai, la construction d'un second dépôt lorsque le premier est plein, la préservation de son contenu, l'attente d'un départ sûr sans refus du chantier et la persistance du plan de construction et du kit. Les contrôles de conservation, rollback, protection, navigation autour d'un mur et trajet impossible sont rejoués.

[Note technique et limites](../../voiddynasty/docs/SERVITOR-1.2.5.md). Les lanceurs reproductibles sont dans `voiddynasty/tools/run_work_*.py`. Les chemins personnels, identifiants RCON et mondes de test ne sont pas publiés. Le rendu artistique du client, toutes les dimensions, une reconstruction Gradle sur poste vierge et une coupure brutale pendant les écritures disque ne sont pas validés par cette recette. L'installation habituelle du joueur n'a pas été modifiée.
