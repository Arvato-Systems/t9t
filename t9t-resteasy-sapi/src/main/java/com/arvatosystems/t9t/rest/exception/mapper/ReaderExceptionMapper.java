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
package com.arvatosystems.t9t.rest.exception.mapper;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.MessageParserException;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.rest.utils.RestUtils;

@Provider
public class ReaderExceptionMapper implements ExceptionMapper<ReaderException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderExceptionMapper.class);

    @Context
    private HttpRequest authContext;

    @Context
    private HttpHeaders httpHeaders;

    // constructor solely used for debugging / logging - temporarily
    public ReaderExceptionMapper() {
        LOGGER.debug("Creating new instance to handle Resteasy ReaderException for request body");
    }

    @Override
    public Response toResponse(final ReaderException e) {
        LOGGER.error("Resteasy request body ReaderException calling {} {}: {} {}", authContext.getHttpMethod(),
                authContext.getUri().getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage());
        final String acceptHeader = RestUtils.determineResponseType(httpHeaders);
        final int errorCode = acceptHeader != null && acceptHeader.equalsIgnoreCase(MediaType.APPLICATION_XML)
                ? T9tException.XML_EXCEPTION : MessageParserException.JSON_EXCEPTION;
        return RestUtils.error(Response.Status.BAD_REQUEST, errorCode, e.getMessage(), acceptHeader);
    }
}
