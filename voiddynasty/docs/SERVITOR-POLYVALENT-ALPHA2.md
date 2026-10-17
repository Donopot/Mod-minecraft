# Servitor polyvalent — accès et construction

Version **1.3.0-alpha.2**, branche `codex/servitor-polyvalent`. Cette version complète le [premier jalon de missions](SERVITOR-POLYVALENT.md) avec l'accès horizontal aux minerais et la construction de plans prédéfinis à partir de matériaux réels.

## Accès aux minerais

`servitor_gather` accepte désormais `access`, qui vaut `surface` par défaut. Le mode historique ne casse que les blocs de la ressource demandée. Le mode **`excavate` doit être explicitement choisi** pour autoriser le dégagement de roche dans la zone sélectionnée.

Quand le chemin natif ne permet pas d'atteindre le minerai, le servitor cherche un passage horizontal borné. Le dégagement tient compte de sa largeur de 1,09 bloc : passage de trois blocs de large et deux de haut, avec un sol existant. Il mine physiquement à portée et en visibilité, avec son outil et les drops natifs. Chaque bloc d'accès rejoint le même journal que le minerai. Le surplus de roche est rapporté dans les dépôts du chantier.

L'accès ne casse jamais hors de la zone indiquée. Il accepte uniquement la roche/terre du catalogue et les blocs correspondant à la ressource demandée. Il refuse les conteneurs, blocs indestructibles, fluides proches et plafonds gravitaires détectés. Il ne force pas les chunks à se charger. Le budget est de **256 blocs d'accès par mission**, minerai dégagé par ce mécanisme compris ; le compteur est sauvegardé. La recherche examine au plus 512 positions par tentative, avec une extension de 24 blocs autour du départ de cette recherche.

Les pauses `ACCESS_NO_SAFE_ROUTE`, `ACCESS_TERRAIN_CHANGED`, `ACCESS_OUT_OF_REACH`, `ACCESS_PATH_CHANGED` et `ACCESS_BUDGET_EXHAUSTED` conservent les objets et les reçus. Elles demandent de corriger la zone ou l'accès, puis de reprendre. Aucun escalier, pont ou changement de niveau automatique n'est encore construit. Le mode ne garantit pas de trouver tous les minerais d'une zone.

## Construction depuis un plan

Deux plans versionnés sont proposés :

| Identifiant | Contenu | Coût |
|---|---|---|
| `platform_3x3_v1` | Plateforme pleine de 3×3, une couche. | 9 blocs. |
| `shelter_5x5_v1` | Abri de 5×5 sur cinq couches : sol, murs, toit et entrée de deux blocs de large sur deux de haut. | 94 blocs. |

Matériaux admis : cobblestone, stone, oak_planks et spruce_planks, dans l'espace de noms `minecraft`. Les plans utilisent des blocs simples ; les portes, escaliers, blocs avec inventaire, explosifs et mécanismes ne sont pas encore pris en charge. L'abri n'a pas encore de mobilier ni de décoration.

Le joueur désigne `anchor` (coin minimal du plan), `source` (coffre simple ou tonneau de matériaux), `origin` (départ du servitor) et la dimension. La zone entière doit être vide, chargée, non protégée, avec un sol porteur sous l'empreinte. Le coffre de matériaux doit être hors de la zone.

`servitor_build_estimate` lit le plan et le site sans créer de mission ni prélever de ressource. Il renvoie `required` et `site_problem`. L'estimation ne réserve ni le terrain ni le stock : le serveur les vérifie de nouveau au moment de travailler.

`servitor_build` prend les mêmes paramètres, plus une clé `request_id`. Exemple :

```json
{
  "request_id": "abri-001",
  "blueprint": "shelter_5x5_v1",
  "material": "minecraft:cobblestone",
  "anchor": [100, 64, 100],
  "source": [94, 64, 100],
  "origin": [96, 64, 96],
  "dimension": "overworld"
}
```

Le servitor construit d'abord son dépôt de chantier, rejoint le coffre de matériaux et réserve **la totalité** du coût dans son sac. Si le stock est insuffisant, il se met en pause sans prélèvement partiel et sans pose. Les objets nommés ou possédant des composants distincts ne sont pas consommés comme des blocs ordinaires.

Il rejoint ensuite chaque emplacement, pose un bloc à portée et en visibilité, et débite une unité seulement après une pose réussie. Les blocs déjà présents ne sont pas remplacés ou comptés comme sa construction. Un terrain modifié, un obstacle, une entité dans l'emplacement ou un support absent provoque une pause. La réussite exige une vérification de tous les blocs du plan et un sac vide.

Les missions de construction exposent `type: BUILD`, `blueprint`, `material`, `materials_reserved` et `placed_total`. `amount` désigne ici le nombre de blocs du plan ; `collected_total` est le nombre de matériaux réservés et `delivered_total` le total des poses effectuées. Ces compteurs historiques ne sont pas diminués par un rollback.

## Annulation, remboursement et redémarrage

`pause`/`resume` conservent le plan, la réservation et la progression. Après un redémarrage propre, la reprise est explicite ; la réservation déjà effectuée n'est pas répétée. Le format de sauvegarde est **5** et les formats 2 à 4 restent lisibles.

`cancel` arrête les poses et conserve les matériaux non consommés. `deposit` rapporte le sac dans le dépôt de chantier. Le coffre de matériaux initial n'est pas nécessairement ce dépôt.

Pour une pose, le reçu conserve l'état posé et son coût exact. `rollback` retire le bloc uniquement s'il correspond encore au reçu, puis rembourse son coût dans le sac. Un changement du terrain ou un sac trop plein suspend la restauration sans remboursement de ce bloc. Déposer les ressources et redemander le rollback permet de poursuivre. Le retrait n'engendre pas de drops supplémentaires. Les reçus déjà traités sont retirés pour empêcher un second remboursement.

Attendre `rollback_state: COMPLETE`, puis récupérer le sac avant `close`. La fermeture abandonne explicitement l'historique de rollback, tout en conservant l'identité de la demande pour empêcher les doublons.

Les protections existantes restent applicables. L'atomicité entre blocs, coffre et SavedData lors d'une coupure brutale pendant les écritures disque reste à traiter ; ces opérations sont vérifiées sur le thread serveur et lors d'arrêts propres.

## MCP et Hermes distant

Six outils typés : `servitor_capabilities`, `servitor_gather`, `servitor_build_estimate`, `servitor_build`, `servitor_mission_status`, `servitor_mission_control`. Le lancement avec **`--servitor-only`** n'enregistre que ces six outils. Le mode historique sans cette option conserve les outils d'administration précédents.

À la demande du joueur, le raccordement distant est fourni sous forme d'[exemples à adapter](../tools/hermes/README.md), sans connexion à son serveur Hermes. Ils comprennent le transport stdio sur SSH, le lanceur Windows, les chemins de configuration et les instructions du rôle. Le format propre à la version d'Hermes devra être adapté lors du raccordement réel.

## Limites et suite

Cette étape utilise encore une pioche et un kit de dépôts d'opérateur ; les matériaux du bâtiment proviennent bien du coffre désigné. Il reste les escaliers et la navigation verticale, les plans importés, les éléments orientés et avec inventaire, la fabrication de matériaux, les files de missions et la validation visuelle en client.

Les fixtures et contrôles sont publiés dans `WorkNextServerTest`, `work_next_acceptance.py`, `WorkConservationTest` et `tools/tests/test_missions.py`. `run_work_lab.py --missions` rejoue toutes les régressions et les nouveaux scénarios ; `--next-only` limite le laboratoire aux accès et à la construction. Le laboratoire démarre un monde neuf en local et n'installe rien dans les parties du joueur.
