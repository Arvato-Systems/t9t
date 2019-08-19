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
package com.arvatosystems.t9t.out.be.impl.output;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;
import com.arvatosystems.t9t.out.services.IMarshallerExt;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;
import de.jpaw.util.ByteUtil;
import de.jpaw.util.ExceptionUtil;

public abstract class AbstractXMLMarshallerExt<R> implements IMarshallerExt<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXMLMarshallerExt.class);
    private final IStandardNamespaceWriter jaxbContextProvider = Jdp.getRequired(IStandardNamespaceWriter.class);
    protected final JAXBContext unmarshalContext;

    protected AbstractXMLMarshallerExt() {
        unmarshalContext = null;
    }

    protected AbstractXMLMarshallerExt(Class<?> responseClass) {
        try {
            unmarshalContext = JAXBContext.newInstance(responseClass);
        } catch (JAXBException e) {
            LOGGER.error("JAXB error: cannot set response class to {}: {}", responseClass.getCanonicalName(), ExceptionUtil.causeChain(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public ByteArray marshal(BonaPortable obj) throws Exception {
        Marshaller marshaller = jaxbContextProvider.getStandardJAXBContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        marshaller.marshal(obj, os);
        return ByteArray.fromByteArrayOutputStream(os);
    }

    @Override
    public BonaPortable unmarshal(ByteBuilder buffer) throws Exception {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Received data\n{}\n", ByteUtil.dump(buffer.getBytes(), 64));
        }
        if (unmarshalContext == null)
            return null;   // cannot parse without a target object in XML
        try {
            Unmarshaller unmarshaller = unmarshalContext.createUnmarshaller();
            return (BonaPortable)unmarshaller.unmarshal(buffer.asByteArrayInputStream());
        } catch (Exception e ) {
            LOGGER.error("Could not unmarshal message due to {}. Message was <{}>", ExceptionUtil.causeChain(e), buffer.toString());
            throw e;
        }
    }
}
