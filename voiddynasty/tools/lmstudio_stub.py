"""Faux LM Studio sur 127.0.0.1:1234, calque sur les logs reels de Donopot.

Comportements reproduits :

1. Hors `/v1`, LM Studio repond 200 avec un corps qui n'est pas une reponse de chat.
2. Le serveur ne charge qu'un modele mais repond quand meme a un id inconnu.
3. Un modele peut ecrire l'appel en texte : `mine_block{"block": "dirt"}` sans tool_calls.
4. Un modele peut boucler sur une tache longue (ici dig_box) et ignorer la consigne de
   l'operateur tant qu'on ne lui annule pas sa tache.

Le stub suit ces etapes : tant qu'aucune consigne n'est presente il renvoie dig_box (tache
longue). Des qu'une CONSIGNE apparait il continue dig_box (modele tetu). Quand le cerveau a
annule la tache, il obeit et renvoie mine_block.
"""
import json
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

MODEL = "qwen2.5-7b-instruct-1m"
CALLS = {"n": 0}


def tool_call(name, args):
    return {"id": "call_%d" % CALLS["n"], "type": "function",
            "function": {"name": name, "arguments": json.dumps(args)}}


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, format, *args):
        sys.stderr.write("[stub] " + (format % args) + "\n")

    def _json(self, payload, code=200):
        body = json.dumps(payload).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _chat(self, model, content="", tool_calls=None, finish="stop"):
        message = {"role": "assistant", "content": content, "reasoning_content": ""}
        if tool_calls:
            message["tool_calls"] = tool_calls
            finish = "tool_calls"
        self._json({
            "id": "chatcmpl-stub-%d" % CALLS["n"],
            "object": "chat.completion",
            "model": model,
            "choices": [{"index": 0, "message": message, "finish_reason": finish}],
            "usage": {"prompt_tokens": 100, "completion_tokens": 20},
        })

    def do_GET(self):
        if self.path.rstrip("/") == "/v1/models":
            self._json({"object": "list", "data": [
                {"id": MODEL, "object": "model", "owned_by": "organization_owner"}]})
        else:
            self._json({"error": "Unexpected endpoint or method. (GET %s). Returning 200 anyway"
                                  % self.path})

    def do_POST(self):
        length = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(length)
        if self.path.rstrip("/") != "/v1/chat/completions":
            self._json({"error": "Unexpected endpoint or method. (POST %s). Returning 200 anyway"
                                  % self.path})
            return
        try:
            request = json.loads(raw or b"{}")
        except Exception:
            request = {}
        CALLS["n"] += 1
        model = request.get("model")
        user = " ".join(m.get("content") or "" for m in request.get("messages", [])
                        if m.get("role") == "user")

        if not request.get("tools"):
            # demande d'arguments JSON (repli quand le modele ecrit l'appel en texte)
            self._chat(model, content='{"block": "dirt", "count": 3, "radius": 8}')
            return

        if user.startswith("ORDRE DE L'OPERATEUR"):
            if "routine" in user:
                sys.stderr.write("[stub] mode ordre -> routine_gather\n")
                spec = ('{"schema":1,"type":"GATHER","request_id":"routine-test-1",'
                        '"item":"minecraft:cobblestone","amount":2,'
                        '"min":[6505,64,6499],"max":[6507,66,6501],'
                        '"destination":[6510,64,6500],"origin":[6502,64,6502]}')
                self._chat(model, tool_calls=[tool_call("routine_gather", {"spec": spec})])
            elif "ramasse" in user:
                sys.stderr.write("[stub] mode ordre -> pickup\n")
                self._chat(model, tool_calls=[tool_call("pickup", {"radius": 12, "count": 16})])
            elif "trouve" in user:
                sys.stderr.write("[stub] mode ordre -> find\n")
                self._chat(model, tool_calls=[tool_call("find",
                                                        {"block": "gold_block", "radius": 16, "limit": 4})])
            elif "dessine" in user:
                sys.stderr.write("[stub] mode ordre -> box\n")
                self._chat(model, tool_calls=[tool_call("box",
                                                        {"x": 6406, "y": 100, "z": 6406, "half": 4, "layers": 2})])
            elif "retiens" in user:
                sys.stderr.write("[stub] mode ordre -> remember\n")
                self._chat(model, tool_calls=[tool_call("remember",
                                                        {"note": "filon de fer repere pres de la paroi est"})])
            elif "memoire" in user:
                sys.stderr.write("[stub] mode ordre -> recall\n")
                self._chat(model, tool_calls=[tool_call("recall", {})])
            else:
                sys.stderr.write("[stub] mode ordre -> probe\n")
                self._chat(model, tool_calls=[tool_call("probe", {"radius": 8})])
            return
        if "realise-la-maintenant" in user:
            sys.stderr.write("[stub] obeit -> mine_block\n")
            self._chat(model, tool_calls=[tool_call("mine_block",
                                                    {"block": "dirt", "count": 3, "radius": 8})])
        elif "CONSIGNE DE L'OPERATEUR" in user:
            sys.stderr.write("[stub] tetu -> relance dig_box malgre la consigne\n")
            self._chat(model, tool_calls=[tool_call("dig_box",
                                                    {"width": 8, "length": 8, "depth": 2})])
        else:
            sys.stderr.write("[stub] tache longue -> dig_box\n")
            self._chat(model, tool_calls=[tool_call("dig_box",
                                                    {"width": 8, "length": 8, "depth": 2})])


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 1234
    ThreadingHTTPServer(("127.0.0.1", port), Handler).serve_forever()
