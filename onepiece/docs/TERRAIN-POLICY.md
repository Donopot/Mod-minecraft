# Terrain — décisions par technique

Politique actuelle : [tous les blocs modifiés restent définitifs](TERRAIN-DEFINITIF-alpha11.md), à la demande explicite du joueur. Cette table couvre les116techniques des23fruits traités, plus Amaru. Les autres fruits restent à évaluer ; leurs anciennes mentions de restitution ne définissent plus le comportement du service commun.

Les appels MinePiece ver15 ont été suivis jusqu’aux projectiles et convertisseurs. BlockDestroyAllDirection utilise une largeur (rayon nominal `(BlockRange+1)/2`), BlockToIce/BlockToSand un rayon. Les volumes ci-dessous sont des adaptations bornées pour le port, pas une reproduction générale des volumes source.

La charge ne creuse pas. Le contact ou la phase active partage un quota entre tous les projectiles du lancement. Les dégâts voient les obstacles présents avant la destruction : un météore arrêté sur un toit ne blesse pas fictivement à travers celui-ci. Les techniques de déplacement sans mutation restent sans mutation.

| Fruit | Technique | Décision | Rayon terrain max | Blocs retirés max / lancement | Origine / phase |
|---|---|---|---:|---:|---|
| aro_aro | arrow | Destruction définitive | 2 | 48 | AIArrow actual contact; bounded adaptation |
| aro_aro | combo | Aucune mutation | — | — | Source movement/contacts have no block destruction |
| aro_aro | move | Aucune mutation | — | — | Source movement/contacts have no block destruction |
| aro_aro | passive | Aucune mutation | — | — | Source movement/contacts have no block destruction |
| bisu_bisu | biscuit | Aucune mutation | — | — | Summons and food creation preserve terrain |
| bisu_bisu | biscuitwarrior | Aucune mutation | — | — | Summons and food creation preserve terrain |
| bisu_bisu | biscuitwarrior2 | Aucune mutation | — | — | Summons and food creation preserve terrain |
| bisu_bisu | crashpretzel | Aucune mutation | — | — | CrashPretzel RangeAttack and Knockback; no BlockDestroy call |
| bomu_bomu | bomboverhead | Destruction définitive | 4 | 96 | B02 source audit; active collision only, bounded adaptation |
| bomu_bomu | kickbomb | Destruction définitive | 3 | 64 | B02 source audit; active collision only, bounded adaptation |
| bomu_bomu | nosefancycannon | Destruction définitive | 2.5 | 48 | B02 source audit; active collision only, bounded adaptation |
| bomu_bomu | punchbomb | Destruction définitive | 3 | 64 | B02 source audit; active collision only, bounded adaptation |
| bomu_bomu | zenshinkibaku | Destruction définitive | 5 | 160 | B02 source audit; active collision only, bounded adaptation |
| goro_goro | amaru | Aucune mutation | — | — | Transformation itself preserves terrain; technique budgets unchanged |
| goro_goro | elthor | Destruction définitive | 3 | 48 | ElThor active strike |
| goro_goro | karikrash | Destruction définitive | 2.5 | 32 | KariKrash front contact |
| goro_goro | mamaragan | Aucune mutation | — | — | Storm has no explicit source BlockDestroy; blink has none |
| goro_goro | passive | Aucune mutation | — | — | Storm has no explicit source BlockDestroy; blink has none |
| goro_goro | raigo | Destruction définitive | 4 | 128 | AIMeteorRaigo true collision |
| goro_goro | sango | Destruction définitive | 1 | 16 | Sango accepted contacts only, four blocks per contact |
| goro_goro | vari | Destruction définitive | 3 | 48 | Vari arrival only; blink never tunnels |
| gura_gura | attacksweep | Destruction définitive | 1.5 | 24 | B02 source audit; active collision only, bounded adaptation |
| gura_gura | gekishin | Destruction définitive | 2.5 | 96 | B02 source audit; active collision only, bounded adaptation |
| gura_gura | kabutowari | Destruction définitive | 1.5 | 16 | B02 source audit; active collision only, bounded adaptation |
| gura_gura | kaishin | Destruction définitive | 3 | 96 | B02 source audit; active collision only, bounded adaptation |
| gura_gura | overheadsuper | Destruction définitive | 3 | 64 | B02 source audit; active collision only, bounded adaptation |
| gura_gura | tenchimeido | Destruction définitive | 3 | 256 | B02 source audit; active collision only, bounded adaptation |
| hie_hie | iceage | Transformation définitive | — | — | BlockToIce; permanent ice |
| hie_hie | iceball | Transformation définitive | — | — | BlockToIce; permanent ice |
| hie_hie | icepartisan | Destruction définitive | 2.5 | 64 | AIEntityBird collision |
| hie_hie | icespikes | Transformation définitive | — | — | BlockToIce; permanent ice |
| hie_hie | pheasantbeak | Destruction définitive | 3 | 96 | AIEntityBird collision |
| horo_horo | kumac | Aucune mutation | — | — | Control, command and summon preserve blocks |
| horo_horo | minihollow | Destruction définitive | 2 | 48 | AIMiniHollow commanded explosions share48blocks |
| horo_horo | negativehollow | Aucune mutation | — | — | Control, command and summon preserve blocks |
| horo_horo | rap | Aucune mutation | — | — | Control, command and summon preserve blocks |
| horo_horo | tokuhoro | Destruction définitive | 3.5 | 96 | Large AIMiniHollow explosion |
| kira_kira | attackoverhead | Destruction définitive | 4.5 | 128 | AttackOverhead actual active contact |
| kira_kira | brilliantpunk | Destruction définitive | 1.5 | 48 | AttackStrongPunch after shoulder rush |
| kira_kira | diamondavalanche | Destruction définitive | 4.5 | 192 | Large RockFragment handler, bounded adaptation |
| kira_kira | diamondbody | Aucune mutation | — | — | Body coat never changes terrain |
| kira_kira | diamondshard | Destruction définitive | 1.25 | 48 | RockFragment handler; diamond identity adaptation |
| kiro_kiro | attackjump | Destruction définitive | 3 | 64 | AttackJump delegates overhead on landing |
| kiro_kiro | attackoverhead | Destruction définitive | 3 | 64 | AttackOverhead contact |
| kiro_kiro | kirooverhead | Destruction définitive | 4 | 96 | Kiro overhead landing |
| kiro_kiro | weightmanipulation | Destruction définitive | 3 | 64 | Source grounded erosion adapted to one heavy landing, never idle ticks |
| magu_magu | daifunka | Destruction définitive + magma définitif | 2.5 | 48 | AIFist impact; permanent magma blocks, finite attributed field |
| magu_magu | inugamiguren | Destruction définitive + magma définitif | 2 | 32 | AIFist impact |
| magu_magu | kageromagumagu | Destruction définitive + magma définitif | 1.5 | 24 | KageroMagu active path and eruption |
| magu_magu | ryuseikazan | Destruction définitive + magma définitif | 2 | 160 | AIFist2; twenty meteor impacts share160 blocks |
| magu_magu | volcanicexplosion | Destruction définitive + magma définitif | 4 | 96 | VolcanicExplosion active eruption |
| mera_mera | daienkai | Destruction définitive | 6 | 160 | AIFlameBall actual impact |
| mera_mera | hibashira | Destruction définitive | 4 | 96 | Hibashira BlockRange16; bounded adaptation |
| mera_mera | higan | Destruction définitive | .9 | 8 | ProjectileLaser block impact; four shots share8 blocks |
| mera_mera | hiken | Destruction définitive | 3 | 48 | AIFist; charged radius capped at3 |
| mera_mera | hotarubi | Destruction définitive | 2.5 | 32 | Hotarubi expanding active field |
| mera_mera | kaenryuo | Destruction définitive | 4 | 64 | KaenRyuo active stages |
| mera_mera | kagero | Destruction définitive | 1 | 12 | Kagero active trail only |
| mero_mero | perfumefemur | Aucune mutation | — | — | Heart projectile and AttackSpeed dash preserve terrain |
| mero_mero | pistolkiss | Aucune mutation | — | — | Heart projectile and AttackSpeed dash preserve terrain |
| mero_mero | rotatekick | Destruction définitive | 2 | 48 | RotateKick active phase |
| mero_mero | roundhousekick | Destruction définitive | 1.5 | 32 | Karate roundhouse active contact |
| mochi_mochi | mochibuild | Transformation définitive | — | — | Plan-added honey construction, permanent24blocks |
| mochi_mochi | mochigatling | Destruction définitive | 1 | 48 | AttackWeakPunch six native pulses share quota |
| mochi_mochi | mochipistol | Destruction définitive | 1.25 | 24 | AttackStrongPunch true contact, bounded adaptation |
| mochi_mochi | musodonatsu | Destruction définitive | 1.25 | 64 | Two reactive rings share a lifetime terrain quota |
| mochi_mochi | yakimochi | Aucune mutation | — | — | AIFist mode -2 has no mode1 block destruction |
| mochi_mochi | zangirimochi | Destruction définitive | 1.5 | 48 | AttackSpeed finishing contact |
| moku_moku | whiteblow | Destruction définitive | 2.5 | 64 | AIFist collision |
| moku_moku | whitelauncher | Aucune mutation | — | — | StepBlock is movement, not destruction |
| moku_moku | whiteout | Destruction définitive | 8 | 256 | WhiteOut active field; source radius16.5 reduced |
| moku_moku | whitevine | Destruction définitive | 3 | 96 | AITornado collision |
| mori_mori | assault | Transformation définitive | — | — | TreeRoot and BlockToGrass, permanent selective grass conversion |
| mori_mori | root | Transformation définitive | — | — | TreeRoot and BlockToGrass, permanent selective grass conversion |
| mori_mori | rootgiant | Transformation définitive | — | — | TreeRoot and BlockToGrass, permanent selective grass conversion |
| mori_mori | rootsurround | Transformation définitive | — | — | TreeRoot and BlockToGrass, permanent selective grass conversion |
| nikyu_nikyu | assault | Destruction définitive | 2.5 | 64 | B02 source audit; active collision only, bounded adaptation |
| nikyu_nikyu | kumapunch | Destruction définitive | 2 | 64 | B02 source audit; active collision only, bounded adaptation |
| nikyu_nikyu | padho | Destruction définitive | 2 | 48 | B02 source audit; active collision only, bounded adaptation |
| nikyu_nikyu | passive | Aucune mutation | — | — | Travel never tunnels |
| nikyu_nikyu | ursusshock | Destruction définitive | 6 | 192 | B02 source audit; active collision only, bounded adaptation |
| ope_ope | amputation | Aucune mutation | — | — | Surgery does not destroy terrain |
| ope_ope | room | Aucune mutation | — | — | Surgery does not destroy terrain |
| ope_ope | shambles | Aucune mutation | — | — | Surgery does not destroy terrain |
| ope_ope | stoneopeope | Destruction définitive | 2 | 96 | B02 source audit; active collision only, bounded adaptation |
| pika_pika | amanomurakumo | Aucune mutation | — | — | Travel or weapon grant, no terrain impact |
| pika_pika | kick | Destruction définitive | 1.5 | 32 | Light kick active impact |
| pika_pika | laser1 | Destruction définitive | 2.5 | 64 | Laser BlockRange4 -> radius2.5 |
| pika_pika | laser2 | Destruction définitive | 4.5 | 160 | Laser2 BlockRange8; shared volley budget |
| pika_pika | magatama | Destruction définitive | 1.5 | 96 | ProjectileLaser block impact; shared volley budget |
| pika_pika | passive | Aucune mutation | — | — | Travel or weapon grant, no terrain impact |
| pika_pika | speedispower | Destruction définitive | 2 | 48 | SpeedIsPower active strike |
| pika_pika | yatanokagami | Aucune mutation | — | — | Travel or weapon grant, no terrain impact |
| riki_riki | attackoverhead | Destruction définitive | 4.5 | 128 | AttackOverhead active impact |
| riki_riki | galleonlariat | Destruction définitive | 3 | 96 | AIProjectileSweep actual collision |
| riki_riki | hadoelbow | Destruction définitive | 3.5 | 128 | HadoElbow active impact |
| riki_riki | stonethrowing | Destruction définitive | 1.25 | 48 | RockFragment actual collision; no held target required |
| riki_riki | stonethrowinglarge | Destruction définitive | 4.5 | 192 | Large RockFragment actual collision |
| suna_suna | barjan | Aucune mutation | — | — | StepBlock and slash visual, no destruction path |
| suna_suna | desertlaspada | Destruction définitive | 2 | 96 | AIProjectileSlash collision |
| suna_suna | desertspada | Destruction définitive | 1.5 | 64 | AIProjectileSlash collision |
| suna_suna | grounddeath | Transformation définitive | — | — | BlockToSand; permanent stable sandstone surface |
| suna_suna | sables | Destruction définitive | 3.5 | 160 | AITornado; bounded field |
| supa_supa | atomicspurt | Aucune mutation | — | — | Source StepBlock dash, charge explosion interaction NONE |
| supa_supa | projectileslash | Destruction définitive | 1.5 | 64 | AIProjectileSlash true collision |
| supa_supa | sparbreak | Destruction définitive | 1 | 64 | Bilateral slash active contact |
| supa_supa | sparklingdaisy | Destruction définitive | 1 | 96 | Six source fan phases share quota, port bounded contact |
| yami_yami | blackhole | Destruction définitive | 8 | 384 | BlackHole active field; source radius24.5 reduced |
| yami_yami | darkball | Destruction définitive | 3.5 | 128 | AIBall collision |
| yami_yami | darkmatter | Destruction définitive | 4.5 | 192 | AIFlameBall collision |
| yami_yami | kurouzu | Aucune mutation | — | — | Attraction, no destruction path |
| zushi_zushi | attackspeed | Destruction définitive | 1.5 | 32 | B02 source audit; active collision only, bounded adaptation |
| zushi_zushi | gravito | Destruction définitive | 3 | 96 | B02 source audit; active collision only, bounded adaptation |
| zushi_zushi | meteor | Destruction définitive | 5 | 192 | B02 source audit; active collision only, bounded adaptation |
| zushi_zushi | meteorsmall | Destruction définitive | 3 | 96 | B02 source audit; active collision only, bounded adaptation |
| zushi_zushi | moko | Destruction définitive | 6 | 160 | B02 source audit; active collision only, bounded adaptation |
| zushi_zushi | rubble | Destruction définitive | 2 | 64 | B02 source audit; active collision only, bounded adaptation |

Les transformations ont leurs propres bornes : IceSpikes six bases ; IceBall chemin de surface jusqu’à16blocs/16cases ; IceAge eau de surface rayon8/128cases ; GroundDeath surface de terre/pierre/sable rayon12/profondeur1/128cases. PACKED_ICE et SANDSTONE restent dans le monde. L’attribution expire après200ticks ; elle ne restaure rien.

Ryusei partage160blocs entre20météores,12au plus par impact ; Higan partage8blocs/2par tir. Laser2 et Magatama partagent aussi leurs quotas. Sango ne creuse qu’aux contacts blessants acceptés,4blocs par contact/16au total. Tenchi partage256blocs entre ses fronts ; Stone Ope partage96blocs entre ses quatre pierres. Raigo ne creuse pas en vol. Amaru ne creuse pas et n’augmente aucun quota.

Mori partage128conversions sélectives de sol par lancement : rayon3aux impacts des racines, rayon6pour Assault/Surround. Mochi Build crée au plus24blocs sans ensevelir les entités ; ils restent définitifs. Les poings des deux anneaux Muso partagent64blocs sur leur durée de vie. Yaki Mochi et Atomic Spurt ne creusent pas. Un tir dévié ne garde aucune autorisation de mutation du terrain de son ancien porteur.

Les impacts destructeurs refusent chunks absents, extérieur de frontière, protections, inventaires/BlockEntity, blocs indestructibles ou résistance>6, fluides et attributions déjà actives. Les obstacles résistants protègent les blocs derrière eux. Plusieurs couches ordinaires peuvent être excavées dans le quota.

La configuration privée `config/opworldlike/technique-terrain.json`, section `enabled`, peut désactiver une destruction ; elle n’active jamais une technique NONE. Un fichier illisible bloque les destructions permanentes. Les anciens réglages de durée des blocs règlent seulement leur attribution, pas leur durée dans le monde. Les champs de dégâts et contrôles sur entités gardent leurs durées distinctes. Les blocs de magma restants retrouvent leur comportement chaud vanilla après le champ attribué.

Les preuves alpha.9 restent figées dans `build/terrain-alpha9-verification.local.json` et l’ancienne table dans `build/terrain-policy-alpha9.md`. La permanence du socle est prouvée en alpha.11 ; les décisions supplémentaires et les preuves du lot03 sont consignées dans [le bilan alpha.12](B03-VALIDATION-2026-10-16.md). Les anciennes promesses de restitution sont remplacées par la demande du joueur.
