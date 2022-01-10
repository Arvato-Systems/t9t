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
package com.arvatosystems.t9t.auth.jwt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Mac;

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
public final class JWT implements IJWT {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWT.class);
    private static final Charset UTF8 = StandardCharsets.UTF_8;
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
        try {
            final KeyStore ks = KeyStore.getInstance("jceks");
            ks.load(in, password.toCharArray());
            in.close();

            return new JWT(ks, password.toCharArray());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Creates a new Message Authentication Code
     *
     * @param keyStore a valid JKS
     * @param alias algorithm to use e.g.: HmacSHA256
     * @return Mac implementation
     */
    private Mac getMac(final KeyStore keyStore, final char[] keyStorePassword, final String alias) {
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

    private X509Certificate getCertificate(final KeyStore keyStore, final String alias) {
        try {
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey getPrivateKey(final KeyStore keyStore, final char[] keyStorePassword, final String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, keyStorePassword);

        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JwtInfo decode(final String token) {
        final String[] segments = token.split("\\.");
        if (segments.length != 3)
            throw new T9tJwtException(T9tJwtException.NUMBER_SEGMENTS, Integer.toString(segments.length));

        // All segment should be base64
        final String headerSeg = segments[0];
        final String payloadSeg = segments[1];
        final String signatureSeg = segments[2];

        if (signatureSeg.length() == 0)
            throw new T9tJwtException(T9tJwtException.MISSING_SIGNATURE);

        // base64 decode and parse JSON
        JwtAlg header;
        JwtInfo payload;
        try {
            header = JwtConverter.parseAlg(new String(base64urlDecode(headerSeg), UTF8));
            payload = JwtConverter.parseJwtInfo(new String(base64urlDecode(payloadSeg), UTF8));
        } catch (final IllegalArgumentException e) { // Decoding illegal information will throw an IllegalArgumentException which should be caught!
            throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        }

        final Crypto crypto = cryptoMap.get(header.getAlg());

        if (crypto == null)
            throw new T9tJwtException(T9tJwtException.ALGORITHM_NOT_SUPPORTED, header.getAlg());

        // verify signature. `sign` will return base64 string.
        final String signingInput = headerSeg + "." + payloadSeg;

        try {
            if (!crypto.verify(base64urlDecode(signatureSeg), signingInput.getBytes(UTF8)))
                throw new T9tJwtException(T9tJwtException.VERIFICATION_FAILED);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return payload;

    }

    @Override
    public String sign(final JwtInfo info, final Long expiresInSeconds, final String algorithmOverride) {
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

    private static byte[] base64urlDecode(final String str) {
        return Base64.getUrlDecoder().decode(str.getBytes(UTF8));
    }

    private static String base64urlEncode(final String str) {
        return base64urlEncode(str.getBytes(UTF8));
    }

    private static String base64urlEncode(final byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}
