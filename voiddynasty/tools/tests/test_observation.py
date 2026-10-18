import asyncio
import base64
import json
from pathlib import Path
import sys
import unittest
from unittest.mock import patch
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
import vd_core as core

MISSION='00000000-0000-0000-0000-000000000001'
def status(state='PREPARING',**extra):return {'ok':True,'mission':{'id':MISSION,'status':state},**extra}

class ObservationTest(unittest.TestCase):
    def test_find_encodes_exact_source_query(self):
        with patch.object(core,'_mission_call',return_value={'ok':True}) as transport:
            core.servitor_find('diamond',[5,64,3],[1,62,2],limit=4)
        tail,dimension=transport.call_args.args
        self.assertEqual(dimension,'overworld');self.assertTrue(tail.startswith('observe '))
        token=tail.split(' ')[1];payload=json.loads(base64.urlsafe_b64decode(token+'='*(-len(token)%4)))
        self.assertEqual(payload,{'schema':1,'type':'FIND','min':[1,62,2],'max':[5,64,3],'limit':4,'item':'minecraft:diamond'})

    def test_invalid_queries_never_reach_rcon(self):
        for changes in [{'limit':True},{'limit':0},{'limit':65},{'zone_min':[True,0,0]},{'zone_max':[100,100,100]},{'dimension':'overworld run stop'}]:
            args=dict(zone_min=[0,0,0],zone_max=[1,1,1],limit=16,dimension='overworld');args.update(changes)
            with self.subTest(changes=changes),patch.object(core,'rcon') as network:
                self.assertEqual(core.servitor_observe(**args)['error'],'INVALID_ARGUMENT');network.assert_not_called()
        with patch.object(core,'rcon') as network:
            self.assertEqual(core.servitor_find('diamond run stop',[0,0,0],[1,1,1])['error'],'INVALID_ARGUMENT');network.assert_not_called()

class MissionWaitTest(unittest.IsolatedAsyncioTestCase):
    async def test_poll_keeps_uuid_and_does_not_submit_or_cancel(self):
        with patch.object(core,'_mission_call',return_value=status()) as call:
            result=await core.servitor_mission_wait(MISSION,0)
        self.assertEqual(result['wait_state'],'pending');self.assertEqual(call.call_count,1);self.assertEqual(call.call_args.args[:2],('status '+MISSION,'overworld'))

    async def test_wait_gets_observed_result(self):
        with patch.object(core,'_mission_call',side_effect=[status(),status('SUCCEEDED',worker_active=False)]):result=await core.servitor_mission_wait(MISSION,2)
        self.assertEqual(result['wait_state'],'settled')

    async def test_pause_and_rollback_are_not_success(self):
        for snapshot,wanted in [(status('PAUSED'),'paused'),(status('CANCELLED',rollback_state='RUNNING'),'pending'),(status('CANCELLED',worker_active=True),'pending'),(status('CANCELLED',rollback_state='FAILED'),'attention')]:
            with self.subTest(snapshot=snapshot),patch.object(core,'_mission_call',return_value=snapshot):result=await core.servitor_mission_wait(MISSION,0)
            self.assertEqual(result['wait_state'],wanted)

    async def test_server_loss_or_wrong_identity_is_unknown(self):
        for snapshot in [{'ok':False,'error':'SERVER_UNAVAILABLE'},{'ok':True,'mission':{'id':'wrong','status':'SUCCEEDED'}}]:
            with patch.object(core,'_mission_call',return_value=snapshot):result=await core.servitor_mission_wait(MISSION,0)
            self.assertFalse(result['ok']);self.assertEqual(result['wait_state'],'unknown')

    async def test_invalid_wait_does_not_touch_server(self):
        for identity,seconds in [('current',0),(MISSION,True),(MISSION,31),(MISSION,-1)]:
            with patch.object(core,'_mission_call') as network:
                result=await core.servitor_mission_wait(identity,seconds);self.assertEqual(result['error'],'INVALID_ARGUMENT');network.assert_not_called()

    async def test_cancelling_the_wait_never_cancels_the_mission(self):
        with patch.object(core,'_mission_call',return_value=status()) as call:
            task=asyncio.create_task(core.servitor_mission_wait(MISSION,30));await asyncio.sleep(.05);task.cancel()
            with self.assertRaises(asyncio.CancelledError):await task
        self.assertTrue(all(c.args[0]=='status '+MISSION for c in call.call_args_list))

if __name__=='__main__':unittest.main()
