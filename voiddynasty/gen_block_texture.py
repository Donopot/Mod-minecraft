from PIL import Image
import os

SIZE = 16
base = os.path.join(os.path.dirname(__file__), "src", "main", "resources", "assets", "umtest", "textures", "block")
os.makedirs(base, exist_ok=True)

img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
px = img.load()

# 4x4 cyan checkerboard — tileable, highly visible in-world
CELL = 4
C1 = (34, 211, 238, 255)   # cyan #22D3EE
C2 = (8, 95, 122, 255)     # dark cyan #085F7A
for y in range(SIZE):
    for x in range(SIZE):
        col = C1 if ((x // CELL) + (y // CELL)) % 2 == 0 else C2
        px[x, y] = col

out = os.path.join(base, "umtest_block.png")
img.save(out)
print("wrote", out)
