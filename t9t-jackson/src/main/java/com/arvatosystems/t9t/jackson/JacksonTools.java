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
package com.arvatosystems.t9t.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.jpaw.fixedpoint.jackson.FixedPointModule;

/**
 * Utility class to provide Jackson ObjectMappers, preconfigured with the usual settings: date / time / timestamp fields as ISO 8601 strings instead of some
 * data structure, Java 8 date/time support, Jpaw fixed point support, Jpaw ByteArray support. Optionally suppress output of nulls.
 */
public final class JacksonTools {

    private JacksonTools() {
    }

    public static ObjectMapper createJacksonMapperForExports(final boolean writeNulls) {
        Builder jsonMapperBuilder = getCommonMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (!writeNulls) {
            jsonMapperBuilder.serializationInclusion(Include.NON_NULL);
        }
        return jsonMapperBuilder.build();
    }

    public static ObjectMapper createJacksonMapperForImports() {
        return getCommonMapper().build();
    }

    /**
     * Common mapper configuration for imports and exports.
     */
    private static Builder getCommonMapper() {
        return JsonMapper.builder().addModules(new JavaTimeModule(), new FixedPointModule(), new JpawModule());
    }
}
