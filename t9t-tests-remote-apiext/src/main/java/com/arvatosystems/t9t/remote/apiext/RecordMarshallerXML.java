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
package com.arvatosystems.t9t.remote.apiext;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.xml.GenericResult;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class RecordMarshallerXML implements IMarshaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordMarshallerXML.class);

    private final JAXBContext context;
    private final Boolean formattedOutput;
    private final boolean contextForAllClasses;

    public RecordMarshallerXML(final JAXBContext context, final boolean formattedOutput, final boolean contextForAllClasses) {
        this.context = context;
        this.formattedOutput = Boolean.valueOf(formattedOutput);
        this.contextForAllClasses = contextForAllClasses;
    }

    @Override
    public String getContentType() {
        return "application/xml";
    }

    @Override
    public ByteArray marshal(final BonaPortable obj) throws Exception {
        final JAXBContext specialContext = contextForAllClasses ? context : JAXBContext.newInstance(obj.getClass());
        final Marshaller marshaller = specialContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        marshaller.marshal(obj, os);
        return ByteArray.fromByteArrayOutputStream(os);
    }

    @Override
    public BonaPortable unmarshal(final ByteBuilder buffer) throws Exception {
        return unmarshal(buffer, BonaPortable.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BonaPortable> T unmarshal(final ByteBuilder buffer, final Class<T> resultClass) {
        try {
            final JAXBContext specialContext = contextForAllClasses || resultClass.equals(GenericResult.class) ? context : JAXBContext.newInstance(resultClass);
            return (T) specialContext.createUnmarshaller().unmarshal(buffer.asByteArrayInputStream());
        } catch (final JAXBException e) {
            LOGGER.error("JAXB exception unmarshalling", e);
            throw new RuntimeException(e);
        }
    }
}
