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
package com.arvatosystems.t9t.in.be.impl

import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import java.io.InputStream
import javax.xml.bind.JAXBElement
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import de.jpaw.util.ExceptionUtil

@AddLogger
@Dependent
@Named("XML")  // generic XML reader
class XmlStreamFormatConverter extends AbstractXmlFormatConverter {

    // to be overridden if required. The default implementation stores all header elements as Strings
    def protected assignTag(String name, String data) {
        inputSession.setHeaderData(name, data)
        // LOGGER.debug("Parsed tag {}: {}", name, data)
    }

    override void process(InputStream xml) {
        var String currentData = null
        var String currentTag  = "NONE"
        val elementClass       = baseBClass.bonaPortableClass
        val staxFactory        = XMLInputFactory.newInstance();
        val reader             = staxFactory.createXMLStreamReader(xml)
        var int event          = if (reader.hasNext()) reader.nextTag() else XMLStreamConstants.END_DOCUMENT

        while (event != XMLStreamConstants.END_DOCUMENT) {
            LOGGER.trace("Parsing XML stream event {}", event)
            switch (event) {
            case XMLStreamConstants.CHARACTERS: {
                if (currentTag !== null) {
                    currentData = reader.text.trim
                    if (currentData.length == 0)
                        currentData = null
                    LOGGER.trace("read text for tag {} is {}", currentTag, currentData)
                }
                event = reader.next();
            }
            case XMLStreamConstants.END_ELEMENT: {
                val name = reader.localName
                // println('''Found END «name»''')
                if (name == cfg.xmlRecordName) {
                    // already done
                } else {
                    LOGGER.debug("XML END {}: {}", name, currentData)
                    assignTag(name, currentData)
                }
                // currentTag = null
                event = reader.next();
            }
            case XMLStreamConstants.START_ELEMENT: {
                currentData = null
                currentTag  = reader.localName
                LOGGER.trace("Parsing START for tag {}, wanting {}", currentTag, cfg.xmlRecordName)
                if (currentTag == cfg.xmlRecordName) {
                    // use JAXB to unmarshal
                    try {
                        val element = m.unmarshal(reader, elementClass);
                        if (element instanceof JAXBElement) {
                            val value = element.getValue();
                            if (value !== null) {
                                inputSession.process(value);
                            }
                        } else {
                            LOGGER.debug("Parsed element is of type {}, cannot use JAXB on it", element?.class?.canonicalName ?: "NULL")
                        }
                    } catch (Exception e) {
                        LOGGER.error("JAXB START tag parsing failed with {}: {}", e.message, ExceptionUtil.causeChain(e))
                        throw e
                    }
                    // see https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/Unmarshaller.html#unmarshal-javax.xml.stream.XMLStreamReader-
                    // see https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/XMLStreamReader.html#getEventType--
                    event = reader.getEventType();  // cursor is already pointing at next event after unmarshal()
                } else {
                    LOGGER.debug("XML START {}", currentTag)
                    event = reader.next();
                }
            }
            default: {
                LOGGER.trace("Skipping event: {}", event)
                event = reader.next();
            }
            }
        }
        reader.close
    }
}
