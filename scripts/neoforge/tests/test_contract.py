import importlib.util
import json
from pathlib import Path
import unittest

MODULE = Path(__file__).resolve().parents[1]/'verify_contract.py'
spec = importlib.util.spec_from_file_location('verify_contract', MODULE)
checker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(checker)

class ContractRegressionTest(unittest.TestCase):
    def setUp(self): self.contract = json.loads(checker.CONTRACT.read_text(encoding='utf-8'))
    def test_reference_is_consistent(self): self.assertEqual(checker.validate(self.contract),88)
    def test_missing_map_item_is_rejected(self):
        self.contract['runtime_checks']['item'].remove('voiddynasty:laser_drill')
        with self.assertRaises(ValueError): checker.validate(self.contract)
    def test_furnace_must_keep_vanilla_be_id(self):
        self.contract['block_entities'][3]['type']='voiddynasty:promethium_furnace'
        with self.assertRaises(ValueError): checker.validate(self.contract)
    def test_air_substitution_cannot_enter_contract(self):
        self.contract['map_required']['block'][0]='minecraft:air'
        with self.assertRaises(ValueError): checker.validate(self.contract)
    def test_slot_casing_is_significant(self):
        self.contract['nbt_profiles']['inventory']['item_keys'][0]='slot'
        with self.assertRaises(ValueError): checker.validate(self.contract)
    def test_paste_cannot_be_enabled_early(self):
        self.contract['import_policy']['worldedit_paste_allowed']=True
        with self.assertRaises(ValueError): checker.validate(self.contract)
    def test_duplicate_identifier_is_rejected(self):
        self.contract['runtime_checks']['item'].append('voiddynasty:laser_drill')
        with self.assertRaises(ValueError): checker.validate(self.contract)

if __name__=='__main__': unittest.main()
