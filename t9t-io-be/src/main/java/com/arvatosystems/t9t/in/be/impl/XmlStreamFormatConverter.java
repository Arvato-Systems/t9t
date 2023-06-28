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
package com.arvatosystems.t9t.in.be.impl;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ExceptionUtil;
import jakarta.xml.bind.JAXBElement;

@Dependent
@Named("XML") // generic XML reader
public class XmlStreamFormatConverter extends AbstractXmlFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlStreamFormatConverter.class);

    @Override
    public void process(final InputStream xml) {
        try {
            String currentData = null;
            String currentTag = "NONE";
            final Class<? extends BonaPortable> elementClass = this.baseBClass.getBonaPortableClass();
            final XMLInputFactory staxFactory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = staxFactory.createXMLStreamReader(xml);
            final DataSinkDTO cfg = inputSession.getDataSinkDTO();
            int event = reader.hasNext() ? reader.nextTag() : XMLStreamConstants.END_DOCUMENT;

            while (event != XMLStreamConstants.END_DOCUMENT) {
                LOGGER.trace("Parsing XML stream event {}", event);
                switch (event) {
                case XMLStreamConstants.CHARACTERS:
                    if (currentTag != null) {
                        currentData = reader.getText().trim();
                        if (currentData.length() == 0) {
                            currentData = null;
                        }
                        LOGGER.trace("read text for tag {} is {}", currentTag, currentData);
                    }
                    event = reader.next();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    final String name = reader.getLocalName();

                    if (!name.equals(cfg.getXmlRecordName())) {
                        LOGGER.debug("XML END {}: {}", name, currentData);
                        assignTag(name, currentData);
                    }

                    event = reader.next();
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    currentData = null;
                    currentTag  = reader.getLocalName();
                    LOGGER.trace("Parsing START for tag {}, wanting {}", currentTag, cfg.getXmlRecordName());
                    if (currentTag.equals(cfg.getXmlRecordName())) {
                        // use JAXB to unmarshal
                        try {
                            final JAXBElement<? extends BonaPortable> element = m.unmarshal(reader, elementClass);
                            if (element != null) {
                                final BonaPortable value = element.getValue();
                                if (value != null) {
                                    inputSession.process(value);
                                }
                            } else {
                                LOGGER.warn("Parsed element is NULL, cannot use JAXB on it");
                            }
                        } catch (final Exception e) {
                            LOGGER.error("JAXB START tag parsing failed with {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                            throw e;
                        }
                        // see https://docs.oracle.com/javase/8/docs/api/jakarta.xml.bind/Unmarshaller.html#unmarshal-javax.xml.stream.XMLStreamReader-
                        // see https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/XMLStreamReader.html#getEventType--
                        event = reader.getEventType();  // cursor is already pointing at next event after unmarshal()
                    } else {
                        LOGGER.debug("XML START {}", currentTag);
                        event = reader.next();
                    }
                    break;
                default:
                    LOGGER.trace("Skipping event: {}", event);
                    event = reader.next();
                    break;
                }
            }

            reader.close();
        } catch (final XMLStreamException e) {
            LOGGER.error("XMLStreamException: " + e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR);
        } catch (final Exception e) {
            LOGGER.error("Exception: " + e.getMessage(), e);
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        }
    }

    // to be overridden if required. The default implementation stores all header elements as Strings
    protected void assignTag(final String name, final String data) {
        inputSession.setHeaderData(name, data);
    }
}
