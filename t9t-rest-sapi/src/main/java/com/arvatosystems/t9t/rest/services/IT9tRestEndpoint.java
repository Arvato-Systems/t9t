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
package com.arvatosystems.t9t.rest.services;

import com.arvatosystems.t9t.base.StringTrimmer;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import jakarta.ws.rs.core.HttpHeaders;

/** Marker interface to allow collection of REST end points. */
public interface IT9tRestEndpoint {
    DataConverter<String, AlphanumericElementaryDataItem> STRING_TRIMMER = new StringTrimmer();

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
        payload.treeWalkString(STRING_TRIMMER, true);
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

    /**
     * Validates that a path parameter or query parameter has no control characters (which might produce HTTP 500 server errors).
     *
     * @param parameter   the parameter
     * @param name        name of the parameter, for log output / response message
     */
    default void checkValidUnicode(final String parameter, final String name) {
        if (parameter != null) {
            for (int i = 0; i < parameter.length(); ++i) {
                final char c = parameter.charAt(i);
                if (c < 32) {
                    throw new T9tException(T9tException.ILLEGAL_CHARACTER, name);
                }
            }
        }
    }

    /**
     * Validates that a path parameter or query parameter consists of ASCII characters only.
     *
     * @param parameter   the parameter
     * @param name        name of the parameter, for log output / response message
     */
    default void checkValidAscii(final String parameter, final String name) {
        if (parameter != null) {
            for (int i = 0; i < parameter.length(); ++i) {
                final char c = parameter.charAt(i);
                if (c < 32 || c > 127) {
                    throw new T9tException(T9tException.ILLEGAL_CHARACTER, name);
                }
            }
        }
    }
}
