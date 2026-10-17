"""Release only a verified private B03 laboratory, preserving normal installations."""
from pathlib import Path
import argparse, hashlib, json, shutil, re, zipfile

P=Path(__file__).resolve().parents[1]
R=P.parent
def read(p): return json.loads(p.read_text(encoding='utf-8'))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def prepend(p,body):
    p.write_text(body+'\n\n---\n\nHistorique conservé ; les statuts anciens ne définissent pas la livraison actuelle.\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')

ap=argparse.ArgumentParser()
for name in ['report','goro','smoke','visual','profile','inspection']: ap.add_argument('--'+name,type=Path,required=True)
a=ap.parse_args()
b=read(P/'build/build-local.json');d=read(a.report);v=read(a.visual/'visual-lab.local.json');m=read(a.profile/'visual-lab.local.json');review=read(a.inspection)
jar=Path(b['jar'])
assert sha(jar)==b['sha256'] and d['process_stopped']
g=read(a.goro);smoke=read(a.smoke)
for test in [g,smoke]:assert test['status']=='passed' and test['process_stopped'] and test['mods']['opworldlike']['sha256']==b['sha256']
assert len(smoke['checks'])==59 and len(smoke['batch03_checks'])==124 and all(c['passed'] for c in smoke['checks']+smoke['batch03_checks'])
assert len(g['goro_checks'])==142 and all(c['passed'] for c in g['goro_checks'])
reference=next((a.report.parent/'mods').glob('opworldlike*.jar'))
assert sha(reference)==d['mods']['opworldlike']['sha256']
with zipfile.ZipFile(reference) as old,zipfile.ZipFile(jar) as final:
    assert set(old.namelist())==set(final.namelist())
    changed=[n for n in old.namelist() if old.read(n)!=final.read(n)]
    assert set(changed)=={'net/mcreator/minepiece/command/RepairTestCommands.class','net/mcreator/minepiece/client/Batch03VisualLab.class'}
# Keep the failed160TPS run intact. Replace only its Goro scope with the successful80TPS run.
failures=[(key,c['id']) for key,rows in d.items() if isinstance(rows,list) for c in rows if isinstance(c,dict) and c.get('passed') is False]
assert failures==[('goro_checks','chain-refused-no-bridge')],failures
verified={**d,'goro_checks':g['goro_checks']}

counts={'checks':59,'mera_checks':92,'magu_checks':74,'goro_checks':142,'batch01_checks':345,'terrain_checks':271,'batch02_checks':363,'batch03_checks':418,'historical_checks':307,'permanent_checks':31}
for key,count in counts.items():
    assert len(verified[key])==count and len({c['id'] for c in verified[key]})==count and all(c['passed'] for c in verified[key]),key
assert d['historical_loot']['passed'] and d['legacy_journal_retained']
assert v['rendered'] and v['stopped'] and v['scenario']=='batch03' and not v.get('retake')
for who in ['actor','observer']:
    assert len(v[who+'_images'])==120 and all(Path(p).is_file() for p in v[who+'_images'])
    assert all(c['passed'] for c in v[who+'_checks']) and len(v[who+'_pal_checks'])==50 and all(c['passed'] for c in v[who+'_pal_checks'])
assert len(v['native_player_checks'])==2 and all(c['passed'] for c in v['native_player_checks'])
assert review['lab']==str(a.visual.resolve()) and len(review['inspected_sheets'])==10
assert all(Path(p).is_file() for p in review['inspected_sheets']) and review['inspected_by_agent'] and not review['human_art_accepted']
observed=next(p for p in (Path(v['client'])/'mods').glob('opworldlike*.jar'))
equivalent=[]
with zipfile.ZipFile(observed) as old,zipfile.ZipFile(jar) as new:
    names={n for n in new.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
    assert names=={n for n in old.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
    for name in names: assert old.read(name)==new.read(name),name
    equivalent=sorted(names)
assets=read(P/'build/asset-audit.local.json');anim=read(P/'build/batch03-animation-audit.local.json')
assert assets['sha256']==anim['jar_sha256']==b['sha256'] and not assets['issues'] and anim['distinct_finite_tracks']==48
assert m['build_sha256']==b['sha256'] and not m['launched'] and not m['capture']
for name,h in m['client_mods'].items(): assert sha(Path(m['client'])/'mods'/name)==h
backup=R/'.runtime/onepiece/launchers-before-b03-alpha12';backup.mkdir(parents=True,exist_ok=True)
for path in P.glob('Tester-*.cmd'):
    if not (backup/path.name).exists(): shutil.copy2(path,backup/path.name)
fruits=['mera_mera','magu_magu','goro_goro','hie_hie','pika_pika','moku_moku','suna_suna','yami_yami','gura_gura','bomu_bomu','zushi_zushi','nikyu_nikyu','ope_ope','mochi_mochi','mori_mori','kira_kira','supa_supa','riki_riki']
py='C:/Users/Anwender/AppData/Local/Programs/Python/Python312/python.exe'
def launcher(name,choices):
    keys='123456789ABCDEFGHI'[:len(choices)]
    lines=['@echo off','setlocal','cd /d "%~dp0"','echo One Piece alpha.12 - lot03 - laboratoire prive']
    if len(choices)>1:
        lines+=['echo Changer de fruit reconstruit la scene de laboratoire.']
        lines+=['echo '+keys[i]+' : '+fruit.replace('_',' ') for i,fruit in enumerate(choices)]
        lines+=['choice /C '+keys+' /N /M "Choisir un fruit : "']
    lines+=['set "fruit='+choices[0]+'"']
    if len(choices)>1: lines+=['if errorlevel '+str(i+1)+' set "fruit='+fruit+'"' for i,fruit in enumerate(choices) if i]
    lines+=['echo R technique / B suivante / F5 camera. V propulsion Mera ou forme Amaru Goro.',
            'echo Les blocs modifies restent definitifs dans ce monde de test.',
            '"'+py+'" "%~dp0tools\\launch_visual_lab.py" --run --resume --lab "'+str(a.profile.resolve())+'" --fruit "%fruit%"','pause','endlocal']
    (P/name).write_bytes(('\r\n'.join(lines)+'\r\n').encode('ascii'))
for name,choices in [('Tester-Mera.cmd',fruits[:1]),('Tester-Magu.cmd',fruits[1:2]),('Tester-Goro.cmd',fruits[2:3]),('Tester-Lot-01.cmd',fruits[3:8]),('Tester-Lot-02.cmd',fruits[8:13]),('Tester-Lot-03.cmd',fruits[13:]),('Tester-OnePiece.cmd',fruits),('Tester-Terrain.cmd',fruits)]: launcher(name,choices)
total=sum(counts.values())
manifest={'version':b['version'],'sha256':b['sha256'],'server_report':str(a.report.resolve()),'checks':counts,'total_server_checks':total,'native_tick_rate':d['native_test_tick_rate'],'historical_tick_rate':20,'all_selected_scopes_passed':True,'reference_full_run_status':d['status'],'reference_sha256':d['mods']['opworldlike']['sha256'],'diagnostic_only_changed_entries':changed,'goro_retry_report':str(a.goro.resolve()),'final_package_smoke_report':str(a.smoke.resolve()),'final_package_direct_distinct_checks':325,'graphical_lab':str(a.visual.resolve()),'graphical_components':v.get('component_labs',[]),'graphical_hash':v['build_sha256'],'render_entries_byte_identical':equivalent,'inspected_sheets':review['inspected_sheets'],'manual_profile':str(a.profile.resolve()),'manual_profile_launched':False,'launcher_backup':str(backup),'human_art_accepted':False,'normal_installation_changed':False}
(P/'build/b03-alpha12-verification.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
header=f'''# Livraison actuelle — lot03 alpha.12

Mochi6, Mori4, Kira5, Supa4 et Riki5 :24techniques corrigées,48gestes finis distincts et24décisions terrain explicites. Les18fruits traités représentent95techniques, plus Amaru ; les40fruits ne sont pas déclarés terminés. Voir [le bilan du lot03](B03-VALIDATION-2026-10-16.md) et [la politique terrain](TERRAIN-POLICY.md).

**{total} contrôles serveur couverts PASS par des recettes complémentaires sur un code de jeu identique**. Le paquet final est `{b['sha256']}` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,418B03,307historiques,31persistance. Loot, redémarrage et migration du journal ancien inchangé PASS. Mécaniques à160ticks/s, reprise Goro à80, historiques à20. La passe160 garde son échec Goro isolé ; seule sa recette Goro est remplacée dans cette couverture par les142PASS de la reprise.325contrôles distincts ont aussi été exécutés directement sur le paquet final. Les deux seules classes modifiées après la passe complète concernent le diagnostic graphique ; tous les autres octets du JAR sont identiques. Ce résultat ne prouve pas le ressenti ou les performances à20TPS.

Session groupée initiale à deux clients, puis reprises ciblées sur le JAR final : couverture retenue120captures acteur+120observateur,50évaluations de poses par rôle (48phases distinctes et le Pistol de suivi), revêtements/expiration, déchargement et retour d’observateur, dégâts natifs DiamondBody. Dix planches inspectées par l’agent. Ressources et classes de rendu identiques octet par octet au JAR observé. Acceptation artistique humaine en attente.

Tester-Lot-03 ouvre les cinq nouveaux fruits ; Tester-OnePiece et Tester-Terrain offrent18fruits. Profil privé neuf `{a.profile.name}`, non lancé à la livraison ; anciens profils et lanceurs sauvegardés. Aucun mod/monde habituel ni VOID modifié. Le terrain reste définitif ; les effets sur entités gardent leurs durées.'''
for rel in ['docs/HANDOFF.md','docs/MIGRATION_STATUS.md','docs/BATCH-03.md']: prepend(P/rel,header)
prepend(P/'MODLOG.md',header.replace('(B03-VALIDATION-2026-10-16.md)','(docs/B03-VALIDATION-2026-10-16.md)').replace('(TERRAIN-POLICY.md)','(docs/TERRAIN-POLICY.md)'))
for key in fruits[13:]:
    path=P/'docs/fruits'/(key.upper()+'.md');s=path.read_text(encoding='utf-8').replace('La recette serveur et le rendu groupé sont en cours ; aucune acceptation artistique humaine revendiquée.','Recette serveur et rendu groupé vérifiés : [bilan final](../B03-VALIDATION-2026-10-16.md). Acceptation artistique humaine en attente.');path.write_text(s,encoding='utf-8')
path=P/'AGENTS.md';s=path.read_text(encoding='utf-8');s=re.sub(r'Current priority, 2026-10-16:.*?(?=\n\nPrevious delivery)',f'Current priority, 2026-10-16: B03 alpha.12 delivered privately. Mochi/Mori/Kira/Supa/Riki24techniques,418B03 and{total} covered native PASS on byte-identical gameplay plus325direct-final-package checks; the original160TPS run retains one Goro failure, followed by142Goro PASS at80TPS, loot/restart/legacy migration PASS. Grouped two-client graphics120+120 captures,50PAL evaluations/client,10sheets inspected, body retracking/expiry and native diamond damage verified. Source adaptations, remaining Gomu cadence debt and limitations in docs/B03-VALIDATION-2026-10-16.md. Manual profile {a.profile.name}, fresh unlaunched; launchers18fruits, new Tester-Lot-03. User requires all block mutations permanent. Keep old proofs/profiles and reserved Forge references. No normal installation changes. Human art acceptance pending. Continue batch-of-five workflow only as requested.',s,flags=re.S);path.write_text(s,encoding='utf-8')
print('RELEASED_PRIVATE_B03',b['version'],'PASS',total,'PROFILE',a.profile.name)
