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
package com.arvatosystems.t9t.ai.mcp;

public final class McpUtils {

    private McpUtils() { }

    // Define MCP error codes (standardized)
    public static final int MCP_PARSE_ERROR         = -32700;
    public static final int MCP_INVALID_REQUEST     = -32600;
    public static final int MCP_METHOD_NOT_FOUND    = -32601;
    public static final int MCP_INVALID_PARAMS      = -32602;
    public static final int MCP_INTERNAL_ERROR      = -32603;

    public static final String HTTP_HEADER_MCP_PROTOCOL = "MCP-Protocol-Version"; // HTTP header which should contain the requested version by the client
    public static final String FALLBACK_MCP_PROTOCOL_VERSION = McpProtocolVersion.UPDATE1.getToken(); // by spec this version should be assumed if the client does not specify any

    public static final int HEARTBEAT_INTERVAL = 30000; // milliseconds
    public static final String ENDPOINT_SSE = "sse";

    public static final String EVENT_ENDPOINT  = "endpoint";
    public static final String EVENT_CONNECTED = "connected";
    public static final String EVENT_HEARTBEAT = "heartbeat";

    public static final String KEY_CONNECTION_ID = "connectionId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_JSONRPC = "jsonrpc";
    public static final String KEY_ID = "id";
    public static final String KEY_METHOD = "method";
    public static final String KEY_NAME = "name";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_ARGUMENTS = "arguments";
    public static final String KEY_CURSOR = "cursor";

    public static final String JSONRPC_VERSION = "2.0";
    public static final String PROTOCOL_VERSION = McpProtocolVersion.UPDATE1.getToken();  // default, as requested by specification
    public static final String SERVER_NAME = "t9t embedded MCP Server";
    public static final String SERVER_VERSION = "9.0-SNAPSHOT";

    public static final String METHOD_INITIALIZE = "initialize";
    public static final String METHOD_TOOLS_LIST = "tools/list";
    public static final String METHOD_TOOLS_CALL = "tools/call";
    public static final String METHOD_PROMPTS_LIST = "prompts/list";
    public static final String METHOD_PROMPTS_GET = "prompts/get";

    public static final int PROMPT_LIST_PAGE_SIZE = 500; // page size for prompt listing
    public static final String ROLE_USER = "user";
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_IMAGE = "image";
    public static final String CONTENT_TYPE_AUDIO = "audio";

}
