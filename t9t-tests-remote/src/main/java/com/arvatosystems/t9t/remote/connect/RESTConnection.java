/**
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.remote.connect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.bonaparte.util.impl.RecordMarshallerJson;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class RESTConnection extends ConnectionDefaults {
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTConnection.class);

    protected IMarshaller marshaller = new RecordMarshallerJson();
    protected final boolean marshallerFrozen;
    protected final String baseRestUrl;
    protected String authentication = null;
    protected RequestMethod requestMethod = RequestMethod.POST;  // the default request method

    public RESTConnection() {
        baseRestUrl = getInitialRestUrl();
        marshallerFrozen = false;
    }

    /** Contructs a REST connection for a base REST URL. */
    public RESTConnection(@Nonnull final String baseRestUrl) {
        this.baseRestUrl = baseRestUrl;
        marshallerFrozen = false;
    }

    /** Contructs a REST connection for an optional base REST URL, and frozen marshaller */
    public RESTConnection(@Nullable final String baseRestUrl, @Nonnull final IMarshaller marshaller) {
        this.baseRestUrl = baseRestUrl == null ? getInitialRestUrl() : baseRestUrl;
        this.marshaller = marshaller;
        marshallerFrozen = true;
    }

    public String getBaseRestUrl() {
        return baseRestUrl;
    }

    public IMarshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(@Nonnull final IMarshaller marshaller) {
        if (marshallerFrozen) {
            throw new T9tException(T9tException.ATTEMPT_TO_CHANGE_FROZEN_FIELD, "marshaller of type " + this.marshaller.getContentType() + " to " + marshaller.getContentType());
        }
        this.marshaller = marshaller;
    }

    public void setAuthentication(@Nullable final String authentication) {
        this.authentication = authentication;
    }

    public void setRequestMethod(@Nonnull final RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    protected void setRequestProperties(@Nonnull final HttpURLConnection connection) {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(5000);
        connection.setRequestProperty(T9tConstants.HTTP_HEADER_CONTENT_TYPE,   marshaller.getContentType());
        connection.setRequestProperty(T9tConstants.HTTP_HEADER_ACCEPT,         marshaller.getContentType());
        connection.setRequestProperty(T9tConstants.HTTP_HEADER_CHARSET,        T9tConstants.HTTP_CHARSET_UTF8);
        connection.setRequestProperty(T9tConstants.HTTP_HEADER_ACCEPT_CHARSET, T9tConstants.HTTP_CHARSET_UTF8);
        connection.setUseCaches(false);
    }

    public boolean isOK(final int httpCode) {
        return httpCode >= 200 && httpCode <= 299;
    }
//    public boolean sendsPayload(final int httpCode) {
//        return isOK(httpCode) || (httpCode >= 400 && httpCode <= 500);
//    }

    /** Sends a request to the REST server for a given full URL and returns the response. */
    public RESTResult doIO(@Nonnull final URL url, @Nullable final ByteArray request, @Nonnull final RequestMethod rqMethod) {
        try {
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(rqMethod.toString());

            setRequestProperties(connection);
            if (authentication != null) {
                connection.setRequestProperty(T9tConstants.HTTP_HEADER_AUTH, authentication);
            }

            OutputStream wr = null;

            if (request != null) {
                // write the request as payload
                connection.setRequestProperty(T9tConstants.HTTP_HEADER_CONTENT_LENGTH, "" + request.length());
                if (requestMethod == RequestMethod.POST || requestMethod == RequestMethod.PUT) {
                     wr = connection.getOutputStream();
                     request.toOutputStream(wr);
                     wr.flush();
                }
            }

            // the status should be available after getInputStream for GET, but before for POST
            // in either case, swapping the order would cause a nasty IOException!

            final int returnCode = connection.getResponseCode();
            final String statusMessage = connection.getResponseMessage();

            // retrieve the response or error response
            InputStream is = null;
            try {
                is = connection.getInputStream();
            } catch (final Exception e) {
                is = connection.getErrorStream();
            }

            final ByteBuilder serializedResponse;
            if (is != null) {
                serializedResponse = new ByteBuilder();
                serializedResponse.readFromInputStream(is, 0);
                is.close();
            } else {
                serializedResponse = null;
            }

            if (wr != null) {
                wr.close();
            }

            return new RESTResult(returnCode, statusMessage, serializedResponse);
        } catch (final Exception e) {
            LOGGER.error("Exception during REST request: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ByteBuilder doIO(@Nonnull final URL url, @Nullable final ByteArray request, final boolean expectError) {
        final RESTResult result = doIO(url, request, requestMethod);
        final boolean resultOk = isOK(result.httpStatusCode());
        if (expectError) {
            if (resultOk) {
                LOGGER.error("REST request successful, but expected error");
            }
        } else {
            if (!resultOk) {
                LOGGER.error("REST request failed: " + result.httpStatusCode() + ": " + result.errorMessage());
            }
        }
        return result.responsePayload();
    }

    /** Sends a request to the REST server for a given path, which is added to the base URL, and returns the response. */
    public RESTResult doIO(@Nonnull final String path, @Nullable final ByteArray request, @Nonnull final RequestMethod rqMethod) throws Exception {
        final URL url = new URL(baseRestUrl + "/" + path);
        return doIO(url, request, rqMethod);
    }

    public BonaPortable doIO(@Nonnull final BonaPortable request) throws Exception {
        final ByteArray serializedRequest = marshaller.marshal(request);

        final String variablePath = request.ret$BonaPortableClass().getProperty("path");
        LOGGER.debug("Sending object {} to URL {}/{} in format {}", request.ret$PQON(), baseRestUrl, variablePath, marshaller.getContentType());
        final URL url = new URL(baseRestUrl + "/" + variablePath);

        final ByteBuilder serializedResponse = doIO(url, serializedRequest, false);

        return serializedResponse != null && serializedResponse.length() > 0 ? marshaller.unmarshal(serializedResponse) : null;
    }

    public String doIO(@Nonnull final String request, @Nonnull final String path, final boolean expectError) throws Exception {
        LOGGER.debug("Sending String to URL {}/{} in format {}", baseRestUrl, path, marshaller.getContentType());
        final URL url = new URL(baseRestUrl + "/" + path);

        final ByteBuilder response = doIO(url, ByteArray.fromString(request, StandardCharsets.UTF_8), expectError);
        if (response != null && response.length() > 0) {
            response.setCharset(StandardCharsets.UTF_8);
            return response.toString();
        }
        return null;
    }
}
