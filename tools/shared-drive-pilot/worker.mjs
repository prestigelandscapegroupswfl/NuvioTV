// Private Stremio-compatible pilot for one authorized Google Drive folder.
const DRIVE = "https://www.googleapis.com/drive/v3/files";
const FOLDER = "application/vnd.google-apps.folder";
const CATALOG = "titanium-drive-marvel";
const IDENTIFIER = /^[A-Za-z0-9_-]{8,128}$/;
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

async function browse(env, token) {
    const queue = [env.DRIVE_FOLDER_ID];
    const visited = new Set();
    const movies = new Map();
    let pages = 0;
    while (queue.length) {
        const parent = queue.shift();
        if (visited.has(parent)) continue;
        visited.add(parent);
        let pageToken;
        do {
            if (++pages > 100) throw new Error("Pilot folder exceeds the listing limit");
            const params = new URLSearchParams({
                q: `'${parent}' in parents and trashed = false`,
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
                }
            }
            pageToken = page.nextPageToken;
        } while (pageToken);
    }
    return movies;
}

function movieMeta(file) {
    return {
        id: `txdrive:${file.id}`,
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

        const route = parts.join("/");
        if (route === "manifest.json") {
            return json({
                id: "com.titaniumx.private.drive.pilot",
                version: "0.1.0",
                name: "Titanium X Private Drive",
                description: "Browse the authorized Marvel MCU pilot folder.",
                resources: ["catalog", "meta", "stream"],
                types: ["movie"],
                idPrefixes: ["txdrive:"],
                catalogs: [{ type: "movie", id: CATALOG, name: "Marvel MCU" }]
            });
        }

        const item = /^(meta|stream)\/movie\/txdrive(?::|%3A)([A-Za-z0-9_-]{8,128})\.json$/i.exec(route);
        const playback = /^play\/([A-Za-z0-9_-]{8,128})$/.exec(route);
        if (route !== `catalog/movie/${CATALOG}.json` && !item && !playback) {
            return json({ error: "Not found" }, 404);
        }
        if (!IDENTIFIER.test(env.DRIVE_FOLDER_ID) || !IDENTIFIER.test(env.SHARED_DRIVE_ID) ||
            !env.GOOGLE_CLIENT_ID || !env.GOOGLE_CLIENT_SECRET || !env.GOOGLE_REFRESH_TOKEN) {
            return json({ error: "Drive pilot setup is incomplete" }, 503);
        }

        try {
            const token = await getAccessToken(env);
            const movies = await browse(env, token);
            if (!item && !playback) return json({ metas: [...movies.values()].map(movieMeta) });

            // A valid add-on key alone never allows reading a file outside the selected folder.
            const file = movies.get(item?.[2] ?? playback?.[1]);
            if (!file) return json({ error: "File not found" }, 404);
            if (item?.[1] === "meta") return json({ meta: movieMeta(file) });
            if (item?.[1] === "stream") {
                return json({
                    streams: [{ name: "Shared Drive", url: `${url.origin}/${key}/play/${file.id}` }]
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
