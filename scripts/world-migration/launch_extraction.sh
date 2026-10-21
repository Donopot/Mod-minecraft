#!/bin/bash
# Lance l'extraction des 173 zones en arriere-plan sur le HP Mini (detache + log).
set -u
WORLD="$HOME/minecraft-server/data-263/world"
CSV="$HOME/repos/Mod-minecraft/plans/neoforge-1.21.1/annexe-zones-export.csv"
OUT="$HOME/minecraft-server/migration-schematics"
mkdir -p "$OUT"
pgrep -f "extract_zones.py" >/dev/null && echo "deja en cours" || {
  cd "$HOME/minecraft-server" || exit 1
  setsid nohup python3 /tmp/extract_zones.py "$WORLD" "$CSV" "$OUT" > /tmp/extract.log 2>&1 &
  echo "extraction lancee (pid $!)"
}
sleep 30
echo "=== LOG (tail) ==="
tail -6 /tmp/extract.log
echo "=== PROGRESSION ==="
echo "schematics: $(ls "$OUT"/*.schem 2>/dev/null | wc -l) fichiers"
du -sh "$OUT"
df -h / | tail -1
