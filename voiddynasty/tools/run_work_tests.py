"""Run native inventory/receipt regression tests against an explicit built candidate JAR."""
from pathlib import Path
import argparse
import json
import os
import subprocess
import uuid


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--candidate', type=Path, required=True)
    parser.add_argument('--classpath-json', type=Path, required=True,
                        help='Local JSON object containing a classpath array of Minecraft/dependency paths')
    parser.add_argument('--jdk', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    project = Path(__file__).resolve().parents[1]
    config = json.loads(args.classpath_json.read_text(encoding='utf-8-sig'))
    dependencies = [Path(p) for p in config['classpath']]
    for path in [args.candidate, *dependencies]:
        if not path.exists():
            raise SystemExit('Missing dependency: ' + str(path))
    out = args.output.resolve() / ('unit-' + uuid.uuid4().hex)
    out.mkdir(parents=True)
    cp = os.pathsep.join(map(str, [out, args.candidate.resolve(), *dependencies]))
    source = project / 'src/test/java/com/donopot/voiddynasty/work/WorkConservationTest.java'
    suffix = '.exe' if os.name == 'nt' else ''
    commands = [
        [str(args.jdk / 'bin' / ('javac' + suffix)), '--release', '25', '-proc:none',
         '-encoding', 'UTF-8', '-cp', cp, '-d', str(out), str(source)],
        [str(args.jdk / 'bin' / ('java' + suffix)), '-cp', cp,
         'com.donopot.voiddynasty.work.WorkConservationTest'],
    ]
    for index, command in enumerate(commands):
        result = subprocess.run(command, cwd=out, capture_output=True, text=True,
                                encoding='utf-8', errors='replace', timeout=90)
        (out / f'{index}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        print(result.stdout, end='')
        if result.returncode:
            print(result.stderr, end='')
            raise SystemExit(result.returncode)
    print('Logs:', out)


if __name__ == '__main__':
    main()
