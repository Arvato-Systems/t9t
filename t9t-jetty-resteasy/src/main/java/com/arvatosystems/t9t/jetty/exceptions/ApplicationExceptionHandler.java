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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.jetty.impl.ResponseFactory;

import de.jpaw.util.ApplicationException;

/**
 * Maps a T9TException and ApplicationException Error Code to a HTTP Status Code
 * @author LUEC034
 */
@Provider
public class ApplicationExceptionHandler implements ExceptionMapper<ApplicationException> {

    @Override
    public Response toResponse(ApplicationException exception) {
        int errorCode = exception.getErrorCode();
        switch (errorCode) {
            case T9tException.NOT_AUTHORIZED:
                return ResponseFactory.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.NOT_AUTHENTICATED:
                return ResponseFactory.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.ACCESS_DENIED:
                return ResponseFactory.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.USER_INACTIVE:
                return ResponseFactory.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.USER_NOT_FOUND:
                return ResponseFactory.error(Response.Status.UNAUTHORIZED, exception.getStandardDescription());
            case T9tException.CL_PARAMETER_ERROR:
                return ResponseFactory.error(Response.Status.BAD_REQUEST, exception.getStandardDescription());
            case T9tException.CL_DENIED:
                return ResponseFactory.error(Response.Status.NOT_ACCEPTABLE, exception.getStandardDescription());
            default:
                return ResponseFactory.error(Response.Status.BAD_REQUEST, exception.getStandardDescription());
        }

    }

}
