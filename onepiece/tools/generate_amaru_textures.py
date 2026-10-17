"""Reproducible, locally authored electric body/energy textures. No external image files."""
from pathlib import Path
import argparse, math, struct, zlib

DEST = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/minepiece/textures/entities'

def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

def encode(pixel):
    data = b''.join(b'\0' + bytes(v for x in range(64) for v in pixel(x, y)) for y in range(64))
    return (b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 64, 64, 8, 6, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(data)) + chunk(b'IEND', b''))

def body(x, y):
    if x >= 60 and y >= 60:
        return 235, 253, 255, 255  # UV patch for the small emissive eyes.
    a = (math.sin(x * .7 + y * .43) + math.sin(y * .61 - x * .21)) * .5
    if abs((x + int(3 * math.sin(y * .6))) % 16 - 8) < 1:
        return 190, 242, 255, 255
    return int(55 + a * 12), int(146 + a * 18), int(205 + a * 15), 255

def energy(x, y):
    return (160, 225, 255, 210) if abs((x + int(4 * math.sin(y * .55))) % 16 - 8) <= 1 else (0, 0, 0, 0)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for name, pixel in [('amaru_body.png', body), ('amaru_energy.png', energy)]:
        path = DEST / name
        data = encode(pixel)
        if args.check:
            if path.read_bytes() != data:
                raise SystemExit('Texture differs from generator: ' + str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        print('AMARU_TEXTURE_PASS', name)
