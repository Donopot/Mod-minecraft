# Magu Magu — finition des animations, alpha.5

Après le retour positif sur alpha.4, l'utilisateur a demandé une finition des animations. Les cinq techniques gardent leurs gestes distincts et leurs valeurs de gameplay.

| Technique | Finition |
|---|---|
| Dai Funka | Charge avec respiration discrète, extension du poing sur 0,10 s, torsion puis retour amorti |
| Inugami Guren | Appui des deux bras, projection avec transfert du poids et retour progressif |
| Kagero | Foulées alternées, préparation basse puis frappe à 0,75 s dans la finition, vagues courbes au sol |
| Volcanic Explosion | Compression, ouverture à 0,75 s et impulsions à 1 / 1,5 s ; jets effilés avec croûte sombre et filaments chauds |
| Ryusei Kazan | Montée des bras progressive, frappes alternées et préparation de magma sans saut périodique de hauteur |

Les huit pistes sont générées par `tools/author_magu_animations.py` : courbes Hermite monotones échantillonnées en clés vectorielles PAL standard. Aucun changement du parseur ni dépendance supplémentaire. Les charges tiennent leur pose jusqu'à la libération forcée à 5 s ; toutes les pistes terminent au neutre. Le visuel de charge suit désormais les changements de visée.

Les ondes Kagero conservent une première vague de rayon 4 quand les coups extérieurs passent au rayon 12 ; les vagues extérieures et les jets Volcanic se déploient progressivement. Une fine limite de portée annonce le grand rayon. Les gerbes conservent leur silhouette verticale et Kagero ses effets bas. Les protections de caméra FPS et VISUAL_READY sont conservées.

La traînée procédurale d'un projectile passe de 864 à 84 quads (environ 90 % de moins pour cette seule couche). Ce calcul géométrique ne constitue pas une mesure de FPS ; les meshes natifs et particules restent présents.

## Essai

Fermer l'ancienne fenêtre de test puis relancer [Tester-Magu.cmd](C:/Users/Anwender/Documents/ChatGPT/codexmod/onepiece/Tester-Magu.cmd). Nouveau profil isolé et neuf `run-cc3436916e724597b1221682609a1bbc`, préparé sans lancement. R maintenir/relâcher, B technique suivante, F5 caméra. Le profil Mera précédent et les parties habituelles sont conservés.

## Vérifications sur le JAR livré

Version **1.0.1-alpha.5**, SHA256 `b991ff05a4b3dca6fc92dfb4d0c1e4788c2e8302cff947a1c645c953546fc35a`, 258 sources Java.

- [Serveur](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-7ea32bb936ba46989f35d65ca80c29da/report.local.json) : **74 Magu + 92 Mera + 59 socle + 307 historiques PASS**, loot et redémarrage réussis, processus arrêté.
- [Deux clients graphiques](C:/Users/Anwender/Documents/ChatGPT/codexmod/.runtime/onepiece-lab/run-e6f94cfc104643cdbab9a6a0585abc8c/visual-lab.local.json) : **8 gestes PAL vérifiés sur chaque client** (UUID attendu, temps qui avance et os réellement transformés), dash réel et durée d'éruption vérifiés ; **55 captures acteur + 40 observateur**, processus arrêtés.
- Quinze images finales réellement ouvertes : charges/tirs, préparation et vagues Kagero, montée et dispersion Volcanic, émission Ryusei et trois vues FPS. La première vue Dai à 30 arrive avant le chargement complet de la scène ; celle à 40 montre la charge. Les captures fixes ne mesurent pas à elles seules la fluidité temporelle ni la synchronisation à l'image près.
- Audit des huit pistes distinctes, finies et bornées PASS ; chargement client **17 cibles PASS** ; ressources **625 JSON / 83 paires / 0 erreur**, quatre anciens avertissements UV Abyss.

L'appréciation de cette finition reste à confirmer en jouant. Les variantes source déjà documentées, les cycles de vie réels et l'essai du modpack complet ne sont pas ajoutés à cette itération. Aucune acceptation globale des 40 fruits n'est déclarée. [Contrat Magu](fruits/MAGU_MAGU.md).
