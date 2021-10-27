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
package com.arvatosystems.t9t.client.jdk11;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class RemoteConnection implements IRemoteConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConnection.class);

    protected final IRemoteDefaultUrlRetriever cfgRetriever = Jdp.getRequired(IRemoteDefaultUrlRetriever.class);
    protected final HttpClient httpClient = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    protected final URI authUri;
    protected final URI rpcUri;

    public RemoteConnection(final String regular) {
        final String regularPath = regular == null ? cfgRetriever.getDefaultRemoteUrl() : regular;
        try {
            rpcUri = new URI(regularPath);
        } catch (final URISyntaxException e) {
            LOGGER.error("FATAL: Cannot construct remote request URI: {}", ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
        try {
            authUri = new URI(IRemoteDefaultUrlRetriever.getDefaultRemoteUrlLogin(regularPath));
        } catch (final URISyntaxException e) {
            LOGGER.error("FATAL: Cannot construct remote authentication URI: {}", ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
    }

    public RemoteConnection() {
        this (null);
    }

    @Override
    public ServiceResponse execute(final String authenticationHeader, final RequestParameters rp) {
        return execSub(rpcUri, authenticationHeader, rp);
    }

    private ServiceResponse convertResponse(final HttpResponse<byte[]> response, final boolean isAuthentication) {
        final int returnCode = response.statusCode();
        final String httpStatusMessage = null;
        LOGGER.debug("*** HTTP Response {}, connection type {}", returnCode, response.version());
        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {   // accept 200, 201, etc...
            LOGGER.warn("response is HTTP {} ({})", returnCode, null);
            return MessagingUtil.createServiceResponse(
                    T9tException.HTTP_ERROR + returnCode,
                    httpStatusMessage,
                    null, null);
        }
        final byte[] receivedBuffer = response.body();
        final BonaPortable obj = new CompactByteArrayParser(receivedBuffer, 0, -1).readRecord();
        if (obj == null) {
            if (isAuthentication) {
                LOGGER.info("Response object is null for AUTHENTICATION: http code {}, status {}", returnCode, httpStatusMessage);
                return MessagingUtil.createServiceResponse(
                        T9tException.GENERAL_AUTH_PROBLEM,
                        AuthenticationResponse.class.getCanonicalName(),
                        null, null);
            } else {
                LOGGER.info("Response object is null for GENERAL request: http code {}, status {}", returnCode, httpStatusMessage);
                return MessagingUtil.createServiceResponse(
                        T9tException.BAD_REMOTE_RESPONSE,
                        Integer.toString(returnCode),
                        null, null);
            }
        }
        if (obj instanceof ServiceResponse) {
            final ServiceResponse r = (ServiceResponse)obj;
            LOGGER.info("Received response type {} with return code {}", r.ret$PQON(), r.getReturnCode());
            if (r.getReturnCode() != 0) {
                LOGGER.info("Error details are {}, message is {}", r.getErrorDetails(), r.getErrorMessage());
            }
            return r;
        }


        LOGGER.error("Response is of wrong type: {}", response.getClass().getCanonicalName());
        return MessagingUtil.createServiceResponse(
                T9tException.GENERAL_EXCEPTION,
                response.getClass().getCanonicalName(),
                null, null);

    }

    protected ServiceResponse execSub(final URI uri, final String authentication, final RequestParameters rp) {
        try {
            LOGGER.debug("Sending request of type {}", rp.ret$PQON());

            final HttpRequest httpRq = buildRequest(uri, authentication, rp);
            final HttpResponse<byte[]> response = httpClient.send(httpRq, HttpResponse.BodyHandlers.ofByteArray());

            return convertResponse(response, rp instanceof AuthenticationRequest);
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null);
        }
    }

    @Override
    public ServiceResponse executeAuthenticationRequest(final AuthenticationRequest rp) {
        return execSub(authUri, null, rp);
    }

    @Override
    public CompletableFuture<ServiceResponse> executeAsync(final String authentication, final RequestParameters rp) {
        try {
            return doIO(rpcUri, authentication, rp);
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null));
        }
    }

    @Override
    public CompletableFuture<ServiceResponse> executeAuthenticationAsync(final AuthenticationRequest rp) {
        try {
            return doIO(authUri, null, rp);
        } catch (final Exception e) {
            final String causeChain = ExceptionUtil.causeChain(e);
            LOGGER.error("I/O error for PQON {}: {}", rp.ret$PQON(), causeChain);
            return CompletableFuture.supplyAsync(() -> MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null));
        }
    }
    private HttpRequest buildRequest(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final CompactByteArrayComposer bac = new CompactByteArrayComposer(false);
        bac.writeRecord(request);
        bac.close();

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri)
                .version(Version.HTTP_2)
                .POST(BodyPublishers.ofByteArray(bac.getBuffer(), 0, bac.getLength()))
                .timeout(Duration.ofSeconds(55));

        if (authentication != null)
            httpRequestBuilder.header("Authorization", authentication);

        httpRequestBuilder.header("Content-Type",   MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Accept",         MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Charset",        "utf-8");
        httpRequestBuilder.header("Accept-Charset", "utf-8");
        return httpRequestBuilder.build();
    }

    private CompletableFuture<ServiceResponse> doIO(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final HttpRequest httpRq = buildRequest(uri, authentication, request);
        final BodyHandler<byte[]> serializedRequest = HttpResponse.BodyHandlers.ofByteArray();
        final CompletableFuture<HttpResponse<byte[]>> responseF = httpClient.sendAsync(httpRq, serializedRequest);
        return responseF.thenApply(response -> {
            return convertResponse(response, request instanceof AuthenticationRequest);
        });
    }
}
