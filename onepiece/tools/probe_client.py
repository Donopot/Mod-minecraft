"""Private CLIENT bytecode check; never starts the game or calls mod entrypoints."""
from pathlib import Path
import json, os, shutil, subprocess, tempfile, zipfile
from installed_runtime import resolve

PROJECT = Path(__file__).resolve().parents[1]
ROOT = PROJECT.parent
build = json.loads((PROJECT / 'build/build-local.json').read_text())
config = json.loads((ROOT / '.runtime/fabric-build/build.local.json').read_text())
jdk = Path(build['jdk']) / 'bin'
output = PROJECT / 'build/client-transform.log'
deps = [Path(p) for p in build['classpath'] if 'compile-only' not in Path(p).parts]
with tempfile.TemporaryDirectory(prefix='onepiece-client-probe-') as temp:
    work = Path(temp)
    mods = work / 'mods'
    mods.mkdir()
    for jar in (Path(config['minecraft']) / 'mods').glob('*.jar'):
        shutil.copy2(jar, mods / jar.name)
    shutil.copy2(build['jar'], mods / Path(build['jar']).name)
    runtime = []
    game = None
    for dep in deps:
        with zipfile.ZipFile(dep) as archive:
            if 'net/minecraft/server/Main.class' in archive.namelist():
                game = dep
            metadata = json.loads(archive.read('fabric.mod.json')) if 'fabric.mod.json' in archive.namelist() else None
            if metadata and metadata['id'] != 'fabricloader':
                if metadata['id'] == 'player_animation_library':
                    shutil.copy2(dep, mods / dep.name)
                continue
        runtime.append(dep)
    if game is None:
        raise RuntimeError('Missing actual Minecraft JAR')
    exact = resolve(config['minecraft'])
    game = exact['game']
    runtime = [game, *exact['libraries']]
    classes = work / 'own-classes'
    classes.mkdir()
    cp = os.pathsep.join(map(str, [classes, *runtime]))
    compile_result = subprocess.run([str(jdk / 'javac.exe'), '-proc:none', '-encoding', 'UTF-8', '-cp', cp,
                                     '-d', str(classes), str(PROJECT / 'tools/ClientTransformProbe.java')],
                                    capture_output=True, text=True)
    if compile_result.returncode:
        output.write_text(compile_result.stdout + compile_result.stderr, encoding='utf-8')
        raise SystemExit('Probe compilation failed: ' + str(output))
    arguments = ['-Djava.awt.headless=true', '-Dfabric.gameJarPath=' + str(game), '-cp', cp,
                 'ClientTransformProbe', str(work)]
    argfile = work / 'probe.args'
    argfile.write_text('\n'.join('"' + arg.replace('\\', '/') + '"' for arg in arguments), encoding='utf-8')
    result = subprocess.run([str(jdk / 'java.exe'), '@' + str(argfile)], cwd=work,
                            capture_output=True, text=True, timeout=90)
    text = result.stdout + result.stderr
    output.write_text(text, encoding='utf-8')
    print('\n'.join(line for line in text.splitlines() if 'CLIENT_' in line or 'Exception' in line or 'Error' in line))
    print('LOG', output)
    raise SystemExit(result.returncode)
