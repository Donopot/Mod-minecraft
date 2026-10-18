# Servitor IA — alpha 7 : récupération, mémoire par monde, routines

Ce lot reprend l'alpha 6 et traite, dans l'ordre recommandé par sa documentation : la
**récupération des ressources** après mort ou changement de dimension, la **mémoire par monde**
avec erreurs explicites, puis les **routines déterministes** « trouver → accéder → récolter →
déposer → revenir ». Aucun second moteur de destruction n'est créé : la récolte reste celle de
`work/`.

## 1. Récupération des sacs

Un sac personnel vit dans les données du **niveau** qui le contient. Un compagnon qui meurt, ou
qui change de dimension, laissait donc ses objets dans le monde qu'il venait de quitter, sans
aucun moyen de les reprendre. Un **registre** unique vit maintenant dans l'overworld, donc
indépendant de la dimension et des redémarrages.

**Transfert en deux temps**, pour ne jamais perdre ni dupliquer :

1. l'entrée est écrite dans le registre en état `PENDING_CLEAR` ;
2. le sac d'origine est vidé ;
3. l'entrée passe en `ACTIVE`.

Un arrêt entre 1 et 2 laisse le sac d'origine plein : à la reprise, l'entrée `PENDING_CLEAR` est
supprimée (elle doublonnerait). Un arrêt entre 2 et 3 laisse le sac vide : l'entrée devient
`ACTIVE` (rien n'est perdu). La réconciliation tourne au démarrage du serveur et avant chaque
lecture du registre.

**Mort** — `ServerLivingEntityEvents.AFTER_DEATH` : le sac du compagnon quitte le niveau et
rejoint le registre avec sa position et sa dimension. Rien ne tombe au sol, donc rien ne peut
brûler, tomber dans la lave ou se désépawner.

**Changement de dimension** — `ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL` : le sac
suit le compagnon. Si le sac de destination n'a pas la place, l'ensemble part au registre plutôt
que d'écraser quoi que ce soit.

**Restitution** — toujours tout-ou-rien, sur une copie calculée avant toute mutation :

```
/servitor bag list                 sacs en attente (id, raison, objets, dimension, position)
/servitor bag audit                sacs vivants de chaque dimension + registre, pour ce compagnon
/servitor bag restore <id>         dans le sac du compagnon vivant
/servitor bag give <id>            dans l'inventaire de l'opérateur
/servitor bag drop <id>            au sol, aux pieds de l'opérateur
```

Un sac plein refuse la restitution **sans rien déplacer** et l'entrée reste disponible pour un
second essai. Une entrée consommée est supprimée : rejouer le même identifiant est refusé, ce qui
interdit la duplication.

## 2. Mémoire par monde

`BrainNotes` écrivait dans le dossier de configuration du mod, partagé par tous les mondes. Chaque
monde a désormais son fichier : `<monde>/voiddynasty/voiddynasty-servitor-notes.json`. Supprimer
un monde supprime ses notes ; deux sauvegardes ne se mélangent plus.

Les erreurs ne sont plus avalées : une écriture impossible lève `BrainNotesException` et le
compte rendu dit `memoire NON enregistree : <raison>`. Une lecture impossible met le fichier
illisible de côté (`.corrompu-<horodatage>`) au lieu de l'écraser, puis signale l'échec. Écriture
atomique par fichier temporaire + renommage.

L'ancien fichier global est repris **une fois** dans le premier monde qui en a besoin (copie, pas
déplacement : rien n'est détruit).

Nouvel outil de lecture : `note_list` (les 20 dernières notes du monde).

## 3. Routines

`routine_gather` prend la même spécification JSON que `work_submit` et fait le reste :

```
routine_gather {"schema":1,"type":"GATHER","request_id":"fer-base-001","item":"minecraft:raw_iron",
                "amount":10,"min":[10,50,10],"max":[25,65,25],
                "destination":[5,64,5],"origin":[7,64,5],"access":"excavate"}
```

1. soumission de la mission native (elle cherche, accède, récolte et dépose) ;
2. observation de son état, sans jamais prendre le corps du compagnon ;
3. quand elle est livrée, retour au point `origin` par une tâche de déplacement ;
4. un seul compte rendu à la fin.

Point de conception : la routine n'est **pas** une tâche du cerveau, elle ne possède pas le corps.
C'est ce qui évite `ACTION_BUSY` — un chantier natif et une tâche du cerveau ne peuvent pas
piloter le même servitor en même temps. Une mission en pause ou en échec n'est ni reprise ni
relancée : la routine le dit et s'arrête, l'opérateur décide (`/work mission control <id> resume`).
`/servitor routine list` affiche les routines en cours.

## 4. Validation réellement effectuée

**Sur un serveur de développement (RCON, monde jetable, faux modèle local) :**

| Étape | Résultat observé |
|---|---|
| sac amorcé (5 diamants + 12 lingots ramassés) | `audit` : `vivant overworld : 16 objet(s)` |
| mort du compagnon | `bag list` : `[bag-1] MORT — 16 objet(s), 2 pile(s) — minecraft:overworld en (6500, 64, 6500)` |
| arrêt + redémarrage du serveur | le registre revient à l'identique, 16 objets |
| nouveau compagnon (autre UUID) | `bag restore bag-1` → `sac bag-1 restitue au servitor (16 objet(s))`, sac : `diamond x5, iron_ingot x11` |
| rejeu de la même restitution | `sac introuvable ou incomplet : bag-1`, sac inchangé |
| notes | fichier écrit dans `world8/voiddynasty/voiddynasty-servitor-notes.json`, ancienne note reprise |
| routine | coffre : `{count: 2, id: "minecraft:cobblestone"}`, puis `aller en 6502, 64, 6502` exécuté |

**Compilation** : `compileJava` et `jar` en `--rerun-tasks` (BUILD SUCCESSFUL) ; les deux fixtures
de test serveur compilent contre le JAR candidat.

**Non exécuté ici** : la recette complète `tools/run_work_lab.py --recovery-only`. Elle exige une
installation Minecraft avec le profil `fabric-loader-0.19.5-26.3` et un `classpath-json` local,
absents de cette machine. Le harnais a été rendu multiplateforme (`javac`/`java` selon l'OS) pour
pouvoir y tourner, et les phases sont prêtes : `setup`, `death`, `restore`, `atomic`, `dimension`,
`stash`, puis `restart` après arrêt réel.

## 5. Limites restantes

- **Changement de dimension** : couvert par le code et par la fixture (`dimension`), pas par un
  essai réel sur le serveur de développement — la migration suit l'événement Fabric, pas un
  téléport manuel.
- **Routine** : pas de reprise automatique après pause ou échec ; pas de file d'attente (une
  routine à la fois par mission) ; le moteur de mission doit être libre (aucun chantier en cours,
  main principale vide) sinon la soumission est refusée avec son motif.
- **Registre** : aucune expiration automatique ; les sacs non réclamés restent jusqu'à
  restitution. Un sac trop volumineux pour la destination reste au registre (comportement voulu).
- **Mémoire** : reprise de l'ancien fichier global dans chaque monde qui n'a pas encore le sien
  (choix explicite : rien n'est détruit).
- **Tests** : les décisions d'un modèle réel, la connexion Hermes et les essais prolongés avec
  joueurs restent à faire.

## 6. Prochaine étape recommandée

Configuration et essais Hermes, puis essais prolongés de navigation et de construction avec un
joueur réel — et exécution de `run_work_lab.py --recovery-only` sur la machine qui dispose du
profil Fabric installé.
