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

import java.util.List;

import com.arvatosystems.t9t.ai.T9tAiMcpConstants;
import com.arvatosystems.t9t.ai.mcp.McpProtocolVersion;
import com.fasterxml.jackson.databind.JsonNode;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;

public final class McpRestUtils {

    private McpRestUtils() { }

    public static final List<String> SUPPORTED_PROTOCOL_VERSIONS = List.of(
            McpProtocolVersion.INITIAL.getToken(),
            McpProtocolVersion.UPDATE1.getToken(),
            McpProtocolVersion.UPDATE2.getToken());

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
    public static Object getId(@Nonnull final JsonNode json) {
        return getValue(json.get(T9tAiMcpConstants.KEY_ID));
    }

    @Nullable
    public static Object getValue(@Nullable final JsonNode valueNode) {
        if (valueNode == null) {
            return null;
        }
        if (valueNode.isNumber()) {
            if (valueNode.isFloatingPointNumber()) {
                return valueNode.asDouble();
            } else {
                return valueNode.asLong();
            }
        } else if (valueNode.isBoolean()) {
            return valueNode.asBoolean();
        } else if (valueNode.isNull()) {
            return null;
        }
        return valueNode.asText();
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

    @Nonnull
    public static String toJson(@Nonnull String key, Object value) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\"").append(key).append("\":");
        if (value instanceof String) {
            sb.append("\"").append(value).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value == null) {
            sb.append("null");
        }
        return sb.append("}").toString();
    }
}
