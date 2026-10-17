# Test de persistance Rogue Trader — 0.3.1

Cette étape prépare l'activation de la livraison. **Elle n'active pas encore les récompenses.**

**Résultat réel : PASS le 15 octobre 2026 à 12:48:56.** Capture utilisateur, six étapes dans le journal et lecture indépendante de la sauvegarde finale confirment la preuve validée, avec réserve zéro. Il n'est plus nécessaire de relancer ce test pour essayer 0.4.0 : charger la copie **PERSISTANCE VALIDEE** et suivre [la recette de livraison](delivery-test.md).

Le stockage inspecté dans le jeu est `Game.State.InGameSettings.List`, un dictionnaire `string -> object` sérialisé dans `settings.json` de chaque sauvegarde. Le mod utilise une seule nouvelle clé, `voiddynasty_bridge_state_v1`, contenant une chaîne JSON avec son UUID de campagne, son reçu de test et une réserve initiale de zéro. Les réglages natifs et les monnaies du jeu ne sont pas modifiés.

## Exécution dans le jeu

1. Charger une sauvegarde habituelle datant d'avant les essais, hors combat/dialogue, en mode `Default`. Ne pas reprendre la copie de test partielle de la version 0.3.0.
2. Ouvrir Ctrl+F10, puis le panneau **VOID DYNASTY Bridge 0.3.1**.
3. Cliquer **Tester persistance sur copies (rechargements)**.
4. Laisser finir les sauvegardes/rechargements automatiques. Ne pas changer manuellement de campagne pendant le test.
5. Le résultat attendu est **PASS - recu persistant et sauvegardes isolees; reserve 0**.

Le test crée trois nouvelles sauvegardes manuelles, en utilisant `Game.SaveGame` et `Game.LoadGame`, les points d'entrée du chargement normal du jeu :

- `VOID DYNASTY - AVANT TEST ...` : copie de l'état courant avant ajout de la clé du mod.
- `VOID DYNASTY - TEST PERSISTANCE` : copie contenant le reçu de test.
- `VOID DYNASTY - PERSISTANCE VALIDEE` : copie finale contenant la preuve validée.

Il lit chaque archive enregistrée pour vérifier le contenu de `settings.json`, recharge la copie sans reçu pour vérifier l'isolation, puis recharge celle avec reçu. Il vérifie que le moteur a bien remplacé les objets joueur et réglages et relit le même état. La preuve validée est ensuite sauvegardée et rechargée une dernière fois.

La lecture des archives est exécutée sur un worker. Les accès aux objets du jeu et les mutations de la clé du mod restent sur le fil Unity. Chaque opération attend son callback natif de fin ainsi que la fin du chargement et le retour du gestionnaire de sauvegarde au repos, avec une limite de 120 secondes. Un fichier absent, un contenu différent, une erreur ou un changement de campagne interrompt le test. Une erreur ne valide jamais la persistance.

La campagne rechargée à la fin est la **copie de test validée**. Les anciennes sauvegardes ne sont pas écrasées. En cas d'échec, conserver les copies et les journaux ; charger la copie `AVANT TEST` pour retrouver l'état sauvegardé avant le reçu. Aucun remboursement ni aucune récompense n'est réalisé.

## Portée de la preuve

Les tests automatiques vérifient le codec de l'état, la conservation des réglages natifs et la lecture des entrées dans les archives. Ils ne remplacent pas le cycle réel à lancer ci-dessus. La livraison doit rester désactivée tant que ce test n'a pas réussi dans le jeu.

## Premier essai réel et correction

Le 15 octobre, la version 0.3.0 a créé deux archives : la copie avant test ne contient pas la clé du pont ; la copie de test contient le reçu, la bonne identité de campagne et une réserve à zéro. L'écriture dans le format réel est donc confirmée. Le rechargement a échoué : l'appel direct à `SaveManager.LoadRoutine` contournait l'orchestration normale des scènes. La version 0.3.1 utilise les points d'entrée ci-dessus. Son test automatique vérifie qu'elle attend à la fois le callback et la disponibilité du moteur ; son cycle réel reste à confirmer.

Avant cet essai, les 165 sauvegardes ont été sauvegardées dans l'archive privée `.runtime/um-state/backups/void-dynasty-pre-persistence/20261015-123346.zip` (environ 2,6 Gio). La version 0.3.1 a ensuite été installée seule côté RT avec copies des fichiers remplacés et vérification des empreintes ; manifeste privé : `.runtime/install-6c01d639-9877-4dfe-82b1-6308206e4442/manifest.local.json`.
