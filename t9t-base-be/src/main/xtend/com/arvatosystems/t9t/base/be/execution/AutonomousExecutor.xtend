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

import com.arvatosystems.t9t.base.services.IAutonomousExecutor
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.base.api.RequestParameters
import org.slf4j.MDC
import com.arvatosystems.t9t.base.api.ServiceRequestHeader

@AddLogger
@Singleton
class AutonomousExecutor implements IAutonomousExecutor {
    static final int DEFAULT_MAX_AUTONOMOUS_TRANSACTIONS = 4

    protected final ExecutorService executorService
    @Inject IRequestProcessor requestProcessor

    new() {
        val autoPoolSizeByCfg = ConfigProvider.configuration.applicationConfiguration?.autonomousPoolSize
        val autoPoolSize = autoPoolSizeByCfg ?: DEFAULT_MAX_AUTONOMOUS_TRANSACTIONS
        LOGGER.info("Creating a new thread pool for autonomous transactions of size {}", autoPoolSize);

        val counter = new AtomicInteger()
        executorService =  Executors.newFixedThreadPool(autoPoolSize) [
            val threadName = "t9t-autonomous-" + counter.incrementAndGet
            LOGGER.info("Launching thread {} of {} for local autonomous transactions", threadName, autoPoolSize)
            return new Thread(it, threadName)
        ]
    }

    override execute(RequestContext ctx, RequestParameters rp) {
        val requestHeader = new ServiceRequestHeader
        requestHeader.invokingProcessRef = ctx.requestRef  // transfer the invoker
        val mdcContext = MDC.copyOfContextMap
        val f = executorService.submit [
            MDC.setContextMap(mdcContext) // Inherit MDC context and ensure old MDC of this worker is reset
            requestProcessor.execute(requestHeader, rp, ctx.internalHeaderParameters.jwtInfo, ctx.internalHeaderParameters.encodedJwt, true)
        ]
        return f.get
    }
}
