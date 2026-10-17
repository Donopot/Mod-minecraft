from pathlib import Path
import json,hashlib,zipfile,shutil
P=Path(__file__).resolve().parents[1];out=P.parent/'.runtime/onepiece/after-b07-alpha16';out.mkdir(exist_ok=True)
b=json.loads((P/'build/build-local.json').read_text());assert b['sha256']=='4f770ab589edbd0a88f9e3c846a093d99ea19f4896c2931e18ed972935a7460f'
with zipfile.ZipFile(out/'source.zip','x',zipfile.ZIP_DEFLATED) as z:
 for path in P.rglob('*'):
  if path.is_file() and (path.parts[len(P.parts)] in {'src','docs','tools'} or path.parent==P and path.suffix in {'.md','.gradle','.properties'}):
   if '__pycache__' not in path.parts:z.write(path,path.relative_to(P))
shutil.copy2(b['jar'],out/Path(b['jar']).name)
for name in ['build-local.json','asset-audit.local.json','batch07-animation-audit.local.json','b07-moves.local.json']:shutil.copy2(P/'build'/name,out/name)
(out/'candidate.local.json').write_text(json.dumps({'build_sha256':b['sha256'],'source_sha256':hashlib.sha256((out/'source.zip').read_bytes()).hexdigest(),'native_report':str(P.parent/'.runtime/onepiece-lab/run-181f275a86124361a9cc7d7d89f6aaf8/report.local.json'),'server_pass':502+59+307,'graphics_pending':True,'human_art_accepted':False},indent=2),encoding='utf-8')
print('B07_FROZEN_CANDIDATE',out)
