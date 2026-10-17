"""Finalize a verified PRIVATE test delivery; never writes normal Minecraft mods or saves."""
from pathlib import Path
import argparse,hashlib,json,re,shutil,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def prepend(p,text):
 s=p.read_text(encoding='utf-8');p.write_text(text+'\n\n---\n\nHistorique conservé ci-dessous ; les statuts anciens ne définissent plus la livraison actuelle.\n\n'+s,encoding='utf-8')
ap=argparse.ArgumentParser();ap.add_argument('--report',type=Path,required=True);ap.add_argument('--profile',type=Path,required=True);a=ap.parse_args()
b=read(P/'build/build-local.json');d=read(a.report);profile=read(a.profile/'visual-lab.local.json');jar=Path(b['jar'])
assert d['status']=='passed' and d['process_stopped'] and d['mods']['opworldlike']['sha256']==b['sha256']==sha(jar)
assert profile['build_sha256']==b['sha256'] and not profile['launched']
counts={'checks':59,'mera_checks':92,'magu_checks':74,'goro_checks':142,'batch01_checks':345,'terrain_checks':271,'batch02_checks':363,'historical_checks':307,'permanent_checks':31}
for key,count in counts.items():assert len(d[key])==count and all(r['passed'] for r in d[key]),key
assert d['legacy_journal_retained'] and d['historical_loot']['passed']
for name,h in profile['client_mods'].items():assert sha(Path(profile['client'])/'mods'/name)==h
assets=read(P/'build/asset-audit.local.json');anim=read(P/'build/batch02-animation-audit.local.json')
assert assets['sha256']==anim['jar_sha256']==b['sha256'] and not assets['issues'] and anim['distinct_finite_tracks']==52
baseline=P/'build/b02-alpha10-before-permanent.jar';visual_equal=[]
with zipfile.ZipFile(baseline) as old,zipfile.ZipFile(jar) as new:
 names={n for n in old.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
 assert names=={n for n in new.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
 for name in names:assert old.read(name)==new.read(name),name
 visual_equal=sorted(names)
backup=R/'.runtime/onepiece/launchers-before-permanent-alpha11';backup.mkdir(parents=True,exist_ok=True)
for p in P.glob('Tester-*.cmd'):
 if not (backup/p.name).exists():shutil.copy2(p,backup/p.name)
py=Path('C:/Users/Anwender/AppData/Local/Programs/Python/Python312/python.exe')
fruits=['mera_mera','magu_magu','goro_goro','hie_hie','pika_pika','moku_moku','suna_suna','yami_yami','gura_gura','bomu_bomu','zushi_zushi','nikyu_nikyu','ope_ope']
def launcher(name,choices):
 lines=['@echo off','setlocal','cd /d "%~dp0"','echo One Piece alpha.11 - terrain definitif - laboratoire prive']
 if len(choices)>1:
  keys='123456789ABCD'[:len(choices)]
  lines += ['echo '+keys[i]+' : '+f.replace('_',' ') for i,f in enumerate(choices)]
  lines += ['choice /C '+keys+' /N /M "Choisir un fruit : "','set "fruit='+choices[0]+'"']
  lines += ['if errorlevel '+str(i+1)+' set "fruit='+f+'"' for i,f in enumerate(choices) if i]
 else:lines+=['set "fruit='+choices[0]+'"']
 lines+=['echo R technique / B suivante / F5 camera. V propulsion Mera ou forme Amaru Goro.',
         'echo Les crateres et blocs crees restent definitifs dans ce monde de test.',
         '"'+str(py)+'" "%~dp0tools\\launch_visual_lab.py" --run --resume --lab "'+str(a.profile.resolve())+'" --fruit "%fruit%"','pause','endlocal']
 (P/name).write_bytes(('\r\n'.join(lines)+'\r\n').encode('ascii'))
launcher('Tester-Mera.cmd',fruits[:1]);launcher('Tester-Magu.cmd',fruits[1:2]);launcher('Tester-Goro.cmd',fruits[2:3])
launcher('Tester-Lot-01.cmd',fruits[3:8]);launcher('Tester-Lot-02.cmd',fruits[8:]);launcher('Tester-OnePiece.cmd',fruits);launcher('Tester-Terrain.cmd',fruits)
total=sum(counts.values())
manifest={'version':b['version'],'jar':str(jar),'sha256':b['sha256'],'server_report':str(a.report.resolve()),'server_checks':counts,'total_server_checks':total,'all_passed':True,'native_tick_rate':d['native_test_tick_rate'],'historical_tick_rate':20,'legacy_journal_retained':True,'manual_profile':str(a.profile.resolve()),'manual_profile_launched':False,'launcher_backup':str(backup),'previous_client_evidence':['run-00da83431e7b49cfb768cf08f3a8b432','run-8f3f645059f645c2b62995dbb62a5128'],'client_and_assets_byte_identical_to_alpha10':visual_equal,'new_graphical_run_claimed':False,'human_art_accepted':False,'normal_installation_changed':False}
(P/'build/permanent-alpha11-verification.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
header=f'''# Livraison actuelle — alpha.11, terrain définitif et lot02

Les71techniques des13fruits traités et Amaru ont une décision terrain explicite. Les météores détruisent les toits au vrai contact ; les cratères, glace, grès, magma, mochi et feuilles créés par les pouvoirs sont définitifs. Aucun ancien état de bloc n’est réappliqué. Les champs et états sur entités restent bornés. [Politique](TERRAIN-POLICY.md), [migration](TERRAIN-DEFINITIF-alpha11.md).

**{total} contrôles serveur PASS** sur le JAR final `{b['sha256']}` :59socle,92Mera,74Magu,142Goro/Amaru,345B01,271terrain,363B02,307historiques,31persistance. Loot, redémarrage, migration du journal ancien inchangé et préservation des modifications joueur PASS. Mécaniques à80ticks/s, historiques à20 ; cela ne prouve pas les performances ou le ressenti.

Les graphismes B02 ont été observés dans une session groupée à deux clients, puis Zushi/Ope repris après correction d’une texture manquante. Les ressources et classes client sont identiques octet par octet à alpha.10 vérifiée graphiquement. Aucune nouvelle session graphique alpha.11 ni acceptation artistique humaine revendiquée. [Bilan B02](B02-VALIDATION-2026-10-16.md).

Tester-OnePiece et Tester-Terrain offrent13fruits ; Tester-Lot-02 offre Gura/Bomu/Zushi/Nikyu/Ope. Profil neuf privé `{a.profile.name}`, non lancé à la livraison. Anciens lanceurs/profils conservés. Aucun mod/monde habituel ni VOID modifié. Les40fruits ne sont pas déclarés terminés.'''
for rel in ['docs/HANDOFF.md','docs/MIGRATION_STATUS.md']:prepend(P/rel,header)
prepend(P/'MODLOG.md',header.replace('(TERRAIN-POLICY.md)','(docs/TERRAIN-POLICY.md)').replace('(TERRAIN-DEFINITIF-alpha11.md)','(docs/TERRAIN-DEFINITIF-alpha11.md)').replace('(B02-VALIDATION-2026-10-16.md)','(docs/B02-VALIDATION-2026-10-16.md)'))
prepend(P/'docs/BATCH-02.md',header)
for key in fruits+['mochi_mochi','mori_mori']:
 p=P/'docs/fruits'/(key.upper()+'.md')
 if p.exists():prepend(p,'**Mise à jour alpha.11** : toutes les modifications de blocs deviennent définitives à la demande du joueur. Les exigences historiques de restitution ci-dessous sont remplacées par [la politique actuelle](../TERRAIN-DEFINITIF-alpha11.md). Les effets sur entités gardent leurs durées. Voir [le bilan B02](../B02-VALIDATION-2026-10-16.md) pour les cinq fruits de ce lot ; les contrôles historiques des autres fruits ne constituent pas leur validation complète.')
p=P/'AGENTS.md';s=p.read_text(encoding='utf-8');s=re.sub(r'Current priority, 2026-10-16:.*?(?=\n\nPrevious delivery)',f'Current priority, 2026-10-16: alpha.11 delivered privately. User requires ALL power block changes permanent, including old helpers. {total} exact-final-hash server PASS, loot/restart/legacy migration PASS. B02 completed server and grouped graphics, missing rock texture fixed in targeted retake; all client/assets byte-identical in alpha.11. Manual profile {a.profile.name}; all previous launchers updated, new Tester-Lot-02. No normal installation changes, human art acceptance pending. See docs/B02-VALIDATION-2026-10-16.md, docs/TERRAIN-DEFINITIF-alpha11.md and build/permanent-alpha11-verification.local.json. Keep prior proofs and profiles. Continue batch-of-five workflow only as requested.',s,flags=re.S);p.write_text(s,encoding='utf-8')
print('RELEASED_PRIVATE_TEST',b['version'],'PASS',total,'PROFILE',a.profile.name)
