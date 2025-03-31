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
package com.arvatosystems.t9t.in.be.jackson;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ExceptionUtil;

@Dependent
@Named("JSONJackson") // generic JSON reader
public class JsonStreamFormatConverter extends AbstractJsonFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonStreamFormatConverter.class);

    @Override
    public void process(final InputStream is) {
        try {
            final Class<? extends BonaPortable> baseClass = this.baseBClass.getBonaPortableClass();
            final JsonParser parser = objectMapper.getFactory().createParser(is);
            final String recordName = inputSession.getDataSinkDTO().getXmlRecordName(); // will be in this case the records field like
                // {'tenantId': "X", 'records':[{bonaparte object}, {bona...}]}
            JsonToken current = parser.nextToken();

            if (recordName == null) {  // no recordname: expect an array at outer level, no additional fields at main object level (simple structure)
                if (current != JsonToken.START_ARRAY) {
                    LOGGER.error("Json did not start with a start token! (expected [)");
                    throw new MessageParserException(MessageParserException.BAD_TRANSMISSION_START);
                }
                while (parser.nextToken() != JsonToken.END_ARRAY) { // parse until the end of the object
                    final BonaPortable node = parser.readValueAs(baseClass); // now parse into the bonaparte object with the objectmapper
                    inputSession.process(node); // process it in the subsequent transformer etc.
                }
            } else {
                if (current != JsonToken.START_OBJECT) {
                    LOGGER.error("Json did not start with a start token! (expected {)");
                    throw new MessageParserException(MessageParserException.BAD_TRANSMISSION_START);
                }

                while (parser.nextToken() != JsonToken.END_OBJECT) { // parse until the end of the object
                    final String fieldName = parser.getCurrentName();
                    current = parser.nextToken();
                    if (fieldName.equals(recordName)) { // search for the records field
                        if (current == JsonToken.START_ARRAY) { // we expect this to be an array
                            // For each of the records in the array
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                final BonaPortable node = parser.readValueAs(baseClass); // now parse into the bonaparte object with the objectmapper
                                inputSession.process(node); // process it in the subsequent transformer etc.
                            }
                        } else {
                            LOGGER.warn("Error: records should be an array: skipping.");
                            parser.skipChildren();
                        }
                    } else {
                        inputSession.setHeaderData(fieldName, parser.getText());
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.error("Jackson JSON exception for data sink {}: {}", inputSession.getDataSinkDTO().getDataSinkId(), ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.IO_EXCEPTION, e.getMessage());
        }
    }
}
