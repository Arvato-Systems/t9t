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
package com.arvatosystems.t9t.jetty.init;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.jetty.ISwaggerInit;
import com.arvatosystems.t9t.jetty.exceptions.GeneralExceptionHandler;
import com.arvatosystems.t9t.jetty.rest.endpoints.StaticResourcesResource;
import com.arvatosystems.t9t.rest.converters.JakartarsParamConverterProvider;
import com.arvatosystems.t9t.rest.filters.CustomLoggingFilter;
import com.arvatosystems.t9t.rest.filters.T9tRestAuthenticationFilter;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.utils.JacksonObjectMapperProvider;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.rest.xml.FormUrlEncodedMessageBodyWriter;
import com.arvatosystems.t9t.rest.xml.XmlMediaTypeDecoder;
import com.arvatosystems.t9t.rest.xml.XmlMediaTypeEncoder;
import com.google.common.base.Splitter;

import de.jpaw.dp.Jdp;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

public class ApplicationConfig extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    private final Set<Object> allSingletons; // we use singletons for REST endpoints and the media en/decoder
    private final Set<Class<?>> allClasses; // anything else

    public ApplicationConfig(@Context final ServletConfig servletConfig) {
        LOGGER.info("t9t servlet context initialization START");
        MessagingUtil.initializeBonaparteParsers();
        Init.initializeT9t();
        LOGGER.info("t9t servlet context initialization COMPLETE");

        // get remote connection config BEFORE we call a connection
//        Jdp.bindInstanceTo(new SystemConfigurationProvider(), IRemoteDefaultUrlRetriever.class);

        // determine (and instantiate) all endpoints.
        allSingletons = new HashSet<>(Jdp.getAll(IT9tRestEndpoint.class));
        final Set<String> allPackages = new HashSet<>(30);

        LOGGER.info("Found {} endpoints:", allSingletons.size());
        for (final Object instance : allSingletons) {
            final Class<?> cls = instance.getClass();
            final Path pathAnnotation = cls.getAnnotation(Path.class);
            if (pathAnnotation == null) {
                LOGGER.error("    NO PATH ANNOTATION SPECIFIED for endpoint {}", cls.getCanonicalName());
            } else {
                LOGGER.info("    Path {} implemented by {}", pathAnnotation.value(), cls.getCanonicalName());
                // add the package to the set of all packages
                allPackages.add(cls.getPackageName());
            }
        }

        final String corsConfig = RestUtils.CONFIG_READER.getProperty("t9t.restapi.cors", null);
        if (corsConfig != null) {
            LOGGER.info("Enabling CORS for {}", corsConfig);
            final Iterable<String> cors = Splitter.on(",").omitEmptyStrings().split(corsConfig);
            final CorsFilter corsFilter = new CorsFilter();
            for (final String url : cors) {
                corsFilter.getAllowedOrigins().add(url);
            }
            corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
            allSingletons.add(corsFilter);
        }

        allSingletons.add(Jdp.getRequired(XmlMediaTypeDecoder.class)); // new + register singleton
        allSingletons.add(Jdp.getRequired(XmlMediaTypeEncoder.class)); // new + register singleton
        allSingletons.add(Jdp.getRequired(FormUrlEncodedMessageBodyWriter.class)); // new + register singleton

        // determine all ExceptionMapper
        allClasses = new HashSet<>(12);
        allClasses.add(GeneralExceptionHandler.class);
        allClasses.add(JacksonObjectMapperProvider.class);
        allClasses.add(JakartarsParamConverterProvider.class);
        allClasses.add(T9tRestAuthenticationFilter.class);

        // Expose openapi.json via GET request
        allClasses.add(OpenApiResource.class);

        final boolean servletLogging = RestUtils.checkIfSet("t9t.restapi.servletLoggingFilter", Boolean.FALSE);
        if (servletLogging) {
            // add a custom logging filter to protocol all requests and responses
            allClasses.add(CustomLoggingFilter.class);
        }
        final boolean enableSwagger = RestUtils.checkIfSet("t9t.restapi.swagger", Boolean.FALSE);
        if (enableSwagger) {
            LOGGER.info("Enabling Swagger REST API documentation endpoints");
            StaticResourcesResource.setEnableSwagger(true);
            Jdp.getRequired(ISwaggerInit.class).configureOpenApi(this, servletConfig, allPackages);
        } else {
            LOGGER.info("Swagger REST API documentation endpoints NOT enabled");
        }
    }

    @Override
    public Set<Object> getSingletons() {
        LOGGER.info("JAX-RS wants all singletons");
        return allSingletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        LOGGER.info("JAX-RS wants all classes");
        return allClasses;
    }
}
