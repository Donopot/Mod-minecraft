"""Prepare P1 commands for Hermes or verify exported-file integrity; never contacts a server."""
import argparse
import csv
import gzip
import hashlib
import json
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_ZONES = ROOT/'plans/neoforge-1.21.1/annexe-zones-export.csv'


def zones(path):
    with path.open(encoding='utf-8-sig',newline='') as stream: rows=list(csv.DictReader(stream))
    expected={f'zone_{n:03d}' for n in range(1,174)}
    if len(rows)!=173 or {r['zone'] for r in rows}!=expected:
        raise ValueError('Expected exactly zone_001 through zone_173, without duplicates')
    result=[]
    for row in rows:
        numbers={key:int(row[key]) for key in ['x1','x2','z1','z2','y1','y2','size_x','size_z']}
        if numbers['x2']-numbers['x1']!=numbers['size_x'] or numbers['z2']-numbers['z1']!=numbers['size_z']:
            raise ValueError('Half-open bounds disagree with size: '+row['zone'])
        if numbers['size_x']<=0 or numbers['size_z']<=0 or (numbers['y1'],numbers['y2'])!=(-64,320):
            raise ValueError('Unexpected source-world bounds: '+row['zone'])
        result.append(dict(zone=row['zone'],**numbers))
    return result


def prepare(rows, output):
    output.mkdir(parents=True,exist_ok=True)
    manifest=[]
    for offset in range(0,len(rows),20):
        lines=['# Hermes: run each command separately as an OP player on the backed-up 26.3 COPY.',
               '# Stop after each export error; never run these on the 1.21.1 target.',
               '# Upper CSV bounds are exclusive; WorldEdit selections are inclusive.','']
        for row in rows[offset:offset+20]:
            x,z=row['x1'],row['z1']
            commands=[f'/execute in minecraft:overworld run tp @s {x+0.5} 100 {z+0.5}',
                      f"//pos1 {x},-64,{z}",f"//pos2 {row['x2']-1},319,{row['z2']-1}",
                      '//copy -e -b',f"//schem save {row['zone']} sponge"]
            lines.extend(['# '+row['zone'],*commands,''])
            manifest.append(dict(zone=row['zone'],source_dimension='minecraft:overworld',
                expected_file=row['zone']+'.schem',origin=[x,100,z],
                size=[row['size_x'],384,row['size_z']],
                source_bounds_exclusive=[row['x1'],-64,row['z1'],row['x2'],320,row['z2']],
                exported=False,worldedit_load_verified=False,sha256=None))
        (output/f'hermes-batch-{offset//20+1:02d}.txt').write_text('\n'.join(lines),encoding='utf-8')
    (output/'export-manifest.json').write_text(json.dumps({'schema':1,'expected':173,'zones':manifest},indent=2)+'\n',encoding='utf-8')
    print('PREPARED:',len(manifest),'exports in 9 command batches; no server contacted')


def verify(rows, exports, report):
    results=[]
    for row in rows:
        path=exports/(row['zone']+'.schem')
        record=dict(zone=row['zone'],file=path.name,integrity_pass=False)
        try:
            if not path.is_file(): raise ValueError('missing schematic')
            raw_hash=hashlib.sha256()
            with path.open('rb') as stream:
                for block in iter(lambda:stream.read(1024*1024),b''): raw_hash.update(block)
            count=0
            limit=max(64*1024*1024,row['size_x']*384*row['size_z']*16)
            with gzip.open(path,'rb') as stream:
                prefix=stream.read(1);count+=len(prefix)
                if prefix!=b'\x0a':raise ValueError('expected gzip-compressed NBT compound root')
                for block in iter(lambda:stream.read(1024*1024),b''):
                    count+=len(block)
                    if count>limit: raise ValueError('expanded schematic exceeds bounded integrity check')
            if count<32:raise ValueError('truncated NBT payload')
            record.update(integrity_pass=True,sha256=raw_hash.hexdigest(),bytes=path.stat().st_size,uncompressed_bytes=count)
        except (OSError,ValueError,EOFError,zlib.error) as error:record['error']=str(error)
        results.append(record)
    value=dict(schema=1,expected=173,integrity_pass_count=sum(r['integrity_pass'] for r in results),
               p1_pass=False,worldedit_and_visual_validation='Required from Hermes; integrity is not a migration acceptance test.',zones=results)
    report.parent.mkdir(parents=True,exist_ok=True)
    report.write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
    print('FILE INTEGRITY:',value['integrity_pass_count'],'/ 173; WorldEdit/visual proof still required')
    return value['integrity_pass_count']==173


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--zones',type=Path,default=DEFAULT_ZONES)
    commands=p.add_subparsers(dest='action',required=True)
    generate=commands.add_parser('prepare');generate.add_argument('--output',type=Path,required=True)
    check=commands.add_parser('verify');check.add_argument('--exports',type=Path,required=True);check.add_argument('--report',type=Path,required=True)
    args=p.parse_args();rows=zones(args.zones)
    if args.action=='prepare':prepare(rows,args.output)
    elif not verify(rows,args.exports,args.report):raise SystemExit(1)

if __name__=='__main__':main()
