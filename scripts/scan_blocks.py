#!/usr/bin/env python3
"""Comptage non destructif de blocs dans une boite via RCON.

Utilise /fill <min> <max> <bloc> replace <bloc> : le bloc est remplace par lui-meme,
aucune modification, mais le serveur renvoie le nombre de blocs correspondants.
Les chunks doivent etre charges (voir --forceload).
"""
import argparse
import struct
import socket
import re
import sys

HOST, PORT = "127.0.0.1", 25585
PW_FILE = "/opt/data/.rcon-hp-mini"


class Rcon:
    def __init__(self):
        with open(PW_FILE, encoding="utf-8") as fh:
            self.pw = fh.read().strip()
        self.sock = socket.create_connection((HOST, PORT), timeout=30)
        self._send(1, 3, self.pw)
        rid, _, body = self._recv()
        if rid == -1:
            raise SystemExit("AUTH_FAILED")
        self._n = 1

    def _send(self, rid, rtype, body):
        payload = struct.pack("<ii", rid, rtype) + body.encode() + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(payload)) + payload)

    def _recv(self):
        raw = self.sock.recv(4)
        (length,) = struct.unpack("<i", raw)
        data = b""
        while len(data) < length:
            data += self.sock.recv(length - len(data))
        rid, rtype = struct.unpack("<ii", data[:8])
        return rid, rtype, data[8:-2].decode("utf-8", "replace")

    def cmd(self, command):
        self._send(2, 2, command)
        return self._recv()[2]


def count(rc, block, lo, hi):
    a = f"{lo[0]} {lo[1]} {lo[2]}"
    b = f"{hi[0]} {hi[1]} {hi[2]}"
    out = rc.cmd(f"fill {a} {b} {block} replace {block}")
    if "filled" in out:
        m = re.search(r"(\d+) block", out)
        return int(m.group(1)) if m else 0
    return ("ERR", out.strip())


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--min", nargs=3, type=int, required=True)
    ap.add_argument("--max", nargs=3, type=int, required=True)
    ap.add_argument("--blocks", nargs="+", default=["minecraft:sand", "minecraft:water",
                                                    "minecraft:gravel", "minecraft:stone"])
    ap.add_argument("--forceload", action="store_true")
    ap.add_argument("--unforceload", action="store_true")
    args = ap.parse_args()
    rc = Rcon()
    lo, hi = args.min, args.max
    if args.forceload:
        print(rc.cmd(f"forceload add {lo[0]} {lo[2]} {hi[0]} {hi[2]}").strip())
    for b in args.blocks:
        print(f"{b}: {count(rc, b, lo, hi)}")
    if args.unforceload:
        print(rc.cmd(f"forceload remove {lo[0]} {lo[2]} {hi[0]} {hi[2]}").strip())


if __name__ == "__main__":
    sys.exit(main())
