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
package com.arvatosystems.t9t.io.be.camel.service.impl;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.RouteController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.be.camel.service.ICamelService;
import com.arvatosystems.t9t.out.be.impl.output.camel.AbstractExtensionCamelRouteBuilder;
import com.arvatosystems.t9t.out.be.impl.output.camel.GenericT9tRoute;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;
import com.jcraft.jsch.JSch;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

/**
 * Central service to manage camel routes.
 */
@Singleton
public class CamelService implements ICamelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelService.class);

    public static final String DEFAULT_ENVIRONMENT = "t9t";

    protected final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    protected final IOutPersistenceAccess outPersistenceAccess = Jdp.getRequired(IOutPersistenceAccess.class);

    private enum ActionType {
        ADD_ROUTES, REMOVE_ROUTES, START_ROUTE
    }

    @Override
    public void addRoutes(final DataSinkDTO dataSink) {
        final String serverEnvironment = T9tUtil.nvl(ConfigProvider.getConfiguration().getImportEnvironment(), DEFAULT_ENVIRONMENT);
        final String dataSinkEnvironment = dataSink.getEnvironment();

        if (!serverEnvironment.equals(dataSinkEnvironment)) {
            LOGGER.debug("Route with dataSinkID {} not configured, since route is bound to environment {}, but current environment is {}",
                          dataSink.getDataSinkId(), dataSinkEnvironment, serverEnvironment);
            return;
        }

        if (isCamelDataSink(dataSink)) {
            modifyRoutesSynched(dataSink, ActionType.ADD_ROUTES);
        }
    }

    @Override
    public void removeRoutes(final DataSinkDTO dataSink) {
        LOGGER.debug("Try to stop and remove all routes for data sink id {}", dataSink.getDataSinkId());
        modifyRoutesSynched(dataSink, ActionType.REMOVE_ROUTES);
    }

    /**
     * Does a short validation of important fields like camelRoute, InputFormatConverter.
     * @param dataSink
     * @return True if the DataSink contains a camelRoute, commFormatName, preTransformerName and baseClassPqon. Otherwise false!
     *
     */
    private boolean isCamelDataSink(final DataSinkDTO dataSink) {
        if (dataSink.getCamelRoute() == null || dataSink.getCamelRoute().isEmpty()) {
            LOGGER.debug("dataSink {} does not contain a camelRoute field - not setting up any route.", dataSink.getDataSinkId());
            return false;
        }
        return true;
    }

    @Override
    public void startRoute(final DataSinkDTO dataSink) {
        modifyRoutesSynched(dataSink, ActionType.START_ROUTE);
    }

    /**
     * Modify a camel route (synchronized).
     */
    private synchronized void modifyRoutesSynched(final DataSinkDTO dataSink, final ActionType action) {
        try {
            final CamelContext context = Jdp.getProvider(CamelContext.class).get();
            final List<String> routeIds = GenericT9tRoute.getPossibleRouteIds(dataSink);
            final RouteController routeController = context.getRouteController();

            switch (action) {
            case ADD_ROUTES:
                context.addRoutes(new GenericT9tRoute(dataSink, fileUtil));
                break;
            case REMOVE_ROUTES:
                for (final String routeId : routeIds) {
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

                for (final String routeId : routeIds) {
                    if (context.getRoute(routeId) != null) {
                        LOGGER.debug("Remove route id {}", routeId);
                        if (!context.removeRoute(routeId)) {
                            LOGGER.error("Route {} could not be removed!", routeId);
                        }
                    }
                }

                final DefaultCamelContext defaultContext = (DefaultCamelContext) context;
                for (final String routeId : routeIds) {
                    final RouteDefinition routeDefinition = defaultContext.getRouteDefinition(routeId);
                    if (routeDefinition != null) {
                        LOGGER.debug("Remove route definition {}", routeId);
                        defaultContext.removeRouteDefinition(routeDefinition);
                    }
                }
                break;
            case START_ROUTE:
                for (final String routeId : routeIds) {
                    if (context.getRoute(routeId) != null) {
                        if (routeController.getRouteStatus(routeId) == ServiceStatus.Stopped) {
                            LOGGER.debug("Start route id {}", routeId);
                            try {
                                routeController.startRoute(routeId);
                            } catch (Exception e) {
                                LOGGER.error("Exception on starting route {}: {}: {}", routeId, e.getMessage(), ExceptionUtil.causeChain(e));
                            }
                        }
                    }
                }
                break;
            }
        } catch (final Exception e) {
            LOGGER.error("Exception removing camel route: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initBeforeContextCreation() {
        try {
            if (ConfigProvider.getCustomParameter("camelEnableInsecureSha1") != null) {
                // may be needed in case of very old sftp servers
                LOGGER.warn("Due to config camelEnableInsecureSha1 in config.xml, enable INSECURE SHA1");
                JSch.setConfig("server_host_key",  JSch.getConfig("server_host_key") + ",ssh-rsa");
                JSch.setConfig("PubkeyAcceptedAlgorithms", JSch.getConfig("PubkeyAcceptedAlgorithms") + ",ssh-rsa");
                JSch.setConfig("kex", JSch.getConfig("kex") + ",diffie-hellman-group1-sha1,diffie-hellman-group14-sha1");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set insecure sftp algorithms");
        }
    }

    @Override
    public void initializeClusterService(final CamelContext camelContext) {
        // empty by default
    }

    @Override
    public void initializeRoutes(final CamelContext camelContext) {
        try {
            // first Initialize routes that derive from AbstractExtensionCamelRouteBuilder
            // these should be static routes that are not configurable (like FileRoute)
            final List<AbstractExtensionCamelRouteBuilder> classList = Jdp.getAll(AbstractExtensionCamelRouteBuilder.class);
            if (classList != null) {
                for (final AbstractExtensionCamelRouteBuilder clazz : classList) {
                    try {
                        LOGGER.info("Adding route: {}", clazz.getClass());
                        camelContext.addRoutes(clazz);
                    } catch (final Exception e) {
                        // in case of problems rather skip a single route instead of not initializing the context at all!
                        LOGGER.debug("There was a problem initializing route: {} due to ", clazz.getClass(), e);
                    }
                }
            } else {
                LOGGER.info("No AbstractExtensionCamelRouteBuilders found.");
            }
            // After initializing these static routes there are additional routes

            String environment = ConfigProvider.getConfiguration().getImportEnvironment();
            if (environment == null) {
                environment = CamelService.DEFAULT_ENVIRONMENT;
            }
            final List<DataSinkDTO> dataSinkDTOList
              = outPersistenceAccess.getDataSinkDTOsForEnvironmentAndChannel(environment, CommunicationTargetChannelType.FILE);
            LOGGER.info("Looking for Camel import routes for environment {}: {} routes found", environment, dataSinkDTOList.size());
            for (final DataSinkDTO dataSinkDTO : dataSinkDTOList) {
                if (dataSinkDTO.getIsActive()) {
                    LOGGER.info("Starting Camel route {}", dataSinkDTO.getDataSinkId());
                    try {
                        addRoutes(dataSinkDTO);
                    } catch (final Exception e) {
                        LOGGER.error("Could not add Camel route for {} due to {}", dataSinkDTO.getDataSinkId(), ExceptionUtil.causeChain(e));
                    }
                } else {
                    LOGGER.info("Not starting inactive Camel route {}", dataSinkDTO.getDataSinkId());
                }
            }

            // start the routes manually to omit invalid routes
            for (Route route : camelContext.getRoutes()) {
                try {
                    camelContext.getRouteController().startRoute(route.getRouteId());
                    LOGGER.debug("started route {} successfully.", route.getRouteId());
                } catch (Exception ex) {
                    LOGGER.error("Unable to start route {}: ", route.getRouteId(), ex);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("CamelContext could not be started... ", e);
        }
    }
}
