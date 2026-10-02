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
package com.arvatosystems.t9t.rest.vertx.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.rest.converters.JakartarsParamConverterProvider;
import com.arvatosystems.t9t.rest.exception.mapper.GeneralExceptionMapper;
import com.arvatosystems.t9t.rest.exception.mapper.ReaderExceptionMapper;
import com.arvatosystems.t9t.rest.filters.CustomLoggingFilter;
import com.arvatosystems.t9t.rest.filters.T9tRestAuthenticationFilter;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.utils.JacksonObjectMapperProvider;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.rest.xml.XmlMediaTypeDecoder;
import com.arvatosystems.t9t.rest.xml.XmlMediaTypeEncoder;

/**
 * JAX-RS application which provides all providers and endpoint singletons to the {@code SeBootstrap} / {@code VertxEmbeddedServer}.
 * This class is instantiated by {@code SeBootstrap.start(...)} via its no-arg constructor - JAX-RS/JDP injection into this class is
 * NOT supported, therefore it must remain a plain class (no {@code @Singleton}).
 */
@ApplicationPath("/")
public class T9tRestApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestApplication.class);

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getSingletons() {
        // must be a LinkedHashSet: provider / filter execution order follows registration order
        final Set<Object> singletons = new LinkedHashSet<>();
        singletons.add(new JakartarsParamConverterProvider());  // Java 8 date/time support for GET parameters
        singletons.add(new JacksonObjectMapperProvider());  // JSON
        singletons.add(new XmlMediaTypeDecoder());  // XML decoder
        singletons.add(new XmlMediaTypeEncoder());  // XML encoder
        singletons.add(new ReaderExceptionMapper());  // exception mapper for request body
        singletons.add(new GeneralExceptionMapper());  // general exception mapper
        singletons.add(new T9tRestAuthenticationFilter());  // authentication filter
        final boolean servletLogging = RestUtils.checkIfSet("t9t.restapi.servletLoggingFilter", Boolean.FALSE);
        if (servletLogging) {
            // add a custom logging filter to protocol all requests and responses
            singletons.add(new CustomLoggingFilter());
        }

        addEndpoints(singletons);
        return singletons;
    }

    protected void addEndpoints(final Set<Object> singletons) {
        final var allEndpoints = Jdp.getAll(IT9tRestEndpoint.class);

        LOGGER.info("Found {} endpoints:", allEndpoints.size());
        for (final Object instance : allEndpoints) {
            final Class<?> cls = instance.getClass();
            final Path pathAnnotation = cls.getAnnotation(Path.class);
            if (pathAnnotation == null) {
                LOGGER.error("    NO PATH ANNOTATION SPECIFIED for endpoint {}", cls.getCanonicalName());
            } else {
                LOGGER.info("    Path {} implemented by {}", pathAnnotation.value(), cls.getCanonicalName());
                singletons.add(instance);
            }
        }
    }
}
