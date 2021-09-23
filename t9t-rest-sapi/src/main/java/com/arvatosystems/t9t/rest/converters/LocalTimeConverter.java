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

import java.time.LocalTime;

import javax.ws.rs.ext.ParamConverter;

/**
 * JAX-RS parameter converter for LocalTime. This is unfortunately still required, JAX-RS seems to still live in the Java 1.6 world.
 */
public class LocalTimeConverter implements ParamConverter<LocalTime> {

    @Override
    public LocalTime fromString(String value) {
        if (value == null)
            return null;
        return LocalTime.parse(value);
    }

    @Override
    public String toString(LocalTime value) {
        if (value == null)
            return null;
        return value.toString();
    }
}
