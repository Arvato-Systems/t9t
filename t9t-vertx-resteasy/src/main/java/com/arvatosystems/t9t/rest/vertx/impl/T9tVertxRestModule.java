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
package com.arvatosystems.t9t.rest.vertx.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.jboss.resteasy.plugins.server.vertx.VertxRegistry;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.resteasy.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.vertx.IRestModule;
import com.arvatosystems.t9t.rest.converters.JavaTimeParamConverterProvider;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.vertx.xml.XmlMediaTypeDecoder;
import com.arvatosystems.t9t.rest.vertx.xml.XmlMediaTypeEncoder;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

@Singleton
public class T9tVertxRestModule implements IRestModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tVertxRestModule.class);

    protected void addEndpoints(Registry registry) {
        final List<IT9tRestEndpoint> allEndpoints = Jdp.getAll(IT9tRestEndpoint.class);

        LOGGER.info("Found {} endpoints:", allEndpoints.size());
        for (final Object instance : allEndpoints) {
            final Class<?> cls = instance.getClass();
            final Path pathAnnotation = cls.getAnnotation(Path.class);
            if (pathAnnotation == null) {
                LOGGER.error("    NO PATH ANNOTATION SPECIFIED for endpoint {}", cls.getCanonicalName());
            } else {
                LOGGER.info ("    Path {} implemented by {}", pathAnnotation.value(), cls.getCanonicalName());
                registry.addSingletonResource(instance);
            }
        }
    }

    @Override
    public void createRestServer(Vertx vertx, int port) {
        LOGGER.info("Starting http server for REST on port {}", port);

        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();

        final List<Object> providers = new ArrayList<>();
        providers.add(new JavaTimeParamConverterProvider());  // Java 8 date/time support for GET parameters
        providers.add(new JacksonObjectMapper());  // JSON
        providers.add(new XmlMediaTypeDecoder());  // XML decoder
        providers.add(new XmlMediaTypeEncoder());  // XML encoder
        providers.add(new ApplicationExceptionHandler());  // exception / error handler
        deployment.setProviders(providers);

        deployment.start();
        final VertxRegistry registry = deployment.getRegistry();

        // add endpoints after the providers
        addEndpoints(registry);

        final Handler<HttpServerRequest> restHandler = new VertxRequestHandler(vertx, deployment);
        vertx.createHttpServer().requestHandler(restHandler).listen(port);
    }
}