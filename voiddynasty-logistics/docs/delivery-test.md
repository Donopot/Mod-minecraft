# Delivery test history

For the current repeatable version 0.5.0, use [this acceptance procedure](repeatable-deliveries.md). The single-mission procedure below documents 0.4.x and its first successful delivery.

# Livraison réelle — version 0.4.1

La preuve de persistance RT 0.3.1 a réussi dans le jeu le 15 octobre 2026 à 12:48:56 : reçu rechargé, copie sans reçu isolée, preuve finale rechargée, réserve zéro. La version 0.4.1 implémente la livraison. Sa compilation et ses tests automatiques ne remplacent pas la recette dans les deux jeux.

## Premier essai

1. Installer les deux modules 0.4.1 avec les jeux fermés. Garder le mod VOID DYNASTY original, Fabric API et GeckoLib.
2. Dans RT, charger **VOID DYNASTY - PERSISTANCE VALIDEE**. Ouvrir le panneau du pont avec Ctrl+F10. Ne plus relancer le test de persistance.
3. Dans Minecraft, créer un **nouveau monde solo de test**, sans ouverture LAN. Prévoir 65 lingots de fer, pour vérifier qu'un lingot reste disponible après l'envoi.
4. Sur un sol fixe, à quai et à l'écart du vaisseau mobile, exécuter `/voidbridge`. Cliquer **Créer le dépôt** : la case deux blocs devant le joueur doit être libre. Un dépôt unique est enregistré pour ce monde.
5. Cliquer **Lier la campagne**, puis dans RT **Lier cette campagne au monde affiché**. Comparer l'UUID du monde avec celui du terminal Minecraft. RT crée une nouvelle sauvegarde de liaison. Attendre la confirmation dans Minecraft.
6. Ouvrir le dépôt et y déposer 65 lingots. Fermer son inventaire, exécuter `/voidbridge`, cliquer **Expédier 64 fer**, puis **Confirmer 64 → +10**.
7. Attendre **Livraison confirmée**. Résultat attendu : état `SETTLED`, réserve RT 10, zéro lingot réservé et un lingot restant dans le dépôt. RT affiche le même identifiant de livraison et une sauvegarde `REWARD_COMMITTED` confirmée.
8. Fermer proprement les deux jeux. Recharger le même monde et **la nouvelle sauvegarde RT `REWARD_COMMITTED`**, qui contient le résultat. Vérifier réserve 10, état terminé, aucune nouvelle expédition possible. Charger la vieille sauvegarde `PERSISTANCE VALIDEE` après la livraison doit bloquer l'envoi et signaler une divergence ; elle ne contient pas le résultat futur.

Le dépôt accepte uniquement le fer, expose zéro emplacement aux hoppers et reste fixe, sans destruction normale, explosion ni poussée par piston. Le terminal demande de rester à huit blocs pour les actions. Il n'est pas prévu pour être déplacé avec le vaisseau. Une copie du bloc à une autre position ne correspond pas au registre du monde et reste inaccessible au pont. Les commandes administrateur et outils externes peuvent toujours altérer le monde : le prototype bloque alors la livraison lorsqu'il constate le manque ou la divergence.

## Déconnexion et récupération

- RT absent : aucune nouvelle livraison. Une réservation existante reste verrouillée.
- Réponse perdue : **Reprendre / reçu** réutilise l'identifiant enregistré. Un reçu déjà sauvegardé solde la réservation, sans nouvelle récompense.
- Ancienne sauvegarde RT, ancien monde, conteneur déplacé/absent ou journal partiellement écrit : `RECOVERY_REQUIRED`. Conserver les fichiers et les journaux ; ne pas effacer le journal pour « débloquer » les objets.
- Aucun bouton de remboursement ou de récompense forcée n'est fourni. L'annulation réseau du simulateur v1 n'est pas exposée par cette première intégration.
- Pour une restauration fiable, conserver un checkpoint appairé des deux sauvegardes, jeux fermés, avant les tests de panne. Voir [persistance](persistence.md). La sauvegarde générale faite avant installation protège les mondes existants ; elle ne constitue pas automatiquement un checkpoint de la nouvelle paire.

## Limites à vérifier dans le jeu

Le premier transfert réel est confirmé : reçu RT sauvegardé avec réserve 10, journal Minecraft SETTLED et identifiants identiques. Le compte du lingot restant, le rechargement propre, les cas de panne et les corrections du bouton/menu 0.4.1 restent à vérifier. Les tests compilés couvrent le registre C#, les répétitions de commit, les divergences, l'attente de la réponse sur le fil du jeu, le protocole C#/Java, le journal du monde et les marqueurs d'écriture interrompue. Ils n'exécutent pas les deux moteurs.

Un journal atomique ne rend pas atomiques les fichiers de régions, inventaires de joueurs et sauvegardes RT. Les marqueurs `RESERVING` / `SETTLING` rendent certains arrêts intermédiaires détectables ; ils imposent une récupération, sans promettre une résistance à toute coupure électrique.

## Mise à jour après une livraison réussie

Conserver le même monde et charger la sauvegarde RT **REWARD_COMMITTED** correspondant à la livraison. Aucun nouvel appairage ni test de persistance n’est nécessaire. Dans un menu RT chargé, le lien reste connecté mais l’envoi est suspendu ; fermer le menu rétablit la disponibilité. La mission déjà soldée reste terminée. Les interrogations périodiques ne doivent plus faire clignoter le bouton ou annuler sa confirmation.
