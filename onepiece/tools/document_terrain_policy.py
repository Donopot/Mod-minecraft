"""Regenerate the reviewed per-technique table, preserving the frozen alpha.9 record."""
from pathlib import Path
import re,shutil
root=Path(__file__).resolve().parents[1]
p=root/'docs/TERRAIN-POLICY.md'
old=root/'build/terrain-policy-alpha9.md'
if not old.exists():shutil.copy2(p,old)
s=(root/'src/main/java/net/mcreator/minepiece/procedures/TechniqueTerrain.java').read_text(encoding='utf-8')
rows=[]
for f,m,r,n,t,why in re.findall(r'destructive\("([^"]+)","([^"]+)",([\d.]+),(\d+),(true|false),"([^"]+)"\);',s):
 rows.append((f,m,'Destruction définitive'+(' + magma définitif' if t=='true' else ''),r,n,why))
for mode,fr,ids,why in re.findall(r'(none|transform)\("([^"]+)","([^"]+)","([^"]+)"\);',s):
 for move in ids.split(','):rows.append((fr,move,'Aucune mutation' if mode=='none' else 'Transformation définitive','—','—',why))
assert len(rows)==117 and len({r[:2] for r in rows})==117,len(rows)
head='''# Terrain — décisions par technique

Politique actuelle : [tous les blocs modifiés restent définitifs](TERRAIN-DEFINITIF-alpha11.md), à la demande explicite du joueur. Cette table couvre les116techniques des23fruits traités, plus Amaru. Les autres fruits restent à évaluer ; leurs anciennes mentions de restitution ne définissent plus le comportement du service commun.

Les appels MinePiece ver15 ont été suivis jusqu’aux projectiles et convertisseurs. BlockDestroyAllDirection utilise une largeur (rayon nominal `(BlockRange+1)/2`), BlockToIce/BlockToSand un rayon. Les volumes ci-dessous sont des adaptations bornées pour le port, pas une reproduction générale des volumes source.

La charge ne creuse pas. Le contact ou la phase active partage un quota entre tous les projectiles du lancement. Les dégâts voient les obstacles présents avant la destruction : un météore arrêté sur un toit ne blesse pas fictivement à travers celui-ci. Les techniques de déplacement sans mutation restent sans mutation.

| Fruit | Technique | Décision | Rayon terrain max | Blocs retirés max / lancement | Origine / phase |
|---|---|---|---:|---:|---|
'''
tail='''
Les transformations ont leurs propres bornes : IceSpikes six bases ; IceBall chemin de surface jusqu’à16blocs/16cases ; IceAge eau de surface rayon8/128cases ; GroundDeath surface de terre/pierre/sable rayon12/profondeur1/128cases. PACKED_ICE et SANDSTONE restent dans le monde. L’attribution expire après200ticks ; elle ne restaure rien.

Ryusei partage160blocs entre20météores,12au plus par impact ; Higan partage8blocs/2par tir. Laser2 et Magatama partagent aussi leurs quotas. Sango ne creuse qu’aux contacts blessants acceptés,4blocs par contact/16au total. Tenchi partage256blocs entre ses fronts ; Stone Ope partage96blocs entre ses quatre pierres. Raigo ne creuse pas en vol. Amaru ne creuse pas et n’augmente aucun quota.

Mori partage128conversions sélectives de sol par lancement : rayon3aux impacts des racines, rayon6pour Assault/Surround. Mochi Build crée au plus24blocs sans ensevelir les entités ; ils restent définitifs. Les poings des deux anneaux Muso partagent64blocs sur leur durée de vie. Yaki Mochi et Atomic Spurt ne creusent pas. Un tir dévié ne garde aucune autorisation de mutation du terrain de son ancien porteur.

Les impacts destructeurs refusent chunks absents, extérieur de frontière, protections, inventaires/BlockEntity, blocs indestructibles ou résistance>6, fluides et attributions déjà actives. Les obstacles résistants protègent les blocs derrière eux. Plusieurs couches ordinaires peuvent être excavées dans le quota.

La configuration privée `config/opworldlike/technique-terrain.json`, section `enabled`, peut désactiver une destruction ; elle n’active jamais une technique NONE. Un fichier illisible bloque les destructions permanentes. Les anciens réglages de durée des blocs règlent seulement leur attribution, pas leur durée dans le monde. Les champs de dégâts et contrôles sur entités gardent leurs durées distinctes. Les blocs de magma restants retrouvent leur comportement chaud vanilla après le champ attribué.

Les preuves alpha.9 restent figées dans `build/terrain-alpha9-verification.local.json` et l’ancienne table dans `build/terrain-policy-alpha9.md`. La permanence du socle est prouvée en alpha.11 ; les décisions supplémentaires et les preuves du lot03 sont consignées dans [le bilan alpha.12](B03-VALIDATION-2026-10-16.md). Les anciennes promesses de restitution sont remplacées par la demande du joueur.
'''
p.write_text(head+'\n'.join('| '+' | '.join(r)+' |' for r in sorted(rows))+'\n'+tail,encoding='utf-8')
print('DOCUMENTED',len(rows),'reviewed terrain decisions')
