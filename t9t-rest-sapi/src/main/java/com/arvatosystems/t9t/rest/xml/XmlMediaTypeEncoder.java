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
package com.arvatosystems.t9t.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.xml.namespaces.IStandardNamespaceWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Encoder to send responses in XML (using JAXB / jakarta.xml.bind).
 */
@Provider
@Produces("application/xml")
@Singleton
public class XmlMediaTypeEncoder implements MessageBodyWriter<BonaPortable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMediaTypeEncoder.class);
    private final IStandardNamespaceWriter jaxbContextProvider = Jdp.getRequired(IStandardNamespaceWriter.class);

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return (mt.equals(MediaType.APPLICATION_XML_TYPE) && (type.isAnnotationPresent(XmlRootElement.class)));
    }

    @Override
    public long getSize(final BonaPortable t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return 0;
    }

    @Override
    public void writeTo(final BonaPortable t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
      final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final Marshaller marshaller = jaxbContextProvider.getStandardJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(t, out);
        } catch (final JAXBException e) {
            LOGGER.error("There has been an error while marshalling the object {} with exception {}", t, e);
        }
    }
}
