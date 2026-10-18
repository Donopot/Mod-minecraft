#!/usr/bin/env python3
"""mock_llm.py — faux endpoint compatible OpenAI pour tester le cerveau du Servitor.

Sert `POST /v1/chat/completions` et joue un scénario DÉTERMINISTE, sans clé d'API ni vrai
modèle. Deux modes, parce qu'un modèle local sait ou non appeler un outil :

    --mode tool   réponse avec `tool_calls` (format natif OpenAI)
    --mode text   réponse avec un JSON dans `content` (modèles sans mode outil)

Le scénario est « conscient de l'état » : si le prompt dit qu'une tâche est en cours
(`"tache":"miner ..."`), le faux modèle répond `wait` au lieu d'empiler un ordre — c'est ce
que ferait un vrai modèle correctement guidé, et ça évite d'annuler la tâche en cours.

Usage :
    python3 mock_llm.py --port 8099 --log /tmp/mock_llm.log [--mode tool|text]
"""
import argparse
import json
import re
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

# Le 2e ordre volontairement hors limites DOIT être refusé par le serveur.
SCRIPT = [
    ("say", {"message": "Servitor pret. Dis-moi ce que tu veux."}),
    ("scan", {"block": "dirt", "radius": 8}),
    ("mine_block", {"block": "dirt", "count": 6, "radius": 8}),
    ("pickup", {"radius": 10, "count": 16}),
    ("goto", {"x": 999999, "y": 64, "z": 0}),
    ("stop", {}),
]

LOCK = threading.Lock()
REQ = {"n": 0}
STEP = {"i": 0}
LOGPATH = {"p": None}
MODE = {"m": "tool"}


def log(line):
    path = LOGPATH["p"]
    if path:
        with open(path, "a", encoding="utf-8") as fh:
            fh.write(line + "\n")


def current_task(prompt):
    m = re.search(r'"tache":"((?:[^"\\]|\\.)*)"', prompt)
    return m.group(1) if m else "aucune"


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, format, *args):  # silence
        pass

    def do_GET(self):
        if self.path.rstrip("/").endswith("/models"):
            data = json.dumps({"object": "list", "data": [
                {"id": "mock-llm", "object": "model"},
                {"id": "mock-llm-text", "object": "model"},
            ]}).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        else:
            self.send_response(404)
            self.send_header("Content-Length", "0")
            self.end_headers()

    def do_POST(self):
        length = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(length).decode("utf-8", "replace")
        try:
            payload = json.loads(raw)
        except Exception:
            payload = {}

        prompt = ""
        for msg in payload.get("messages", []):
            if msg.get("role") == "user":
                prompt = (msg.get("content") or "")[:6000]

        task = current_task(prompt)
        busy = not task.lower().startswith("aucune")

        with LOCK:
            REQ["n"] += 1
            n = REQ["n"]
            if busy:
                name, args = "wait", {"seconds": 2}
                why = "tache en cours : %s" % task[:60]
            else:
                i = STEP["i"]
                name, args = SCRIPT[i] if i < len(SCRIPT) else ("wait", {"seconds": 5})
                STEP["i"] += 1
                why = "etape %d du scenario" % (i + 1)

        log("=" * 70)
        log("REQUETE #%d mode=%s modele=%s outils=%d" % (
            n, MODE["m"], payload.get("model"), len(payload.get("tools") or [])))
        log("PROMPT (extrait):\n" + prompt[:2500])
        log("REPONSE -> %s %s   (%s)" % (name, json.dumps(args, ensure_ascii=False), why))

        message: dict = {"role": "assistant", "content": ""}
        if MODE["m"] == "tool":
            message["content"] = ""
            message["tool_calls"] = [{
                "id": "call_%d" % n,
                "type": "function",
                "function": {"name": name, "arguments": json.dumps(args)},
            }]
            finish = "tool_calls"
        else:
            # JSON dans le texte : doit être reconnu par BrainLlm.orderFromText
            message["content"] = json.dumps({"tool": name, "args": args}, ensure_ascii=False)
            finish = "stop"

        body = {
            "id": "mock-%d" % n,
            "object": "chat.completion",
            "model": payload.get("model", "mock"),
            "choices": [{"index": 0, "message": message, "finish_reason": finish}],
        }
        data = json.dumps(body).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--port", type=int, default=8099)
    ap.add_argument("--log", default="/tmp/mock_llm.log")
    ap.add_argument("--mode", choices=["tool", "text"], default="tool")
    args = ap.parse_args()
    LOGPATH["p"] = args.log
    MODE["m"] = args.mode
    open(args.log, "w", encoding="utf-8").close()
    srv = ThreadingHTTPServer(("127.0.0.1", args.port), Handler)
    print("mock LLM sur 127.0.0.1:%d, mode %s, journal %s" % (args.port, args.mode, args.log), flush=True)
    srv.serve_forever()


if __name__ == "__main__":
    main()
