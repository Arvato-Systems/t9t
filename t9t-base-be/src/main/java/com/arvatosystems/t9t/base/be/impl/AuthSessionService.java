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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.request.ApiKeySessionInvalidationRequest;
import com.arvatosystems.t9t.base.request.LoginSessionInvalidationRequest;
import com.arvatosystems.t9t.base.request.UserSessionInvalidationRequest;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.IAuthSessionService;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

import java.util.List;

@Singleton
public class AuthSessionService implements IAuthSessionService {

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public void loginSessionInvalidation(@Nonnull final RequestContext ctx, final long sessionRef) {
        final LoginSessionInvalidationRequest request = new LoginSessionInvalidationRequest();
        request.setSessionRef(sessionRef);
        sendRequest(ctx, request);
    }

    @Override
    public void userSessionInvalidation(@Nonnull final RequestContext ctx, @Nonnull final String userId, boolean removeInvalidation) {
        userSessionInvalidationOnCurrentServer(ctx, userId, removeInvalidation);
        userSessionInvalidationOnUplinkServer(userId, removeInvalidation, ctx.internalHeaderParameters.getEncodedJwt());
    }

    @Override
    public void userSessionInvalidationOnCurrentServer(@Nonnull final RequestContext ctx, @Nonnull final String userId, boolean removeInvalidation) {
        final UserSessionInvalidationRequest request = new UserSessionInvalidationRequest();
        request.setUserId(userId);
        request.setRemoveInvalidation(removeInvalidation);
        executor.executeSynchronous(ctx, request);
    }

    @Override
    public void userSessionInvalidationOnUplinkServer(@Nonnull final String userId, boolean removeInvalidation, @Nonnull final String encodedJwt) {
        final List<UplinkConfiguration> uplinkConfigs = ConfigProvider.getConfiguration().getUplinkConfiguration();
        if (uplinkConfigs != null) {
            final UserSessionInvalidationRequest request = new UserSessionInvalidationRequest();
            request.setUserId(userId);
            request.setRemoveInvalidation(removeInvalidation);
            final String authHeader = T9tConstants.HTTP_AUTH_PREFIX_JWT + encodedJwt;
            for (final UplinkConfiguration uplinkConfig : uplinkConfigs) {
                if (T9tUtil.isTrue(uplinkConfig.getInternalService())) {
                    final IForeignRequest remoteExecutor = SimpleCallOutExecutor.createCachedExecutor(uplinkConfig.getKey(), uplinkConfig.getUrl());
                    remoteExecutor.execute(authHeader, request);
                }
            }
        }
    }

    @Override
    public void apiKeySessionInvalidation(@Nonnull final RequestContext ctx, final long apiKeyRef, boolean removeInvalidation) {
        final ApiKeySessionInvalidationRequest request = new ApiKeySessionInvalidationRequest();
        request.setApiKeyRef(apiKeyRef);
        request.setRemoveInvalidation(removeInvalidation);
        sendRequest(ctx, request);
    }

    private void sendRequest(@Nonnull final RequestContext ctx, @Nonnull final RequestParameters request) {
        executor.executeSynchronous(ctx, request);
        final List<UplinkConfiguration> uplinkConfigs = ConfigProvider.getConfiguration().getUplinkConfiguration();
        if (uplinkConfigs != null) {
            for (final UplinkConfiguration uplinkConfig : uplinkConfigs) {
                if (T9tUtil.isTrue(uplinkConfig.getInternalService())) {
                    final IForeignRequest remoteExecutor = SimpleCallOutExecutor.createCachedExecutor(uplinkConfig.getKey(), uplinkConfig.getUrl());
                    remoteExecutor.execute(ctx, request);
                }
            }
        }
    }
}
