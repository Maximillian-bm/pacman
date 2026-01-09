package com.example.GameLogic;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtil {

    public static int getLobbyID(String lobbyURI) {
        URI uri = parse(lobbyURI);

        // Path looks like: /5/ or /5
        String[] segments = uri.getPath().split("/");
        if (segments.length < 2) {
            throw new IllegalArgumentException("Invalid lobby URI: " + lobbyURI);
        }

        return Integer.parseInt(segments[1]);
    }

    public static String getSyncURI(String lobbyURI) {
        return buildActionURI(lobbyURI, "sync");
    }

    public static String getRawActionURI(String lobbyURI) {
        return buildActionURI(lobbyURI, "rawAction");
    }

    public static String getCleanActionURI(String lobbyURI) {
        return buildActionURI(lobbyURI, "cleanAction");
    }

    // ---------- Helpers ----------

    private static String buildActionURI(String lobbyURI, String action) {
        URI uri = parse(lobbyURI);

        String lobbyId = String.valueOf(getLobbyID(lobbyURI));
        String query = uri.getQuery(); // "keep"

        return uri.getScheme() + "://" +
               uri.getAuthority() +
               "/" + lobbyId +
               "/" + action +
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

