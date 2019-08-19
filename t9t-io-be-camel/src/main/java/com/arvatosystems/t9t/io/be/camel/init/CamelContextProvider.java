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
package com.arvatosystems.t9t.io.be.camel.init;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.be.camel.service.CamelDataSinkChangeListener;
import com.arvatosystems.t9t.io.be.camel.service.CamelService;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;
import com.arvatosystems.t9t.out.be.impl.output.camel.AbstractExtensionCamelRouteBuilder;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;
import de.jpaw.util.ExceptionUtil;

/**
 *
 * @author LUEC034
 */
@Startup(95000)
@Singleton
public class CamelContextProvider implements StartupShutdown, Provider<CamelContext> {

    private CamelContext camelContext = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextProvider.class);

    protected final IOutPersistenceAccess iOutPersistenceAccess = Jdp.getRequired(IOutPersistenceAccess.class);
    protected final CamelService camelService = Jdp.getRequired(CamelService.class);
    protected final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    @Override
    public void onStartup() {
        camelContext = new DefaultCamelContext();
        Jdp.registerWithCustomProvider(CamelContext.class, this);
        // first Initialize routes that derive from
        // AbstractExtensionCamelRouteBuilder
        // these should be static routes that are not configurable (like
        // FileRoute)
        List<AbstractExtensionCamelRouteBuilder> classList = Jdp.getAll(AbstractExtensionCamelRouteBuilder.class);
        if (classList != null) {
            for (AbstractExtensionCamelRouteBuilder clazz : classList) {
                try {
                    LOGGER.info("Adding route: {}", clazz.getClass());
                    camelContext.addRoutes(clazz);
                } catch (Exception e) {
                    // in case of problems rather skip a single route instead of not initializing the context at all!
                    LOGGER.debug("There was a problem initializing route: {} due to ", clazz.getClass(), e);
                }
            }
        } else {
            LOGGER.info("No AbstractExtensionCamelRouteBuilders found.");
        }
        // After initializing these static routes there are additional routes
        try {
            String environment = ConfigProvider.getConfiguration().getImportEnvironment();
            if (environment == null) {
                environment = CamelService.DEFAULT_ENVIRONMENT;
            }
            List<DataSinkDTO> dataSinkDTOList = iOutPersistenceAccess.getDataSinkDTOsForEnvironment(environment);
            LOGGER.info("Looking for Camel import routes for environment {}: {} routes found", environment, dataSinkDTOList.size());
            for (DataSinkDTO dataSinkDTO : dataSinkDTOList) {
                if (dataSinkDTO.getIsActive()) {
                    LOGGER.info("Starting Camel route {}", dataSinkDTO.getDataSinkId());
                    try {
                        camelService.addRoutes(dataSinkDTO);
                    } catch (Exception e) {
                        LOGGER.error("Could not add Camel route for {} due to {}", dataSinkDTO.getDataSinkId(), ExceptionUtil.causeChain(e));
                    }
                } else {
                    LOGGER.info("Not starting inactive Camel route {}", dataSinkDTO.getDataSinkId());
                }
            }
            camelContext.start();

        } catch (Exception e) {
            LOGGER.error("CamelContext could not be started... ", e);
        }
        // Register listener to receive data sink changes
        asyncProcessor.registerSubscriber(DataSinkChangedEvent.BClass.INSTANCE.getPqon(), Jdp.getRequired(CamelDataSinkChangeListener.class, "IOCamelDataSinkChange"));
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
            } catch (Exception e) {
                LOGGER.error("Exception while shutting down Camel context:", e);
                // unfortunately we cannot do anything about it, but the system is shutting down anyway at this point
            }
        }
    }
}
