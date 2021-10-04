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
package com.arvatosystems.t9t.jetty.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 *
 * @author LUEC034
 */
@Provider
@Produces("application/xml")
@Singleton
public class XmlMediaTypeEncoder implements MessageBodyWriter<BonaPortable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMediaTypeEncoder.class);
    private final IStandardNamespaceWriter jaxbContextProvider = Jdp.getRequired(IStandardNamespaceWriter.class);

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return (mt.equals(MediaType.APPLICATION_XML_TYPE) && (type.isAnnotationPresent(XmlRootElement.class)));
    }

    @Override
    public long getSize(BonaPortable t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return 0;
    }

    @Override
    public void writeTo(BonaPortable t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        try {
            Marshaller marshaller = jaxbContextProvider.getStandardJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(t, out);
        } catch (JAXBException e) {
            LOGGER.error("There has been an error while marshalling the object {} with exception {}", t, e);
        }
    }
}
