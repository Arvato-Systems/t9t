package com.arvatosystems.t9t.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.jpaw.fixedpoint.jackson.FixedPointModule;

/**
 * Utility class to provide Jackson ObjectMappers, preconfigured with the usual settings:
 * date / time / timestamp fields as ISO 8601 strings instead of some data structure
 * Java 8 date/time support
 * Jpaw fixed point support
 * Optionally suppress output of nulls.
 */
public final class JacksonTools {
    private JacksonTools() { }

    public static ObjectMapper createJacksonMapperForExports(boolean writeNulls) {
        final ObjectMapper objectMapper = new ObjectMapper();
        if (!writeNulls) {
            objectMapper.setSerializationInclusion(Include.NON_NULL);          // do not write null assignments
        }
        objectMapper.registerModule(new JavaTimeModule());                     // Java 8 date/time support
        objectMapper.registerModule(new FixedPointModule());                   // JPAW fixed point support
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // dates and timestamp not as data structure, but as string in ISO 8601 format
        return objectMapper;
    }

    public static ObjectMapper createJacksonMapperForImports() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new FixedPointModule());
        return objectMapper;
    }
}
