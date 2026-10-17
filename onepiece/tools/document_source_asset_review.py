"""Publish the reviewed per-fruit decisions without rewriting historical test evidence."""
from pathlib import Path
import csv, io, json
P=Path(__file__).resolve().parents[1]
review=json.loads((P/'docs/asset-review-40.json').read_text(encoding='utf-8'))
traces=json.loads((P.parent/'.runtime/onepiece/asset-source-audit/technique-traces.local.json').read_text(encoding='utf-8'))
trace={(r['fruit'],r['move']):r for r in traces['rows']}
def load(name):
    with (P/'docs'/name).open(encoding='utf-8',newline='') as f:
        reader=csv.DictReader(f)
        return reader.fieldnames,list(reader)
def save(name,fields,rows):
    with (P/'docs'/name).open('w',encoding='utf-8',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=fields,lineterminator='\n')
        writer.writeheader();writer.writerows(rows)
fields,fruits=load('matrice-fruits-40.csv')
assert len(fruits)==len(review['fruits'])==40
assert {r['fruit'] for r in fruits}==set(review['fruits'])
assert sum(int(r['techniques']) for r in fruits)==213
for field in ['decision_assets_ver15','limite_acceptation']:
    if field not in fields:fields.append(field)
for row in fruits:
    row['revue_assets_minepiece']='REVUE_SOURCE_TERMINEE_ORIGINAUX_OU_ADAPTATION_EXPLICITE'
    row['decision_assets_ver15']=review['fruits'][row['fruit']]
    row['limite_acceptation']='ESSAIS_GRAPHIQUES_PAR_PAQUET_ET_SCOPE; ACCEPTATION_HUMAINE_EN_ATTENTE'
save('matrice-fruits-40.csv',fields,fruits)
fields,moves=load('matrice-techniques-213.csv')
assert len(moves)==213 and {(r['fruit'],r['technique']) for r in moves}==set(trace)
for field in ['revue_assets_ver15','racine_source_tracee','bilan_assets_fruit']:
    if field not in fields:fields.append(field)
for row in moves:
    t=trace[(row['fruit'],row['technique'])]
    row['revue_assets_ver15']='REVUE_SOURCE_MANUELLE_TERMINEE'
    row['racine_source_tracee']=';'.join(t['source_procedure_roots']) or 'ALIAS_OU_EXTENSION_PORT_VOIR_BILAN_FRUIT'
    row['bilan_assets_fruit']='docs/asset-review-40.json#'+row['fruit']
save('matrice-techniques-213.csv',fields,moves)
lines=['# Assets MinePiece — revue des 40 fruits',
       '', 'Les40 fruits et213 techniques ont été revus à partir des procédures et classes de rendu de MinePiece ver15. Les correspondances de noms seules ont servi à chercher les fichiers ; les décisions ci-dessous reposent sur la source effective.',
       '', 'Cette revue distingue les fichiers originaux, leurs adaptations au moteur26.3 et les ajouts du port. Elle ne signifie pas que chaque technique possède un modèle original distinct. Les scènes et les réserves artistiques sont détaillées dans [la validation alpha.19](ASSETS-ALPHA19-VALIDATION.md).',
       '', '| Fruit | Techniques | Décision sur les assets |', '|---|---:|---|']
for row in fruits:
    lines.append('| '+row['fruit']+' | '+row['techniques']+' | '+review['fruits'][row['fruit']]+' |')
lines+=['', 'Référence privée : `'+review['source_sha256']+'`. Les2881 fichiers originaux restent en réserve. Le graphe automatique trouve142 noms exacts et186 racines résolues ;27 entrées exigent un alias manuel ou correspondent à une extension du port. Ces chiffres décrivent la recherche, pas le nombre de remplacements.',
        '', 'La passe ajoute53 liaisons de profils visuels,60 fichiers originaux vérifiés octet par octet et8 adaptations de pistes. Trois géométries Java originales sont reprises : sphère sombre57éléments, racines20cubes et missile15cubes. Le dragon Kaido complet original déjà corrigé est conservé. Les formes complètes absentes de ver15 gardent leurs adaptations documentées, avec les modèles hybrides source lorsqu’ils existent.',
        '', 'Les animations PAL et les enveloppes/traînées procédurales restent utiles lorsque la source utilise des particules ou un carrier invisible. Leur présence ne constitue pas un oubli de modèle. Les matériaux blancs Riki/Supa/Gura et la robe blanche Phoenix sont explicitement adaptés.',
        '', 'Source des revues privées : `../.runtime/onepiece/asset-source-audit/READONLY-PHASE01-03-2026-10-17.md`, dumps des huit phases, `phase07-08-review.local.md` et rapports des images. Les captures sont des instants sélectionnés, sans acceptation artistique humaine implicite.']
(P/'docs/ASSETS-MINEPIECE-40.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('DOCUMENTED40FRUITS213TECHNIQUES')
