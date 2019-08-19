/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.event.EventData
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor
import com.arvatosystems.t9t.base.services.IEventHandler
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor
import de.jpaw.annotations.AddLogger
import de.jpaw.annotations.Lazy
import de.jpaw.dp.Jdp
import de.jpaw.dp.Singleton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

// async processor without cross-server scaling. For vert.x please see AsyncProcessor which uses the event bus
@AddLogger
@Singleton
class LocalAsyncProcessor implements IAsyncRequestProcessor {
    // private static LinkedTransferQueue<ServiceRequest> ltq = new LinkedTransferQueue<ServiceRequest>

    // must be lazy due to cyclic dependency
    @Lazy IUnauthenticatedServiceRequestExecutor serviceRequestExecutor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor)

    protected final ExecutorService executorService

    new() {
        val config = ConfigProvider.configuration
        val poolSize = config.applicationConfiguration?.localAsyncPoolSize ?: 4
        LOGGER.info("LocalAsyncProcessor implementation selected - all async commands will be executed within same JVM, using an Executor pool size of {}", poolSize);
        val counter = new AtomicInteger()
        executorService =  Executors.newFixedThreadPool(poolSize) [
            val threadName = "t9t-async-" + counter.incrementAndGet
            LOGGER.info("Launching thread {} for local async processing", threadName)
            return new Thread(it, threadName)
        ]
    }

    override submitTask(ServiceRequest request) {
        LOGGER.debug("async request {} submitted via LOCAL queue", request.requestParameters.ret$PQON)
        request.freeze  // async must freeze it to avoid subsequent modification
        val sre = getServiceRequestExecutor()        // resolve lazy binding outside of lambda
        executorService.submit [ sre.executeTrusted(request) ]  // perform trusted request - callers are internal
        // ltq.put(request)
    }

    /** Sends event data to a single subscriber (node). */
    override send(EventData data) {
        LOGGER.debug("async event {} sent via LocalAsyncProcessor", data.data.ret$PQON)
        // data.freeze
        LOGGER.debug("(currently events are discarded in localtests)")
    }

    /** Publishes event data to all subscribers. */
    override publish(EventData data) {
        LOGGER.debug("async event {} published via LocalAsyncProcessor", data.data.ret$PQON)
        // data.freeze
        LOGGER.debug("(currently events are discarded in localtests)")
    }

    override registerSubscriber(String eventID, IEventHandler subscriber) {
        registerSubscriber(eventID, T9tConstants.GLOBAL_TENANT_REF42, subscriber);
    }

    override registerSubscriber(String eventID, Long tenantRef, IEventHandler subscriber) {
        LOGGER.debug("subscriber (not) registered: {} for {} of tenant {}", subscriber.class.canonicalName, eventID, tenantRef)
    }

}
