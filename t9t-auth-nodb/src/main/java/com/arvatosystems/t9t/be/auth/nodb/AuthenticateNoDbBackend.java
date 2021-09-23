/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.server.services.IAuthenticate;

import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class AuthenticateNoDbBackend implements IAuthenticate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateNoDbBackend.class);
    private static final JwtInfo DEMO_JWT_INFO  = new JwtInfo();
    static {
        DEMO_JWT_INFO.setUserId("techuser");
        DEMO_JWT_INFO.setUserRef(1L);
        DEMO_JWT_INFO.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        DEMO_JWT_INFO.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        DEMO_JWT_INFO.setName("technical user");
        DEMO_JWT_INFO.setLocale("en");
        DEMO_JWT_INFO.setZoneinfo("UTC");
        DEMO_JWT_INFO.setPermissionsMin(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE));
        DEMO_JWT_INFO.setPermissionsMax(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE, OperationType.DELETE, OperationType.UPDATE));
        DEMO_JWT_INFO.freeze();
    }

    private static final AtomicLong sessionCounter = new AtomicLong(77000L);

    private final IJWT generator = Jdp.getRequired(IJWT.class); // JWT.createDefaultJwt();
    private final UUID apiKey;

    // constructor: retrieve the API key from the config file
    public AuthenticateNoDbBackend() {
        apiKey = ConfigProvider.getConfiguration().getNoDbBackendApiKey();
        if (apiKey == null) {
            LOGGER.error("Missing or invalid API key in config file");
        }
        LOGGER.info("Successfully retrieved API key for gateway services");
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest rq) {
        final AuthenticationParameters ap = rq.getAuthenticationParameters();
        if (ap == null) {
            LOGGER.info("Authentication without parameters");
            throw new ObjectValidationException(ObjectValidationException.MAY_NOT_BE_BLANK);
        }
        LOGGER.info("Authentication for method {}", ap.ret$PQON());
        if (!(ap instanceof ApiKeyAuthentication)) {
            throw new UnsupportedOperationException("Unsupported authentication parameters");
        }
        final JwtInfo jwt = auth((ApiKeyAuthentication)ap);

        if (jwt == null) {
            throw new ApplicationException(ApplicationException.CL_DENIED * 100000000 + 1);
        }

        final AuthenticationResponse authResp = new AuthenticationResponse();
        authResp.setJwtInfo(jwt);
        authResp.setEncodedJwt(generator.sign(jwt, 60 * 60L, null)); // 1 hour of validity
        authResp.setTenantName("Arvato Systems GmbH");
        authResp.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        authResp.setProcessRef(0L);
        authResp.setTenantNotUnique(false);
        authResp.setMustChangePassword(false);
        return authResp;
    }


    protected JwtInfo auth(ApiKeyAuthentication ap) {
        if (!ap.getApiKey().equals(apiKey)) {
            return null;  // reject invalid request
        }
        final JwtInfo myInfo = DEMO_JWT_INFO.ret$MutableClone(false, false);
        myInfo.setSessionId(UUID.randomUUID());
        myInfo.setSessionRef(sessionCounter.incrementAndGet());
        myInfo.setJsonTokenIdentifier(myInfo.getSessionRef().toString());
        return myInfo;
    }
}
