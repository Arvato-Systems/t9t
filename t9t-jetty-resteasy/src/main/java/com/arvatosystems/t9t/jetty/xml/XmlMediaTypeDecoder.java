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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arvatosystems.t9t.jetty.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
@Consumes("application/xml")
@Singleton
public class XmlMediaTypeDecoder implements MessageBodyReader<BonaPortable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMediaTypeDecoder.class);
    private final IStandardNamespaceWriter jaxbContextProvider = Jdp.getRequired(IStandardNamespaceWriter.class);

    @Override
    public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        //LOGGER.info("XmlMediaType decoder called with type {} and class {}", mt.toString(), type.toGenericString());
        return (type.isAnnotationPresent(XmlRootElement.class)) && mt.equals(MediaType.APPLICATION_XML_TYPE);

    }

    @Override
    public BonaPortable readFrom(Class<BonaPortable> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, String> mm, InputStream in) throws IOException, WebApplicationException {
        //LOGGER.info("Trying to parse type {} and class {}", mt.toString(), type.toGenericString());
        try {
            Unmarshaller unmarshaller = jaxbContextProvider.getStandardJAXBContext().createUnmarshaller();
            BonaPortable bonaportable = (BonaPortable) unmarshaller.unmarshal(in);
            return bonaportable;
        } catch (JAXBException | ClassCastException e) {
            LOGGER.error("There has been an error while unmarshalling object in XmlMediaTypeDecoder {}", e);
            return null;
        }
    }

}
