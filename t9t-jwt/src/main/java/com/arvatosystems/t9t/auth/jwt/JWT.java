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
package com.arvatosystems.t9t.auth.jwt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Mac;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.pojos.api.auth.JwtAlg;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.util.ExceptionUtil;

/**
 * JWT and JWS implementation draft-ietf-oauth-json-web-token-32.
 *
 * @author Paulo Lopes
 * Modified to fit into bonaparte environment by Arvato Systems.
 *
 * This class is not annotated by @Singleton, because this project does not have a Jdp dependency,
 * and also because in t9t we use a pool of JWTs.
 */
public final class JWT extends AbstractJWT implements IJWT {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWT.class);
    private static final AtomicBoolean INSTANCES_CREATED = new AtomicBoolean();

    private final Map<String, Crypto> cryptoMap;
    private static String defaultKeystore = "/t9tkeystore.jceks";
    private static String defaultKeystorePw = "secret/changeMe";
    private static boolean keystoreFromResource = true;         // load the keystore from resource instead of file system

    public static void setKeyStore(final String path, final String password, final boolean isResource) {
        if (INSTANCES_CREATED.get()) {
            throw new RuntimeException("Cannot modify static parameters after default JWTs have been created");
        }
        if (password != null) {
            defaultKeystorePw = password;
        }
        if (path != null) {
            defaultKeystore = path;
            keystoreFromResource = isResource;
        }
    }

    public static JWT createDefaultJwt() {
        INSTANCES_CREATED.set(true);
        try {
            return createJwt(keystoreFromResource
                    ? IJWT.class.getResourceAsStream(defaultKeystore)
                    : new FileInputStream(defaultKeystore),
                    defaultKeystorePw);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("No keystore file " + defaultKeystore + " found");
        }
    }

    public static JWT createJwt(final InputStream in, final String password) {
        if (in == null) {
            LOGGER.info("No keystore inputstream provided - keystore file may be missing from the deployment");
            throw new RuntimeException("No inputstream for keyfile provided");
        }
        LOGGER.debug("Creating new JWT authentication instance");
        final KeyStore ks = loadKeyStore(in, password);
        return new JWT(ks, password.toCharArray());
    }

    public JWT(final KeyStore keyStore, final char[] keyStorePassword) {

        final Map<String, Crypto> tmp = new HashMap<>();

        // load MACs
        for (final String alg : Arrays.<String>asList("HS256", "HS384", "HS512")) {
            try {
                final Mac mac = getMac(keyStore, keyStorePassword, alg);
                if (mac != null) {
                    tmp.put(alg, new CryptoMac(mac));
                } else {
                    LOGGER.warn("{} not available", alg);
                }
            } catch (final RuntimeException e) {
                LOGGER.warn(alg + " not supported", e);
            }
        }

        // load SIGNATUREs
        for (final String alg : Arrays.<String>asList("RS256", "RS384", "RS512", "ES256", "ES384", "ES512")) {
            try {
                final X509Certificate certificate = getCertificate(keyStore, alg);
                final PrivateKey privateKey = getPrivateKey(keyStore, keyStorePassword, alg);
                if (certificate != null && privateKey != null) {
                    tmp.put(alg, new CryptoSignature(certificate, privateKey));
                } else {
                    LOGGER.warn("JWT algorithm {} not available", alg);
                }
            } catch (final Exception e) {
                LOGGER.warn("{} not supported: {}", alg, ExceptionUtil.causeChain(e));
            }
        }

        // Spec requires "none" to always be available, but according to https://www.owasp.org/index.php/JSON_Web_Token_(JWT)_Cheat_Sheet_for_Java
        // that would be a very bad idea! Therefore we do not do that.

        cryptoMap = Collections.unmodifiableMap(tmp);
    }

    @Override
    @Nonnull
    public String sign(@Nonnull final JwtInfo info, @Nullable final Long expiresInSeconds, @Nullable final String algorithmOverride) {
        final String algorithm = algorithmOverride == null ? "HS256" : algorithmOverride;

        final Crypto crypto = cryptoMap.get(algorithm);
        if (crypto == null)
            throw new T9tJwtException(T9tJwtException.ALGORITHM_NOT_SUPPORTED, algorithm);

        // header, typ is fixed value.
        final String header = "{\"typ\":\"JWT\",\"alg\":\"" + algorithm + "\"}";

        // set the "issued at" field
        final long timestamp = System.currentTimeMillis() / 1000L;  // divide it to get 1 second precision
        info.setIssuedAt(Instant.ofEpochSecond(timestamp));

        // if a duration has been given, set the expiry time
        if (expiresInSeconds != null)
            info.setExpiresAt(Instant.ofEpochSecond(timestamp + expiresInSeconds.longValue()));

        final Map<String, Object> payload = JwtConverter.asMap(info);
        payload.put(MimeTypes.JSON_FIELD_PQON, "api.auth.JwtPayload");

        final String json = BonaparteJsonEscaper.asJson(payload);

        try {
            // create segments, all segment should be base64 string
            final String headerSegment = base64urlEncode(header);
            final String payloadSegment = base64urlEncode(json);
            final String signingInput = headerSegment + "." + payloadSegment;
            final String signSegment = base64urlEncode(crypto.sign(signingInput.getBytes(UTF8)));
            return headerSegment + "." + payloadSegment + "." + signSegment;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    protected Crypto getCrypto(@Nonnull final JwtAlg header, @Nonnull final JwtInfo payload) {
        return cryptoMap.get(header.getAlg());
    }
}
