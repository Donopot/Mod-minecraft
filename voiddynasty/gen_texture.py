from PIL import Image
import os

SIZE = 16
base = os.path.join(os.path.dirname(__file__), "src", "main", "resources", "assets", "umtest", "textures", "item")
os.makedirs(base, exist_ok=True)

img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
px = img.load()

CX = CY = 7.5
for y in range(SIZE):
    for x in range(SIZE):
        d = abs(x - CX) + abs(y - CY)  # Manhattan distance -> diamond (rotated square)
        if d <= 6.6:
            if d > 5.0:
                # rim / outline (darker cyan)
                col = (14, 116, 144, 255)   # #0E7490
            else:
                # facet gradient: light (top-left) -> cyan (bottom-right)
                t = (x + y) / (2.0 * SIZE)
                r = int(165 + (34 - 165) * t)
                g = int(243 + (211 - 243) * t)
                b = int(252 + (238 - 252) * t)
                col = (r, g, b, 255)
            px[x, y] = col

out = os.path.join(base, "umtest_item.png")
img.save(out)
print("wrote", out)
