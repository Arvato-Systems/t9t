/*
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
package com.arvatosystems.t9t.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

    private static final boolean FAIL_ON_UNKNOWN_PROPERTIES_DEFAULT = false;
    private static final boolean ALSO_NULLS_DEFAULT = false;

    private JacksonTools() {
    }

    /**
     * Create {@link ObjectMapper} with given parameters/configuration.
     *
     * @param writeNulls Pass {@code false} to include only properties with non-null values ( {@code Include.NON_NULL})
     * @param failOnUnkownProperties Pass {@code false} to avoid failures when payload contains unknown fields
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper createObjectMapper(final boolean writeNulls, final boolean failOnUnkownProperties) {
        Builder builder = JsonMapper.builder();
        builder.addModules(new JavaTimeModule(), new FixedPointModule(), new JpawModule());
        builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnkownProperties);
        if (!writeNulls) {
            builder.serializationInclusion(Include.NON_NULL);
        }
        return builder.build();
    }

    /**
     * Create {@link ObjectMapper} with given parameters/configuration.
     * Uses default configuration for {@code DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES}=FALSE
     *
     * @param writeNulls Pass {@code false} to include only properties with non-null values (Include.NON_NULL)
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper createObjectMapper(final boolean writeNulls) {
        return createObjectMapper(writeNulls, FAIL_ON_UNKNOWN_PROPERTIES_DEFAULT);
    }


    /**
     * Create {@link ObjectMapper} with given parameters/configuration.
     * Uses default configuration for {@code DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES=FALSE}
     * and {@code SerializationInclusion=Include.NON_NULL}
     *
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper createObjectMapper() {
        return createObjectMapper(ALSO_NULLS_DEFAULT, FAIL_ON_UNKNOWN_PROPERTIES_DEFAULT);
    }

    @Deprecated(forRemoval = true, since = "6.4")
    public static ObjectMapper createJacksonMapperForExports(final boolean writeNulls) {
        return createObjectMapper(writeNulls, FAIL_ON_UNKNOWN_PROPERTIES_DEFAULT);
    }

    @Deprecated(forRemoval = true, since = "6.4")
    public static ObjectMapper createJacksonMapperForExports(final boolean writeNulls, final boolean failOnUnkownProperties) {
        return createObjectMapper(writeNulls, failOnUnkownProperties);
    }

    @Deprecated(forRemoval = true, since = "6.4")
    public static ObjectMapper createJacksonMapperForImports() {
        return createObjectMapper(ALSO_NULLS_DEFAULT, FAIL_ON_UNKNOWN_PROPERTIES_DEFAULT);
    }

}
