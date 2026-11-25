# Titanium X collection pack builder

Use a collections export as the source. Each generated pack has an installable collection array and a separate manifest with its version and required add-on IDs.

```sh
python3 tools/collection-packs/build_pack.py collections-export.json packs --starter genres
python3 tools/collection-packs/build_pack.py collections-export.json packs --starter franchises
python3 tools/collection-packs/build_pack.py collections-export.json packs --starter decades
python3 tools/collection-packs/build_pack.py collections-export.json packs \
  --id tx-family --name "Titanium X Family" --collection "Family & Kids"
```

Import the pack JSON in Titanium X's Collections screen. The *.manifest.json file is for inspection and is separate from the in-app import. Keep the original export for backup. A pack uses its own collection IDs so a first install does not replace your originals. To update an installed pack, keep the same pack slug and original collection IDs. Re-importing that pack replaces its previously installed collections, including edits to those pack copies. Profiles synced through Nuvio can share the imported changes with other clients on that profile.

A collection only organizes catalogs. The manifest lists which add-on IDs the app must recognize. Native TMDB and Trakt sources depend on the app's configuration for those services. Stream playback uses the user's configured providers. Keep personal exports private and review artwork URLs and source references before publishing a pack.
