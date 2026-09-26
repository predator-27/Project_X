"""Compress + fan-out the splash image to every project's res folder.

Input:  1440x3200 PNG (6.2 MB)
Output: 1080x2400 WEBP q=82 (~few hundred KB) — indistinguishable on-device
        at any density but a fraction of the APK cost.
"""
from __future__ import annotations
import os
import sys
from PIL import Image

SRC = r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\RES\LOGO\SP.png"

MAX_LONG_EDGE = 2400        # crisp on any modern phone; 1440p wastes bytes
QUALITY = 82

TARGETS = [
    r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\Project_X-main\app\src\main\res\drawable-nodpi\splash.webp",
    r"C:\Users\still\Documents\ABHISHEK N MINE\Campus\composeApp\src\androidMain\res\drawable-nodpi\splash.webp",
]


def main() -> None:
    if not os.path.exists(SRC):
        sys.exit(f"missing: {SRC}")

    img = Image.open(SRC).convert("RGB")
    w, h = img.size
    print(f"source: {w}x{h}  ({os.path.getsize(SRC)//1024} KB)")

    long_edge = max(w, h)
    if long_edge > MAX_LONG_EDGE:
        scale = MAX_LONG_EDGE / long_edge
        new_size = (int(w * scale), int(h * scale))
        img = img.resize(new_size, Image.LANCZOS)
        print(f"resized: {new_size[0]}x{new_size[1]}")

    for path in TARGETS:
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path, "WEBP", quality=QUALITY, method=6)
        print(f"  wrote {os.path.relpath(path)}  ({os.path.getsize(path)//1024} KB)")

    print("done.")


if __name__ == "__main__":
    main()
