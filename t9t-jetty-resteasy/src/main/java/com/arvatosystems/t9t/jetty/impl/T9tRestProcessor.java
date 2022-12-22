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
package com.arvatosystems.t9t.jetty.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.rest.services.IGatewayStringSanitizerFactory;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.auth.AuthenticationResult;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
public class T9tRestProcessor implements IT9tRestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestProcessor.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();

    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);
    protected final IGatewayStringSanitizerFactory gatewayStringSanitizerFactory = Jdp.getRequired(IGatewayStringSanitizerFactory.class);
    protected final DataConverter<String, AlphanumericElementaryDataItem> stringSanitizer = gatewayStringSanitizerFactory.createStringSanitizerForGateway();

    @Override
    public void performAsyncBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp, final RequestParameters requestParameters,
      final String infoMsg) {
        performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    @Override
    public <T extends BonaPortable, R extends RequestParameters> void performAsyncBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp,
        final String infoMsg, final List<T> inputData, final Function<T, R> requestConverterSingle,
        final Function<List<T>, RequestParameters> requestConverterBatch) {

        // must evaluate httpHeaders now, because httpHeaders is a proxy and no longer valid in the other thread (when we process the response)
        final String acceptHeader = determineResponseType(httpHeaders);
        if (inputData == null || inputData.isEmpty()) {
            LOGGER.error("{}: No input data provided", infoMsg);
            returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.MISSING_PARAMETER, null);  // missing parameter
            return;
        }
        for (T elementToCheck: inputData) {
            if (elementToCheck == null) {
                LOGGER.error("Null as list element");
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.MISSING_PARAMETER, "List element null not allowed");
                return;
            }
            try {
                elementToCheck.validate();
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e.getErrorCode(), e.getMessage());
                return;
            }
        }
        final RequestParameters rq;
        if (inputData.size() == 1) {
            try {
                rq = requestConverterSingle.apply(inputData.get(0));
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e.getErrorCode(), e.getMessage());  // missing parameter
                return;
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.GENERAL_EXCEPTION, e.getMessage());
                return;
            }
        } else {
            if (requestConverterBatch == null) {
                LOGGER.error("{}: More than one record provided ({})", infoMsg, inputData.size());
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.TOO_MANY_RECORDS, null);
                return;
            }
            try {
                rq = requestConverterBatch.apply(inputData);
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e.getErrorCode(), e.getMessage());
                return;
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.GENERAL_EXCEPTION, e.getMessage());
                return;
            }
        }
        performAsyncBackendRequest(httpHeaders, resp, rq, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    /**
     * Method to replace the former ImportResource.
     * It takes a list element of a specific type,
     * and a lambda which produces a ServiceRequest,
     * as well as a response generator.
     **/
    @Override
    public <T extends ServiceResponse> void performAsyncBackendRequest(final HttpHeaders httpHeaders,
            final AsyncResponse resp, final RequestParameters requestParameters, final String infoMsg,
            final Class<T> backendResponseClass, final Function<T, BonaPortable> responseMapper) {
        final String acceptHeader = determineResponseType(httpHeaders);
        try {
            requestParameters.validate();  // validate the request before we launch a worker thread!
        } catch (final ApplicationException e) {
            LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e.getErrorCode(), e.getMessage());  // missing parameter
            return;
        }
        final int invocationNo = COUNTER.incrementAndGet();
        final String authorizationHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
        // assign a message ID unless there is one already provided
        if (requestParameters.getMessageId() == null) {
            requestParameters.setMessageId(RandomNumberGenerators.randomFastUUID());
        }
        if (stringSanitizer != null) {
            try {
                requestParameters.treeWalkString(stringSanitizer, true);
            } catch (ApplicationException e) {
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.ILLEGAL_CHARACTER, e.getMessage());
                return;
            }
        }
        LOGGER.debug("Starting {}: {} with assigned messageId {}", invocationNo, infoMsg, requestParameters.getMessageId());

        final CompletableFuture<ServiceResponse> readResponse = connection.executeAsync(authorizationHeader, requestParameters);
        readResponse.thenAccept(sr -> {
            LOGGER.debug("Response obtained {}: {}", invocationNo, infoMsg);
            final Response.ResponseBuilder responseBuilder = RestUtils.createResponseBuilder(sr.getReturnCode());
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
            final int errorCode = (e instanceof ApplicationException ? ((ApplicationException)e).getErrorCode() : T9tException.GENERAL_EXCEPTION);
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, errorCode, e.getMessage(), acceptHeader));
            e.printStackTrace();
            return null;
        });
    }

    @Override
    public void performAsyncAuthBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp, final AuthenticationRequest requestParameters) {
        // must evaluate httpHeaders now, because httpHeaders is a proxy and no longer valid in the other thread
        final String acceptHeader = determineResponseType(httpHeaders);
        final CompletableFuture<ServiceResponse> readResponse = connection.executeAuthenticationAsync(requestParameters);
        readResponse.thenAccept(sr -> {
            final Response.ResponseBuilder responseBuilder = RestUtils.createResponseBuilder(sr.getReturnCode());
            responseBuilder.type(acceptHeader == null || acceptHeader.length() == 0 ? "application/json" : acceptHeader);
            if (ApplicationException.isOk(sr.getReturnCode())) {
                if (AuthenticationResponse.class.isAssignableFrom(sr.getClass())) {

                    final AuthenticationResponse result = (AuthenticationResponse)sr;
                    if (result.getEncodedJwt() != null) {
                        final AuthenticationResult authResult = new AuthenticationResult();
                        authResult.setJwt(result.getEncodedJwt());
                        authResult.setPasswordExpires(result.getPasswordExpires());
                        authResult.setMustChangePassword(result.getMustChangePassword());
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
            final int errorCode = (e instanceof ApplicationException ? ((ApplicationException)e).getErrorCode() : T9tException.GENERAL_EXCEPTION);
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, errorCode, e.getMessage(), acceptHeader));
            e.printStackTrace();
            return null;
        });
    }
}
