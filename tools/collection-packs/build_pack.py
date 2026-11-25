#!/usr/bin/env python3
"""Turn a Titanium X collections export into an independently installable pack."""

import argparse
import copy
import json
import re
import uuid
from collections import Counter
from pathlib import Path

NAMESPACE = uuid.UUID("24e1a5cc-4b9b-4a04-8aa1-1a587db307b5")
STARTERS = {
    "genres": ("Genres",),
    "franchises": ("Studios & Franchises", "Boxsets & Collections"),
    "decades": ("Decades",),
}


def encode_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def stable_id(pack_id, kind, original_id):
    return str(uuid.uuid5(NAMESPACE, f"{pack_id}/{kind}/{original_id}"))


def build_pack(collections, pack_id, name, version, titles):
    matches = {title: [c for c in collections if c.get("title") == title] for title in titles}
    invalid = [title for title, groups in matches.items() if len(groups) != 1]
    if invalid:
        raise ValueError("Expected exactly one matching collection for: " + ", ".join(invalid))

    output = []
    coverage = Counter()
    required_addons = set()
    for title in titles:
        group = copy.deepcopy(matches[title][0])
        original_id = group.get("id")
        folders = group.get("folders")
        if not isinstance(original_id, str) or not original_id or not isinstance(folders, list) or not folders:
            raise ValueError(f"{title}: missing collection ID or folders")
        group["id"] = stable_id(pack_id, "collection", original_id)
        group["title"] = f"Titanium X · {title}"
        folder_ids = set()
        for folder in folders:
            folder_id = folder.get("id")
            folder_title = folder.get("title")
            if not isinstance(folder_id, str) or not folder_id or folder_id in folder_ids or not folder_title:
                raise ValueError(f"{title}: invalid or duplicate folder")
            folder_ids.add(folder_id)
            folder["id"] = stable_id(pack_id, f"folder/{original_id}", folder_id)
            sources = folder.get("sources")
            if sources is None:
                sources = [{"provider": "addon", **item} for item in folder.get("catalogSources", [])]
            if not isinstance(sources, list) or not sources:
                raise ValueError(f"{folder_title}: no catalog source configured")
            for source in sources:
                if not isinstance(source, dict):
                    raise ValueError(f"{folder_title}: invalid source")
                provider = source.get("provider", "addon")
                if provider == "addon":
                    addon_id = source.get("addonId")
                    if not all(isinstance(value, str) and value.strip()
                               for value in (addon_id, source.get("catalogId"), source.get("type"))):
                        raise ValueError(f"{folder_title}: incomplete add-on binding")
                    required_addons.add(addon_id)
                elif provider == "tmdb":
                    if not source.get("tmdbSourceType"):
                        raise ValueError(f"{folder_title}: incomplete TMDB binding")
                elif provider == "trakt":
                    if not isinstance(source.get("traktListId"), int) or source["traktListId"] <= 0:
                        raise ValueError(f"{folder_title}: incomplete Trakt binding")
                else:
                    raise ValueError(f"{folder_title}: unknown source provider {provider!r}")
                coverage[provider] += 1
            folder["sources"] = sources
            folder["tileShape"] = "LANDSCAPE"
        output.append(group)

    manifest = {
        "schemaVersion": 1,
        "id": pack_id,
        "name": name,
        "version": version,
        "importFile": f"{pack_id}.json",
        "collections": len(output),
        "folders": sum(len(group["folders"]) for group in output),
        "sourcesByProvider": dict(sorted(coverage.items())),
        "requiresAddons": sorted(required_addons),
    }
    return output, manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("export", type=Path)
    parser.add_argument("output_dir", type=Path)
    parser.add_argument("--starter", choices=STARTERS)
    parser.add_argument("--collection", action="append", help="Exact collection title; repeat to add another.")
    parser.add_argument("--id", help="Stable pack slug; reuse it when releasing an update.")
    parser.add_argument("--name")
    parser.add_argument("--version", default="0.1.0")
    args = parser.parse_args()
    if bool(args.starter) == bool(args.collection):
        parser.error("Choose a starter or supply one or more --collection values.")
    pack_id = args.id or (f"tx-{args.starter}" if args.starter else None)
    if not pack_id or not re.fullmatch(r"[a-z0-9][a-z0-9-]{1,62}", pack_id):
        parser.error("Use --id with 2–63 lowercase letters, digits, or hyphens.")
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", args.version):
        parser.error("--version must use major.minor.patch.")
    titles = tuple(dict.fromkeys(args.collection or STARTERS[args.starter]))
    name = args.name or f"Titanium X {args.starter.title() if args.starter else titles[0]}"
    collections = json.loads(args.export.read_text(encoding="utf-8"))
    if not isinstance(collections, list) or not all(isinstance(item, dict) for item in collections):
        parser.error("Input must be the collection array exported by Titanium X.")
    try:
        output, manifest = build_pack(collections, pack_id, name, args.version, titles)
    except ValueError as exc:
        parser.error(str(exc))
    args.output_dir.mkdir(parents=True, exist_ok=True)
    encode_json(args.output_dir / manifest["importFile"], output)
    encode_json(args.output_dir / f"{pack_id}.manifest.json", manifest)
    print(f"{manifest['name']}: {manifest['folders']} folders and {sum(manifest['sourcesByProvider'].values())} catalog sources")


if __name__ == "__main__":
    main()
