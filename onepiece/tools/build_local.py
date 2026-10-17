"""Private offline build against the user's verified installed Minecraft/Fabric libraries."""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import zipfile

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parent

def main():
    paths = json.loads((ROOT / '.runtime/onepiece/paths.local.json').read_text(encoding='utf-8'))
    private = Path(paths['private'])
    config = json.loads((ROOT / '.runtime/fabric-build/build.local.json').read_text(encoding='utf-8'))
    jdk = Path(config['jdk'])
    classes = PROJECT / 'build/local-classes'
    classes.mkdir(parents=True, exist_ok=True)
    # Fresh output: stale class files are never packaged after deleted/renamed Java source.
    for path in classes.rglob('*.class'):
        path.unlink()
    dependencies = [Path(p) for p in config['classpath'] if 'voiddynasty' not in Path(p).name.lower()]
    dependencies += sorted((private / 'libs').glob('*.jar'))
    loader = next(p for p in dependencies if p.name == 'fabric-loader-0.19.5.jar')
    with zipfile.ZipFile(loader) as archive:
        nested = next(n for n in archive.namelist() if n.startswith('META-INF/jars/mixinextras-') and n.endswith('.jar'))
        extra = private / 'compile-dependencies' / Path(nested).name
        extra.parent.mkdir(parents=True,exist_ok=True)
        extra.write_bytes(archive.read(nested))
    dependencies.append(extra)
    for path in dependencies:
        if not path.is_file(): raise SystemExit('Missing dependency: ' + str(path))
    helper = PROJECT / 'build/own-build-tools'
    helper.mkdir(parents=True,exist_ok=True)
    raw_cp = os.pathsep.join(map(str,dependencies))
    command = [str(jdk/'bin/javac.exe'),'-encoding','UTF-8','-proc:none','-classpath',raw_cp,'-d',str(helper),str(PROJECT/'tools/CompileHeaders.java')]
    subprocess.run(command,check=True,cwd=PROJECT)
    headers = private / 'compile-only/installed-tweakers.jar'
    header_args=['-classpath',str(helper)+os.pathsep+raw_cp,'CompileHeaders',str(dependencies[0]),str(headers),*map(str,dict.fromkeys(dependencies))]
    header_file=PROJECT/'build/headers-local.args'
    header_file.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in header_args),encoding='utf-8')
    subprocess.run([str(jdk/'bin/java.exe'),'@'+str(header_file)],check=True,cwd=PROJECT)
    dependencies.insert(0,headers)
    sources = sorted((PROJECT / 'src/main/java').rglob('*.java'))
    args = ['--release', '25', '-encoding', 'UTF-8', '-proc:none', '-classpath', os.pathsep.join(map(str, dependencies)), '-d', str(classes)]
    args += list(map(str, sources))
    argfile = PROJECT / 'build/compile-local.args'
    argfile.write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in args), encoding='utf-8')
    result = subprocess.run([str(jdk/'bin/javac.exe'), '@'+str(argfile)], cwd=PROJECT, capture_output=True, text=True)
    log = result.stdout + result.stderr
    (PROJECT/'build/compile-local.log').write_text(log, encoding='utf-8')
    print(log[-14000:])
    if result.returncode: raise SystemExit(result.returncode)
    version = next(line.split('=',1)[1].strip() for line in (PROJECT/'gradle.properties').read_text().splitlines() if line.startswith('version='))
    output = PROJECT / 'build/libs' / ('opworldlike-'+version+'.jar')
    output.parent.mkdir(parents=True,exist_ok=True)
    entries = {}
    for folder in [private/'src/main/resources', PROJECT/'src/main/resources', classes]:
        if folder.exists():
            for path in folder.rglob('*'):
                if path.is_file(): entries[path.relative_to(folder).as_posix()] = path.read_bytes()
    entries['fabric.mod.json'] = entries['fabric.mod.json'].replace(b'${version}',version.encode())
    entries['LICENSE_opworldlike'] = (PROJECT/'LICENSE').read_bytes()
    with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as archive:
        for name, data in sorted(entries.items()): archive.writestr(name,data)
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix('.jar.sha256').write_text(digest+'  '+output.name+'\n',encoding='utf-8')
    (PROJECT/'build/build-local.json').write_text(json.dumps({'jar':str(output),'version':version,'jdk':str(jdk),'classpath':list(map(str,dependencies)),'source_count':len(sources),'sha256':digest},indent=2),encoding='utf-8')
    print('BUILT',output,'SOURCES',len(sources),'SHA256',digest)

if __name__ == '__main__': main()
