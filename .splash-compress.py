"""Compress the splash video for shipping in the APK.

Uses imageio-ffmpeg's bundled binary (no system ffmpeg required).
Target: H.264 CRF 26, no audio, ~1080p max — good visual for splash,
usually lands ~2-5 MB from a 20+ MB source.
"""
from __future__ import annotations
import os, subprocess, sys
import imageio_ffmpeg

SRC = r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\RES\LOGO\logo ani (1).mp4"
DST = r"C:\Users\still\Documents\ABHISHEK N MINE\Project_X-main\Project_X-main\app\src\main\res\raw\splash_animation.mp4"

ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
print(f"ffmpeg: {ffmpeg}")
print(f"source: {os.path.getsize(SRC)/(1024*1024):.2f} MB")

# -crf 26 = visually near-lossless for animation
# -preset slow  = better compression, slower encode (fine for one-shot)
# -vf scale=... cap long edge at 1080 px (splash rarely needs more)
# -an           = drop audio (splash is muted anyway)
# -movflags +faststart = ready for progressive playback
cmd = [
    ffmpeg, "-y", "-i", SRC,
    "-c:v", "libx264", "-crf", "26", "-preset", "slow",
    "-vf", "scale='if(gt(iw,ih),-2,min(1080,iw))':'if(gt(iw,ih),min(1080,ih),-2)'",
    "-pix_fmt", "yuv420p",
    "-an",
    "-movflags", "+faststart",
    DST,
]
print("running:", " ".join(f'"{a}"' if " " in a else a for a in cmd))
try:
    r = subprocess.run(cmd, capture_output=True, text=True, timeout=180)
except subprocess.TimeoutExpired:
    sys.exit("ffmpeg timed out")
if r.returncode != 0:
    print("STDERR (tail):"); print("\n".join(r.stderr.splitlines()[-15:]))
    sys.exit(f"ffmpeg failed: exit {r.returncode}")

out_mb = os.path.getsize(DST)/(1024*1024)
print(f"OK -> {DST}")
print(f"result: {out_mb:.2f} MB   (from source, {(1 - out_mb / (os.path.getsize(SRC)/(1024*1024))) * 100:.0f}% smaller)")
