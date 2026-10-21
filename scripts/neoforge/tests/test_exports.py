import gzip
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[2]/'world-migration/prepare_neoforge_exports.py'
spec=importlib.util.spec_from_file_location('exports',SCRIPT)
exports=importlib.util.module_from_spec(spec);spec.loader.exec_module(exports)

class ExportPreparationTest(unittest.TestCase):
    def setUp(self):self.rows=exports.zones(exports.DEFAULT_ZONES)
    def test_exclusive_bounds_become_inclusive_worldedit_selection(self):
        with tempfile.TemporaryDirectory() as temp:
            out=Path(temp);exports.prepare(self.rows,out)
            commands=(out/'hermes-batch-01.txt').read_text()
            self.assertIn('//pos1 -272,-64,-192',commands)
            self.assertIn('//pos2 303,319,175',commands)
            self.assertEqual(len(list(out.glob('hermes-batch-*.txt'))),9)
            manifest=json.loads((out/'export-manifest.json').read_text())
            self.assertEqual(len(manifest['zones']),173)
            self.assertEqual(manifest['zones'][0]['size'],[576,384,368])
            self.assertTrue(all(not z['exported'] for z in manifest['zones']))
    def test_missing_files_do_not_complete_p1(self):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp);report=root/'report.json'
            self.assertFalse(exports.verify(self.rows,root,report))
            result=json.loads(report.read_text())
            self.assertEqual(result['integrity_pass_count'],0)
            self.assertFalse(result['p1_pass'])
    def test_corrupt_gzip_is_reported(self):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp);(root/'zone_001.schem').write_bytes(b'not a gzip schematic')
            self.assertFalse(exports.verify(self.rows,root,root/'report.json'))
            result=json.loads((root/'report.json').read_text())
            self.assertFalse(result['zones'][0]['integrity_pass'])
            self.assertIn('error',result['zones'][0])
    def test_invalid_deflate_stream_is_reported_without_aborting_manifest(self):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp)
            (root/'zone_001.schem').write_bytes(bytes.fromhex('1f8b0800000000000003ff0000000000000000'))
            self.assertFalse(exports.verify(self.rows,root,root/'report.json'))
            result=json.loads((root/'report.json').read_text())
            self.assertEqual(len(result['zones']),173)
            self.assertFalse(result['zones'][0]['integrity_pass'])
            self.assertIn('error',result['zones'][0])
    def test_duplicate_zone_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'zones.csv'
            text=exports.DEFAULT_ZONES.read_text().replace('zone_173,','zone_172,')
            path.write_text(text)
            with self.assertRaises(ValueError):exports.zones(path)

if __name__=='__main__':unittest.main()
