"""Sequential private graphical retakes for the reviewed ver15 asset pass.

Does not install mods or modify normal worlds. Each runner owns its fresh lab.
Failed manifests and output are retained; later independent scenes still run.
"""
from pathlib import Path
import argparse, hashlib, json, subprocess, sys, time

P = Path(__file__).resolve().parents[1]
SCENES = [
    ('batch07', 'hito_hito_daibutsu,tori_tori_phoenix,uo_uo_seiryu'),
    ('batch08', 'ryu_ryu_brachiosaurus'),
    ('mera', ''),
    ('goro', ''),
    ('batch01', 'hie_hie'),
    ('batch02', 'zushi_zushi'),
    ('batch04', 'horo_horo,bisu_bisu'),
    ('batch05', 'uta_uta'),
    ('batch06', 'inu_inu_wolf'),
    ('batch03', 'mori_mori,riki_riki'),
]

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--only', default='')
    a = ap.parse_args()
    build = json.loads((P/'build/build-local.json').read_text(encoding='utf-8'))
    digest = hashlib.sha256(Path(build['jar']).read_bytes()).hexdigest()
    assert digest == build['sha256']
    dest = P/'build/source-assets-runs'/time.strftime('%Y%m%d-%H%M%S')
    dest.mkdir(parents=True)
    rows = []
    for scenario, retake in SCENES:
        if a.only and scenario not in a.only.split(','):
            continue
        # Fail closed if someone rebuilds the candidate during this queue.
        assert hashlib.sha256(Path(build['jar']).read_bytes()).hexdigest() == digest
        cmd = [sys.executable, str(P/'tools/launch_visual_lab.py'),
               '--accept-eula', '--capture', '--scenario', scenario, '--run']
        if retake:
            cmd += ['--retake', retake]
        output = dest/(scenario+'.log')
        print('SCENE_BEGIN', scenario, retake, flush=True)
        with output.open('w', encoding='utf-8') as log:
            code = subprocess.run(cmd, stdout=log, stderr=subprocess.STDOUT).returncode
        body = output.read_text(encoding='utf-8')
        labs = [line.split('VISUAL_LAB_PREPARED ',1)[1] for line in body.splitlines()
                if line.startswith('VISUAL_LAB_PREPARED ')]
        rows.append(dict(scenario=scenario, retake=retake, exit_code=code,
                         lab=labs[0] if labs else None, log=str(output)))
        (dest/'queue.local.json').write_text(json.dumps(dict(build_sha256=digest,
            runs=rows, complete=False), indent=2), encoding='utf-8')
        print('SCENE_END', scenario, code, labs, flush=True)
    (dest/'queue.local.json').write_text(json.dumps(dict(build_sha256=digest,
        runs=rows, complete=True), indent=2), encoding='utf-8')
    print('QUEUE_COMPLETE', dest, flush=True)

if __name__ == '__main__':
    main()
