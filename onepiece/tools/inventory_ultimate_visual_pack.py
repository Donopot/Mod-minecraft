from pathlib import Path
import zipfile,json,hashlib,io
P=Path(__file__).resolve().parents[1];folder=P.parent/'.runtime/onepiece/reference-jars';outer=json.loads((folder/'ultimate-pack-inventory.local.json').read_text(encoding='utf-8'));src=Path(outer['source'])
with zipfile.ZipFile(src) as z:
 name=next(n for n in z.namelist() if n.endswith('/MBM Mine Piece ver13 1.20.1.zip'));data=z.read(name)
sha=hashlib.sha256(data).hexdigest();dest=folder/'MBM-Mine-Piece-ver13-visual-reference.zip'
if not dest.exists():dest.write_bytes(data)
with zipfile.ZipFile(io.BytesIO(data)) as z:
 names=[n for n in z.namelist() if not n.endswith('/')];models=[n for n in names if any(t in n.lower() for t in ['/geo/','/models/','/animations/']) or n.endswith(('.jem','.jpm','.bbmodel'))];relevant=[n for n in names if any(t in n.lower() for t in ['makami','okuchi','seiryu','dragon','phoenix','pterano','brachio','pachy','daibutsu','amaru','queen','yamato','kaido'])]
(folder/'ultimate-visual-assets.local.json').write_text(json.dumps({'source_archive_sha256':outer['sha256'],'reserved':str(dest),'sha256':sha,'entries':len(names),'models':models,'relevant_assets':relevant,'installed':False},indent=2),encoding='utf-8');print('MBM',len(names),'files',len(models),'models/animations',len(relevant),'relevant');print(json.dumps(relevant[:85]))
