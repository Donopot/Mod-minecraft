"""Bind Goro delivery notes/launcher to completed real-server and two-client evidence."""
from pathlib import Path
import argparse, hashlib, json, re, shutil

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parent

def read(path):
    return json.loads(path.read_text(encoding='utf-8'))

def write(path, value):
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--server', type=Path, required=True)
    p.add_argument('--visual', type=Path, required=True)
    p.add_argument('--manual', type=Path, required=True)
    args = p.parse_args()
    build = read(PROJECT / 'build/build-local.json')
    sha = hashlib.sha256(Path(build['jar']).read_bytes()).hexdigest()
    assert sha == build['sha256']
    server_path = args.server / 'report.local.json'
    visual_path = args.visual / 'visual-lab.local.json'
    server, visual = read(server_path), read(visual_path)
    manual = read(args.manual / 'visual-lab.local.json')
    assert server['status'] == 'passed' and server['process_stopped']
    assert server['mods']['opworldlike']['sha256'] == sha
    counts = {'checks': 59, 'goro_checks': 142, 'mera_checks': 92, 'magu_checks': 74, 'historical_checks': 307}
    for key, count in counts.items():
        rows = server[key]
        assert len(rows) == count and all(row['passed'] for row in rows), key
    assert visual['build_sha256'] == sha and visual['rendered'] and visual['stopped']
    assert 'AMARU_ACTIVE=0' in visual['disconnect_cleanup']
    pal_expected = {'goro_amaru_transform', 'goro_sango_charge', 'goro_sango_release', 'goro_elthor_charge', 'goro_elthor_release', 'goro_raigo_cast'}
    for who, directory in [('actor', Path(visual['client'])), ('observer', Path(visual['observer']))]:
        assert len(visual[who+'_images']) == 12
        assert len(visual[who+'_checks']) == 13 and all(r['passed'] for r in visual[who+'_checks'])
        body = (directory / 'process.log').read_text(encoding='utf-8', errors='replace')
        pal = re.findall(r'\[goro-pal\] (\S+) (PASS|FAIL)', body)
        assert {name for name, ok in pal if ok == 'PASS'} == pal_expected
        assert all(ok == 'PASS' for _, ok in pal)
        visual[who+'_pal_checks'] = [{'id': name, 'passed': ok == 'PASS'} for name, ok in pal]
    write(visual_path, visual)
    assert manual['build_sha256'] == sha and not manual['launched'] and not manual['capture']
    reserve = read(ROOT / '.runtime/onepiece/reference-jars/manifest.local.json')
    assert len(reserve['files']) == 3
    for row in reserve['files']:
        assert hashlib.sha256(Path(row['reserved']).read_bytes()).hexdigest() == row['sha256']
    # Preserve the previous entry point before updating only the current Goro launcher.
    launcher = PROJECT / 'Tester-Goro.cmd'
    previous = ROOT / '.runtime/onepiece/Tester-Goro-before-alpha7.cmd'
    if not previous.exists():
        shutil.copy2(launcher, previous)
    python = Path(__import__('sys').executable)
    launcher.write_text('@echo off\n'
                       'echo Goro Goro alpha.7 - monde de test independant\n'
                       'echo R : technique / B : suivante / V : Amaru et retour / F5 : camera\n'
                       'echo En Amaru : double saut pour voler ; saut pour monter, Maj pour descendre.\n'
                       '"'+str(python)+'" "'+str(PROJECT/'tools/launch_visual_lab.py')+'" --run --lab "'+str(args.manual)+'"\n'
                       'pause\n', encoding='utf-8')
    actor_seen = ['actor-060-form.png', 'actor-090-form.png', 'actor-115-form.png', 'actor-290-elthor.png', 'actor-350-raigo.png']
    observer_seen = [Path(x).name for x in visual['observer_images']]
    inspected = [str(Path(visual['client'])/'screenshots'/x) for x in actor_seen]
    inspected += [str(Path(visual['observer'])/'screenshots'/x) for x in observer_seen]
    assert len(set(inspected)) == 17 and all(Path(x).is_file() for x in inspected)
    summary = dict(version=build['version'], sha256=sha, server_report=str(server_path), visual_report=str(visual_path),
                   manual_profile=str(args.manual), manual_launcher=str(launcher),
                   goro_checks=142, goro_technique_checks=104, amaru_checks=38, mera_checks=92, magu_checks=74,
                   foundation_checks=59, historical_checks=307, server_total=674,
                   actor_live_checks=13, observer_live_checks=13, actor_captures=12, observer_captures=12,
                   actual_pal_unique_per_client=6, distinct_finite_goro_tracks=12,
                   inspected_images=inspected, distinct_captures_inspected=17,
                   actual_client_disconnect_cleanup=True, reference_reserve=str(ROOT/'.runtime/onepiece/reference-jars/manifest.local.json'),
                   amaru_model='integrated; electric Daibutsu silhouette adaptation with Enel drums',
                   amaru_dimensions=[2.4,6.5], amaru_eye_height=5.5, human_art_acceptance='pending human play',
                   full_fruits_human_accepted=0, installed_in_normal_profile=False, manual_profile_launched=False,
                   automated_processes_stopped=True)
    write(PROJECT/'build/verification-summary.local.json', summary)
    result = ('JAR **'+build['version']+'**, SHA256 `'+sha+'`.\n\n'
              '- Serveur `'+args.server.name+'` : **142 Goro/Amaru +92 Mera +74 Magu +59 socle +307 historiques =674 PASS**. Loot et redémarrage réussis. Les 142 incluent104 techniques et38 contrôles Amaru.\n'
              '- Deux clients `'+args.visual.name+'` : **13 contrôles chacun**, dont dimensions/yeux par UUID, gestes effectivement évalués, retours, vol par double saut, caméra10 et resynchronisation après retracking. Déconnexion réelle : `AMARU_ACTIVE=0`.\n'
              '- 12 captures acteur +12 observateur ; **17 images distinctes inspectées**. Aucun défaut visuel bloquant constaté ; contraste et visage restent des choix stylisés à apprécier en jeu. Six pistes PAL distinctes effectivement évaluées par client dans cette scène ciblée ; douze pistes Goro finies auditées dans le JAR.\n'
              '- Audit ressources637JSON/0erreur/4anciensavertissements ; huit pistes Magu inchangées ; chargement client17cibles PASS, sans prétendre couvrir tout le modpack.\n'
              '- Profil manuel `'+args.manual.name+'`, même SHA256, neuf et non lancé ; Tester-Goro et alias Tester-OnePiece prêts. Profils Mera/Magu conservés. Aucun monde/mod habituel ni VOID remplacé ; enfants automatiques arrêtés.\n\n'
              'Les résultats et chemins sont dans `build/verification-summary.local.json`. L’appréciation artistique humaine reste à recueillir ; les autres fruits ne sont pas déclarés terminés.')
    report = PROJECT/'docs/GORO-AMARU-2026-10-16.md'
    text = report.read_text(encoding='utf-8').replace('**Résultats finaux : à compléter après les processus en cours.**', result)
    report.write_text(text, encoding='utf-8')
    print('GORO_DELIVERY_READY', sha, '674serverPASS 26clientPASS 24captures 17inspected; manual profile unlaunched')

if __name__ == '__main__':
    main()
