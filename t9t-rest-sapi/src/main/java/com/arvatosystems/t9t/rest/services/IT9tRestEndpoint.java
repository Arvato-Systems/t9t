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
package com.arvatosystems.t9t.rest.services;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageParserException;
import jakarta.ws.rs.core.HttpHeaders;

/** Marker interface to allow collection of REST end points. */
public interface IT9tRestEndpoint {
    /**
     * Determines which format to use for the response.
     * The type of the response is either the type requested by ACCEPT, or, in case that is null, matches the content type.
     *
     * @param httpHeaders   the HTTP headers provided to the REST request
     * @return              the type of response, or null
     */
    default String determineResponseType(final HttpHeaders httpHeaders) {
        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);
        return accept != null ? accept : httpHeaders.getHeaderString(HttpHeaders.CONTENT_TYPE);
    }

    /**
     * Validates that the payload is not null, and then also validates the contents of the payload using the generated validation code.
     * This generates a readable exception and not some NPE / Stacktrace.
     *
     * @param payload   the payload object
     */
    default void validatePayload(final BonaPortable payload) {
        if (payload == null) {
            throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, "Missing payload to REST request");
        }
        payload.validate();
    }

    /**
     * Validates that a path or query parameter is not null.
     *
     * @param parameter   the parameter
     * @param name        name of the parameter, for log output / response message
     */
    default void checkNotNull(final Object parameter, final String name) {
        if (parameter == null) {
            throw new T9tException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, name + " can not be blank");
        }
    }
}
