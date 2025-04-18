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
package com.arvatosystems.t9t.zkui.init;

import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.client.init.SystemConfigurationProvider;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.zkui.components.dropdown28.factories.IDropdown28BasicFactory;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28Registry;
import com.arvatosystems.t9t.zkui.converters.grid.AllItemConverters;
import com.arvatosystems.t9t.zkui.converters.grid.IItemConverter;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Initializes Jdp and the remoter.
 */
@Singleton
public class T9tInitializer implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tInitializer.class);

    public T9tInitializer() {
        LOGGER.debug("T9tInitializer CONSTRUCTOR (web.xml)");
        // do it here - contextInitialized is called too late
        Init.initializeT9t();
        Jdp.bindInstanceTo(new SystemConfigurationProvider(), IRemoteDefaultUrlRetriever.class);
        LOGGER.debug("T9tInitializer CONSTRUCTOR (JDP scanning) COMPLETE");
        LOGGER.debug(Jdp.dump());  // list all scanned interfaces / classes


    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOGGER.debug("T9tInitializer contextInitialized()");

        // now initialize all dropdowns
        Map<String, IDropdown28BasicFactory> dropdowns = Jdp.getInstanceMapPerQualifier(IDropdown28BasicFactory.class);
        for (IDropdown28BasicFactory df : dropdowns.values()) {
            LOGGER.debug("Registering dropdown {}", df.getDropdownId());
            Dropdown28Registry.register(df);
        }
        LOGGER.info("Found {} dropdown mappings", dropdowns.size());

        // now initialize all itemConverters
        Map<String, IItemConverter> converters = Jdp.getInstanceMapPerQualifier(IItemConverter.class);
        for (Map.Entry<String, IItemConverter> df : converters.entrySet()) {
            LOGGER.debug("Registering item converter {} for {}", df.getValue().getClass().getSimpleName(), df.getKey());
            AllItemConverters.register(df.getKey(), df.getValue());
        }
        LOGGER.info("Found {} item value converters", converters.size());
        LOGGER.debug("T9tInitializer contextInitialized() ends");
    }
}
