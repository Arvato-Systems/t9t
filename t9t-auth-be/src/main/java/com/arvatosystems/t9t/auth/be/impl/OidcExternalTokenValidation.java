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

import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.OidClaims;
import com.arvatosystems.t9t.auth.services.IExternalTokenValidation;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.OidConfiguration;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import de.jpaw.dp.Singleton;

/** Validate external OIDC and OAuth2 access tokens. */
@Singleton
public class OidcExternalTokenValidation implements IExternalTokenValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcExternalTokenValidation.class);

    private static final String DEFAULT_ISSUER_PREFIX = "https://sts.windows.net/";
    private static final String DEFAULT_ISSUER_SUFFIX = "/";
    private static final String DEFAULT_JWKS_URI_PREFIX = "https://login.microsoftonline.com/";
    private static final String DEFAULT_JWKS_URI_SUFFIX = "/discovery/v2.0/keys";

    private static final long TTL = 60 * 60 * 1000; // 1 hour
    private static final long REFRESH_TIMEOUT = 60 * 1000; // 1 minute

    protected final OidConfiguration oidConfiguration = ConfigProvider.getConfiguration().getOidConfiguration();
    protected final String jwksUri = oidConfiguration == null ? null : oidConfiguration.getJwksUri() != null
            ?  oidConfiguration.getJwksUri()
            : DEFAULT_JWKS_URI_PREFIX + oidConfiguration.getTenantId() + DEFAULT_JWKS_URI_SUFFIX;
    protected final String issuer = oidConfiguration == null ? null : oidConfiguration.getIssuer() != null
            ?  oidConfiguration.getIssuer()
            : DEFAULT_ISSUER_PREFIX + oidConfiguration.getTenantId().toString() + DEFAULT_ISSUER_SUFFIX;
    protected final String audience = oidConfiguration == null ? null : oidConfiguration.getAudience();

    // a customizable (possibly filtered) debug output of claims we intend to use
    protected void listClaims(final JWTClaimsSet claimsSet) {
        LOGGER.debug("Full set of provided claims is {}", claimsSet.toString());
    }

    @Override
    public OidClaims validateToken(final String accessToken) {
        if (oidConfiguration == null) {
            LOGGER.debug("Token not validated, no OID configuration in server.xml");
            return null;
        }
        try {
            LOGGER.debug("Validating access token");
            final URL jwksUrl = new URL(jwksUri);
            final SecurityContext ctx = new SimpleSecurityContext();
            final JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(jwksUrl).retrying(true).cache(TTL, REFRESH_TIMEOUT).build();
            final JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
            final ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(keySelector);

            final JWTClaimsSet exactMatchClaims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .build();
            jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(exactMatchClaims, null));

            final JWTClaimsSet claimsSet;
            try {
                claimsSet = jwtProcessor.process(accessToken, ctx);
            } catch (final BadJOSEException ex) {
                LOGGER.debug("Access token validation fail. Reason: {}", ex.getMessage());
                return null;
            }
            listClaims(claimsSet);
            final Date now = new Date();
            final Date expDate = claimsSet.getExpirationTime();
            if (expDate != null && expDate.before(now)) {
                LOGGER.debug("Access token already expired. Exp Date: {}", expDate);
                return null;
            }
            final Date nbfDate = claimsSet.getNotBeforeTime();
            if (nbfDate != null && nbfDate.after(now)) {
                LOGGER.debug("Access token not yet valid. NBF Date: {}", nbfDate);
                return null;
            }
            final OidClaims selectedClaims = new OidClaims();
            if (expDate != null) {
                selectedClaims.setExp(expDate.getTime() / 1000L);
            }
            selectedClaims.setUpn(MessagingUtil.truncField(T9tUtil.nvl(claimsSet.getStringClaim("upn"), claimsSet.getStringClaim("unique_name")), 255));
            selectedClaims.setIdp(MessagingUtil.truncField(T9tUtil.nvl(claimsSet.getStringClaim("idp"), claimsSet.getStringClaim("iss")), 80));
            selectedClaims.setOid(MessagingUtil.truncField(claimsSet.getStringClaim("oid"), 36));
            selectedClaims.setIpAddress(MessagingUtil.truncField(claimsSet.getStringClaim("ipaddr"), 255));
            selectedClaims.setEmailAddress(MessagingUtil.truncField(claimsSet.getStringClaim("email"), 255));
            selectedClaims.setName(MessagingUtil.truncField(claimsSet.getStringClaim("name"), 80));
            return selectedClaims;
        } catch (final Exception ex) {
            LOGGER.error("Unable to validate token. Cause: {} ", ex);
            return null;
        }
    }
}
