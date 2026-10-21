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
parser.add_argument('--empty-only',action='store_true',help='Only run empty/exhausted selection regressions')
parser.add_argument('--navigation-only',action='store_true',help='Focus on movement, excavated stairs, route changes and clean restart')
parser.add_argument('--brain-only',action='store_true',help='Exercise native AI validation, bags, ownership, MCP observation and clean restart; no LLM calls')
parser.add_argument('--reliability-only',action='store_true',help='Movement, incomplete routines, full gather/return and restart regression')
parser.add_argument('--recovery-only',action='store_true',help='Exercise companion bag recovery after death, dimension change and clean restart; no LLM calls')
parser.add_argument('--accept-eula',action='store_true',required=True,help='Explicit acceptance of Minecraft EULA for the disposable server')
args=parser.parse_args()
if args.brain_only: args.empty_only=True
if args.recovery_only or args.reliability_only: args.empty_only=True
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
test_sources.append(test.with_name('WorkEmptyServerTest.java'))
if not args.empty_only: test_sources.append(test.with_name('WorkNavigationServerTest.java'))
if args.brain_only: test_sources.append(test.with_name('BrainSafetyServerTest.java'))
if args.recovery_only: test_sources.append(test.with_name('BrainRecoveryServerTest.java'))
if args.reliability_only: test_sources.append(test.parent.parent/'ai/BrainReliabilityServerTest.java')
if args.missions: test_sources.extend([test.with_name('MissionServerTest.java'),test.with_name('WorkNextServerTest.java')])
javac=jdk/('javac.exe' if os.name=='nt' else 'javac');javab=jdk/('java.exe' if os.name=='nt' else 'java')
r=subprocess.run([str(javac),'--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,test_sources)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=60)
(work/'server-test-compile.log').write_text(r.stdout+r.stderr,encoding='utf-8')
if r.returncode:print(r.stdout+r.stderr);raise SystemExit(1)
lab=work/('lab-'+uuid.uuid4().hex);lab.mkdir();mods=lab/'mods';mods.mkdir()
for p in args.dependencies.glob('*.jar'):
 if 'fabric-api' in p.name.lower() or 'geckolib' in p.name.lower() or 'bridge' in p.name.lower():shutil.copy2(p,mods/p.name)
shutil.copy2(args.candidate,mods/'voiddynasty-candidate.jar')
with zipfile.ZipFile(mods/'work-test.jar','w',zipfile.ZIP_DEFLATED) as z:
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
 z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'work-test','version':'1','environment':'server','entrypoints':{'main':['com.donopot.voiddynasty.'+p.parent.name+'.'+p.stem for p in test_sources]},'depends':{'voiddynasty':'*','fabric-api':'*'}}))
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
 output=(lab/('process-'+str(len(results))+'.log')).open('w',encoding='utf-8');proc=subprocess.Popen([str(javab),'@'+str(lab/'launch.args')],cwd=lab,stdout=output,stderr=subprocess.STDOUT)
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
 for phase in ['empty','noKit','noSpawn','large','stale','protected','carry']:
  for suffix in ['Setup','Check']:
   name=phase+suffix; result=rcon.cmd('emptycheck '+name); print(name,result,flush=True);results.append({'phase':name,'result':result})
   if 'PASS '+name not in result: raise RuntimeError(result)
   if suffix=='Setup':time.sleep(3 if phase=='carry' else 1)
 result=rcon.cmd('emptycheck idle');print('idle',result,flush=True);results.append({'phase':'idle','result':result})
 if 'PASS idle' not in result: raise RuntimeError(result)
 if args.brain_only:
  from brain_work_acceptance import before_restart as brain_before,after_restart as brain_after
  asyncio.run(brain_before(rcon,lab,results,mission_notes))
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/brain-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  asyncio.run(brain_after(rcon,lab,results,mission_notes))
 if args.reliability_only:
  def reliability(name, seconds=0):
   if seconds: time.sleep(seconds)
   answer=rcon.cmd('reliabilitycheck '+name);print(answer,flush=True);results.append({'phase':name,'result':answer})
   if 'PASS '+name not in answer:raise AssertionError(answer)
  reliability('notes');reliability('setup');reliability('moved',25)
  # Load the completed companion for the next command, as an arriving player would.
  # Movement and the gather/return cycle themselves run without this fixture ticket.
  rcon.cmd('forceload add 998 998 1036 1012');time.sleep(2)
  reliability('errors');reliability('incomplete');reliability('incompleteDone',2);reliability('gather')
  rcon.cmd('forceload remove 998 998 1036 1012');time.sleep(40)
  rcon.cmd('forceload add 998 998 1036 1012');time.sleep(2)
  reliability('returned')
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/reliability-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  reliability('restart');rcon.cmd('forceload remove 998 998 1036 1012');time.sleep(25)
  rcon.cmd('forceload add 998 998 1036 1012');time.sleep(2)
  reliability('resumed');reliability('second',5)
 if args.recovery_only:
  from brain_recovery_acceptance import before_restart as rec_before,after_restart as rec_after
  rec_before(rcon,lab,results,mission_notes)
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/recovery-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  rec_after(rcon,lab,results,mission_notes)
 if not args.next_only and not args.empty_only and not args.navigation_only:
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
 if args.missions and not args.empty_only:
  from work_next_acceptance import before_restart as next_before,after_restart as next_after
  asyncio.run(next_before(rcon,lab,mission_notes,skip_access=args.build_only))
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/build-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  asyncio.run(next_after(rcon,lab,mission_notes))
 if not args.empty_only and not args.next_only:
  from work_navigation_acceptance import before_restart as nav_before,after_restart as nav_after
  nav_before(rcon,results)
  rcon.cmd('save-all flush');rcon.cmd('stop');proc.wait(timeout=45);rcon.sock.close();rcon=None;output.close()
  (lab/'logs/latest.log').rename(lab/'logs/navigation-before-restart.log')
  proc,rcon,output=launch();time.sleep(2)
  nav_after(rcon,results)
 print('ALL SERVER PHASES PASS',flush=True)
finally:
 (lab/'report.local.json').write_text(json.dumps(results,indent=2))
 if args.missions or args.brain_only: (lab/'mission-report.local.json').write_text(json.dumps(mission_notes,indent=2))
 if proc is not None and proc.poll() is None:
  if rcon is not None:
   try:rcon.cmd('stop')
   except Exception:pass
  try:proc.wait(timeout=45)
  except subprocess.TimeoutExpired:proc.terminate();proc.wait(timeout=20)
 if output:output.close()
