#!/usr/bin/env python3
"""Minimal RCON client (no deps). Usage: rcon.py '<commande>' [plusieurs commandes...]"""
import socket
import struct
import sys

HOST = "127.0.0.1"
PORT = 25585
PW_FILE = "/opt/data/.rcon-hp-mini"


def pack(rid, rtype, body):
    payload = struct.pack("<ii", rid, rtype) + body.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(payload)) + payload


def recv_packet(sock):
    raw_len = sock.recv(4)
    if len(raw_len) < 4:
        raise ConnectionError("connexion fermee par le serveur")
    (length,) = struct.unpack("<i", raw_len)
    data = b""
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            raise ConnectionError("tronque")
        data += chunk
    rid, rtype = struct.unpack("<ii", data[:8])
    return rid, rtype, data[8:-2].decode("utf-8", "replace")


def main():
    with open(PW_FILE, "r", encoding="utf-8") as fh:
        password = fh.read().strip()
    cmds = sys.argv[1:]
    if not cmds:
        print("usage: rcon.py '<commande>' ...")
        return 2
    sock = socket.create_connection((HOST, PORT), timeout=15)
    sock.sendall(pack(1, 3, password))
    rid, _, body = recv_packet(sock)
    if rid == -1:
        print("AUTH_FAILED")
        return 1
    for cmd in cmds:
        sock.sendall(pack(2, 2, cmd))
        _, _, body = recv_packet(sock)
        print(">> " + cmd)
        print(body)
    sock.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
