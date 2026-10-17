#!/usr/bin/env python3
"""vdctl.py — ligne de commande des outils VOID DYNASTY (même cœur que le serveur MCP).

Usage :
    vdctl.py status
    vdctl.py vdyntest [scope]
    vdctl.py scan <x1> <y1> <z1> <x2> <y2> <z2> [dimension]
    vdctl.py ship <type> <x> <y> <z>
    vdctl.py fleet <action> <joueur> [nom] [extra]
    vdctl.py cmd "<commande autorisée>"

Sortie : JSON sur stdout, code retour 0 si l'outil a réussi.
"""
import json
import sys

import vd_core


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    what = argv[1]
    try:
        if what == "status":
            res = vd_core.server_status()
        elif what == "vdyntest":
            res = vd_core.vdyntest(argv[2] if len(argv) > 2 else "all")
        elif what == "scan":
            if len(argv) < 8:
                print(__doc__)
                return 2
            dim = argv[8] if len(argv) > 8 else "overworld"
            res = vd_core.scan_zone(*[int(v) for v in argv[2:8]], dimension=dim)
        elif what == "ship":
            res = vd_core.place_ship(argv[2], int(argv[3]), int(argv[4]), int(argv[5]))
        elif what == "fleet":
            res = vd_core.fleet(argv[2], argv[3],
                                argv[4] if len(argv) > 4 else None,
                                " ".join(argv[5:]) if len(argv) > 5 else None)
        elif what == "cmd":
            res = vd_core.command(" ".join(argv[2:]))
        else:
            print("outil inconnu : %s" % what)
            return 2
    except Exception as e:
        print(json.dumps({"error": "%s: %s" % (type(e).__name__, e)}, ensure_ascii=False))
        return 1
    print(json.dumps(res, ensure_ascii=False, indent=2))
    if isinstance(res, dict) and (res.get("refused") or res.get("error")):
        return 1
    if isinstance(res, dict) and res.get("fail"):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
