# Assets MinePiece — revue des 40 fruits

Les40 fruits et213 techniques ont été revus à partir des procédures et classes de rendu de MinePiece ver15. Les correspondances de noms seules ont servi à chercher les fichiers ; les décisions ci-dessous reposent sur la source effective.

Cette revue distingue les fichiers originaux, leurs adaptations au moteur26.3 et les ajouts du port. Elle ne signifie pas que chaque technique possède un modèle original distinct. Les scènes et les réserves artistiques sont détaillées dans [la validation alpha.19](ASSETS-ALPHA19-VALIDATION.md).

| Fruit | Techniques | Décision sur les assets |
|---|---:|---|
| aro_aro | 4 | Flèches aroaro_arrow d’origine sur les quatre techniques ; clips arrow1 et attack1_1/2 repris, translation bornée à15% pour le port et léger angle de présentation du tir. |
| batto_batto | 6 | Modèle hybride source conservé et techniques/particules vérifiées ; pas de mesh de doigt supplémentaire prouvé. Forme animale complète adaptée. |
| bisu_bisu | 4 | Soldats et textures biscuit originaux conservés. Masquage visuel près de la caméra FPS, sans modifier invocations ni dégâts. |
| bomu_bomu | 5 | Explosions et particules source conservées. Booger utilise un objet bloc vanilla dans ver15 ; identité SRG f_50710_ non résolue, projectile du port conservé explicitement. |
| gomu_gomu | 4 | Bras originaux rebasés et animation extensible conservés. Gears complets du port restent des adaptations ; ver15 ne fournit pas tous les modèles complets demandés. |
| goro_goro | 7 | Raigo ball_maximum/tex_white2 rétabli et normalisé ; sprites électriques originaux. Amaru reste l’adaptation documentée, aucun modèle Amaru équivalent dans ver15. |
| gura_gura | 6 | Fractures crack avec piste attack source ; balayage entity_slash. Matériau blanc du balayage déclaré comme adaptation de la branche arme. |
| hie_hie | 5 | Partisan et oiseau phoenix/tex_ice originaux ; pointes reprises de Modelwooden_spear2. Ice Age ordinaire reste une adaptation ; le gros IceBall source appartient à une branche amplifiée conditionnelle. |
| hito_hito_daibutsu | 5 | Modèles/couche dorée conservés ; ImpactWave rubble/tex_white_trans source en complément du faisceau ; ImpactBall émet FLASH source, sans rendre le carrier invisible. |
| horo_horo | 5 | Ghost1/Ghost2 et textures source, pivots et orientation recalés sur les projectiles ; Kumacy natif conservé. Variante aléatoire source fixée pour un rendu déterministe. |
| imu | 3 | Bras/flèche originaux avec pivots adaptés à la portée serveur. SilentCommand et SovereignGrasp sont des extensions du port avec visuels adaptés. |
| inu_inu_okuchi_no_makami | 7 | Robe feather_robe_lightblue originale ajoutée à l’hybride. Narikabura conserve le carrier visible du port et émet BLUE source ; forme animale complète adaptée. |
| inu_inu_wolf | 8 | Tête-projectile Jabra et Net originaux ; hybride natif conservé. Forme animale complète adaptée faute d’original joueur complet équivalent. |
| kage_kage | 6 | Clone corrigé en biped/tex_black du joueur ; gecko_moria est un modèle PNJ inapproprié ici. Sprites BAT et effets d’ombre source. |
| kira_kira | 5 | Revêtement diamant natif conservé. Shard/Avalanche/Body du catalogue sont des extensions du port, conserver leurs adaptations documentées. |
| kiro_kiro | 4 | Effets corporels et particules conservés ; aucun modèle projectile original supplémentaire justifié par les procédures. |
| magu_magu | 5 | Poings magma et tête Inugami originaux conservés ; conserver le polissage précédent. Les autres effets utilisent les particules et matériaux source. |
| mera_mera | 7 | Poing Hiken et cœur Entei natifs ; flammes animées de propulsion sur les sept attaques. Enveloppe et taille adaptées au port. |
| mero_mero | 4 | Sprite original Heart sur plan mince adapté au monde ; RotateKick entity_slash source. Pétrification conserve ses textures et couches adaptées. |
| mochi_mochi | 6 | Donuts muso_donatsu/tex_white3 et Yaki fist_small/tex_black originaux. DATA_move=-2 interdit la flamme Yaki ; bras originaux avec pivots adaptés au port. |
| moku_moku | 4 | Tornade White Vine originale et sprites BIG_SMOKE ; WhiteBlow désactive explicitement la couche de flamme dans la source, conserver la fumée. |
| mori_mori | 4 | Racine géante et branches reprises du modèle Java Modelwooden_spear2 à20cubes, UV et pivots conservés ; autres racines principalement blocs et particules. |
| neko_neko_leopard | 7 | Gaicho retrouve sa texture projectile_slash_gaicho ; hybride natif. Forme animale complète adaptée, sans remplacement par un PNJ. |
| neko_neko_sabertiger | 5 | Gagan retrouve projectile_tooth/haki_red avec profil distinct du combo de griffes. Hybride natif, forme animale complète adaptée. |
| nikyu_nikyu | 5 | PadHo et UrsusShock reprennent pad et la couche visible tex_white. Le matériau source et sa silhouette sont conservés ; lisibilité arrière à confirmer humainement. |
| ope_ope | 4 | Débris StoneOpe rubble/tex_stone source. Room, Shambles et Amputation ajoutés par le port gardent leurs visuels adaptés ; aucun original équivalent prouvé. |
| pika_pika | 8 | Géométrie originale de l’épée Amano et texture animée conservées. Les rayons sont principalement des effets et particules, sans modèle supplémentaire prouvé. |
| riki_riki | 5 | RockFragment et texture originaux ; balayage projectile_slash. Le carrier source clear/cloud devient blanc visible dans le port, adaptation explicite. |
| ryu_ryu_brachiosaurus | 5 | Missile original Modelmissile à15cubes repris ; BlackCofee ajoute le matériau haki_yellow au faisceau. Hybride natif et forme complète adaptée. |
| ryu_ryu_pachycephalosaurus | 5 | Hybride natif, charges et particules source conservés ; forme complète adaptée. Aucun nouveau mesh joueur complet original approprié identifié. |
| ryu_ryu_pteranodon | 3 | Hybride joueur original conservé ; forme complète adaptée. Les modèles PNJ disponibles ne remplacent pas une forme joueur complète. |
| soru_soru | 7 | Zeus et Prometheus originaux conservés ; balayage source blanc, foudre et feu adaptés aux épisodes finis. Les cadrages groupés ne prouvent pas tous les détails des visages des homies. |
| suna_suna | 5 | Slashes slash_sand, Barjan entity_slash et GroundDeath tornado natifs ; sprites de sable originaux. |
| supa_supa | 4 | Lames corporelles natives conservées ; projectile_slash rétabli. Matériau blanc sans Haki explicitement adapté. |
| tori_tori_phoenix | 6 | BlueBird corrigé en fist_small/tex_blue visible dans la source ; FLAME_BLUE sur les attaques concernées. Forme complète adaptée conserve son atlas UV compatible. Robe blanche hybride explicitement adaptée. |
| uo_uo_seiryu | 8 | Dragon complet original51os/346cubes conservé. Kaifu slash_green/vertical, Kongo slash_purple/idle1 et matériau Boro flame_tiger rétablis ; faisceau long du port conservé. |
| ushi_ushi_giraffe | 5 | AmaneDachi entity_slash/slash_blue avec idle4 original ; hybride natif, forme animale complète adaptée. |
| uta_uta | 7 | Note3D et cinq couleurs source, guerriers/Tot/bras natifs conservés ; noyaux jaunes source Posing/Shield réduits et masqués lorsque la caméra les traverse, avec anneau lisible conservé. |
| yami_yami | 4 | DarkBall repris du modèle Java original à57éléments ; DarkMatter rubble noir et sprites DARK originaux. |
| zushi_zushi | 6 | Deux météores ball_maximum/meteor d’origine, dimensions normalisées ; débris rubble/tex_stone. |

Référence privée : `f9ecbb8f86148a01def15f1efd20bc33775a2a3633287a4a69fde183a4d09704`. Les2881 fichiers originaux restent en réserve. Le graphe automatique trouve142 noms exacts et186 racines résolues ;27 entrées exigent un alias manuel ou correspondent à une extension du port. Ces chiffres décrivent la recherche, pas le nombre de remplacements.

La passe ajoute53 liaisons de profils visuels,60 fichiers originaux vérifiés octet par octet et8 adaptations de pistes. Trois géométries Java originales sont reprises : sphère sombre57éléments, racines20cubes et missile15cubes. Le dragon Kaido complet original déjà corrigé est conservé. Les formes complètes absentes de ver15 gardent leurs adaptations documentées, avec les modèles hybrides source lorsqu’ils existent.

Les animations PAL et les enveloppes/traînées procédurales restent utiles lorsque la source utilise des particules ou un carrier invisible. Leur présence ne constitue pas un oubli de modèle. Les matériaux blancs Riki/Supa/Gura et la robe blanche Phoenix sont explicitement adaptés.

Source des revues privées : `../.runtime/onepiece/asset-source-audit/READONLY-PHASE01-03-2026-10-17.md`, dumps des huit phases, `phase07-08-review.local.md` et rapports des images. Les captures sont des instants sélectionnés, sans acceptation artistique humaine implicite.
