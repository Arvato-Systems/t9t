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
package com.arvatosystems.t9t.base.be.lifecycle;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;

import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

/**
 * Framework initialization class.
 * Set DNS TTL to configured value.
 */
@Startup(37)
public class Init00037InitDnsTtl implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init00037InitDnsTtl.class);

    @Override
    public void onStartup() {
        final ApplicationConfiguration applCfg = ConfigProvider.getConfiguration().getApplicationConfiguration();
        if (applCfg != null && applCfg.getDnsTtl() != null) {
            try {
                final Integer dnsTtl = applCfg.getDnsTtl();
                // see https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-jvm-ttl.html
                java.security.Security.setProperty("networkaddress.cache.ttl", dnsTtl.toString());
                LOGGER.info("Set TTL for DNS lookups to {}", dnsTtl);
            } catch (final Exception e) {
                LOGGER.error("Attempt to set TTL for DNS lookups to {} FAILED: {}", applCfg.getDnsTtl(), e);
            }
        }
    }
}
