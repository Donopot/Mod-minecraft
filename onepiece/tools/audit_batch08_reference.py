"""Private read-only ver15 bytecode inventory for the final two Zoans."""
from pathlib import Path
import zipfile,subprocess,json,hashlib,concurrent.futures
P=Path(__file__).resolve().parents[1];R=P.parent;jar=R/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar';out=R/'.runtime/onepiece/b08-source-audit';out.mkdir(exist_ok=True)
extra=['AttackJumpProcedure','AttackSweepProcedure','AttackOverheadProcedure','AIEnergyProcedure','AIProjectileSlashProcedure','ProjectileLaserProjectileHitsBlockProcedure','ProjectileLaserProjectileHitsLivingEntityProcedure','MissileProjectileHitsBlockProcedure','MissileProjectileHitsLivingEntityProcedure']
with zipfile.ZipFile(jar) as z:
 names=[n for n in z.namelist() if n.startswith('net/mcreator/minepiece/procedures/') and n.endswith('.class') and (any(t in n.lower() for t in ['brachio','blackcof','sparkingqueen','headbutt','ulzugan','ulmeteor','pachy']) or Path(n).stem in extra)]
javap=R/'.runtime/jdk/jdk-25.0.4.1+1/bin/javap.exe'
def read(n):
 result=subprocess.run([str(javap),'-classpath',str(jar),'-c','-p',n[:-6].replace('/','.')],capture_output=True,text=True,encoding='utf-8',check=True)
 p=out/(Path(n).stem+'.txt');p.write_text(result.stdout,encoding='utf-8');return {'class':n,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()}
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as ex:rows=list(ex.map(read,names))
(out/'bytecode-manifest.local.json').write_text(json.dumps({'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'classes':rows},indent=2),encoding='utf-8')
print('B08_READONLY_CLASSES',len(rows));print('\n'.join(Path(r['class']).stem for r in rows))
