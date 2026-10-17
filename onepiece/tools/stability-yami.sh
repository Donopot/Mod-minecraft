#!/bin/bash
# Stabilité Yami Pika : 3 passages complets, échecs comptés par assertion.
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/yami-stab-build.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/yami-stab-build.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/yami-stab-build.log | head -10
   exit 1
fi
echo "BUILD OK"

python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cd /opt/data/workspace/opworldlike-fabric || exit 1

for i in 1 2 3; do
   python3 tools/run_tests.py > "/tmp/yami-stab-$i.log" 2>&1
   echo "--- passage $i : $(grep -E 'RÉSULTAT GLOBAL' "/tmp/yami-stab-$i.log")"
   grep -E "ÉCHEC" "/tmp/yami-stab-$i.log" | head -4
done

echo "=== bilan des échecs sur 3 passages ==="
grep -hE "ÉCHEC" /tmp/yami-stab-1.log /tmp/yami-stab-2.log /tmp/yami-stab-3.log | sort | uniq -c | sort -rn | head -10
echo "=== phases A6/A7 par passage ==="
grep -hoE "dftest (ope_expiry|pika_impact|yami_impact|ope_restart)\] §a[0-9]+§f/[0-9]+ PASS §c[0-9]+ FAIL" /tmp/yami-stab-*.log
