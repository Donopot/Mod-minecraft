from PIL import Image
import os, math

SIZE = 16
base = os.path.join(os.path.dirname(__file__), "src", "main", "resources", "assets", "umtest", "textures", "item")
os.makedirs(base, exist_ok=True)

img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
px = img.load()
cx = cy = 7.5
R = 6.5

for y in range(SIZE):
    for x in range(SIZE):
        d = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
        if d <= R:
            t = d / R  # 0 = center, 1 = edge
            # white-hot core -> cyan/blue energy edge
            r = int(255 * (1 - t) + 34 * t)
            g = int(255 * (1 - t) + 150 * t)
            b = int(255 * (1 - t) + 255 * t)
            px[x, y] = (r, g, b, 255)

out = os.path.join(base, "energy_wave.png")
img.save(out)
print("wrote", out)
