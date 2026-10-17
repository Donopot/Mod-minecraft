# Livraison actuelle — lot03 alpha.12

Mochi6, Mori4, Kira5, Supa4 et Riki5 :24techniques corrigées,48gestes finis distincts et24décisions terrain explicites. Les18fruits traités représentent95techniques, plus Amaru ; les40fruits ne sont pas déclarés terminés. Voir [le bilan du lot03](B03-VALIDATION-2026-10-16.md) et [la politique terrain](TERRAIN-POLICY.md).

**2102 contrôles serveur couverts PASS par des recettes complémentaires sur un code de jeu identique**. Le paquet final est `eb8a6258e66ea15701e1f86892a8be605eb37a2338383232b273f3fb707ed13e` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,418B03,307historiques,31persistance. Loot, redémarrage et migration du journal ancien inchangé PASS. Mécaniques à160ticks/s, reprise Goro à80, historiques à20. La passe160 garde son échec Goro isolé ; seule sa recette Goro est remplacée dans cette couverture par les142PASS de la reprise.325contrôles distincts ont aussi été exécutés directement sur le paquet final. Les deux seules classes modifiées après la passe complète concernent le diagnostic graphique ; tous les autres octets du JAR sont identiques. Ce résultat ne prouve pas le ressenti ou les performances à20TPS.

Session groupée initiale à deux clients, puis reprises ciblées sur le JAR final : couverture retenue120captures acteur+120observateur,50évaluations de poses par rôle (48phases distinctes et le Pistol de suivi), revêtements/expiration, déchargement et retour d’observateur, dégâts natifs DiamondBody. Dix planches inspectées par l’agent. Ressources et classes de rendu identiques octet par octet au JAR observé. Acceptation artistique humaine en attente.

Tester-Lot-03 ouvre les cinq nouveaux fruits ; Tester-OnePiece et Tester-Terrain offrent18fruits. Profil privé neuf `run-12c860c742964c8a8e4288ab573e3e08`, non lancé à la livraison ; anciens profils et lanceurs sauvegardés. Aucun mod/monde habituel ni VOID modifié. Le terrain reste définitif ; les effets sur entités gardent leurs durées.

---

Historique conservé ; les statuts anciens ne définissent pas la livraison actuelle.

# Lot03 — Mochi, Mori, Kira, Supa, Riki

Demande du joueur de poursuivre par cinq fruits avant tests visuels.24techniques : Mochi6, Mori4, Kira5, Supa4, Riki5. Version de travail alpha.12, référence mécanique exclusive MinePiecever15. Terrain toujours définitif ; chaque technique reçoit sa politique et son quota avant validation. Les anciens contrats sont des hypothèses historiques, pas une preuve du comportement source.

Trois audits en lecture seule ; root seul édite. Baseline alpha.11 et sources sauvegardées dans `.runtime/onepiece/before-b03-source.zip` et `build/before-b03-alpha11.jar`. Aucun monde/mod habituel changé.

Défauts prouvés : Supa transforme même Projectile Slash en mêlée instantanée ; Mori Giant/Surround/Assault remplacent les dégâts par croissance/immobilisation/drainage ; Mochi Gatling soumet ses coups dans le même tick et colle parfois après refus ; Riki simule le lancer par une poussée et casse en avance le décor. Les helpers Supa/Riki ignorent les protections génériques et doivent passer par TechniqueTerrain. Les postures Supa effacent des potions externes ; les corps Kira/Supa n’ont pas de rendu actif lorsque player_form vaut0.

MusoDonatsu source est une invocation qui suit le porteur et réagit à ses gestes, pas un projectile isolé. Les racines source utilisent TreeRoot/AITree avec conversion locale de sol en herbe ; les racines géantes ne sont pas un cube de feuilles. Les setters ATTACK_DAMAGE dans les projectiles Riki ne prouvent aucun multiplicateur d’attribut du porteur : celui du contrat historique est une adaptation, à séparer des valeurs source.

Développement, recette serveur par technique, puis une session groupée acteur/observateur. Les résultats seront associés au hash final et aux captures réellement inspectées. Aucun résultat nouveau ni acceptation artistique revendiqué à cette étape.


## Réalisation et adaptations explicites

Les dégâts de catalogue sont conservés comme budgets de port. Les setters Forge f_22276_15/20/50/200 sont MAX_HEALTH, f_22281_ est ATTACK_DAMAGE ; confondre ces champs aurait multiplié artificiellement les dégâts. Le chemin source passe par le NBT Damage, RangeAttack et DamageFix. Les audits privés désassemblés sont conservés dans `.runtime/onepiece/b03-audit`.

48gestes PAL finis et distincts (préparation/libération) ; retour neutre des os. Bras Mochi, pierres Riki, lame Supa et revêtements natifs réutilisés en privé. Racines procédurales ligneuses, cristal facetté, éventail et coupes bilatérales distincts. Les deux anneaux réagissent aux gestes offensifs au lieu de frapper automatiquement.

Les tirs renvoyés par Nikyu gardent la forme, l’effet et le budget de dégâts du projectile, sous l’autorité du déviateur ; ils cessent de modifier les blocs. Cette restriction volontaire évite d’emprunter l’autorisation terrain de l’ancien porteur ou de renouveler ses quotas. Les effets visuels sont aussi liés à l’objet joueur exact, au fruit et à sa dimension.

Recette native : cinq scènes par technique (ouverte, mur obsidienne, mur pierre, allié seul dans l’axe, invulnérable seul dans l’axe), dégâts exacts, coût/recharge, attribution capturée au coup avant expiration vanilla100ticks, cadence Gatling, Muso réactif, quotas, coffres. Les joueurs FakePlayer du banc reçoivent explicitement un tick de recharge ; les victimes sont réellement insérées et tickées. Expirations et invalidation après départ du projectile sont mesurées sur des ticks serveur réels. La vérification d’animation serveur porte seulement sur le nom/fichier ; l’évaluation des poses et les images appartiennent à la recette groupée client.

Ancien G4 Gomu : le helper immédiat ne peut pas annoncer six coups acceptés dans un même tick. Le diagnostic indépendant est corrigé pour confronter le total retourné à la perte réelle de HP ; le Gatling Gomu conserve sa dette de cadence pour son propre lot. Le nouveau Gatling Mochi possède, lui, six coups natifs espacés et24HP mesurés. Gomu ne fait pas partie des fruits déclarés terminés ici.
