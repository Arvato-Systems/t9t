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
package com.arvatosystems.t9t.rest.filters;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class AuthFilterCustomization implements IAuthFilterCustomization {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestAuthenticationFilter.class);
    private static final int MAX_AUTH_ENTRIES = 200;
    private static final int MAX_SIZE_AUTH_HEADER = 4096;

    protected static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    protected static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
    protected final boolean enableSwagger = RestUtils.checkIfSet("t9t.restapi.swagger", Boolean.FALSE);
    protected final boolean enablePing = enableSwagger || RestUtils.checkIfSet("t9t.restapi.unauthedPing", Boolean.FALSE); // swagger implies ping

    protected final Cache<String, Boolean> goodAuths = Caffeine.newBuilder().maximumSize(MAX_AUTH_ENTRIES).expireAfterWrite(5L, TimeUnit.MINUTES)
      .<String, Boolean>build();

    protected final IPAddressBlocker ipBlockerService = Jdp.getRequired(IPAddressBlocker.class);


    /** Checks if the request came from a blocked IP address. */
    @Override
    public boolean filterBlockedIpAddress(final ContainerRequestContext requestContext, final String remoteIp) {
        if (ipBlockerService.isIpAddressBlocked(remoteIp)) {
            throwForbidden(requestContext);
            return true;
        }
        return false;
    }

    @Override
    public void abortFilter(final ContainerRequestContext requestContext, final Response errorStatus) {
        // throw new WebApplicationException(errorStatus);
        requestContext.abortWith(errorStatus);
        return;
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 401. */
    protected void throwUnauthorized(final ContainerRequestContext requestContext) {
        abortFilter(requestContext, Response.status(Status.UNAUTHORIZED).build());
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 403. */
    protected void throwForbidden(final ContainerRequestContext requestContext) {
        abortFilter(requestContext, Response.status(Status.FORBIDDEN).build());
    }

    @Override
    public boolean filterUnauthenticated(final ContainerRequestContext requestContext) {
        final String path = requestContext.getUriInfo().getPath();
        LOGGER.debug("filterUnauthenticated for method {}, type {}, path {}", requestContext.getMethod(), requestContext.getMediaType(), path);
        switch (requestContext.getMethod()) {
        case HttpMethod.POST:
            // allow login, but only if JWT is enabled
            if (!isValidLogin(path, requestContext.getMethod())) {
                throwUnauthorized(requestContext);
                return true;  // filtered
            }
            return false; // OK
        case HttpMethod.GET:
            if (enableSwagger && isValidSwagger(path)) {
                return false; // OK
            }
            if (enablePing && isValidPing(path)) {
                return false; // OK
            }
            if (isValidLogin(path, requestContext.getMethod())) {
                return false; // OK
            }
            throwUnauthorized(requestContext);
            return true;  // filtered
        default:
            throwUnauthorized(requestContext);
            return true;  // filtered
        }
    }

    @Override
    public boolean filterAuthenticated(final ContainerRequestContext requestContext, final String authHeader) {
        // first, check if we know this authentication - use a fast track in that case
        if (authHeader.length() <= MAX_SIZE_AUTH_HEADER && goodAuths.getIfPresent(authHeader) != null) {
            // fast track - we have successfully authenticated this one before
            return false; // OK
        }

        // must have valid authentication - first, check allowed size (depending on method)
        final int firstSpace = authHeader.indexOf(' ');
        if (firstSpace <= 0) {
            throwForbidden(requestContext);
            return true; // filtered
        }
        final String typeOfAuth = authHeader.substring(0, firstSpace + 1);  // add 1 because the constant includes the space
        final String authParam = authHeader.substring(firstSpace + 1);
        final int authLength = authParam.length();

        switch (typeOfAuth) {
        case T9tConstants.HTTP_AUTH_PREFIX_JWT:
            if (!allowAuthJwt()) {
                throwForbidden(requestContext);
                return true; // filtered
            }
            if (authLength < 10 || authLength > 4096) { // || !BASE64_PATTERN.matcher(authParam).matches()) {
                LOGGER.debug("Invalid JWT - length {}", authLength);
                throwForbidden(requestContext);
                return true; // filtered
            }
            return filterJwt(requestContext, authHeader, authParam);
        case T9tConstants.HTTP_AUTH_PREFIX_API_KEY:
            if (!allowAuthApiKey() || authLength != 36 || !UUID_PATTERN.matcher(authParam).matches()) {
                LOGGER.debug("Invalid UUID - length {}", authLength);
                throwForbidden(requestContext);
                return true; // filtered
            }
            return filterApiKey(requestContext, authHeader, authParam);
        case T9tConstants.HTTP_AUTH_PREFIX_USER_PW:
            if (!allowAuthBasic() || authLength < 8 || authLength > 80 || !BASE64_PATTERN.matcher(authParam).matches()) {
                throwForbidden(requestContext);
                return true; // filtered
            }
            return filterBasic(requestContext, authHeader, authParam);
        }
        throwForbidden(requestContext);
        return true; // filtered
    }

    /**
     * Check for acceptable Basic authentication.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterBasic(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        // TODO further checks on basic auth
        return false;
    }

    /**
     * Check for acceptable API key authentication.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterApiKey(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        // TODO further checks on API key auth
        return false;
    }

    /** Check for acceptable JWT authentication. Should throw an exception if this type is not desired.
     * The purpose of this method is to ensure "authentication before parameter validation".
     */
    protected boolean filterJwt(final ContainerRequestContext requestContext, final String authHeader, final String authParam) {
        // TODO further checks on JWT auth
        return false;
    }

    @Override
    public boolean allowAuthJwt() {
        return true;
    }

    @Override
    public boolean allowAuthBasic() {
        return true;
    }

    @Override
    public boolean allowAuthApiKey() {
        return true;
    }

    /** Invoked for POST requests or GET: Check for correct login path. */
    protected boolean isValidLogin(final String path, final String method) {
        return "/apikey".equals(path);
    }

    /** Invoked for GET requests: Check for valid swagger path. */
    protected boolean isValidSwagger(final String path) {
        return "/openapi.json".equals(path) || path.startsWith("/swagger-ui/");
    }

    /** Invoked for GET requests: Check for allowed GET paths. */
    protected boolean isValidPing(final String path) {
        return "/ping".equals(path);
    }

    @Override
    public boolean filterSupportedMediaType(final ContainerRequestContext requestContext) {
        final MediaType mediaType = requestContext.getMediaType();
        if (mediaType != null) {
            final String subType = mediaType.getSubtype();
            if (!"application".equals(mediaType.getType()) || !("xml".equals(subType) || "json".equals(subType))) {
                abortFilter(requestContext, Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean filterCorrectIdempotencyPattern(final ContainerRequestContext requestContext, final String idempotencyHeader) {
        if (idempotencyHeader == null) {
            return false; // no filtering applies
        }
        final int idempotencyHeaderLength = idempotencyHeader.length();
        if (idempotencyHeaderLength != 36 || !BASE64_PATTERN.matcher(idempotencyHeader).matches()) {
            LOGGER.error("Invoked with bad HTTP header {} of length {}", T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY, idempotencyHeaderLength);
            abortFilter(requestContext, Response.status(Status.BAD_GATEWAY).build());
            return true;
        }
        return false;
    }
}
