"""Private, isolated Windows/offline server recipe; never stops an existing server.

--prepare creates a lab and launch arguments only. --run starts only that lab's child.
EULA acceptance must already exist in the configured Minecraft directory or be explicit.
Historical 312 results remain historical: this runner writes a distinct per-run report.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import secrets
import shutil
import socket
import struct
import subprocess
import time
import uuid
import zipfile
from installed_runtime import resolve

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parent


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def reserve():
    sock = socket.socket()
    sock.bind(('127.0.0.1', 0))
    return sock, sock.getsockname()[1]


class Rcon:
    def __init__(self, port, password):
        self.sock = socket.create_connection(('127.0.0.1', port), timeout=10)
        self.sock.settimeout(45)
        self.rid = 0
        self.send(3, password)
        while True:
            rid, kind, _ = self.recv()
            if kind == 2:
                if rid != self.rid:
                    raise RuntimeError('RCON authentication refused')
                break

    def read(self, count):
        out = b''
        while len(out) < count:
            part = self.sock.recv(count - len(out))
            if not part:
                raise EOFError('RCON disconnected')
            out += part
        return out

    def send(self, kind, body):
        self.rid += 1
        data = struct.pack('<ii', self.rid, kind) + body.encode() + b'\0\0'
        self.sock.sendall(struct.pack('<i', len(data)) + data)

    def recv(self):
        length, = struct.unpack('<i', self.read(4))
        if not 10 <= length <= 4 * 1024 * 1024:
            raise RuntimeError('Invalid RCON frame length')
        data = self.read(length)
        rid, kind = struct.unpack('<ii', data[:8])
        return rid, kind, data[8:-2].decode('utf-8', 'replace')

    def cmd(self, body):
        self.send(2, body)
        while True:
            rid, _, text = self.recv()
            if rid == self.rid:
                return text


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--run', action='store_true', help='Launch the newly created private lab')
    parser.add_argument('--prepare', action='store_true', help='Only prepare files (default)')
    parser.add_argument('--accept-eula', action='store_true', help='Explicitly accept Minecraft EULA for this lab')
    parser.add_argument('--test-tick-rate',type=int,choices=[20,40,80,160],default=20,help='Native tick acceleration for mechanics only; historical checks reset to20')
    parser.add_argument('--timeout', type=int, default=240)
    parser.add_argument('--historical-time',choices=['midnight','noon'],default='midnight',help='Night isolates deferred zombie witnesses; noon is an expected-failure sunlight diagnostic')
    parser.add_argument('--historical', action='store_true', help='Also rerun legacy scenarios, expiry and restart in this disposable lab')
    parser.add_argument('--magu', action='store_true', help='Run five-technique Magu suite in a disposable world')
    parser.add_argument('--mera', action='store_true', help='Run the seven-technique Mera acceptance suite in this disposable world')
    parser.add_argument('--terrain',action='store_true',help='Previous45 techniques terrain acceptance')
    parser.add_argument('--permanent',action='store_true',help='Physical expiry, restart and legacy-journal permanent terrain checks')
    parser.add_argument('--batch02',action='store_true',help='Run B02 five-fruit acceptance')
    parser.add_argument('--batch03-only',choices=['mochi','mori','kira','supa','riki'],default='')
    parser.add_argument('--batch05',action='store_true',help='Run B05 Kage/Soru/Uta/Imu/Gomu native acceptance')
    parser.add_argument('--batch08',action='store_true')
    parser.add_argument('--batch08-only',default='')
    parser.add_argument('--batch07',action='store_true')
    parser.add_argument('--batch07-only',default='')
    parser.add_argument('--batch06-only',default='',help='Comma-separated B06 fruits; core/form/lifecycle always rerun')
    parser.add_argument('--batch06',action='store_true',help='Run B06 five Zoans native acceptance and beast forms')
    parser.add_argument('--batch05-only',default='')
    parser.add_argument('--batch04-only',default='',help='Comma-separated B04 retake fruits; core/lifecycle always rerun')
    parser.add_argument('--batch04',action='store_true',help='Run B04 Aro/Horo/Mero/Kiro/Bisu native acceptance')
    parser.add_argument('--batch03',action='store_true',help='Run B03 Mochi/Mori/Kira/Supa/Riki native acceptance')
    parser.add_argument('--batch01',action='store_true',help='Run the five-fruit B01 acceptance suite')
    parser.add_argument('--goro', action='store_true', help='Run seven Goro techniques in the disposable world')
    args = parser.parse_args()
    if args.batch05_only:
        selected=args.batch05_only.split(',')
        if not args.batch05 or len(selected)!=len(set(selected)) or not set(selected)<={'kage','soru','uta','imu','gomu'}:
            parser.error('--batch05-only requires --batch05 and distinct short IDs: kage,soru,uta,imu,gomu')
    build = json.loads((PROJECT / 'build/build-local.json').read_text(encoding='utf-8'))
    config = json.loads((ROOT / '.runtime/fabric-build/build.local.json').read_text(encoding='utf-8'))
    jar = Path(build['jar'])
    if digest(jar) != build['sha256']:
        raise SystemExit('Build manifest/hash mismatch; build again before testing')
    if any(p.stat().st_mtime_ns > jar.stat().st_mtime_ns for p in (PROJECT / 'src/main/java').rglob('*.java')):
        raise SystemExit('Source changed after build; refusing stale JAR')
    existing_eula = Path(config['minecraft']) / 'eula.txt'
    accepted = existing_eula.is_file() and re.search(r'^\s*eula\s*=\s*true\s*$', existing_eula.read_text(), re.M)
    if args.run and not accepted and not args.accept_eula:
        raise SystemExit('No existing EULA acceptance. Use --accept-eula only with explicit authorization.')
    lab = ROOT / '.runtime/onepiece-lab' / ('run-' + uuid.uuid4().hex)
    lab.mkdir(parents=True)
    mods = lab / 'mods'
    mods.mkdir()
    reservations = [reserve(), reserve()]
    port, rcon_port = [entry[1] for entry in reservations]
    password = secrets.token_urlsafe(32)
    game = None
    runtime = []
    staged = {}
    # Compilation uses extracted API modules; runtime dependencies require the
    # real umbrella mod's identity as well. Let Loader resolve its nested modules.
    api_candidates = []
    for candidate in (Path(config['minecraft']) / 'mods').glob('*.jar'):
        with zipfile.ZipFile(candidate) as archive:
            if 'fabric.mod.json' in archive.namelist():
                meta = json.loads(archive.read('fabric.mod.json'))
                if meta['id'] == 'fabric-api':
                    api_candidates.append(candidate)
    if len(api_candidates) != 1:
        raise RuntimeError('Expected exactly one installed Fabric API umbrella JAR')
    api = api_candidates[0]
    shutil.copy2(api, mods / api.name)
    staged['fabric-api'] = {'source': str(api), 'sha256': digest(api)}
    for value in build['classpath']:
        path = Path(value)
        # Synthetic widened class headers must NEVER be used at runtime.
        if 'compile-only' in path.parts or 'installed-tweakers' in path.name:
            continue
        with zipfile.ZipFile(path) as archive:
            names = set(archive.namelist())
            metadata = json.loads(archive.read('fabric.mod.json')) if 'fabric.mod.json' in names else None
            if 'net/minecraft/server/Main.class' in names:
                game = path
            if metadata and metadata['id'] != 'fabricloader':
                if metadata.get('environment') == 'client':
                    continue
                mid = metadata['id']
                if mid.startswith('fabric-'):
                    continue
                sha = digest(path)
                if mid in staged and staged[mid]['sha256'] != sha:
                    raise RuntimeError('Conflicting dependency JARs for ' + mid)
                if mid not in staged:
                    shutil.copy2(path, mods / path.name)
                    staged[mid] = {'source': str(path), 'sha256': sha}
                continue
        if path not in runtime:
            runtime.append(path)
    if game is None:
        raise RuntimeError('No real installed Minecraft game JAR found')
    exact = resolve(config['minecraft'])
    game = exact['game']
    runtime = [game, *exact['libraries']]
    shutil.copy2(jar, mods / jar.name)
    staged['opworldlike'] = {'source': str(jar), 'sha256': digest(jar)}
    (lab / 'eula.txt').write_text('eula='+('true' if accepted or args.accept_eula else 'false')+'\n', encoding='utf-8')
    properties = {
        'server-ip': '127.0.0.1', 'server-port': port,
        'enable-rcon': 'true', 'rcon.port': rcon_port, 'rcon.password': password,
        'online-mode': 'false', 'enable-query': 'false', 'level-name': 'repair-world',
        'white-list': 'false', 'enforce-secure-profile': 'false',
        'level-seed': '44127', 'level-type': 'minecraft:flat', 'generate-structures': 'false',
        'generator-settings': json.dumps({'layers': [{'block': 'minecraft:bedrock', 'height': 1}], 'biome': 'minecraft:plains'}),
        'spawn-protection': 0, 'view-distance': 3, 'simulation-distance': 3,
        'pause-when-empty-seconds': 0, 'max-players': 2, 'sync-chunk-writes': 'true',
    }
    if args.historical:
        cfg = lab / 'config/opworldlike'
        cfg.mkdir(parents=True)
        (cfg / 'devilfruit-loot.toml').write_text(
            'enabled = true\nchest-drop-chance = 1.0\nunique = true\nreincarnation-on-death = true\n'
            'weight-legendary = 6\nweight-rare = 25\nweight-common = 60\n', encoding='utf-8')
    (lab / 'server.properties').write_text(''.join(f'{k}={v}\n' for k, v in properties.items()), encoding='utf-8')
    launch = ['-Xmx2G', '-Dopworldlike.repairTests=true', '-Dfabric.gameJarPath=' + str(game),
              '-Dlog4j2.formatMsgNoLookups=true', '-cp', os.pathsep.join(map(str, runtime)),
              'net.fabricmc.loader.impl.launch.knot.KnotServer', 'nogui']
    (lab / 'launch.args').write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in launch), encoding='utf-8')
    manifest = {'status': 'prepared', 'lab': str(lab), 'game': str(game), 'mods': staged,
                'runtime_libraries': list(map(str, exact['libraries'])),
                'port': port, 'rcon_port': rcon_port, 'checks': [],
                'unverified': ['rendering', 'observer client', 'actual player mortality', 'all 213 move semantics']}
    report = lab / 'report.local.json'
    report.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    print('LAB', lab, flush=True)
    if not args.run:
        for sock, _ in reservations: sock.close()
        print('PREPARED ONLY; no server launched', flush=True)
        return 0
    proc = rcon = None
    try:
        # Only these ephemeral ports are released; no existing server is contacted or stopped.
        for sock, _ in reservations: sock.close()
        with (lab / 'process.log').open('w', encoding='utf-8') as output:
            proc = subprocess.Popen([str(Path(build['jdk']) / 'bin/java.exe'), '@' + str(lab / 'launch.args')],
                                    cwd=lab, stdout=output, stderr=subprocess.STDOUT)
            deadline = time.monotonic() + args.timeout
            log = lab / 'logs/latest.log'
            while time.monotonic() < deadline:
                if proc.poll() is not None:
                    raise RuntimeError('Created server exited before ready; see process.log')
                text = log.read_text(encoding='utf-8', errors='replace') if log.exists() else ''
                if 'Done (' in text:
                    break
                time.sleep(.5)
            else:
                raise TimeoutError('Created server did not become ready')
            rcon = Rcon(rcon_port, password)
            arena = 'forceload add -32 -32 255 191' if args.historical else 'forceload add 0 0 24 24'
            for command in [arena, 'time set midnight']:
                print(command, rcon.cmd(command), flush=True)
            # forceload schedules chunk activation; the command itself doesn't
            # guarantee a ticking entity section on the same server tick.
            time.sleep(2)
            rcon.cmd('tick rate '+str(args.test_tick_rate))
            manifest['native_test_tick_rate']=args.test_tick_rate
            print('drepair', rcon.cmd('drepair'), flush=True)
            deadline = time.monotonic() + 30
            while time.monotonic() < deadline:
                text = log.read_text(encoding='utf-8', errors='replace')
                end = re.search(r'\[drepair\] END passed=(\d+) failed=(\d+) total=(\d+)', text)
                if end: break
                if proc.poll() is not None: raise RuntimeError('Server exited during repair tests')
                time.sleep(.25)
            else:
                raise TimeoutError('Missing repair test completion marker')
            checks = re.findall(r'\[drepair\] \[(PASS|FAIL)\] (\S+) \| (.*)', text)
            manifest['checks'] = [{'id': cid, 'passed': state == 'PASS', 'detail': detail} for state, cid, detail in checks]
            # Combat 22, form lifecycle 10, custom refusal/guard/attribution 6.
            expected_count = 59
            ok = len(checks) == expected_count and len({c[1] for c in checks}) == expected_count and all(c[0] == 'PASS' for c in checks)
            ok = ok and tuple(map(int, end.groups())) == (expected_count, 0, expected_count)
            manifest['status'] = 'passed' if ok else 'failed'
            if args.mera:
                rcon.cmd('forceload add 64 64 112 112')
                time.sleep(2)
                print('dmera', rcon.cmd('dmera'), flush=True)
                deadline = time.monotonic() + 100
                while time.monotonic() < deadline:
                    mera_text = log.read_text(encoding='utf-8',errors='replace')
                    mera_end = re.search(r'\[dmera\] END passed=(\d+) failed=(\d+) total=(\d+)',mera_text)
                    if mera_end: break
                    if proc.poll() is not None: raise RuntimeError('Owned server exited during Mera suite')
                    time.sleep(.5)
                else: raise TimeoutError('Mera suite did not finish')
                rows = re.findall(r'\[dmera\] \[(PASS|FAIL)\] (\S+) \| (.*)',mera_text)
                moves = ['hiken','higan','hibashira','kagero','hotarubi','kaenryuo','daienkai']
                required = {move+'-'+suffix for move in moves for suffix in ['cast-cost','cooldown-spam','real-damage','attributed','outside-intact','refusal-no-burn','ally-safe','solid-wall']}
                required.update({'hiken-telegraph-no-premature-hit','hotarubi-telegraph-no-premature-hit','daienkai-telegraph-no-premature-hit',
                    'reflection-retains-projectile','cross-dimension-projectile-cancelled','fire-blast-knockback','visual-anchor-no-gravity','flight-ownership-persisted','disconnect-revokes-owned-flight','reconnect-low-energy-revokes-flight','ultimate-first-hit-once','charged-higan-four-projectiles','charged-higan-budget','charged-higan-cost','hiken-charge-timeout','suppression-stops-maintained','body-fire-counter','body-magma-counter','body-suppressed','burn-natural-expiry','volume-circle-not-square','volume-height-bound','fire-resistance-potion','charge-started','reset-cancels-charge','no-energy-no-effect','field-natural-expiry',
                    'burn-cap-continuous-refresh','water-extinguishes','survival-propulsion-accessible','propulsion-exact-cost','propulsion-body-wall','propulsion-exhaustion','propulsion-stops-on-spectator','propulsion-clear-stops-motion','fruit-change-revokes-propulsion'})
                missing = sorted(required-{cid for _,cid,_ in rows})
                mera_ok = not missing and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['mera_checks'] = [{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['mera_missing'] = missing
                ok = ok and mera_ok
                manifest['status'] = 'passed' if ok else 'failed'
            if args.magu:
                rcon.cmd('forceload add 64 64 112 112');rcon.cmd('execute in minecraft:the_nether run forceload add 64 64 112 112')
                time.sleep(2)
                print('dmagu',rcon.cmd('dmagu'),flush=True)
                deadline=time.monotonic()+140
                while time.monotonic()<deadline:
                    magu_text=log.read_text(encoding='utf-8',errors='replace')
                    magu_end=re.search(r'\[dmagu\] END passed=(\d+) failed=(\d+) total=(\d+)',magu_text)
                    if magu_end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during Magu suite')
                    time.sleep(.5)
                else:raise TimeoutError('Magu suite did not finish')
                rows=re.findall(r'\[dmagu\] \[(PASS|FAIL)\] (\S+) \| (.*)',magu_text)
                moves=['daifunka','inugamiguren','kageromagumagu','volcanicexplosion','ryuseikazan']
                required={move+'-'+suffix for move in moves for suffix in ['cast-cost','cooldown-spam','real-damage','attributed','outside-intact','refusal-no-burn','ally-safe','solid-wall']}
                required.update({move+'-'+suffix for move in ['daifunka','inugamiguren'] for suffix in ['charge-no-premature-hit','impact-budget']})
                required.update({move+'-animation-start' for move in moves})
                required.update({'daifunka-animation-release','inugamiguren-animation-release','kagero-animation-transition'})
                required.update({'pool-skips-other-owned','pool-attributed-hit','pool-native-floor-disabled','zero-pool-limit','suppression-clears-charge','fruit-change-keeps-pool','no-energy-no-effect','reset-clears-charge','fruit-change-clears-charge','body-fire-lava-resistance','body-suppressed','ice-really-extinguishes','fire-resistance-no-burn','burn-no-vanilla-tail','magma-water-distinct','burn-refresh-budget','pool-preserves-inventory','pool-population-cap','pool-clear-keeps-terrain','pool-dimension-owned','pool-expiry-armed','pool-natural-expiry'})
                missing=sorted(required-{cid for _,cid,_ in rows})
                manifest['magu_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['magu_missing']=missing
                ok=ok and tuple(map(int,magu_end.groups()))==(len(required),0,len(required)) and not missing and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.goro:
                rcon.cmd('forceload add 128 32 191 159')
                time.sleep(2)
                print('dgoro',rcon.cmd('dgoro'),flush=True)
                deadline=time.monotonic()+150
                while time.monotonic()<deadline:
                    goro_text=log.read_text(encoding='utf-8',errors='replace')
                    goro_end=re.search(r'\[dgoro\] END passed=(\d+) failed=(\d+) total=(\d+)',goro_text)
                    if goro_end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during Goro suite')
                    time.sleep(.5)
                else:raise TimeoutError('Goro suite did not finish')
                rows=re.findall(r'\[dgoro\] \[(PASS|FAIL)\] (\S+) \| (.*)',goro_text)
                moves=['vari','elthor','sango','mamaragan','karikrash','passive','raigo']
                required={move+'-'+suffix for move in moves for suffix in ['cast-cost','cooldown','gesture','ally','refusal','outside','expired','wall']}
                required.update({move+'-'+suffix for move in moves if move!='passive' for suffix in ['damage','attribution']})
                required.update({move+'-'+suffix for move in ['vari','elthor','sango','karikrash'] for suffix in ['telegraph','slow']})
                required.update({move+'-body-collision' for move in ['vari','passive']})
                required.update({'chain-budget-'+str(i) for i in range(4)})
                required.update({'storm-three-impacts','passive-distance','passive-no-damage','raigo-single-budget','no-energy','reset-charge','storm-pending','suppression-clears','reset-storm','fruit-change','dimension-clears','body-lightning','body-suppressed','chain-refused-exposed','chain-refused-no-bridge','blink-backward','blink-ceiling','gomu-electric-counter','gomu-counter-suppressed','storm-dodge','raigo-deflection-owner','raigo-deflected-impact'})
                required.update({'amaru-energy-end', 'amaru-charge-start', 'amaru-blink-volume-fluid', 'amaru-eye', 'amaru-toggle-off', 'amaru-dimension-clears', 'amaru-revert-idempotent', 'amaru-ceiling-collision', 'amaru-no-combat-bonus', 'amaru-no-leak', 'amaru-stale-save-clears', 'amaru-bbox', 'amaru-charge-not-early', 'amaru-maintenance', 'amaru-flight-revoked', 'amaru-low-ceiling-refused', 'amaru-flight-granted', 'amaru-fruit-change', 'amaru-side-collision', 'amaru-flight-cost', 'amaru-narrow-refused', 'amaru-external-flight', 'amaru-equipment', 'amaru-energy-refused', 'amaru-login-clears', 'amaru-death-clears', 'amaru-suppressed-refused', 'amaru-suppression', 'amaru-duration-end', 'amaru-active-cost', 'amaru-crouch', 'amaru-cancel-charge', 'amaru-charge-revalidates'})
                required.update({'amaru-flight-owner-persisted','amaru-stale-flight-clears'})
                required.update({'amaru-no-regeneration','amaru-charge-scale-revalidates','amaru-marker-removal'})
                manifest['goro_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['goro_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,goro_end.groups()))==(len(required),0,len(required)) and not manifest['goro_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch01:
                rcon.cmd('forceload add 256 64 351 159')
                time.sleep(2)
                print('dbatch01',rcon.cmd('dbatch01'),flush=True)
                deadline=time.monotonic()+360
                while time.monotonic()<deadline:
                    batch_text=log.read_text(encoding='utf-8',errors='replace')
                    batch_end=re.search(r'\[dbatch01\] END passed=(\d+) failed=(\d+) total=(\d+)',batch_text)
                    if batch_end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B01')
                    time.sleep(.5)
                else:raise TimeoutError('B01 did not finish')
                catalogue={'hie':['icepartisan','pheasantbeak','icespikes','iceball','iceage'],'pika':['laser1','laser2','kick','yatanokagami','magatama','amanomurakumo','speedispower','passive'],'moku':['whiteblow','whitevine','whitelauncher','whiteout'],'suna':['desertspada','barjan','sables','desertlaspada','grounddeath'],'yami':['kurouzu','blackhole','darkball','darkmatter']}
                peaceful={'yatanokagami','amanomurakumo','passive'}
                required={f'{fruit}/{move}/{suffix}' for fruit,ids in catalogue.items() for move in ids for suffix in ['cost','cooldown','animation','ticking','ally','refused','outside','bounded','wall']}
                required|={f'{fruit}/{move}/{suffix}' for fruit,ids in catalogue.items() for move in ids if move not in peaceful for suffix in ['telegraph','damage','attribution']}
                required|={f'pika/{move}/{suffix}' for move in ['yatanokagami','passive'] for suffix in ['movement','peaceful']}
                required|={f'{fruit}/{move}/body-wall' for fruit,ids in catalogue.items() for move in ids if move in ['kick','whitelauncher','barjan','speedispower','passive','yatanokagami']}
                required|={f'{fruit}/{move}/movement' for fruit,move in [('pika','kick'),('pika','speedispower'),('moku','whitelauncher'),('suna','barjan')]}
                required|={'yami/kurouzu/pull-displacement','yami/blackhole/pull-displacement','yami/kurouzu/pull-control','yami/blackhole/pull-control','core/ice-external-marker','core/ice-chest','core/sword-cleanup','core/ice-reset-immediate','core/sand-reset-immediate','core/sword-recast','core/sword-full','core/yami-pull-without-window','core/yami-independent-expiry'}
                required|={'pika/amanomurakumo/sword','pika/amanomurakumo/no-duplicate','pika/magatama/staggered'}
                required|={'core/'+cid for cid in ['ice-external-slow','sand-external-slow','light-external-speed','smoke-capture','smoke-exit','smoke-no-energy','yami-sealed-absorption']}
                required|={'core/fruit-change-'+fruit for fruit in catalogue}
                rows=re.findall(r'\[dbatch01\] \[(PASS|FAIL)\] (\S+) \| (.*)',batch_text)
                manifest['batch01_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch01_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,batch_end.groups()))==(len(required),0,len(required)) and not manifest['batch01_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.terrain:
                rcon.cmd('forceload add 496 64 543 127');time.sleep(2)
                print('dterrain',rcon.cmd('dterrain'),flush=True)
                deadline=time.monotonic()+480
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    terrain_end=re.search(r'\[dterrain\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if terrain_end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during terrain suite')
                    time.sleep(.5)
                else:raise TimeoutError('Terrain suite did not finish')
                catalogue={'mera_mera':['hiken','higan','hibashira','kagero','hotarubi','kaenryuo','daienkai'],'magu_magu':['daifunka','inugamiguren','kageromagumagu','volcanicexplosion','ryuseikazan'],'goro_goro':['vari','elthor','sango','mamaragan','karikrash','passive','raigo'],'hie_hie':['icepartisan','pheasantbeak','icespikes','iceball','iceage'],'pika_pika':['laser1','laser2','kick','yatanokagami','magatama','amanomurakumo','speedispower','passive'],'moku_moku':['whiteblow','whitevine','whitelauncher','whiteout'],'suna_suna':['desertspada','barjan','sables','desertlaspada','grounddeath'],'yami_yami':['kurouzu','blackhole','darkball','darkmatter']}
                destructive={'mera_mera':catalogue['mera_mera'],'magu_magu':catalogue['magu_magu'],'goro_goro':['vari','elthor','sango','karikrash','raigo'],'hie_hie':['icepartisan','pheasantbeak'],'pika_pika':['laser1','laser2','kick','magatama','speedispower'],'moku_moku':['whiteblow','whitevine','whiteout'],'suna_suna':['desertspada','desertlaspada','sables'],'yami_yami':['blackhole','darkball','darkmatter']}
                required={'catalogue','guard/chest','guard/bedrock','guard/resistance','guard/temporary','guard/fruit-reset','guard/none','guard/protection','guard/radius','guard/cover','guard/config-disabled','temporary/created','temporary/player-edit'}
                required|={f'{f}/{m}/{test}' for f,ms in catalogue.items() for m in ms for test in ['policy','native-cast','charge-safe','native-terrain']}
                required|={f'{f}/{m}/{test}' for f,ms in destructive.items() for m in ms for test in ['stone-impact','shared-budget']}
                required|={f'{f}/{m}/{check}' for f,ms in {'hie_hie':['icespikes','iceball','iceage'],'suna_suna':['grounddeath']}.items() for m in ms for check in ['native-transform-active','native-expiry','reset-keeps-terrain']}
                rows=re.findall(r'\[dterrain\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['terrain_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['terrain_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,terrain_end.groups()))==(len(required),0,len(required)) and not manifest['terrain_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch02:
                rcon.cmd('forceload add 352 64 463 159');time.sleep(2)
                print('dbatch02',rcon.cmd('dbatch02'),flush=True)
                deadline=time.monotonic()+420
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch02\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B02')
                    time.sleep(.5)
                else:raise TimeoutError('B02 did not finish')
                catalogue={'gura':['attacksweep','overheadsuper','gekishin','kabutowari','tenchimeido','kaishin'],'bomu':['nosefancycannon','kickbomb','punchbomb','bomboverhead','zenshinkibaku'],'zushi':['attackspeed','gravito','meteorsmall','moko','rubble','meteor'],'nikyu':['padho','ursusshock','assault','kumapunch','passive'],'ope':['stoneopeope','room','shambles','amputation']}
                peaceful={'room','shambles','passive'};dash={'kickbomb','attackspeed','assault','kumapunch','passive'}
                required={f'{fruit}/{move}/{suffix}' for fruit,ids in catalogue.items() for move in ids for suffix in ['cost','cooldown','animation','ticking','ally','refused','outside','bounded','wall']}
                required|={f'{fruit}/{move}/{suffix}' for fruit,ids in catalogue.items() for move in ids if move not in peaceful for suffix in ['telegraph','damage','attribution']}
                required|={f'{fruit}/{move}/peaceful' for fruit,ids in catalogue.items() for move in ids if move in peaceful}
                required|={f'{fruit}/{move}/{suffix}' for fruit,ids in catalogue.items() for move in ids if move in dash for suffix in ['movement','body-wall']}
                required|={'ope/shambles/swap','ope/room/room-open','ope/amputation/section','ope/stoneopeope/single-budget','ope/stoneopeope/staggered'}
                required|={f'{fruit}/{move}/terrain' for fruit,ids in catalogue.items() for move in ids}
                required|={'core/'+cid for cid in ['explosion-refused','chest','section-external-buff','section-external-restore','swap-owner-outside','meteor-roof','ope-room-dispatch','ope-surgery-during-room','ope-combo-swapped','ope-amputation-dispatch','ope-combo-section']}
                required|={'core/reset-'+fruit for fruit in catalogue}
                rows=re.findall(r'\[dbatch02\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch02_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch02_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch02_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch03:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch03',rcon.cmd('dbatch03'+(' '+args.batch03_only if args.batch03_only else '')),flush=True)
                deadline=time.monotonic()+max(600,19000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch03\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B03')
                    time.sleep(.5)
                else:raise TimeoutError('B03 native suite timed out')
                catalogue={'mochi':['mochipistol','mochigatling','yakimochi','zangirimochi','musodonatsu','mochibuild'],'mori':['root','rootgiant','assault','rootsurround'],'kira':['attackoverhead','brilliantpunk','diamondshard','diamondavalanche','diamondbody'],'supa':['projectileslash','atomicspurt','sparklingdaisy','sparbreak'],'riki':['attackoverhead','hadoelbow','galleonlariat','stonethrowing','stonethrowinglarge']}
                full_catalogue=dict(catalogue)
                if args.batch03_only:catalogue={args.batch03_only:catalogue[args.batch03_only]}
                peaceful={'mochibuild','diamondbody'};dash={'zangirimochi','brilliantpunk','atomicspurt','galleonlariat'}
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['cost','cooldown','animation','ticking','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms if m not in peaceful for suffix in ['telegraph','damage','attribution']}
                required|={f'{f}/{m}/peaceful' for f,ms in catalogue.items() for m in ms if m in peaceful}
                required|={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms if m in dash for suffix in ['movement','body-wall']}
                if 'mochi' in catalogue:required|={'mochi/mochigatling/cadence-early','mochi/mochigatling/six-hit-budget','mochi/musodonatsu/no-autonomous-damage','mochi/musodonatsu/gesture','mochi/musodonatsu/rings-follow'}
                if 'kira' in catalogue:required.add('kira/diamondbody/body-active')
                required|={'core/'+prefix+'-'+f for f in full_catalogue for prefix in ['no-energy','reset','fruit-change','suppression','chest']}
                required|={'core/'+cid for cid in ['stance-exclusive','stance-expiry','sticky-bounded-refresh','sticky-fruit-release','root-fruit-release','diamond-cap','diamond-fruit-release','refused-no-control']}
                required|={'life/'+cid for cid in ['supa-native-expiry','diamond-native-expiry','diamond-death','supa-fruit-change','shot-launched-4','shot-launched-5','shot-launched-6','shot-cancelled-4','shot-cancelled-5','shot-cancelled-6','rings-live','rings-native-expiry','surround-late-arrival','grass-positive','grass-permanent']}
                rows=re.findall(r'\[dbatch03\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch03_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch03_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch03_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch04:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch04',rcon.cmd('dbatch04'+(' '+args.batch04_only if args.batch04_only else '')),flush=True)
                deadline=time.monotonic()+max(600,22000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch04\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B04')
                    time.sleep(.5)
                else:raise TimeoutError('B04 native suite timed out')
                catalogue={'aro':['arrow','move','combo','passive'],'horo':['negativehollow','minihollow','tokuhoro','rap','kumac'],'mero':['pistolkiss','roundhousekick','rotatekick','perfumefemur'],'kiro':['attackoverhead','attackjump','kirooverhead','weightmanipulation'],'bisu':['crashpretzel','biscuitwarrior','biscuitwarrior2','biscuit']}
                full_b04_catalogue=dict(catalogue)
                if args.batch04_only:catalogue={f:ms for f,ms in catalogue.items() if f in args.batch04_only.split(',')}
                utility={'passive','weightmanipulation','biscuit'};summons={'kumac','biscuitwarrior','biscuitwarrior2'}
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['telegraph','cost','cooldown','animation','ticking','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/'+('peaceful' if m in utility else 'damage') for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/{m}/attribution' for f,ms in catalogue.items() for m in ms if m not in utility|summons}
                if 'aro' in catalogue:required|={'aro/passive/movement','aro/move/movement','aro/combo/first-contact'}
                if 'mero' in catalogue:required.add('mero/perfumefemur/movement')
                if 'bisu' in catalogue:required.add('bisu/biscuit/three-claps')
                if 'horo' in catalogue:required|={f'horo/{m}/{suffix}' for m in ['minihollow','rap'] for suffix in ['waiting','command']}
                required|={'core/'+prefix+'-'+f for f in full_b04_catalogue for prefix in ['no-energy','reset','fruit-change','suppression','chest']}
                required|={'core/'+cid for cid in ['weight-finite','weight-no-stack','weight-fruit-release','control-no-refresh','control-foreign-preserved','empty-rap-refund','weight-native-landing','weight-one-impact','weight-invalid-height']}
                required|={'life/'+cid for cid in ['cast-0','cast-1','cast-2','cast-3','soldier-live','soldier-expiry','mini-launched','arrow-launched','projectile-cleanup-1','projectile-cleanup-2','stone-live','stone-cannot-attack','stone-source-release']}
                rows=re.findall(r'\[dbatch04\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch04_filter']=args.batch04_only
                manifest['batch04_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch04_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch04_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch08:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch08',rcon.cmd('dbatch08'+(' '+args.batch08_only if args.batch08_only else '')),flush=True)
                deadline=time.monotonic()+max(700,21000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch08\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B08')
                    time.sleep(.5)
                else:raise TimeoutError('B08 native suite timed out')
                catalogue=json.loads((PROJECT/'build/b08-moves.local.json').read_text(encoding='utf-8'))
                full_catalogue=dict(catalogue)
                if args.batch08_only:catalogue={f:ms for f,ms in catalogue.items() if f in args.batch08_only.split(',')}
                utility=set()
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['cost','cooldown','animation','ticking','telegraph','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/'+('peaceful' if m in utility else 'damage') for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/{m}/attribution' for f,ms in catalogue.items() for m in ms if m not in utility}
                required|={f'core/{prefix}-{f}' for f in full_catalogue for prefix in ['no-energy','reset','fruit-change']}
                required|={f'form/{prefix}-{f}' for f in full_catalogue for prefix in ['room','hybrid','beast','dimensions','base']}
                required|={f'life/{prefix}-{f}' for f in full_catalogue for prefix in ['begin','expiry']}
                required|={f'core/chest-{f}' for f in full_catalogue}
                required|={'life/'+x for x in ['projectile-cast','projectile-live','projectile-cancelled']}
                required|={f'{f}/{m}/movement' for f,ms in catalogue.items() for m in ms if m in {'headbutt','ulzugan','brachiobober','attackjump','ulmeteor'}}
                required|={'core/'+x for x in ['counter-applied','counter-victim-reset-retained','counter-owner-release']}
                rows=re.findall(r'\[dbatch08\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch08_filter']=args.batch08_only
                manifest['batch08_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch08_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch08_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch07:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch07',rcon.cmd('dbatch07'+(' '+args.batch07_only if args.batch07_only else '')),flush=True)
                deadline=time.monotonic()+max(700,21000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch07\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B07')
                    time.sleep(.5)
                else:raise TimeoutError('B07 native suite timed out')
                catalogue=json.loads((PROJECT/'build/b07-moves.local.json').read_text(encoding='utf-8'))
                full_catalogue=dict(catalogue)
                if args.batch07_only:catalogue={f:ms for f,ms in catalogue.items() if f in args.batch07_only.split(',')}
                utility={'hyogaheki'}
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['cost','cooldown','animation','ticking','telegraph','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/'+('peaceful' if m in utility else 'damage') for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/{m}/attribution' for f,ms in catalogue.items() for m in ms if m not in utility}
                required|={f'core/{prefix}-{f}' for f in full_catalogue for prefix in ['no-energy','reset','fruit-change']}
                required|={f'form/{prefix}-{f}' for f in full_catalogue for prefix in ['room','hybrid','beast','dimensions','base']}
                required|={f'life/{prefix}-{f}' for f in full_catalogue for prefix in ['begin','expiry']}
                required|={'form/'+x for x in ['foreign-flight','foreign-flight-retained','owned-flight','owned-flight-release']}
                required|={f'core/chest-{f}' for f in full_catalogue}|{'core/'+x for x in ['ice-defense','ice-authority','ice-fire','ice-wrong-fruit']}
                required|={'life/'+x for x in ['projectile-cast','projectile-live','projectile-cancelled','defense-begin','defense-expiry']}
                rows=re.findall(r'\[dbatch07\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch07_filter']=args.batch07_only
                manifest['batch07_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch07_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch07_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch06:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch06',rcon.cmd('dbatch06'+(' '+args.batch06_only if args.batch06_only else '')),flush=True)
                deadline=time.monotonic()+max(700,21000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch06\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B06')
                    time.sleep(.5)
                else:raise TimeoutError('B06 native suite timed out')
                catalogue=json.loads((PROJECT/'build/b06-moves.local.json').read_text(encoding='utf-8'))
                full_catalogue=dict(catalogue)
                if args.batch06_only:catalogue={f:ms for f,ms in catalogue.items() if f in args.batch06_only.split(',')}
                utility={'soru','poursuite','kamiezanshin'}
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['cost','cooldown','animation','ticking','telegraph','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/'+('peaceful' if m in utility else 'damage') for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/{m}/attribution' for f,ms in catalogue.items() for m in ms if m not in utility}
                required|={f'{f}/{m}/movement' for f,ms in catalogue.items() for m in ms if m in {'soru','poursuite'}}
                if 'saber' in catalogue:required|={f'saber/shiganmadara/cadence-{n}' for n in [11,23,35]}
                required|={f'core/{prefix}-{f}' for f in full_catalogue for prefix in ['no-energy','reset','fruit-change']}
                required|={f'form/{prefix}-{f}' for f in full_catalogue for prefix in ['room','hybrid','beast','dimensions','base']}
                required|={f'life/{prefix}-{f}' for f in full_catalogue for prefix in ['begin','expiry']}
                required|={'form/'+x for x in ['foreign-flight','foreign-flight-retained','owned-flight','owned-flight-release']}
                required|={f'core/chest-{f}' for f in full_catalogue}|{'core/'+x for x in ['pursuit-start','pursuit-foreign-speed','pursuit-fruit-change']}
                required|={'life/'+x for x in ['guard-cast','projectile-cast','projectile-live','projectile-cancelled','guard-service-counter','guard-expiry']}
                rows=re.findall(r'\[dbatch06\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch06_filter']=args.batch06_only
                manifest['batch06_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch06_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch06_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.batch05:
                rcon.cmd('forceload add 656 64 767 159');time.sleep(2)
                print('dbatch05',rcon.cmd('dbatch05'+(' '+args.batch05_only if args.batch05_only else '')),flush=True)
                deadline=time.monotonic()+max(700,23000/args.test_tick_rate+180)
                while time.monotonic()<deadline:
                    body=log.read_text(encoding='utf-8',errors='replace')
                    end=re.search(r'\[dbatch05\] END passed=(\d+) failed=(\d+) total=(\d+)',body)
                    if end:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during B05')
                    time.sleep(.5)
                else:raise TimeoutError('B05 native suite timed out')
                catalogue={'kage':['attackoverhead','brickbat','tsunotokage','doppelman','islandshatterpunch','shadowasgard'],'soru':['attackoverhead','attacksweep','raitei','hebunrifire','furugora','mamaraid','ikoku'],'uta':['noteattack','notelaser','posing','noteshield','notewarrior','totmusicapunch','summontotmusica'],'imu':['imuarrow','silentcommand','sovereigngrasp'],'gomu':['gomu_pistol','gomu_bazooka','gomu_gatling','gear_change']}
                full=dict(catalogue)
                if args.batch05_only:catalogue={f:ms for f,ms in catalogue.items() if f in args.batch05_only.split(',')}
                utility={'shadowasgard','silentcommand','gear_change'};summons={'doppelman','notewarrior','summontotmusica'}
                required={f'{f}/{m}/{suffix}' for f,ms in catalogue.items() for m in ms for suffix in ['cost','cooldown','animation','ticking','telegraph','ally','refused','outside','bounded','wall','terrain','ally-on-axis','refused-on-axis']}
                required|={f'{f}/{m}/'+('peaceful' if m in utility else 'damage') for f,ms in catalogue.items() for m in ms}
                required|={f'{f}/{m}/attribution' for f,ms in catalogue.items() for m in ms if m not in utility|summons}
                if 'kage' in catalogue:required.add('kage/shadowasgard/shadow')
                if 'imu' in catalogue:required.add('imu/silentcommand/control')
                if 'gomu' in catalogue:required|={'gomu/gear_change/gear'}|{f'gomu/gomu_gatling/cadence-{t}' for t in [18,30,78]}
                required|={f'core/{prefix}-{f}' for f in full for prefix in ['no-energy','reset','fruit-change','suppression','chest']}
                required|={'core/foreign-slow','core/gear-cycle','core/gear-fruit-release','core/gear-room-refusal','core/gear-fourth-flight','core/gear-fourth-dimensions','core/gear-fourth-flight-release','core/veil-foreign','core/veil-disabled','core/veil-finite','core/tot-room-refusal'}|{f'core/gear-{g}' for g in [2,3,4,5]}
                required|={f'life/cast-{n}' for n in range(4)}|{f'life/summon-{suffix}-{n}' for n in [0,1] for suffix in ['live','expiry']}|{'life/shot-live','life/shot-cancelled','life/gear-live','life/gear-death','life/tot-projectiles-expired'}
                rows=re.findall(r'\[dbatch05\] \[(PASS|FAIL)\] (\S+) \| (.*)',body)
                manifest['batch05_filter']=args.batch05_only
                manifest['batch05_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['batch05_missing']=sorted(required-{cid for _,cid,_ in rows})
                ok=ok and tuple(map(int,end.groups()))==(len(required),0,len(required)) and not manifest['batch05_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(rows) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if ok else 'failed'
            if args.historical:
                rcon.cmd('tick rate 20')
                # Earlier fruit suites can run through dawn. Reset time for the deferred
                # zombie witnesses, whose exact20HP control must not burn in sunlight.
                rcon.cmd('time set '+args.historical_time)
                manifest['historical_fixture_time']=args.historical_time
                for scope in ['all', 'persistwrite']:
                    print('dftest', scope, rcon.cmd('dftest ' + scope)[:200], flush=True)
                # Deferred scenarios explicitly require actual server ticks.
                time.sleep(5)
                for scope in ['ope_expiry', 'pika_impact', 'yami_impact', 'nikyu_impact']:
                    print('dftest', scope, rcon.cmd('dftest ' + scope)[:200], flush=True)
                first_log = log.read_text(encoding='utf-8', errors='replace')
                (lab / 'historical-first.log').write_text(first_log, encoding='utf-8')
                rcon.cmd('stop')
                proc.wait(timeout=60)
                rcon.sock.close()
                rcon = None
                # Only restart the process created by this recipe, using its own world.
                proc = subprocess.Popen([str(Path(build['jdk']) / 'bin/java.exe'), '@' + str(lab / 'launch.args')],
                                        cwd=lab, stdout=output, stderr=subprocess.STDOUT)
                deadline = time.monotonic() + args.timeout
                while time.monotonic() < deadline:
                    if proc.poll() is not None:
                        raise RuntimeError('Owned test server exited during restart')
                    second_log = log.read_text(encoding='utf-8', errors='replace')
                    if 'Done (' in second_log and 'Stopping server' not in second_log:
                        try:
                            rcon = Rcon(rcon_port, password)
                            break
                        except OSError:
                            pass
                    time.sleep(.5)
                else:
                    raise TimeoutError('Owned test server restart timeout')
                rcon.cmd('forceload add 0 0 128 128')
                time.sleep(2)
                for scope in ['persistverify', 'mochi_expiry', 'ope_restart', 'lootprep']:
                    print('dftest', scope, rcon.cmd('dftest ' + scope)[:200], flush=True)
                for command in ['setblock 100 64 100 minecraft:stone replace', 'setblock 100 64 100 minecraft:chest',
                                'loot insert 100 64 100 loot minecraft:chests/simple_dungeon']:
                    rcon.cmd(command)
                loot = rcon.cmd('data get block 100 64 100 Items')
                manifest['historical_loot'] = {'passed': 'minepiece:item_devil_fruit' in loot, 'detail': loot}
                combined = first_log + '\n' + log.read_text(encoding='utf-8', errors='replace')
                rows = re.findall(r'\[dftest\] \[(PASS|FAIL)\] (\S+) — (.*)', combined)
                manifest['historical_checks'] = [{'id': cid, 'passed': state == 'PASS', 'detail': detail}
                                                 for state, cid, detail in rows]
                old_report = (PROJECT / 'docs/CORE-030-rapport-tests.md').read_text(encoding='utf-8')
                expected = set(re.findall(r'^\| (?:all|persistwrite|ope_expiry|pika_impact|yami_impact|nikyu_impact|persistverify|mochi_expiry|ope_restart|lootprep) \| `([^`]+)`', old_report, re.M))
                missing = sorted(expected - {cid for _, cid, _ in rows})
                manifest['historical_missing'] = missing
                legacy_ok = bool(expected) and not missing and all(state == 'PASS' for state, _, _ in rows)
                legacy_ok = legacy_ok and manifest['historical_loot']['passed']
                manifest['status'] = 'passed' if ok and legacy_ok else 'failed'
            if args.permanent:
                rcon.cmd('forceload add 592 64 655 95');time.sleep(2)
                rcon.cmd('dpermanent begin')
                deadline=time.monotonic()+30
                while time.monotonic()<deadline:
                    permanent_text=log.read_text(encoding='utf-8',errors='replace')
                    if '[dpermanent] END native-expiry' in permanent_text:break
                    if proc.poll() is not None:raise RuntimeError('Owned server exited during permanent terrain expiry')
                    time.sleep(.5)
                else:raise TimeoutError('Permanent expiry test timed out')
                pieces=[permanent_text]
                def restart_owned():
                    nonlocal proc,rcon
                    rcon.cmd('stop');proc.wait(timeout=60);rcon.sock.close();rcon=None
                    proc=subprocess.Popen([str(Path(build['jdk'])/'bin/java.exe'),'@'+str(lab/'launch.args')],cwd=lab,stdout=output,stderr=subprocess.STDOUT)
                    deadline=time.monotonic()+args.timeout
                    while time.monotonic()<deadline:
                        if proc.poll() is not None:raise RuntimeError('Owned permanent test server exited on restart')
                        body=log.read_text(encoding='utf-8',errors='replace')
                        if 'Done (' in body and 'Stopping server' not in body:
                            try:rcon=Rcon(rcon_port,password);break
                            except OSError:pass
                        time.sleep(.5)
                    else:raise TimeoutError('Permanent test restart timed out')
                    rcon.cmd('forceload add 592 64 655 95');time.sleep(2)
                restart_owned();rcon.cmd('dpermanent verify');rcon.cmd('dpermanent seedlegacy');time.sleep(.25)
                pieces.append(log.read_text(encoding='utf-8',errors='replace'))
                rcon.cmd('stop');proc.wait(timeout=60);rcon.sock.close();rcon=None
                # Move only this recipe's disposable journal; preserve it for diagnosis.
                lease=(lab/properties['level-name']/'data/opworldlike/power-block-leases.dat').resolve()
                if not lease.is_relative_to(lab.resolve()):raise RuntimeError('Journal escaped disposable lab')
                legacy=lease.with_name('temporary-blocks.dat');legacy_hash=digest(legacy)
                lease.rename(lease.with_name('power-block-leases.before-migration.dat'))
                proc=subprocess.Popen([str(Path(build['jdk'])/'bin/java.exe'),'@'+str(lab/'launch.args')],cwd=lab,stdout=output,stderr=subprocess.STDOUT)
                deadline=time.monotonic()+args.timeout
                while time.monotonic()<deadline:
                    if proc.poll() is not None:raise RuntimeError('Owned legacy test server exited')
                    body=log.read_text(encoding='utf-8',errors='replace')
                    if 'Done (' in body and 'Stopping server' not in body:
                        try:rcon=Rcon(rcon_port,password);break
                        except OSError:pass
                    time.sleep(.5)
                else:raise TimeoutError('Legacy migration restart timed out')
                rcon.cmd('forceload add 592 64 655 95');time.sleep(2);rcon.cmd('dpermanent verifylegacy');time.sleep(.25)
                pieces.append(log.read_text(encoding='utf-8',errors='replace'))
                (lab/'permanent-combined.log').write_text('\n'.join(pieces),encoding='utf-8')
                rows=re.findall(r'\[dpermanent\] \[(PASS|FAIL)\] (\S+) \| (.*)','\n'.join(pieces))
                required={stage+'/'+material for stage in ['placed','expiry','restart'] for material in ['crater','ice','sand','magma','mochi','leaves','quake']}
                required|={'edit-armed','release-keeps-block','expiry/player-edit','restart/player-edit','restart/released','legacy/seed','legacy/leaves-stable','legacy/player-edit','legacy/ice-retained','legacy/journal-preserved'}
                manifest['permanent_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for state,cid,detail in rows]
                manifest['permanent_missing']=sorted(required-{cid for _,cid,_ in rows})
                manifest['legacy_journal_retained']=digest(legacy)==legacy_hash
                good=manifest['legacy_journal_retained'] and not manifest['permanent_missing'] and len(rows)==len(required) and len({cid for _,cid,_ in rows})==len(required) and all(state=='PASS' for state,_,_ in rows)
                manifest['status']='passed' if manifest['status']=='passed' and good else 'failed'
    except Exception as error:
        manifest['status'] = 'error'
        manifest['error'] = str(error)
    finally:
        if proc is not None and proc.poll() is None:
            if rcon is not None:
                try: rcon.cmd('stop')
                except Exception: pass
            try: proc.wait(timeout=60)
            except subprocess.TimeoutExpired:
                # Process object identifies ONLY the child created above; never a PID search.
                proc.terminate()
                try: proc.wait(timeout=15)
                except subprocess.TimeoutExpired:
                    proc.kill(); proc.wait(timeout=15)
        if rcon is not None: rcon.sock.close()
        for sock, _ in reservations: sock.close()
        manifest['process_stopped'] = proc is None or proc.poll() is not None
        report.write_text(json.dumps(manifest, indent=2), encoding='utf-8')
    print('RESULT', manifest['status'], 'REPORT', report, flush=True)
    return 0 if manifest['status'] == 'passed' else 1


if __name__ == '__main__':
    raise SystemExit(main())
