# Recherche du modèle Amaru — 16 octobre 2026

L'utilisateur a fourni trois JAR à examiner pour la forme géante électrique de Goro. Audit des archives, des modèles et des références de classes, sans installation ni lancement de Minecraft. Aucun changement de référence gameplay du port.

**Résultat actuel : Volt Amaru retrouvé dans Mine Mine no Mi 0.11.6.** La transformation emploie le modèle Java Daibutsu partagé, agrandi et recouvert d'un effet d'énergie. Cette référence a ensuite été adaptée et évaluée en jeu dans alpha.7 ; voir [livraison](GORO-AMARU-2026-10-16.md). L’audit de découverte ci-dessous décrit la référence Forge, distincte de cette adaptation.

## Mine Mine no Mi 1.20.1 — 0.11.6 (nouvelle référence fournie)

Chemin : `C:/Users/Anwender/Downloads/mine-mine-no-mi-1.20.1-0.11.6.jar`.
SHA256 `ceab4866a28236c5f74fc0245dc8a079ce9284b93eee7865ec8414c487de30cc` ; 21 485 453 octets, 7 173 entrées. Cette version appartient à l'intervalle demandé par ompworld.

Chaîne vérifiée par lecture du bytecode : `abilities/goro/VoltAmaruAbility` démarre `ModMorphs.VOLT_AMARU` à la fin de la charge, active son revêtement et retire forme/revêtement lors de l'arrêt. `morphs/goro/VoltAmaruMorphInfo` définit taille, caméra et texture. `ModMorphs$Client` construit un `models/morphs/DaibutsuModel` aux offsets2338..2354, stocke ce modèle en variable7, puis l'emploie pour DAIBUTSU et VOLT_AMARU (offsets2401..2434) via `PlayerMorphRenderer.Factory`. Il existe donc une géométrie effective même sans fichier nommé AmaruModel.

- Géométrie : modèle humanoïde Daibutsu à UV64×64, torse et membres épaissis, bras écartés, tête relevée. `setupAnim` réutilise les animations PlayerModel et corrige leurs positions/échelles. Ce n'est pas seulement le joueur standard agrandi.
- Taille : échelle de rendu2,5 ; enveloppe debout2,4×6,5 blocs, accroupie2,4×6,2 ; hauteur des yeux5,5 ; distance caméra10 ; ombre1,9.
- Peau : skin du joueur ; aucun PNG de corps Amaru autonome identifié. Le rendu électrique est un overlay BODY de type ENERGY, sans texture dédiée. Couleur normale jaune et variante bleue sélectionnée par le pack Goro.
- `textures/abilities/volt_amaru.png` et sa variante dans `packs/blue-goro-pack` sont des icônes. Les sprites Goro sont des particules, pas la géométrie du corps.
- Animations : aucune piste Gecko/PAL ou animationJSON propre à Amaru identifiée ; mouvements humanoïdes hérités et ajustés. `VoltAmaruFlightAbility` ajoute une logique de vol, distincte d'un fichier d'animation de transformation.

Le JAR est Forge1.20.1 ; notre port est Fabric26.3. Sa classe compilée ne peut pas être ajoutée directement : adapter géométrie, rendu ENERGY, dimensions/caméra et cycle de transformation au client/serveur du port. La présence du modèle ne prouve pas encore son rendu ni sa fidélité à la forme anime/manga demandée. Les paramètres de combat de Mine Mine no Mi ne remplacent pas silencieusement notre référence MinePiece ver15.

## MinePiece ver15 Forge1.20.1

Chemin : `C:/Users/Anwender/Downloads/MinePiece-ver15-forge-1.20.1.jar`.
SHA256 `f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704` : identique à la référence déjà inspectée.

Enel dispose de `geo/enel.geo.json`, `animations/enel.animation.json`, `textures/entities/enel.png`, de vêtements et du modèle Java `Modelenel_drum` avec `textures/entities/enel_drum.png`. Ces ressources sont celles d'Enel et de ses accessoires, pas un modèle Amaru identifié.

Vérification au-delà des noms : `KleidersRenderEventProcedure` (branche Goro, offsets15342..15609) crée seulement le modèle de tambours à l'échelle.9375 et copie les poses du joueur. Le modèle compte13boîtes (supports et4tambours) ; ses racines corporelles sont vides. Le tag source can_transform exclut Goro, et KeyTransform ne crée pas sa forme. AIEnel/EnelEntity n'apportent pas de mode géant ou de changement de texture/taille Amaru. Aucun nom Amaru/Amaro/Raijin/ThunderGod n'a été trouvé dans l'inventaire et les constantes de classes.

Conclusion : aucune forme Amaru complète et activable retrouvée dans ce JAR. Les tambours restent réutilisables comme accessoire.

## One Mine Piece World1.3.48

Chemin : `C:/Users/Anwender/Downloads/ompworld-1.3.48.jar`.
SHA256 `e5cc7523f0c0592f4c48c5444f539c1603720d303ae44dfb328af2efc48c3006`.

642entrées, dont391régions `.mca`. Le dossier assets ne contient que deux fichiers de langue et le modèle item log_pose.json : **aucune texture PNG, aucun modèle de personnage, aucun fichier géométrique ou animation de personnage**. Pas de JAR imbriqué contenant de tels modèles. Les références goro_goro_no_mi concernent le loot ; raijin_island désigne une île, pas une forme.

META-INF/mods.toml déclare une dépendance obligatoire au mod **Mine Mine no Mi**, identifiant `mineminenomi`, version **[0.11.5,0.12)**. Il s'agit d'une dépendance différente de MinePiece ver15. Ce JAR de monde ne fournit pas ses ressources de personnage.

Conclusion de cette première recherche : pas de modèle Amaru dans ompworld. L'utilisateur a ensuite fourni le JAR mineminenomi ci-dessus ; la demande de ressource est résolue. Pas de recherche globale du disque ni téléchargement effectué.

## Suite

La référence Amaru est identifiée ; son adaptation et sa validation dans Fabric26.3 sont maintenant livrées dans alpha.7. Aucun nouveau besoin de JAR pour cette recherche. Le lot B01 de cinq fruits reste indépendant ; sa méthode avant tests visuels ne change pas. Données d'inventaire : `.runtime/onepiece/amaru-resource-audit.local.json` depuis la racine du workspace.
