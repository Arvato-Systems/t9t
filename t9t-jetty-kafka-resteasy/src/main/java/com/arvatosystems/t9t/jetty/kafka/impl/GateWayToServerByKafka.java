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
package com.arvatosystems.t9t.jetty.kafka.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authz.api.QuerySinglePermissionRequest;
import com.arvatosystems.t9t.authz.api.QuerySinglePermissionResponse;
import com.arvatosystems.t9t.base.IKafkaRequestTransmitter;
import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.jetty.kafka.IGateWayToServerByKafka;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class GateWayToServerByKafka implements IGateWayToServerByKafka {
    private static final Logger LOGGER = LoggerFactory.getLogger(GateWayToServerByKafka.class);

    protected final IKafkaRequestTransmitter kafkaTransmitter = Jdp.getRequired(IKafkaRequestTransmitter.class);
    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);

    protected final Cache<String, Boolean> authenticationCache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).<String, Boolean>build();

    @Override
    public Response.Status sendToServer(final String authentication, final String partitionKey, final RequestParameters request) {
        // first, check if permissions exist
        if (authentication == null) {
            return Status.UNAUTHORIZED;
        }
        final String trimmedAuth = authentication.trim();
        if (trimmedAuth.length() == 0) {
            return Status.UNAUTHORIZED;
        }
        final Boolean isAuthenticated = authenticationCache.getIfPresent(trimmedAuth);
        if (Boolean.FALSE.equals(isAuthenticated)) {
            return Status.FORBIDDEN;
        }
        if (isAuthenticated == null) {
            // must query backend
            try {
                final QuerySinglePermissionRequest permRq = new QuerySinglePermissionRequest();
                permRq.setPermissionType(PermissionType.BACKEND);
                permRq.setResourceId(request.ret$PQON());
                final ServiceResponse resp = connection.execute(trimmedAuth, permRq);
                if (ApplicationException.isOk(resp.getReturnCode()) && resp instanceof QuerySinglePermissionResponse) {
                    final QuerySinglePermissionResponse qspr = (QuerySinglePermissionResponse)resp;
                    if (!qspr.getPermissions().contains(OperationType.EXECUTE)) {
                        // forbidden
                        authenticationCache.put(trimmedAuth, Boolean.FALSE);
                        return Status.FORBIDDEN;
                    }
                    // here: OK!
                    authenticationCache.put(trimmedAuth, Boolean.TRUE);
                    // fall through
                } else {
                    // some error
                    return Status.INTERNAL_SERVER_ERROR;
                }
            } catch (Exception e) {
                LOGGER.error("Cannot determine permissions:", e);
                return Status.INTERNAL_SERVER_ERROR;
            }
        }
        // at this point, we know it is authenticated
        kafkaTransmitter.write(request, partitionKey);

        return Status.OK;
    }

    @Override
    public <T extends BonaPortable, R extends RequestParameters> Response
      sendToServer(String authentication, String pathInfo, List<T> inputData,
            Function<T, R> requestParameterConverter, Function<R, String> partitionKeyExtractor) {
        if (inputData.size() == 1) {
            try {
                final R rq = requestParameterConverter.apply(inputData.get(0));
                final String partitionKey = partitionKeyExtractor.apply(rq);
                LOGGER.debug("Sending object of key {} via path {}", partitionKey, pathInfo);
                return responseFromStatus(sendToServer(authentication, partitionKey, rq));
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request conversion for {}: {}: {}", pathInfo, e.getMessage(), ExceptionUtil.causeChain(e));
                LOGGER.error("Return code = {}, details = {}", e.getErrorCode(), e.getMessage());
                return responseFromStatus(Status.BAD_REQUEST);
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion for {}: {}: {}", pathInfo, e.getMessage(), ExceptionUtil.causeChain(e));
                return responseFromStatus(Status.BAD_REQUEST);
            }
        } else {
            LOGGER.error("Not exactly one record posted, but {} for {}", inputData.size(), pathInfo);
            return responseFromStatus(Status.BAD_REQUEST);
        }
    }

    protected Response responseFromStatus(final Status status) {
        final Response.ResponseBuilder response = Response.status(status);
        final Response responseObj = response.build();
        return responseObj;
    }
}
