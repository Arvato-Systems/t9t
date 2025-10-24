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
package com.arvatosystems.t9t.io.be.camel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;
import com.arvatosystems.t9t.out.be.impl.output.camel.CamelOutputProcessor;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Event to be executed on other nodes if a data sink change has been performed.
 */
@Singleton
@Named("IOCamelDataSinkChange")
public class CamelDataSinkChangeListener implements IEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelDataSinkChangeListener.class);

    private final ICamelService camelService = Jdp.getRequired(ICamelService.class);

    @SuppressWarnings("incomplete-switch")
    @Override
    public int execute(final RequestContext context, final EventParameters untypedEvent) {
        if (untypedEvent instanceof DataSinkChangedEvent event) {
            final DataSinkDTO dataSink = event.getDataSink();

            switch (event.getOperation()) {
            case INACTIVATE:
            case DELETE: {
                LOGGER.debug("Config of data sink id {} changed ({}) - remove routes", dataSink.getDataSinkId(), event.getOperation());
                remove(dataSink);
                break;
            }

            case ACTIVATE:
            case CREATE: {
                LOGGER.debug("Config of data sink id {} changed ({}) - add routes", dataSink.getDataSinkId(), event.getOperation());
                add(dataSink);
                break;
            }

            case MERGE:
            case PATCH:
            case UPDATE: {
                LOGGER.debug("Config of data sink id {} changed ({}) - update routes", dataSink.getDataSinkId(), event.getOperation());
                remove(dataSink);
                add(dataSink);
                break;
            }
            }
        } else {
            LOGGER.error("Discarding event of wrong type {}", untypedEvent == null ? "(NULL)" : untypedEvent.getClass().getCanonicalName());
        }
        return 0;
    }

    private void remove(final DataSinkDTO dataSink) {
        camelService.removeRoutes(dataSink);
    }

    private void add(final DataSinkDTO dataSink) {
        final boolean isInput = Boolean.TRUE.equals(dataSink.getIsInput());
        if (isInput) {
            // import data route (always using camel)
            camelService.addRoutes(dataSink);
            camelService.startRoute(dataSink); // start manually
        } else {
            // export data sink (may or may not use camel)
            if (dataSink.getCamelRoute() != null) {
                // If an camelRoute (which is loaded from camelEndpoint.properties) exist, also reload this config
                CamelOutputProcessor.flushConfig();
            }
        }
    }
}
