#!/bin/bash
# Prévisualisation des blocs uniquement : ne valide pas P1, aucun collage autorisé.
set -eu
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
: "${WORLD:?Indiquer WORLD vers une copie cohérente et sauvegardée du monde source}"
CSV="$SCRIPT_DIR/../../plans/neoforge-1.21.1/annexe-zones-export.csv"
OUT="${OUT:-$HOME/minecraft-server/migration-schematics-preview-v2}"
mkdir -p "$OUT"
pgrep -f "extract_zones.py" >/dev/null && echo "deja en cours" || {
  cd "$HOME/minecraft-server" || exit 1
  setsid nohup python3 "$SCRIPT_DIR/extract_zones.py" "$WORLD" "$CSV" "$OUT" > /tmp/extract.log 2>&1 &
  echo "extraction lancee (pid $!)"
}
sleep 30
echo "=== LOG (tail) ==="
tail -6 /tmp/extract.log
echo "=== PROGRESSION ==="
echo "schematics: $(ls "$OUT"/*.schem 2>/dev/null | wc -l) fichiers"
du -sh "$OUT"
df -h / | tail -1
