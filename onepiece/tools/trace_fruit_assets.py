"""Read-only class constant-pool graph for every catalogue technique.

The graph identifies source candidates; branch-sensitive setters still require
bytecode review before they become an accepted rendering binding.
"""
from pathlib import Path
import json,re,zipfile,struct,io,hashlib
P=Path(__file__).resolve().parents[1];R=P.parent
jar=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar';out=R/'.runtime/onepiece/asset-source-audit';out.mkdir(exist_ok=True)
def cp(data):
 s=io.BytesIO(data);assert s.read(4)==b'\xca\xfe\xba\xbe';s.read(4);count=struct.unpack('>H',s.read(2))[0];rows=[];i=1;sizes={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,17:4,18:4,19:2,20:2}
 while i<count:
  tag=s.read(1)[0]
  if tag==1:rows.append(s.read(struct.unpack('>H',s.read(2))[0]).decode('utf-8',errors='replace'))
  else:s.read(sizes[tag])
  i+=2 if tag in [5,6] else 1
 return rows
def norm(s):return re.sub('[^a-z0-9]','',s.lower())
def registry_fields(data):
 s=io.BytesIO(data);s.read(8);count=struct.unpack('>H',s.read(2))[0];entries={};i=1;sizes={3:4,4:4,5:8,6:8,15:3,16:2,19:2,20:2}
 while i<count:
  tag=s.read(1)[0]
  if tag==1:value=s.read(struct.unpack('>H',s.read(2))[0]).decode('utf-8',errors='replace')
  elif tag in [7,8]:value=struct.unpack('>H',s.read(2))[0]
  elif tag in [9,10,11,12,17,18]:value=struct.unpack('>HH',s.read(4))
  else:value=s.read(sizes[tag])
  entries[i]=(tag,value);i+=2 if tag in [5,6] else 1
 refs=[]
 for tag,value in entries.values():
  if tag==9:
   owner=entries[entries[value[0]][1]][1];nt=entries[value[1]][1];name=entries[nt[0]][1]
   if owner=='net/mcreator/minepiece/init/MinepieceModEntities':refs.append(name)
 return sorted(set(refs))
registry=(P/'src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java').read_text(encoding='utf-8')
fruits={f.lower():re.findall(r'(?:DevilFruitMove\.\w+|new DevilFruitMove)\("([^"]+)"',body) for f,body in re.findall(r'(?:MOVES\.put|putZoan)\(MinepieceModItems\.ITEM_DEVIL_FRUIT_(\w+), List\.of\((.*?)\)\);',registry,re.S)}
assert len(fruits)==40 and sum(map(len,fruits.values()))==213,(len(fruits),sum(map(len,fruits.values())),[(f,len(m)) for f,m in fruits.items()])
with zipfile.ZipFile(jar) as z:
 names=z.namelist();classes={n[:-6]:cp(z.read(n)) for n in names if n.startswith('net/mcreator/minepiece/') and n.endswith('.class')}
 fields={n[:-6]:registry_fields(z.read(n)) for n in names if n.startswith('net/mcreator/minepiece/') and n.endswith('.class')}
 assets={n[len('assets/minepiece/'):]:hashlib.sha256(z.read(n)).hexdigest() for n in names if n.startswith('assets/minepiece/') and not n.endswith('/')}
 procedures={k for k in classes if '/procedures/' in k};models={k for k in classes if '/model/' in k or '/renderer/' in k or '/layer/' in k};move_roots={}
 for moves in fruits.values():
  for move in moves:move_roots[move]=sorted(k for k in procedures if norm(Path(k).name.removesuffix('Procedure'))==norm(move))
 report=[]
 for fruit,moves in fruits.items():
  skills=sorted(k for k in procedures if norm(Path(k).name.removesuffix('Procedure'))=='skill'+norm(fruit))
  for move in moves:
   roots=move_roots[move];root_kind='exact_name'
   if not roots:
    called={v for skill in skills for v in classes[skill] if v in procedures}
    matched=sorted(v for v in called if re.search(re.escape(norm(move))+r'[0-9]*$',norm(Path(v).name.removesuffix('Procedure'))))
    if len(matched)==1:roots=matched;root_kind='source_skill_reference_and_suffix'
   visited=set();front=list(roots)
   for depth in range(4):
    nxt=[]
    for cls in front:
     if cls in visited:continue
     visited.add(cls)
     for value in classes[cls]:
      if value in procedures and value not in visited and not Path(value).name.startswith(('Skill','AI')):nxt.append(value)
      if value.startswith('net/mcreator/minepiece/entity/') and value in classes:
       visited.add(value)
       stem=Path(value).name.removesuffix('Entity')
       visited.update(k for k in models if Path(k).name in [stem+'Model',stem+'Renderer',stem+'Layer'])
    front=nxt
   pool={s for cls in visited for s in classes[cls]};refs=[]
   for asset,sha in assets.items():
    stem=Path(asset).name.split('.')[0]
    if asset in pool or stem in pool or ('minepiece:'+stem) in pool:refs.append({'path':asset,'sha256':sha,'match':'exact_resource' if asset in pool else 'literal_basename'})
   direct={root for root in roots}|{k for root in roots for k in procedures if k.startswith(root+'$')}
   entity_fields=sorted({field for k in direct for field in fields[k]})
   entity_classes={k for k in classes if '/entity/' in k and '/model/' not in k and '/layer/' not in k and '$' not in k and any(norm(Path(k).name.removesuffix('Entity'))==norm(f) for f in entity_fields)}
   model_classes={v for k in entity_classes for v in classes[k] if v in models}
   for k in entity_classes:
    stem=Path(k).name.removesuffix('Entity');model_classes.update(v for v in models if Path(v).name in [stem+'Model',stem+'Renderer',stem+'Layer'])
   model_pool={v for k in model_classes for v in classes[k]};exact_assets=sorted(a for a in assets if a in model_pool)
   report.append({'fruit':fruit,'move':move,'source_skill_candidates':skills,'source_procedure_roots':roots,'root_resolution':root_kind if roots else 'unmapped','direct_entity_registry_fields':entity_fields,'direct_entity_classes':sorted(entity_classes),'direct_render_classes':sorted(model_classes),'direct_model_resource_paths':exact_assets,'trace_classes':sorted(visited),'asset_candidates':refs,'review_status':'needs_branch_review' if roots else 'needs_root_mapping'})
manifest={'source_jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'fruits':40,'techniques':213,'class_count':len(classes),'techniques_with_exact_procedure_name':sum(bool(r['source_procedure_roots']) and r['root_resolution']=='exact_name' for r in report),'techniques_with_resolved_procedure_root':sum(bool(r['source_procedure_roots']) for r in report),'rows':report,'classes':classes,'source_code_executed':False}
(out/'technique-traces.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
lines=['# Traçage des assets des techniques MinePiece','', 'Les 213 entrées des 40 fruits sont indexées. Les liens constants ci-dessous ne résolvent pas à eux seuls les branches dynamiques des procédures partagées ; ils servent à sélectionner les bytecodes à lire avant toute substitution.','', '| Fruit | Technique | Procédure exacte | Assets candidats |','|---|---|---|---:|']
for r in report:lines.append('| '+r['fruit']+' | '+r['move']+' | '+(', '.join(Path(k).name for k in r['source_procedure_roots']) or 'à relier depuis Skill')+' | '+str(len(r['asset_candidates']))+' |')
(P/'docs/ASSETS-TECHNIQUES-213.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('TRACED',len(report),'techniques;',manifest['techniques_with_resolved_procedure_root'],'resolved roots; branch review remains required')
