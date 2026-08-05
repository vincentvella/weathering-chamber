#!/usr/bin/env python3
"""
Reproducible asset generator for the Weathering Chamber mod.

Produces, from scratch (deterministic — fixed RNG seeds):
  * In-game textures  -> src/main/resources/assets/weathering/textures/
      block/weathering_chamber_{side,bottom,top,front}.png   (16x16)
      gui/weathering_chamber.png                              (256x256 sheet)
  * GitHub preview art -> docs/img/
      block_faces.png    upscaled 4-face reference card
      block_iso.png      isometric block render
      gui.png            crisp GUI preview
      erosion_chain.png  cobblestone -> gravel -> sand diagram
      banner.png         wide repo header

Usage:  python tools/generate_assets.py
Requires: Pillow, numpy
"""
from __future__ import annotations
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "weathering", "textures")
IMG = os.path.join(ROOT, "docs", "img")

# ---------------------------------------------------------------- palette
STONE = [(0x6f, 0x6f, 0x73), (0x7c, 0x7c, 0x80), (0x89, 0x89, 0x90),
         (0x66, 0x66, 0x6a), (0x5a, 0x5a, 0x5e), (0x94, 0x94, 0x9a)]
CRACK = (0x47, 0x47, 0x4b)
STONE_HI = (0xa3, 0xa3, 0xaa)
WATER_D = (0x2f, 0x5c, 0x8f)
WATER = (0x3f, 0x78, 0xb5)
WATER_L = (0x5b, 0x9b, 0xd6)
WATER_HI = (0x8f, 0xc3, 0xef)
SAND = (0xd8, 0xce, 0xa0)
SAND_D = (0xb7, 0xac, 0x7c)
GRAVEL = (0x8b, 0x83, 0x78)
GRAVEL_D = (0x6f, 0x68, 0x5f)
IRON = (0x53, 0x53, 0x5a)
IRON_HI = (0x7d, 0x7d, 0x86)
IRON_D = (0x39, 0x39, 0x40)
DARK = (0x1c, 0x1c, 0x20)


def clamp(v):
    return 0 if v < 0 else 255 if v > 255 else int(v)


def shade(c, f):
    """f>0 lightens toward white, f<0 darkens."""
    r, g, b = c
    if f >= 0:
        return (clamp(r + (255 - r) * f), clamp(g + (255 - g) * f), clamp(b + (255 - b) * f))
    return (clamp(r * (1 + f)), clamp(g * (1 + f)), clamp(b * (1 + f)))


def blend(c1, c2, f):
    f = max(0.0, min(1.0, f))
    return tuple(clamp(a * (1 - f) + b * f) for a, b in zip(c1, c2))


def new(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def stone_base(px, rng, darken=0.0):
    for y in range(16):
        for x in range(16):
            c = STONE[rng.integers(len(STONE))]
            if rng.integers(6) == 0:
                c = shade(c, 0.10)
            if rng.integers(7) == 0:
                c = shade(c, -0.14)
            px[x, y] = shade(c, -darken) + (255,)
    for _ in range(3 + int(rng.integers(3))):
        cx, cy, ln = int(rng.integers(16)), int(rng.integers(16)), 2 + int(rng.integers(4))
        for _ in range(ln):
            if 0 <= cx < 16 and 0 <= cy < 16:
                px[cx, cy] = CRACK + (255,)
            cx += int(rng.integers(3)) - 1
            cy += 1 if rng.integers(2) == 0 else 0
            if cx < 0 or cx > 15 or cy > 15:
                break


def tex_side():
    im = new(16, 16)
    px = im.load()
    rng = np.random.default_rng(1001)
    stone_base(px, rng)
    cols = [2 + int(rng.integers(2)), 7 + int(rng.integers(2)), 12 + int(rng.integers(2))]
    for x in cols:
        if rng.integers(4) == 0:
            continue
        ln = 6 + int(rng.integers(8))
        for y in range(ln):
            base = px[x, y][:3]
            t = 0.35 * (1.0 - y / ln)
            px[x, y] = blend(base, WATER, t) + (255,)
            if rng.integers(3) == 0 and x + 1 < 16:
                px[x + 1, y] = blend(px[x + 1, y][:3], WATER, t * 0.6) + (255,)
    for _ in range(3):
        px[int(rng.integers(16)), 8 + int(rng.integers(8))] = STONE_HI + (255,)
    return im


def tex_bottom():
    im = new(16, 16)
    rng = np.random.default_rng(2002)
    stone_base(im.load(), rng, darken=0.10)
    return im


def tex_top():
    im = new(16, 16)
    px = im.load()
    rng = np.random.default_rng(3003)
    stone_base(px, rng, darken=0.02)
    x0, y0, x1, y1 = 2, 2, 13, 13
    for x in range(x0 - 1, x1 + 2):
        px[x, y0 - 1] = shade((0x6a, 0x6a, 0x6e), -0.25) + (255,)
        px[x, y1 + 1] = STONE_HI + (255,)
    for y in range(y0 - 1, y1 + 2):
        px[x0 - 1, y] = shade((0x6a, 0x6a, 0x6e), -0.25) + (255,)
        px[x1 + 1, y] = STONE_HI + (255,)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            dy = (y - y0) / (y1 - y0)
            c = blend(WATER_D, WATER, dy)
            if rng.integers(5) == 0:
                c = blend(c, WATER_L, 0.5)
            px[x, y] = c + (255,)
    for x in range(x0, x1 + 1):
        h = 1 + int(rng.integers(3))
        for k in range(h):
            y = y1 - k
            c = SAND if rng.integers(2) == 0 else GRAVEL
            px[x, y] = blend(c, WATER, 0.25 * (k + 1)) + (255,)
    for _ in range(4):
        x = x0 + 1 + int(rng.integers(x1 - x0 - 1))
        y = y0 + 1 + int(rng.integers(4))
        px[x, y] = WATER_HI + (255,)
        if x + 1 <= x1:
            px[x + 1, y] = blend(WATER, WATER_HI, 0.5) + (255,)
    return im


def tex_front():
    im = new(16, 16)
    px = im.load()
    rng = np.random.default_rng(4004)
    stone_base(px, rng, darken=0.04)
    x0, y0, x1, y1 = 4, 4, 11, 12
    for x in range(x0 - 1, x1 + 2):
        px[x, y0 - 1] = IRON_HI + (255,)
        px[x, y1 + 1] = IRON_D + (255,)
    for y in range(y0 - 1, y1 + 2):
        px[x0 - 1, y] = IRON_HI + (255,)
        px[x1 + 1, y] = IRON_D + (255,)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = DARK + (255,)
    for x in range(x0 + 1, x1 + 1, 2):
        for y in range(y0, y1 + 1):
            px[x, y] = blend(IRON, DARK, 0.3 + 0.4 * rng.random()) + (255,)
    for _ in range(3):
        x = x0 + 1 + int(rng.integers(x1 - x0 - 1))
        ln = 2 + int(rng.integers(4))
        yy = y0
        for yy in range(y0, min(y0 + ln, y1 + 1)):
            px[x, yy] = blend(WATER_L, DARK, 0.15) + (255,)
        if yy + 1 <= y1:
            px[x, yy + 1] = WATER_HI + (255,)
    for x in range(x0, x1 + 1):
        h = 1 if x in (x0, x1) else 1 + int(rng.integers(3))
        for k in range(h):
            y = y1 - k
            c = GRAVEL if rng.integers(3) == 0 else SAND
            px[x, y] = shade(c, -0.05 * k) + (255,)
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px[rx, ry] = IRON_HI + (255,)
        px[rx + 1, ry + 1] = IRON_D + (255,)
        px[rx, ry + 1] = IRON + (255,)
        px[rx + 1, ry] = IRON + (255,)
    return im


# ---------------------------------------------------------------- GUI sheet
PANEL = (0xc6, 0xc6, 0xc6)
PANEL_HI = (0xff, 0xff, 0xff)
PANEL_DK = (0x55, 0x55, 0x55)
SLOT = (0x8b, 0x8b, 0x8b)
SLOT_DK = (0x37, 0x37, 0x37)
SLOT_HI = (0xff, 0xff, 0xff)


def arrow_mask():
    m = np.zeros((16, 24), dtype=bool)
    for x in range(24):
        for y in range(16):
            on = False
            if x <= 14 and 6 <= y <= 9:
                on = True
            if x >= 12:
                t = (x - 12) / 11.0
                top = 2 + t * (7 - 2)
                bot = 13 - t * (13 - 8)
                if top - 0.001 <= y <= bot + 0.001:
                    on = True
            m[y, x] = on
    return m


def draw_slot(px, ix, iy):
    x, y = ix - 1, iy - 1
    for j in range(18):
        for i in range(18):
            px[x + i, y + j] = SLOT + (255,)
    for i in range(18):
        px[x + i, y] = SLOT_DK + (255,)
        px[x, y + i] = SLOT_DK + (255,)
    for i in range(18):
        px[x + i, y + 17] = SLOT_HI + (255,)
        px[x + 17, y + i] = SLOT_HI + (255,)
    px[x, y] = SLOT_DK + (255,)
    px[x + 17, y + 17] = SLOT_HI + (255,)


def draw_ring(px, ix, iy, rgb):
    x, y = ix - 1, iy - 1
    for i in range(1, 17):
        px[x + i, y + 1] = rgb + (255,)
        px[x + i, y + 16] = rgb + (255,)
        px[x + 1, y + i] = rgb + (255,)
        px[x + 16, y + i] = rgb + (255,)


def gui_sheet():
    im = new(256, 256)
    px = im.load()
    for y in range(166):
        for x in range(176):
            px[x, y] = PANEL + (255,)
    for x in range(176):
        px[x, 0] = PANEL_HI + (255,)
        px[x, 165] = PANEL_DK + (255,)
    for y in range(166):
        px[0, y] = PANEL_HI + (255,)
        px[175, y] = PANEL_DK + (255,)
    for y in range(5, 16):
        for x in range(7, 169):
            px[x, y] = blend(px[x, y][:3], WATER_L, 0.06) + (255,)
    draw_slot(px, 56, 17)
    draw_slot(px, 116, 35)
    draw_ring(px, 116, 35, blend(SLOT, SAND, 0.35))
    m = arrow_mask()
    for y in range(16):
        for x in range(24):
            if m[y, x]:
                px[79 + x, 34 + y] = (0x9a, 0x9a, 0x9a, 255)
    for x in range(24):
        if m[0, x]:
            px[79 + x, 34] = (0x80, 0x80, 0x80, 255)
    for row in range(3):
        for col in range(9):
            draw_slot(px, 8 + col * 18, 84 + row * 18)
    for col in range(9):
        draw_slot(px, 8 + col * 18, 142)
    # filled arrow sprite at (176,14): water -> sand
    for y in range(16):
        for x in range(24):
            if m[y, x]:
                t = x / 23.0
                c = blend(WATER, SAND, t)
                vy = abs(y - 7.5) / 7.5
                c = shade(c, -0.18 * vy)
                px[176 + x, 14 + y] = c + (255,)
    for x in range(24):
        ty = next((y for y in range(16) if m[y, x]), -1)
        if ty >= 0:
            base = px[176 + x, 14 + ty][:3]
            px[176 + x, 14 + ty] = blend(base, WATER_HI, 0.5) + (255,)
    return im


# ---------------------------------------------------------------- previews
def upscale(im, factor):
    return im.resize((im.width * factor, im.height * factor), Image.NEAREST)


def load_font(size):
    for name in ("segoeui.ttf", "arial.ttf", "DejaVuSans.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def paste_affine(canvas, tex, p0, p1, p2, shade_f=0.0):
    """Map square `tex` onto the parallelogram with corners p0(top-left),
    p1(top-right), p2(bottom-left) on `canvas` (both RGBA)."""
    if shade_f:
        arr = np.asarray(tex).astype(float)
        arr[..., :3] = np.clip(arr[..., :3] * (1 + shade_f), 0, 255)
        tex = Image.fromarray(arr.astype(np.uint8), "RGBA")
    n = tex.width
    # forward: dest = p0 + (x/n)(p1-p0) + (y/n)(p2-p0)
    m = np.array([[(p1[0] - p0[0]) / n, (p2[0] - p0[0]) / n],
                  [(p1[1] - p0[1]) / n, (p2[1] - p0[1]) / n]], dtype=float)
    minv = np.linalg.inv(m)
    a, b = minv[0]
    d, e = minv[1]
    c = -(a * p0[0] + b * p0[1])
    f = -(d * p0[0] + e * p0[1])
    warped = tex.transform(canvas.size, Image.AFFINE, (a, b, c, d, e, f), resample=Image.NEAREST)
    canvas.alpha_composite(warped)


def render_iso(top, front, side, face=256):
    top, front, side = (upscale(t, face // 16) for t in (top, front, side))
    hw, qh, fh = 128, 64, 168
    margin = 24
    W = 2 * hw + 2 * margin
    H = 2 * qh + fh + 2 * margin
    canvas = new(W, H)
    cx, cy = W // 2, margin
    T = (cx, cy)
    R = (cx + hw, cy + qh)
    B = (cx, cy + 2 * qh)
    L = (cx - hw, cy + qh)
    # top diamond: (0,0)->T (0,n)? map corners tl=T, tr=R, bl=L
    paste_affine(canvas, top, T, R, L, shade_f=0.0)
    # front (left visible face): tl=L, tr=B, bl=L+fh
    paste_affine(canvas, front, L, B, (L[0], L[1] + fh), shade_f=-0.12)
    # side (right visible face): tl=B, tr=R, bl=B+fh
    paste_affine(canvas, side, B, R, (B[0], B[1] + fh), shade_f=-0.26)
    return canvas


def card(bg=(0x1b, 0x1e, 0x24)):
    return bg


def render_faces(faces):
    scale = 8
    labels = ["side", "front", "top", "bottom"]
    order = [faces["side"], faces["front"], faces["top"], faces["bottom"]]
    cell = 16 * scale
    pad = 22
    label_h = 26
    cols = 4
    W = cols * cell + (cols + 1) * pad
    H = cell + pad + label_h + pad
    im = Image.new("RGBA", (W, H), (0x1b, 0x1e, 0x24, 255))
    d = ImageDraw.Draw(im)
    font = load_font(18)
    for i, (lab, f) in enumerate(zip(labels, order)):
        x = pad + i * (cell + pad)
        y = pad
        big = upscale(f, scale)
        im.alpha_composite(big, (x, y))
        d.rectangle([x - 1, y - 1, x + cell, y + cell], outline=(0x3a, 0x40, 0x4a, 255))
        tb = d.textbbox((0, 0), lab, font=font)
        tw = tb[2] - tb[0]
        d.text((x + (cell - tw) // 2, y + cell + 4), lab, font=font, fill=(0xc7, 0xcd, 0xd6, 255))
    return im


def swatch(kind, seed):
    """Tiny 16x16 representative material tile (cobblestone/gravel/sand)."""
    im = new(16, 16)
    px = im.load()
    rng = np.random.default_rng(seed)
    if kind == "cobble":
        base = STONE
        for y in range(16):
            for x in range(16):
                px[x, y] = base[rng.integers(len(base))] + (255,)
        for _ in range(6):
            cx, cy = int(rng.integers(16)), int(rng.integers(16))
            px[cx, cy] = CRACK + (255,)
    elif kind == "gravel":
        pal = [GRAVEL, GRAVEL_D, shade(GRAVEL, 0.12), (0x9a, 0x90, 0x84), STONE[2]]
        for y in range(16):
            for x in range(16):
                px[x, y] = pal[rng.integers(len(pal))] + (255,)
    else:  # sand
        pal = [SAND, SAND_D, shade(SAND, 0.08), (0xe6, 0xdd, 0xb2)]
        for y in range(16):
            for x in range(16):
                px[x, y] = pal[rng.integers(len(pal))] + (255,)
    return im


def render_chain(scale=6):
    tiles = [("cobble", "Cobblestone", 11), ("gravel", "Gravel", 22), ("sand", "Sand", 33)]
    cell = 16 * scale
    gap = 64
    pad = 24
    label_h = 26
    W = pad * 2 + 3 * cell + 2 * gap
    H = pad * 2 + cell + label_h
    im = Image.new("RGBA", (W, H), (0x1b, 0x1e, 0x24, 255))
    d = ImageDraw.Draw(im)
    font = load_font(20)
    x = pad
    y = pad
    for i, (kind, name, seed) in enumerate(tiles):
        big = upscale(swatch(kind, seed), scale)
        im.alpha_composite(big, (x, y))
        d.rectangle([x - 1, y - 1, x + cell, y + cell], outline=(0x3a, 0x40, 0x4a, 255))
        tb = d.textbbox((0, 0), name, font=font)
        d.text((x + (cell - (tb[2] - tb[0])) // 2, y + cell + 3), name, font=font,
               fill=(0xc7, 0xcd, 0xd6, 255))
        if i < 2:
            ax0 = x + cell + 12
            ax1 = x + cell + gap - 12
            ay = y + cell // 2
            d.line([ax0, ay, ax1, ay], fill=WATER_L + (255,), width=4)
            d.polygon([(ax1, ay - 8), (ax1 + 12, ay), (ax1, ay + 8)], fill=WATER_L + (255,))
            d.text(((ax0 + ax1) // 2 - 14, ay - 26), "grind", font=load_font(14),
                   fill=(0x8f, 0xc3, 0xef, 255))
        x += cell + gap
    return im


def render_banner(iso):
    W, H = 1280, 360
    im = Image.new("RGBA", (W, H), (0x14, 0x17, 0x1c, 255))
    # subtle vertical gradient
    top_c = np.array([0x1d, 0x2a, 0x3a])
    bot_c = np.array([0x12, 0x14, 0x18])
    grad = np.zeros((H, W, 3), dtype=np.uint8)
    for y in range(H):
        grad[y, :] = (top_c * (1 - y / H) + bot_c * (y / H)).astype(np.uint8)
    im = Image.fromarray(np.dstack([grad, np.full((H, W), 255, np.uint8)]), "RGBA")
    d = ImageDraw.Draw(im)
    isr = iso.resize((int(iso.width * 0.92), int(iso.height * 0.92)), Image.NEAREST)
    im.alpha_composite(isr, (60, (H - isr.height) // 2))
    tx = 60 + isr.width + 60
    d.text((tx, 84), "Weathering Chamber", font=load_font(64), fill=(0xf2, 0xf4, 0xf7, 255))
    d.text((tx, 168), "A water-powered machine that erodes",
           font=load_font(30), fill=(0xb6, 0xc6, 0xd6, 255))
    d.text((tx, 206), "cobblestone → gravel → sand.",
           font=load_font(30), fill=(0xb6, 0xc6, 0xd6, 255))
    d.text((tx, 258), "Sand, made renewable.  ·  Fabric · MC 1.21.1 & 26.2",
           font=load_font(22), fill=(0x7f, 0x9c, 0xba, 255))
    # accent underline
    d.line([tx + 2, 156, tx + 640, 156], fill=WATER + (255,), width=3)
    return im


def save(im, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)
    print("  wrote", os.path.relpath(path, ROOT).replace("\\", "/"))


def main():
    print("Game textures:")
    faces = {"side": tex_side(), "bottom": tex_bottom(), "top": tex_top(), "front": tex_front()}
    for name, im in faces.items():
        save(im, os.path.join(TEX, "block", f"weathering_chamber_{name}.png"))
    save(gui_sheet(), os.path.join(TEX, "gui", "weathering_chamber.png"))

    print("Preview art:")
    iso = render_iso(faces["top"], faces["front"], faces["side"])
    save(iso, os.path.join(IMG, "block_iso.png"))
    save(render_faces(faces), os.path.join(IMG, "block_faces.png"))
    # GUI preview: show the water->sand progress arrow ~65% filled so the themed
    # detail is visible (in-game it fills left-to-right as the grind progresses).
    sheet = gui_sheet()
    filled_cols = round(0.65 * 24)
    fill = sheet.crop((176, 14, 176 + filled_cols, 30))
    sheet.alpha_composite(fill, (79, 34))
    save(upscale(sheet.crop((0, 0, 176, 166)), 3), os.path.join(IMG, "gui.png"))
    save(render_chain(), os.path.join(IMG, "erosion_chain.png"))
    save(render_banner(iso), os.path.join(IMG, "banner.png"))
    print("Done.")


if __name__ == "__main__":
    main()
