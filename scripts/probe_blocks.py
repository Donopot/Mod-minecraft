#!/usr/bin/env python3
"""Sonde read-only : quel bloc occupe chaque position ? Via RCON + data get comme feedback."""
import struct
import socket
import sys

HOST, PORT = "127.0.0.1", 25585
PW_FILE = "/opt/data/.rcon-hp-mini"
FEEDBACK = "data get entity @e[type=voiddynasty:servitor_2,limit=1] Pos"


class Rcon:
    def __init__(self):
        with open(PW_FILE, encoding="utf-8") as fh:
            pw = fh.read().strip()
        self.sock = socket.create_connection((HOST, PORT), timeout=30)
        self._send(1, 3, pw)
        if self._recv()[0] == -1:
            raise SystemExit("AUTH_FAILED")

    def _send(self, rid, rtype, body):
        payload = struct.pack("<ii", rid, rtype) + body.encode() + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(payload)) + payload)

    def _recv(self):
        (length,) = struct.unpack("<i", self.sock.recv(4))
        data = b""
        while len(data) < length:
            data += self.sock.recv(length - len(data))
        rid, rtype = struct.unpack("<ii", data[:8])
        return rid, rtype, data[8:-2].decode("utf-8", "replace")

    def cmd(self, command):
        self._send(2, 2, command)
        return self._recv()[2]


def probe(rc, pos, candidates):
    x, y, z = pos
    for block in candidates:
        out = rc.cmd(f"execute if block {x} {y} {z} {block} run {FEEDBACK}")
        if "entity data" in out:
            return block
    return "?"


def main():
    rc = Rcon()
    candidates = ["minecraft:air", "minecraft:rail", "minecraft:powered_rail",
                  "minecraft:grass_block", "minecraft:dirt", "minecraft:stone",
                  "minecraft:birch_planks", "minecraft:water", "minecraft:oak_planks"]
    pts = []
    for arg in sys.argv[1:]:
        pts.append(tuple(int(v) for v in arg.split(",")))
    for x, y, z in pts:
        print(f"{x} {y} {z} -> {probe(rc, (x, y, z), candidates)}")


if __name__ == "__main__":
    main()
