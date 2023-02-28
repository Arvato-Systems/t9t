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
package com.arvatosystems.t9t.ipblocker.services.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ipblocker.services.IIPAddressBlocker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ConfigurationReaderFactory;

@Singleton
public class IPAddressBlocker implements IIPAddressBlocker {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPAddressBlocker.class);

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.restapi", null);

    public static boolean checkIfSet(final String configurationNameName, Boolean defaultValue) {
        final Boolean configuredValue = CONFIG_READER.getBooleanProperty(configurationNameName);
        if (configuredValue == null) {
            LOGGER.info("No value configured for {}, using default {}", configurationNameName, defaultValue);
            return defaultValue;
        } else {
            LOGGER.info("Configuration of {} is {}", configurationNameName, configuredValue);
            return configuredValue;
        }
    }

    private static final int MAX_AUTH_ENTRIES = 200;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES                       = 1000;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES   = 10;
    private static final int DEFAULT_MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES    = 10;
    private static final int DEFAULT_BAD_AUTHS_PER_IP_ADDRESS_LIMIT             = 50;

    private static final int MAX_BAD_IP_ADDRESSES
      = CONFIG_READER.getIntProperty("t9t.restapi.maxBadIp",              DEFAULT_MAX_BAD_IP_ADDRESSES);
    private static final int MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES
      = CONFIG_READER.getIntProperty("t9t.restapi.badAuthsPerIpDuration", DEFAULT_MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES);
    private static final int MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES
      = CONFIG_READER.getIntProperty("t9t.restapi.badIpLockoutDuration",  DEFAULT_MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES);
    private static final int BAD_AUTHS_PER_IP_ADDRESS_LIMIT
      = CONFIG_READER.getIntProperty("t9t.restapi.badAuthsPerIpLimit",    DEFAULT_BAD_AUTHS_PER_IP_ADDRESS_LIMIT);

    protected final Cache<String, Boolean> goodAuths = Caffeine.newBuilder().maximumSize(MAX_AUTH_ENTRIES).expireAfterWrite(5L, TimeUnit.MINUTES)
      .<String, Boolean>build();
    protected final Cache<String, AtomicInteger> badAuthsPerIp;
    protected final Cache<String, Boolean> blockedIps;

    public IPAddressBlocker() {
        if (MAX_BAD_IP_ADDRESSES <= 0) {
            badAuthsPerIp = null;
            blockedIps = null;
            LOGGER.info("Bad IP address check DISABLED because t9t.restapi.maxBadIp <= 0");
        } else {
            LOGGER.info("Bad IP address check configuration: {} max IP addresses, {} bad attempts per {} minutes disable an IP address for {} minutes",
                MAX_BAD_IP_ADDRESSES, BAD_AUTHS_PER_IP_ADDRESS_LIMIT, MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES, MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES);
            badAuthsPerIp = Caffeine.newBuilder()
                .maximumSize(MAX_BAD_IP_ADDRESSES)
                .expireAfterWrite(MAX_BAD_IP_ADDRESSES_INTERVAL_IN_MINUTES, TimeUnit.MINUTES)
                .<String, AtomicInteger>build();
            blockedIps = Caffeine.newBuilder()
                .maximumSize(MAX_BAD_IP_ADDRESSES)
                .expireAfterWrite(MAX_BAD_IP_ADDRESSES_LOCKOUT_IN_MINUTES, TimeUnit.MINUTES)
                .<String, Boolean>build();
        }
    }

    /** Checks if the request came from a blocked IP address. */
    @Override
    public boolean isIpAddressBlocked(final String remoteIp) {
        if (blockedIps == null || remoteIp == null) {
            return false;
        }
        if (blockedIps.getIfPresent(remoteIp) != null) {
            return true;
        }
        return false;
    }

    /** Records a failed authentication event. */
    @Override
    public void registerBadAuthFromIp(final String remoteIp) {
        if (badAuthsPerIp == null || remoteIp == null) {
            return;
        }
        final AtomicInteger counter = badAuthsPerIp.get(remoteIp, unused -> new AtomicInteger());
        final int newValue = counter.incrementAndGet();
        if (newValue >= BAD_AUTHS_PER_IP_ADDRESS_LIMIT) {
            // block this IP and reset the counter
            blockedIps.put(remoteIp, Boolean.TRUE);
            badAuthsPerIp.invalidate(remoteIp);
            LOGGER.warn("Too many bad authentication attempts from {} - temporarily blocking IP", remoteIp);
        }
    }
}
