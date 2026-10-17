"""Freeze B05 evidence without pretending complementary recipes ran on one artifact."""
from pathlib import Path
import json, hashlib, zipfile, argparse
P=Path(__file__).resolve().parents[1]; R=P.parent; L=R/'.runtime/onepiece-lab'
def read(p): return json.loads(p.read_text(encoding='utf-8'))
def write(p,d): p.write_text(json.dumps(d,indent=2,ensure_ascii=False),encoding='utf-8')
ap=argparse.ArgumentParser();ap.add_argument('--final-visual',type=Path,required=True);ap.add_argument('--history',type=Path,required=True);a=ap.parse_args()
b=read(P/'build/build-local.json');assert hashlib.sha256(Path(b['jar']).read_bytes()).hexdigest()==b['sha256']
ids=['564d02480c354a0c9289c1d59104d1ac','fc47cd0cd5a6454fb944780ab0883ec5','722ab492948e4d34ba294523d9619f5d','2c61990725c3454faf2660ff99b54704']
reports=[read(L/('run-'+i)/'report.local.json') for i in ids]
coverage={}
for i,r in zip(ids,reports):
 for c in r['batch05_checks']:
  if c['passed']:coverage[c['id']]={'report':str(L/('run-'+i)/'report.local.json'),'sha256':r['mods']['opworldlike']['sha256']}
assert len(coverage)==458,len(coverage)
h=read(a.history);assert h['status']=='passed' and len(h['historical_checks'])==307 and len(h['checks'])==59
assert all(c['passed'] for k in ['checks','historical_checks'] for c in h[k])
v=read(a.final_visual/'visual-lab.local.json');assert v['build_sha256']==b['sha256'] and v['rendered'] and v['stopped']
for who in ['actor','observer']:
 assert len(v[who+'_images'])==21 and len(v[who+'_pal_checks'])==14
 assert all(c['passed'] for k in [who+'_checks',who+'_pal_checks'] for c in v[k])
 sheets=read(P/'build/batch05-final-gomu-review/sheets.local.json')['contact_sheets']
 assert len(sheets)==2 and all(Path(s).is_file() for s in sheets)
inspection=read(P/'build/batch05-final-gomu-inspection.local.json');assert inspection['review_passed'] and inspection['inspected_by_agent']
retake=L/'run-009477397e664a6b8afe79e863e87085'; rv=read(retake/'visual-lab.local.json')
assert rv['rendered'] and rv['stopped'] and all(c['passed'] for who in ['actor','observer'] for k in [who+'_checks',who+'_pal_checks'] for c in rv[k])
prior=next((Path(rv['client'])/'mods').glob('opworldlike*.jar'))
with zipfile.ZipFile(prior) as old,zipfile.ZipFile(b['jar']) as final:
 changed=sorted(n for n in set(old.namelist())|set(final.namelist()) if n not in old.namelist() or n not in final.namelist() or old.read(n)!=final.read(n))
 assert changed==['net/mcreator/minepiece/command/RepairTestCommands.class'],changed
proof={'version':b['version'],'sha256':b['sha256'],'fruits_implemented':28,'techniques_implemented':143,'amaru':True,'batch05_covered_pass':458,'coverage_only':True,'coverage':coverage,'foundation_and_current_history':str(a.history.resolve()),'historical_checks':307,'foundation_checks':59,'permanent_checks':31,'permanent_report':str(L/'run-fc47cd0cd5a6454fb944780ab0883ec5/report.local.json'),'original_failures_retained':True,'exact_current_native_before_fixture_change':str(L/'run-2c61990725c3454faf2660ff99b54704/report.local.json'),'current_gameplay_native_scope':'161 B05 checks (Gomu/Imu plus core/lifecycle for all five); other scopes retain their own hashes','graphical_first_failed_review':str(P/'build/batch05-first-inspection.local.json'),'graphical_retake':str(retake),'graphical_final_gomu':str(a.final_visual.resolve()),'final_delta_from_retake':changed,'inspections':[str(P/'build/batch05-retake-inspection.local.json'),str(P/'build/batch05-final-gomu-inspection.local.json')],'human_art_accepted':False,'normal_installation_changed':False}
write(P/'build/b05-alpha14-verification.local.json',proof)
header=f'''# Lot05 vérifié en laboratoire — 17 octobre 2026

Kage, Soru, Uta, Imu et Gomu : 27 techniques, 54 phases PAL distinctes et finies. Cela porte le travail à **28 fruits et 143 techniques**, plus Amaru. Il reste 12 Zoans ; les 40 fruits ne sont pas déclarés terminés.

Paquet alpha.14 `{b['sha256']}`. 458 contrôles B05 couverts par plusieurs recettes complémentaires, 59 du socle, 307 historiques et 31 de permanence. Ce total de 855 est une couverture multi-paquets, pas une exécution unique sur le paquet final. Le dernier contrôle natif ciblé couvre directement Gomu/Imu et le socle/cycle de vie des cinq fruits ; la dernière modification ne déplace qu'une cible du décor graphique. Les erreurs des premières recettes et la première revue graphique échouée sont conservées avec leurs corrections dans le manifeste privé.

Captures groupées acteur/observateur : 136+136 au premier passage, 106+106 après corrections, puis 21+21 pour dégager la caméra de face des Gears. Planches effectivement inspectées, poses évaluées et erreurs de ressources contrôlées. Acceptation artistique humaine en attente. Les cratères peuvent rapprocher la caméra troisième personne vanilla. La correction équivalente d'alignement des anciens bras Mochi reste dans la liste de finition.

Destruction et transformation de blocs définitives ; effets sur les entités bornés. Aucun monde/mod habituel ni VOID modifié. Preuves : `build/b05-alpha14-verification.local.json`. Le développement continue avec Loup, Léopard, Tigre à dents de sabre, Girafe et Batto.'''
(P/'docs/B05-VALIDATION-2026-10-17.md').write_text(header,encoding='utf-8')
for rel in ['docs/HANDOFF.md','docs/MIGRATION_STATUS.md','MODLOG.md']:
 p=P/rel;p.write_text(header+'\n\n---\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')
for fruit in ['KAGE_KAGE','SORU_SORU','UTA_UTA','IMU','GOMU_GOMU']:
 p=P/'docs/fruits'/f'{fruit}.md'
 if p.exists():p.write_text('# État lot05 : implémenté, testé serveur et vu en laboratoire\n\nVoir [validation](../B05-VALIDATION-2026-10-17.md) et [adaptations](../BATCH-05.md). Acceptation artistique humaine en attente ; les affirmations historiques ci-dessous sont à lire avec les corrections de source du lot05.\n\n---\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')
p=P/'AGENTS.md';text=p.read_text(encoding='utf-8');p.write_text('# Current handoff 2026-10-17\n\nB05 frozen alpha.14:28fruits/143techniques+Amaru. Proof build/b05-alpha14-verification.local.json; docs/B05-VALIDATION-2026-10-17.md. 458 B05 covered across scoped recipes,855 incl foundation/history/permanence, never claim one exact-hash full run. All owned labs stopped. Final Gomu fixture moves creepers to26 to clear front camera. Continue ALL40; next B06 Wolf/Leopard/Sabertiger/Giraffe/Batto31moves. Remaining12Zoans/70moves. Full beast forms required. Backlog oldMochi floatingarm + nativeGecko stone + thirdperson cratercamera. Root alone edits; attempted3 B06 read-only layer audits hit usage, so root audits source. Human acceptance pending. No normal installation changes. Prior paragraphs below are historical.\n\n'+text,encoding='utf-8')
backup=R/'.runtime/onepiece/after-b05-alpha14';backup.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(backup/'source.zip','w',zipfile.ZIP_DEFLATED) as z:
 for sub in ['src','docs','tools']:
  for p in (P/sub).rglob('*'):
   if p.is_file() and '__pycache__' not in p.parts:z.write(p,p.relative_to(P))
 for sub in ['AGENTS.md','MODLOG.md','gradle.properties']:z.write(P/sub,sub)
import shutil
shutil.copy2(b['jar'],backup/Path(b['jar']).name);shutil.copy2(P/'build/b05-alpha14-verification.local.json',backup/'verification.local.json')
print('FROZEN_PRIVATE_B05',b['sha256'],'28fruits/143techniques; continue B06')
