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
package com.arvatosystems.t9t.rest.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.xml.namespaces.IStandardNamespaceWriter;

/**
 * Decoder to parse requests in XML format (using JAXB / jakarta.xml.bind).
 */
@Provider
@Consumes("application/xml")
@Singleton
public class XmlMediaTypeDecoder implements MessageBodyReader<BonaPortable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMediaTypeDecoder.class);
    private final IStandardNamespaceWriter jaxbContextProvider = Jdp.getRequired(IStandardNamespaceWriter.class);

    @Override
    public boolean isReadable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        //LOGGER.info("XmlMediaType decoder called with type {} and class {}", mt.toString(), type.toGenericString());
        return (type.isAnnotationPresent(XmlRootElement.class)) && mt.equals(MediaType.APPLICATION_XML_TYPE);

    }

    @Override
    public BonaPortable readFrom(final Class<BonaPortable> type, final Type type1, final Annotation[] antns, final MediaType mt,
      final MultivaluedMap<String, String> mm, final InputStream in) throws IOException, WebApplicationException {
        //LOGGER.info("Trying to parse type {} and class {}", mt.toString(), type.toGenericString());
        try {
            final XMLInputFactory secureXmlInputFactory = XMLInputFactory.newInstance();
            secureXmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            secureXmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            final XMLStreamReader xsr = secureXmlInputFactory.createXMLStreamReader(in);
            try {
                final Unmarshaller unmarshaller = jaxbContextProvider.getStandardJAXBContext().createUnmarshaller();
                return (BonaPortable) unmarshaller.unmarshal(xsr);
            } finally {
                xsr.close();
            }
        } catch (JAXBException | ClassCastException | XMLStreamException e) {
            LOGGER.error("There has been an error while unmarshalling object in XmlMediaTypeDecoder", e);
            throw new WebApplicationException("Invalid XML input", Response.Status.BAD_REQUEST);
        }
    }
}
