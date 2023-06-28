/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.jetty.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.jetty.ISwaggerInit;
import com.arvatosystems.t9t.jetty.init.JettyServer;
import com.arvatosystems.t9t.jetty.oas.DateTimeConverters;
import com.arvatosystems.t9t.jetty.oas.JsonSchemaOpenApiUtil;

import de.jpaw.dp.Singleton;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
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
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.core.Application;

@Singleton
public class SwaggerInit implements ISwaggerInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerInit.class);

    @Override
    public void configureOpenApi(final Application application, final ServletConfig servletConfig, final Set<String> allPackages) {
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
        LOGGER.info("Adding custom swagger converter for ByteArray fields.");
        ModelConverters.getInstance().addConverter(JsonSchemaOpenApiUtil.getByteArrayModelConverter());

        try {
            new JaxrsOpenApiContextBuilder()
              .servletConfig(servletConfig)
              .application(application)
              .openApiConfiguration(oasConfig)
              .buildContext(true);
        } catch (final OpenApiConfigurationException e) {
             throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected OpenAPI createConfiguredOpenApi(final Info info) {
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

    protected Info createRestApiInfoForSwagger() {
        return new Info()
             .title("REST API")
             // omit version info for security reasons - specialized implementations might decide to add this at their own risk
             // .version(this.getClass().getPackage().getSpecificationVersion() + "." + this.getClass().getPackage().getImplementationVersion())
             .description("This is a REST API documentation for the API gateway.")
             .contact(new Contact()
                 .name("Sales Arvato Systems")
                 .email("sales@arvato-systems.de"));
    }
}
