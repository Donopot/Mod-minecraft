"""Reserve the user-supplied visual reference without loading any mod code."""
from pathlib import Path
import zipfile,hashlib,json,shutil
P=Path(__file__).resolve().parents[1];folder=P.parent/'.runtime/onepiece/reference-jars';src=Path('C:/Users/Anwender/Downloads/MinePieceXMineNoMi-0.1.13.jar');dest=folder/src.name
digest=hashlib.sha256(src.read_bytes()).hexdigest()
if dest.exists():assert hashlib.sha256(dest.read_bytes()).hexdigest()==digest
else:shutil.copy2(src,dest)
manifest=json.loads((folder/'manifest.local.json').read_text());manifest['files']=[r for r in manifest['files'] if Path(r['reserved']).name!=dest.name]+[{'original':str(src),'reserved':str(dest),'sha256':digest,'bytes':src.stat().st_size}];(folder/'manifest.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
with zipfile.ZipFile(dest) as z:
 names=z.namelist();assets=[n for n in names if n.startswith('assets/') and not n.endswith('/')];models=[n for n in names if any(t in n.lower() for t in ['model','render','morph','amaru','phoenix','makami','seiryu','pachy','brachio'])]
 meta={n:z.read(n).decode('utf-8',errors='replace') for n in ['META-INF/mods.toml','fabric.mod.json','pack.mcmeta'] if n in names}
 report={'jar_sha256':digest,'reserved':str(dest),'assets':assets,'visual_classes_or_resources':models,'metadata':meta,'loaded':False,'installed':False};(folder/'MinePieceXMineNoMi-assets.local.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
 print('RESERVED',dest.name,'SHA256',digest,'assets',len(assets),'visual_candidates',len(models));print(json.dumps(report,indent=2))
