/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.jackson;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.AbstractFormatGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * Implementation of {@linkplain ITextDataGenerator} which generates output data in JSON format.
 * The current implementation utilises Jackson library for JSON generation.
 */
@Dependent
@Named("JSONJackson")
public class JSONJacksonDataGenerator extends AbstractFormatGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONJacksonDataGenerator.class);
    protected OutputStreamWriter osw = null;
    protected ObjectMapper objectMapper = null;

    @Override
    protected void openHook() throws IOException {
        super.openHook();
        final boolean useNulls = Boolean.TRUE.equals(sinkCfg.getJsonWriteNulls());
        osw = new OutputStreamWriter(outputResource.getOutputStream(), encoding);
        objectMapper = JacksonTools.createObjectMapper(useNulls, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable object) {

        try {
            objectMapper.writer().writeValue(osw, object);
        } catch (final Exception ex) {
            LOGGER.error("Failed to generate JSON data for output", ex);
            throw new T9tException(T9tIOException.OUTPUT_JSON_EXCEPTION, "Failed to generate JSON data");
        }
    }
}
