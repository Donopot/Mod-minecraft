Current version: **0.6.0 native cargo missions, protocol v4**. See [the current guide](native-cargo-missions.md). The description below records the earlier logistics version.

# Livraisons répétables — 0.5.0

Chaque **nouvel envoi de 64 lingots de fer ajoute 10 points** de réserve logistique personnalisée. La livraison déjà reçue reste enregistrée et ses 10 points sont conservés. Le monde, le dépôt et l'appairage restent les mêmes.

## Test depuis la livraison existante

1. Charger le monde Minecraft **testatst** et la sauvegarde RT **VOID DYNASTY - REWARD_COMMITTED 133057**. Aucun nouveau test de persistance ni appairage n'est nécessaire.
2. Exécuter `/voidbridge`. Vérifier **1 livraison**, réserve **10**, état **SETTLED** et le dépôt existant. Avec au moins 64 lingots déposés et RT hors menu/combat, le bouton d'envoi doit être actif.
3. Cliquer **Expédier 64 fer (2)** puis confirmer. Attendre la confirmation. Résultat attendu : **2 livraisons, réserve 20**, 64 lingots consommés et zéro réservé. Avec les 65 lingots visibles sur la dernière capture, il doit en rester **1**.
4. Ajouter 64 lingots et envoyer à nouveau : **3 livraisons, réserve 30**, sans nouvel appairage. Le bouton devient disponible après chaque livraison si le dépôt contient encore 64 lingots.
5. Fermer proprement les deux jeux. Recharger le même monde et **la toute dernière sauvegarde RT REWARD_COMMITTED**, créée par le nouvel envoi. Vérifier le compte et le total. Charger l'ancienne sauvegarde à 10 après avoir atteint 20 est un retour en arrière et doit bloquer le pont.

Un menu RT chargé conserve le lien et les reçus visibles mais suspend les nouvelles expéditions. Le menu principal ou un chargement de campagne rend le récepteur temporairement indisponible. Aucun envoi n'est remboursé ou récompensé une seconde fois après un simple délai réseau. **Reprendre / reçu** utilise toujours l'identifiant de la transaction en cours.

## Persistance et compatibilité

- Protocol **v3**, mission **v2**, deux modules 0.5.0 nécessaires. Les schémas sont `live-v3-request.schema.json` et `live-v3-response.schema.json`.
- L'état RT garde le même emplacement par sauvegarde et les mêmes identités. Il ajoute un registre de reçus ordonné ; le total doit être égal à dix fois leur nombre. L'ancien reçu unique est importé comme numéro 1, avec son hash d'origine. La conversion en mémoire n'attribue aucun point ; elle est sauvegardée avec la prochaine préparation réelle.
- Le monde conserve le nombre d'envois soldés et l'identifiant du dernier reçu. La nouvelle empreinte fige mission, paire, cargaison, numéro suivant et reçu précédent. Un même UUID ne peut pas désigner deux livraisons.
- Le dépôt conserve le dernier reçu soldé jusqu'à la réservation suivante. Le journal du monde et les marqueurs de réservation/solde restent contrôlés ; une divergence bloque la progression.
- Une mise à jour depuis 0.4.x exige un état stable sans livraison en vol. Une préparation/réservation ancienne doit être terminée avec 0.4.1 avant la migration.
- Le prototype conserve **256 reçus maximum**, sans suppression des anciens. À capacité, il bloque explicitement au lieu d'oublier des identifiants. Le total maximal de cette version est 2560.

Les tests compilés couvrent migration, répétitions, déduplication, reprise après réponse perdue, retours en arrière et capacité. Le cycle répétable dans les deux moteurs reste à valider après installation ; les preuves de la livraison unique précédente ne prouvent pas ce nouveau cycle.
