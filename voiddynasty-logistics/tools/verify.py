"""Run the milestone checks and save their actual output as a reviewable report."""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def main():
    report = ["VOID DYNASTY initial milestone verification\nSimulation tests; no in-game validation.\n"]
    for arguments in (["-m", "unittest", "discover", "-s", "tests", "-v"], ["-m", "void_bridge", "demo"]):
        result = subprocess.run([sys.executable, *arguments], cwd=ROOT, capture_output=True, text=True)
        report.append("$ python " + " ".join(arguments) + "\n" + result.stdout + result.stderr + f"\nExit code: {result.returncode}\n")
        if result.returncode:
            (ROOT / "docs/test-results.txt").write_text("\n".join(report), encoding="utf-8")
            print(result.stdout + result.stderr)
            return result.returncode
    (ROOT / "docs/test-results.txt").write_text("\n".join(report), encoding="utf-8")
    print("PASS: automated suite and lost-response demo. Report: docs/test-results.txt")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
