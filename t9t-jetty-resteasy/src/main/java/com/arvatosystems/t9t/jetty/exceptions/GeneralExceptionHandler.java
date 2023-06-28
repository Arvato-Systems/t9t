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
package com.arvatosystems.t9t.jetty.exceptions;

import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.rest.utils.RestUtils;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionHandler implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralExceptionHandler.class);

    @Context
    private HttpRequest authContext;

    @Context
    private HttpHeaders httpHeaders;

    // constructor solely used for debugging / logging - temporarily
    public  GeneralExceptionHandler() {
        LOGGER.debug("Creating new instance");
    }

    @Override
    public Response toResponse(final Exception e) {
        final String acceptHeader = RestUtils.determineResponseType(httpHeaders);
        return RestUtils.createExceptionResponse(e, acceptHeader, authContext.getUri(), authContext.getHttpMethod());
    }
}
