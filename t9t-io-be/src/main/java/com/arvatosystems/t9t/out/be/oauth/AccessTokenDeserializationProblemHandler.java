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
package com.arvatosystems.t9t.out.be.oauth;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AccessTokenDeserializationProblemHandler extends DeserializationProblemHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenDeserializationProblemHandler.class);

    @Override
    public Object handleMissingInstantiator(final DeserializationContext ct, final Class<?> instClass, final ValueInstantiator valueInsta, final JsonParser p,
        final String msg) throws IOException {
        final String fieldName = p != null && p.getParsingContext() != null && p.getParsingContext().getCurrentName() != null
            ? p.getParsingContext().getCurrentName()
            : null;
        LOGGER.warn("Unable to deserialize field {} from OAuth server response. Expected field type is {}. Error message: {}", fieldName, instClass, msg);
        return null;
    }
}
