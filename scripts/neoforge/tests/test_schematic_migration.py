"""Synthetic regression worlds only. No production world or server access."""
import copy
import gzip
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

SCRIPTS = Path(__file__).resolve().parents[2] / 'world-migration'
sys.path.insert(0, str(SCRIPTS))
from schematic_nbt import Tag, decode, encode, load, payload, plain, validate, varints
import extract_zones as extract
import remap_schem as remap


def compound(**fields):
    return Tag(10, fields)


def fixture():
    inventory = Tag(9, (10, [compound(Slot=Tag(1,-1), id=Tag(8,'voiddynasty:laser_drill'),
                                      count=Tag(3,1), components=compound(ammo=Tag(3,7)))]))
    be = compound(Id=Tag(8,'voiddynasty:super_container'), Pos=Tag(11,[0,0,0]),
                  Data=compound(Items=inventory, CustomName=Tag(8,'coffre\0😀'),
                                seed=Tag(4,123), progress=Tag(5,0.25)))
    entity = compound(Id=Tag(8,'voiddynasty:servitor_2'),
                      Pos=Tag(9,(6,[Tag(6,0.5),Tag(6,0.0),Tag(6,0.5)])),
                      Data=compound(UUID=Tag(11,[-1,2,3,4]), Tags=Tag(9,(8,[Tag(8,'brain')]))))
    return compound(Schematic=compound(
        Version=Tag(3,3), DataVersion=Tag(3,5023), Width=Tag(2,2), Height=Tag(2,1), Length=Tag(2,1),
        Offset=Tag(11,[10,-64,20]), Metadata=compound(Name=Tag(8,'fixture')),
        Blocks=compound(Palette=compound(**{'voiddynasty:super_container':Tag(3,0),
                                           'minecraft:iron_chain[axis=y]':Tag(3,1)}),
                        Data=Tag(7,b'\0\1'), BlockEntities=Tag(9,(10,[be]))),
        Biomes=compound(Palette=compound(**{'minecraft:plains':Tag(3,0)}), Data=Tag(7,b'\0\0')),
        Entities=Tag(9,(10,[entity]))))


def write_fixture(path, root):
    path.write_bytes(gzip.compress(encode('',root)))


class SchematicMigrationTest(unittest.TestCase):
    def test_all_nbt_types_keep_original_bytes_and_named_root(self):
        root = compound(byte=Tag(1,-128), short=Tag(2,-32768), integer=Tag(3,-2147483648),
                        long=Tag(4,-9223372036854775808), float=Tag(5,0.125), double=Tag(6,0.0625),
                        bytes=Tag(7,b'\0\xff'), text=Tag(8,'nul\0 et 😀'),
                        ints=Tag(11,[-1,2]), longs=Tag(12,[-1,9223372036854775807]),
                        empty=Tag(9,(3,[])), nested=compound(values=Tag(9,(1,[Tag(1,7)]))))
        raw = encode('named\0root😀',root)
        name, decoded = decode(raw)
        self.assertEqual(name,'named\0root😀')
        self.assertEqual(plain(decoded)['text'],'nul\0 et 😀')
        self.assertEqual(encode(name,decoded),raw)
        for value in plain(decoded).values():
            self.assertEqual(extract._wrap(value)[1],payload(value.nbt_tag))

    def test_truncated_nbt_and_mixed_lists_are_rejected(self):
        with self.assertRaises(ValueError): decode(encode('',fixture())[:-1])
        with self.assertRaises(ValueError): decode(encode('',fixture())+b'extra')
        with self.assertRaises(ValueError): extract.t_list([('int',extract.t_int(1)),('string',extract.t_string('lost'))])

    def test_remap_preserves_inventory_entities_biomes_and_offsets_byte_for_byte(self):
        with tempfile.TemporaryDirectory() as temp:
            source, target = Path(temp)/'source.schem', Path(temp)/'target.schem'
            write_fixture(source,fixture())
            before_bytes=source.read_bytes()
            report=remap.remap_file(source,target,
                {'voiddynasty:super_container','minecraft:chain[axis=y]'},
                {'minecraft:iron_chain[axis=y]':'minecraft:chain[axis=y]'})
            original=load(source)[1].value['Schematic']
            changed=load(target)[1].value['Schematic']
            for key in ('Entities','Biomes','Metadata','Offset','DataVersion'):
                self.assertEqual(payload(original.value[key]),payload(changed.value[key]))
            self.assertEqual(payload(original.value['Blocks'].value['BlockEntities']),
                             payload(changed.value['Blocks'].value['BlockEntities']))
            self.assertEqual(report['substitutions'],{'minecraft:iron_chain[axis=y] -> minecraft:chain[axis=y]':1})
            self.assertEqual(report['block_entities_preserved'],1)
            self.assertFalse(report['paste_allowed'])
            self.assertEqual(source.read_bytes(),before_bytes)

    def test_unknown_mod_ids_and_invalid_properties_cannot_become_air(self):
        with tempfile.TemporaryDirectory() as temp:
            source,target=Path(temp)/'source.schem',Path(temp)/'target.schem'
            write_fixture(source,fixture())
            for known in ({'minecraft:iron_chain[axis=y]'},
                          {'voiddynasty:super_container','minecraft:iron_chain[axis=x]'}):
                with self.subTest(known=known), self.assertRaisesRegex(ValueError,'Unknown target states'):
                    remap.remap_file(source,target,known,{})
                self.assertFalse(target.exists())

    def test_explicit_air_and_block_entity_replacements_are_refused(self):
        with tempfile.TemporaryDirectory() as temp:
            source,target=Path(temp)/'source.schem',Path(temp)/'target.schem'
            write_fixture(source,fixture())
            known={'voiddynasty:super_container','minecraft:iron_chain[axis=y]','minecraft:air','minecraft:chest'}
            for subst, error in (({'minecraft:iron_chain[axis=y]':'minecraft:air'},'air substitution'),
                                 ({'voiddynasty:super_container':'minecraft:chest'},'NBT adapter')):
                with self.subTest(subst=subst), self.assertRaisesRegex(ValueError,error):
                    remap.remap_file(source,target,known,subst)
                self.assertFalse(target.exists())

    def test_existing_export_is_never_overwritten(self):
        with tempfile.TemporaryDirectory() as temp:
            source=Path(temp)/'source.schem'
            write_fixture(source,fixture())
            original=source.read_bytes()
            with self.assertRaisesRegex(ValueError,'overwrite'):
                remap.remap_file(source,source,set(),{})
            self.assertEqual(source.read_bytes(),original)

    def test_invalid_sponge_layout_and_corrupt_indices_fail(self):
        root=fixture()
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'legacy.schem'
            write_fixture(path,root.value['Schematic'])
            result=subprocess.run([sys.executable,str(SCRIPTS/'verify_schem.py'),str(path)],capture_output=True,text=True)
            self.assertEqual(result.returncode,1,result.stdout+result.stderr)
            self.assertIn('"structure_pass": false',result.stdout)
        for data in (b'\0',b'\0\5',b'\0\x80',b'\xff\xff\xff\xff\x08'):
            broken=copy.deepcopy(root)
            broken.value['Schematic'].value['Blocks'].value['Data']=Tag(7,data)
            with self.subTest(data=data), self.assertRaises(ValueError):validate(broken)
        broken=copy.deepcopy(root)
        be=broken.value['Schematic'].value['Blocks'].value['BlockEntities'].value[1][0]
        be.value['Items']=be.value['Data'].value['Items']
        with self.assertRaisesRegex(ValueError,'flattened'):validate(broken)

    def test_single_air_palette_is_valid_structure_but_never_p1(self):
        root=fixture()
        blocks=root.value['Schematic'].value['Blocks'].value
        blocks['Palette']=compound(**{'minecraft:air':Tag(3,0)})
        blocks['Data']=Tag(7,b'\0\0')
        blocks['BlockEntities']=Tag(9,(10,[]))
        self.assertFalse(validate(root)['p1_pass'])

    def test_exact_target_registry_includes_properties_and_mod_states(self):
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'blocks.json'
            path.write_text('{"voiddynasty:super_container":{"states":[{"properties":{"facing":"north","open":"false"}}]}}')
            self.assertEqual(remap.target_states(path),{'voiddynasty:super_container[facing=north,open=false]'})

    def test_multichunk_export_has_global_xyz_order_and_preserves_typed_inventory(self):
        class Cache:
            def get(self,cx,cz):
                if (cx,cz)==(1,1):return None
                sections=[]
                for cy in range(2):
                    states=[f'test:chunk_{cx}_{cz}_{cy}_a',f'test:chunk_{cx}_{cz}_{cy}_b']
                    cells=[(x+2*y+z)%2 for y in range(16) for z in range(16) for x in range(16)]
                    longs=[sum(v<<(4*j) for j,v in enumerate(cells[i:i+16])) for i in range(0,4096,16)]
                    sections.append({'Y':cy,'block_states':{'palette':states,'data':longs}})
                be=plain(fixture().value['Schematic'].value['Blocks'].value['BlockEntities'].value[1][0].value['Data'])
                if (cx,cz)==(0,0):be.update(x=1,y=17,z=1,id='voiddynasty:super_container')
                return {'sections':sections,'block_entities':[be] if (cx,cz)==(0,0) else []}
        cache=Cache()
        states={s for cx,cz in ((0,0),(0,1),(1,0)) for sec in cache.get(cx,cz)['sections'] for s in sec['block_states']['palette']}
        with tempfile.TemporaryDirectory() as temp:
            decoded=[]
            for force in ('','1'):
                path=Path(temp)/('slow.schem' if force else 'fast.schem')
                with patch.dict(os.environ,{'FORCE_SLOW':force}):
                    extract.export_zone('',cache,'test',0,32,0,32,0,31,path,5023,states)
                root=load(path)[1]
                report=validate(root)
                d=plain(root)['Schematic']
                inverse={v:k for k,v in d['Blocks']['Palette'].items()}
                decoded.append([inverse[i] for i in varints(d['Blocks']['Data'])])
                self.assertEqual(report['block_entities'],1)
                be=d['Blocks']['BlockEntities'][0]
                self.assertEqual(be['Pos'],[1,17,1])
                self.assertEqual(be['Data']['Items'][0]['Slot'].nbt_tag.kind,1)
                self.assertEqual(be['Data']['seed'].nbt_tag.kind,4)
                self.assertEqual(be['Data']['progress'].nbt_tag.kind,5)
                self.assertEqual(d['Metadata']['VoidDynastyExtractionComplete'],0)
            expected=[]
            for y in range(32):
                for z in range(32):
                    for x in range(32):
                        expected.append('minecraft:air' if (x//16,z//16)==(1,1) else
                                        f'test:chunk_{x//16}_{z//16}_{y//16}_'+('b' if (x+2*y+z)%2 else 'a'))
            self.assertEqual(decoded[0],expected)
            self.assertEqual(decoded[1],expected)

    def test_corrupt_regions_cannot_be_reported_as_empty(self):
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'dimensions/minecraft/overworld/region/r.0.0.mca'
            path.parent.mkdir(parents=True)
            path.write_bytes(b'truncated')
            with self.assertRaisesRegex(ValueError,'tronquée'):extract.ChunkCache(temp).get(0,0)
        with self.assertRaisesRegex(ValueError,'longs'):extract.unpack([],4,15)

    def test_large_palette_and_partial_negative_chunk_coordinates(self):
        states=[f'test:block_{i}' for i in range(260)]
        cells=[(x+2*z+3*y)%260 for y in range(16) for z in range(16) for x in range(16)]
        longs=[sum(v<<(9*j) for j,v in enumerate(cells[i:i+7])) for i in range(0,4096,7)]
        class Cache:
            def get(self,cx,cz):
                return {'sections':[{'Y':-1,'block_states':{'palette':states,'data':longs}}]}
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'large.schem'
            extract.export_zone('',Cache(),'test',-3,17,-2,17,-15,-1,path,5023,set(states))
            root=load(path)[1]
            self.assertEqual(validate(root)['dimensions'],[20,15,19])
            self.assertEqual(plain(root)['Schematic']['Metadata']['SourceOrigin'],[-3,-15,-2])
            blocks=plain(root)['Schematic']['Blocks']
            inverse={v:k for k,v in blocks['Palette'].items()}
            actual=[inverse[i] for i in varints(blocks['Data'])]
            expected=[states[((x&15)+2*(z&15)+3*(y&15))%260]
                      for y in range(-15,0) for z in range(-2,17) for x in range(-3,17)]
            self.assertEqual(actual,expected)

    def test_cli_validates_structure_without_claiming_p1(self):
        with tempfile.TemporaryDirectory() as temp:
            path=Path(temp)/'valid.schem'
            write_fixture(path,fixture())
            result=subprocess.run([sys.executable,str(SCRIPTS/'verify_schem.py'),str(path)],capture_output=True,text=True)
            self.assertEqual(result.returncode,0,result.stdout+result.stderr)
            self.assertIn('"structure_pass": true',result.stdout)
            self.assertIn('"p1_pass": false',result.stdout)


if __name__=='__main__':unittest.main()
