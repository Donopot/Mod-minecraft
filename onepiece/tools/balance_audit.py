#!/usr/bin/env python3
"""CORE-025 — audit d'équilibrage de la couche Fruits du Démon.

Extrait statiquement toutes les techniques déclarées dans DevilFruitRegistry (constructeur
`new DevilFruitMove(...)` et fabriques `melee/projectile/buff/areaEffect`), en déduit la distribution
réelle des valeurs et signale les aberrations de puissance (dégâts hors budget, coûts incohérents).

Les fabriques ont deux formes, distinguées par le NOMBRE d'arguments (fiable) plutôt que par
l'inspection des littéraux :
    melee      (id, name, dmg, range,    knockback,                    energy, cd, particle)          -> 8
    melee      (id, name, dmg, range,    knockback, effect, dur, amp, energy, cd, particle)          -> 11
    projectile (id, name, dmg, lifetime, knockback,                    energy, cd, particle[, speed]) -> 8-9
    projectile (id, name, dmg, lifetime, knockback, effect, dur, amp, energy, cd, particle[, speed]) -> 11-12
    buff/areaEffect (id, name, effect, dur, amp, energy, cd, particle)                                -> 8

Usage : python3 tools/balance_audit.py [--json] [--csv out.csv]
"""
import csv
import json
import os
import re
import statistics
import sys

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(PROJECT, "src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java")


def split_args(s):
    """Découpe une liste d'arguments Java (parenthèses/crochets/chaînes respectés)."""
    out, depth, cur, instr = [], 0, [], False
    for ch in s:
        if instr:
            cur.append(ch)
            if ch == '"':
                instr = False
            continue
        if ch == '"':
            instr = True
            cur.append(ch)
        elif ch in "([{":
            depth += 1
            cur.append(ch)
        elif ch in ")]}":
            depth -= 1
            cur.append(ch)
        elif ch == "," and depth == 0:
            out.append("".join(cur).strip())
            cur = []
        else:
            cur.append(ch)
    if cur:
        out.append("".join(cur).strip())
    return out


def call_args(src, start):
    """Arguments de l'appel dont la parenthèse ouvrante suit `start`."""
    i = src.index("(", start) + 1
    depth, j, instr = 1, i, False
    while j < len(src) and depth > 0:
        c = src[j]
        if instr:
            if c == '"':
                instr = False
        elif c == '"':
            instr = True
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
        j += 1
    return split_args(src[i:j - 1])


def lit(a):
    """Littéral Java -> valeur Python (str / float), sinon None."""
    a = (a or "").strip()
    if len(a) >= 2 and a[0] == '"' and a[-1] == '"':
        return a[1:-1]
    if re.fullmatch(r"-?\d+(?:\.\d+)?[dDfF]?", a):
        return float(re.sub(r"[dDfF]$", "", a))
    return None


def num(a, default=0.0):
    v = lit(a)
    return v if isinstance(v, (int, float)) else default


def extract(src):
    """Une seule passe sur le fichier : forme d'appel + fruit courant (dernier MOVES.put en amont)."""
    markers = [(m.start(), m.group(1)) for m in
               re.finditer(r"MOVES\.put\(MinepieceModItems\.(\w+)", src)]

    def fruit_at(pos):
        name = "?"
        for off, f in markers:
            if off < pos:
                name = f
            else:
                break
        return name

    rows = []
    pattern = re.compile(r"new\s+DevilFruitMove\s*\(|DevilFruitMove\.(melee|projectile|buff|areaEffect)\s*\(")
    for m in pattern.finditer(src):
        a = call_args(src, m.start())
        fruit = fruit_at(m.start())
        if m.group(0).startswith("new"):
            if len(a) < 15:
                continue
            tmatch = re.search(r"MoveType\.(\w+)", a[2])
            rows.append(dict(
                fruit=fruit, id=lit(a[0]), name=lit(a[1]),
                type=tmatch.group(1) if tmatch else "?",
                damage=num(a[3]), range=num(a[4]), knockback=num(a[5]),
                effect=(lit(a[6]) or ""), eff_dur=int(num(a[7])), eff_amp=int(num(a[8])),
                energy=num(a[9]), cooldown=int(num(a[10])),
                lifetime=int(num(a[13])), speed=num(a[14]), form="ctor"))
            continue

        fab, typ = m.group(1), {"melee": "MELEE", "projectile": "PROJECTILE",
                                "buff": "BUFF", "areaEffect": "EFFECT"}[m.group(1)]
        if len(a) < 7:
            continue
        d = dict(fruit=fruit, id=lit(a[0]), name=lit(a[1]), type=typ, damage=0.0, range=0.0,
                 knockback=0.0, effect="", eff_dur=0, eff_amp=0, energy=0.0, cooldown=0,
                 lifetime=0, speed=0.0, form=fab)
        if typ in ("MELEE", "PROJECTILE"):
            d["damage"] = num(a[2])
            if typ == "MELEE":
                d["range"] = num(a[3])
            else:
                d["lifetime"] = int(num(a[3]))
                if len(a) in (9, 12):            # variante avec vitesse explicite
                    d["speed"] = num(a[-1])
            d["knockback"] = num(a[4])
            if len(a) >= 11:                     # variante avec effet de statut
                d["effect"] = lit(a[5]) or ""
                d["eff_dur"] = int(num(a[6]))
                d["eff_amp"] = int(num(a[7]))
                d["energy"] = num(a[8])
                d["cooldown"] = int(num(a[9]))
            else:
                d["energy"] = num(a[5])
                d["cooldown"] = int(num(a[6]))
        else:                                    # buff / areaEffect
            d["effect"] = lit(a[2]) or ""
            d["eff_dur"] = int(num(a[3]))
            d["eff_amp"] = int(num(a[4]))
            d["energy"] = num(a[5])
            d["cooldown"] = int(num(a[6]))
        rows.append(d)
    return rows


def main():
    rows = extract(open(SRC, encoding="utf-8").read())
    seen, uniq = set(), []
    for r in rows:
        k = (r.get("fruit"), r["id"], r["type"])
        if k in seen:
            continue
        seen.add(k)
        uniq.append(r)
    rows = uniq

    if "--json" in sys.argv:
        print(json.dumps(rows, ensure_ascii=False, indent=1))
        return 0
    if "--csv" in sys.argv:
        out = sys.argv[sys.argv.index("--csv") + 1]
        with open(out, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
            w.writeheader()
            w.writerows(rows)
        print("csv:", out, len(rows), "lignes")
        return 0

    fruits = sorted({r["fruit"] for r in rows})
    dmg = sorted((r for r in rows if r["damage"]), key=lambda r: -r["damage"])
    e = [r["energy"] for r in rows]
    c = [r["cooldown"] for r in rows]

    print(f"=== {len(rows)} techniques, {len(fruits)} fruits ===\n")
    print("--- top 12 dégâts ---")
    for r in dmg[:12]:
        print(f"  {r['damage']:>10.1f}  {r['type']:<10} énergie={r['energy']:<6.0f} cd={r['cooldown']:<4} "
              f"{r['fruit'][:26]:<26} {r['id']}")
    print("\n--- 6 plus faibles (dégâts > 0) ---")
    for r in dmg[-6:]:
        print(f"  {r['damage']:>10.1f}  {r['type']:<10} {r['fruit'][:26]:<26} {r['id']}")

    vals = [r["damage"] for r in dmg]
    med = statistics.median(vals)
    print(f"\ndégâts  : n={len(vals)} min={min(vals):.1f} médiane={med:.1f} max={max(vals):.1f}")
    print(f"énergie : min={min(e):.0f} médiane={statistics.median(e):.0f} max={max(e):.0f}")
    print(f"cooldown: min={min(c)} médiane={statistics.median(c):.0f} max={max(c)}")
    print(f"incohérences énergie : {sum(1 for r in rows if r['damage'] > 0 and r['energy'] <= 0)} "
          f"technique(s) à dégâts mais coût nul")

    print("\n--- hors budget (> 60 dégâts = 3× les PV de base) ---")
    out = [r for r in dmg if r["damage"] > 60]
    for r in out:
        print(f"  {r['damage']:>10.1f}  {r['fruit'][:28]:<28} {r['id']:<20} "
              f"(énergie={r['energy']:.0f}, cd={r['cooldown']})")
    print(f"  total : {len(out)} / {len(dmg)} techniques offensives")
    return 0


if __name__ == "__main__":
    sys.exit(main())
