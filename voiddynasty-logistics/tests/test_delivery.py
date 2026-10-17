"""Actual live ledger/transport and world journal, without claiming engine tests."""
import json
import os
from pathlib import Path
import socket
import subprocess
import tempfile
import unittest
import uuid
from void_bridge.protocol import digest, MISSION

ROOT = Path(__file__).resolve().parents[1]
CATALOG=json.loads((ROOT/'protocol/cargo-missions.v1.json').read_text(encoding='utf-8'))
HASHES={m['mission_id']:digest(dict(campaign_id='00000000-0000-4000-8000-000000000002',world_id='00000000-0000-4000-8000-000000000001',pair_id='00000000-0000-4000-8000-000000000003',cargo=[dict(item_id=m['item_id'],count=m['count'])],mission_id=m['mission_id'],mission_version=3,native_cargo_blueprint=m['cargo_blueprint_id'],previous_delivery_id='',sequence=1)) for m in CATALOG['missions']}
FINGERPRINT=HASHES['logistics_first_delivery']

class DeliveryBuildTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not (ROOT/'.runtime/rt-build.local.json').is_file() or not (ROOT/'.runtime/fabric-build/build.local.json').is_file():
            raise unittest.SkipTest('Build both game adapters first to enable compiled delivery tests')
        rt=json.loads((ROOT/'.runtime/rt-build.local.json').read_text(encoding='utf-8-sig'))
        java=json.loads((ROOT/'.runtime/fabric-build/build.local.json').read_text())
        cls.work=ROOT/'.runtime/delivery-tests';cls.work.mkdir(exist_ok=True)
        shared=[str(ROOT/'rt-adapter'/name) for name in ['PerSaveState.cs','DeliveryLedger.cs','DiagnosticJson.cs','DiagnosticServer.cs','DeliveryServer.cs','DeliveryAvailability.cs','CargoCatalog.cs','NativeCargoArchive.cs']]
        for name in ['DeliveryLedgerHarness','DeliveryServerHarness']:
            result=subprocess.run([rt['compiler'],'/nologo','/out:'+str(cls.work/(name+'.exe')),'/reference:System.Runtime.Serialization.dll','/reference:System.Xml.dll','/reference:System.IO.Compression.dll','/reference:System.IO.Compression.FileSystem.dll',*shared,str(ROOT/'tests'/(name+'.cs'))],capture_output=True,text=True)
            if result.returncode:raise RuntimeError(result.stdout+result.stderr)
        cls.java=str(Path(java['jdk'])/'bin/java.exe')
        cls.cp=';'.join([str(cls.work),java['classes'],*java['classpath']])
        result=subprocess.run([str(Path(java['jdk'])/'bin/javac.exe'),'--release','25','-proc:none','-classpath',cls.cp,'-d',str(cls.work),str(ROOT/'tests/DeliveryJavaHarness.java')],capture_output=True,text=True)
        if result.returncode:raise RuntimeError(result.stdout+result.stderr)

    def test_real_csharp_ledger_dedup_and_rollback(self):
        result=subprocess.run([str(self.work/'DeliveryLedgerHarness.exe')],capture_output=True,text=True,timeout=5)
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)
        self.assertIn('replayed commits',result.stdout)
        self.assertIn(FINGERPRINT,result.stdout)
        for key,value in HASHES.items():self.assertIn("HASH "+key+" "+value,result.stdout)

    def test_existing_real_receipt_migrates_without_reward_or_identity_changes(self):
        archive=ROOT/'.runtime/evidence/first-delivery/rt-save.zks'
        if not archive.is_file():self.skipTest('Private real save fixture not present')
        result=subprocess.run([str(self.work/'DeliveryLedgerHarness.exe'),str(archive)],capture_output=True,text=True,timeout=5)
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)
        self.assertIn('reserve=10, completed=1',result.stdout)

    def test_native_cargo_archive_checks_entity_and_blueprint(self):
        import zipfile
        cid='0'*31+'1';blueprint=CATALOG['missions'][0]['cargo_blueprint_id']
        with tempfile.TemporaryDirectory() as directory:
            p=Path(directory)/'cargo.zks'
            def check(rows):
                with zipfile.ZipFile(p,'w') as z:z.writestr('player.json',json.dumps({'CargoState':{'$id':'1','m_CargoEntities':rows}}))
                result=subprocess.run([str(self.work/'DeliveryLedgerHarness.exe'),'archive',str(p),cid,blueprint],capture_output=True,text=True,timeout=5)
                self.assertEqual(result.returncode,0,result.stderr)
                return result.stdout.strip()
            row={'UniqueId':cid,'Blueprint':blueprint,'Parts':{'nested':'ignored'}}
            self.assertEqual(check([row]),'FOUND')
            self.assertEqual(check([row,row]),'ABSENT')
            self.assertEqual(check([{**row,'Blueprint':'a'*32}]),'ABSENT')
            self.assertEqual(check([]),'ABSENT')

    def test_native_cargo_reader_accepts_actual_game_archive(self):
        import zipfile
        archive=ROOT/'.runtime/evidence/first-delivery/rt-save.zks'
        if not archive.is_file():self.skipTest('Private game fixture not present')
        with zipfile.ZipFile(archive) as z:
            entity=json.loads(z.read('player.json'))['CargoState']['m_CargoEntities'][0]
        result=subprocess.run([str(self.work/'DeliveryLedgerHarness.exe'),'archive',str(archive),entity['UniqueId'],entity['Blueprint']],capture_output=True,text=True,timeout=5)
        self.assertEqual(result.returncode,0,result.stderr)
        self.assertEqual(result.stdout.strip(),'FOUND')

    def test_live_server_withholds_ack_until_main_thread_finishes(self):
        with socket.socket() as probe:probe.bind(('127.0.0.1',0));port=probe.getsockname()[1]
        process=subprocess.Popen([str(self.work/'DeliveryServerHarness.exe'),'a'*64,str(port)],stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
        try:
            self.assertEqual(process.stdout.readline().strip(),'READY')
            request=dict(protocol_version=4,type='commit',message_id=str(uuid.uuid4()),token='a'*64,session_id='',campaign_id='',world_id='',pair_id='',checkpoint_id='',delivery_id='',known_state='RESERVED',mission_hash='',payload_hash='',item_id='minecraft:iron_ingot',count=64,completed_count=0,last_receipt_id='',mission_id='logistics_first_delivery')
            with socket.create_connection(('127.0.0.1',port),timeout=3) as connection:
                connection.sendall((json.dumps(request)+'\n').encode())
                process.stdin.write('TAKE\n');process.stdin.flush()
                self.assertEqual(process.stdout.readline().strip(),'PENDING commit')
                connection.settimeout(.15)
                with self.assertRaises(socket.timeout):connection.recv(1)
                process.stdin.write('DURABLE\n');process.stdin.flush()
                self.assertEqual(process.stdout.readline().strip(),'ACK')
                connection.settimeout(3)
                reply=json.loads(connection.makefile('rb').readline())
                self.assertEqual(reply['reply_to'],request['message_id'])
                with tempfile.TemporaryDirectory() as directory:
                    response=Path(directory)/'reply.json';response.write_text(json.dumps(reply,separators=(',',':')))
                    result=subprocess.run([self.java,'-classpath',self.cp,'com.donopot.voiddynastybridge.DeliveryJavaHarness',str(Path(directory)/'world.json'),str(response)],capture_output=True,text=True,timeout=8)
                    self.assertEqual(result.returncode,0,result.stdout+result.stderr)
                    self.assertIn(FINGERPRINT,result.stdout)
                    for key,value in HASHES.items():self.assertIn('HASH '+key+' '+value,result.stdout)
            # Authentication and duplicate keys are rejected before any job reaches Unity.
            for invalid in [json.dumps({**request,'token':'b'*64}),json.dumps(request)[:-1]+',"count":65}']:
                with socket.create_connection(('127.0.0.1',port),timeout=3) as c:
                    c.sendall((invalid+'\n').encode())
                    try:self.assertEqual(c.recv(1),b'')
                    except ConnectionResetError:pass
        finally:
            process.stdin.write('STOP\n');process.stdin.flush()
            out,error=process.communicate(timeout=5)
            self.assertEqual(process.returncode,0,error)
