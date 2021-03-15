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
package com.arvatosystems.t9t.io.be.camel.service;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.RouteController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.be.impl.output.camel.GenericT9tRoute;

import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Central service to manage camel routes.
 *
 * @author TWEL006
 */
@Singleton
public class CamelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelService.class);

    public static final String DEFAULT_ENVIRONMENT = "t9t";

    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    public void addRoutes(DataSinkDTO dataSink) {
        try {
            final String serverEnvironment = firstNonNull(ConfigProvider.getConfiguration()
                                                                        .getImportEnvironment(),
                                                          DEFAULT_ENVIRONMENT);
            final String dataSinkEnvironment = dataSink.getEnvironment();

            if (!serverEnvironment.equals(dataSinkEnvironment)) {
                LOGGER.debug("Route with dataSinkID {} not configured, since route is bound to environment {}, but current environment is {}",
                             dataSink.getDataSinkId(), dataSinkEnvironment, serverEnvironment);
                return;
            }

            if (validateT9TCamelComponent(dataSink)) {
                Jdp.getProvider(CamelContext.class)
                   .get()
                   .addRoutes(new GenericT9tRoute(dataSink, fileUtil));
            } else {
                LOGGER.error("There have been problems while configuring route with dataSinkID: {} ", dataSink.getDataSinkId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeRoutes(DataSinkDTO dataSink) {
        try {
            final CamelContext context = Jdp.getProvider(CamelContext.class)
                                            .get();

            LOGGER.debug("Try to stop and remove all routes for data sink id {}", dataSink.getDataSinkId());
            final List<String> routeIds = GenericT9tRoute.getPossibleRouteIds(dataSink);
            final RouteController routeController = context.getRouteController();

            for (String routeId : routeIds) {
                if (context.getRoute(routeId) != null) {
                    if (routeController.getRouteStatus(routeId) == ServiceStatus.Started
                     || routeController.getRouteStatus(routeId) == ServiceStatus.Starting
                     || routeController.getRouteStatus(routeId) == ServiceStatus.Suspended
                     || routeController.getRouteStatus(routeId) == ServiceStatus.Suspending) {
                        LOGGER.debug("Stop route id {}", routeId);
                        routeController.stopRoute(routeId);
                    }
                }
            }

            for (String routeId : routeIds) {
                if (context.getRoute(routeId) != null) {
                    LOGGER.debug("Remove route id {}", routeId);
                    if (!context.removeRoute(routeId)) {
                        LOGGER.error("Route {} could not be removed!", routeId);
                    }
                }
            }

            final ModelCamelContext mcc = context.adapt(ModelCamelContext.class);
            for (String routeId : routeIds) {
                final RouteDefinition routeDefinition = mcc.getRouteDefinition(routeId);
                if (routeDefinition != null) {
                    LOGGER.debug("Remove route definition {}", routeId);
                    mcc.removeRouteDefinition(routeDefinition);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does a short validation of important fields like camelRoute, InputFormatConverter.
     * @param dataSink
     * @return True if the DataSink contains a camelRoute, commFormatName, preTransformerName and baseClassPqon. Otherwise false!
     *
     */
    private boolean validateT9TCamelComponent(DataSinkDTO dataSink) {
        if (dataSink.getCamelRoute() == null || dataSink.getCamelRoute().isEmpty()) {
            LOGGER.error("dataSink {} does not contain a camelRoute field.", dataSink.getDataSinkId());
            return false;
        }
        if (dataSink.getCommFormatType().getBaseEnum() == MediaType.USER_DEFINED && (dataSink.getCommFormatName() == null || dataSink.getCommFormatName().isEmpty())) {
            LOGGER.error("dataSink {} does not contain a commFormatName for an InputFormatConverter.", dataSink.getDataSinkId());
            return false;
        }
        return true;
    }

}
