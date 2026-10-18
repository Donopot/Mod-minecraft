"""Combined real-client HUD/input acceptance in a new loopback-only world and profile.
Only this runner's child JVMs are stopped. No normal options, mods, accounts or saves touched.
"""
from pathlib import Path
import argparse,hashlib,json,os,secrets,shutil,subprocess,sys,time,uuid,zipfile

p=argparse.ArgumentParser(description=__doc__)
for name in ('void','onepiece','dependencies','classpath-json','jdk','minecraft','assets','output'):p.add_argument('--'+name,required=True,type=Path)
p.add_argument('--accept-eula',required=True,action='store_true')
args=p.parse_args();repo=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(repo/'onepiece/tools'))
from installed_runtime import resolve
from run_local_tests import Rcon,reserve
lab=args.output.resolve()/('run-'+uuid.uuid4().hex);lab.mkdir(parents=True)
classes=lab/'test-classes';classes.mkdir()
sources=sorted((repo/'voiddynasty/src/test/java/com/donopot/voiddynasty/client').glob('Hud*Test.java'))
config=json.loads(args.classpath_json.read_text(encoding='utf-8-sig'))
cp=os.pathsep.join([str(args.void.resolve()),str(args.onepiece.resolve()),*config['classpath']])
jdk=args.jdk.resolve()/'bin'
r=subprocess.run([str(jdk/'javac.exe'),'--release','25','-encoding','UTF-8','-proc:none','-cp',cp,'-d',str(classes),*map(str,sources)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=60)
(lab/'test-compile.log').write_text(r.stdout+r.stderr,encoding='utf-8')
if r.returncode:print(r.stdout+r.stderr);raise SystemExit(r.returncode)
mods=lab/'mods';mods.mkdir()
for jar in args.dependencies.glob('*.jar'):
    if any(part in jar.name.lower() for part in ['fabric-api','geckolib','playeranimlib','bridge']):shutil.copy2(jar,mods/jar.name)
for jar in [args.void,args.onepiece]:shutil.copy2(jar,mods/jar.name)
with zipfile.ZipFile(mods/'hud-test.jar','w',zipfile.ZIP_DEFLATED) as z:
    for c in classes.rglob('*.class'):z.write(c,c.relative_to(classes).as_posix())
    z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'hud-test','version':'1','environment':'*','entrypoints':{'main':['com.donopot.voiddynasty.client.HudServerTest'],'client':['com.donopot.voiddynasty.client.HudClientTest']},'depends':{'voiddynasty':'*','opworldlike':'*','fabric-api':'*'}}))
reservations=[reserve(),reserve()];port,rport=[x[1] for x in reservations];password=secrets.token_urlsafe(32)
props={'server-ip':'127.0.0.1','server-port':port,'enable-rcon':'true','rcon.port':rport,'rcon.password':password,'online-mode':'false','level-name':'hud-test-world','level-type':'minecraft:flat','generate-structures':'false','generator-settings':json.dumps({'layers':[{'block':'minecraft:bedrock','height':1},{'block':'minecraft:stone','height':60},{'block':'minecraft:dirt','height':2},{'block':'minecraft:grass_block','height':1}],'biome':'minecraft:plains'}),'spawn-protection':0,'view-distance':4,'simulation-distance':4,'pause-when-empty-seconds':0,'max-players':1,'difficulty':'peaceful'}
(lab/'server.properties').write_text(''.join(f'{k}={v}\n' for k,v in props.items()),encoding='utf-8')
(lab/'eula.txt').write_text('eula=true\n',encoding='utf-8')
username='HudLab';player_uuid=uuid.UUID(bytes=hashlib.md5(('OfflinePlayer:'+username).encode()).digest(),version=3)
(lab/'ops.json').write_text(json.dumps([{'uuid':str(player_uuid),'name':username,'level':4,'bypassesPlayerLimit':True}]),encoding='utf-8')
exact=resolve(args.minecraft.resolve());runtime_cp=os.pathsep.join(map(str,[exact['game'],*exact['libraries']]))
def argfile(path,entries):path.write_text('\n'.join('"'+str(item).replace('\\','/')+'"' for item in entries),encoding='utf-8')
argfile(lab/'server.args',['-Xmx2G','-Dfabric.gameJarPath='+str(exact['game']),'-cp',runtime_cp,'net.fabricmc.loader.impl.launch.knot.KnotServer','nogui'])
client=lab/'client';client.mkdir();shutil.copytree(mods,client/'mods')
options={'fullscreen':'false','lang':'fr_fr','renderDistance':4,'simulationDistance':4,'guiScale':2,'maxFps':60,'pauseOnLostFocus':'false','onboardAccessibility':'false','skipMultiplayerWarning':'true','soundCategory_master':0,'tutorialStep':'none'}
(client/'options.txt').write_text(''.join(f'{k}:{v}\n' for k,v in options.items()),encoding='utf-8')
client_args=['-Xmx2G','-XX:StackShadowPages=32','--enable-native-access=ALL-UNNAMED','--add-exports','java.base/jdk.internal.misc=ALL-UNNAMED','-Dfabric.gameJarPath='+str(exact['game']),'-Djava.library.path='+str(client/'natives/java'),'-Djna.tmpdir='+str(client/'natives/jna'),'-Dorg.lwjgl.system.SharedLibraryExtractPath='+str(client/'natives/lwjgl'),'-Dio.netty.native.workdir='+str(client/'natives/netty'),'-cp',runtime_cp,'net.fabricmc.loader.impl.launch.knot.KnotClient','--username',username,'--uuid',player_uuid.hex,'--accessToken','0','--version','26.3','--versionType','private-test','--gameDir',str(client),'--assetsDir',str(args.assets.resolve()),'--assetIndex',exact['asset_index'],'--width','1100','--height','800','--quickPlayMultiplayer','127.0.0.1:'+str(port)]
argfile(client/'client.args',client_args)
for sock,_ in reservations:sock.close()
(args.output.resolve()/'latest-lab.txt').write_text(str(lab),encoding='utf-8');print('LAB',lab,flush=True)
server=game=rcon=None;handles=[];report={'lab':str(lab),'candidate_sha256':{jar.name:hashlib.sha256(jar.read_bytes()).hexdigest() for jar in [args.void,args.onepiece]},'passed':False}
try:
    output=(lab/'server-process.log').open('w',encoding='utf-8');handles.append(output)
    server=subprocess.Popen([str(jdk/'java.exe'),'@'+str(lab/'server.args')],cwd=lab,stdout=output,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    deadline=time.monotonic()+150
    while time.monotonic()<deadline:
        if server.poll() is not None:raise RuntimeError('Test server exited; inspect log')
        log=lab/'logs/latest.log'
        if log.exists() and 'Done (' in log.read_text(encoding='utf-8',errors='replace'):break
        time.sleep(.5)
    else:raise TimeoutError('Server startup')
    rcon=Rcon(rport,password);rcon.cmd('time set day');rcon.cmd('gamerule minecraft:advance_time false')
    for reopen in [False,True]:
        name='reopen' if reopen else 'initial'
        if reopen:argfile(client/'client.args',['-Dhudtest.reopen=true',*client_args])
        output=(lab/('client-'+name+'.log')).open('w',encoding='utf-8');handles.append(output)
        game=subprocess.Popen([str(jdk/'java.exe'),'@'+str(client/'client.args')],cwd=client,stdout=output,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
        try:game.wait(timeout=240)
        except subprocess.TimeoutExpired:raise TimeoutError('Owned client test '+name)
        text=(lab/('client-'+name+'.log')).read_text(encoding='utf-8',errors='replace')
        if game.returncode!=0 or 'HUD_CLIENT COMPLETE' not in text or 'HUD_CLIENT FAIL' in text:raise RuntimeError('Client test failed: '+name+'; inspect log')
        print('CLIENT PASS',name,flush=True)
        time.sleep(1)
    text=(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace')
    expected=['menu','cast','reload','prepareLegacy','legacy']
    if any('HUD_SERVER PASS '+phase not in text for phase in expected) or 'HUD_SERVER FAIL' in text:raise RuntimeError('Server input verification failed')
    images=sorted((client/'screenshots').glob('*.png'))
    if len(images)!=8:raise RuntimeError('Missing captures: '+str(len(images)))
    report.update(passed=True,captures=[str(path) for path in images],server_phases=expected)
    print('ALL HUD/INPUT PHASES PASS; 8 screenshots ready for inspection',flush=True)
finally:
    if game is not None and game.poll() is None:game.terminate();game.wait(timeout=30)
    if server is not None and server.poll() is None:
        if rcon is not None:
            try:rcon.cmd('stop')
            except Exception:pass
        try:server.wait(timeout=45)
        except subprocess.TimeoutExpired:server.terminate();server.wait(timeout=30)
    for handle in handles:handle.close()
    (lab/'report.local.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
