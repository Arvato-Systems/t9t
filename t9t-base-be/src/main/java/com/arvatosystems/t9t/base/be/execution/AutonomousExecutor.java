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
package com.arvatosystems.t9t.base.be.execution;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
    public ServiceResponse execute(RequestContext ctx, RequestParameters rp) {
        final InternalHeaderParameters ihdr = ctx.internalHeaderParameters;
        final ServiceRequestHeader requestHeader = new ServiceRequestHeader();
        requestHeader.setInvokingProcessRef(ctx.getRequestRef()); // transfer the invoker
        requestHeader.setMessageId(ihdr.getMessageId());
        requestHeader.setIdempotencyBehaviour(ihdr.getIdempotencyBehaviour());
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        final Future<ServiceResponse> f = executorService.submit(() -> {
            MDC.setContextMap(mdcContext); // Inherit MDC context and ensure old MDC of this worker is reset
            return requestProcessor.execute(requestHeader, rp, ihdr.getJwtInfo(), ihdr.getEncodedJwt(), true);
        });
        try {
            return f.get();
        } catch (InterruptedException e) {
            LOGGER.error("Thread got interrupted", e);
            return new ServiceResponse(T9tException.THREAD_INTERRUPTED);
        } catch (ExecutionException e) {
            LOGGER.error("Thread failed to excetute", e);
            return new ServiceResponse(T9tException.GENERAL_EXCEPTION);
        }
    }

    @Override
    public ExecutorService getExecutorServiceForMetering() {
        return executorService;
    }
}
