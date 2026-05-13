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
package com.arvatosystems.t9t.auth.be.impl;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.auth.jwt.T9tJwtException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.server.services.IExtAuthenticationProcessor;

@Singleton
public class ExtAuthenticationProcessor implements IExtAuthenticationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtAuthenticationProcessor.class);

    protected final IJWT jwtService = Jdp.getRequired(IJWT.class, "extJwt");

    @Override
    @Nonnull
    public AuthenticationInfo validateAndParseJwt(@Nonnull final String encodedJwt) {
        if (T9tUtil.isBlank(encodedJwt)) {
            LOGGER.warn("Received null or empty JWT token");   // should have been caught by the caller!
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }
        LOGGER.debug("Attempting to decode external JWT token");

        final JwtInfo jwtInfo = jwtService.decode(encodedJwt);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Decoded JwtInfo: {}", ToStringHelper.toStringML(jwtInfo));
        }

        // Build and return AuthenticationInfo
        final AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setJwtInfo(jwtInfo);
        authInfo.setEncodedJwt(encodedJwt);

        LOGGER.debug("Successfully parsed external JWT for subject: {}", jwtInfo.getUserId());
        return authInfo;
    }
}
