#!/bin/bash
# Finalisation du Fruit Mori Mori : jar final + paquet client + restauration de mods/.
set -u
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
FAB=/opt/data/workspace/opworldlike-fabric
SRV=/opt/data/workspace/mc-server
CLI=/opt/data/workspace/donopot-devilfruits-client

python3 "$FAB/tools/mc_kill_server.py" --kill

# mods/ doit revenir à la BASELINE : aucun jar opworldlike
rm -f "$SRV/mods/opworldlike-1.0.0.jar"
echo "mods/ (baseline) :"
ls -1 "$SRV/mods"

cp -f "$FAB/build/libs/opworldlike-1.0.0.jar" "$CLI/opworldlike-1.0.0.jar"
rm -f /opt/data/workspace/donopot-devilfruits-client.zip
cd "$CLI" || exit 1
zip -qr /opt/data/workspace/donopot-devilfruits-client.zip . -x '.git/*'

echo "jar  : $(stat -c%s "$FAB/build/libs/opworldlike-1.0.0.jar") o"
echo "zip  : $(stat -c%s /opt/data/workspace/donopot-devilfruits-client.zip) o"
echo "contrats : $(ls -1 "$FAB/docs/fruits" | wc -l)/40"
