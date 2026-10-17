"""Prepare a frozen, private graphical test profile; only --run opens Minecraft.

No launcher account files or access tokens are read. Local testing uses FruitLab
on a server bound only to 127.0.0.1. Neither normal mods nor saves are changed.
"""
from pathlib import Path
import argparse, hashlib, json, os, re, socket, subprocess, sys, time, uuid, zipfile, shutil
from installed_runtime import resolve
from run_local_tests import Rcon

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parent

def prepare(accept_eula, capture=False, scenario="foundation",retake="",forms_only=False):
    cmd = [sys.executable, str(PROJECT / 'tools/run_local_tests.py'), '--prepare']
    if accept_eula: cmd.append('--accept-eula')
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
    except subprocess.CalledProcessError as error:
        print(error.stdout or '', file=sys.stderr)
        print(error.stderr or '', file=sys.stderr)
        raise
    lab = Path(next(line[4:] for line in result.stdout.splitlines() if line.startswith('LAB ')))
    manifest = json.loads((lab / 'report.local.json').read_text())
    config = json.loads((ROOT / '.runtime/fabric-build/build.local.json').read_text())
    build = json.loads((PROJECT / 'build/build-local.json').read_text())
    exact = resolve(config['minecraft'])
    client = lab / 'client'
    (client / 'mods').mkdir(parents=True)
    for jar in (lab / 'mods').glob('*.jar'): shutil.copy2(jar, client / 'mods' / jar.name)
    for dep in map(Path, build['classpath']):
        with zipfile.ZipFile(dep) as archive:
            if 'fabric.mod.json' in archive.namelist():
                metadata = json.loads(archive.read('fabric.mod.json'))
                if metadata['id'] == 'player_animation_library':
                    shutil.copy2(dep, client / 'mods' / dep.name)
                    break
    else: raise RuntimeError('Missing local player animation library')
    index = Path(config['minecraft']) / 'assets/indexes' / (exact['asset_index'] + '.json')
    if not index.is_file(): raise RuntimeError('Installed asset index missing')
    objects = json.loads(index.read_text())['objects']
    missing = [key for key, value in objects.items() if not (index.parent.parent / 'objects' / value['hash'][:2] / value['hash']).is_file()]
    if missing: raise RuntimeError(f'Installed asset cache incomplete: {len(missing)} objects missing')
    options = {'fullscreen': 'false', 'lang': 'fr_fr', 'renderDistance': 6, 'simulationDistance': 5,
               'guiScale': 2, 'maxFps': 60, 'pauseOnLostFocus': 'false', 'onboardAccessibility': 'false',
               'skipMultiplayerWarning': 'true', 'soundCategory_master': .4, 'tutorialStep': 'none', 'showSubtitles': 'false'}
    (client / 'options.txt').write_text(''.join(f'{k}:{v}\n' for k,v in options.items()), encoding='utf-8')
    natives = client / 'natives'
    natives.mkdir()
    offline_uuid = uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:FruitLab').digest(), version=3).hex
    cp = os.pathsep.join(map(str, [exact['game'], *exact['libraries']]))
    arguments = ['-Xmx2G', '-XX:StackShadowPages=32', '--enable-native-access=ALL-UNNAMED',
        '--add-exports', 'java.base/jdk.internal.misc=ALL-UNNAMED',
        '-Dfabric.gameJarPath='+str(exact['game']), '-Djava.library.path='+str(natives / 'java'),
        '-Djna.tmpdir='+str(natives / 'jna'), '-Dorg.lwjgl.system.SharedLibraryExtractPath='+str(natives / 'lwjgl'),
        '-Dio.netty.native.workdir='+str(natives / 'netty'), '-cp', cp,
        'net.fabricmc.loader.impl.launch.knot.KnotClient', '--username', 'FruitLab', '--uuid', offline_uuid,
        '--accessToken', '0', '--version', '26.3', '--versionType', 'private-test', '--gameDir', str(client),
        '--assetsDir', str(index.parent.parent), '--assetIndex', exact['asset_index'], '--width', '1100', '--height', '700',
        '--quickPlayMultiplayer', '127.0.0.1:' + str(manifest['port'])]
    if forms_only: arguments.insert(0, "-Dopworldlike.formsOnly=true")
    if capture: arguments.insert(0, '-Dopworldlike.visualCapture=true')
    arguments.insert(0, '-Dopworldlike.visualScenario='+scenario)
    if retake: arguments.insert(0, '-Dopworldlike.batchRetake='+retake)
    (client / 'launch.args').write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in arguments), encoding='utf-8')
    server_args = lab / 'launch.args'
    server_args.write_text('"-Dopworldlike.visualLab=true"\n"-Dopworldlike.visualScenario='+scenario+'"\n'+server_args.read_text(), encoding='utf-8')
    if capture:
        server_args.write_text('"-Dopworldlike.visualCapture=true"\n'+server_args.read_text(),encoding='utf-8')
    frozen = {'jdk': build['jdk'], 'build_sha256': build['sha256'], 'lab': str(lab), 'client': str(client),
              'client_mods': {jar.name: hashlib.sha256(jar.read_bytes()).hexdigest() for jar in (client / 'mods').glob('*.jar')},
              'rendered': False, 'launched': False, 'capture': capture, 'scenario': scenario, 'retake':retake,'forms_only':forms_only}
    if capture and scenario in ('mera','magu','goro','amaru','batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08'):
        observer = lab / 'observer'
        shutil.copytree(client, observer)
        observer_args = [arg.replace(str(client),str(observer)).replace('FruitLab','FruitWitness').replace(offline_uuid,uuid.UUID(bytes=hashlib.md5(b'OfflinePlayer:FruitWitness').digest(),version=3).hex) for arg in arguments]
        (observer / 'launch.args').write_text('\n'.join('"'+arg.replace('\\','/')+'"' for arg in observer_args),encoding='utf-8')
        frozen['observer'] = str(observer)
    (lab / 'visual-lab.local.json').write_text(json.dumps(frozen, indent=2), encoding='utf-8')
    (lab / 'Lancer-le-test.cmd').write_text('@echo off\r\n"'+sys.executable+'" "'+str(Path(__file__).resolve())+'" --run --lab "'+str(lab)+'"\r\npause\r\n', encoding='utf-8')
    print('VISUAL_LAB_PREPARED', lab)
    print('No client or server launched.')
    return lab

def run(lab,fruit="hie_hie",resume=False,fresh_scene=False):
    frozen = json.loads((lab / 'visual-lab.local.json').read_text())
    manifest = json.loads((lab / 'report.local.json').read_text())
    if 'eula=true' not in (lab / 'eula.txt').read_text(): raise RuntimeError('Minecraft EULA acceptance is required')
    client = Path(frozen['client'])
    for name, digest in frozen['client_mods'].items():
        if hashlib.sha256((client / 'mods' / name).read_bytes()).hexdigest() != digest:
            raise RuntimeError('Frozen test mod changed: ' + name)
    for port in [manifest['port'], manifest['rcon_port']]:
        with socket.socket() as check: check.bind(('127.0.0.1', port))
    settings = dict(line.split('=', 1) for line in (lab / 'server.properties').read_text().splitlines() if '=' in line)
    manual_state=lab/'manual-state.local.json'
    previous_manual=json.loads(manual_state.read_text(encoding='utf-8')) if manual_state.exists() else {}
    resume_scene=resume and not fresh_scene and previous_manual.get('fruit')==fruit and (lab/settings['level-name']/'level.dat').is_file()
    server_arguments=lab/'launch.args'
    if resume_scene:
        # A normal join preserves saved fruit/location. The lab JOIN hook would replace both.
        server_arguments=lab/'resume-launch.args'
        server_arguments.write_text((lab/'launch.args').read_text(encoding='utf-8').replace('-Dopworldlike.visualLab=true','-Dopworldlike.visualLab=false').replace('-Dopworldlike.repairTests=true','-Dopworldlike.repairTests=false'),encoding='utf-8')
    server = game = observer_game = observer_output = rcon = None
    java = Path(frozen['jdk']) / 'bin/java.exe'
    frozen['launched'] = True
    try:
        with (lab / 'visual-server.log').open('w', encoding='utf-8') as output, (client / 'process.log').open('w', encoding='utf-8') as client_output:
            server = subprocess.Popen([str(java), '@'+str(server_arguments)], cwd=lab, stdout=output, stderr=subprocess.STDOUT)
            deadline = time.monotonic() + 120
            log = lab / 'logs/latest.log'
            while time.monotonic() < deadline:
                if server.poll() is not None: raise RuntimeError('Test server failed; see visual-server.log')
                text = log.read_text(errors='replace') if log.exists() else ''
                if 'Done (' in text:
                    try:
                        rcon = Rcon(manifest['rcon_port'], settings['rcon.password'])
                        break
                    except OSError: pass
                time.sleep(.5)
            else: raise TimeoutError('Private server readiness timeout')
            rcon.cmd('forceload add 0 0 24 24'); time.sleep(2)
            if not resume_scene:
                # This preflight rebuilds the arena; never rerun it over saved manual terrain.
                rcon.cmd('drepair')
                deadline = time.monotonic() + 30
                while '[drepair] END passed=59 failed=0 total=59' not in log.read_text(encoding='utf-8', errors='replace'):
                    if time.monotonic() > deadline: raise RuntimeError('Graphical lab preflight did not pass its 59 checks')
                    time.sleep(.5)
            rcon.cmd('time set day' if frozen.get('scenario')=='batch06' else 'time set noon')
            if frozen.get('scenario') in ('mera','magu','goro','amaru','batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08'): rcon.cmd('forceload add -16 -16 64 64')
            game = subprocess.Popen([str(java.with_name('javaw.exe')), '@'+str(client / 'launch.args')],
                                    cwd=client, stdout=client_output, stderr=subprocess.STDOUT)
            if frozen.get('observer'):
                observer = Path(frozen['observer'])
                observer_output = (observer / 'process.log').open('w',encoding='utf-8')
                observer_game = subprocess.Popen([str(java.with_name('javaw.exe')), '@'+str(observer / 'launch.args')],cwd=observer,stdout=observer_output,stderr=subprocess.STDOUT)
            print('Private Minecraft test running. Close its window to stop its server. Normal installation untouched.', flush=True)
            deadline = time.monotonic() + 120
            joined = False
            while time.monotonic() < deadline and game.poll() is None:
                if 'FruitLab' in rcon.cmd('list'):
                    rcon.cmd('op FruitLab'); joined = True; break
                time.sleep(1)
            if not joined: raise RuntimeError('Graphical client did not join the local server; see client/process.log')
            if frozen.get('scenario') in ('batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08') and not frozen.get('capture'):
                initial={'mera_mera':'hiken','magu_magu':'daifunka','goro_goro':'vari','gura_gura':'attacksweep','bomu_bomu':'nosefancycannon','zushi_zushi':'attackspeed','nikyu_nikyu':'padho','ope_ope':'stoneopeope','hie_hie':'icepartisan','pika_pika':'laser1','moku_moku':'whiteblow','suna_suna':'desertspada','yami_yami':'kurouzu','mochi_mochi':'mochipistol','mori_mori':'root','kira_kira':'attackoverhead','supa_supa':'projectileslash','riki_riki':'attackoverhead','aro_aro':'arrow','horo_horo':'negativehollow','mero_mero':'pistolkiss','kiro_kiro':'attackoverhead','bisu_bisu':'crashpretzel','kage_kage':'attackoverhead','soru_soru':'attackoverhead','uta_uta':'noteattack','imu':'imuarrow','gomu_gomu':'gomu_pistol','inu_inu_okuchi_no_makami':'attackoverhead','hito_hito_daibutsu':'attackoverhead','tori_tori_phoenix':'attackspeed','uo_uo_seiryu':'attackoverhead','ryu_ryu_pteranodon':'attackspeed','inu_inu_wolf':'shigan','neko_neko_leopard':'shigan','neko_neko_sabertiger':'shigan','ushi_ushi_giraffe':'shigan','batto_batto':'shigan','ryu_ryu_brachiosaurus':'attackoverhead','ryu_ryu_pachycephalosaurus':'attackjump'}
                if not resume_scene:
                    rcon.cmd('execute as FruitLab run dvisual '+fruit+' '+initial[fruit])
                    manual_state.write_text(json.dumps({'fruit':fruit}),encoding='utf-8')
                message={'text':('Reprise du terrain sauvegarde, commandes de decor desactivees. ' if resume_scene else 'Nouvelle scene de laboratoire. /dvisual reconstruit le decor. ')+'R technique, B suivante, F5 camera.','color':'gold'}
                rcon.cmd('tellraw FruitLab '+json.dumps(message))
            if frozen.get('observer'):
                deadline=time.monotonic()+90
                while 'FruitWitness' not in rcon.cmd('list'):
                    if observer_game.poll() is not None: raise RuntimeError('Observer failed before joining')
                    if time.monotonic()>deadline: raise TimeoutError('Observer did not join')
                    time.sleep(.5)
                rcon.cmd('op FruitWitness')
                rcon.cmd('dmeraready')
            deadline = time.monotonic() + (650 if frozen.get('scenario') in ('batch06','batch07','batch08') else 380 if frozen.get('scenario')=='batch05' else 260 if frozen.get('scenario') in ('batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08') else 120)
            while game.poll() is None and server.poll() is None:
                if frozen.get('capture') and time.monotonic() > deadline:
                    raise TimeoutError('Capture timeline did not finish')
                time.sleep(1)
            if game.poll() not in (None, 0): raise RuntimeError('Graphical client exited with an error; see client/process.log')
            if observer_game is not None:
                observer_game.wait(timeout=30)
                if observer_game.returncode != 0: raise RuntimeError('Observer client exited with an error')
            if frozen.get('capture') and frozen.get('scenario')=='batch08':
                catalogue={'ryu_ryu_brachiosaurus': ['attackoverhead', 'brachiobober', 'blackcofee', 'brachiolauncher', 'sparkingqueen'], 'ryu_ryu_pachycephalosaurus': ['attackjump', 'attacksweep', 'headbutt', 'ulzugan', 'ulmeteor']}
                short={'ryu_ryu_brachiosaurus': 'brachio', 'ryu_ryu_pachycephalosaurus': 'pachy'}
                if frozen.get('retake'):catalogue={f:ms for f,ms in catalogue.items() if f in frozen['retake'].split(',')}
                from collections import Counter
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/forms-{phase}' for f in catalogue for phase in ['hybrid','beast','base']}
                expected_pal=Counter(f'b08_{short[f]}_{m}{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['', '_release'])
                for f,ms in catalogue.items():
                    for suffix in ['', '_release']:expected_pal[f'b08_{short[f]}_{ms[0]}'+suffix]+=2
                if frozen.get('forms_only'):
                    count=0;required={cid for cid in required if '/forms-' in cid};expected_pal=Counter()
                    for f,ms in catalogue.items():
                        for suffix in ['', '_release']:expected_pal['b08_'+short[f]+'_'+ms[0]+suffix]+=2
                expected_images=count*5+len(catalogue)*6
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=expected_images:raise RuntimeError(f'B08 {who}: expected{expected_images} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace');rows=re.findall(r'\[batch08-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(required) or {cid for cid,state,_ in rows if state=='PASS'}!=required or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B08 missing/failed render/form checks on '+who)
                    if 'Unable to find animation:' in body or 'GeckoLibException' in body:raise RuntimeError('B08 native mesh/animation failure on '+who)
                    poses=re.findall(r'\[batch08-pal\] (\S+) (PASS|FAIL)',body);frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B08 missing/failed evaluated poses on '+who)
                frozen['rendered']=True
                print(f'BATCH08_CAPTURED {expected_images}+{expected_images} images, {sum(expected_pal.values())} evaluated phases/client. Inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch07':
                catalogue={'inu_inu_okuchi_no_makami': ['attackoverhead', 'attacksweep', 'raimeihakke', 'narikabura', 'namujihyoga', 'himorogiri', 'hyogaheki'], 'hito_hito_daibutsu': ['attackoverhead', 'brilliantpunk', 'impactball', 'overheadsuper', 'impactwave'], 'tori_tori_phoenix': ['attackspeed', 'ongle', 'bluebird', 'marcotornado', 'hooin', 'fujiazami'], 'uo_uo_seiryu': ['attackoverhead', 'attacksweep', 'raimeihakke', 'kongokabura', 'kaifu', 'borobreath', 'kosanze', 'ikoku'], 'ryu_ryu_pteranodon': ['attackspeed', 'tankyudon', 'barizodon']}
                short={'inu_inu_okuchi_no_makami': 'makami', 'hito_hito_daibutsu': 'daibutsu', 'tori_tori_phoenix': 'phoenix', 'uo_uo_seiryu': 'seiryu', 'ryu_ryu_pteranodon': 'ptero'}
                if frozen.get('retake'):catalogue={f:ms for f,ms in catalogue.items() if f in frozen['retake'].split(',')}
                from collections import Counter
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/forms-{phase}' for f in catalogue for phase in ['hybrid','beast','base']}
                required|={f'{f}/forms-native-flight' for f in catalogue if f in {'tori_tori_phoenix','uo_uo_seiryu','ryu_ryu_pteranodon'}}
                expected_pal=Counter(f'b07_{short[f]}_{m}{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['', '_release'])
                for f,ms in catalogue.items():
                    for suffix in ['', '_release']:expected_pal[f'b07_{short[f]}_{ms[0]}'+suffix]+=2
                if frozen.get('forms_only'):
                    count=0;required={cid for cid in required if '/forms-' in cid};expected_pal=Counter()
                    for f,ms in catalogue.items():
                        for suffix in ['', '_release']:expected_pal['b07_'+short[f]+'_'+ms[0]+suffix]+=2
                expected_images=count*5+len(catalogue)*6
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=expected_images:raise RuntimeError(f'B07 {who}: expected{expected_images} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace');rows=re.findall(r'\[batch07-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(required) or {cid for cid,state,_ in rows if state=='PASS'}!=required or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B07 missing/failed render/form checks on '+who)
                    if 'Unable to find animation:' in body or 'GeckoLibException' in body:raise RuntimeError('B07 native mesh/animation failure on '+who)
                    poses=re.findall(r'\[batch07-pal\] (\S+) (PASS|FAIL)',body);frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B07 missing/failed evaluated poses on '+who)
                frozen['rendered']=True
                print(f'BATCH07_CAPTURED {expected_images}+{expected_images} images, {sum(expected_pal.values())} evaluated phases/client. Inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch06':
                catalogue={'inu_inu_wolf':['shigan','soru','rankyaku','lupusfall','okamihajiki','jushigan','localareanetwork','poursuite'],'neko_neko_leopard':['shigan','soru','rankyaku','gaicho','attackjump','shugan','rokuogan'],'neko_neko_sabertiger':['shigan','attackspeed','shiganmadara','gagan','kibasen'],'ushi_ushi_giraffe':['shigan','soru','rankyaku','rankyakuran','amanedachi'],'batto_batto':['shigan','soru','rankyaku','tobushigan','kamiezanshin','yurushitene']}
                short=dict(zip(catalogue,['wolf','leopard','saber','giraffe','batto']))
                if frozen.get('retake'):catalogue={f:ms for f,ms in catalogue.items() if f in frozen['retake'].split(',')}
                from collections import Counter
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/forms-{phase}' for f in catalogue for phase in ['hybrid','beast','base']}
                expected_pal=Counter(f'b06_{short[f]}_{m}{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['', '_release'])
                for f in catalogue:
                    for suffix in ['', '_release']:expected_pal[f'b06_{short[f]}_shigan'+suffix]+=2
                expected_images=count*5+len(catalogue)*6
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=expected_images:raise RuntimeError(f'B06 {who}: expected{expected_images} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace');rows=re.findall(r'\[batch06-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(required) or {cid for cid,state,_ in rows if state=='PASS'}!=required or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B06 missing/failed render/form checks on '+who)
                    if 'Unable to find animation:' in body or 'GeckoLibException' in body:raise RuntimeError('B06 native mesh/animation failure on '+who)
                    poses=re.findall(r'\[batch06-pal\] (\S+) (PASS|FAIL)',body);frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B06 missing/failed evaluated poses on '+who)
                if 'batto_batto' in catalogue:
                    native=re.findall(r'\[batch06-native\] (\S+) (PASS|FAIL) (.*)',(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace'))
                    frozen['native_player_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in native]
                    if len(native)!=3 or {cid for cid,state,_ in native if state=='PASS'}!={'counter-baseline','counter-hit','counter-expiry'} or any(state=='FAIL' for _,state,_ in native):raise RuntimeError('B06 real-player counter hook failed or evidence missing')
                frozen['rendered']=True
                print(f'BATCH06_CAPTURED {expected_images}+{expected_images} images, {sum(expected_pal.values())} evaluated phases/client. Inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch05':
                catalogue={'kage_kage':['attackoverhead','brickbat','tsunotokage','doppelman','islandshatterpunch','shadowasgard'],'soru_soru':['attackoverhead','attacksweep','raitei','hebunrifire','furugora','mamaraid','ikoku'],'uta_uta':['noteattack','notelaser','posing','noteshield','notewarrior','totmusicapunch','summontotmusica'],'imu':['imuarrow','silentcommand','sovereigngrasp'],'gomu_gomu':['gomu_pistol','gomu_bazooka','gomu_gatling','gear_change']}
                if frozen.get('retake'):catalogue={f:ms for f,ms in catalogue.items() if f in frozen['retake'].split(',')}
                from collections import Counter
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ms in catalogue.items() for m in ms}
                if 'uta_uta' in catalogue:required|={'uta_uta/notewarrior-second-soldier','uta_uta/noteshield-guard'}
                if 'gomu_gomu' in catalogue:required|={f'gomu_gomu/gear_change-form-{g}' for g in [2,3,4,5]}|{'gomu_gomu/gear_change-base'}
                expected_pal=Counter(f'b05_{f.split("_")[0]}_{m}{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['', '_release'])
                if 'gomu_gomu' in catalogue:
                    for suffix in ['', '_release']:expected_pal['b05_gomu_gear_change'+suffix]+=3
                expected_images=count*5+(1 if 'gomu_gomu' in catalogue else 0)
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=expected_images:raise RuntimeError(f'B05 {who}: expected{expected_images} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace');rows=re.findall(r'\[batch05-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(required) or {cid for cid,state,_ in rows if state=='PASS'}!=required or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B05 missing/failed render/form checks on '+who)
                    if 'Unable to find animation:' in body or 'GeckoLibException' in body:raise RuntimeError('B05 native mesh/animation resource failure on '+who)
                    poses=re.findall(r'\[batch05-pal\] (\S+) (PASS|FAIL)',body);frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B05 missing/failed evaluated bone phases on '+who)
                frozen['rendered']=True
                print(f'BATCH05_CAPTURED {expected_images}+{expected_images} images, {sum(expected_pal.values())} evaluated phases/client. Actual inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch04':
                catalogue={'aro_aro':['arrow','move','combo','passive'],'horo_horo':['negativehollow','minihollow','tokuhoro','rap','kumac'],'mero_mero':['pistolkiss','roundhousekick','rotatekick','perfumefemur'],'kiro_kiro':['attackoverhead','attackjump','kirooverhead','weightmanipulation'],'bisu_bisu':['crashpretzel','biscuitwarrior','biscuitwarrior2','biscuit']}
                if frozen.get('retake'):catalogue={f:ms for f,ms in catalogue.items() if f in frozen['retake'].split(',')}
                from collections import Counter
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ms in catalogue.items() for m in ms}
                if 'horo_horo' in catalogue:required|={f'horo_horo/{m}-detonation' for m in ['minihollow','rap']}
                if 'mero_mero' in catalogue:required|={f'mero_mero/{m}-'+suffix for m in ['pistolkiss','perfumefemur'] for suffix in ['statue','statue-expired']}
                expected_pal=Counter(f'b04_{f.split("_")[0]}_{m}{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['', '_release'])
                if 'horo_horo' in catalogue:
                    for m in ['minihollow','rap']:
                        for suffix in ['', '_release']:expected_pal['b04_horo_'+m+suffix]+=1
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=count*5:raise RuntimeError(f'B04 {who}: expected{count*5} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace');rows=re.findall(r'\[batch04-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    checks=required|({'aro_aro/move-movement','aro_aro/passive-movement'} if who=='actor' and 'aro_aro' in catalogue else set())
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(checks) or {cid for cid,state,_ in rows if state=='PASS'}!=checks or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B04 missing/failed render/movement checks on '+who)
                    poses=re.findall(r'\[batch04-pal\] (\S+) (PASS|FAIL)',body);frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B04 missing/failed evaluated bone phases on '+who)
                frozen['rendered']=True
                print(f'BATCH04_CAPTURED {count*5}+{count*5} images, {sum(expected_pal.values())} evaluated phases/client. Actual inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch03':
                catalogue={'mochi_mochi':['mochipistol','mochigatling','yakimochi','zangirimochi','musodonatsu','mochibuild'],'mori_mori':['root','rootgiant','assault','rootsurround'],'kira_kira':['attackoverhead','brilliantpunk','diamondshard','diamondavalanche','diamondbody'],'supa_supa':['projectileslash','atomicspurt','sparklingdaisy','sparbreak'],'riki_riki':['attackoverhead','hadoelbow','galleonlariat','stonethrowing','stonethrowinglarge']}
                if frozen.get('retake'):catalogue={f:ids for f,ids in catalogue.items() if f in frozen['retake'].split(',')}
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ids in catalogue.items() for m in ids}
                required|={f'supa_supa/{m}-'+suffix for m in catalogue.get('supa_supa',[]) for suffix in ['coat','coat-expired']}
                if 'kira_kira' in catalogue:required.add('diamond-expired')
                if 'mochi_mochi' in catalogue:required.add('mochi_mochi/musodonatsu-reactive')
                from collections import Counter
                expected_pal=Counter(f'b03_{f.split("_")[0]}_{m}{suffix}' for f,ids in catalogue.items() for m in ids for suffix in ['', '_release'])
                if 'mochi_mochi' in catalogue:
                    for suffix in ['', '_release']:expected_pal['b03_mochi_mochipistol'+suffix]+=1
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=count*5:raise RuntimeError(f'B03 {who}: expected{count*5} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
                    rows=re.findall(r'\[batch03-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    checks=required|({'diamond-retrack-absent','diamond-retrack-restored'} if who=='observer' and 'kira_kira' in catalogue else set())|({f'{f}/{m}-movement' for f,ids in catalogue.items() for m in ids if m in ['zangirimochi','brilliantpunk','atomicspurt','galleonlariat']} if who=='actor' else set())
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(checks) or {cid for cid,state,_ in rows if state=='PASS'}!=checks or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B03 missing/failed native render/movement checks on '+who)
                    poses=re.findall(r'\[batch03-pal\] (\S+) (PASS|FAIL)',body)
                    frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if Counter(cid for cid,state in poses if state=='PASS')!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B03 missing/failed evaluated poses on '+who)
                frozen['rendered']=True
                native=re.findall(r'\[batch03-native\] (\S+) (PASS|FAIL) (.*)',(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace'))
                frozen['native_player_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in native]
                if 'kira_kira' in catalogue and (len(native)!=2 or {cid for cid,state,_ in native if state=='PASS'}!={'diamond-reduction','diamond-armor-bypass'}):raise RuntimeError('Native diamond player damage proof failed')
                print(f'BATCH03_CAPTURED {count*5} actor+{count*5} observer; {sum(expected_pal.values())} evaluated phases/client. Actual image inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch02':
                catalogue={'gura_gura':['attacksweep','overheadsuper','gekishin','kabutowari','tenchimeido','kaishin'],'bomu_bomu':['nosefancycannon','kickbomb','punchbomb','bomboverhead','zenshinkibaku'],'zushi_zushi':['attackspeed','gravito','meteorsmall','moko','rubble','meteor'],'nikyu_nikyu':['padho','ursusshock','assault','kumapunch','passive'],'ope_ope':['stoneopeope','room','shambles','amputation']}
                if frozen.get('retake'):catalogue={f:ids for f,ids in catalogue.items() if f in frozen['retake'].split(',')}
                count=sum(map(len,catalogue.values()));required={f'{f}/{m}-render' for f,ids in catalogue.items() for m in ids}
                expected_pal={f'b02_{m}{suffix}' for ids in catalogue.values() for m in ids for suffix in ['', '_release']}
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'));frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=count*5:raise RuntimeError(f'B02 {who}: expected{count*5} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
                    rows=re.findall(r'\[batch02-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    native={m+'-movement' for m in ['kickbomb','attackspeed','assault','kumapunch','passive','shambles'] if any(m in ids for ids in catalogue.values())}
                    if 'bomu_bomu' in catalogue:native.add('bomboverhead-leap')
                    if 'gura_gura' in catalogue:native.add('overheadsuper-leap')
                    checks=required | (native if who=='actor' else set())
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(checks) or {cid for cid,state,_ in rows if state=='PASS'}!=checks or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B02 missing/failed native render/movement checks on '+who)
                    poses=re.findall(r'\[batch02-pal\] (\S+) (PASS|FAIL)',body)
                    frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if len(poses)!=len(expected_pal) or {cid for cid,state in poses if state=='PASS'}!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B02 missing/failed evaluated poses on '+who)
                frozen['rendered']=True
                print(f'BATCH02_CAPTURED {count*5} actor+{count*5} observer; {len(expected_pal)} evaluated phases/client. Image inspection required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='batch01':
                catalogue={'hie_hie':['icepartisan','pheasantbeak','icespikes','iceball','iceage'],'pika_pika':['laser1','laser2','kick','yatanokagami','magatama','amanomurakumo','speedispower','passive'],'moku_moku':['whiteblow','whitevine','whitelauncher','whiteout'],'suna_suna':['desertspada','barjan','sables','desertlaspada','grounddeath'],'yami_yami':['kurouzu','blackhole','darkball','darkmatter']}
                if frozen.get('retake'): catalogue={f:ids for f,ids in catalogue.items() if f in frozen['retake'].split(',')}
                count=sum(map(len,catalogue.values()))
                required={f'{fruit}/{move}-render' for fruit,ids in catalogue.items() for move in ids}
                expected_pal={f'b01_{move}{suffix}' for ids in catalogue.values() for move in ids for suffix in ['', '_release'] if suffix!='_release' or move not in ['passive','yatanokagami']}
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'))
                    frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=count*4:raise RuntimeError(f'B01 {who}: expected{count*4} captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
                    rows=re.findall(r'\[batch01-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    checks=required | (({'native-sword'} if 'pika_pika' in catalogue else set()) | {m+'-movement' for m in ['kick','yatanokagami','speedispower','passive','whitelauncher','barjan'] if any(m in ids for ids in catalogue.values())} if who=='actor' else set())
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(checks) or {cid for cid,state,_ in rows if state=='PASS'}!=checks or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B01 failed/missing live checks on '+who)
                    poses=re.findall(r'\[batch01-pal\] (\S+) (PASS|FAIL)',body)
                    frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if len(poses)!=len(expected_pal) or {cid for cid,state in poses if state=='PASS'}!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B01 failed/missing evaluated PAL poses on '+who)
                native=re.findall(r'\[batch01-native\] (\S+) (PASS|FAIL) (.*)',(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace'))
                frozen['native_player_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in native]
                if len(native)!=5 or {cid for cid,state,_ in native if state=='PASS'}!={'absorption','capacity','explosion','no-energy','pull-hit'}:raise RuntimeError('Native player damage/absorption checks failed')
                frozen['rendered']=True
                print(f'BATCH01_CAPTURED {count*4} actor+{count*4} observer; {len(expected_pal)} evaluated gestures per client. Image review required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='amaru':
                expected={'uuid-form','dimensions','mesh-form','revert-base','suppression-base','ceiling-base','restore-base','sango-mesh-animated','elthor-mesh-animated','raigo-mesh-animated','mesh-restored'}
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'))
                    frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=12:raise RuntimeError(f'Amaru {who}: expected12captures, got{len(shots)}')
                    required=expected | ({'native-flight','camera-distance'} if who=='actor' else {'retrack-absent','retrack-restored'})
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
                    rows=re.findall(r'\[amaru-client-check\] (\S+) (PASS|FAIL) (.*)',body)
                    frozen[who+'_checks']=[{'id':cid,'passed':ok=='PASS','detail':detail} for cid,ok,detail in rows]
                    if len(rows)!=len(required) or {cid for cid,ok,_ in rows if ok=='PASS'}!=required or any(ok=='FAIL' for _,ok,_ in rows):
                        raise RuntimeError('Amaru missing/failed live checks on '+who)
                cleanup=rcon.cmd('damarucount')
                frozen['disconnect_cleanup']=cleanup
                if 'AMARU_ACTIVE=0' not in cleanup:raise RuntimeError('Amaru survived actual client disconnect')
                frozen['rendered']=True
                print('AMARU_CAPTURED 12 actor+12 observer; visual review remains required.',flush=True)
            elif frozen.get('capture'):
                shots = sorted((client / 'screenshots').glob('actor-*.png' if frozen.get('scenario') in ('mera','magu','goro','amaru','batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08') else '0*-*.png'))
                frozen['captured_images'] = [str(path) for path in shots]
                expected = 49 if frozen.get('scenario')=='goro' else 55 if frozen.get('scenario')=='magu' else 48 if frozen.get('scenario')=='mera' else 9
                if len(shots) != expected: raise RuntimeError(f'Missing graphical evidence: expected {expected}, got {len(shots)}')
                if frozen.get('observer'):
                    witness_shots=sorted((Path(frozen['observer']) / 'screenshots').glob('observer-*.png'))
                    frozen['observer_images']=[str(path) for path in witness_shots]
                    witness_expected=49 if frozen.get('scenario')=='goro' else 40 if frozen.get('scenario')=='magu' else 24
                    if len(witness_shots)!=witness_expected: raise RuntimeError(f'Missing observer screenshots: {len(witness_shots)}/{witness_expected}')
                    observer_log=(Path(frozen['observer'])/'process.log').read_text(encoding='utf-8',errors='replace')
                    prefix=frozen['scenario']
                    live_checks=re.findall(r'\['+prefix+r'-client-check\] (\S+) (PASS|FAIL)',observer_log)
                    frozen['observer_live_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in live_checks]
                    if {cid for cid,state in live_checks if state=='PASS'} != ({'raigo-projectile'} if prefix=='goro' else {'eruption-after-15-ticks'} if prefix=='magu' else {'hibashira-after-15-ticks','kaenryuo-after-15-ticks'}) or any(state=='FAIL' for _,state in live_checks):
                        raise RuntimeError('Mera observer lifetime checks failed or missing')
                    actor_log=(client/'process.log').read_text(encoding='utf-8',errors='replace')
                    if prefix in ('magu','goro'):
                        expected_pal={'magu_fist_charge','magu_fist_release','magu_hound_charge','magu_hound_release','magu_kagero_dash','magu_kagero_finish','magu_volcanic_cast','magu_meteors_cast'}
                        if prefix=='goro': expected_pal={f'goro_{move}_{phase}' for move in ['vari','elthor','sango','karikrash'] for phase in ['charge','release']} | {'goro_storm_cast','goro_blink','goro_raigo_cast'}
                        for who,body in [('actor',actor_log),('observer',observer_log)]:
                            poses=re.findall(r'\['+prefix+r'-pal\] (\S+) (PASS|FAIL)',body)
                            frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                            if len(poses)!=len(expected_pal) or {cid for cid,state in poses if state=='PASS'}!=expected_pal or any(state=='FAIL' for _,state in poses):
                                raise RuntimeError('Magu actual PAL pose/time checks failed or missing on '+who)

                    actor_checks=re.findall(r'\['+prefix+r'-client-check\] (\S+) (PASS|FAIL)',actor_log)
                    frozen['actor_live_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in actor_checks]
                    if actor_checks != [('blink-real-distance' if prefix=='goro' else 'dash-real-distance' if prefix=='magu' else 'propulsion-real-distance','PASS')]: raise RuntimeError('Actual client propulsion did not move or evidence is missing')
                if frozen.get('scenario')=='goro':
                    storm_text=(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace')
                    storm=re.findall(r'\[goro-storm\] strike delay=(\d+) strikes=(\d+) hits=(\d+)',storm_text)
                    frozen['native_storm_checks']=[{'delay_ticks':int(delay),'strike':int(number),'targets_hurt':int(hits),'passed':int(delay)==10 and int(hits)>0} for delay,number,hits in storm]
                    if len(storm)!=3 or [int(number) for _,number,_ in storm]!=[1,2,3] or any(int(delay)!=10 or int(hits)<1 for delay,_,hits in storm):
                        raise RuntimeError('Native true-player storm timing missing or wrong')
                frozen['rendered']=True
                print('CAPTURED', len(shots), 'images; visual review is still required.', flush=True)
    finally:
        if observer_game is not None and observer_game.poll() is None:
            observer_game.terminate();observer_game.wait(timeout=15)
        if observer_output is not None: observer_output.close()
        if game is not None and game.poll() is None:
            game.terminate(); game.wait(timeout=15)
        if server is not None and server.poll() is None:
            if rcon:
                try: rcon.cmd('stop')
                except Exception: pass
            try: server.wait(timeout=60)
            except subprocess.TimeoutExpired: server.terminate(); server.wait(timeout=15)
        if rcon: rcon.sock.close()
        frozen['stopped'] = True
        (lab / 'visual-lab.local.json').write_text(json.dumps(frozen, indent=2), encoding='utf-8')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--forms-only',action='store_true',help='Scoped retake of complete form cycles, retaining two actual skill casts')
    parser.add_argument('--retake',default='',help='Comma separated B01 fruit IDs; only after a completed grouped run')
    parser.add_argument('--fruit',choices=['mera_mera','magu_magu','goro_goro','hie_hie','pika_pika','moku_moku','suna_suna','yami_yami','gura_gura','bomu_bomu','zushi_zushi','nikyu_nikyu','ope_ope','mochi_mochi','mori_mori','kira_kira','supa_supa','riki_riki','aro_aro','horo_horo','mero_mero','kiro_kiro','bisu_bisu','kage_kage','soru_soru','uta_uta','imu','gomu_gomu','inu_inu_wolf','neko_neko_leopard','neko_neko_sabertiger','ushi_ushi_giraffe','batto_batto','inu_inu_okuchi_no_makami','hito_hito_daibutsu','tori_tori_phoenix','uo_uo_seiryu','ryu_ryu_pteranodon','ryu_ryu_brachiosaurus','ryu_ryu_pachycephalosaurus'],default='hie_hie',help='Initial fruit in a manual B01 profile')
    parser.add_argument('--run', action='store_true')
    parser.add_argument('--resume',action='store_true',help='Keep saved manual terrain when reopening the same selected fruit; switching fruit prepares a new scene')
    parser.add_argument('--fresh-scene',action='store_true',help='Explicitly rebuild the manual test arena even for the same fruit')
    parser.add_argument('--lab', type=Path)
    parser.add_argument('--accept-eula', action='store_true')
    parser.add_argument('--capture', action='store_true', help='Capture a short scripted scene then close only this test client')
    parser.add_argument('--scenario',choices=['foundation','mera','magu','goro','amaru','batch01','batch02','batch03','batch04','batch05','batch06','batch07','batch08'],default='foundation')
    args = parser.parse_args()
    lab = args.lab.resolve() if args.lab else prepare(args.accept_eula, args.capture,args.scenario,args.retake,args.forms_only)
    if args.run: run(lab,args.fruit,args.resume,args.fresh_scene)
