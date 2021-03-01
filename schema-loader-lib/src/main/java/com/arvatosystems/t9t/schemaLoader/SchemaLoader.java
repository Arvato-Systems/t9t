/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.schemaLoader;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration;

public class SchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaLoader.class);

    private final SchemaLoaderConfiguration configuration;
    private final MigrationElements migrationElements;
    private final SchemaElements schemaElements;

    public SchemaLoader(SchemaLoaderConfiguration configuration) {
        this.configuration = configuration;
        this.schemaElements = new SchemaElements(configuration);
        this.migrationElements = new MigrationElements(configuration, schemaElements);

        showHeader();
        scanForResources();
    }

    private void showHeader() {
        LOGGER.info("==============================================================================================");
        LOGGER.info("                                    Schema Loader");
        LOGGER.info("----------------------------------------------------------------------------------------------");
        LOGGER.info("           Action: {}", configuration.getAction());
        LOGGER.info("         Database: {}", configuration.getDatabase());
        LOGGER.info("              URL: {}", configuration.getSelectedDatabaseConfiguration()
                                                          .getUrl());
        LOGGER.info("             User: {}", configuration.getSelectedDatabaseConfiguration()
                                                          .getUsername());
        LOGGER.info("  Script Location: {}", configuration.getScriptLocation());
        LOGGER.info("        Log Table: {}", configuration.getMigrationLogTable());
        LOGGER.info("==============================================================================================");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(configuration.toString());
        }
    }

    /**
     * Scan for SQL resources at provided script location. If the script location is not a classpath URL, add the given URL to classpath
     * to allow alway handling by classloader.
     */
    private void scanForResources() {
        final ClassLoader classLoader;
        final String path;
        if (startsWithIgnoreCase(configuration.getScriptLocation(), "classpath:")) {
            classLoader = configuration.getClassloader();
            path = trimToNull(removeStartIgnoreCase(configuration.getScriptLocation(), "classpath:"));
            LOGGER.debug("Scanning for SQL scripts in classpath {}", path);
        } else {
            try {
                path = null;
                LOGGER.debug("Scanning for SQL scripts in URL {}", configuration.getScriptLocation());
                classLoader = new URLClassLoader(new URL[] { new URL(configuration.getScriptLocation()) }, configuration.getClassloader());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid script path", e);
            }
        }
        configuration.setClassloader(classLoader);

        final Reflections reflections = new Reflections(path, new ResourcesScanner(), classLoader);
        final Pattern sqlPathPattern = Pattern.compile(join("^.*?/",
                                                            configuration.getSelectedDatabaseConfiguration()
                                                                         .getType(),
                                                            "/(?<dbElementType>.+?)/.+\\.sql$"),
                                                       Pattern.CASE_INSENSITIVE);
        final Pattern sqlResourcePattern = Pattern.compile(".*\\.sql",
                                                           Pattern.CASE_INSENSITIVE);

        configuration.getInstall()
                     .getSqlScriptPathsByType()
                     .putAll(reflections.getResources(sqlResourcePattern)
                                        .stream()
                                        .map(resource -> sqlPathPattern.matcher(resource))
                                        .filter(Matcher::matches)
                                        .collect(groupingBy(matcher -> matcher.group("dbElementType"),
                                                            mapping(matcher -> matcher.group(),
                                                                    toSet()))));

        configuration.getMigration()
                     .getMigrationScriptPaths()
                     .addAll(configuration.getInstall()
                                          .getSqlScriptPathsByType("Migration")
                                          .stream()
                                          .map(resource -> join("classpath:/",
                                                                substringBeforeLast(resource,
                                                                                    "/")))
                                          .collect(toSet()));

        if (LOGGER.isDebugEnabled()) {
            for (Entry<String, Set<String>> sqlScriptPathsByTypeEntry : configuration.getInstall()
                                                                                     .getSqlScriptPathsByType()
                                                                                     .entrySet()) {
                for (String scriptPath : sqlScriptPathsByTypeEntry.getValue()) {
                    LOGGER.debug("Found script for type {}: {}",
                                 sqlScriptPathsByTypeEntry.getKey(),
                                 scriptPath);
                }
            }
        }

        configuration.getInstall()
                     .getSqlScriptPathsByType()
                     .remove("Migration");
    }

    public void executeAction() {
        switch (configuration.getAction()) {
            case INFO: {
                info();
                break;
            }

            case INSTALL: {
                install();
                break;
            }

            case MIGRATE: {
                migrate();
                break;
            }

            case REPAIR: {
                repair();
                break;
            }

            case BASELINE: {
                baseline();
                break;
            }
        }
    }

    public void baseline() {
        migrationElements.baseline();
        migrationElements.info();
    }

    public void info() {
        migrationElements.info();
    }

    public void install() {
        migrationElements.clearMigrationLog();
        schemaElements.install();

        if (!isEmpty(configuration.getInstall()
                                  .getBaselineVersion())) {
            migrationElements.baseline();
            migrationElements.info();
        }
    }

    public void migrate() {
        migrationElements.migrate();
        migrationElements.info();
    }

    public void repair() {
        migrationElements.repair();
        migrationElements.info();
    }
}
