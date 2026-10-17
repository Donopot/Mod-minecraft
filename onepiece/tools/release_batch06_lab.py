"""Release only the frozen B06 package, using actual server results and inspected two-role graphics."""
from pathlib import Path
import json,hashlib,shutil,argparse,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent;S=R/'.runtime/onepiece/after-b06-alpha15-r2'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def prepend(p,s):p.write_text(s+'\n\n---\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')
ap=argparse.ArgumentParser()
for name in ['native','visual','inspection','history']:ap.add_argument('--'+name,type=Path,required=True)
a=ap.parse_args();b=read(S/'build.local.json');pending=read(S/'pending-proof.local.json');n=read(a.native);v=read(a.visual/'visual-lab.local.json');i=read(a.inspection);h=read(a.history)
artifact=S/Path(b['jar']).name;assert sha(artifact)==b['sha256']==pending['build_sha256'] and sha(S/'source.zip')==pending['source_zip_sha256']
assert n['status']=='passed' and n['process_stopped'] and n['mods']['opworldlike']['sha256']==b['sha256']
for key,count in [('batch06_checks',535),('checks',59)]:assert len(n[key])==count and len({c['id'] for c in n[key]})==count and all(c['passed'] for c in n[key]),key
assert h['status']=='passed' and h['mods']['opworldlike']['sha256']==b['sha256'] and len(h['historical_checks'])==307 and all(c['passed'] for c in h['historical_checks'])
assert v['rendered'] and v['stopped'] and not v.get('retake')
visual_artifact=next((Path(v['client'])/'mods').glob('opworldlike*.jar'))
assert sha(visual_artifact)==v['build_sha256']
with zipfile.ZipFile(visual_artifact) as before,zipfile.ZipFile(artifact) as after:
 delta=sorted(name for name in set(before.namelist())|set(after.namelist()) if name not in before.namelist() or name not in after.namelist() or before.read(name)!=after.read(name))
 assert delta==['net/mcreator/minepiece/command/DevilFruitTestCommands.class','net/mcreator/minepiece/procedures/ZoanForms.class'],delta
 # Only historical fixtures and server-side energy/form invalidation changed. All client classes/assets match.
 assert all(before.read(name)==after.read(name) for name in after.namelist() if name.startswith('assets/') or '/client/' in name)
for who in ['actor','observer']:
 assert len(v[who+'_images'])==185 and all(Path(p).is_file() for p in v[who+'_images'])
 assert len(v[who+'_checks'])==46 and all(c['passed'] for c in v[who+'_checks'])
 assert len(v[who+'_pal_checks'])==82 and all(c['passed'] for c in v[who+'_pal_checks'])
assert len(v['native_player_checks'])==3 and all(c['passed'] for c in v['native_player_checks'])
assert i['inspected_by_agent'] and i['review_passed'] and not i['human_art_accepted'] and len(i['inspected_sheets'])==10 and all(Path(p).is_file() for p in i['inspected_sheets'])
assert Path(i['lab']).resolve()==a.visual.resolve()
assets=read(S/'asset-audit.local.json');pal=read(S/'batch06-animation-audit.local.json');assert not assets['issues'] and assets['sha256']==pal['jar_sha256']==b['sha256'] and pal['distinct_finite_tracks']==62
manual=Path(pending['manual_profile']);m=read(manual/'visual-lab.local.json');assert m['build_sha256']==b['sha256'] and not m['launched'] and not m['capture']
for name,digest in m['client_mods'].items():assert sha(Path(m['client'])/'mods'/name)==digest
backup=R/'.runtime/onepiece/launchers-before-b06-alpha15';backup.mkdir(exist_ok=True)
for old in P.glob('Tester-*.cmd'):
 if not (backup/old.name).exists():shutil.copy2(old,backup/old.name)
fruits=['mera_mera','magu_magu','goro_goro','hie_hie','pika_pika','moku_moku','suna_suna','yami_yami','gura_gura','bomu_bomu','zushi_zushi','nikyu_nikyu','ope_ope','mochi_mochi','mori_mori','kira_kira','supa_supa','riki_riki','aro_aro','horo_horo','mero_mero','kiro_kiro','bisu_bisu','kage_kage','soru_soru','uta_uta','imu','gomu_gomu','inu_inu_wolf','neko_neko_leopard','neko_neko_sabertiger','ushi_ushi_giraffe','batto_batto']
def launcher(name,choices):
 keys='123456789ABCDEFGHIJKLMNOPQRSTUVWX'[:len(choices)]
 assert len(keys)==len(choices)
 lines=['@echo off','setlocal','cd /d "%~dp0"','echo One Piece alpha.15 - laboratoire prive - 33 fruits']
 if len(choices)>1:lines+=['echo Changer de fruit reconstruit la scene de laboratoire.']+['echo '+keys[i]+' : '+f.replace('_',' ') for i,f in enumerate(choices)]+['choice /C '+keys+' /N /M "Choisir un fruit : "']
 lines+=['set "fruit='+choices[0]+'"']
 if len(choices)>1:lines+=['if errorlevel '+str(i+1)+' set "fruit='+f+'"' for i,f in enumerate(choices) if i]
 lines+=['echo R technique / B suivante / V hybride, animal complet, humain / F5 camera.','echo Vol Batto : double saut. Gomu : V parcourt les Gears 2, 3, 4, 5, humain.','echo Les modifications de blocs sont definitives dans ce monde de test.','"C:/Users/Anwender/AppData/Local/Programs/Python/Python312/python.exe" "%~dp0tools\\launch_visual_lab.py" --run --resume --lab "'+str(manual)+'" --fruit "%fruit%"','pause','endlocal']
 (P/name).write_bytes(('\r\n'.join(lines)+'\r\n').encode('ascii'))
for name,choices in [('Tester-Mera.cmd',fruits[:1]),('Tester-Magu.cmd',fruits[1:2]),('Tester-Goro.cmd',fruits[2:3]),('Tester-Lot-01.cmd',fruits[3:8]),('Tester-Lot-02.cmd',fruits[8:13]),('Tester-Lot-03.cmd',fruits[13:18]),('Tester-Lot-04.cmd',fruits[18:23]),('Tester-Lot-05.cmd',fruits[23:28]),('Tester-Lot-06.cmd',fruits[28:]),('Tester-OnePiece.cmd',fruits),('Tester-Terrain.cmd',fruits)]:launcher(name,choices)
manifest={'version':b['version'],'sha256':b['sha256'],'source_archive':str(S/'source.zip'),'source_archive_sha256':pending['source_zip_sha256'],'native_report':str(a.native.resolve()),'native_checks':535,'foundation_checks':59,'historical_report':str(a.history.resolve()),'historical_checks':307,'total_unique_server_checks':901,'exact_final_server_package':True,'native_tick_rate':80,'historical_tick_rate':20,'graphical_lab':str(a.visual.resolve()),'graphical_sha256':v['build_sha256'],'graphical_delta':delta,'client_classes_and_assets_identical':True,'graphical_images_per_role':185,'pal_evaluations_per_role':82,'graphical_checks_per_role':46,'native_real_player_checks':3,'inspection':str(a.inspection.resolve()),'inspected_sheets':i['inspected_sheets'],'original_failed_recipes_retained':['run-1e2eece9dde54e31803d6913a9da8690','run-70161921dca54b288ea4658d94d2d981','run-2cf8240437054f7894e3b8c163f92c21','run-89a1a436dbf14fc28b43732a2952d39f'],'manual_profile':str(manual),'manual_profile_launched':False,'fruits_implemented':33,'techniques_implemented':174,'amaru':True,'human_art_accepted':False,'normal_installation_changed':False}
(P/'build/b06-alpha15-verification.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8');(S/'verification.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
header=f'''# Lot06 vérifié en laboratoire — 17 octobre 2026

Loup, Léopard, Tigre à dents de sabre, Girafe et Batto :31 techniques,62 phases PAL distinctes,5 hybrides et5 formes animales complètes. **33 fruits et174 techniques traités**, plus Amaru. Il reste7 Zoans et39 techniques ; le travail continue jusqu'aux40.

Paquet alpha.15 `{b['sha256']}` :535 contrôles B06,59 du socle et307 historiques PASS sur ce même paquet, soit901 contrôles serveur distincts répartis sur deux recettes. 80ticks/s pour les mécaniques,20 pour les historiques ; ce n'est pas une mesure de performances. Les premières recettes échouées sont conservées avec leurs corrections : cibles chevauchant le mur, anciens tests synchrones, immunité intrinsèque des faux joueurs et horloge de capture de l'observateur.

Session graphique complète acteur+observateur :185 captures par rôle,82 évaluations PAL par rôle,46 contrôles par rôle,10 planches effectivement inspectées. Les dimensions/yeux des formes sont contrôlés sur les deux clients. Trois contrôles sur un vrai joueur vérifient le coup normal, l'esquive/immobilisation du contre et son expiration. Les classes client et toutes les ressources sont identiques au paquet final ; seuls les anciens scénarios et les conditions serveur de coupure du vol ont changé depuis ces captures. Acceptation artistique humaine en attente.

Tester-Lot-05 et Tester-Lot-06 sont disponibles ; Tester-OnePiece et Tester-Terrain proposent33fruits. Profil manuel neuf `{manual.name}`, jamais lancé à la livraison. Changer de fruit reconstruit explicitement le décor du laboratoire ; les pouvoirs ne restaurent jamais le terrain. Aucun monde/mod habituel ni VOID modifié. Adaptations : docs/BATCH-06.md. Preuves : build/b06-alpha15-verification.local.json.'''
(P/'docs/B06-VALIDATION-2026-10-17.md').write_text(header,encoding='utf-8')
for rel in ['docs/HANDOFF.md','docs/MIGRATION_STATUS.md','MODLOG.md']:prepend(P/rel,header)
for name in ['INU_INU_WOLF','NEKO_NEKO_LEOPARD','NEKO_NEKO_SABERTIGER','USHI_USHI_GIRAFFE','BATTO_BATTO']:
 p=P/'docs/fruits'/f'{name}.md';prepend(p,'# Lot06 : implémenté, testé serveur et vu en laboratoire\n\nVoir [bilan](../B06-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-06.md). Le cycle comporte maintenant une forme animale complète. Les affirmations historiques ci-dessous doivent être lues avec les corrections de source ; aucune acceptation artistique humaine acquise.')
(P/'LIRE-LOT-06.md').write_text('# Tester le lot06\n\nOuvrir Tester-Lot-06.cmd et choisir Loup, Léopard, Tigre à dents de sabre, Girafe ou Batto. R lance la technique ; B sélectionne la suivante ; V parcourt hybride, animal complet et humain ; F5 change la vue. Les formes durent60secondes et consomment de l’énergie. Batto : double saut pour voler. Kamie Zanshin est un contre, pas une frappe directe. Tester-OnePiece propose33fruits dans le laboratoire privé.\n\nLe même fruit conserve sa scène sauvegardée. Un changement de fruit reconstruit le décor de test. Les modifications de blocs faites par les pouvoirs restent définitives. Aucun changement aux parties habituelles. [Bilan](docs/B06-VALIDATION-2026-10-17.md).\n',encoding='utf-8')
prepend(P/'AGENTS.md','Current B06 frozen alpha.15:33fruits/174techniques+Amaru,901 exact-package serverPASS across535B06+59foundation+307history.185+185 screenshots,82PAL perrole,46clientchecks perrole,3actual-player counterchecks,10sheets inspected; client/assets byte-identical to final package, delta only historical fixtures + server flight policy. Proof build/b06-alpha15-verification.local.json, snapshot ../.runtime/onepiece/after-b06-alpha15-r2/source.zip. Human art pending. Current source may already be B07 work: never overwrite B06 frozen jar/profile/snapshot. Continue ALL40. Next B07 Makami7/Daibutsu5/Phoenix6/Seiryu8/Pteranodon3=29moves; B08last2dinosaurs10. Remaining7Zoans/39moves. PartialB07 root javap audit ../.runtime/onepiece/b07-source-audit. Old Mochi arm pivot + Gecko statue + victim-side counter reset cleanup still final-audit backlog. Normal installation untouched. Older sections below are historical.')
print('RELEASED_PRIVATE_B06',b['sha256'],'33fruits/174techniques; continue B07')
