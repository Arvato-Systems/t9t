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
package com.arvatosystems.t9t.rest.converters;

import java.util.UUID;

import jakarta.ws.rs.ext.ParamConverter;

/**
 * JAX-RS parameter converter for UUID. This is unfortunately still required.
 */
public class UuidConverter implements ParamConverter<UUID> {

    @Override
    public UUID fromString(final String value) {
        if (value == null)
            return null;
        return UUID.fromString(value);
    }

    @Override
    public String toString(final UUID value) {
        if (value == null)
            return null;
        return value.toString();
    }
}
