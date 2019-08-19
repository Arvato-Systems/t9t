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
package com.arvatosystems.t9t.remote.connect

import com.arvatosystems.t9t.base.types.SessionParameters
import de.jpaw.bonaparte.core.BonaPortableFactory
import java.util.Properties
import java.io.FileInputStream

class ConnectionDefaults {
    public static final String DEFAULT_HOST         = "localhost";      // override by system property HOST or environment variable
    public static final String DEFAULT_PORT         = "8024";           // override by system property PORT or environment variable
    public static final String DEFAULT_PORT_TCP     = "8023";           // override by system property PORT or environment variable

    public static final String INITIAL_TENANT_ID    = "@";
    public static final String INITIAL_USER_ID      = "admin";
    public static final String INITIAL_PASSWORD     = "changeMe";
    public static final String DEFAULT_REST_URL     = "http:8080//localhost";

    static public final SessionParameters SESSION_PARAMETERS = new SessionParameters => [
        dataUri                 = System.getProperty("user.name")   // optional identifier of the local host
        locale                  = "en-US"                           // BCP 47 language tag (ISO630 language code)
        userAgent               = "t9t-local-tests"                 // any string to identify the client
        zoneinfo                = "Europe/Berlin"                   // IANA tz identifier
        validate
        freeze
    ]

    def protected static getInitialPassword() {
        return System.getProperty("t9t.password") ?: System.getenv("PASSWORD") ?: property_password ?: INITIAL_PASSWORD
    }
    def protected static getInitialHost() {
        return System.getProperty("t9t.host") ?: System.getProperty("HOST") ?: property_host ?: System.getenv("HOST") ?: DEFAULT_HOST
    }
    def protected static getInitialPort() {
        return System.getProperty("t9t.port") ?: System.getProperty("PORT") ?: property_port ?: System.getenv("PORT") ?: DEFAULT_PORT
    }
    def protected static getInitialPortTcp() {
        return System.getProperty("t9t.port.tcp") ?: System.getProperty("PORTTCP") ?: System.getenv("PORTTCP") ?: property_portTcp ?: DEFAULT_PORT_TCP
    }
    def protected static getInitialRestUrl() {
        return System.getProperty("rest.url") ?: property_rest_url ?: System.getenv("RESTURL") ?: DEFAULT_REST_URL
    }


    protected static final String DUMMY_INITIALIZER   = BonaPortableFactory.addToPackagePrefixMap("t9t", "com.arvatosystems.t9t");

    protected static String property_host     = null;
    protected static String property_port     = null;
    protected static String property_portTcp  = null;
    protected static String property_password = null;
    protected static String property_rest_url = null;

    protected static final String DUMMY_INITIALIZER2 = {
        val props = new Properties
        val propFileName = System.getProperty("user.home") + System.getProperty("t9t.propfile.relpath", "") + "/t9taccess.properties"
        try {
            props.load(new FileInputStream(propFileName))
            property_host       = props.get("t9t.host") as String
            property_port       = props.get("t9t.port") as String
            property_portTcp    = props.get("t9t.port.tcp") as String
            property_password   = props.get("t9t.password") as String
            property_rest_url   = props.get("rest.url") as String
        } catch (Exception e) {
            println("Could not read properties file " + propFileName + ", using hardcoded defaults: " + e.class.simpleName + ": " + e.message)
        }
        null
    }
}
