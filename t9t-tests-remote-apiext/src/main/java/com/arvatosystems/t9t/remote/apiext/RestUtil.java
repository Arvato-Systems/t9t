/**
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
package com.arvatosystems.t9t.remote.apiext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.remote.connect.RESTConnection;
import com.arvatosystems.t9t.remote.connect.RESTResult;
import com.arvatosystems.t9t.remote.connect.RequestMethod;
import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.util.ByteArray;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class RestUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUtil.class);
    private static final String BASE_URL = getBaseUrl("http.url");  // it does not change: store it as a static variable!

    private RestUtil() { }

    /** Returns the base URL from a specified system property, or null if it has not been set. */
    @Nullable
    public static String getBaseUrl(@Nonnull final String systemProperty) {
        final String url = System.getProperty(systemProperty);
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }

    /** Returns the base URL from the default system property, or null if it has not been set. */
    @Nullable
    public static String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * Performs a REST call and asserts that the result is OK (HTTP 2xx), then returns the expected type of result.
     * Special case for result type GenericResult, because it occurs so often.
     * Throws an exception in case of error or if no result obtained.
     */
    @Nonnull
    public static GenericResult performRESTCallAndAssertOk(@Nonnull final RequestMethod method, @Nonnull final RESTConnection connection, @Nonnull final String path, @Nullable final BonaPortable payload) {
        return performRESTCallAndAssertOk(method, connection, path, payload, GenericResult.class);
    }

    @Nullable
    private static ByteArray toByteArray(@Nullable final Object payload, final RESTConnection connection)  throws Exception {
        if (payload == null) {
            return null;
        }
        if (payload instanceof BonaPortable bon) {
            return connection.getMarshaller().marshal(bon);
        } else if (payload instanceof String s) {
            return ByteArray.fromString(s);
        } else if (payload instanceof ByteArray ba) {
            return ba;
        } else {
            throw new T9tException(T9tException.UNSUPPORTED_OPERAND, "REST call needs String or BonaPortable, got " + payload.getClass().getCanonicalName());
        }
    }

    @Nonnull
    private static String getSummary(@Nonnull final RequestMethod method, @Nonnull final String path, @Nullable final Object payload) {
        return method + " /" + T9tUtil.nvl(path, "") + (payload == null ? "" : " with payload " + payload.getClass().getCanonicalName());
    }

    /**
     * Performs a REST call and asserts that the result is OK (HTTP 2xx), then returns the expected type of result.
     * Throws an exception in case of error or if no result obtained.
     */
    @Nonnull
    public static <T extends BonaPortable> T performRESTCallAndAssertOk(@Nonnull final RequestMethod method, @Nonnull final RESTConnection connection, @Nonnull final String path,
      @Nullable final Object payload, @Nonnull final Class<T> resultClass) {
        try {
            final ByteArray binaryPayload = toByteArray(payload, connection);
            final RESTResult result = connection.doIO(path, binaryPayload, method);
            if (!connection.isOK(result.httpStatusCode())) {
                final String summary = getSummary(method, path, payload);
                LOGGER.error("{} failed with HTTP code {}: {}", summary, result.httpStatusCode(), result.errorMessage());
                throw new RuntimeException(summary + " failed with HTTP code " + result.httpStatusCode() + ": " + result.errorMessage());
            }
            if (result.responsePayload() == null) {
                final String summary = getSummary(method, path, payload);
                LOGGER.error("{} returned no response payload", summary);
                throw new RuntimeException(summary + " returned no response payload");
            }
            return connection.getMarshaller().unmarshal(result.responsePayload(), resultClass);
        } catch (final Exception e) {
            LOGGER.error("Unexpected exception {}: {}", e.getClass().getCanonicalName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs a REST call and either returns the expected result class, or in case of an error, the GenericResult response.
     * In case the response did not have any payload, <code>null</code> is returned.
     */
    @Nullable
    public static <T extends BonaPortable> Object performRESTCall(@Nonnull final RequestMethod method, @Nonnull final RESTConnection connection, @Nonnull final String path,
      @Nullable final Object payload, @Nonnull final Class<T> resultClass) throws Exception {
        final ByteArray binaryPayload = toByteArray(payload, connection);
        final RESTResult result = connection.doIO(path, binaryPayload, method);
        if (result.responsePayload() == null) {
            return null;
        }
        if (!connection.isOK(result.httpStatusCode())) {
            return connection.getMarshaller().unmarshal(result.responsePayload(), GenericResult.class);
        }
        return connection.getMarshaller().unmarshal(result.responsePayload(), resultClass);
    }

    /**
     * Performs a REST call and asserts that the result is an error (HTTP 2xx).
     * Throws an exception in case of no error or a different error than expected.
     */
    public static GenericResult performRESTCallAndAssertError(@Nonnull final RequestMethod method, @Nonnull final RESTConnection connection, @Nonnull final String path,
      @Nullable final Object payload, final int expectedError, @Nonnull final String message) {
        try {
            final ByteArray binaryPayload = toByteArray(payload, connection);
            final RESTResult result = connection.doIO(path, binaryPayload, method);
            if (connection.isOK(result.httpStatusCode())) {
                final String summary = getSummary(method, path, payload);
                LOGGER.error("{} was OK, but expected error {}: {}", summary, expectedError, message);
                throw new RuntimeException(summary + " was OK, but expected error " + expectedError + ": " + message);
            }
            final GenericResult gr = connection.getMarshaller().unmarshal(result.responsePayload(), GenericResult.class);
            if (expectedError >= 0) {
                if (gr.getReturnCode() != expectedError) {
                    final String summary = getSummary(method, path, payload);
                    LOGGER.error("{} returned {}, but expected error {}: {}", summary, gr.getReturnCode(), expectedError, message);
                    throw new RuntimeException(summary + " returned " + gr.getReturnCode() + ", but expected error " + expectedError + ": " + message);
                }
            }
            return gr;
        } catch (final Exception e) {
            // any other is reported
            LOGGER.error("Unexpected exception {}: {}", e.getClass().getCanonicalName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
