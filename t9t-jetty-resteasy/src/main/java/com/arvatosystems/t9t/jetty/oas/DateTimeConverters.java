/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class DateTimeConverters implements ModelConverter {

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (type.isSchemaProperty()) {
            JavaType _type = Json.mapper().constructType(type.getType());
            if (_type != null) {
                Class<?> cls = _type.getRawClass();
                if (LocalDateTime.class.isAssignableFrom(cls)) {
                    return createSchema("2021-01-14T08:25:36", "ISO8601 formatted timestamp, assumed in UTC", "YYYY-MM-ddThh:mm:ss");
                }
                if (LocalDate.class.isAssignableFrom(cls)) {
                    return createSchema("2021-01-14", "ISO8601 formatted date", "YYYY-MM-dd");
                }
                if (LocalTime.class.isAssignableFrom(cls)) {
                    return createSchema("08:25:36", "ISO8601 formatted local time", "hh:mm:ss");
                }
                if (UUID.class.isAssignableFrom(cls)) {
                    return createSchema("550e8400-e29b-11d4-a716-446655440000", "36 character string, representing a UUID", "hhhhhhhh-hhh-hhhh-hhhh-hhhhhhhhhhhh");
                }
                if (Instant.class.isAssignableFrom(cls)) {
                    final IntegerSchema is = new IntegerSchema();
                    is.setExample("1610612736");
                    is.setDescription("Instant in UNIX time (seconds since 1.1.1970). Example is morning of January 14th, 2021");
                    return new IntegerSchema();
                }
            }
        }
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }

    private StringSchema createSchema(String example, String description, String format) {
        final StringSchema ss = new StringSchema();
        ss.setExample(example);
        ss.setDescription(description);
        if (format != null) {
            ss.setFormat(format);
        }
        return ss;
    }
}
