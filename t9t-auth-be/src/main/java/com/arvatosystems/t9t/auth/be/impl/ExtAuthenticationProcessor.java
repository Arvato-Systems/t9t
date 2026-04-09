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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.pojos.api.auth.JwtAlg;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.auth.jwt.T9tJwtException;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.server.services.IExtAuthenticationProcessor;

@Singleton
public class ExtAuthenticationProcessor implements IExtAuthenticationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtAuthenticationProcessor.class);

    protected final IJWT jwtService = Jdp.getRequired(IJWT.class);

    @Override
    public AuthenticationInfo validateAndParseJwt(final String encodedJwt) {
        if (encodedJwt == null || encodedJwt.isBlank()) {
            LOGGER.warn("Received null or empty JWT token");   // should have been caught by the caller!
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }
        LOGGER.debug("Attempting to decode JWT token: {}", encodedJwt);  // FIXME (TBE-658): remove this line before moving to production!

        try {
            final JwtInfo jwtInfo = jwtService.decode(encodedJwt);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Decoded JwtInfo: {}", ToStringHelper.toStringML(jwtInfo));
            }

            // Build and return AuthenticationInfo
            final AuthenticationInfo authInfo = new AuthenticationInfo();
            authInfo.setJwtInfo(jwtInfo);
            authInfo.setEncodedJwt(encodedJwt);

            LOGGER.debug("Successfully parsed JWT for subject: {}", jwtInfo.getUserId());  // subject
            return authInfo;
        } catch (final Exception e) {
            LOGGER.error("Failed to decode JWT token: {}", e.getMessage(), e);

            // temp: decode without validation. FIXME (TBE-658): remove this code before moving to production!
            final String[] segments = encodedJwt.split("\\.");
            if (segments.length != 3) {
                throw new T9tJwtException(T9tJwtException.NUMBER_SEGMENTS, Integer.toString(segments.length));
            }

            // All segment should be base64
            final String headerSeg = segments[0];
            final String payloadSeg = segments[1];
            final String signatureSeg = segments[2];

            if (signatureSeg.length() == 0) {
                throw new T9tJwtException(T9tJwtException.MISSING_SIGNATURE);
            }

            // base64 decode and parse JSON
            final JwtAlg header;
            final JwtInfo payload;
            try {
                header = JwtConverter.parseAlg(new String(base64urlDecode(headerSeg), StandardCharsets.UTF_8));
                payload = JwtConverter.parseJwtInfo(new String(base64urlDecode(payloadSeg), StandardCharsets.UTF_8));
            } catch (final IllegalArgumentException e1) { // Decoding illegal information will throw an IllegalArgumentException which should be caught!
                LOGGER.error("Failed to decode JWT segments: malformed content: {}", e1.getMessage(), e1);
                throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
            }
            // Build and return AuthenticationInfo
            final AuthenticationInfo authInfo = new AuthenticationInfo();
            authInfo.setJwtInfo(payload);
            authInfo.setEncodedJwt(encodedJwt);

            LOGGER.debug("Parsed JWT for subject (with ignored failed validation for header {}): {}", header, payload.getUserId());  // subject
            return authInfo;
        }
    }

    private static byte[] base64urlDecode(final String str) {
        return Base64.getUrlDecoder().decode(str.getBytes(StandardCharsets.UTF_8));
    }
}
