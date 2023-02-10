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

import static de.jpaw.util.ApplicationException.CLASSIFICATION_FACTOR;
import static de.jpaw.util.ApplicationException.CL_DATABASE_ERROR;
import static de.jpaw.util.ApplicationException.CL_DENIED;
import static de.jpaw.util.ApplicationException.CL_INTERNAL_LOGIC_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARAMETER_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARSER_ERROR;
import static de.jpaw.util.ApplicationException.CL_RESOURCE_EXHAUSTED;
import static de.jpaw.util.ApplicationException.CL_SERVICE_UNAVAILABLE;
import static de.jpaw.util.ApplicationException.CL_SUCCESS;
import static de.jpaw.util.ApplicationException.CL_TIMEOUT;
import static de.jpaw.util.ApplicationException.CL_VALIDATION_ERROR;

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
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.xml.bind.JAXBException;


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
     * Determines which format to use for the response.
     * The type of the response is either the type requested by ACCEPT, or, in case that is null, matches the content type.
     * This is the default implementation, called from exception handlers.
     * The method within the request processors can be overridden.
     *
     * @param httpHeaders   the HTTP headers provided to the REST request
     * @return              the type of response, or null
     */
    public static String determineResponseType(final HttpHeaders httpHeaders) {
        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        return accept != null ? accept : httpHeaders.getHeaderString(HttpHeaders.CONTENT_TYPE);
    }

    /**
     * Produces a result message for the specific reason of bad input data size.
     *
     * @param parameter   the parameter list
     */
    public static GenericResult generateBadListSizeError(final List<?> parameter) {
        return createErrorResult(T9tException.REST_BAD_LIST_SIZE, parameter == null ? "List was null" : "Size " + parameter.size());
    }

    /**
     * Creates a GenericResult, based on error code and error details.
     * Error details will only be provided for certain return codes (parser errors or invalid references),
     * in order to not leak internal implementation details (stack traces).
     *
     * @param returnCode  the t9t return code
     * @param errorDetails  details describing the error
     */
    public static GenericResult createErrorResult(final int returnCode, final String errorDetails) {
        final GenericResult result = new GenericResult();
        result.setProcessRef(0L);

        final int classification = returnCode / ApplicationException.CLASSIFICATION_FACTOR;
        switch (classification) {
        case CL_SUCCESS:
            // in these cases, specifically do not return any of the error fields, it could cause confusion
            result.setErrorDetails(null);
            result.setErrorMessage(null);
            result.setReturnCode(returnCode);  // this one is required because it could be different to 0
            break;
        case CL_DENIED:
        case CL_PARSER_ERROR:
        case CL_PARAMETER_ERROR:
        case CL_VALIDATION_ERROR:
            // in these cases, provide detail, it's a client error and tell the client which field was not ok
            result.setErrorDetails(errorDetails);
            result.setErrorMessage(ApplicationException.codeToString(returnCode));
            result.setReturnCode(returnCode);
            break;
        case CL_TIMEOUT:
        case CL_SERVICE_UNAVAILABLE:
            result.setErrorDetails(null);
            result.setErrorMessage("Timeout");
            result.setReturnCode(returnCode);
            break;
            // for security reasons, do not transmit internal exception details to external callers
        case CL_DATABASE_ERROR:
        case CL_INTERNAL_LOGIC_ERROR:
        case CL_RESOURCE_EXHAUSTED:
        default:
            // for security reasons, do not transmit internal exception details to external callers
            result.setErrorDetails(null);
            result.setErrorMessage(ApplicationException.codeToString(T9tException.GENERAL_SERVER_ERROR));
            result.setReturnCode(T9tException.GENERAL_SERVER_ERROR);
            break;
        }
        return result;
    }

    public static Response error(final Response.Status status, final int errorCode, final String message, final String acceptHeader) {
        final GenericResult genericResult = createErrorResult(errorCode, message);
        return create(status, genericResult, acceptHeader);
    }

    public static Response createExceptionResponse(final Exception e, final String acceptHeader, final UriInfo uriInfo, final String method) {
        if (e instanceof ApplicationException ae) {
            final int errorCode = ae.getErrorCode();
            LOGGER.error("Application exception calling {} {}: {} {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), errorCode, e.getMessage());
            return error(mapStatusFromErrorCode(errorCode), errorCode, ae.getMessage(), acceptHeader);
        }
        if (e instanceof JacksonException) {
            // a problem parsing the JSON should be communicated as such
            LOGGER.error("JSON exception calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage());
            return error(Response.Status.BAD_REQUEST, MessageParserException.JSON_EXCEPTION, e.getMessage(), acceptHeader);
        }
        if (e instanceof JAXBException || e instanceof XMLStreamException) {
            // a problem parsing the JSON should be communicated as such
            LOGGER.error("XML exception calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage());
            return error(Response.Status.BAD_REQUEST, T9tException.XML_EXCEPTION, e.getMessage(), acceptHeader);
        }
        if (e instanceof WebApplicationException we) {
            final StatusType status = we.getResponse().getStatusInfo();
            LOGGER.warn("WebApplicationException calling {} {}: {} {}",
                    method, uriInfo.getAbsolutePath(), status.getStatusCode(), status.getReasonPhrase());
            return we.getResponse();
        }
        LOGGER.error("General exception occurred calling {} {}: {} {} {}",
                method, uriInfo.getAbsolutePath(), e.getClass().getSimpleName(), e.getMessage(), e);
        return error(Response.Status.INTERNAL_SERVER_ERROR, T9tException.GENERAL_EXCEPTION, null, acceptHeader);  // do not disclose internal message
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
            case ApplicationException.CL_VALIDATION_ERROR:
                return Response.Status.BAD_REQUEST;
            case ApplicationException.CL_PARAMETER_ERROR:
                return Response.Status.BAD_REQUEST;
            case ApplicationException.CL_TIMEOUT:
                return Response.Status.GATEWAY_TIMEOUT;
            case ApplicationException.CL_DATABASE_ERROR:
                return Response.Status.SERVICE_UNAVAILABLE;
            default:
                return Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
    }

    public static Response.ResponseBuilder createResponseBuilder(final int returncode) {
        // special case for already transformed http status codes:
        if (returncode >= T9tException.HTTP_ERROR + 100 && returncode <= T9tException.HTTP_ERROR + 599) { // 100..599 is the allowed range for http status codes
            return Response.status(returncode - T9tException.HTTP_ERROR);
        }
        // default: map via classification
        switch (returncode / CLASSIFICATION_FACTOR) {
        case CL_SUCCESS:
            return Response.status(Status.OK.getStatusCode());
        case CL_DENIED:
            return Response.status(Status.NOT_ACCEPTABLE.getStatusCode());  // Request was not processed for business reasons.
        case CL_PARSER_ERROR:
            return Response.status(Status.BAD_REQUEST.getStatusCode());
        case CL_VALIDATION_ERROR:
            return Response.status(Status.BAD_REQUEST.getStatusCode());
        case CL_PARAMETER_ERROR:
            return Response.status(Status.BAD_REQUEST.getStatusCode());     // or 422... no resteasy constant for this one
        case CL_TIMEOUT:
            return Response.status(Status.GATEWAY_TIMEOUT.getStatusCode());
        case CL_INTERNAL_LOGIC_ERROR:
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        case CL_DATABASE_ERROR:
            return Response.status(Status.SERVICE_UNAVAILABLE.getStatusCode());
        default:
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }
}
