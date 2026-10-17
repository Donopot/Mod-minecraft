#!/bin/bash
# Stabilité : 3 passages. Compte les échecs ET relève la géométrie de l'acteur + les couloirs.
export JAVA_HOME=/opt/data/jdk/jdk-25.0.4.1+1
export PATH="$JAVA_HOME/bin:$PATH"
cd /opt/data/workspace/opworldlike-fabric || exit 1

./gradlew jar --no-daemon > /tmp/mori13.log 2>&1
if ! grep -q "BUILD SUCCESSFUL" /tmp/mori13.log; then
   echo "BUILD FAILED"
   grep -E "error:" -A3 /tmp/mori13.log | head -10
   exit 1
fi
echo "BUILD OK"

python3 tools/mc_kill_server.py --kill
cd /opt/data/workspace/mc-server || exit 1
rm -f world8/session.lock
cd /opt/data/workspace/opworldlike-fabric || exit 1

for i in 1 2 3; do
   python3 tools/run_tests.py > "/tmp/stabD-$i.log" 2>&1
   echo "--- passage $i : $(grep -E 'RÉSULTAT GLOBAL' "/tmp/stabD-$i.log")"
   grep -E "ÉCHEC" "/tmp/stabD-$i.log" | head -3
done

echo "=== bilan des échecs sur 3 passages ==="
grep -hE "ÉCHEC" /tmp/stabD-1.log /tmp/stabD-2.log /tmp/stabD-3.log | sort | uniq -c | sort -rn | head -10
echo "=== W2 : acteur observe a chaque passage ==="
grep -hoE "acteur : hauteur d'yeux [0-9.]+ \(attendu [0-9.]+, sain = [a-z]+\)" /tmp/stabD-*.log | sort | uniq -c
echo "=== W2 : issue ==="
grep -hoE "cibles prises = [0-9]+" /tmp/stabD-*.log | sort | uniq -c
