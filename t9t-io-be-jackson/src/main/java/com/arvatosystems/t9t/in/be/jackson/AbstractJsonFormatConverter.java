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
package com.arvatosystems.t9t.in.be.jackson;

import java.util.Map;

import com.arvatosystems.t9t.in.be.impl.AbstractInputFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.core.BonaPortableClass;

public abstract class AbstractJsonFormatConverter extends AbstractInputFormatConverter {
    protected ObjectMapper objectMapper;

    @Override
    public void open(final IInputSession inputSession, final Map<String, Object> params, final BonaPortableClass<?> baseBClass) {
        super.open(inputSession, params, baseBClass);
        objectMapper = JacksonTools.createObjectMapper();
    }
}
