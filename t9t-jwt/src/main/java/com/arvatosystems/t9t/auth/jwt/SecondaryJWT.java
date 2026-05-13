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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.auth.JwtAlg;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.util.ExceptionUtil;

import com.arvatosystems.t9t.base.T9tUtil;

/**
 * Secondary JWT implementation that validates JWTs issued by external providers (e.g., Keycloak).
 * Unlike {@link JWT}, certificates here are keyed by the issuer (the JWT "iss" claim), not by algorithm.
 * This class does not support signing - only decoding/validation.
 * This class is not annotated by @Singleton, because in t9t we use a pool of instances
 * (see {@link SecondaryJWTPool}).
 */
public final class SecondaryJWT extends AbstractJWT implements IJWT {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryJWT.class);
    private static final AtomicBoolean INSTANCES_CREATED = new AtomicBoolean();

    private final Map<String, Crypto> cryptoMap;
    private static String keystorePath = null;
    private static String keystorePw = "secret/changeMe";

    /**
     * Configures the secondary keystore path and password. Must be called before any instances are created.
     *
     * @param path     file system path to the JCEKS keystore, or null to keep current
     * @param password keystore password, or null to keep current
     */
    public static void setKeyStore(@Nullable final String path, @Nullable final String password) {
        if (INSTANCES_CREATED.get()) {
            throw new RuntimeException("Cannot modify static parameters after Secondary JWT instances have been created");
        }
        if (password != null) {
            keystorePw = password;
        }
        if (path != null) {
            keystorePath = path;
        }
    }

    /**
     * Creates a new SecondaryJWT from the configured keystore path.
     * If no path is configured, then throw an exception
     */
    public static SecondaryJWT createDefaultJwt() {
        if (keystorePath == null) {
            throw new RuntimeException("No secondary keystore path configured, secondary JWT validation not possible");
        }
        try {
            final SecondaryJWT jwt = createSecondaryJwt(new FileInputStream(keystorePath), keystorePw);
            INSTANCES_CREATED.set(true);
            return jwt;
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Secondary keystore file not found: " + keystorePath);
        }
    }

    /**
     * Creates a new SecondaryJWT from the given input stream and password.
     * Loads all trusted certificate entries from the keystore, using the alias as the issuer URL key.
     */
    public static SecondaryJWT createSecondaryJwt(@Nonnull final InputStream in, @Nonnull final String password) {
        LOGGER.debug("Creating new Secondary JWT authentication instance");
        final KeyStore ks = loadKeyStore(in, password);
        return new SecondaryJWT(ks, password.toCharArray());
    }

    private SecondaryJWT(@Nonnull final KeyStore keyStore, @Nonnull final char[] keyStorePassword) {
        final Map<String, Crypto> tmp = new HashMap<>();
        final Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            final Crypto cryptoSignature = getCryptoSignature(keyStore, keyStorePassword, alias);
            if (cryptoSignature != null) {
                tmp.put(alias, cryptoSignature);
            } else {
                LOGGER.warn("Secondary JWT issuer {} not available", alias);
            }
        }
        this.cryptoMap = Collections.unmodifiableMap(tmp);
        LOGGER.debug("Secondary JWT created with {} certificates", cryptoMap.size());
    }

    @Override
    @Nonnull
    public String sign(@Nonnull final JwtInfo info, @Nullable final Long expiresInSeconds, @Nullable final String algorithmOverride) {
        throw new UnsupportedOperationException("Secondary JWT does not support signing");
    }

    @Override
    @Nullable
    protected Crypto getCrypto(@Nonnull final JwtAlg header, @Nonnull final JwtInfo payload) {
        if (T9tUtil.isBlank(payload.getIssuer())) {
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED, "Missing issuer claim in secondary JWT");
        }
        return cryptoMap.get(payload.getIssuer());
    }

    @Nullable
    private Crypto getCryptoSignature(@Nonnull final KeyStore keyStore, @Nonnull final char[] keyStorePassword, @Nonnull final String alias) {
        try {
            final X509Certificate certificate = getCertificate(keyStore, alias);
            if (certificate != null) {
                // private key is not needed for verification
                return new CryptoSignature(certificate, null);
            }
        } catch (final Exception e) {
            LOGGER.warn("{} not supported: {}", alias, ExceptionUtil.causeChain(e));
        }
        return null;
    }
}

