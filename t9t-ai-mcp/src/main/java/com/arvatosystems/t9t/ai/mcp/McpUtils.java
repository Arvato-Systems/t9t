package com.arvatosystems.t9t.ai.mcp;

public final class McpUtils {

    private McpUtils() { }

    public static final int HEARTBEAT_INTERVAL = 30000; // milliseconds
    public static final String ENDPOINT_SSE = "sse";

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

    public static final String JSONRPC_VERSION = "2.0";
    public static final String PROTOCOL_VERSION = "2025-03-26";
    public static final String SERVER_NAME = "t9t embedded MCP Server";
    public static final String SERVER_VERSION = "9.0-SNAPSHOT";

    public static final String METHOD_INITIALIZE = "initialize";
    public static final String METHOD_TOOLS_LIST = "tools/list";
    public static final String METHOD_TOOLS_CALL = "tools/call";

}
