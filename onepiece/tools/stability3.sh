#!/bin/bash
# Stabilité : 3 passages complets de la recette, échecs comptés par assertion.
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/mori10.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/mori10.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/mori10.log | head -10
   exit 1
fi
echo "BUILD OK"

python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cd /opt/data/workspace/opworldlike-fabric || exit 1

for i in 1 2 3; do
   python3 tools/run_tests.py > "/tmp/stab-$i.log" 2>&1
   echo "--- passage $i : $(grep -E 'RÉSULTAT GLOBAL' "/tmp/stab-$i.log")"
   grep -E "ÉCHEC" "/tmp/stab-$i.log" | head -3
done

echo "=== bilan des échecs sur 3 passages ==="
grep -hE "ÉCHEC" /tmp/stab-1.log /tmp/stab-2.log /tmp/stab-3.log | sort | uniq -c | sort -rn | head -10
