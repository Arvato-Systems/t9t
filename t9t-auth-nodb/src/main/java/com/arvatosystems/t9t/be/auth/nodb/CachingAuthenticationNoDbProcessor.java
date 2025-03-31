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
package com.arvatosystems.t9t.be.auth.nodb;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class CachingAuthenticationNoDbProcessor implements ICachingAuthenticationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingAuthenticationNoDbProcessor.class);

    protected static final AuthenticationInfo UNSUPPORTED_AUTH_METHOD = new AuthenticationInfo();
    static {
        UNSUPPORTED_AUTH_METHOD.setHttpStatusCode(403);
        UNSUPPORTED_AUTH_METHOD.setMessage("Unsupported authentication method");
        UNSUPPORTED_AUTH_METHOD.freeze();
    }
    protected static final AuthenticationInfo ACCESS_DENIED_INVALID_API_KEY = new AuthenticationInfo();
    static {
        ACCESS_DENIED_INVALID_API_KEY.setHttpStatusCode(403);
        ACCESS_DENIED_INVALID_API_KEY.setMessage("Invalid API key");
        ACCESS_DENIED_INVALID_API_KEY.freeze();
    }

    protected final IJWT jwt = Jdp.getRequired(IJWT.class);
    protected final IAuthenticate authModule = Jdp.getRequired(IAuthenticate.class);

    protected AuthenticationInfo storeSuccessful(final String header, final String encodedJwt, final JwtInfo jwtInfo) {
        final AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setEncodedJwt(encodedJwt);
        authInfo.setJwtInfo(jwtInfo);
//        authCache.put(header, authInfo);
        return authInfo;
    }

    @Override
    public AuthenticationInfo getCachedJwt(final String authorizationHeader) {
        if (!authorizationHeader.startsWith(T9tConstants.HTTP_AUTH_PREFIX_API_KEY)) {
            return UNSUPPORTED_AUTH_METHOD;
        }
        try {
            final UUID uuid = UUID.fromString(authorizationHeader.substring(8).trim());
            final AuthenticationResponse authResp = authModule.login(new AuthenticationRequest(new ApiKeyAuthentication(uuid)));
            if (authResp.getReturnCode() == 0) {
                return storeSuccessful(authorizationHeader, authResp.getEncodedJwt(), authResp.getJwtInfo());
            } else {
                LOGGER.info("Auth by API key rejected: Code {}: {} {}", authResp.getReturnCode(), authResp.getErrorMessage(), authResp.getErrorDetails());
            }
        } catch (final Exception e) {
            LOGGER.info("Bad API Key auth: {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return ACCESS_DENIED_INVALID_API_KEY;
    }
}
