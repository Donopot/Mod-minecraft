#!/bin/bash
# Stabilité finale : 4 passages, échecs comptés par assertion, couloirs mesurés.
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/mori12.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/mori12.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/mori12.log | head -10
   exit 1
fi
echo "BUILD OK"

python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cd /opt/data/workspace/opworldlike-fabric || exit 1

for i in 1 2 3 4; do
   python3 tools/run_tests.py > "/tmp/stabC-$i.log" 2>&1
   echo "--- passage $i : $(grep -E 'RÉSULTAT GLOBAL' "/tmp/stabC-$i.log")"
   grep -E "ÉCHEC" "/tmp/stabC-$i.log" | head -3
done

echo "=== bilan des échecs sur 4 passages ==="
grep -hE "ÉCHEC" /tmp/stabC-1.log /tmp/stabC-2.log /tmp/stabC-3.log /tmp/stabC-4.log | sort | uniq -c | sort -rn | head -10
echo "=== couloirs (debris retires / obstacles restants) ==="
grep -hoE "couloir dégagé \([0-9]+ bloc\(s\) retiré\(s\), obstacles restants = [0-9]+\)" /tmp/stabC-*.log | sort | uniq -c
grep -hoE "couloir du contrôle positif : obstacles = [0-9]+" /tmp/stabC-*.log | sort | uniq -c
