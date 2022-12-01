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
package com.arvatosystems.t9t.rest.utils;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.xml.GenericResult;
import com.fasterxml.jackson.core.JacksonException;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ConfigurationReaderFactory;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.xml.bind.JAXBException;
import jakarta.ws.rs.core.UriInfo;


/**
 * Creates a HTTP Response with status and Payload
 */
public final class RestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

    private RestUtils() { }

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.restapi", null);

    public static boolean checkIfSet(final String configurationNameName, Boolean defaultValue) {
        final Boolean configuredValue = CONFIG_READER.getBooleanProperty(configurationNameName);
        if (configuredValue == null) {
            LOGGER.info("No value configured for {}, using default {}", configurationNameName, defaultValue);
            return defaultValue;
        } else {
            LOGGER.info("Configuration of {} is {}", configurationNameName, configuredValue);
            return configuredValue;
        }
    }

    public static String getConfigValue(final String configurationNameName) {
        return CONFIG_READER.getProperty(configurationNameName);
    }

    public static Response create(final Response.Status status, final Object payload, final String acceptHeader) {
        final Response.ResponseBuilder response = Response.status(status);
        response.entity(payload);

        if (payload instanceof String) {
            response.type(MediaType.TEXT_PLAIN_TYPE);
        } else {
            if (acceptHeader != null) {
                response.type(acceptHeader);
            } else {
                response.type(MediaType.APPLICATION_XML);
            }
        }
        return response.build();
    }

    /**
     * Produces a result message for the specific reason of bad input data size.
     *
     * @param parameter   the parameter list
     */
    public static GenericResult generateBadListSizeError(final List<?> parameter) {
        return createErrorResult(T9tException.REST_BAD_LIST_SIZE, parameter == null ? "List was null" : "Size " + parameter.size());
    }

    public static GenericResult createErrorResult(final int returnCode, final String errorDetails) {
        final GenericResult result = new GenericResult();
        result.setErrorDetails(errorDetails);
        result.setErrorMessage(T9tException.codeToString(returnCode));
        result.setReturnCode(returnCode);
        result.setProcessRef(0L);
        return result;
    }

    public static Response error(final Response.Status status, final int errorCode, final String message, final HttpHeaders httpHeaders) {
        final String acceptHeader = httpHeaders == null ? null : httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        final GenericResult genericResult = createErrorResult(errorCode, message);
        return create(status, genericResult, acceptHeader);
    }

    public static Response createExceptionResponse(final Exception e, final HttpHeaders httpHeaders, final UriInfo uriInfo, final String method) {
        if (e instanceof ApplicationException) {
            final ApplicationException ae = (ApplicationException)e;
            final int errorCode = ae.getErrorCode();
            LOGGER.error("Application exception calling {} {}: {} {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), errorCode, e.getMessage());
            return error(mapStatusFromErrorCode(errorCode), errorCode, ae.getMessage(), httpHeaders);
        }
        if (e instanceof JacksonException) {
            // a problem parsing the JSON should be communicated as such
            LOGGER.error("JSON exception calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage());
            return error(Response.Status.BAD_REQUEST, MessageParserException.JSON_EXCEPTION, e.getMessage(), httpHeaders);
        }
        if (e instanceof JAXBException || e instanceof XMLStreamException) {
            // a problem parsing the JSON should be communicated as such
            LOGGER.error("XML exception calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage());
            return error(Response.Status.BAD_REQUEST, T9tException.XML_EXCEPTION, e.getMessage(), httpHeaders);
        }
        if (e instanceof WebApplicationException) {
            final WebApplicationException we = (WebApplicationException)e;
            final StatusType status = we.getResponse().getStatusInfo();
            LOGGER.warn("WebApplicationException calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), status.getStatusCode(), status.getReasonPhrase());
            return we.getResponse();
        }
        LOGGER.error("General exception occurred calling {} {}: {} {} {}",
                method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage(), e);
        return error(Response.Status.INTERNAL_SERVER_ERROR, T9tException.GENERAL_EXCEPTION, null, httpHeaders);  // do not disclose internal message
    }

    /** Creates a specific HTTP return code from t9t exception code / return code. */
    public static Response.Status mapStatusFromErrorCode(final int errorCode) {
        // first, check for very specific codes
        switch (errorCode) {
        case T9tException.NOT_AUTHORIZED:
            return Response.Status.UNAUTHORIZED;
        case T9tException.NOT_AUTHENTICATED:
            return Response.Status.UNAUTHORIZED;
        case T9tException.ACCESS_DENIED:
            return Response.Status.UNAUTHORIZED;
        case T9tException.USER_INACTIVE:
            return Response.Status.UNAUTHORIZED;
        case T9tException.USER_NOT_FOUND:
            return Response.Status.UNAUTHORIZED;
        case T9tException.NOT_YET_IMPLEMENTED:
            return Response.Status.NOT_IMPLEMENTED;
        default:
            switch (errorCode / ApplicationException.CLASSIFICATION_FACTOR) {
            case ApplicationException.CL_SUCCESS:
                return Response.Status.OK;
            case ApplicationException.CL_DENIED:
                return Response.Status.NOT_ACCEPTABLE;
            case ApplicationException.CL_PARSER_ERROR:
                return Response.Status.BAD_REQUEST;
            case ApplicationException.CL_PARAMETER_ERROR:
                return Response.Status.BAD_REQUEST;
            case ApplicationException.CL_TIMEOUT:
                return Response.Status.GATEWAY_TIMEOUT;
            default:
                return Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
    }
}
