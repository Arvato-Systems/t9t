/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.stubs;

import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.ITimeZoneProvider;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

/**
 * Provides a time zone.
 *
 * This implementation ignores the tenant and returns a global time zone.
 * It is used in configurations which do not have access to a data base.
 */
@Fallback
@Singleton
public class NoTimeZoneProvider implements ITimeZoneProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoTimeZoneProvider.class);

    private final ZoneId serverTimeZone;

    private ZoneId getServerTimeZone() {
        final ServerConfiguration serverConfig = ConfigProvider.getConfiguration().getServerConfiguration();
        if (serverConfig == null || serverConfig.getTimeZone() == null) {
            LOGGER.info("No default server application time zone has been configured - using UTC");
            return ZoneOffset.UTC;
        } else {
            try {
                LOGGER.info("Setting server time zone to {}", serverConfig.getTimeZone());
                return ZoneId.of(serverConfig.getTimeZone());
            } catch (final Exception e) {
                LOGGER.error("Could not process time zone, falling back to UTC: {} {}", e.getClass().getSimpleName(), e.getMessage());
                return ZoneOffset.UTC;
            }
        }
    }

    public NoTimeZoneProvider() {
        serverTimeZone = getServerTimeZone();
    }

    @Override
    public ZoneId getTimeZoneOfTenant(final Long tenantRef) {
        return serverTimeZone;
    }
}
