# Mera — dégagement de la caméra, alpha 21

Cette reprise corrige la visibilité des effets près de la caméra. Le catalogue reste de 40 fruits et 213 techniques, avec les commandes configurables de l'alpha 20.

- Les sprites `FLAME` et `FLAME_SMALL` s'atténuent près de la caméra réelle en vue subjective, arrière et de face. La distance prend en compte la diagonale et la taille actuelle du sprite, ainsi que sa position interpolée. Les sprites éloignés gardent leur taille ; le feu reste émis dans le monde.
- Hiken masque son mesh lorsqu'il englobe la caméra. Sa marge suit l'échelle du poing chargé.
- Higan masque aussi sa traînée de deux blocs avant qu'elle ne dégage la caméra. Une première inspection a montré que protéger seulement le centre de la petite balle laissait encore un flash orange au départ.
- Entei et sa sphère de charge partagent une marge adaptée à leur échelle. Le noyau natif et les flammes restent visibles à distance.
- Le modèle peut arriver avant le profil de technique, car la fabrique ajoute le projectile au monde avant l'affectation finale du profil. Dans cet état incomplet, une marge locale masque le mesh près de la caméra. Les règles propres aux autres fruits complètent cette décision sans l'écraser. Les journaux de recette enregistrent la décision finale.

Les classes serveur sont identiques octet par octet à l'alpha 20. Les dégâts, coûts, recharges, sauvegardes et destructions définitives de terrain sont conservés. La protection des deux sprites orange s'applique aussi aux autres techniques qui emploient ces mêmes sprites ; les autres familles de particules ne changent pas.

## Vérification

Compilation des 327 sources et test Java autonome du dégagement : réussis. Le test balaie la transition d'atténuation et vérifie les cas de proximité, l'observateur éloigné et les marges des tirs chargés.

La scène Mera couvre les sept techniques, la propulsion et les trois vues caméra, avec deux clients réels dans un monde jetable. La recette attend 51 captures acteur et 24 observateur, vérifie le déplacement réel, la durée des effets et les décisions de visibilité des sprites et tirs. Le premier passage a conservé son arrêt de contrôle sur l'ancien total de 48 captures et son défaut Higan ; la reprise utilise le contrôle mis à jour.

Le candidat final `93687e13497210097f3ba00932bb9ad38b96ad947ad5e9de628ee09b3cd34505` compile et passe les 59 contrôles natifs du socle dans un monde neuf. La scène fournit 51 captures acteur et 24 observateur, réunies sur cinq planches effectivement inspectées ; le départ Higan est aussi inspecté à pleine résolution. Le déplacement réel de propulsion et les deux durées d'effets contrôlées réussissent. Les sept émetteurs de techniques sont présents sur les deux clients. Les contrôles des images centrales Hiken, Higan et Entei passent après correction du flash Higan. Les caméras subjective, arrière et de face sont observées. Les sprites loin de la caméra et les noyaux des tirs restent visibles.

Les tentatives précédentes restent conservées : arrêt sur l'ancien total de captures, puis flash Higan encore visible malgré une décision intermédiaire annoncée comme masquée. Le contrôle final du lanceur a également exigé à tort `hiken:hidden` même lorsque la première image était correctement masquée au stade `pending-profile:hidden`. Sa condition est corrigée pour accepter ces deux étapes valides. La recette finale est donc revalidée hors ligne sur ses journaux et captures complets, avec le contrôle central des pixels ; l'ancien résultat d'arrêt reste conservé. Les processus des quatre passages sont arrêtés.

Cela ne constitue ni une validation artistique humaine ni une nouvelle recette exhaustive des 40 fruits. Les grandes flammes éloignées, le noyau Entei et les débris de blocs restent volontairement visibles ; cette passe n'en fait pas une refonte artistique.

## Suite

Vérifier les ailes du Phoenix, puis les détails des compagnons de Soru. Le rapprochement de la caméra vanilla dans les cratères reste une réserve distincte. Aucune installation habituelle ni aucun monde personnel n'est modifié par les laboratoires.
