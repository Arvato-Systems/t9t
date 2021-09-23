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
package com.arvatosystems.t9t.jetty.init;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.client.init.AbstractConfigurationProvider;
import com.arvatosystems.t9t.client.init.JndiConfigurationProvider;
import com.arvatosystems.t9t.client.init.SystemConfigurationProvider;
import com.arvatosystems.t9t.jaxrs.xml.XmlMediaTypeDecoder;
import com.arvatosystems.t9t.jaxrs.xml.XmlMediaTypeEncoder;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.jetty.exceptions.ApplicationExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.GeneralExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.RestExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.T9tExceptionHandler;
import com.arvatosystems.t9t.jetty.impl.GenericResultFactory;
import com.arvatosystems.t9t.rest.converters.JavaTimeParamConverterProvider;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;

import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.dp.Jdp;

@ApplicationPath("/rest/api") // set the path to REST web services
public class ApplicationConfig extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    private final Set<Object> allSingletons;         // we use singletons for REST endpoints and the media en/decoder
    private final Set<Class<?>> allClasses;          // anything else

    public ApplicationConfig() {
        LOGGER.info("t9t servlet context initialization START");
        MessagingUtil.initializeBonaparteParsers();
        BonaPortableFactory.useFixedClassLoader(null);
        Init.initializeT9t();
        LOGGER.info("t9t servlet context initialization COMPLETE");

        // get remote connection config BEFORE we call a connection
        try {
            LOGGER.info("Initializing remote - trying JNDI");
            Jdp.bindInstanceTo(new JndiConfigurationProvider(), AbstractConfigurationProvider.class);
        } catch (final Exception e) {
            LOGGER.error("Error initializing via JNDI: {}: {}, fallback using system",
                    e.getClass().getSimpleName(),
                    e.getMessage());
            Jdp.bindInstanceTo(new SystemConfigurationProvider(), AbstractConfigurationProvider.class);
        }
        // now we can set up the connection
        GenericResultFactory.initializeConnection(Jdp.getRequired(IRemoteConnection.class));
        LOGGER.info("Backend connection initialization COMPLETE");

        // determine (and instantiate) all endpoints.
        allSingletons = new HashSet<Object>(Jdp.getAll(IT9tRestEndpoint.class));

        LOGGER.info("Found {} endpoints:", allSingletons.size());
        for (final Object instance : allSingletons) {
            final Class<?> cls = instance.getClass();
            final Path pathAnnotation = cls.getAnnotation(Path.class);
            if (pathAnnotation == null) {
                LOGGER.error("    NO PATH ANNOTATION SPECIFIED for endpoint {}", cls.getCanonicalName());
            } else {
                LOGGER.info ("    Path {} implemented by {}", pathAnnotation.value(), cls.getCanonicalName());
            }
        }

        allSingletons.add(Jdp.getRequired(XmlMediaTypeDecoder.class));  // new + register singleton
        allSingletons.add(Jdp.getRequired(XmlMediaTypeEncoder.class));  // new + register singleton

        // determine all ExceptionMapper
        allClasses = new HashSet<>(12);
        allClasses.add(T9tExceptionHandler.class);
        allClasses.add(ApplicationExceptionHandler.class);
        allClasses.add(RestExceptionHandler.class);
        allClasses.add(GeneralExceptionHandler.class);
        allClasses.add(StandaloneObjectMapper.class);
        allClasses.add(JavaTimeParamConverterProvider.class);

        if (GenericResultFactory.checkIfSet("t9t.restapi.servletLoggingFilter", "T9T_RESTAPI_SERVLET_LOGGING_FILTER")) {
            // add a custom logging filter to protocol all requests and responses
            allClasses.add(CustomLoggingFilter.class);
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
