package com.example.GameLogic;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtil {

    /**
     * Extracts the lobby id from a lobby-specific URI like:
     * tcp://127.0.0.1:50000/5sync?keep
     * tcp://127.0.0.1:50000/5rawAction?keep
     * tcp://127.0.0.1:50000/5cleanAction?keep
     */
    public static int getLobbyID(String lobbySpecificURI) {
        URI uri = parse(lobbySpecificURI);

        // Path will look like: "/5sync" or "/5rawAction" etc.
        String path = uri.getPath(); // includes leading "/"
        if (path == null || path.length() < 2) {
            throw new IllegalArgumentException("Invalid lobby-specific URI (missing path): " + lobbySpecificURI);
        }

        String tail = path.substring(1); // remove leading '/'
        int i = 0;
        while (i < tail.length() && Character.isDigit(tail.charAt(i))) i++;

        if (i == 0) {
            throw new IllegalArgumentException("Invalid lobby-specific URI (missing lobby id): " + lobbySpecificURI);
        }

        return Integer.parseInt(tail.substring(0, i));
    }

    /**
     * Builds: tcp://host:port/{lobbyId}sync?keep (preserves base query if present)
     */
    public static String getSyncURI(String baseURI, int lobbyId) {
        return buildLobbyURI(baseURI, lobbyId, "sync");
    }

    /**
     * Builds: tcp://host:port/{lobbyId}rawAction?keep (preserves base query if present)
     */
    public static String getRawActionURI(String baseURI, int lobbyId) {
        return buildLobbyURI(baseURI, lobbyId, "rawAction");
    }

    /**
     * Builds: tcp://host:port/{lobbyId}cleanAction?keep (preserves base query if present)
     */
    public static String getCleanActionURI(String baseURI, int lobbyId) {
        return buildLobbyURI(baseURI, lobbyId, "cleanAction");
    }

    // ---------- Helpers ----------

    private static String buildLobbyURI(String baseURI, int lobbyId, String actionSuffix) {
        if (lobbyId < 0) {
            throw new IllegalArgumentException("lobbyId must be non-negative: " + lobbyId);
        }

        URI uri = parse(baseURI);

        // Base is like: tcp://127.0.0.1:50000/?keep
        // We ignore any base path and rebuild exactly as required: "/{id}{action}"
        String query = uri.getQuery(); // e.g. "keep"

        return uri.getScheme() + "://" +
               uri.getAuthority() +
               "/" + lobbyId + actionSuffix +
               (query != null ? "?" + query : "");
    }

    private static URI parse(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
    }
}


