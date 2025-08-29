package com.arvatosystems.t9t.mcp.restapi;

import com.arvatosystems.t9t.ai.mcp.McpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;

public final class McpRestUtils {

    private McpRestUtils() { }

    public static void sendResponse(@Nonnull final AsyncResponse response, @Nonnull final Response.Status status, @Nullable String message) {
        response.resume(Response.status(status).entity(message).build());
    }

    @Nonnull
    public static MediaData getJsonMediaData(@Nonnull final String data) {
        final MediaData mediaData = new MediaData();
        mediaData.setMediaType(MediaType.JSON);
        mediaData.setText(data);
        return mediaData;
    }

    @Nullable
    public static String getId(@Nonnull final JsonNode json) {
        return getTextValue(json, McpUtils.KEY_ID);
    }

    @Nullable
    public static String getMethod(@Nonnull final JsonNode json) {
        return getTextValue(json, McpUtils.KEY_METHOD);
    }

    @Nullable
    public static String getName(@Nonnull final JsonNode json) {
        return getTextValue(json, McpUtils.KEY_NAME);
    }

    @Nullable
    public static String getArgumentValue(@Nonnull final JsonNode json) {
        final JsonNode valueNode = json.get(McpUtils.KEY_ARGUMENTS);
        return valueNode != null ? valueNode.toString() : null;
    }

    @Nullable
    public static String getTextValue(@Nonnull final JsonNode json, @Nonnull final String key) {
        final JsonNode valueNode = json.get(key);
        return valueNode != null ? valueNode.asText() : null;
    }
}
