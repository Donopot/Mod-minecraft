"""Release only after real native results and reviewed grouped graphical evidence exist."""
from pathlib import Path
import argparse,json,hashlib,shutil,zipfile
P=Path(__file__).resolve().parents[1];R=P.parent
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def prepend(p,text):p.write_text(text+'\n\n---\n\nArchive des étapes précédentes :\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')
ap=argparse.ArgumentParser()
for n in ['report','retake','history','visual','profile','inspection']:ap.add_argument('--'+n,type=Path,required=True)
a=ap.parse_args();b=read(P/'build/build-local.json');d=read(a.report);v=read(a.visual/'visual-lab.local.json');m=read(a.profile/'visual-lab.local.json');review=read(a.inspection)
assert sha(Path(b['jar']))==b['sha256']
original=d;retake=read(a.retake);history=read(a.history)
assert original['process_stopped'] and retake['status']=='passed' and retake['process_stopped'] and history['status']=='passed' and history['process_stopped']
assert retake['batch04_filter']=='' and history['batch04_filter']=='mero'
failures={(key,c['id']) for key in ['checks','batch03_checks','batch04_checks','historical_checks','permanent_checks'] for c in original.get(key,[]) if not c['passed']}
# Only the418 successful B03 checks are retained from the first recipe. Its other scopes remain failed in their original report.
d={'checks':retake['checks'],'batch03_checks':original['batch03_checks'],'batch04_checks':retake['batch04_checks'],'historical_checks':history['historical_checks'],'permanent_checks':history['permanent_checks'],'historical_loot':history['historical_loot'],'legacy_journal_retained':history['legacy_journal_retained'],'native_test_tick_rate':retake['native_test_tick_rate']}
counts={'checks':59,'batch03_checks':418,'batch04_checks':365,'historical_checks':307,'permanent_checks':31}
for key,count in counts.items():assert len(d[key])==count and len({c['id'] for c in d[key]})==count and all(c['passed'] for c in d[key]),key
assert d['historical_loot']['passed'] and d['legacy_journal_retained']
reference=next((a.report.parent/'mods').glob('opworldlike*.jar'));retake_jar=next((a.retake.parent/'mods').glob('opworldlike*.jar'));history_jar=next((a.history.parent/'mods').glob('opworldlike*.jar'))
assert sha(reference)==original['mods']['opworldlike']['sha256']
assert sha(retake_jar)==retake['mods']['opworldlike']['sha256']==b['sha256']
assert sha(history_jar)==history['mods']['opworldlike']['sha256']
# The old B03 gameplay is unchanged. Additions are scoped B04 visual replication, fixtures, name, trail and rendering.
allowed={'net/mcreator/minepiece/client/Batch04VisualLab.class','net/mcreator/minepiece/procedures/ParameciaStoneData.class','net/mcreator/minepiece/procedures/ParameciaControl.class','net/mcreator/minepiece/procedures/ParameciaBatchTechniques.class','opworldlike.mixins.json','br/com/oneminepiece/opworldlike/mixin/ParameciaLivingRendererMixin.class','assets/minepiece/geckolib/animations/heart_bullet.animation.json','br/com/oneminepiece/opworldlike/mixin/ParameciaStoneDataMixin.class','net/mcreator/minepiece/client/renderer/SkillProjectileRenderer.class','net/mcreator/minepiece/command/DevilFruitTestCommands.class','net/mcreator/minepiece/command/RepairTestCommands.class','net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.class','net/mcreator/minepiece/command/Batch04TestCommands.class','net/mcreator/minepiece/procedures/DevilFruitMove.class'}
with zipfile.ZipFile(reference) as old,zipfile.ZipFile(b['jar']) as final:
 changed=sorted(n for n in set(old.namelist())|set(final.namelist()) if n not in old.namelist() or n not in final.namelist() or old.read(n)!=final.read(n))
 assert set(changed)==allowed,changed
with zipfile.ZipFile(history_jar) as old,zipfile.ZipFile(b['jar']) as final:
 assert set(old.namelist())==set(final.namelist())
 history_changes=[n for n in old.namelist() if old.read(n)!=final.read(n)]
 assert set(history_changes)=={'net/mcreator/minepiece/procedures/ParameciaBatchTechniques.class','net/mcreator/minepiece/client/renderer/SkillProjectileRenderer.class','net/mcreator/minepiece/command/RepairTestCommands.class','net/mcreator/minepiece/client/renderer/TechniqueVisualProfile.class'},history_changes
 # The only procedure change here selects ASH instead of END_ROD for the B04 trail.
assert v['scenario']=='batch04' and v['rendered'] and v['stopped'] and not v.get('retake')
for who in ['actor','observer']:
 assert len(v[who+'_images'])==105 and all(Path(p).is_file() for p in v[who+'_images'])
 assert len(v[who+'_checks'])==(29 if who=='actor' else 27) and all(c['passed'] for c in v[who+'_checks'])
 assert len(v[who+'_pal_checks'])==46 and all(c['passed'] for c in v[who+'_pal_checks'])
observed=next((Path(v['client'])/'mods').glob('opworldlike*.jar'))
with zipfile.ZipFile(observed) as old,zipfile.ZipFile(b['jar']) as final:
 names={n for n in final.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
 assert names=={n for n in old.namelist() if n.startswith('assets/') or '/client/' in n or 'Renderer' in n}
 assert all(old.read(n)==final.read(n) for n in names)
assert review['inspected_by_agent'] and not review['human_art_accepted'] and len(review['inspected_sheets'])==10 and all(Path(p).is_file() for p in review['inspected_sheets'])
assert review['lab']==str(a.visual.resolve())
assets=read(P/'build/asset-audit.local.json');animations=read(P/'build/batch04-animation-audit.local.json')
assert not assets['issues'] and assets['sha256']==animations['jar_sha256']==b['sha256'] and animations['distinct_finite_tracks']==42
assert m['build_sha256']==b['sha256'] and not m['launched'] and not m['capture']
for name,h in m['client_mods'].items():assert sha(Path(m['client'])/'mods'/name)==h
backup=R/'.runtime/onepiece/launchers-before-b04-alpha13';backup.mkdir(parents=True,exist_ok=True)
for p in P.glob('Tester-*.cmd'):
 if not (backup/p.name).exists():shutil.copy2(p,backup/p.name)
fruits=['mera_mera','magu_magu','goro_goro','hie_hie','pika_pika','moku_moku','suna_suna','yami_yami','gura_gura','bomu_bomu','zushi_zushi','nikyu_nikyu','ope_ope','mochi_mochi','mori_mori','kira_kira','supa_supa','riki_riki','aro_aro','horo_horo','mero_mero','kiro_kiro','bisu_bisu']
def launcher(name,choices):
 keys='123456789ABCDEFGHIJKLMN'[:len(choices)]
 lines=['@echo off','setlocal','cd /d "%~dp0"','echo One Piece alpha.13 - lot04 - laboratoire prive']
 if len(choices)>1:
  lines+=['echo Changer de fruit reconstruit la scene de laboratoire.']+['echo '+keys[i]+' : '+f.replace('_',' ') for i,f in enumerate(choices)]+['choice /C '+keys+' /N /M "Choisir un fruit : "']
 lines+=['set "fruit='+choices[0]+'"']
 if len(choices)>1:lines+=['if errorlevel '+str(i+1)+' set "fruit='+f+'"' for i,f in enumerate(choices) if i]
 lines+=['echo R technique / B suivante / F5 camera. Maj = recul Aro.','echo Mini Hollow : attendre la recharge, B deux fois pour Rap puis R.','echo Les blocs modifies restent definitifs dans ce monde de test.','"C:/Users/Anwender/AppData/Local/Programs/Python/Python312/python.exe" "%~dp0tools\\launch_visual_lab.py" --run --resume --lab "'+str(a.profile.resolve())+'" --fruit "%fruit%"','pause','endlocal']
 (P/name).write_bytes(('\r\n'.join(lines)+'\r\n').encode('ascii'))
for name,choices in [('Tester-Mera.cmd',fruits[:1]),('Tester-Magu.cmd',fruits[1:2]),('Tester-Goro.cmd',fruits[2:3]),('Tester-Lot-01.cmd',fruits[3:8]),('Tester-Lot-02.cmd',fruits[8:13]),('Tester-Lot-03.cmd',fruits[13:18]),('Tester-Lot-04.cmd',fruits[18:]),('Tester-OnePiece.cmd',fruits),('Tester-Terrain.cmd',fruits)]:launcher(name,choices)
manifest={'version':b['version'],'sha256':b['sha256'],'native_report':str(a.report.resolve()),'native_sha256':original['mods']['opworldlike']['sha256'],'retake_report':str(a.retake.resolve()),'retake_sha256':retake['mods']['opworldlike']['sha256'],'original_run_status':original['status'],'original_failures_retained':sorted(failures),'retake_run_status':retake['status'],'coverage_only':True,'direct_final_package_checks':len(retake['checks'])+len(retake['batch04_checks']),'history_report':str(a.history.resolve()),'history_sha256':history['mods']['opworldlike']['sha256'],'history_changes':history_changes,'native_scope_sources':{'checks':str(a.retake.resolve()),'batch03_checks':str(a.report.resolve()),'batch04_checks':str(a.retake.resolve()),'historical_checks':str(a.history.resolve()),'permanent_checks':str(a.history.resolve())},'checks':counts,'total_server_checks':sum(counts.values()),'native_test_tick_rate':d['native_test_tick_rate'],'historical_tick_rate':20,'diagnostic_or_renderer_changes_since_native_run':changed,'graphical_lab':str(a.visual.resolve()),'graphical_sha256':v['build_sha256'],'inspected_sheets':review['inspected_sheets'],'manual_profile':str(a.profile.resolve()),'manual_profile_launched':False,'launcher_backup':str(backup),'normal_installation_changed':False,'human_art_accepted':False}
(P/'build/b04-alpha13-verification.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
header=f'''# Livraison actuelle — lot04 alpha.13

Aro (4), Horo (5), Mero (4), Kiro (4) et Bisu (4) : 21 techniques, 42 phases d'animation distinctes et 21 décisions terrain. Avec les lots précédents,116 techniques sur 23 fruits sont traités, plus Amaru ; les 40 fruits ne sont pas déclarés terminés.

**{sum(counts.values())} contrôles serveur couverts PASS par trois recettes complémentaires** :59 du socle, 418 du lot03, 365 du lot04, 307 historiques et 31 de persistance, loot et redémarrage compris. Mécaniques à 80 ticks/s et historiques à 20. Ces résultats ne constituent pas une mesure de performances ou une validation artistique. Paquet `{b['sha256']}`. La première recette conserve un échec de mur contournable par Kumacy et cinq anciens tests synchrones incompatibles avec les nouvelles charges/prérequis. Seuls ses 418 contrôles PASS du lot03 sont retenus. La recette finale exécute directement les 365 contrôles du lot04 et 59 du socle ; la recette historique corrigée apporte 307 contrôles et 31 de persistance. Les classes de mécanique du lot03 restent identiques ; les écarts sont circonscrits aux ajouts visuels/réseau du lot04, au nom Aro et aux fixtures. La recette historique diffère du paquet final uniquement par la traînée B04 et trois classes de rendu/décor. Aucun résultat échoué n'est effacé. Les anciennes preuves des autres lots restent distinctes.

Session graphique groupée à deux clients :105 captures acteur et 105 observateur, 46 phases PAL évaluées par rôle (42 distinctes et deux gestes de suivi),56 contrôles de rendu/déplacement/expiration,10 planches inspectées. Acceptation artistique humaine en attente. Adaptations et limites : [bilan](B04-VALIDATION-2026-10-16.md).

Tester-Lot-04 propose les cinq fruits ; Tester-OnePiece et Tester-Terrain en proposent 23. Profil manuel privé neuf `{a.profile.name}`, non lancé à la livraison. Anciens profils/lanceurs conservés. Toute mutation de blocs reste définitive ; les contrôles et invocations restent temporaires. Aucun mod/monde habituel ni VOID modifié.'''
for rel in ['docs/HANDOFF.md','docs/MIGRATION_STATUS.md']:prepend(P/rel,header)
prepend(P/'MODLOG.md',header.replace('(B04-VALIDATION-2026-10-16.md)','(docs/B04-VALIDATION-2026-10-16.md)'))
(P/'LIRE-LOT-04.md').write_text('''# Tester le lot04

Ouvrir `Tester-Lot-04.cmd` et choisir Aro, Horo, Mero, Kiro ou Bisu. `Tester-OnePiece.cmd` propose les 23 fruits traités. Chaque lanceur ouvre un monde privé de test ; la progression habituelle n'est pas importée.

R lance la technique, B sélectionne la suivante, F5 change la caméra. Aro Passive recule avec Maj. Mini Hollow crée trois fantômes ; après la recharge, B deux fois sélectionne Rap, puis R les fait exploser. Rap vide ne coûte rien. Weight Manipulation parcourt les poids ; le serveur affiche le poids confirmé. Les biscuits sont comestibles.

Reprendre le même fruit conserve le monde sauvegardé. Changer de fruit reconstruit explicitement le décor du laboratoire ; cette commande de test efface les modifications de la scène. Les pouvoirs eux-mêmes ne restaurent jamais leurs cratères/blocs. Les effets sur entités ont une échéance.

Les tests automatiques et captures sont consignés dans [le bilan](docs/B04-VALIDATION-2026-10-16.md). Le ressenti et l'aspect final restent à apprécier en jeu.
''',encoding='utf-8')
print('RELEASED_PRIVATE_B04',b['version'],sum(counts.values()),a.profile.name)
