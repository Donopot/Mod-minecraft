"""Inspect the user-supplied pack as inert ZIP data. Never install or run pack contents."""
from pathlib import Path
import zipfile,hashlib,json,io
P=Path(__file__).resolve().parents[1];folder=P.parent/'.runtime/onepiece/reference-jars';src=next(Path('C:/Users/Anwender/Downloads').glob('*One Piece Ultimate - (Mine Piece).zip'))
with src.open('rb') as f:digest=hashlib.file_digest(f,'sha256').hexdigest()
with zipfile.ZipFile(src) as z:
 rows=[{'path':i.filename,'bytes':i.file_size} for i in z.infolist() if not i.is_dir()];mods=[r for r in rows if r['path'].lower().endswith('.jar')];packs=[r for r in rows if any(t in r['path'].lower() for t in ['resourcepack','shaderpack']) or r['path'].lower().endswith('manifest.json')];direct=[r for r in rows if '/assets/' in '/'+r['path']]
 candidates=[r for r in mods if any(t in Path(r['path']).name.lower() for t in ['minepiece','mine-mine','mineminenomi','ompworld','onepiece','one-piece'])]
 visual=[]
 for r in candidates:
  if r['bytes']>500_000_000:continue
  data=z.read(r['path']);sha=hashlib.sha256(data).hexdigest();dest=folder/(sha[:12]+'-'+Path(r['path']).name)
  if not dest.exists():dest.write_bytes(data)
  with zipfile.ZipFile(io.BytesIO(data)) as inner:
   assets=[n for n in inner.namelist() if n.startswith('assets/') and not n.endswith('/')];interesting=[n for n in assets if any(t in n.lower() for t in ['makami','okuchi','seiryu','dragon','phoenix','pterano','brachio','pachy','daibutsu','amaru'])]
  visual.append({'path':r['path'],'reserved':str(dest),'sha256':sha,'assets':len(assets),'interesting_assets':interesting})
report={'source':str(src),'sha256':digest,'entries':len(rows),'jar_inventory':mods,'resourcepacks_and_manifest':packs,'direct_assets':direct,'reserved_visual_candidates':visual,'installed':False,'executed':False}
(folder/'ultimate-pack-inventory.local.json').write_text(json.dumps(report,indent=2,ensure_ascii=False),encoding='utf-8')
print('PACK_ENTRIES',len(rows),'JARS',len(mods),'DIRECT_ASSETS',len(direct));print('RESOURCES',json.dumps(packs,ensure_ascii=True));print('VISUAL_CANDIDATES',json.dumps([{k:v for k,v in r.items() if k!='interesting_assets'}|{'relevant_assets':len(r['interesting_assets'])} for r in visual],ensure_ascii=True))
