#!/bin/bash
# Confirmation finale après nettoyage : 2 passages complets.
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/mori14.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/mori14.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/mori14.log | head -10
   exit 1
fi
echo "BUILD OK"

python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cd /opt/data/workspace/opworldlike-fabric || exit 1

for i in 1 2; do
   python3 tools/run_tests.py > "/tmp/final-$i.log" 2>&1
   echo "--- passage $i : $(grep -E 'RÉSULTAT GLOBAL' "/tmp/final-$i.log")"
   grep -E "ÉCHEC" "/tmp/final-$i.log" | head -3
done

python3 tools/mc_kill_server.py --kill
cp -f build/libs/opworldlike-1.0.0.jar /opt/data/workspace/donopot-devilfruits-client/opworldlike-1.0.0.jar
echo "jar final : $(stat -c%s build/libs/opworldlike-1.0.0.jar) o"
