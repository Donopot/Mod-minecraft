#!/bin/bash
# Étape 1 : build + copie du jar. (Le serveur et les invocations sont pilotés séparément.)
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/mori15.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/mori15.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/mori15.log | head -10
   exit 1
fi
echo "BUILD OK"
python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cp -f /opt/data/workspace/opworldlike-fabric/build/libs/opworldlike-1.0.0.jar mods/
rm -f logs/latest.log
echo "jar copié"
