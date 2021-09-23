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
package com.arvatosystems.t9t.schemaLoader.config;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * Factory for creating configuration objects using spring boots properties configuration factory. The properties can be
 * provided using spring boots relaxed binding as described here:
 * http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-relaxed-binding
 *
 * <ol>
 * <li>Commandline arguments (--xyz=foo)</li>
 * <li>JVM-Properties (-Dxyz=foo)</li>
 * <li>Property file provided with JVM-Property -DpropertyFile=C:/my.properties</li>
 * <li>Property file provided with environment variable</li>
 * <li>Property file schemaLoader.properties in JVM working dir</li>
 * <li>Property file schemaLoader.properties in user home</li>
 * <li>Property file schemaLoader.properties in classpath</li>
 * </ol>
 */
public class ConfigurationFactory {

    private final MutablePropertySources propertySources;

    public ConfigurationFactory(String[] commandlineArgs) {
        try {
            propertySources = new MutablePropertySources();

            // Add classpath file
            final ClassPathResource classpathProperties = new ClassPathResource("schemaLoader.properties");
            if (classpathProperties.exists()) {
                propertySources.addFirst(new ResourcePropertySource("classpath://schemaLoader.properties", classpathProperties));
            }

            // Add properties file from user home
            File file = new File(System.getProperty("user.home"), "/schemaLoader.properties");
            if (file.canRead()) {
                propertySources.addFirst(new ResourcePropertySource(join("file://", file.toString()), new FileSystemResource(file)));
            }

            file = new File(System.getProperty("user.home"), "/schemaLoader.yml");
            if (file.canRead()) {
                propertySources.addFirst(parseYml(file));
            }

            // Add properties file from working dir
            file = new File("./schemaLoader.properties");
            if (file.canRead()) {
                propertySources.addFirst(new ResourcePropertySource(join("file://", file.toString()), new FileSystemResource(file)));
            }

            file = new File("./schemaLoader.yml");
            if (file.canRead()) {
                propertySources.addFirst(parseYml(file));
            }

            // Add properties file given with system property
            if (System.getProperty("propertyFile") != null) {
                file = new File(System.getProperty("propertyFile"));
                if (file.canRead()) {
                    if (endsWithIgnoreCase(file.getName(), ".yml")) {
                        propertySources.addFirst(parseYml(file));
                    } else {
                        propertySources.addFirst(new ResourcePropertySource(file.toString(), new FileSystemResource(file)));
                    }
                }
            }

            // Add environment properties
            propertySources.addFirst(new SystemEnvironmentPropertySource("envirommentVariables", (Map) System.getenv()));

            // Add system properties
            propertySources.addFirst(new PropertiesPropertySource("jvmProperties", System.getProperties()));

            // Add commandline properties
            if (commandlineArgs != null) {
                propertySources.addFirst(new SimpleCommandLinePropertySource("commandlineArgs", commandlineArgs));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initializing configuration", e);
        }
    }

    private static PropertySource<Map<String, Object>> parseYml(File file) {
        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileSystemResource(file));
        factory.afterPropertiesSet();

        return new PropertiesPropertySource(file.toString(), factory.getObject());
    }

    // invoked from Main for SchemaLoaderConfiguration
    public <T> T load(Class<T> configClass) {
        try {
            // Create bean structure using spring boots properties system by binding system properties to bean
            // attributes.
            // Normally spring boot would do this for us, but since we are not using springs DI container we will have
            // to do it manually.
            final PropertiesConfigurationFactory<T> cfgFactory = new PropertiesConfigurationFactory<>(configClass);

            cfgFactory.setPropertySources(propertySources);
            cfgFactory.setExceptionIfInvalid(true);
            cfgFactory.setIgnoreInvalidFields(false);
            cfgFactory.setIgnoreNestedProperties(false);
            cfgFactory.setIgnoreUnknownFields(true);

            return cfgFactory.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Error creating configuration object", e);
        }
    }

}
