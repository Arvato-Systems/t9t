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
package com.arvatosystems.t9t.schemaLoader;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.schemaLoader.config.DatabaseConfiguration;
import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration;

public class SchemaElements {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaElements.class);

    public static final Map<String, List<DropDefinition>> DROP_DEFINITIONS = new HashMap<>();

    static {
        defineDropFunction("DEFAULT",       "Sequence",     "create sequence (?<name>.+?) ",                                name -> join("DROP SEQUENCE IF EXISTS ", name));
        defineDropFunction("DEFAULT",       "View",         "create (or replace )?view (?<name>.+?) ",                      name -> join("DROP VIEW IF EXISTS ", name, " CASCADE"));
        defineDropFunction("DEFAULT",       "Table",        "create table (?<name>.+?) ",                                   name -> join("DROP TABLE IF EXISTS ", name, " CASCADE"));
        defineDropFunction("DEFAULT",       "Function",     "create (or replace )?function (?<name>.+?)\\s*\\(",            name -> join("DROP FUNCTION IF EXISTS ", name, "() CASCADE"));
        defineDropFunction("DEFAULT",       "Trigger",      "create (or replace )?trigger (?<name>.+?) ",                   name -> join("DROP TRIGGER IF EXISTS ", name, " CASCADE"));

        defineDropFunction("POSTGRES",      "Sequence",     "create sequence (?<name>.+?) ",                                name -> join("DROP SEQUENCE IF EXISTS ", name));
        defineDropFunction("POSTGRES",      "View",         "create (or replace )?view (?<name>.+?) ",                      name -> join("DROP VIEW IF EXISTS ", name, " CASCADE"));
        defineDropFunction("POSTGRES",      "Table",        "create table (?<name>.+?) ",                                   name -> join("DROP TABLE IF EXISTS ", name, " CASCADE"));
        defineDropFunction("POSTGRES",      "Function",     "create (or replace )?function (?<name>.+?)\\s*\\(",            name -> join("DROP FUNCTION IF EXISTS ", name, "() CASCADE"));
        defineDropFunction("POSTGRES",      "Trigger",      "create trigger (?<name>.+?) ",                                 name -> join("DROP TRIGGER IF EXISTS ", name, " ON ", name.replace("_tr", ""), " CASCADE"));
        defineDropFunction("POSTGRES",      "Trigger",      "create (or replace )?function (?<name>.+?)\\s*\\(",            name -> join("DROP FUNCTION IF EXISTS ", name, "() CASCADE"));

        defineDropFunction("ORACLE",        "Sequence",     "create sequence (?<name>.+?) ",                                name -> join("DROP SEQUENCE ", name));
        defineDropFunction("ORACLE",        "View",         "create (or replace )?view (?<name>.+?) ",                      name -> join("DROP VIEW ", name, " CASCADE"));
        defineDropFunction("ORACLE",        "Table",        "create table (?<name>.+?) ",                                   name -> join("DROP TABLE ", name, " CASCADE CONSTRAINTS"));
        defineDropFunction("ORACLE",        "Function",     "create (or replace )?function (?<name>.+?)\\s*\\(",            name -> join("DROP FUNCTION ", name));
        defineDropFunction("ORACLE",        "Trigger",      "create (or replace )?trigger (?<name>.+?) ",                   name -> join("DROP TRIGGER ", name));
    }

    private final SchemaLoaderConfiguration configuration;

    public SchemaElements(SchemaLoaderConfiguration configuration) {
        this.configuration = configuration;
    }

    public void install() {
        LOGGER.info("Perform schema installation");
        final DatabaseConfiguration dbConfiguration = configuration.getSelectedDatabaseConfiguration();

        try (final Connection connection = DriverManager.getConnection(dbConfiguration.getUrl(), dbConfiguration.getUsername(), dbConfiguration.getPassword())) {

            for (String dropObjectType : configuration.getInstall().getDrop()) {
                executeDrop(connection, dropObjectType);
            }

            for (String createObjectType : configuration.getInstall().getCreate()) {
                executeCreate(connection, createObjectType);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error during schema create/drop", e);
        }
    }

    public void executeCreate(Connection connection, String dbObjectType) {
        LOGGER.info("execute create scripts of db object type {}", dbObjectType);
        final Set<String> sqlResources = configuration.getInstall().getSqlScriptPathsByType(dbObjectType);

        for (String sqlResource : sqlResources) {
            LOGGER.debug("execute create script {}", sqlResource);

            final String sqlScript = loadScript(sqlResource);
            executeSqlScript(connection, sqlScript);
        }
    }


    public void executeDrop(Connection connection, String dbObjectType) {
        LOGGER.info("generate and execute drop scripts of db object type {}", dbObjectType);
        final Set<String> sqlResources = configuration.getInstall().getSqlScriptPathsByType(dbObjectType);

        List<DropDefinition> dropDefinitions = DROP_DEFINITIONS.get(join(configuration.getSelectedDatabaseConfiguration().getType(), "/", dbObjectType));
        if (dropDefinitions == null) {
            dropDefinitions = DROP_DEFINITIONS.get(join("DEFAULT/", dbObjectType));
        }

        if (dropDefinitions == null || dropDefinitions.isEmpty()) {
            return;
        }

        for (String sqlResource : sqlResources) {
            LOGGER.debug("use create script {} to generated drop statements", sqlResource);
            final String sqlScript = loadScript(sqlResource);

            for (DropDefinition dropDefinition : dropDefinitions) {
                executeDropScript(connection, sqlScript, dropDefinition.pattern, dropDefinition.dropScriptFunction);
            }
        }
    }

    private void executeDropScript(Connection connection, String createScript, Pattern dbObjectNamePattern, Function<String, String> dropScriptCreator) {
        if (StringUtils.isEmpty(createScript)) {
            return;
        }

        final Matcher dbObjectNameMatcher = dbObjectNamePattern.matcher(createScript);

        while (dbObjectNameMatcher.find()) {
            final String dbObjectName = trimToNull(dbObjectNameMatcher.group("name"));

            if (dbObjectName == null) {
                continue;
            }

            LOGGER.debug("drop db element {}", dbObjectName);

            final String dropScript = dropScriptCreator.apply(dbObjectName);

            try (final Statement statement = connection.createStatement()) {
                LOGGER.trace("Execute statement: {}", dropScript);
                statement.execute(dropScript);
            } catch (SQLException e) {
                LOGGER.error("Error executing script: {}\n{}", e.getMessage(), dropScript);
            }
        }
    }

    private void executeSqlScript(Connection connection, String script) {
        if (StringUtils.isEmpty(script)) {
            return;
        }

        try (final Statement statement = connection.createStatement()) {
            LOGGER.trace("Execute script: {}", script);

            statement.execute(script);
        } catch (SQLException e) {
            LOGGER.error("Error executing script: {}\n{}", e.getMessage(), script);
        }
    }



    private static class DropDefinition {
        public Pattern pattern;
        public Function<String, String> dropScriptFunction;

        public DropDefinition(Pattern pattern, Function<String, String> dropScriptFunction) {
            this.pattern = pattern;
            this.dropScriptFunction = dropScriptFunction;
        }
    }

    private static void defineDropFunction(String dbType, String dbElementType, String pattern, Function<String, String> dropScriptFunction) {
        final String key = join(dbType, "/", dbElementType);

        DROP_DEFINITIONS.computeIfAbsent(key, k -> new LinkedList<>()).add(new DropDefinition(toRegex(pattern), dropScriptFunction));
    }

    private static Pattern toRegex(String simple) {
        // Each sequence of whitespace is in pattern is replaces with a regex match \s+ (sequence of at least on whitespace)
        simple = StringUtils.replaceAll(simple, "\\s+", "\\\\s+");

        return Pattern.compile(simple, Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
    }

    private String loadScript(String path) {
        try (InputStream in = configuration.getClassloader().getResourceAsStream(path)) {
            return IOUtils.toString(in, configuration.getScriptEncoding());
        } catch (IOException e) {
            throw new RuntimeException(join("Error reading resource ", path), e );
        }
    }

}
