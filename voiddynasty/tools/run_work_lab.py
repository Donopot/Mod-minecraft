"""Native conservation, rollback, pathfinding and clean-restart acceptance in a disposable world.
Only this tool's child JVM is stopped. EULA: https://www.minecraft.net/eula
The One Piece tools imported below supply profile parsing and RCON framing only.
"""
from pathlib import Path
import asyncio,json,subprocess,os,sys,zipfile,uuid,shutil,time,secrets
import argparse
parser=argparse.ArgumentParser(description="Create and test a NEW loopback-only VOID world. Never touches a player instance.")
parser.add_argument('--candidate',type=Path,required=True)
parser.add_argument('--classpath-json',type=Path,required=True,help='Local JSON object with classpath array (not committed)')
parser.add_argument('--jdk',type=Path,required=True)
parser.add_argument('--minecraft',type=Path,required=True)
parser.add_argument('--dependencies',type=Path,required=True,help='Directory containing Fabric API, GeckoLib and optional bridge JARs')
parser.add_argument('--output',type=Path,required=True)
parser.add_argument('--missions',action='store_true',help='Also exercise real MCP mission tools; requires requirements-mcp.txt')
parser.add_argument('--next-only',action='store_true',help='Focus on access/build scenarios only; implies --missions')
parser.add_argument('--build-only',action='store_true',help='Focus on construction scenarios only; implies --next-only')
parser.add_argument('--accept-eula',action='store_true',required=True,help='Explicit acceptance of Minecraft EULA for the disposable server')
args=parser.parse_args()
if args.build_only: args.next_only=True
if args.next_only: args.missions=True
repo=Path(__file__).resolve().parents[2];work=args.output.resolve();work.mkdir(parents=True,exist_ok=True)
sys.path.insert(0,str(repo/'onepiece/tools'))
from installed_runtime import resolve
from run_local_tests import Rcon,reserve
b=json.loads(args.classpath_json.read_text(encoding='utf-8-sig'))
jdk=args.jdk.resolve()/'bin';cp=os.pathsep.join([str(args.candidate.resolve()),*b['classpath']])
test=repo/'voiddynasty/src/test/java/com/donopot/voiddynasty/work/WorkServerTest.java';classes=work/'test-mod-classes';classes.mkdir(exist_ok=True)
test_sources=[test]
if args.missions: test_sources.extend([test.with_name('MissionServerTest.java'),test.with_name('WorkNextServerTest.java')])
r=subprocess.run([str(jdk/'javac.exe'),'--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,test_sources)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=60)
(work/'server-test-compile.log').write_text(r.stdout+r.stderr,encoding='utf-8')
if r.returncode:print(r.stdout+r.stderr);raise SystemExit(1)
lab=work/('lab-'+uuid.uuid4().hex);lab.mkdir();mods=lab/'mods';mods.mkdir()
for p in args.dependencies.glob('*.jar'):
 if 'fabric-api' in p.name.lower() or 'geckolib' in p.name.lower() or 'bridge' in p.name.lower():shutil.copy2(p,mods/p.name)
shutil.copy2(args.candidate,mods/'voiddynasty-candidate.jar')
with zipfile.ZipFile(mods/'work-test.jar','w',zipfile.ZIP_DEFLATED) as z:
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
 z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'work-test','version':'1','environment':'server','entrypoints':{'main':['com.donopot.voiddynasty.work.'+p.stem for p in test_sources]},'depends':{'voiddynasty':'*','fabric-api':'*'}}))
reservations=[reserve(),reserve()];port,rport=[x[1] for x in reservations];password=secrets.token_urlsafe(32)
props={'server-ip':'127.0.0.1','server-port':port,'enable-rcon':'true','rcon.port':rport,'rcon.password':password,'online-mode':'false','level-name':'work-test-world','level-type':'minecraft:flat','generate-structures':'false','generator-settings':json.dumps({'layers':[{'block':'minecraft:bedrock','height':1}],'biome':'minecraft:plains'}),'spawn-protection':0,'view-distance':4,'simulation-distance':4,'pause-when-empty-seconds':0,'sync-chunk-writes':'true','max-players':1}
(lab/'server.properties').write_text(''.join(f'{k}={v}\n' for k,v in props.items()))
(lab/'eula.txt').write_text('eula=true\n') # User accepted EULA for these disposable local tests.
exact=resolve(args.minecraft.resolve())
launch_args=['-Xmx2G','-Dfabric.gameJarPath='+str(exact['game']),'-cp',os.pathsep.join(map(str,[exact['game'],*exact['libraries']])),'net.fabricmc.loader.impl.launch.knot.KnotServer','nogui']
(lab/'launch.args').write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in launch_args))
for sock,_ in reservations:sock.close()
(work/'latest-lab.txt').write_text(str(lab));print('LAB',lab,flush=True)
results=[]
mission_notes={}
def run_phase(rcon,name):
 result=rcon.cmd('workcheck '+name);print(name,result,flush=True);results.append({'phase':name,'result':result})
 if 'PASS '+name not in result:raise RuntimeError(result)
def launch():
 output=(lab/('process-'+str(len(results))+'.log')).open('w',encoding='utf-8');proc=subprocess.Popen([str(jdk/'java.exe'),'@'+str(lab/'launch.args')],cwd=lab,stdout=output,stderr=subprocess.STDOUT)
 deadline=time.monotonic()+120
 while time.monotonic()<deadline:
  log=lab/'logs/latest.log';text=log.read_text(encoding='utf-8',errors='replace') if log.exists() else ''
  if proc.poll() is not None:raise RuntimeError('Lab exited; see process log')
  if 'Done (' in text:
   try:return proc,Rcon(rport,password),output
   except OSError:pass
  time.sleep(.5)
 proc.terminate();proc.wait(timeout=20);raise TimeoutError('Lab startup timeout')
proc=rcon=output=None
try:
 proc,rcon,output=launch()
 for cmd in ['forceload add 0 0 63 47','time set midnight','gamerule minecraft:advance_time false']:
  print(cmd,rcon.cmd(cmd),flush=True)
 if not args.next_only:
  time.sleep(3)
  run_phase(rcon,'rollback');run_phase(rcon,'mining')
  run_phase(rcon,'autoDepotSetup');time.sleep(5);run_phase(rcon,'autoDepotCheck')
  run_phase(rcon,'extendDepotSetup');time.sleep(5);run_phase(rcon,'extendDepotCheck')
  run_phase(rcon,'pendingSpawnSetup');time.sleep(3);run_phase(rcon,'pendingSpawnCheck')
  run_phase(rcon,'navigationSetup')
  time.sleep(25)
  run_phase(rcon,'navigationCheck');run_phase(rcon,'unreachableSetup')
  time.sleep(23)
  run_phase(rcon,'unreachableCheck')
  time.sleep(3)
  run_phase(rcon,'depositCheck');run_phase(rcon,'restartSetup')
  print(rcon.cmd('save-all flush'),flush=True);rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  # Ensure readiness log belongs to the new child, not the stopped session.
  (lab/'logs/latest.log').rename(lab/'logs/first-run.log')
  proc,rcon,output=launch();time.sleep(2);run_phase(rcon,'restartCheck')
  if args.missions:
   import asyncio
   from mission_acceptance import before_restart,after_restart
   asyncio.run(before_restart(rcon,lab,mission_notes))
   rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
   (lab/'logs/latest.log').rename(lab/'logs/mission-before-restart.log')
   proc,rcon,output=launch();time.sleep(2)
   asyncio.run(after_restart(rcon,lab,mission_notes))
 if args.missions:
  from work_next_acceptance import before_restart as next_before,after_restart as next_after
  asyncio.run(next_before(rcon,lab,mission_notes,skip_access=args.build_only))
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/build-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  asyncio.run(next_after(rcon,lab,mission_notes))
 print('ALL SERVER PHASES PASS',flush=True)
finally:
 (lab/'report.local.json').write_text(json.dumps(results,indent=2))
 if args.missions: (lab/'mission-report.local.json').write_text(json.dumps(mission_notes,indent=2))
 if proc is not None and proc.poll() is None:
  if rcon is not None:
   try:rcon.cmd('stop')
   except Exception:pass
  try:proc.wait(timeout=45)
  except subprocess.TimeoutExpired:proc.terminate();proc.wait(timeout=20)
 if output:output.close()
