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
package com.arvatosystems.t9t.base.be.execution;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.event.EventData;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//async processor without cross-server scaling. For vert.x please see AsyncProcessor which uses the event bus
@Singleton
public class LocalAsyncProcessor implements IAsyncRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAsyncProcessor.class);

    // must be lazy due to cyclic dependency
    @IsLogicallyFinal
    private IUnauthenticatedServiceRequestExecutor serviceRequestExecutor;

    protected final ExecutorService executorService;

    public LocalAsyncProcessor() {
        final T9tServerConfiguration config = ConfigProvider.getConfiguration();
        final int poolSize = config.getApplicationConfiguration() == null ? 4 : config.getApplicationConfiguration().getLocalAsyncPoolSize();
        LOGGER.info("LocalAsyncProcessor implementation selected - all async commands will be executed within same JVM, using an Executor pool size of {}",
                poolSize);
        final AtomicInteger counter = new AtomicInteger();

        executorService = Executors.newFixedThreadPool(poolSize, (final Runnable r) -> {
            final String threadName = "t9t-async-" + counter.incrementAndGet();
            LOGGER.info("Launching thread {} for local async processing", threadName);
            return new Thread(r, threadName);
        });
    }

    @Override
    public void submitTask(final ServiceRequest request, final boolean localNodeOnly, final boolean publish) {
        LOGGER.debug("async request {} submitted via LOCAL queue", request.getRequestParameters().ret$PQON());
        request.freeze(); // async must freeze it to avoid subsequent modification
        final IUnauthenticatedServiceRequestExecutor sre = getServiceRequestExecutor(); // resolve lazy binding outside of lambda
        executorService.submit(() -> {
            sre.executeTrusted(request);
        }); // perform trusted request - callers are internal
    }

    /** Sends event data to a single subscriber (node). */
    @Override
    public void send(final EventData data) {
        LOGGER.debug("async event {} sent via LocalAsyncProcessor", data.getData().ret$PQON());
        LOGGER.debug("(currently events are discarded in localtests)");
    }

    /** Publishes event data to all subscribers. */
    @Override
    public void publish(final EventData data) {
        LOGGER.debug("async event {} published via LocalAsyncProcessor", data.getData().ret$PQON());
        LOGGER.debug("(currently events are discarded in localtests)");
    }

    @Override
    public void registerSubscriber(final String eventID, final IEventHandler subscriber) {
        registerSubscriber(eventID, T9tConstants.GLOBAL_TENANT_ID, subscriber);
    }

    @Override
    public void registerSubscriber(final String eventID, final String tenantId, final IEventHandler subscriber) {
        LOGGER.debug("subscriber (not) registered: {} for {} of tenant {}", subscriber.getClass().getCanonicalName(), eventID, tenantId);
    }

    protected IUnauthenticatedServiceRequestExecutor getServiceRequestExecutor() {
        if (serviceRequestExecutor == null) {
            serviceRequestExecutor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);
            if (serviceRequestExecutor == null) {
                throw new RuntimeException("Lazy initialization of serviceRequestExecutor failed");
            }
        }
        return serviceRequestExecutor;
    }
}
