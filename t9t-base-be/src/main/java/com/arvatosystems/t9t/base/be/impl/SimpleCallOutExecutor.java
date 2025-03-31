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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.client.jdk11.RemoteConnection;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public final class SimpleCallOutExecutor implements IForeignRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCallOutExecutor.class);

    private static final Map<String, IForeignRequest> HANDLER_CACHE = new ConcurrentHashMap<>();
    private static final String MY_OWN_SERVER_ID = ConfigProvider.getConfiguration().getServerIdSelf();
    private final IRemoteConnection remoteConnection;

    private SimpleCallOutExecutor(final String key, final String url) {
        LOGGER.info("Creating simple remote call out connector for key {} at URL {}", key, url);
        remoteConnection = new RemoteConnection(url);
    }

    private static final class LocalJvmExecutor implements IForeignRequest {
        private final IExecutor executor = Jdp.getRequired(IExecutor.class);
        private LocalJvmExecutor(String key) {
            LOGGER.info("Installing local JVM executor for caller with key {}", key);
        }

        @Override
        public ServiceResponse execute(final RequestContext ctx, final RequestParameters rp) {
            return executor.executeSynchronous(ctx, rp);
        }
        @Override
        public ServiceResponse execute(final RequestParameters rp, final String apiKey) {
            throw new T9tException(T9tException.NO_LONGER_SUPPORTED);
        }
        @Override
        public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestContext ctx,
                final RequestParameters params, final Class<T> requiredType) {
            return executor.executeSynchronousAndCheckResult(ctx, params, requiredType);
        }
        @Override
        public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestParameters params, final String apiKey,
                final Class<T> requiredType) {
            throw new T9tException(T9tException.NO_LONGER_SUPPORTED);
        }
    }

    /**
     * Creates a new instance for the given URL, or returns an existing one for the given key.
     */
    public static IForeignRequest createCachedExecutor(final String key, final String url) {
        return HANDLER_CACHE.computeIfAbsent(key, unused -> {
            if ("local".equals(url) || key.equals(MY_OWN_SERVER_ID)) {
                return new LocalJvmExecutor(key);
            } else {
                return new SimpleCallOutExecutor(key, url);
            }
        });
    }

    public static IForeignRequest createUncachedExecutor(final String key, final String url) {
        return new SimpleCallOutExecutor(key, url);
    }

    protected ServiceResponse execute(final String authHeader, final RequestParameters rp) {
        final ServiceResponse response = remoteConnection.execute(authHeader, rp);
        if (response == null) {
            final ServiceResponse resp2 = new ServiceResponse();
            resp2.setReturnCode(T9tException.UPSTREAM_NULL_RESPONSE);
            return resp2;
        }
        return response;
    }

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RequestParameters rp) {
        return execute(T9tConstants.HTTP_AUTH_PREFIX_JWT + ctx.internalHeaderParameters.getEncodedJwt(), rp);
    }

    @Override
    public ServiceResponse execute(final RequestParameters rp, final String apiKey) {
        return execute(T9tConstants.HTTP_AUTH_PREFIX_API_KEY + apiKey, rp);
    }

    protected <T extends ServiceResponse> T executeSynchronousAndCheckResult(final String authHeader,
      final RequestParameters rp, final Class<T> requiredType) {
        final ServiceResponse response = execute(authHeader, rp);
        if (!ApplicationException.isOk(response.getReturnCode())) {
            throw new T9tException(response.getReturnCode(), response.getErrorDetails());
        }
        // the response must be a subclass of the expected one
        if (!requiredType.isAssignableFrom(response.getClass())) {
            LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}", rp.ret$PQON(),
                    requiredType.getSimpleName(), response.ret$PQON());
            throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, requiredType.getSimpleName());
        }
        return requiredType.cast(response); // all OK
    }

    @Override
    public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestContext ctx, final RequestParameters params,
            final Class<T> requiredType) {
        return executeSynchronousAndCheckResult(T9tConstants.HTTP_AUTH_PREFIX_JWT + ctx.internalHeaderParameters.getEncodedJwt(), params, requiredType);
    }

    @Override
    public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestParameters params, final String apiKey,
            final Class<T> requiredType) {
        return executeSynchronousAndCheckResult(T9tConstants.HTTP_AUTH_PREFIX_API_KEY + apiKey, params, requiredType);
    }
}
