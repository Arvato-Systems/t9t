/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.mcp.restapi;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
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
        return getTextValue(json, T9tAiMcpConstants.KEY_ID);
    }

    @Nullable
    public static String getMethod(@Nonnull final JsonNode json) {
        return getTextValue(json, T9tAiMcpConstants.KEY_METHOD);
    }

    @Nullable
    public static String getName(@Nonnull final JsonNode json) {
        return getTextValue(json, T9tAiMcpConstants.KEY_NAME);
    }

    @Nullable
    public static String getArgumentValue(@Nonnull final JsonNode json) {
        final JsonNode valueNode = json.get(T9tAiMcpConstants.KEY_ARGUMENTS);
        return valueNode != null ? valueNode.toString() : null;
    }

    @Nullable
    public static String getTextValue(@Nonnull final JsonNode json, @Nonnull final String key) {
        final JsonNode valueNode = json.get(key);
        return valueNode != null ? valueNode.asText() : null;
    }
}
