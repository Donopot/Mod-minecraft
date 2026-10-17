# Lot01 alpha.8 — Hie, Pika, Moku, Suna, Yami

Les cinq fruits et leurs26 techniques ont leurs routes dédiées, dégâts/effets attribués, protections et phases d’animation. Les services communs nettoient leurs seuls états lors des changements de fruit, monde, mort et déconnexion. Le détail des budgets et adaptations est dans [l’implémentation](B01-IMPLEMENTATION-2026-10-16.md).

JAR final : `opworldlike-1.0.1-alpha.8.jar`, SHA256 `a3d61c1dc092bc3fe99c3de32f94ac145a1cee14a592b05f5f63945ac3b507c4`. Aucun mod ordinaire, monde personnel ou module VOID n’est remplacé. Les JAR de référence et les lanceurs Mera/Magu/Goro restent conservés.

## Preuves serveur

**711 contrôles PASS sur le JAR final** :345B01+59 socle+307 historiques ; loot et redémarrage PASS. Rapport `run-c3fe24997a6f48c9bbbad409cda27a28/report.local.json`. Le processus du laboratoire est arrêté.

| Fruit | Techniques | Contrôles propres B01 |
|---|---:|---:|
|Hie Hie|5|60|
|Pika Pika|8|100|
|Moku Moku|4|50|
|Suna Suna|5|62|
|Yami Yami|4|52|
|Socle partagé B01|—|21|

Le candidat `e6bbac…` a passé92Mera+74Magu+142Goro et345B01 ; sa mesure historique A7c avait échoué à l’aube. Ce rapport global reste **failed** et n’est pas présenté comme un passage complet. Le diagnostic en plein jour `run-d4e91bcbc6054cc0bc99ba9607d79fa5` reproduit exactement l’échec et trace `onFire` sur les deux zombies, sans projectile sur le témoin couvert. Le harnais remet la nuit avant les témoins différés ; leur critère de20 PV à couvert reste strict.

La comparaison complète des entrées JAR (`build/batch01-final-diff.local.json`) confirme que les classes de gameplay sont identiques entre `e6bbac…` et le final. Seuls le driver de reprise, trois classes client et la classe de test historique diagnostique diffèrent. Les308 contrôles Mera/Magu/Goro du candidat sont donc des preuves sur le même code de gameplay, avec leurs propres hash d’exécution, et ne sont pas annoncés comme relancés sur le hash final.

## Preuves client et inspection

La session groupée `run-61db2816dadc457a98ab2c5ba70b4476` couvre les cinq fruits :104 captures acteur+104observateur,33+26 contrôles de rendu/déplacement/sabre,50 phases PAL effectivement évaluées par client et5 contrôles natifs Yami PASS. Ses10 planches ont été ouvertes et inspectées. Le premier essai antérieur avait révélé les sprites trop grands ; la session ci-dessus montre leurs corrections et a motivé les derniers gardes de caméra/HUD.

La reprise ciblée Pika/Moku/Yami `run-391cf870fa5448a4b1b154a243d6211a` passe sur `7be6d0…` :64+64 captures,22+16 contrôles client,30 phases PAL par client et5 contrôles Yami. Ses6 planches ont été ouvertes. Le passif Pika affiche maintenant «Pas lumineux» ; Magatama reste une perle distincte, les rayons sont visibles en observateur, le sabre inflige7.872 dégâts sur la cible réelle. Les six déplacements dépassent2 blocs. Les compteurs d’entrée du renderer ne prouvent pas seuls la visibilité : certains effets sont volontairement omis près de la caméra FPS, et la visibilité a été contrôlée séparément dans les planches acteur/observateur. Les meshes White Vine et Dark Ball restent visuellement volumineux très près du départ ; ils ne provoquent plus le plein écran opaque de Dark Matter. L’avis artistique humain reste attendu.

Entre cette reprise et le final, une seule entrée JAR diffère : `DevilFruitTestCommands.class`, journal diagnostique limité au laboratoire. Toutes les classes client, animations et ressources sont identiques. Aucun nouveau passage graphique n’est déduit d’un nom de paquet : les captures et poses renvoient à leurs hash réels.

## Limites de la preuve

Les tests serveur emploient un FakePlayer comme lanceur et de vrais mobs insérés comme victimes. Les déplacements des victimes sont réellement tickés ; leur IA de navigation est neutralisée sans désactiver leur physique. Le test Yami natif emploie `hurtServer` sur le vrai joueur avec cooldown de dégâts neutralisé, pas une cadence naturelle d’attaques. L’expiration accélérée d’absorption est une horloge manuelle explicitement étiquetée. Les pistes sont mesurées sur UUID/temps/pose ; la qualité artistique ne se déduit pas des compteurs.

Les52 pistes sont distinctes, finies et reviennent au neutre ; les12 pistes Goro et8Magu sont préservées. Audit ressources :691 JSON,83paires de techniques,0 erreur,4 warnings anciens. Pas de benchmark FPS, pas de déclaration des40 fruits terminés, pas de publication des ressources privées.

## Essai manuel

[Guide](TESTER-LOT-01.md). `Tester-Lot-01.cmd` choisit un des cinq fruits et démarre un monde neuf privé, profil `run-c7fb4cef17cc4fd6ad31e5d44b855bb2`. Le profil n’a pas été lancé. R : technique ; B : suivante ; F5 : caméra. Le lanceur réutilise ensuite ce monde jetable. L’alias Tester-OnePiece pointe vers ce nouveau lanceur ; les trois lanceurs précédents sont conservés.
