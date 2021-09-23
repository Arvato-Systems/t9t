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
package com.arvatosystems.t9t.rest.vertx.impl;

import static de.jpaw.util.ApplicationException.CLASSIFICATION_FACTOR;
import static de.jpaw.util.ApplicationException.CL_DATABASE_ERROR;
import static de.jpaw.util.ApplicationException.CL_DENIED;
import static de.jpaw.util.ApplicationException.CL_INTERNAL_LOGIC_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARAMETER_ERROR;
import static de.jpaw.util.ApplicationException.CL_PARSER_ERROR;
import static de.jpaw.util.ApplicationException.CL_SUCCESS;
import static de.jpaw.util.ApplicationException.CL_TIMEOUT;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationInfo;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.server.services.ICachingAuthenticationProcessor;
import com.arvatosystems.t9t.server.services.IRequestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;
import io.vertx.core.Vertx;

@Singleton
public class T9tRestProcessor implements IT9tRestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestProcessor.class);

    protected final Vertx vertx = Jdp.getRequired(Vertx.class);
    protected final ICachingAuthenticationProcessor authenticationProcessor = Jdp.getRequired(ICachingAuthenticationProcessor.class);
    protected final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);

    @Override
    public <T extends ServiceResponse> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp,
            RequestParameters requestParameters, String infoMsg, Class<T> backendResponseClass,
            Function<T, BonaPortable> responseMapper) {
        try {
            requestParameters.validate();  // validate the request before e launch a worker thread!
        } catch (ApplicationException e) {
            LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(e.getErrorCode(), e.getMessage()));  // missing parameter
            return;
        }
        final String authHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.length() < 8) {
            returnAsyncResult(httpHeaders, resp, Status.UNAUTHORIZED, null);  // missing auth header
            return;
        }
        vertx.<ServiceResponse>executeBlocking(
            promise -> {
                try {
                    // Clear all old MDC data, since a completely new request is now processed
                    MDC.clear();
                    // get the authentication info
                    final AuthenticationInfo authInfo = authenticationProcessor.getCachedJwt(authHeader);
                    if (authInfo.getEncodedJwt() == null) {
                        promise.fail("Not authenticated");
                        return;
                    }
                    // Authentication is valid. Now populate the MDC and start processing the request.
                    final JwtInfo jwtInfo = authInfo.getJwtInfo();
                    MDC.put(T9tConstants.MDC_USER_ID, jwtInfo.getUserId());
                    MDC.put(T9tConstants.MDC_TENANT_ID, jwtInfo.getTenantId());
                    MDC.put(T9tConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.getSessionRef(), null));

                    LOGGER.debug("{}: processing start", infoMsg);
                    promise.complete(requestProcessor.execute(null, requestParameters, jwtInfo, authInfo.getEncodedJwt(), false));
                    LOGGER.debug("{}: processing end", infoMsg);
                } catch (Exception e) {
                    LOGGER.debug("{}: processing exception", infoMsg);
                    promise.fail(e);
                }
            }, false, ar -> {
                if (ar.succeeded()) {
                    final ServiceResponse sr = ar.result();
                    if (!ApplicationException.isOk(sr.getReturnCode())) {
                        final Response.ResponseBuilder responseBuilder = createResponseBuilder(sr.getReturnCode());
                        final Response responseObj = responseBuilder.build();
                        resp.resume(responseObj);
                    }
                    if (backendResponseClass.isAssignableFrom(sr.getClass())) {
                        final BonaPortable resultForREST = responseMapper.apply((T)sr);
                        if (resultForREST instanceof MediaData) {
                            // special handling for MediaData: return a String or byte array, depending on contents
                            resumeMediaData(resp, (MediaData)resultForREST);
                        } else {
                            resp.resume(resultForREST);
                        }
                    } else {
                        resp.resume(error(Status.INTERNAL_SERVER_ERROR, "Response type mismatch"));
                    }
                } else {
                    resp.resume(error(Status.INTERNAL_SERVER_ERROR, "Processing failed"));
                }
            }
        );
    }

    /** For the special case of a response of type MediaData, either return the byte[] or the text. */
    protected void resumeMediaData(AsyncResponse resp, MediaData md) {
        final ResponseBuilder rb = Response.status(Status.OK);
        final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(md.getMediaType());
        if (mtd != null) {
            final String mimeType = mtd.getMimeType();
            if (mimeType != null && !mimeType.isEmpty()) {
                rb.type(mimeType);
            }
        }

        if (md.getRawData() != null) {
            rb.entity(md.getRawData().getBytes());
        } else {
            rb.entity(md.getText());
        }
        resp.resume(rb.build());
    }

    @Override
    public void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp, RequestParameters requestParameters, String infoMsg) {
        performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    @Override
    public void returnAsyncResult(final HttpHeaders httpHeaders, final AsyncResponse resp, final Response.Status status, final Object result) {
        final String acceptHeader = httpHeaders.getHeaderString("Accept");
        final Response.ResponseBuilder response = Response.status(status);
        response.type(acceptHeader == null || acceptHeader.length() == 0 ? "application/json" : acceptHeader);
        if (result != null) {
            response.entity(result);
        }
        final Response responseObj = response.build();
        resp.resume(responseObj);
    }

    @Override
    public GenericResult createResultFromServiceResponse(final ServiceResponse response) {
        final GenericResult result = new GenericResult();
        result.setErrorDetails(response.getErrorDetails());
        result.setErrorMessage(response.getErrorMessage());
        result.setReturnCode(response.getReturnCode());
        result.setProcessRef(response.getProcessRef());
        return result;
    }

    public static Response error(final Response.Status status, final String message) {
        final Response.ResponseBuilder response = Response.status(status);
        response.type(MediaType.TEXT_PLAIN_TYPE).entity(message);
        return response.build();
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

    private GenericResult createErrorResult(int returnCode, String errorDetails) {
        final GenericResult result = new GenericResult();
        result.setErrorDetails(errorDetails);
        result.setErrorMessage(T9tException.codeToString(returnCode));
        result.setReturnCode(returnCode);
        result.setProcessRef(0L);
        return result;
    }

    @Override
    public <T> void performAsyncBackendRequest(HttpHeaders httpHeaders, AsyncResponse resp,
            String infoMsg, List<T> inputData,
            Function<T, RequestParameters> requestConverterSingle, Function<List<T>, RequestParameters> requestConverterBatch) {
        if (inputData == null || inputData.isEmpty()) {
            LOGGER.error("{}: No input data provided", infoMsg);
            returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(T9tException.MISSING_PARAMETER, null));  // missing parameter
            return;
        }
        final RequestParameters rq;
        if (inputData.size() == 1) {
            try {
                rq = requestConverterSingle.apply(inputData.get(0));
            } catch (ApplicationException e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(e.getErrorCode(), e.getMessage()));  // missing parameter
                return;
            } catch (Exception e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(T9tException.GENERAL_EXCEPTION, e.getMessage()));  // missing parameter
                return;
            }
        } else {
            if (requestConverterBatch == null) {
                LOGGER.error("{}: More than one record provided ({})", infoMsg, inputData.size());
                returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(T9tException.TOO_MANY_RECORDS, null));  // too many parameters
                return;
            }
            try {
                rq = requestConverterBatch.apply(inputData);
            } catch (ApplicationException e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(e.getErrorCode(), e.getMessage()));  // missing parameter
                return;
            } catch (Exception e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(httpHeaders, resp, Status.BAD_REQUEST, createErrorResult(T9tException.GENERAL_EXCEPTION, e.getMessage()));  // missing parameter
                return;
            }
        }
        performAsyncBackendRequest(httpHeaders, resp, rq, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }
}
