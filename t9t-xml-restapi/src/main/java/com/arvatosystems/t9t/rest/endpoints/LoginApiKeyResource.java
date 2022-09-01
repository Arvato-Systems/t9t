/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rest.endpoints;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.rest.parsers.RestParameterParsers;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.auth.AuthByApiKey;
import com.arvatosystems.t9t.xml.auth.AuthenticationResult;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import jakarta.ws.rs.core.Response;

/**
 * Login via API key or username / password
 */
@Singleton
@Tag(name = "login")
@Path("apikey")
public class LoginApiKeyResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginApiKeyResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    public static SessionParameters convertSessionParameters(final com.arvatosystems.t9t.xml.auth.SessionParameters spIn) {
        if (spIn == null) {
            return null;
        }
        final SessionParameters spOut = new SessionParameters();
        spOut.setDataUri(spIn.getDataUri());
        spOut.setLocale(spIn.getLanguageTag());
        spOut.setUserAgent(spIn.getUserAgent());
        spOut.setZoneinfo(spIn.getZoneInfo());
        return spOut;
    }

    @Operation(
        summary = "Create a session / JWT token by API key",
        description = "The request creates a session at the host and returns a JWT which can be used as authentication token for subsequent requests."
          + " Authentication is by API key.",
        responses = {
            @ApiResponse(description = "Authentication successful.", content = @Content(schema = @Schema(implementation = AuthenticationResult.class)))
        }
    )
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    public void login(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final AuthByApiKey authByApiKey) {
        if (authByApiKey == null) {
            LOGGER.error("Login attempted at /apikey without any data...");
            final String acceptHeader = determineResponseType(httpHeaders);
            restProcessor.returnAsyncResult(acceptHeader, resp, Response.Status.BAD_REQUEST, "Null parameter");
            return;
        }
        loginSub(httpHeaders, resp, authByApiKey.getApiKey(), convertSessionParameters(authByApiKey.getSessionParameters()));
    }

    @Operation(
        summary = "Create a session / JWT token by API key",
        description = "The request creates a session at the host and returns a JWT which can be used as authentication token for subsequent requests."
          + " Authentication is by API key.",
        responses = {
            @ApiResponse(description = "Authentication successful.", content = @Content(schema = @Schema(implementation = AuthenticationResult.class)))
        }
    )
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("/{apikey}")
    public void login(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp,
            @Parameter(required = true, description = "Api key.") @PathParam("apikey") final String apiKey) {
        loginSub(httpHeaders, resp, RestParameterParsers.parseUUID(apiKey, "apikey", true), null);
    }

    /**
     * Caches the ApiKey and connects it to a JWT.
     * This cache must expire significantly faster than the JWT duration (max 1/2 of it).
     */
    private static final Cache<UUID, String> API_KEY_TO_JWT_CACHE = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    public void loginSub(final HttpHeaders httpHeaders, final AsyncResponse resp, final UUID apiKey, final SessionParameters sp) {
        LOGGER.debug("Login attempted at /apikey ...");

        if (apiKey == null) {
            LOGGER.error("NULL apiKey - exception");
            throw new T9tException(T9tException.ACCESS_DENIED);  // can occur in case of malformatted API keys
        }
        final String jwt = API_KEY_TO_JWT_CACHE.getIfPresent(apiKey);
        if (jwt != null) {
            final String acceptHeader = determineResponseType(httpHeaders);
            LOGGER.debug("JWT already cached for ApiKey.");
            final AuthenticationResult result = new AuthenticationResult();
            result.setJwt(jwt);
            restProcessor.returnAsyncResult(acceptHeader, resp, Response.Status.OK, result);
            return;
        }
        final AuthenticationRequest authenticationParamsRequest = new AuthenticationRequest();
        authenticationParamsRequest.setAuthenticationParameters(new ApiKeyAuthentication(apiKey));
        authenticationParamsRequest.setSessionParameters(sp);
        // execute ServiceRequest
        restProcessor.performAsyncAuthBackendRequest(httpHeaders, resp, authenticationParamsRequest, s -> API_KEY_TO_JWT_CACHE.put(apiKey, s));
    }
}
