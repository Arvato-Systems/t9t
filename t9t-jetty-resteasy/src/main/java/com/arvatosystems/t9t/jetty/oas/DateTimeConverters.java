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
package com.arvatosystems.t9t.jetty.oas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.UUID;

import com.fasterxml.jackson.databind.JavaType;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class DateTimeConverters implements ModelConverter {

    @Override
    public Schema resolve(final AnnotatedType type, final ModelConverterContext context, final Iterator<ModelConverter> chain) {
        if (type.isSchemaProperty()) {
            final JavaType myType = Json.mapper().constructType(type.getType());
            if (myType != null) {
                final Class<?> cls = myType.getRawClass();
                if (LocalDateTime.class.isAssignableFrom(cls)) {
                    return createSchema("2021-01-14T08:25:36", "ISO8601 formatted timestamp, assumed in UTC", "yyyy-MM-ddThh:mm:ss");
                }
                if (LocalDate.class.isAssignableFrom(cls)) {
                    return createSchema("2021-01-14", "ISO8601 formatted date", "yyyy-MM-dd");
                }
                if (LocalTime.class.isAssignableFrom(cls)) {
                    return createSchema("08:25:36", "ISO8601 formatted local time", "hh:mm:ss");
                }
                if (UUID.class.isAssignableFrom(cls)) {
                    return createSchema("550e8400-e29b-11d4-a716-446655440000",
                      "36 character string, representing a UUID", "hhhhhhhh-hhh-hhhh-hhhh-hhhhhhhhhhhh");
                }
                if (cls.getCanonicalName().startsWith("de.jpaw.fixedpoint")) { // MicroUnits eta are not a required dependency
                    if (cls.getCanonicalName().endsWith("MicroUnits")) {
                        return createFixedPointSchema(6);
                    }
                    if (cls.getCanonicalName().endsWith("MilliUnits")) {
                        return createFixedPointSchema(3);
                    }
                    if (cls.getCanonicalName().endsWith("NanoUnits")) {
                        return createFixedPointSchema(9);
                    }
                }
                if (Instant.class.isAssignableFrom(cls)) {
                    return createSchema("2021-01-14T08:25:36Z",
                        "Instant in UNIX time (seconds since Jan 1st, 1970, 00:00:00 UTC). Example is morning of January 14th, 2021", "yyyy-MM-ddThh:mm:ssZ");
                }
            }
        }
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }

    /** Creates a schema for a fixed point type. */
    private NumberSchema createFixedPointSchema(int decimals) {
        final NumberSchema ns = new NumberSchema();
        ns.setExample("3.14");
        ns.setDescription("Numeric value with at most " + decimals + " fractional digits");
        return ns;
    }

    private StringSchema createSchema(final String example, final String description, final String format) {
        final StringSchema ss = new StringSchema();
        ss.setExample(example);
        ss.setDescription(description);
        if (format != null) {
            ss.setFormat(format);
        }
        return ss;
    }
}
