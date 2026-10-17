"""Reserve ver15 assets and index candidates for all 40 fruits without loading Forge code.

Alias matches are candidates, not proof that a procedure uses an asset. Exact
model-class resource strings are recorded separately for subsequent tracing.
"""
from pathlib import Path
import csv,hashlib,io,json,re,struct,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent
src=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar'
out=R/'.runtime/onepiece/minepiece-assets-original';out.mkdir(exist_ok=True)
ALIASES={
 'aro_aro':['aroaro','aro_aro','gunko'], 'batto_batto':['batto','batbat','wings_bat'],
 'bisu_bisu':['bisu','biscuit','cracker'], 'bomu_bomu':['bomu','bomb','mr5'],
 'gomu_gomu':['gomu','luffy','gear'], 'goro_goro':['goro','enel','lightning','raigo'],
 'gura_gura':['gura','newgate','crack','quake'], 'hie_hie':['hie','kuzan','ice','partisan','pheasant'],
 'hito_hito_daibutsu':['hito','sengoku','daibutsu'], 'horo_horo':['horo','perona','hollow','ghost'],
 'imu':['imu','abyss'], 'inu_inu_okuchi_no_makami':['makami','yamato'], 'inu_inu_wolf':['wolf','jabra'],
 'kage_kage':['kage','moria','chimera','shadow','oars'], 'kiro_kiro':['kiro','valentine'],
 'kira_kira':['kira','jozu','diamond'], 'magu_magu':['magu','akainu','lava'],
 'mera_mera':['mera','hiken','ace','sabo'], 'mero_mero':['mero','hancock','kiss'],
 'mochi_mochi':['mochi','katakuri','muso','donatsu'], 'moku_moku':['moku','smoker','smoke'],
 'mori_mori':['mori','aramaki','flower'], 'neko_neko_leopard':['leopard','lucci'],
 'neko_neko_sabertiger':['sabertiger','whos_who'], 'nikyu_nikyu':['nikyu','kuma','pad','ursus'],
 'ope_ope':['ope','law','room'], 'pika_pika':['pika','kizaru','magatama'], 'riki_riki':['riki','burgess'],
 'ryu_ryu_brachiosaurus':['brachio','queen','missile'], 'ryu_ryu_pachycephalosaurus':['pachy','ulti'],
 'ryu_ryu_pteranodon':['ptero','king'], 'soru_soru':['soru','linlin','prometheus','zeus','hera'],
 'suna_suna':['suna','crocodile','sand'], 'supa_supa':['supa','mr1'],
 'tori_tori_phoenix':['phoenix','marco'], 'uo_uo_seiryu':['seiryu','kaidou','dragon_head'],
 'uta_uta':['uta','tot_musica','note'], 'ushi_ushi_giraffe':['giraffe','kaku'],
 'yami_yami':['yami','kurohige','blackhole'], 'zushi_zushi':['zushi','fujitora','meteor','rubble','rock_fragment']}
def sha(data):return hashlib.sha256(data).hexdigest()
def strings(data):
 stream=io.BytesIO(data);assert stream.read(4)==b'\xca\xfe\xba\xbe';stream.read(4);count=struct.unpack('>H',stream.read(2))[0];result=[];index=1
 sizes={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,17:4,18:4,19:2,20:2}
 while index<count:
  tag=stream.read(1)[0]
  if tag==1:
   size=struct.unpack('>H',stream.read(2))[0];result.append(stream.read(size).decode('utf-8',errors='replace'))
  else:stream.read(sizes[tag])
  index+=2 if tag in (5,6) else 1
 return result
def matches(name,aliases):
 # Token boundaries prevent e.g. 'king' from matching every 'sparking' file.
 stem=Path(name).name.lower()
 return any(re.search(r'(^|[_/])'+re.escape(alias)+r'([_.0-9]|$)',stem) for alias in aliases)
build=json.loads((P/'build/build-local.json').read_text(encoding='utf-8'))
with zipfile.ZipFile(src) as z,zipfile.ZipFile(build['jar']) as current:
 names=z.namelist();assets=[n for n in names if n.startswith('assets/') and not n.endswith('/')];active=set(current.namelist());records=[]
 for name in assets:
  relative=Path(name);assert '..' not in relative.parts
  data=z.read(name);target=out/relative;target.parent.mkdir(parents=True,exist_ok=True)
  if not target.exists():target.write_bytes(data)
  assert sha(target.read_bytes())==sha(data)
  records.append({'path':name,'sha256':sha(data),'bytes':len(data),'packed_state':'identical' if name in active and current.read(name)==data else 'modified' if name in active else 'absent'})
 classes={}
 for name in names:
  if name.endswith('.class') and name.startswith('net/mcreator/minepiece/'):
   refs=sorted({s for s in strings(z.read(name)) if any(s.endswith(ext) for ext in ['.geo.json','.animation.json','.png','.ogg'])})
   if refs:classes[name]=refs
 fruits=list(csv.DictReader((P/'docs/matrice-fruits-40.csv').open(encoding='utf-8')));assert len(fruits)==40 and set(ALIASES)=={f['fruit'] for f in fruits}
 rows=[]
 for fruit in fruits:
  key=fruit['fruit'];aliases=ALIASES[key]
  found=[r for r in records if matches(r['path'],aliases)]
  rows.append({'fruit':key,'techniques':int(fruit['techniques']),'aliases':aliases,'candidate_assets':found,'resource_strings':{k:v for k,v in classes.items() if any(a.replace('_','') in Path(k).stem.lower().replace('_','') for a in aliases)},'association_status':'candidates_require_procedure_trace','integrated_reviewed':False})
report={'source_jar':str(src.resolve()),'jar_sha256':sha(src.read_bytes()),'candidate_jar_sha256':build['sha256'],'reserved_root':str(out.resolve()),'all_assets':records,'fruits':rows,'classes_with_resource_strings':classes,'fruits_count':40,'techniques_count':sum(r['techniques'] for r in rows),'code_executed':False,'forge_installed':False}
(out/'catalogue.local.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
lines=['# Assets MinePiece — revue des 40 fruits','',f"Référence privée ver15 : `{report['jar_sha256']}`. Tous les {len(records)} fichiers d’assets sont réservés sans exécuter le JAR. Les correspondances ci-dessous sont des **candidats**, à confirmer par les procédures et classes de rendu ; un nom proche ne prouve pas un usage en jeu.",'','| Fruit | Techniques | Assets candidats | Identiques dans le paquet | À relier / comparer |','|---|---:|---:|---:|---:|']
for row in rows:
 found=row['candidate_assets'];same=sum(a['packed_state']=='identical' for a in found);lines.append(f"| {row['fruit']} | {row['techniques']} | {len(found)} | {same} | {len(found)-same} |")
lines+=['','La revue d’intégration reste à faire fruit par fruit. Les modèles Java de transformation, les ressources communes et les délégués de techniques sont aussi inventoriés dans le catalogue privé ; une absence dans les candidats ne signifie pas que l’asset est absent du JAR.','Le dragon de Kaido est déjà relié à sa géométrie et sa texture originales, avec reprise graphique séparée. Les autres remplacements seront vérifiés après les fruits en cours.']
(P/'docs/ASSETS-MINEPIECE-40.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('RESERVED',len(records),'assets; 40 fruits /',report['techniques_count'],'techniques; candidate mapping, not visual acceptance')
