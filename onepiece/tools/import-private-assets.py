"""Restore private visual resources from the matching locally built mod JAR.

This imports no compiled classes, dependencies, accounts, saves or credentials.
"""
from pathlib import Path
import argparse, hashlib, json, zipfile

project=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--jar',required=True,type=Path)
parser.add_argument('--check',action='store_true',help='Validate the archive without writing assets')
args=parser.parse_args()
manifest=json.loads((project/'docs/private-assets-manifest.json').read_text(encoding='utf-8'))
files=[]
with zipfile.ZipFile(args.jar) as archive:
    for row in manifest['files']:
        relative=Path(row['path'])
        if relative.is_absolute() or '..' in relative.parts:
            raise SystemExit('Invalid resource path in manifest')
        data=archive.read(row['path'])
        if hashlib.sha256(data).hexdigest()!=row['sha256']:
            raise SystemExit('Resource differs from the recorded version: '+row['path'])
        files.append((project/'src/main/resources'/relative,data))
if not args.check:
    for target,data in files:
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_bytes(data)
print(('CHECKED' if args.check else 'RESTORED'),len(files),'private resources')
