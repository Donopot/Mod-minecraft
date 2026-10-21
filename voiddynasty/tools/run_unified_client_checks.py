"""Check real Fabric client transformations and containers with only the unified mod and its libraries."""
from pathlib import Path
import argparse,json,os,shutil,subprocess,sys,uuid,zipfile

PROJECT=Path(__file__).resolve().parents[1];REPO=PROJECT.parent


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--build-json',type=Path,required=True)
    p.add_argument('--minecraft',type=Path,required=True)
    p.add_argument('--dependencies',type=Path,required=True)
    p.add_argument('--output',type=Path,required=True)
    p.add_argument('--legacy-bridge',type=Path,help='Also verify that Loader rejects an accidentally retained standalone bridge')
    args=p.parse_args();build=json.loads(args.build_json.read_text(encoding='utf-8'))
    sys.path.insert(0,str(REPO/'onepiece/tools'))
    from installed_runtime import resolve
    exact=resolve(args.minecraft.resolve())
    work=args.output/('client-'+uuid.uuid4().hex);mods=work/'mods';mods.mkdir(parents=True)
    candidate=Path(build['artifact']);shutil.copy2(candidate,mods/candidate.name)
    for path in args.dependencies.glob('*.jar'):
        if 'fabric-api' in path.name or 'geckolib' in path.name:shutil.copy2(path,mods/path.name)
    classes=work/'test-classes';classes.mkdir()
    javac=Path(build['jdk'])/'bin'/('javac.exe' if os.name=='nt' else 'javac')
    java=javac.with_name('java.exe' if os.name=='nt' else 'java')
    compile_cp=os.pathsep.join([str(candidate),*build['classpath']])
    sources=[PROJECT/'src/test/java/com/donopot/voiddynasty/unified/UnifiedKnotHarness.java',REPO/'voiddynasty-logistics/tests/RefitInventoryHarness.java']
    r=subprocess.run([str(javac),'--release','25','-proc:none','-encoding','UTF-8','-d',str(classes),'-cp',compile_cp,*map(str,sources)],capture_output=True,text=True,encoding='utf-8',errors='replace')
    (work/'compile.log').write_text(r.stdout+r.stderr,encoding='utf-8')
    if r.returncode:raise SystemExit((r.stdout+r.stderr)[-6000:])
    runtime_cp=os.pathsep.join(map(str,[classes.resolve(),exact['game'],*exact['libraries']]))
    r=subprocess.run([str(java),'-Djava.awt.headless=true','-Dfabric.gameJarPath='+str(exact['game']),'-cp',runtime_cp,
        'com.donopot.voiddynasty.unified.UnifiedKnotHarness',str(work.resolve()),str(classes.resolve()),
        str(REPO/'voiddynasty-bridge/src/main/resources/resourcepacks/refit')],cwd=work,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=100)
    evidence=r.stdout+r.stderr;(work/'result.log').write_text(evidence,encoding='utf-8')
    args.output.mkdir(parents=True,exist_ok=True);(args.output/'latest-client.txt').write_text(str(work.resolve()),encoding='utf-8')
    if r.returncode:raise SystemExit(evidence[-12000:])
    assert 'PASS UNIFIED CLIENT' in evidence
    print(evidence[-2600:]);print('Evidence:',work.resolve())
    if args.legacy_bridge:
        conflict=work/'conflict';(conflict/'mods').mkdir(parents=True)
        for path in mods.glob('*.jar'):shutil.copy2(path,conflict/'mods'/path.name)
        shutil.copy2(args.legacy_bridge,conflict/'mods'/args.legacy_bridge.name)
        r=subprocess.run([str(java),'-Djava.awt.headless=true','-Dfabric.gameJarPath='+str(exact['game']),'-cp',runtime_cp,
            'com.donopot.voiddynasty.unified.UnifiedKnotHarness',str(conflict.resolve()),str(classes.resolve()),
            str(REPO/'voiddynasty-bridge/src/main/resources/resourcepacks/refit')],cwd=conflict,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=100)
        (conflict/'result.log').write_text(r.stdout+r.stderr,encoding='utf-8')
        if r.returncode==0 or 'voiddynasty_bridge' not in r.stdout+r.stderr:raise SystemExit('Legacy bridge conflict was not rejected clearly')
        print('PASS STANDALONE BRIDGE CONFLICT REJECTED')


if __name__=='__main__':main()
