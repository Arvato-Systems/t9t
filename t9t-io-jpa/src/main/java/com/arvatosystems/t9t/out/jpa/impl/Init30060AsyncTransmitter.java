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
package com.arvatosystems.t9t.out.jpa.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;
import de.jpaw.util.ExceptionUtil;

// start and stop the async transmitter service at the desired point in time

@Startup(30060)
public class Init30060AsyncTransmitter implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init30060AsyncTransmitter.class);
    private IAsyncQueue transmitter;

    @Override
    public void onStartup() {
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final String transmitterQualifier = cfg.getAsyncMsgConfiguration() == null ? "noop" : cfg.getAsyncMsgConfiguration().getStrategy();
        LOGGER.info("Selected async transmitter of qualifier {} by configuration", transmitterQualifier);
        try {
            Jdp.bindByQualifierWithFallback(IAsyncQueue.class, transmitterQualifier);
        } catch (Exception e) {
            LOGGER.error("Cannot bind: {}", ExceptionUtil.causeChain(e));
            // invalid qualifier: fall back to "noop"
            Jdp.bindByQualifierWithFallback(IAsyncQueue.class, "noop");
        }
        LOGGER.info("Starting async transmitter...");
        transmitter = Jdp.getRequired(IAsyncQueue.class);
        transmitter.open();
    }

    @Override
    public void onShutdown() {
        LOGGER.info("Shutting down async transmitter...");
        transmitter.close();
    }
}
