# Lot05 — Kage, Soru, Uta, Imu et Gomu en développement

27techniques. Audits privés : ../.runtime/onepiece/b05-source-audit/SOURCE-AUDIT.md et b05-gomu-readonly-audit.md. Référence gameplay unique MinePiecever15 ; MMnM ne fournit que des références de géométrie. Paquet candidat alpha.14, non livré.

Le port garde ses coûts/recharges et budgets de dégâts déclarés : Kage15/15/15/invocation15/44/Asgard ; Soru15/12/15/20/16/25/30 ; Uta10/10/20/20/soldats15/deuxpoings15+15/Tot18 ; Imu25/contrôle/30 ; Gomu11/20/sixcontacts6/Gear. Ces budgets sont des adaptations du port. Source : notes15, Hebunri12, Bazooka14/portée3 et ImuStrong11 avant puissance ; Island44 est une valeur de destruction et ses dégâts source actifs24. Ne pas appeler les budgets port identiques à ces valeurs source.

Kage : essaim de3chauves-souris partageant15dégâts par victime, lance d'ombre mobile, vrai Doppelman limité240ticks, frappe lourde et Asgard agrandi. Le vol de3ombres et600ticks vient du contrat du port ; la source offre1800ticks et un doublement de taille/portée. Son IA miroir étendue est adaptée en compagnon mobile borné. Ombres rendues sur toutes fins.

Soru : vrais Zeus/Prometheus flottants200ticks, frappe électrique et tackle de feu commandés par leur technique ; Furugora mobile, saut/impact MamaRaid et onde Ikoku. La branche de combinaison PAIR_UUID est adaptée à une onde à quota unique et budget30. Pas de copie d'une mécanique d'une autre version.

Uta : notes physiques espacées et volley rapide à trois angles15degrés ; chaque fratrie partage son budget par victime. Posing et Shield produisent de vraies ondes. Shield garde80ticks et réduit40% des dégâts frontaux avec son propre état, adaptation bornée du maintien source120ticks. Deux variantes natives de soldats, plutôt que les quatre positions de formation source. Tot réel ramené à une silhouette de combat, énergie18àcadence20ticks et quota96sur sa durée240ticks. Aucun domaine de vitesse n'est appliqué par Posing.

Gomu : bras physiques résolus à chaque contact ; six impacts à12ticks d'intervalle, une entité à deux bras pour Bazooka et rétraction finie. Les Gears2/3 s'appuient sur la source ; 4/5 sont des ajouts demandés. Cycle2→3→4→5→base ; durée60secondes, maintien2énergie/s (4énergie/s pour4/5), coût40par changement via technique ou toucheV. Fourth : silhouette gonflée, bras haki, hitbox1,8×2,85, vol accordé seulement si nécessaire et révoqué par jeton propre. Fifth : cheveux blancs et écharpe de nuages créés en géométrie, visage conservé. Les statistiques restent explicites : +20%attaque, vitesse20%/0%/30%/40%, échelle1/1/1,5/1,2. Les formes ne persistent pas en reprenant la partie ; les jetons de vol sont réconciliés.

Imu : l'Arrow source emploie volontairement un bras et la texture imu_arrow ; correction du contrat antérieur. Silent Command, Sovereign Grasp et le voile+4armure restent des choix du mod. Le voile respecte sa configuration, refuse NaN/Infinity et ne retire que son modificateur.

Toutes les27politiques terrain sont explicites dans TechniqueTerrain. Destruction/transformation définitives, quotas communs aux projectiles frères ; coffres, protections et blocs résistants préservés. Contrôles/invocations temporaires. Les31effets enregistrés ont désormais une icône livrée, vérifiée dans le JAR fusionné.

Validation en cours : première recette native sur c912e6a4b0b133d8c15316ac63a9e704488a7358069ada347e38cd078137e99b,449contrôles attendus,59socle. Les ajouts graphiques, volFourth et contrôles électriques ultérieurs nécessiteront leur propre vérification finale. Aucun résultat artistique humain acquis.


Contrôle des premières272captures : flècheImu sur mauvaismesh, texturetex_white presque transparente, Tot trop petit et impactMamaRaid erroné. Corrigés. Le véritable mesh du bras possède un pivotY22 et un décalage animé−2 : sa pose de libération est20pixels au-dessus de son origine. L’ancrer à l’œil produisait donc une barre flottante. ClonegéométriqueB05 rebasé−20pixels, animation relative conservée ; son épaule coïncide avec le rayon physique. ThicknessXY est synchronisé pour les grands Gears. La correction équivalente du vieux Mochi natif reste à reprendre avant la conclusion40fruits.
