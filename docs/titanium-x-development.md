# Titanium X — Android TV development

Branch: `titanium-x-dev`. Base: NuvioTV (GPL-3.0); keep LICENSE and corresponding source available with distributed APKs.

## Installation identity

- Launcher label: Titanium X.
- Full preview APK: `com.titaniumx.tv.debug`.
- Full release APK: `com.titaniumx.tv`.
- FileProvider authority uses the application ID. App storage is separate from Nuvio.
- Upstream in-app updates are disabled. Never install an upstream Nuvio APK as a Titanium X update.
- Preview and release are separate app IDs. A future release needs an explicit export/import migration.
- Inherited `nuvio:` and `stremio:` links may show an Android app chooser. Select the intended app; do not assume a shared link targets Titanium X.

## Preservation requirements

Keep collection IDs, folder IDs, ordering, catalog bindings, pinning, cover URLs, focus GIFs, hero backdrops/videos, logos, tile shape and title visibility through export/import. Do not replace the user's collections on startup or overwrite the working Nuvio install. Import only into Titanium X after exporting its current state.

The saved animated export and historical build-config archive are separate snapshots. Do not merge or deduplicate them silently. The animated export currently has 8 collections, 233 folders, 358 sources and 15 focus-GIF URLs. The older handover describes 15 collections; none of the inspected exports proves that larger state is complete. Preserve all original snapshots.

Preserve the six customized gradient palettes and existing custom-color settings. Keep both hosted artwork pipelines (nuvio-covers and titanium-x-assets) independent of APK releases. Do not rewrite their URLs or delete assets while fixing builds.

## Advanced customization roadmap

Existing Android capabilities include collections, focus GIFs/hero media fields, selectable palettes, custom themes and standard addon support. These need Shield validation.

Retain these planned native features: Artwork Downloader Pro; character and extra character artwork; standard/animated/textless/extra posters; keyart and animated keyart; fanart and animated fanart; transparent renders; clearlogos/clearart; disc art and animated disc art; Blu-ray/3D cases; studio/network/collection/genre/decade art; theme editor; layout and widget manager; wizard/first-run setup; build export/import; provider/account management; maintenance and update manager.

Kodi Python addons and skin XML are specifications and reusable artwork sources, not Android implementations. Do not describe these planned native ports as shipped.

## Build and device validation

Run the existing PR Full Debug Build workflow from titanium-x-dev for a first preview. It uses a newly generated CI signing key per run: preview APKs from different runs cannot reliably update each other. Before a long-lived install, configure a persistent owner-controlled signing key using repository secrets; never commit the key or passwords.

The universal APK includes arm64-v8a and armeabi-v7a and is suitable as the first Shield compatibility candidate. Successful compilation does not verify device playback or Android TV performance.

Shield acceptance: launch alongside Nuvio; remote D-pad navigation/back; all six palettes; collection import then export comparison; focus animations and hero media; authorized addon installation and playback; audio passthrough/subtitles; app restart persistence. Test cloud sign-in separately: upstream backend settings are not automatically inherited by a fork.

## Branded Android preview implementation

Original asset: Titanium X: Cinematic Neon Reveal.gif (nine frames, original timing), preserved byte-for-byte as app/src/main/res/raw/titanium_intro.gif. Intro overlay runs for at most six seconds; skip with the Shield remote or Back. It respects the existing startup splash setting. This is the original GIF; no sound asset was present.

Native vector Titanium X wordmark, X launcher marks and TV banners replace default visual branding while preserving launcher color choices. Existing theme palettes, collections storage and artwork URLs remain unchanged. Upstream licenses and service/support attribution are retained.
