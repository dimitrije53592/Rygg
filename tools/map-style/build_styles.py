"""Builds the bundled Rygg outdoor map style from OpenFreeMap's Liberty style.

Usage: python -I tools/map-style/build_styles.py <liberty.json>
Download the input from https://tiles.openfreemap.org/styles/liberty.

Writes the same style to app/src/main/assets/map/rygg-outdoor.json (what the map renders) and
public/map/rygg-outdoor.json (served by Firebase Hosting; offline packs are built from it, because
MapLibre only downloads pack styles over HTTP). Deploy Hosting after regenerating.

The vector source points at OpenFreeMap's stable /planet/latest tile URL instead of its TileJSON.
The TileJSON names a build-stamped tile URL that changes weekly, which would orphan the tiles in
every offline pack after each rebuild.
"""

import copy
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT_DIRS = [ROOT / "app" / "src" / "main" / "assets" / "map", ROOT / "public" / "map"]

VECTOR_SOURCE = {
    "type": "vector",
    "tiles": ["https://tiles.openfreemap.org/planet/latest/{z}/{x}/{y}.pbf"],
    "minzoom": 0,
    "maxzoom": 14,
    "bounds": [-180, -85.05113, 180, 85.05113],
    "attribution": (
        '<a href="https://openfreemap.org" target="_blank">OpenFreeMap</a> '
        '<a href="https://www.openmaptiles.org/" target="_blank">&copy; OpenMapTiles</a> '
        'Data from <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
    ),
}

DROPPED_LAYERS = {
    "building-3d",
    "road_one_way_arrow",
    "road_one_way_arrow_opposite",
    "highway-shield-us-interstate",
    "road_shield_us",
}

TRAIL_LAYERS = {"tunnel_path_pedestrian", "road_path_pedestrian", "bridge_path_pedestrian"}
TRAIL_COLOR = "#a0522d"
TRAIL_MIN_ZOOM = 12


def outdoor(style):
    style = copy.deepcopy(style)
    style["name"] = "Rygg Outdoor"
    style["sources"]["openmaptiles"] = VECTOR_SOURCE
    style["layers"] = [l for l in style["layers"] if l["id"] not in DROPPED_LAYERS]
    for layer in style["layers"]:
        paint = layer.setdefault("paint", {})
        if layer["id"] in TRAIL_LAYERS:
            layer["minzoom"] = TRAIL_MIN_ZOOM
            paint["line-color"] = TRAIL_COLOR
            paint["line-dasharray"] = [2, 1]
            paint["line-width"] = [
                "interpolate", ["exponential", 1.2], ["zoom"], 12, 0.8, 14, 1.6, 18, 4,
            ]
        elif layer["id"] == "highway-name-path":
            layer["minzoom"] = 14
            paint["text-color"] = TRAIL_COLOR
        elif layer["id"] == "landcover_wood":
            paint["fill-color"] = "#b9d8a0"
            paint["fill-opacity"] = 0.6
    peaks_at = next(i for i, l in enumerate(style["layers"]) if l["id"] == "poi_r20")
    style["layers"].insert(peaks_at, peak_layer())
    return style


def peak_layer():
    return {
        "id": "mountain_peak",
        "type": "symbol",
        "source": "openmaptiles",
        "source-layer": "mountain_peak",
        "minzoom": 11,
        "filter": ["match", ["get", "class"], ["peak", "volcano"], True, False],
        "layout": {
            "icon-image": "triangle_11",
            "text-anchor": "top",
            "text-offset": [0, 0.6],
            "text-field": [
                "case",
                ["has", "ele"],
                ["concat", ["coalesce", ["get", "name_en"], ["get", "name"]], "\n",
                 ["to-string", ["get", "ele"]], " m"],
                ["coalesce", ["get", "name_en"], ["get", "name"]],
            ],
            "text-font": ["Noto Sans Regular"],
            "text-max-width": 9,
            "text-size": 11,
        },
        "paint": {
            "text-color": "#5b4636",
            "text-halo-color": "#ffffff",
            "text-halo-width": 1,
        },
    }


def main():
    liberty = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
    style = outdoor(liberty)
    body = json.dumps(style, separators=(",", ":"))
    for out_dir in OUT_DIRS:
        out_dir.mkdir(parents=True, exist_ok=True)
        path = out_dir / "rygg-outdoor.json"
        path.write_text(body, encoding="utf-8")
        print(path, path.stat().st_size)


if __name__ == "__main__":
    main()
