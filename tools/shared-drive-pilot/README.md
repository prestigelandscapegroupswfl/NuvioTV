# Titanium X Shared Drive catalogs

Deploy `worker.mjs` to a private HTTPS Worker. Titanium X uses its existing player to play the HTTPS streams returned by the add-on. Keep its manifest URL private because the path contains the access key.

Configure these Worker secrets:

- `ADDON_KEY` is a random 32-byte key encoded as 64 lowercase hex characters.
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` and `GOOGLE_REFRESH_TOKEN` authorize read-only Drive access.
- `SHARED_DRIVE_ID` selects the Shared Drive containing the video files.
- `DRIVE_COLLECTIONS_JSON` lists selected folders as objects with stable `id`, display `name`, and `folderId`. Put individual boxsets in this list so each card opens a focused catalog. The legacy single-folder configuration continues to use `DRIVE_FOLDER_ID`.

The manifest is at `https://<worker-host>/<key>/manifest.json`. Add it to the Titanium X profile, then fetch the manifest privately and run `python tools/shared-drive-pilot/collections_from_manifest.py manifest.json titanium-x-drive.json`. Import the generated JSON under Collections. The script copies catalog IDs and matches folder names to the Titanium X artwork. The collection JSON doesn't contain the add-on URL or Google credentials.

Test a small, verified folder before enabling larger catalogs. Use the Shield to open a title, start playback, and seek. The add-on serves movies from the selected folders and validates membership before returning video bytes. It never searches the entire Shared Drive for a stream.
