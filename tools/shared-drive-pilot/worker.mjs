// Private Stremio-compatible catalogs backed by authorized Shared Drive folders.
const DRIVE = "https://www.googleapis.com/drive/v3/files";
const FOLDER = "application/vnd.google-apps.folder";
const CATALOG = "titanium-drive-marvel";
const IDENTIFIER = /^[A-Za-z0-9_-]{8,128}$/;
const COLLECTION_ID = /^[a-z][a-z0-9-]{1,62}$/;
const NO_CACHE = "private, no-store";

let tokenCache;

function json(value, status = 200) {
    return Response.json(value, {
        status,
        headers: { "Cache-Control": NO_CACHE }
    });
}

async function getAccessToken(env) {
    if (tokenCache?.environment === env && tokenCache.expiresAt > Date.now()) {
        return tokenCache.token;
    }
    const response = await fetch("https://oauth2.googleapis.com/token", {
        method: "POST",
        body: new URLSearchParams({
            grant_type: "refresh_token",
            client_id: env.GOOGLE_CLIENT_ID,
            client_secret: env.GOOGLE_CLIENT_SECRET,
            refresh_token: env.GOOGLE_REFRESH_TOKEN
        })
    });
    if (!response.ok) throw new Error("Google authorization needs attention");
    const { access_token: token, expires_in: lifetime } = await response.json();
    if (!token || !Number.isFinite(lifetime)) throw new Error("Invalid Google response");
    tokenCache = { environment: env, token, expiresAt: Date.now() + (lifetime - 60) * 1000 };
    return token;
}

function configuredCollections(env) {
    if (!env.DRIVE_COLLECTIONS_JSON) {
        if (!IDENTIFIER.test(env.DRIVE_FOLDER_ID ?? "")) throw new Error("Missing folder");
        return new Map([[CATALOG, { id: CATALOG, name: "Marvel MCU", folderId: env.DRIVE_FOLDER_ID }]]);
    }
    const entries = JSON.parse(env.DRIVE_COLLECTIONS_JSON);
    if (!Array.isArray(entries) || entries.length === 0 || entries.length > 100) {
        throw new Error("Invalid collection configuration");
    }
    const collections = new Map();
    for (const entry of entries) {
        if (typeof entry?.id !== "string" || !COLLECTION_ID.test(entry.id) ||
            typeof entry.name !== "string" || !entry.name.trim() || entry.name.length > 120 ||
            typeof entry.folderId !== "string" || !IDENTIFIER.test(entry.folderId) ||
            collections.has(entry.id)) {
            throw new Error("Invalid or duplicate collection");
        }
        collections.set(entry.id, { id: entry.id, name: entry.name.trim(), folderId: entry.folderId });
    }
    return collections;
}

async function browse(env, token, folderId, skip) {
    const queue = [folderId];
    const visited = new Set();
    const movies = new Map();
    let pages = 0;
    while (queue.length) {
        const parent = queue.shift();
        if (visited.has(parent)) continue;
        visited.add(parent);
        let pageToken;
        do {
            if (++pages > 45) throw new Error("Collection exceeds the listing limit");
            const params = new URLSearchParams({
                q: `'${parent}' in parents and trashed = false`,
                orderBy: "name_natural",
                corpora: "drive",
                driveId: env.SHARED_DRIVE_ID,
                includeItemsFromAllDrives: "true",
                supportsAllDrives: "true",
                pageSize: "100",
                fields: "nextPageToken,files(id,name,mimeType,capabilities(canDownload))"
            });
            if (pageToken) params.set("pageToken", pageToken);
            const response = await fetch(`${DRIVE}?${params}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            if (!response.ok) throw new Error("Drive listing failed");
            const page = await response.json();
            for (const file of page.files ?? []) {
                if (!IDENTIFIER.test(file.id)) continue;
                if (file.mimeType === FOLDER) queue.push(file.id);
                else if (file.mimeType?.startsWith("video/") && file.capabilities?.canDownload === true) {
                    movies.set(file.id, file);
                    if (movies.size >= skip + 100) return [...movies.values()].slice(skip);
                }
            }
            pageToken = page.nextPageToken;
        } while (pageToken);
    }
    return [...movies.values()].slice(skip);
}

async function movieInCollection(env, token, folderId, id) {
    const params = new URLSearchParams({
        supportsAllDrives: "true",
        fields: "id,name,mimeType,parents,driveId,trashed,capabilities(canDownload)"
    });
    const headers = { Authorization: `Bearer ${token}` };
    let movie;
    const visited = new Set();
    for (let fileId = id; IDENTIFIER.test(fileId ?? "") && visited.size < 40;) {
        if (fileId === folderId) return movie;
        if (visited.has(fileId)) return undefined;
        visited.add(fileId);
        const response = await fetch(`${DRIVE}/${fileId}?${params}`, { headers });
        if (response.status === 404) return undefined;
        if (!response.ok) throw new Error("Drive file lookup failed");
        const file = await response.json();
        if (file.id !== fileId || file.trashed || file.driveId !== env.SHARED_DRIVE_ID) return undefined;
        if (!movie) {
            if (!file.mimeType?.startsWith("video/") || file.capabilities?.canDownload !== true) {
                return undefined;
            }
            movie = file;
        } else if (file.mimeType !== FOLDER) {
            return undefined;
        }
        fileId = file.parents?.[0];
    }
    return undefined;
}

function movieMeta(file, collection) {
    return {
        id: collection.id === CATALOG ? `txdrive:${file.id}` : `txdrive:${collection.id}:${file.id}`,
        type: "movie",
        name: file.name.replace(/\.[^.]+$/, "")
    };
}

export default {
    async fetch(request, env) {
        if (request.method !== "GET") return json({ error: "Method not allowed" }, 405);
        const url = new URL(request.url);
        const [, key, ...parts] = url.pathname.split("/");
        if (!/^[a-f0-9]{64}$/.test(env.ADDON_KEY ?? "") || key !== env.ADDON_KEY) {
            return json({ error: "Not found" }, 404);
        }

        let collections;
        try {
            collections = configuredCollections(env);
        } catch {
            return json({ error: "Drive collection setup is incomplete" }, 503);
        }
        const route = parts.join("/");
        if (route === "manifest.json") {
            return json({
                id: "com.titaniumx.private.drive.pilot",
                version: "0.2.0",
                name: "Titanium X Private Drive",
                description: "Browse the authorized Shared Drive collections.",
                resources: ["catalog", "meta", "stream"],
                types: ["movie"],
                idPrefixes: ["txdrive:"],
                catalogs: [...collections.values()].map(({ id, name }) => ({
                    type: "movie", id, name, extra: [{ name: "skip", isRequired: false }]
                }))
            });
        }
        const catalog = /^catalog\/movie\/([a-z0-9-]+)(?:\/skip=(\d+))?\.json$/.exec(route);
        const item = /^(meta|stream)\/movie\/txdrive(?::|%3A)(?:([a-z0-9-]+)(?::|%3A))?([A-Za-z0-9_-]{8,128})\.json$/i.exec(route);
        const playback = /^play\/(?:(?<collection>[a-z0-9-]+)\/)?(?<fileId>[A-Za-z0-9_-]{8,128})$/.exec(route);
        const collection = collections.get(catalog?.[1] ?? item?.[2] ?? playback?.groups.collection ?? CATALOG);
        if ((!catalog && !item && !playback) || !collection) return json({ error: "Not found" }, 404);
        const skip = Number(catalog?.[2] ?? 0);
        if (!Number.isSafeInteger(skip) || skip > 4000) return json({ metas: [] });
        if (!IDENTIFIER.test(env.SHARED_DRIVE_ID ?? "") ||
            !env.GOOGLE_CLIENT_ID || !env.GOOGLE_CLIENT_SECRET || !env.GOOGLE_REFRESH_TOKEN) {
            return json({ error: "Drive pilot setup is incomplete" }, 503);
        }

        try {
            const token = await getAccessToken(env);
            if (catalog) {
                const movies = await browse(env, token, collection.folderId, skip);
                return json({ metas: movies.map(file => movieMeta(file, collection)) });
            }

            // A valid add-on key alone never allows reading a file outside the selected folder.
            const file = await movieInCollection(
                env, token, collection.folderId, item?.[3] ?? playback?.groups.fileId
            );
            if (!file) return json({ error: "File not found" }, 404);
            if (item?.[1] === "meta") return json({ meta: movieMeta(file, collection) });
            if (item?.[1] === "stream") {
                const path = collection.id === CATALOG ? file.id : `${collection.id}/${file.id}`;
                return json({
                    streams: [{ name: "Shared Drive", url: `${url.origin}/${key}/play/${path}` }]
                });
            }

            const headers = new Headers({ Authorization: `Bearer ${token}` });
            const range = request.headers.get("Range");
            if (range) headers.set("Range", range);
            const upstream = await fetch(`${DRIVE}/${file.id}?alt=media&supportsAllDrives=true`, {
                headers,
                redirect: "error"
            });
            if (![200, 206, 416].includes(upstream.status)) {
                await upstream.body?.cancel();
                return json({ error: "Drive playback failed" }, 502);
            }
            const responseHeaders = new Headers({ "Cache-Control": NO_CACHE });
            for (const name of ["Content-Type", "Content-Length", "Content-Range", "Accept-Ranges"]) {
                const value = upstream.headers.get(name);
                if (value !== null) responseHeaders.set(name, value);
            }
            return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
        } catch {
            return json({ error: "Couldn't read the authorized Drive folder" }, 502);
        }
    }
};
