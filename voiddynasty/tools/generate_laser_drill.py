"""Generate the native cuboid drill and its four focusing-rotor frames.
All materials reference Minecraft block textures; no third-party assets are bundled.
"""
import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/voiddynasty"
TEXTURES = {
    "casing": "minecraft:block/polished_blackstone",
    "steel": "minecraft:block/iron_block",
    "brass": "minecraft:block/copper_block",
    "grip": "minecraft:block/black_concrete",
    "warning": "minecraft:block/yellow_concrete",
    "energy": "minecraft:block/cyan_concrete",
    "light": "minecraft:block/sea_lantern",
    "particle": "minecraft:block/iron_block",
}
DISPLAY = {
    "gui": {"rotation": [25, -45, 0], "translation": [0, 0, 0], "scale": [0.68]*3},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.5]*3},
    "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, 0], "scale": [0.65]*3},
    "firstperson_righthand": {"rotation": [0, 0, 0], "translation": [1, 1.2, -1], "scale": [0.72]*3},
    "firstperson_lefthand": {"rotation": [0, 0, 0], "translation": [1, 1.2, -1], "scale": [0.72]*3},
    "thirdperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 0, 1], "scale": [0.7]*3},
    "thirdperson_lefthand": {"rotation": [0, 0, 0], "translation": [0, 0, 1], "scale": [0.7]*3},
}


def cube(name, a, b, material, *, glow=0, rotation=None):
    faces = {face: {"uv": [0, 0, 16, 16], "texture": "#"+material}
             for face in ("north", "south", "east", "west", "up", "down")}
    out = {"name": name, "from": a, "to": b, "faces": faces}
    if glow:
        out.update(light_emission=glow, shade=False)
    if rotation is not None:
        out["rotation"] = {"axis": "z", "angle": rotation, "origin": [8, 10, -3]}
    return out


def model(active=False, frame=0, charge=False):
    parts = []
    def add(name, a, b, material, **kw): parts.append(cube(name, a, b, material, **kw))
    # Receiver, angled grip, battery pack and carry handle.
    add("receiver", [5,7,3], [11,13,14], "casing")
    add("armour top", [4.5,12,4], [11.5,13.5,12.5], "steel")
    add("rear cap", [5.5,7.5,14], [10.5,12.5,15], "brass")
    add("grip", [6.5,1.5,9.5], [9.5,7.5,12], "grip")
    add("grip heel", [6,1,9], [10,2,12.5], "brass")
    for y in [2.5,3.5,4.5,5.5]:
        add("grip rib", [6.3,y,9.35], [9.7,y+0.25,12.15], "casing")
    add("trigger guard bottom", [6.75,4,6.5], [9.25,4.6,9.5], "steel")
    add("trigger guard front", [6.75,4.4,6.5], [9.25,7,7.1], "steel")
    add("trigger", [7.5,5.6,8], [8.5,7.5,8.55], "brass")
    add("battery", [5.7,5,12], [10.3,8,15], "casing")
    add("battery copper rim", [5.5,5.5,14.5], [10.5,7.5,15.3], "brass")
    for z in [6,10.5]: add("handle post", [6.5,13.5,z], [9.5,15.5,z+0.8], "brass")
    add("carry handle", [6.5,15,6], [9.5,16,11.3], "grip")
    # Cooling slots and the twin side capacitors.
    for x in [4.35,11.05]:
        add("side battery housing", [x,8,7], [x+0.6,11.5,12.8], "brass")
        for z in [7.5,8.6,9.7,10.8,11.9]:
            add("cooling slot", [x-0.03,8.5,z], [x+0.63,11,z+0.5], "grip")
        add("energy spine", [x-0.07,11.5,7.5], [x+0.67,11.85,12.3], "light" if active else "energy", glow=12 if active else 0)
    for x in [4.4,11.15]:
        add("hazard panel", [x,8.5,3.8], [x+0.45,11.8,6.6], "warning")
        for z in [4.1,5.2]: add("hazard stripe", [x-0.03,8.45,z], [x+0.48,11.85,z+0.4], "grip")
    # Barrel with stepped brass collars and vented heat shroud.
    add("barrel", [5.8,7.8,-3], [10.2,12.2,3.5], "grip")
    for z in [-1.7,0.2,2.1]:
        add("heat collar", [5.2,7.2,z], [10.8,12.8,z+0.6], "brass")
    for x in [5.1,10.5]: add("shroud rail", [x,8,-2.4], [x+0.4,12,3.5], "steel")
    for y in [7.1,12.5]: add("shroud rail", [6,y,-2.4], [10,y+0.4,3.5], "steel")
    # Front casing remains stationary while the square focusing cage rotates.
    add("emitter rim", [4.8,6.8,-4], [11.2,13.2,-2.7], "casing")
    add("emitter copper", [5.2,7.2,-4.3], [10.8,12.8,-3.9], "brass")
    add("dark aperture", [5.8,7.8,-4.45], [10.2,12.2,-4.3], "grip")
    add("lens", [6.5,8.5,-4.65], [9.5,11.5,-4.45], "light" if active or charge else "energy", glow=15 if active else 6 if charge else 0)
    angle = [0,22.5,45,-22.5][frame]
    rotor_z = -5.15 if active else -4.9
    for name,a,b in [
        ("rotor top", [5.8,12.15,rotor_z], [10.2,12.65,-4.65]),
        ("rotor bottom", [5.8,7.35,rotor_z], [10.2,7.85,-4.65]),
        ("rotor left", [5.35,7.8,rotor_z], [5.85,12.2,-4.65]),
        ("rotor right", [10.15,7.8,rotor_z], [10.65,12.2,-4.65])]:
        add(name,a,b,"steel",rotation=angle)
    for x,y in [(6,8),(9.4,8),(6,11.4),(9.4,11.4)]:
        add("focus lamp", [x,y,rotor_z-0.1], [x+0.6,y+0.6,rotor_z], "light" if active else "energy", glow=15 if active else 0,rotation=angle)
    # Side fasteners.
    for x in [4.15,11.55]:
        for y in [8.2,11.6]: add("rivet",[x,y,6.6],[x+0.3,y+0.4,7.1],"steel")
    display = copy.deepcopy(DISPLAY)
    if active:
        # Small reciprocating motion of the held body, separate from the rotor.
        for name in ["firstperson_righthand","firstperson_lefthand"]:
            display[name]["translation"][2] += [0,0.12,0,-0.12][frame]
    return {"credit": "Void Dynasty - native industrial laser drill", "textures": TEXTURES,
            "display": display, "elements": parts}


def ref(name): return {"type":"minecraft:model", "model":"voiddynasty:item/"+name}


def main():
    models = {"laser_drill":model(), "laser_drill_charge":model(charge=True)}
    for i in range(4): models["laser_drill_active_"+str(i)] = model(active=True,frame=i)
    for name, data in models.items():
        header = {key:value for key,value in data.items() if key != "elements"}
        text = json.dumps(header,indent=2)[:-2] + ',\n  "elements": [\n'
        text += ",\n".join("    "+json.dumps(element,separators=(",", ":")) for element in data["elements"])
        text += "\n  ]\n}\n"
        (ROOT/"models/item"/(name+".json")).write_text(text,encoding="utf-8")
    active = {"type":"minecraft:range_dispatch", "property":"minecraft:use_cycle", "period":8,
              "fallback":ref("laser_drill_active_0"),
              "entries":[{"threshold":i*2, "model":ref("laser_drill_active_"+str(i))} for i in range(4)]}
    using = {"type":"minecraft:range_dispatch", "property":"minecraft:use_duration",
             "fallback":ref("laser_drill_charge"), "entries":[{"threshold":6,"model":active}]}
    item = {"model":{"type":"minecraft:condition", "property":"minecraft:using_item",
                     "on_true":using, "on_false":ref("laser_drill")}}
    (ROOT/"items/laser_drill.json").write_text(json.dumps(item,indent=2)+"\n",encoding="utf-8")
    print("Generated",len(models),"models;",len(models["laser_drill"]["elements"]),"cuboids per model")


if __name__ == "__main__": main()
