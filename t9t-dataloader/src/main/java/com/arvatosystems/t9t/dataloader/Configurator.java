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
package com.arvatosystems.t9t.dataloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton which holds current configuration.
 * <p>
 * Prior to first use, {@link #createInstance(String, String ,String, String , String)} must be called in order to read the config-file.
 * </p>
 */
public class Configurator {

    private static final Logger logger = LoggerFactory.getLogger(Configurator.class);

    /**
     * Singleton instance
     */
    private static Configurator myself = null;

    /**
     * Configuration
     */
    private Properties prop = null;

    /**
     * dbIdentifier
     */
    private String dbIdentifier = null;

    /**
     * Get the singleton instance of this class
     *
     * @return the Configurator object, may be null, if instance is not initialized, i.e. createInstance was never called
     * @see #createInstance(String, String, String ,String, String)
     */
    private static Configurator getInstance() {
        if (myself == null) {
            logger.error("Configurator is not initialized. Call createInstance(configFile) first!!");
        }
        return myself;
    }

    /**
     * Create an instance of Configurator and initialize it.
     *
     * @param dbUrl JDBC Url
     * @param dbUser Database username
     * @param dbPassword Database password
     * @param dbDriver JDBC driver
     * @return the newly created Object
     * @throws IOException if config file is not loadable
     */
    public static Configurator createInstance(String dbUrl, String dbUser, String dbPassword, String dbDriver, String dbIdentifier) throws IOException {
        synchronized (Configurator.class) {

            myself = new Configurator();
            myself.prop = new Properties();
            myself.prop.put(dbIdentifier + ".uri", dbUrl);
            myself.prop.put(dbIdentifier + ".drivername", dbDriver);
            myself.prop.put(dbIdentifier + ".connectionProperties", "user=" + dbUser + ";password=" + dbPassword);

            myself.dbIdentifier = dbIdentifier;

        }
        return myself;
    }

    /**
     *
     * private constructor
     */
    private Configurator() {
    }

    public static String getDbIdentifier() {
        return getInstance().dbIdentifier;
    }

    /**
     * @see #getValue(String, String)
     * @param key Name of the property
     * @return value of property, otherwise null
     */
    public static String getValue(String key) {
        return getValue(key, null);
    }

    /**
     * Get value for given property. If the property does not exist defaultValue will be returned.
     *
     * @param key Name of the property
     * @param defaultValue default value, will be returned if the key does not exist
     * @return Value as String
     */
    public static String getValue(String key, String defaultValue) {
        try {
            String s = getInstance().prop.getProperty(key);
            if (s == null) {
                return defaultValue;
            } else {
                return s;
            }
        } catch (MissingResourceException mre) {
            logger.debug("Property [{}] not defined in Properties {}", key, getInstance().prop);
            return defaultValue;
        }
    }

    /**
     * Gets the value for the given key, splits in by ';' and splits the resulting values by '='. Note: It is not possible to have keys or values with ';' or
     * '=' as normal characters.
     *
     * @param key the key string
     * @param defaultValue a defaultValue
     * @return the newly constructed Properties or the defaultValue, if the key does not exist
     */
    public static Properties getAsProperties(String key, Properties defaultValue) {
        String s = getValue(key, null);

        if (s != null) {
            String[] entries = s.split(";");
            if (entries.length > 0) {
                Properties p = new Properties();
                for (String entry : entries) {
                    String[] kv = entry.split("=", 2);
                    if (kv.length > 0) {
                        if (kv.length == 1) {
                            p.setProperty(kv[0], "");
                        } else {
                            p.setProperty(kv[0], kv[1]);
                        }
                    }
                }
                return p;
            }
        }
        return defaultValue;
    }

    public static String getMavenVersion(Class<?> clazz) {
        Properties p = new Properties();
        try {
            p.load(clazz.getResourceAsStream("/application.properties"));
            return p.getProperty("version", "@VERSION_NOT_FOUND@");
        } catch (IOException e) {
            return "@ERR:" + e.getMessage() + "@";
        }
    }
}
