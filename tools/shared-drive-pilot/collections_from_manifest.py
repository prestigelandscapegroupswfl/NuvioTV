"""Turn a deployed Titanium X Drive add-on manifest into native collection rows."""

import argparse
import json
from pathlib import Path
import re
import uuid

ADDON_ID = "com.titaniumx.private.drive.pilot"
CATALOG_ID = re.compile(r"[a-z][a-z0-9-]{1,62}")
ROWS = ("Boxsets", "Beloved Animated Specials")
SPECIALS = {
    "rankin bass", "rankin bass specials", "peanuts", "peanuts specials", "peanuts charlie brown",
    "charlie brown", "garfield", "garfield specials", "garfield movies", "garfield shows",
    "scooby doo", "scooby doo where are you", "scooby doo specials", "looney tunes",
    "looney tunes specials", "holiday specials", "animated holiday specials",
}


def normalize(title):
    return re.sub(r"[^a-z0-9]+", " ", title.lower()).strip()


def stable_id(key):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"titanium-x/shared-drive/{key}"))


def build_collections(manifest, artwork):
    if not isinstance(manifest, dict) or manifest.get("id") != ADDON_ID:
        raise ValueError("Expected a manifest from the Titanium X private Drive add-on")
    rows = {name: [] for name in ROWS}
    catalogs = manifest.get("catalogs")
    if not isinstance(catalogs, list):
        raise ValueError("Manifest has no catalog list")

    seen = set()
    for catalog in catalogs:
        if not isinstance(catalog, dict):
            raise ValueError("Invalid catalog entry")
        catalog_id = catalog.get("id")
        name = catalog.get("name")
        if (catalog.get("type") != "movie" or not isinstance(catalog_id, str)
                or not CATALOG_ID.fullmatch(catalog_id) or catalog_id in seen
                or not isinstance(name, str) or not name.strip()):
            raise ValueError("Manifest has an invalid or duplicate movie catalog")
        seen.add(catalog_id)

        title = name.strip()
        key = normalize(title)
        row_name = ROWS[1] if key in SPECIALS else ROWS[0]
        group = "beloved animated specials" if row_name == ROWS[1] else "boxsets collections"
        cover_url, focus_url, hide_title = artwork.get((group, key), (None, None, False))
        source = {"addonId": ADDON_ID, "type": "movie", "catalogId": catalog_id}
        rows[row_name].append({
            "id": stable_id(catalog_id),
            "title": title,
            "tileShape": "LANDSCAPE",
            "hideTitle": hide_title,
            "coverImageUrl": cover_url,
            "focusGifUrl": focus_url or None,
            "focusGifEnabled": bool(focus_url),
            "catalogSources": [source],
            "sources": [{"provider": "addon", **source}]
        })

    if not seen:
        raise ValueError("Expected at least one Drive catalog")
    return [
        {
            "id": stable_id(f"row/{name}"),
            "title": f"Titanium X · {name}",
            "focusGlowEnabled": True,
            "viewMode": "TABBED_GRID",
            "showAllTab": True,
            "folders": folders
        }
        for name, folders in rows.items() if folders
    ]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--artwork", type=Path, default=Path(__file__).resolve().parents[2] /
                        "app/src/main/assets/titanium-collection-art.tsv")
    args = parser.parse_args()

    try:
        artwork = {}
        for line in args.artwork.read_text(encoding="utf-8").splitlines():
            row, name, cover, motion, hide_title = line.split("\t")
            artwork[(row, name)] = (cover, motion, hide_title == "1")
        rows = build_collections(json.loads(args.manifest.read_text(encoding="utf-8")), artwork)
        args.output.write_text(json.dumps(rows, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    except (OSError, ValueError) as error:
        parser.exit(1, f"{error}\n")


if __name__ == "__main__":
    main()
