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
package com.arvatosystems.t9t.jetty.impl;

import static de.jpaw.util.ApplicationException.CLASSIFICATION_FACTOR;
import static de.jpaw.util.ApplicationException.CL_DATABASE_ERROR;
import static de.jpaw.util.ApplicationException.CL_DENIED;
import static de.jpaw.util.ApplicationException.CL_INTERNAL_LOGIC_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARAMETER_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARSER_ERROR;
import static de.jpaw.util.ApplicationException.CL_SUCCESS;
import static de.jpaw.util.ApplicationException.CL_TIMEOUT;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.auth.AuthenticationResult;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class T9tRestProcessor implements IT9tRestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestProcessor.class);
    private static final AtomicInteger counter = new AtomicInteger();
    private static IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);

    @Override
    public void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, RequestParameters requestParameters, String infoMsg) {
        performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    @Override
    public <T> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, String infoMsg,
            List<T> inputData, Function<T, RequestParameters> requestConverterSingle,
            Function<List<T>, RequestParameters> requestConverterBatch) {
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);  // must evaluate it now, because httpHeaders is a proxy and no longer valid in the other thread
        if (inputData == null || inputData.isEmpty()) {
            LOGGER.error("{}: No input data provided", infoMsg);
            returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(T9tException.MISSING_PARAMETER, null));  // missing parameter
            return;
        }
        final RequestParameters rq;
        if (inputData.size() == 1) {
            try {
                rq = requestConverterSingle.apply(inputData.get(0));
            } catch (ApplicationException e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(e.getErrorCode(), e.getMessage()));  // missing parameter
                return;
            } catch (Exception e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(T9tException.GENERAL_EXCEPTION, e.getMessage()));  // missing parameter
                return;
            }
        } else {
            if (requestConverterBatch == null) {
                LOGGER.error("{}: More than one record provided ({})", infoMsg, inputData.size());
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(T9tException.TOO_MANY_RECORDS, null));  // too many parameters
                return;
            }
            try {
                rq = requestConverterBatch.apply(inputData);
            } catch (ApplicationException e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(e.getErrorCode(), e.getMessage()));  // missing parameter
                return;
            } catch (Exception e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, createErrorResult(T9tException.GENERAL_EXCEPTION, e.getMessage()));  // missing parameter
                return;
            }
        }
        performAsyncBackendRequest(httpHeaders, resp, rq, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    @Override
    public GenericResult createResultFromServiceResponse(ServiceResponse response) {
        final GenericResult result = new GenericResult();
        result.setErrorDetails(response.getErrorDetails());
        result.setErrorMessage(response.getErrorMessage());
        result.setReturnCode(response.getReturnCode());
        result.setProcessRef(response.getProcessRef());
        return result;
    }

    @Override
    public void returnAsyncResult(final String acceptHeader, AsyncResponse resp, Response.Status status, Object result) {
        final Response.ResponseBuilder response = Response.status(status);
        response.type(acceptHeader == null || acceptHeader.length() == 0 ? "application/json" : acceptHeader);
        response.entity(result);
        final Response responseObj = response.build();
        resp.resume(responseObj);
    }

    private static GenericResult createErrorResult(int returnCode, String errorDetails) {
        final GenericResult result = new GenericResult();
        result.setErrorDetails(errorDetails);
        result.setErrorMessage(T9tException.codeToString(returnCode));
        result.setReturnCode(returnCode);
        result.setProcessRef(0L);
        return result;
    }

    private static Response.ResponseBuilder createResponseBuilder(int returncode) {
        // special case for already transformed http status codes:
        if (returncode >= T9tException.HTTP_ERROR + 100 && returncode <= T9tException.HTTP_ERROR + 599) {  // 100..599 is the allowed range for http status codes
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
        case CL_PARAMETER_ERROR:
            return Response.status(Status.BAD_REQUEST.getStatusCode());     // or 422... no resteasy constant for this one
        case CL_TIMEOUT:
            return Response.status(Status.GATEWAY_TIMEOUT.getStatusCode());
        case CL_INTERNAL_LOGIC_ERROR:
            return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        case CL_DATABASE_ERROR:
            return Response.status(Status.SERVICE_UNAVAILABLE.getStatusCode());
        default:
            return Response.status(Status.BAD_REQUEST.getStatusCode());
        }
    }

    public static Status getHttpReturnCodeOk(final int returncode) {
        switch (returncode / CLASSIFICATION_FACTOR) {
        case CL_SUCCESS:
            return Status.OK;
        case CL_DENIED:
            return Status.NOT_ACCEPTABLE;   // Request was not processed for business reasons.
        case CL_PARSER_ERROR:
            return Status.BAD_REQUEST;
        case CL_PARAMETER_ERROR:
            return Status.BAD_REQUEST;   // TODO: modify to 422
        case CL_TIMEOUT:
            return Status.GATEWAY_TIMEOUT;
        case CL_INTERNAL_LOGIC_ERROR:
            return Status.INTERNAL_SERVER_ERROR;
        case CL_DATABASE_ERROR:
            return Status.SERVICE_UNAVAILABLE;
        default:
            return Status.BAD_REQUEST;
        }
    }

    public static Status getHttpReturnCodeCreated(final int returncode) {
        final Status temp = getHttpReturnCodeOk(returncode);
        return (temp.equals(Status.OK)) ? Status.CREATED : temp;
    }

    /**
     * Method to replace the former ImportResource.
     * It takes a list element of a specific type,
     * and a lambda which produces a ServiceRequest,
     * as well as a response generator.
     **/
    @Override
    public <T extends ServiceResponse> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp,
            RequestParameters requestParameters, String infoMsg, Class<T> backendResponseClass, Function<T, BonaPortable> responseMapper) {
        final int invocationNo = counter.incrementAndGet();
        final String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        LOGGER.debug("Starting {}: {}", invocationNo, infoMsg);

        final CompletableFuture<ServiceResponse> readResponse = connection.executeAsync(authorizationHeader, requestParameters);
        readResponse.thenAccept(sr -> {
            LOGGER.debug("Response obtained {}: {}", invocationNo, infoMsg);
            final Response.ResponseBuilder responseBuilder = createResponseBuilder(sr.getReturnCode());
            responseBuilder.type(acceptHeader == null || acceptHeader.length() == 0 ? MediaType.APPLICATION_JSON : acceptHeader);
            if (ApplicationException.isOk(sr.getReturnCode())) {
                if (backendResponseClass.isAssignableFrom(sr.getClass())) {

                    final BonaPortable resultForREST = responseMapper.apply((T)sr);
                    if (resultForREST instanceof MediaData) {
                        // special handling for MediaData: return a String or byte array, depending on contents
                        final MediaData md = (MediaData)resultForREST;
                        final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(md.getMediaType());
                        if (mtd != null) {
                            final String mimeType = mtd.getMimeType();
                            if (mimeType != null && !mimeType.isEmpty()) {
                                responseBuilder.type(mimeType);
                            }
                        }

                        if (md.getRawData() != null) {
                            responseBuilder.entity(md.getRawData().getBytes());
                        } else {
                            responseBuilder.entity(md.getText());
                        }
                    } else {
                        // any other Bonaportable...
                        responseBuilder.entity(resultForREST);
                    }
                } else {
                    // this is a coding issue!
                    LOGGER.error("Coding bug: expected response of class {} to {}, but got {} (return code {})", backendResponseClass.getSimpleName(),
                            requestParameters.getClass().getSimpleName(), sr.getClass().getSimpleName(), sr.getReturnCode());
                    responseBuilder.entity(createResultFromServiceResponse(sr));  // this is like the error result
                }
            } else {
                // map error report
                responseBuilder.entity(createResultFromServiceResponse(sr));
            }
            final Response responseObj = responseBuilder.build();
            resp.resume(responseObj);
        }).exceptionally(e -> {
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()));
            e.printStackTrace();
            return null;
        });
    }

    @Override
    public void performAsyncAuthBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, AuthenticationRequest requestParameters, Consumer<String> cacheUpdater) {
        final String acceptHeader = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);  // must evaluate it now, because httpHeaders is a proxy and no longer valid in the other thread
        final CompletableFuture<ServiceResponse> readResponse = connection.executeAuthenticationAsync(requestParameters);
        readResponse.thenAccept(sr -> {
            final Response.ResponseBuilder responseBuilder = createResponseBuilder(sr.getReturnCode());
            responseBuilder.type(acceptHeader == null || acceptHeader.length() == 0 ? "application/json" : acceptHeader);
            if (ApplicationException.isOk(sr.getReturnCode())) {
                if (AuthenticationResponse.class.isAssignableFrom(sr.getClass())) {

                    final AuthenticationResponse result = (AuthenticationResponse)sr;
                    if (result.getEncodedJwt() != null) {
                        if (cacheUpdater != null) {
                            cacheUpdater.accept(result.getEncodedJwt());
                        }
                        final AuthenticationResult authResult = new AuthenticationResult();
                        authResult.setJwt(result.getEncodedJwt());
                        returnAsyncResult(acceptHeader, resp, Response.Status.OK, authResult);
                    }
                } else {
                    // this is a coding issue!
                    LOGGER.error("Coding bug: expected response of class AuthenticationResponse, but got {} (return code {})",
                        sr.getClass().getSimpleName(), sr.getReturnCode());
                    responseBuilder.entity(createResultFromServiceResponse(sr));  // this is like the error result
                }
            } else {
                // map error report
                responseBuilder.entity(createResultFromServiceResponse(sr));
            }
            final Response responseObj = responseBuilder.build();
            resp.resume(responseObj);
        }).exceptionally(e -> {
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()));
            e.printStackTrace();
            return null;
        });
    }
}