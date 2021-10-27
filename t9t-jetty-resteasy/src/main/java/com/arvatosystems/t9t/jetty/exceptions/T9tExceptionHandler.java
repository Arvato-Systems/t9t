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
import com.arvatosystems.t9t.jetty.impl.RestUtils;
import com.arvatosystems.t9t.xml.GenericResult;

/**
 * Handles T9tExceptions and transforms them into a GenericResult.
 */
@Provider
public class T9tExceptionHandler implements ExceptionMapper<T9tException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tExceptionHandler.class);

    @Context
    private HttpRequest authContext;

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public Response toResponse(final T9tException exception) {
        LOGGER.error("There has been an error calling {} with method {}. Exception {}",
          authContext.getUri().getAbsolutePath(), authContext.getHttpMethod(), exception);
        final GenericResult genericResult = new GenericResult();
        genericResult.setErrorDetails(exception.getMessage());
        genericResult.setErrorMessage(exception.getStandardDescription());
        genericResult.setReturnCode(exception.getErrorCode());
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        return RestUtils.create(Response.Status.BAD_REQUEST, genericResult, acceptHeader);
    }
}
