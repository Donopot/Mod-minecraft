#!/bin/bash
# Verifie un serveur voiddynasty par RCON : version chargee, sous-commandes /work,
# et harnais /vdyntest. Chaque controle peut echouer : rien n'est suppose vrai.
#
# Usage : ./verify_server_263.sh [conteneur]
#
# Code de sortie : 0 si tout est conforme, 1 sinon.
set -uo pipefail

CONTAINER=${1:-mc-263}
FAIL=0
rc() { docker exec "$CONTAINER" rcon-cli "$1" 2>&1; }
ok() { echo "  PASS  $1"; }
ko() { echo "  FAIL  $1"; FAIL=1; }

echo "=== disponibilite RCON ==="
if rc list | tail -1 | grep -q "players online"; then ok "RCON repond"; else ko "RCON muet"; echo "arret."; exit 1; fi

echo
echo "=== version chargee ==="
V=$(docker logs "$CONTAINER" 2>&1 | grep -aE "^\s+- voiddynasty" | tail -1 | tr -d '\t ' | sed 's/-voiddynasty//')
echo "  version : ${V:-inconnue}"
[ -n "$V" ] && ok "mod voiddynasty declare" || ko "mod voiddynasty absent du log de demarrage"
# grep -q fermerait le tube et pipefail prendrait le SIGPIPE pour un echec : on compte.
NSTART=$(docker logs "$CONTAINER" 2>&1 | grep -ac "SERVER_STARTED" || true)
[ "${NSTART:-0}" -gt 0 ] && ok "SERVER_STARTED vu ($NSTART)" || ko "SERVER_STARTED absent"

echo
echo "=== sous-commandes /work ==="
OUT=$(rc "work status")
echo "  status : $(echo "$OUT" | head -1 | cut -c1-120)"
echo "$OUT" | grep -qiE "chantier|aucun" && ok "work status repond" || ko "work status muet"

OUT=$(rc "work deposit")
echo "  deposit : $(echo "$OUT" | head -1 | cut -c1-120)"
echo "$OUT" | grep -qiE "depot|sac" && ok "work deposit enregistre" || ko "work deposit absent"

OUT=$(rc "work rollback 0")
if echo "$OUT" | grep -qiE "not be less than|refus|invalide"; then ok "work rollback 0 refuse (argument verifie)"
else ko "work rollback 0 n'a pas refuse : $(echo "$OUT" | head -1 | cut -c1-100)"; fi

echo
echo "=== harnais /vdyntest all ==="
BEFORE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
OUT=$(rc "vdyntest all")
echo "$OUT" | grep -aE "PASS|FAIL" | tail -3 | sed 's/^/  /'
PASS=$(echo "$OUT" | grep -aoE "[0-9]+ PASS" | tail -1 | grep -oE "[0-9]+")
FAILN=$(echo "$OUT" | grep -aoE "[0-9]+ FAIL" | tail -1 | grep -oE "[0-9]+")
echo "  bilan : ${PASS:-?} PASS / ${FAILN:-?} FAIL"
if [ -z "${PASS:-}" ]; then
  ko "harnais muet ou illisible"
else
  ok "harnais a repondu"
fi

# Le harnais n'est pas idempotent : sur un monde vivant il oscille entre 34/1 et 28/7
# (projectiles qui ne touchent pas, campagne dont les chunks ne sont pas charges).
# Seul F-campagne-recompenses est un defaut connu du test ; tout autre echec est signale.
sleep 2
NAMES=$(docker logs --since "$BEFORE" "$CONTAINER" 2>&1 | grep -a "\[FAIL\]" | sed -E 's/.*\[FAIL\][[:space:]]*//; s/ —.*//' | sort -u)
if [ -z "$NAMES" ]; then
  echo "  aucun test en echec dans ce passage"
else
  echo "  tests en echec dans ce passage :"
  echo "$NAMES" | sed 's/^/    - /'
  UNKNOWN=$(echo "$NAMES" | grep -v "^F-campagne-recompenses$" || true)
  if [ -n "$UNKNOWN" ]; then
    ko "echecs non connus : $(echo "$UNKNOWN" | tr '\n' ' ')"
  else
    ok "seul le defaut connu F-campagne-recompenses est present"
  fi
fi

echo
if [ "$FAIL" = "0" ]; then echo "RESULTAT : conforme"; else echo "RESULTAT : ecarts detectes"; fi
exit "$FAIL"
