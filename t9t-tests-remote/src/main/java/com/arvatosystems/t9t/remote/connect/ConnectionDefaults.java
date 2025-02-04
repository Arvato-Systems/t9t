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
package com.arvatosystems.t9t.remote.connect;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.types.SessionParameters;

import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ConnectionDefaults {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionDefaults.class);

    public static final String DEFAULT_SCHEMA       = "http";
    public static final String DEFAULT_HOST         = "localhost";      // override by system property HOST or environment variable
    public static final String DEFAULT_PORT         = "8024";           // override by system property PORT or environment variable
    public static final String DEFAULT_PORT_TCP     = "8023";           // override by system property PORT or environment variable

    public static final String INITIAL_TENANT_ID    = "@";
    public static final String INITIAL_USER_ID      = "admin";
    public static final String INITIAL_PASSWORD     = "changeMe";
    public static final String DEFAULT_REST_URL     = "http:8080//localhost";
    public static final String DEFAULT_REST_VERIFY  = "Y";

    public static final SessionParameters SESSION_PARAMETERS = new SessionParameters(
        null, // tenantId
        "en-US", // locale, BCP 47 language tag (ISO630 language code)
        "Europe/Berlin", // zoneinfo, IANA tz identifier
        System.getProperty("user.name"),    // dataUri, optional identifier of the local host
        "t9t-local-tests"    // userAgent, any string to identify the client
    );

    protected static final String PROPERTY_HOST;
    protected static final String PROPERTY_PORT;
    protected static final String PROPERTY_PORTTCP;
    protected static final String PROPERTY_PASSWORD;
    protected static final String PROPERTY_REST_URL;
    protected static final String PROPERTY_REST_VERIFY;

    static {
        // static class initialization
        SESSION_PARAMETERS.freeze();
        BonaPortableFactory.addToPackagePrefixMap("t9t", "com.arvatosystems.t9t");

        // read any properties file
        final Properties props = new Properties();
        final String propFileName = System.getProperty("user.home") + System.getProperty("t9t.propfile.relpath", "") + "/t9taccess.properties";
        try {
            props.load(new FileInputStream(propFileName));
        } catch (Exception e) {
            LOGGER.error("Could not read properties file {}, using hardcoded defaults. Cause: {}:{}", propFileName, ExceptionUtil.causeChain(e));
        }
        PROPERTY_HOST        = (String)props.get("t9t.host");
        PROPERTY_PORT        = (String)props.get("t9t.port");
        PROPERTY_PORTTCP     = (String)props.get("t9t.port.tcp");
        PROPERTY_PASSWORD    = (String)props.get("t9t.password");
        PROPERTY_REST_URL    = (String)props.get("rest.url");
        PROPERTY_REST_VERIFY = (String)props.get("t9t.restapi.verify");
    }

    /**
     * Returns a configuration value, read from system property (highest prio), environment variable (second prio),
     * some property file, or a hardcoded last resort fallback.
     */
    public static String getConfigFromVariousSources(@Nullable final String systemProperty, @Nullable final String environmentProperty,
      @Nullable final String propFileValue, @Nullable final String fallback) {
        final String sysPropValue = System.getProperty(systemProperty);
        if (sysPropValue != null) {
            return sysPropValue;
        }
        final String environmentValue = System.getenv(environmentProperty);
        if (environmentValue != null) {
            return environmentValue;
        }
        if (propFileValue != null) {
            return propFileValue;
        }
        return fallback;
    }

    /**
     * Returns a configuration value, read from system property (highest prio), environment variable (second prio), or a hardcoded last resort fallback.
     * In this case, the name of the environment variable is constructed from the property name.
     */
    public static String getConfigFromVariousSources(@Nonnull final String systemProperty, @Nullable final String fallback) {
        final String environmentName = systemProperty.toUpperCase().replace('.', '_');
        return getConfigFromVariousSources(systemProperty, environmentName, null, fallback);
    }

    /** Constructs the property prefix for the given endpoint, for environments with multiple server types / gateways. */
    public static String getPropertyPrefix(final boolean isRest, @Nonnull final String endpoint) {
        final String type = isRest ? "gateway." : "server.";
        return type + endpoint + ".";
    }

    public static String getUrl(final boolean isRest, @Nonnull final String endpoint, final int defaultPort) {
        final String prefix = getPropertyPrefix(isRest, endpoint);
        final String host = getConfigFromVariousSources(prefix + "host", DEFAULT_HOST);
        final String port = getConfigFromVariousSources(prefix + "port", Integer.toString(defaultPort));
        final String schema = getConfigFromVariousSources(prefix + "schema", DEFAULT_SCHEMA);
        return schema + "://" + host + ":" + port;
    }

    protected static String getInitialPassword() {
        return getConfigFromVariousSources("t9t.password",          "PASSWORD",     PROPERTY_PASSWORD,    INITIAL_PASSWORD);
    }
    protected static String getInitialHost() {
        return getConfigFromVariousSources("t9t.host",              "HOST",         PROPERTY_HOST,        DEFAULT_HOST);
    }
    protected static String getInitialPort() {
        return getConfigFromVariousSources("t9t.port",              "PORT",         PROPERTY_PORT,        DEFAULT_PORT);
    }
    protected static String getInitialPortTcp() {
        return getConfigFromVariousSources("t9t.port.tcp",          "PORTTCP",      PROPERTY_PORTTCP,     DEFAULT_PORT_TCP);
    }
    protected static String getInitialRestUrl() {
        return getConfigFromVariousSources("rest.url",              "RESTURL",      PROPERTY_REST_URL,    DEFAULT_REST_URL);
    }

    /** Checkstyle wants at least one instance method or complains about constructors. */
    public void justToAvoidCheckStyleError() {
    }
}
