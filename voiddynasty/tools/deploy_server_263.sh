#!/bin/bash
# Installe une version de voiddynasty dans un serveur itzg/minecraft-server (Fabric 26.3)
# et vérifie que le serveur est réellement pret — par RCON, pas en greppant un log
# qui contient deja les demarrages precedents.
#
# Usage :
#   ./deploy_server_263.sh <chemin-du-jar> [conteneur] [repertoire-data]
# Exemple :
#   ./deploy_server_263.sh ../build/libs/voiddynasty-1.2.4.jar mc-263 /home/donopot/minecraft-server/data-263
#
# Prerequis : le conteneur tourne, enable-rcon=true et rcon-cli disponible dans l'image.
set -euo pipefail

JAR_SRC=${1:?usage: deploy_server_263.sh <jar> [conteneur] [data-dir]}
CONTAINER=${2:-mc-263}
DATA=${3:-/home/donopot/minecraft-server/data-263}

MODS="$DATA/mods"
ARCHIVE="$(dirname "$DATA")/mods-archive"
BASE=$(basename "$JAR_SRC")
VERSION=$(echo "$BASE" | sed -E 's/^voiddynasty-(.*)\.jar$/\1/')

[ -f "$JAR_SRC" ] || { echo "ERREUR : jar introuvable : $JAR_SRC"; exit 1; }
case "$BASE" in voiddynasty-*.jar) ;; *) echo "ERREUR : le nom doit etre voiddynasty-<version>.jar"; exit 1;; esac
mkdir -p "$MODS" "$ARCHIVE"

echo "--- mods avant ---"
ls -l "$MODS" | awk 'NR>1 {print $5, $9}'

echo
echo "--- archivage des anciennes versions ---"
shopt -s nullglob
for old in "$MODS"/voiddynasty-*.jar; do
  [ "$(basename "$old")" = "$BASE" ] && continue
  mv -v "$old" "$ARCHIVE"/
done
shopt -u nullglob

echo
echo "--- installation de $BASE ---"
cp "$JAR_SRC" "$MODS/$BASE"
md5sum "$MODS/$BASE"
ls -l "$MODS" | awk 'NR>1 {print $5, $9}'

echo
echo "--- un seul voiddynasty dans mods ? ---"
COUNT=$(ls "$MODS"/voiddynasty-*.jar 2>/dev/null | wc -l)
echo "$COUNT"
[ "$COUNT" = "1" ] || { echo "ERREUR : $COUNT jars voiddynasty dans $MODS"; exit 1; }

echo
echo "--- redemarrage du conteneur ---"
docker restart "$CONTAINER" >/dev/null
echo "redemarrage lance"

echo
echo "--- attente de RCON (le seul signal fiable) ---"
READY=0
for i in $(seq 1 90); do
  if docker exec "$CONTAINER" rcon-cli list >/dev/null 2>&1; then READY=1; break; fi
  sleep 3
done
if [ "$READY" != "1" ]; then
  echo "ECHEC : RCON ne repond pas apres 270 s"
  docker logs --tail 25 "$CONTAINER" 2>&1 | cut -c1-160
  exit 1
fi
echo "RCON pret apres $((i*3)) s"

echo
echo "--- version effectivement chargee ---"
docker logs "$CONTAINER" 2>&1 | grep -a "SERVER_STARTED" | tail -1
docker logs "$CONTAINER" 2>&1 | grep -aE "^\s+- voiddynasty" | tail -1

echo
echo "--- joueurs ---"
docker exec "$CONTAINER" rcon-cli list 2>&1 | tail -2
echo
echo "OK : $BASE installe et serveur pret. Lancer verify_server_263.sh pour le harnais."
