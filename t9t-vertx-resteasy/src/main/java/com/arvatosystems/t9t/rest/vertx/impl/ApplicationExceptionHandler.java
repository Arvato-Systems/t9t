package com.arvatosystems.t9t.rest.vertx.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.arvatosystems.t9t.base.T9tException;

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
import de.jpaw.util.ApplicationException;

/**
 * Maps all ApplicationException error codes to some HTTP Status Code (this includes T9TExceptions).
 */
@Provider
public class ApplicationExceptionHandler implements ExceptionMapper<ApplicationException> {

    @Override
    public Response toResponse(final ApplicationException exception) {
        final int errorCode = exception.getErrorCode();
        switch (errorCode) {
            case T9tException.NOT_AUTHORIZED:
                return T9tRestProcessor.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.NOT_AUTHENTICATED:
                return T9tRestProcessor.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.ACCESS_DENIED:
                return T9tRestProcessor.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.USER_INACTIVE:
                return T9tRestProcessor.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.USER_NOT_FOUND:
                return T9tRestProcessor.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.CL_PARAMETER_ERROR:
                return T9tRestProcessor.error(Response.Status.BAD_REQUEST, exception.getStandardDescription());
            case T9tException.CL_DENIED:
                return T9tRestProcessor.error(Response.Status.NOT_ACCEPTABLE, exception.getStandardDescription());
            default:
                return T9tRestProcessor.error(Response.Status.BAD_REQUEST, exception.getStandardDescription());
        }
    }
}
