import base64
import json
from pathlib import Path
import struct
import sys
import unittest
from unittest.mock import patch, Mock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import vd_core as core


class MissionToolsTest(unittest.TestCase):
    def valid(self, **updates):
        spec = dict(request_id='test-1', item='diamond', amount=10, zone_min=[1,64,1], zone_max=[3,64,1],
                    destination=[8,64,1], origin=[0,64,0])
        spec.update(updates)
        return spec

    def test_gather_uses_single_encoded_argument_and_structured_reply(self):
        with patch.object(core, 'rcon', return_value=['VDMISSION {"ok":true,"mission":{"id":"one"}}']) as transport:
            result = core.servitor_gather(**self.valid())
        self.assertTrue(result['ok'])
        command = transport.call_args.args[0]
        prefix = 'execute in minecraft:overworld run work mission submit '
        self.assertTrue(command.startswith(prefix))
        encoded = command[len(prefix):]
        decoded = json.loads(base64.urlsafe_b64decode(encoded+'='*((-len(encoded))%4)))
        self.assertEqual(decoded['item'], 'minecraft:diamond')
        self.assertEqual(decoded['amount'], 10)
        self.assertNotIn(' ', encoded)

    def test_invalid_inputs_never_touch_server(self):
        for changes in [dict(amount=True), dict(amount=1.5), dict(amount=0), dict(amount=4097),
                        dict(zone_min=[True,64,1]), dict(zone_max=[1,2]), dict(item='diamond\nstop'),
                        dict(request_id='x run stop'), dict(dimension='overworld run stop'),
                        dict(destination=[2,64,1]), dict(zone_max=[29999984,2047,29999984])]:
            with self.subTest(changes=changes), patch.object(core, 'rcon') as transport:
                result = core.servitor_gather(**self.valid(**changes))
                self.assertEqual(result['error'], 'INVALID_ARGUMENT')
                transport.assert_not_called()

    def test_reversed_corners_normalized(self):
        with patch.object(core, '_mission_call', return_value={'ok':True}) as transport:
            core.servitor_gather(**self.valid(zone_min=[3,64,1], zone_max=[1,64,1]))
        encoded = transport.call_args.args[0].split(' ',1)[1]
        spec = json.loads(base64.urlsafe_b64decode(encoded+'='*((-len(encoded))%4)))
        self.assertEqual(spec['min'], [1,64,1])

    def test_excavation_requires_valid_explicit_mode(self):
        with patch.object(core,'rcon') as transport:
            self.assertEqual(core.servitor_gather(**self.valid(access='anywhere'))['error'],'INVALID_ARGUMENT')
            transport.assert_not_called()
        with patch.object(core,'_mission_call',return_value={'ok':True}) as transport:
            core.servitor_gather(**self.valid(access='excavate'))
        encoded=transport.call_args.args[0].split(' ',1)[1]
        self.assertEqual(json.loads(base64.urlsafe_b64decode(encoded+'='*((-len(encoded))%4)))['access'],'excavate')

    def test_build_and_estimate_use_typed_encoded_requests(self):
        arguments=dict(blueprint='platform_3x3_v1',material='minecraft:cobblestone',anchor=[20,64,20],source=[15,64,20],origin=[16,64,20])
        with patch.object(core,'_mission_call',return_value={'ok':True}) as transport:
            core.servitor_build_estimate(**arguments)
            self.assertTrue(transport.call_args.args[0].startswith('estimate '))
            core.servitor_build(request_id='build-one',**arguments)
        encoded=transport.call_args.args[0].split(' ',1)[1]
        payload=json.loads(base64.urlsafe_b64decode(encoded+'='*((-len(encoded))%4)))
        self.assertEqual(payload['type'],'BUILD');self.assertEqual(payload['request_id'],'build-one')
        for changes in [dict(blueprint='house run stop'),dict(material='minecraft:tnt'),dict(anchor=[True,64,20]),dict(dimension='overworld run stop')]:
            with patch.object(core,'rcon') as network:
                self.assertEqual(core.servitor_build(request_id='build-one',**{**arguments,**changes})['error'],'INVALID_ARGUMENT')
                network.assert_not_called()

    def test_bad_uuid_or_action_never_touch_server(self):
        with patch.object(core, 'rcon') as transport:
            self.assertEqual(core.servitor_mission_status('current run stop')['error'], 'INVALID_ARGUMENT')
            self.assertEqual(core.servitor_mission_control('00000000-0000-0000-0000-000000000001','op')['error'], 'INVALID_ARGUMENT')
            transport.assert_not_called()

    def test_cancelled_carry_can_be_recovered_with_typed_action(self):
        with patch.object(core, '_mission_call', return_value={'ok':True}) as transport:
            core.servitor_mission_control('00000000-0000-0000-0000-000000000001','deposit')
        self.assertIn(' deposit', transport.call_args.args[0])

    def test_missing_json_marker_is_not_a_success(self):
        with patch.object(core, 'rcon', return_value=['Unknown command']):
            self.assertEqual(core.servitor_capabilities()['error'], 'PROTOCOL_ERROR')

    def test_invalid_reply_is_not_a_success(self):
        with patch.object(core, 'rcon', return_value=['VDMISSION {"ok":"yes"}']):
            self.assertEqual(core.servitor_capabilities()['error'], 'PROTOCOL_ERROR')

    def test_offline_server_is_structured_failure(self):
        with patch.object(core, 'rcon', side_effect=RuntimeError('offline')):
            self.assertEqual(core.servitor_capabilities()['error'], 'SERVER_UNAVAILABLE')


class RconCorrelationTest(unittest.TestCase):
    def test_marker_is_not_pipelined_before_first_reply(self):
        sock=Mock()
        def receive():
            count=sock.sendall.call_count
            if count==1:return (1,2,'')
            if count==2:return (2,0,'response')
            if count==3:return (3,0,'sentinel')
            self.fail('unexpected request sequence')
        with patch.object(core,'_props',return_value=('fixture',1234)),patch.object(core.socket,'create_connection',return_value=sock),patch.object(core.Rcon,'_recv',side_effect=receive):
            connection=core.Rcon(timeout=2)
            self.assertEqual(connection.cmd('list'),'response')
            connection.close()

    def test_auth_empty_packet_and_stale_ids_are_not_command_results(self):
        sock = Mock()
        with patch.object(core, '_props', return_value=('fixture', 1234)), patch.object(core.socket, 'create_connection', return_value=sock), \
                patch.object(core.Rcon, '_recv', side_effect=[(1,0,''),(1,2,''),(1,0,'stale'),(2,0,'actual'),(3,0,'sentinel')]):
            connection = core.Rcon()
            self.assertEqual(connection.cmd('list'), 'actual')
            connection.close()
        self.assertEqual(sock.sendall.call_count, 3)

    def test_fragments_are_joined_until_correlated_sentinel(self):
        sock=Mock()
        with patch.object(core,'_props',return_value=('fixture',1234)),patch.object(core.socket,'create_connection',return_value=sock),patch.object(core.Rcon,'_recv',side_effect=[(1,2,''),(2,0,'VDMISSION {"ok":'),(1,0,'stale'),(2,0,'true}'),(3,0,'players')]):
            connection=core.Rcon(timeout=2)
            self.assertEqual(connection.cmd('work mission capabilities'),'VDMISSION {"ok":true}')
            connection.close()

    def test_missing_end_marker_never_returns_partial_json(self):
        sock=Mock()
        with patch.object(core,'_props',return_value=('fixture',1234)),patch.object(core.socket,'create_connection',return_value=sock),patch.object(core.Rcon,'_recv',side_effect=[(1,2,''),(2,0,'VDMISSION {"ok":true}'),RuntimeError('closed')]):
            connection=core.Rcon(timeout=2)
            with self.assertRaises(RuntimeError):connection.cmd('work mission capabilities')
            connection.close()

    def test_auth_rejection_closes_socket(self):
        sock = Mock()
        with patch.object(core, '_props', return_value=('fixture',1234)), patch.object(core.socket, 'create_connection', return_value=sock), \
                patch.object(core.Rcon, '_recv', return_value=(-1,2,'')):
            with self.assertRaises(RuntimeError): core.Rcon()
        sock.close.assert_called_once()

    def test_oversized_packet_rejected_before_body_read(self):
        connection = object.__new__(core.Rcon)
        with patch.object(connection, '_recvn', return_value=struct.pack('<i',2*1024*1024)) as receive:
            with self.assertRaises(RuntimeError): connection._recv()
        receive.assert_called_once_with(4)


if __name__ == '__main__':
    unittest.main()
