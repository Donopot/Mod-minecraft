"""Own low-poly block models; reference installed textures without copying any bitmap."""
from pathlib import Path
import json
from bridge_project import RESOURCES

ROOT = RESOURCES / "resourcepacks/refit"
ASSETS = ROOT / "assets"
FACES = ("north", "south", "west", "east", "up", "down")


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")


def box(lo, hi, texture, cull=False):
    return {"from": lo, "to": hi, "faces": {
        face: {"texture": "#" + texture, **({"cullface": face} if cull else {})} for face in FACES}}


def model(name, elements, textures, namespace="voiddynasty"):
    write(ASSETS / namespace / "models/block" / (name + ".json"), {
        "parent": "minecraft:block/block", "ambientocclusion": True,
        "textures": {"particle": textures.get("panel", next(iter(textures.values()))), **textures}, "elements": elements})


def item(name, namespace="voiddynasty"):
    write(ASSETS / namespace / "models/item" / (name + ".json"), {"parent": f"{namespace}:block/{name}"})


def reinforced(panel, accent="minecraft:block/iron_block"):
    tex = {"panel": panel, "frame": "minecraft:block/polished_blackstone", "metal": accent}
    parts = [box([.5, .5, .5], [15.5, 15.5, 15.5], "panel", True)]
    for x in (0, 14):
        for z in (0, 14):
            parts.append(box([x, 0, z], [x+2, 16, z+2], "frame", True))
    for y in (0, 14.5):
        parts.append(box([0, y, 0], [16, y+1.5, 16], "metal", True))
    return parts, tex


def main():
    write(ROOT / "pack.mcmeta", {"pack": {"description": "VOID DYNASTY — Refonte industrielle", "min_format": [97, 1], "max_format": [97, 1]}})
    names = ["ceramite_block"] + [c+"_ceramite_block" for c in (
        "blue", "red", "black", "green", "dark_green", "light_blue", "white", "yellow", "pink")]
    for name in names:
        elements, tex = reinforced("voiddynasty:block/" + name)
        model(name, elements, tex);item(name)

    for name, panel, accent in [("super_chest", "minecraft:block/green_terracotta", "minecraft:block/iron_block"),
                                ("super_container", "minecraft:block/cyan_terracotta", "minecraft:block/copper_block"),
                                ("ammo_crate", "minecraft:block/green_terracotta", "minecraft:block/copper_block")]:
        elements, tex = reinforced(panel, accent)
        tex.update(latch="minecraft:block/gold_block", vent="minecraft:block/black_concrete")
        elements += [box([6.5, 6, 0], [9.5, 10, .5], "latch"), box([3, 10, 0], [5, 12, .5], "vent"), box([11, 10, 0], [13, 12, .5], "vent")]
        model(name, elements, tex);item(name)
        if name == "super_chest":model("cargo", elements, tex, "voiddynasty_bridge");item("cargo", "voiddynasty_bridge")

    for name, vanilla in [("promethium_furnace", "furnace"), ("promethium_blast_furnace", "blast_furnace"), ("promethium_smoker", "smoker")]:
        elements, tex = reinforced("minecraft:block/polished_deepslate", "minecraft:block/copper_block")
        tex.update(front="minecraft:block/"+vanilla+"_front", glow="minecraft:block/redstone_block")
        elements += [box([3, 3, 0], [13, 12, .6], "front"), box([14, 5, 2], [16, 11, 14], "frame")]
        model(name, elements, tex);model(name+"_on", elements, {**tex, "front": "minecraft:block/"+vanilla+"_front_on"});item(name)
        variants = {}
        for facing, turn in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
            for lit in (False, True):variants[f"facing={facing},lit={str(lit).lower()}"] = {"model": "voiddynasty:block/"+name+("_on" if lit else ""), "y": turn}
        write(ASSETS / "voiddynasty/blockstates" / (name+".json"), {"variants": variants})

    elements, tex = reinforced("minecraft:block/gray_concrete", "minecraft:block/copper_block")
    tex.update(vent="minecraft:block/black_concrete", warning="minecraft:block/yellow_concrete", drill="minecraft:block/iron_block")
    elements += [box([3, 4, 0], [13, 12, .5], "vent"), box([4, 3, 0], [6, 13, 1], "warning"), box([10, 3, 0], [12, 13, 1], "warning")]
    model("mining_rig", elements, tex);item("mining_rig")

    tex = {"panel": "minecraft:block/cut_copper", "band": "minecraft:block/polished_blackstone"}
    parts = [box([2, 0, 2], [14, 16, 14], "panel"), box([1, 2, 1], [15, 4, 15], "band"), box([1, 12, 1], [15, 14, 15], "band")]
    model("barrel", parts, tex);item("barrel")
    parts = [box([3, 0, 3], [13, 2, 13], "frame"), box([4, 2, 4], [12, 11, 12], "panel"), box([5, 11, 5], [11, 14, 11], "head"), box([6, 14, 6], [10, 16, 10], "head")]
    model("cannon_shell", parts, {"panel": "minecraft:block/copper_block", "frame": "minecraft:block/polished_blackstone", "head": "minecraft:block/gold_block"});item("cannon_shell")
    parts = []
    for y, z, xs in [(0, 1, (0, 8)), (5, 2, (0, 8)), (10, 1, (0, 8))]:
        for x in xs:parts.append(box([x+.3, y, z], [x+7.7, y+5, z+12], "panel"))
    model("sandbags", parts, {"panel": "minecraft:block/brown_terracotta"});item("sandbags")
    parts = [box([0, 0, 6], [16, 16, 10], "panel"), box([0, 0, 4], [2, 16, 12], "frame"), box([14, 0, 4], [16, 16, 12], "frame"),
             box([2, 0, 5], [14, 2, 11], "metal"), box([2, 14, 5], [14, 16, 11], "metal"), box([7, 4, 5], [9, 12, 11], "metal")]
    model("gothic_wall", parts, {"panel": "minecraft:block/polished_deepslate", "frame": "minecraft:block/polished_blackstone", "metal": "minecraft:block/copper_block"});item("gothic_wall")
    tex = {"panel": "minecraft:block/iron_block", "pipe": "minecraft:block/copper_block", "bottle": "minecraft:block/cyan_concrete", "cap": "minecraft:block/gold_block"}
    base = [box([1, 0, 1], [15, 2, 15], "panel"), box([7, 2, 7], [9, 15, 9], "pipe"), box([2, 13, 7], [14, 15, 9], "pipe")]
    model("promethium_brewing_stand", base, tex);item("promethium_brewing_stand")
    variants = {}
    for mask in range(8):
        parts = list(base)
        for i, x in enumerate((2, 6.5, 11)):
            if mask & (1 << i):parts += [box([x, 2, 3], [x+3, 8, 6], "bottle"), box([x+.7, 8, 3.7], [x+2.3, 10, 5.3], "cap")]
        model(f"promethium_brewing_stand_{mask}", parts, tex)
        variants[",".join(f"has_bottle_{i}={str(bool(mask & (1 << i))).lower()}" for i in range(3))] = {"model": f"voiddynasty:block/promethium_brewing_stand_{mask}"}
    write(ASSETS / "voiddynasty/blockstates/promethium_brewing_stand.json", {"variants": variants})
    print("Generated own refit resources:", len(list(ROOT.rglob("*.json"))), "JSON files")


if __name__ == "__main__":
    main()
