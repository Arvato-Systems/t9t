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
package com.arvatosystems.t9t.rest.endpoints;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.auth.AuthByUserIdPassword;
import com.arvatosystems.t9t.xml.auth.AuthenticationResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Login via API key or username / password
 */
@Singleton
@Tag(name = "login")
@Path("userpw")
public class LoginUserPwResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginUserPwResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    /**
     * Caches the basicAuth String in form of the Hash and connects it to a JWT.
     * This cache must expire significantly faster than the JWT duration (max 1/2 of it).
     */
    private static final Cache<String, String> basicAuthHashToJwtCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    @Operation(
        summary = "Create a session / JWT token by user ID / password",
        description = "The request creates a session at the host and returns a JWT which can be used as authentication token for subsequent requests. Authentication is by user name / password",
        responses = {
            @ApiResponse(description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationResult.class)))
        }
    )
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @POST
    public void login(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final AuthByUserIdPassword authByUserPw) {
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);  // must evaluate it now, because httpHeaders is a proxy and no longer valid in the other thread
        if (authByUserPw == null) {
            LOGGER.error("Login attempted at /userpw without any data...");
            restProcessor.returnAsyncResult(acceptHeader, resp, Response.Status.BAD_REQUEST, "Null parameter");
            return;
        }
        final String cacheKey = authByUserPw.getUserId() + ":" + authByUserPw.getPassword();

        LOGGER.debug("Attempting to retrieve JWT for basic authenticaton.");
        String jwt = basicAuthHashToJwtCache.getIfPresent(cacheKey);
        if (jwt != null) {
            LOGGER.debug("JWT already cached for user {}. Returning...", authByUserPw.getUserId());
            final AuthenticationResult result = new AuthenticationResult();
            result.setJwt(jwt);
            restProcessor.returnAsyncResult(acceptHeader, resp, Response.Status.OK, result);
            return;
        }

        // create AuthenticationParamsRequest
        final AuthenticationRequest authenticationParamsRequest = new AuthenticationRequest();
        // create User+Password Hash
        PasswordAuthentication passwordAuthentication = new PasswordAuthentication();
        passwordAuthentication.setUserId(authByUserPw.getUserId());
        passwordAuthentication.setPassword(authByUserPw.getPassword());
        authenticationParamsRequest.setAuthenticationParameters(passwordAuthentication);
        authenticationParamsRequest.setSessionParameters(LoginApiKeyResource.convertSessionParameters(authByUserPw.getSessionParameters()));

        // execute ServiceRequest
        // execute ServiceRequest
        restProcessor.performAsyncAuthBackendRequest(httpHeaders, resp, authenticationParamsRequest, s -> basicAuthHashToJwtCache.put(cacheKey, s));
    }
}
