#!/bin/bash
# Corrige Ponder (ponderjs -> ponder officiel), verifie les modIds, redemarre et controle le chargement.
set -u
MODS="$HOME/minecraft-server/neoforge-1211/mods"
DISABLED="$HOME/minecraft-server/mods-disabled"
mkdir -p "$DISABLED"

echo "=== 1. PONDER OFFICIEL (maven Create) ==="
python3 - <<'PY'
import os, re, urllib.request
UA = {"User-Agent": "hermes/1.0"}
base = "https://maven.createmod.net/net/createmod/ponder/ponder-neoforge-1.21.1"
try:
    xml = urllib.request.urlopen(urllib.request.Request(base + "/maven-metadata.xml", headers=UA), timeout=30).read().decode()
    vs = re.findall(r"<version>([^<]+)</version>", xml)
    print("  versions dispo:", ", ".join(vs[-6:]))
    # on prefere 1.0.82+mc1.21.1 (requis par Create 6.0.10) si present
    pick = next((v for v in reversed(vs) if v.startswith("1.0.82")), vs[-1])
    url = f"{base}/{pick}/ponder-neoforge-1.21.1-{pick}.jar"
    dest = os.path.expanduser(f"~/minecraft-server/neoforge-1211/mods/ponder-neoforge-1.21.1-{pick}.jar")
    data = urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=90).read()
    open(dest, "wb").write(data)
    print(f"  [OK] ponder {pick} -> {len(data)//1024} ko")
except Exception as e:
    print("  [ERREUR] ponder:", e)
PY

echo
echo "=== 2. NEUTRALISATION PONDERJS ==="
if [ -f "$MODS/ponderjs-neoforge-1.21.1-2.4.0.jar" ]; then
  mv "$MODS/ponderjs-neoforge-1.21.1-2.4.0.jar" "$DISABLED/" && echo "  ponderjs deplace vers $DISABLED (non supprime)"
else
  echo "  ponderjs deja absent"
fi

echo
echo "=== 3. MOD IDS DES JARS PRESENTS ==="
python3 - <<'PY'
import glob, os, re, zipfile
mods = os.path.expanduser("~/minecraft-server/neoforge-1.21.1/mods").replace("1.21.1", "neoforge-1211") + "/"
for f in sorted(glob.glob(mods + "*.jar")):
    try:
        z = zipfile.ZipFile(f)
        t = z.read("META-INF/neoforge.mods.toml").decode("utf-8", "replace")
        ids = re.findall(r'modId\s*=\s*"([^"]+)"', t)
        print(f"  {','.join(ids[:3]):26s} {os.path.basename(f)}")
    except Exception as e:
        print(f"  ?? {os.path.basename(f)}: {e}")
PY

echo
echo "=== 4. REDEMARRAGE ==="
docker restart mc-neoforge-1211 >/dev/null && echo "  restart envoye"
for i in $(seq 1 44); do
  if docker logs --since 4m mc-neoforge-1211 2>&1 | grep -qE "Done \(|Missing or unsupported|Failed to"; then break; fi
  sleep 5
done

echo
echo "=== 5. ERREURS DE DEPENDANCES / CHARGEMENT ==="
docker logs --since 6m mc-neoforge-1211 2>&1 | grep -iE "missing or unsupported|incompatib|failed to (load|start)|Loading \d+ mods|Exception" | head -20

echo
echo "=== 6. MODS CHARGES ==="
docker logs --since 6m mc-neoforge-1211 2>&1 | grep -iE "^\[.*\].*(create|geckolib|waystones|balm|ponder|flywheel|worldedit|openparties|warhammer)" | head -12

echo
echo "=== 7. ETAT FINAL ==="
docker logs --tail 6 mc-neoforge-1211 2>&1
docker ps --filter name=mc-neoforge-1211 --format "{{.Names}} {{.Status}}"
free -h | head -2
