"""Build the single VOID DYNASTY mod from both canonical source trees, using explicit local libraries.

No game references, JDK, dependency downloads or legacy compiled VOID classes are packaged.
Gradle supports the same source/resource layout; this builder also works with a verified local
classpath when the matching Loom distribution is unavailable.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import subprocess
import uuid
import zipfile

PROJECT=Path(__file__).resolve().parents[1]
BRIDGE=PROJECT.parent/'voiddynasty-bridge'


def resources():
    entries={}
    for project in [PROJECT,BRIDGE]:
        base=project/'src/main/resources'
        for path in sorted(base.rglob('*')):
            if not path.is_file():continue
            name=path.relative_to(base).as_posix()
            if project==BRIDGE and name=='fabric.mod.json':continue
            if name in entries:raise ValueError('Conflicting resource: '+name)
            entries[name]=path
    return entries


def sources():
    entries={}
    for project in [PROJECT,BRIDGE]:
        base=project/'src/main/java'
        for path in sorted(base.rglob('*.java')):
            name=path.relative_to(base).as_posix()
            if name in entries:raise ValueError('Duplicate source class: '+name)
            entries[name]=path
    return list(entries.values())


def fingerprint():
    paths=[*sources(),*resources().values(),PROJECT/'build.gradle',PROJECT/'gradle.properties',Path(__file__),Path(__file__).with_name('CompileAccess.java'),PROJECT/'LICENSE']
    digest=hashlib.sha256()
    for path in sorted(paths):
        digest.update(path.relative_to(PROJECT.parent).as_posix().encode()+b'\0'+path.read_bytes()+b'\0')
    return digest.hexdigest()


def clean_classpath(paths):
    """Old mods and class directories must never mask a missing owned source during compilation."""
    result=[]
    for value in paths:
        path=Path(value).resolve()
        if path.is_dir() or 'compile-only' in path.parts or 'installed-tweakers' in path.name:continue
        if not path.is_file():raise ValueError('Missing dependency: '+str(path))
        with zipfile.ZipFile(path) as jar:
            if 'fabric.mod.json' in jar.namelist():
                mod=json.loads(jar.read('fabric.mod.json'))
                if mod.get('id') in {'voiddynasty','voiddynasty_bridge','onepiece','opworldlike'}:continue
            if 'com/donopot/voiddynasty/VoidDynastyMod.class' in jar.namelist():continue
            if 'com/donopot/voiddynastybridge/BridgeMod.class' in jar.namelist():continue
        if path not in result:result.append(path)
    return result


def write_entry(jar,name,data):
    entry=zipfile.ZipInfo(name,(2026,1,1,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED
    entry.external_attr=0o644<<16;jar.writestr(entry,data)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--classpath-json',type=Path,required=True,help='Private JSON with classpath array of local JARs')
    parser.add_argument('--jdk',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    args.output.mkdir(parents=True,exist_ok=True)
    version=next(row.split('=',1)[1].strip() for row in (PROJECT/'gradle.properties').read_text(encoding='utf-8').splitlines() if row.startswith('version='))
    dependencies=clean_classpath(json.loads(args.classpath_json.read_text(encoding='utf-8-sig'))['classpath'])
    start=fingerprint();java_sources=sources();data=resources()
    classes=args.output/('classes-'+uuid.uuid4().hex);classes.mkdir()
    javac=args.jdk/'bin'/('javac.exe' if os.name=='nt' else 'javac')
    java=args.jdk/'bin'/('java.exe' if os.name=='nt' else 'java')
    tooling=args.output/('compile-only-'+uuid.uuid4().hex);tooling.mkdir()
    game=None
    for path in dependencies:
        with zipfile.ZipFile(path) as jar:
            if 'net/minecraft/server/Main.class' in jar.namelist():
                if game is not None:raise ValueError('Multiple game archives')
                game=path
    if game is None:raise ValueError('Missing game archive')
    subprocess.run([str(javac),'--release','25','-proc:none','-d',str(tooling),'-classpath',os.pathsep.join(map(str,dependencies)),str(Path(__file__).with_name('CompileAccess.java'))],check=True)
    headers=tooling/'access-view.jar'
    subprocess.run([str(java),'-classpath',os.pathsep.join(map(str,[tooling,*dependencies])),'CompileAccess',str(game),str(headers),str(PROJECT/'src/main/resources/voiddynasty.accesswidener')],check=True)
    arguments=['--release','25','-proc:none','-encoding','UTF-8','-d',str(classes),'-classpath',os.pathsep.join(map(str,[headers,*dependencies])),*map(str,java_sources)]
    argfile=classes.parent/(classes.name+'.args')
    argfile.write_text('\n'.join('"'+value.replace('\\','/').replace('"','\\"')+'"' for value in arguments),encoding='utf-8')
    result=subprocess.run([str(javac),'@'+str(argfile)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=180)
    (args.output/'compile.log').write_text(result.stdout+result.stderr,encoding='utf-8')
    if result.returncode:raise SystemExit(result.stdout+result.stderr)
    if fingerprint()!=start:raise SystemExit('Sources changed during build; refusing stale artifact')
    target=args.output/f'voiddynasty-{version}.jar'
    with zipfile.ZipFile(target,'w') as jar:
        write_entry(jar,'LICENSE_voiddynasty',(PROJECT/'LICENSE').read_bytes())
        for path in sorted(classes.rglob('*.class')):write_entry(jar,path.relative_to(classes).as_posix(),path.read_bytes())
        for name,path in sorted(data.items()):
            content=path.read_bytes()
            if name=='fabric.mod.json':content=content.replace(b'${version}',version.encode())
            write_entry(jar,name,content)
    with zipfile.ZipFile(target) as jar:
        assert jar.testzip() is None and len(jar.namelist())==len(set(jar.namelist()))
        meta=json.loads(jar.read('fabric.mod.json'))
        assert meta['id']=='voiddynasty' and meta['version']==version
        assert 'com.donopot.voiddynastybridge.BridgeMod' in meta['entrypoints']['main']
        assert not any(name.startswith('META-INF/jars/voiddynasty') for name in jar.namelist())
        assert not any(name.startswith('net/minecraft/') for name in jar.namelist())
    sha=hashlib.sha256(target.read_bytes()).hexdigest()
    target.with_suffix('.jar.sha256').write_text(sha+'  '+target.name+'\n',encoding='utf-8')
    manifest={'version':version,'artifact':str(target.resolve()),'sha256':sha,'source_sha256':start,
              'source_count':len(java_sources),'resource_count':len(data),'jdk':str(args.jdk.resolve()),
              'classes':str(classes.resolve()),'classpath':list(map(str,dependencies)),
              'modules':['voiddynasty','voiddynasty-bridge'],'mod_id':'voiddynasty'}
    (args.output/'build.local.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
    print(json.dumps({k:manifest[k] for k in ['artifact','sha256','source_count','resource_count','source_sha256']},indent=2))


if __name__=='__main__':main()
