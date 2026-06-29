/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rest.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.QuerySystemSettingRequest;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.SystemSetting;

/**
 * Endpoint retrieving specific system settings.
 */
@Path("systemsetting")
@Tag(name = "t9t")
@Singleton
public class SystemSettingRestResource implements IT9tRestEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemSettingRestResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Retrieve system settings",
        description = "A successful GET provides the value of approved system parameters.",
        responses = {
            @ApiResponse(responseCode = "200",
              description = "Request passed.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SystemSetting.class))),
            @ApiResponse(responseCode = "default", description = "In case of error, a generic response with the error code is returned.",
              content = @Content(schema = @Schema(implementation = GenericResult.class))) })
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void retrieveSystemSetting(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, @Parameter(required = true, description = "Variable name.") @QueryParam("key") final String key) {
        checkNotNull(key, "key");
        final QuerySystemSettingRequest rq = new QuerySystemSettingRequest();
        rq.setSettingName(key);
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, rq, "GET /systemsetting?key=" + key, ServiceResponse.class, (unusedResponse) -> handleSystemSettingResponse(key));  // backend call is only used as auth gate - denied requests are caught
    }

    private SystemSetting handleSystemSettingResponse(final String key) {
        final String value = RestUtils.CONFIG_READER.getProperty(key, null);
        if (value == null) {
            LOGGER.debug("No value set for requested system setting '{}'", key);
            return new SystemSetting("(unset)");
        }
        LOGGER.debug("Providing system setting for '{}' as value '{}'", key, value);
        return new SystemSetting(value);
    }
}
