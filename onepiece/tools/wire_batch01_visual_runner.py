from pathlib import Path
p=Path(__file__).resolve().parent/'launch_visual_lab.py';s=p.read_text(encoding='utf-8')
s=s.replace("('mera','magu','goro','amaru')","('mera','magu','goro','amaru','batch01')").replace("['foundation','mera','magu','goro','amaru']","['foundation','mera','magu','goro','amaru','batch01']")
needle='            deadline = time.monotonic() + 120\n            while game.poll()'
assert needle in s;s=s.replace(needle,"            deadline = time.monotonic() + (260 if frozen.get('scenario')=='batch01' else 120)\n            while game.poll()")
needle="            if frozen.get('capture') and frozen.get('scenario')=='amaru':"
assert needle in s
s=s.replace(needle,'''            if frozen.get('capture') and frozen.get('scenario')=='batch01':
                catalogue={'hie_hie':['icepartisan','pheasantbeak','icespikes','iceball','iceage'],'pika_pika':['laser1','laser2','kick','yatanokagami','magatama','amanomurakumo','speedispower','passive'],'moku_moku':['whiteblow','whitevine','whitelauncher','whiteout'],'suna_suna':['desertspada','barjan','sables','desertlaspada','grounddeath'],'yami_yami':['kurouzu','blackhole','darkball','darkmatter']}
                required={f'{fruit}/{move}-render' for fruit,ids in catalogue.items() for move in ids}
                expected_pal={f'b01_{move}{suffix}' for ids in catalogue.values() for move in ids for suffix in ['', '_release'] if suffix!='_release' or move not in ['passive','yatanokagami']}
                for who,directory in [('actor',client),('observer',Path(frozen['observer']))]:
                    shots=sorted((directory/'screenshots').glob(who+'-*.png'))
                    frozen[who+'_images']=[str(p) for p in shots]
                    if len(shots)!=104:raise RuntimeError(f'B01 {who}: expected104 captures, got{len(shots)}')
                    body=(directory/'process.log').read_text(encoding='utf-8',errors='replace')
                    rows=re.findall(r'\\[batch01-client-check\\] (\\S+) (PASS|FAIL) (.*)',body)
                    checks=required | ({'native-sword'} | {m+'-movement' for m in ['kick','yatanokagami','speedispower','passive','whitelauncher','barjan']} if who=='actor' else set())
                    frozen[who+'_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in rows]
                    if len(rows)!=len(checks) or {cid for cid,state,_ in rows if state=='PASS'}!=checks or any(state=='FAIL' for _,state,_ in rows):raise RuntimeError('B01 failed/missing live checks on '+who)
                    poses=re.findall(r'\\[batch01-pal\\] (\\S+) (PASS|FAIL)',body)
                    frozen[who+'_pal_checks']=[{'id':cid,'passed':state=='PASS'} for cid,state in poses]
                    if len(poses)!=50 or {cid for cid,state in poses if state=='PASS'}!=expected_pal or any(state=='FAIL' for _,state in poses):raise RuntimeError('B01 failed/missing evaluated PAL poses on '+who)
                native=re.findall(r'\\[batch01-native\\] (\\S+) (PASS|FAIL) (.*)',(lab/'logs/latest.log').read_text(encoding='utf-8',errors='replace'))
                frozen['native_player_checks']=[{'id':cid,'passed':state=='PASS','detail':detail} for cid,state,detail in native]
                if len(native)!=2 or {cid for cid,state,_ in native if state=='PASS'}!={'absorption','capacity'}:raise RuntimeError('Native player damage/absorption checks failed')
                frozen['rendered']=True
                print('BATCH01_CAPTURED 104 actor+104 observer; 50 evaluated gestures per client. Image review required.',flush=True)
            elif frozen.get('capture') and frozen.get('scenario')=='amaru':''')
p.write_text(s,encoding='utf-8')
