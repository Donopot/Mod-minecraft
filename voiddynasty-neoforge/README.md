# VoidDynasty NeoForge 1.21.1 — état P0

Ce répertoire contient pour l'instant uniquement le GameTest de contrat destiné à P3. Aucun mod NeoForge jouable ni registre de contenu n'est encore implémenté. P1 appartient à Hermes, qui seul accède au monde source.

Le contrat canonique est `plans/neoforge-1.21.1/contrat-ids.json`. Le test vérifie explicitement l'existence des IDs ; il ne confond pas un identifiant absent avec le bloc air ou une entité par défaut. Il doit échouer avant l'implémentation des registres P3.

Le compilateur préparatoire se trouve dans `scripts/neoforge/p0-compiler`, avec ModDevGradle 2.0.148, NeoForge 21.1.264, Gradle 9.2.1 et toolchain Java 21. Il ne démarre aucun serveur et ne produit pas un mod installable.

```sh
python3 scripts/neoforge/verify_contract.py
python3 -m unittest discover -s scripts/neoforge/tests -v
scripts/neoforge/p0-compiler/gradlew -p scripts/neoforge/p0-compiler classes
```

Sur Windows, utiliser `gradlew.bat`. `JAVA_HOME` doit désigner le JDK 21. La compilation copie le contrat sans modification dans `migration/contrat-ids.json` sur le classpath. Le futur build P3 doit reprendre cette ressource et le source set GameTest ; exécuter alors le test en jeu, sans masquer les échecs par un `required=false`.

Suivi : `plans/neoforge-1.21.1/AVANCEMENT.md`. Relais pour le serveur : `plans/neoforge-1.21.1/HERMES-P1-EXPORTS.md`.
