"""Single-file simulation saves, never used as Rogue Trader save extensions."""
import json
import os
from pathlib import Path
import tempfile


class JsonStore:
    def __init__(self, path):
        self.path = Path(path)

    def load(self):
        return json.loads(self.path.read_text(encoding="utf-8"))

    def save(self, state):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        fd, temporary = tempfile.mkstemp(prefix=self.path.name + ".", suffix=".tmp", dir=self.path.parent)
        try:
            with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as stream:
                json.dump(state, stream, sort_keys=True, indent=2, allow_nan=False)
                stream.write("\n")
                stream.flush()
                os.fsync(stream.fileno())
            os.replace(temporary, self.path)
        finally:
            if os.path.exists(temporary):
                os.unlink(temporary)

