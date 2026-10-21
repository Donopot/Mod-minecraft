import json, os, re, urllib.request

UA = {"User-Agent": "hermes/1.0 (donopot-server-setup)"}
MODS = "/home/donopot/minecraft-server/neoforge-1211/mods"
os.makedirs(MODS, exist_ok=True)


def get(u, raw=False):
    r = urllib.request.Request(u, headers=UA)
    with urllib.request.urlopen(r, timeout=60) as f:
        return f.read() if raw else json.load(f)


def download(url, dest):
    data = get(url, raw=True)
    with open(dest, "wb") as f:
        f.write(data)
    return len(data)


TARGETS = ["geckolib", "balm", "waystones", "open-parties-and-claims", "worldedit", "ponder"]

print("=== MODRINTH (1.21.1 + neoforge) ===")
for slug in TARGETS:
    try:
        vers = get(f"https://api.modrinth.com/v2/project/{slug}/version")
        hit = [v for v in vers if "1.21.1" in v.get("game_versions", []) and "neoforge" in v.get("loaders", [])]
        if not hit:
            print(f"  [SKIP] {slug}: aucune build 1.21.1 neoforge")
            continue
        v = hit[0]
        f0 = v["files"][0]
        dest = os.path.join(MODS, f0["filename"])
        size = download(f0["url"], dest)
        print(f"  [OK] {slug:24s} {v['version_number']:32s} {size//1024:6d} ko  {f0['filename']}")
    except Exception as e:
        print(f"  [ERR] {slug}: {e}")

# flywheel : maven Create (absent de Modrinth en 1.21.1)
print("\n=== FLYWHEEL (maven.createmod.net) ===")
try:
    xml = get("https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/maven-metadata.xml", raw=True).decode()
    versions = re.findall(r"<version>([^<]+)</version>", xml)
    print("  versions dispo:", ", ".join(versions[-6:]))
    ver = versions[-1]
    url = f"https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/{ver}/flywheel-neoforge-1.21.1-{ver}.jar"
    dest = os.path.join(MODS, f"flywheel-neoforge-1.21.1-{ver}.jar")
    print(f"  telechargement {ver} ...")
    size = download(url, dest)
    print(f"  [OK] flywheel {ver} -> {size//1024} ko")
except Exception as e:
    print("  [ERR] flywheel:", e)

print("\n=== CONTENU mods/ ===")
for fn in sorted(os.listdir(MODS)):
    print(f"  {os.path.getsize(os.path.join(MODS, fn))//1024:7d} ko  {fn}")
