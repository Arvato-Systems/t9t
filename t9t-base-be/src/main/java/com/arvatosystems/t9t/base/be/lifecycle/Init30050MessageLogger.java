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
package com.arvatosystems.t9t.base.be.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.server.services.IRequestLogger;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;


// start and stop the logger service at the desired point in time

@Startup(30050)
public class Init30050MessageLogger implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init30050MessageLogger.class);
    private IRequestLogger messageLogger;

    @Override
    public void onStartup() {
        LOGGER.info("Starting message logger...");
        messageLogger = Jdp.getRequired(IRequestLogger.class);
        messageLogger.open();
    }

    @Override
    public void onShutdown() {
        LOGGER.info("Shutting down message logger...");
        messageLogger.close();
    }
}
