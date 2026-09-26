"""One-shot launcher icon generator for Project-X (and Campus).

Reads any raster/ICO source, then writes:
  - adaptive foreground PNG (432×432, safe-zone padded)
  - legacy ic_launcher.png + ic_launcher_round.png at 5 densities
  - values/colors.xml background color
  - deletes the AGP-default vector drawables so the raster wins

Re-runnable — swap the SRC path and re-execute.
"""
from __future__ import annotations

import os
import sys
from PIL import Image, ImageDraw

# ─── inputs ─────────────────────────────────────────────────
SRC = r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\RES\LOGO\frosted_glass_logo.ico"

# Background color painted behind the adaptive foreground.
BG_HEX = "#0F1724"        # deep navy so a frosted-glass logo pops
BG_COLOR_RGBA = (15, 23, 36, 255)

# Targets — every entry gets the same icon set.
TARGETS = [
    r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\Project_X-main\app\src\main\res",
    r"C:\Users\still\Documents\ABHISHEK N MINE\Campus\composeApp\src\androidMain\res",
]

# ─── constants ──────────────────────────────────────────────
LEGACY = {
    "mdpi":    48,
    "hdpi":    72,
    "xhdpi":   96,
    "xxhdpi":  144,
    "xxxhdpi": 192,
}
ADAPTIVE_SIZE = 432        # 108dp × 4
SAFE_ZONE = 264            # 66dp × 4 — max area guaranteed visible under any mask

# ─── helpers ────────────────────────────────────────────────
def load_source(path: str) -> Image.Image:
    if not os.path.exists(path):
        sys.exit(f"source not found: {path}")
    img = Image.open(path)
    # .ico can contain many frames — pick the largest.
    largest = img
    if getattr(img, "n_frames", 1) > 1:
        best_area = 0
        for i in range(img.n_frames):
            img.seek(i)
            area = img.width * img.height
            if area > best_area:
                best_area, largest = area, img.copy()
    largest = largest.convert("RGBA")
    print(f"source: {os.path.basename(path)} -> {largest.size[0]}x{largest.size[1]}")
    return largest


def fit_centered(src: Image.Image, canvas_size: int, target_edge: int,
                 background: tuple | None = None) -> Image.Image:
    w, h = src.size
    scale = target_edge / max(w, h)
    new_w, new_h = max(1, int(w * scale)), max(1, int(h * scale))
    scaled = src.resize((new_w, new_h), Image.LANCZOS)
    canvas = Image.new("RGBA", (canvas_size, canvas_size), background or (0, 0, 0, 0))
    canvas.paste(scaled, ((canvas_size - new_w) // 2, (canvas_size - new_h) // 2), scaled)
    return canvas


def with_circle_mask(src: Image.Image) -> Image.Image:
    mask = Image.new("L", src.size, 0)
    ImageDraw.Draw(mask).ellipse((0, 0, src.size[0] - 1, src.size[1] - 1), fill=255)
    out = src.copy()
    out.putalpha(mask)
    return out


def write(img: Image.Image, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, "PNG", optimize=True)
    print(f"  wrote {os.path.relpath(path)}  ({os.path.getsize(path)//1024} KB)")


def write_text(path: str, content: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"  wrote {os.path.relpath(path)}")


def rm(path: str) -> None:
    if os.path.exists(path):
        os.remove(path)
        print(f"  removed {os.path.relpath(path)}")


def apply(res_root: str, src: Image.Image) -> None:
    print(f"\n=== target: {res_root} ===")

    # 1. Vector drawables from AGP template would collide with our raster foreground.
    rm(os.path.join(res_root, "drawable", "ic_launcher_foreground.xml"))
    rm(os.path.join(res_root, "drawable", "ic_launcher_background.xml"))

    # 2. Adaptive foreground (padded to safe zone, transparent surround).
    fg = fit_centered(src, ADAPTIVE_SIZE, SAFE_ZONE)
    write(fg, os.path.join(res_root, "drawable-nodpi", "ic_launcher_foreground.png"))

    # 3. Legacy icons — full-bleed logo on the coloured background so pre-v26
    #    launchers still get the branded rounded tile.
    for density, px in LEGACY.items():
        square = fit_centered(src, px, int(px * 0.85), background=BG_COLOR_RGBA)
        write(square, os.path.join(res_root, f"mipmap-{density}", "ic_launcher.png"))
        write(with_circle_mask(square),
              os.path.join(res_root, f"mipmap-{density}", "ic_launcher_round.png"))

    # 4. Adaptive-icon XML — reference the colour + foreground we just wrote.
    #    Both mipmap-anydpi (Project-X convention) and mipmap-anydpi-v26 (Campus)
    #    receive the same XML so it works either way.
    for adaptive_dir in ("mipmap-anydpi", "mipmap-anydpi-v26"):
        # Only overwrite if the directory is already present OR create v26 for KMP.
        target = os.path.join(res_root, adaptive_dir)
        # Always write v26; only write anydpi (no version) if it already exists.
        if adaptive_dir == "mipmap-anydpi-v26" or os.path.isdir(target):
            for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
                write_text(os.path.join(target, name), ADAPTIVE_XML)

    # 5. Background colour.
    write_text(os.path.join(res_root, "values", "colors.xml"), COLORS_XML)


ADAPTIVE_XML = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""

COLORS_XML = f"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">{BG_HEX}</color>
</resources>
"""


def main() -> None:
    src = load_source(SRC)
    for t in TARGETS:
        if os.path.isdir(t):
            apply(t, src)
        else:
            print(f"skip missing: {t}")
    print("\ndone.")


if __name__ == "__main__":
    main()
