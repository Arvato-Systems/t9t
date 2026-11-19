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
package com.arvatosystems.t9t.remote.connect;

import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.util.ApplicationException;

import com.arvatosystems.t9t.api.auth.JwtInfo;
import com.arvatosystems.t9t.authc.api.SwitchLanguageRequest;
import com.arvatosystems.t9t.authc.api.SwitchTenantRequest;
import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;

public abstract class AbstractConnection extends ConnectionDefaults implements ITestConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnection.class);

    // Cache the last JWT / JwtInfo record.
    private String lastJwt;
    private JwtInfo lastJwtInfo;

    // Run a request and expect that it returns a ServiceResponse.
    @Override
    @Nonnull
    public final ServiceResponse srIO(@Nonnull final RequestParameters rp) {
        final Object resp = doIO(rp);
        if (resp == null) {
            LOGGER.error("Request {} returned null", rp.ret$PQON());
        } else if (resp instanceof ServiceResponse) {
            return (ServiceResponse)resp;
        } else {
            LOGGER.error("Request {} returned an object of class {}", rp.ret$PQON(), resp.getClass().getCanonicalName());
        }
        throw new RuntimeException("Abnormal end");
    }

    // Run a request and expect that it completes successfully.
    @Override
    @Nonnull
    public final ServiceResponse okIO(@Nonnull final RequestParameters rp) {
        final ServiceResponse resp = srIO(rp);
        if (ApplicationException.isOk(resp.getReturnCode())) {
            return resp;
        }
        LOGGER.error("Request {} returned an error: code {}: {} ({})", rp.ret$PQON(), resp.getReturnCode(), resp.getErrorDetails(), resp.getErrorMessage());
        throw new RuntimeException("Abnormal end");
    }

    // Run a request and expect that it completes successfully with a specific response type.
    @Override
    @Nonnull
    public final <T extends ServiceResponse> T typeIO(@Nonnull final RequestParameters rp, @Nonnull final Class<T> responseClass) {
        final ServiceResponse resp = okIO(rp);
        if (responseClass.isAssignableFrom(resp.getClass())) {
            return responseClass.cast(resp);
        }
        LOGGER.error("Request {} returned class {}, but expected {}", rp.ret$PQON(), resp.getClass().getCanonicalName(), responseClass.getCanonicalName());
        throw new RuntimeException("Abnormal end");
    }

    // Run a request and expect a specific error code.
    @Override
    public final void errIO(@Nonnull final RequestParameters rp, final int errorCode) {
        final ServiceResponse resp = srIO(rp);
        if (resp.getReturnCode() != errorCode) {
            LOGGER.error("Request {} expected {}, but returned {}: {} ({})", rp.ret$PQON(), errorCode, resp.getReturnCode(), resp.getErrorDetails(), resp.getErrorMessage());
            throw new RuntimeException("Abnormal end");
        }
        LOGGER.info("Expected / wanted error {} and got it.", errorCode);
    }

    @Override
    @Nonnull
    public final AuthenticationResponse auth(@Nonnull final AuthenticationParameters params) {
        // Before a new auth attempt, clear the last.
        lastJwtInfo = null;
        lastJwt = null;
        final AuthenticationRequest request = new AuthenticationRequest();
        request.setAuthenticationParameters(params);
        request.setSessionParameters(SESSION_PARAMETERS);
        request.validate();
        final AuthenticationResponse authResult = typeIO(request, AuthenticationResponse.class);
        lastJwtInfo = authResult.getJwtInfo();
        lastJwt = authResult.getEncodedJwt();
        return authResult;
    }

    @Override
    @Nonnull
    public final AuthenticationResponse auth(@Nonnull final String myUserId, @Nonnull final String myPassword) {
        final PasswordAuthentication params = new PasswordAuthentication();
        params.setUserId(myUserId);
        params.setPassword(myPassword);
        return auth(params);
    }

    @Override
    @Nonnull
    public final AuthenticationResponse changePassword(@Nonnull final String myUserId, @Nonnull final String myPassword, @Nonnull final String newPassword) {
        final PasswordAuthentication params = new PasswordAuthentication();
        params.setUserId(myUserId);
        params.setPassword(myPassword);
        params.setNewPassword(newPassword);
        return auth(params);
    }

    @Override
    @Nonnull
    public final AuthenticationResponse auth(@Nonnull final UUID apiKey) {
        return auth(new ApiKeyAuthentication(apiKey));
    }

    @Override
    public void switchTenant(@Nonnull final String newTenantId, final int expectedCode) {
        final SwitchTenantRequest request = new SwitchTenantRequest(newTenantId);
        if (ApplicationException.isOk(expectedCode)) {
            final AuthenticationResponse authResult = typeIO(request, AuthenticationResponse.class);
            lastJwtInfo = authResult.getJwtInfo();
            lastJwt = authResult.getEncodedJwt();
            setAuthentication(T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.getEncodedJwt()); // the encoded token
        } else {
            errIO(request, expectedCode);
        }
    }

    @Override
    public void switchLanguage(@Nonnull final String newLanguage) {
        final SwitchLanguageRequest request = new SwitchLanguageRequest(newLanguage);
        final AuthenticationResponse authResult = typeIO(request, AuthenticationResponse.class);
        lastJwtInfo = authResult.getJwtInfo();
        lastJwt = authResult.getEncodedJwt();
        setAuthentication(T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.getEncodedJwt()); // the encoded token
    }

    @Override
    @Nullable
    public String getLastJwt() {
        return lastJwt;
    }

    @Override
    @Nullable
    public JwtInfo getLastJwtInfo() {
        return lastJwtInfo;
    }
}
