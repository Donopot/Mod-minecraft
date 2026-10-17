from pathlib import Path
import json,sys
from run_local_tests import Rcon
P=Path(__file__).resolve().parents[1];lab=Path(sys.argv[1]).resolve();assert lab.parent==(P.parent/'.runtime/onepiece-lab').resolve();m=json.loads((lab/'report.local.json').read_text());v=json.loads((lab/'visual-lab.local.json').read_text());assert v['capture'] and v['scenario']=='batch07' and v['launched'] and not v.get('stopped')
settings=dict(l.split('=',1) for l in (lab/'server.properties').read_text().splitlines() if '=' in l)
r=Rcon(m['rcon_port'],settings['rcon.password']);players=r.cmd('list');assert 'FruitLab' in players and 'FruitWitness' in players;print(r.cmd('op FruitWitness'));r.sock.close()
