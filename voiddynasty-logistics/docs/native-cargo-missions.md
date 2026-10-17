# Missions et cargaisons natives — 0.6.1

Chaque mission repetable consomme les materiaux affiches et cree **une cargaison native complete** de la categorie correspondante. Le joueur choisit avec **Mission precedente / Mission suivante** dans `/voidbridge`.

| Mission / cargaison RT | Cout Minecraft |
|---|---|
| Composants de vaisseau | 64 lingots de fer |
| Carburant | 128 charbons |
| Provisions | 128 pains |
| Textiles | 64 laines blanches |
| Mecanismes | 64 redstones |
| Armes a distance | 64 fleches |
| Armes de melee | 8 epees en fer |
| Armures | 4 plastrons en fer |
| Presents sacres | 32 lingots d'or |
| Xeno-artefacts | 16 perles de l'End |
| Marchandises heretiques | 32 obsidiennes pleureuses |
| Bijoux | 16 emeraudes |
| Torpilles | 64 poudres a canon |
| Evacuation de civils | 128 pommes de terre cuites |

Les quantites sont les choix d'equilibrage du prototype. Les noms de categories, references de blueprints et voies de creation ont ete verifies dans cette installation du jeu. Les categories enumerees sans modele complet verifie (batteries, transuraniens, divers et debris de combats spatiaux) ne sont pas inventees dans ce premier catalogue. La mission d'evacuation represente un convoi de civils finance par ses provisions ; elle ne deplace pas de villageois Minecraft.

## Premier essai natif

1. Charger **testatst** dans Minecraft et la derniere sauvegarde RT **REWARD_COMMITTED** correspondant au monde. Ne pas relancer le test de persistance ni refaire l'appairage.
2. Dans `/voidbridge`, choisir **Composants de vaisseau : 64 lingots de fer**. Avec les 65 lingots de la derniere capture, l'envoi doit laisser 1 lingot.
3. Cliquer **Expedier**, puis **Confirmer la livraison**. La selection et les quantites sont figees pour cet envoi.
4. Attendre la confirmation. Dans RT, ouvrir la gestion des cargaisons et verifier une nouvelle cargaison **Composants de vaisseaux** complete. Le terminal affiche **1 cargaison native livree**. Les anciens 10 points de logistique restent a 10 : ils ne sont pas convertis et ne sont plus la recompense de ces missions.
5. Choisir **Carburant**, deposer 128 charbons et expedier. Une cargaison de carburant doit s'ajouter, avec un nouveau recu. Les autres materiaux du depot restent en place.
6. Fermer proprement les jeux, puis recharger le meme monde et **la derniere nouvelle sauvegarde RT REWARD_COMMITTED**. Verifier cargaisons, compte et absence de nouveau gain sur un recu deja confirme.

## Sauvegarde et reprise

La preparation et l'intention **REWARD_PENDING** sont sauvegardees avant la creation native. Ensuite le mod appelle la meme fabrique `CargoState.Create(BlueprintCargo)` que les recompenses natives, verifie que la cargaison est pleine et releve son identifiant. Il sauvegarde ensemble le recu et la campagne, puis lit `settings.json` et recherche cet identifiant/blueprint dans `player.json` de la nouvelle archive avant de confirmer a Minecraft.

Un chargement interrompu au stade REWARD_PENDING exige une recuperation : il ne cree pas une nouvelle cargaison automatiquement. Conserver les sauvegardes et le depot ; ne pas effacer le journal. Les sauvegardes precedentes et le checkpoint appaire restent les moyens de restauration. Un delai reseau ne rembourse jamais les materiaux reserves.

Les anciens recus de logistique sont conserves avec leur ancien hash et leur recompense. Les nouveaux recus contiennent aussi mission, type de recompense, identifiant de cargaison et blueprint natif. Le registre est borne a 256 recus sans eviction. Une cargaison deja vendue ou utilisee dans RT n'est pas recreee : le recu reste termine.

## Protocole et verification

Les deux modules 0.6.1 utilisent le protocole **v4** et le catalogue `protocol/cargo-missions.v1.json`. Son hash fige recettes, quantites et recompenses natives. Le hash de livraison inclut aussi la mission, le blueprint cible, le numero et le recu precedent. Les schemas sont `live-v4-request.schema.json` et `live-v4-response.schema.json`.

Les tests compiles verifient les 14 empreintes Java/C#/Python, les quantites, la conservation des recus reels, la separation des points historiques et des cargaisons, les intents interrompus, la lecture des entites dans une vraie archive et la deduplication. La premiere attribution native et le rendu du selecteur restent a verifier dans les jeux.
