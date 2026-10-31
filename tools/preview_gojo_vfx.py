"""Render the actual GLSL materials in OpenGL, outside Minecraft.

pip install moderngl numpy pillow
python tools/preview_gojo_vfx.py --output /path/to/materials.png
This verifies GPU compilation and material appearance, not Forge integration.
"""
import argparse
import json
import math
from pathlib import Path
import numpy as np
import moderngl
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
SHADERS = ROOT / "src/main/resources/assets/arcanecontrol/shaders/core"


def load_program(ctx, name, expected_blend=None):
    descriptor = json.loads((SHADERS / f"{name}.json").read_text())
    def source(key, extension):
        namespace, path = descriptor[key].split(":", 1)
        assert namespace == "arcanecontrol"
        return (SHADERS / (path + extension)).read_text()
    program = ctx.program(vertex_shader=source("vertex", ".vsh"), fragment_shader=source("fragment", ".fsh"))
    for attribute in descriptor["attributes"]:
        assert attribute in program, f"Missing linked attribute: {attribute}"
    for uniform in descriptor["uniforms"]:
        assert uniform["name"] in program, f"Missing linked uniform: {uniform['name']}"
        assert len(uniform["values"]) == uniform["count"]
    blend = descriptor["blend"]
    assert blend["srcrgb"] == "srcalpha"
    expected = expected_blend or ("one" if name.endswith("_additive") else "1-srcalpha")
    assert blend["dstrgb"] == expected
    return program


def mesh(radius=1, detail=72, color=(1, 1, 1, 1)):
    vertices = []
    for i in range(detail):
        for j in range(detail * 2):
            for di, dj in ((0, 0), (1, 0), (1, 1), (0, 0), (1, 1), (0, 1)):
                u, v = (j + dj) / (detail * 2), (i + di) / detail
                a, lat = u * math.tau, (v - .5) * math.pi
                vertices.append((math.cos(a) * math.cos(lat) * radius,
                                 math.sin(lat) * radius, math.sin(a) * math.cos(lat) * radius,
                                 *color, u, v))
    return np.array(vertices, dtype="f4")


def render(output, time=1.7, backend=None):
    ctx = moderngl.create_standalone_context(require=320, **({"backend": backend} if backend else {}))
    program = load_program(ctx, "gojo_energy")
    additive = load_program(ctx, "gojo_energy_additive")
    additive.release()
    width, height = 480, 360
    fbo = ctx.simple_framebuffer((width, height), components=4)
    fbo.use()
    perspective = np.zeros((4, 4), dtype="f4")
    f = 1 / math.tan(math.radians(42) / 2)
    perspective[0, 0], perspective[1, 1] = f / (width / height), f
    perspective[2, 2], perspective[2, 3], perspective[3, 2] = -1.002, -.2002, -1
    program["ProjMat"].write(perspective.T.tobytes())
    program["EffectTime"].value = time
    program["Viewer"].value = (0, 0, 1)
    vbo = ctx.buffer(mesh().tobytes())
    vao = ctx.vertex_array(program, [(vbo, "3f 4f 2f", "Position", "Color", "UV0")])
    sheet = Image.new("RGB", (width * 2, height * 2 + 70), (8, 10, 18))
    draw = ImageDraw.Draw(sheet)
    panels = [(0, "AZUL - absorcao"), (1, "VERMELHO - concentracao"),
              (2, "ROXO - massa e veios"), (4, "VAZIO INFINITO - ceu")]
    for index, (mode, label) in enumerate(panels):
        fbo.clear(.025, .032, .05, 1, depth=1)
        ctx.enable(moderngl.DEPTH_TEST)
        ctx.enable(moderngl.BLEND)
        ctx.blend_func = (moderngl.SRC_ALPHA, moderngl.ONE_MINUS_SRC_ALPHA)
        ctx.disable(moderngl.CULL_FACE)
        view = np.eye(4, dtype="f4")
        if mode != 4:
            view[2, 3] = -3.5
        else:
            view[:3, :3] *= 10
        program["ModelViewMat"].write(view.T.tobytes())
        program["EffectMode"].value = mode
        vao.render(moderngl.TRIANGLES)
        image = Image.frombytes("RGBA", (width, height), fbo.read(components=4)).transpose(Image.Transpose.FLIP_TOP_BOTTOM)
        data = np.asarray(image)
        assert data[:, :, :3].std() > 2, f"Blank material: {label}"
        center = data[height//2-8:height//2+8, width//2-8:width//2+8, :3].mean(axis=(0, 1))
        if mode == 0:
            assert center.mean() < 55, "Blue lost its dark core"
        elif mode == 1:
            assert center[0] > 180 and center[0] > center[2] * 1.2, "Red lost its concentrated warm core"
        elif mode == 2:
            assert center[2] > center[1] * 1.5, "Purple lost its violet identity"
        x, y = index % 2 * width, index // 2 * height
        sheet.paste(image.convert("RGB"), (x, y))
        draw.text((x + 14, y + 14), label, fill=(222, 230, 245))
    draw.text((14, height * 2 + 12), "PREVIA OPENGL DOS MATERIAIS REAIS - NAO E CAPTURA DO MINECRAFT", fill=(222, 230, 245))
    draw.text((14, height * 2 + 34), "Sem bloom Photon, cenario, particulas, gestos ou composicao completa.", fill=(160, 177, 200))
    output = Path(output)
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output)
    print(f"GPU: {ctx.info['GL_RENDERER']} | {ctx.info['GL_VERSION']}")
    print(f"Compiled, linked and rendered 4 materials: {output}")
    vao.release(); vbo.release(); program.release(); fbo.release(); ctx.release()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True)
    parser.add_argument("--time", type=float, default=1.7)
    parser.add_argument("--backend", help="Use egl for a headless Mesa runner")
    args = parser.parse_args()
    render(args.output, args.time, args.backend)
