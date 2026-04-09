/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.services.IGatewayStringSanitizerFactory;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.rest.utils.RestUtils;
import com.arvatosystems.t9t.xml.auth.AuthenticationResult;

@Singleton
public class T9tRestProcessor implements IT9tRestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tRestProcessor.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * Class loader used by Spring Boot to load classes/resources from a repackaged boot jar.
     *
     * When the gateway is started using the Spring Boot launcher, the application is loaded using a
     * org.springframework.boot.loader.launch.LaunchedClassLoader (exact subclass depends on the
     * Spring Boot version). Many libraries (including JAXB via java.util.ServiceLoader) rely on the
     * thread context class loader (TCCL) to discover provider implementations.
     *
     * Problem:
     *   Async callbacks in this gateway are executed on worker threads (typically ForkJoinPool.commonPool).
     *   Those threads are not created by the Boot launcher and therefore do NOT necessarily have
     *   the Boot class loader set as their TCCL.
     *   As a consequence, provider discovery may fail at runtime, e.g. JAXB cannot locate the
     *   ContextFactory implementation which results in jakarta.xml.bind.JAXBException:
     *   Implementation of Jakarta XML Binding-API has not been found...
     *
     * Solution:
     * Capture the Boot class loader once during class initialization and explicitly set it as the
     * TCCL for all async callback code paths. This keeps behavior stable regardless of how the executing
     * threads were created.
     *
     * Notes:
     * This is intentionally implemented as a small, local utility instead of introducing a global executor wrapper,
     * because only specific callbacks require the correct TCCL (JAXB / RESTEasy provider).
     * The previous TCCL is always restored to avoid side effects on unrelated code that might run
     * on the same worker thread afterwards.
     */
    private static final ClassLoader BOOT_CLASSLOADER = T9tRestProcessor.class.getClassLoader();

    protected final IRemoteConnection connection = Jdp.getRequired(IRemoteConnection.class);
    protected final IGatewayStringSanitizerFactory gatewayStringSanitizerFactory = Jdp.getRequired(IGatewayStringSanitizerFactory.class);
    protected final DataConverter<String, AlphanumericElementaryDataItem> stringSanitizer = gatewayStringSanitizerFactory.createStringSanitizerForGateway();
    protected final IPAddressBlocker ipBlockerService = Jdp.getRequired(IPAddressBlocker.class);

    @Override
    public ServiceResponse performSyncBackendRequest(final RequestParameters requestParameters, final String authHeader, final String infoMsg) {
        return connection.execute(authHeader, requestParameters);
    }

    @Override
    public void performAsyncBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp, final RequestParameters requestParameters,
      final String infoMsg) {
        performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, ServiceResponse.class, sr -> createResultFromServiceResponse(sr));
    }

    @Override
    public <T, R extends RequestParameters> void performAsyncBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp,
        final String infoMsg, final List<T> inputData, final Function<T, R> requestConverterSingle,
        final Function<List<T>, R> requestConverterBatch) {

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
                if (elementToCheck instanceof BonaPortable bon) {
                    // can use the Bonaparte extended validation
                    bon.validate();
                }
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e);
                return;
            }
        }
        final RequestParameters rq;
        if (inputData.size() == 1) {
            try {
                rq = requestConverterSingle.apply(inputData.get(0));
            } catch (final ApplicationException e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e);  // missing parameter
                return;
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion (single): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.GENERAL_EXCEPTION, null);
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
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e);
                return;
            } catch (final Exception e) {
                LOGGER.error("Exception during request conversion (multi): {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, T9tException.GENERAL_EXCEPTION, null);
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
        performAsyncBackendRequest(httpHeaders, resp, requestParameters, infoMsg, backendResponseClass, responseMapper, this::createResultFromServiceResponse);
    }

    @Override
    public <T extends ServiceResponse> void performAsyncBackendRequest(final HttpHeaders httpHeaders,
            final AsyncResponse resp, final RequestParameters requestParameters, final String infoMsg,
            final Class<T> backendResponseClass, final Function<T, BonaPortable> responseMapper,
            final Function<ServiceResponse, BonaPortable> errorResponseMapper) {
        final String acceptHeader = determineResponseType(httpHeaders);
        final String remoteIp = httpHeaders.getHeaderString(T9tConstants.HTTP_HEADER_FORWARDED_FOR);
        try {
            requestParameters.validate();  // validate the request before we launch a worker thread!
        } catch (final ApplicationException e) {
            LOGGER.error("Exception during request validation: {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
            returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e);  // missing parameter
            return;
        }
        final int invocationNo = COUNTER.incrementAndGet();
        final String authorizationHeader = MessagingUtil.massageAuthHeader(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
        final String sessionToken = httpHeaders.getHeaderString(T9tConstants.HTTP_HEADER_X_SESSION_TOKEN);
        // assign a message ID unless there is one already provided
        if (requestParameters.getMessageId() == null) {
            requestParameters.setMessageId(RandomNumberGenerators.randomFastUUID());
        }
        if (stringSanitizer != null) {
            try {
                requestParameters.treeWalkString(stringSanitizer, true);
            } catch (final ApplicationException e) {
                returnAsyncResult(acceptHeader, resp, Status.BAD_REQUEST, e);
                return;
            }
        }
        LOGGER.debug("Starting {}: {} with assigned messageId {}", invocationNo, infoMsg, requestParameters.getMessageId());
        requestParameters.setWhenSent(System.currentTimeMillis());  // assumes all server clocks are sufficiently synchronized
        requestParameters.setTransactionOriginType(TransactionOriginType.GATEWAY_EXTERNAL);

        final CompletableFuture<ServiceResponse> readResponse = connection.executeAsync(authorizationHeader, sessionToken, requestParameters);
        readResponse.thenAccept(withBootTccl(sr -> {
            LOGGER.debug("Response obtained {}: {}", invocationNo, infoMsg);
            if (ApplicationException.isOk(sr.getReturnCode())) {
                final Response.ResponseBuilder responseBuilder = RestUtils.createResponseBuilder(sr.getReturnCode());
                responseBuilder.type(acceptHeader == null || acceptHeader.length() == 0 ? MediaType.APPLICATION_JSON : acceptHeader);
                if (backendResponseClass.isAssignableFrom(sr.getClass())) {

                    final BonaPortable resultForREST = responseMapper.apply((T)sr);
                    if (resultForREST instanceof MediaData md) {
                        // special handling for MediaData: return a String or byte array, depending on contents
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
                    responseBuilder.entity(errorResponseMapper.apply(sr));  // this is like the error result
                }
                addSecurityHeader(responseBuilder);
                final Response responseObj = responseBuilder.build();
                resp.resume(responseObj);
            } else {
                // map error report
                createGenericResultEntity(sr, resp, acceptHeader, () -> ipBlockerService.registerBadAuthFromIp(remoteIp), errorResponseMapper);
            }
        })).exceptionally(withBootTcclThrowable(e -> {
            final int errorCode = e instanceof ApplicationException ae ? ae.getErrorCode() : T9tException.GENERAL_EXCEPTION;
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, errorCode, e.getMessage(), acceptHeader));
            e.printStackTrace();
            return null;
        }));
    }

    @Override
    public void performAsyncAuthBackendRequest(final HttpHeaders httpHeaders, final AsyncResponse resp, final AuthenticationRequest requestParameters) {
        // must evaluate httpHeaders now, because httpHeaders is a proxy and no longer valid in the other thread
        final String acceptHeader = determineResponseType(httpHeaders);
        final String remoteIp = httpHeaders.getHeaderString(T9tConstants.HTTP_HEADER_FORWARDED_FOR);
        requestParameters.setWhenSent(System.currentTimeMillis());  // assumes all server clocks are sufficiently synchronized
        requestParameters.setTransactionOriginType(TransactionOriginType.GATEWAY_EXTERNAL);
        final CompletableFuture<ServiceResponse> readResponse = connection.executeAuthenticationAsync(requestParameters);
        readResponse.thenAccept(withBootTccl(sr -> {
            if (ApplicationException.isOk(sr.getReturnCode())) {
                final Response.ResponseBuilder responseBuilder = RestUtils.createResponseBuilder(sr.getReturnCode());
                responseBuilder.type(acceptHeader == null || acceptHeader.length() == 0 ? "application/json" : acceptHeader);
                if (AuthenticationResponse.class.isAssignableFrom(sr.getClass())) {

                    final AuthenticationResponse result = (AuthenticationResponse)sr;
                    if (result.getEncodedJwt() != null) {
                        final AuthenticationResult authResult = new AuthenticationResult();
                        authResult.setJwt(result.getEncodedJwt());
                        authResult.setPasswordExpires(result.getPasswordExpires());
                        authResult.setMustChangePassword(result.getMustChangePassword());
                        returnAsyncResult(acceptHeader, resp, Response.Status.OK, authResult);
                        return;
                    }
                } else {
                    // this is a coding issue!
                    LOGGER.error("Coding bug: expected response of class AuthenticationResponse, but got {} (return code {})",
                        sr.getClass().getSimpleName(), sr.getReturnCode());
                    responseBuilder.entity(createResultFromServiceResponse(sr));  // this is like the error result
                }
                addSecurityHeader(responseBuilder);
                final Response responseObj = responseBuilder.build();
                resp.resume(responseObj);
            } else {
                // map error report
                createGenericResultEntity(sr, resp, acceptHeader, () -> ipBlockerService.registerBadAuthFromIp(remoteIp),
                    this::createResultFromServiceResponse);
            }
        })).exceptionally(withBootTcclThrowable(e -> {
            final int errorCode = e instanceof ApplicationException ae ? ae.getErrorCode() : T9tException.GENERAL_EXCEPTION;
            resp.resume(RestUtils.error(Response.Status.INTERNAL_SERVER_ERROR, errorCode, e.getMessage(), acceptHeader));
            e.printStackTrace();
            return null;
        }));
    }

    /**
     * Wraps a Runnable so it runs with the Spring Boot class loader as thread context class loader (TCCL).
     *
     * Use this for asynchronous callbacks which may be executed on threads where the TCCL is not set to the
     * Spring Boot BOOT_CLASSLOADER. This is particularly important for JAXB usage, because JAXB discovers
     * its runtime implementation via java.util.ServiceLoader using the TCCL.
     *
     * Implementation details:
     * If the current TCCL is already BOOT_CLASSLOADER, we run the callback directly (fast path).
     * Otherwise we temporarily set the TCCL to BOOT_CLASSLOADER, run the callback, and restore the previous TCCL
     * in a finally block.
     *
     */
    private static Runnable withBootTccl(final Runnable r) {
        return () -> {
            final Thread t = Thread.currentThread();
            final ClassLoader prev = t.getContextClassLoader();
            if (prev == BOOT_CLASSLOADER) {
                r.run();
                return;
            }
            t.setContextClassLoader(BOOT_CLASSLOADER);
            try {
                r.run();
            } finally {
                t.setContextClassLoader(prev);
            }
        };
    }

    /**
     * Wraps a java.util.function.Consumer so it runs with the Boot class loader as TCCL.
     * This is mainly used with CompletableFuture#thenAccept(java.util.function.Consumer)
     */
    private static <T> java.util.function.Consumer<T> withBootTccl(final java.util.function.Consumer<T> c) {
        return (t) -> withBootTccl(() -> c.accept(t)).run();
    }

    /**
     * Wraps a java.util.function.Function (typically used as an exception handler) so it runs with the
     * Boot class loader as TCCL.
     * This is mainly used with CompletableFuture#exceptionally(java.util.function.Function).
     */
    private static <T> java.util.function.Function<Throwable, T> withBootTcclThrowable(final java.util.function.Function<Throwable, T> f) {
        return (thr) -> {
            final Thread th = Thread.currentThread();
            final ClassLoader prev = th.getContextClassLoader();
            if (prev == BOOT_CLASSLOADER) {
                return f.apply(thr);
            }
            th.setContextClassLoader(BOOT_CLASSLOADER);
            try {
                return f.apply(thr);
            } finally {
                th.setContextClassLoader(prev);
            }
        };
    }
}
