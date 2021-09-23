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
package com.arvatosystems.t9t.jetty.exceptions;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.jetty.impl.ResponseFactory;
import com.arvatosystems.t9t.xml.GenericResult;

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
    public Response toResponse(Exception e) {
        LOGGER.error("Exception occurred in application calling {}: {}", authContext.getUri().getAbsolutePath(), e.getMessage(), e);
        GenericResult genericResult = new GenericResult();
        genericResult.setErrorDetails(String.format("   %s -> %s", authContext.getUri().getPath(), (e.getMessage() != null ? e.getMessage() : e.toString())));
        genericResult.setErrorMessage(T9tException.codeToString(T9tException.GENERAL_EXCEPTION));
        genericResult.setReturnCode(T9tException.GENERAL_EXCEPTION);
        final String acceptHeader = httpHeaders.getHeaderString("Accept");
        return ResponseFactory.create(Response.Status.INTERNAL_SERVER_ERROR, genericResult, acceptHeader);
    }
}
