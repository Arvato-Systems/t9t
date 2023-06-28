/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
public class AutonomousExecutor implements IAutonomousExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutonomousExecutor.class);
    private static final int DEFAULT_MAX_AUTONOMOUS_TRANSACTIONS = 4;

    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);
    protected final ExecutorService executorService;

    public AutonomousExecutor() {
        final ApplicationConfiguration applicationConfig = ConfigProvider.getConfiguration().getApplicationConfiguration();
        final Integer autoPoolSizeByCfg = applicationConfig == null ? null : applicationConfig.getAutonomousPoolSize();
        final int autoPoolSize = autoPoolSizeByCfg == null ? DEFAULT_MAX_AUTONOMOUS_TRANSACTIONS : autoPoolSizeByCfg;
        LOGGER.info("Creating a new thread pool for autonomous transactions of size {}", autoPoolSize);

        final AtomicInteger counter = new AtomicInteger();
        executorService = Executors.newFixedThreadPool(autoPoolSize, (final Runnable r) -> {
            final String threadName = "t9t-autonomous-" + counter.incrementAndGet();
            LOGGER.debug("Launching thread {} of {} for local autonomous transactions", threadName, autoPoolSize);
            return new Thread(r, threadName);
        });
    }

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RequestParameters rp, final boolean skipPermissionCheck) {
        final InternalHeaderParameters ihdr = ctx.internalHeaderParameters;
        final ServiceRequestHeader requestHeader = new ServiceRequestHeader();
        requestHeader.setInvokingProcessRef(ctx.getRequestRef()); // transfer the invoker
        requestHeader.setMessageId(ihdr.getMessageId());
        requestHeader.setIdempotencyBehaviour(ihdr.getIdempotencyBehaviour());
        requestHeader.setPlannedRunDate(Instant.now());
        if (!rp.was$Frozen()) {
            rp.setWhenSent(requestHeader.getPlannedRunDate().toEpochMilli());
            rp.setTransactionOriginType(TransactionOriginType.AUTONOMOUS);
        }
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        final Future<ServiceResponse> f = executorService.submit(() -> {
            MDC.setContextMap(mdcContext); // Inherit MDC context and ensure old MDC of this worker is reset
            return requestProcessor.execute(requestHeader, rp, ihdr.getJwtInfo(), ihdr.getEncodedJwt(), skipPermissionCheck, null);
        });
        try {
            return f.get();
        } catch (InterruptedException e) {
            LOGGER.error("Thread got interrupted", e);
            return new ServiceResponse(T9tException.THREAD_INTERRUPTED);
        } catch (ExecutionException e) {
            LOGGER.error("Thread failed to execute", e);
            return new ServiceResponse(T9tException.GENERAL_EXCEPTION);
        }
    }

    @Override
    public ExecutorService getExecutorServiceForMetering() {
        return executorService;
    }

    @Override
    public <T extends ServiceResponse> T executeAndCheckResult(final RequestContext ctx, final RequestParameters rp, final Class<T> responseClass) {
        final ServiceResponse response = execute(ctx, rp, true);
        if (!ApplicationException.isOk(response.getReturnCode())) {
            LOGGER.error("Error during request handler execution for {} (returnCode={}, errorMsg={}, errorDetails={})", rp.ret$PQON(),
                    response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
            throw new T9tException(response.getReturnCode(), response.getErrorDetails());
        }
        // the response must be a subclass of the expected one
        if (!responseClass.isAssignableFrom(response.getClass())) {
            LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}", rp.ret$PQON(),
                    responseClass.getSimpleName(), response.ret$PQON());
            throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, responseClass.getSimpleName());
        }
        return responseClass.cast(response); // all OK
    }
}
