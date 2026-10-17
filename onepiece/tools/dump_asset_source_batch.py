"""Disassemble a scoped asset-review batch without executing the reference JAR."""
from pathlib import Path
import argparse,concurrent.futures,hashlib,json,subprocess,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent
ap=argparse.ArgumentParser();ap.add_argument('--fruits',required=True);ap.add_argument('--name',required=True);a=ap.parse_args()
assert a.name.isascii() and a.name.replace('-','').isalnum()
trace=R/'.runtime/onepiece/asset-source-audit/technique-traces.local.json';m=json.loads(trace.read_text(encoding='utf-8'));wanted=set(a.fruits.split(','));rows=[r for r in m['rows'] if r['fruit'] in wanted];assert {r['fruit'] for r in rows}==wanted
jar=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar';assert hashlib.sha256(jar.read_bytes()).hexdigest()==m['source_jar_sha256']
javap=R/'.runtime/jdk/jdk-25.0.4.1+1/bin/javap.exe';out=R/'.runtime/onepiece/asset-source-audit'/a.name;out.mkdir(exist_ok=True)
roots={k for row in rows for key in ['source_skill_candidates','source_procedure_roots','direct_entity_classes','direct_render_classes'] for k in row[key]}
with zipfile.ZipFile(jar) as z:names={n[:-6] for n in z.namelist() if n.endswith('.class')}
roots|={k for root in list(roots) for k in names if k.startswith(root+'$')}
def dump(cls):
 p=out/(cls.replace('/','__')+'.txt')
 if not p.exists():
  result=subprocess.run([str(javap),'-classpath',str(jar),'-c','-p',cls.replace('/','.')],capture_output=True,text=True,encoding='utf-8',check=True);p.write_text(result.stdout,encoding='utf-8')
 return {'class':cls,'file':str(p.resolve()),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as ex:files=list(ex.map(dump,sorted(roots)))
(out/'manifest.local.json').write_text(json.dumps({'fruits':sorted(wanted),'techniques':len(rows),'source_jar_sha256':m['source_jar_sha256'],'classes':files,'loaded_as_mod':False},indent=2),encoding='utf-8')
print(a.name,len(wanted),'fruits',len(rows),'techniques',len(files),'bytecode files; branch review required')
