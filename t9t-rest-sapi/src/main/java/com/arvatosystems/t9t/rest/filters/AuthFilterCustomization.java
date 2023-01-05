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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.dp.Singleton;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class AuthFilterCustomization implements IAuthFilterCustomization {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestAuthenticationFilter.class);
    private static final int MAX_AUTH_ENTRIES = 200;
    private static final int MAX_SIZE_AUTH_HEADER = 4096;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES                       = 1000;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES   = 10;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES    = 10;
    private static final int DEFAULT_BAD_AUTHS_PER_IP_ADDRESS_LIMIT             = 50;

    private static final int MAX_BAD_IP_ADDRESSES
      = RestUtils.CONFIG_READER.getIntProperty("t9t.restapi.maxBadIp",              DEFAULT_MAX_BAD_IP_ADDRESSES);
    private static final int MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES
      = RestUtils.CONFIG_READER.getIntProperty("t9t.restapi.badAuthsPerIpDuration", DEFAULT_MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES);
    private static final int MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES
      = RestUtils.CONFIG_READER.getIntProperty("t9t.restapi.badIpLockoutDuration",  DEFAULT_MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES);
    private static final int BAD_AUTHS_PER_IP_ADDRESS_LIMIT
      = RestUtils.CONFIG_READER.getIntProperty("t9t.restapi.badAuthsPerIpLimit",    DEFAULT_BAD_AUTHS_PER_IP_ADDRESS_LIMIT);

    protected static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    protected static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
    protected final boolean enableSwagger = RestUtils.checkIfSet("t9t.restapi.swagger", Boolean.FALSE);
    protected final boolean enablePing = enableSwagger || RestUtils.checkIfSet("t9t.restapi.unauthedPing", Boolean.FALSE); // swagger implies ping

    protected final Cache<String, Boolean> goodAuths = Caffeine.newBuilder().maximumSize(MAX_AUTH_ENTRIES).expireAfterWrite(5L, TimeUnit.MINUTES)
      .<String, Boolean>build();
    protected final Cache<String, AtomicInteger> badAuthsPerIp;
    protected final Cache<String, Boolean> blockedIps;

    public AuthFilterCustomization() {
        if (MAX_BAD_IP_ADDRESSES <= 0) {
            badAuthsPerIp = null;
            blockedIps = null;
            LOGGER.info("Bad IP address check DISABLED because t9t.restapi.maxBadIp <= 0");
        } else {
            LOGGER.info("Bad IP address check configuration: {} max IP addresses, {} bad attempts per {} minutes disable an IP address for {} minutes",
                MAX_BAD_IP_ADDRESSES, BAD_AUTHS_PER_IP_ADDRESS_LIMIT, MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES, MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES);
            badAuthsPerIp = Caffeine.newBuilder()
                .maximumSize(MAX_BAD_IP_ADDRESSES)
                .expireAfterWrite(MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES, TimeUnit.MINUTES)
                .<String, AtomicInteger>build();
            blockedIps = Caffeine.newBuilder()
                .maximumSize(MAX_BAD_IP_ADDRESSES)
                .expireAfterWrite(MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES, TimeUnit.MINUTES)
                .<String, Boolean>build();
        }
    }

    /** Checks if the request came from a blocked IP address. */
    @Override
    public boolean isBlockedIpAddress(final String remoteIp) {
        if (blockedIps == null || remoteIp == null) {
            return false;
        }
        return blockedIps.getIfPresent(remoteIp) != null;
    }

    /** Records a failed authentication event. */
    @Override
    public void registerBadAuthFromIp(final String remoteIp) {
        if (badAuthsPerIp == null || remoteIp == null) {
            return;
        }
        final AtomicInteger counter = badAuthsPerIp.get(remoteIp, unused -> new AtomicInteger());
        final int newValue = counter.incrementAndGet();
        if (newValue >= BAD_AUTHS_PER_IP_ADDRESS_LIMIT) {
            // block this IP and reset the counter
            blockedIps.put(remoteIp, Boolean.TRUE);
            badAuthsPerIp.invalidate(remoteIp);
            LOGGER.warn("Too many bad authentication attempts from {} - temporarily blocking IP", remoteIp);
        }
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 401. */
    protected void throwUnauthorized() {
        throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
    }

    /** Constructs a new Response object for the current request and throw a WebApplicationException of code 403. */
    protected void throwForbidden() {
        throw new WebApplicationException(Response.status(Status.FORBIDDEN).build());
    }

    @Override
    public void filterUnauthenticated(final ContainerRequestContext requestContext) {
        final String path = requestContext.getUriInfo().getPath();
        LOGGER.debug("filterUnauthenticated for method {}, type {}, path {}", requestContext.getMethod(), requestContext.getMediaType(), path);
        switch (requestContext.getMethod()) {
        case HttpMethod.POST:
            // allow login, but only if JWT is enabled
            if (!isValidLogin(path, requestContext.getMethod())) {
                throwUnauthorized();
            }
            break;
        case HttpMethod.GET:
            if (enableSwagger && isValidSwagger(path)) {
                return; // OK
            }
            if (enablePing && isValidPing(path)) {
                return; // OK
            }
            if (isValidLogin(path, requestContext.getMethod())) {
                return; // GET type session
            }
            break;
        default:
            throwUnauthorized();
        }
    }

    @Override
    public void filterAuthenticated(final String authHeader, final ContainerRequestContext requestContext) {
        // first, check if we know this authentication - use a fast track in that case
        if (authHeader.length() <= MAX_SIZE_AUTH_HEADER && goodAuths.getIfPresent(authHeader) != null) {
            // fast track - we have successfully authenticated this one before
            return;
        }

        // must have valid authentication - first, check allowed size (depending on method)
        final int firstSpace = authHeader.indexOf(' ');
        if (firstSpace <= 0) {
            throwForbidden();
        }
        final String typeOfAuth = authHeader.substring(0, firstSpace + 1);  // add 1 because the constant includes the space
        final String authParam = authHeader.substring(firstSpace + 1);
        final int authLength = authParam.length();

        switch (typeOfAuth) {
        case T9tConstants.HTTP_AUTH_PREFIX_JWT:
            if (!allowAuthJwt()) {
                throwForbidden();
            }
            if (authLength < 10 || authLength > 4096) { // || !BASE64_PATTERN.matcher(authParam).matches()) {
                LOGGER.debug("Invalid JWT - length {}", authLength);
                throwForbidden();
            }
            filterJwt(authHeader, requestContext);
            break;
        case T9tConstants.HTTP_AUTH_PREFIX_API_KEY:
            if (!allowAuthApiKey() || authLength != 36 || !UUID_PATTERN.matcher(authParam).matches()) {
                LOGGER.debug("Invalid UUID - length {}", authLength);
                throwForbidden();
            }
            filterApiKey(authHeader, requestContext);
            break;
        case T9tConstants.HTTP_AUTH_PREFIX_USER_PW:
            if (!allowAuthBasic() || authLength < 8 || authLength > 80 || !BASE64_PATTERN.matcher(authParam).matches()) {
                throwForbidden();
            }
            filterBasic(authHeader, requestContext);
            break;
        default:
            throwForbidden();
        }
    }

    @Override
    public void filterBasic(final String authHeader, final ContainerRequestContext requestContext) {
        // TODO further checks on basic auth
    }

    @Override
    public void filterApiKey(final String authHeader, final ContainerRequestContext requestContext) {
        // TODO further checks on API key auth
    }

    @Override
    public void filterJwt(final String authHeader, final ContainerRequestContext requestContext) {
        // TODO further checks on JWT auth
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
    public void filterSupportedMediaType(final MediaType mediaType) {
        if (mediaType != null) {
            final String subType = mediaType.getSubtype();
            if (!"application".equals(mediaType.getType()) || !("xml".equals(subType) || "json".equals(subType))) {
                throw new WebApplicationException(Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build());
            }
        }
    }

    @Override
    public void filterCorrectIdempotencyPattern(final String idempotencyHeader, final ContainerRequestContext requestContext) {
        final int idempotencyHeaderLength = idempotencyHeader.length();
        if (idempotencyHeaderLength != 36 || !BASE64_PATTERN.matcher(idempotencyHeader).matches()) {
            LOGGER.error("Invoked with bad HTTP header {} of length {}", T9tConstants.HTTP_HEADER_IDEMPOTENCY_KEY, idempotencyHeaderLength);
            throw new WebApplicationException(Response.status(Status.BAD_GATEWAY).build());
        }
    }
}
