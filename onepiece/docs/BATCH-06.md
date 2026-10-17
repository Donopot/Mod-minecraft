# Lot06 — Loup, Léopard, Tigre à dents de sabre, Girafe et Batto

État du 17 octobre 2026 : implémentation candidate alpha.15, 31 techniques, 62 phases PAL distinctes et finies. Validation en cours ; aucun résultat artistique humain acquis. Les 28 fruits du lot05 restent figés séparément. Ce lot portera le total à33 fruits et174 techniques lorsqu'il sera vérifié ; il restera7 Zoans et39 techniques.

La référence gameplay est exclusivement MinePiece ver15 Forge1.20.1. Relevés privés et bytecode dans `.runtime/onepiece/b06-source-audit/`, SHA du JAR dans le manifeste de réserve. MMnM0.11.6 fournit uniquement les géométries/textures de Léopard et Girafe, ainsi que des géométries préparées pour les lots suivants. Aucun JAR Forge n'est installé dans Fabric.

## Techniques

Toutes les entrées ont un routage propre, sans repli générique après refus. Préparation8ticks pour Soru,20 pour Rokuogan/AmaneDachi/Kamie et10 pour les autres. Coûts et recharges conservés du catalogue du port. Autorité liée à l'instance exacte du porteur, à son monde et à son fruit ; mort, suppression, changement de fruit, déconnexion et arrêt nettoient leurs états. Les projectiles déjà lancés perdent aussi leur autorité.

Shigan est une frappe au contact ; Soru est un déplacement sûr de6blocs. Rankyaku produit une vraie lame volante, avec une silhouette de loup pour le fruit Loup. Lupus Fall et Gaicho ont des profils physiques distincts. Jushigan utilise les deux mains, Okami Hajiki/Shugan/Yurushitene une charge avec collision, Local Area Network trois projectiles partageant un budget de15dégâts par victime. Aucun soin ou vol de vie inventé : les appels source `m_21153_` règlent la santé des projectiles.

Le bond du Léopard est un arc avec frappe de15 à l'arrivée, conformément à sa délégation source vers AttackOverhead. Attack Speed du Tigre est une charge offensive avec une frappe de11, adaptation explicite du helper StrongPunch/Sword ; ces deux entrées étaient précédemment de simples buffs de vitesse à0dégât. Shigan Madara répartit12dégâts sur3contacts espacés de12ticks ; le projectile Gagan garde15, Kibasen16. Rankyaku Ran émet3lames à6ticks d'écart partageant15dégâts ; AmaneDachi crée une onde circulaire mobile. Rokuogan est une onde de pression bornée à35, portée12.

Tobu Shigan est un projectile fin à2,2dégâts, plutôt qu'un coup de mêlée. Kamie Zanshin n'inflige aucun dégât direct : la constante source999999 sert au contexte de garde et n'est suivie d'aucun appel RangeAttack/hurt. La procédure pose `attack=false`, déplace le porteur puis applique STUN30, amplificateur1. Adaptation : garde de60ticks, une esquive sûre d'au plus0,8bloc toutes les8ticks face à une attaque vivante à6blocs, puis immobilisation30ticks de l'adversaire, portée d'interaction des blocs réduite et attaque/armure/ténacité réduites de20%. Les propres modificateurs sont retirés à échéance ; aucun effet tiers n'est supprimé. La réception du coup doit être vérifiée sur un vrai joueur : `Fabric FakePlayer.isInvulnerableTo` retourne toujours vrai.

La poursuite du Loup est l'ajout propre au port. Elle conserve ses paramètres d'endurance/coût/distance, refuse une seconde activation qui rechargerait son endurance, vérifie le fruit et le monde exacts, et ne supprime plus une potion de vitesse étrangère. Elle passe par la préparation et l'animation de libération communes.

## Formes

CycleV : hybride → animal complet → humain. Les hybrides viennent de ver15 ; les formes animales complètes étendent cette source selon la demande de l'utilisateur. Coût40 par entrée/changement, durée60secondes et maintien1énergie/s sont des choix explicites de ce port. +6attaque, +4armure ; vitesse+20% hybride/+30% animale. La forme ne survit pas à une reconnexion. Refus d'espace avant débit. Aucun bonus ne s'empile lors du cycle.

| Fruit | Forme complète | Corps natif largeur×hauteur / œil |
|---|---|---|
| Loup | Quadrupède gris, museau, oreilles, queue et marche articulée créés en géométrie |1,2×1,2 /0,95|
| Léopard | Géométrie/texture quadrupède de la référence privée MMnM |1,3×1,35 /1,1|
| Tigre à dents de sabre | Rig quadrupède adapté, pelage orange, rayures et défenses créés en géométrie |1,6×1,5 /1,2|
| Girafe | Géométrie/texture de marche de la référence privée MMnM |1,5×3,4 /3,05|
| Batto | Corps complet, oreilles, doigts d'ailes et membranes articulés créés en géométrie |1,2×1,2 /1|

Le modèle quadrupède conserve ses pivots de repos et reçoit les rotations de tête, marche et gestes de combat ; il n'est pas déplacé aux pivots d'un humain. Les dimensions et l'œil passent par un marqueur d'attribut synchronisé, identique côté serveur et observateur. Les ailes hybrides Batto et Phénix choisissent désormais leur variante au sol/en l'air au lieu de superposer les deux maillages.

Batto reçoit le vol seulement s'il n'en possédait pas déjà un. Le jeton persistant ne révoque que ce vol. Aucun décollage automatique ; double saut pour voler. Coût de vol configuré en plus du maintien. Un vol étranger et le mode créatif/spectateur sont préservés.

## Terrain et vérification

31 décisions explicites dans TechniqueTerrain. Soru, poursuite, Shugan, Tobu Shigan, Kamie et Yurushitene préservent les blocs ; les autres impacts physiques ont un quota permanent partagé par tous les projectiles/contacts frères. AIWolfProjectile, AIEnergy et AIProjectileSlash appellent bien BlockDestroy dans la source. Les coffres et leurs contenus, protections, fluides et blocs résistants restent préservés. Aucune restauration temporaire de terrain.

Premier passage natif `run-1e2eece9dde54e31803d6913a9da8690` :491/521B06 PASS,30échecs de cibles chevauchant le mur et subissant l'étouffement. Cinq anciens tests du socle supposaient encore des coups instantanés, une forme Léopard persistante ou une Girafe gérée par l'ancien service géant ; ils ont été actualisés. Les contrôles du service géant utilisent désormais Daibutsu, qui emploie encore ce service. Rapport conservé.

Deuxième passage `run-70161921dca54b288ea4658d94d2d981`, SHA5f673a1dbe3491be5243c0d9a82ee2ea0712aa53a292ae432ff5fe1f5cd1bf43 :534/535B06 PASS et59/59socle PASS. Toutes les techniques, murs, quotas terrain, corps complets et sorties passent ; le contrôle de réception du contre échoue parce que le faux joueur est intrinsèquement invulnérable. Ce rapport est conservé et n'est pas déclaré entièrement PASS. Le nouveau test distingue le service du contre de sa réception native sur un joueur réel. Retest ciblé Batto et tous les contrôles de formes/cycle de vie en cours ; captures groupées après ses prérequis.
