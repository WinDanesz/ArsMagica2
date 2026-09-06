"""Export the jointed Blockbench model to the Minecraft 1.12.2 ModelRenderer API.

Run from any directory with Python 3. Geometry is generated; animation stays in
ModelNatureElemental.java below the END GENERATED GEOMETRY marker.
"""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "ModelNatureElemental.bbmodel"
OUTPUT = ROOT / "src/main/java/am2/client/entity/models/ModelNatureElemental.java"
MARKER = "    // END GENERATED GEOMETRY\n"


def number(value):
    formatted = f"{value:.6f}".rstrip("0").rstrip(".")
    return formatted + ("F" if "." in formatted else ".0F")


def vector(values):
    return ", ".join(number(v) for v in values)


def export():
    model = json.loads(SOURCE.read_text(encoding="utf-8"))
    groups = {g["uuid"]: g for g in model["groups"]}
    cubes = {c["uuid"]: c for c in model["elements"]}
    lines = []
    emitted = set()

    def group(node, parent=None):
        g = groups[node["uuid"]]
        name = g["name"]
        origin = g["origin"]
        base = parent["origin"] if parent else [0, 24, 0]
        # Blockbench's modded-entity export flips X/Y; Java feet rest at Y=24.
        lines.append(f"        {name} = new ModelRenderer(this);")
        lines.append(f"        {name}.setRotationPoint({vector([base[0] - origin[0], base[1] - origin[1], origin[2] - base[2]])});")
        assert not any(g.get("rotation", [0, 0, 0])), "Unexpected group rest rotation"
        if parent:
            lines.append(f"        {parent['name']}.addChild({name});")
        for child in node["children"]:
            if isinstance(child, dict):
                group(child, g)
                continue
            c = cubes[child]
            assert child not in emitted
            emitted.add(child)
            size = [b - a for a, b in zip(c["from"], c["to"])]
            assert all(s == int(s) for s in size), "1.12.2 boxes require integral dimensions"
            pivot = c.get("origin", origin)
            rotation = c.get("rotation", [0, 0, 0])
            uv = c.get("uv_offset", [0, 0])
            lines.append(f"        // {c['name']}")
            lines.append(f"        addCube({name}, {int(uv[0])}, {int(uv[1])},")
            lines.append(f"                {vector([origin[0] - pivot[0], origin[1] - pivot[1], pivot[2] - origin[2]])},")
            lines.append(f"                {vector([pivot[0] - c['to'][0], pivot[1] - c['to'][1], c['from'][2] - pivot[2]])},")
            lines.append(f"                {', '.join(str(int(s)) for s in size)}, {str(c.get('mirror_uv', False)).lower()},")
            lines.append(f"                {vector([math.radians(-rotation[0]), math.radians(-rotation[1]), math.radians(rotation[2])])});")
        lines.append("")

    for node in model["outliner"]:
        group(node)
    assert emitted == set(cubes), "Every source cube must be exported exactly once"
    original = OUTPUT.read_text(encoding="utf-8")
    prefix = original.split("    public ModelNatureElemental() {")[0]
    suffix = original.split(MARKER, 1)[1]
    constructor = "    public ModelNatureElemental() {\n"
    constructor += f"        textureWidth = {model['resolution']['width']};\n        textureHeight = {model['resolution']['height']};\n\n"
    OUTPUT.write_text(prefix + constructor + "\n".join(lines) + "    }\n\n" + MARKER + suffix, encoding="utf-8")
    print(f"Exported {len(emitted)} cubes and {len(groups)} joints to {OUTPUT.name}")


if __name__ == "__main__":
    export()
