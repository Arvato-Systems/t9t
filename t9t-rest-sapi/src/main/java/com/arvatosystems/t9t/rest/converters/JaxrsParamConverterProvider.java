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
package com.arvatosystems.t9t.rest.converters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.fixedpoint.types.MicroUnits;

@Provider
public class JaxrsParamConverterProvider implements ParamConverterProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsParamConverterProvider.class);
    // ensure we instantiate the converters only once
    private final ParamConverter<LocalDate>     localDateConverter     = new LocalDateConverter();
    private final ParamConverter<LocalDateTime> localDateTimeConverter = new LocalDateTimeConverter();
    private final ParamConverter<LocalTime>     localTimeConverter     = new LocalTimeConverter();
    private final ParamConverter<UUID>          uuidConverter          = new UuidConverter();
    private final ParamConverter<MicroUnits>    microUnitsConverter    = new MicroUnitsConverter();

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) {
        LOGGER.debug("Requesting converter for class {}", rawType.getCanonicalName());

        if (rawType.equals(LocalDate.class))
            return (ParamConverter) localDateConverter;
        if (rawType.equals(LocalDateTime.class))
            return (ParamConverter) localDateTimeConverter;
        if (rawType.equals(LocalTime.class))
            return (ParamConverter) localTimeConverter;
        if (rawType.equals(UUID.class))
            return (ParamConverter) uuidConverter;
        if (rawType.equals(MicroUnits.class))
            return (ParamConverter) microUnitsConverter;
        return null;
    }
}
