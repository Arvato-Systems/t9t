package com.arvatosystems.t9t.mcp.restapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;

public interface IMcpRestRequestHandler {

    void handleRequest(@Nonnull HttpHeaders httpHeaders, @Nonnull AsyncResponse resp, @Nonnull String id, @Nonnull JsonNode body);
}
