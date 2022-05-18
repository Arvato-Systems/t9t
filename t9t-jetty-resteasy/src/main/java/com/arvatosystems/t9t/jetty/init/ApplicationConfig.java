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
package com.arvatosystems.t9t.jetty.init;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.jdp.Init;
import com.arvatosystems.t9t.jetty.exceptions.ApplicationExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.GeneralExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.RestExceptionHandler;
import com.arvatosystems.t9t.jetty.exceptions.T9tExceptionHandler;
import com.arvatosystems.t9t.jetty.impl.RestUtils;
import com.arvatosystems.t9t.jetty.oas.DateTimeConverters;
import com.arvatosystems.t9t.jetty.oas.JsonSchemaOpenApiUtil;
import com.arvatosystems.t9t.jetty.rest.endpoints.StaticResourcesResource;
import com.arvatosystems.t9t.jetty.xml.XmlMediaTypeDecoder;
import com.arvatosystems.t9t.jetty.xml.XmlMediaTypeEncoder;
import com.arvatosystems.t9t.rest.converters.JaxrsParamConverterProvider;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;

import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.dp.Jdp;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

public class ApplicationConfig extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    private final Set<Object> allSingletons;         // we use singletons for REST endpoints and the media en/decoder
    private final Set<Class<?>> allClasses;          // anything else

    public ApplicationConfig(@Context final ServletConfig servletConfig) {
        LOGGER.info("t9t servlet context initialization START");
        MessagingUtil.initializeBonaparteParsers();
        BonaPortableFactory.useFixedClassLoader(null);
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
                LOGGER.info ("    Path {} implemented by {}", pathAnnotation.value(), cls.getCanonicalName());
                // add the package to the set of all packages
                allPackages.add(cls.getPackageName());
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
        allClasses.add(JaxrsParamConverterProvider.class);

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
            configureOpenApi(servletConfig, allPackages);
        } else {
            LOGGER.info("Swagger REST API documentation endpoints NOT enabled");
        }
    }

    private void configureOpenApi(final ServletConfig servletConfig, final Set<String> allPackages) {
        final Info info = createRestApiInfoForSwagger();
        final OpenAPI oas = createConfiguredOpenApi(info);
        final SwaggerConfiguration oasConfig = new SwaggerConfiguration()
          .openAPI(oas)
          .prettyPrint(true)
          .resourcePackages(allPackages);
        LOGGER.info("Adding custom swagger converter for Java 8 LocalDate/Time types will be displayed as string, Instant as integer in swaggerUI");
        ModelConverters.getInstance().addConverter(new DateTimeConverters());
        LOGGER.info("Adding custom swagger converter for display z field as Json schema.");
        ModelConverters.getInstance().addConverter(JsonSchemaOpenApiUtil.getJsonModelConverter());
        try {
            new JaxrsOpenApiContextBuilder()
              .servletConfig(servletConfig)
              .application(this)
              .openApiConfiguration(oasConfig)
              .buildContext(true);
        } catch (final OpenApiConfigurationException e) {
             throw new RuntimeException(e.getMessage(), e);
        }
    }

    private OpenAPI createConfiguredOpenApi(final Info info) {
        final OpenAPI oas = new OpenAPI();
        oas.info(info);
        oas.addSecurityItem(new SecurityRequirement().addList("apiKey"));
        oas.schemaRequirement("apiKey", new SecurityScheme()
          .type(Type.APIKEY)
          .in(In.HEADER)
          .name("Authorization")
          .description("Use API-Key to authorize access to application - most endpoints are secured"));

        final String basePath = JettyServer.getContextPath() + JettyServer.getApplicationPath();
        final String basePathKey = "basePath";
        final ServerVariables variables = new ServerVariables();
        final ServerVariable variable = new ServerVariable();
        variable.setDefault(basePath);
        variables.addServerVariable(basePathKey, variable);
        oas.addServersItem(new Server().url("{" + basePathKey + "}").description("Base Path").variables(variables));
        JsonSchemaOpenApiUtil.addJsonSchema(oas);
        return oas;
    }

    private Info createRestApiInfoForSwagger() {
        return new Info()
             .title("REST API")
             .version(this.getClass().getPackage().getSpecificationVersion() + "." + this.getClass().getPackage().getImplementationVersion())
             .description("This is a REST API documentation for the API gateway.")
             .contact(new Contact()
                 .name("Sales Arvato Systems")
                 .email("sales@arvato-systems.de"));
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
