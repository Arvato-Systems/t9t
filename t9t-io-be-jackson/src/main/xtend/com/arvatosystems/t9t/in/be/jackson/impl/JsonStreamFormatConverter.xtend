/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.jackson.impl

import com.fasterxml.jackson.core.JsonToken
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import java.io.InputStream
import de.jpaw.bonaparte.core.MessageParserException
import com.arvatosystems.t9t.in.be.jackson.AbstractJsonFormatConverter

@AddLogger
@Dependent
@Named("JSON") // generic JSON reader
class JsonStreamFormatConverter extends AbstractJsonFormatConverter {

    override process(InputStream is) {
        val baseClass = baseBClass.bonaPortableClass
        val parser = factory.createParser(is)
        val recordName = cfg.xmlRecordName // will be in this case the records field like {'tenantId': "X", 'records':[{bonaparte object}, {bona...}]}
        var JsonToken current;
        current = parser.nextToken

        if (recordName === null) {  // no recordname: expect an array at outer level, no additional fields at main object level (simple structure)
            if (current !== JsonToken.START_ARRAY) {
                LOGGER.error("Json did not start with a start token! (expected [)")
                throw new MessageParserException(MessageParserException.BAD_TRANSMISSION_START)
            }
            while (parser.nextToken() !== JsonToken.END_ARRAY) { // parse until the end of the object
                val node = parser.readValueAs(baseClass) // now parse into the bonaparte object with the objectmapper
                inputSession.process(node) // process it in the subsequent transformer etc.
            }
        } else {
            if (current !== JsonToken.START_OBJECT) {
                LOGGER.error("Json did not start with a start token! (expected {)")
                throw new MessageParserException(MessageParserException.BAD_TRANSMISSION_START)
            }

            while (parser.nextToken() !== JsonToken.END_OBJECT) { // parse until the end of the object
                val fieldName = parser.getCurrentName()
                current = parser.nextToken();
                if (fieldName.equals(recordName)) { // search for the records field
                    if (current == JsonToken.START_ARRAY) { // we expect this to be an array
                        // For each of the records in the array
                        while (parser.nextToken() !== JsonToken.END_ARRAY) {
                            val node = parser.readValueAs(baseClass) // now parse into the bonaparte object with the objectmapper
                            inputSession.process(node) // process it in the subsequent transformer etc.
                        }
                    } else {
                        LOGGER.warn("Error: records should be an array: skipping.");
                        parser.skipChildren();
                    }
                } else {
                    inputSession.setHeaderData(fieldName, parser.text)
                }
            }
        }
    }
}
