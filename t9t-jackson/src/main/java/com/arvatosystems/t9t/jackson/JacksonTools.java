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
 * data structure Java 8 date/time support Jpaw fixed point support Optionally suppress output of nulls.
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
        return JsonMapper.builder().addModules(new JavaTimeModule(), new FixedPointModule());
    }

}
