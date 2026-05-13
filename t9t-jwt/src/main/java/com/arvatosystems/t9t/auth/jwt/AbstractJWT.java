/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jwt;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.crypto.Mac;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.pojos.api.auth.JwtAlg;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

/**
 * Abstract base class for JWT implementations, providing shared utility methods and constants.
 */
public abstract class AbstractJWT implements IJWT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJWT.class);

    /** UTF-8 charset used throughout for base64url encoding/decoding and JSON parsing. */
    protected static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final String DEFAULT_KEYSTORE_TYPE = "jceks";

    /**
     * Loads a JCEKS keystore from the given input stream.
     *
     * @param in       the input stream to the JCEKS keystore file
     * @param password the keystore password
     * @return the loaded {@link KeyStore}
     * @throws RuntimeException if loading fails
     */
    protected static KeyStore loadKeyStore(final InputStream in, final String password) {
        try {
            final KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE_TYPE);
            ks.load(in, password.toCharArray());
            in.close();
            return ks;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load keystore " + DEFAULT_KEYSTORE_TYPE, e);
        }
    }

    /**
     * Splits a JWT token string into its three base64url-encoded segments and validates
     * that the segment count is exactly 3 and the signature segment is non-empty.
     *
     * @param token the compact JWT string (header.payload.signature)
     * @return a 3-element array: [headerSeg, payloadSeg, signatureSeg]
     * @throws T9tJwtException if the segment count is wrong or the signature is missing
     */
    protected static String[] splitAndValidateToken(final String token) {
        final String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new T9tJwtException(T9tJwtException.NUMBER_SEGMENTS, Integer.toString(segments.length));
        }
        if (segments[2].isEmpty()) {
            throw new T9tJwtException(T9tJwtException.MISSING_SIGNATURE);
        }
        return segments;
    }

    @Override
    @Nonnull
    public JwtInfo decode(@Nonnull final String token) {
        final String[] segments = splitAndValidateToken(token);
        final String headerSeg = segments[0];
        final String payloadSeg = segments[1];
        final String signatureSeg = segments[2];

        final JwtAlg header = parseHeader(headerSeg);
        final JwtInfo payload = parsePayload(payloadSeg);

        final Crypto crypto = getCrypto(header, payload);
        if (crypto == null) {
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }

        final String signingInput = headerSeg + "." + payloadSeg;
        try {
            if (!crypto.verify(base64urlDecode(signatureSeg), signingInput.getBytes(UTF8))) {
                throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return payload;
    }

    /**
     * Base64url-decodes and parses the JWT header segment into a {@link JwtAlg} object.
     *
     * @param headerSeg the base64url-encoded header segment
     * @return the parsed algorithm header
     * @throws T9tJwtException if the header cannot be decoded or parsed
     */
    @Nonnull
    protected static JwtAlg parseHeader(@Nonnull final String headerSeg) {
        try {
            return JwtConverter.parseAlg(new String(base64urlDecode(headerSeg), UTF8));
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Failed to decode JWT header segment. ", e);
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }
    }

    /**
     * Base64url-decodes and parses the JWT payload segment into a {@link JwtInfo} object.
     *
     * @param payloadSeg the base64url-encoded payload segment
     * @return the parsed JWT claims
     * @throws T9tJwtException if the payload cannot be decoded or parsed
     */
    @Nonnull
    protected static JwtInfo parsePayload(@Nonnull final String payloadSeg) {
        try {
            return JwtConverter.parseJwtInfo(new String(base64urlDecode(payloadSeg), UTF8));
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Failed to decode JWT payload segment. ", e);
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }
    }

    /**
     * Creates a new Message Authentication Code
     *
     * @param keyStore a valid JKS
     * @param alias algorithm to use e.g.: HmacSHA256
     * @return Mac implementation
     */
    @Nullable
    protected Mac getMac(@Nonnull final KeyStore keyStore, @Nonnull final char[] keyStorePassword, @Nonnull final String alias) {
        try {
            final Key secretKey = keyStore.getKey(alias, keyStorePassword);

            // key store does not have the requested algorithm
            if (secretKey == null) {
                return null;
            }

            final Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);

            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    protected X509Certificate getCertificate(@Nonnull final KeyStore keyStore, @Nonnull final String alias) {
        try {
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    protected PrivateKey getPrivateKey(@Nonnull final KeyStore keyStore, @Nonnull final char[] keyStorePassword, @Nonnull final String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, keyStorePassword);

        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /** Decodes a base64url-encoded string to bytes. */
    @Nonnull
    protected static byte[] base64urlDecode(@Nonnull final String str) {
        return Base64.getUrlDecoder().decode(str.getBytes(UTF8));
    }

    /** Encodes a string to base64url. */
    @Nonnull
    protected static String base64urlEncode(@Nonnull final String str) {
        return base64urlEncode(str.getBytes(UTF8));
    }

    /** Encodes a byte array to base64url. */
    @Nonnull
    protected static String base64urlEncode(@Nonnull final byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    @Nullable
    protected abstract Crypto getCrypto(@Nonnull JwtAlg header, @Nonnull JwtInfo payload);
}


