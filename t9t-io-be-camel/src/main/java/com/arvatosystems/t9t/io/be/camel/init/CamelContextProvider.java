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
package com.arvatosystems.t9t.io.be.camel.init;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.io.be.camel.service.CamelDataSinkChangeListener;
import com.arvatosystems.t9t.io.be.camel.service.ICamelService;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;

/**
 * Create routes for all camel routes.
 */
@Startup(95002)
@Singleton
public class CamelContextProvider implements StartupShutdown, Provider<CamelContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextProvider.class);

    @IsLogicallyFinal  // set by startup
    private CamelContext camelContext = null;

    protected final ICamelService camelService = Jdp.getRequired(ICamelService.class);
    protected final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    @Override
    public void onStartup() {
        camelService.initBeforeContextCreation();
        camelContext = new DefaultCamelContext();
        camelService.initAfterContextCreation(camelContext);
        Jdp.registerWithCustomProvider(CamelContext.class, this);

        camelContext.setAutoStartup(false); // to allow manually start
        camelContext.start();

        camelService.initializeClusterService(camelContext);
        camelService.initializeRoutes(camelContext);

        // Register listener to receive data sink changes
        asyncProcessor.registerSubscriber(DataSinkChangedEvent.BClass.INSTANCE.getPqon(),
          Jdp.getRequired(CamelDataSinkChangeListener.class, "IOCamelDataSinkChange"));
    }

    @Override
    public CamelContext get() {
        return camelContext;
    }


    @Override
    public void onShutdown() {
        if (camelContext != null) {
            LOGGER.info("Shutting down Camel context");
            try {
                camelContext.stop();
                LOGGER.info("Camel context successfully shut down");
            } catch (final Exception e) {
                LOGGER.error("Exception while shutting down Camel context:", e);
                // unfortunately we cannot do anything about it, but the system is shutting down anyway at this point
            }
        }
    }
}
