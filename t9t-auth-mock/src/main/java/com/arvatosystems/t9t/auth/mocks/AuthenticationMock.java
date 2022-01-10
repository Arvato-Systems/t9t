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
package com.arvatosystems.t9t.auth.mocks;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.services.IAuthenticator;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthX500DistinguishedName;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.server.services.IAuthenticate;

import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Fallback
@Singleton
public class AuthenticationMock implements IAuthenticate {
    public static final String DEMO_API_KEY = "fec1e81e-0ca7-4709-b865-7150975d2c78";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationMock.class);
    private static final UUID DEMO_API_KEY_UUID = UUID.fromString(DEMO_API_KEY);
    private static final JwtInfo DEMO_JWT_INFO = getDemoJwtInfo();
    private static final AtomicLong SESSION_COUNTER = new AtomicLong(77000L);

    private static final String DEMO_USER_ID = "john";
    private static final String DEMO_PASSWORD = "password";

    private final IAuthenticator authenticator = Jdp.getRequired(IAuthenticator.class);

    private static JwtInfo getDemoJwtInfo() {
        final JwtInfo jwtInfo = new JwtInfo();
        jwtInfo.setUserId("john");
        jwtInfo.setUserRef(Long.valueOf(93847593L));
        jwtInfo.setTenantId("ACME");
        jwtInfo.setTenantRef(Long.valueOf(98347569L));
        jwtInfo.setName("John E. Smith");
        jwtInfo.setLocale("en");
        jwtInfo.setZoneinfo("UTC");
        jwtInfo.setPermissionsMin(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE));
        jwtInfo.setPermissionsMax(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ, OperationType.CREATE,
          OperationType.DELETE, OperationType.UPDATE));
        jwtInfo.freeze();
        return jwtInfo;
    }

    @Override
    public AuthenticationResponse login(final AuthenticationRequest rq) {
        final AuthenticationParameters ap = rq.getAuthenticationParameters();
        if (ap == null) {
            LOGGER.info("Authentication without parameters");
            throw new ObjectValidationException(ObjectValidationException.MAY_NOT_BE_BLANK);
        }
        LOGGER.info("Authentication for method {}", ap.ret$PQON());

        final JwtInfo jwt = auth(ap);

        if (jwt == null) {
            throw new ApplicationException(((ApplicationException.CL_DENIED * 100000000) + 1));
        }

        final AuthenticationResponse response = new AuthenticationResponse();
        response.setJwtInfo(jwt);
        response.setEncodedJwt(this.authenticator.doSign(jwt));
        response.setTenantName("ACME Corp.");
        response.setTenantId("ACME");
        response.setProcessRef(0L);

        return response;
    }

    protected JwtInfo authWithApiKey(final ApiKeyAuthentication ap) {
        if (ap.getApiKey() != DEMO_API_KEY_UUID) {
            return null;
        }
        final JwtInfo myInfo = DEMO_JWT_INFO.ret$MutableClone(false, false);
        myInfo.setSessionRef(SESSION_COUNTER.incrementAndGet());
        myInfo.setJsonTokenIdentifier(myInfo.getSessionRef().toString());
        return myInfo;
    }

    protected JwtInfo authWithPassword(final PasswordAuthentication ap) {
        if (DEMO_USER_ID != ap.getUserId() || DEMO_PASSWORD != ap.getPassword()) {
            return null;
        }
        final JwtInfo myInfo = DEMO_JWT_INFO.ret$MutableClone(false, false);
        myInfo.setSessionRef(SESSION_COUNTER.incrementAndGet());
        myInfo.setJsonTokenIdentifier(myInfo.getSessionRef().toString());
        return myInfo;
    }

    protected JwtInfo authWithAuthX500(final AuthX500DistinguishedName dn) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub");
    }

    protected JwtInfo authWithUnknown(final AuthenticationParameters unknown) {
        throw new UnsupportedOperationException("Unsupported authentication parameters");
    }

    protected JwtInfo auth(final AuthenticationParameters ap) {
        if (ap instanceof ApiKeyAuthentication) {
            return authWithApiKey((ApiKeyAuthentication) ap);
        } else if (ap instanceof AuthX500DistinguishedName) {
            return authWithAuthX500((AuthX500DistinguishedName) ap);
        } else if (ap instanceof PasswordAuthentication) {
            return authWithPassword((PasswordAuthentication) ap);
        } else if (ap != null) {
            return authWithUnknown(ap);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types.");
        }
    }
}
