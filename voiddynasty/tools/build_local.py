"""Build VOID DYNASTY with explicit installed dependencies, without old mod classes."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import uuid
import zipfile

PROJECT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--classpath-json", type=Path, required=True)
    parser.add_argument("--jdk", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    out = args.output.resolve()
    out.mkdir(parents=True, exist_ok=True)
    deps = []
    for value in json.loads(args.classpath_json.read_text(encoding="utf-8-sig"))["classpath"]:
        path = Path(value).resolve()
        if path.is_dir():
            continue
        with zipfile.ZipFile(path) as jar:
            names = jar.namelist()
            if any(n.startswith(("com/donopot/voiddynasty/", "com/donopot/voiddynastybridge/",
                                 "net/mcreator/minepiece/")) for n in names):
                continue
        if path not in deps:
            deps.append(path)
    version = next(line.split("=", 1)[1] for line in
                   (PROJECT / "gradle.properties").read_text().splitlines() if line.startswith("version="))
    suffix = ".exe" if os.name == "nt" else ""
    javac, java = [str(args.jdk.resolve() / "bin" / (name + suffix)) for name in ("javac", "java")]
    classes = out / ("classes-" + uuid.uuid4().hex)
    headers_dir = out / ("headers-" + uuid.uuid4().hex)
    classes.mkdir()
    headers_dir.mkdir()
    game = next(p for p in deps if "net/minecraft/server/Main.class" in zipfile.ZipFile(p).namelist())
    cp = os.pathsep.join(map(str, deps))
    subprocess.run([javac, "--release", "25", "-proc:none", "-cp", cp, "-d", str(headers_dir),
                    str(PROJECT / "tools/CompileAccess.java")], check=True)
    headers = headers_dir / "access-view.jar"
    subprocess.run([java, "-cp", os.pathsep.join([str(headers_dir), cp]), "CompileAccess",
                    str(game), str(headers), str(PROJECT / "src/main/resources/voiddynasty.accesswidener")],
                   check=True)
    sources = sorted((PROJECT / "src/main/java").rglob("*.java"))
    argv = ["--release", "25", "-proc:none", "-encoding", "UTF-8", "-d", str(classes),
            "-cp", os.pathsep.join([str(headers), cp]), *map(str, sources)]
    argfile = out / "compile.args"
    argfile.write_text("\n".join('"' + x.replace("\\", "/").replace('"', '\\"') + '"' for x in argv),
                       encoding="utf-8")
    result = subprocess.run([javac, "@" + str(argfile)], capture_output=True, text=True,
                            encoding="utf-8", errors="replace", timeout=180)
    (out / "compile.log").write_text(result.stdout + result.stderr, encoding="utf-8")
    if result.returncode:
        raise SystemExit(result.stdout + result.stderr)
    artifact = out / f"voiddynasty-{version}.jar"
    with zipfile.ZipFile(artifact, "w", zipfile.ZIP_DEFLATED) as jar:
        for base in (classes, PROJECT / "src/main/resources"):
            for path in sorted(base.rglob("*")):
                if path.is_file():
                    data = path.read_bytes()
                    name = path.relative_to(base).as_posix()
                    if name == "fabric.mod.json":
                        data = data.replace(b"${version}", version.encode())
                    info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0))
                    info.compress_type = zipfile.ZIP_DEFLATED
                    jar.writestr(info, data)
        jar.write(PROJECT / "LICENSE", "LICENSE_voiddynasty")
    digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
    artifact.with_suffix(".jar.sha256").write_text(digest + "  " + artifact.name + "\n")
    (out / "build.local.json").write_text(json.dumps(
        {"artifact": str(artifact), "sha256": digest, "classpath": list(map(str, deps)),
         "classes": str(classes), "version": version}, indent=2))
    print(f"Built {artifact}\nSHA256 {digest}")


if __name__ == "__main__":
    main()
